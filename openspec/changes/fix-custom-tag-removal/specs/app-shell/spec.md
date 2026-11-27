## ADDED Requirements

### Requirement: A popup menu never covers its anchor
A popup menu SHALL never cover its anchor: below if it fits above the safe bottom edge (system bars, cutout, keyboard)
with 8 dp, else above with 8 dp, else on the larger side, capped, scrolling; aligned to the anchor's start. A tap outside,
on the anchor too, SHALL only close it. An item SHALL take input at once; its effect SHALL run once the menu's window is
gone, and never if a configuration change cut the closing short. The lift rule stays (BRD D199 (1), EC-UX-91, D164).

#### Scenario: A short window at 200%
- **WHEN** a custom tag's chip sits 20%, 40%, 60% or 80% down a 1080 × 900 or 1080 × 700 px window at 200% font, and the
  user long-presses it
- **THEN** the menu's bounds never intersect the chip's bounds (EC-UX-91)

#### Scenario: The check can fail
- **WHEN** the placement test runs against `develop`'s menu at 1080 × 700 px, 200%, with the chip about 40% down
- **THEN** the menu covers the chip and the test fails
- **AND** the 1080 × 900 px case, which Robolectric cannot show because it draws no system bars, fails on a device with
  `develop`'s build (walk row (a); QA's `n1rep`, 2 of 2)

#### Scenario: The item's effect comes after the menu has gone
- **WHEN** the user taps "Remove from my list"
- **THEN** the tap is taken at once, the menu closes, and the removal and its message come once the menu's window is gone
  (about 0.3 s; at once with "Remove animations")

#### Scenario: Rotation just after Remove
- **WHEN** the user taps "Remove from my list" and the phone rotates, the window is resized in split screen, or the theme
  or font changes before the menu has finished closing (about 0.3 s)
- **THEN** the pick is dropped: the tag is still listed, nothing is written, and no message shows; the person can remove
  it again (D199 (1))

#### Scenario: A tap on the chip's place while the menu is open
- **WHEN** the menu is open and the user taps where the chip is
- **THEN** the menu only closes; nothing is removed or toggled

#### Scenario: Room for no item on either side
- **WHEN** neither side has room for one 48 dp item, as a pinned keyboard inset makes it in a JVM test
- **THEN** the menu opens on the larger side and may cover other content, but never its chip
- **AND** this proves the placement arithmetic only: on a device the case was not reachable (see "The keyboard is open"),
  so no person meets the narrow strip

#### Scenario: The keyboard is open
- **WHEN** the keyboard is up while the user long-presses a chip, as in a 360 dp tall landscape window at 200%
- **THEN** the menu, a focusable popup as Material's always was, takes window focus, the keyboard closes by itself, and
  the menu opens full size by the rule above; it may lie partly under the sliding keyboard for a few frames, never on its
  chip (device walk 2026-11-27)

#### Scenario: A full window is unchanged
- **WHEN** the window is the full phone at 100% or 200% and the menu fits below
- **THEN** it opens where it opens on `develop`, under the chip (QA: 0 of 20 placements overlapped)

#### Scenario: Right to left
- **WHEN** "Force RTL layout direction" is on
- **THEN** the menu aligns to the chip's start edge, mirrored, and the vertical rule is the same

#### Scenario: TalkBack, keyboard and reduced motion
- **WHEN** the menu opens with TalkBack, Switch Access or a keyboard, or with "Remove animations"
- **THEN** focus moves into the menu as before, it answers at once with no wait, the lift rule is unchanged, and it keeps
  Material's short fade, which follows "Remove animations"

## MODIFIED Requirements

### Requirement: The keyboard never hides a focused field that shows
When a keyboard frame would leave a focused field that is not following the keyboard, and showed at least 1 px, with less
than its line, the page SHALL hold the lesser of its line with clearance and what showed, less at most 1 px of rounding
and never below 1 px, in that frame, for every user. Each such frame SHALL move the line at most the keyboard's and the
bar's step plus 1 px, and not after the keyboard stops. A field covering the whole visible part SHALL NOT be held (BRD
EC-UX-88, D194).

#### Scenario: Reopening the keyboard with TalkBack (the reopen path)
- **WHEN** TalkBack is on in the 360 dp window, the note is focused, Back closed the keyboard, the page was scrolled so the
  note's top is near the bottom edge, and a double tap reopens the keyboard without moving the cursor
- **THEN** in every frame at least the lesser of the line with its clearance (up to 8 dp each side) and what showed before
  stays visible, less at most 1 px of pixel rounding and never less than 1 px, measured against what showed before the
  keyboard and not frame to frame; TalkBack's focus stays on the note, and the next double tap writes nothing

#### Scenario: Each held frame keeps to the keyboard
- **WHEN** the same reopening is recorded frame by frame
- **THEN** each held frame moves the line by at most the keyboard's step plus the bar's step plus 1 px, and 0 px once the
  keyboard has stopped

#### Scenario: Remove animations and API 26
- **WHEN** the same reopening runs with "Remove animations" on, or on API 26
- **THEN** the hold is one step with the keyboard, and the note stays in the accessibility tree
- **AND** what showed is kept less at most 1 px of rounding and never below 1 px, as in every other frame (#262)

#### Scenario: Later growth
- **WHEN** the user scrolled the note partly out of view with the keyboard up, and then switches Gboard to symbols, which is
  taller
- **THEN** the page holds what showed, up to the line, and does not pull the note to its line

#### Scenario: A field scrolled wholly away
- **WHEN** the note was scrolled wholly out of view before the keyboard grows
- **THEN** the page does not move for it

#### Scenario: A field that covers the whole visible part
- **WHEN** the note already covers the whole visible part before the keyboard grows, as in a split screen where that part is
  under about 154 px
- **THEN** the page does not hold it, and the keyboard's frame moves nothing for it

#### Scenario: The first typed character after a hold
- **WHEN** the note was held with its caret line cut, and the user types one character
- **THEN** the typing follow (D180 (2), unchanged) brings the whole caret line in one direction, settles within 15 frames,
  leaves it cut by 0 px, and focus and data are unchanged; its peak speed per frame is logged, not capped
- **AND** with "Remove animations" that bring is one step, of up to about 430 px

#### Scenario: Typing, then a double tap (device row K1)
- **WHEN** TalkBack is on, the caret is at the end of 4 lines, 60 px of the note shows, the keyboard reopens, the user types
  one character and then double taps
- **THEN** focus stays on the note, nothing is written, and the caret line shows whole

#### Scenario: Until the first keystroke (a known limit)
- **WHEN** the keyboard reopens on a focused note whose cursor did not move, and the note is held
- **THEN** the caret line may stay cut until the first keystroke, while the note stays visible and focused

#### Scenario: Ordinary openings are unchanged
- **WHEN** a field is opened by a tap or a double tap, so it follows the keyboard
- **THEN** no hold applies, and the requirement "The line being typed comes into view with the keyboard" governs every frame

#### Scenario: The checks can fail
- **WHEN** the reopen path runs on develop
- **THEN** the note is wholly hidden, TalkBack's focus moves, and the next double tap writes, which shows the row can fail
- **AND** a build that brings the line instead of holding (rejected: its rescue frame moved 158 to 494 px against a 62 px
  keyboard step) fails the per-frame row
