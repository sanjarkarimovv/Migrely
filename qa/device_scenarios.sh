#!/usr/bin/env bash
# End-to-end scenarios that need the device itself: the clock, the time zone or the system ending the app.
# Maestro drives the app; this script does what only adb can do between flows, the way real life would. No test code
# is added to the app. The clock, automatic time and the time zone are restored on exit, even when a step fails.
#
#   qa/device_scenarios.sh                         every scenario
#   qa/device_scenarios.sh long midnight           only the named ones
#
# Scenarios: long (72-hour check), history_death, past_death, details_kill, midnight, travel.
set -euo pipefail

QA_SERIAL="emulator-5580"
APP="com.migrely.app"
export MAESTRO_CLI_NO_ANALYTICS=1
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ADB="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"
FLOWS="$ROOT/.maestro/device"
adbq() { "$ADB" -s "$QA_SERIAL" "$@"; }

if [[ "$(adbq shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]]; then
  echo "$QA_SERIAL is not booted (see qa/maestro.sh for the start command)." >&2
  exit 2
fi

source "$ROOT/qa/lib.sh"
qa_lock_device
ORIGINAL_AUTO_TIME="$(adbq shell settings get global auto_time | tr -d '\r')"
ORIGINAL_ZONE="$(adbq shell getprop persist.sys.timezone | tr -d '\r')"
restore() {
  adbq shell cmd alarm set-timezone "$ORIGINAL_ZONE" >/dev/null || true
  adbq shell cmd alarm set-time "$(($(date +%s) * 1000))" >/dev/null || true
  adbq shell settings put global auto_time "$ORIGINAL_AUTO_TIME"
  echo "Restored: clock $(adbq shell date | tr -d '\r'), zone $ORIGINAL_ZONE, auto_time $ORIGINAL_AUTO_TIME"
}
trap 'restore; qa_unlock_device' EXIT
adbq shell settings put global auto_time 0

device_seconds() { adbq shell date +%s | tr -d '\r'; }
set_clock_millis() { adbq shell cmd alarm set-time "$1" >/dev/null; }
shift_clock_hours() { # $1 hours
  set_clock_millis $((($(device_seconds) + $1 * 3600) * 1000))
  echo "    clock moved $1 h -> $(adbq shell date | tr -d '\r')"
}
set_zone() { adbq shell cmd alarm set-timezone "$1" >/dev/null; echo "    time zone -> $1"; }
local_time_millis() { # $1 zone, $2 HH:MM, today's date in that zone
  python3 -I -c "import sys, datetime as d, zoneinfo as z
tz = z.ZoneInfo(sys.argv[1]); h, m = map(int, sys.argv[2].split(':'))
t = d.datetime.now(tz).replace(hour=h, minute=m, second=0, microsecond=0)
print(int(t.timestamp() * 1000))" "$1" "$2"
}
background_and_kill() { # the system ends the app while it is in the background
  adbq shell input keyevent KEYCODE_HOME
  sleep 2
  adbq shell run-as "$APP" kill -9 "$(adbq shell pidof "$APP" | tr -d '\r')"
  sleep 1
  [[ -z "$(adbq shell pidof "$APP" | tr -d '\r')" ]] && echo "    app process ended in the background"
}
kill_in_foreground() {
  adbq shell run-as "$APP" kill -9 "$(adbq shell pidof "$APP" | tr -d '\r')"
  sleep 1
  echo "    app process killed in the foreground"
}
open_from_launcher() { # like tapping the app icon: brings back the existing task
  adbq shell monkey -p "$APP" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
  sleep 3
}
run_flow() {
  echo "  flow $1"
  maestro --device "$QA_SERIAL" test "$FLOWS/$1.yaml" --test-output-dir "$out/$1" >"$out/$1.log" 2>&1 || {
    echo "  FAILED: $1 (log: $out/$1.log)"
    return 1
  }
}

scenario_long() {
  echo "Scenario long: an attack left open for days (EC-ATK-02, SAF-F3)"
  run_flow start-attack-and-close
  shift_clock_hours 73
  run_flow long-attack-still-going
  shift_clock_hours 73
  run_flow long-attack-dont-ask
  shift_clock_hours 73
  run_flow long-attack-no-prompt
}

scenario_history_death() {
  echo "Scenario history_death: History keeps its month and day after process death (EC-ATK-10)"
  run_flow history-setup
  background_and_kill
  open_from_launcher
  run_flow history-restored
}

scenario_past_death() {
  echo "Scenario past_death: picked times survive process death and nothing is stored (EC-ATK-10)"
  run_flow past-setup
  background_and_kill
  open_from_launcher
  run_flow past-restored
}

scenario_details_kill() {
  echo "Scenario details_kill: a choice made a second before the app dies is kept (ATK-F6)"
  run_flow details-setup
  sleep 1
  kill_in_foreground
  open_from_launcher
  run_flow details-restored
}

scenario_midnight() {
  echo "Scenario midnight: home open across local midnight (today-sky)"
  set_clock_millis "$(local_time_millis "$ORIGINAL_ZONE" 23:55)"
  run_flow midnight-before
  local now_local
  now_local="$(adbq shell date +%H:%M | tr -d '\r')"
  if [[ "$now_local" < "23:55" ]]; then
    echo "  INCONCLUSIVE: the setup ran past midnight ($now_local)"
    return 1
  fi
  echo "    waiting for midnight (device time $now_local)"
  while [[ "$(adbq shell date +%H | tr -d '\r')" == "23" ]]; do sleep 5; done
  run_flow midnight-after
}

scenario_travel() {
  echo "Scenario travel: logged late in Berlin, opened the next morning in Tashkent (EC-TIM-02)"
  set_zone Europe/Berlin
  set_clock_millis "$(local_time_millis Europe/Berlin 23:15)"
  run_flow travel-before
  set_zone Asia/Tashkent
  run_flow travel-after
}

out="$ROOT/build/qa/device/$(date +%Y%m%d-%H%M%S)"
mkdir -p "$out"
if [[ $# -eq 0 ]]; then
  set -- long history_death past_death details_kill midnight travel
fi
failed=()
for s in "$@"; do
  "scenario_$s" || failed+=("$s")
  # Each scenario starts from the real time, so one scenario's clock never leaks into the next.
  adbq shell cmd alarm set-timezone "$ORIGINAL_ZONE" >/dev/null
  set_clock_millis $(($(date +%s) * 1000))
done
echo "Results: $out"
if [[ ${#failed[@]} -gt 0 ]]; then
  echo "Failed: ${failed[*]}"
  exit 1
fi
echo "All scenarios passed"
