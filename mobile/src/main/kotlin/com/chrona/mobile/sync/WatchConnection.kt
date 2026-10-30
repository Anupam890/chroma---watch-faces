package com.chrona.mobile.sync

import android.util.Log
import com.chrona.mobile.data.FaceRepository
import com.chrona.shared.ChronaJson
import com.chrona.shared.DataLayerContract
import com.chrona.shared.InstallResult
import com.chrona.shared.TransferManifest
import com.chrona.shared.WatchFaceItem
import com.chrona.shared.WatchState
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.PutDataMapRequest
import com.chrona.shared.UserPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phone-side orchestration of everything that crosses the Data Layer.
 *
 *  - Capability discovery → find the Chrona watch node
 *  - DataClient            → observe [WatchState] published by the watch
 *  - MessageClient         → commands (install/remove/set-active) + install results
 *  - ChannelClient         → stream the APK bytes
 */
@Singleton
class WatchConnection @Inject constructor(
    private val messageClient: MessageClient,
    private val channelClient: ChannelClient,
    private val dataClient: DataClient,
    private val capabilityClient: CapabilityClient,
    private val faceRepository: FaceRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _watchNode = MutableStateFlow<Node?>(null)
    val watchNode: StateFlow<Node?> = _watchNode.asStateFlow()

    private val _watchState = MutableStateFlow<WatchState?>(null)
    val watchState: StateFlow<WatchState?> = _watchState.asStateFlow()

    private val _installProgress = MutableStateFlow<Map<String, InstallProgress>>(emptyMap())
    val installProgress: StateFlow<Map<String, InstallProgress>> = _installProgress.asStateFlow()

    private val _events = MutableSharedFlow<SyncEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<SyncEvent> = _events.asSharedFlow()

    init {
        scope.launch { refreshNode() }
        scope.launch { loadCachedWatchState() }
        capabilityClient.addListener({ info -> _watchNode.value = info.nodes.pickBest() }, DataLayerContract.CAPABILITY_WATCH)
    }

    suspend fun refreshNode() {
        runCatching {
            val info = capabilityClient.getCapability(DataLayerContract.CAPABILITY_WATCH, CapabilityClient.FILTER_REACHABLE).await()
            _watchNode.value = info.nodes.pickBest()
            _watchNode.value?.let { messageClient.sendMessage(it.id, DataLayerContract.Messages.PING, ByteArray(0)).await() }
        }.onFailure { Log.w(TAG, "capability lookup failed", it) }
    }

    private suspend fun loadCachedWatchState() {
        runCatching {
            val items = dataClient.getDataItems(android.net.Uri.parse("wear://*${DataLayerContract.DataItems.WATCH_STATE}")).await()
            items.firstOrNull()?.let { onWatchStateItem(DataMapItem.fromDataItem(it).dataMap.getString(DataLayerContract.DataItems.KEY_JSON)) }
            items.release()
        }
    }

    /** Called by [PhoneDataLayerListenerService]. */
    fun onWatchStateItem(json: String?) {
        json ?: return
        runCatching { ChronaJson.decodeFromString(WatchState.serializer(), json) }
            .onSuccess { _watchState.value = it }
            .onFailure { Log.w(TAG, "bad WatchState payload", it) }
    }

    /** Called by [PhoneDataLayerListenerService]. */
    fun onInstallResult(json: String) {
        val result = runCatching { ChronaJson.decodeFromString(InstallResult.serializer(), json) }.getOrNull() ?: return
        _installProgress.value = _installProgress.value - result.faceId
        scope.launch { _events.emit(if (result.success) SyncEvent.Installed(result.faceId) else SyncEvent.Failed(result.faceId, result.errorMessage ?: result.errorCode ?: "Unknown error")) }
    }

    // ---------------------------------------------------------------- commands

    suspend fun install(face: WatchFaceItem, setActive: Boolean, replaceSlotId: String? = null) {
        val node = _watchNode.value ?: run { _events.emit(SyncEvent.Failed(face.id, "No watch connected")); return }
        val state = _watchState.value
        if (state != null && !state.supportsWatchFacePush) {
            _events.emit(SyncEvent.Failed(face.id, "This watch runs ${state.wearOsLabel}. Direct install needs Wear OS 6 — install from Google Play instead."))
            return
        }
        if (state != null && face.wffVersion.minApi > state.apiLevel) {
            _events.emit(SyncEvent.Failed(face.id, "${face.name} needs ${face.wffVersion.minWearOs} or newer."))
            return
        }
        setProgress(face.id, InstallProgress.Preparing)
        try {
            val apk = faceRepository.prepareApk(face)
            val manifest = TransferManifest(
                faceId = face.id,
                packageName = face.pushPackageName(APP_PACKAGE),
                sizeBytes = apk.file.length(),
                sha256 = apk.sha256,
                validationToken = face.validationToken,
                setActiveAfterInstall = setActive,
                replaceSlotId = replaceSlotId
            )
            messageClient.sendMessage(node.id, DataLayerContract.Messages.INSTALL_REQUEST, ChronaJson.encodeToString(TransferManifest.serializer(), manifest).toByteArray()).await()

            setProgress(face.id, InstallProgress.Transferring(0f))
            val channel = channelClient.openChannel(node.id, DataLayerContract.Channels.APK_TRANSFER).await()
            val out: OutputStream = channelClient.getOutputStream(channel).await()
            withContext(Dispatchers.IO) {
                out.use { os ->
                    apk.file.inputStream().use { input ->
                        val total = apk.file.length().toFloat()
                        var sent = 0L
                        val buf = ByteArray(32 * 1024)
                        var n: Int
                        while (input.read(buf).also { n = it } > 0) {
                            os.write(buf, 0, n); sent += n
                            setProgress(face.id, InstallProgress.Transferring(sent / total))
                        }
                        os.flush()
                    }
                }
            }
            channelClient.close(channel).await()
            setProgress(face.id, InstallProgress.InstallingOnWatch)
            // Watch replies via INSTALL_RESULT; time out defensively.
            val completed = withTimeoutOrNull(INSTALL_TIMEOUT_MS) {
                while (_installProgress.value.containsKey(face.id)) kotlinx.coroutines.delay(250)
                true
            }
            if (completed == null) {
                _installProgress.value = _installProgress.value - face.id
                _events.emit(SyncEvent.Failed(face.id, "Watch did not confirm install. Check the watch screen."))
            }
        } catch (t: Throwable) {
            Log.e(TAG, "install failed", t)
            _installProgress.value = _installProgress.value - face.id
            _events.emit(SyncEvent.Failed(face.id, t.message ?: "Transfer failed"))
        }
    }

    suspend fun remove(slotId: String) = sendSimple(DataLayerContract.Messages.REMOVE_REQUEST, slotId)
    suspend fun setActive(slotId: String) = sendSimple(DataLayerContract.Messages.SET_ACTIVE_REQUEST, slotId)
    suspend fun openWatchApp() = sendSimple(DataLayerContract.Messages.OPEN_APP, "")

    suspend fun syncPrefs(prefs: UserPrefs) {
        runCatching {
            val req = PutDataMapRequest.create(DataLayerContract.DataItems.USER_PREFS).apply {
                dataMap.putString(DataLayerContract.DataItems.KEY_JSON, ChronaJson.encodeToString(UserPrefs.serializer(), prefs))
                dataMap.putLong(DataLayerContract.DataItems.KEY_TIMESTAMP, System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()
            dataClient.putDataItem(req).await()
        }
    }

    private suspend fun sendSimple(path: String, payload: String) {
        val node = _watchNode.value ?: return
        runCatching { messageClient.sendMessage(node.id, path, payload.toByteArray()).await() }
            .onFailure { Log.w(TAG, "message $path failed", it) }
    }

    private fun setProgress(id: String, p: InstallProgress) { _installProgress.value = _installProgress.value + (id to p) }

    private fun Set<Node>.pickBest(): Node? = firstOrNull { it.isNearby } ?: firstOrNull()

    sealed interface InstallProgress {
        data object Preparing : InstallProgress
        data class Transferring(val fraction: Float) : InstallProgress
        data object InstallingOnWatch : InstallProgress
    }

    sealed interface SyncEvent {
        data class Installed(val faceId: String) : SyncEvent
        data class Failed(val faceId: String, val reason: String) : SyncEvent
    }

    companion object {
        private const val TAG = "WatchConnection"
        const val APP_PACKAGE = "com.chrona"
        private const val INSTALL_TIMEOUT_MS = 90_000L
    }
}
