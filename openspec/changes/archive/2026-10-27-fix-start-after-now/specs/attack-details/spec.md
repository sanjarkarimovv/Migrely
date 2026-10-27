## MODIFIED Requirements

### Requirement: Exact start and end times
The user SHALL be able to set the start, and the end of an ended attack, to any date and time with a date picker
followed by a time picker. Times SHALL be shown and picked in the attack's time zone, in the device's 12- or 24-hour
format. A start after now, or an end more than 5 minutes after now, SHALL be rejected. An end before the start SHALL
be rejected. Each rejection SHALL be shown as an inline error under the time, and nothing SHALL be stored (BRD
EC-ATK-03, EC-ATK-04, EC-TIM-02, EC-TIM-04, EC-TIM-05).

#### Scenario: Correct the start
- **WHEN** the user changes the start of an attack from 14:05 to 13:40
- **THEN** the start is stored as 13:40 and the duration updates

#### Scenario: End in the future
- **WHEN** the user picks an end 20 minutes after now
- **THEN** an inline error says the time cannot be in the future and the end is unchanged

#### Scenario: Clock tolerance
- **WHEN** the user picks an end 3 minutes after the phone's current time
- **THEN** the end is stored

#### Scenario: End before start
- **WHEN** the user picks an end earlier than the start
- **THEN** an inline error says the end must be after the start and nothing is stored

#### Scenario: Attack in progress
- **WHEN** the details of the attack in progress are shown
- **THEN** its start can be changed and its end shows "In progress" without a picker

#### Scenario: Start in the next few minutes
- **WHEN** at 13:54 the user sets an ended attack's start to 13:56 and its end to 13:59
- **THEN** an inline error under the start says the time cannot be in the future, and nothing is stored (QA #89)
