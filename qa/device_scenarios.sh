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
# landscape_band, mark_travel, mark_earliest_month, stats_counting, stats_chronic, stats_progress, reminder_daily,
# reminder_permission, reminder_effect, reminder_replan (restarts the emulator), upgrade.
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
qa_check_baseline
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
  qa_restore_setting global auto_time "$ORIGINAL_AUTO_TIME"
  qa_restore_setting secure accessibility_interactive_ui_timeout_ms "$ORIGINAL_TIMEOUT"
  adbq shell cmd locale set-app-locales "$APP" --locales "$ORIGINAL_APP_LOCALES" >/dev/null 2>&1
  qa_restore_setting system user_rotation "$ORIGINAL_USER_ROTATION"
  qa_restore_setting system accelerometer_rotation "$ORIGINAL_AUTO_ROTATE"
  qa_restore_setting global animator_duration_scale "$ORIGINAL_ANIMATOR"
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

# Notifications (D64). WorkManager runs the app's reminders as JobScheduler jobs in its own namespace; a job is run on
# demand, and the notifications are read from "dumpsys notification".
run_app_jobs() { # run every job the app has planned, now; prints how many ran
  local ids id n=0
  ids="$(adbq shell dumpsys jobscheduler | python3 -I -c "import re, sys
print(' '.join(sorted(set(re.findall(r'systemjobscheduler:u0a\d+/(\d+) @androidx\.work\.systemjobscheduler@$APP/', sys.stdin.read())))))")"
  for id in $ids; do
    adbq shell cmd jobscheduler run -f -n androidx.work.systemjobscheduler "$APP" "$id" >/dev/null
    n=$((n + 1))
  done
  sleep 5
  echo "    ran $n planned job(s)"
}
app_notifications() { # one line per notification of the app: "title | text | action, action"
  adbq shell dumpsys notification --noredact | python3 -I -c "import re, sys
for block in re.split(r'\n\s*NotificationRecord\(', sys.stdin.read())[1:]:
    if 'pkg=$APP' not in block[:400]:
        continue
    title = re.search(r'android\.title=\S+ \((.*?)\)\n', block)
    text = re.search(r'android\.text=\S+ \((.*?)\)\n', block)
    actions = re.findall(r'\[\d+\] \"(.*?)\" ->', block)
    print((title.group(1) if title else ''), '|', (text.group(1) if text else ''), '|', ', '.join(actions))"
}
expect_notification() { # $1 extended regex for "title | text | actions"
  local found
  found="$(app_notifications)"
  if grep -Eq "$1" <<<"$found"; then echo "    notification: $(grep -E "$1" <<<"$found" | head -1)"; return 0; fi
  echo "  FAILED: no notification matching /$1/ (found: '${found:-none}')"
  return 1
}
expect_no_notification() {
  local found
  found="$(app_notifications)"
  if [[ -z "$found" ]]; then echo "    no notification"; return 0; fi
  echo "  FAILED: unexpected notification '$found'"
  return 1
}
tap_notification_action() { # $1 the action's label, $2 the notification's title (regex); tapped in the open shade
  local point title
  adbq shell cmd statusbar expand-notifications
  sleep 2
  point="$(point_in "$1" 0.5)"
  if [[ -z "$point" ]]; then
    # Collapsed under another notification: a swipe down on its title shows the actions.
    title="$(point_in "$2" 0.5)"
    if [[ -n "$title" ]]; then
      adbq shell input swipe $title ${title% *} $((${title#* } + 400)) 300
      sleep 2
      point="$(point_in "$1" 0.5)"
    fi
  fi
  [[ -n "$point" ]] || { adbq shell cmd statusbar collapse; echo "  FAILED: no \"$1\" in the shade"; return 1; }
  adbq shell input tap $point
  sleep 3
  adbq shell cmd statusbar collapse
  echo "    tapped \"$1\" in the notification"
}
notifications_permission() { # $1 grant or reset (revoked and asked as if for the first time)
  if [[ "$1" == "grant" ]]; then
    adbq shell pm grant "$APP" android.permission.POST_NOTIFICATIONS
  else
    adbq shell pm revoke "$APP" android.permission.POST_NOTIFICATIONS
    adbq shell pm clear-permission-flags "$APP" android.permission.POST_NOTIFICATIONS user-set user-fixed
  fi
}

expect_planned() { # $1 zone, $2 HH:MM: the app's daily job runs at the next $2 in $1 (within 5 min)
  local got want
  got="$(adbq shell dumpsys jobscheduler | python3 -I -c "import re, sys
t = sys.stdin.read()
m = re.search(r'JOB [^\n]*@androidx\.work\.systemjobscheduler@$APP/[^\n]*\n(.*?)\n  JOB ', t + '\n  JOB ', re.S)
e = m and re.search(r'Run time: earliest=([-+])(?:(\d+)d)?(?:(\d+)h)?(?:(\d+)m)?', m.group(1))
print('' if not e else (1 if e.group(1) == '+' else -1) * (int(e.group(2) or 0) * 1440 + int(e.group(3) or 0) * 60 + int(e.group(4) or 0)))")"
  want="$(python3 -I -c "import sys, datetime as d, zoneinfo as z
zone = z.ZoneInfo(sys.argv[1]); now = d.datetime.fromtimestamp(int(sys.argv[3]), zone)
h, m = map(int, sys.argv[2].split(':')); t = now.replace(hour=h, minute=m, second=0, microsecond=0)
t = t if t > now else t + d.timedelta(days=1)
print(int((t - now).total_seconds() // 60))" "$1" "$2" "$(device_seconds)")"
  if [[ -z "$got" ]]; then echo "  FAILED: no planned daily job"; return 1; fi
  if (( got - want > 5 || want - got > 5 )); then
    echo "  FAILED: the daily job runs in $got min, expected about $want min (the next $2 in $1)"
    return 1
  fi
  echo "    daily job planned in $got min (the next $2 in $1: $want min)"
}
expect_switch() { # $1 the switch's label (regex), $2 true or false: its checked state on screen
  local state
  adbq shell rm -f /sdcard/qa-switch.xml
  adbq shell uiautomator dump /sdcard/qa-switch.xml >/dev/null
  state="$(adbq exec-out cat /sdcard/qa-switch.xml | python3 -I -c "import re, sys, xml.etree.ElementTree as ET
for n in ET.fromstring(sys.stdin.read()).iter('node'):
    labels = ' '.join(m.get('content-desc') or m.get('text') or '' for m in n.iter('node'))
    if n.get('checkable') == 'true' and re.search(sys.argv[1], labels):
        print(n.get('checked')); break" "$1")"
  if [[ "$state" == "$2" ]]; then echo "    \"$1\" checked: $state"; return 0; fi
  echo "  FAILED: \"$1\" is checked=${state:-not found}, expected $2"
  return 1
}
open_app() { adbq shell monkey -p "$APP" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 4; }

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
when_millis() { # $1 months before this month, $2 day of the month or "last", $3 HH:MM, in the device's zone
  # "This month" is the month of MED_BASE_MILLIS, computed once, so a long scenario never straddles two months.
  python3 -I -c "import sys, calendar, datetime as d, zoneinfo as z
zone = z.ZoneInfo(sys.argv[5]); base = d.datetime.fromtimestamp(int(sys.argv[4]) / 1000, zone)
y, m = base.year, base.month - int(sys.argv[1])
while m < 1: y, m = y - 1, m + 12
day = calendar.monthrange(y, m)[1] if sys.argv[2] == 'last' else int(sys.argv[2])
h, mi = map(int, sys.argv[3].split(':'))
print(int(d.datetime(y, m, day, h, mi, tzinfo=zone).timestamp() * 1000))" "$1" "$2" "$3" "$MED_BASE_MILLIS" "$ORIGINAL_ZONE"
}
long_attack() { # $1 months back, $2 first day, $3 last day: a migraine from 10:00 on the first day to 10:00 on the last
  set_clock_millis "$(when_millis "$1" "$2" 10:00)" || return 1
  run_flow_env attack-start LEVEL= || return 1
  set_clock_millis "$(when_millis "$1" "$3" 10:00)" || return 1
  run_flow attack-end || return 1
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

# Covers: medication-overuse › A standing indicator in History › At the limit
# Covers: medication-overuse › A standing indicator in History › Two categories
# Covers: medication-overuse › A standing indicator in History › After an undo
# Covers: medication-overuse › A standing indicator in History › A past month
scenario_med_overuse() {
  echo "Scenario med_overuse: painkillers on 13 days warn softly, \"Got it\" hides it, 15 days warn about the doctor; History's \"Last 30 days\" (D59)"
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
  # History's "Last 30 days" (D59). History shows its month summary, and so the section, once a migraine is logged.
  run_flow attack-log-keep || return 1
  # At the limit with 15 days; a day 30 days back does not count, a day 29 back does.
  run_flow_env indicator-check MONTHS_BACK=0 "ROW=Simple painkillers: 15 days \(limit 15\), at the limit" DOCTOR=true || return 1
  set_clock_millis "$(day_millis 30)" || return 1
  run_flow_env med-log-home NAME=Ibuprofen || return 1
  set_clock_millis "$(( $(day_millis 0) + 7200000 ))" || return 1
  run_flow_env indicator-check MONTHS_BACK=0 "ROW=Simple painkillers: 15 days \(limit 15\), at the limit" DOCTOR=true || return 1
  set_clock_millis "$(day_millis 29)" || return 1
  run_flow_env med-log-home NAME=Ibuprofen || return 1
  set_clock_millis "$(( $(day_millis 0) + 7200000 ))" || return 1
  run_flow_env indicator-check MONTHS_BACK=0 "ROW=Simple painkillers: 16 days \(limit 15\), over the limit" DOCTOR=true || return 1
  # A triptan in an attack today: two categories, and the union of days over its limit.
  run_flow med-add-triptan-in-attack || return 1
  run_flow_env indicator-check MONTHS_BACK=0 "ROW=Triptans: 1 day \(limit 10\)" DOCTOR=true || return 1
  run_flow_env indicator-check MONTHS_BACK=0 "ROW=All attack medication: 16 days \(limit 10\), over the limit" DOCTOR=true || return 1
  # Deleting that attack and restoring it with Undo counts its triptan again.
  long_undo_window
  run_flow delete-attack-undo || { default_undo_window; return 1; }
  default_undo_window
  run_flow_env indicator-check MONTHS_BACK=0 "ROW=Triptans: 1 day \(limit 10\)" DOCTOR=true || return 1
  # Last month (with a migraine of its own, so History reaches it) has no "Last 30 days" section.
  run_flow past-attack-last-month || return 1
  run_flow_env indicator-check MONTHS_BACK=1 ROW= DOCTOR=false || return 1
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
  # The header's "Settings" gear is the first stop (change 7).
  expect_focus TAB "^Settings" || return 1
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
  expect_focus SHIFT_TAB "^Settings" || return 1
  expect_focus TAB "^Period today" || return 1
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
  # The gear, the "Period" chip and today's attack row come before the month.
  tab_until "Opens History\.$" 5 || return 1
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
  expect_focus TAB "^Settings" && expect_focus TAB "^Period today" && expect_focus TAB "Opens History\.$" &&
    expect_focus TAB "^Today, .*$(day_re 24)" &&
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

# Covers: month-stats › How the numbers are counted › An attack over two months
# Covers: month-stats › Migraines started › An attack from the month before
# Covers: month-stats › How the numbers are counted › A pain-free mark under an attack
scenario_stats_counting() {
  echo "Scenario stats_counting: a migraine over two months; a pain-free mark under a later migraine (D57, D58)"
  set_clock_millis "$(when_millis 1 last 22:00)" || return 1
  run_flow clean || return 1
  run_flow_env attack-start LEVEL= || return 1
  set_clock_millis "$(when_millis 0 1 03:00)" || return 1
  run_flow attack-end || return 1
  run_flow stats-two-months-after || return 1
  set_clock_millis "$(when_millis 0 10 12:00)" || return 1
  run_flow mark-pain-free-today || return 1
  set_clock_millis "$(when_millis 0 12 12:00)" || return 1
  run_flow_env pain-free-attack-later DAY=10 || return 1
}

# Covers: month-stats › The chronic indicator › Fifteen migraine days
# Covers: month-stats › The chronic indicator › Fourteen migraine days
scenario_stats_chronic() {
  echo "Scenario stats_chronic: the chronic card at 15 migraine days, none at 14 (D54)"
  set_clock_millis "$(when_millis 0 1 09:00)" || return 1
  run_flow clean || return 1
  long_attack 0 1 15 || return 1
  run_flow_env chronic-check DAYS=15 SHOWN=true || return 1
  set_clock_millis "$(when_millis 0 2 09:00)" || return 1
  run_flow clean || return 1
  long_attack 0 2 15 || return 1
  run_flow_env chronic-check DAYS=14 SHOWN=false || return 1
}

# Covers: month-stats › The progress message › Better than last month
# Covers: month-stats › The progress message › Too few entries
# Covers: month-stats › The progress message › Worse
# Covers: month-stats › The progress message › The current month
# Covers: month-stats › Progress on home › Home in normal mode
# Covers: month-stats › Progress on home › Attack mode
scenario_stats_progress() {
  echo "Scenario stats_progress: the progress message against last month, its threshold, and on home (EC-UX-14, D55)"
  # A: 12, then 10 days (2 fewer), then 8 days this month (too few entries: no message).
  set_clock_millis "$(when_millis 2 1 09:00)" || return 1
  run_flow clean || return 1
  long_attack 2 1 12 || return 1
  long_attack 1 1 10 || return 1
  long_attack 0 1 8 || return 1
  set_clock_millis "$(when_millis 0 15 12:00)" || return 1
  run_flow_env progress-check MONTHS_BACK=1 "TEXT=2 fewer migraine days than [A-Z][a-z]+" || return 1
  run_flow_env progress-check MONTHS_BACK=0 TEXT= || return 1
  # B: 10, then 12 days (2 more), then 11 days by the 15th against 12 by the 15th last month (1 fewer).
  set_clock_millis "$(when_millis 2 1 09:00)" || return 1
  run_flow clean || return 1
  long_attack 2 1 10 || return 1
  long_attack 1 1 12 || return 1
  long_attack 0 1 11 || return 1
  set_clock_millis "$(when_millis 0 15 12:00)" || return 1
  run_flow_env progress-check MONTHS_BACK=1 "TEXT=2 more migraine days than [A-Z][a-z]+" || return 1
  run_flow_env progress-check MONTHS_BACK=0 "TEXT=1 fewer migraine day than this time last month" || return 1
  run_flow_env progress-home "TEXT=1 fewer migraine day than this time last month" ATTACK=false || return 1
  run_flow_env attack-start LEVEL= || return 1
  run_flow_env progress-home "TEXT=1 fewer migraine day than this time last month" ATTACK=true || return 1
}

# Covers: reminders › The daily reminder › A calm day
# Covers: reminders › The daily reminder › Already answered
# Covers: reminders › Answering from the notification › Pain-free day
# Covers: reminders › Answering from the notification › An attack today
# Covers: quality-gates › Notifications are checked black-box › Daily reminder on demand
scenario_reminder_daily() {
  echo "Scenario reminder_daily: \"How was today?\" at 8:00 PM, its actions, and the days it stays away (D66, EC-REM-06)"
  notifications_permission grant
  # The 15th: a calm day. The reminder comes, and "Pain-free day" marks the day and clears it.
  set_clock_millis "$(when_millis 0 15 19:58)" || return 1
  run_flow daily-reminder-on || return 1
  run_app_jobs
  expect_notification "^How was today\? \|.*\| Pain-free day, Log a migraine$" || return 1
  tap_notification_action "Pain-free day" "How was today\?" || return 1
  expect_no_notification || return 1
  run_flow_env today-check "LABEL=Today: clear\. Pain-free today\. Marked by you\." || return 1
  # The 16th: a migraine today, so no reminder. The 17th: already marked pain-free, so no reminder.
  set_clock_millis "$(when_millis 0 16 19:50)" || return 1
  run_flow_env attack-start LEVEL= || return 1
  run_flow attack-end || return 1
  set_clock_millis "$(when_millis 0 16 19:58)" || return 1
  run_flow app-open || return 1
  run_app_jobs
  expect_no_notification || return 1
  set_clock_millis "$(when_millis 0 17 19:50)" || return 1
  run_flow mark-pain-free-today || return 1
  set_clock_millis "$(when_millis 0 17 19:58)" || return 1
  run_flow app-open || return 1
  run_app_jobs
  expect_no_notification || return 1
  # The 18th: the reminder comes, then a migraine is logged; "Pain-free day" then sets no mark (EC-REM-06).
  set_clock_millis "$(when_millis 0 18 19:58)" || return 1
  run_flow app-open || return 1
  run_app_jobs
  expect_notification "^How was today\?" || return 1
  run_flow attack-log-keep || return 1
  tap_notification_action "Pain-free day" "How was today\?" || return 1
  expect_notification "A migraine is logged today\." || return 1
  run_flow_env today-check "LABEL=Today: cloudy\..*" || return 1
  # The 19th: "Log a migraine" opens home and clears the notification.
  adbq shell cmd notification cancel_all >/dev/null 2>&1 || true
  set_clock_millis "$(when_millis 0 19 19:58)" || return 1
  run_flow app-open || return 1
  run_app_jobs
  expect_notification "^How was today\?" || return 1
  tap_notification_action "Log a migraine" "How was today\?" || return 1
  expect_no_notification || return 1
  run_flow home-ready || return 1
}

# Covers: reminders › Notification permission › Turning the daily reminder on
# Covers: reminders › Notification permission › Refused
# Covers: reminders › The Reminders screen › Opening Reminders
# Covers: quality-gates › Notifications are checked black-box › Refused permission
scenario_reminder_permission() {
  echo "Scenario reminder_permission: asked only when turned on; after a refusal, asked again only after 30 days (D68)"
  local start
  start="$(when_millis 0 1 12:00)"
  set_clock_millis "$start" || return 1
  run_flow clean || return 1
  notifications_permission reset
  open_app
  run_flow_env reminders-turn-on "CHOICE=Don.t allow" || return 1
  expect_switch "Daily reminder" false || return 1
  key BACK; key BACK
  # Asked again neither the same day nor 29 days later.
  run_flow_env reminders-turn-on CHOICE=none || return 1
  expect_switch "Daily reminder" false || return 1
  adbq shell input keyevent HOME
  set_clock_millis $((start + 29 * 86400000)) || return 1
  open_app
  run_flow_env reminders-turn-on CHOICE=none || return 1
  adbq shell input keyevent HOME
  # 31 days later it asks again; allowed, the reminder is on.
  set_clock_millis $((start + 31 * 86400000)) || return 1
  open_app
  run_flow_env reminders-turn-on CHOICE=Allow || return 1
  expect_switch "Daily reminder" true || return 1
  adbq shell dumpsys package "$APP" | grep -q "POST_NOTIFICATIONS: granted=true" || {
    echo "  FAILED: the permission is not granted"; return 1; }
  adbq shell input keyevent HOME
  notifications_permission reset
}

# Covers: reminders › The daily reminder › Travel
# Covers: quality-gates › Notifications are checked black-box › After a reboot
scenario_reminder_replan() {
  echo "Scenario reminder_replan: the daily reminder follows the local time after travel, and survives a reboot (EC-REM-04, 05)"
  notifications_permission grant
  set_zone Asia/Tashkent || return 1
  set_clock_millis "$(mid_month_millis Asia/Tashkent 12:00)" || return 1
  run_flow daily-reminder-on || return 1
  expect_planned Asia/Tashkent 20:00 || return 1
  set_zone Europe/Berlin || return 1
  run_flow app-open || return 1
  expect_planned Europe/Berlin 20:00 || return 1
  echo "    rebooting the emulator"
  adbq reboot
  adbq wait-for-device
  local i
  for i in $(seq 1 60); do [[ "$(adbq shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]] && break; sleep 3; done
  [[ "$(adbq shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]] || { echo "  FAILED: no boot after 3 min"; return 1; }
  sleep 15
  adbq shell dumpsys jobscheduler | grep -q "@androidx.work.systemjobscheduler@$APP/" || {
    echo "  FAILED: no planned job after the reboot (the app was not opened)"; return 1; }
  echo "    the daily job is planned again after the reboot, before the app is opened"
}

# Covers: reminders › "Did it help?" notification › Two hours later
# Covers: reminders › "Did it help?" notification › During an attack
# Covers: reminders › "Did it help?" notification › Answered first
scenario_reminder_effect() {
  echo "Scenario reminder_effect: \"Did it help?\" 2 h after an intake, never during an attack, and not after answering (D67, D69)"
  notifications_permission grant
  set_clock_millis "$(when_millis 0 14 09:00)" || return 1
  run_flow clean || return 1
  run_flow_env med-first-log-home NAME=Ibuprofen "TYPE=Simple painkiller" || return 1
  # The 15th: logged at 14:00, asked at 16:00.
  set_clock_millis "$(when_millis 0 15 14:00)" || return 1
  run_flow_env med-log-keep NAME=Ibuprofen || return 1
  set_clock_millis "$(when_millis 0 15 16:01)" || return 1
  run_app_jobs
  expect_notification "^Did Ibuprofen help\?" || return 1
  adbq shell am force-stop "$APP"
  # The 16th: an attack starts after the intake; nothing is sent, and home asks once it ends.
  set_clock_millis "$(when_millis 0 16 14:00)" || return 1
  run_flow_env med-log-keep NAME=Ibuprofen || return 1
  set_clock_millis "$(when_millis 0 16 14:30)" || return 1
  run_flow_env attack-start LEVEL= || return 1
  run_flow app-open || return 1
  set_clock_millis "$(when_millis 0 16 16:01)" || return 1
  run_app_jobs
  expect_no_notification || return 1
  run_flow attack-end || return 1
  run_flow med-question-due || return 1
  # The 17th: answered on home before the notification would come; nothing is sent.
  set_clock_millis "$(when_millis 0 17 14:00)" || return 1
  run_flow_env med-log-keep NAME=Ibuprofen || return 1
  set_clock_millis "$(when_millis 0 17 16:00)" || return 1
  run_flow_env effect-answer-card NAME=Ibuprofen || return 1
  set_clock_millis "$(when_millis 0 17 16:01)" || return 1
  run_app_jobs
  expect_no_notification || return 1
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
    med_link_home med_overuse keyboard_home keyboard_history keyboard_rtl landscape_band mark_travel mark_earliest_month \
    stats_counting stats_chronic stats_progress reminder_daily reminder_permission reminder_effect reminder_replan
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
