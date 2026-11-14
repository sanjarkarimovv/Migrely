# Tasks

Base: `bugfix/181-messages-everywhere` from `origin/develop` 52c12ce5. Every commit and the PR say `Refs #181`, `Refs #185`,
`Refs #183` and `Refs #88` (and `Refs #153` where the short-window work touches it). Never use a closing keyword, because QA filed
these issues and retests them. No domain, data or permission change.

**Work from the UX spec.** `migrely-work/t63/ux-spec.md` §2 is the engineer's rule list (rules 1–8). §3 holds the state tables QA
and the engineer cover: O1–O20 (order), F-a–F-h (focus), M1–M12 (motion and announcements), P1–P7 (placement), S1–S4 and R1–R5
("Saved." and the review). Each task below names the rules and rows it must satisfy. If a rule there and this file disagree, ask
`product-manager` before you code.

## 1. Plan (product-manager)

- [x] 1.1 BRD: D178, EC-UX-35…38, EC-REV-13, §13 row 46; EC-UX-16 and EC-UX-34 updated. Verify: `grep -c "^| D178 " docs/BRD.md`
  prints 1 and `git show origin/develop:docs/BRD.md | grep -c "^| D178 "` prints 0.
- [x] 1.2 Proposal, design, the `in-app-messages` and `in-app-review` deltas, tasks. Verify:
  `openspec validate fix-messages-everywhere --strict` exits 0.

## 2. UX (ux-designer, before any code)

- [x] 2.1 Answer design Q1–Q8 in a ux spec. `team-lead` stores it in `migrely-work/t63/ux-spec.md`. Answer at least:
  - the Tab and TalkBack order: Back and the bar's actions, then the message control, then the content; on Home and onboarding,
    the message first (Q1, Q2);
  - where focus goes when the focused message leaves (Q3);
  - "Saved." per screen: Home in both modes, History and Reminders, plus Doctor report's silent clear (Q7);
  - the Home review request while "Couldn't save" or "Saved." shows (Q8);
  - the arrival fade's look and replacement in place (Q4, Q5);
  - the short-window Home Today placement (Q6).

  Verify: the spec file exists and answers each Q by number. Any answer that departs from a PM default comes back to
  `product-manager`, who updates D178 and the deltas before code starts. Done: `migrely-work/t63/ux-spec.md` (276 lines), with
  Q1–Q8 in §1 and findings F1–F9 in §6. Q5, Q6 and Q8 depart from the PM defaults.
- [x] 2.2 PM folds ux's answers into the design and deltas where they differ. Verify:
  `openspec validate fix-messages-everywhere --strict` exits 0 after the update. Done: `team-lead` accepted F1–F8, including F6
  (the measured offset in portrait too). They are in D178 (1), (3)–(6), (10), EC-UX-16, 35–38, EC-REV-13, design D1, D3–D6, Q1–Q8,
  A11, A12, A15, A17, A18, A20 and N8, and in both deltas. F9 is listed under out of scope in the proposal only.

## 3. Presentation (android-engineer)

- [x] 3.1 `HomeViewModel`, `HistoryViewModel` and `RemindersViewModel` emit one "write saved" event on each success in the design's
  table (middle column), including Reminders' visit date `Saved` with `askForPermission = true` (F4), and none on a result that
  writes nothing. Write the tests first, one per write path, plus one for each
  non-write result (`NotInProgress`, `Unchanged`, `NothingToRemove`). Verify:
  `./gradlew :features:home:presentation:test :features:history:presentation:test :features:reminders:presentation:test`
  passes, and the new tests fail first on 52c12ce5.

  Done. The event is a `Saved` notification in each module, as on Attack details (D165 (4)). Home sends it after the new
  state (`loadToday(thenTell = Saved)`, D158 order); History and Reminders send it before the reload, as their own
  messages do. 26 new tests: Home 10 (5 write paths, 5 non-write: `NotInProgress` x4, `AlreadyInProgress`), History 4
  (2 write paths, own message, failure), Reminders 11 (5 reminder writes, failure, visit `Saved`, visit `Saved` with a
  permission ask (order: `Saved`, then the ask), `Unchanged`, `NothingToRemove`, remove and undo keep their own message).
  - Red, on 52c12ce5's view models with only the `Saved` objects added: 14 failed, all the write paths (Home 5/64, History
    2/60, Reminders 7/37); the non-write tests passed, as they must. Log `migrely-work/walks/msgs/3.1-red.log`.
  - Green: Home 64/64, History 60/60, Reminders 37/37, exit 0. One old test changed on purpose: "Given notifications off
    when a visit is saved then permission is asked for it" read the first notification; the ask is now the last one, after
    `Saved` (ux F4).

