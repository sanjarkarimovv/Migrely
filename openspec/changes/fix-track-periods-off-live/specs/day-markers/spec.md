## MODIFIED Requirements

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
