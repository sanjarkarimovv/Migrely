# Tasks

Base: `bugfix/255-details-talkback-order` (worktree `migraine-app-255`): eng-255's red tests 0ecb3bc6 on 05b37589, with
`origin/develop` f70b6450 (#245) merged in 3ad21ac7. Release 1.3.3 (team-lead 2026-11-19), new tracker row. Write `Refs #255`
in every commit and in the PR into `develop`, never a closing keyword: QA filed #255 and closes it after the retest. BRD D193,
EC-UX-78 to EC-UX-84, §13 row 57. Root cause: `migrely-work/255/eng-255-report.md`. Design: this change's `design.md` (D1 to
D8).

Ownership:
- `product-manager`: 0 (done) and 9.2;
- `ux-designer`: 1, before any code in 2 to 4;
- `android-engineer`: 2 to 6;
- `code-reviewer`: 7;
- `qa-engineer`: 8 and 9.1 (emulator-5580 API 36, then emulator-5582 API 26, one at a time).

No `medical-reviewer`: no string and no health sentence changes. Every test is written first and shown red on `develop`
f70b6450 (or on this branch before the fix), then green; each mutation must turn its test red again. Logs go to
`migrely-work/255/eng/`. Gradle under load: `nice -n 19 ./gradlew --max-workers=2 …`.

## 0. Plan (`product-manager`)

- [x] 0.1 BRD D193, EC-UX-78 to EC-UX-84, §13 row 57; proposal, design, `app-shell` delta, tasks. Verify:
  `openspec validate fix-talkback-scroll-order --strict` and `openspec validate --all --strict` exit 0.

## 1. Design inputs (`ux-designer`, before code)

All three in `docs/ux/255-talkback-scroll-order.md`, 91ceda44, with team-lead's decisions of 2026-11-19 folded into design
D3 to D5.

- [x] 1.1 TalkBack's scroll step: floor(V/2) px, V the actually visible part (ux section 1; design D3). The narrower
  wording ("stays on screen while the gap is under V/2; the next stop is always next in order") is adopted in the spec.
  Verify: `git log --oneline -1 -- docs/ux/255-talkback-scroll-order.md` → 91ceda44.
- [x] 1.2 Chip names: option 3, yes, only with D2; spoken "Not selected. Nausea" / "Selected. Nausea" (ux section 2; design
  D4.2). Other stops: data-driven through the cut sweep, not blanket (team-lead; design D4.3). Verify: the same note,
  section 2.
- [x] 1.3 Drawn: nothing drawn changes; D2 wraps only the scroll content; audit cases T1 to T4 (ux section 3; design D4a,
  D5). Verify: the same note, section 3.

## 2. Shared mechanism (`android-engineer`, `designsystem`)

