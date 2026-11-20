# Design

Release 1.3.3 (its only content), issue #259 (S1). BRD D194, EC-UX-39, EC-UX-83 to EC-UX-88; D180 (2), (5), D189, D192.
Cause and prototype: eng-259 (`migrely-work/259/`: `rows.jsonl`, `logs/`, `raw/`, `shots/`, `proto2.diff`); QA rows x1 to x7
in `migrely-work/qa-245c/`. Fix chosen by `team-lead`: prototype 2 first, then the explicit route of D3 (2026-11-20).
Motion verdicts: ux-259, accepted by `team-lead`: C1 to C6, and HOLD with H1 to H5 and K1 to K4 (2026-11-20). This file
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

Goals: D194 (1) to (4) and (9) on all four screens, one shared mechanism in `designsystem` (each screen hands its `ScrollState` in
one line), D192 and #238 untouched, a check for each rule that can fail.

Non-goals: a TalkBack-only path, a new bar rule, the follow while typing new lines, moving between fields, #255.

## Decisions

### D1. Only the person ends the follow

`KeyboardFollow` keeps the keyboard height its collector last handled (`keyboardFollowed`). `onPositioned` records "what the
person sees" only when the height equals both the last layout's and the followed one. A layout that comes between a frame
and the collector (Compose's semantics pass, or any other) keeps the record from before.

The field follows from the start of its own bring into view (`onBringing`, on focus or an edit), not only once that bring
ends. So a keyboard that comes while the page is still bringing the field keeps the line (cr-259 round 1, F1). Layouts during
the bring record nothing; `onBringEnded` ends it, done or cancelled.

A scroll by the person while the keyboard holds still still ends the following (D180 (5)), and so does a drag that cancels
the bring. The control cell in `KeyboardFollowTest` stays green and the never-drop mutant fails it. Removing `onBringing`
turns all four F1 cells red, the one-step ones included.

### D2. Inside a typing space, the page keeps the line in view within the layout pass

The page, not the field, moves the line. While the keyboard grows and the focused field follows it (D1), the typing space
scrolls the page's own `ScrollState` inside the layout pass that shrinks the visible part, before the page's content is
placed. So the line is in view in the very frame the keyboard moved, and no frame and no accessibility layout ever sees it
under the keyboard.

How far: `scrollToShow(top, bottom, visible)`, a pure function of viewport numbers in px: 0 where the area shows, the
distance below or above, the area's top first where it is taller than the visible part. Which area: the note's caret line
box (with the margins D192 R2 leaves), or, for a single-line field, ux U1's three areas (the box with its supporting line,
the input line with margins, the input line), the largest that fits. That keeps develop's rest position (C4).

**No settle (cr-259 round 1, F2).** A focused field wholly in view before the visible part shrank is one that Compose's own
focused-child scroll would bring back once clipped, animated, after the keyboard stops. That produced a 24 px settle at a
page-end start on 4d1d0430. The page now adds, in the same pass, the distance Compose's default `BringIntoViewSpec` would
use (`focusedChildScroll`), so Compose has nothing left to do. Either way an edge of the field is lined up, so the line
inside stays whole. The C3 limit L1 to L5 that ux and team-lead had accepted for 4d1d0430 is withdrawn: there is no settle
from any start position.

### D3. Mechanism (for `code-reviewer`)

**Chosen: the explicit route (4d1d0430, team-lead decision 2026-11-20), with one public API (3806e56e).**

