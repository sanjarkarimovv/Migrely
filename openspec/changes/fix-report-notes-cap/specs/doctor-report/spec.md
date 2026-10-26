## MODIFIED Requirements

### Requirement: Large periods
The report SHALL be made in the background with progress showing, and SHALL have at most 50 pages. When the migraines
table would not fit, it SHALL show the latest and say "Showing the latest N of M migraines. The summary counts all of
them."; the notes are shortened the same way: only the notes of those N migraines, after "Notes from the latest N
migraines." (BRD EC-REP-02, D84).

#### Scenario: Twelve heavy months
- **WHEN** the user makes a 12-month report with 900 attacks
- **THEN** the report has at most 50 pages and the attacks table says how many it shows

#### Scenario: Twelve heavy months with notes
- **WHEN** the user makes a 12-month report of 900 attacks, each with a note, with "Include notes" on
- **THEN** the report has at most 50 pages, the attacks table shows the latest N of 900, and the notes section says
  "Notes from the latest N migraines." and holds only their notes

#### Scenario: Notes of a report that fits
- **WHEN** the user makes a report with notes on whose migraines all fit
- **THEN** every note is in it, with no "Notes from the latest" line

#### Scenario: No note among the migraines shown
- **WHEN** the migraines table is shortened and none of the migraines it shows has a note
- **THEN** the notes section says "Notes from the latest N migraines." over an empty notes table
