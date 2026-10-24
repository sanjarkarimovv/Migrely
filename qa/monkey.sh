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

source "$ROOT/qa/lib.sh"
qa_lock_device
out="$ROOT/build/qa/monkey/$(date +%Y%m%d-%H%M%S)"
mkdir -p "$out"
# Random swipes can open the notification shade and flip quick settings; compare and restore them afterwards.
WATCHED=("system user_rotation" "system accelerometer_rotation" "system font_scale" "secure ui_night_mode"
  "global airplane_mode_on" "global zen_mode")
declare -a BEFORE
for i in "${!WATCHED[@]}"; do BEFORE[$i]="$(adbq shell settings get ${WATCHED[$i]} | tr -d '\r')"; done
restore() {
  set +e
  adbq shell cmd statusbar collapse >/dev/null 2>&1
  for i in "${!WATCHED[@]}"; do
    now="$(adbq shell settings get ${WATCHED[$i]} | tr -d '\r')"
    if [[ "$now" != "${BEFORE[$i]}" ]]; then
      echo "monkey changed ${WATCHED[$i]}: ${BEFORE[$i]} -> $now (restored)"
      if [[ "${BEFORE[$i]}" == "null" ]]; then adbq shell settings delete ${WATCHED[$i]} >/dev/null
      else adbq shell settings put ${WATCHED[$i]} "${BEFORE[$i]}"; fi
    fi
  done
  qa_unlock_device
}
trap restore EXIT
adbq shell pm clear "$APP" >/dev/null
# Older Android versions sometimes refuse to clear a buffer ("failed to clear the 'main' log"); the crash and events
# buffers are what the verdict reads, so a failed clear only means more history to scan.
adbq logcat -c || true
adbq logcat -c -b crash || true
adbq logcat -c -b events || true
adbq shell monkey -p "$APP" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
sleep 3
# --ignore-crashes/--ignore-timeouts keep monkey going when ANOTHER app fails: the AOSP keyboard of the API 26 image
# crashes under random input ("Empty keySpec"). The verdict below reads only this app's crashes and ANRs.
adbq shell monkey -p "$APP" -s "$SEED" --throttle 50 --pct-syskeys 0 --ignore-crashes --ignore-timeouts -v "$EVENTS" \
  >"$out/monkey.txt" 2>&1 || true
adbq logcat -d -b crash >"$out/crash-buffer.txt"
adbq logcat -d -b events >"$out/events.txt"
adbq logcat -d >"$out/logcat.txt"

# Java crashes ("Process: <app>") and native ones (the tombstone header ">>> <app> <<<") are in the crash buffer;
# ANRs are in the events buffer (am_anr), which is small and is not filled by the app's own logging.
crashes=$(grep -c -e "Process: $APP" -e ">>> $APP <<<" "$out/crash-buffer.txt" || true)
anrs=$(grep -c "am_anr.*$APP" "$out/events.txt" || true)
finished=$(grep -c "// Monkey finished" "$out/monkey.txt" || true)
others=$(grep -c "^// CRASH: " "$out/monkey.txt" || true)
echo "monkey seed $SEED, $EVENTS events on $QA_SERIAL: crashes $crashes, ANRs $anrs, finished $finished," \
  "crashes of other apps $others (logs: $out)"
if [[ "$crashes" -gt 0 || "$anrs" -gt 0 || "$finished" -eq 0 ]]; then
  grep -A12 "Process: $APP" "$out/crash-buffer.txt" | head -30 || true
  exit 1
fi
