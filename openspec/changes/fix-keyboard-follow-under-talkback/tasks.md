# Tasks

Base: `bugfix/259-talkback-focus-bar-aside` (worktree `migraine-app-259`) from `origin/develop` 4d141abe. Release 1.3.3, its
only content. Write `Refs #259` in every commit and in the PR into `develop`, never a closing keyword: QA retests and closes
#259. BRD D194, EC-UX-39, EC-UX-83 to EC-UX-87. Design: this change's `design.md` (D1 to D5, motion C1 to C6, rows T1 to T7
and M1 to M5). Source: eng-259's prototype 2, `migrely-work/259/proto2.diff`.

Ownership:
- `product-manager`: 0 (done) and 8;
- `android-engineer`: 1 (done) to 5;
- `code-reviewer`: 6;
- `qa-engineer`: 7 (emulator-5580 API 36, then emulator-5582 API 26, one at a time).

No `medical-reviewer`: no string and no health sentence changes. Layer: `designsystem` only; no domain, presentation, data,
DI, navigation or screen change. Every new test is shown red on `develop` 4d141abe (or on the code before its step), then
green; each mutation must turn its test red again. Logs go to `migrely-work/259/eng/`. Under load use
`nice -n 19 ./gradlew --max-workers=2 …`; device tasks need `ANDROID_SERIAL=emulator-5570`.

## 0. Plan (`product-manager`)

- [x] 0.1 BRD D194, EC-UX-83 to EC-UX-87, EC-UX-39 pointer, section 13 row 58; proposal, design (motion verdict C1 to C6
  folded in), `app-shell` delta, tasks. Verify: `openspec validate fix-keyboard-follow-under-talkback --strict` and
  `openspec validate --all --strict` exit 0.

## 1. Red tests (`android-engineer`, `designsystem`)

- [x] 1.1 `designsystem/src/testDebug/.../input/KeyboardFollowTest.kt` (2bf5857c) replays the order logged on 5570. Red on
  develop: "Compose's own layout before the follow does not end it" (brought 0 of 1) and "The following goes on through the
  next frames" (0 of 2). Controls green: "With only the frame's layout the line is brought into view" and "A scroll away while
  the keyboard holds still still ends the following"; the never-drop mutant fails the scroll-away control. Verify:
  `./gradlew :designsystem:testDebugUnitTest --tests '*KeyboardFollowTest'` shows 2 failed, 2 passed on 2bf5857c.

## 2. The follow survives Compose's layout (`designsystem`)

- [ ] 2.1 `KeyboardFollow`: keep `keyboardFollowed`, set by the collector; `onPositioned` records only when the height equals
  both the last layout's and the followed one (design D1). KDoc names #259. Verify: the 1.1 tests, 4 of 4 green; the
  never-drop mutant still fails the scroll-away control (log in `migrely-work/259/eng/2.1-mutant.log`).

## 3. The line moves in step with the keyboard (`designsystem`)

- [ ] 3.1 `TypingSpace.scrollToShow(coordinates, area)` (design D2): unit tests first, red without it, for a line that shows
  (0), below the visible part, above it, taller than it (its top first), an unknown page (null) and detached coordinates
  (null). Verify: `./gradlew :designsystem:testDebugUnitTest --tests '*TypingSpace*'` green, and red with the function
  returning 0.
- [ ] 3.2 `CursorFollowingTextField`: inside a typing space, on keyboard growth while following, hand `scrollToShow` of the
  caret's line box to the page in the same turn (design D3); outside a typing space keep `bringIntoView`. Robolectric test:
  a note in a `verticalScroll` inside a `TypingSpace`, IME insets driven by hand in steps (as `KeyboardRoomTest` does), with
  an accessibility-like extra layout before the collector; after each step the line is inside the visible part. Red on
  develop. Verify: `./gradlew :designsystem:testDebugUnitTest` green; the test red with the old bring restored.
- [ ] 3.3 `SingleLineFieldInView`: the same, with the input line plus `marginAround` (D192 R2), and its placed coordinates
  kept outside state so placing does not recompose. Same Robolectric shape. Verify: as 3.2.
- [ ] 3.4 Motion at the unit level (C1 to C3): with insets stepped 0, 30, 70, 100% and the bar stepping aside in the second
  step, the page's scroll delta per step is never above the keyboard's delta plus 1 px, never negative, and zero after the
  last step; a skipped step never produces a leap above the summed keyboard delta. Verify: the frame test green; a mutant
  that computes the distance from positions taken before the bar shrank fails C2 (log `migrely-work/259/eng/3.4-mutant.log`).
