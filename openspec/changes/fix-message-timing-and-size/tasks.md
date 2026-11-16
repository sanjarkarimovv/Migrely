# Tasks

**Base:** `feature/fix-message-timing-and-size` from `origin/develop` 40536f49.

**No GitHub issue exists** for these items (searched 2026-11-15: undo, timeout, "Time to take action", dark room,
snackbar). If QA files one for the Undo ceiling, commits and the PR say `Refs #N`, never a closing keyword.

**Scope:** no domain, data, permission, manifest or Room change. The only data-module edit is making the retention value
public (design D2).

**Work from the UX spec.** `migrely-work/t66/ux-spec.md` §B: B1, B2 (P1–P7) and B3. If a rule there and this file disagree,
ask `product-manager` before you code.

**Commands:**
- Run Gradle with `nice -n 19 ./gradlew --max-workers=2 …` under load.
- Use `set -o pipefail` with pipes, and never `gradlew … | grep … && git commit`.

## 0. Order

- [x] 0.1 Merge `origin/develop` after D180 (`bugfix/141-keyboard-follows-cursor`) and D181
  (`bugfix/217-privacy-wording-and-focus`) land, and resolve the spec overlap.
  - Keep every BRD row in numeric order: EC-UX-39…51 before 52…59, and §13 rows 48 and 49 before 50.
  - If either branch took D182, EC-UX-52…59, EC-ATK-24 or row 50, this change renumbers, because it merges second.
  - In `MigrelySnackbar.kt` keep D181's single spoken name, D180's no-IME rule and this change's size.

  Verify:
  - `git merge-base --is-ancestor origin/bugfix/141-keyboard-follows-cursor HEAD && git merge-base --is-ancestor origin/bugfix/217-privacy-wording-and-focus HEAD`
    exits 0;
  - `grep -c "^| D182 " docs/BRD.md` prints 1;
  - `openspec validate fix-message-timing-and-size --strict` exits 0.

  **Done 2026-11-16 (android-engineer), merge commit 3cd3144f** of `origin/develop` 03a8900e (develop had moved past
  50c784e5 to #232, D184, listing). Only `docs/BRD.md` conflicted; no code conflict, since this branch had no code.
  - Branch scan (`git branch -r`, every branch's BRD): D182, EC-UX-52…59, EC-ATK-24 and §13 row 50 exist only here.
    Others: D183 and row 51 on `feature/quiet-late-daily-reminder`, D184 on develop, D185–D188 and row 52 on
    `feature/add-word-of-mouth-loops`. No renumbering.
  - Order kept: EC-UX-39…51 then 52…59; §13 rows 48, 49, 50; §17 D180, D181, D182, D184.
  - The two bugfix branches are deleted on origin, so the ancestor check used their PR merge commits:
    `git merge-base --is-ancestor d3d98673 HEAD && git merge-base --is-ancestor 50c784e5 HEAD` → exit 0
    (d3d98673 = PR #225, 50c784e5 = PR #231).
  - `grep -c "^| D182 " docs/BRD.md` → 1.
  - `openspec validate fix-message-timing-and-size --strict` → exit 0; `openspec validate --all --strict` → 33 passed,
    0 failed.
  - Spec overlap: D180 and D181 each only ADD one requirement to `in-app-messages`; none of this change's three MODIFIED
    requirements is touched by them.

## 1. Plan (product-manager)

- [x] 1.1 BRD:
  - new: D182, EC-UX-52…59, EC-ATK-24, §13 row 50;
  - updated: EC-UX-02, EC-UX-16, D23 and §18.3 B.

  Verify: `grep -c "^| D182 " docs/BRD.md` prints 1, and `git show origin/develop:docs/BRD.md | grep -c "^| D182 "`
  prints 0.
- [x] 1.2 Proposal, design, the `in-app-messages` and `attack-mode` deltas, these tasks.
  Verify: `openspec validate fix-message-timing-and-size --strict` exits 0.

## 2. UX (ux-designer)

- [x] 2.1 Confirm that the deltas match §B. Nothing new is asked, and no copy changes.
  Verify: a reply to `product-manager` saying "matches" or listing differences by scenario name.
  **Done:** ux-t64 reviewed bc0f5896 on 2026-11-15 (1 error + 7 differences, folded in by PM cc2bab9c).

## 3. Spike (android-engineer, before the pause code)

- [ ] 3.1 TalkBack focus on a message's text and control, on API 36 (`emulator-5570`) and on API 26, on a device
  `team-lead` assigns (`emulator-5582` belongs to QA) (design D3).
  - **Pass:** 10 of 10 correct focus and clear events on each level, for the text and the control.
  - Also open TalkBack's own menu on a message and log `isWindowFocused` and the focus events, so we know whether that
    menu resumes the countdown.
  - The probe adds no semantics node, label, action or change to the reading order.
  - Write the result in this file under 3.1: the method, the counts, and the logcat or test file.
  - **On failure:** send `product-manager` the result. product-manager drops "The countdown pauses while the message holds
    TalkBack focus" from the delta and adds the known limit to D182 and EC-UX-54 before task 4.4 starts.

  Verify: the counts are written here. On failure, `openspec validate fix-message-timing-and-size --strict` exits 0 after
  the PM edit.

## 4. Shared mechanism (`:designsystem`)

- [ ] 4.1 **Red first, Home and History, before any production change** (EC-ATK-24).
  - Add the ceiling tests to `features/home/ui/src/testDebug/.../HomeMessagesTest.kt` and
    `features/history/ui/src/testDebug/.../HistoryMessagesTest.kt`.
  - Each test:
    - offers a deletion 20 s after `deletedAt`;
    - sets the interactive timeout to 120 s in a fake `AccessibilityManager`;
    - holds the Compose clock;
    - moves the wall and elapsed clocks to 122 s after `deletedAt`, through Robolectric's shadowed clock or the seam of 4.2;
    - drives the lifecycle through pause and resume;
    - asserts that "Undo" is gone and "This can no longer be undone." never appears.
  - Add a 114 s positive control: Undo restores and "Undone." is shown.
  - Add one red test for the timeout split (D182 (1)): with an interactive timeout of 20 s and a non-interactive one of
    8 s, the information message `IntakeUndone` on Log medication stays 20 s. On 40536f49 it leaves at 8 s. Log
    medication's own Undo button is a screen control, not a message, and stays out of scope (design Context).
  - If the tests need the clock seam, land 4.2's seam alone first, with no behaviour change, and run the red tests on that
    commit.

  Verify:
  - `./gradlew :features:home:ui:testDebugUnitTest --tests '*HomeMessagesTest*' :features:history:ui:testDebugUnitTest --tests '*HistoryMessagesTest*'`
    fails on the two 122 s tests, and only those;
  - `./gradlew :features:medication:ui:testDebugUnitTest --tests '*MedicationMessages*'` fails on the `IntakeUndone` test;
  - paste the failing test names and the SHA here.
- [ ] 4.2 Add `MessageClock` and `LocalMessageClock`, and `UNDO_CEILING_MILLIS = 115_000` in `DeletionOffer.kt`. Move the
  retention value to a public `DELETED_ATTACK_RETENTION_MILLIS` in `datasource/source`, with `DELETED_RETENTION` derived
  from it. Add the `:app` JVM test that ties them (design D2).
  Verify:
  - `./gradlew :app:testDebugUnitTest --tests '*UndoCeiling*'` passes;
  - the same run with the retention changed to 3 minutes as a mutation fails;
  - revert the mutation and record both results here.
- [ ] 4.3 Add `MessageCountdown`, the plain state machine: pause, the 300 ms grace, window focus, the 3 s floor, the
  ceiling's two bounds, saved state and the reboot rule (design D2).
  - Unit tests cover P1–P7 from ux §B2, a clock set back an hour, a clock set forward, and an elapsed clock below the saved
    stamp.
  - The saved state follows design D2: the pause ends and the 3 s floor applies at the save, and save-to-restore time
    counts as running time on `elapsedRealtime`. Tests cover a rotation while paused (about 3 s back), a restore after the
    time left at the save has run out (nothing, even if paused), and a timeout changed while shown (counted against the
    unpaused time already shown).
  - Negative control: with the floor set to 0, P2 fails.

  Verify: `./gradlew :designsystem:testDebugUnitTest --tests '*MessageCountdown*'` passes, and the negative control run
  fails.
- [ ] 4.4 Add `rememberMessageCountdown(...)`.
  - It sleeps until the next deadline.
  - It re-evaluates on `LifecycleResumeEffect` and when window focus returns.
  - It takes keyboard focus from `MessageFocus`, and TalkBack focus from the spike's method if 3.1 passed.
  - It feeds `timeLeft` from `MessageCountdown.timeLeftFraction`. The line stops while paused, never grows, goes on from
    where it stopped over the new time left, and reaches 0 at the earlier of timeout and ceiling. It replaces the
    per-host helpers that restart the line at `remaining / timeout`: `AttackMessages.kt:138` `rememberTimeLeft` and Home's
    equivalent.
  - The TalkBack probe adds no semantics node, label, action or change to the reading order.

  Verify: `./gradlew :designsystem:testDebugUnitTest` passes, including Robolectric tests where window focus loss resumes
  at once with no 300 ms grace, the line after a 0.5 s resume goes on from where it stopped (no jump), and the semantics
  tree with and without the probe is identical.
- [ ] 4.5 `messageTimeoutMillis` is the only timeout. Add a Konsist rule, next to `app/src/test/.../MessageCallSitesTest.kt`,
  that fails on `calculateRecommendedTimeoutMillis` outside `MessageTimeout.kt` (design D1).
  Verify: `./gradlew :app:testDebugUnitTest --tests '*MessageCallSites*'` fails on 40536f49's four hosts, which is the
  negative control, and passes after task 5.1.
- [ ] 4.6 Add `LocalInDarkRoom` in `MigrelyDarkRoom`. In a dark room `MigrelySnackbar` uses a 64 dp minimum height, a
  64 x 64 dp minimum for Undo, and a 64 x 64 dp Dismiss with a 24 dp icon (design D4).
  The size changes in the same frame as the room's switch, with no size animation of its own, and the control keeps its
  node so focus and the pause stay.
  Verify:
  - `./gradlew :designsystem:testDebugUnitTest --tests '*Snackbar*'` passes, with bounds of at least 64 dp inside an active
    room and 48 dp outside, and a test that switches the room under a focused control and finds the new size in the next
    frame with focus kept;
  - with the local forced false, the test fails.

## 5. Hosts (each `ui` module)

- [ ] 5.1 Home, History, Attack details and Add past migraine, and the medication screens:
  - drop the local `timeoutMillis`;
  - use `messageTimeoutMillis` and `rememberMessageCountdown`;
  - deletion messages pass `deletedAtEpochMillis` as the action time.

  Verify:
  - `./gradlew :features:home:ui:testDebugUnitTest :features:history:ui:testDebugUnitTest :features:attack:ui:testDebugUnitTest :features:medication:ui:testDebugUnitTest`
    passes, including 4.1's tests, now green;
  - a per-host test shows "Saved." or an information message leaving at 20 s with an interactive 20 s and a
    non-interactive 8 s timeout.
- [ ] 5.2 Reminders, Doctor report, Your data, Settings, Onboarding and Paywall use `rememberMessageCountdown`. Their
  timeout is already interactive.
  Verify: `./gradlew :features:reminders:ui:testDebugUnitTest :features:report:ui:testDebugUnitTest :features:backup:ui:testDebugUnitTest :features:settings:ui:testDebugUnitTest :features:onboarding:ui:testDebugUnitTest :features:subscription:ui:testDebugUnitTest`
  passes.
- [ ] 5.3 Saved state per host covers `runMillis` (pause ended and floor applied at the save), `savedElapsedMillis` and the
  stamps. Rotation and process-death restore tests cover the scenarios "Rotate while paused", "Process death while
  paused", "Restored after the time ran out" and "Restored after the ceiling".
  Verify: `./gradlew :features:home:ui:testDebugUnitTest --tests '*Restore*' :features:history:ui:testDebugUnitTest --tests '*Restore*'`
  passes.

## 6. Negative control on the fix

- [ ] 6.1 With the resume re-evaluation removed from `rememberMessageCountdown` as a temporary edit, 4.1's 122 s tests fail
  again on Home and History. Restore the edit.
  Verify: paste the failing and then the passing test names, and confirm `git diff` is clean afterwards.

## 7. Device walk (android-engineer, own emulator only)

- [ ] 7.1 Walk ux §B2's device list and §B3's bounds on API 36 (`emulator-5570`) and API 26 (as `team-lead` assigns):
  keyboard and TalkBack, Home attack mode portrait 200%, History, Reminders, landscape attack mode, dark-room Log
  medication.
  - Run the ceiling path of design A4 on the branch.
  - Check that a touch ending keyboard mode counts as focus leaving (scenario "A touch ends keyboard mode").
  - Check the 48 to 64 dp change when "Migraine ended" replaces a message on Home, and the reverse after "Undo": same frame,
    no jump.
  - Restore font scale, rotation, the a11y timeout and the locale.

  Verify: one line per check in this file with the result, and `adb shell settings get secure accessibility_interactive_ui_timeout_ms`
  printing the original value at the end.

## 8. Gate, review, docs

- [ ] 8.1 Gate. Verify: `./gradlew assembleDebug test ktlintCheck detekt lint` exits 0.
- [ ] 8.2 `code-reviewer`, then fixes.
  Verify: no open blocker in the review notes.
- [ ] 8.3 QA independent check, design "Acceptance" A1–A6 on the branch SHA, with `develop` 40536f49 as the negative
  control for A1 and A4.
  Verify: the QA report lists A1–A6 with counts.
- [ ] 8.4 Archive the change, update the BRD §13 row status, PR into `develop`, self-merge with a merge commit.
  Verify: `openspec list` no longer shows `fix-message-timing-and-size`, and `openspec validate --all --strict` exits 0.
