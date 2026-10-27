#!/usr/bin/env bash
# Installs the split APKs that Google Play builds from a bundle for one device profile, the way a user gets the app,
# and checks the app in that profile's language (BRD D117, D204). Play splits the bundle by language, density and ABI,
# so a resource or locale mistake that the universal APK hides shows only here.
#
#   qa/split_apks.sh <app-release.aab> ru-26            on emulator-5582 (Android 8.0)
#   qa/split_apks.sh <app-release.aab> he-36            on emulator-5580; also uz-36 and en-36
#   qa/split_apks.sh --smoke <app-release.aab> <profile>    then the smoke suite on the installed splits
#
# A profile is a bundletool device spec in qa/fixtures/device-specs/. The bundle is signed with the debug key here, as QA
# has no upload key (D102). The checks:
#   1. the profile's language split is among the APKs, and `pm path` lists every one of them;
#   2. the profile's language is read back from the device, so a language that was never applied cannot pass;
#   3. in that language, home, History and the date picker show English month and day names, the region's date order
#      and first weekday, and no Cyrillic or Hebrew in the app's own text (D117).
# The language is the app's own on API 33 and later. On Android 8.0 it is the system's, set through the emulator's
# Custom Locale app. Either way it is put back on exit, even when a step fails, and the debug APK under test
# ($QA_APK, as for qa/device_scenarios.sh) is installed again, since the other QA scripts need a debuggable build.
#
# Covers: quality-gates › Release Go/No-Go gate › The APKs users install
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APP="com.migrely.app"
export MAESTRO_CLI_NO_ANALYTICS=1
SMOKE=false
if [[ "${1:-}" == "--smoke" ]]; then SMOKE=true; shift; fi
AAB="${1:?usage: qa/split_apks.sh [--smoke] <app-release.aab> <profile>}"
PROFILE="${2:?usage: qa/split_apks.sh [--smoke] <app-release.aab> <profile>}"
SPEC="$ROOT/qa/fixtures/device-specs/$PROFILE.json"
[[ -f "$AAB" ]] || { echo "No bundle at $AAB" >&2; exit 2; }
[[ -f "$SPEC" ]] || { echo "No profile $PROFILE (looked for $SPEC)" >&2; exit 2; }

# The device follows the profile's Android version: Android 8.0 is emulator-5582, the target version emulator-5580.
read -r LOCALE SDK_WANTED ABIS < <(python3 -I -c "import json, sys
s = json.load(open(sys.argv[1]))
print(s['supportedLocales'][0], s['sdkVersion'], ','.join(s['supportedAbis']))" "$SPEC")
if [[ "$SDK_WANTED" -lt 29 ]]; then default_serial=emulator-5582; else default_serial=emulator-5580; fi
QA_SERIAL="${QA_SERIAL:-$default_serial}"
case "$QA_SERIAL" in
  emulator-5580 | emulator-5582) ;;
  *) echo "Refusing to run on $QA_SERIAL: QA runs only on emulator-5580 or emulator-5582." >&2; exit 2 ;;
esac
source "$ROOT/qa/lib.sh"
sdk="$(qa_adb shell getprop ro.build.version.sdk | tr -d '\r')"
abi="$(qa_adb shell getprop ro.product.cpu.abi | tr -d '\r')"
[[ "$sdk" == "$SDK_WANTED" ]] || { echo "$QA_SERIAL runs API $sdk, but profile $PROFILE is API $SDK_WANTED." >&2; exit 2; }
[[ ",$ABIS," == *",$abi,"* ]] || { echo "$QA_SERIAL is $abi, but profile $PROFILE has $ABIS." >&2; exit 2; }

# The region's first weekday and date order, from CLDR. D117 formats with English and the phone's country: en-IL takes
# its day-first order from en-001, while en-RU and en-UZ, which CLDR does not have, fall back to en's month-first.
case "$LOCALE" in
  *-RU | *-UZ) FIRST_DAY=Monday DAY_FIRST=false ;;
  *-US) FIRST_DAY=Sunday DAY_FIRST=false ;;
  *-IL) FIRST_DAY=Sunday DAY_FIRST=true ;;
  *) echo "No first weekday or date order is known for $LOCALE; add it here." >&2; exit 2 ;;
