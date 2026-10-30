package com.chrona.shared

/**
 * Single source of truth for every path, key and capability used over the
 * Wear OS Data Layer. Both the phone and watch modules depend on this file so
 * the two sides can never drift apart.
 */
object DataLayerContract {

    /** Capability the watch app advertises so the phone can find a Chrona-enabled node. */
    const val CAPABILITY_WATCH = "chrona_watch"
    /** Capability the phone app advertises so the watch can request catalog refreshes. */
    const val CAPABILITY_PHONE = "chrona_phone"

    // ---- MessageClient paths (fire-and-forget commands) ----
    object Messages {
        /** phone -> watch: "a watch-face APK is about to arrive on a channel". Payload: [TransferManifest] JSON. */
        const val INSTALL_REQUEST = "/chrona/install/request"
        /** watch -> phone: result of install. Payload: [InstallResult] JSON. */
        const val INSTALL_RESULT = "/chrona/install/result"
        /** phone -> watch: remove a watch face by slot id. Payload: slotId UTF-8. */
        const val REMOVE_REQUEST = "/chrona/remove/request"
        /** phone -> watch: set the given slot as the active watch face. Payload: slotId UTF-8. */
        const val SET_ACTIVE_REQUEST = "/chrona/active/request"
        /** watch -> phone: ask phone to resend the device state. Empty payload. */
        const val PING = "/chrona/ping"
        /** phone -> watch: open the watch companion app. */
        const val OPEN_APP = "/chrona/open"
    }

    // ---- ChannelClient paths (streams for binary APK transfer) ----
    object Channels {
        const val APK_TRANSFER = "/chrona/channel/apk"
    }

    // ---- DataClient paths (synced state, survives disconnects) ----
    object DataItems {
        /** watch -> phone: current state of the watch (installed faces, OS level, API level). */
        const val WATCH_STATE = "/chrona/state/watch"
        /** phone -> watch: user preferences that should mirror onto the watch. */
        const val USER_PREFS = "/chrona/state/prefs"

        const val KEY_JSON = "json"
        const val KEY_TIMESTAMP = "ts"
    }
}
