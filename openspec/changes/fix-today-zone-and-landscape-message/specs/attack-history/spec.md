## MODIFIED Requirements

### Requirement: The month's attacks
Under the calendar, History SHALL list every attack that touches the shown month, newest first. Each row SHALL show its
day, its start and end, or "In progress", and its peak, or "Not recorded". Times in another zone than the device's
SHALL name that zone's city once, after the last time shown. A title SHALL count them, and tapping a row SHALL open
that attack's details. The list SHALL load one month at a time (BRD HIS-F2, EC-HIS-02, EC-TIM-02, D36).

#### Scenario: Newest first
- **WHEN** October has attacks on 3, 14 and 20 October
- **THEN** History lists 20, 14 and 3 October in that order under "3 migraines in October"

#### Scenario: Attack across two months
- **WHEN** an attack ran from 23:00 on 30 September to 02:00 on 1 October
- **THEN** it is listed both in September and in October

#### Scenario: Empty month
- **WHEN** nothing was logged in September
- **THEN** History shows "No migraines logged in September" under the calendar

#### Scenario: Logged in another zone
- **WHEN** an attack ran on 14 October from 23:15 to 23:45 Berlin time and History is viewed in Tashkent
- **THEN** its row shows "Wed, Oct 14, 11:15 PM – 11:45 PM (Berlin)", with the city once
- **AND** an attack in progress since 23:15 Berlin time shows "Wed, Oct 14, 11:15 PM (Berlin)" and "In progress"

#### Scenario: Open an attack
- **WHEN** the user taps a listed attack
- **THEN** its details screen opens, where it can be edited or deleted
