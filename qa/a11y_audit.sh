#!/usr/bin/env bash
# Accessibility audit on the QA emulator: drives every state in .maestro/a11y-tour/ with Maestro, captures the screen
# (uiautomator dump + screencap) and checks it with qa/a11y_audit.py. Each state is captured three times:
# font 100% portrait, font 200% portrait (BRD EC-UX-03) and font 100% landscape.
#
#   qa/a11y_audit.sh                 all states
#   qa/a11y_audit.sh home-calm       one state
#
# Device settings changed here are restored on exit, even when the run fails.
set -euo pipefail

QA_SERIAL="emulator-5580"
export MAESTRO_CLI_NO_ANALYTICS=1
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ADB="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"
adbq() { "$ADB" -s "$QA_SERIAL" "$@"; }

if [[ "$(adbq shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]]; then
  echo "$QA_SERIAL is not booted (see qa/maestro.sh for the start command)." >&2
  exit 2
fi

# Take the device first, then read the settings: another QA run may have a temporary value in place until it ends.
source "$ROOT/qa/lib.sh"
qa_lock_device
qa_check_baseline
trap qa_unlock_device EXIT
SETTINGS=("system font_scale" "system user_rotation" "system accelerometer_rotation"
  "secure accessibility_interactive_ui_timeout_ms")
declare -a ORIGINAL
for i in "${!SETTINGS[@]}"; do
  ORIGINAL[$i]="$(adbq shell settings get ${SETTINGS[$i]} | tr -d '\r')"
done
restore() {
  set +e # restore everything even if one command fails
  for i in "${!SETTINGS[@]}"; do
    qa_restore_setting ${SETTINGS[$i]} "${ORIGINAL[$i]}"
  done
  qa_unlock_device
}
trap restore EXIT

# Messages with Undo stay 5 s by default; a 30 s accessibility timeout keeps them on screen until the capture.
adbq shell settings put secure accessibility_interactive_ui_timeout_ms 30000

out="$ROOT/build/qa/a11y/$(date +%Y%m%d-%H%M%S)"
mkdir -p "$out"
states=("$@")
if [[ ${#states[@]} -eq 0 ]]; then
  states=()
  for flow in "$ROOT"/.maestro/a11y-tour/*.yaml; do states+=("$(basename "$flow" .yaml)"); done
fi

configure() { # $1 font scale, $2 rotation (0 portrait, 1 landscape)
  adbq shell settings put system font_scale "$1"
  adbq shell settings put system accelerometer_rotation 0
  adbq shell settings put system user_rotation "$2"
  # The display turns asynchronously; a flow started before it settles races the activity's recreation.
  local want="ROTATION_$(($2 * 90))"
  for _ in $(seq 1 20); do
    adbq shell dumpsys window displays | grep -q "mCurrentRotation=$want" && return 0
    sleep 0.5
  done
  echo "Warning: the display did not reach $want" >&2
}

skipped=()
for config in "font100:1.0:0" "font200:2.0:0" "landscape:1.0:1"; do
  IFS=: read -r label scale rotation <<<"$config"
  configure "$scale" "$rotation"
  for state in "${states[@]}"; do
    name="$state.$label"
    if ! maestro --device "$QA_SERIAL" test "$ROOT/.maestro/a11y-tour/$state.yaml" >"$out/$name.log" 2>&1; then
      echo "[skip] $name: the tour flow failed (see $out/$name.log)"
      skipped+=("$name")
      continue
    fi
    # uiautomator exits 0 without writing a file when the screen never goes idle; never pair an old tree with a new
    # screenshot.
    adbq shell rm -f /sdcard/a11y.xml
    if ! adbq shell uiautomator dump /sdcard/a11y.xml | grep -q "dumped to"; then
      echo "[skip] $name: uiautomator could not dump the screen"
      skipped+=("$name")
      continue
    fi
    adbq pull /sdcard/a11y.xml "$out/$name.xml" >/dev/null
    adbq exec-out screencap -p >"$out/$name.png"
  done
done

echo "Captures: $out"
status=0
# "wm density" prints a second "Override density" line when one is set; the last line is the density in effect.
density="$(adbq shell wm density | tail -1 | awk '{print $NF}' | tr -d '\r')"
python3 -I "$ROOT/qa/a11y_audit.py" "$out" --density "$density" || status=1
# A state that could not be reached was not checked: that is a failure, not a pass. A tour flow that cannot find a
# control by its label usually means the control is hidden or covered (#20).
if [[ ${#skipped[@]} -gt 0 ]]; then
  echo "${#skipped[@]} state(s) could not be captured: ${skipped[*]}"
  status=1
fi
exit $status
