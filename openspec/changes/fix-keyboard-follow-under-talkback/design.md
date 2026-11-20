# Design

Release 1.3.3 (its only content), issue #259 (S1). BRD D194, EC-UX-39, EC-UX-83 to EC-UX-87; D180 (2), (5), D189, D192.
Cause and prototype: eng-259 (`migrely-work/259/`: `rows.jsonl`, `logs/`, `raw/`, `shots/`, `proto2.diff`); QA rows x1 to x7
in `migrely-work/qa-245c/`. Fix chosen by `team-lead`: prototype 2 first, then the explicit route of D3 (2026-11-20).
Motion verdict: ux-259, accepted by `team-lead` with conditions C1 to C6 and the C3 limit L1 to L5 (2026-11-20). This file
binds what the person sees and what QA checks; the code shape is the engineer's call within it, and `code-reviewer` judges
the mechanism.

## Context

The race, in order, with TalkBack on, at 360 dp, 200% landscape, on the note:

| Step | What happens | Effect |
|---|---|---|
| 1 | The keyboard's first frame; D192's bar steps aside | the line is fully under the keyboard |
| 2 | Compose's `semanticsChangeChecker` runs `measureAndLayout()` (only with an accessibility service on) | `onPositioned` at the new height, before the collector |
| 3 | `KeyboardFollow.onPositioned` sees an unchanged height since the last layout and a hidden line | `following = false` |
| 4 | The collector sees the keyboard grow, but following is off | nothing is brought into view |
| 5 | The note, fully hidden, is pruned from the accessibility tree | TalkBack loses its focused node |
| 6 | TalkBack's ENSURE_ON_SCREEN | focus moves to the first focusable node: an impact radio |

At 411 dp the line is never fully hidden on the first frame, so step 5 does not happen. Prototype 1 fixed step 3 only: the
animated bring then sent `TYPE_VIEW_SCROLLED` while the note was still pruned, and TalkBack re-homed focus to the "Notes"
heading in 4 of 7 rows. So two things are needed: the follow must survive step 2, and the line must not be hidden at the
moment the page reports a scroll.

## Goals and non-goals

Goals: D194 (1) to (4) on all four screens, one shared mechanism in `designsystem` (each screen hands its `ScrollState` in
one line), D192 and #238 untouched, a check for each rule that can fail.

Non-goals: a TalkBack-only path, a new bar rule, the follow while typing new lines, moving between fields, #255.

## Decisions

### D1. A layout counts only at a keyboard height the follow already acted on

