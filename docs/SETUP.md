# Setup & Release

## 1. Tooling
* Android Studio Ladybug (2024.2) or newer, JDK 17
* SDK Platform 36 (Wear OS 6) + build-tools 35+
* A Wear OS 6 emulator (API 36, "Wear OS Large Round") or physical watch, paired with a phone/emulator

## 2. Run in debug
```bash
./gradlew :mobile:assembleDebug :wear:assembleDebug :watchfaces:classic_aurora:assembleDebug
scripts/install_debug.sh <phone> <watch>
adb -s <watch> shell pm grant com.chrona com.google.wear.permission.PUSH_WATCH_FACES
```
Open Chrona on both devices. The watch publishes its state; the phone's **My Watch** tab should show the model and "Direct install ✓".

## 3. Produce installable faces
1. Create a keystore **only for faces** (must differ from the app key):
   `keytool -genkeypair -v -keystore keystore/faces.jks -alias chrona-faces -keyalg RSA -keysize 2048 -validity 10000`
2. Download the validator CLI from Google's Maven repo (`com.google.android.wearable.watchface.validator:validator-push-cli`) into `tools/`.
3. Run:
   ```bash
   FACE_KEYSTORE_PASS=… FACE_KEY_ALIAS=chrona-faces FACE_KEY_PASS=… scripts/build_faces.sh
   ```
4. Copy the tokens from `mobile/src/main/assets/faces/tokens.json` into `Catalog.kt` (`validationToken = "…"`), or load the JSON at runtime (see Future Enhancements).
5. Rebuild `:mobile`. Install from the phone now succeeds.

> Any change to a face's XML or resources changes its APK bytes → **re-run the validator** and update the token.

## 4. Older watches (Wear OS 4/5)
Publish each `watchfaces/*` module to Google Play as its own listing. Chrona detects API < 36 and tells the user to install from Play. Optionally add a deep link per face: `https://play.google.com/store/apps/details?id=com.chrona.watchfacepush.<id>`.

## 5. Release checklist
- [ ] Bump `versionCode` in `mobile`, `wear` and any changed face
- [ ] `./gradlew lint testDebugUnitTest`
- [ ] Re-validate changed faces, update tokens
- [ ] Upload phone + wear AABs to the same Play listing (wear track)
- [ ] Review Watch Face Push policy: https://developer.android.com/watch-face-push-policies
