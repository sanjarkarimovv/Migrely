# Shared by the qa/*.sh scripts. Source it after setting QA_SERIAL.

# One QA script per device at a time: two runs on one emulator clear each other's data and settings.
# The lock lives in the user's cache, not in TMPDIR, so every session on this machine sees the same lock.
QA_LOCK_ROOT="${HOME}/.cache/migrely-qa"

qa_lock_device() {
  mkdir -p "$QA_LOCK_ROOT"
  local lock="$QA_LOCK_ROOT/$QA_SERIAL.lock"
  local attempt
  for attempt in 1 2 3; do
    if mkdir "$lock" 2>/dev/null; then
      echo $$ >"$lock/pid"
      QA_LOCK="$lock"
      return 0
    fi
    local owner
    owner="$(cat "$lock/pid" 2>/dev/null || true)"
    if [[ -z "$owner" ]]; then
      # Another run has just created the lock and not written its pid yet: give it a moment.
      sleep 1
      owner="$(cat "$lock/pid" 2>/dev/null || true)"
    fi
    if [[ -n "$owner" ]] && kill -0 "$owner" 2>/dev/null; then
      echo "$QA_SERIAL is in use by another QA run (pid $owner: $(ps -o command= -p "$owner" | cut -c1-80))." >&2
      exit 3
    fi
    # The owner is gone without cleaning up. Move the stale lock aside atomically, so that of two runs taking it over
    # at the same time only one succeeds, then try again.
    mv "$lock" "$lock.stale.$$" 2>/dev/null && rm -rf "$lock.stale.$$"
  done
  echo "Could not take the lock for $QA_SERIAL ($lock)." >&2
  exit 3
}

qa_unlock_device() {
  # Only the owner releases the lock.
  if [[ -n "${QA_LOCK:-}" && "$(cat "$QA_LOCK/pid" 2>/dev/null || true)" == "$$" ]]; then
    rm -rf "$QA_LOCK"
  fi
  return 0
}

# The QA emulators' baseline: what a fresh QA AVD has and what every QA script must leave behind. Scripts save the
# values they find and restore those, so a value left over by a crashed run would be "restored" again; it is reported
# when a script starts. qa/reset_device.sh puts the baseline back.
QA_BASELINE=("system accelerometer_rotation=1" "system user_rotation=0" "system font_scale=1.0"
  "secure accessibility_interactive_ui_timeout_ms=null" "global animator_duration_scale=null" "global auto_time=1")

qa_adb() { "${ADB:-${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb}" -s "$QA_SERIAL" "$@"; }

qa_check_baseline() {
  local entry key want have
  for entry in "${QA_BASELINE[@]}"; do
    key="${entry%=*}"
    want="${entry##*=}"
    have="$(qa_adb shell settings get $key 2>/dev/null | tr -d '\r')"
    if [[ "$have" != "$want" ]]; then
      echo "Warning: $QA_SERIAL starts with $key=$have, not the baseline $want; this run restores $have." \
        "Run qa/reset_device.sh first if that value is left over." >&2
    fi
  done
}

qa_restore_setting() { # $1 namespace, $2 key, $3 value or "null": put it back and read it back
  local have
  if [[ "$3" == "null" ]]; then
    qa_adb shell settings delete "$1" "$2" >/dev/null 2>&1
  else
    qa_adb shell settings put "$1" "$2" "$3" >/dev/null 2>&1
  fi
  have="$(qa_adb shell settings get "$1" "$2" 2>/dev/null | tr -d '\r')"
  if [[ "$have" != "$3" ]]; then
    echo "Warning: could not restore $1 $2 to $3 (it is '${have:-unreadable}'); run qa/reset_device.sh." >&2
  fi
}
