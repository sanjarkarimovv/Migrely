## MODIFIED Requirements

### Requirement: Elapsed time follows a moved start
When the start of the attack in progress changes, or the app comes back to the foreground, the start time and the
elapsed time SHALL be computed again at once, not at the next periodic update. A start in another zone than the
device's SHALL show that zone's city (BRD ATK-F1, EC-TIM-02, D36).

#### Scenario: Started an hour earlier
- **WHEN** an attack started just now and the user taps "1 h" under "Started earlier?"
- **THEN** attack mode shows "1 h 0 min" at once

#### Scenario: Back from another time zone
- **WHEN** an attack was started at 14:33 in Dubai, the phone moves to Tashkent while the app is in the background,
  and the user opens the app again
- **THEN** attack mode shows "Started 2:33 PM (Dubai)" at once
