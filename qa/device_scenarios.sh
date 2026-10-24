#!/usr/bin/env bash
# End-to-end scenarios that need the device itself: the clock, the time zone, exact tap timing or the system ending the
# app. Maestro drives the app; this script does what only adb can do between flows, the way real life would. No test
# code is added to the app. The clock, automatic time and the time zone are restored on exit, even when a step fails.
#
#   qa/device_scenarios.sh                         every scenario
#   qa/device_scenarios.sh long midnight           only the named ones
#
# Scenarios: long (72-hour check), history_death, past_death, details_kill, midnight, travel, travel_west, same_date,
# zone_attack, zone_history, double_tap, med_effect, med_effect_attack, med_effect_skip, med_effect_late,
# med_preventive, med_two_waiting, med_link_home, med_overuse, keyboard_home, keyboard_history, keyboard_rtl,
# landscape_band, mark_travel, mark_earliest_month, upgrade.
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
ORIGINAL_TIMEOUT="$(adbq shell settings get secure accessibility_interactive_ui_timeout_ms | tr -d '\r')"
ORIGINAL_USER_ROTATION="$(adbq shell settings get system user_rotation | tr -d '\r')"
ORIGINAL_AUTO_ROTATE="$(adbq shell settings get system accelerometer_rotation | tr -d '\r')"
ORIGINAL_ANIMATOR="$(adbq shell settings get global animator_duration_scale | tr -d '\r')"
# "Locales for <app> for user 0 are [iw]" -> "iw"; empty means the app follows the system language.
ORIGINAL_APP_LOCALES=""
locales_line="$(adbq shell cmd locale get-app-locales "$APP" 2>/dev/null | tr -d '\r' || true)"
if [[ "$locales_line" =~ \[(.*)\] ]]; then ORIGINAL_APP_LOCALES="${BASH_REMATCH[1]}"; fi
put_or_delete() { # $1 namespace, $2 key, $3 value or "null"
  if [[ "$3" == "null" ]]; then adbq shell settings delete "$1" "$2" >/dev/null; else adbq shell settings put "$1" "$2" "$3"; fi
}
restore() {
  set +e # restore everything even if one command fails
  adbq shell cmd alarm set-timezone "$ORIGINAL_ZONE" >/dev/null 2>&1
  adbq shell cmd alarm set-time "$(($(date +%s) * 1000))" >/dev/null 2>&1
  adbq shell settings put global auto_time "$ORIGINAL_AUTO_TIME" >/dev/null 2>&1
  put_or_delete secure accessibility_interactive_ui_timeout_ms "$ORIGINAL_TIMEOUT" >/dev/null 2>&1
  adbq shell cmd locale set-app-locales "$APP" --locales "$ORIGINAL_APP_LOCALES" >/dev/null 2>&1
  put_or_delete system user_rotation "$ORIGINAL_USER_ROTATION" >/dev/null 2>&1
  put_or_delete system accelerometer_rotation "$ORIGINAL_AUTO_ROTATE" >/dev/null 2>&1
  put_or_delete global animator_duration_scale "$ORIGINAL_ANIMATOR" >/dev/null 2>&1
  echo "Restored: clock $(adbq shell date | tr -d '\r'), zone $ORIGINAL_ZONE, auto_time $ORIGINAL_AUTO_TIME"
  qa_unlock_device
}
trap restore EXIT
adbq shell settings put global auto_time 0

