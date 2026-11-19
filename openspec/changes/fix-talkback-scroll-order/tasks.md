# Tasks

Base: `bugfix/255-details-talkback-order` (worktree `migraine-app-255`): eng-255's red tests 0ecb3bc6 on 05b37589, with
`origin/develop` f70b6450 (#245) merged in 3ad21ac7. Release 1.3.3 (team-lead 2026-11-19), new tracker row. Write `Refs #255`
in every commit and in the PR into `develop`, never a closing keyword: QA filed #255 and closes it after the retest. BRD D193,
EC-UX-78 to EC-UX-84, §13 row 57. Root cause: `migrely-work/255/eng-255-report.md`. Design: this change's `design.md` (D1 to
D8).

Ownership:
- `product-manager`: 0 (done) and 9;
- `ux-designer`: 1, before any code in 2 to 4;
- `android-engineer`: 2 to 6;
- `code-reviewer`: 7;
- `qa-engineer`: 8 (emulator-5580 API 36, then emulator-5582 API 26, one at a time).

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

- [ ] 3.1 For every page and dialog in design D8, a test measures each clickable node's touch bounds, width and height, at
  360 and 411 dp wide and at 100% and 200% font, against its bar (48 dp; 64 dp for attack mode's intensity choices,
  "Migraine ended", a message's Undo and Dismiss). Verify: a table in `migrely-work/255/eng/touch-targets.md` (site,
  controls, smallest size) and the test names; summary line here.
- [ ] 3.2 T1 History calendar days (`MonthSkyCalendar.kt:320-345`) at 360 dp wide, 200% font: if a cell is under 48 dp, a
  touch area covering the whole cell pitch, drawn cell, clip and focus ring unchanged. Verify: a test that a tap in the 6 dp
  gap opens the nearer day under D2 (red with D2 and no pitch area), and the History screenshot identical.
- [ ] 3.3 T2 and T3 Material pickers (`MigrelyDatePicker.kt:151`, `MigrelyDateTimePicker.kt:147, 189`,
  `RemindersScreen.kt:589`): if any control is under 48 dp under D2, that picker stays out of D2 (keeps D3); tell
  `product-manager` to record it in D193 (#255 stays open there). T4 attack mode: no change; the 48 dp secondary buttons
  pass. Verify: the 3.1 table rows for T2 to T4 and the decision written here.

## 4. Screens, red first per group (`android-engineer`, `ui` modules)

For each group: order and scroll tests and the cut sweep (2.5) through the shared fixture at 100% portrait (with the bottom
inset) and 200% landscape 360 dp, red before the entry point is applied, green after; screenshots before and after identical
to the pixel. Where a gap to the next stop is V/2 or more, the scroll test asserts the order, not that the item stayed. A
stop the sweep finds nameless gets its words on its own node (same string resource, inner `Text` cleared) and nothing else
does; each such stop passes the D181 one-name check (heard once, visible words in order). List them per group here.

- [ ] 4.1 G1 attack: Attack details (keyboard up and down; the aside bar at 360 dp keeps Back out of the tree, D192) and Add
  past attack; `ChoiceChip` option 3 (`AttackChoiceSections.kt:204-262`). Verify: `:features:attack:ui:testDebugUnitTest` green; the 2.0 and 2.1 tests green;
  `MessageBarAsideTest`, `AttackDetailsKeyboardRoomTest` unchanged and green.
- [ ] 4.2 G2 home: Today (3 sites) and attack mode (3 sites, 64 dp). Verify: `:features:home:ui:testDebugUnitTest` green.
- [ ] 4.3 G3 medication: Edit medication, Log medication (normal and dark room), Medications. Verify:
  `:features:medication:ui:testDebugUnitTest` green, `EditMedicationProblemFeedbackTest` and
  `FirstMedicationProblemFeedbackTest` unchanged and green.
- [ ] 4.4 G4 settings: Settings, Settings pages, Delete all data. Verify: `:features:settings:ui:testDebugUnitTest` green,
  `DeleteKeyboardTest` unchanged and green.
- [ ] 4.5 G5: History, Insights, Report, Your data and its 2 dialogs, Reminders and its dialog, Paywall, Onboarding. Verify:
  each module's `testDebugUnitTest` green.
- [ ] 4.6 G6 `designsystem` pickers (3 sites; D2 only where 3.3 kept it, D3 on all). Verify: `:designsystem:testDebugUnitTest` green, `KeyboardRoomTest`,
  `TopBarAsideTest`, `TypedLineMarginsTest` unchanged and green.
- [ ] 4.7 The source check (2.4) green with no exceptions outside the entry point. Mutations on the final code: remove D2 →
  order tests red; remove D3 → scroll tests red. Logs `4.7-*.log`.

## 5. Gate (`android-engineer`)

- [ ] 5.1 Full gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt
  lint` → BUILD SUCCESSFUL, exit 0; test counts in `5.1-gate.log`.
- [ ] 5.2 No permission, manifest or string change. Verify: `./gradlew :app:checkReleasePermissions` exit 0 and `git diff
  origin/develop --stat -- '*AndroidManifest.xml' '*strings.xml'` empty.

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
- [ ] 6.4 Restore font scale, rotation, `wm size`, density, animator scales, TalkBack and keyboard. Verify: settings read
  back. Never take a `uiautomator dump` during a TalkBack walk (it resets focus).

## 7. Review (`code-reviewer`)

- [ ] 7.1 Independent review of the diff against this spec and design: one entry point, delegate correctness, no drawn
  change, touch-target audit complete, guards untouched, tests red first with mutations. Verify: review verdict with
  findings fixed or answered, recorded here.

## 8. Acceptance (`qa-engineer`)

- [ ] 8.1 Negative control: TalkBack walks on Attack details on v1.3.1 and `develop` f70b6450 loop or skip (issue steps at
  360 dp 200% landscape; 100% portrait no keyboard). Verify: QA rows with APK hashes read back.
- [ ] 8.2 Candidate: the 8.1 walks reach both ends with every stop once and named; one walk per other page and dialog;
  API 26 at 360 and 411 dp; RTL; Remove animations; a message showing; dark room attack mode; rotation and process death mid
  walk; Switch Access scan and scroll; Tab; a chip cut at the top and at the bottom edge says its name on the
  candidate and only its state on `develop`. Verify: `docs/qa/fix-talkback-scroll-order.md` with each row, and what it does
  not prove.
- [ ] 8.3 Guards on the candidate: #238 KR rows, #245 SW rows that touch TalkBack (SW5, SW9), #239 V1. Verify: same file.
- [ ] 8.4 Maestro after names move to content descriptions (option 3 and any stop from the sweep): run the flows that select
  chips or those stops by text (`attack-details/`, `custom-tags/`, `attack-logging/`, `past-attacks/`,
  `a11y-tour/custom-tag-dialog.yaml`, `device/details-*.yaml`, more if the sweep reaches buttons and rows); check `childOf`,
  `containsChild`, `index:` and `selected:`/`checked:` next to a text match. QA updates any that need it. Verify: the flows'
  run output on the candidate and on `develop`.

## 9. Close-out (`product-manager`)

- [ ] 9.1 D193 result line (QA verdict, pages left out if any, what stays unproven), spec sync and archive of
  `fix-talkback-scroll-order`. Verify: `openspec validate --all --strict` exit 0 after archive; `openspec list` shows no
  active change.
