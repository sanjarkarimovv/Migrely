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

- [ ] 1.1 TalkBack's scroll step (design D4.1): the value or rule, with the reason, for TalkBack and Switch Access, checked
  against the 61 px visible part (360 dp, 200% landscape, keyboard up) and a long page at 100% portrait. Verify: a decision
  note `docs/ux/255-talkback-scroll-order.md` committed on this branch; `git log --oneline -1 --
  docs/ux/255-talkback-scroll-order.md`.
- [ ] 1.2 Chip name on the chip node (design D4.2, option 3): yes or no; if no, the other way a chip cut by the edge says its
  name. If neither, tell `product-manager`, who changes "Every stop says its name" before code. Verify: the same note, a
  section "Chip names", with the exact spoken form for a selected and an unselected chip.
- [ ] 1.3 Confirm nothing drawn changes (D193 (2)) and list any page where the audit (task 3) may force a touch-area change
  that ux must look at. Verify: the same note, a section "Drawn".

## 2. Shared mechanism (`android-engineer`, `designsystem`)

- [ ] 2.0 Re-run eng-255's red tests on this branch (f70b6450 merged) before any fix. Verify: `nice -n 19 ./gradlew
  --max-workers=2 :features:attack:ui:testDebugUnitTest --tests '*AttackDetailsTalkBack*'` → 7 tests, the no-keyboard order
  test passes, the rest fail with a named wrong step; log `migrely-work/255/eng/2.0-red.log`. If the counts differ from
  0ecb3bc6's (6 of 7 red), report to `product-manager` before going on.
- [ ] 2.1 Navigation-bar case (design D7): an order test with a bottom system-bar inset so the visible part ends above the
  window (as `walk-b1`). Verify: red on this branch before the fix, log `2.1-red.log`.
- [ ] 2.2 Shared test fixture: move `TalkBackTree` (order from the traversal data, scroll by the accessibility action, "only N
  items" guard) where every `ui` module's tests can use it, no copies. Verify: the 2.0 and 2.1 tests still red for the same
  reason after the move.
- [ ] 2.3 The entry point (D1) with exact bounds (D2) and the step (D3, value from 1.1). `ViewConfiguration` delegate: only
  `minimumTouchTargetSize` changes. Verify: a `designsystem` test that (a) a node 1 to 24 dp below the visible edge with a
  bottom inset is absent from the tree, (b) one accessibility scroll moves by the step within 1 px, (c) `doubleTapTimeoutMillis`,
  `longPressTimeoutMillis` and `touchSlop` inside equal those outside. Red first. Mutations: no D2 → (a) red; no D3 → (b) red;
  a non-delegating configuration → (c) red.
- [ ] 2.4 Source check (D1): fails on `verticalScroll(` in `src/main` outside `designsystem`'s entry point, naming file and
  line. Verify: red now with 29 sites listed (design D8); green at the end of 4; negative control: a scratch raw call turns it
  red again. Log `2.4-*.log`.

## 3. Touch-target audit (`android-engineer`, before D2 lands on each page)

- [ ] 3.1 For every page and dialog in design D8, a test measures each clickable node's touch bounds at 100% portrait: ≥ 48
  dp, ≥ 64 dp in attack mode. Verify: a table in `migrely-work/255/eng/touch-targets.md` (site, controls, smallest size) and
  the test names; summary line here. Any failure: fix its touch area without a visual change and show the screenshot
  identical, or leave the page out of D2 and tell `product-manager` (design D5, D193 (4)).

## 4. Screens, red first per group (`android-engineer`, `ui` modules)

For each group: order and scroll tests through the shared fixture at 100% portrait (with the bottom inset) and 200%
landscape 360 dp, red before the entry point is applied, green after; screenshots before and after identical to the pixel.

- [ ] 4.1 G1 attack: Attack details (keyboard up and down; the aside bar at 360 dp keeps Back out of the tree, D192) and Add
  past attack; chip names per 1.2. Verify: `:features:attack:ui:testDebugUnitTest` green; the 2.0 and 2.1 tests green;
  `MessageBarAsideTest`, `AttackDetailsKeyboardRoomTest` unchanged and green.
- [ ] 4.2 G2 home: Today (3 sites) and attack mode (3 sites, 64 dp). Verify: `:features:home:ui:testDebugUnitTest` green.
- [ ] 4.3 G3 medication: Edit medication, Log medication (normal and dark room), Medications. Verify:
  `:features:medication:ui:testDebugUnitTest` green, `EditMedicationProblemFeedbackTest` and
  `FirstMedicationProblemFeedbackTest` unchanged and green.
- [ ] 4.4 G4 settings: Settings, Settings pages, Delete all data. Verify: `:features:settings:ui:testDebugUnitTest` green,
  `DeleteKeyboardTest` unchanged and green.
- [ ] 4.5 G5: History, Insights, Report, Your data and its 2 dialogs, Reminders and its dialog, Paywall, Onboarding. Verify:
  each module's `testDebugUnitTest` green.
- [ ] 4.6 G6 `designsystem` pickers (3 sites). Verify: `:designsystem:testDebugUnitTest` green, `KeyboardRoomTest`,
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
  with Back). Verify: walk logs `6.1-*.txt` with stop counts; the same walk on `develop` loops or skips (control).
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
  walk; Switch Access scan and scroll; Tab. Verify: `docs/qa/fix-talkback-scroll-order.md` with each row, and what it does
  not prove.
- [ ] 8.3 Guards on the candidate: #238 KR rows, #245 SW rows that touch TalkBack (SW5, SW9), #239 V1. Verify: same file.
- [ ] 8.4 If 1.2 chose option 3: `.maestro/` flows that select chips by text still pass or are updated by QA. Verify: the
  affected flows' run output.

## 9. Close-out (`product-manager`)

- [ ] 9.1 D193 result line (QA verdict, pages left out if any, what stays unproven), spec sync and archive of
  `fix-talkback-scroll-order`. Verify: `openspec validate --all --strict` exit 0 after archive; `openspec list` shows no
  active change.