- [ ] 3.5 Fallback and guards: a following field with no typing space is brought into view as on develop; an ancestor that
  consumes post-scroll (not the page) does not take the distance or the test shows what it takes; a drag inside the field
  scrolls the page as on develop. Verify: tests green; the fallback test red when the null branch scrolls.
- [ ] 3.6 Guards unchanged: `KeyboardRoomTest`, the screens' keyboard-room tests, D192's bar and margin tests (including
  `TypedLineMarginsTest`) pass without edits. Verify: `git diff 4d141abe -- '*Test.kt'` shows only added tests.

## 4. Gate (`android-engineer`)

- [ ] 4.1 Full gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt
  lint 2>&1 | tee migrely-work/259/eng/gate.log` exits 0; `:app:checkReleasePermissions` unchanged from 1.3.1.

## 5. Device walk on emulator-5570 (`android-engineer`)

API 36, Gboard, 200% font, landscape; window 360 dp and 411 dp tall. Restore font, rotation, animator scales, TalkBack and
window size afterwards. One JSON row per run in `migrely-work/259/rows.jsonl`.

- [ ] 5.1 T1, T4: TalkBack double tap on the note at 360 dp (6 runs) and 411 dp (2 runs): focus still on the note after 3 s
  (TalkBack VERBOSE log), line 112/112. Verify: rows with `focus_kept` true in every run.
- [ ] 5.2 T2: a migraine with impact 2 recorded, and one with none; TalkBack opens the note, one more double tap. Verify: a
  database read before and after (`adb shell run-as … sqlite3` or the debug export) unchanged in every run.
- [ ] 5.3 T3: Log medication (usual and dark room), Edit medication (name, dose), Delete all data, TalkBack double tap, at
  360 and 411 dp, 2 runs each: focus kept, no intake, medication or deletion written. Verify: rows per screen.
- [ ] 5.4 T5: TalkBack off, 360 dp, bar aside: line 112/112 on the four screens. Verify: layout dump bounds.
- [ ] 5.5 T6 (#238): close by Back, hide key, Done; TalkBack on and off: 0 `TYPE_VIEW_SCROLLED` from the page, scroll
  position 0 px (bar height where aside), focus on the field. Verify: rows and TalkBack log lines.
- [ ] 5.6 T7: Remove animations with TalkBack on, 360 dp: focus kept, one-step move. Verify: rows.
- [ ] 5.7 M1: 60 fps `screenrecord` of the note's keyboard opening at 360 dp, TalkBack off and on, candidate and develop:
  frames at keyboard start, middle, last and +500 ms with the cursor line's top y and the keyboard's top y. Pass C1 and C2 on
  every frame, last equals +500 ms. Negative control: develop's frame +500 ms differs from its last frame. Verify: a frame
  table in `migrely-work/259/eng/m1.md`.
- [ ] 5.8 M2 to M4: final frames against develop at 411 and 360 dp (within 1 px; SW13 band if reached); the note at the page
  end and Delete all data in the dark theme, no stretch or glow; Log medication from attack mode, no lighter frame. Verify:
  shots in `migrely-work/259/shots/` and a short table.

## 6. Review (`code-reviewer`)

- [ ] 6.1 Review the diff against `develop` 4d141abe, with design D3's mechanism as an explicit question (side channel versus
  an explicit `ScrollState`); fixes go back to 2 to 5 and the gate reruns. Verify: review verdict recorded in the PR.

## 7. Independent retest (`qa-engineer`)

- [ ] 7.1 emulator-5580, API 36: rows T1 to T7 and M1 to M4 on the debug build of the candidate SHA, with v1.3.1 as the
  control (focus kept there) and v1.3.2 (focus jumps) as the negative control.
- [ ] 7.2 The R8 release build of the same SHA: T1, T2, T6 at 360 dp.
- [ ] 7.3 emulator-5582, API 26 (AOSP keyboard): M5, T1, T6 at 360 and 411 dp. Cases QA cannot reach (hardware or floating
  keyboard, split screen) are recorded as not reachable, not as a pass. Verify: QA's Go/No-Go record names the SHA, each row
  and what it does not prove; QA closes #259 after the retest.

## 8. Archive (`product-manager`)

- [ ] 8.1 After review and QA: record results in D194's evidence, archive the change into `openspec/specs/app-shell`; then
  `android-engineer` opens the PR into `develop` with `Refs #259`. Verify: `openspec archive fix-keyboard-follow-under-talkback --yes` and
  `openspec validate --all --strict` exit 0.
