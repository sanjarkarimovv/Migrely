## MODIFIED Requirements

### Requirement: Delete with undo
The user SHALL be able to delete an attack from its details screen without a confirmation dialog. The app SHALL return
to the home screen and show "Migraine deleted" with "Undo" for the undo window of `in-app-messages`. Undo SHALL restore
the attack with all its details. After the window the attack SHALL be unrecoverable and SHALL never be shown, counted or
exported. A deletion stamped after now SHALL never be offered (BRD ATK-F7, EC-ATK-14, EC-ATK-21, D23).

#### Scenario: Delete and undo
- **WHEN** the user deletes an attack and taps "Undo" on the home screen within the window
- **THEN** the attack is back with its times, pain entries and details

#### Scenario: Window passes
- **WHEN** the user deletes an attack and does not tap "Undo"
- **THEN** the message disappears and the attack is never shown again

#### Scenario: Delete the attack in progress
- **WHEN** the user deletes the attack in progress
- **THEN** the home screen leaves attack mode and offers "Undo"

#### Scenario: Clock set back after a deletion
- **WHEN** the user deleted an attack with the phone's clock three days ahead, the clock was then set back, and the
  app is opened again
- **THEN** neither home nor History offers "Undo" for it, and the attack is gone for good

#### Scenario: A real deletion after one stamped ahead
- **WHEN** after that the user deletes another attack
- **THEN** home offers "Undo" for that one
