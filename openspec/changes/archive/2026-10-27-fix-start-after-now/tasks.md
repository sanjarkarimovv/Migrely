# Tasks

## 1. Fix

- [x] 1.1 BRD EC-ATK-03
- [x] 1.2 Tests first: update, merge and create-past each reject a start 2 or 3 minutes after now as `InFuture(Start)`.
  They fail before the change
- [x] 1.3 Domain: the three use cases check the start against now
- [x] 1.4 Details: the start picker ends at now

## 2. Verify

- [x] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [x] 2.2 `openspec validate fix-start-after-now --strict`
- [x] 2.3 On emulator-5570: in a past migraine's details, the start picker offers no time after now, and the end still
  takes up to 5 minutes ahead

## 3. Review

- [x] 3.1 Independent review (`superpowers:code-reviewer`)

## Verification record

- **Tests:**
  - The three new tests failed before the change (3 of 104).
  - After it, `:features:attack:domain:test` passes 105/105, including the edge pair: a start at now is stored, and
    now + 1 ms is refused.
- **Gate:** `assembleDebug test ktlintCheck detekt` passes.
- **Emulator (emulator-5570, API 36, debug), 0 FATAL EXCEPTION:**
  - **Details start picker:** today's date can be picked and later dates are disabled. A start 1 minute after now was
    refused with OK disabled; the current minute and an hour back were accepted and saved.
  - **End:** still accepted up to the tolerance (6:27 PM and 6:28 PM saved at 18:24 and 18:26).
  - **"Add a past migraine":** its start is still capped one minute before the current one, as before.
  - **The message, after the review fix:**
    - a start after the end, while before now, gives "Pick a time before the end.";
    - a start after now, while before the end, gives "Pick a time before now.".
- **Review findings:**
  - **The picker named only the end** even when now was the limit. It now names whichever applies.
  - **The proposal said the in-progress start was always now or earlier.** That is corrected: develop allowed up to now
    + 5 minutes.
  - **Accepted:** a start already stored after now blocks saving that attack's end until now passes it.
  - **Out of scope:** an import or a clock moved back can still bring an attack after now.
  - **Smaller fixes:** edge tests added, tests renamed, a long comment wrapped, and the picker reads a named `openedAt`.
- **Seen once, not reproduced:** an ANR at 18:34:04 ("Input dispatching timed out", 5 s) on the debug build. It came
  on tapping Next in the start date picker, right after saving an end.
  - At the dump, the main thread was idle in `MessageQueue.next`.
  - The emulator's CPU pressure was "some avg10=42.49", with two emulators and Gradle on the host.
  - The same flow ran again without an ANR.
  - Taken as host load. QA's R8 runs and monkey on 1.0.1, and Android vitals after release, are the checks that
    would show a real one.