device_seconds() { adbq shell date +%s | tr -d '\r'; }
# A clock or zone move that silently fails would let the "absence" checks pass without testing anything, so every move
# is read back and fails the step when the device does not follow.
set_clock_millis() { # $1 epoch millis
  [[ "$1" =~ ^[0-9]{12,}$ ]] || { echo "  FAILED: bad clock target '$1'"; return 1; }
  adbq shell cmd alarm set-time "$1" >/dev/null
  local now=$(( $(device_seconds) * 1000 )) diff
  diff=$(( now - $1 )); diff=${diff#-}
  [[ $diff -le 60000 ]] || { echo "  FAILED: the device clock did not move (wanted $1, has $now)"; return 1; }
}
shift_clock_hours() { # $1 hours
  set_clock_millis $((($(device_seconds) + $1 * 3600) * 1000)) || return 1
  echo "    clock moved $1 h -> $(adbq shell date | tr -d '\r')"
}
set_zone() {
  adbq shell cmd alarm set-timezone "$1" >/dev/null
  [[ "$(adbq shell getprop persist.sys.timezone | tr -d '\r')" == "$1" ]] || { echo "  FAILED: zone did not change to $1"; return 1; }
  echo "    time zone -> $1"
}
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
STEP=0
run_flow() {
  STEP=$((STEP + 1))
  local name
  name="$(printf '%03d' "$STEP")-${CURRENT:-x}-$1"
  echo "  flow $1"
  maestro --device "$QA_SERIAL" test "$FLOWS/$1.yaml" --test-output-dir "$out/$name" >"$out/$name.log" 2>&1 || {
    echo "  FAILED: $1 (log: $out/$name.log)"
    return 1
  }
}
# Centre x and a y in the given fraction of the height of the first node whose text matches a regex.
point_in() { # $1 regex, $2 fraction from the top of the node (0..1)
  adbq shell rm -f /sdcard/qa-point.xml
  adbq shell uiautomator dump /sdcard/qa-point.xml >/dev/null
  adbq exec-out cat /sdcard/qa-point.xml | python3 -I -c "import re, sys, xml.etree.ElementTree as ET
root = ET.fromstring(sys.stdin.read()); parents = {c: p for p in root.iter() for c in p}
for n in root.iter('node'):
    if re.fullmatch(sys.argv[1], n.get('text', '')):
        while n.get('clickable') != 'true' and n in parents: n = parents[n]
        x1, y1, x2, y2 = map(int, n.get('bounds').replace('][', ',').strip('[]').split(','))
        print((x1 + x2) // 2, int(y1 + (y2 - y1) * float(sys.argv[2]))); break" "$1" "$2"
}

# Keyboard checks (D62): keys go through adb, and the focused node is read from uiautomator. Compose keeps a control's
# label on a child of the focusable node, so the labels under the focused node are joined.
focused_label() {
  focused_node label
}
focused_node() { # $1 "label" (the labels under the focused node, joined) or "checked" (its checked state)
  # A failed dump must not leave an older screen behind, so the file is removed first.
  adbq shell rm -f /sdcard/qa-focus.xml
  adbq shell uiautomator dump /sdcard/qa-focus.xml >/dev/null
  adbq exec-out cat /sdcard/qa-focus.xml | python3 -I -c "import sys, xml.etree.ElementTree as ET
for n in ET.fromstring(sys.stdin.read()).iter('node'):
    if n.get('focused') == 'true':
        if sys.argv[1] == 'checked':
            print(n.get('checked'))
        else:
            print(' | '.join(m.get('content-desc') or m.get('text') for m in n.iter('node') if m.get('content-desc') or m.get('text')))
        break" "$1"
}
expect_checked() { # $1 true or false: the checked state of the focused node, as accessibility services report it
  local state try
  for try in 1 2 3; do
    sleep 1
    state="$(focused_node checked)"
    if [[ "$state" == "$1" ]]; then echo "    checked: $state ($(focused_label))"; return 0; fi
  done
  echo "  FAILED: the focused node is checked=${state:-unknown}, expected $1"
  return 1
}
key() { # $1 an Android key name (TAB, DPAD_LEFT, ENTER) or SHIFT_TAB
  if [[ "$1" == "SHIFT_TAB" ]]; then adbq shell input keycombination 59 61; else adbq shell input keyevent "$1"; fi
}
expect_focus() { # $1 key, $2 extended regex the focused label must match after it
  local label="" try
  key "$1"
  for try in 1 2 3; do
    sleep 1
    label="$(focused_label)"
    if [[ "$label" =~ $2 ]]; then echo "    $1 -> $label"; return 0; fi
  done
  echo "  FAILED: after $1 the focus is on '${label:-nothing}', expected /$2/"
  return 1
}
# Landscape message band (D52): the top of "Rest now." against the bottom of the message, read from uiautomator.
# $1 "below": the message shows and the guidance starts at or below its bottom; the guidance top is kept in $out.
# $1 "back": the message is gone and the guidance has moved back up.
band_check() {
  adbq shell rm -f /sdcard/qa-band.xml
  adbq shell uiautomator dump /sdcard/qa-band.xml >/dev/null
  adbq exec-out cat /sdcard/qa-band.xml | python3 -I -c "import re, sys, xml.etree.ElementTree as ET
mode, kept = sys.argv[1], sys.argv[2]
def box(n): return list(map(int, re.findall(r'\d+', n.get('bounds'))))
nodes = [(n.get('content-desc') or n.get('text') or '', box(n)) for n in ET.fromstring(sys.stdin.read()).iter('node')]
guidance = [b for label, b in nodes if label.startswith('Rest now.')]
started = [b for label, b in nodes if label.startswith('Started ') and '·' in label]
controls = [b for label, b in nodes if label in ('Mild', 'Moderate', 'Severe')]
message = [b for label, b in nodes if label in ('Undo the start', 'Migraine started')]
if not guidance or not started: sys.exit('  FAILED: \"Rest now.\" or the start line is not on screen')
top = guidance[0][1]
if mode == 'below':
    if not message or not controls: sys.exit('  FAILED: the start message or the intensity controls are not on screen')
    bottom, right, controls_left = max(b[3] for b in message), max(b[2] for b in message), min(b[0] for b in controls)
    if top < bottom or started[0][1] < bottom:
        sys.exit(f'  FAILED: the guidance (y={top}) or the start line (y={started[0][1]}) is under the message (ends y={bottom})')
    if right > controls_left: sys.exit(f'  FAILED: the message reaches x={right}, over the controls column (x={controls_left})')
    open(kept, 'w').write(str(top))
    print(f'    message ends at y={bottom} and x={right}; guidance at y={top}, start line at y={started[0][1]}; controls from x={controls_left}')
else:
    if message: sys.exit('  FAILED: the message is still on screen')
    before = int(open(kept).read())
    if top >= before: sys.exit(f'  FAILED: the guidance stayed at y={top} (y={before} with the message)')
    print(f'    message gone, guidance back up at y={top} (y={before} with the message)')
" "$1" "$out/band-top.txt"
}

tab_until() { # $1 extended regex, $2 most Tabs: press Tab until the focused label matches
  local label="" n
  for ((n = 1; n <= $2; n++)); do
    key TAB
    sleep 1
    label="$(focused_label)"
    if [[ "$label" =~ $1 ]]; then echo "    TAB x$n -> $label"; return 0; fi
  done
  echo "  FAILED: $2 Tabs never reached /$1/ (last: '${label:-nothing}')"
  return 1
}
day_re() { echo "(^|[^0-9])$1([^0-9]|\$)"; } # a day label for day $1 of the month, in any language
DAY_CELL=": (nothing logged|calm|hazy|cloudy|storm|rain|pain-free|period)" # what follows the date in a cell's label

run_flow_env() { # $1 flow, then KEY=VALUE pairs passed to Maestro with -e
  local flow="$1"
  shift
  local args=()
  for kv in "$@"; do args+=(-e "$kv"); done
  STEP=$((STEP + 1))
  local name
  name="$(printf '%03d' "$STEP")-${CURRENT:-x}-$flow"
  echo "  flow $flow ($*)"
  maestro --device "$QA_SERIAL" test "${args[@]}" "$FLOWS/$flow.yaml" --test-output-dir "$out/$name" >"$out/$name.log" 2>&1 || {
    echo "  FAILED: $flow (log: $out/$name.log)"
    return 1
  }
}
# Computed once, so a host clock crossing a month end during a long scenario cannot split its days across two months.
MED_BASE_MILLIS="$(mid_month_millis "$ORIGINAL_ZONE" 10:00)"
[[ "$MED_BASE_MILLIS" =~ ^[0-9]{12,}$ ]] || { echo "Could not compute the base time" >&2; exit 2; }
day_millis() { # $1 days before 10:00 on the 15th of this month in the device's zone (negative: after)
  echo $(( MED_BASE_MILLIS - $1 * 86400000 ))
}
long_undo_window() { adbq shell settings put secure accessibility_interactive_ui_timeout_ms 20000; }
default_undo_window() { put_or_delete secure accessibility_interactive_ui_timeout_ms "$ORIGINAL_TIMEOUT"; }

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
  set_clock_millis "$(mid_month_millis "$ORIGINAL_ZONE" 23:55)" || return 1
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
  set_zone Europe/Berlin || return 1
  set_clock_millis "$(mid_month_millis Europe/Berlin 23:15)" || return 1
  run_flow travel-before || return 1
  set_zone Asia/Tashkent || return 1
  run_flow travel-after || return 1
}

scenario_travel_west() {
  echo "Scenario travel_west: logged at 01:00 in Tashkent, opened the evening before in Berlin (EC-TIM-02)"
  set_zone Asia/Tashkent || return 1
  set_clock_millis "$(mid_month_millis Asia/Tashkent 01:00)" || return 1
  run_flow log-and-close || return 1
  set_zone Europe/Berlin || return 1
  run_flow travel-west-after || return 1
}

scenario_same_date() {
  echo "Scenario same_date: logged at 08:00 in Tashkent, opened the same date in Dubai (EC-TIM-02)"
  set_zone Asia/Tashkent || return 1
  set_clock_millis "$(mid_month_millis Asia/Tashkent 08:00)" || return 1
  run_flow log-and-close || return 1
  set_zone Asia/Dubai || return 1
  run_flow same-date-after || return 1
}

scenario_zone_attack() {
  echo "Scenario zone_attack: the zone changes while an attack is in progress and the app is in the background"
  set_zone Asia/Dubai || return 1
  run_flow zone-attack-setup || return 1
  adbq shell input keyevent KEYCODE_HOME
  sleep 2
  set_zone Asia/Tashkent || return 1
  open_from_launcher
  run_flow zone-attack-after || return 1
}

scenario_zone_history() {
  echo "Scenario zone_history: the zone changes while History is on screen"
  set_zone Asia/Dubai || return 1
  run_flow zone-history-setup || return 1
  set_zone Asia/Tashkent || return 1
  sleep 2
  run_flow zone-history-after || return 1
}

scenario_med_effect() {
  echo "Scenario med_effect: \"Did it help?\" 2 h after an intake, answered, changed and kept by Undo (MED-F4)"
  set_clock_millis "$(day_millis 0)" || return 1
  run_flow med-attack-intake-setup || return 1
  shift_clock_hours 1 || return 1
  run_flow med-no-question || return 1
  shift_clock_hours 2 || return 1
  long_undo_window
  run_flow med-effect-answer || { default_undo_window; return 1; }
  default_undo_window
}

scenario_med_effect_attack() {
  echo "Scenario med_effect_attack: no question while an attack is in progress (G4)"
  set_clock_millis "$(day_millis 0)" || return 1
  run_flow med-attack-open-setup || return 1
  shift_clock_hours 3 || return 1
  run_flow med-attack-no-question || return 1
}

scenario_med_effect_skip() {
  echo "Scenario med_effect_skip: a skipped question never comes back"
  set_clock_millis "$(day_millis 0)" || return 1
  run_flow med-attack-intake-setup || return 1
  shift_clock_hours 3 || return 1
  run_flow med-skip || return 1
  shift_clock_hours 1 || return 1
  run_flow med-no-question || return 1
}

scenario_med_effect_late() {
  echo "Scenario med_effect_late: the question is still due at 23 h and gone at 25 h"
  set_clock_millis "$(day_millis 0)" || return 1
  run_flow med-attack-intake-setup || return 1
  shift_clock_hours 23 || return 1
  run_flow med-question-due || return 1
  shift_clock_hours 2 || return 1
  run_flow med-no-question || return 1
}

scenario_med_preventive() {
  echo "Scenario med_preventive: a preventive medication is never asked about"
  set_clock_millis "$(day_millis 0)" || return 1
  run_flow med-add-preventive || return 1
  shift_clock_hours 3 || return 1
  run_flow med-no-question || return 1
}

scenario_med_two_waiting() {
  echo "Scenario med_two_waiting: with two intakes waiting, the older one is asked first"
  set_clock_millis "$(day_millis 0)" || return 1
  run_flow clean || return 1
  run_flow_env med-first-log-home NAME=Sumatriptan TYPE=Triptan || return 1
  shift_clock_hours 1 || return 1
  run_flow_env med-add-and-log-home NAME=Ibuprofen "TYPE=Simple painkiller" || return 1
  shift_clock_hours 3 || return 1
  run_flow med-two-waiting || return 1
}

scenario_med_link_home() {
  echo "Scenario med_link_home: an intake logged from home inside a past attack's time joins that attack (EC-MED-09)"
  set_clock_millis "$(day_millis 0)" || return 1
  run_flow med-link-setup || return 1
  shift_clock_hours -1 || return 1
  run_flow_env med-first-log-home NAME=Ibuprofen "TYPE=Simple painkiller" || return 1
  shift_clock_hours 1 || return 1
  run_flow med-link-after || return 1
}

scenario_med_overuse() {
  echo "Scenario med_overuse: painkillers on 13 days warn softly, \"Got it\" hides it, 15 days warn about the doctor"
  local d
  set_clock_millis "$(day_millis 12)" || return 1
  run_flow clean || return 1
  run_flow_env med-first-log-home NAME=Ibuprofen "TYPE=Simple painkiller" || return 1
  for d in 11 10 9 8 7 6 5 4 3 2 1 0 0; do
    set_clock_millis "$(day_millis "$d")" || return 1
    run_flow_env med-log-home NAME=Ibuprofen || return 1
  done
  run_flow med-overuse-soft || return 1
  for d in 13 14; do
    set_clock_millis "$(day_millis "$d")" || return 1
    run_flow_env med-log-home NAME=Ibuprofen || return 1
  done
  set_clock_millis "$(( $(day_millis 0) + 3600000 ))" || return 1
  run_flow med-overuse-doctor || return 1
}

# Covers: day-markers › Accessible marks › Screen reader
# Covers: month-sky › Days reachable with a keyboard › One Tab stop
# Covers: month-sky › Days reachable with a keyboard › Entering on today
# Covers: month-sky › Arrow keys move between days › Arrow keys
# Covers: month-sky › Arrow keys move between days › Arrow keys stay on selectable days
# Covers: quality-gates › Keyboard paths are checked with key events › One Tab stop for the month
# Covers: quality-gates › Keyboard paths are checked with key events › Arrows inside the month
# Covers: quality-gates › Keyboard paths are checked with key events › Enter on a day
scenario_keyboard_home() {
  echo "Scenario keyboard_home: home's month is one Tab stop with arrows, on the 24th (#22, D38, D62)"
  set_clock_millis "$(day_millis -9)" || return 1
  run_flow clean || return 1
  run_flow home-ready || return 1
  expect_focus TAB "^Period today" || return 1
  # Enter toggles the chip; accessibility services read it as "Period today", checked (day-markers › Screen reader).
  key ENTER
  expect_checked true || return 1
  key ENTER
  expect_checked false || return 1
  expect_focus TAB "Opens History\.$" || return 1
  expect_focus TAB "^Today, .*$(day_re 24)" || return 1
  expect_focus TAB "^Migraine started" || return 1
  expect_focus SHIFT_TAB "^Today, " || return 1
  expect_focus DPAD_RIGHT "^Today, " || return 1
  expect_focus DPAD_DOWN "^Today, " || return 1
  expect_focus DPAD_UP "$(day_re 17)" || return 1
  expect_focus DPAD_LEFT "$(day_re 16)" || return 1
  expect_focus DPAD_LEFT "$(day_re 15)" || return 1
  expect_focus DPAD_LEFT "$(day_re 14)" || return 1
  expect_focus DPAD_RIGHT "$(day_re 15)" || return 1
  expect_focus DPAD_DOWN "$(day_re 22)" || return 1
  expect_focus TAB "^Migraine started" || return 1
  expect_focus SHIFT_TAB "^Today, " || return 1
  expect_focus SHIFT_TAB "Opens History\.$" || return 1
  expect_focus SHIFT_TAB "^Period today" || return 1
  expect_focus TAB "Opens History\.$" || return 1
  expect_focus TAB "^Today, " || return 1
  expect_focus DPAD_UP "$(day_re 17)" || return 1
  key ENTER
  run_flow_env keyboard-day-chosen DAY=17 || return 1
}

# Covers: month-sky › Days reachable with a keyboard › Tab through the days
scenario_keyboard_history() {
  echo "Scenario keyboard_history: History's month is one Tab stop; Enter chooses a day and Tab comes back to it (#22)"
  set_clock_millis "$(day_millis -9)" || return 1
  run_flow log-and-close || return 1
  run_flow home-ready || return 1
  # With today's attack, its row comes between the "Period" chip and the month.
  tab_until "Opens History\.$" 4 || return 1
  key ENTER
  run_flow history-ready || return 1
  tab_until "^Today, .*$(day_re 24)" 4 || return 1
  expect_focus DPAD_LEFT "$(day_re 23)" || return 1
  key ENTER
  run_flow_env keyboard-day-chosen DAY=23 || return 1
  # Maestro does not move the focus, so it is still on the 23rd. The next Tab leaves the month, and Shift+Tab comes back
  # to the chosen day, not to today.
  local label
  key TAB
  sleep 1
  label="$(focused_label)"
  if [[ "$label" =~ $DAY_CELL ]]; then echo "  FAILED: Tab stayed in the month ('$label')"; return 1; fi
  echo "    TAB -> $label"
  expect_focus SHIFT_TAB "[^0-9]23$DAY_CELL" || return 1
}

# The mirrored arrows are in the requirement "Arrow keys move between days"; it has no right-to-left scenario.
scenario_keyboard_rtl() {
  echo "Scenario keyboard_rtl: the arrows are mirrored in a right-to-left language (Hebrew), on the 24th (D38)"
  set_clock_millis "$(day_millis -9)" || return 1
  run_flow clean || return 1
  adbq shell cmd locale set-app-locales "$APP" --locales iw >/dev/null
  run_flow home-ready || { adbq shell cmd locale set-app-locales "$APP" --locales "$ORIGINAL_APP_LOCALES"; return 1; }
  local ok=0
  expect_focus TAB "^Period today" && expect_focus TAB "Opens History\.$" && expect_focus TAB "^Today, .*$(day_re 24)" &&
    expect_focus DPAD_RIGHT "$(day_re 23)" && expect_focus DPAD_LEFT "$(day_re 24)" &&
    expect_focus DPAD_LEFT "$(day_re 24)" || ok=1
  adbq shell cmd locale set-app-locales "$APP" --locales "$ORIGINAL_APP_LOCALES" >/dev/null
  return $ok
}

# Partly checks: in-app-messages › Landscape attack mode keeps messages beside the controls › Message during landscape
# attack mode. Not checked: that the guidance moves "at once" with Reduce motion (timing) and taps under the message.
scenario_landscape_band() {
  echo "Scenario landscape_band: in landscape attack mode the guidance starts below the message and comes back (#26, D52)"
  local motion ok=0
  for motion in animated reduced; do
    echo "  motion: $motion"
    local scale=1.0
    [[ "$motion" == reduced ]] && scale=0.0
    adbq shell settings put global animator_duration_scale "$scale"
    if [[ "$(adbq shell settings get global animator_duration_scale | tr -d '\r')" != "$scale" ]]; then
      echo "  FAILED: the animator scale did not change to $scale"
      put_or_delete global animator_duration_scale "$ORIGINAL_ANIMATOR"
      return 1
    fi
    long_undo_window
    { run_flow landscape-attack-message && band_check below && run_flow landscape-message-gone && band_check back; } ||
      ok=1
    # Portrait and the default settings again, also after a failure, so the next scenario starts as usual.
    run_flow portrait
    put_or_delete global animator_duration_scale "$ORIGINAL_ANIMATOR"
    default_undo_window
    [[ $ok -eq 0 ]] || return 1
  done
}

# Covers: day-markers › Period on any day › After travel
# Covers: quality-gates › Device scenarios without test code › Day mark after travel
scenario_mark_travel() {
  echo "Scenario mark_travel: a period day marked in Tashkent stays on its date in Berlin and Pago Pago (EC-DAY-04)"
  set_zone Asia/Tashkent || return 1
  # Noon on the 15th in Tashkent: 08:00 or 09:00 on the 15th in Berlin, 20:00 on the 14th in Pago Pago.
  set_clock_millis "$(mid_month_millis Asia/Tashkent 12:00)" || return 1
  run_flow mark-period-today || return 1
  set_zone Europe/Berlin || return 1
  run_flow mark-travel-berlin || return 1
  set_zone Pacific/Pago_Pago || return 1
  run_flow mark-travel-pago || return 1
}

# Covers: attack-history › Month by month › A mark before any attack
scenario_mark_earliest_month() {
  echo "Scenario mark_earliest_month: a period marked last month, before any migraine, keeps last month in History (D51)"
  set_clock_millis "$(day_millis 30)" || return 1
  run_flow mark-period-today || return 1
  set_clock_millis "$(day_millis 0)" || return 1
  run_flow earliest-month-after || return 1
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
    double_tap med_effect med_effect_attack med_effect_skip med_effect_late med_preventive med_two_waiting \
    med_link_home med_overuse keyboard_home keyboard_history keyboard_rtl landscape_band mark_travel mark_earliest_month
  if [[ -n "${QA_OLD_APK:-}" ]]; then
    set -- "$@" upgrade
  else
    echo "Note: upgrade is not in this run; set QA_OLD_APK to the older APK to include it."
  fi
fi
failed=()
for s in "$@"; do
  CURRENT="$s"
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
