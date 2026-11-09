## MODIFIED Requirements

### Requirement: Choosing the period
The "Doctor report" screen SHALL offer 1, 3, 6 and 12 months and a custom range of at most 24 months ending no later
than today, and an "Include notes" switch that is off at first. When the last doctor visit is within 24 months, it
SHALL also offer "Since last visit", from that visit's day to today, listed first and chosen at first; otherwise 3
months SHALL be chosen at first (BRD REP-F1, EC-REP-04, D81, D168).

#### Scenario: Opening the report
- **WHEN** the user with no past doctor visit taps "Doctor report" in History
- **THEN** 3 months is chosen, notes are off, and the screen shows the dates the report will cover

#### Scenario: Since the last visit
- **WHEN** the last visit was August 20 and the user opens the report on November 18
- **THEN** "Since last visit" is listed first and chosen, covering August 20 to November 18, both included

#### Scenario: A last visit too long ago
- **WHEN** the last visit was more than 24 months ago
- **THEN** "Since last visit" is not offered and 3 months is chosen

#### Scenario: Made on the visit day
- **WHEN** visits are saved for August 20 and November 20 and the report is opened on November 20
- **THEN** "Since last visit" covers August 20 to November 20

#### Scenario: A short period
- **WHEN** the last visit was 10 days ago and "Since last visit" is chosen
- **THEN** the summary gives totals for the 11 days and no averages a month, as for any period under 28 days

#### Scenario: Opened from the reminder
- **WHEN** a Premium user opens the report from the visit reminder
- **THEN** the period chosen is the same as when opened from History
