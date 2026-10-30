#!/usr/bin/env bash
# Installs phone + watch debug builds to connected devices.
# Usage: scripts/install_debug.sh <phone-serial> <watch-serial>
set -euo pipefail
cd "$(dirname "$0")/.."
./gradlew :mobile:assembleDebug :wear:assembleDebug
adb -s "$1" install -r mobile/build/outputs/apk/debug/mobile-debug.apk
adb -s "$2" install -r wear/build/outputs/apk/debug/wear-debug.apk
echo "Tip: adb -s $2 shell pm grant com.chrona com.google.wear.permission.PUSH_WATCH_FACES"
