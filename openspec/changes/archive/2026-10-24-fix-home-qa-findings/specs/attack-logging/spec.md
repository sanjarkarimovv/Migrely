## ADDED Requirements

### Requirement: Double taps never cross a mode switch
After the home screen switches between normal mode and attack mode, every control of the new mode SHALL ignore taps
for 1 second, or until the switch animation ends when it is longer, so a second tap of a double tap never reaches the
control that replaced the one tapped first. Messages SHALL stay tappable. The guard SHALL NOT apply when the app opens
or the screen rotates (BRD EC-UX-18, EC-ATK-01, D32).

#### Scenario: Double tap on "Migraine started"
- **WHEN** no attack is in progress and the user double-taps "Migraine started" in its lower part, where "Migraine
  ended" appears
- **THEN** exactly one attack is in progress and attack mode is shown

#### Scenario: Double tap on "Migraine ended"
- **WHEN** an attack is in progress and the user double-taps "Migraine ended" in its upper part, where "Migraine
  started" appears
- **THEN** the attack has ended and no attack is in progress

#### Scenario: Deliberate tap after the guard
- **WHEN** attack mode has been shown for more than 1.2 seconds and the user taps "Migraine ended"
- **THEN** the attack ends

#### Scenario: Quick undo does not weaken the guard
- **WHEN** the user ends an attack, taps "Undo" within a second, and later double-taps "Migraine ended"
- **THEN** the attack has ended and no attack is in progress

#### Scenario: Opening the app
- **WHEN** the app opens on the home screen and the user taps "Migraine started" at once
- **THEN** the attack starts
