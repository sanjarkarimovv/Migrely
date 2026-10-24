#!/usr/bin/env bash
# End-to-end scenarios that need the device itself: the clock, the time zone, exact tap timing or the system ending the
# app. Maestro drives the app; this script does what only adb can do between flows, the way real life would. No test
# code is added to the app. The clock, automatic time and the time zone are restored on exit, even when a step fails.
#
#   qa/device_scenarios.sh                         every scenario
#   qa/device_scenarios.sh long midnight           only the named ones
#
# Scenarios: long (72-hour check), history_death, past_death, details_kill, midnight, travel, travel_west, same_date,
# zone_attack, zone_history, double_tap, upgrade.
# upgrade needs QA_OLD_APK=<older debug APK of com.migrely.app>; without it, it is reported as not run.
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
  set +e # restore everything even if one command fails
  adbq shell cmd alarm set-timezone "$ORIGINAL_ZONE" >/dev/null 2>&1
  adbq shell cmd alarm set-time "$(($(date +%s) * 1000))" >/dev/null 2>&1
  adbq shell settings put global auto_time "$ORIGINAL_AUTO_TIME" >/dev/null 2>&1
  echo "Restored: clock $(adbq shell date | tr -d '\r'), zone $ORIGINAL_ZONE, auto_time $ORIGINAL_AUTO_TIME"
  qa_unlock_device
}
trap restore EXIT
adbq shell settings put global auto_time 0

