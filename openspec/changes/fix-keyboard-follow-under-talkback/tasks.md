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

- [x] 2.1 `KeyboardFollow`: keep `keyboardFollowed`, set by the collector; `onPositioned` records only when the height equals
  both the last layout's and the followed one (design D1). KDoc names #259. Verify: the 1.1 tests, 4 of 4 green; the
  never-drop mutant still fails the scroll-away control (log in `migrely-work/259/eng/2.1-mutant.log`).
  Done 89548bb3. `KeyboardFollowTest` 4 of 4 green (2 red on 2bf5857c); the never-drop mutant fails only the scroll-away
  control, "expected:<1> but was:<2>" (`eng/2.1-mutant.log`).

## 3. The line moves in step with the keyboard (`designsystem`)

- [x] 3.1 `TypingSpace.scrollToShow(coordinates, area)` (design D2): unit tests first, red without it, for a line that shows
  (0), below the visible part, above it, taller than it (its top first), an unknown page (null) and detached coordinates
  (null). Verify: `./gradlew :designsystem:testDebugUnitTest --tests '*TypingSpace*'` green, and red with the function
  returning 0.
  Done 232a615c. `TypingSpaceScrollToShowTest` 6 of 6 green; returning 0 fails 5 of 6 (the inside cell is 0 by definition)
  (`eng/3.1-mutant.log`).
  Since 4d1d0430 `scrollToShow` is a pure function of viewport numbers and `TypingSpaceScrollToShowTest` has 5 `@Test`
  (inside, below, above, taller, exactly as tall); the detached-field and unknown-page cases moved to the page's own
  layout pass (cr-259 F4).
