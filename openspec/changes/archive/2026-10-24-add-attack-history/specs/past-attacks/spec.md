## ADDED Requirements

### Requirement: Add for a chosen day
When the add screen is opened for a day before today, "Started" SHALL be on that day at the clock time of the usual
default start, and "Ended" two hours later but never after the current minute. Opened for today, the add screen SHALL use
the usual defaults. Every other rule of this capability SHALL apply unchanged (BRD D30, D26).

#### Scenario: A past day
- **WHEN** it is 17:20 on 24 October and the add screen opens for 20 October
- **THEN** "Started" shows 20 October, 15:20 and "Ended" shows 20 October, 17:20

#### Scenario: Yesterday, late at night
- **WHEN** it is 01:10 on 24 October and the add screen opens for 23 October
- **THEN** "Started" shows 23 October, 23:10 and "Ended" shows 24 October, 01:10

#### Scenario: Today
- **WHEN** it is 17:20 on 24 October and the add screen opens for 24 October
- **THEN** "Started" shows 15:20 and "Ended" shows 17:20, both on 24 October
