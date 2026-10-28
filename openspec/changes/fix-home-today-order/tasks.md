# Tasks

## 1. Test first

- [x] 1.1 `HomeViewModelTest`, with an executor that answers out of order:
  - on the shortcut's path, `onEnter(true)` then `onResume()`, when the resume's read answers last, attack mode stays;
  - an older read's failure says nothing.
  Both fail with the guard taken out.

## 2. Fix

- [x] 2.1 `loadToday`: the newest read wins

## 3. Verify

- [x] 3.1 Gate: `assembleDebug test ktlintCheck detekt lint`
- [x] 3.2 `openspec validate fix-home-today-order --strict`
- [x] 3.3 On emulator-5570, with home open: fire the "Start migraine" shortcut intent and expect attack mode within 5 s, then end the attack. Ten times, one of them after a clock jump.

## 4. Review

- [x] 4.1 Independent review (`superpowers:code-reviewer`)

## Verification record

- **Gate:** `assembleDebug test ktlintCheck detekt lint` passes.
- **Unit tests:** `HomeViewModelTest` is 33/33.
- **Emulator walk (emulator-5570, API 36, debug build):**
  - The "Start migraine" shortcut intent was fired with home open, 11 times. Attack mode showed every time, within 3 s. The firings came:
    - right after Settings, History or details;
    - from HOME;
    - 1 s after a cold start;
    - 0.7 s after ending the previous attack;
    - while reopening from the launcher;
    - from the real launcher shortcut;
    - once after the clock jumped 120 h ahead. That time the header showed the new date.
  - **Crashes:** 0 crashes and 0 ANRs.
- **Review:** nothing blocking. Done in this change:
  - the test follows the shortcut's real path;
  - a helper for answers given later;
  - the class KDoc names the kept counter;
  - the proposal's wording.
- **Follow-up issue:** the same unordered reads in other view models: details, History, the trial banner, the medication prompt.
- **Seen in the walk, not changed:**
  - home learns of a manual clock change at its next 30-second tick, or on resume (design D11);
  - the attack screen shows no date.
