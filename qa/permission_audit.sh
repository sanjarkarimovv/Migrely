#!/usr/bin/env bash
# Fails when the built app asks for a permission outside the allowlist, and always on network access
# (BRD principle 4: health data never leaves the device).
#
#   qa/permission_audit.sh                        the debug APK
#   qa/permission_audit.sh <apk>                  any APK
#   qa/permission_audit.sh --list <file>          a saved list of permissions, one per line (self-test)
#
# A change that needs a new permission (for example POST_NOTIFICATIONS for reminders) adds it to ALLOWED with its BRD ID.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
ALLOWED=(
  # Added by AndroidX for receivers registered at runtime; not a user-facing permission.
  "com.migrely.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
  # Reminders (change 6): asked for only when a reminder is turned on (EC-REM-01, D65).
  "android.permission.POST_NOTIFICATIONS"
  # WorkManager reschedules the reminder work after a reboot (EC-REM-04).
  "android.permission.RECEIVE_BOOT_COMPLETED"
  # WorkManager holds a wake lock while the reminder work runs (D66).
  "android.permission.WAKE_LOCK"
  # Google Play Billing Library for Premium (change 9, D107); it talks to the Play Store app, not to the network.
  "com.android.vending.BILLING"
)
# No network at all (BRD privacy constraints, D43) and no exact alarms (EC-REM-03).
NEVER=("android.permission.INTERNET" "android.permission.ACCESS_NETWORK_STATE" "android.permission.SCHEDULE_EXACT_ALARM"
  "android.permission.USE_EXACT_ALARM")

if [[ "${1:-}" == "--list" ]]; then
  permissions="$(cat "$2")"
  source_name="$2"
else
  apk="${1:-$ROOT/app/build/outputs/apk/debug/app-debug.apk}"
  aapt2="$(ls -d "$SDK"/build-tools/*/ | sort -V | tail -1)aapt2"
  # Both "uses-permission:" and "uses-permission-sdk-23:" grant permissions.
  permissions="$("$aapt2" dump permissions "$apk" | sed -n "s/^uses-permission\(-sdk-23\)\{0,1\}: name='\([^']*\)'.*/\2/p")"
  source_name="$apk"
fi

status=0
while IFS= read -r permission; do
  [[ -z "$permission" ]] && continue
  for never in "${NEVER[@]}"; do
    if [[ "$permission" == "$never" ]]; then
      echo "FAIL: $permission is never allowed (health data must not leave the device)"
      status=1
      continue 2
    fi
  done
  allowed=false
  for ok in "${ALLOWED[@]}"; do [[ "$permission" == "$ok" ]] && allowed=true; done
  if $allowed; then
    echo "ok:   $permission"
  else
    echo "FAIL: $permission is not in the allowlist"
    status=1
  fi
done <<<"$permissions"

[[ $status -eq 0 ]] && echo "Permission audit passed for $source_name" || echo "Permission audit failed for $source_name"
exit $status
