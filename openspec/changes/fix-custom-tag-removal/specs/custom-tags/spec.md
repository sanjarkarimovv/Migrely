## ADDED Requirements

### Requirement: Removing a tag says so and can be undone
Removing a custom tag from the list, by its menu, the TalkBack action, Switch Access or a keyboard, SHALL show "Removed
from your list" with Undo in Attack details' message host, spoken once on every path. Undo SHALL un-archive the tag,
restore its chip with its selection on this attack and show "Undone.", unless that kind's list is full, when it SHALL
show "Your list is full. Remove one to add another." (BRD D199, EC-ATK-25, EC-ATK-26, EC-ATK-27).

#### Scenario: Removing from the menu
- **WHEN** the user long-presses "Jaw pain", which is not selected on this attack, and taps "Remove from my list"
- **THEN** the chip leaves the list and "Removed from your list" shows with "Undo" (EC-ATK-25)

#### Scenario: Removing with TalkBack's action
- **WHEN** a TalkBack user picks the custom action "Remove from my list" on a chip
- **THEN** "Removed from your list" is spoken once, politely, and the message offers Undo, whose spoken name is "Undo
  removing Jaw pain"

#### Scenario: Removing with TalkBack through the menu
- **WHEN** a TalkBack user double-taps and holds a chip, then double-taps "Remove from my list" in the menu
- **THEN** "Removed from your list" is spoken once after the menu has closed, and TalkBack's log shows the live-region
  utterance; the menu's closing never swallows it (the build at 2d3ca53f spoke it 0 of 6 times)

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

#### Scenario: Focus after a removal, a known limit
- **WHEN** a TalkBack or keyboard user removes a chip
- **THEN** the app moves no focus, as on `develop`: focus lands on the chip that slides into the removed chip's place, or
  on "Add your own symptom" after the last one; TalkBack may say that chip's state before its name, and a double tap in
  the first seconds can select it on this attack, which is visible, saved and undone by one more double tap
- **AND** this pre-existing behaviour and the keyboard targets after removal and after Undo are tracked in #296 (1.5.0),
  not in this change

#### Scenario: German
- **WHEN** the app is German
- **THEN** the message reads "Aus deiner Liste entfernt", the button "Rückgängig", and its spoken name "Rückgängig: Jaw
  pain zurück in deine Liste", which starts with the visible word (EC-UX-45)

#### Scenario: Long and unusual names
- **WHEN** the tag's name has 40 characters, emoji or letters with accents
- **THEN** the visible message stays the same short text, and the spoken Undo name carries the whole name unchanged

#### Scenario: 200% font and landscape
- **WHEN** the message shows at 200% font, in portrait at 360 dp and in a 360 dp tall landscape window
- **THEN** it wraps without "…", sits where the screen's other messages sit (the "Messages appear at the top and stay dim"
  rule), which may lie over the page's first row of chips, and Undo stays inside the window and reachable

#### Scenario: Rotation while the message shows
- **WHEN** the phone is rotated while the message shows
- **THEN** the message and its Undo behave as the screen's other Undo messages do on rotation

#### Scenario: During an attack, Free or Premium
- **WHEN** Attack details is open during an attack, or the user is Free or Premium
- **THEN** the same message and Undo show, in the dark theme during an attack; nothing is Premium

#### Scenario: Backup and restore
- **WHEN** a backup is made after a removal or after an Undo
- **THEN** it holds the tag's archived state as stored, as before; no new data is kept for the message

### Requirement: An archived chip offers no remove
A custom tag that is archived but still shown on an attack SHALL offer no remove: no long-press menu or label and no
"Remove from my list" action, until Undo un-archives it. A long press on it SHALL do nothing and change no
selection; a plain tap SHALL work as before (BRD EC-ATK-11, EC-ATK-28, D199 (3)).

#### Scenario: The removed tag was selected on this attack
- **WHEN** the user removes "Jaw pain", which is selected on this attack, and the chip stays shown
- **THEN** a long press opens no menu, the chip has no long-press label, and TalkBack's actions offer no "Remove from my
  list"; a second "Removed from your list" can never be shown for it (EC-ATK-28)

#### Scenario: A long press on an archived chip
- **WHEN** the user long-presses the archived "Jaw pain" chip with a finger, or double-taps and holds it with TalkBack, on
  this attack or on a past attack
- **THEN** nothing happens: no menu opens, the chip stays selected, nothing is written, and the chip stays on the attack
  (on the code at d4b2dcac the long press acted as a tap, deselected it, and the chip vanished with no message)

#### Scenario: A plain tap on an archived chip
- **WHEN** the user taps the archived chip
- **THEN** it toggles as before (pre-existing, EC-ATK-11): deselected, it leaves the attack after the reload

#### Scenario: Undo brings the remove back
- **WHEN** the user taps Undo for that removal
- **THEN** the chip offers the menu, the long-press label and the TalkBack action again

#### Scenario: A past attack shows an archived tag
- **WHEN** the user opens a past attack that uses a tag removed earlier
- **THEN** that chip shows as selected and offers no remove

#### Scenario: The check can fail
- **WHEN** the archived-chip test runs against the code at 2d3ca53f (or `develop`)
- **THEN** the archived chip still offers the menu and the action, and the test fails
