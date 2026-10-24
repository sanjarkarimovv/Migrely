# Shared by the qa/*.sh scripts. Source it after setting QA_SERIAL.

# One QA script per device at a time: two runs on one emulator clear each other's data and settings.
# The lock is released when the script exits, also on failure.
qa_lock_device() {
  local lock="${TMPDIR:-/tmp}/migrely-qa-$QA_SERIAL.lock"
  if ! mkdir "$lock" 2>/dev/null; then
    local owner
    owner="$(cat "$lock/pid" 2>/dev/null || true)"
    if [[ -n "$owner" ]] && kill -0 "$owner" 2>/dev/null; then
      echo "$QA_SERIAL is in use by another QA run (pid $owner: $(ps -o command= -p "$owner" | cut -c1-80))." >&2
      exit 3
    fi
    # The previous owner is gone without cleaning up: take the lock over.
    rm -rf "$lock" && mkdir "$lock"
  fi
  echo $$ >"$lock/pid"
  QA_LOCK="$lock"
}

qa_unlock_device() {
  [[ -n "${QA_LOCK:-}" ]] && rm -rf "$QA_LOCK"
  return 0
}
