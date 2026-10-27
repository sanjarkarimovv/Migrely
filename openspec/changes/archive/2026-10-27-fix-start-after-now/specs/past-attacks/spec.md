## MODIFIED Requirements

### Requirement: Times in the past only
A start after now, an end more than 5 minutes after now, and an end that is not after the start SHALL NOT be stored,
in a new migraine or a merge. Each SHALL be shown as an inline error under the time concerned (BRD EC-ATK-03,
EC-ATK-04, EC-TIM-04).

#### Scenario: End in the future
- **WHEN** the phone's clock moves back while the screen is open, so the picked end is now 10 minutes in the future, and
  the user taps "Add migraine"
- **THEN** an inline error under "Ended" says the time is in the future and nothing is stored

#### Scenario: Clock tolerance
- **WHEN** the picked end is 3 minutes after the phone's current time
- **THEN** the attack is stored

#### Scenario: End not after the start
- **WHEN** the end is not after the start when the user taps "Add migraine"
- **THEN** an inline error under "Ended" says the end must be after the start and nothing is stored

#### Scenario: Start after now
- **WHEN** the phone's clock moves back while the screen is open, so the picked start is now 2 minutes in the future,
  and the user taps "Add migraine"
- **THEN** an inline error under "Started" says the time is in the future and nothing is stored
