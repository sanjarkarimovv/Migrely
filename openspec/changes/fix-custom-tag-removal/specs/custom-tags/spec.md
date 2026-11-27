## ADDED Requirements

### Requirement: Removing a tag says so and can be undone
Removing a custom tag from the list, by its menu, the TalkBack action, Switch Access or a keyboard, SHALL show "Removed
from your list" with Undo in Attack details' message host, with the screen's message timing. Undo SHALL un-archive the
tag, bring its chip back in place with its selection on this attack, and show "Undone.", unless that kind's list is full,
when it SHALL show "Your list is full. Remove one to add another." (BRD D199, EC-ATK-25, EC-ATK-26, EC-ATK-27).

#### Scenario: Removing from the menu
- **WHEN** the user long-presses "Jaw pain", which is not selected on this attack, and taps "Remove from my list"
- **THEN** the chip leaves the list and "Removed from your list" shows with "Undo" (EC-ATK-25)

#### Scenario: Removing with TalkBack's action
- **WHEN** a TalkBack user picks the custom action "Remove from my list" on a chip
- **THEN** "Removed from your list" is announced politely, once, and the message offers Undo, whose spoken name is "Undo
  removing Jaw pain"

#### Scenario: The check can fail
- **WHEN** the removal test runs against `develop`
- **THEN** no message shows and the test fails

#### Scenario: Undo brings it back
- **WHEN** the user taps Undo while the message shows
- **THEN** the chip is back in its place in the list with the same label and selected state, the tag is offered on other
  attacks again, and "Undone." shows

#### Scenario: A tag selected on this attack
- **WHEN** the removed tag is selected on this attack
- **THEN** it stays shown and selected on this attack and on past attacks that use it (EC-ATK-11), the message shows, and
  Undo makes it offered on new attacks again

#### Scenario: The list filled up before Undo
- **WHEN** the user removed a tag, then added tags until that kind's list holds 100, and then taps Undo
- **THEN** "Your list is full. Remove one to add another." shows, the tag stays archived, and adding its name later still
  brings it back (EC-ATK-26, EC-ATK-12)

#### Scenario: A second removal
- **WHEN** the user removes a second tag while the first message shows
- **THEN** the second message replaces the first, Undo restores only the second tag, and the first can be re-added by name
  (EC-ATK-27)

#### Scenario: Leaving the screen
- **WHEN** the user leaves Attack details while the message shows
- **THEN** the Undo ends with the message, and the tag stays archived (EC-ATK-27)

#### Scenario: The write fails
- **WHEN** storage is full or the write fails when removing, or when undoing
- **THEN** the screen's existing save-error message shows; on a failed removal the chip stays, on a failed Undo the tag
  stays archived (EC-ATK-27)

#### Scenario: Keyboard focus
- **WHEN** a keyboard user removes a chip, or undoes the removal
- **THEN** focus moves to the next chip in the section, or to "+ Your own" if the removed chip was last; after Undo it
  returns to the restored chip

#### Scenario: TalkBack focus after removal
- **WHEN** a TalkBack user removes a chip or closes the menu without removing
- **THEN** focus is not forced; QA records where it lands, and the next double tap never toggles another chip

#### Scenario: German
- **WHEN** the app is German
- **THEN** the message reads "Aus deiner Liste entfernt", the button "Rückgängig", its spoken name "Rückgängig: Jaw pain
  zurück in die Liste", which starts with the visible word (EC-UX-45)

#### Scenario: Long and unusual names
- **WHEN** the tag's name has 40 characters, emoji or letters with accents
- **THEN** the visible message stays the same short text, and the spoken Undo name carries the whole name unchanged

#### Scenario: 200% font and landscape
- **WHEN** the message shows at 200% font, in portrait at 360 dp and in a 360 dp tall landscape window
- **THEN** it wraps without "…", covers no control, and Undo stays reachable, as the screen's other messages do

#### Scenario: Rotation while the message shows
- **WHEN** the phone is rotated while the message shows
- **THEN** the message and its Undo behave as the screen's other Undo messages do on rotation

#### Scenario: During an attack, Free or Premium
- **WHEN** Attack details is open during an attack, or the user is Free or Premium
- **THEN** the same message and Undo show, in the dark theme during an attack; nothing is Premium

#### Scenario: Backup and restore
- **WHEN** a backup is made after a removal or after an Undo
- **THEN** it holds the tag's archived state as stored, as before; no new data is kept for the message