- [x] 3.2 `CursorFollowingTextField`: inside a typing space, on keyboard growth while following, hand `scrollToShow` of the
  caret's line box to the page in the same turn (design D3); outside a typing space keep `bringIntoView`. Robolectric test:
  a note in a `verticalScroll` inside a `TypingSpace`, IME insets driven by hand in steps (as `KeyboardRoomTest` does), with
  an accessibility-like extra layout before the collector; after each step the line is inside the visible part. Red on
  develop. Verify: `./gradlew :designsystem:testDebugUnitTest` green; the test red with the old bring restored.
  Done d8524f24 (`InStepScroll`, `TypingSpace.visibleHeight`) and 44f9c6b7. `KeyboardFollowFramesTest` runs each step as on
  5570: the frame's layout, a second layout at the same height, then the follow, with effects on a `StandardTestDispatcher`
  and no clock between (Robolectric's default runs the collector inside the inset dispatch, before any layout). 11 green; on
  develop 4d141abe 5 fail, with the old animated bring 5 fail (`eng/3.2-red-*.log`). `show` reports false where the page
  takes less than the distance (a scrollable takes no raw scroll while another scroll of it runs): the old bring then.
  Superseded by the explicit route (4d1d0430, design D3 decision 2026-11-20): `TypingSpace.page(scrollState)` scrolls the
  page's `ScrollState` in its own placement while the keyboard grows, before the content is placed; fields register a
  `KeptField` and keep develop's bring in the collector; `InStepScroll` removed. `KeyboardFollowFramesTest` reads each
  step in the frame's own layout, before any collector: 12 green; on edae56a4 (with a `page` shim) 4 fail, among them a
  single-line field wholly in view (130 px under the keyboard) and a note wholly in view (107 px); mutants: no in-pass
  scroll 7 red, previous-layout positions 7 red (C2), follow state ignored 1 red (scroll-away cell)
  (`eng/r2-*.log`). designsystem 287, attack 128, medication 156, settings 43 green.
- [x] 3.3 `SingleLineFieldInView`: the same, with the input line plus `marginAround` (D192 R2), and its placed coordinates
  kept outside state so placing does not recompose. Same Robolectric shape. Verify: as 3.2.
  Done 44f9c6b7, with ux U1's three areas (the largest that fits) rather than the input line alone, so the rest position is
  develop's (C4). Limit: a field wholly in view before a shrink is Compose's own focused child; the page animates it back
  itself and takes no raw scroll meanwhile, so the follow falls back to the old bring. A single-line field is wholly in view
  after its focus bring, so in practice it moves in step only while cut (cell "cut at the bottom ... first frame", red with
  develop's field, `eng/3.3-red-oldbring.log`) and otherwise as on develop (cell "wholly in view ... brought back by the page
  itself"). The note at 360 dp never fits whole and stays in step. For code-reviewer (design D3).
- [x] 3.4 Motion at the unit level (C1 to C3): with insets stepped 0, 30, 70, 100% and the bar stepping aside in the second
  step, the page's scroll delta per step is never above the keyboard's delta plus 1 px, never negative, and zero after the
  last step; a skipped step never produces a leap above the summed keyboard delta. Verify: the frame test green; a mutant
  that computes the distance from positions taken before the bar shrank fails C2 (log `migrely-work/259/eng/3.4-mutant.log`).
  Done (frame test). Steps 0, 30, 70, 100%: scroll per step within the keyboard's step + 1 px and never negative; where it
  scrolls, the line's bottom plus its margin sits at the part's bottom; nothing moves 1 s after the last step (old bring: the
  spring moves 408 px after); one 100% step stays within 628 px. The bar shrinks with the keyboard in every step (its own
  rule, R6), not in one. Mutant with page positions from the previous step fails C2, "expected:<507.0> but was:<429.0>"
  (`eng/3.4-mutant.log`).
- [x] 3.5 Fallback and guards: a following field with no typing space is brought into view as on develop; an ancestor that
  consumes post-scroll (not the page) does not take the distance or the test shows what it takes; a drag inside the field
  scrolls the page as on develop. Verify: tests green; the fallback test red when the null branch scrolls.
  Done. Outside a typing space: no move in the keyboard's turn, then develop's bring (red on develop, which drops the
  follow; red when the fallback does nothing, `eng/3.5-mutant-fallback.log`). An ancestor between the note and the page sees
  the distance and leaves it to the page. A drag that starts on the note scrolls the page: green, but a mutant that makes
  the field's connection swallow drags cannot reach it (the page's own scrollable handles that drag, so the field's
  connection is not in its chain); recorded as a guard, not a killed mutant. Per screen (9eb83586): `PageScrollParent`
  checks no other scroll or nested-scroll node lies between each field and its page on Attack details, Log and Edit
  medication, Delete all data (4 tests); a nested scroll added inside Attack details' page fails it
  (`eng/3.5-screens-mutant.log`).
  Reopen on a still-focused field (device row u3, team-lead decision (a'); cr-259 round 2; ux HOLD): a focused field
  that is not followed and that a keyboard frame is about to hide (`keyboardHides`: it showed, at least 1 px; the
  keyboard takes some of it; less than its line would be left; it does not already cover the whole visible part) is
  held in that layout pass: the page keeps min(the line with its clearance, what showed), the line being the caret line
  plus up to 21 px (8 dp) each side, so the field moves with the keyboard and no further and stays in the accessibility tree. No
  state between frames (the N1 latch cannot return); no hand-over (team-lead accepted: HOLD with the hand-over brought
  the line back in frame 2, 192 to 572 px, `eng/r7-hold-handover.log`). The first typed character brings the caret
  line through the typing follow (BRD D180 (2)). 1 px: Compose rounds clipped bounds to whole pixels
  (`roundToIntRect`) and prunes empty ones. NoteKeyboardTest's reopen cell unchanged and green.
  Cells (`KeyboardFollowFramesTest`): H1 x4 (60 px caret line 1; 60 and 350 px caret at end; one-step), H2 x6 (60, 150,
  350 px, caret on line 1 and at the end of 4 lines), H3 (one way, settled within 15 frames, cut 0, focus and text
  kept), H4 (an ordinary opening, caret at end, line in each frame), R1 and R1c (a scroll away after a hold or a normal
  opening stays put on symbols). Negative control (`eng/r7-negative-control-d7f985ac.log`): d7f985ac's BRING reds the
  four H1 cells (the line moved 177, 513, 397 and 1078 px against 81, 81, 83 and 821 allowed). Mutants
  (`eng/r7-mut-*.log`): H1 (hold doubled) reds 2 H1 cells; H2 (hold 1 px) reds the 6 H2 cells and H3; the shown-before
  guard removed reds only the pure sub-pixel cell (the "takes some" guard already excludes a wholly hidden field); H3
  (no typing bring) reds H3's final-cut check; H4 (hold for followed fields too) reds H4 and 15 ordinary cells; B (no
  `onBringing`) reds all four F1 cells, the one-step ones included (N4 closed under HOLD).
  cr-259 round 3 S4-1 and S4-3 (`eng/r8-mut-*.log`): a cell at 350 px with the caret at the end checks the hold keeps no
  more than the line with its clearance; the cap dropped reds it (177 px shown against 154). A field covering the whole
  visible part is not held (split screen); the pure cell is red without that guard.
  Device round 4 on emulator-5570 (`eng/r9-*.log`, rows `u-`/`dg-` in `rows.jsonl`): 43fb9202 failed u3 in 4 of 6
  valid rows (note wholly hidden, focus to an impact radio, impact written; 2 more were ANR at load 26, void). A diag
  build's log showed why: under TalkBack, Gboard's first reopen frame is 0 to 270 or 0 to 422 px, taking more of the
  note than showed (87 to 89 px) in one frame; the hold, `held - shownInPage(now)` with the clamp at 0, scrolled only
  89 px and left the note's top 50 px below the edge, and the next frame saw nothing showing. The hold is now
  `fieldTop + held - visible` (its top `held` px above the bottom; equal to before while any part shows). Eight cells
  replay those frames (0 to 270 and 0 to 422 px; 60 and 89 px shown; caret on line 1 and at the end), each with H1 and
  H2: all red on 43fb9202 (0 px shown at 270 or 422 px), green now; the clamped form put back reds exactly these 8.
  Numbers (Robolectric, 360 dp at 200%, keyboard steps 62 to 63 px, bar 19 to 20 px; `eng/r7-hold.log`): every held
  frame moves the line 61 to 63 px, the note keeps 59 to 60, 150 or 153 to 154 px showing, 0 px after the keyboard. H3
  peaks, logged not capped (ux option 1, `eng/r7-h3-measure.log`): caret line 1 at 60 px, 26 px over 12 frames; caret
  at end at 60, 150, 350 px, 100, 81, 79 px over 13 frames; final cut 0 in each. K2 (Remove animations: the typing
  bring snaps in one step, up to about 430 px) is accepted; it is logged on the device row T7, not fixed.
- [x] 3.6 Guards unchanged: `KeyboardRoomTest`, the screens' keyboard-room tests, D192's bar and margin tests (including
  `TypedLineMarginsTest`) pass without edits. Verify: `git diff 4d141abe -- '*Test.kt'` shows only added tests.
  Done. `git diff 4d141abe --numstat -- '*Test.kt'`: 6 new files, 0 lines removed; no existing test edited.
  Since 4d1d0430/3806e56e (one page API, cr-259 F3): `TypedLineMarginsTest`, `TypingSpaceBarTest`, `TypingSpaceCloseTest`
  and `TypingSpaceFocusTest` changed only their harness from `pageModifier` + `verticalScroll` to `scrollingPage(state)`;
  no assertion or expected value changed.

## 4. Gate (`android-engineer`)

- [x] 4.1 Full gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt
  lint 2>&1 | tee migrely-work/259/eng/gate.log` exits 0; `:app:checkReleasePermissions` unchanged from 1.3.1.
  Done at 9eb83586: `assembleDebug test ktlintCheck detekt lint` exit 0 (BUILD SUCCESSFUL, 2142 tasks), 3510 tests in 53
  modules, 0 failed (`eng/gate.log`); `:app:checkReleasePermissions` exit 0; merged release manifest: ACCESS_NETWORK_STATE,
  POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, WAKE_LOCK, BILLING, the dynamic receiver permission; no INTERNET.
  Rerun at 4d1d0430: exit 0, 3510 tests in 53 modules, 0 failed; permissions exit 0, same set, no INTERNET.
  Final head (HOLD with cr-259 round 3 S4-1 and S4-3, `eng/r8-gate.log`): exit 0, 3539 tests in 53 modules, 0 failed;
  `:app:checkReleasePermissions` exit 0, no INTERNET.

## 5. Device walk on emulator-5570 (`android-engineer`)

API 36, Gboard, 200% font, landscape; window 360 dp and 411 dp tall. Restore font, rotation, animator scales, TalkBack and
window size afterwards. One JSON row per run in `migrely-work/259/rows.jsonl`.

Final head d7aa097e (debug "cand8", sha256 787d7a3f…; `python3 migrely-work/259/lib/r4sum.py`; TalkBack on, 360 dp). A
row counts only if the 1-minute load was under 8 at its start and end and at least 3 GB of disk was free. U3 6 valid rows,
K1 3, T1 17, F2 page-end 2 (mid-page 2, both void): 0 focus moves, 0 writes but the typed "x" in K1; details in BRD D194
(10) and design "Engineer's results on 5570". Controls: 43fb9202 (diag build 4 rows: 3 moves, 3 impact writes) and v1.3.1
(3 rows: 3 moves to "Back. Button", 0 writes). Lead decision 2026-11-20 (to speed up 1.3.3, as the owner asked): the
engineer's rows not run on this head go to QA, and the tasks below stay unticked until QA's result.

- [x] 5.1 T1, T4: TalkBack double tap on the note at 360 dp (6 runs) and 411 dp (2 runs): focus still on the note after 3 s
  (TalkBack VERBOSE log), line 112/112. Verify: rows with `focus_kept` true in every run.
  NOT MET (candidate 9eb83586 debug, sha256 50d8e381…; 5570, 2026-11-20). 360 dp: 17 TalkBack openings of the note,
  focus kept in 14; in 3 (`m1c2-`, `m3b-`, `w7-tb2-det-360`, two with the recorder running, one without) TalkBack moved
  it to the "Notes" heading (`sourceAction=MANUAL_SCROLL`, `currentNode=null`: a `TYPE_VIEW_SCROLLED` reached it while the
  note was still out of the tree after the keyboard's first frame). No control took focus, nothing was written (17 of 17),
  the line ended in view. 411 dp: 2 of 2 kept, 112/112. Develop control: jumps to an impact radio in every 360 dp row
  (`d1`, `d4`, `m1d`, `m1d2`, `d3-tbclose`). Back to design: the in-step move still runs a turn after the frame that hid
  the note.
  Explicit route (4d1d0430 debug, sha256 e86fdac9…): **360 dp 32 TalkBack openings, 0 focus moves**, 0 writes, line
  112/112: 13 plain (`v1`), 9 with the recorder running (`v2` ×5, `v8` ×4), 4 with the note wholly in view before the
  keyboard (`v8`, reference shots `shots/v8-refs.png`), 3 more page-end starts (`v6`), 2 Remove animations (`v4`), T2
  and T6 openings. 411 dp 2 of 2. Previous route (edae56a4): 3 of 17 moved.
  Final head d7aa097e: T1 at 360 dp 17 valid rows, 0 moves, 0 writes, 112/112. T4 (411 dp) handed to QA 7.1 (lead
  decision 2026-11-20).
- [x] 5.2 T2: a migraine with impact 2 recorded, and one with none; TalkBack opens the note, one more double tap. Verify: a
  database read before and after (`adb shell run-as … sqlite3` or the debug export) unchanged in every run.
  Partly. Candidate, impact 2 recorded, TalkBack opens the note, one more double tap: attack row identical before and
  after (`w7-tb2i-det-360` n0: impact 2, updated-at 1795130902048, both). Two more preset rows (`w5`) unchanged, pre-state
  not captured. Develop control: `d4` row 2 cleared a recorded 2 (2 → empty). Develop with the preset (`d5`) did not jump:
  the preset leaves the note in a position the first keyboard frame does not hide, so that control cannot fail.
  Explicit route: impact 2 recorded, attack row identical after the opening and one more double tap, in the preset
  geometry and in the plain T1 geometry where develop cleared it (`v1-tb2i`, last `v1-tb2`).
  Final head d7aa097e: the impact preset row (`t-tb2i`) was void (load 50 at its end); T2's impact preset handed to QA
  7.1 (lead decision 2026-11-20).
- [ ] 5.3 T3: Log medication (usual and dark room), Edit medication (name, dose), Delete all data, TalkBack double tap, at
  360 and 411 dp, 2 runs each: focus kept, no intake, medication or deletion written. Verify: rows per screen.
  Partly. TalkBack double tap, candidate: Log medication 360 ×2, 411 ×2; Delete all data 360 ×3, 411 ×2; Edit
  medication name 360 ×2, 411 ×1, dose 360 ×2, 411 ×1: focus kept and nothing written in all 15. Log medication in the dark
  room was run TalkBack off only (`m4`). Develop (earlier, `d2`, `d3`): no jump on these screens either (latent).
  Explicit route, one opening each at 360 dp: Log medication, Delete all data, Edit medication name and dose: focus kept,
  nothing written (`v5`).
  Handed to QA 7.1 (lead decision 2026-11-20): not run on the final head d7aa097e.
- [ ] 5.4 T5: TalkBack off, 360 dp, bar aside: line 112/112 on the four screens. Verify: layout dump bounds.
  Partly. TalkBack off, Gboard's full keyboard, bar aside, 112/112: note (`m1c-off-det-360`), Log medication name
  (`w2-off-log-360`), Edit medication name (`w6-off-edit-360`), Log medication in the dark room (`m4`). Delete all data not
  obtained: 5570's Gboard mostly showed its compact physical-keyboard bar with TalkBack off (and `dumpsys` reported a
  keyboard frame while none was drawn); only rows where the bar measurably stepped aside are counted.
  Explicit route: not obtained on 5570 (Gboard's compact bar with TalkBack off in every attempt, including a TalkBack
  warm-up, `v5`, `v7`). The in-pass follow has no TalkBack branch; the frame tests cover it without an accessibility
  service.
  Handed to QA 7.4 (lead decision 2026-11-20): not run on the final head d7aa097e.
- [ ] 5.5 T6 (#238): close by Back, hide key, Done; TalkBack on and off: 0 `TYPE_VIEW_SCROLLED` from the page, scroll
  position 0 px (bar height where aside), focus on the field. Verify: rows and TalkBack log lines.
  Partly. TalkBack on, Back (`w1-tbclose-det-360`): 0 `TYPE_VIEW_SCROLLED`, no focus move, "keyboard hidden". TalkBack
  off, Back: 0 px on the candidate only with the compact bar (bar not aside); with the full keyboard and the bar aside, 0 px
  on prototype 2 (`p4-close-det-360`). Hide key and Done with the full keyboard: not obtained (compact bar).
  Explicit route: TalkBack + Back (`v1-tbclose`): 0 `TYPE_VIEW_SCROLLED`, no focus move, "keyboard hidden". TalkBack off:
  not obtained with the full keyboard.
  Handed to QA 7.1 (T6, TalkBack on) and 7.4 (TalkBack off, hide key, Done) (lead decision 2026-11-20): not run on the
  final head d7aa097e.
- [x] 5.6 T7: Remove animations with TalkBack on, 360 dp: focus kept, one-step move. Verify: rows.
  Done. Remove animations (all three scales 0, restored), TalkBack, 360 dp: 2 of 2 focus kept, 112/112, nothing
  written (`w3`).
  Explicit route: 2 of 2 kept, 112/112 (`v4`).
  Final head d7aa097e: T7 with K2 handed to QA 7.1 (lead decision 2026-11-20).
- [ ] 5.7 M1: 60 fps `screenrecord` of the note's keyboard opening at 360 dp, TalkBack off and on, candidate and develop:
  frames at keyboard start, middle, last and +500 ms with the cursor line's top y and the keyboard's top y. Pass C1 and C2 on
  every frame, last equals +500 ms. Negative control: develop's frame +500 ms differs from its last frame. Verify: a frame
  table in `migrely-work/259/eng/m1.md`.
  Partly (`migrely-work/259/eng/m1.md`). 5570's compositor produced 2 to 3 frames per opening (about 5 fps), so a
  60 fps per-frame C1 check is not possible here. Candidate, TalkBack off: field under the keyboard for one captured frame
  (the keyboard jumped 422 px in one frame), in place the next; no C1 or C2 break where seen. Develop with TalkBack: the
  field stays under the keyboard after it settles. Develop's TalkBack-off spring (C3 control) not recorded (compact bar);
  shown at unit level (408 px after the keyboard settled).
  Explicit route (recorded, TalkBack on): no captured frame with the field hidden, including a 944 → 317 px keyboard
  jump in one captured frame; no C1 or C2 break; C3 settled where the note started cut (`v2`). Where the note starts
  wholly in view (`v8`), Compose's own focused-child scroll still moves it about 24 px after the keyboard settles, to
  align the field's top (the field is taller than the 255 px left): line in view, focus kept, a C3 tail for ux.
  Handed to QA 7.5 (lead decision 2026-11-20): not run on the final head d7aa097e; 5570 cannot measure per frame. Run
  there instead: F2 at the page end with animations on and off, 2 valid rows, 0 px settle at +0.5 s and +1.0 s, the
  negative control saw a 20 px shift; mid-page 2 rows, both void.
- [ ] 5.8 M2 to M4: final frames against develop at 411 and 360 dp (within 1 px; SW13 band if reached); the note at the page
  end and Delete all data in the dark theme, no stretch or glow; Log medication from attack mode, no lighter frame. Verify:
  shots in `migrely-work/259/shots/` and a short table.
  Partly. M2: 411 dp final caret 278..390 on candidate and develop (0 px); 360 dp candidate 185..297, as QA's develop row
  x1-base on 5580. M3 (TalkBack, recorded): the note at the page's end and Delete all data, dark: no stretch or glow in the
  frames (`shots/m3/sheet.png`, `shots/m3c/sheet.png`); at the page's end the note was wholly in view before the keyboard,
  and the page's own animation moved it 71 px after the keyboard settled (the 3.3 limit, as on develop). M4: the band's
  colour is the dark room's (4, 6, 9) in all 61 frames, no lighter frame.
  Explicit route: rest caret 185..297 at 360 dp (19 rows) and 278..390 at 411 dp (2), the same as edae56a4.
  Handed to QA 7.1 (lead decision 2026-11-20): M2 to M4 not run on the final head d7aa097e (its T1 rows rest at
  185..297, as before).

## 6. Review (`code-reviewer`)

- [x] 6.1 Review the diff against `develop` 4d141abe, with design D3's mechanism as an explicit question (side channel versus
  an explicit `ScrollState`); fixes go back to 2 to 5 and the gate reruns. Verify: review verdict recorded in the PR.
  cr-259 round 1 (CHANGES REQUESTED on dd0210b7) fixed on the branch, see eng-259's report: F1 the follow holds while the
  field's own bring runs; F2 the page lands where Compose's focused-child scroll leaves nothing to do; F3 one
  `TypingSpace.scrollingPage(state)`, an Attack details frame test, `PageScrollParent` tests removed; F4 this row and 3.1;
  F5 cells for a keyboard changing height and for moving between fields.
  cr-259 round 2 (CHANGES REQUESTED on b8a8a871; F1 to F4 closed, F5 rotation to QA): N1 latch replaced by the hand-over,
  N3 rescue from the first frame leaving less than a line, N4 one-step cells at 16 ms, see 3.5. N3 then ruled HOLD by
  ux (the rescue keeps what showed instead of bringing the line), hand-over dropped, H3 amended (one way, within 15
  frames, cut 0); see 3.5. cr-259 round 3 APPROVES the code at 9e3ed175; its S4-1 (a cell for the cap at the line) and
  S4-3 (no hold for a field covering the visible part) are added after it, see 3.5. Rounds 3 and 4 approved the earlier heads.
  cr-259 round 5 APPROVES d7aa097e (2026-11-20 11:52, diff 43fb9202..d7aa097e): the hold `fieldTop + held - visible`
  equals the earlier form while any part of the field shows, cannot over-scroll, and keeps H1.

## 7. Independent retest (`qa-engineer`)

- [ ] 7.1 emulator-5580, API 36: rows T1 to T7 and M1 to M4 on the debug build of the candidate SHA, with v1.3.1 as the
  control (focus kept there) and v1.3.2 (focus jumps) as the negative control.
- [ ] 7.2 The R8 release build of the same SHA: T1, T2, T6 and U3 (7.6) at 360 dp.
- [ ] 7.3 emulator-5582, API 26 (AOSP keyboard): M5, T1, T6 and U3 (the hold in one step) at 360 and 411 dp. Cases QA cannot reach (hardware or floating
  keyboard, split screen) are recorded as not reachable, not as a pass. Verify: QA's Go/No-Go record names the SHA, each row
  and what it does not prove; QA closes #259 after the retest.
- [ ] 7.4 T5 and TalkBack-off T6 on emulator-5580 (not obtained on 5570, whose Gboard showed only its compact bar with
  TalkBack off): the four screens at 360 dp with the bar aside, line 112/112; close by Back, the hide key and Done, 0 px
  and 0 `TYPE_VIEW_SCROLLED`. Verify: rows in QA's folder, or "not reachable" with the reason, never a pass.
- [ ] 7.5 Motion at 60 fps (design D4, general evidence): the note's opening at 360 dp, TalkBack on, from a cut start and
  from a page-end start (the note wholly in view at the page's end), captured with `screenrecord`, with the cursor line's
  top y and the keyboard's top y per frame from the keyboard's start to +500 ms. Pass: C1 and C2 per frame, and the +500 ms
  frame equals the last frame from both starts (no settle, F2). Verify: a frame table in QA's folder.
- [ ] 7.6 The reopen path U3 (design D6), independent of the engineer, 360 dp, TalkBack on. The note is focused, Back
  closes the keyboard, the page is scrolled so the note's top is near the bottom edge, and a double tap reopens the
  keyboard; then one more double tap. Run it on the candidate (at least 5 runs), on v1.3.1 (control: is production
  affected?) and on develop (negative control: hidden, focus moves, writes). Pass on the candidate: focus on the note, 0
  writes (database read before and after). H2: in every captured frame at least min(what showed before, the line with its clearance, up to 8 dp each side) is
  visible. H1: each held frame moves the line no more than the keyboard step plus the bar step plus 1 px, and 0 after the
  keyboard stops. Verify: rows and a frame table.
- [ ] 7.7 K1: U3 with the caret at the end of 4 lines and 60 px shown; type one character, then double tap. Pass: focus on
  the note, 0 writes, the caret line whole afterwards (H3 (c)). Log the typing bring's peak px per frame; do not cap it.
  Verify: rows.
- [ ] 7.8 Rotation with the keyboard up (cr-259 F5, not reachable in unit tests), TalkBack on, on the note and on Edit
  medication's name: rotate to portrait and back. Pass: the bar is decided again (D192 R5), the line shows, focus stays on
  the field, nothing is written. Verify: rows.
- [ ] 7.9 Go/No-Go "does not prove" section names these limits verbatim:
  - H5: "On a reopen with focus kept, the caret line may stay cut until the first keystroke."
  - K3: "The typing bring (D180 (2), unchanged) can move up to about 100 px per frame at its peak, eased, after a rescue or a
    drag."
  - K2: with Remove animations that bring is one step of up to about 430 px.

  Also: real phones and keyboards other than Gboard and AOSP. Verify: the record names follow-up #260.

## 8. Archive (`product-manager`)

- [ ] 8.1 After review and QA: record results in D194's evidence, archive the change into `openspec/specs/app-shell`; then
  `android-engineer` opens the PR into `develop` with `Refs #259`. Verify: `openspec archive fix-keyboard-follow-under-talkback --yes` and
  `openspec validate --all --strict` exit 0.
