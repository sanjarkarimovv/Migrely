## ADDED Requirements

### Requirement: Readable dim text
Every text in attack mode SHALL reach a contrast of at least 4.5:1 against its background, while staying dim and never
pure white (BRD §10, EC-UX-01).

#### Scenario: Start time line
- **WHEN** attack mode shows "Started 14:05 · 42 min"
- **THEN** that line has a contrast of at least 4.5:1 against the card

### Requirement: Elapsed time follows a moved start
When the start of the attack in progress changes, the elapsed time SHALL be computed again at once, not at the next
periodic update (BRD ATK-F1).

#### Scenario: Started an hour earlier
- **WHEN** an attack started just now and the user taps "1 h" under "Started earlier?"
- **THEN** attack mode shows "1 h 0 min" at once