device_seconds() { adbq shell date +%s | tr -d '\r'; }
set_clock_millis() { adbq shell cmd alarm set-time "$1" >/dev/null; }
shift_clock_hours() { # $1 hours
  set_clock_millis $((($(device_seconds) + $1 * 3600) * 1000))
  echo "    clock moved $1 h -> $(adbq shell date | tr -d '\r')"
}
set_zone() { adbq shell cmd alarm set-timezone "$1" >/dev/null; echo "    time zone -> $1"; }
# The 15th of the current month at HH:MM in a zone. A fixed mid-month day keeps "yesterday" and "tomorrow" inside the
# month that home's calendar shows, whatever the real date is.
mid_month_millis() { # $1 zone, $2 HH:MM
  python3 -I -c "import sys, datetime as d, zoneinfo as z
tz = z.ZoneInfo(sys.argv[1]); h, m = map(int, sys.argv[2].split(':'))
t = d.datetime.now(tz).replace(day=15, hour=h, minute=m, second=0, microsecond=0)
print(int(t.timestamp() * 1000))" "$1" "$2"
}
app_pid() { adbq shell pidof "$APP" | tr -d '\r'; }
background_and_kill() { # the system ends the app while it is in the background
  adbq shell input keyevent KEYCODE_HOME
  sleep 2
  local pid
  pid="$(app_pid)"
  [[ -n "$pid" ]] || { echo "  FAILED: the app was not running before the kill"; return 1; }
  adbq shell run-as "$APP" kill -9 "$pid"
  sleep 1
  [[ -z "$(app_pid)" ]] || { echo "  FAILED: the app process survived the kill"; return 1; }
  echo "    app process $pid ended in the background"
}
kill_in_foreground() {
  local pid
  pid="$(app_pid)"
  [[ -n "$pid" ]] || { echo "  FAILED: the app was not running before the kill"; return 1; }
  adbq shell run-as "$APP" kill -9 "$pid"
  sleep 1
  [[ -z "$(app_pid)" ]] || { echo "  FAILED: the app process survived the kill"; return 1; }
  echo "    app process $pid killed in the foreground"
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
# Centre x and a y in the given fraction of the height of the first node whose text matches a regex.
point_in() { # $1 regex, $2 fraction from the top of the node (0..1)
  adbq shell uiautomator dump /sdcard/qa-point.xml >/dev/null
  adbq exec-out cat /sdcard/qa-point.xml | python3 -I -c "import re, sys, xml.etree.ElementTree as ET
root = ET.fromstring(sys.stdin.read()); parents = {c: p for p in root.iter() for c in p}
for n in root.iter('node'):
    if re.fullmatch(sys.argv[1], n.get('text', '')):
        while n.get('clickable') != 'true' and n in parents: n = parents[n]
        x1, y1, x2, y2 = map(int, n.get('bounds').replace('][', ',').strip('[]').split(','))
        print((x1 + x2) // 2, int(y1 + (y2 - y1) * float(sys.argv[2]))); break" "$1" "$2"
}

# Each step ends the scenario on failure: a scenario runs inside "||", where set -e does not apply.
scenario_long() {
  echo "Scenario long: an attack left open for days (EC-ATK-02, SAF-F3)"
  run_flow start-attack-and-close || return 1
  shift_clock_hours 73 || return 1
  run_flow long-attack-still-going || return 1
  shift_clock_hours 73 || return 1
  run_flow long-attack-dont-ask || return 1
  shift_clock_hours 73 || return 1
  run_flow long-attack-no-prompt || return 1
}

scenario_history_death() {
  echo "Scenario history_death: History keeps its month and day after process death (EC-ATK-10)"
  run_flow history-setup || return 1
  background_and_kill || return 1
  open_from_launcher
  run_flow history-restored || return 1
}

scenario_past_death() {
  echo "Scenario past_death: picked times survive process death and nothing is stored (EC-ATK-10)"
  run_flow past-setup || return 1
  background_and_kill || return 1
  open_from_launcher
  run_flow past-restored || return 1
}

scenario_details_kill() {
  echo "Scenario details_kill: a choice made a second before the app dies is kept (ATK-F6)"
  run_flow details-setup || return 1
  sleep 1
  kill_in_foreground || return 1
  open_from_launcher
  run_flow details-restored || return 1
}

scenario_midnight() {
  echo "Scenario midnight: home open across local midnight (today-sky)"
  set_clock_millis "$(mid_month_millis "$ORIGINAL_ZONE" 23:55)"
  run_flow midnight-before || return 1
  local now_local
  now_local="$(adbq shell date +%H:%M | tr -d '\r')"
  if [[ "$now_local" < "23:55" ]]; then
    echo "  INCONCLUSIVE: the setup ran past midnight ($now_local)"
    return 1
  fi
  echo "    waiting for midnight (device time $now_local)"
  while [[ "$(adbq shell date +%H | tr -d '\r')" == "23" ]]; do sleep 5; done
  run_flow midnight-after || return 1
}

scenario_travel() {
  echo "Scenario travel: logged late in Berlin, opened the next morning in Tashkent (EC-TIM-02)"
  set_zone Europe/Berlin
  set_clock_millis "$(mid_month_millis Europe/Berlin 23:15)"
  run_flow travel-before || return 1
  set_zone Asia/Tashkent
  run_flow travel-after || return 1
}

scenario_travel_west() {
  echo "Scenario travel_west: logged at 01:00 in Tashkent, opened the evening before in Berlin (EC-TIM-02)"
  set_zone Asia/Tashkent
  set_clock_millis "$(mid_month_millis Asia/Tashkent 01:00)"
  run_flow log-and-close || return 1
  set_zone Europe/Berlin
  run_flow travel-west-after || return 1
}

scenario_same_date() {
  echo "Scenario same_date: logged at 08:00 in Tashkent, opened the same date in Dubai (EC-TIM-02)"
  set_zone Asia/Tashkent
  set_clock_millis "$(mid_month_millis Asia/Tashkent 08:00)"
  run_flow log-and-close || return 1
  set_zone Asia/Dubai
  run_flow same-date-after || return 1
}

scenario_zone_attack() {
  echo "Scenario zone_attack: the zone changes while an attack is in progress and the app is in the background"
  set_zone Asia/Dubai
  run_flow zone-attack-setup || return 1
  adbq shell input keyevent KEYCODE_HOME
  sleep 2
  set_zone Asia/Tashkent
  open_from_launcher
  run_flow zone-attack-after || return 1
}

scenario_zone_history() {
  echo "Scenario zone_history: the zone changes while History is on screen"
  set_zone Asia/Dubai
  run_flow zone-history-setup || return 1
  set_zone Asia/Tashkent
  sleep 2
  run_flow zone-history-after || return 1
}

scenario_double_tap() {
  echo "Scenario double_tap: double taps never cross a mode switch (EC-UX-18, D32), with exact gaps between the taps"
  local gap point
  for gap in 0.15 0.4 0.7; do
    run_flow double-tap-ready || return 1
    point="$(point_in 'Migraine started' 0.85)"
    [[ -n "$point" ]] || { echo "  FAILED: \"Migraine started\" not found"; return 1; }
    adbq shell "input tap $point & sleep $gap; input tap $point"
    echo "    double tap on \"Migraine started\" at ($point), ${gap} s apart"
    run_flow double-tap-start-result || return 1
    point="$(point_in 'Migraine ended' 0.15)"
    [[ -n "$point" ]] || { echo "  FAILED: \"Migraine ended\" not found"; return 1; }
    sleep 1.5 # past the guard that follows the start
    adbq shell "input tap $point & sleep $gap; input tap $point"
    echo "    double tap on \"Migraine ended\" at ($point), ${gap} s apart"
    run_flow double-tap-end-result || return 1
    # The upper part of "Migraine started" lies over "+ Medication taken" in attack mode (change 3).
    run_flow double-tap-ready || return 1
    point="$(point_in 'Migraine started' 0.15)"
    adbq shell "input tap $point & sleep $gap; input tap $point"
    echo "    double tap on the upper part of \"Migraine started\" at ($point), ${gap} s apart"
    run_flow double-tap-start-result || return 1
  done
}

scenario_upgrade() {
  echo "Scenario upgrade: a newer build installed over an older one keeps every attack (EC-STO-01, ATK-F6)"
  if [[ -z "${QA_OLD_APK:-}" || ! -f "${QA_OLD_APK:-}" ]]; then
    echo "  NOT RUN: set QA_OLD_APK to the older APK (for a release: the APK of the previous release)"
    return 1
  fi
  local new_apk="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
  adbq uninstall "$APP" >/dev/null 2>&1 || true
  adbq install "$QA_OLD_APK" >/dev/null || { echo "  FAILED: could not install $QA_OLD_APK"; return 1; }
  echo "    installed the older build $(adbq shell dumpsys package "$APP" | grep -m1 versionCode | tr -s ' ')"
  run_flow upgrade-seed || return 1
  adbq install -r "$new_apk" >/dev/null || { echo "  FAILED: could not install the newer build over it"; return 1; }
  echo "    installed the newer build over it, data kept"
  run_flow upgrade-kept || return 1
}

out="$ROOT/build/qa/device/$(date +%Y%m%d-%H%M%S)"
mkdir -p "$out"
if [[ $# -eq 0 ]]; then
  set -- long history_death past_death details_kill midnight travel travel_west same_date zone_attack zone_history \
    double_tap
  if [[ -n "${QA_OLD_APK:-}" ]]; then
    set -- "$@" upgrade
  else
    echo "Note: upgrade is not in this run; set QA_OLD_APK to the older APK to include it."
  fi
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
