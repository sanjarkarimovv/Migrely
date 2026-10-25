## MODIFIED Requirements

### Requirement: A month summary
Under the calendar, History SHALL show a summary of the month shown, titled with the month's name, and "<month> so
far" for the current month. It SHALL show the migraines started, migraine days, pain-free days, average peak and days
with attack medication, counted to today in the current month. With nothing logged, that is no attack, no day mark and
no attack-medication intake, History's first-use card SHALL show instead. Preventive intakes SHALL not count
(BRD STA-F1, EC-HIS-01, D53, D99).

#### Scenario: A past month
- **WHEN** History shows September 2026, with 3 attacks started in September, one of them from 29 to 30 September
- **THEN** the summary is titled "September" and shows 3 migraines started and 4 migraine days

#### Scenario: The current month
- **WHEN** History shows October 2026 on 24 October
- **THEN** the summary is titled "October so far" and counts days up to 24 October, an attack in progress included

#### Scenario: Nothing logged yet
- **WHEN** no attack, no day mark and no attack-medication intake was ever logged
- **THEN** History shows its first-use card and no summary

#### Scenario: Only medication logged
- **WHEN** no migraine was ever logged and ibuprofen was taken on 4 and 5 October
- **THEN** History's October summary shows 0 migraine days and 2 days with attack medication

### Requirement: The progress message
The summary SHALL compare a past month's migraine days with the whole previous month's, and the current month's, up to
today's date, with the same dates of the previous month, up to its last day. It SHALL show only when
both periods have at least 10 days with an entry: a day an attack touches, a marked day, or a day with attack
medication. Fewer days SHALL read warm, and more or as many SHALL read neutral. It SHALL not show before any attack is
logged, since a comparison of migraine days means nothing without one (BRD EC-UX-14, G3, D55, D99).

#### Scenario: Better than last month
- **WHEN** September 2026 had 6 migraine days and August 9, each with 10 or more days with entries
- **THEN** September's summary reads "3 fewer migraine days than August", in the warm colour

#### Scenario: The current month
- **WHEN** on 24 October there were 4 migraine days so far, against 5 by 24 September
- **THEN** the message reads "1 fewer migraine day than this time last month"

#### Scenario: Worse
- **WHEN** September had 2 more migraine days than August
- **THEN** it reads "2 more migraine days than August", in the neutral colour

#### Scenario: Too few entries
- **WHEN** August has entries on only 8 days
- **THEN** September's summary shows no progress message

#### Scenario: No migraine logged
- **WHEN** no migraine was ever logged, and September and October each have 12 days with attack medication
- **THEN** October's summary shows no progress message
