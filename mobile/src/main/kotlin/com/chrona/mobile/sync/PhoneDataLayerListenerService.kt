package com.chrona.mobile.sync

import com.chrona.shared.DataLayerContract
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Wakes the phone app for watch events even when the UI is not running. */
@AndroidEntryPoint
class PhoneDataLayerListenerService : WearableListenerService() {

    @Inject lateinit var connection: WatchConnection

    override fun onMessageReceived(event: MessageEvent) {
        when (event.path) {
            DataLayerContract.Messages.INSTALL_RESULT -> connection.onInstallResult(String(event.data))
        }
    }

    override fun onDataChanged(events: DataEventBuffer) {
        events.forEach { e ->
            if (e.type == DataEvent.TYPE_CHANGED && e.dataItem.uri.path == DataLayerContract.DataItems.WATCH_STATE) {
                val json = DataMapItem.fromDataItem(e.dataItem).dataMap.getString(DataLayerContract.DataItems.KEY_JSON)
                connection.onWatchStateItem(json)
            }
        }
    }
}