`KeyboardFollow` keeps the keyboard height its collector last handled (`keyboardFollowed`). `onPositioned` records "what the
person sees" only when the height equals both the last layout's and the followed one. A layout that comes between a frame
and the collector (Compose's semantics pass, or any other) keeps the record from before. A scroll by the person while the
keyboard holds still still ends the following (D180 (5)), so the control cell in `KeyboardFollowTest` stays green and the
never-drop mutant fails it.

### D2. Inside a typing space, the page keeps the line in view within the layout pass

The page, not the field, moves the line. While the keyboard grows and the focused field follows it (D1), the typing space
scrolls the page's own `ScrollState` inside the layout pass that shrinks the visible part, before the page's content is
placed. So the line is in view in the very frame the keyboard moved, and no frame and no accessibility layout ever sees it
under the keyboard. How far: `scrollToShow(top, bottom, visible)`, a pure function of viewport numbers in px: 0 where the
area shows, the distance below or above, the area's top first where it is taller than the visible part. Which area: the
note's caret line box (with the margins D192 R2 leaves), or, for a single-line field, ux U1's three areas (the box with its
supporting line, the input line with margins, the input line), the largest that fits. That keeps develop's rest position
(C4).

### D3. Mechanism (for `code-reviewer`)

**Chosen: the explicit route (commit 4d1d0430, team-lead decision 2026-11-20).**

- `TypingSpace.page(scrollState)` replaces `pageModifier` on the four screens: Attack details, Log medication, Edit
  medication, Delete all data (one line each). It goes right before the page's `verticalScroll(scrollState)` with the same
  state. #255's `pageScroll(state)` can hand the same state in.
- In its own placement block, before it places the page content, it calls `ScrollState.dispatchRawDelta(shift)`.
  `verticalScroll` sets `maxValue` in measure and reads `value` in placement, so the content is placed at the new value in
  the same pass. Positions are read with `page.localPositionOf(field, area)` against the page's last placement, inside
  `Snapshot.withoutReadObservation`, so the page is not laid out again because they changed.
- It acts only when the keyboard grew since the page's last placement, only while the kept field follows (`KeyboardFollow
  .isFollowing`, D1), and only downward in content (`shift > 0`): a keyboard that shrinks or closes never scrolls (D189).
- A focused field registers a `KeptField` (its coordinates, whether it follows, its areas) while it has focus. It also keeps
  develop's animated `bringIntoView` in the keyboard collector, which does nothing once the line already shows. Outside a
  typing space that bring is the whole behaviour, as on develop.

**Why the first route was replaced.** The first build (`InStepScroll`, d8524f24 to edae56a4) sent the distance as a nested
post-scroll (`NestedScrollDispatcher.dispatchPostScroll`, source `SideEffect`) from the field to the page. On emulator-5570
at 360 dp it kept focus in 14 of 17 TalkBack openings: in 3, TalkBack moved focus to the "Notes" heading
(`sourceAction=MANUAL_SCROLL`, `currentNode=null`). The scroll still ran one turn after the frame that hid the note, so a
`TYPE_VIEW_SCROLLED` reached TalkBack while the note was pruned. The page also refused the post-scroll while Compose's own
`ContentInViewNode` bring was running (a scrollable takes no raw scroll while another scroll of it runs). A snap
`BringIntoViewSpec` was not built: it would still act after the layout that hides the line.

**Guards and tests.**

- `KeyboardFollowFramesTest` (12 cells) runs each step as on 5570: the frame's own layout, a second layout at the same
  height, then the collector, on a `StandardTestDispatcher`. It reads each step in the frame's own layout, before any
  collector. On edae56a4 (with a `page` shim) 4 cells fail: a single-line field wholly in view 130 px under the keyboard, the
  note wholly in view 107 px under, the note line 84 px, a cut single-line field 170 px. All 12 pass on 4d1d0430.
- Mutants: no in-pass scroll turns 7 cells red. Positions from the previous layout turn 7 red, which is ux's C2 risk. Ignoring
  the follow state turns the scroll-away cell red.
- `TypingSpaceScrollToShowTest` (6 cells; returning 0 fails 5).
- `KeyboardFollowTest` (2bf5857c; 4 of 4).
- `PageScrollParent` per screen (9eb83586): no other scroll or nested-scroll node lies between each field and its page; a
  nested scroll added inside Attack details' page fails it.
- The fallback outside a typing space (red when it does nothing).
- `KeyboardRoomTest`, the screens' keyboard-room tests and D192's bar and margin tests pass unedited.
- Gate at 4d1d0430: exit 0, 3510 tests in 53 modules, 0 failed; release permissions unchanged, no INTERNET.

**For review.** Scrolling a `ScrollState` from a placement block is a side effect inside layout. It is bounded:
- it runs once per keyboard growth step;
- it does not observe reads;
- it is a no-op when nothing grew, when nothing follows or when the line shows.

The reviewer judges whether that is acceptable against a pre-measure alternative. Any route must keep the acceptance rows
below; the spec states behaviour only.

### D4. Motion (ux-259 verdict, accepted with conditions)

One gesture, one movement: the line rides up on the keyboard's own inset curve and stops when it stops, as D192 R6's bar does
("no curve of our own"). The old spring added a lag and a slow tail during which the line sat under the keyboard. This
applies to everyone, TalkBack on or off.

- **C1 Keep to the keyboard.** In each frame the line moves no further than the keyboard's top moved in that frame, plus 1 px;
  the bar's own step counts in the frame where it steps aside. Never a catch-up leap after skipped frames.
- **C2 Only upward while opening.** No down-then-up, and no double move in a frame where both the bar and the scroll act.
  Risk (ux): a scroll computed from positions taken before the bar shrank moves twice. The page reads positions in the same
  pass that has the bar's new height; the previous-layout mutant proves the test can fail.
- **C3 Nothing moves once the keyboard has settled.** No tail. Typing a new line keeps D180 (2) (out of scope).
  **Known limit, accepted by ux-259 and team-lead for 1.3.3 (2026-11-20), not a blocker.**
  - The case: the note starts wholly in view, at the page's end, and is taller than the 255 px left.
  - What happens: Compose's own focused-child scroll moves it about 24 px (about 9 dp) upward after the keyboard settles, to
    align the field's top. It happens once per opening, about 0.15 to 0.3 s after the keyboard's last frame, next to the
    bar's own last step (bar rows 23 to 0). Develop does the same (71 px at the page's end in M3).
  - Why accepted (ux): the S1 harm is gone (32 of 32 focus kept, 0 writes, 112/112). The tail reads as the end of the same
    movement. Nothing flashes, the light does not change, and the words never leave view. Holding the S1 fix for it would
    put polish above safety.
  - Conditions. If any fails, the tail becomes a blocker:
    - **L1** It happens only when the note starts wholly in view and is taller than the space left (the page-end case), in
      no other SW cell.
    - **L2** At most one settle per opening, upward only, at most 32 px at 200%, no bounce.
    - **L3** After the settle the cursor line is still 112/112, focus stays on the field and nothing is written. The field's
      top border may go under the status bar; the line may not.
    - **L4** With Remove animations, at most a single step of up to 32 px, never a series of small steps.
    - **L5** Recorded here with these numbers, and in QA's Go/No-Go "does not prove" section.
  - Follow-up (team-lead): no refinement in 1.3.3. QA files an S4 follow-up (ux/motion, 1.3.4 or later): place the field
    where Compose would, in the same pass. QA's retest captures the page-end opening at 60 fps as evidence and as that
    follow-up's negative-control baseline (tasks 7.4, 7.5).
- **C4 The rest position is unchanged.** The final position equals develop's within 1 px in every #245 SW cell, including
  the 4 and 21 px margins at 411 dp and R2 where it applies. Only the path changes.
- **C5 No overscroll stretch or glow** from the page's scroll at its end (the note at the bottom of Attack details, Delete all
  data's word). `dispatchRawDelta` bypasses the overscroll effect.
- **C6 Unchanged:** #238's close never scrolls; bar rules R3 to R11; fields outside a typing space keep the old bring; the
  dark room and dark backgrounds get no lighter frame.

With Remove animations and on API 26 to 29 the inset arrives in one step, so the page moves in one step; at most one frame
may show the line hidden. A visible frame with the line hidden followed by a separate jump is not acceptable.

Not proven by ux: a real phone (whether a one-frame lag reads as the line chasing the keyboard), Samsung Keyboard and
SwiftKey stepwise insets, moving between fields while the keyboard is up.

### D5. What stays as it is

Shrinking and closing never scroll (only growth acts); no bottom IME inset means no follow; the bar's decision (D192 R5) and
semantics (R11) are untouched; `KeyboardRoom` (#238) is untouched; a field outside a typing space keeps `bringIntoView`; a
field wholly in view before a shrink is Compose's own focused child and is moved by the page as on develop.

## Acceptance rows (device)

Engineer on emulator-5570 (API 36, Gboard); QA independently on emulator-5580 (API 36, with v1.3.1 as the control and the R8
build) and emulator-5582 (API 26). Rows go to `migrely-work/259/` (engineer) and QA's own folder.

| Row | Setup | Pass | Control that can fail |
|---|---|---|---|
| T1 | TalkBack, double tap the note, 360 dp, 200% landscape | focus on the note after 3 s, line 112/112 | develop jumps to an impact radio |
| T2 | T1 with impact 2 recorded, then one more double tap | database impact still 2 | develop cleared a recorded 2 (`d4`) |
| T3 | T1 on Log medication (usual, dark room), Edit medication (name, dose), Delete all data, 360 and 411 dp | focus kept, nothing written | latent: report counts, not a claim |
| T4 | T1 at 411 dp | no jump | |
| T5 | TalkBack off, 360 dp, bar aside | line 112/112 | |
| T6 | #238 close (Back, hide, Done), TalkBack on and off | 0 scroll events, 0 px (bar height where aside) | a close that scrolls |
| T7 | Remove animations, TalkBack on | focus kept, one-step move, at most one hidden frame | |
| M1 | 60 fps record, note, 360 dp, TalkBack off and on, candidate and develop | C1, C2 per frame; last frame equals +500 ms | develop's spring moves after the last frame |
| M2 | Final frame against develop, 411 dp, 360 dp, SW13 band if reached | equal within 1 px | |
| M3 | Note at the page's end and Delete all data, dark | no stretch or glow | |
| M4 | Log medication from attack mode, 360 dp | no lighter frame | |
| M5 | API 26 (5582) and Remove animations | before and after frames, at most one hidden | |

### Engineer's results on 5570 (debug 4d1d0430, sha256 e86fdac9…; lead re-counted)

| Row | Result | Gap |
|---|---|---|
| T1 360 dp | 32 of 32 openings kept focus: 13 plain, 9 with the recorder under host load, 4 with the note wholly in view first, 3 page-end starts, plus the T2, T6 and T7 openings; 0 writes; 112/112 | first route: 3 of 17 moved |
| T4 411 dp | 2 of 2 kept, 112/112 | |
| T2 | impact 2 identical after the opening and one more double tap, in the plain geometry where develop cleared it | |
| T3 | Log medication, Delete all data, Edit name, Edit dose at 360 dp: kept, nothing written | one opening each; dark-room Log with TalkBack not run |
| T5 | not obtained | 5570's Gboard showed only its compact bar with TalkBack off |
| T6 | TalkBack + Back: 0 `TYPE_VIEW_SCROLLED`, no focus move | TalkBack off, hide key and Done not obtained (compact bar) |
| T7 | 2 of 2 kept, 112/112 | |
| M1 | no captured frame with the field hidden, even at a 944 → 317 px keyboard jump; no C1 or C2 break | 5570 renders about 5 fps: no 60 fps per-frame proof; C3 page-end tail, the D4 limit |
| M2 | rest caret 185..297 at 360 dp (19 rows), 278..390 at 411 dp (2), identical to develop | SW13 band not reached |
| M3, M4 | on the first route: no stretch or glow; dark room colour in all 61 frames | not re-run on 4d1d0430 |
| M5 | | QA, 5582 |

Not proven by the engineer: 60 fps motion, real phones, the R8 build, API 26, other keyboards. QA's task 7 covers R8 and
API 26.

## Risks

- **A side effect in layout (D3).** A scroll from a placement block can surprise a future maintainer, and a future screen
  that puts another scrollable between the field and the page would bypass it. Mitigation: KDoc on `TypingSpace.page`,
  `PageScrollParent` tests per screen, and the bring fallback.
- **C2 double move.** Mitigated by reading positions in the same pass; the previous-layout mutant proves the test can fail.
- **C3 tail at a page-end start** (D4): accepted as a known limit under L1 to L5; it is Compose's own focused-child scroll
  and is also on develop. If QA finds it outside L1 to L4, it becomes a blocker.
- **TalkBack off on 5570.** T5 and TalkBack-off T6 have no device row on the explicit route. The in-pass follow has no
  TalkBack branch and the frame tests cover it without a service, but QA must measure both on 5580.
- **Unordered reads.** A detached field, or one that no longer follows, is skipped and never scrolled on stale coordinates.
- Real phones and other keyboards are not proven; recorded in NOT CHECKED, not claimed.
