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
echo "$QA_SERIAL is back to its baseline: ${QA_BASELINE[*]}, app language from the system."
