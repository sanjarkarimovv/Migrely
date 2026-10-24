## MODIFIED Requirements

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

### Requirement: Period today from home
In the normal mode, home SHALL show a "Period" toggle under today's card with today's period mark, while "Track periods"
is on or today has a period mark. One tap SHALL mark or
unmark today. Attack mode SHALL NOT show it (BRD DAY-F1, D48, D79, G4).

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

## ADDED Requirements

### Requirement: Track periods
Settings SHALL show a "Track periods" switch, on by default, which onboarding's third step also sets. Turning it off
SHALL keep every period mark already saved and shown (BRD DAY-F1, D79).

#### Scenario: Turning it off
- **WHEN** the user turns "Track periods" off in Settings
- **THEN** home and History stop offering new period marks, and October's saved marks still show
