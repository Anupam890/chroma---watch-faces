#!/usr/bin/env bash
# Builds every Watch Face Format module, signs it with the dedicated *face* key,
# runs the Watch Face Push validator, and copies APK + token into the phone app assets.
#
# Prereqs:
#   1. keystore/faces.jks  (DIFFERENT key from the app key — required by Watch Face Push)
#   2. validator jar from Google Maven:
#        https://maven.google.com/web/index.html#com.google.android.wearable.watchface.validator:validator-push-cli
#   3. env: FACE_KEYSTORE_PASS, FACE_KEY_ALIAS, FACE_KEY_PASS
set -euo pipefail
cd "$(dirname "$0")/.."

VALIDATOR=${VALIDATOR_JAR:-tools/validator-push-cli.jar}
MARKETPLACE_PKG=com.chrona
OUT=mobile/src/main/assets/faces
TOKENS=mobile/src/main/assets/faces/tokens.json
mkdir -p "$OUT"
echo "{" > "$TOKENS"; first=1

for mod in classic_aurora mono_digital pulse_sport memories_photo skyline_weather; do
  echo "▶ building $mod"
  ./gradlew -q ":watchfaces:$mod:assembleRelease"
  unsigned=$(ls watchfaces/$mod/build/outputs/apk/release/*-unsigned.apk 2>/dev/null || ls watchfaces/$mod/build/outputs/apk/release/*.apk | head -1)
  aligned="$OUT/$mod.apk"
  "$ANDROID_HOME"/build-tools/*/zipalign -f -p 4 "$unsigned" "$aligned.tmp"
  "$ANDROID_HOME"/build-tools/*/apksigner sign --ks keystore/faces.jks --ks-pass "pass:$FACE_KEYSTORE_PASS" \
      --ks-key-alias "$FACE_KEY_ALIAS" --key-pass "pass:$FACE_KEY_PASS" --out "$aligned" "$aligned.tmp"
  rm -f "$aligned.tmp"

  echo "▶ validating $mod"
  token=$(java -jar "$VALIDATOR" --apk_path="$aligned" --package_name="$MARKETPLACE_PKG" | grep -oE '[A-Za-z0-9+/=_-]{20,}' | tail -1)
  [[ -n "$token" ]] || { echo "✗ validation failed for $mod"; exit 1; }
  [[ $first == 1 ]] || echo "," >> "$TOKENS"; first=0
  printf '  "%s": "%s"' "$mod" "$token" >> "$TOKENS"
  echo "✓ $mod  $(du -k "$aligned" | cut -f1) KB"
done
echo -e "\n}" >> "$TOKENS"
echo "Done. Tokens written to $TOKENS — wire them into Catalog.kt or load at runtime."
