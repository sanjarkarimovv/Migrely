## ADDED Requirements

### Requirement: A start never falls inside an ended attack
The clock tolerance lets an ended attack end up to 5 minutes after now. When a new start falls inside such an attack,
the attack's end SHALL move to the start, and any of its pain entries after the start SHALL move to the start too, in
the same step as the start. An end further after the start, left by a phone clock moved back, SHALL stay as it is
(BRD EC-ATK-05, EC-ATK-19, EC-TIM-04).

#### Scenario: A past migraine ends a few minutes from now
- **WHEN** at 13:54 the user adds a past migraine from 11:54 to 13:58, then taps "Migraine started" at 13:54
- **THEN** the past migraine ends at 13:54 and the new attack starts at 13:54
- **AND** the two migraines touch at one instant and do not overlap

#### Scenario: The phone clock moved back
- **WHEN** the phone clock moved back an hour, so the previous migraine ends 50 minutes after the new start
- **THEN** the previous migraine keeps its end

#### Scenario: The previous migraine ended earlier
- **WHEN** the previous migraine ended at 13:30 and the user taps "Migraine started" at 14:00
- **THEN** the previous migraine still ends at 13:30

#### Scenario: Undoing the start
- **WHEN** the user taps "Undo" after such a start
- **THEN** the new attack is removed, and the earlier migraine keeps the end it was given
