## ADDED Requirements

### Requirement: Add your own symptom or trigger
On an attack's details screen the user SHALL be able to add their own symptom or trigger by name. The name SHALL be
trimmed and SHALL be 1 to 40 characters. The new tag SHALL be added to the user's list for that kind and selected on
the attack (BRD ATK-F5, EC-ATK-12).

#### Scenario: Add a symptom
- **WHEN** the user adds the symptom "Jaw pain"
- **THEN** "Jaw pain" appears in the symptom choices of every attack and is selected on this one

#### Scenario: Name too long
- **WHEN** the user enters a 41-character name
- **THEN** input stops at 40 characters

#### Scenario: Empty name
- **WHEN** the user enters only spaces
- **THEN** nothing can be added

### Requirement: No duplicate names
A name that matches an existing tag of the same kind, ignoring case and surrounding spaces, SHALL NOT create a new tag.
Instead the existing tag SHALL be selected on the attack, and an archived match SHALL return to the list (BRD
EC-ATK-12).

#### Scenario: Same name, different case
- **WHEN** "Jaw pain" exists and the user adds "jaw PAIN "
- **THEN** no new tag is created and "Jaw pain" is selected on the attack

#### Scenario: Archived match
- **WHEN** "Jaw pain" was removed from the list and the user adds "Jaw pain" again
- **THEN** the original "Jaw pain" returns to the list and is selected

### Requirement: At most 100 per kind
The user SHALL have at most 100 custom symptoms and 100 custom triggers in their list. Adding one more SHALL be refused
with a message (BRD EC-ATK-12).

#### Scenario: Limit reached
- **WHEN** the user has 100 custom triggers and adds another
- **THEN** a message says the list is full and nothing is added

### Requirement: Remove from the list without losing history
The user SHALL be able to remove a custom tag from their list. It SHALL then no longer be offered on attacks that do
not use it, but SHALL stay stored and shown on the attacks that do (BRD EC-ATK-11).

#### Scenario: Remove a used tag
- **WHEN** "Jaw pain" is selected on last week's attack and the user removes it from the list
- **THEN** new attacks no longer offer "Jaw pain"
- **AND** last week's attack still shows "Jaw pain" as selected
