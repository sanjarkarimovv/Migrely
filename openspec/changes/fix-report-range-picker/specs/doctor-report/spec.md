## ADDED Requirements

### Requirement: Custom range at any size
The custom range picker SHALL fill the screen, with Cancel, its title and Done at the top. The weeks of the month SHALL
stay visible and pickable in landscape and at a 200% font size. Done SHALL take the chosen range, or the start day
alone when only a start is chosen (BRD EC-UX-03, EC-UX-04, D127).

#### Scenario: Largest font size in landscape
- **WHEN** the phone is in landscape, the system font size is 200%, and the user taps "Custom" on the "Doctor report"
  screen
- **THEN** at least two weeks of the month can be seen, and a range can be picked and taken with "Done"

#### Scenario: Only a start chosen
- **WHEN** the user picks October 5 as the start, picks no end, and taps "Done"
- **THEN** the report covers October 5 alone
