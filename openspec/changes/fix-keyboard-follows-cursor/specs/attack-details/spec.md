## ADDED Requirements

### Requirement: The note's cursor line stays above the keyboard
While the note has focus and the keyboard is open, the line with the cursor SHALL stay fully visible above the keyboard: while
typing, after a tap anywhere in the note, after Tab, after a paste and after the cursor moves. The page SHALL scroll only as far
as needed and never to a part of the note away from the cursor. Saving SHALL work as before (BRD EC-UX-39, D180, D152).

#### Scenario: Typing past the keyboard at 200%
- **WHEN** at 200% font the user types a note of 90 characters or more with the on-screen keyboard, past the keyboard's top
- **THEN** after each key the line with the cursor is fully visible above the keyboard

#### Scenario: Typing past the keyboard at 100%
- **WHEN** at 100% font the user types a note of 200 characters or more, past the keyboard's top
- **THEN** after each key the line with the cursor is fully visible above the keyboard

#### Scenario: Tab into a long note
- **WHEN** a note of 996 characters is focused with Tab, so the cursor is at its end, at 100% or 200% font
- **THEN** the note's last line, with the cursor, is visible above the keyboard

#### Scenario: A tap in the first lines of a long note
- **WHEN** the user taps a word in the first lines of a 996-character note, at 200% font
- **THEN** the line with the cursor is visible above the keyboard, and the page does not jump to another part of the note

#### Scenario: A tap in a line already above the keyboard
- **WHEN** the user taps a line that is already visible above the keyboard
- **THEN** the page does not move

#### Scenario: A paste
- **WHEN** the user pastes text that moves the cursor below the keyboard's top
- **THEN** the line with the cursor is visible above the keyboard

#### Scenario: The cursor moves
- **WHEN** the user drags the cursor handle, or moves the cursor with arrow keys, to a line under the keyboard's top
- **THEN** the page scrolls just enough to show that line above the keyboard

#### Scenario: Landscape
- **WHEN** the phone is in landscape at 100% or 200% font and the user types past the keyboard's top
- **THEN** the line with the cursor is visible between the top bar and the keyboard

#### Scenario: The note still saves
- **WHEN** the user types with the keyboard open and leaves the screen, or the app goes to the background
- **THEN** the note is saved as before this change, with nothing lost

#### Scenario: The safety card with the keyboard closed
- **WHEN** the keyboard is closed and a first 10 makes the safety card appear out of view
- **THEN** the card comes into view exactly as D167 says, and the D167 tests still pass

#### Scenario: Without the change
- **WHEN** the typing rows run on a build of the same commit without the keyboard padding on the details' scroll
- **THEN** the cursor line ends under the keyboard as in #141, which shows the check can fail
