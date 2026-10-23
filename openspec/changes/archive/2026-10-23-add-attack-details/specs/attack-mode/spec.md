## MODIFIED Requirements

### Requirement: Dark room while an attack is in progress
While an attack is in progress the home screen SHALL be shown in attack mode: a background darker than the normal home
screen, a single line of guidance text, the start time with the elapsed time, and only these controls: pain intensity
(Mild, Moderate, Severe), moving the start earlier, "Migraine ended", and a dim "Details" link that opens the attack's
details (BRD section 18.3 B, D24).

#### Scenario: Attack mode content
- **WHEN** an attack started at 14:05 has been in progress for 42 minutes
- **THEN** the home screen shows "Rest now. We'll keep time." and "Started 14:05 · 42 min"
- **AND** shows the intensity choices, the option to move the start earlier, "Migraine ended" and the "Details" link,
  and no other controls

#### Scenario: Elapsed time keeps up
- **WHEN** the attack mode screen stays open
- **THEN** the elapsed time is updated at least once every minute

#### Scenario: Describe a long attack
- **WHEN** an attack has been in progress for two days and the user taps "Details"
- **THEN** the details screen of that attack opens
