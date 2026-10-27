#!/usr/bin/env bash
# Puts a QA emulator back to its baseline settings (qa/lib.sh QA_BASELINE), for example after an emulator crashed in
# the middle of a run and left the screen in landscape or a long accessibility timeout.
#
#   qa/reset_device.sh                          emulator-5580
#   QA_SERIAL=emulator-5582 qa/reset_device.sh
set -euo pipefail

QA_SERIAL="${QA_SERIAL:-emulator-5580}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
case "$QA_SERIAL" in
  emulator-5580 | emulator-5582) ;;
  *) echo "Refusing to run on $QA_SERIAL: QA runs only on emulator-5580 or emulator-5582." >&2; exit 2 ;;
esac
source "$ROOT/qa/lib.sh"
qa_lock_device
trap qa_unlock_device EXIT
for entry in "${QA_BASELINE[@]}"; do
  key="${entry%=*}"
  qa_restore_setting ${key% *} ${key#* } "${entry##*=}"
done
qa_adb shell cmd locale set-app-locales com.migrely.app --locales "" >/dev/null 2>&1 || true
# qa/split_apks.sh stopped half-way on Android 8.0 may leave the system language changed.
if [[ "$(qa_system_locale)" != "$QA_BASELINE_LOCALE" ]]; then
  qa_set_system_locale "$QA_BASELINE_LOCALE"
fi
# A device scenario stopped half-way may leave the browser disabled (privacy_link).
disabled="$(qa_adb shell pm list packages -d 2>/dev/null | tr -d '\r')"
if grep -qx "package:com.android.chrome" <<<"$disabled"; then
  qa_adb shell pm enable com.android.chrome >/dev/null
fi
echo "$QA_SERIAL is back to its baseline: ${QA_BASELINE[*]}, system language $QA_BASELINE_LOCALE," \
  "app language from the system."
