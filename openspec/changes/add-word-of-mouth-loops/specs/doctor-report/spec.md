## ADDED Requirements

### Requirement: The Migrely credit line
While the credit switch is on, the last page SHALL carry "Made with Migrely, a personal migraine diary app. Not a medical
device. migrely.health" as a second footer line under "Patient-entered data. Not a diagnosis.", placed by line 1's
measured height, in the footer's small grey type across the content width. It SHALL have no logo, colour, QR or link,
never be cut or overlap the page number, and not change the page count (BRD REP-F5, D186).

#### Scenario: A three-month report
- **WHEN** a Premium user with the switch on, as by default, makes a report of several pages
- **THEN** only the last page shows the credit line, below the footer rule and under the disclaimer, and no other page
  or content block shows it (EC-REP-05)

#### Scenario: One page
- **WHEN** the report has one page
- **THEN** that page shows the credit line (EC-REP-06)

#### Scenario: A4 and Letter
- **WHEN** the report is made on A4 (595 × 842) or on Letter (612 × 792)
- **THEN** the credit line lays out as one line with no ellipsis (`lineCount == 1`, `getEllipsisCount(0) == 0`), inside
  the bottom margin, clear of "Page N of M", and the page count equals that of the same report with the switch off
  (EC-REP-06)

#### Scenario: The 50-page cap
- **WHEN** a report is cut at the page limit (EC-REP-02)
- **THEN** the credit line is on the last page drawn

#### Scenario: Turned off
- **WHEN** the switch is off and a report is made
- **THEN** the PDF has no credit line, its pages are laid out as before this change, and "Patient-entered data. Not a
  diagnosis." is still on every page (EC-REP-07; QA negative control, medical-reviewer condition)

#### Scenario: Changed after sharing
- **WHEN** a report was shared and the switch is changed afterwards
- **THEN** the shared file is unchanged, and the next report follows the new choice (EC-REP-08)

#### Scenario: Another device language
- **WHEN** the phone's language is not English, or is right to left
- **THEN** the PDF and the credit line stay English and left to right, as the report is today (EC-REP-10)

### Requirement: The credit switch
The Doctor report SHALL offer "Mention Migrely in the report", after "Include notes", on by default; its summary SHALL
quote the credit line exactly. The choice SHALL survive restarts and process death, move with Android's backup, return to
on after "Delete all data", and stay out of JSON and CSV exports, whose content and `schemaVersion` SHALL not change
(BRD REP-F5, D71, D186).

#### Scenario: The summary quotes the line
- **WHEN** the switch's summary is shown
- **THEN** it reads 'One small line on the last page: "Made with Migrely, a personal migraine diary app. Not a medical
  device. migrely.health"', and a test asserts it contains the mapper's credit constant exactly (medical-reviewer
  condition W6)

#### Scenario: Kept
- **WHEN** the user turns the switch off, then the app restarts or its process is killed and restored
- **THEN** the switch is still off

#### Scenario: Not saved
- **WHEN** the choice cannot be saved, as with storage full or a write error
- **THEN** the switch returns to the stored value and the report shows "Couldn't save that. Try again." as an error
  message, so a switch never looks changed when it is not (EC-REP-12)

#### Scenario: Loading and making a report
- **WHEN** the screen is loading, or a report is being made
- **THEN** the switch is hidden until its value is read, and is shown but disabled while the report is made, which uses
  the value at the tap on "Create report" (EC-REP-13)

#### Scenario: Delete all data
- **WHEN** the user deletes all data
- **THEN** the switch is on again (EC-REP-07)

#### Scenario: Exports unchanged
- **WHEN** the user exports JSON or CSV with the switch on or off
- **THEN** the files are the same as before this change, and `schemaVersion` is unchanged (EC-REP-07)

#### Scenario: Free or lapsed Premium
- **WHEN** a Free user, or one whose Premium ended, opens the doctor report
- **THEN** the paywall shows, the switch is not shown, and the stored choice is kept for when Premium returns (EC-REP-09)

#### Scenario: TalkBack, 200% font, landscape and keyboard
- **WHEN** the switch is reached with TalkBack, at 200% font, in landscape or with a keyboard
- **THEN** it is one node with its title, its summary, its on or off state and the switch role, the text wraps without
  "…", and Tab reaches it between "Include notes" and "Create report"; Space toggles it (EC-REP-11)
