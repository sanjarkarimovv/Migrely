## MODIFIED Requirements

### Requirement: Add your own symptom or trigger
On an attack's details screen the user SHALL be able to add their own symptom or trigger by name. The name SHALL be
trimmed and SHALL be 1 to 40 characters. An edit that does not fit SHALL insert only the whole characters that fit,
never removing text elsewhere in the name or splitting a character such as an emoji. When nothing fits, the name and the
cursor SHALL stay as they were. The new tag SHALL be added to the user's list for that kind and selected on the attack
(BRD ATK-F5, EC-ATK-12).

#### Scenario: Add a symptom
- **WHEN** the user adds the symptom "Jaw pain"
- **THEN** "Jaw pain" appears in the symptom choices of every attack and is selected on this one

#### Scenario: Name too long
- **WHEN** the user enters a 41-character name
- **THEN** input stops at 40 characters

#### Scenario: Empty name
- **WHEN** the user enters only spaces
- **THEN** nothing can be added

#### Scenario: Typing in the middle of a full name
- **WHEN** the name has 40 characters and the user types a letter in the middle
- **THEN** the name is unchanged, its last character included, and the cursor stays where it was

#### Scenario: An emoji that does not fit
- **WHEN** the name has 39 characters and the user types "👍", which takes two
- **THEN** the name is unchanged, and no half of the emoji is stored

#### Scenario: Pasting across the cap
- **WHEN** the name has 36 characters and the user pastes "abc👍👍", where each "👍" takes two
- **THEN** "abc" is inserted, the name has 39 characters, and the cursor is after "abc"

#### Scenario: The screen is recreated
- **WHEN** the dialog holds a typed name with the cursor in the middle and the screen is recreated
- **THEN** the name and the cursor are as they were
