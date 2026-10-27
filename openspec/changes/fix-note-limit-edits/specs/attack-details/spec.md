## MODIFIED Requirements

### Requirement: Note with a limit
The user SHALL be able to write a note of up to 1,000 characters. Input SHALL stop at 1,000 characters and a counter
SHALL show the characters used. An edit that does not fit SHALL insert only the whole characters that fit. It SHALL
never remove text elsewhere in the note or split a character such as an emoji. When nothing fits, the note and the
cursor SHALL stay as they were (BRD ATK-F5, EC-ATK-13).

#### Scenario: Reaching the limit
- **WHEN** the note has 1,000 characters and the user types another
- **THEN** the note stays at 1,000 characters and the counter shows "1000/1000"

#### Scenario: Typing in the middle of a full note
- **WHEN** the note has 1,000 characters and the user types a letter in the middle
- **THEN** the note is unchanged, its last character included, and the cursor stays where it was

#### Scenario: An emoji that does not fit
- **WHEN** the note has 999 characters and the user types "👍", which takes two
- **THEN** the note is unchanged, and no half of the emoji is stored

#### Scenario: Pasting across the limit
- **WHEN** the note has 995 characters and the user pastes "abc👍👍"
- **THEN** "abc" is inserted, the note has 998 characters, and the cursor is after "abc"
