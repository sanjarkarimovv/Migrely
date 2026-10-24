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
