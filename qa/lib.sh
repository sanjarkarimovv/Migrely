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
  "secure accessibility_interactive_ui_timeout_ms=null" "secure accessibility_non_interactive_ui_timeout_ms=null"
  "global animator_duration_scale=null" "global auto_time=1" "global zen_mode=0" "global airplane_mode_on=0")

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
  have="$(qa_system_locale)"
  if [[ "$have" != "$QA_BASELINE_LOCALE" ]]; then
    echo "Warning: $QA_SERIAL starts with the system language $have, not the baseline $QA_BASELINE_LOCALE." \
      "Run qa/reset_device.sh first if that value is left over." >&2
  fi
}

# The system language of both QA AVDs. Android 8.0 has no per-app language, so qa/split_apks.sh changes the system's
# there, through the emulator's Custom Locale app.
QA_BASELINE_LOCALE=en-US
qa_system_locale() { # the system language as a BCP-47 tag; a fresh AVD has no persist.sys.locale yet
  local tag
  tag="$(qa_adb shell getprop persist.sys.locale | tr -d '\r')"
  [[ -n "$tag" ]] || tag="$(qa_adb shell getprop ro.product.locale | tr -d '\r')"
  echo "${tag:-unknown}"
}
qa_set_system_locale() { # $1 BCP-47 tag: sets the system language and waits up to 20 s for it to apply
  local want="$1" have="" end
  # Custom Locale takes "ru_RU" and logs "Invalid locale: expected ab_CD" for "ru-RU" (API 26 image), changing nothing.
  # A tag with a script, such as uz-Latn-UZ, has no ab_CD form.
  [[ "$want" =~ ^[a-z]{2,3}-[A-Z]{2}$ ]] || { echo "  FAILED: $want is not a language-COUNTRY tag" >&2; return 1; }
  qa_adb shell am broadcast -a com.android.intent.action.SET_LOCALE --es com.android.intent.extra.LOCALE "${want//-/_}" \
    com.android.customlocale2 >/dev/null
  end=$((SECONDS + 20))
  while ((SECONDS < end)); do
    have="$(qa_system_locale)"
    [[ "$have" == "$want" ]] && return 0
    sleep 0.5
  done
  echo "  FAILED: the system language is $have, not $want" >&2
  return 1
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
