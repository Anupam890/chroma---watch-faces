# Architecture

## Modules
| Module | Type | minSdk | Purpose |
|---|---|---|---|
| `:shared` | Android library | 30 | `DataLayerContract` (paths/keys), serialisable models, bundled `Catalog`, shared `Json` |
| `:mobile` | App `com.chrona` | 26 | Marketplace UI, transfer orchestration |
| `:wear` | App `com.chrona` | 33 | Receiver, Watch Face Push bridge, small management UI |
| `:watchfaces:*` | Apps `com.chrona.watchfacepush.<id>` | 33–36 | Pure Watch Face Format packages (`hasCode=false`) |

Phone and watch share **the same applicationId** so the Data Layer pairs them and Play installs the companion automatically.

## Install sequence (Wear OS 6+)
```
User taps "Install"            WatchConnection (phone)             WatchDataLayerListenerService (watch)
        │                               │                                      │
        │ install(face)                 │                                      │
        │──────────────────────────────►│ prepareApk(): copy from assets,      │
        │                               │ compute SHA-256                      │
        │                               │ MessageClient INSTALL_REQUEST ──────►│ expectTransfer(manifest)
        │                               │ ChannelClient.openChannel ──────────►│ onChannelOpened
        │                               │ stream bytes (32 KB chunks) ────────►│ receiveApk(): write + hash
        │  progress 0→100 %             │                                      │ verify SHA-256 == manifest
        │                               │                                      │ WatchFaceInstaller.install()
        │                               │                                      │   addWatchFace(pfd, token)
        │                               │                                      │   setWatchFaceAsActive(slot)
        │                               │◄──── MessageClient INSTALL_RESULT ───│
        │  snackbar "Installed ✓"       │◄──── DataClient WATCH_STATE ─────────│ publishState()
```

## Why each Data Layer client
* **CapabilityClient** – discover the node running Chrona (`chrona_watch` / `chrona_phone`).
* **MessageClient** – low-latency, fire-and-forget commands; fails fast if the node is unreachable.
* **ChannelClient** – the only sanctioned way to stream a large binary without hitting the 100 KB `DataItem` limit.
* **DataClient** – eventually-consistent state (`WatchState`, `UserPrefs`) that survives disconnects and app restarts.

## Watch Face Push rules baked into the code
1. Package name `com.chrona.watchfacepush.<faceId>` – enforced in `WatchFaceItem.pushPackageName()` and each face's `build.gradle.kts`.
2. Validation token travels in `TransferManifest.validationToken`; the watch passes it straight to `addWatchFace`.
3. Face APKs contain only manifest + resources (`hasCode=false`, packaging excludes).
4. Faces are signed with a separate key (`keystore/faces.jks`).
5. All API-36 calls live in `WatchFaceInstaller`; everything else runs on Wear OS 4/5 and degrades gracefully.

## State management
* Phone: single `UiState` derived with `combine()` from catalog, prefs (DataStore), watch node, watch state and per-face progress.
* Watch: `WatchSyncManager.status` drives the UI; `publishState()` is called after every mutation so the phone is never stale.

## Testing
* `:shared` – catalog invariants, JSON round-trips.
* `:mobile` – `UiState` derivations.
* Data Layer code is isolated behind injectable Google Play clients so it can be mocked with MockK.
