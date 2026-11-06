## ADDED Requirements

### Requirement: Names and doses at their caps
An edit that does not fit the medication name (60 characters, in the edit form or when adding one while logging) or the
dose (20 characters) SHALL insert only the whole characters that fit. It SHALL never remove text elsewhere in the field
or split a character such as an emoji. When nothing fits, the text and the cursor SHALL stay as they were. Text and
cursor SHALL survive recreation of the screen (BRD EC-MED-13, D35).

#### Scenario: Typing in the middle of a full name
- **WHEN** the name has 60 characters, in the edit form or when adding one while logging, and the user types a letter in
  the middle
- **THEN** the name is unchanged, its last character included, and the cursor stays where it was

#### Scenario: Typing in the middle of a full dose
- **WHEN** the dose has 20 characters and the user types a letter in the middle
- **THEN** the dose is unchanged, its last character included, and the cursor stays where it was

#### Scenario: An emoji that does not fit
- **WHEN** the name has 59 characters or the dose has 19 and the user types "👍", which takes two
- **THEN** the field is unchanged, and no half of the emoji is stored

#### Scenario: Pasting across the cap
- **WHEN** the dose has 16 characters and the user pastes "abc👍👍", where each "👍" takes two
- **THEN** "abc" is inserted, the dose has 19 characters, and the cursor is after "abc"

#### Scenario: The screen is recreated
- **WHEN** a name or dose holds typed text with the cursor in the middle and the screen is recreated
- **THEN** the text and the cursor are as they were

### Requirement: The edit form shows what it saves
A stored name or dose SHALL open with the cursor at its start, because a person recognises a medication by the start of
its name. What the form shows SHALL be what it saves: when two inputs race in one frame, the losing edit SHALL be dropped
from the field as well as from the save. A cursor move or a refused edit SHALL NOT change the form, so it SHALL NOT clear
a shown save problem (BRD EC-MED-13, D152).

#### Scenario: A long stored name
- **WHEN** a medication with a name wider than the field is opened for editing
- **THEN** the cursor is at the start of the name and dose, and the start of the name is on screen

#### Scenario: Typing and tapping a kind in one frame
- **WHEN** a letter typed in the name or dose and a tap on a kind arrive before the next frame
- **THEN** the kind changes, the field shows the same text that "Save" would store, and typing the letter again keeps it

#### Scenario: Deleting and retyping in one frame
- **WHEN** the cursor was moved, and a deletion and a retype in the name or dose arrive before the next frame
- **THEN** the field shows the same text that "Save" would store

#### Scenario: A cursor move or a refused edit
- **WHEN** a save problem shows and the user moves the cursor, or types in a full field
- **THEN** the form does not change and the save problem stays