- `TypingSpace.scrollingPage(scrollState)` is the only public route. It is the page's measuring and keeping step plus
  `verticalScroll(scrollState)` on the same state, so the two can never differ (cr-259 F3). All four screens use it: Attack
  details, Log medication, Edit medication, Delete all data. A screen whose scroll needs more (#255's TalkBack step) adds it
  after this, on the same state.
- In its own placement block, before it places the page content, it calls `ScrollState.dispatchRawDelta(shift)`.
  `verticalScroll` sets `maxValue` in measure and reads `value` in placement, so the content is placed at the new value in
  the same pass. Positions are read with `page.localPositionOf(field, …)` against the page's last placement, inside
  `Snapshot.withoutReadObservation`, so the page is not laid out again because they changed. Nothing is kept between frames
  except the keyboard height and visible height at the last placement.
- It acts only when the keyboard grew since the page's last placement, and only downward in content: a keyboard that shrinks
  or closes never scrolls (D189). A following field gets D2; a focused field that does not follow gets D6's hold, or nothing.
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

**Guards and tests** (engineer's numbers; see tasks 3.x):

- `KeyboardFollowFramesTest` runs each step as on 5570: the frame's own layout, a second layout at the same height, then the
  collector, on a `StandardTestDispatcher`, and reads each step in the frame's own layout.
  - The 12 first cells: on edae56a4 (with a `page` shim) 4 fail. They are a single-line field wholly in view 130 px under the
    keyboard, the note wholly in view 107 px under, the note line 84 px, and a cut single-line field 170 px.
  - Later cells add F1, F2, F5 (a keyboard changing height, moving between fields) and D6's H1 to H4, R1 and R1c.
- `AttackDetailsKeyboardFramesTest` guards the real screen's wiring: the note's line shows in every keyboard frame and stays
  once the keyboard is up.
- `TypingSpaceScrollToShowTest` (5 cells: inside, below, above, taller, exactly as tall) and `TypingSpaceKeyboardHidesTest`.
  The detached-field and unknown-page cases moved into the page's own pass (cr-259 F4).
- `KeyboardFollowTest` (2bf5857c; 4 of 4).
- Mutants:
  - no in-pass scroll: 7 cells red;
  - positions from the previous layout: 7 red (ux's C2 risk);
  - the follow state ignored: the scroll-away cell red;
  - no `onBringing`: the 4 F1 cells red;
  - D6's mutants, listed there.
- The per-screen `PageScrollParent` tests are removed: with `scrollingPage` the page and its scroll are one modifier, and the
  Attack details frames test covers the wiring end to end.
- The fallback outside a typing space (red when it does nothing).
- `KeyboardRoomTest`, the screens' keyboard-room tests and D192's bar and margin tests keep every assertion. Four harnesses
  changed only from `pageModifier` plus `verticalScroll` to `scrollingPage(state)`.
- Gate at 4d1d0430: exit 0, 3510 tests in 53 modules, 0 failed; release permissions unchanged, no INTERNET. The gate on the
  final head is the engineer's task 4.1.

**For review.** Scrolling a `ScrollState` from a placement block is a side effect inside layout. It is bounded:
- it runs once per keyboard growth step;
- it does not observe reads;
- it keeps no state of its own between frames beyond two heights;
- it is a no-op when nothing grew, when no field is focused, or when the line (or, for D6, what showed) already shows.

Any route must keep the acceptance rows below; the spec states behaviour only.

### D4. Motion (ux-259 verdict, accepted with conditions)

One gesture, one movement: the line rides up on the keyboard's own inset curve and stops when it stops, as D192 R6's bar does
("no curve of our own"). The old spring added a lag and a slow tail during which the line sat under the keyboard. This
applies to everyone, TalkBack on or off.

- **C1 Keep to the keyboard.** In each frame the line moves no further than the keyboard's top moved in that frame, plus 1 px;
  the bar's own step counts in the frame where it steps aside. Never a catch-up leap after skipped frames.
- **C2 Only upward while opening.** No down-then-up, and no double move in a frame where both the bar and the scroll act.
  Risk (ux): a scroll computed from positions taken before the bar shrank moves twice. The page reads positions in the same
  pass that has the bar's new height; the previous-layout mutant proves the test can fail.
- **C3 Nothing moves once the keyboard has settled.** No tail, from any start position (D2, F2). Typing a new line keeps
  D180 (2) (out of scope).
- **C4 The rest position is unchanged.** The final position equals develop's within 1 px in every #245 SW cell, including
  the 4 and 21 px margins at 411 dp and R2 where it applies. Only the path changes.
- **C5 No overscroll stretch or glow** from the page's scroll at its end (the note at the bottom of Attack details, Delete all
  data's word). `dispatchRawDelta` bypasses the overscroll effect.
- **C6 Unchanged:** #238's close never scrolls; bar rules R3 to R11; fields outside a typing space keep the old bring; the
  dark room and dark backgrounds get no lighter frame.

With Remove animations and on API 26 to 29 the inset arrives in one step, so the page moves in one step; at most one frame
may show the line hidden. A visible frame with the line hidden followed by a separate jump is not acceptable.

Not proven by ux: a real phone (whether a one-frame lag reads as the line chasing the keyboard), Samsung Keyboard and
SwiftKey stepwise insets.

### D5. What stays as it is

Shrinking and closing never scroll (only growth acts); no bottom IME inset means no follow; the bar's decision (D192 R5) and
semantics (R11) are untouched; `KeyboardRoom` (#238) is untouched; a field outside a typing space keeps `bringIntoView`;
the typing follow (D180 (2)) is unchanged; moving between fields is covered by frame cells (F5) and behaves as on develop.

### D6. The keyboard never hides a focused field that shows (team-lead rule (a'), ux HOLD)

**The path (device row u3, cr-259 round 2 N2/N3).**
1. The note is focused and Back closed the keyboard; focus stays on the note.
2. The page is scrolled so the note's top is near the bottom edge.
3. A TalkBack double tap reopens the keyboard without moving the cursor.

The field is focused but not following (the person moved the page), so D2 does nothing. On develop the keyboard's frame hides
the note wholly, it leaves the accessibility tree, TalkBack moves its focus, and the next double tap writes (4 of 5 on 5570;
develop z2 too). v1.3.1 (production) moves focus too, to "Back. Button", but writes nothing (3 of 3 rows on 5570): the path
was already in production as an accessibility fault, and 1.3.3 fixes it there as well.

**The rule.** For every user, with no TalkBack branch: when a keyboard frame would leave a focused, unfollowed field that
showed with less than its line, the page holds min(the line with its clearance, what showed) in that layout pass
(`keyboardHides`). The hold is measured from the field's top, `hold = fieldTop + held - visible`, so that its top stays
`held` px above the visible part's bottom (d7aa097e). While any part shows this equals the earlier
`held - shownInPage(now)`; it differs when one keyboard frame takes more than showed, as Gboard's first reopen frame under
TalkBack does (0 to 270 or 0 to 422 px against 87 to 89 px shown), where the clamped form left the note wholly hidden on
43fb9202. The line with its clearance is the caret line plus the margin D192 R2 leaves, up to 21 px
(8 dp) each side, so up to about 154 px at 200%, not the 112 px line alone (cr-259 round 3, S4-2); within H1 and H2.
- "Showed" means at least 1 px (`MIN_SHOWN_PX`), because Compose rounds clipped bounds to whole pixels (`roundToIntRect`) and
  prunes empty ones.
- The hold is stateless: it reads only this frame and the last placement's visible height.
- The field moves with the keyboard and no further, and stays in the tree.
- It also fires on later growth, for example symbols after a partial scroll-away.
- It never pulls the field to its line, and a field scrolled wholly away stays put (R1).
- A field that covers the whole visible part is not held (`before < visibleBefore`, 43fb9202, cr-259 round 4): in a split
  screen whose visible part is under about 154 px, holding the line with its clearance is not possible.

**Conditions (ux-259, accepted by team-lead).**
- **H1** Each held frame moves the line by at most the keyboard step plus the bar step plus 1 px; 0 after the keyboard stops;
  one step with Remove animations or API 26. Robolectric at 360 dp, 200%: keyboard steps 62 to 63 px, bar 19 to 20 px; held
  frames move the line 61 to 63 px.
- **H2** At least min(shown before, the line with its clearance) shows in every frame (cells at 60, 150 and 350 px, the caret on line 1 and at the
  end of 4 lines: 59 to 60, 150, and 153 to 154 px kept).
- **H3 (amended)** One typed character after a hold brings the whole caret line through the unchanged D180 (2) spring, with
  four checks:
  - (a) in one direction;
  - (b) settled within 15 frames;
  - (c) final cut 0;
  - (d) focus and data unaffected.

  The peak is logged, not capped: 26 px per frame for caret line 1 at 60 px; 100, 81 and 79 px per frame for the caret at
  the end of 4 lines at 60, 150 and 350 px.
- **H4** Ordinary openings (the field follows) are unchanged.
- **H5 (known limit)** "On a reopen with focus kept, the caret line may stay cut until the first keystroke."

**ux K1 to K4.**
- **K1:** device row on 5570, TalkBack on, the caret at the end of 4 lines, 60 px shown. Reopen, type one character, then
  double tap: focus stays on the note, 0 writes, the line is whole.
- **K2:** with Remove animations the typing bring snaps in one step of up to about 430 px. Accepted and recorded (T7).
- **K3:** limit text for QA's Go/No-Go "does not prove": "The typing bring (D180 (2), unchanged) can move up to about 100 px
  per frame at its peak, eased, after a rescue or a drag."
- **K4:** follow-up #260 (S4).

**Rejected.**
- BRING, bringing the line at once (d7f985ac): its rescue frame moved the line 158 to 494 px against a 62 px keyboard step, a
  sudden jump. As the negative control it reds the four H1 cells (177, 513, 397 and 1078 px against 81, 81, 83 and 821
  allowed).
- The N1 latch and the hand-over to the follow: the latch kept state across frames, and the hand-over brought BRING back
  (the line in frame 2, 192 to 572 px).

**Mutants.**
- The hold doubled: 2 H1 cells red.
- A hold of 1 px: the 6 H2 cells and H3 red.
- The shown-before guard removed: only the sub-pixel cell red. The "takes some" guard already excludes a wholly hidden field.
- No typing bring: H3's final-cut check red.
- The hold for followed fields too: H4 and 15 ordinary cells red.

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
| T7 | Remove animations, TalkBack on | focus kept, one-step move, at most one hidden frame; K2 logged | |
| U3 | D6's reopen path, TalkBack, 360 dp, then one more double tap | focus on the note, 0 writes; H2 in every frame | develop: hidden, focus moves, writes (4/5); 43fb9202: hidden, moves, writes; v1.3.1: focus to "Back", 0 writes (3/3) |
| K1 | U3 with the caret at the end of 4 lines, 60 px shown; type one character, then double tap | focus kept, 0 writes, line whole | |
| F5 | rotation with the keyboard up, TalkBack on | bar decided again, line shows, focus kept | not reachable in unit tests |
| M1 | 60 fps record, note, 360 dp, TalkBack off and on, candidate and develop, including a page-end start | C1, C2 per frame; last frame equals +500 ms | develop's spring moves after the last frame |
| M2 | Final frame against develop, 411 dp, 360 dp, SW13 band if reached | equal within 1 px | |
| M3 | Note at the page's end and Delete all data, dark | no stretch or glow | |
| M4 | Log medication from attack mode, 360 dp | no lighter frame | |
| M5 | API 26 (5582) and Remove animations | before and after frames, at most one hidden | |

### Engineer's results on 5570

On 4d1d0430 (debug, sha256 e86fdac9…; lead re-counted):

| Row | Result | Gap |
|---|---|---|
| T1 360 dp | 32 of 32 openings kept focus; 0 writes; 112/112 | first route: 3 of 17 moved |
| T4 411 dp | 2 of 2 kept, 112/112 | |
| T2 | impact 2 identical after the opening and one more double tap | |
| T3 | Log medication, Delete all data, Edit name, Edit dose at 360 dp: kept, nothing written | one opening each |
| T6 | TalkBack + Back: 0 `TYPE_VIEW_SCROLLED`, no focus move | TalkBack off not obtained (compact bar) |
| T7 | 2 of 2 kept, 112/112 | |
| M1 | no captured frame with the field hidden; no C1 or C2 break | about 5 fps on 5570; the 24 px page-end settle seen here is removed by F2 |
| M2 | rest caret 185..297 at 360 dp, 278..390 at 411 dp, identical to develop | |

On the final head d7aa097e (debug "cand8", sha256 787d7a3f…; rows in `migrely-work/259/rows.jsonl`, summary
`python3 migrely-work/259/lib/r4sum.py`). Setup: API 36, Gboard, 360 dp landscape, 200% font, TalkBack on. A row counts only
if the 1-minute load was under 8 at its start and its end and at least 3 GB of disk was free; other rows are void.

| Row | Result | Gap |
|---|---|---|
| U3 | 6 valid rows: 0 focus moves, 0 writes. Where the caret line shows, 48 to 52 px of it shows; elsewhere less than the line (H5, expected) | |
| K1 | 3 valid rows: 0 focus moves; the only write is the typed "x"; caret line whole at 185..297, 112/112 | |
| T1 360 dp | 17 valid rows: 0 moves, 0 writes, 112/112 | |
| F2 | page-end start, animations on and off: 2 valid rows, 0 px settle at +0.5 s and +1.0 s; the negative control saw a 20 px shift | mid-page start: 2 rows, both void |
| U3 control, 43fb9202 | its diag build: 4 rows, 3 focus moves to an impact radio, 3 impact writes (Gboard's first reopen frame 0 to 270 or 422 px) | the plain debug: 2 finished rows, 1 moved and wrote at load 8.6 (void) |
| U3 control, v1.3.1 | 3 rows: 3 focus moves to "Back. Button", 0 writes | control rows record only the end load |

Not run by the engineer on the final head (team-lead decision 2026-11-20, to speed up 1.3.3 as the owner asked): T3, T5,
T6, M1 and M2 to M4; also T4, T7 with K2, M2 and T2's impact preset. QA covers them independently in 7.1, 7.4 and 7.5.

Not proven by the engineer: per-frame motion (on 5570 `screenrecord` kept 2 to 3 keyboard frames per opening), real
phones, the R8 build, API 26, other keyboards than Gboard, rotation. QA's task 7 covers
them.

## Risks

- **A side effect in layout (D3).** A scroll from a placement block can surprise a future maintainer, and a future screen
  that puts another scrollable between the field and the page would bypass it. Mitigation: KDoc on `TypingSpace`, the single
  `scrollingPage` API, the Attack details frames test, and the bring fallback.
- **C2 double move.** Mitigated by reading positions in the same pass; the previous-layout mutant proves the test can fail.
- **F2 mirrors Compose's default `BringIntoViewSpec`.** A Compose upgrade that changes that default would bring a settle
  back. Mitigation: the F2 frame cells and M1's +500 ms check; recheck on every Compose bump.
- **H5 and K3.** After a hold the caret line can stay cut until the first keystroke, and that keystroke's spring can move up
  to about 100 px per frame (one step of about 430 px with Remove animations). Both are recorded limits, with follow-up #260.
- **Production exposure of the reopen path.** v1.3.1 moves focus to "Back" on that path (3 of 3 on 5570) without a write:
  an accessibility fault already in production, fixed by 1.3.3. Only Gboard on API 36 was measured.
- **TalkBack off on 5570.** T5 and TalkBack-off T6 have no device row on the explicit route. QA must measure both on 5580.
- **Unordered reads.** A detached field is skipped and never scrolled on stale coordinates.
- Real phones and other keyboards are not proven; recorded in NOT CHECKED, not claimed.
