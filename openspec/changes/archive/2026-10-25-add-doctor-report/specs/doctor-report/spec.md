## Purpose

Defines the report a patient shares with their doctor: the period it covers, what it holds and how it counts, how a
large period is handled, and how the file is made and shared without any permission (BRD REP-F1–F4, EC-REP-01–03,
D81–D85).

## ADDED Requirements

### Requirement: Choosing the period
The "Doctor report" screen SHALL offer 1, 3, 6 and 12 months and a custom range of at most 24 months ending no later
than today, with 3 months chosen at first, and an "Include notes" switch that is off at first (BRD REP-F1, D81).

#### Scenario: Opening the report
- **WHEN** the user taps "Doctor report" in History
- **THEN** 3 months is chosen, notes are off, and the screen shows the dates the report will cover

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
- attack-medication days a month on average, and the 30-day spans back from its last day at the overuse limit, as
  home's card counts them;
- the number of "Bed rest" attacks;
- the average peak level;
- whether a calendar month it touches had 15 or more migraine days, as History counts them.

(BRD REP-F2, D82.)

#### Scenario: Three months
- **WHEN** the report covers 92 days with 18 migraine days and 4 attacks at "Bed rest"
- **THEN** the summary says 5.9 migraine days a month and 4 "Bed rest" attacks

#### Scenario: A month only partly in the period
- **WHEN** a one-month report starts on 26 September and triptans were taken on 11 of its days across the month change
- **THEN** the span 26 September to 25 October is at the overuse limit, as home's card says

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
them."; the notes are shortened the same way (BRD EC-REP-02, D84).

#### Scenario: Twelve heavy months
- **WHEN** the user makes a 12-month report with 900 attacks
- **THEN** the report has at most 50 pages and the attacks table says how many it shows

### Requirement: The file and sharing
The report SHALL be written to the app's cache under a temporary name and renamed when complete. Leftover partial files
and reports older than a day SHALL be removed when a report starts and when the app starts. "Share" SHALL open the
system share sheet with the PDF, and no permission SHALL be asked for (BRD REP-F4, EC-REP-03, D85).

#### Scenario: Closed while making it
- **WHEN** the app is closed while a report is being made
- **THEN** no partial file is left after the next start, and a new report can be made
