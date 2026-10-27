## ADDED Requirements

### Requirement: A start never overlaps an ended attack
A started attack SHALL NOT overlap an ended attack. An ended attack's end can lie after the new start, because the
clock tolerance allows it. In that case its end SHALL move to the start, and any of its pain entries after the start
SHALL move to the start too, in the same step as the start (BRD EC-ATK-05, EC-ATK-19).

#### Scenario: A past migraine ends a few minutes from now
- **WHEN** at 13:54 the user adds a past migraine from 11:54 to 13:58, then taps "Migraine started" at 13:54
- **THEN** the past migraine ends at 13:54 and the new attack starts at 13:54
- **AND** the two migraines touch at one instant and do not overlap

#### Scenario: The previous migraine ended earlier
- **WHEN** the previous migraine ended at 13:30 and the user taps "Migraine started" at 14:00
- **THEN** the previous migraine still ends at 13:30

#### Scenario: Undoing the start
- **WHEN** the user taps "Undo" after such a start
- **THEN** the new attack is removed, and the earlier migraine keeps the end it was given
