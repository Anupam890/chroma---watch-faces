package com.chrona.wear.sync

import android.content.Intent
import android.util.Log
import com.chrona.shared.ChronaJson
import com.chrona.shared.DataLayerContract
import com.chrona.shared.TransferManifest
import com.chrona.wear.MainActivity
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Entry point for everything the phone sends. Runs even if the watch UI is
 * closed, so installs work "silently" from the phone.
 */
@AndroidEntryPoint
class WatchDataLayerListenerService : WearableListenerService() {

    @Inject lateinit var sync: WatchSyncManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(event: MessageEvent) {
        val payload = String(event.data)
        when (event.path) {
            DataLayerContract.Messages.INSTALL_REQUEST -> runCatching {
                ChronaJson.decodeFromString(TransferManifest.serializer(), payload)
            }.onSuccess { sync.expectTransfer(it, event.sourceNodeId) }.onFailure { Log.w(TAG, "bad manifest", it) }

            DataLayerContract.Messages.REMOVE_REQUEST -> scope.launch { sync.remove(payload) }
            DataLayerContract.Messages.SET_ACTIVE_REQUEST -> scope.launch { sync.setActive(payload) }
            DataLayerContract.Messages.PING -> scope.launch { sync.publishState() }
            DataLayerContract.Messages.OPEN_APP -> startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    override fun onChannelOpened(channel: ChannelClient.Channel) {
        if (channel.path == DataLayerContract.Channels.APK_TRANSFER) scope.launch { sync.receiveApk(channel) }
    }

    override fun onDataChanged(events: DataEventBuffer) {
        events.forEach { e ->
            if (e.type == DataEvent.TYPE_CHANGED && e.dataItem.uri.path == DataLayerContract.DataItems.USER_PREFS) {
                sync.onPrefs(DataMapItem.fromDataItem(e.dataItem).dataMap.getString(DataLayerContract.DataItems.KEY_JSON))
            }
        }
    }

    private companion object { const val TAG = "WatchDLService" }
}
