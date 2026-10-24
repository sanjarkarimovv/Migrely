#!/usr/bin/env bash
# Runs the Maestro end-to-end flows in .maestro/ on a QA emulator only.
#
#   qa/maestro.sh                      all flows (regression)
#   qa/maestro.sh --tags smoke         only flows tagged smoke
#   qa/maestro.sh .maestro/today-sky   one folder or one flow file
#   qa/maestro.sh --install ...        build and reinstall the debug APK first
#   QA_SERIAL=emulator-5582 qa/maestro.sh --tags smoke    on the Android 8.0 QA emulator
#
# Three passes:
#   1. flows with the default timing;
#   2. flows tagged a11y-timeout, which check what Undo does rather than how long the window lasts, with Android's
#      "Time to take action" at 20 s (the app honours it, EC-UX-16). Maestro's 1–7 s per step on the software-GPU
#      emulator can then never use up the 5 s window;
#   3. flows tagged known-bug, read the other way round: a failure is the expected reproduction of an open issue and
#      does not fail the run; a pass means the issue looks fixed, so the tag should go.
# The exit status comes from passes 1 and 2 only, so an open known bug never hides a new regression.
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
KNOWN_BUG_TAG="known-bug"
GEOMETRY_TAG="geometry"
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
qa_check_baseline

SDK_LEVEL="$(adbq shell getprop ro.build.version.sdk | tr -d '\r')"
ORIGINAL_TIMEOUT="$(adbq shell settings get secure accessibility_interactive_ui_timeout_ms | tr -d '\r')"
ORIGINAL_ROTATION="$(adbq shell settings get system user_rotation | tr -d '\r')"
ORIGINAL_AUTO_ROTATE="$(adbq shell settings get system accelerometer_rotation | tr -d '\r')"
put_or_delete() { # $1 namespace, $2 key, $3 value or "null"
  if [[ "$3" == "null" ]]; then adbq shell settings delete "$1" "$2" >/dev/null; else adbq shell settings put "$1" "$2" "$3"; fi
}
restore() {
  set +e # restore everything even if one command fails
  qa_restore_setting secure accessibility_interactive_ui_timeout_ms "$ORIGINAL_TIMEOUT"
  # Flows that turn the screen use Maestro's setOrientation, which can leave auto-rotate off.
  qa_restore_setting system user_rotation "$ORIGINAL_ROTATION"
  qa_restore_setting system accelerometer_rotation "$ORIGINAL_AUTO_ROTATE"
  qa_unlock_device
}
trap restore EXIT

if $install; then
  # Always build: Gradle is incremental, and an old APK must never be retested by mistake.
  (cd "$ROOT" && ./gradlew :app:assembleDebug --console=plain -q)
  adbq install -r "$APK" >/dev/null
fi

# Flow files of the run: the folders listed in .maestro/config.yaml, or the given folder or file.
flow_files() {
  local dirs=() glob
  if [[ -d "$target" && "$target" == "$ROOT/.maestro" ]]; then
    while IFS= read -r glob; do
      [[ -n "$glob" ]] && dirs+=("$ROOT/.maestro/${glob%%/*}")
    done < <(sed -n 's/^[[:space:]]*-[[:space:]]*"\{0,1\}\([^"]*\)"\{0,1\}[[:space:]]*$/\1/p' "$ROOT/.maestro/config.yaml")
    if [[ ${#dirs[@]} -eq 0 ]]; then
      echo "No flow folders found in .maestro/config.yaml" >&2
      exit 2
    fi
  else
    dirs=("$target")
  fi
  find "${dirs[@]}" -name '*.yaml' | sort
}
has_tag() { # $1 flow file, $2 tag: exact match on one of the comma-separated names in "tags: [...]"
  grep -m1 '^tags:' "$1" | tr -d '[] ' | sed 's/^tags://' | tr ',' '\n' | grep -qx "$2"
}
wanted() { # $1 flow file: matches the --tags filter (any of a comma-separated list), if one was given
  [[ -z "$tags" ]] && return 0
  local t
  for t in $(echo "$tags" | tr ',' ' '); do has_tag "$1" "$t" && return 0; done
  return 1
}

pass1=() pass2=() pass3=()
skipped_geometry=0
while IFS= read -r flow; do
  wanted "$flow" || continue
  if [[ "$SDK_LEVEL" -lt 29 ]] && has_tag "$flow" "$GEOMETRY_TAG"; then
    # Screen points measured on the API 36 edge-to-edge layout do not hold with Android 8's navigation bar.
    skipped_geometry=$((skipped_geometry + 1))
    continue
  fi
  if has_tag "$flow" "$KNOWN_BUG_TAG"; then pass3+=("$flow")
  elif has_tag "$flow" "$LONG_TIMEOUT_TAG"; then pass2+=("$flow")
  else pass1+=("$flow")
  fi
done < <(flow_files)

run_id="$(date +%Y%m%d-%H%M%S)"
out="$ROOT/build/qa/maestro/$run_id"
mkdir -p "$out"
status=0
echo "Maestro on $QA_SERIAL (API $SDK_LEVEL) -> $out"
[[ $skipped_geometry -gt 0 ]] && echo "Skipped $skipped_geometry flow(s) tagged $GEOMETRY_TAG: not valid below API 29"

run_pass() { # $1 name, then flow files
  local name="$1"
  shift
  maestro --device "$QA_SERIAL" test "$@" --format junit --output "$out/report-$name.xml" --test-output-dir "$out/$name"
}
long_timeout_on() {
  if [[ "$SDK_LEVEL" -ge 29 ]]; then
    adbq shell settings put secure accessibility_interactive_ui_timeout_ms "$LONG_TIMEOUT_MS"
  else
    echo "Note: API $SDK_LEVEL has no accessibility timeout setting; these flows run with the default 5 s window."
  fi
}
long_timeout_off() { put_or_delete secure accessibility_interactive_ui_timeout_ms "$ORIGINAL_TIMEOUT"; }

if [[ ${#pass1[@]} -gt 0 ]]; then
  echo "Pass 1: ${#pass1[@]} flow(s) with the default timing"
  run_pass default "${pass1[@]}" || status=1
fi
if [[ ${#pass2[@]} -gt 0 ]]; then
  echo "Pass 2: ${#pass2[@]} flow(s) with a $((LONG_TIMEOUT_MS / 1000)) s accessibility timeout"
  long_timeout_on
  run_pass "$LONG_TIMEOUT_TAG" "${pass2[@]}" || status=1
  long_timeout_off
fi
if [[ ${#pass3[@]} -gt 0 ]]; then
  echo "Pass 3: ${#pass3[@]} known-bug flow(s); a failure is the expected reproduction"
  for flow in "${pass3[@]}"; do
    has_tag "$flow" "$LONG_TIMEOUT_TAG" && long_timeout_on >/dev/null
    issue="$(grep -o 'issues/[0-9]*' "$flow" | head -1 || true)"
    if run_pass "known-bug-$(basename "$flow" .yaml)" "$flow" >"$out/known-bug-$(basename "$flow" .yaml).log" 2>&1; then
      echo "  NOW PASSES: $(basename "$flow") (${issue:-no issue linked}): retest the issue and remove the known-bug tag"
    else
      echo "  still reproduces: $(basename "$flow") (${issue:-no issue linked})"
    fi
    long_timeout_off
  done
fi

echo "Reports: $out/report-*.xml"
exit $status