- [x] 2.0 Re-run eng-255's red tests on this branch (f70b6450 merged) before any fix. Verify: `nice -n 19 ./gradlew
  --max-workers=2 :features:attack:ui:testDebugUnitTest --tests '*AttackDetailsTalkBack*'` → 7 tests, the no-keyboard order
  test passes, the rest fail with a named wrong step; log `migrely-work/255/eng/2.0-red.log`. If the counts differ from
  0ecb3bc6's (6 of 7 red), report to `product-manager` before going on.
  Result (eng-255b, 81323590): 7 tests completed, 6 failed; "no keyboard, order" passes; the 6 name their wrong step
  (e.g. "Back of the head -> Change when it ended", "Aura left the screen"). Logs are in `migrely-work/255b/` (`255/eng`
  links there): `2.0-red.log`, `2.0-red-summary.txt`.
- [x] 2.1 Navigation-bar case (design D7): an order test with a bottom system-bar inset so the visible part ends above the
  window (as `walk-b1`). Verify: red on this branch before the fix, log `2.1-red.log`.
  Result: `Portrait at 100 percent, navigation bar, no keyboard, the order runs down the page` (24 dp bar) red, 4 order
  tests completed, 3 failed; wrong steps such as "Add your own symptom -> Pain" (`2.1-red-summary.txt`).
- [x] 2.2 Shared test fixture: move `TalkBackTree` (order from the traversal data, scroll by the accessibility action, "only N
  items" guard) where every `ui` module's tests can use it, no copies. Replace the merged-semantics `talkBackName()`
  (`TalkBackTree.kt:76`) with an unmerged read as TalkBack builds the name (own text or description, then visible
  non-focusable children), so a cut name can fail. Verify: the 2.0 and 2.1 tests still red for the same reason after the
  move; a fixture test where a chip's text child is clipped out returns an empty name with the new read and the full name
  with the old one (shows why the old read could not fail).
  Result: new test-support module `:architecture:ui-test` (package `com.migrely.app.test.talkback`, like
  `:architecture:presentation-test`), used through `testDebugImplementation`; attack's copy deleted. `TalkBackTree.spokenName`
  is the unmerged read; the merged read is kept only to find stops and label failures, renamed `mergedWords()` so it is
  not mistaken for a name check. `setNavigationBar` moved there too. `SpokenNameReadTest`: 2 tests, 0 failures (whole chip
  "Nausea"; chip cut 8 dp above its text: new read "", merged "Nausea"). Attack TalkBack tests: 8 completed, 7 failed with
  the same named steps as 2.0 and 2.1 (`2.2-summary.txt`).
- [x] 2.3 The entry point (D1) with exact bounds (D2, scroll content only, D4a) and the step floor(V/2) px (D3).
  `ViewConfiguration` delegate: only `minimumTouchTargetSize` changes. Verify: a `designsystem` test that (a) a node 1 to 24
  dp below the visible edge with a bottom inset is absent from the tree, (b) one accessibility scroll moves floor(V/2) px
  within 1 px, also with an inset inside the scroll content (V = what shows, not `viewportSize`), (c)
  `doubleTapTimeoutMillis`, `longPressTimeoutMillis` and `touchSlop` inside equal those outside, (d) the minimum touch target
  reads 48 dp in a top bar, a message host and dialog buttons beside the scroll and 0 inside it. Red first. Mutations: no D2
  → (a) red; no D3 or `viewportSize / 2` → (b) red; a non-delegating configuration → (c) red; D2 around the whole screen →
  (d) red.
  Result: `designsystem/layout/PageScroll.kt`: `PageScroll { Column(Modifier….pageScroll(state)) { … } }`. The
  provider wraps the scrolling layout itself, not only its children: Compose widens the clip in the clipping
  coordinator's `rectInParent`, which reads that layout node's own configuration. `pageScroll` exists only in the
  scope, so a page cannot get the step without exact bounds. V = the viewport's `boundsInRoot` less
  `MigrelyInsets.barsAndKeyboard` over it, read when TalkBack scrolls. `PageScrollTest` red on a stub (7 tests, 6
  failed: hidden items [1, 12, 23] dp in the tree, moved 867 and 891 px for 433, minimum target 48 inside), green
  after (7, 0). Mutations, each red: no D2 → 4 failed (a, c, d); no D3 → 2 failed (b both); step from the viewport
  (`viewportSize / 2`) → 1 failed (moved 445 for 433); non-delegating configuration → 1 failed (c); D2 around the
  whole sample screen → 1 failed (d). `:designsystem:testDebugUnitTest` 273 tests, 0 failed (KeyboardRoomTest 9,
  TopBarAsideTest 13, TypedLineMarginsTest 5 unchanged). Logs `2.3-*.log`, `mut-2.3-*.txt`.
- [x] 2.4 Source check (D1): fails on `verticalScroll(` in `src/main` outside `designsystem`'s entry point, naming file and
  line. Verify: red now with 29 sites listed (design D8); green at the end of 4; negative control: a scratch raw call turns it
  red again. Log `2.4-*.log`.
  Result: `app/src/test/.../architecture/ArchitectureScrollsTest` (Konsist, beside `ArchitectureDialogsTest`): red now,
  "raw verticalScroll calls: 29", the 29 sites of D8 by file and line (`2.4-red-summary.txt`). Negative control: a
  scratch `ScratchRawScroll.kt` in home ui → 30, naming `ScratchRawScroll.kt:8`, its comment line 7 not counted; file
  removed → 29. It stays red, committed on purpose, until 4.7 turns it green.

- [x] 2.5 Cut sweep in the shared fixture (design D7): for each stop, scroll so 1 px to 12 dp of it shows at the top edge,
  then at the bottom edge, and read its name with the 2.2 read; report each stop whose name is empty or partial. Verify: on
  Attack details under D2 alone (no option 3) the sweep is red for symptom chips (negative control, log `2.5-red.log`);
  with option 3 on `ChoiceChip` it is green for chips.
  Result: `cutSweep(page, of)` in `:architecture:ui-test` (`CutSweep.kt`): cuts of 1 px, 4, 8 and 12 dp at each edge,
  compared with the stop's name when whole; a stop is any merged node with words, a click or merged children, and
  one that says nothing even when whole is reported too. `AttackDetailsCutSweepTest` (portrait 100%, D2 given to the
  whole screen by the test, as prototype 2): before option 3, 190 findings, every symptom, trigger and location chip
  says '' when cut (`2.5-red-summary.txt`); with option 3 on `ChoiceChip` (`contentDescription = spokenLabel ?:
  label`, inner `Text` cleared) the chip sweep is green (1 test, 0 failed). Mutations, each red: description back to
  spokenLabel only → chips say nothing even when whole; the original code → 152 findings in 29 chips. A first run of
  that mutation passed because the sweep skipped stops with no words; the sweep was fixed to count them. D181 checks
  (`AttackSpokenNameTest`, the "Your own" chips) green; attack ui 136 tests, 7 failed = the 7 known red of 2.0/2.1.
  **Input to 4.1 (D4.3, data-driven):** with chips fixed, 10 more stops on Attack details lose their name when cut
  (38 findings): "Remove pain level 6 at 14:15" and "… 9 at 15:05" (icon buttons), "Add pain level", "Add medication",
  the four impact choices ("0 · No impact" … "3 · Bed rest", radio rows), the note field (says its text without
  "49/1000") and "Delete migraine" (`2.5-option3-summary.txt`). Expect the same for Material buttons on other pages.

## 3. Touch-target audit (`android-engineer`, before D2 lands on each page)

- [x] 3.1 For every page and dialog in design D8, a test measures each clickable node's touch bounds, width and height, at
  360 and 411 dp wide and at 100% and 200% font, against its bar (48 dp; 64 dp for attack mode's intensity choices,
  "Migraine ended", a message's Undo and Dismiss). Verify: a table in `migrely-work/255/eng/touch-targets.md` (site,
  controls, smallest size) and the test names; summary line here.
  **Stopped (eng-255b, 2026-11-19), decision needed:** fixture `touchTargets(page)` in `:architecture:ui-test` measures
  the node that takes each click inside the page, with no widening. `AttackTouchTargetAuditTest` (Attack details, 360
  and 411 dp, 100% and 200%): 43 controls in the page, 41 at 48 dp or more; the two "Remove pain level" Material3
  `IconButton`s are **40×40 dp** at all four sizes (`3.1-attack-summary.txt`). Material's
  `minimumInteractiveComponentSize` keeps a 48 dp layout, but the click lands on the 40 dp node and the 4 dp ring is
  touchable only through `ViewConfiguration.minimumTouchTargetSize`, which D2 sets to 0. Design D2's note ("Material
  controls keep their 48 dp layout size") holds for layout, not for touch. Not a T1 to T4 case; the same pattern
  (Material icon buttons, checkboxes, switches) appears in 21 `src/main` files, not yet measured.
  **Lead decision 2026-11-19, option 1:** inside D2 every clickable, toggleable or selectable node is itself at least
  48×48 dp (its own size, never `ViewConfiguration` widening); nothing drawn changes. Icon buttons:
  `designsystem/button/MigrelyIconButton` (48 dp clickable node; Material's 40 dp circle, container, icon and ripple
  drawn inside it, presses moved by the 4 dp inset so the ripple starts under the finger). `MigrelyIconButtonTest`:
  red on a stub that is Material's `IconButton` (tap 2 dp outside the 40 dp circle, inside 48 dp, inside a page: no
  click), green after; the control test shows Material's button misses that tap inside a page. At rest and disabled:
  0 pixels differ from Material's (mutation, icon moved: 198 pixels differ, red). Named limits: the circle's size does
  not show at rest (transparent container), and Robolectric draws no platform ripple (Material's own pressed button
  equals its resting one), so the pressed comparison is the instrumented `MigrelyIconButtonPressedTest` (compiles; to
  run on emulator-5570 in the device round). TalkBack's focus outline follows the node: 48 dp square, was 40 dp.
  The 9 in-page icon buttons use it (Attack details' intake and pain-entry Close, History's month chevrons, Today's
  Settings, a medication row's Close, the effect question's Skip, the visit card's and trial banner's Close); top-bar
  ones, the Paywall's close row above its scroll and the message host stay Material. Checkboxes, switches, radios:
  each passes `null` as its own handler, the row is the toggleable or selectable node, so no change (rule 2).
  **Result 3.1:** `auditTouchTargets` fixture; one `*TouchTargetAuditTest` per ui module (attack, home, medication,
  settings, history, report, backup, reminders, subscription, onboarding, insights) at 360 and 411 dp, 100% and 200%,
  plus landscape 800×360 dp for home and history (two-pane sites): 48 tests, 0 failed; 31 screens and cards, every
  control at its bar (smallest 48.0 dp; attack mode's 64 dp controls at 64; History's day cells 47-48 dp, T1). Table
  `migrely-work/255b/touch-targets.md` (127 rows). Your data's two dialogs scroll text only; Warning signs, About,
  Licences have no control in the page. With the full ui suites: every module green except the 7 known red attack
  tests (4.1).
- [x] 3.2 T1 History calendar days (`MonthSkyCalendar.kt:320-345`) at 360 dp wide, 200% font: if a cell is under 48 dp, a
  touch area covering the whole cell pitch, drawn cell, clip and focus ring unchanged. Verify: a test that a tap in the 6 dp
  gap opens the nearer day under D2 (red with D2 and no pitch area), and the History screenshot identical.
  Result: at 360 dp a cell is 41.7 dp. The day's clickable, focus and semantics now sit on a slot of the whole pitch
  (cell plus 3 dp, half the gap, on each side); the drawn cell (clip, sky, rings, focus ring, ripple from
  `LocalIndication`, presses moved by the inset) is inside it, and the weeks are laid out 3 dp out on every side so
  each cell is drawn where it was. `MonthSkyCalendarPitchTest`: inside `PageScroll`, taps 1 dp into the gap on each
  side of day 1, across and down, red before (clicked `[]`), green after (`[1, 2, 1, 8]`). Pixels of the window at 360
  and 411 dp, 100% and 200%, with the chosen, today, period and focus rings: byte-identical before and after
  (`3.2-before/`); control: without the 3 dp outset all four differ. TalkBack's outline for a day is the pitch.
- [x] 3.3 T2 and T3 Material pickers (`MigrelyDatePicker.kt:151`, `MigrelyDateTimePicker.kt:147, 189`,
  `RemindersScreen.kt:589`): if any control is under 48 dp under D2, that picker stays out of D2 (keeps D3); tell
  `product-manager` to record it in D193 (#255 stays open there). T4 attack mode: no change; the 48 dp secondary buttons
  pass. Verify: the 3.1 table rows for T2 to T4 and the decision written here.
  Result (`PickerTouchTargetAuditTest`, 4 sizes): the date picker and the date-time picker's date step have the mode
  toggle and month arrows at 40×40 dp and the month/year button at 193×40; the time step has AM/PM at 52×40 and its
  Cancel/OK (inside its scroll) 40 dp tall; day cells and clock numbers are 48. **Decision: T2 and T3 apply** —
  `MigrelyDatePicker.kt:151`, `MigrelyDateTimePicker.kt:147, 189` and Reminders' time dialog (`RemindersScreen.kt:589`,
  same Material `TimePicker`) stay out of D2 and keep D3 (for 4.6: the entry point needs a step-only form);
  `product-manager` records it in D193, #255 stays open there. T4: attack mode's 64 dp controls measure 64 in
  landscape (in portrait they sit outside the scroll); its Details button is 48 dp or more. If Material's controls
  ever reach 48 dp the picker test turns red.
  PM (team-lead 2026-11-19): recorded in D193 (4) and design D5 as a residual; instead of #255 staying open there, QA
  files a follow-up issue for the pickers at close-out (9.1) and #255 closes on the rest.

## 4. Screens, red first per group (`android-engineer`, `ui` modules)

For each group: order and scroll tests and the cut sweep (2.5) through the shared fixture at 100% portrait (with the bottom
inset) and 200% landscape 360 dp, red before the entry point is applied, green after; screenshots before and after identical
to the pixel. Where a gap to the next stop is V/2 or more, the scroll test asserts the order, not that the item stayed. A
stop the sweep finds nameless gets its words on its own node (same string resource, inner `Text` cleared) and nothing else
does; each such stop passes the D181 one-name check (heard once, visible words in order). List them per group here.

  **How (eng-255b, 2026-11-19).** Shared fixture `talkBackWalk` (`:architecture:ui-test`): for each screen of a module,
  in a window drawn edge to edge with a 24 dp navigation bar, every vertical scroll area gets the order check down the
  page (a step is wrong when it goes back up both in reading order, `traversalIndex` honoured, and on screen), the
  swipe-back and swipe-on check (TalkBack's step; where the next item is not yet in view it steps again in the same
  swipe, design D3; the item it was on must stay on screen while the gap is under V/2), the cut sweep, and a heard-once
  check (no stop says a part of its words twice). It dumps each screen's window pixels. One `*TalkBackWalkTest` per ui
  module (portrait 411 dp 100%, landscape 360 dp tall 200%) over the same screens as its audit (`*TestScreens.kt`).
  Before (`4.0-walk-before-summary.txt`): red in 10 of 11 modules (home green both). Every site wired by one
  mechanical change (`PageScroll { Column(… .pageScroll(state) …) }`), then the stops the sweep found get
  `ownWords(...)` on their own node and `wordsShownOnly()` on their Text (`hideFromAccessibility`: out of TalkBack's
  reading, still in the semantics tree, so tests, Maestro and Voice Access find the shown words). Fields: label on
  the field's node, counter as its state description while no problem shows. After: 22 walk tests green; window
  pixels of all 27 screens × 2 sizes byte-identical before and after (`4.x-pixels-final.txt`, 54 of 54).
  Named per group: G1 Attack details' Got it, Add pain level, Add medication, the 4 impact choices, the note's
  counter, Delete migraine, Add past attack's Add migraine; every in-page icon button through `MigrelyIconButton`'s
  new `contentDescription`. G3 kind rows, category rows (title, examples), Save, Name and Dose fields (Edit and Log
  medication). G4 Settings' switch, link and value rows, the policy link, the Delete field. G5 History's Period and
  Pain-free switches, Add a past migraine, Show all of the month; Report's period chips, Custom, Create report; Your
  data's two action rows; Reminders' two switch rows; Paywall's buy and Restore buttons; Onboarding's Restore;
  Insights' period chips.

- [x] 4.1 G1 attack: Attack details (keyboard up and down; the aside bar at 360 dp keeps Back out of the tree, D192) and Add
  past attack; `ChoiceChip` option 3 (`AttackChoiceSections.kt:204-262`). Verify: `:features:attack:ui:testDebugUnitTest` green; the 2.0 and 2.1 tests green;
  `MessageBarAsideTest`, `AttackDetailsKeyboardRoomTest` unchanged and green.
- [x] 4.2 G2 home: Today (3 sites) and attack mode (3 sites, 64 dp). Verify: `:features:home:ui:testDebugUnitTest` green.
- [x] 4.3 G3 medication: Edit medication, Log medication (normal and dark room), Medications. Verify:
  `:features:medication:ui:testDebugUnitTest` green, `EditMedicationProblemFeedbackTest` and
  `FirstMedicationProblemFeedbackTest` unchanged and green.
- [x] 4.4 G4 settings: Settings, Settings pages, Delete all data. Verify: `:features:settings:ui:testDebugUnitTest` green,
  `DeleteKeyboardTest` unchanged and green.
- [x] 4.5 G5: History, Insights, Report, Your data and its 2 dialogs, Reminders and its dialog, Paywall, Onboarding. Verify:
  each module's `testDebugUnitTest` green.
- [x] 4.6 G6 `designsystem` pickers (3 sites; D2 only where 3.3 kept it, D3 on all). Verify: `:designsystem:testDebugUnitTest` green, `KeyboardRoomTest`,
  `TopBarAsideTest`, `TypedLineMarginsTest` unchanged and green.
  Result 4.1-4.6 (`4.x-suites3.log`): designsystem 287 tests, attack 142, home 130, medication 160, settings 48,
  history 69, report 14, backup 22, reminders 33, subscription 48, onboarding 11, insights 21, ui-test 2: 0 failed
  (the walk's last fix, the geometric order, then re-ran the 22 walk tests green, `4.x-walk-final.log`). The 7 tests of
  2.0/2.1 green; guards `MessageBarAsideTest`, `AttackDetailsKeyboardRoomTest`, `EditMedicationProblemFeedbackTest`,
  `FirstMedicationProblemFeedbackTest`, `DeleteKeyboardTest`, `KeyboardRoomTest`, `TopBarAsideTest`,
  `TypedLineMarginsTest` unchanged and green. 4.2: home was green before too (its screens' bands fall where the order
  holds); it is wired all the same. 4.6: pickers and Reminders' time dialog use `PageScroll(exactBounds = false)`;
  `PageScrollTest` "Step only…" (48 dp kept, half step) red with exact bounds forced on, green after. Mutation of the
  heard-once check (`wordsShownOnly` a no-op): attack walk red, "said twice: Got it | Got it".
- [x] 4.7 The source check (2.4) green with no exceptions outside the entry point. Mutations on the final code: remove D2 →
  order tests red; remove D3 → scroll tests red. Logs `4.7-*.log`.
  Result: `ArchitectureScrollsTest` green (was 29 sites; its negative control is 2.4's). Mutations on the final code
  (`mut-4.7-*.txt`): D2 forced off → 5 of 10 attack TalkBack tests red (the 3 order tests and both walks); the step
  element removed → 6 of 10 red (the 4 scroll tests and both walks).

## 5. Gate (`android-engineer`)

- [x] 5.1 Full gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt
  lint` → BUILD SUCCESSFUL, exit 0; test counts in `5.1-gate.log`.
  Result (e8055704): exit 0, BUILD SUCCESSFUL in 6m 38s; 3,589 tests, 0 failed, 0 skipped, in 566 result files
  (`migrely-work/255b/5.1-gate.log`).
- [x] 5.2 No permission, manifest or string change. Verify: `./gradlew :app:checkReleasePermissions` exit 0 and `git diff
  origin/develop --stat -- '*AndroidManifest.xml' '*strings.xml'` empty.
  Result: `:app:checkReleasePermissions` exit 0; the diff is empty (0 lines).

## 6. Device walks (`android-engineer`, emulator-5570, TalkBack VERBOSE, console swipes)

- [ ] 6.1 Attack details: previous from the end to Back and next from Back to the end, every stop once and named, at 100%
  portrait (no keyboard; keyboard up) and 200% landscape in the 360 dp window (`wm size 945x2100`; keyboard up, then closed
  with Back). Include a stop on a chip cut at an edge: it says "Not selected. Nausea" (or "Selected. …"). Verify: walk logs
  `6.1-*.txt` with stop counts and the cut chip's line; the same walk on `develop` loops or skips (control). Read each
  scroll's distance from the log: floor(V/2) within 1 px.
- [ ] 6.2 One walk per other page and dialog at 100% portrait and 200% landscape, keyboard up on the four text-field screens.
  Verify: `6.2-*.txt`, each reaching both ends with no repeat.
- [ ] 6.3 Hardware keyboard Tab and Shift+Tab on Attack details and one dialog: order unchanged, every stop in view. Verify:
  `6.3-*.txt`.
- [ ] 6.4 Pressed-state pixels of `MigrelyIconButton` against Material's (Robolectric draws no ripple). Verify:
  `ANDROID_SERIAL=emulator-5570 nice -n 19 ./gradlew --max-workers=2 :designsystem:connectedDebugAndroidTest
  -Pandroid.testInstrumentationRunnerArguments.class=com.migrely.app.designsystem.button.MigrelyIconButtonPressedTest`
  → BUILD SUCCESSFUL, 0 failed; log `6.4-pressed.log`.
- [ ] 6.5 Restore font scale, rotation, `wm size`, density, animator scales, TalkBack and keyboard. Verify: settings read
  back. Never take a `uiautomator dump` during a TalkBack walk (it resets focus).

## 7. Review (`code-reviewer`)

- [ ] 7.1 Independent review of the diff against this spec and design: one entry point, delegate correctness, no drawn
  change, touch-target audit complete, guards untouched, tests red first with mutations. Verify: review verdict with
  findings fixed or answered, recorded here.
  Engineer's fixes for cr-255's round 1 on f8d958da (eng-255b; logs `migrely-work/255b/r1-*`), each red first:
  - Root cause: the fixture now reads what Android exposes (`AccessibilityNodeInfo`: the description Compose hands
    over, first entry only and on a fake child for a merging node; text; state, not for an edit box; visibility), and
    the walk checks that every word shown on a stop in view is said. On the old code it was red: 9-10 Settings rows,
    category examples, Your data and Reminders rows (`r1-walk-red.txt`); M1 (counter hidden) red, "not said: '9/60'".
  - F1: only a control's name goes on its own node (`ownWords(name)`, one string); summaries, values, disabled reasons,
    examples and the policy address stay spoken children. F2/F7: counters are spoken children again (also heard on
    the device, 6.1: "…remembering. 0/1000"); `FieldProblemFeedback.kt` is develop's text again.
  - F3: `ArchitectureScrollsTest` also pins `PageScroll(exactBounds = false)` to the 3 picker sites and the time
    dialog; M2 (date picker set to true) red. F8: it also rejects `horizontalScroll(`, `.scrollable(`, `LazyColumn`,
    `LazyRow`, `LazyVerticalGrid` with `(` or `{`; a scratch `LazyColumn { }` red at its line, green once removed (a
    first scratch with no parentheses passed and showed the gap).
  - F4: `AttackDetailsExactBoundsScopeTest` on the real page with its message below the top bar: Back and Dismiss
    expose 48×48 dp; M3 (exact bounds reaching the top bar) red, Back 40×40.
  - F5: `MessageLayout` gives its message and content a `MessageCover`; `MigrelySnackbarHost` fills in the shown
    message's bounds; `PageScroll` leaves that part out of V. `PageScrollTest` case with a message: cover ignored →
    red (433 px for 403). Attack details with a message showing joined the walk and audit screens.
  - F6: calendar pixels asserted against PNG goldens rendered from develop's calendar code (test resources, 24-31 KB);
    no outset → red (18,719-35,450 pixels differ). `talkBackWalk`'s pixel dump is a manual tool, off unless
    `MIGRELY_SCREEN_PIXELS` is set, never asserted.
  - Open for product (not a regression; develop does the same): a message over the top of a page covers the items
    under it, so they are out of TalkBack's tree until the message goes; at the page's top, swiping back ends at the
    first uncovered item. The walk logs it as "COVERED at the top" instead of failing.

## 8. Acceptance (`qa-engineer`)

- [ ] 8.1 Negative control: TalkBack walks on Attack details on v1.3.1 and `develop` f70b6450 loop or skip (issue steps at
  360 dp 200% landscape; 100% portrait no keyboard). Verify: QA rows with APK hashes read back.
- [ ] 8.2 Candidate: the 8.1 walks reach both ends with every stop once and named; one walk per other page and dialog;
  API 26 at 360 and 411 dp; RTL; Remove animations; a message showing; dark room attack mode; rotation and process death mid
  walk; Switch Access scan and scroll; Tab; a chip cut at the top and at the bottom edge says its name on the
  candidate and only its state on `develop`; known, intended change: TalkBack's focus outline is the 48 dp node on in-page
  icon buttons and the whole pitch on History days (was the 40 dp circle and the drawn cell), not a defect; the pickers and
  Reminders' time dialog are a recorded residual (design D5), walked for the half step only. Verify: `docs/qa/fix-talkback-scroll-order.md` with each row, and what it does
  not prove.
- [ ] 8.3 Guards on the candidate: #238 KR rows, #245 SW rows that touch TalkBack (SW5, SW9), #239 V1. Verify: same file.
- [ ] 8.4 Maestro after names move to content descriptions (option 3 and any stop from the sweep): run the flows that select
  chips or those stops by text (`attack-details/`, `custom-tags/`, `attack-logging/`, `past-attacks/`,
  `a11y-tour/custom-tag-dialog.yaml`, `device/details-*.yaml`, more if the sweep reaches buttons and rows); check `childOf`,
  `containsChild`, `index:` and `selected:`/`checked:` next to a text match. QA updates any that need it. Verify: the flows'
  run output on the candidate and on `develop`.

## 9. Close-out (`product-manager`)

- [ ] 9.1 (`qa-engineer`) File a follow-up issue for the residual: the Material date picker, the date-time picker and
  Reminders' time dialog stay out of exact bounds (40 dp controls: mode toggle, month arrows, month/year button, AM/PM,
  time-step Cancel and OK), so cause B can still reach them. #255 is then closed by QA on the rest after the retest.
  Verify: `gh issue view <new>` shows the pickers, the measured sizes and a link to #255; `gh issue view 255` closed by QA.
- [ ] 9.2 (`product-manager`) D193 result line (QA verdict, the picker residual with its follow-up issue number, what stays
  unproven), spec sync and archive of `fix-talkback-scroll-order`. Verify: `openspec validate --all --strict` exit 0 after
  archive; `openspec list` shows no active change.
