## ADDED Requirements

### Requirement: Custom range at any size
The custom range picker SHALL fill the screen, with Cancel, its title and Done at the top. The weeks of the month SHALL
stay visible and pickable in landscape and in portrait at a 200% font size. Done SHALL be on only once the range has an
end; picking the same day twice SHALL give a range of one day (BRD EC-UX-03, EC-UX-04, D127).

#### Scenario: Largest font size in landscape
- **WHEN** the phone is in landscape, the system font size is 200%, and the user taps "Custom" on the "Doctor report"
  screen
- **THEN** at least two weeks of the month can be seen, and a range can be picked and taken with "Done"

#### Scenario: Only a start chosen
- **WHEN** the user picks October 5 as the start and no end
- **THEN** "Done" is off

#### Scenario: One day
- **WHEN** the user picks October 5 twice and taps "Done"
- **THEN** the report covers October 5 alone
