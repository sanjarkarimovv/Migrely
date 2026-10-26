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
# reminder_permission, reminder_effect, reminder_replan (restarts the emulator), med_only_history,
# message_tap_guard (#52), theme_check, onboarding_states, delete_all_data, backup_export, backup_import,
# message_duration (#56), premium_store_states, premium_pending, premium_plans, premium_entitlement, premium_offline,
# trial_banner, paywall_self_show, report_three_months, report_counting, report_heavy, insights, upgrade, release_apk.
# On demand only: backup_android (bmgr).
# QA_APK=<apk> names the build under test (default: this worktree's debug APK), for example a release branch's.
# release_apk needs QA_RELEASE_APK=<release APK>; without it, it is reported as not run.
# upgrade needs QA_OLD_APK=<older debug APK of com.migrely.app>; without it, it is reported as not run.
set -euo pipefail

QA_SERIAL="emulator-5580"
APP="com.migrely.app"
export MAESTRO_CLI_NO_ANALYTICS=1
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APK="${QA_APK:-$ROOT/app/build/outputs/apk/debug/app-debug.apk}"
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
# The checks that read the APK (D113, D115) and the reinstalls must be about the build on the phone.
AAPT2="$(ls -d "${ANDROID_HOME:-$HOME/Library/Android/sdk}"/build-tools/*/aapt2 | sort -V | tail -1)"
BUILT_VERSION="$("$AAPT2" dump badging "$APK" | sed -nE "1s/.*versionCode='([0-9]+)'.*versionName='([^']*)'.*/\1 \2/p")"
INSTALLED_PACKAGE="$(adbq shell dumpsys package "$APP" | tr -d '\r')"
INSTALLED_VERSION="$(sed -nE 's/^ *versionCode=([0-9]+).*/\1/p' <<<"$INSTALLED_PACKAGE" | head -1) $(sed -nE 's/^ *versionName=(.*)/\1/p' <<<"$INSTALLED_PACKAGE" | head -1)"
if [[ "$BUILT_VERSION" != "$INSTALLED_VERSION" ]]; then
  echo "The phone has $APP $INSTALLED_VERSION, but the APK under test is $BUILT_VERSION ($APK)."
  echo "Install it (qa/maestro.sh --install) or set QA_APK to the APK that is installed."
  exit 2
