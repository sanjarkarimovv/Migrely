# Design

Release 1.3.1, tracker T74, issue #238 (S3, accessibility). BRD D189, EC-UX-43, EC-UX-61 to EC-UX-65; D180 (2), (5), (8);
D178. The behaviour is `ux-designer`'s `docs/ux/238-keyboard-room.md` (R1 to R14, per-screen notes §5, acceptance KR1 to
KR16), accepted by `team-lead`. This file binds what the person sees and what QA checks; the code shape is the engineer's
call within it.

## Context

Since D180 (2) every screen with a text field pads its scroll's visible part for the keyboard, outside `verticalScroll`, so the
focused line stays above the keyboard (#141). When the keyboard closes, that visible part grows. If the page was near its end,
the scroll position no longer fits: `ScrollState` clamps it, Compose reports `TYPE_VIEW_SCROLLED`, and TalkBack 16 treats the
event as the person's own scroll and moves accessibility focus to the first item in view. On Attack details that is the first
trigger chip, "Not selected. Stress" (#238, QA headless 3/3, TalkBack VERBOSE log).

| Screen | Field | Visible part ends at | What 1.3.0 pulls into view on close |
|---|---|---|---|
| Attack details | note | `barsAndKeyboard` bottom padding before the scroll (`AttackDetailsScreen.kt`) | trigger chips (reproduced) |
| Edit medication | name, usual dose | same, inline `rememberScrollState()` (`EditMedicationScreen.kt:230-232`) | the page top: Name or a Kind radio (not reproduced) |
| Log medication | first-use name | same, inline `rememberScrollState()` (`LogMedicationScreen.kt:333-335`); dark room from attack mode | the page top (not reproduced) |
| Delete all data | the word | the Delete row: `rides` uses `barsAndKeyboard`, else `behindKeyboard` padding (`DeleteAllDataScreen.kt:210-243`) | the warning text (not reproduced) |

Commit a1efa2bc (on this branch, based on 1.3.0's head 294c10c6) adds `KeyboardRoom` in `designsystem/.../input/` and wires it
on Attack details. It is the existing part of this change, not yet reviewed.

## Goals and non-goals

Goals: R1 to R14 on all four screens; one shared helper (DRY); a check for each rule that can fail.

Layers touched: UI only. `designsystem` (the helper), then the `ui` modules of `features/attack`, `features/medication` and
`features/settings`. No domain, presentation, data, datasource, DI or navigation change.

Non-goals: the keyboard's opening, the cursor-follow rule, messages' fit rule, the three clamp cases outside the keyboard
(ux §10), any string, permission, data or migration.

## Decisions

### D1. Keep the scroll position; add room, never scroll (D189 (1), R1, R3)

Terms: *K* = how much the visible part grows when the keyboard closes or gets shorter; *b* = how much of the page lay below
the visible part just before. If *b* < *K*, room of *K* − *b* appears after the last item, exactly enough that the scroll
position does not change. If *b* ≥ *K*, no room. The scroll position in px is the same before and after on every API level,
with or without animations. Why: the bug is the scroll event; removing the scroll removes the cause for every accessibility
service, and also removes the sighted jump on API 26 to 29.

### D2. The room is the page (D189 (2), R2)

Background only, of the screen's own colour, because the room is part of the scrolling content and draws nothing itself. It
sits after the last item and below the page's existing bottom padding. In attack mode (Log medication from the attack card,
details from attack mode) it is that screen's dark room background. Never lighter: a dark page stays dark as the light system
keyboard goes.

### D3. Only the person removes it (D189 (3), R4 to R7)

It shrinks in step with any scroll up the person causes (finger, fling, TalkBack's scroll action, Tab or Shift+Tab bringing a
control into view, Page Up, a mouse wheel, D181 (6)'s bring-into-view after a failed Save), and never grows back from
scrolling. A reopened or taller keyboard uses the room first, then D180 (2) applies unchanged. Content that grows uses it up.
It is never animated away: animating it away at the page's end is itself a scroll, and the bug returns a few hundred ms later
(rejected option C).

### D4. Not saved (D189 (4), (5), R8 to R10)

The room is `remember`, not `rememberSaveable`. After rotation, process death, or leaving the screen and coming back, the
screen comes back without it, at its saved scroll position or the nearest one the page allows; a band with no visible cause
would read as a broken layout. Background and return without recreation keep it as it was. With no bottom keyboard inset
(hardware or floating keyboard, split screen, full-screen editor) nothing grows, so no room.

### D5. Not in the accessibility tree (D189 (6), R11 to R13)

The room is a layout modifier on the scrolling column that adds height, not a child node: no semantics, not focusable, no Tab
stop, no ripple. A tap in it does what a tap on empty page background does: nothing. Nothing is announced. Input focus stays
in the field (U6); TalkBack's focus stays there because no scroll event fires. The first Back closes only the keyboard.

### D6. One helper for four screens (DRY)

`KeyboardRoom` (a1efa2bc) takes the window inset that pads the visible part. Attack details, Edit medication and Log
medication pad with `MigrelyInsets.barsAndKeyboard.only(Bottom)`, so they can use it as it is, with the `ScrollState` hoisted
out of the inline `rememberScrollState()`. Delete all data's visible part ends at the Delete row, whose bottom is either the
keyboard inset (`rides`) or `behindKeyboard`; the room there needs the growth of the visible part from that layout, not from
an inset alone (ux §9). Engineer's choice, within these limits: generalise the helper's input to "bottom padding of the
visible part in px" (or read the visible part's size from the scroll), keep one helper, and keep `KeyboardRoomTest` green. The
modifier goes right after `verticalScroll` on the column, never as a child: both medication columns use `spacedBy(12.dp)`, and
a child would add a 12 dp gap even with zero room.

### D7. Delete all data (D189 (8), ux §5.4)

The field stays where it is; only the Delete row moves with the keyboard as D180 (8) defines. After Done or Back the field and
"Delete everything" show together (D180 (8) and the old design's D7 still hold). The divider above the row follows
`canScrollForward`; with the page's end in view it is hidden, which is correct.

### Rejected

- B. Let the page settle as in 1.3.0, then put TalkBack focus back on the field: Compose has no public way to set
  accessibility focus; moving input focus makes TalkBack read the field again (breaks U8); it races TalkBack, whose timing
  differs (headless 3/3, windowed 0/9); the motion stays.
- C. Room, then animate it away: shrinking at the page's end is a scroll; the event fires, only later; unasked motion.
- D. Room kept until the screen is left: a page ending in empty space looks broken, for no gain.
- E. Room only with TalkBack on: Switch Access and Voice Access react to scroll events too; two behaviours double the matrix.
- F. Stop padding the visible part for the keyboard: reopens #141.

### Cost (accepted by `team-lead`)

Near the page's end a band of empty background stays until the person scrolls up. On a short page that fits without the
keyboard (Edit medication and Log medication at 100%), the page's top stays scrolled out of view until the person scrolls up.
Nothing is lost or out of reach; one swipe brings it back. This UX cost ranks below the accessibility and data-integrity risk
it removes.

## Edge cases

| Case | Expected | Where |
|---|---|---|
| Empty, first use (Log medication first-use form), Free and Premium | no room before the first close on this visit; same rules for both tiers | R14, EC-UX-61 |
| Near the end on each of the four screens | scroll position unchanged, room fills the area | R1, R3, EC-UX-61 |
| Far from the end (*b* ≥ *K*) | no room, nothing moves | R1, EC-UX-61 |
| Loading, failure, full storage | a failure message while the room shows keeps its position (D180 (4)); a failed Save's bring-into-view shrinks the room | R4, R7, EC-UX-62 |
| Offline | not affected: no network anywhere | none |
| During an attack | Log medication from attack mode: the room is `AttackModeBackground` | R2, EC-UX-65 |
| Rotation, process death, leaving and returning | no room on return, text kept, nearest valid scroll position | R8, EC-UX-63 |
| Background without recreation | room kept | R9, EC-UX-63 |
| Keyboard reopens, grows, shrinks, switches | room used first, then D180 (2); a shorter keyboard adds room | R5, R6, EC-UX-62 |
| Content grows (chip, error line, "Add and log") | uses the room, page does not move | R7, EC-UX-62 |
| Hardware or floating keyboard, split screen, full-screen editor | no room ever; unreachable rows are "not reachable" | R10, EC-UX-63 |
| 200% font, portrait and landscape, 411 and 360 dp | page still; room never taller than the keyboard; field lines fully visible | EC-UX-65 |
| Remove animations, API 26 one-step inset | page still, no frame lower (end state only proven) | R3, EC-UX-65 |
| TalkBack, Switch Access, Voice Access, keyboard, D-pad | focus stays on the field; room never a stop; Tab and Page keys scroll and shrink it | R11 to R13, EC-UX-61 |
| Right to left | vertical only; nothing to mirror | ux §6 |
| Double tap right after close | lands on the empty room or the field, never on a chip, radio or button that slid in | R11, EC-UX-61 |
| Delete all data, row rides or stays behind | field still; field and "Delete everything" together; no divider | D7, EC-UX-64 |
| Time, data, backup, purchase | not touched: layout only, no stored state | none |

## Acceptance (QA, this diff only)

Builds: **candidate** = this branch at the SHA under test (debug); **control** = `v1.3.0`; **mutation** = the candidate with the
room removed from the screen under test (built by QA or the engineer, never committed). Where v1.3.0 does not reproduce the
focus jump on a screen, that screen's TalkBack row proves only "no jump observed" and says so. Run TalkBack rows headless
first (where the control reproduced), and record windowed runs separately. QA's emulators: emulator-5580 (API 36), then
emulator-5582 (API 26), one at a time.

Set-up "near the end": the field is focused, the keyboard is open, and less than a keyboard's height of content lies below the
visible part. Details: Notes with "Delete migraine" above the keyboard, or a long note typed to its end. Edit medication: Usual
dose. Log medication: first use, Name. Delete all data: the word typed, row riding (portrait 100%) and behind (200% landscape,
360 dp).

| # | Where | Steps | Pass | Negative control |
|---|---|---|---|---|
| KR1 | each screen, API 36, TalkBack VERBOSE, 100% portrait | set-up near the end, TalkBack on the field, Back once | within 3 s only the field or "keyboard hidden" is spoken; one more double tap reopens the keyboard in that field; no chip, radio or kind changed | v1.3.0 and mutation: focus moves to the first item in view (details: "Not selected. Stress") |
| KR2 | details and one single-line screen, TalkBack on | as KR1, closing with the hide key, then Done | as KR1 | v1.3.0, same path |
| KR3 | each screen | accessibility event log across Back from the set-up | zero `TYPE_VIEW_SCROLLED` from the page's scroll container within 3 s | v1.3.0: at least one |
| KR4 | each screen, API 36 and API 26, 100% portrait | field bounds from a layout dump before Back and after the keyboard is gone | identical within 1 px | v1.3.0: the field moves down by more than 0 |
| KR5 | details and Log medication, dark, light, Log medication from attack mode | screenshot after the close; sample the band and the page background at the top | every band pixel equals the background; none brighter | the same check on the keyboard-open screenshot's keyboard area fails |
| KR6 | details | layout dump at the hide key's former place, then a tap | no clickable or focusable node; the tap changes nothing | v1.3.0: a clickable node there (by dump only, no tap) |
| KR7 | each screen | after the close: swipe up a third of the band, then more than the band, then scroll to the end | field moves with the finger, band shrinks by the same; then no band; normal bottom padding at the end | mutation whose room never shrinks: the band shows again at the end |
| KR8 | details, 200% landscape | after the close, swipe forward from the note, then back several items | forward: "Delete migraine", then the page's end; never the room; back in D178 order | none needed |
| KR9 | each screen | after the close, tap the field | keyboard opens, no jump, cursor line at least 8 dp above it (EC-UX-39), single-line per U1 | none needed |
| KR10 | details and Edit medication, band visible | rotate and back; background, `am kill`, return from recents; leave and reopen | no band after each; text kept; nothing cut | none needed |
| KR11 | each screen, portrait and landscape, 411 and 360 dp, 200% | set-up, close | KR4 holds; band not taller than the keyboard; field lines fully visible | none needed |
| KR12 | details and Delete all data, animator scales 0 | as KR4 | KR4 holds; no frame with the page lower | none needed |
| KR13 | details | hardware keyboard; floating keyboard and split screen if reachable | no band, no change on close | an unreachable row is "not reachable", not a pass |
| KR14 | details, band visible | trigger a message; close the keyboard while a message shows | the message's position equals its position without a band; it does not cover Back | none needed |
| KR15 | Delete all data, both row cases | type the word, Done | field still; field and "Delete everything" together; no divider with the end in view | mutation: the field moves; v1.3.0 for KR4 |
| KR16 | details, long page, note mid-page with more than a keyboard below | Back | no band, nothing moves, TalkBack as KR1 | none needed |

Restore every device setting afterwards: font scale, rotation, animator scales, TalkBack, keyboard.

## What the checks will not prove

- **One frame on API 26 to 29.** KR4 measures the end state. Whether one frame draws the page lower before the room arrives
  needs a 60 fps screen recording; without it the row says "end state only".
- **Other TalkBack builds.** TalkBack 16 on the emulator only. If another version, or Samsung's TalkBack, moves focus on the
  keyboard close itself (not on a scroll), the room does not help, and that is a different bug.
- **Windowed versus headless.** The control reproduced headless only (3/3, windowed 0/9). A windowed pass on the candidate
  proves little on its own.
- **How people read the band.** Whether a band taller than half a short landscape window at 200% reads as "the page ended" or
  "failed to load", and whether sighted people miss the Name field scrolled out of view on Edit medication. No real session.
- **Real phones and other keyboards.** Gboard on API 36 and the AOSP keyboard on API 26 only; API 27 to 35, foldables and
  tablets are not run.
- **The three clamp cases outside the keyboard** (ux §10) are not covered and not tested.
- **Screens without a field.** Not touched by this change; nothing here proves anything about them.

## Risks

- **Delete all data's source of growth.** If the helper reads only a window inset, the row's `behindKeyboard` case may get no
  room or the wrong amount. D6 asks for the visible part's growth; KR4 and KR15 on both row cases catch it.
- **Unordered reads.** The room reads the inset and the scroll's extent in a snapshot flow; a layout between the two reads
  could give a stale value for one frame. a1efa2bc reads both again on collect; review checks the same on the other screens.
- **Archive order.** The MODIFIED delta targets a requirement that exists only in `fix-keyboard-follows-cursor`'s delta.
  Archive that change first; if its text changes before then, sync this delta (tasks 8.1, 8.2).

## Migration

None. No data, string, permission or manifest change.
