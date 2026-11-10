# doctor-report Specification

## Purpose
Defines the report a patient shares with their doctor: the period it covers, what it holds and how it counts, how a
large period is handled, and how the file is made and shared without any permission (BRD REP-F1–F4, EC-REP-01–03,
D81–D85).

## Requirements

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

### Requirement: Nothing to report
When nothing was logged in the chosen period, "Create report" SHALL be off and say "Nothing was logged in this period."
(BRD EC-REP-01, D81).

#### Scenario: An empty month
- **WHEN** the user chooses 1 month and nothing was logged in it
- **THEN** "Create report" is off and the reason shows next to it

### Requirement: The summary page
The first page SHALL show the period, the date made, and, for the period:
- migraine days a month on average;
- the number of attacks;
- attack-medication days a month on average, and the 30-day spans back from its last day that reach an ICHD-3 8.2
  medication-day threshold, as home's card counts them;
- the number of "Bed rest" attacks;
- the average peak level;
- whether a calendar month it touches had 15 or more migraine days, as History counts them.

(BRD REP-F2, D82, D151.)

#### Scenario: Three months
- **WHEN** the report covers 92 days with 18 migraine days and 4 attacks at "Bed rest"
- **THEN** the summary says 5.9 migraine days a month and 4 "Bed rest" attacks

#### Scenario: A month only partly in the period
- **WHEN** a one-month report starts on 26 September and triptans were taken on 11 of its days across the month change
- **THEN** the span 26 September to 25 October reaches the threshold, as home's card says

#### Scenario: The label does not say limit
- **WHEN** the summary page shows the overuse spans
- **THEN** the label reads "30-day spans reaching an ICHD-3 8.2 medication-day threshold", and the value is the dates or
  "None"

### Requirement: The details
After the summary the report SHALL hold:
- a calendar heat map per month that reads in black and white;
- the attacks table;
- the medications table, with intakes, days used and how they helped;
- the notes, only when chosen.

Every page SHALL end with "Patient-entered data. Not a diagnosis." and its page number (BRD REP-F2, REP-F3, D82).

#### Scenario: Notes left out
- **WHEN** "Include notes" is off
- **THEN** no attack's note appears anywhere in the report

### Requirement: Counting like the app
Days SHALL be counted as the app counts them: an attack's days in the zone it was logged in, a medication's days in
the zone it was taken in, and attacks being deleted left out (BRD D45, D57, D83).

#### Scenario: Logged in Tokyo
- **WHEN** an attack was logged in Tokyo on the evening of 14 October, Tashkent time
- **THEN** the report counts it on 14 or 15 October as the calendar shows it

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

### Requirement: The file and sharing
The report SHALL be written to the app's cache under a temporary name and renamed when complete. Leftover partial files
and reports older than a day SHALL be removed when a report starts and when the app starts. "Share" SHALL open the
system share sheet with the PDF, and no permission SHALL be asked for (BRD REP-F4, EC-REP-03, D85).

#### Scenario: Closed while making it
- **WHEN** the app is closed while a report is being made
- **THEN** no partial file is left after the next start, and a new report can be made

### Requirement: The doctor report is premium
The doctor report SHALL need Premium. A free user's tap on "Doctor report" SHALL open the paywall with its headline, and
the report screen SHALL open once Premium is on (BRD §7.1, D108).

#### Scenario: Free user
- **WHEN** a free user taps "Doctor report"
- **THEN** the paywall opens; after buying, the report screen opens

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
