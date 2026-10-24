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

SETTINGS=("system font_scale" "system user_rotation" "system accelerometer_rotation"
  "secure accessibility_interactive_ui_timeout_ms")
declare -a ORIGINAL
for i in "${!SETTINGS[@]}"; do
  ORIGINAL[$i]="$(adbq shell settings get ${SETTINGS[$i]} | tr -d '\r')"
done
restore() {
  for i in "${!SETTINGS[@]}"; do
    if [[ "${ORIGINAL[$i]}" == "null" ]]; then
      adbq shell settings delete ${SETTINGS[$i]} >/dev/null
    else
      adbq shell settings put ${SETTINGS[$i]} "${ORIGINAL[$i]}"
    fi
  done
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
}

for config in "font100:1.0:0" "font200:2.0:0" "landscape:1.0:1"; do
  IFS=: read -r label scale rotation <<<"$config"
  configure "$scale" "$rotation"
  for state in "${states[@]}"; do
    name="$state.$label"
    if ! maestro --device "$QA_SERIAL" test "$ROOT/.maestro/a11y-tour/$state.yaml" >"$out/$name.log" 2>&1; then
      echo "[skip] $name: the tour flow failed (see $out/$name.log)"
      continue
    fi
    adbq shell uiautomator dump /sdcard/a11y.xml >/dev/null
    adbq pull /sdcard/a11y.xml "$out/$name.xml" >/dev/null
    adbq exec-out screencap -p >"$out/$name.png"
  done
done

echo "Captures: $out"
python3 -I "$ROOT/qa/a11y_audit.py" "$out" --density "$(adbq shell wm density | awk '{print $NF}' | tr -d '\r')"
