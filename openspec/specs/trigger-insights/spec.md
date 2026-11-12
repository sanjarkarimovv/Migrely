# trigger-insights Specification

## Purpose
Defines the Insights screen: the period it looks at, when it has enough to say something, and what each section says,
always as things that come together and never as causes (BRD INS-F1–F6, EC-INS-01–03, D87–D92).

## Requirements

### Requirement: The period
Insights SHALL look at the last 3, 6 or 12 months, with 6 chosen at first. The trends SHALL always cover the last 12
calendar months (BRD D87, D92).

#### Scenario: Opening Insights
- **WHEN** the user taps "Insights" in History
- **THEN** 6 months is chosen and the sections for those months show

### Requirement: Enough to go on
The sections other than trends SHALL show only when at least 5 migraines started in the period and the first migraine
started at least 30 days ago. The days count from when that migraine started, not from when it was entered, so past
migraines added later and a restored backup count in full. Until then a card SHALL say how many more migraines, or
how many more days, are needed (BRD EC-INS-01, D88).

#### Scenario: Three migraines so far
- **WHEN** 3 migraines started in the period
- **THEN** the card says "2 more migraines to see your patterns"

### Requirement: Triggers, never causes
Insights SHALL list the five triggers logged with the most migraines in the period, each with its count and share,
under "Often logged with your migraines". No text SHALL say a trigger causes or leads to a migraine (BRD INS-F1, INS-F6,
D89).

#### Scenario: Stress
- **WHEN** stress was logged with 6 of the period's 10 migraines
- **THEN** the list shows "Stress — 6 of 10 migraines (60%)"

### Requirement: When migraines come
Insights SHALL count the period's migraines by weekday and by time of day: night 00–06, morning 06–12, afternoon 12–18
and evening 18–24. Each is counted in the zone it was logged in. The busiest SHALL be named, or "No single day stands
out" when it ties (BRD INS-F2, D90).

#### Scenario: Mondays
- **WHEN** 3 of 10 migraines started on Mondays and no other day had 3
- **THEN** the text says "Most often on Mondays: 3 of 10"

### Requirement: Around the period
With Track periods on and at least two cycles starting in the period, Insights SHALL say what share of migraines started
from 2 days before a period through its third day, and what share of the period's days those are. With Track periods
off the section SHALL NOT exist (BRD INS-F3, EC-INS-02, EC-INS-03, D91).

#### Scenario: Tracking off
- **WHEN** Track periods is off
- **THEN** Insights shows nothing about periods

### Requirement: How you rated your medications
Insights SHALL head its medication section "How you rated your medications". For each attack medication with at least 3
answers to "Did it help?" in the period, it SHALL show how many of those answers were "A lot" or "Completely"; with fewer
answers it SHALL say how many it has. It SHALL never say which medication works (BRD INS-F4, INS-F6, D92, D174).

#### Scenario: Ibuprofen
- **WHEN** 7 of 9 answers for Ibuprofen were "A lot" or "Completely"
- **THEN** Ibuprofen shows "Helped a lot or completely: 7 of 9 answers (78%)"

#### Scenario: The heading
- **WHEN** Insights shows the medication section
- **THEN** its heading reads "How you rated your medications"
- **AND** no text on the Insights screen says "what helped", in any letter case

#### Scenario: Fewer than three answers
- **WHEN** Sumatriptan has 2 answers to "Did it help?" in the period
- **THEN** under the same heading Sumatriptan shows "Needs 3 answers (has 2)"

#### Scenario: No attack medication
- **WHEN** no attack medication was logged in the period
- **THEN** the section keeps its heading and says "No attack medication was logged in this period."

#### Scenario: Large font, narrow screen, landscape
- **WHEN** the font is at 200% on a 360 dp wide screen, in portrait or landscape
- **THEN** the heading wraps onto more lines and is never clipped or cut with "…" (BRD EC-INS-04)

#### Scenario: TalkBack
- **WHEN** TalkBack moves through Insights
- **THEN** "How you rated your medications" is read as a heading
- **AND** each medication's name is read together with its answers line, as before (BRD EC-INS-05)

#### Scenario: Upgrade or restore
- **WHEN** a person updates from a version that showed "What helped", or restores a backup made by one
- **THEN** every answer, count and percentage is the same as before, and only the heading's words differ (BRD EC-INS-06)

### Requirement: Twelve months
Insights SHALL show, for each of the last 12 calendar months, migraine days as bars, and migraines, medication days and
bed-rest migraines as text a screen reader reads (BRD INS-F5, D92).

#### Scenario: A screen reader
- **WHEN** TalkBack reads the trends
- **THEN** each month is read with its four numbers

### Requirement: Insights are premium
Insights SHALL need Premium. A free user's tap on "Insights" SHALL open the paywall with its headline (BRD §7.1, D108).

#### Scenario: Free user
- **WHEN** a free user taps "Insights"
- **THEN** the paywall opens with an Insights headline
