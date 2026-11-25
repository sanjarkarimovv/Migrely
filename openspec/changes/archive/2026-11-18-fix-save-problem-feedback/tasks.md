# Tasks

Base: `bugfix/239-save-error-feedback` (worktree `migraine-app-239`) from `origin/develop` 76c38c38, with
`origin/ux/239-237-feedback` 4001103f merged (fast-forward). Release: 1.3.2, tracker T75. Write `Refs #239` in every commit
and in the PR into `develop`, never a closing keyword: QA filed #239 and closes it after the retest. BRD D191, EC-UX-49,
EC-UX-50, EC-UX-66 to EC-UX-71. Design: `docs/ux/239-237-validation-and-save-feedback.md` section 3 (F1 to F12) and section
5 (V1 to V16); this change's `design.md` (D1 to D5).

Ownership:
- `product-manager`: 0 (done) and 8;
- `ux-designer`: 1 (done);
- `android-engineer`: 2 to 5;
- `code-reviewer`: 6;
- `qa-engineer`: 7 (emulator-5580 API 36, then emulator-5582 API 26, one at a time).

No `medical-reviewer`: no string and no health sentence changes. Layers: UI of `features/medication` only. Every test below
is written first and shown red on `develop` 76c38c38, then green; each mutation is a deliberately broken build that must turn
the test red again. Logs go to `migrely-work/239/eng/`. Robolectric tests live in `features/medication/ui/src/testDebug`.

## 0. Plan (`product-manager`)

- [x] 0.1 BRD D191, EC-UX-66 to EC-UX-71, EC-UX-49 and EC-UX-50 pointers, §13 row 55; proposal, design, `medication-list`,
  `medication-intakes` and `in-app-messages` deltas, tasks. Verify: `openspec validate fix-save-problem-feedback --strict` and
  `openspec validate --all --strict` exit 0.

## 1. Design (`ux-designer`)

- [x] 1.1 Rules F1 to F12, per-screen notes, acceptance V1 to V16: `docs/ux/239-237-validation-and-save-feedback.md`
  (4001103f). Scope by `team-lead` 2026-11-18: Edit medication and Log medication first use in; Add past migraine F2/F3 to
  the backlog; F9 in. Verify: `git log --oneline -1 -- docs/ux/239-237-validation-and-save-feedback.md` on this branch.

## 2. Edit medication (`android-engineer`, `features/medication/ui`)

- [x] 2.1 Announce once per arrival (D1, D2; F5, F7). Verify: a test (for example `EditMedicationProblemFeedbackTest`) that
  clears Name, presses Save, and asserts one polite live-region node carrying "Enter a name." appears once per `NotSaved`
  arrival; a second Save with no edit announces again (the node's content changes or is re-stamped). Red on 76c38c38 (no
  such node). Mutation: key the effect on the problem value instead of the arrival count → the second-Save case red.
  - Record (2026-11-18, android-engineer, 8509c53c; logs `migrely-work/239/eng/`): `EditMedicationProblemFeedbackTest`, full screen over the real ViewModel. One shared mechanism,
    `FieldProblemFeedback.kt`: per `NotSaved` arrival, touch and focus not moved -> the field comes into view and a
    `ProblemAnnouncer` speaks the text from its own 1 dp polite live-region node at the page's top (no actions), gone
    after 1 s (3 s before cr-239 F2), when the problem clears, or when the form leaves; the same text again re-announces
    (gone for a frame first).
    Red on de414287 (no node). Mutation `LaunchedEffect(problem)` -> the 3 "again" cases red (mutations.txt).
