## MODIFIED Requirements

### Requirement: Start an attack in one tap
The user SHALL be able to start an attack with a single tap on the home screen or from the launcher shortcut. A started
attack SHALL record the current time and the device's current time zone as its start. After starting, the app SHALL
offer to undo the start for the undo window defined in `in-app-messages`: 5 seconds, or longer when the user's
accessibility timeout asks for more (BRD ATK-F1, 6.1 item 13, EC-UX-16).

#### Scenario: Start from the home screen
- **WHEN** no attack is in progress and the user taps "Migraine started"
- **THEN** an attack in progress is stored with the current time and time zone as its start
- **AND** the home screen switches to attack mode

#### Scenario: Start from the launcher shortcut
- **WHEN** no attack is in progress and the user chooses "Migraine started" from the app icon's shortcut menu
- **THEN** the app opens with an attack in progress, started at the current time

#### Scenario: Undo a start
- **WHEN** the user taps "Undo" within the undo window after starting an attack
- **THEN** the attack is removed and nothing about it remains stored
- **AND** the home screen leaves attack mode

#### Scenario: Undo window passes
- **WHEN** the undo window passes after starting an attack without the user tapping "Undo"
- **THEN** the undo option disappears and the attack stays in progress

### Requirement: End an attack in one tap
The user SHALL be able to end the attack in progress with a single tap, which records the current time as its end.
After ending, the app SHALL offer to undo the end for the undo window defined in `in-app-messages` (BRD ATK-F2,
EC-UX-16).

#### Scenario: End an attack
- **WHEN** an attack is in progress and the user taps "Migraine ended"
- **THEN** the attack's end is stored as the current time
- **AND** the home screen leaves attack mode

#### Scenario: Undo an end
- **WHEN** the user taps "Undo" within the undo window after ending an attack
- **THEN** the attack is in progress again with its original start and intensity points