fi
ORIGINAL_AUTO_TIME="$(adbq shell settings get global auto_time | tr -d '\r')"
ORIGINAL_ZONE="$(adbq shell getprop persist.sys.timezone | tr -d '\r')"
ORIGINAL_TIMEOUT="$(adbq shell settings get secure accessibility_interactive_ui_timeout_ms | tr -d '\r')"
ORIGINAL_TEXT_TIMEOUT="$(adbq shell settings get secure accessibility_non_interactive_ui_timeout_ms | tr -d '\r')"
ORIGINAL_USER_ROTATION="$(adbq shell settings get system user_rotation | tr -d '\r')"
ORIGINAL_AUTO_ROTATE="$(adbq shell settings get system accelerometer_rotation | tr -d '\r')"
ORIGINAL_ANIMATOR="$(adbq shell settings get global animator_duration_scale | tr -d '\r')"
ORIGINAL_NIGHT="$(adbq shell cmd uimode night 2>/dev/null | tr -d '\r' | sed 's/.*: //')"
ORIGINAL_NOTIFICATIONS="$(adbq shell dumpsys package "$APP" | grep -q "POST_NOTIFICATIONS: granted=true" && echo grant || echo reset)"
# "Locales for <app> for user 0 are [iw]" -> "iw"; empty means the app follows the system language.
ORIGINAL_APP_LOCALES=""
locales_line="$(adbq shell cmd locale get-app-locales "$APP" 2>/dev/null | tr -d '\r' || true)"
if [[ "$locales_line" =~ \[(.*)\] ]]; then ORIGINAL_APP_LOCALES="${BASH_REMATCH[1]}"; fi
put_or_delete() { # $1 namespace, $2 key, $3 value or "null"
  if [[ "$3" == "null" ]]; then adbq shell settings delete "$1" "$2" >/dev/null; else adbq shell settings put "$1" "$2" "$3"; fi
}
restore() {
  set +e # restore everything even if one command fails
  # After a reboot (reminder_replan) the device may still be starting.
  local wait
  for wait in $(seq 1 60); do [[ "$(adbq shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]] && break; sleep 3; done
  adbq shell cmd alarm set-timezone "$ORIGINAL_ZONE" >/dev/null 2>&1
  adbq shell cmd alarm set-time "$(($(date +%s) * 1000))" >/dev/null 2>&1
  qa_restore_setting global auto_time "$ORIGINAL_AUTO_TIME"
  qa_restore_setting secure accessibility_interactive_ui_timeout_ms "$ORIGINAL_TIMEOUT"
  qa_restore_setting secure accessibility_non_interactive_ui_timeout_ms "$ORIGINAL_TEXT_TIMEOUT"
  adbq shell cmd locale set-app-locales "$APP" --locales "$ORIGINAL_APP_LOCALES" >/dev/null 2>&1
  qa_restore_setting system user_rotation "$ORIGINAL_USER_ROTATION"
  qa_restore_setting system accelerometer_rotation "$ORIGINAL_AUTO_ROTATE"
  qa_restore_setting global animator_duration_scale "$ORIGINAL_ANIMATOR"
  notifications_permission "$ORIGINAL_NOTIFICATIONS" >/dev/null 2>&1
  [[ -n "$ORIGINAL_NIGHT" ]] && adbq shell cmd uimode night "$ORIGINAL_NIGHT" >/dev/null 2>&1
  # The screen dumps this script writes to shared storage would show in the phone's Files app.
  adbq shell "rm -f /sdcard/qa-*.xml" >/dev/null 2>&1
  # Files the backup scenarios put in Download.
  adbq shell "rm -f /sdcard/Download/migrely-* /sdcard/Download/seed-*.json /sdcard/Download/broken.json /sdcard/Download/newer.json /sdcard/Download/older.json /sdcard/Download/add.json" >/dev/null 2>&1
  # A scenario stopped half-way may leave the simulated store unavailable or unreachable.
  adbq shell am broadcast -p "$APP" -f 0x20 -a com.migrely.app.debug.SIMULATED_STORE --es command available >/dev/null 2>&1
  if [[ -n "${BMGR_TRANSPORT:-}" ]]; then
    adbq shell bmgr transport "$BMGR_TRANSPORT" >/dev/null 2>&1
    [[ "$BMGR_ENABLED" == *disabled* ]] && adbq shell bmgr enable false >/dev/null 2>&1
    adbq shell settings delete secure backup_local_transport_parameters >/dev/null 2>&1
  fi
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
run_app_jobs() { # $1 the fewest jobs that must run (default 0): run every job the app has planned, now
  local ids id n=0
  ids="$(adbq shell dumpsys jobscheduler | python3 -I -c "import re, sys
print(' '.join(sorted(set(re.findall(r'systemjobscheduler:u0a\d+/(\d+) @androidx\.work\.systemjobscheduler@$APP/', sys.stdin.read())))))")"
  for id in $ids; do
    adbq shell cmd jobscheduler run -f -n androidx.work.systemjobscheduler "$APP" "$id" >/dev/null
    n=$((n + 1))
  done
  sleep 5
  echo "    ran $n planned job(s)"
  if (( n < ${1:-0} )); then echo "  FAILED: expected at least $1 planned job(s)"; return 1; fi
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
  local point title attempt
  # The shade is sometimes still drawing (for example right after a boot), so it is opened up to twice.
  for attempt in 1 2; do
    adbq shell cmd statusbar expand-notifications
    sleep 3
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
    [[ -n "$point" ]] && break
    adbq shell cmd statusbar collapse
    sleep 2
  done
  if [[ -z "$point" ]]; then
    adbq exec-out uiautomator dump /dev/tty 2>/dev/null | python3 -I -c "import re, sys
labels = [l for l in re.findall(r'(?:text|content-desc)=\"([^\"]+)\"', sys.stdin.read()) if l.strip()]
print('    shade shows:', ' | '.join(labels[:25]))"
    adbq shell cmd statusbar collapse
    echo "  FAILED: no \"$1\" in the shade"
    return 1
  fi
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
t = t + '\n  JOB '
for m in re.finditer(r'JOB [^\n]*@androidx\.work\.systemjobscheduler@$APP/[^\n]*\n(.*?)(?=\n  JOB )', t, re.S):
    e = re.search(r'Run time: earliest=([-+])(?:(\d+)d)?(?:(\d+)h)?(?:(\d+)m)?', m.group(1))
    if e: print((1 if e.group(1) == '+' else -1) * (int(e.group(2) or 0) * 1440 + int(e.group(3) or 0) * 60 + int(e.group(4) or 0)))")"
  want="$(python3 -I -c "import sys, datetime as d, zoneinfo as z
zone = z.ZoneInfo(sys.argv[1]); now = d.datetime.fromtimestamp(int(sys.argv[3]), zone)
h, m = map(int, sys.argv[2].split(':')); t = now.replace(hour=h, minute=m, second=0, microsecond=0)
t = t if t > now else t + d.timedelta(days=1)
print(int((t - now).total_seconds() // 60))" "$1" "$2" "$(device_seconds)")"
  if [[ -z "$got" ]]; then echo "  FAILED: no planned daily job"; return 1; fi
  # Other jobs ("Did it help?") may be planned too: one of them must be the daily one.
  local minutes
  for minutes in $got; do
    if (( minutes - want <= 5 && want - minutes <= 5 )); then
      echo "    daily job planned in $minutes min (the next $2 in $1: $want min)"
      return 0
    fi
  done
  echo "  FAILED: no job runs at the next $2 in $1 (about $want min); planned: $(echo $got)"
  # WorkManager's own view (debug build), to tell a missing plan from a job not yet handed to JobScheduler.
  adbq shell "run-as $APP sqlite3 no_backup/androidx.work.workdb 'select n.name, s.state, s.initial_delay / 60000, s.last_enqueue_time from workname n join workspec s on s.id = n.work_spec_id;'" 2>/dev/null |
    while IFS='|' read -r name state delay enqueued; do
      echo "    WorkManager: $name state $state, ${delay} min after $(date -r $((enqueued / 1000)) +%H:%M) (device $(adbq shell date +%H:%M </dev/null | tr -d '\r'))"
    done
  return 1
}
expect_job_in() { # $1 minutes: one of the app's jobs is planned to run in about that many minutes (within 5)
  local got
  got="$(adbq shell dumpsys jobscheduler | python3 -I -c "import re, sys
t = sys.stdin.read() + '\n  JOB '
for m in re.finditer(r'JOB [^\n]*@androidx\.work\.systemjobscheduler@$APP/[^\n]*\n(.*?)(?=\n  JOB )', t, re.S):
    e = re.search(r'Run time: earliest=([-+])(?:(\d+)d)?(?:(\d+)h)?(?:(\d+)m)?', m.group(1))
    if e: print((1 if e.group(1) == '+' else -1) * (int(e.group(2) or 0) * 1440 + int(e.group(3) or 0) * 60 + int(e.group(4) or 0)))")"
  local minutes
  for minutes in $got; do
    if (( minutes - $1 <= 5 && $1 - minutes <= 5 )); then echo "    a job is planned in $minutes min"; return 0; fi
  done
  echo "  FAILED: no job planned in about $1 min (planned: ${got:-none})"
  return 1
}
expect_colour() { # $1 regex of a text on screen, $2 warm, amber or neutral
  local box got
  adbq shell rm -f /sdcard/qa-colour.xml
  adbq shell uiautomator dump /sdcard/qa-colour.xml >/dev/null
  box="$(adbq exec-out cat /sdcard/qa-colour.xml | python3 -I -c "import re, sys, xml.etree.ElementTree as ET
for n in ET.fromstring(sys.stdin.read()).iter('node'):
    if re.fullmatch(sys.argv[1], n.get('text') or n.get('content-desc') or ''):
        print(*re.findall(r'\d+', n.get('bounds'))); break" "$1")"
  [[ -n "$box" ]] || { echo "  FAILED: \"$1\" is not on screen"; return 1; }
  adbq exec-out screencap -p >"$out/colour-$STEP.png"
  got="$(python3 -I "$ROOT/qa/text_colour.py" "$out/colour-$STEP.png" $box)"
  if [[ "$got" == "$2 "* ]]; then echo "    colour of \"$1\": $got"; return 0; fi
  echo "  FAILED: \"$1\" is $got, expected $2"
  return 1
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

qa_store() { # $1 a simulated store command (debug builds, D201): available, unavailable, unreachable, reset,
  # complete-pending or decline-pending. -f 0x20 reaches a stopped app too.
  adbq shell am broadcast -p "$APP" -f 0x20 -a com.migrely.app.debug.SIMULATED_STORE --es command "$1" >/dev/null
  sleep 1
  case "$1" in
    available | unavailable | unreachable)
      local store
      store="$(adbq shell run-as "$APP" cat shared_prefs/migrely_simulated_store.xml 2>/dev/null | tr -d '\r')"
      [[ "$store" == *"name=\"mode\">$1<"* ]] || { echo "  FAILED: the simulated store did not take \"$1\""; return 1; }
      ;;
  esac
}

shared_prefs_files() { # the shared-preferences files the app has written, one name a line
  adbq shell run-as "$APP" ls shared_prefs 2>/dev/null | tr -d '\r'
}

pull_report() { # $1 local path: the newest finished report in the app's cache (cache/reports/<id>/<name>.pdf)
  local name
  name="$(adbq shell "run-as $APP sh -c 'ls -t \$(find cache/reports -name \"*.pdf\")'" 2>/dev/null | tr -d '\r' | head -1)"
  [[ "$name" == *.pdf ]] || { echo "  FAILED: no report in cache/reports"; return 1; }
  adbq exec-out run-as "$APP" cat "$name" >"$1"
  [[ -s "$1" ]] || { echo "  FAILED: the report $name could not be read"; return 1; }
  echo "    report: ${name##*/} ($(wc -c <"$1" | tr -d ' ') bytes)"
}

screen_luminance() { # the average luminance (0-255) of the screen's left margin, where only the background shows
  adbq exec-out screencap -p >"$out/screen-$STEP.png"
  python3 -I -c "import sys
from pathlib import Path
sys.path.insert(0, sys.argv[2])
from a11y_audit import read_png
rows, bpp = read_png(Path(sys.argv[1]))
px = [rows[y][x * bpp:x * bpp + 3] for y in range(400, 2000, 20) for x in range(5, 30, 5)]
print(int(sum(0.2126 * p[0] + 0.7152 * p[1] + 0.0722 * p[2] for p in px) / len(px)))" "$out/screen-$STEP.png" "$ROOT/qa"
}
expect_theme() { # $1 light or dark: the screen in front, measured on its background; the app must be in front
  local l focus
  sleep 2
  focus="$(adbq shell dumpsys window | grep -m1 mCurrentFocus | tr -d '\r')"
  [[ "$focus" == *"$APP"* ]] || { echo "  FAILED: the app is not in front ($focus)"; return 1; }
  l="$(screen_luminance)"
  [[ "$l" =~ ^[0-9]+$ ]] || { echo "  FAILED: the screen could not be measured ('$l')"; return 1; }
  if [[ "$1" == light && $l -gt 150 ]] || [[ "$1" == dark && $l -lt 100 ]]; then echo "    $1 (background luminance $l)"; return 0; fi
  echo "  FAILED: expected $1, background luminance $l"
  return 1
}

app_job_count() { # how many jobs the app has registered with JobScheduler (WorkManager's namespace)
  local dump
  dump="$(adbq shell dumpsys jobscheduler)"
  python3 -I -c "import re, sys
print(len(set(re.findall(r'JOB [^\n]*:u0a\d+/(\d+): \S+ @androidx\.work\.systemjobscheduler@' + re.escape(sys.argv[1]) + '/', sys.stdin.read()))))" "$APP" <<<"$dump"
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
# Covers: medication-overuse › A standing indicator in History › No migraine logged
# Covers: medication-overuse › A standing indicator in History › One day
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
  # History's "Last 30 days" (D59), with no migraine logged yet (D99): at the limit with 15 days; a day 30 days back does
  # not count, a day 29 back does; over the limit reads amber.
  run_flow_env indicator-check MONTHS_BACK=0 "ROW=Simple painkillers: 15 days \(limit 15\), at the limit" DOCTOR=true || return 1
  set_clock_millis "$(day_millis 30)" || return 1
  run_flow_env med-log-home NAME=Ibuprofen || return 1
  set_clock_millis "$(( $(day_millis 0) + 7200000 ))" || return 1
  run_flow_env indicator-check MONTHS_BACK=0 "ROW=Simple painkillers: 15 days \(limit 15\), at the limit" DOCTOR=true || return 1
  set_clock_millis "$(day_millis 29)" || return 1
  run_flow_env med-log-home NAME=Ibuprofen || return 1
  set_clock_millis "$(( $(day_millis 0) + 7200000 ))" || return 1
  run_flow_env indicator-check MONTHS_BACK=0 "ROW=Simple painkillers: 16 days \(limit 15\), over the limit" DOCTOR=true || return 1
  expect_colour "Simple painkillers: 16 days \(limit 15\), over the limit" amber || return 1
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

# Until a right-to-left language ships, a Hebrew phone gets the app's English, left-to-right layout (D117, EC-UX-05):
# Right is the next day and Left the previous one, as on an English phone. The layout keeps start and end, so it will
# mirror once a right-to-left language ships (the requirement "Arrow keys move between days", D38).
scenario_keyboard_rtl() {
  echo "Scenario keyboard_rtl: a right-to-left phone (Hebrew) gets the English, left-to-right layout, on the 24th (D117)"
  set_clock_millis "$(day_millis -9)" || return 1
  run_flow clean || return 1
  adbq shell cmd locale set-app-locales "$APP" --locales iw >/dev/null
  run_flow home-ready || { adbq shell cmd locale set-app-locales "$APP" --locales "$ORIGINAL_APP_LOCALES"; return 1; }
  local ok=0
  expect_focus TAB "^Settings" && expect_focus TAB "^Period today" && expect_focus TAB "Opens History\.$" &&
    expect_focus TAB "^Today, .*$(day_re 24)" &&
    expect_focus DPAD_LEFT "$(day_re 23)" && expect_focus DPAD_RIGHT "$(day_re 24)" &&
    expect_focus DPAD_RIGHT "$(day_re 24)" || ok=1
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
  echo "Scenario stats_progress: the progress message against last month, its threshold, colour and home (EC-UX-14, D55)"
  # A: 12 days, then 10 days: last month reads "2 fewer", in the warm colour.
  set_clock_millis "$(when_millis 2 1 09:00)" || return 1
  run_flow clean || return 1
  long_attack 2 1 12 || return 1
  long_attack 1 1 10 || return 1
  set_clock_millis "$(when_millis 0 15 12:00)" || return 1
  run_flow_env progress-check MONTHS_BACK=1 "TEXT=2 fewer migraine days than [A-Z][a-z]+" || return 1
  expect_colour "2 fewer migraine days than [A-Z][a-z]+" warm || return 1
  # C: only 8 days, then 12 days: the comparison month has too few entries, so last month shows no message.
  set_clock_millis "$(when_millis 2 1 09:00)" || return 1
  run_flow clean || return 1
  long_attack 2 1 8 || return 1
  long_attack 1 1 12 || return 1
  set_clock_millis "$(when_millis 0 15 12:00)" || return 1
  run_flow_env progress-check MONTHS_BACK=1 TEXT= || return 1
  # B: 10 days, then the 5th to the 20th (16 days, 11 of them by the 15th), then 10 days this month.
  # Last month reads "6 more" in the neutral colour. This month compares with the same dates of last month (11, not 16):
  # "1 fewer migraine day than this time last month".
  set_clock_millis "$(when_millis 2 1 09:00)" || return 1
  run_flow clean || return 1
  long_attack 2 1 10 || return 1
  long_attack 1 5 20 || return 1
  long_attack 0 1 10 || return 1
  set_clock_millis "$(when_millis 0 15 12:00)" || return 1
  run_flow_env progress-check MONTHS_BACK=1 "TEXT=6 more migraine days than [A-Z][a-z]+" || return 1
  expect_colour "6 more migraine days than [A-Z][a-z]+" neutral || return 1
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
  run_app_jobs 1 || return 1
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
  run_app_jobs 1 || return 1
  expect_no_notification || return 1
  set_clock_millis "$(when_millis 0 17 19:50)" || return 1
  run_flow mark-pain-free-today || return 1
  set_clock_millis "$(when_millis 0 17 19:58)" || return 1
  run_flow app-open || return 1
  run_app_jobs 1 || return 1
  expect_no_notification || return 1
  # The 18th: the reminder comes, then a migraine is logged; "Pain-free day" then sets no mark (EC-REM-06).
  set_clock_millis "$(when_millis 0 18 19:58)" || return 1
  run_flow app-open || return 1
  run_app_jobs 1 || return 1
  expect_notification "^How was today\?" || return 1
  run_flow attack-log-keep || return 1
  tap_notification_action "Pain-free day" "How was today\?" || return 1
  expect_notification "A migraine is logged today\." || return 1
  # No mark was set: today's "Pain-free day" stays off (and disabled) in History.
  run_flow history-today || return 1
  expect_switch "Pain-free day" false || return 1
  adbq shell input keyevent HOME
  # The 19th: "Log a migraine" opens home and clears the notification.
  set_clock_millis "$(when_millis 0 19 19:58)" || return 1
  run_flow app-open || return 1
  run_app_jobs 1 || return 1
  expect_notification "^How was today\?" || return 1
  tap_notification_action "Log a migraine" "How was today\?" || return 1
  expect_no_notification || return 1
  run_flow home-shown || return 1
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
  # Opening Reminders: "Daily reminder" is off at 8:00 PM.
  run_flow reminders-open || return 1
  expect_switch "Daily reminder" false || return 1
  key BACK; key BACK
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
  echo "Scenario reminder_replan: the daily reminder follows a zone or clock change and survives a reboot (EC-REM-04, 05)"
  notifications_permission grant
  set_zone Asia/Tashkent || return 1
  set_clock_millis "$(mid_month_millis Asia/Tashkent 12:00)" || return 1
  run_flow daily-reminder-on || return 1
  expect_planned Asia/Tashkent 20:00 || return 1
  # The app is in the background, not restarted: the zone and clock changes alone re-plan it.
  set_zone Europe/Berlin || return 1
  sleep 5
  expect_planned Europe/Berlin 20:00 || return 1
  shift_clock_hours 3 || return 1
  sleep 5
  expect_planned Europe/Berlin 20:00 || return 1
  local before after i
  before="$(adbq shell cat /proc/sys/kernel/random/boot_id | tr -d '\r')"
  echo "    rebooting the emulator"
  adbq reboot
  adbq wait-for-disconnect 2>/dev/null || sleep 15
  adbq wait-for-device
  for i in $(seq 1 60); do [[ "$(adbq shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]] && break; sleep 3; done
  after="$(adbq shell cat /proc/sys/kernel/random/boot_id 2>/dev/null | tr -d '\r')"
  [[ -n "$after" && "$after" != "$before" ]] || { echo "  FAILED: the emulator did not restart"; return 1; }
  sleep 15
  # Planned again without opening the app, for the next 8:00 PM. The emulator takes the host's time zone again when it
  # boots (measured: Berlin became Tashkent), so the expected time follows the zone the device has now.
  local zone_now
  zone_now="$(adbq shell getprop persist.sys.timezone | tr -d '\r')"
  expect_planned "$zone_now" 20:00 || return 1
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
  expect_job_in 120 || return 1
  set_clock_millis "$(when_millis 0 15 16:01)" || return 1
  run_app_jobs 1 || return 1
  expect_notification "^Did Ibuprofen help\?" || return 1
  adbq shell am force-stop "$APP"
  # The 16th: an attack starts after the intake; nothing is sent, and home asks once it ends.
  set_clock_millis "$(when_millis 0 16 14:00)" || return 1
  run_flow_env med-log-keep NAME=Ibuprofen || return 1
  set_clock_millis "$(when_millis 0 16 14:30)" || return 1
  run_flow attack-start-keep || return 1
  set_clock_millis "$(when_millis 0 16 16:01)" || return 1
  # Measured: the app keeps no "Did it help?" job while an attack is in progress, so none may run here.
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

# Covers: attack-history › Month by month › Medication before any attack
# Covers: month-sky › Accessible days › Screen reader on a day with medication only
scenario_med_only_history() {
  echo "Scenario med_only_history: ibuprofen last month and no migraine: History reaches that month (D99, D100, #47)"
  set_clock_millis "$(when_millis 1 20 10:00)" || return 1
  run_flow clean || return 1
  run_flow_env med-first-log-home NAME=Ibuprofen "TYPE=Simple painkiller" || return 1
  set_clock_millis "$(when_millis 0 15 12:00)" || return 1
  run_flow med-only-past-check || return 1
}

node_bounds() { # $1 regex of a label on screen: prints "x1 y1 x2 y2" of the first match
  adbq shell rm -f /sdcard/qa-bounds.xml
  adbq shell uiautomator dump /sdcard/qa-bounds.xml >/dev/null
  adbq exec-out cat /sdcard/qa-bounds.xml | python3 -I -c "import re, sys, xml.etree.ElementTree as ET
for n in ET.fromstring(sys.stdin.read()).iter('node'):
    if re.fullmatch(sys.argv[1], n.get('content-desc') or n.get('text') or ''):
        print(*re.findall(r'\d+', n.get('bounds'))); break" "$1"
}

# Covers: in-app-messages › A message never passes taps to what lies under it › Undo just as the message goes
# Covers: in-app-messages › A message never passes taps to what lies under it › Afterwards
# History's Undo sits on top of the top-bar actions (#52). A tap there just after the message leaves must reach nothing
# (a guard under the message, 800 ms); a tap later must open Doctor report as usual. Taps go through adb, timed from
# the tap on "Delete migraine" (the undo message lasts 5 s), at the centre of "Doctor report" where Undo overlaps it.
scenario_message_tap_guard() {
  echo "Scenario message_tap_guard: a tap where Undo was, just after the message leaves, opens nothing (#52, D114)"
  local delete offset screen box point guarded=0
  run_flow history-open || return 1
  box="$(node_bounds "Doctor report")"
  [[ -n "$box" ]] || { echo "  FAILED: no \"Doctor report\" in History's top bar"; return 1; }
  set -- $box
  point="$((($1 + $3) / 2)) $((($2 + $4) / 2))"
  for offset in 5.2 5.5 5.8 7.5; do
    run_flow history-delete-ready || return 1
    delete="$(point_in 'Delete migraine' 0.5)"
    [[ -n "$delete" ]] || { echo "  FAILED: \"Delete migraine\" not found"; return 1; }
    adbq shell "input tap $delete; sleep $offset; input tap $point"
    sleep 2
    screen="$(focused_screen)"
    echo "    tap at ($point) ${offset} s after the delete: $screen"
    case "$screen" in
      "doctor report") [[ "$offset" == "7.5" ]] || { echo "  FAILED: the tap reached Doctor report under the message (#52)"; return 1; } ;;
      "history (deleted)") [[ "$offset" != "7.5" ]] && guarded=$((guarded + 1)) ;;
      "history (undone)") ;;
      *) echo "  FAILED: an unexpected screen after the tap"; return 1 ;;
    esac
    [[ "$offset" != "7.5" || "$screen" == "doctor report" ]] || { echo "  FAILED: the late tap did not open Doctor report (control)"; return 1; }
  done
  [[ $guarded -ge 1 ]] || { echo "  FAILED: every early tap landed on Undo, so the guard was never reached"; return 1; }
}

focused_screen() { # "doctor report", "history (undone)", "history (deleted)" or "unknown", from what is on screen
  local xml
  adbq shell rm -f /sdcard/qa-screen.xml
  adbq shell uiautomator dump /sdcard/qa-screen.xml >/dev/null
  xml="$(adbq exec-out cat /sdcard/qa-screen.xml)"
  # A free user's tap on Doctor report opens its paywall (D108); for the guard either means the tap got through.
  if grep -qE 'text="(Create report|A report for your doctor)"' <<<"$xml"; then echo "doctor report"
  elif grep -q 'Opens its details' <<<"$xml"; then echo "history (undone)"
  elif grep -q 'text="Log your first migraine"' <<<"$xml"; then echo "history (deleted)"
  else echo "unknown"; fi
}

# Covers: app-settings › Theme › Light
# Covers: app-settings › Theme › An attack in progress
scenario_theme_check() {
  echo "Scenario theme_check: Light at once and at the next start; attack mode and its screens dark; Match the phone (D94)"
  set_clock_millis "$(when_millis 0 15 12:00)" || return 1
  run_flow clean || return 1
  open_app
  expect_theme dark || return 1
  run_flow_env theme-choose THEME=Light || return 1
  echo "    Settings, right after choosing Light:"; expect_theme light || return 1
  adbq shell am force-stop "$APP"; open_app
  echo "    home at the next start:"; expect_theme light || return 1
  run_flow attack-start-keep || return 1
  open_app
  echo "    attack mode:"; expect_theme dark || return 1
  run_flow attack-details-open || return 1
  echo "    details opened from attack mode:"; expect_theme dark || return 1
  adbq shell input keyevent BACK
  run_flow attack-end || return 1
  open_app
  echo "    home after the attack:"; expect_theme light || return 1
  run_flow_env theme-choose "THEME=Match the phone" || return 1
  adbq shell cmd uimode night no >/dev/null
  open_app
  expect_theme light || return 1
  adbq shell cmd uimode night yes >/dev/null
  open_app
  expect_theme dark || return 1
  adbq shell cmd uimode night "$ORIGINAL_NIGHT" >/dev/null
}

# Covers: onboarding › Four skippable steps › Answering
# Covers: onboarding › The daily reminder offer › Turning it on
# Covers: onboarding › Shown once, to a new user › Starting an attack from the shortcut
# Covers: onboarding › Shown once, to a new user › An update
scenario_onboarding_states() {
  echo "Scenario onboarding_states: answers stored, the reminder offer, the attack shortcut and an update (D76-D80)"
  local prefs
  set_clock_millis "$(when_millis 0 15 12:00)" || return 1
  run_flow onboarding-answer || return 1
  expect_planned Asia/Tashkent 20:00 || return 1
  # The answers, as the debug build stores them (read only).
  prefs="$(adbq shell run-as "$APP" cat shared_prefs/migrely_preferences.xml 2>/dev/null | tr -d '\r')"
  local want
  for want in 'name="goal">find_triggers<' 'name="attack_frequency">4_to_14<' 'name="period_tracking" value="false"' 'name="onboarding_finished" value="true"'; do
    [[ "$prefs" == *"$want"* ]] || { echo "  FAILED: the saved answers lack $want"; return 1; }
  done
  echo "    answers saved: triggers, 4 to 14 a month, no period tracking, onboarding finished"
  # The launcher shortcut (res/xml/shortcuts.xml) names the activity, so the intent does too.
  adbq shell pm clear "$APP" >/dev/null
  adbq shell am start -a com.migrely.app.action.START_ATTACK -n "$(adbq shell cmd package resolve-activity --brief "$APP" | tail -1 | tr -d '\r')" >/dev/null
  run_flow onboarding-shortcut || return 1
  # An update over a version with entries and no onboarding answer saved yet opens home.
  run_flow clean || return 1
  run_flow_env attack-start LEVEL= || return 1
  run_flow attack-end || return 1
  adbq shell run-as "$APP" ls shared_prefs/migrely_preferences.xml >/dev/null 2>&1 || { echo "  FAILED: no preferences to remove"; return 1; }
  adbq shell run-as "$APP" rm -f shared_prefs/migrely_preferences.xml
  ! adbq shell run-as "$APP" ls shared_prefs/migrely_preferences.xml >/dev/null 2>&1 || { echo "  FAILED: the preferences are still there"; return 1; }
  "$ADB" -s "$QA_SERIAL" install -r "$APK" 2>&1 | grep -q Success || { echo "  FAILED: the update did not install"; return 1; }
  run_flow update-no-onboarding || return 1
}

# Covers: app-settings › Delete all data › Deleted
scenario_delete_all_data() {
  echo "Scenario delete_all_data: DELETE empties records, copies and reminders and restarts into onboarding, dark (D96)"
  notifications_permission grant
  adbq shell "rm -f /sdcard/Download/migrely-*" >/dev/null
  set_clock_millis "$(when_millis 0 15 12:00)" || return 1
  run_flow backup-data || return 1
  run_flow backup-export || return 1
  local name
  name="$(basename "$(adbq shell 'ls -t /sdcard/Download/migrely-2*.json | head -1' | tr -d '\r')")"
  # A Replace leaves an automatic copy behind.
  import_and_expect "$name" "2 migraines, 1 medication and 1 marked day" Replace "Backup imported: 2 migraines\." || return 1
  run_flow_env copy-row-check SHOWN=true || return 1
  run_flow_env theme-set THEME=Light || return 1
  run_flow reminder-on-granted || return 1
  expect_switch "Daily reminder" true || return 1
  key BACK; key BACK
  adbq shell input keyevent HOME
  expect_planned Asia/Tashkent 20:00 || return 1
  run_flow delete-everything || return 1
  expect_theme dark || return 1
  local jobs
  jobs="$(app_job_count)"
  [[ "$jobs" == 0 ]] || { echo "  FAILED: $jobs job(s) still planned after Delete all data"; return 1; }
  echo "    no planned job"
  run_flow after-delete-check || return 1
  run_flow_env copy-row-check SHOWN=false || return 1
  # Nothing comes back at the next start.
  run_flow after-delete-relaunch || return 1
}

pull_newest() { # $1 glob in /sdcard/Download (for example "migrely-*.json"), $2 local path: pull the newest match
  local name
  name="$(adbq shell "ls -t /sdcard/Download/$1 2>/dev/null | head -1" | tr -d '\r')"
  [[ -n "$name" ]] || { echo "  FAILED: no $1 in Download"; return 1; }
  adbq pull "$name" "$2" >/dev/null || { echo "  FAILED: could not pull $name"; return 1; }
  echo "    pulled ${name##*/}"
}

seed_import() { # $1 seed name, $2 "Import" (an empty phone) or "Replace": the seed, written for the emulator's date by
  # qa/seed_backup.py, goes to Download and through Your data's import (D202); what the app must show is in
  # $out/seed-$1.expected
  local today file="seed-$1.json"
  today="$(adbq shell date +%F | tr -d '\r')"
  python3 -I "$ROOT/qa/seed_backup.py" "$1" "$out/$file" --today "$today" \
    --zone "$(adbq shell getprop persist.sys.timezone | tr -d '\r')" >"$out/seed-$1.expected" ||
    { echo "  FAILED: qa/seed_backup.py $1"; return 1; }
  adbq push "$out/$file" "/sdcard/Download/$file" >/dev/null
  import_and_expect "$file" ".*" "$2" "Backup imported: [0-9]+ migraines?.*" || return 1
}

start_attack_shortcut() { # the launcher shortcut "Start migraine": straight into attack mode
  adbq shell am start -a com.migrely.app.action.START_ATTACK \
    -n "$(adbq shell cmd package resolve-activity --brief "$APP" | tail -1 | tr -d '\r')" >/dev/null
}

tap_time() { perl -MTime::HiRes=time -e 'printf "%.2f", time'; }

# Also checks: in-app-messages › Message durations by kind (Your data's information message; retest of #56)
# The file is tapped through adb, and screenshots taken back to back from that tap (about 8 a second) show when the
# message comes and goes (qa/message_timing.py). An information message stays 8 s, or the accessibility timeout for
# messages to read when it is longer (accessibility_non_interactive_ui_timeout_ms; both timeouts are set to 20 s, as
# "Time to take action" sets them).
scenario_message_duration() {
  echo "Scenario message_duration: a Your data message stays 8 s, or the accessibility timeout when longer (#56, EC-UX-16)"
  local timeout box sent end frames result stayed low high ok=0
  run_flow clean || return 1
  adbq shell "echo 'This is a shopping list, not a backup.' > /sdcard/Download/broken.json"
  for timeout in default 20; do
    if [[ "$timeout" == 20 ]]; then
      adbq shell settings put secure accessibility_interactive_ui_timeout_ms 20000
      adbq shell settings put secure accessibility_non_interactive_ui_timeout_ms 20000
      low=19.5; high=21.5
    else
      default_undo_window
      put_or_delete secure accessibility_non_interactive_ui_timeout_ms "$ORIGINAL_TEXT_TIMEOUT"
      low=7.5; high=9
    fi
    run_flow_env import-pick-ready FILE=broken.json || { ok=1; break; }
    box="$(adbq exec-out uiautomator dump /dev/tty 2>/dev/null | python3 -I -c "import re, sys, xml.etree.ElementTree as ET
raw = sys.stdin.read(); raw = raw[raw.find('<?xml'):raw.rfind('>') + 1]
top = lambda n: int(re.findall(r'\d+', n.get('bounds'))[1])
hits = [n for n in ET.fromstring(raw).iter('node') if n.get('text') == 'broken.json']
if hits:
    x1, y1, x2, y2 = map(int, re.findall(r'\d+', max(hits, key=top).get('bounds')))
    print((x1 + x2) // 2, (y1 + y2) // 2)")"
    [[ -n "$box" ]] || { echo "  FAILED: broken.json is not in the picker's results"; ok=1; break; }
    frames="$out/message-frames-$timeout"
    mkdir -p "$frames"
    sent="$(tap_time)"
    adbq shell input tap $box
    end="$(perl -e "print $sent + $high + 4")"
    while perl -MTime::HiRes=time -e "exit !(time < $end)"; do
      adbq exec-out screencap -p >"$frames/$(tap_time).png"
    done
    result="$(python3 -I "$ROOT/qa/message_timing.py" "$frames" "$sent")"
    echo "    accessibility timeout $timeout: $result"
    stayed="$(sed -nE 's/.*stayed ([0-9.]+) s.*/\1/p' <<<"$result")"
    [[ -n "$stayed" ]] && perl -e "exit !($stayed >= $low && $stayed <= $high)" ||
      { echo "  FAILED: expected $low to $high s (#56)"; ok=1; break; }
    rm -rf "$frames"
  done
  default_undo_window
  put_or_delete secure accessibility_non_interactive_ui_timeout_ms "$ORIGINAL_TEXT_TIMEOUT"
  return $ok
}

# Covers: subscription › Billing unavailable › No billing
# Covers: subscription › Billing unavailable › No Google Play billing service on the phone
# The debug half of "No Google Play billing service": with the store unavailable on a fresh install, the line shows at
# once and no paywall shows itself. The release half (a release build with no Play products) is in the Go/No-Go record.
scenario_premium_store_states() {
  echo "Scenario premium_store_states: unavailable and unreachable stores; no paywall by itself without billing (EC-BIL-11, D110)"
  run_flow clean || return 1
  qa_store unavailable || return 1
  adbq shell am force-stop "$APP"
  run_flow_env paywall-state "STATE=Premium isn.t available in your region yet" NO_BUY=true ABSENT= TAP= || return 1
  run_flow_env paywall-state "STATE=Everything else in Migrely works as before\." NO_BUY=true ABSENT= TAP= || return 1
  qa_store unreachable || return 1
  adbq shell am force-stop "$APP"
  run_flow_env paywall-state "STATE=Google Play can.t be reached right now" NO_BUY=true ABSENT= "TAP=Try again" || return 1
  qa_store available || return 1
  adbq shell am force-stop "$APP"
  run_flow_env paywall-state "STATE=Start free trial" NO_BUY=false ABSENT= TAP= || return 1
  echo "    unavailable: the line and no buy button; unreachable: Try again; available again: the products"
  adbq shell pm clear "$APP" >/dev/null
  qa_store unavailable || return 1
  run_flow no-self-paywall || return 1
  run_flow_env paywall-state "STATE=Premium isn.t available in your region yet" NO_BUY=true ABSENT= TAP= || return 1
  echo "    a new install with billing unavailable: no paywall after onboarding"
  qa_store available || return 1
}

# Covers: subscription › Buying › Pending
scenario_premium_pending() {
  echo "Scenario premium_pending: a cancelled sheet changes nothing; pending unlocks nothing until it completes (EC-BIL-08)"
  run_flow clean || return 1
  run_flow_env premium-buy PLAN=Monthly BUTTON=Subscribe SHEET=Cancel "RESULT=Migrely Premium" || return 1
  run_flow_env premium-check STATUS=Free REPORT=locked || return 1
  run_flow_env premium-buy PLAN=Monthly BUTTON=Subscribe "SHEET=Pay later \\(pending\\)" "RESULT=Payment pending" || return 1
  run_flow_env premium-check "STATUS=Payment pending" REPORT=locked || return 1
  qa_store complete-pending || return 1
  adbq shell am force-stop "$APP"
  run_flow_env premium-check "STATUS=Premium · monthly" REPORT=open || return 1
  echo "    cancelled: Free; pending: locked; completed from the shop (adb): Premium · monthly"
  run_flow clean || return 1
  run_flow_env premium-buy PLAN=Monthly BUTTON=Subscribe "SHEET=Pay later \\(pending\\)" "RESULT=Payment pending" || return 1
  qa_store decline-pending || return 1
  adbq shell am force-stop "$APP"
  run_flow_env premium-check STATUS=Free REPORT=locked || return 1
  echo "    declined at the shop: Free"
}

# Covers: subscription › Products from Google Play › Prices shown
# Covers: subscription › Products from Google Play › No trial left
# Covers: subscription › Changing plans › Lifetime over a subscription
# The euro price has a no-break space before the sign, so the pattern takes any character there.
scenario_premium_plans() {
  echo "Scenario premium_plans: prices in the phone's currency, no trial left, lifetime over a subscription (SUB-F1, EC-BIL-10)"
  run_flow clean || return 1
  adbq shell cmd locale set-app-locales "$APP" --locales de-DE >/dev/null
  local price
  for price in "Yearly\\. 7 days free, then 29,99.€/year\\." "Monthly\\. 4,99.€/month\\." "Lifetime\\. 59,99.€ · one payment\\."; do
    run_flow_env paywall-state "STATE=$price" NO_BUY=false ABSENT= TAP= ||
      { adbq shell cmd locale set-app-locales "$APP" --locales "$ORIGINAL_APP_LOCALES" >/dev/null; return 1; }
  done
  adbq shell cmd locale set-app-locales "$APP" --locales "$ORIGINAL_APP_LOCALES" >/dev/null
  echo "    de-DE: euros with a decimal comma, and the trial line"
  run_flow_env premium-buy PLAN=Monthly BUTTON=Subscribe "SHEET=Complete the purchase" "RESULT=You have Premium" || return 1
  run_flow_env plan-change PLAN=Lifetime "BUTTON=Buy once" "RESULT=Your subscription still renews\\. Migrely can.t cancel it for you.*" \
    "ALSO=Manage subscription" || return 1
  run_flow_env premium-check "STATUS=Premium · lifetime" REPORT=open || return 1
  run_flow_env premium-check "STATUS=Your subscription still renews\\. Cancel it in Google Play\\." REPORT=open || return 1
  echo "    lifetime over monthly: Premium · lifetime, and the note to cancel the monthly plan in Google Play"
  run_flow clean || return 1
  run_flow_env premium-buy PLAN=Yearly "BUTTON=Start free trial" "SHEET=Complete the purchase" "RESULT=Your free trial has started" || return 1
  run_flow_env premium-manage "CHOICE=End the subscription" AFTER=Free || return 1
  adbq shell am force-stop "$APP"
  run_flow_env premium-check STATUS=Free REPORT=locked || return 1
  run_flow_env paywall-state "STATE=Yearly\\. .29\\.99/year\\." NO_BUY=false "ABSENT=.*7 days free.*|Start free trial" TAP= || return 1
  run_flow_env paywall-state STATE=Subscribe NO_BUY=false ABSENT= TAP= || return 1
  echo "    after a used trial: the yearly price with no trial line"
}

# Covers: subscription › The entitlement › New phone
# Covers: subscription › The entitlement › Reinstall or backup on another account
# Covers: subscription › The entitlement › Refunded
scenario_premium_entitlement() {
  echo "Scenario premium_entitlement: found again without the cache, never backed up, and locked after a refund (D106, D115)"
  run_flow clean || return 1
  run_flow log-and-close || return 1
  run_flow_env premium-buy PLAN=Lifetime "BUTTON=Buy once" "SHEET=Complete the purchase" "RESULT=You have Premium" || return 1
  run_flow_env premium-check "STATUS=Premium · lifetime" REPORT=open || return 1
  shared_prefs_files | grep -qx migrely_entitlement.xml ||
    { echo "  FAILED: no migrely_entitlement.xml after a purchase ($(shared_prefs_files | tr '\n' ' '))"; return 1; }
  python3 -I "$ROOT/qa/backup_rules.py" "$APK" migrely_entitlement.xml | sed 's/^/    /'
  [[ "${PIPESTATUS[0]}" == 0 ]] || return 1
  adbq shell am force-stop "$APP"
  adbq shell run-as "$APP" rm shared_prefs/migrely_entitlement.xml
  ! shared_prefs_files | grep -qx migrely_entitlement.xml || { echo "  FAILED: the cache is still there"; return 1; }
  run_flow_env premium-check "STATUS=Premium · lifetime" REPORT=open || return 1
  run_flow_env premium-restore "STATUS=Premium · lifetime" || return 1
  echo "    the entitlement cache removed: Premium found again at the first check, and Restore purchases keeps it"
  # A lifetime purchase alone has no Manage row, so the simulated store's refund page is out of reach; emptying the
  # store is the same to the app: Google Play no longer reports the purchase. The app asks Google Play at start and at
  # most every 15 minutes, so the next check comes an hour later.
  qa_store reset || return 1
  shift_clock_hours 1 || return 1
  adbq shell am force-stop "$APP"
  run_flow_env premium-check STATUS=Free REPORT=locked || return 1
  run_flow_env history-count COUNT=1 || return 1
  echo "    refunded: Free, the report locked, the migraine kept"
}

# Covers: subscription › The entitlement › Offline
# Covers: quality-gates › Premium is checked through the simulated store › Offline for too long
scenario_premium_offline() {
  echo "Scenario premium_offline: Premium stays 3 days without Google Play, then locks; entries stay (EC-BIL-01, D106)"
  set_clock_millis "$(when_millis 0 15 12:00)" || return 1
  run_flow clean || return 1
  run_flow log-and-close || return 1
  run_flow_env premium-buy PLAN=Monthly BUTTON=Subscribe "SHEET=Complete the purchase" "RESULT=You have Premium" || return 1
  qa_store unreachable || return 1
  shift_clock_hours 48 || return 1
  adbq shell am force-stop "$APP"
  run_flow_env premium-check "STATUS=Premium · monthly" REPORT=open || return 1
  echo "    2 days unreachable: Premium · monthly"
  shift_clock_hours 48 || return 1
  adbq shell am force-stop "$APP"
  run_flow_env premium-check STATUS=Free REPORT=locked || return 1
  run_flow_env history-count COUNT=1 || return 1
  echo "    4 days unreachable: locked, the migraine kept"
  qa_store available || return 1
}

# Covers: subscription › Trial ending reminder › A day before
scenario_trial_banner() {
  echo "Scenario trial_banner: the day before the trial ends home shows the banner once (EC-BIL-02, D111)"
  set_clock_millis "$(when_millis 0 10 12:00)" || return 1
  run_flow clean || return 1
  run_flow_env premium-buy PLAN=Yearly "BUTTON=Start free trial" "SHEET=Complete the purchase" "RESULT=Your free trial has started" || return 1
  run_flow_env premium-check "STATUS=Free trial" REPORT=open || return 1
  run_flow home-no-banner || return 1
  shift_clock_hours 144 || return 1
  adbq shell am force-stop "$APP"
  run_flow trial-banner || return 1
  echo "    6 days in: \"Your free trial ends tomorrow\" with Manage; dismissed, it does not come back"
}

# Covers: subscription › When the paywall shows itself › Within 7 days
# Covers: subscription › When the paywall shows itself › The 5th migraine
# Covers: subscription › When the paywall shows itself › Patterns not ready yet
# Patterns are ready with 5 migraines started in the last 6 months, the first started at least 30 days ago (D88, as
# worded in b93659e). Back-filled migraines count: four-old's start 40 days back although they are imported today.
scenario_paywall_self_show() {
  echo "Scenario paywall_self_show: the patterns paywall after the 5th migraine, not within 7 days, not in attack mode (D110)"
  set_clock_millis "$(when_millis 0 10 12:00)" || return 1
  run_flow clean || return 1
  shift_clock_hours 72 || return 1
  seed_import four-old Import || return 1
  run_flow log-attack || return 1
  run_flow home-no-paywall || return 1
  echo "    5th migraine 3 days after the onboarding paywall: none"
  shift_clock_hours 120 || return 1
  adbq shell am force-stop "$APP"
  start_attack_shortcut
  run_flow attack-mode-no-paywall || return 1
  run_flow patterns-paywall || return 1
  echo "    8 days after it: none in attack mode; once on home, then never again"
  set_clock_millis "$(when_millis 0 10 12:00)" || return 1
  run_flow clean || return 1
  shift_clock_hours 72 || return 1
  seed_import five-new Import || return 1
  shift_clock_hours 120 || return 1
  adbq shell am force-stop "$APP"
  run_flow home-no-paywall || return 1
  echo "    5 migraines, the first 15 days ago, 8 days after the onboarding paywall: none"
}

screen_labels_scrolled() { # $1 swipes: every text and label seen on screen while scrolling down that many times
  local i
  for ((i = 0; i <= $1; i++)); do
    adbq exec-out uiautomator dump /dev/tty 2>/dev/null | python3 -I -c "import sys, xml.etree.ElementTree as ET
raw = sys.stdin.read(); raw = raw[raw.find('<?xml'):raw.rfind('>') + 1]
try:
    nodes = list(ET.fromstring(raw).iter('node'))
except ET.ParseError:
    nodes = []
for n in nodes:
    for label in (n.get('text'), n.get('content-desc')):
        if label:
            print(label.replace('\n', ' '))"
    adbq shell input swipe 540 1800 540 900 400
    sleep 1
  done | sort -u
}

expect_seen() { # $1 file of texts, one a line, $2 file of labels seen: every text was seen as a whole label
  local missing
  [[ -s "$1" ]] || { echo "  FAILED: nothing to look for in $1"; return 1; }
  missing="$(grep -vxFf "$2" "$1")"
  [[ -z "$missing" ]] && return 0
  echo "  FAILED: not on screen:"
  sed 's/^/      /' <<<"$missing"
  return 1
}

report_texts() { # $1 seed: its PDF texts into REPORT_TEXTS and its patterns ("re:" lines) into REPORT_PATTERNS; expand
  # them as ${REPORT_TEXTS[@]+"${REPORT_TEXTS[@]}"}, since bash 3.2 under set -u takes an empty array for unbound
  REPORT_TEXTS=()
  REPORT_PATTERNS=()
  local line
  python3 -I "$ROOT/qa/seed_expect.py" pdf "$out/seed-$1.expected" >"$out/seed-$1.pdf-texts" ||
    { echo "  FAILED: no PDF texts for $1"; return 1; }
  while IFS= read -r line; do
    if [[ "$line" == re:* ]]; then REPORT_PATTERNS+=("${line#re:}"); else REPORT_TEXTS+=("$line"); fi
  done <"$out/seed-$1.pdf-texts"
}

# Covers: doctor-report › The summary page › Three months
# Covers: doctor-report › The details › Notes left out
# Covers: quality-gates › Generated reports are checked by their text › A three-month report
scenario_report_three_months() {
  echo "Scenario report_three_months: the PDF's summary, tables and footer from a seed; notes only when chosen (D82, D202)"
  run_flow clean || return 1
  seed_import report-three-months Import || return 1
  run_flow_env premium-buy PLAN=Monthly BUTTON=Subscribe "SHEET=Complete the purchase" "RESULT=You have Premium" || return 1
  run_flow_env report-make "PERIOD=3 months" NOTES=false || return 1
  pull_report "$out/report-three-months.pdf" || return 1
  report_texts report-three-months || return 1
  python3 -I "$ROOT/qa/report_pdf.py" "$out/report-three-months.pdf" --every-page "Patient-entered data. Not a diagnosis." \
    --absent NOTE-ALPHA-7 NOTE-BETA-9 --text ${REPORT_TEXTS[@]+"${REPORT_TEXTS[@]}"} --regex ${REPORT_PATTERNS[@]+"${REPORT_PATTERNS[@]}"} | sed 's/^/    notes off: /'
  [[ "${PIPESTATUS[0]}" == 0 ]] || return 1
  run_flow_env report-make "PERIOD=3 months" NOTES=true || return 1
  pull_report "$out/report-three-months-notes.pdf" || return 1
  python3 -I "$ROOT/qa/report_pdf.py" "$out/report-three-months-notes.pdf" --text NOTE-ALPHA-7 NOTE-BETA-9 | sed 's/^/    notes on: /'
  [[ "${PIPESTATUS[0]}" == 0 ]] || return 1
}

# Covers: doctor-report › The summary page › A month only partly in the period
# Covers: doctor-report › Counting like the app › Logged in Tokyo
scenario_report_counting() {
  echo "Scenario report_counting: overuse spans across a month change, and an attack logged in Tokyo (D82, D83)"
  local seed period
  for seed in report-partial-month report-tokyo; do
    period="3 months"; [[ "$seed" == report-partial-month ]] && period="1 month"
    run_flow clean || return 1
    seed_import "$seed" Import || return 1
    run_flow_env premium-buy PLAN=Monthly BUTTON=Subscribe "SHEET=Complete the purchase" "RESULT=You have Premium" || return 1
    if [[ "$seed" == report-partial-month ]]; then
      run_flow_env home-text "TEXT=Triptans on 11 of the last 30 days" || return 1
    fi
    run_flow_env report-make "PERIOD=$period" NOTES=false || return 1
    pull_report "$out/$seed.pdf" || return 1
    report_texts "$seed" || return 1
    python3 -I "$ROOT/qa/report_pdf.py" "$out/$seed.pdf" --every-page "Patient-entered data. Not a diagnosis." \
      --text ${REPORT_TEXTS[@]+"${REPORT_TEXTS[@]}"} --regex ${REPORT_PATTERNS[@]+"${REPORT_PATTERNS[@]}"} | sed "s/^/    $seed: /"
    [[ "${PIPESTATUS[0]}" == 0 ]] || return 1
  done
}

# Covers: doctor-report › Large periods › Twelve heavy months
# Covers: doctor-report › The file and sharing › Closed while making it
scenario_report_heavy() {
  echo "Scenario report_heavy: 900 attacks in 12 months make at most 50 pages; a report closed half-made leaves nothing (D84, D85)"
  local files box want
  run_flow clean || return 1
  seed_import report-heavy Import || return 1
  run_flow_env premium-buy PLAN=Monthly BUTTON=Subscribe "SHEET=Complete the purchase" "RESULT=You have Premium" || return 1
  # Closed while making it: Create report is tapped through adb and the app is closed half a second later (a
  # 12-month report of 900 attacks takes about a second; the PDF is drawn in memory, then written as ".partial" and
  # renamed). The report's folder at the kill shows it had started. After the next start there is no ".partial" file
  # and no empty folder. Then planted leftovers (a partial file, an empty folder, a report two days old), read back
  # first, are gone after the next start too.
  run_flow_env report-ready "PERIOD=12 months" || return 1
  box="$(node_bounds "Create report")"
  [[ -n "$box" ]] || { echo "  FAILED: no Create report"; return 1; }
  set -- $box
  adbq shell "input tap $((($1 + $3) / 2)) $((($2 + $4) / 2)); sleep 0.5; am force-stop $APP"
  files="$(adbq shell run-as "$APP" find cache/reports -mindepth 1 | tr -d '\r')" || { echo "  FAILED: cache/reports unreadable"; return 1; }
  [[ -n "$files" ]] || { echo "  FAILED: no report had started when the app was closed"; return 1; }
  echo "    closed 0.5 s after Create report: $(grep -c . <<<"$files") entries under cache/reports"
  open_app
  sleep 3
  files="$(adbq shell "run-as $APP find cache/reports -mindepth 1 \\( -name '*.partial' -o -type d -empty \\)" | tr -d '\r')" ||
    { echo "  FAILED: cache/reports unreadable"; return 1; }
  [[ -z "$files" ]] || { echo "  FAILED: left after the next start: $(tr '\n' ' ' <<<"$files")"; return 1; }
  local old planted
  old="$(( $(adbq shell date +%s | tr -d '\r') - 2 * 86400 ))"
  adbq shell "run-as $APP sh -c 'mkdir -p cache/reports/qa-partial cache/reports/qa-empty cache/reports/qa-old &&
    echo x > cache/reports/qa-partial/migrely-report-qa.pdf.partial && echo x > cache/reports/qa-old/migrely-report-qa.pdf &&
    touch -d @$old cache/reports/qa-old/migrely-report-qa.pdf'"
  planted="$(adbq shell run-as "$APP" find cache/reports -path '*qa-*' | tr -d '\r')"
  for want in qa-partial/migrely-report-qa.pdf.partial qa-empty qa-old/migrely-report-qa.pdf; do
    grep -q "$want\$" <<<"$planted" || { echo "  FAILED: could not plant $want"; return 1; }
  done
  adbq shell am force-stop "$APP"
  open_app
  sleep 3
  files="$(adbq shell run-as "$APP" find cache/reports -mindepth 1 | tr -d '\r')" || { echo "  FAILED: cache/reports unreadable"; return 1; }
  if grep -q 'qa-' <<<"$files"; then echo "  FAILED: left after the next start: $(grep 'qa-' <<<"$files" | tr '\n' ' ')"; return 1; fi
  echo "    after the next start: no partial file, no empty folder, no report older than a day (3 planted, all gone)"
  run_flow_env report-make "PERIOD=12 months" NOTES=false || return 1
  pull_report "$out/report-heavy.pdf" || return 1
  report_texts report-heavy || return 1
  python3 -I "$ROOT/qa/report_pdf.py" "$out/report-heavy.pdf" --pages-at-most 50 --every-page "Patient-entered data. Not a diagnosis." \
    --regex "Showing the latest [0-9]+ of 900 migraines\. The summary counts all of them\." --text ${REPORT_TEXTS[@]+"${REPORT_TEXTS[@]}"} --regex ${REPORT_PATTERNS[@]+"${REPORT_PATTERNS[@]}"} |
    sed 's/^/    /'
  [[ "${PIPESTATUS[0]}" == 0 ]] || return 1
}

# Covers: trigger-insights › Triggers, never causes › Stress
# Covers: trigger-insights › When migraines come › Mondays
# Covers: trigger-insights › What helped › Ibuprofen
# Covers: trigger-insights › Around the period › Tracking off
# Covers: trigger-insights › Twelve months › A screen reader
# Covers: trigger-insights › Enough to go on › Three migraines so far
# Covers: trigger-insights › The period › Opening Insights
scenario_insights() {
  echo "Scenario insights: triggers, days and times, what helped, the period, the 12 months for TalkBack, the N-more card (D87-D92)"
  local seed
  for seed in insights-patterns insights-three; do
    run_flow clean || return 1
    seed_import "$seed" Import || return 1
    run_flow_env premium-buy PLAN=Monthly BUTTON=Subscribe "SHEET=Complete the purchase" "RESULT=You have Premium" || return 1
    if [[ "$seed" == insights-patterns ]]; then
      # Track periods is on by default, and 3 periods start in these 6 months: the section shows with its share.
      run_flow_env insights-open TRACKING=on || return 1
      screen_labels_scrolled 14 >"$out/insights-$seed-on.seen"
      python3 -I "$ROOT/qa/seed_expect.py" period "$out/seed-$seed.expected" >"$out/insights-$seed.period" ||
        { echo "  FAILED: no period texts for $seed"; return 1; }
      expect_seen "$out/insights-$seed.period" "$out/insights-$seed-on.seen" || return 1
      echo "    $seed, Track periods on: \"Around your period\" with its share"
    fi
    run_flow_env insights-open TRACKING=off || return 1
    screen_labels_scrolled 14 >"$out/insights-$seed.seen"
    python3 -I "$ROOT/qa/seed_expect.py" insights "$out/seed-$seed.expected" >"$out/insights-$seed.want" ||
      { echo "  FAILED: no Insights texts for $seed"; return 1; }
    expect_seen "$out/insights-$seed.want" "$out/insights-$seed.seen" || return 1
    if grep -q "Around your period" "$out/insights-$seed.seen"; then echo "  FAILED: a period section with Track periods off"; return 1; fi
    if grep -qiE "caus|leads? to" "$out/insights-$seed.seen"; then echo "  FAILED: a text says a trigger causes migraines"; return 1; fi
    echo "    $seed, Track periods off: $(wc -l <"$out/insights-$seed.want" | tr -d ' ') texts seen; no period section; nothing says \"cause\""
  done
}

# Covers: subscription › A store for testing › Release
# On demand, with QA_RELEASE_APK=<release APK>: the release build holds no simulated store and is not debuggable
# (D113); the debug APK under test is the control that the search still finds it.
scenario_release_apk() {
  echo "Scenario release_apk: no simulated store in the release build (D113)"
  if [[ -z "${QA_RELEASE_APK:-}" || ! -f "${QA_RELEASE_APK:-}" ]]; then
    echo "  NOT RUN: set QA_RELEASE_APK to the release APK"
    return 1
  fi
  python3 -I "$ROOT/qa/release_apk.py" "$QA_RELEASE_APK" "$APK" | sed 's/^/    /'
  [[ "${PIPESTATUS[0]}" == 0 ]]
}

# Covers: data-backup › JSON backup › Exporting (2 attacks, 1 medication, 1 day mark rather than 16, 2 and 5)
# Covers: data-backup › CSV export › Opening in Excel
# Covers: data-backup › CSV export › A phone in Uzbek or Russian
# Covers: data-backup › CSV export › A note that looks like a formula
# Covers: quality-gates › Files through the system picker are checked by their bytes › An exported CSV
scenario_backup_export() {
  echo "Scenario backup_export: the JSON backup and the CSV, checked by their bytes, in English, Uzbek and Russian (D71, D72)"
  local language separator result
  adbq shell "rm -f /sdcard/Download/migrely-*" >/dev/null
  set_clock_millis "$(when_millis 0 15 12:00)" || return 1
  run_flow backup-data || return 1
  run_flow backup-export || return 1
  pull_newest "migrely-2*.json" "$out/backup.json" || return 1
  result="$(python3 -I "$ROOT/qa/backup_files.py" json "$out/backup.json" 2 1 1)"
  echo "    $result"
  [[ "$result" == "JSON ok"* ]] || return 1
  for language in en uz ru; do
    separator=","
    if [[ "$language" != "en" ]]; then
      separator=";"
      adbq shell cmd locale set-app-locales "$APP" --locales "$language" >/dev/null
      sleep 2
      run_flow backup-export-csv || return 1
    fi
    pull_newest "migrely-attacks-*.csv" "$out/attacks-$language.csv" || return 1
    result="$(python3 -I "$ROOT/qa/backup_files.py" csv "$out/attacks-$language.csv" "$separator" --rows 2 --formula "=SUM(1)" --text "Голова")"
    echo "    $language: $result"
    [[ "$result" == "CSV ok"* ]] || return 1
  done
  adbq shell cmd locale set-app-locales "$APP" --locales "$ORIGINAL_APP_LOCALES" >/dev/null
}

screen_text_matching() { # $1 Python regex: prints the first text or label on screen that matches it in full
  adbq exec-out uiautomator dump /dev/tty 2>/dev/null | python3 -I -c "import re, sys, xml.etree.ElementTree as ET
raw = sys.stdin.read(); raw = raw[raw.find('<?xml'):raw.rfind('>') + 1]
try:
    nodes = list(ET.fromstring(raw).iter('node'))
except ET.ParseError:
    nodes = []
for n in nodes:
    for label in (n.get('text') or '', n.get('content-desc') or ''):
        if label and re.fullmatch(sys.argv[1], label, re.S):
            print(label); sys.exit(0)" "$1"
}
expect_text_soon() { # $1 Python regex, $2 seconds: some text or label on screen matches it in full within that time
  local end=$((SECONDS + $2)) found
  while ((SECONDS < end)); do
    found="$(screen_text_matching "$1")"
    if [[ -n "$found" ]]; then echo "    message: $found"; return 0; fi
  done
  echo "  FAILED: no \"$1\" on screen within $2 s"
  return 1
}
import_and_expect() { # $1 file in Download, $2 HOLDS regex or "", $3 choice or "", $4 extended regex of the result message
  # Run without a report folder: writing it takes seconds, and the result message lasts only about 4 s (#56).
  local attempt seen log others="This file isn.t a Migrely backup.*|Update Migrely to open this file.*|Backup imported:.*|Added [0-9]+ migraines?.*|Added the other entries.*|Nothing new.*|Couldn.t.*"
  for attempt in 1 2; do
    STEP=$((STEP + 1))
    log="$out/$(printf '%03d' "$STEP")-${CURRENT:-x}-import-step.log"
    echo "  flow import-step ($1, ${3:-no choice})"
    maestro --device "$QA_SERIAL" test -e "FILE=$1" -e "HOLDS=$2" -e "CHOICE=$3" "$FLOWS/import-step.yaml" >"$log" 2>&1 || {
      echo "  FAILED: import-step (log: $log)"
      return 1
    }
    if seen="$(expect_text_soon "$4" 10)"; then echo "$seen"; break; fi
    # Not the expected message: another result or a crash is a failure, not #56.
    local other
    other="$(screen_text_matching "$others")"
    [[ -z "$other" ]] || { echo "  FAILED: the import said \"$other\", expected /$4/"; return 1; }
    adbq shell pidof "$APP" >/dev/null || { echo "  FAILED: the app is not running after the import"; return 1; }
    # A refused file changes nothing, so it is tried once more to read its message; a real import is not repeated.
    if [[ -z "$3" && $attempt == 1 ]]; then echo "    the message was missed; trying the refused file again"; continue; fi
    # Messages stay 8 s since #56 was fixed, so a missed one is a failure.
    echo "  FAILED: \"$4\" was never read"
    return 1
  done
}

history_count_now() { # prints how many migraines History lists this month (1 to 5)
  local c
  for c in 1 2 3 4 5; do
    if run_flow_env history-count "COUNT=$c" >/dev/null 2>&1; then echo "$c"; return 0; fi
  done
  echo "?"
  return 1
}

# Covers: data-backup › Import shows what it brings › Before importing
# Covers: data-backup › Replace or add › Add
# Covers: data-backup › Replace or add › The same attack logged on two phones
# Covers: data-backup › Replace or add › A medication by the same name
# Covers: data-backup › Replace or add › Replace
# Covers: data-backup › Restore the automatic copy › Undoing a Replace
# Covers: data-backup › Files that cannot be imported › A broken file
# Covers: data-backup › Files that cannot be imported › A newer file
# Covers: onboarding › Restore from a backup › A new phone without Android's backup
# Covers: quality-gates › Files through the system picker are checked by their bytes › A prepared broken file
# Also checks: an older schemaVersion with optional fields left out is imported (EC-BAK-03)
scenario_backup_import() {
  echo "Scenario backup_import: refused files, an older file, Add, Replace and its copy, and onboarding's restore (D73-D75, EC-BAK-01-05)"
  adbq shell "rm -f /sdcard/Download/migrely-* /sdcard/Download/broken.json /sdcard/Download/newer.json /sdcard/Download/older.json /sdcard/Download/add.json" >/dev/null
  set_clock_millis "$(when_millis 0 15 12:00)" || return 1
  # The phone: migraines A and B, Ibuprofen with one intake, a period mark. Its backup E is the source of the fixtures.
  run_flow backup-data || return 1
  run_flow backup-export || return 1
  pull_newest "migrely-2*.json" "$out/import-source.json" || return 1
  python3 -I "$ROOT/qa/backup_files.py" fixtures "$out/import-source.json" "$out/fixtures" | sed 's/^/    /'
  local f name
  for f in broken newer older add; do adbq push "$out/fixtures/$f.json" "/sdcard/Download/$f.json" >/dev/null; done
  name="$(basename "$(adbq shell 'ls -t /sdcard/Download/migrely-2*.json | head -1' | tr -d '\r')")"
  # Refused files change nothing (EC-BAK-01, 02).
  import_and_expect broken.json "" "" "This file isn.t a Migrely backup\. Nothing was changed\." || return 1
  import_and_expect newer.json "" "" "Update Migrely to open this file\." || return 1
  run_flow_env history-count COUNT=2 || return 1
  # An older file, its optional fields left out, is offered and adds nothing new (EC-BAK-03).
  import_and_expect older.json "2 migraines, 1 medication and 1 marked day" Add "Nothing new: everything in it is already here\." || return 1
  run_flow_env history-count COUNT=2 || return 1
  # Add: A again under another ID overlaps and is skipped; a new migraine three days earlier is added; "ibuprofen"
  # under another ID maps to the phone's Ibuprofen, and its intake counts as Ibuprofen on that new day.
  import_and_expect add.json "2 migraines, 1 medication and 0 marked days" Add "Added 1 migraine\. 1 was already here\." || return 1
  run_flow_env history-count COUNT=3 || return 1
  run_flow_env med-list-count NAME=Ibuprofen || return 1
  run_flow_env days-medication-check DAYS=2 || return 1
  # Known bug #58: a deleted migraine stays a hidden row until a purge, so Add of E may count it "already here".
  run_flow delete-first-attack || return 1
  run_flow_env history-count COUNT=2 || return 1
  import_and_expect "$name" "2 migraines, 1 medication and 1 marked day" Add "(Added 1 migraine\. 1 was already here\.|Nothing new: everything in it is already here\.)" || return 1
  if run_flow_env history-count COUNT=3 >/dev/null 2>&1; then
    echo "    NOW PASSES (#58): Add brought the deleted migraine back"
  else
    run_flow_env history-count COUNT=2 || { echo "  FAILED: after Add History lists neither 2 (#58) nor 3"; return 1; }
    echo "    KNOWN BUG #58 still reproduces: Add left the deleted migraine out"
  fi
  # Replace with E keeps a copy of what was here; restoring it brings that back and keeps a copy of E's state.
  local before
  before="$(history_count_now)" || { echo "  FAILED: History's count could not be read"; return 1; }
  import_and_expect "$name" "2 migraines, 1 medication and 1 marked day" Replace "Backup imported: 2 migraines\." || return 1
  run_flow_env history-count COUNT=2 || return 1
  run_flow restore-copy || return 1
  run_flow_env history-count "COUNT=$before" || return 1
  run_flow_env copy-row-check SHOWN=true || return 1
  # A new phone without Android's backup: onboarding's "Restore from a backup file" imports E, and Back returns there.
  adbq shell pm clear "$APP" >/dev/null
  run_flow_env onboarding-restore "FILE=$name" || return 1
  run_flow_env history-count-after-onboarding COUNT=2 || return 1
}

# On demand (not in the default run). Android's own backup through the local transport. The app turns cloud backup off
# without end-to-end encryption; the local transport can claim encryption (fake_encryption_flag), so a backup and a
# restore after "pm clear" can be tried. The outcome is printed as observed; "A new phone" itself needs a real Google
# account backup (a D61 gap).
scenario_backup_android() {
  echo "Scenario backup_android: bmgr with the local transport claiming encryption; the outcome is recorded (D74)"
  BMGR_ENABLED="$(adbq shell bmgr enabled | tr -d '\r')"
  BMGR_TRANSPORT="$(adbq shell bmgr list transports | tr -d '\r' | sed -n 's/^ *\* //p')"
  adbq shell settings put secure backup_local_transport_parameters fake_encryption_flag=true
  adbq shell bmgr enable true >/dev/null
  adbq shell bmgr transport com.android.localtransport/.LocalTransport >/dev/null
  set_clock_millis "$(when_millis 0 15 12:00)" || return 1
  run_flow log-and-close || return 1
  echo "    backupnow: $(adbq shell bmgr backupnow "$APP" 2>&1 | tr -d '\r' | grep -E "$APP|Backup finished" | tr '\n' ' ')"
  adbq shell pm clear "$APP" >/dev/null
  echo "    restore: $(adbq shell bmgr restore "$APP" 2>&1 | tr -d '\r' | tail -2 | tr '\n' ' ')"
  if run_flow_env history-count-after-onboarding COUNT=1 >/dev/null 2>&1; then
    echo "    OBSERVED: the migraine came back after pm clear and a restore"
  else
    echo "    OBSERVED: nothing came back (the app's backup rules or the transport kept it out)"
  fi
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
  local new_apk="$APK"
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
    stats_counting stats_chronic stats_progress reminder_daily reminder_permission reminder_effect reminder_replan \
    med_only_history message_tap_guard \
    theme_check onboarding_states delete_all_data backup_export backup_import message_duration \
    premium_store_states premium_pending premium_plans premium_entitlement premium_offline trial_banner paywall_self_show \
    report_three_months report_counting report_heavy insights
  if [[ -n "${QA_OLD_APK:-}" ]]; then
    set -- "$@" upgrade
  else
    echo "Note: upgrade is not in this run; set QA_OLD_APK to the older APK to include it."
  fi
  if [[ -n "${QA_RELEASE_APK:-}" ]]; then
    set -- "$@" release_apk
  else
    echo "Note: release_apk is not in this run; set QA_RELEASE_APK to the release APK to include it."
  fi
fi
failed=()
for s in "$@"; do
  CURRENT="$s"
  "scenario_$s" || failed+=("$s")
  # Language and night mode back after every scenario, so a failure mid-way cannot leak into the next one.
  adbq shell cmd locale set-app-locales "$APP" --locales "$ORIGINAL_APP_LOCALES" >/dev/null 2>&1
  [[ -n "$ORIGINAL_NIGHT" ]] && adbq shell cmd uimode night "$ORIGINAL_NIGHT" >/dev/null 2>&1
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
