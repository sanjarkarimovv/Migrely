## ADDED Requirements

### Requirement: The note's cursor line stays above the keyboard
While the note has focus and the keyboard is open, the line with the cursor SHALL stay fully visible, at least 8 dp above the
keyboard, and 8 dp below the top bar when the cursor moves up: while typing, after a tap, Tab, a paste or a cursor move. The
page SHALL scroll only as far as needed, and a tap's place SHALL win over opening at the end (BRD EC-UX-39, D180, D152).

#### Scenario: Typing past the keyboard at 200%
- **WHEN** at 200% font the user types a note of 90 characters or more with the on-screen keyboard, past the keyboard's top
- **THEN** after each key the line with the cursor is fully visible, with at least 8 dp between it and the keyboard

#### Scenario: Typing past the keyboard at 100%
- **WHEN** at 100% font the user types a note of 200 characters or more, past the keyboard's top
- **THEN** after each key the line with the cursor is fully visible, with at least 8 dp between it and the keyboard

#### Scenario: The counter while typing
- **WHEN** the line with the cursor is the note's last line and the keyboard is open
- **THEN** the counter "N/1000" under the field may stay under the keyboard, and it shows again when the keyboard closes

#### Scenario: Tab into a long note
- **WHEN** a note of 996 characters is focused with Tab, so the cursor is at its end, at 100% or 200% font
- **THEN** the note's last line, with the cursor, is visible at least 8 dp above the keyboard

#### Scenario: A tap in the first lines of a long note
- **WHEN** the user taps a word in the first lines of a 996-character note, at 200% font
- **THEN** the cursor is where the user tapped, its line is visible above the keyboard, and neither the cursor nor the page goes
  to the note's end

#### Scenario: A tap in a line already above the keyboard
- **WHEN** the user taps a line that is already at least 8 dp above the keyboard
- **THEN** the page does not move

#### Scenario: A paste
- **WHEN** the user pastes text that moves the cursor below the keyboard's top
- **THEN** the line with the cursor is visible at least 8 dp above the keyboard

#### Scenario: The cursor moves down
- **WHEN** the user drags the cursor handle, or moves the cursor with arrow keys, to a line under the keyboard's top
- **THEN** the page scrolls just enough to show that line 8 dp above the keyboard

#### Scenario: The cursor moves up
- **WHEN** the user moves the cursor to a line above the visible part of the note
- **THEN** the page scrolls just enough to show that line 8 dp below the top bar

#### Scenario: Landscape
- **WHEN** the phone is in landscape at 100% or 200% font, on a window 411 dp or 360 dp tall, and the user types past the
  keyboard's top
- **THEN** the line with the cursor is visible between the top bar and the keyboard

#### Scenario: TalkBack moves on from the note
- **WHEN** TalkBack is on, the keyboard is open on the note, and the user swipes forward to "Delete migraine"
- **THEN** that button is scrolled into the space above the keyboard, never left under it

#### Scenario: The note still saves
- **WHEN** the user types with the keyboard open and leaves the screen, or the app goes to the background
- **THEN** the note is saved as before this change, with nothing lost

#### Scenario: The safety card with the keyboard closed
- **WHEN** the keyboard is closed and a first 10 makes the safety card appear out of view
- **THEN** the card comes into view exactly as D167 says, and the D167 tests still pass

#### Scenario: The safety card with the keyboard open
- **WHEN** the note has focus with the keyboard open, the user scrolls up to Pain and adds a first 10, and dismisses the dialog,
  with or without "Remove animations"
- **THEN** the safety card comes into view as D167 says and stays in view, and the page does not scroll back to the note

#### Scenario: Without the change
- **WHEN** the typing rows run on a build of the same commit without the keyboard padding on the details' scroll
- **THEN** the cursor line ends under the keyboard as in #141, which shows the check can fail

### Requirement: A message may lie over the cursor line
While a message lies over the content (D158, outside card mode), it MAY cover the note's cursor line. The message SHALL NOT move
for this, and it SHALL stay dismissible by a sideways swipe on its visible part and by TalkBack's Dismiss or Undo action. The
untimed storage-full error SHALL win over the line (BRD EC-UX-39, EC-UX-41, D158, D180).

#### Scenario: A message over the line being typed
- **WHEN** a message lies over the content at 200% font in landscape and the line with the cursor is under it
- **THEN** the message stays where it is, and a sideways swipe on it or TalkBack's Dismiss or Undo action removes it

#### Scenario: Storage full while typing
- **WHEN** "Couldn't save. Your phone's storage is full." shows while the user types the note
- **THEN** the error stays over the line until it is dismissed, and the note's text is not lost

### Requirement: An autosave does not move the cursor or the page
While the user types in the note, its autosave SHALL NOT move the cursor or the page. The cursor SHALL go to the note's end only
when the stored text really differs from the field's, as from a merge or a reload (BRD EC-UX-44, D180, D152).

#### Scenario: W-AUTOSAVE
- **WHEN** at 200% font the user taps the middle of a 996-character note, types, waits 1 second so the autosave runs, and types
  again
- **THEN** neither the cursor nor the page moves, and the second text goes in where the first ended

#### Scenario: A real change from outside
- **WHEN** a merge or a reload changes the note's text while the field is not being typed in
- **THEN** the new text shows with the cursor at the end, as "Note with a limit" says
