## ADDED Requirements

### Requirement: The Migrely credit line
While the credit switch is on, the last page SHALL carry one line under "Patient-entered data. Not a diagnosis.": the
approved credit text, which says Migrely is a personal diary app, not a medical device, and names `migrely.health`. It
SHALL use the footer's small grey type across the content width, have no logo, colour, QR or link, never be cut or
overlap the page number, and not change the page count (BRD REP-F5, D186).

#### Scenario: A three-month report
- **WHEN** a Premium user with the switch on, as by default, makes a report of several pages
- **THEN** only the last page shows the credit line, below the footer rule and under the disclaimer, and no other page
  or content block shows it (EC-REP-05)

#### Scenario: One page
- **WHEN** the report has one page
- **THEN** that page shows the credit line (EC-REP-06)

#### Scenario: A4 and Letter
- **WHEN** the report is made on A4 (595 × 842) or on Letter (612 × 792)
- **THEN** the credit line is one line, not ellipsized, inside the bottom margin, clear of "Page N of M", and the page
  count equals that of the same report with the switch off (EC-REP-06)

#### Scenario: The 50-page cap
- **WHEN** a report is cut at the page limit (EC-REP-02)
- **THEN** the credit line is on the last page drawn

#### Scenario: Turned off
- **WHEN** the switch is off and a report is made
- **THEN** the PDF has no credit line, and its pages are laid out as before this change (EC-REP-07)

#### Scenario: Changed after sharing
- **WHEN** a report was shared and the switch is changed afterwards
- **THEN** the shared file is unchanged, and the next report follows the new choice (EC-REP-08)

#### Scenario: Another device language
- **WHEN** the phone's language is not English, or is right to left
- **THEN** the PDF and the credit line stay English and left to right, as the report is today (EC-REP-10)

### Requirement: The credit switch
The Doctor report screen SHALL offer one switch for the credit line, on by default. The choice SHALL survive restarts and
process death, move with Android's backup, return to on after "Delete all data", and stay out of the JSON and CSV
exports, whose content and `schemaVersion` SHALL not change (BRD REP-F5, D71, D186).

#### Scenario: Kept
- **WHEN** the user turns the switch off, then the app restarts or its process is killed and restored
- **THEN** the switch is still off

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
- **THEN** it is one node with its label, its on or off state and the switch role, the label wraps without "…", and Tab
  reaches it in visual order (EC-REP-11)