## 4. UI models, mappers and strings

- [x] 4.1 `Saved` in the home, history and reminders message models: confirmation, polite, `READING_MILLIS`, no action, no
  haptic. The string is "Saved." in each module's `strings.xml`. Verify:
  `./gradlew :features:home:ui:testDebugUnitTest :features:history:ui:testDebugUnitTest :features:reminders:ui:testDebugUnitTest --tests '*MessageBehaviour*'`
  passes, with a case for `Saved` in each.

  Done. `Saved` is confirmation, `READING_MILLIS`, no undo; the screen gives haptics only to errors and start/end, so none.
  Strings `home_message_saved`, `history_message_saved`, `reminders_message_saved` = "Saved.". The screens show it only in
  place of an error (6.2's rule, in the message collector of each). History had no `*MessageBehaviour*` test class, so
  `HistoryMessageBehaviourUiMapperTest` is new. Verify run: home:ui 64/64, history:ui 42/42, reminders:ui 23/23, exit 0;
  each module's `*MessageBehaviour*` result file has a `Saved` case. Not run red first: the task asks for coverage, not a
  red test.

## 5. designsystem

- [x] 5.1 Never transparent (design D4, ux rules 4 and 8). First write a red test: in no frame of an arrival, and of a replacement,
  is the message's node in the semantics tree while fully transparent. Its negative control at `initialAlpha = 0f` fails. Then make
  in-place replacement the host's behaviour, give every posted message its own identity, and start the arrival fade at
  `ENTER_START_ALPHA = 0.02f` (or use the fallback). Invert `ReplaceInPlaceTest`'s default-fade case and say so in the PR. Verify:
  `./gradlew :designsystem:testDebugUnitTest` passes, and the new test fails on 52c12ce5's host.

  Done. `MessageNeverTransparentTest` (4 cases: arrival, same-text retry, other-text replacement, replacement during the
  arrival) counts frames in which a node with the text is in the tree while `SemanticsNode.isTransparent` (Compose's
  own test, read by reflection, so a Compose rename fails loudly). The host now swaps every replacement in place (one
  content key) and fades in from `ENTER_START_ALPHA = 0.02f`. The `replaceInPlace` parameter is gone, and Attack details
  lost its argument. Each screen's shown message carries a posting time (6.x), so a same-text retry is a new node.
  - Red on 52c12ce5's host: 4/4 failed (2, 3, 2 and 1 transparent frames). Log `migrely-work/walks/msgs/5-red.log`.
  - Negative control, local, `ENTER_START_ALPHA = 0f`: the arrival case fails again (the in-place cases do not fade, so
    they stay green). Log `walks/msgs/5-negative-controls.log`. Restored to 0.02 before the green run.
  - Green: `:designsystem:testDebugUnitTest` 185/185, exit 0.
  - `ReplaceInPlaceTest` "By default the new message fades in over the old one" is inverted to "The new message takes the
    old one's place at once, with no cross-fade": the spec changed (D178 (5)); the test was not bent. For the PR text.
- [x] 5.2 The order mechanism (design D3, ux rules 1–3). First write red tests on a sample screen: Tab from the top goes bar,
  message control, content; Shift+Tab reverses; the semantics traversal order matches; a message's appearance does not move focus;
  focus clears on leave and on replacement and never moves into the content; a leaving message is out of the focus and semantics
  order from its first exit frame (F8). Verify:
  `./gradlew :designsystem:testDebugUnitTest` passes, and the tests fail with the message placed after the content, as on
  52c12ce5.

  Done. `MessageLayout(topBar, message, content)` in `designsystem/snackbar`: one `Layout` that places the slots in
  reading order (Tab follows placement order), draws the message above the content with `zIndex`, and marks the slots
  as traversal groups with `traversalIndex` 0, 1, 2 inside one traversal group. `MessageOrderTest` (11 cases): Tab from
  the top, a hardware Tab key, Shift+Tab, no top bar, TalkBack's order (traversal index path), appearance keeps focus,
  focus on leave, focus on replacement, Tab after a leave, and from the first exit frame no node in the merged tree and
  no Tab stop.
  - Red with a placeholder `MessageLayout` that kept 52c12ce5's `Box { Column { bar; content }; message }`: 9/11
    failed. "A message that appears does not move focus" and "not a Tab stop during the exit" passed there too.
  - Finding: on Android, clearing focus while a keyboard is in use gives it back to the first control at once (the
    tests saw focus land on Back, both on 52c12ce5 and with `clearFocus`). So "focus clears" is built as a parking
    target before the top bar: no semantics, no action, focusable only while it holds focus. Nothing looks focused,
    Enter and Space do nothing, Tab goes to the first stop and Shift+Tab to the last (ux Q3). The host moves focus there
    in the composition that drops or swaps the focused message, before the node goes.
  - A leaving message gets `clearAndSetSemantics {}` and a focus group whose `onEnter` cancels, from its first exit
    frame (F8). Touch is unchanged.
  - Green: 11/11 in the 185/185 run.
- [x] 5.3 Geometry is unchanged: the host's bounds and `onMessageBottomChanged` match the old placement within 1 px at 100% and 200%
  wherever the message fits. Verify: `MessagePlaceAndTapTest`, `MessageClearanceLayoutTest` and a new bounds test pass in
  `./gradlew :designsystem:testDebugUnitTest`.

  Done. `MessageLayoutBoundsTest` composes 52c12ce5's structure and `MessageLayout` in one window and compares the bar,
  the message, the content and the reported bottom within 0.5 dp (1 px at Robolectric's mdpi), at font scale 1 and 2:
  2/2 pass. `MessagePlaceAndTapTest` 18/18 and `MessageClearanceLayoutTest` 3/3 pass unchanged.
- [x] 5.4 Replacement size and guard (F1, F2, ux rules 4 and 5). With motion on, a shorter replacement eases the reported bottom
  over 250 ms (non-null to non-null only); a taller one moves it in the same frame; reduce motion is instant. The 0.8 s guard
  covers the taller of the old and new message. Verify: tests for both directions and for a tap at the old Dismiss strip pass in
  `./gradlew :designsystem:testDebugUnitTest`, and the guard test fails with the guard following only the new message (N8).

  Done. `MessageReplacementTest` (5 cases). The size animation of an in-place change is `tween(250)` when shorter and
  `snap()` when taller. The container's size lags a frame, so during a replacement the host reports
  `max(area, new message)` and a taller message pushes in the same frame. The guard covers the taller of the two.
  - Red on 52c12ce5's host: 3/5 failed (taller in the same frame, reduce motion at once, the tap at the old strip).
  - N8, local, guard following only the new message: "Soon after a shorter replacement, a tap where the taller one was
    reaches nothing" fails. Log `walks/msgs/5-negative-controls.log`. Restored before the green run.
  - Green: 5/5.
- [x] 5.5 Fit rule (F5, ux rule 6). The message is always whole inside the safe area. It rises only as far as needed, never above
  safe top + 8 dp, and does nothing where it fits. Verify: a host test with an over-tall message in a short window, and a no-op
  test at normal sizes, pass in `./gradlew :designsystem:testDebugUnitTest`.

  Done. `fitTopOffset` replaces `padding(top = topOffset)` in the host: it moves down by the offset, or only as far as
  still ends inside the bottom safe edge, never above the safe top + 8 dp. `MessageFitTest` (3 cases, 600x300 dp
  window): fits (no-op, top = 108 dp), too tall for its place (rises to end 8 dp above the bottom), taller than the
  screen (top at 8 dp). Red on 52c12ce5: 2/3 failed (the no-op case passed, as it must). Green 3/3.

## 6. UI (screens)

- [x] 6.1 All 13 screens use the order mechanism: Settings, Paywall, Doctor report, Your data, Reminders, History, My
  medications, Edit medication, Log medication, onboarding, Home, Attack details and Add past migraine. Verify: one Compose test
  per module for the Tab order passes in `./gradlew test`, and D165's Attack details tests stay green
  (`./gradlew :features:attack:ui:testDebugUnitTest`).

  Done. All 13 use `MessageLayout`: the nine bar screens and the paywall (its Close row is the top bar) through a
  script that moved the first child of their column into `topBar`; onboarding and Home with no top bar; Attack details
  and Add past migraine keep their top bar in their `*Contents` (a private `*TopBar`) and take a `messages` slot, so the
  bar is still drawn by the same composable. Tests: one Tab-order test per module, 10 modules (settings, subscription,
  report, backup, reminders, history, medication, onboarding, attack, home), each 1/1. Attack details: Back, Dismiss,
  "Got it" (#185). D165's tests: `:features:attack:ui` 93/93.
  - Negative control, local: `MessageLayout` placing the message after the content (52c12ce5's order): 9/9 module
    order tests fail. Log `walks/msgs/6-negative-order-placement.log`. Restored.
  - Not covered by a unit test: Edit medication, Log medication and Add past migraine (same module as one tested
    screen, same layout). The walk covers Attack details, History and Home.
- [x] 6.2 Home, History and Reminders replace an error with `Saved` on the event and ignore it otherwise. Doctor report clears
  an error on `Share`. Your data, My medications and Log medication show that their own message replaces the error, with no new
  code where none is needed. Verify: tests for every scenario of "An error on the other screens goes when a later save succeeds"
  pass in each module's `testDebugUnitTest`.

  Done. Home and History: "Saved." only in place of an error, in the message collector (`HomeMessagesTest` 5 cases,
  `HistoryMessagesTest` 3 cases: after an error, with no error, while Undo shows, two quick saves, failed retry).
  Reminders: the same rule in the screen's collector, its message now saveable and stamped
  (`ShownRemindersMessageUiModel`). Doctor report clears an error on `Share`. Your data, My medications and Log
  medication need no new code: a later message replaces the error (`MedicationMessagesTest` "Its own message after an
  error replaces it"). Edit medication and Add past migraine close on success.
  - Gap: Reminders' rule, Doctor report's `Share` clear and Your data are in their DI-bound screen functions and have no
    Compose test here. A3, A4 and A5 (QA) and code review cover them; 3.1 covers the events.
  - Found: the design's context said every screen keeps its message in `rememberSaveable`; Reminders, Doctor report and
    Your data used `remember` (and Reminders had no posting identity). The spec requires rotation and process death to
    keep the error, so they now use `rememberSaveable` with a stamped, parcelable shown model.
- [x] 6.3 `ScreenLeaveObserver` on the 9 screens clears an error, and only an error, on leave. Verify: tests for "belongs to the
  visit" pass, and the leave test fails with a lifecycle `ON_STOP` clear in its place.

  Done on Home, History, the three medication screens (shared `MedicationMessages`), Reminders, Doctor report, Your
  data and Add past migraine. Tests with a real `NavHost`: forward and back clears the error, the background
  (`ON_STOP` and back) keeps it, Undo survives a trip (`HomeMessagesTest`, `HistoryMessagesTest`,
  `MedicationMessagesTest`).
  - Negative control N4, local: Home's leave clear swapped for `LifecycleEventEffect(ON_STOP)`: "An error stays when
    the app goes to the background and comes back" fails (1/13). Log `walks/msgs/6.3-negative-N4.log`. Restored.
- [x] 6.4 Home gates `reviewPrompt()` on "no message shows" and on a `rememberSaveable` "error this visit" flag that the
  `ScreenLeaveObserver` clears (design D5, F3, ux R1–R5). Verify: Home UI tests show the slot leaving with any message and
  returning when it goes; staying absent for the rest of the visit after an error, across rotation; and returning after a
  navigation away and back, and `./gradlew :features:home:ui:testDebugUnitTest :features:review:ui:testDebugUnitTest`
  passes.

  Done. `ReviewGate` (`rememberReviewGate`): two `rememberSaveable` flags, "a message shows" from `HomeMessages`'
  `onShownChanged` and "error this visit" from `onErrorPosted`, cleared by `ScreenLeaveObserver`. Home composes the
  review slot only when both are open; the review module is unchanged. Tests: `HomeMessagesTest` 5 cases (no message:
  there; error: gone; error dismissed: gone for the visit; next visit: back; Undo: gone, back after it leaves) and
  `HomeReviewGateRestoreTest` 3 cases (error flag and message flag survive a restore). `:features:review:ui` unchanged
  and green in the gate.
- [x] 6.5 Home Today placement (design D6, F5, F6, ux rule 7, P1–P6). In a short window the message sits in the start column. In
  every window it sits at `max(MESSAGE_OFFSET_BELOW_TOP_BAR, settingsRowHeight + 2.dp)`, from the row's measured height, never its
  position. Verify: `HomeScreen` placement tests pass in `./gradlew :features:home:ui:testDebugUnitTest`, covering:
  - 411 and 274 dp at 100% and 200%, LTR and RTL;
  - portrait 360 dp at 200% with a wrapped date;
  - a scroll that does not move the message;
  - updated `messagesBesideControls` and `messageTopOffset` cases.

  Done. `messageTopOffset(mode, settingsRowHeight)` = `max(56 dp, row + 2 dp)` on Today and while loading, 56 dp until
  measured, 0 in attack mode; the row reports its height with `onSizeChanged`. `messagesBesideControls` is true for
  Today and attack mode in a short window. The host's fit rule (5.5) keeps the 274 dp storage-full error whole.
  `HomePlacementTest` (native graphics, so real text sizes) 10/10: 411 dp at 100% and 200%, 411 dp RTL, 274 dp at 100%
  and 200%, 274 dp storage full at 200% LTR and RTL (whole on screen, Dismiss on screen, clear of "Migraine started"),
  360 dp portrait at 200% with the date wrapped (the precondition checks the wrap), 360 dp at 200%, and a scroll that
  does not move the message. `MessageTopOffsetTest` 6 and `MessagesBesideControlsTest` 6 updated.
  - Negative control, local: 52c12ce5's rules (attack-only start column, fixed offset): 8/10 placement tests fail;
    the scroll case passes both ways, as it should.
  - `MessageCallSitesTest` (app, a text pin) now pins `messageTopOffset(uiState.mode, settingsRowHeight)`: the rule
    changed with D178 (6).

## 7. Docs

- [x] 7.1 `docs/ARCHITECTURE.md`, in the messages part: one line for the order mechanism, one for replacement in place and the
  never-transparent rule, and one for the "write saved" event on the other screens. Verify:
  `grep -n "write saved" docs/ARCHITECTURE.md` prints a line.

  Done. Three lines in §10's messages part: the order mechanism, replacement in place and never transparent, and the
  "write saved" event. `grep -n "write saved" docs/ARCHITECTURE.md` prints line 372.

## 8. Gate and walk (android-engineer)

- [x] 8.1 The gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`

  Done. `set -o pipefail; ANDROID_HOME=… nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`
  -> exit 0, BUILD SUCCESSFUL in 3m 20s, 2142 tasks (676 executed). Result files: 3156 test cases (debug and release
  unit tests together), 0 failures, 0 errors, 0 skipped. Load 7-8 at the start. The first run failed on one text pin,
  `MessageCallSitesTest` (Home's offset call changed by D178 (6)); the pin was updated, not the code. Log
  `walks/msgs/8.1-gate.log`.
  exits 0.
- [x] 8.2 No permission added. Verify: `./gradlew :app:checkReleasePermissions` exits 0.

  Done. `:app:checkReleasePermissions` -> exit 0. Merged release manifest: POST_NOTIFICATIONS, ACCESS_NETWORK_STATE,
  BILLING, WAKE_LOCK, RECEIVE_BOOT_COMPLETED and the dynamic-receiver permission; no INTERNET. No dependency was
  added to main code (only test dependencies in four UI modules).
- [ ] 8.3 A walk on `emulator-5570`, debug: design checks A1, A7, A8, A12 (Attack details, History, Home), A16 (one screen), A17
  (ux M3 and M4) and A18 (ux P1, P4 and P6) at 100% and 200%. Restore storage, font scale, animator scale, rotation and locale. Verify: the results and what the
  walk does not prove are in the packet.

## 9. Review (code-reviewer)

- [ ] 9.1 Review `git diff origin/develop...bugfix/181-messages-everywhere` against D178 and the deltas. Areas at risk:
  - geometry of the 13 hosts;
  - D165's card mode and hold;
  - the leave clear (navigation, not lifecycle);
  - the review slot's cancel path and its visit flag;
  - the replacement easing, the taller-message guard and the fit rule;
  - focus clearing on leave and on replacement;
  - the inverted `ReplaceInPlaceTest` case.

  Fixes go on the same branch. Verify: no open blocker in the review packet.

## 10. QA (qa-engineer, independent, this diff only)

- [ ] 10.1 Run design checks A1–A20 and negative controls N1–N8 on the branch head SHA, debug and R8 release. Cover the ux spec's
  §3 tables O1–O20, F-a–F-h, M1–M12, P1–P7, S1–S4 and R1–R5. Its font cells apply: 100% and 200% on API 34+, and 130% on API 26. Scope is what the diff
  reaches (owner rule 2026-11-09). The whole-app regression stays nightly on `develop`. Verify: the table is in `docs/qa/` with the
  SHA, the counts and what it does not prove. The retest results go on #181, #185, #183 and #88, and on #153 for its message part.

## 11. Close (product-manager)

- [ ] 11.1 Acceptance against D178 and the ux spec's §3 rows: every EC-UX-35…38 and EC-REV-13 scenario is covered by a passing test or a QA row. Verify: the
  coverage list is in this file's verification record.
- [ ] 11.2 Archive on the branch with `openspec archive fix-messages-everywhere --yes`, then validate. Verify:
  `openspec validate --specs --strict` exits 0.
- [ ] 11.3 Hand to `team-lead`: PR into `develop`, tracker T63, and a note that `fix-keyboard-follows-cursor` branches after this
  merge. Verify: the PR number is in the packet.

## Verification record

To be filled in by the engineer, the reviewer and QA.
