#!/usr/bin/env bash
# Runs the Maestro end-to-end flows in .maestro/ on a QA emulator only.
#
#   qa/maestro.sh                      all flows (regression)
#   qa/maestro.sh --tags smoke         only flows tagged smoke
#   qa/maestro.sh .maestro/today-sky   one folder or one flow file
#   qa/maestro.sh --install ...        reinstall the debug APK first
#   QA_SERIAL=emulator-5582 qa/maestro.sh --tags smoke    on the Android 8.0 QA emulator
#
# Two passes: flows tagged a11y-timeout check what Undo does, not how long the window lasts, so they run with
# Android's "Time to take action" at 20 s. The app honours it (EC-UX-16), and Maestro's 1–7 s per step on the
# software-GPU emulator can then never use up the 5 s window. Every other flow runs with the default timing.
#
# The serial is pinned on purpose: emulator-5570 belongs to the dev session and other emulators to other
# projects. Flows clear the app's data, so running them anywhere else would wipe someone else's state.
set -euo pipefail

QA_SERIAL="${QA_SERIAL:-emulator-5580}"
case "$QA_SERIAL" in
  emulator-5580 | emulator-5582) ;;
  *) echo "Refusing to run on $QA_SERIAL: QA flows only run on emulator-5580 (API 36) or emulator-5582 (API 26)." >&2
     exit 2 ;;
esac
# The app promises that nothing leaves the device; the test tooling does not phone home either.
export MAESTRO_CLI_NO_ANALYTICS=1
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ADB="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"
APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
LONG_TIMEOUT_TAG="a11y-timeout"
LONG_TIMEOUT_MS=20000
adbq() { "$ADB" -s "$QA_SERIAL" "$@"; }

install=false
tags=""
target="$ROOT/.maestro"
while [[ $# -gt 0 ]]; do
  case "$1" in
    --install) install=true; shift ;;
    --tags) tags="$2"; shift 2 ;;
    *) target="$1"; shift ;;
  esac
done

if [[ "${ANDROID_SERIAL:-$QA_SERIAL}" != "$QA_SERIAL" ]]; then
  echo "Refusing to run: ANDROID_SERIAL=$ANDROID_SERIAL, but QA flows only run on $QA_SERIAL." >&2
  exit 2
fi
if [[ "$(adbq shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]]; then
  echo "$QA_SERIAL is not booted. Start it with (5580: Migrely_QA_API_36, 5582: Migrely_QA_API_26):" >&2
  echo "  emulator -avd <avd> -port <port> -gpu swiftshader_indirect -no-snapshot-save" \
    "-no-boot-anim -no-audio -no-window" >&2
  exit 2
fi

source "$ROOT/qa/lib.sh"
qa_lock_device
trap qa_unlock_device EXIT

if $install; then
  [[ -f "$APK" ]] || (cd "$ROOT" && ./gradlew :app:assembleDebug --console=plain -q)
  adbq install -r "$APK" >/dev/null
fi

run_id="$(date +%Y%m%d-%H%M%S)"
out="$ROOT/build/qa/maestro/$run_id"
mkdir -p "$out"
status=0

# Pass 1: default timing.
args=(--device "$QA_SERIAL" test "$target" --exclude-tags "$LONG_TIMEOUT_TAG"
  --format junit --output "$out/report.xml" --test-output-dir "$out")
[[ -n "$tags" ]] && args+=(--include-tags "$tags")
echo "Maestro on $QA_SERIAL -> $out"
if [[ "$tags" != "$LONG_TIMEOUT_TAG" ]]; then
  maestro "${args[@]}" || status=$?
fi

# Pass 2: flows tagged a11y-timeout (and the requested tag, if any), with a 20 s accessibility timeout.
flow_dirs=()
if [[ -d "$target" && "$target" == "$ROOT/.maestro" ]]; then
  while IFS= read -r glob; do flow_dirs+=("$ROOT/.maestro/${glob%/\*}"); done \
    < <(sed -n 's/^  - "\(.*\)"$/\1/p' "$ROOT/.maestro/config.yaml")
else
  flow_dirs=("$target")
fi
long_flows=()
while IFS= read -r flow; do
  tag_line="$(grep -m1 '^tags:' "$flow" || true)"
  [[ "$tag_line" == *"$LONG_TIMEOUT_TAG"* ]] || continue
  [[ -z "$tags" || "$tag_line" == *"$tags"* ]] || continue
  long_flows+=("$flow")
done < <(find "${flow_dirs[@]}" -name '*.yaml' 2>/dev/null | sort)

if [[ ${#long_flows[@]} -gt 0 ]]; then
  original="$(adbq shell settings get secure accessibility_interactive_ui_timeout_ms | tr -d '\r')"
  restore() {
    if [[ "$original" == "null" ]]; then
      adbq shell settings delete secure accessibility_interactive_ui_timeout_ms >/dev/null
    else
      adbq shell settings put secure accessibility_interactive_ui_timeout_ms "$original"
    fi
  }
  trap 'restore; qa_unlock_device' EXIT
  adbq shell settings put secure accessibility_interactive_ui_timeout_ms "$LONG_TIMEOUT_MS"
  echo "Pass 2: ${#long_flows[@]} flow(s) with a $((LONG_TIMEOUT_MS / 1000)) s accessibility timeout"
  maestro --device "$QA_SERIAL" test "${long_flows[@]}" --format junit --output "$out/report-$LONG_TIMEOUT_TAG.xml" \
    --test-output-dir "$out/$LONG_TIMEOUT_TAG" || status=$?
  restore
  trap qa_unlock_device EXIT
fi

echo "Reports: $out/report*.xml"
exit $status
