## ADDED Requirements

### Requirement: Medications taken
The details screen SHALL list the attack's intakes, oldest first, each with its medication, dose and time; an archived
medication SHALL still be named. Removing an intake SHALL be undoable. "Add medication" SHALL open the "Medication taken"
screen for this attack (BRD ATK-F5, MED-F3, EC-MED-01, D34).

#### Scenario: Intakes listed
- **WHEN** the attack has Ibuprofen 400 mg at 14:20 and Sumatriptan 50 mg at 16:00
- **THEN** the "Medications" section lists "Ibuprofen 400 mg · 14:20" and then "Sumatriptan 50 mg · 16:00"

#### Scenario: Remove with undo
- **WHEN** the user removes Ibuprofen and taps "Undo"
- **THEN** Ibuprofen is listed again

#### Scenario: Add from details
- **WHEN** the attack ended and the user taps "Add medication"
- **THEN** the "Medication taken" screen opens with the time set to the attack's start
