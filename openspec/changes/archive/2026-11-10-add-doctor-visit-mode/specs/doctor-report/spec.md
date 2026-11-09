## MODIFIED Requirements

### Requirement: Choosing the period
The "Doctor report" screen SHALL offer 1, 3, 6 and 12 months and a custom range of at most 24 months ending no later
than today, and an "Include notes" switch that is off at first. When the last doctor visit is within 24 months, it
SHALL also offer "Since <short date>", from that visit's day to today, listed first and chosen at first, with the
covers line "Since your last visit: <from> to <to>"; otherwise 3 months SHALL be chosen at first (BRD REP-F1,
EC-REP-04, D81, D168).

#### Scenario: Opening the report
- **WHEN** the user with no past doctor visit taps "Doctor report" in History
- **THEN** 3 months is chosen, notes are off, and the screen shows the dates the report will cover

#### Scenario: Since the last visit
- **WHEN** the last visit was August 20 and the user opens the report on November 18
- **THEN** the chip "Since Aug 20" is listed first and chosen, TalkBack reads it as "Since your last visit, Thursday,
  August 20, 2026", and the line reads "Since your last visit: Aug 20, 2026 to Nov 18, 2026"

#### Scenario: A last visit too long ago
- **WHEN** the last visit was more than 24 months ago
- **THEN** no "Since" chip is offered and 3 months is chosen

#### Scenario: Made on the visit day
- **WHEN** visits are saved for August 20 and November 20 and the report is opened on November 20
- **THEN** the chip reads "Since Aug 20" and covers August 20 to November 20

#### Scenario: A short period
- **WHEN** the last visit was 10 days ago and its "Since" chip is chosen
- **THEN** the summary gives totals for the 11 days and no averages a month, as for any period under 28 days

#### Scenario: Opened from the reminder
- **WHEN** a Premium user opens the report from the visit reminder
- **THEN** the period chosen is the same as when opened from History

#### Scenario: A visit last year
- **WHEN** on January 10, 2027 the last visit was December 15, 2026
- **THEN** the chip reads "Since Dec 15, 2026", and TalkBack reads the full date

#### Scenario: A cancelled visit left in place
- **WHEN** a visit date passed without the visit, and the user did not remove it
- **THEN** the chip still starts from that date, and the user can choose another period
