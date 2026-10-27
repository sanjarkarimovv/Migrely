## MODIFIED Requirements

### Requirement: End an attack in one tap
The user SHALL be able to end the attack in progress with a single tap, which records the current time as its end.
After ending, the app SHALL offer to undo the end for the undo window defined in `in-app-messages`. Undo SHALL NOT
reopen the attack when, in progress again, it would overlap another attack: the attack then stays ended and the app
says the end can no longer be undone (BRD ATK-F2, EC-UX-16, EC-ATK-20).

#### Scenario: End an attack
- **WHEN** an attack is in progress and the user taps "Migraine ended"
- **THEN** the attack's end is stored as the current time
- **AND** the home screen leaves attack mode

#### Scenario: Undo an end
- **WHEN** the user taps "Undo" within the undo window after ending an attack
- **THEN** the attack is in progress again with its original start and intensity points

#### Scenario: Undo after a past migraine was added after the end
- **WHEN** at 14:00 the user answers "It has ended" for an attack started three days ago and picks 13:10 as its end,
  adds a past migraine from 13:20 to 13:30 within the undo window, and then taps "Undo"
- **THEN** the attack stays ended at 13:10 and the past migraine is unchanged
- **AND** the app says the end can no longer be undone

#### Scenario: Undo after a past migraine starting at the end
- **WHEN** the user taps "Migraine ended" at 13:30, adds a past migraine from 13:30 to 13:32 within the undo window,
  and then taps "Undo"
- **THEN** the attack stays ended at 13:30
- **AND** the app says the end can no longer be undone
