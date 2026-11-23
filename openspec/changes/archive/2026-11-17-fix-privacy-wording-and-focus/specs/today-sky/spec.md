## ADDED Requirements

### Requirement: The quiet links stack instead of breaking a word
"Add a past migraine" and "Log medication" SHALL sit side by side only while each label's longest word fits its half.
Otherwise they SHALL stack, each full width and at least 48 dp tall, "Add a past migraine" on top. Tab, TalkBack and
Switch Access order SHALL stay "Add a past migraine", then "Log medication", which stays the last stop in both layouts
(BRD EC-UX-48, D26, D34, D181).

#### Scenario: Default font size
- **WHEN** the font size is 100% on a 360 dp phone in portrait
- **THEN** the two links sit side by side as before

#### Scenario: Largest font size in portrait
- **WHEN** the font size is 200% on a 360 dp phone in portrait
- **THEN** the links stack, each full width, at least 48 dp tall, with whole words, and "Log medication" is not broken as
  "medicatio / n"

#### Scenario: Short window
- **WHEN** the phone is in a short landscape window at 200% font
- **THEN** the links in the end column stack, and at 100% they sit side by side

#### Scenario: Keyboard order in both layouts
- **WHEN** the links are stacked or side by side and the user presses Tab from "Migraine started"
- **THEN** focus goes to "Add a past migraine", then "Log medication", and Tab past "Log medication" wraps as before

#### Scenario: Attack mode
- **WHEN** an attack is in progress
- **THEN** neither link shows, as before