esac
language="${LOCALE%%-*}"
# bundletool names the Hebrew split by Android's old code.
split_language="$language"; [[ "$language" == "he" ]] && split_language=iw

QA_APK="${QA_APK:-$ROOT/app/build/outputs/apk/debug/app-debug.apk}"
out="$(mktemp -d "${TMPDIR:-/tmp}/split-apks.XXXXXX")"
LANGUAGE_SET=""
ORIGINAL_SYSTEM_LOCALE=""
SPLITS_INSTALLED=false
restore_language() {
  if [[ "$LANGUAGE_SET" == app ]]; then
    qa_adb shell cmd locale set-app-locales "$APP" --locales "" >/dev/null 2>&1 || true
  elif [[ "$LANGUAGE_SET" == system ]]; then
    # Android 8.0 has no per-app language; the emulator's Custom Locale app changes the system's, as Settings would.
    qa_set_system_locale "$ORIGINAL_SYSTEM_LOCALE" ||
      echo "Warning: the system language is not back to $ORIGINAL_SYSTEM_LOCALE; run qa/reset_device.sh." >&2
  fi
  LANGUAGE_SET=""
}
restore_app() {
  $SPLITS_INSTALLED || return 0
  SPLITS_INSTALLED=false
  if [[ -f "$QA_APK" ]]; then
    qa_adb uninstall "$APP" >/dev/null 2>&1 || true
    qa_adb install "$QA_APK" >/dev/null 2>&1 || echo "Warning: could not install $QA_APK again." >&2
  else
    echo "Warning: no debug APK at $QA_APK; the release build from the splits stays installed." >&2
  fi
}
finish() {
  restore_language
  restore_app
  qa_unlock_device
}
trap finish EXIT

app_texts() { # every text and label of the app's own window, one per line
  qa_adb exec-out uiautomator dump /dev/tty 2>/dev/null | python3 -I -c "import sys, xml.etree.ElementTree as ET
raw = sys.stdin.read(); raw = raw[raw.find('<?xml'):raw.rfind('>') + 1]
for n in ET.fromstring(raw).iter('node'):
    if n.get('package') != sys.argv[1]:
        continue
    for label in (n.get('text') or '', n.get('content-desc') or ''):
        if label:
            print(label.replace('\n', ' '))" "$APP"
}

check_texts() { # $1 file of texts, $2 what must be there (Python regex, full match of one line), $3 name of the screen
  python3 -I -c "import re, sys
lines = open(sys.argv[1], encoding='utf-8').read().splitlines()
foreign = [l for l in lines if re.search('[Ѐ-ӿ֐-׿]', l)]
if foreign:
    print(f'  FAILED: {sys.argv[3]} shows text that is not English: {foreign[:3]}'); sys.exit(1)
if not any(re.fullmatch(sys.argv[2], l) for l in lines):
    print(f'  FAILED: {sys.argv[3]} has no line matching {sys.argv[2]}'); sys.exit(1)
print(f'    {sys.argv[3]}: English, {len(lines)} texts')" "$1" "$2" "$3"
}

run_step() { # $1 device step in .maestro/device
  echo "  flow $1"
  maestro --device "$QA_SERIAL" test "$ROOT/.maestro/device/$1.yaml" --test-output-dir "$out/$1" >"$out/$1.log" 2>&1 || {
    echo "  FAILED: $1 (log: $out/$1.log)"
    return 1
  }
}

qa_lock_device
qa_check_baseline
echo "Split APKs of $(basename "$AAB") for $PROFILE ($LOCALE, API $sdk, $abi) on $QA_SERIAL"

