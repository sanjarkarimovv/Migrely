# medication-list Specification

## Purpose
Defines the user's own list of medications: what a medication records, how it is added, edited and archived, and the
rules that keep the list clean, so that every intake can name what was taken and later be counted for overuse (BRD
MED-F1, MED-F2, EC-MED-01, EC-MED-07, D35).

## Requirements

### Requirement: Add a medication
The user SHALL be able to add a medication with a name of 1 to 60 characters, a kind (acute or preventive) and, for an
acute medication, a category: simple painkiller, triptan, combination or other, or "Not sure". The user SHALL also be
able to give an optional default dose of at most 20 characters. Surrounding spaces SHALL be trimmed (BRD MED-F1,
MED-F2, D35).

#### Scenario: Add an acute medication
- **WHEN** the user adds "Ibuprofen", acute, simple painkiller, default dose "400 mg"
- **THEN** "Ibuprofen · 400 mg" appears in My medications

#### Scenario: Preventive has no category
- **WHEN** the user chooses preventive
- **THEN** no category is asked

#### Scenario: Name too long
- **WHEN** the user types a 61st character in the name
- **THEN** the field stops at 60 characters and a counter shows the limit

### Requirement: Names are unique
Two medications SHALL NOT share a name, ignoring case and surrounding spaces. Adding the name of an active medication
SHALL be refused with a message naming it. Adding the name of an archived medication SHALL bring that medication back
with its history (BRD EC-MED-07).

#### Scenario: Duplicate name
- **WHEN** "Ibuprofen" exists and the user adds " ibuprofen "
- **THEN** nothing is added and "You already have Ibuprofen." is shown

#### Scenario: Renaming to an archived name
- **WHEN** "Sumatriptan" is archived and the user renames another medication to "Sumatriptan"
- **THEN** nothing is saved and "Sumatriptan is in your archive. Add it as a new medication to bring it back." is shown

#### Scenario: Archived name
- **WHEN** "Sumatriptan" (Triptan, 50 mg) was archived and the user adds "sumatriptan" with "Not sure" and no dose
- **THEN** Sumatriptan is active again as it was, Triptan with 50 mg, and keeps its past intakes

### Requirement: Edit a medication
The user SHALL be able to change a medication's name, kind, category and default dose under the same rules. Past
intakes SHALL show the new name (BRD D35).

#### Scenario: Rename
- **WHEN** the user renames "Ibuprofen" to "Ibuprofen 400"
- **THEN** past intakes show "Ibuprofen 400"

### Requirement: Archive instead of delete
Removing a medication SHALL archive it. An archived medication SHALL disappear from My medications and from the
choices when logging, and SHALL stay in the history of every intake. Archiving SHALL be undoable from the message
(BRD EC-MED-01, EC-UX-16).

#### Scenario: Archive
- **WHEN** the user archives "Paracetamol", which was taken last week
- **THEN** Paracetamol is no longer offered, "Medication removed" with "Undo" is shown, and last week's attack still
  lists it

### Requirement: Order and limit
My medications SHALL list the most recently used medications first, then by name; the choices when logging put acute
medications before preventive ones in that order (see medication-intakes). At most 50 medications SHALL be active at
once; at the limit, adding, and "Undo" after archiving, SHALL be refused with an explanation (BRD D35).

#### Scenario: Recently used first
- **WHEN** Sumatriptan was taken yesterday and Ibuprofen last month
- **THEN** Sumatriptan is listed before Ibuprofen

#### Scenario: Limit reached
- **WHEN** 50 medications are active and the user tries to add another
- **THEN** it is refused with "You can keep up to 50 medications. Remove one you no longer take."

### Requirement: Accessible medication screens
The medication screens SHALL work with TalkBack, keyboards and switch access, and at 200% font and in landscape,
without clipped text (BRD EC-UX-03, EC-UX-04, EC-UX-19).

#### Scenario: Screen reader on a medication
- **WHEN** TalkBack focuses "Ibuprofen · 400 mg"
- **THEN** it reads the name, the dose and the category, for example "Ibuprofen, 400 mg, simple painkiller. Opens it to
  edit."

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
