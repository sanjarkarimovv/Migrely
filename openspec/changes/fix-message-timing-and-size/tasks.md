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

  **Status 2026-11-16, 23:44 (android-engineer):** the API 36 automated rows pass. The API 26 half could not be run.
  Real TalkBack gestures and TalkBack's menu are not checked. Result and method are below the preparation notes.
  - First record, 2026-11-16 22:20: nothing had run on a device yet.
  - **API 26 half: not runnable on our API 26 image.**
    - The image has no TalkBack (team-lead's information). I could not confirm it with `pm list packages`: the guard
      allows android-engineer only `emulator-5570`, and 5572 was assigned by mistake.
    - No TalkBack APK may be downloaded or sideloaded without the owner.
    - A run without TalkBack proves nothing, for a reason in Compose UI 1.9.3
      (`AndroidComposeViewAccessibilityDelegateCompat`):
      - focus and clear events go out only when `isEnabled`, that is `accessibilityManager.isEnabled &&
        enabledServices.isNotEmpty()`. UiAutomation is never in that list; Compose's own comment says this filters it
        out;
      - `requestAccessibilityFocus` returns false unless touch exploration is on.
    - So UiAutomation alone gets no focus and no events. The internal `accessibilityForceEnabledForTesting` would only
      test a forced path. team-lead ruled out both as evidence.
  - **API 36 half (real TalkBack)** waits for `emulator-5570`, once the other project's `emulator-5560` is closed.
  - **Prepared for that run** (androidTest only, no production code):
    - `designsystem/src/androidTest/.../snackbar/AccessibilityFocusProbe.kt`, the probe, using public APIs only:
      - a `View.AccessibilityDelegate` set on the ComposeView, the parent of the AndroidComposeView;
      - `onRequestSendAccessibilityEvent` catches `TYPE_VIEW_ACCESSIBILITY_FOCUSED` and `_CLEARED`;
      - on each one it asks the AndroidComposeView's node provider for `findFocus(FOCUS_ACCESSIBILITY)`, which Compose
        implements, and checks whether that node's screen bounds lie inside the message;
      - every other delegate method keeps its default, so it adds no semantics node, label or action and does not
        change the reading order;
      - open for 4.4: a delegate already on that ComposeView would need chaining, and `getAccessibilityDelegate` is
        API 29+.
    - `designsystem/src/androidTest/.../snackbar/TalkBackFocusSpikeTest.kt`, which skips itself unless TalkBack is on
      (`isEnabled && isTouchExplorationEnabled`), so normal instrumented runs are unaffected:
      - UiAutomation, with `FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES` so that TalkBack stays on, moves accessibility
        focus the way a TalkBack swipe does;
      - text: 10 rounds of focus on "Migraine deleted", seen inside, then onto "Content", seen outside;
      - control: the same on "Undo the deletion";
      - move: 10 rounds from text to control, where every event between them must read inside, including Compose's
        clear of the text;
      - what TalkBack reads (each node's class, text, description, actions and bounds from UiAutomation) is the same
        with and without the probe;
      - every event is logged under the tag `T64Probe` with `windowFocused`.
    - Checked without a device: `:designsystem:compileDebugAndroidTestKotlin :designsystem:ktlintCheck
      :designsystem:detekt` exit 0, and `:designsystem:ktlintAndroidTestSourceSetCheck --rerun` exits 0.
  - **Still to do on 5570:**
    - turn TalkBack on and record the original `enabled_accessibility_services` and `accessibility_enabled`;
    - `ANDROID_SERIAL=emulator-5570 ./gradlew :designsystem:connectedDebugAndroidTest
      -Pandroid.testInstrumentationRunnerArguments.class=com.migrely.app.designsystem.snackbar.TalkBackFocusSpikeTest`;
    - `adb logcat -s T64Probe`;
    - by hand: open TalkBack's menu on the message and log `windowFocused` and the events;
    - real swipes for a sample of the moves, since UiAutomation's focus action is not a TalkBack gesture;
    - a negative control on the "reads the same" check: a probe that sets a description on its host must fail it;
    - restore the settings.

  **API 36 result, 2026-11-16, `emulator-5570`, `Migrely_API_36`, TalkBack 16.0.0.738667889:**
  - **Method.**
    - TalkBack was turned on through `enabled_accessibility_services`. TalkBack bound, and touch exploration was on.
    - Command: `ANDROID_SERIAL=emulator-5570 nice -n 19 ./gradlew --max-workers=2 :designsystem:connectedDebugAndroidTest
      -Pandroid.testInstrumentationRunnerArguments.class=com.migrely.app.designsystem.snackbar.TalkBackFocusSpikeTest`.
    - The probe log comes from `adb logcat -d 'T64Probe:I' '*:S'`.
    - Logs are kept in `migrely-work/t64/`: `spike-runN.log` and `spike-runN-probe.log`, for N = 1 to 9.
  - **Final harness, runs 7, 8 and 9 in a row. Each run exits 0 and passes all 5 automated tests.**
    - Text: 10 of 10.
    - Control: 10 of 10.
    - Text to control: 10 of 10, each checked against Android's own `isAccessibilityFocused`.
    - What TalkBack reads is the same with and without the probe.
    - The negative control passes: a delegate that adds a description makes the reading differ, so the check can fail.
    - `holdForChecksByHand` is skipped, as intended.
  - **Pass bar of 10 of 10 per row: met on all three runs.**
  - **What a move looks like.**
    - Each move sends two `FOCUS_CLEARED` events, then `FOCUSED`. The first clear still reads the old node; the second
      reads no focus.
    - So from text to control the probe reads "outside" for a moment. Longest gap per round:
      - run 7: 7, 3, 5, 2, 4, 1, 2, 3, 1, 1 ms;
      - run 8: 1, 1, 11, 1, 2, 2, 1, 2, 1, 1 ms;
      - run 9: 1, 3, 2, 1, 5, 4, 8, 3, 1, 1 ms.
    - The largest gap of any round, in runs 4 to 9, was 21 ms, against the 300 ms grace.
    - Every focus event had `windowFocused=true`. The only `windowFocused=false` events are the clears at test
      teardown.
  - **Earlier runs, kept as results.**
    - Run 1: two failures, both from the harness.
      - The move test required every event to read "inside", which is stricter than the design's grace.
      - The reading dump included where TalkBack's focus sat (actions 64 and 128), and TalkBack moved that focus
        between the two dumps.
    - Run 2: 1 of 10 on the move test. Run 3: 0 of 10 on the move test alone. Neither logged the action's result.
    - Run 5: the text test failed at 1 of 10.
      - The probe saw the focus at 23:36:43.191.
      - The `performAction` that caused it returned `false` 2 s later.
      - The text was then already focused, so seven more calls came back false at once.
      - The rounds failed through the driver's result. The probe's readings were right.
    - Since then the harness:
      - only logs the action's result;
      - judges each round by the probe, checked against `isAccessibilityFocused`;
      - leaves out the focus-only actions 64 and 128 from the reading dump.
    - Run 9 shows one late `false` result, which this harness rightly ignores.
  - **Not checked: real TalkBack gestures and TalkBack's menu.**
    - Three different ways of making a gesture from adb failed:
      - `input swipe` and `input touchscreen swipe` (TalkBack's touch explorer saw nothing);
      - TalkBack's keyboard keys through `input keycombination` (Alt + arrows; nothing);
      - raw `sendevent` on `/dev/input/event1` (denied by SELinux; I did not root the image).
    - So the menu question is open: does TalkBack's menu take window focus, and so resume the countdown?
    - What the run does show:
      - with TalkBack on, TalkBack focused the message's text by itself, and the probe saw it
        (`spike-manual.log`);
      - every focus move in runs 4 to 9 went through Compose's real path with TalkBack bound.
    - Gestures and the menu need a person at the emulator window or on a phone.
  - **Restored.** Every setting matches the values recorded before the run (`diff` of
    `migrely-work/t64/spike-original-settings.txt` against `spike-final-settings.txt` is empty), and no service is
    bound. The emulator was shut down with `emu kill`, and `adb devices` is empty.

## 4. Shared mechanism (`:designsystem`)

- [x] 4.1 **Red first, Home and History, before any production change** (EC-ATK-24).
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

  **Done 2026-11-16 (android-engineer).** The tests needed the clock seam, so it landed alone first:
  - **740626db**: `MessageClock`, `SystemMessageClock` and `LocalMessageClock` in `designsystem/snackbar/MessageClock.kt`.
    Nothing reads them yet, so behaviour can't change. On that commit `:designsystem:testDebugUnitTest` (189 tests),
    `HomeMessagesTest` (13), `HistoryMessagesTest` (7) and `MedicationMessagesTest` (4) all pass, with 0 failures,
    along with `:designsystem:ktlintCheck :designsystem:detekt`.
  - **bdc89f0d**: the red tests, with no production change. Each UI module's tests get `MessageFakes.kt`, holding
    `FakeAccessibilityManager` (interactive and reading timeouts apart) and `FakeMessageClock` (wall and elapsed
    clocks moving together). As with `SpokenName.kt`, the copies stay per module because the modules share no test
    module.
  - How the tests work:
    - The Home and History tests stamp the deletion on the phone's wall clock (the hosts still read
      `System.currentTimeMillis()` when a deletion arrives, and History offers only deletions made after it opened).
    - They offer it 20 s later, with Undo shown, then hold the Compose clock.
    - They move the fake clocks to 122 s or 114 s after the deletion between `CREATED` and `RESUMED`, then let 100 ms
      of frames run.
    - The Undo callback fakes D23: "Undone." within 120 s, otherwise "This can no longer be undone.".

  Command, from `bdc89f0d`, with load under 7:
  `nice -n 19 ./gradlew --max-workers=2 --continue :designsystem:ktlintCheck :designsystem:detekt
  :features:{home,history,medication}:ui:ktlintCheck :features:{home,history,medication}:ui:detekt
  :features:home:ui:testDebugUnitTest :features:history:ui:testDebugUnitTest :features:medication:ui:testDebugUnitTest`.
  It exits 1 because of the three red tests and nothing else:
  - every ktlint and detekt task passes;
  - Home: 120 tests, 1 failed. `HomeMessagesTest > Lock and unlock at 122 seconds on Home leaves no Undo`
    (`HomeMessagesTest.kt:302`): `Failed: assertDoesNotExist. Reason: Did not expect any node but found '1' node that
    satisfies: (ContentDescription = 'Undo the deletion' (ignoreCase: false))`.
  - History: 58 tests, 1 failed. `HistoryMessagesTest > Lock and unlock at 122 seconds on History leaves no Undo`
    (`HistoryMessagesTest.kt:216`): the same assertion message.
  - Medication: 91 tests, 1 failed. `MedicationMessagesTest > A removed intake on Log medication stays for the time to
    take action` (`MedicationMessagesTest.kt:145`, the 19 s check): `Failed: assertExists. Reason: Expected exactly
    '1' node but could not find any node that satisfies: (Text + InputText + EditableText contains 'Removed. Nothing
    was logged.' (ignoreCase: false))`. It left at 8 s.
  - The positive controls pass: `Undo before the ceiling still restores on Home` and `... on History`.
  - What these runs do not prove:
    - That the 122 s tests turn green for the right reason. Task 6.1's negative control covers that.
    - Robolectric's lifecycle is not a real lock. Task 7.1 and QA's A4 cover the device.
- [x] 4.2 Add `MessageClock` and `LocalMessageClock`, and `UNDO_CEILING_MILLIS = 115_000` in `DeletionOffer.kt`. Move the
  retention value to a public `DELETED_ATTACK_RETENTION_MILLIS` in `datasource/source`, with `DELETED_RETENTION` derived
  from it. Add the `:app` JVM test that ties them (design D2).
  Verify:
  - `./gradlew :app:testDebugUnitTest --tests '*UndoCeiling*'` passes;
  - the same run with the retention changed to 3 minutes as a mutation fails;
  - revert the mutation and record both results here.

  **Done 2026-11-16 (android-engineer).** The clock seam landed first, in 740626db (see 4.1).
  - `DELETED_ATTACK_RETENTION_MILLIS = 120_000L` is in
    `datasource/source/.../attack/datasource/DeletedAttackRetention.kt`.
  - `AttackDataSourceImpl`'s `DELETED_RETENTION` is now `Duration.ofMillis(DELETED_ATTACK_RETENTION_MILLIS)`, so its
    value is unchanged.
  - `UNDO_CEILING_MILLIS = 115_000L` is in `DeletionOffer.kt`.
  - The tie test is `app/src/test/java/com/migrely/app/message/UndoCeilingTest.kt`.

  Results:
  - `:app:testDebugUnitTest --tests '*UndoCeiling*'`: 1 test, it passes.
  - Mutation, with the retention at `180_000L`: exit 1, `UndoCeilingTest > The Undo ceiling is the deleted attack's
    retention less 5 seconds FAILED`, `expected:<175000> but was:<115000>`.
  - After the revert: exit 0.
  - In the same run as the pass, `:datasource:implementation:testDebugUnitTest` passes all 229 tests.
  - ktlint and detekt pass on `:datasource:source`, `:datasource:implementation`, `:designsystem` and `:app`.
- [x] 4.3 Add `MessageCountdown`, the plain state machine: pause, the 300 ms grace, window focus, the 3 s floor, the
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

  **Done 2026-11-16 (android-engineer).** `designsystem/.../snackbar/MessageCountdown.kt` holds `MessageCountdown`,
  `CountdownHold`, `UndoCeiling`, `SavedMessageCountdown` and a private `TimeLeftLine`. They are plain Kotlin, with no
  Compose types.
  - The class reads `MessageClock` on each call and first settles a grace that has run out.
  - The grace, 300 ms, does not count as shown time.
  - Window focus loss resumes at once.
  - The 3 s floor applies whenever a pause or a grace ends, and at a save while paused. It never passes the ceiling.
  - `save()` leaves the live countdown unchanged, because Compose saves on every move to the background too.
  - `restore()` returns null when the time or the ceiling has passed, or when the elapsed clock reads below the saved
    stamps (a restart).

  Where the API differs from design D2's wording, with the same rules:
  - The focus inputs come in as one `update(CountdownHold(keyboardFocus, screenReaderFocus, screenReaderOn,
    windowFocused))`, not as separate calls. 4.4 can feed it straight from Compose state.
  - The saved model has no `shownWallMillis`. The countdown never reads it, and for an Undo that is not a deletion it
    is the `actionWallMillis` that is saved.
  - The saved model adds `lineFraction`, so the line cannot grow after a restore.
  - The line runs on `elapsedRealtime` from the fraction where it stopped. A wall clock set forward can end an Undo
    before its line reaches 0. The message still leaves on time.

  Results:
  - `:designsystem:testDebugUnitTest --tests '*MessageCountdown*'`: 27 tests, 0 failures. They cover P1–P7, the grace,
    TalkBack (moving from text to control, moving on, turned off, and off with focus), the window lost and back, the
    ceiling (held deletion, late offer, the floor capped by the ceiling), the clock set back an hour and set forward, a
    timeout changed while shown, the saved state (rotation paused and running, process death, a restore after the time
    ran out, a restore after the ceiling, an elapsed clock below the stamp, save leaving the live state alone) and the
    line (stops, never grows, meets the ceiling).
  - Negative control, with `RESUME_FLOOR_MILLIS = 0L`: exit 1, 4 of 27 tests fail:
    - `P2 focus leaving with 0_5 s left gives 3 s`: `expected:<3000> but was:<500>`;
    - P5: `expected:<3000> but was:<1000>`;
    - the paused rotation: `expected:<2800> but was:<1800>`;
    - the line under the floor: `expected:<0.05> but was:<0.0>`.
  - After the revert: `:designsystem:testDebugUnitTest :designsystem:ktlintCheck :designsystem:detekt` exits 0, with
    216 tests and 0 failures.
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
