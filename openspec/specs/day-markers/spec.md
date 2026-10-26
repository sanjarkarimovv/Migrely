# day-markers Specification

## Purpose
Defines how a calendar day is marked as a period day or a pain-free day, the rules that keep the marks true to the
attacks logged, and where the marks are made, so that the calendar can show a clear sky only when the user said so and
period days can later be related to migraines (BRD DAY-F1, DAY-F2, HIS-F3, D47, D48).

## Requirements

### Requirement: Mark a chosen day
Under a chosen day, History SHALL show two switches, "Period" and "Pain-free day", with the day's marks; "Period" shows
while "Track periods" is on, or while the day has a period mark. Turning one on
SHALL mark the day at once, and turning it off SHALL remove the mark. Only today and past days can be chosen and marked
(BRD DAY-F1, DAY-F2, HIS-F3, EC-DAY-03, EC-DAY-05, D47, D48, D79).

#### Scenario: Mark a period day
- **WHEN** the user chooses 14 October and turns on "Period"
- **THEN** 14 October is marked as a period day and its cell shows the period mark

#### Scenario: Remove a mark
- **WHEN** 14 October is marked as a period day and the user turns "Period" off
- **THEN** the mark is removed

#### Scenario: Mark a pain-free day
- **WHEN** nothing was logged on 6 October and the user chooses it and turns on "Pain-free day"
- **THEN** 6 October shows a clear sky

#### Scenario: Future day
- **WHEN** today is 23 October
- **THEN** 25 October cannot be chosen, so it cannot be marked

#### Scenario: Period tracking off
- **WHEN** "Track periods" is off and the user chooses 14 October, which has no period mark
- **THEN** only "Pain-free day" shows

#### Scenario: Removing a mark with tracking off
- **WHEN** "Track periods" is off and 12 October has a period mark
- **THEN** "Period" shows as on for 12 October, so the mark can be removed

### Requirement: Pain-free only without attacks
A day that an attack touches SHALL NOT be marked pain-free: its "Pain-free day" switch SHALL be off and disabled, with
"A migraine is logged on this day." When an attack is logged later on a day marked pain-free, the attack SHALL win: the
day SHALL NOT show or count as pain-free while an attack touches it (BRD EC-DAY-01, D47).

#### Scenario: Attack day
- **WHEN** the user chooses 14 October, which has an attack
- **THEN** "Pain-free day" is off, cannot be turned on, and says "A migraine is logged on this day."

#### Scenario: Attack added later
- **WHEN** 6 October is marked pain-free and the user adds a past migraine on 6 October
- **THEN** 6 October shows the migraine's sky, not a clear sky

#### Scenario: Attack deleted again
- **WHEN** that migraine on 6 October is deleted
- **THEN** 6 October shows a clear sky again

### Requirement: Period on any day
A period mark SHALL be allowed on any day that can be marked, with or without attacks and together with a pain-free
mark. Marks SHALL belong to the calendar date, without a time zone (BRD EC-DAY-02, EC-DAY-04, D47).

#### Scenario: Period and migraine
- **WHEN** 14 October has a storm and the user turns on "Period"
- **THEN** 14 October shows the storm and the period mark

#### Scenario: After travel
- **WHEN** 14 October was marked as a period day in Tashkent and the user later opens History in Berlin
- **THEN** 14 October still shows the period mark

### Requirement: Period today from home
In the normal mode, home SHALL show a "Period" toggle under today's card with today's period mark, while "Track periods"
is on or today has a period mark, following the setting as soon as home shows again. One tap SHALL mark or unmark
today; after that tap the toggle SHALL stay until home is next opened or returned to, so a mark removed by mistake can
be put back. Attack mode SHALL NOT show it (BRD DAY-F1, D48, D79, G4).

#### Scenario: Mark today from home
- **WHEN** no attack is in progress and the user taps "Period" on home
- **THEN** today is marked as a period day, the toggle shows as on, and today's cell in the month calendar shows the
  mark

#### Scenario: Attack mode
- **WHEN** an attack is in progress
- **THEN** home shows no "Period" toggle

#### Scenario: Period tracking off
- **WHEN** "Track periods" is off and today has no period mark
- **THEN** home shows no "Period" toggle

#### Scenario: Turning tracking off from home
- **WHEN** home shows the "Period" toggle, today has no period mark, and the user turns "Track periods" off in Settings
  and goes back
- **THEN** home shows no "Period" toggle at once, without a restart

#### Scenario: Removing today's mark with tracking off
- **WHEN** "Track periods" is off, today has a period mark, and the user taps "Period" on home
- **THEN** the mark is removed and the toggle stays, off, so it can be turned on again; after the user leaves home and
  comes back, the toggle is gone

### Requirement: Accessible marks
The switches and the home toggle SHALL be at least 48 dp, have their own labels and states for screen readers, and
stay readable at 200% font (BRD EC-UX-03, EC-UX-04).

#### Scenario: Screen reader
- **WHEN** TalkBack focuses the home toggle while today is marked
- **THEN** it reads "Period today", checked

#### Scenario: Disabled switch
- **WHEN** TalkBack focuses "Pain-free day" on an attack day
- **THEN** it reads it as disabled, followed by "A migraine is logged on this day."

### Requirement: Track periods
Settings SHALL show a "Track periods" switch, on by default, which onboarding's third step also sets. Turning it off
SHALL keep every period mark already saved and shown (BRD DAY-F1, D79).

#### Scenario: Turning it off
- **WHEN** the user turns "Track periods" off in Settings
- **THEN** home and History stop offering new period marks, and October's saved marks still show