- [x] 2.2 Real error in the field's semantics, one stop (D3; F6). Verify: the merged node for Name has error "Enter a name."
  (not "Invalid input"); no other focusable node carries the error text; the announcement node is not focusable or
  traversable. Red on 76c38c38 ("Invalid input"). Mutation: drop the override → red.
  - Record: Name's merged node has `Error` "Enter a name."; only the field carries the text as text; the announcement
    node has no RequestFocus, OnClick or Focused. Correction (cr-239 F2): that does not make it "not traversable".
    TalkBack can land on a node that has text, and on 5570 a swipe walk from Name reached it. See the F2 record under
    5.1: it now lives 1 s, and a swipe within that second is a D191 residual. `Modifier.problemSemantics` comes before Material's default in the
    same node, and the first peer wins. Red on de414287 ("Invalid input"). Mutation: no override -> 2 cases red.
  - Added after the device walk (df7bbca4): the visible error line is hidden from semantics (`FieldSupportingLine`,
    shared by both forms), because TalkBack read it inside the field next to the error ("Name. Enter a name.. Error:
    Enter a name."). The tests now wait for the field's `Error` and assert no node carries the text as text; red on
    8509c53c (2 cases), mutation "read-twice" -> the same 2 red, "live-line" (counter as live region) -> the typing
    case red (mutations-f6.txt).
- [x] 2.3 Typing is quiet (F8). Verify: after the error, typing "Ib" produces no live-region change and no node in the field
  with `liveRegion`. Mutation: put `liveRegion = Polite` on the supporting text → red.
  - Record: after the error, typing "Ib" leaves no node with `LiveRegion` anywhere (green on de414287, a guard).
    Mutation: Polite live region on Name's supporting text -> red (and 4 other cases).
- [x] 2.4 Scroll into view (D5; F2). Verify: in a 411x891 dp window with a 250 dp IME inset, Name focused, page scrolled to
  Save: after Save, the field region's bounds are fully inside the visible part (below the top bar, above the IME inset). Same
  with no IME inset and Name unfocused. Already in view → scroll value unchanged. Red on 76c38c38 (Name off screen).
  Mutation: remove the bring-into-view request → red.
  - Record: 411x891 with a 250 dp IME inset, Name focused, page at Save -> Name's whole region inside the visible
    scroll area; 411x480 without IME, nothing focused -> the same, and no focus; already in view -> scroll value
    unchanged. Uses a new `inViewRequests` on `SingleLineFieldInView` (designsystem), U1's nested areas, one frame after
    the result so the error line counts. Red on de414287 (not in view). Mutation: no request -> 4 cases red (2 Edit, 2
    first use).
- [x] 2.5 Focus and keyboard (D1; F3, F4). Verify: (a) keyboard mode → Name focused (existing, keep green); (b) touch, IME
  inset present, Usual dose focused → Name focused, cursor at end, selection collapsed, no IME hide call; (c) touch, no IME
  inset → no field focused; (d) touch, IME inset, Name focused → focus unchanged. (b) red on 76c38c38. Mutation: request
  focus in every mode → (c) red.
  - Record: (a) keyboard mode stays covered by `EditMedicationKeyboardOrderTest` (green); (b) touch, IME up, cursor in
    Usual dose -> Name focused, selection `TextRange(9)` after the name "Ibuprofen" whose cursor was put at 0 (red on
    de414287: focus stayed in the dose); (c) touch, no IME -> nothing focused; (d) touch, IME up, Name focused ->
    unchanged. No keyboard show or hide call exists in the code. "Keyboard up" is read from the IME inset's height at
    the result (a window with no insets reads `isImeVisible` as true, which moved focus in an existing test). Touch is
    given by a fake `InputModeManager` (Robolectric never stays in touch mode). Mutation: focus in every mode -> (c)
    red, 10 red in all.
- [x] 2.6 Pending and one field (F1, F12). Verify: two quick presses → one `onSaveAction`, one announcement; empty name and
  invalid dose → only Name has an error. Mutation: allow the press while pending → red.
  - Record: two presses while the duplicate lookup is held -> one lookup, one announcement; empty name with a dose ->
    only Name has `Error`. Mutation: press allowed while pending (guard and enabled) -> the two-presses case red.

## 3. Log medication first use and messages (`android-engineer`, `features/medication/ui`)

- [x] 3.1 Apply 2.1 to 2.5 to `FirstMedicationForm` through the same mechanism (no second copy), in the normal page and the
  dark room. Hardware keyboard: the park then the move to Name in the same result frame. Verify: a test class for the
  first-use form with the announcement, field semantics, scroll and keyboard-mode focus cases, each red on 76c38c38; the
  dark-room variant of the announcement and scroll cases.
  - Record: `FirstMedicationProblemFeedbackTest` (normal and dark room): announcement once and again, real error,
    into view with the IME (411x700), hardware keyboard Tab to Add and log + Enter -> Name focused after the park. All
    6 red on de414287 (the record said 7; there were 6 tests). Same `FieldProblemFeedback`, no copy; the screen counts its own `NotSaved` arrivals. Typing in
    the first-use form does not clear its problem (unchanged), so only 2.1, 2.2, 2.4 and the keyboard case apply.
- [x] 3.2 A field problem clears an older message (D4; F9). Verify: in `MedicationMessages` tests and one screen test per
  form: "Couldn't save. Your phone's storage is full." shows, then `NotSaved(InvalidName)` → the message node is gone after
  one frame, no haptic call, the field error shows; then `Message(StorageFull)` → shown again. Red on 76c38c38. Mutation:
  clear only error-tone messages → the "You can keep up to 50 medications…" case red.
  - Record: `MedicationMessages` clears on `NotSaved` exactly as on `Logged` (#237's `leavesAtOnce`: no fade, draws
    nothing while leaving, no haptic). Host tests: storage full then `NotSaved` -> gone within two frames, no haptic
    call, then a new storage full shows; the 50-medications message the same. Screen tests: Edit and first use, a
    full-disk refusal then an empty name -> no message, field error. #237's guard "A refused medication after an error
    …" inverted to "clears the error and logs nothing". All red on de414287. Mutation: clear only error tone -> the
    50-medications case red.
- [x] 3.3 Rotation and process death (EC-UX-70 "Rotation after the clear"). Verify: `StateRestorationTester` after a clear
  by `NotSaved`: the field error shows, no message, and no new announcement on restore. Mutation: re-announce on restore →
  red.
  - Record: `StateRestorationTester` on Edit after the clear (past the announcement): field error shown, no message,
    no announcement node. Red on de414287 (message restored). Mutation: `problemArrivals` in `rememberSaveable` ->
    red.

## 4. Merge and gate (`android-engineer`)

- [x] 4.0 After #237 (`clear-message-when-intake-logged`) is merged into `develop`, merge `origin/develop` into this branch
  and resolve `MedicationMessages` so both clears (`Logged`, `NotSaved`) exist; rerun 3.2 and #237's 3.1. Verify: both test
  classes green on the merged tree; `git log --oneline -1 origin/develop` recorded here. If #237 is not merged yet when
  review starts, record that and do this before QA.
  - Record (2026-11-18, android-engineer): by `team-lead`'s decision #237 was merged first, before #239's code:
    8b96026b (de414287, BRD rows 54/55 and D190/D191 kept in order) and its review fixes d2fa83b4 (6e96b31b, a test-fake
    conflict resolved by keeping both builders). Both clears exist (`Logged`, `NotSaved` share `leavesAtOnce`); #237's
    3.1 and this 3.2 are green on the merged tree (gate-3.log). Still open: #237 is not in `develop` yet
    (`origin/develop` 9a48dbcf); merge `origin/develop` after it lands, before QA.
  - Done (2026-11-18): #237 merged as PR #251, `origin/develop` ae3593c6, merged here as 56e36528. One BRD conflict:
    develop's archived D190 text taken, D191 kept after it. The merge brought only docs, openspec and `qa/` files, no
    product file. Gate on 56e36528 exit 0, every task up to date from gate-4 (same inputs), 3438 tests, 0 failures
    (`gate-5.log`).
- [x] 4.1 Full gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`
  exit 0, with the counts of tests run (never `testDebugUnitTest` alone).
  - Record: exit 0, BUILD SUCCESSFUL on 95041d45 (`gate-3.log`) and again on df7bbca4 after the F6 fix (`gate-4.log`,
    1m 20s, BUILD SUCCESSFUL). Result XMLs: 3438 tests, 0 failures;
    `:features:medication:ui` 145 (debug) + 23 (release), `:designsystem` 237. Two earlier runs failed on test code only
    (a `;` in a test name, a ktlint class signature), fixed before gate-3.
- [x] 4.2 No permission, string or manifest change. Verify: `./gradlew :app:checkReleasePermissions` exit 0 and
  `git diff origin/develop -- '*.xml' '*strings*' '*AndroidManifest*'` empty except tests.
  - Record: `:app:checkReleasePermissions` exit 0. `git diff origin/develop -- '*.xml' '*AndroidManifest*'`: only #237's
    two strings (merged in); #239 adds no string, manifest or Gradle change.

## 5. Device walk (`android-engineer`, emulator-5570, headless first)

- [x] 5.1 V1, V2, V4, V5 and V6 on the candidate (debug build of the branch head, hash read back from the device), API 36,
  TalkBack 16 at VERBOSE, Gboard, 100% portrait. Verify: per row the TalkBack utterances in the 5 s after the tap that contain
  the error text, split into app-caused and TalkBack-caused (ux section 5), and a `uiautomator dump` with Name's bounds and
  the IME's top.
  - Record (2026-11-18, android-engineer, emulator-5570 `Migrely_API_36`, headless swiftshader, API 36, Gboard,
    1080x2400 @420, 100% portrait). Candidate: debug APK of df7bbca4 (sha256 f4c8b929…, read back on the device);
    control: v1.3.1's product tree, QA's debug APK of 76c38c38 (fde165db…, read back). TalkBack at VERBOSE; each press
    waits until TalkBack has been silent for 3 s, then counts the fragments in the next 5 s. Log
    `migrely-work/239/eng/walk/walk.log`, shots `migrely-work/239/shots/`.

    | Row | Candidate | v1.3.1 control |
    |---|---|---|
    | V1 Edit, Name cleared, keyboard up, TalkBack press on Save | 1 utterance "Enter a name." (app-caused); Name scrolled to [42,296]-[1038,532], below the top bar (296) and above the keyboard (1517); input focus and keyboard kept | 0 utterances; Name stays off screen |
    | V6 second press, no edit | 1 utterance "Enter a name." again | 0 utterances |
    | V2 Edit, no TalkBack, 1 s after Save | Name region in view as V1, focused, keyboard up | Name off screen (only the dose field in view) |
    | V2 first use | Name already in view at 100% (the page fits above the keyboard); stays focused, keyboard up, announcement node present | not run |
    | V4 cursor in Usual dose, Save, type "a" | focus moved to Name, keyboard stayed (1517), Name reads "a", dose empty; with TalkBack: 1 utterance, the field's reading "Editing. Edit box. Name. Error: Enter a name." | focus stays in the dose and "a" goes into the dose |
    | V5 first use, Tab to Add and log, Enter | focus to Name; with TalkBack 1 utterance "Edit box. Name. Error: Enter a name." (after the F6 fix) | not run |
    | V5 Edit, Tab to Save, Enter | focus to Name; with TalkBack only "Showing English (US) (QWERTY)" in 3 of 3 runs | the same: focus to Name (D181 (6)) and only the keyboard announcement |

    Found and fixed during the walk (df7bbca4): the first candidate (95041d45) read the focused Name as "Editing. Edit
    box. Name. Enter a name.. Error: Enter a name." (the visible line merged in next to the error). The error line is now
    hidden from semantics, so the field reads its error once; tests tightened and mutation-checked (read-twice -> 2
    red; live-line -> 1 red).

    Not conclusive here: V5 on Edit with TalkBack. The emulator has no hardware keyboard configured, so moving focus
    by `input keyevent` shows Gboard, and TalkBack speaks only its "Showing English…" on both builds. On a real hardware
    keyboard the soft keyboard stays hidden; QA's V5 on a device with a keyboard decides it. The field's reading of its
    error is shown in V4 (touch) and V5 first use.
- [x] 5.2 V11 (dark room, first use) and V12 at 200% portrait and landscape. Verify: dumps with the field region's bounds and
  one screenshot per cell in `migrely-work/239/shots/`.
  - Record: V11 (attack mode, Medication taken, first use, keyboard up, TalkBack press on Add and log): 1 utterance
    "Enter a name."; Name moved from under the top bar (top 267) to [42,296]-[1038,532]; dark room colours on the shot
    (`V11-cand-after.png`). A first V11 run lost the announcement because TalkBack had been switched on 1 s before the
    press and was still reading its start-up focus; rerun after TalkBack fell silent. Error colour not measured (F11,
    unchanged code).
    V12 at 200% (`V12-*.png`): portrait, Edit and first use: Name off screen before, [42,296]-[1038,647] after, keyboard
    up (1517), focused, announced. Landscape, keyboard closed: Name region [583,231]-[1946,582] (Edit) and
    [583,231]-[1946,576] (first use) inside the visible part [231,1080], keyboard stayed closed. Landscape, keyboard
    up (Tab, Enter): the visible part is only [231,394]; the field [168,457] cannot fit, and its input line is kept in
    view while the error line is under the keyboard, as U1 orders. Only the 411 dp landscape height was walked; 360 dp
    was not.
- [x] 5.3 Restore font scale, rotation, animator scales, TalkBack, keyboard and locale. Verify: the settings read back.
  - Record: TalkBack off (`enabled_accessibility_services` null, `accessibility_enabled` 0) and its log level back to
    ERROR, font 1.0, `user_rotation` 0, `accelerometer_rotation` 1, animator 1 (never changed), Gboard still the input
    method, locale not changed, `wm size` 1080x2400 and density 420, no filler files (no storage was filled). The app is
    the candidate with its data cleared. Emulator turned off (`adb emu kill`).

## 6. Review (`code-reviewer`)

- [x] 6.1 Review sections 2 to 4 against design D1 to D5: one mechanism for both forms, effect keyed on arrivals, no live
  region on the field, no "Invalid input", focus rules by mode and IME, no keyboard show or hide call, clear on `NotSaved`
  without fade or haptic, merge with #237 correct, no string change. Verify: a review packet with findings, each fixed or
  answered by the engineer before QA.
  - Record (2026-11-18, relayed by `team-lead`): `code-reviewer` cr-239 **APPROVED 1ce3eab9** on re-review. F1, F3 and F4
    fixed with mutations (section "Review cr-239" below); F2 is the accepted residual in BRD D191 (8): for 1 s a swipe can
    land on the 1 dp announcement node.

## 7. Acceptance (`qa-engineer`, this diff only)

- [x] 7.1 V1 to V16 (ux section 5), plus V17 and V3T (design "Acceptance", the D191 (8) residual; a press with no error heard is a fail) on the PR head with v1.3.1 as the negative control (V1, V2, V3, V4, V9, V10 and V11 must
  show the defect on v1.3.1, or that row proves nothing and says so); V7 with its mutation build; V15 as a guard. APK hashes
  read back on the device. Verify: a QA record `docs/qa/fix-save-problem-feedback.md` with each row, build SHA, result,
  control result and the app-caused and TalkBack-caused utterance counts.
  - Record: `docs/qa/fix-save-problem-feedback.md` (qa-239, branch `qa/239-acceptance` 9bffad27, merged here with a merge
    commit), verdict line: "**QA (qa-239), 2026-11-18: GO with recorded limits** for merging
    `bugfix/239-save-error-feedback` @ 1ce3eab9…". Proved: V1 one "Enter a name." at 0.64 s (v1.3.1: 0 in 5 s); V3T 5 of
    5 presses heard (v1.3.1: 0 of 5); V4 "a" lands in Name (v1.3.1: in Usual dose); V2, V3x, V9, V10, V11, V12 portrait,
    V13, V14 pass; V7 passes and its mutant M-V7b fails; V8 never "Invalid input"; V17 the D191 (8) residual 3 of 3;
    keyboard-closing race 8 of 8; "Migrely" 0 of 5 on both builds; API 26 V2 and V3x pass. Limits (none S1 or S2): V5
    Edit with TalkBack not proven (Gboard's "Showing English…" cuts the reading on both builds); V6-timed heard twice in
    3 of 4 runs, once in 1; V12 landscape 200% with the keyboard up shows only the input line (#245, D192); V16 RTL not
    reachable; V15 not run. Carried into D191 (9) by task 8.2.
- [x] 7.2 Restore every device setting. Verify: the settings read back in the QA record.
  - Record: QA record section "Restore (task 7.2)": emulator-5580 snapshot `changed: {}` against the start, emulator-5582
    read back, both emulators off.

## 8. Close (`product-manager`, then `android-engineer`)

- [x] 8.1 Before archive: diff the MODIFIED "Edit medication with a keyboard" against the live
  `openspec/specs/medication-list/spec.md`, and check that `in-app-messages` and `medication-intakes` gained nothing from #237
  that conflicts with the ADDED requirements here. Verify: the diff shows only the body's BRD ids and the two rewritten
  scenarios.
  - Record (2026-11-18, product-manager, branch at the QA merge 63c707e5, `openspec/specs` equal to `origin/develop`
    ae3593c6): per-requirement diff of "Edit medication with a keyboard", live vs delta: 3 hunks, the body's "D181" ->
    "D181, D191" and the scenarios "Save with a name problem" and "Save problem by touch"; the other ten scenarios equal.
    #237 (archived 2026-11-18) left one conflict: medication-intakes "A logged intake clears the message on Log
    medication" said "Only a stored intake SHALL clear it: … a refused medication SHALL keep the list and show their
    message as before", while the code (`LogMedicationMessagesTest` "A refused medication after an error clears the error
    and logs nothing") and in-app-messages "A field problem replaces the message on the medication forms" clear it on a
    name problem. Synced: a MODIFIED delta for that requirement (body sentence and BRD ids; scenario "A name problem on
    the first-medication form" added; the thirteen live scenarios unchanged), and the EC-MED-16 row points to EC-UX-70.
    in-app-messages "An error on the other screens goes when a later save succeeds" ("clear on success, never on the
    attempt") does not conflict: "attempt" is the tap before the outcome (its scenario "Not on the tap"), and the
    field-problem clear acts on the `NotSaved` outcome; no ADDED name here exists in the live specs.
- [x] 8.2 BRD D191 updated with QA's results (what was proved, what stays open, including the speech counts), then archive.
  Verify: `openspec archive fix-save-problem-feedback --yes` and `openspec validate --all --strict` exit 0.
  - Record (2026-11-18, product-manager): D191 (9) QA results (V1 1 utterance at 0.64 s vs 0; V3T 5/5 vs 0/5; the five
    limits and the (8) residual; V12 landscape tied to #245, D192) and (10) the 8.1 spec sync; EC-MED-16 points to
    EC-UX-70 (efc3ea8a). Archived with `--yes` into `openspec/changes/archive/2026-11-18-fix-save-problem-feedback/`;
    the exit codes are in the archive commit's message.
- [ ] 8.3 PR into `develop` with `Refs #239`, merge commit, worktree removed after merge, tracker T75 updated by `team-lead`.
  Verify: `gh pr view <n> --json state,mergeCommit` shows MERGED.

## Review cr-239 (2026-11-18, android-engineer; logs `migrely-work/239/eng/cr/`)

- F1 (no test for the announcement in #239's own keyboard-up case): Edit and first-use keyboard-up tests now await
  and count exactly one announcement. Mutation `announce only when the IME height is 0` -> 3 red (Edit, first use,
  dark room). e940e3f5.
- F3 (stale live region on the logged page): `FieldProblemFeedback` clears its announcer on dispose. Test "A dose
  logged right after a name problem leaves only the logged line live" (`live.single()`), red on a85623a8; mutation
  `onDispose { }` -> red. e940e3f5. Not walked: with a 1 s lifetime a device dump (about 1.5 s) cannot tell the two
  builds apart.
- F4 (a second Save inside the lifetime): test with no clock advance asserts a new announcement node id. Mutation
  "no frame between hide and show" -> red (`no-frame`); the wider mutation also removing `shown = null` -> 3 red.
- Smaller: first use "With no keyboard, no field takes focus" (guard; focus-always mutation -> red). Record 3.1 count
  corrected.
- Keyboard closing (cr's trace): focus moves now read `WindowInsets.imeAnimationTarget`, where the keyboard is going,
  so a Save tapped while it closes does not move focus and reopen it. Robolectric applies insets without the
  animation, so no unit test can show the race; on 5570 V4 (keyboard up) still moves focus to Name (0918f73f).
- F2 (the announcement node is a swipe stop): proved on 5570 with TalkBack (console swipes; `input swipe` is not a
  TalkBack gesture). Probe with a 60 s lifetime: a swipe walk from Name reached "Enter a name." (1 of 13 stops). A
  pane title instead of a live region was not a stop but made TalkBack say "Migrely" and re-read Name on every press:
  rejected. No option without text both speaks and skips, so the node stays and lives 1 s (0918f73f): 500, 1000 and
  3000 ms were spoken alike on the first press. Residual for D191 (sent to `product-manager`): a swipe from Name
  within that second lands on it (3/3); 1.5 s after the press it goes to the title (3/3). Also seen with the keyboard
  closed and TalkBack on: each press makes TalkBack say "Migrely", and in 1 of 2 runs the second press's announcement
  was flushed by TalkBack's own re-reading of Name (the error is still heard, from TalkBack), the same at 3000 ms.
- Re-walk on 0918f73f (5570 rebooted clean; sha256 6d7bea21…): V2 Edit (Name into view, keyboard up), V4 (focus to
  Name, "a" in Name), V1 and V6 (1 utterance "Enter a name." each). A first re-walk was void: console touches had put
  Gboard into its compact pill with no IME inset.
- Gate on 0918f73f: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`
  exit 0, BUILD SUCCESSFUL; result XMLs 3441 tests, 0 failures, `:features:medication:ui` debug 148 (`gate-6.log`).
