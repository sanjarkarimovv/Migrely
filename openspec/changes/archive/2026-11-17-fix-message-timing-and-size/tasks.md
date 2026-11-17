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

- [x] 3.1 TalkBack focus on a message's text and control, on API 36 (`emulator-5570`) and on API 26, on a device
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

  **Decided 2026-11-17 (team-lead, after cr-t64 S3 #2):** the API 26 half stays unrun. The TalkBack pause is built for
  every level and verified on API 36 only; this is a known limit in D182 and EC-UX-54. The keyboard pause is verified on
  both levels (7.1). The production path is task 7.2.

  **Status 2026-11-16, 23:44 (android-engineer):** the API 36 automated rows pass. The API 26 half could not be run.
  Real TalkBack gestures and TalkBack's menu are not checked. Result and method are below the preparation notes.
  - First record, 2026-11-16 22:20: nothing had run on a device yet.
  - **Accepted by team-lead, 2026-11-16 (relayed by main):** the spike passes for Compose's focus path on API 36, with
    3 runs, each 10 of 10 on all three rows, and the longest moment "outside" 21 ms against the 300 ms grace. 4.4
    starts with the TalkBack pause.
  - The TalkBack menu question stays open. It becomes a row in QA's 8.3: a person on a real phone, with TalkBack
    gestures.
  - If the menu resumes the countdown, product-manager adds a known-limit line to D182 and EC-UX-54 then. No PM edit
    now.
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
- [x] 4.4 Add `rememberMessageCountdown(...)`.
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

  **Done 2026-11-17 (android-engineer).** Three new files in `designsystem/.../snackbar/`:
  - `MessageCountdownState.kt`, holding `rememberMessageCountdown(message, timeoutMillis, undoActionWallMillis,
    onExpired)` and `MessageCountdownState.rememberTimeLeft()`:
    - the countdown saves itself with `rememberSaveable` keyed by the message, so a host keeps only its message;
    - `isOver` is true when a restored message's time or ceiling has passed, so a host shows nothing, not even one
      frame;
    - the driver sleeps until `MessageCountdown.nextCheckInMillis()` (new, with 2 tests) and looks again on every
      `LifecycleResumeEffect`, on any change to the hold, and on a timeout change;
    - the hold is read from keyboard focus, TalkBack focus, `isTouchExplorationEnabled` and
      `LocalWindowInfo.isWindowFocused`;
    - the line is an `Animatable` restarted from the countdown's fraction at each change, so it is finite and needs no
      per-frame tick.
  - `ScreenReaderFocus.kt`, the spike's probe made production:
    - one `View.AccessibilityDelegate` per ComposeView, shared by every message host on it;
    - it is never installed over a delegate already there (readable from API 29);
    - TalkBack's focused node counts as the message's when its screen bounds lie inside the message.
  - `MigrelySnackbarHost(countdown = ...)` feeds the countdown with keyboard focus (from `MessageFocus`) and TalkBack
    focus (the probe, matched against the message's `positionOnScreen` bounds).

  Results:
  - `:designsystem:testDebugUnitTest :designsystem:ktlintCheck :designsystem:detekt` exits 0, with 222 tests and 0
    failures (the 4.6 files set aside for this slice).
  - The 4 tests of `MessageCountdownHostTest`:
    - keyboard focus holds the message, which then leaves with its time left;
    - the window losing focus resumes at once, with the floor and no grace;
    - after a 0.5 s resume the line goes on from 0.1, with no jump;
    - the countdown adds nothing to the semantics tree.
  - Negative control on the semantics test: with the host adding `contentDescription = "probe"` while it has a
    countdown, the test fails (the tree differs); after the revert, 4 of 4 pass.
    - That test first passed even with the mutation, because the switch to the countdown never reached composition.
      It now asserts that the host really recomposed with the countdown.
  - Open for the device walk (7.1), which no JVM test proves: whether a touch ends keyboard mode (and so focus).
- [x] 4.5 `messageTimeoutMillis` is the only timeout. Add a Konsist rule, next to `app/src/test/.../MessageCallSitesTest.kt`,
  that fails on `calculateRecommendedTimeoutMillis` outside `MessageTimeout.kt` (design D1).
  Verify: `./gradlew :app:testDebugUnitTest --tests '*MessageCallSites*'` fails on 40536f49's four hosts, which is the
  negative control, and passes after task 5.1.

  **Rule added 2026-11-17 (android-engineer).** It is `MessageCallSitesTest > Only messageTimeoutMillis asks Android for
  the accessibility timeout`: every production file whose text has `calculateRecommendedTimeoutMillis` must be
  `MessageTimeout` alone.
  - Negative control, on the hosts as they are before 5.1 (unchanged since 40536f49): the rule fails with
    `expected:<[MessageTimeout]> but was:<[MessageTimeout, AttackMessages, HistoryMessages, HomeMessages,
    MedicationMessages]>`. Those are exactly the four hosts of design D1.
  - The other 4 tests of `MessageCallSitesTest` pass.
  - The task is ticked after 5.1 turns the rule green.
  - **Green after 5.1:** `:app:testDebugUnitTest --tests '*MessageCallSites*'` gives 5 tests, 0 failures.
- [x] 4.6 Add `LocalInDarkRoom` in `MigrelyDarkRoom`. In a dark room `MigrelySnackbar` uses a 64 dp minimum height, a
  64 x 64 dp minimum for Undo, and a 64 x 64 dp Dismiss with the same 20 dp icon (design D4).
  The size changes in the same frame as the room's switch, with no size animation of its own, and the control keeps its
  node so focus and the pause stay.
  Verify:
  - `./gradlew :designsystem:testDebugUnitTest --tests '*Snackbar*'` passes, with bounds of at least 64 dp inside an active
    room and 48 dp outside, and a test that switches the room under a focused control and finds the new size in the next
    frame with focus kept;
  - with the local forced false, the test fails.

  **Done 2026-11-17 (android-engineer).**
  - `LocalInDarkRoom` lives in `theme/DarkRoom.kt` and equals `active || LocalInDarkRoom.current`.
  - In a dark room `MigrelySnackbar` uses a 64 dp minimum height, `sizeIn(64, 64)` for Undo, and `size(64.dp)` for
    Dismiss.
  - The local is read where the message is drawn, with no size animation, so the control keeps its node and its focus.
  - **Difference from design D4, for the review:** Dismiss's icon is 20 dp (`ICON_SIZE`), not 24 dp. UX B3 says "the
    same icon", so it stays at 20 dp.
    - **Resolved 2026-11-17 (PM, cr-t64 S3 #1):** the plan was wrong. The spec, design D4 and D182 (4) now say the same
      20 dp icon.

  Results:
  - `:designsystem:testDebugUnitTest :designsystem:ktlintCheck :designsystem:detekt` exits 0, with 227 tests and 0
    failures.
  - `SnackbarDarkRoomSizeTest`, 5 of 5:
    - in an active room, Undo is at least 64 x 64 dp and Dismiss is 64 x 64 dp;
    - outside the room, Undo keeps its 48 dp minimum height (measured 51 dp) and does not grow;
    - outside the room, Dismiss keeps its 48 dp touch target and does not grow. Its layout bounds are Material's
      40 dp button, and the 48 dp target lies outside them, so this reads the touch bounds;
    - switching the room under a focused Undo gives 64 dp in the next frame, with focus kept.
  - Negative control, with `MigrelyDarkRoom` providing `LocalInDarkRoom provides false`: 3 of 5 fail (the two in-room
    sizes and the switch), and pass again after the revert.

## 5. Hosts (each `ui` module)

- [x] 5.1 Home, History, Attack details and Add past migraine, and the medication screens:
  - drop the local `timeoutMillis`;
  - use `messageTimeoutMillis` and `rememberMessageCountdown`;
  - deletion messages pass `deletedAtEpochMillis` as the action time.

  Verify:
  - `./gradlew :features:home:ui:testDebugUnitTest :features:history:ui:testDebugUnitTest :features:attack:ui:testDebugUnitTest :features:medication:ui:testDebugUnitTest`
    passes, including 4.1's tests, now green;
  - a per-host test shows "Saved." or an information message leaving at 20 s with an interactive 20 s and a
    non-interactive 8 s timeout.

  **Done 2026-11-17 (android-engineer).** The four hosts now use `messageTimeoutMillis` and `rememberMessageCountdown`:
  `HomeMessages`, `HistoryMessages`, `AttackMessages` (Attack details, Add past migraine) and `MedicationMessages`
  (My medications, Edit medication, Log medication).
  - Their own `timeoutMillis`, the `LaunchedEffect(delay)` and the `rememberTimeLeft` helpers are deleted. So are the
    Shown models' `remainingMillis` and the three unit-test files that covered only it.
  - Time comes from `LocalMessageClock`: `isFreshDeletion`, History's `enteredAt`, and every `shownAtEpochMillis`
    (Attack details and Add past migraine too).
  - Ceiling start for each Undo:
    - deletion messages pass `deletedAtEpochMillis`;
    - every other Undo passes the moment it showed. For Attack details that is the moment it could be seen, after the
      safety card.
  - Each host passes `isOver` to hide a restored message whose time has passed, and gives the countdown to
    `MigrelySnackbarHost`.

  Results:
  - Command: `:features:{home,history,attack,medication}:ui:testDebugUnitTest`, with ktlint and detekt on the same
    modules and `:app:testDebugUnitTest --tests '*MessageCallSites*'`. Exit 0:

    | Module | Tests | Failures |
    |---|---|---|
    | Home | 118 | 0 |
    | History | 57 | 0 |
    | Attack | 119 | 0 |
    | Medication | 91 | 0 |
    | `MessageCallSites` | 5 | 0 |

  - The 4.1 tests now pass: both 122 s tests, both 114 s controls, and IntakeUndone.
  - New split tests, each passing: "Saved." stays for the time to take action (20 s) and not the reading timeout (8 s)
    on Home, History and Attack details. IntakeUndone on Log medication is from 4.1.
  - Tests that time a message by the Compose clock now give the host a `ComposeMessageClock` that follows it, because
    Robolectric's own clocks stand still: `SaveMessageTest`, and `HomeMessagesTest`'s default.
    - The clock must live as long as the test. One created in composition came back after navigation reading below the
      saved stamp, and the countdown dropped the message as after a restart. That is the reboot rule doing its job.
- [x] 5.2 Reminders, Doctor report, Your data, Settings, Onboarding and Paywall use `rememberMessageCountdown`. Their
  timeout is already interactive.
  Verify: `./gradlew :features:reminders:ui:testDebugUnitTest :features:report:ui:testDebugUnitTest :features:backup:ui:testDebugUnitTest :features:settings:ui:testDebugUnitTest :features:onboarding:ui:testDebugUnitTest :features:subscription:ui:testDebugUnitTest`
  passes.

  **Done 2026-11-17 (android-engineer).** Six screens now use `rememberMessageCountdown` and give the countdown to
  `MigrelySnackbarHost`, with `isOver` hiding a message restored past its time: Reminders, Doctor report, Your data,
  Settings, Onboarding and Paywall.
  - Reminders' "Visit removed" Undo counts its ceiling from when it showed. The other five screens have no Undo.
  - Their `LaunchedEffect(delay)` and Reminders' `rememberTimeLeft` are deleted, and so is `remainingMillis` in the
    Shown models of Reminders, Doctor report and Your data. No test used it.
  - Reminders, Doctor report and Your data stamp `shownAtEpochMillis` from `LocalMessageClock`.

  Result: the verify command, with ktlint and detekt on the same six modules, exits 0.

  | Module | Tests | Failures |
  |---|---|---|
  | Reminders | 27 | 0 |
  | Doctor report | 8 | 0 |
  | Your data | 16 | 0 |
  | Settings | 31 | 0 |
  | Onboarding | 5 | 0 |
  | Subscription | 42 | 0 |
- [x] 5.3 Saved state per host covers `runMillis` (pause ended and floor applied at the save), `savedElapsedMillis` and the
  stamps. Rotation and process-death restore tests cover the scenarios "Rotate while paused", "Process death while
  paused", "Restored after the time ran out" and "Restored after the ceiling".
  Verify: `./gradlew :features:home:ui:testDebugUnitTest --tests '*Restore*' :features:history:ui:testDebugUnitTest --tests '*Restore*'`
  passes.

  **Done 2026-11-17 (android-engineer).**
  - The saved state lives in `rememberMessageCountdown`'s own `rememberSaveable`, keyed by the message the host already
    saves, so every host saves it the same way: `runMillis` (pause ended and floor applied at the save),
    `savedElapsedMillis`, `shownElapsedMillis`, `actionWallMillis` and the line's fraction.
  - The tests are `HomeMessagesRestoreTest` and `HistoryMessagesRestoreTest`, each with four, using
    `StateRestorationTester` and a fake clock:
    - rotate while paused: about 3 s back, Undo not focused, gone at 3 s;
    - process death while paused, restored 2 s later: 2 s left;
    - restored after the time ran out: nothing, even though it was paused;
    - restored past the ceiling: no Undo.
  - The tester saves and restores at once, so the time a restore takes is added inside the restored composition, before
    the message is restored. Its dispose and re-emit need frames, so the Compose clock runs for that step alone; the
    fake message clock times the countdown.

  Results:
  - The verify command exits 0: Home `*Restore*` 7 tests, History `*Restore*` 5 tests, 0 failures (the 8 above, plus
    restore tests that already existed).
  - ktlint and detekt on both modules exit 0.
  - Negative control, with the countdown's `Saver` saving nothing: 3 of 4 fail on each screen (the paused rotation,
    process death and "ran out"). They pass again after the revert.
    - "Restored after the ceiling" still passes under that mutation, as it should: a fresh countdown still counts its
      wall bound from `deletedAt`, so the Undo stays gone.

## 6. Negative control on the fix

- [x] 6.1 With the resume re-evaluation removed from `rememberMessageCountdown` as a temporary edit, 4.1's 122 s tests fail
  again on Home and History. Restore the edit.
  Verify: paste the failing and then the passing test names, and confirm `git diff` is clean afterwards.

  **Done 2026-11-17 (android-engineer).** The temporary edit removed `LifecycleResumeEffect(state) { state.wake() ... }`
  from `rememberMessageCountdown`. The command was `:features:home:ui:testDebugUnitTest --tests '*HomeMessagesTest*'
  :features:history:ui:testDebugUnitTest --tests '*HistoryMessagesTest*'`.
  - **Without it, exit 1.** Exactly 1 of 16 tests fails on Home and 1 of 10 on History:
    - `HomeMessagesTest > Lock and unlock at 122 seconds on Home leaves no Undo`;
    - `HistoryMessagesTest > Lock and unlock at 122 seconds on History leaves no Undo`;
    - both with `assertDoesNotExist ... found '1' node`: Undo was still on screen.
    - The 114 s controls passed.
  - **Edit restored:** `git diff` prints 0 lines.
  - **Same command, exit 0:** 16 of 16 and 10 of 10 pass. Passing now: `Lock and unlock at 122 seconds on Home leaves no
    Undo`, `... on History leaves no Undo`, and `Undo before the ceiling still restores on Home` and `... on History`.

## 7. Device walk (android-engineer, own emulator only)

- [x] 7.1 Walk ux §B2's device list and §B3's bounds on API 36 (`emulator-5570`) and API 26 (as `team-lead` assigns):
  keyboard on both levels and TalkBack on API 36 only (API 26 is a known limit, D182), Home attack mode portrait 200%,
  History, Reminders, landscape attack mode, dark-room Log medication.
  - Run the ceiling path of design A4 on the branch.
  - Check that a touch ending keyboard mode counts as focus leaving (scenario "A touch ends keyboard mode").
  - Check the 48 to 64 dp change when "Migraine ended" replaces a message on Home, and the reverse after "Undo": same frame,
    no jump.
  - Restore font scale, rotation, the a11y timeout and the locale.

  Verify: one line per check in this file with the result, and `adb shell settings get secure accessibility_interactive_ui_timeout_ms`
  printing the original value at the end.

  **Walk 2026-11-17 (android-engineer), on `emulator-5570` only, one emulator at a time.**
  - Build: the T64 debug APK from 7c85a7d0, sha256 `fca7f23d…dd15f257`, identical on the device and locally.
    - API 36 had 1.3.0 (versionCode 12), so it was installed with `adb install -r -d`.
    - API 26 was a fresh install, through onboarding.
  - Evidence is in `migrely-work/t64/`: `walk-*.png`, `walk-switch*.mp4`, `frames-switch/`, and `walk{36,26}-*-settings.txt`.
  - API 36 is `Migrely_API_36`, density 2.625. API 26 is `Migrely_API_26` on `-port 5570`, after API 36 was off.
  - API 26 has no "Time to take action" setting (it came with API 29), so an Undo lasts its base 5 s there.
  - Rows run across a network drop at 06:3x were voided and rerun; only the reruns count.

  Sizes (ux §B3; 168 px = 64 dp, 126 px = 48 dp):
  - API 36, Home attack mode, portrait 100%: Undo 168x168 px; Dismiss ("Undone.") 168x168 px; message 168 px tall.
    **Pass.**
  - API 36, Home attack mode, portrait 200%: Undo 230x168 px. **Pass.** Screenshot `walk-03-attack-200.png`: the message
    is whole, the rain card's text and Details are uncovered, and "Migraine ended" is untouched.
  - API 36, Home attack mode, landscape 200%: Undo 230x168 px. **Pass.**
  - API 36, Home attack mode, landscape 100%: Undo 168x168 px; the guidance column sits below the message. **Pass.**
  - API 36, dark-room Log medication (from attack mode), "Removed. Nothing was logged.": Dismiss 168x168 px; message 168 px
    tall. **Pass.** Storage full could not be caused; this message has the same Dismiss.
  - API 36, outside the room, "Migraine ended" on Home Today and "Migraine deleted" on History: Undo 152x126 px (48 dp
    tall). **Pass.**
  - API 26, Home attack mode at 200%, portrait and landscape: Undo 230x168 px. **Pass.**
  - API 26, outside the room at 200%, Home Today and History: Undo 230x126 px. **Pass.**

  Room switching under a message (API 36, recorded):
  - **48 to 64 dp and back: no size animation and no later jump within the recording's frame spacing.**
    - "Migraine ended" (48 dp) replacing "Migraine started" (64 dp): the first frame with the new message already has the
      light-room size, while the room still cross-fades.
    - After Undo: the old message fades out, then "Undone." arrives at 64 dp. That clear-then-arrive order is D178's.
    - The recording is 5–25 fps (30–300 ms between frames), so "the same frame" is not proven.
    - The message also moves down to Today's place; that is Home's existing offset per mode, not part of this change.

  Keyboard rows, ux §B2:
  - API 36, P1, Home attack mode at 200%, "Time to take action" 20 s: Undo focused about 1 s in, held to 36 s and still
    shown; it left 19–21 s after focus left, its 19 s left. **Pass.**
  - API 36, P3, History, 120 s setting: deletion Undo focused at 16 s and held. It was there at 112 s and gone by 116 s
    after the deletion. Then nothing had focus, the app was still running, and the crash log was empty. **Pass.**
  - API 36, "A touch ends keyboard mode", Home Today, 20 s setting: Undo focused, then a touch on the date text. Focus
    cleared at once, and the message left 16–19 s later with the time it had left. Had focus stayed, it would have held
    to its ceiling. **Pass.**
  - API 36, P5, Home attack mode, 20 s setting: Undo focused and held to 28 s. The shade was opened for 28 s and the
    message ran out under it. **Pass.**
  - API 26, P1, Home attack mode at 200%: Undo focused at once, held to 16 s against its 5 s, gone about 4 s after focus
    left. **Pass.**
  - API 26, P2, the floor, Home Today at 200%: Undo focused with about 1.4 s left. After focus left it was still shown
    at +2.3 s and gone by +3.5 s, which is the 3 s floor plus the grace, not 1.4 s. **Pass.**
  - API 26, History at 200%: deletion Undo focused after the top bar (4 Tabs), held to 14.9 s against its 5 s, gone
    3.2–4.9 s after focus left. **Pass.**
  - API 26, P5, Home Today: Undo focused, held to 10.4 s, then the shade for 13 s (`dumpsys window`: the StatusBar takes
    focus, and Migrely gets it back when the shade closes). The message was gone. **Pass.**
  - Tab order, both levels: the message's control comes right after the top bar (History: Back, Insights, Doctor
    report, Undo), and first in attack mode. **Pass.**

  Ceiling, design A4 (API 36, 120 s setting): delete on Details, Undo offered at about 2 s, lock at 20–23 s, unlock at
  125 s.
  - Home: no Undo, no "This can no longer be undone.", and a tap where Undo was did nothing. **Pass.**
  - History: the same. **Pass.**
  - Deleting on Details closes it at once, so the offer came at about 2 s, not about 20 s; the ceiling counts from the
    deletion either way.
  - The negative control on `develop` 40536f49 is QA's (8.3 A4).

  Not checked, and why:
  - **TalkBack rows in the real app (API 36):** adb cannot drive TalkBack. Injected touches and keys never reach it, and
    raw `sendevent` is denied, as in 3.1. The production TalkBack path passed in 7.2.
    - The open questions go to QA 8.3 with a person: TalkBack's menu, and whether TalkBack focuses a new message on its
      own in the real screens (7.2's finding).
  - **Reminders:**
    - "Saved." there comes only after an error, which cannot be caused on purpose.
    - "Visit removed" needs a visit date first, which raises Android's notification-permission dialog and would change
      a device permission.
    - Its host tests pass (27 of 27), and QA's A2 lists it.
  - **P6** (a new message replacing a held one) was not walked; the unit tests cover it.
  - **Feel** (how 3 s and 300 ms feel, whether 64 dp is heavy) needs ux-designer on a device (ux §D).

  Settings, restored and compared with the values recorded before the walk:
  - API 36: identical, including `accessibility_interactive_ui_timeout_ms` = null at the end, TalkBack off, font scale
    1.0, auto-rotate on.
  - API 26: identical except `font_scale`. It was unset before and reads 1.0 after `settings delete`, which is the same
    effective scale.
  - Both emulators are off. `adb devices` shows only the owner's phone, which was not touched.

  **Result, 2026-11-17 (android-engineer):** done. QA ran in 8.3 the rows this walk did not run: A1 and A2 cover
  Reminders "Saved." (API 36 and 26) and "Visit removed" (API 26), and P6 (2 rows on API 36, 1 on API 26); A3 drives
  TalkBack in the real app through console gestures. Record: `docs/qa/fix-message-timing-and-size.md`.

- [x] 7.2 The production TalkBack path on API 36 (`emulator-5570`) with TalkBack on: an instrumented test on the real
  `MigrelySnackbarHost` with `ScreenReaderFocus`, not the spike's bare Box (design D3, cr-t64 S3 #2).
  - It moves TalkBack focus onto a message's text, then to its control within 300 ms, then off to the content.
  - It checks: paused on the text, no resume during the move, resumed 300 ms after leaving with at least 3 s left.
  - It skips itself, with a message, when TalkBack is off, so a run without TalkBack proves nothing and says so.
  - Restore `enabled_accessibility_services` and `accessibility_enabled` afterwards.

  Verify: `ANDROID_SERIAL=emulator-5570 ./gradlew :designsystem:connectedDebugAndroidTest
  -Pandroid.testInstrumentationRunnerArguments.class=<the new test>` passes with TalkBack on, with the run count written
  here; negative control: with `ScreenReaderFocus` not fed to the countdown as a temporary edit, the test fails; then it
  passes after the revert.

  **Test written 2026-11-17 (android-engineer); not run yet, it waits for `emulator-5570`.** The test is
  `designsystem/src/androidTest/.../snackbar/MessageTalkBackPauseTest.kt`.
  - It uses the real `MigrelySnackbarHost`, with `ScreenReaderFocusEffect` feeding `rememberMessageCountdown` (no fake
    clock).
  - 10 rounds, each with a fresh 4 s Undo message:
    - wait until 1.5 s are left;
    - TalkBack focus on the text, which must be paused;
    - then on "Undo the start", which must stay paused at every 10 ms poll for 600 ms;
    - then on the content, which must be paused right after, then resumed, with at least 3 s left (less 400 ms of
      slack for the poll and the frame).
  - Each round logs under the tag `T64Pause`, including how long after leaving the "still paused" check ran.
  - It skips itself with a message when TalkBack is off.
  - The node finder is shared with the spike in `AccessibilityTree.kt`.
  - `:designsystem:compileDebugAndroidTestKotlin :designsystem:ktlintCheck :designsystem:detekt` exits 0.
  - Command for the device run:
    `ANDROID_SERIAL=emulator-5570 ./gradlew :designsystem:connectedDebugAndroidTest
    -Pandroid.testInstrumentationRunnerArguments.class=com.migrely.app.designsystem.snackbar.MessageTalkBackPauseTest`.
  - Negative control for the device run: drop the `ScreenReaderFocusEffect(...)` call in `MigrelySnackbarHost`.

  **Run 2026-11-17 on `emulator-5570` (`Migrely_API_36`, TalkBack 16 bound, touch exploration on). Pass.**
  - Command: `ANDROID_SERIAL=emulator-5570 nice -n 19 ./gradlew --max-workers=2 :designsystem:connectedDebugAndroidTest
    -Pandroid.testInstrumentationRunnerArguments.class=com.migrely.app.designsystem.snackbar.MessageTalkBackPauseTest`.
  - Logs are in `migrely-work/t64/pause-*.log`.
  - **Run A: exit 0, 10 of 10 rounds. Run B, after the negative control's revert: exit 0, 10 of 10.**
    - Every round: paused on the text, paused at every poll through the move to "Undo the start", still paused when
      checked 22–33 ms after leaving, then resumed.
    - Time left after the resume, run B: 2990–3684 ms. The 2.99 s rounds are the floor; on the others see the finding
      below.
  - **Negative control:** `ScreenReaderFocusEffect(...)` removed from `MigrelySnackbarHost`. Exit 1, 0 of 10: no round
    paused on the text, and each message ran out. After the revert `git diff` was empty.
  - **First run: 5 of 10, from the harness.**
    - UiAutomation's node cache went stale between rounds and did not see the new message's text in 5 rounds.
    - A debug run showed `findByLabel` returning nothing in exactly those rounds.
    - Now the cache is cleared (`clearCache()`, API 34 and up) and the node refreshed before each move. With that, 10
      of 10 in the debug run and in runs A and B.
  - **Finding for product-manager and ux-designer: TalkBack moves its own focus onto a new message.**
    - In 5 of 10 rounds of each run, a fresh node shows TalkBack's focus already on the new message's text before the
      test moves it.
    - The countdown then pauses from about the moment the message appears; those rounds end with 3.6–3.7 s left, not
      the 3 s floor.
    - For an Undo the ceiling still ends it. A "Saved." or information message that TalkBack focuses on its own stays
      until the person moves on or closes it. That fits the delta's letter ("pauses while TalkBack focus holds it"),
      but nobody chose to hold it.
    - This harness puts the message below a button. Whether the real screens do the same is checked in 7.1's TalkBack
      rows.
    - **Decided 2026-11-17, 09:55 (team-lead): accepted as intended.** TalkBack's own focus counts as focus. Recorded by
      product-manager in D182 (2), EC-UX-60, the spec scenario "TalkBack focuses a new message by itself", design D3,
      and the 8.3 row below.

## 8. Gate, review, docs

- [x] 8.1 Gate. Verify: `./gradlew assembleDebug test ktlintCheck detekt lint` exits 0.

  **Done 2026-11-17 (android-engineer)** on 0be03fda: branch 2df55c93 merged with `origin/develop` c49b9e09, no
  conflicts.
  - Command: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 --continue assembleDebug test ktlintCheck detekt lint
    :app:checkReleasePermissions :app:checkReleaseDebugComponents`, started at load 1.19.
  - Result: exit 0, `BUILD SUCCESSFUL in 3m 14s`, 2144 tasks (1398 executed).
  - JUnit reports: 1370 JVM-module tests, 1265 debug unit tests and 726 release unit tests, all with 0 failures and 0
    skipped.
  - `:app:checkReleasePermissions` and `:app:checkReleaseDebugComponents` pass. The merged release manifest holds 6
    `uses-permission` and no INTERNET: the word appears only in two comments.
  - `openspec validate --all --strict`: 34 passed, 0 failed, exit 0.
- [x] 8.2 `code-reviewer`, then fixes.
  Verify: no open blocker in the review notes.

  **cr-t64 on bc8d0d9b: approved with findings, no S1 or S2.** product-manager folded the spec findings into 24a29a0d.
  android-engineer, 2026-11-17:
  - **S4 #3, fixed in c6a3de7a.** `MessageTimeout.kt`'s KDoc no longer says "without Undo". Comment paragraphs this
    branch wrote past 120 characters are rewrapped (comments only).
  - **S4 #5, fixed red to green.** During the exit fade, the leaving message's line jumped to 0: its content read the
    new, empty countdown, and `rememberTimeLeft` started a fresh `Animatable` at 0.
    - Red, before the fix: `MessageCountdownHostTest > A leaving message keeps its own line through the fade`, with
      `expected:<0.6> but was:<0.0>` one frame into the fade.
    - Fix: `MigrelySnackbarHost` keeps, per message drawn (`remember(shown)`), the countdown that message entered with,
      and provides it through `LocalShownCountdown`. Hosts ask for the line with `rememberMessageTimeLeft()`, and
      `MessageCountdownState.rememberTimeLeft` is now internal, so a host cannot read another message's line.
    - Green: `:designsystem:testDebugUnitTest` exits 0 with 228 tests, ktlint and detekt clean.
  - **Line-length check of every module the branch touched:** ktlint and detekt on `:designsystem`, `:app`,
    `:datasource:source`, `:datasource:implementation` and the ten feature `ui` modules all exit 0, as do the tests of
    the five hosts whose line call changed: Home 122, History 61, Attack 119, Medication 91, Reminders 27, all with 0
    failures.

  **Result, 2026-11-17 (android-engineer):** done, no open blocker. cr-t64 approved bc8d0d9b with findings, no S1 or S2.
  The S3 and S4 findings are fixed in 24a29a0d, c6a3de7a, ac573e81 and 507f0918. The delta review of 507f0918 raised
  S4 #1, fixed by product-manager in 8d1a0894. The only later code change is the instrumented test 7c85a7d0 (node-cache
  clearing), which QA reran in 8.3: 10/10, negative control 0/10.
- [x] 8.3 QA independent check, design "Acceptance" A1–A6 on the branch SHA, with `develop` 40536f49 as the negative
  control for A1 and A4.
  - A3 runs on API 36 with TalkBack on, through the real host (7.2's test rerun by QA). API 26 TalkBack is a known limit,
    not a failure.
  - The TalkBack menu question stays a manual row: a person on a real phone with TalkBack opens its menu on a message
    and notes whether the countdown resumes.
  - Manual row, EC-UX-60: a person on a real phone with TalkBack, on Home, History and dark-room Log medication, notes
    whether a new message takes TalkBack focus by itself, and whether it leaves 3 s or more after focus moves on (an
    Undo at the latest 115 s after its action).
  Verify: the QA report lists A1–A6 with counts.

  **Result, 2026-11-17 (qa-t64, recorded by android-engineer):** **GO with risks** on 58e62072, record
  `docs/qa/fix-message-timing-and-size.md`. A1–A6 pass with counts, and each new check fails or differs on the control
  `v1.3.0`. No defect filed.
  - Two rows stay **PENDING-PERSON**: the TalkBack menu and EC-UX-60, each on a real phone. team-lead decided they move
    to the Go/No-Go of the release that ships T64 (1.3.1). They do not block the merge into `develop`.
  - The emulator already answered both. The menu takes window focus and the countdown runs (3/3, by design D3).
    TalkBack focused a new Undo on Home by itself, and the Undo left at the 115 s ceiling.
- [x] 8.4 Archive the change, update the BRD §13 row status, PR into `develop`, self-merge with a merge commit.
  Verify: `openspec list` no longer shows `fix-message-timing-and-size`, and `openspec validate --all --strict` exits 0.

  **Result, 2026-11-17 (android-engineer):** the branch merged QA's record (`origin/qa/t64-message-timing` 1161a609) and
  `origin/develop` d82cdf05, with no conflicts and only `openspec/` and `docs/` files from either.
  - BRD §13 row 50 now holds its status and the QA record, and its release is 1.3.1. D182's text is unchanged.
  - The change is archived. The PR into `develop` is open; team-lead gates and merges it, so the self-merge step is the
    lead's.
