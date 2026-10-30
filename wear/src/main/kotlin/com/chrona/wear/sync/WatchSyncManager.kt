package com.chrona.wear.sync

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import com.chrona.shared.ChronaJson
import com.chrona.shared.DataLayerContract
import com.chrona.shared.InstallResult
import com.chrona.shared.TransferManifest
import com.chrona.shared.UserPrefs
import com.chrona.shared.WatchState
import com.chrona.shared.apiToWearOsLabel
import com.chrona.wear.push.WatchFaceInstaller
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val installer: WatchFaceInstaller,
    private val messageClient: MessageClient,
    private val channelClient: ChannelClient,
    private val dataClient: DataClient,
    private val nodeClient: NodeClient
) {
    private val pending = ConcurrentHashMap<String, Pair<TransferManifest, String>>() // path -> (manifest, phoneNodeId)
    private var lastPhoneNode: String? = null

    private val _status = MutableStateFlow<Status>(Status.Idle)
    val status: StateFlow<Status> = _status.asStateFlow()

    private val _prefs = MutableStateFlow(UserPrefs())
    val prefs: StateFlow<UserPrefs> = _prefs.asStateFlow()

    sealed interface Status {
        data object Idle : Status
        data class Receiving(val faceId: String, val fraction: Float) : Status
        data class Installing(val faceId: String) : Status
        data class Done(val faceId: String, val ok: Boolean, val message: String) : Status
    }

    fun expectTransfer(m: TransferManifest, phoneNode: String) {
        pending[DataLayerContract.Channels.APK_TRANSFER] = m to phoneNode
        lastPhoneNode = phoneNode
        _status.value = Status.Receiving(m.faceId, 0f)
    }

    suspend fun receiveApk(channel: ChannelClient.Channel) {
        val (manifest, phoneNode) = pending.remove(channel.path) ?: run {
            Log.w(TAG, "channel opened without manifest; rejecting"); channelClient.close(channel); return
        }
        val target = File(context.cacheDir, "incoming/${manifest.faceId}.apk").apply { parentFile?.mkdirs() }
        try {
            val input = channelClient.getInputStream(channel).await()
            withContext(Dispatchers.IO) {
                val digest = MessageDigest.getInstance("SHA-256")
                input.use { ins ->
                    target.outputStream().use { out ->
                        val buf = ByteArray(32 * 1024); var n: Int; var got = 0L
                        while (ins.read(buf).also { n = it } > 0) {
                            out.write(buf, 0, n); digest.update(buf, 0, n); got += n
                            _status.value = Status.Receiving(manifest.faceId, got.toFloat() / manifest.sizeBytes)
                        }
                    }
                }
                val sha = digest.digest().joinToString("") { "%02x".format(it) }
                if (sha != manifest.sha256) throw IllegalStateException("Checksum mismatch — transfer corrupted")
            }
            _status.value = Status.Installing(manifest.faceId)
            when (val r = installer.install(target, manifest)) {
                is WatchFaceInstaller.Outcome.Success -> report(phoneNode, InstallResult(manifest.faceId, true, r.slotId), "Installed")
                is WatchFaceInstaller.Outcome.Failure -> report(phoneNode, InstallResult(manifest.faceId, false, errorCode = r.code, errorMessage = r.message), r.message)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "receive failed", t)
            target.delete()
            report(phoneNode, InstallResult(manifest.faceId, false, errorCode = "TRANSFER", errorMessage = t.message), t.message ?: "Transfer failed")
        } finally {
            runCatching { channelClient.close(channel).await() }
            publishState()
        }
    }

    private suspend fun report(node: String, result: InstallResult, msg: String) {
        _status.value = Status.Done(result.faceId, result.success, msg)
        runCatching { messageClient.sendMessage(node, DataLayerContract.Messages.INSTALL_RESULT, ChronaJson.encodeToString(InstallResult.serializer(), result).toByteArray()).await() }
    }

    suspend fun remove(slotId: String) { installer.remove(slotId); publishState() }
    suspend fun setActive(slotId: String) { installer.setActive(slotId); publishState() }

    fun onPrefs(json: String?) { json?.let { runCatching { ChronaJson.decodeFromString(UserPrefs.serializer(), it) }.onSuccess { p -> _prefs.value = p } } }

    /** Publish a fresh [WatchState] via DataClient so the phone sees it even if it was offline. */
    suspend fun publishState() {
        runCatching {
            val (installed, remaining) = installer.installed()
            val node = nodeClient.localNode.await()
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val state = WatchState(
                nodeId = node.id,
                deviceName = Build.MODEL,
                apiLevel = Build.VERSION.SDK_INT,
                wearOsLabel = apiToWearOsLabel(Build.VERSION.SDK_INT),
                supportsWatchFacePush = installer.isSupported,
                hasPushPermission = installer.hasPermission,
                installedFaces = installed,
                activeSlotId = installed.firstOrNull { it.isActive }?.slotId,
                remainingSlots = remaining,
                batteryPercent = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            )
            val req = PutDataMapRequest.create(DataLayerContract.DataItems.WATCH_STATE).apply {
                dataMap.putString(DataLayerContract.DataItems.KEY_JSON, ChronaJson.encodeToString(WatchState.serializer(), state))
                dataMap.putLong(DataLayerContract.DataItems.KEY_TIMESTAMP, System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()
            dataClient.putDataItem(req).await()
        }.onFailure { Log.w(TAG, "publishState failed", it) }
    }

    private companion object { const val TAG = "WatchSyncManager" }
}
