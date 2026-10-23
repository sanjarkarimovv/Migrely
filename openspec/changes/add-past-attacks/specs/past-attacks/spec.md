## ADDED Requirements

### Requirement: Add an attack that has ended
The user SHALL be able to add an attack that has already ended by picking its start and end. The screen SHALL open with
the start two hours before now and the end now, both rounded down to the minute. "Add migraine" SHALL store the attack in
the device's current time zone and open its details screen. Going back from there SHALL return home (BRD ATK-F3, D26,
EC-TIM-01).

#### Scenario: Defaults
- **WHEN** the user opens "Add a past migraine" at 16:42:30
- **THEN** "Started" shows 14:42 and "Ended" shows 16:42

#### Scenario: Add and describe
- **WHEN** the user picks yesterday 21:00 to 23:30 and taps "Add migraine"
- **THEN** the attack is stored and its details screen opens with those times and the pain "Not recorded"

#### Scenario: Back from the new attack
- **WHEN** the user goes back from the details screen of the attack just added
- **THEN** the home screen is shown, not the add screen

#### Scenario: Leave without adding
- **WHEN** the user goes back from the add screen
- **THEN** nothing is stored and the home screen is shown

### Requirement: Moving the start keeps the duration
When the user picks a new start, the end SHALL move by the same amount, so the duration stays the same, but never past
the current minute. The end SHALL be pickable from one minute after the start up to the clock tolerance after now (BRD
D26, EC-ATK-03).

#### Scenario: Last night
- **WHEN** the screen shows 14:42 to 16:42 and the user picks yesterday 21:00 as the start
- **THEN** "Ended" shows yesterday 23:00 and the duration stays 2 h

#### Scenario: Never past now
- **WHEN** it is 16:42 and the user picks 16:00 today as the start of a 2-hour attack
- **THEN** "Ended" shows 16:42

### Requirement: Times in the past only
A start or end more than 5 minutes after now, and an end that is not after the start, SHALL NOT be stored. Each SHALL be
shown as an inline error under the time concerned (BRD EC-ATK-03, EC-ATK-04, EC-TIM-04).

#### Scenario: End in the future
- **WHEN** the phone's clock moves back while the screen is open, so the picked end is now 10 minutes in the future, and
  the user taps "Add migraine"
- **THEN** an inline error under "Ended" says the time is in the future and nothing is stored

#### Scenario: Clock tolerance
- **WHEN** the picked end is 3 minutes after the phone's current time
- **THEN** the attack is stored

#### Scenario: End not after the start
- **WHEN** the end is not after the start when the user taps "Add migraine"
- **THEN** an inline error under "Ended" says the end must be after the start and nothing is stored

### Requirement: Overlapping attacks are named
A past attack that overlaps other attacks SHALL NOT be stored. A card SHALL name every overlapping migraine with its
times, oldest first, and offer "Merge them" and "Change the times". Attacks that only touch at one instant SHALL NOT
overlap. An overlap shorter than a minute SHALL snap to the other attack's boundary instead (BRD EC-ATK-05, EC-ATK-15,
EC-ATK-18).

#### Scenario: One overlap
- **WHEN** a migraine ran 12:00–13:30 and the user adds 13:00–15:00
- **THEN** nothing is stored and a card says it overlaps the migraine from 12:00 to 13:30, with "Merge them" and
  "Change the times"

#### Scenario: Two overlaps
- **WHEN** migraines ran 09:00–10:00 and 12:00–13:30 and the user adds 08:00–14:00
- **THEN** the card names both migraines, 09:00–10:00 first

#### Scenario: Change the times
- **WHEN** the card is shown and the user taps "Change the times" or picks a new start or end
- **THEN** the card disappears and the times stay as they were picked

#### Scenario: Touching attacks
- **WHEN** a migraine ended at 13:30 and the user adds 13:30–15:00
- **THEN** the attack is stored