# 1. The splits Play would deliver to this profile, installed together as one app.
bundletool build-apks --bundle="$AAB" --output="$out/app.apks" --overwrite --ks="$HOME/.android/debug.keystore" \
  --ks-pass=pass:android --ks-key-alias=androiddebugkey --key-pass=pass:android >"$out/build-apks.log" 2>&1 ||
  { echo "  FAILED: bundletool build-apks (log: $out/build-apks.log)"; exit 1; }
bundletool extract-apks --apks="$out/app.apks" --device-spec="$SPEC" --output-dir="$out/splits" >"$out/extract-apks.log" 2>&1 ||
  { echo "  FAILED: bundletool extract-apks (log: $out/extract-apks.log)"; exit 1; }
splits=("$out"/splits/*.apk)
echo "    splits: $(cd "$out/splits" && ls | tr '\n' ' ')"
[[ -f "$out/splits/base-$split_language.apk" ]] ||
  { echo "  FAILED: no $split_language language split for $PROFILE"; exit 1; }
qa_adb uninstall "$APP" >/dev/null 2>&1 || true
SPLITS_INSTALLED=true
qa_adb install-multiple "${splits[@]}" >"$out/install.log" 2>&1 ||
  { echo "  FAILED: install-multiple (log: $out/install.log)"; exit 1; }
installed="$(qa_adb shell pm path "$APP" | tr -d '\r' | grep -c '^package:' || true)"
[[ "$installed" == "${#splits[@]}" ]] ||
  { echo "  FAILED: pm path lists $installed APKs, not ${#splits[@]}"; exit 1; }
echo "    pm path: $installed APKs installed"

# 2. The app in the profile's language, read back from the device.
run_step language-setup
if [[ "$sdk" -ge 33 ]]; then
  LANGUAGE_SET=app
  qa_adb shell cmd locale set-app-locales "$APP" --locales "$LOCALE" >/dev/null
  have="$(qa_adb shell cmd locale get-app-locales "$APP" | tr -d '\r')"
  [[ "$have" == *"[$LOCALE]"* ]] || { echo "  FAILED: the app's language is not $LOCALE: $have"; exit 1; }
else
  ORIGINAL_SYSTEM_LOCALE="$(qa_system_locale)"
  LANGUAGE_SET=system
  qa_set_system_locale "$LOCALE"
fi
echo "    language: $LOCALE ($LANGUAGE_SET), read back from the device"

# 3. English names, the region's date order and first weekday (D117).
months='January|February|March|April|May|June|July|August|September|October|November|December'
weekdays='Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday'
if $DAY_FIRST; then order="day first"; dated=".*\b($weekdays),? [0-9]{1,2} ($months)\b.*"
else order="month first"; dated=".*\b($weekdays), ($months) [0-9]{1,2}\b.*"; fi
run_step language-home
app_texts >"$out/home.txt"
check_texts "$out/home.txt" "$dated" "Home, today's date $order,"
run_step language-history
app_texts >"$out/history.txt"
check_texts "$out/history.txt" ".*\b($months)\b.*" "History"
run_step language-picker
app_texts >"$out/picker.txt"
check_texts "$out/picker.txt" "Cancel" "The date picker"
first="$(grep -m1 -xE "$weekdays" "$out/picker.txt" || true)"
[[ "$first" == "$FIRST_DAY" ]] ||
  { echo "  FAILED: the date picker's week starts on '${first:-nothing}', not $FIRST_DAY"; exit 1; }
echo "    the week starts on $first"
qa_adb shell input keyevent KEYCODE_BACK

# 4. The smoke suite on the same splits. maestro.sh takes the device for itself, so this run lets it go first. On
# Android 8.0 the system language stays the profile's for the suite; on later versions the suite's clean starts clear
# the app's own language.
if $SMOKE; then
  qa_unlock_device
  QA_SERIAL="$QA_SERIAL" "$ROOT/qa/maestro.sh" --tags smoke
  qa_lock_device
fi
restore_language
echo "Split APKs passed for $PROFILE (details: $out)"
