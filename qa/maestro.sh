#!/usr/bin/env bash
# Runs the Maestro end-to-end flows in .maestro/ on the QA emulator only.
#
#   qa/maestro.sh                      all flows (regression)
#   qa/maestro.sh --tags smoke         only flows tagged smoke
#   qa/maestro.sh .maestro/today-sky   one folder or one flow file
#   qa/maestro.sh --install ...        reinstall the debug APK first
#
# The serial is pinned on purpose: emulator-5570 belongs to the dev session and other emulators to other
# projects. Flows clear the app's data, so running them anywhere else would wipe someone else's state.
set -euo pipefail

QA_SERIAL="emulator-5580"
# The app promises that nothing leaves the device; the test tooling does not phone home either.
export MAESTRO_CLI_NO_ANALYTICS=1
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ADB="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"
APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"

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
if [[ "$("$ADB" -s "$QA_SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]]; then
  echo "$QA_SERIAL is not booted. Start it with:" >&2
  echo "  emulator -avd Migrely_QA_API_36 -port 5580 -gpu swiftshader_indirect -no-snapshot-save" \
    "-no-boot-anim -no-audio -no-window" >&2
  exit 2
fi

if $install; then
  [[ -f "$APK" ]] || (cd "$ROOT" && ./gradlew :app:assembleDebug --console=plain -q)
  "$ADB" -s "$QA_SERIAL" install -r "$APK" >/dev/null
fi

run_id="$(date +%Y%m%d-%H%M%S)"
out="$ROOT/build/qa/maestro/$run_id"
mkdir -p "$out"

args=(--device "$QA_SERIAL" test "$target" --format junit --output "$out/report.xml" --test-output-dir "$out")
[[ -n "$tags" ]] && args+=(--include-tags "$tags")

echo "Maestro on $QA_SERIAL -> $out"
status=0
maestro "${args[@]}" || status=$?
echo "Report: $out/report.xml"
exit $status
