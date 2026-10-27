# month-stats Specification

## Purpose
Defines the numbers History shows for a month, the chronic-migraine indicator, and the progress message on History and
home, so that a user and their doctor can see how a month went without any number being guessed or blamed (BRD STA-F1,
STA-F2, EC-UX-14, D53–D56).

## Requirements

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

### Requirement: Migraines started
The summary's first number SHALL be labelled "Migraines started", because it counts the attacks that started in the
month, while the list under it holds every attack that touches the month (BRD EC-ATK-06, D58).

#### Scenario: An attack from the month before
- **WHEN** History shows October, and the month's list includes an attack from 30 September to 1 October
- **THEN** that attack is in the list but not in "Migraines started", and 1 October counts as a migraine day

### Requirement: How the numbers are counted
An attack SHALL count once, in the month in which it started, in the zone where it started. A migraine day SHALL be a
date an attack touches, by the calendar's rule. A pain-free day SHALL be a date marked pain-free that no attack
touches. A day with attack medication SHALL be a date with an intake of a medication for attacks, in the zone where it
was taken (BRD EC-ATK-06, EC-TIM-02, EC-DAY-01, D36, D45, D53).

#### Scenario: An attack over two months
- **WHEN** an attack starts on 31 August at 22:00 and ends on 1 September at 03:00
- **THEN** August counts it as a migraine, and both August and September count a migraine day

#### Scenario: A pain-free mark under an attack
- **WHEN** 10 September is marked pain-free and an attack is logged on it later
- **THEN** 10 September counts as a migraine day and not as a pain-free day

#### Scenario: Preventive medication
- **WHEN** only a preventive medication was taken on a day
- **THEN** that day is not a day with attack medication

### Requirement: Average peak
The average peak SHALL be the mean of the month's attacks with a known peak, with Mild as 1, Moderate as 2 and Severe
as 3, rounded to the nearest level with halves up, and shown as that level's word. Attacks without a pain level SHALL
not count. With none known, it SHALL show "—" and say "No pain level logged" (BRD STA-F1, EC-ATK-08, D56).

#### Scenario: Rounded to a level
- **WHEN** a month has a Mild and a Moderate attack
- **THEN** the average peak reads "Moderate"

#### Scenario: No pain level
- **WHEN** a month's attacks have no pain level
- **THEN** the average peak reads "—", and screen readers hear "Average peak: no pain level logged"

### Requirement: The chronic indicator
With 15 or more migraine days in the month shown, the summary SHALL show a calm information card that suggests talking
to a doctor, without warning colours or blame (BRD STA-F2, G1, D54).

#### Scenario: Fifteen migraine days
- **WHEN** October 2026 has 15 migraine days by 24 October
- **THEN** the summary shows "15 or more migraine days in October. That's a lot. Talk to your doctor about it."

#### Scenario: Fourteen migraine days
- **WHEN** a month has 14 migraine days
- **THEN** no such card shows

### Requirement: The progress message
The summary SHALL compare a past month's migraine days with the whole previous month's, and the current month's, up to
today's date, with the same dates of the previous month, up to its last day. It SHALL show only when both periods have
at least 10 days with an entry (an attack, a mark or attack medication) and an attack was ever logged. Fewer days
SHALL read warm, and more or as many neutral (BRD EC-UX-14, G3, D55, D99).

#### Scenario: Not before any attack
- **WHEN** no migraine was ever logged, and simple painkillers were taken on 12 days of this month and 12 of the last
- **THEN** the summary shows no progress message, since a comparison of migraine days means nothing without one

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

### Requirement: Progress on home
In normal mode, home's month card SHALL show the current month's progress message under its title, above the
calendar, so it shows without scrolling, by the same rule, and SHALL show nothing in its place when there is none.
Attack mode SHALL show no progress message (BRD 18.3 A, EC-UX-13, D55, D58).

#### Scenario: Home in normal mode
- **WHEN** the current month has a progress message
- **THEN** home shows it under the month card's title, above the calendar

#### Scenario: Attack mode
- **WHEN** an attack is in progress
- **THEN** home shows no progress message
