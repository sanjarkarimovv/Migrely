## MODIFIED Requirements

### Requirement: Times never overlap another attack
A start or end that would make the attack overlap other attacks SHALL be rejected with an inline error that names every
overlapping attack's start and end, oldest first, and offers "Merge them". Merging SHALL follow `past-attacks`, keep this
attack, and show "Migraines merged.". Attacks that only touch at one instant SHALL NOT count as overlapping, and an
overlap shorter than a minute SHALL snap to the other attack's boundary (BRD EC-ATK-05, EC-ATK-15, EC-ATK-18, D27).

#### Scenario: Overlapping start
- **WHEN** another attack ran 12:00–13:30 and the user moves this attack's start from 14:05 to 13:00
- **THEN** an inline error says it would overlap the migraine from 12:00 to 13:30, offers "Merge them", and the start
  stays 14:05

#### Scenario: Two overlapping attacks
- **WHEN** attacks ran 09:00–10:00 and 12:00–13:30 and the user moves this attack's start from 14:05 to 08:30
- **THEN** the inline error names both attacks, 09:00–10:00 first

#### Scenario: Merge from the details screen
- **WHEN** the inline error names the migraine from 12:00 to 13:30 and the user taps "Merge them"
- **THEN** this attack runs from 12:00 to its end with the other attack's pain entries and details, the other attack
  is gone, and "Migraines merged." is shown

#### Scenario: Touching attacks
- **WHEN** another attack ended at 13:30 and the user sets this attack's start to 13:30
- **THEN** the start is stored

#### Scenario: Seconds do not overlap
- **WHEN** another attack ended at 13:30:40 and the user sets this attack's start to 13:30
- **THEN** the start is stored as 13:30:40
