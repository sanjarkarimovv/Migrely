# Design

Release 1.3.3 (its only content), issue #259 (S1). BRD D194, EC-UX-39, EC-UX-83 to EC-UX-87; D180 (2), (5), D189, D192.
Cause and prototype: eng-259 (`migrely-work/259/`: `rows.jsonl`, `logs/`, `raw/`, `shots/`, `proto2.diff`); QA rows x1 to x7
in `migrely-work/qa-245c/`. Fix chosen by `team-lead` (prototype 2). Motion verdict: ux-259, accepted by `team-lead` with
conditions C1 to C6 (2026-11-20). This file binds what the person sees and what QA checks; the code shape is the engineer's
call within it, and `code-reviewer` judges the mechanism.

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

Goals: D194 (1) to (4) on all four screens, one shared mechanism in `designsystem`, no screen change, D192 and #238 untouched,
a check for each rule that can fail.

Non-goals: a TalkBack-only path, a new bar rule, the follow while typing new lines, moving between fields, #255.

## Decisions

### D1. A layout counts only at a keyboard height the follow already acted on

`KeyboardFollow` keeps the keyboard height its collector last handled (`keyboardFollowed`). `onPositioned` records "what the
person sees" only when the height equals both the last layout's and the followed one. A layout that comes between a frame
and the collector (Compose's semantics pass, or any other) keeps the record from before. A scroll by the person while the
keyboard holds still still ends the following (D180 (5)), so the control cell in `KeyboardFollowTest` stays green and the
never-drop mutant fails it.

### D2. Inside a typing space, the page moves in the same turn as the keyboard

When the collector sees the keyboard grow and the follow is on, the field asks its `TypingSpace` how far the page must
scroll for the line (with the margins D192 R2 leaves) to show: `TypingSpace.scrollToShow(coordinates, area)`, in px, 0 where
it shows, the area's top first where it is taller than the visible part, null while the page is unknown or the field is
detached. The field hands that distance to the page synchronously, so the line is in view in the frame the keyboard moved,
before any scroll event reaches TalkBack. Used by `CursorFollowingTextField` (the caret's line box) and
`SingleLineFieldInView` (the input line with `marginAround`). A null answer (no typing space) falls back to the old animated
`bringIntoView`.

### D3. Mechanism (for `code-reviewer`)

Chosen (device-proven, prototype 2): each following field installs `nestedScroll(NoNestedScroll, dispatcher)` and sends the
distance with `NestedScrollDispatcher.dispatchPostScroll(Offset.Zero, Offset(0, -shift), NestedScrollSource.SideEffect)`.
The page's `verticalScroll` is the field's nested-scroll parent and consumes the post-scroll at once. This is a side channel
to the page's scroll: it relies on the page being the nearest nested-scroll parent that consumes vertical post-scroll, on
all four screens.

Alternatives considered:
- An explicit `ScrollState` passed through `TypingSpace`: direct and readable, but touches the four screens' call sites.
- A snap `BringIntoViewSpec`: would keep `bringIntoView`, but unproven on device and still dispatched after the layout.

Lead decision: keep the device-proven mechanism, document it here and in KDoc, cover it with tests, and let `code-reviewer`
judge. The spec states behaviour, not mechanism, so a reviewer-requested switch to the `ScrollState` route needs no spec
change, only the same acceptance rows.

Guards the tests must hold: a nested-scroll ancestor that is not the page (a top bar scroll behaviour, a pager) must not take
the distance; an overscroll effect must not stretch or glow from `SideEffect` (C5); the field's `NoNestedScroll` connection
must not change how a drag inside the field scrolls the page.

### D4. Motion (ux-259 verdict, accepted with conditions)

One gesture, one movement: the line rides up on the keyboard's own inset curve and stops when it stops, as D192 R6's bar does
("no curve of our own"). The old spring added a lag and a slow tail during which the line sat under the keyboard. This
applies to everyone, TalkBack on or off.

- **C1 Keep to the keyboard.** In each frame the line moves no further than the keyboard's top moved in that frame, plus 1 px;
  the bar's own step counts in the frame where it steps aside. Never a catch-up leap after skipped frames.
- **C2 Only upward while opening.** No down-then-up, and no double move in a frame where both the bar and the scroll act.
  Risk (ux): a scroll computed from positions taken before the bar shrank moves twice. `scrollToShow` must read positions
  from the same layout that has the bar's new height.
- **C3 Nothing moves once the keyboard has settled.** No tail. Typing a new line keeps D180 (2) (out of scope).
- **C4 The rest position is unchanged.** The final position equals develop's within 1 px in every #245 SW cell, including
  the 4 and 21 px margins at 411 dp and R2 where it applies. Only the path changes.
- **C5 No overscroll stretch or glow** from the side-effect scroll at the page's end (the note at the bottom of Attack
  details, Delete all data's word).
- **C6 Unchanged:** #238's close never scrolls; bar rules R3 to R11; fields outside a typing space keep the old bring; the
  dark room and dark backgrounds get no lighter frame.

With Remove animations and on API 26 to 29 the inset arrives in one step, so the page moves in one step; at most one frame
may show the line hidden. A visible frame with the line hidden followed by a separate jump is not acceptable.

Not proven by ux: a real phone (whether a one-frame lag reads as the line chasing the keyboard), Samsung Keyboard and
SwiftKey stepwise insets, moving between fields while the keyboard is up.

### D5. What stays as it is

Shrinking and closing never scroll (the collector acts only on growth); no bottom IME inset means no follow; the bar's
decision (D192 R5) and semantics (R11) are untouched; `KeyboardRoom` (#238) is untouched; the field outside a typing space
keeps `bringIntoView`.

## Acceptance rows (device)

Engineer on emulator-5570 (API 36, Gboard); QA independently on emulator-5580 (API 36, with v1.3.1 as the control and the R8
build) and emulator-5582 (API 26). Rows go to `migrely-work/259/` (engineer) and QA's own folder.

| Row | Setup | Pass | Control that can fail |
|---|---|---|---|
| T1 | TalkBack, double tap the note, 360 dp, 200% landscape | focus on the note after 3 s, line 112/112 | v1.3.2 jumps (8/8 in QA) |
| T2 | T1 with impact 2 recorded, then one more double tap | database impact still 2 | v1.3.2 clears it |
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

## Risks

- The side channel (D3) could be taken by another nested-scroll ancestor on a future screen. Mitigation: KDoc on
  `TypingSpace`, a test with an ancestor that consumes post-scroll, and the fallback for fields outside a typing space.
- C2 double move (D4). Mitigation: a Robolectric frame test with the bar stepping aside in the same frame.
- Unordered async reads: `scrollToShow` reads `page` and coordinates at collection time; a detached field returns null and
  falls back, never scrolls on stale coordinates.
- Real phones and other keyboards are not proven; recorded in NOT CHECKED, not claimed.
