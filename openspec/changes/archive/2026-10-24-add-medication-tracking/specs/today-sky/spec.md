## ADDED Requirements

### Requirement: Log a medication from home
In the normal mode, the home screen SHALL offer "Log medication" next to "Add a past migraine", which opens the
"Medication taken" screen on its own. Attack mode SHALL offer "+ Medication taken" instead (BRD MED-F3, D34, G4).

#### Scenario: Log from home
- **WHEN** no attack is in progress and the user taps "Log medication"
- **THEN** the "Medication taken" screen opens with the time set to now
