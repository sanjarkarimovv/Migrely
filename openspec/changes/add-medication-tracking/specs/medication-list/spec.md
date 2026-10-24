## Purpose

Defines the user's own list of medications: what a medication records, how it is added, edited and archived, and the
rules that keep the list clean, so that every intake can name what was taken and later be counted for overuse (BRD
MED-F1, MED-F2, EC-MED-01, EC-MED-07, D35).

## ADDED Requirements

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
My medications and the choices when logging SHALL list the most recently used medications first, then by name. At most
50 medications SHALL be active at once; at the limit, adding SHALL be refused with an explanation (BRD D35).

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
