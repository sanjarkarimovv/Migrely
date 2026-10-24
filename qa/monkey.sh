#!/usr/bin/env bash
# Seeded random-input stress run on the QA emulator. Fails on any crash or "app not responding" of the app.
#
#   qa/monkey.sh                 seed 20261024, 5000 events
#   qa/monkey.sh <seed> <events>
#
# The same seed replays the same events, so a failure can be reproduced.
set -euo pipefail

QA_SERIAL="${QA_SERIAL:-emulator-5580}"
APP="com.migrely.app"
SEED="${1:-20261024}"
EVENTS="${2:-5000}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ADB="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"
adbq() { "$ADB" -s "$QA_SERIAL" "$@"; }

case "$QA_SERIAL" in
  emulator-5580 | emulator-5582) ;;
  *) echo "Refusing to run on $QA_SERIAL: QA runs only on emulator-5580 or emulator-5582." >&2; exit 2 ;;
esac
if [[ "$(adbq shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]]; then
  echo "$QA_SERIAL is not booted." >&2
  exit 2
fi

out="$ROOT/build/qa/monkey/$(date +%Y%m%d-%H%M%S)"
mkdir -p "$out"
adbq shell pm clear "$APP" >/dev/null
adbq logcat -c
adbq shell monkey -p "$APP" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
sleep 3
adbq shell monkey -p "$APP" -s "$SEED" --throttle 50 --pct-syskeys 0 -v "$EVENTS" >"$out/monkey.txt" 2>&1 || true
adbq logcat -d -b crash >"$out/crash-buffer.txt"
adbq logcat -d >"$out/logcat.txt"

crashes=$(grep -c "Process: $APP" "$out/crash-buffer.txt" || true)
anrs=$(grep -c "ANR in $APP" "$out/logcat.txt" || true)
finished=$(grep -c "// Monkey finished" "$out/monkey.txt" || true)
echo "monkey seed $SEED, $EVENTS events on $QA_SERIAL: crashes $crashes, ANRs $anrs, finished $finished (logs: $out)"
if [[ "$crashes" -gt 0 || "$anrs" -gt 0 || "$finished" -eq 0 ]]; then
  grep -A12 "Process: $APP" "$out/crash-buffer.txt" | head -30 || true
  exit 1
fi