#### Scenario: Seconds do not overlap
- **WHEN** a migraine ended at 13:30:40 and the user adds 13:30–15:00
- **THEN** the attack is stored starting at 13:30:40

### Requirement: Merge overlapping attacks
"Merge them" SHALL combine the attacks into one: the edited attack, or for a new attack the earliest overlapping one.
Its times SHALL cover every merged attack and pain entry and never shrink. Pain entries and tags SHALL be combined, the
highest impact kept, and non-empty notes joined oldest first with a blank line. It SHALL be in progress if any was. The
rest SHALL be removed in the same step, with no undo (BRD EC-ATK-05, D27, D28).

#### Scenario: Merge a new attack
- **WHEN** a migraine ran 12:00–13:30 with a pain entry of 6 and "Nausea", the user adds 13:00–15:00 and taps "Merge
  them"
- **THEN** one migraine from 12:00 to 15:00 with the entry of 6 and "Nausea" remains, its details screen opens, and
  "Migraines merged." is shown

#### Scenario: Merge three into one
- **WHEN** the user adds 08:00–14:00 over migraines at 09:00–10:00 (impact "Had to stop", note "Bright office") and
  12:00–13:30 (impact "Bed rest", note "Skipped lunch") and taps "Merge them"
- **THEN** one migraine from 08:00 to 14:00 remains with impact "Bed rest" and the note "Bright office", a blank line
  and "Skipped lunch"

#### Scenario: Merge with the attack in progress
- **WHEN** a migraine ran 10:00–11:00, another has been in progress since 11:30, and on the first one's details screen
  the user moves its end to 12:00 and taps "Merge them"
- **THEN** one migraine in progress since 10:00 remains and the home screen shows attack mode for it

#### Scenario: Nothing overlaps any more
- **WHEN** the overlapping migraine was deleted before the user taps "Merge them"
- **THEN** the new attack is stored as picked and its details screen opens without "Migraines merged."

### Requirement: Merged notes stay within the limit
A note that results from a merge SHALL hold at most 1,000 characters. When the joined notes are longer, the end of the
newest note SHALL be cut, and no character SHALL be split in half (BRD EC-ATK-13, EC-ATK-16).

#### Scenario: Long notes
- **WHEN** two migraines with notes of 700 characters each are merged
- **THEN** the merged note is the first note, a blank line and the first 298 characters of the second

### Requirement: One attack per tap
Tapping "Add migraine" or "Merge them" again before the result arrives SHALL NOT store a second attack or merge twice.
Storing a past attack SHALL check for overlaps again in the same step that writes it (BRD EC-ATK-17).

#### Scenario: Double tap
- **WHEN** the user double-taps "Add migraine"
- **THEN** exactly one attack is stored and one details screen opens

### Requirement: Picked times survive interruptions
Times picked on the add screen SHALL survive rotation and the app being closed by the system while in the background.
Nothing SHALL be stored until the user taps "Add migraine" or "Merge them" (BRD EC-ATK-10).

#### Scenario: Rotation
- **WHEN** the user picks yesterday 21:00 as the start and rotates the phone
- **THEN** "Started" still shows yesterday 21:00

#### Scenario: Process death
- **WHEN** the user picks times, leaves the app and the system ends it, then returns
- **THEN** the add screen shows the picked times and nothing has been stored

### Requirement: Accessible add screen
Each time row SHALL read as one control with its label and value, the overlap card SHALL be announced when it appears,
and every button SHALL be at least 48 dp high. The screen SHALL stay usable at a 200% font size and in landscape (BRD
EC-UX-03, EC-UX-04).

#### Scenario: Screen reader on a time
- **WHEN** TalkBack focuses the start row
- **THEN** it reads "Change when it started, now" followed by the start

#### Scenario: Card announced
- **WHEN** the overlap card appears
- **THEN** TalkBack announces that it overlaps the named migraine

#### Scenario: Largest font size in landscape
- **WHEN** the font size is 200% and the phone is in landscape
- **THEN** the times, the card and "Add migraine" can all be reached by scrolling
