## ADDED Requirements

### Requirement: Add a past migraine from home
When no attack is in progress, the home screen SHALL offer "Add a past migraine" under "Migraine started", in portrait
and in landscape. It SHALL open the add screen of `past-attacks`. Attack mode SHALL NOT offer it (BRD ATK-F3, 18.3 A,
18.4 G4, D26).

#### Scenario: Open from home
- **WHEN** no attack is in progress and the user taps "Add a past migraine"
- **THEN** the add screen opens

#### Scenario: Not in attack mode
- **WHEN** an attack is in progress
- **THEN** the home screen does not show "Add a past migraine"

#### Scenario: Landscape
- **WHEN** the phone is in landscape with no attack in progress
- **THEN** "Add a past migraine" is shown under "Migraine started"
