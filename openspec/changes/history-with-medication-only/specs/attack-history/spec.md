## MODIFIED Requirements

### Requirement: Month by month
History SHALL show one month at a time with "Previous month" and "Next month" buttons. It SHALL NOT go before the
earliest of the month in which the first attack started, in that attack's zone, the month of the first day mark, and the
month of the first attack-medication intake, in its zone, nor past the current month. Preventive intakes SHALL not count.
With nothing logged or marked, only the current month SHALL be shown (BRD EC-HIS-03, D51, D99).

#### Scenario: Previous month
- **WHEN** History shows October 2026 and the first attack was in August 2026, and the user taps "Previous month"
- **THEN** September 2026 is shown

#### Scenario: Not before the first attack
- **WHEN** History shows August 2026, the month of the first attack
- **THEN** "Previous month" is disabled

#### Scenario: Not past this month
- **WHEN** History shows the current month
- **THEN** "Next month" is disabled

#### Scenario: A mark before any attack
- **WHEN** no migraine was ever logged, 30 September is marked as a period day, and today is 2 October
- **THEN** History can go back to September 2026, where 30 September shows its period mark

#### Scenario: Medication before any attack
- **WHEN** no migraine was ever logged, ibuprofen (an attack medication) was logged on 20 September, and today is
  2 October
- **THEN** History can go back to September 2026

### Requirement: No attacks yet
When no attack, no day mark and no attack-medication intake has been logged, History SHALL show the current month, a short explanation of the skies and "Log your
first migraine", which opens the add screen of `past-attacks` (BRD EC-HIS-01, D99). Otherwise it SHALL show the month and its summary as usual, even with no migraine logged.

#### Scenario: First use
- **WHEN** no attack has ever been logged and the user opens History
- **THEN** it shows the current month with every past day dashed, the explanation and "Log your first migraine"

#### Scenario: Only medication logged
- **WHEN** no migraine was ever logged and ibuprofen was taken on 16 of the last 30 days
- **THEN** History shows the current month with its summary, "0" migraine days, its days with attack medication, and
  the "Last 30 days" indicator, without "Log your first migraine"
