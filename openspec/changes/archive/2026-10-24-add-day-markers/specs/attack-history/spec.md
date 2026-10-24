## MODIFIED Requirements

### Requirement: Choose a day
Tapping today or a past day SHALL choose it: the day is outlined, and the list shows that day's attacks, oldest first,
under the day's title, with its marks (`day-markers`) and "Show all of <month>" to return to the month. Tapping the
chosen day again, or changing the month, SHALL clear the choice (BRD HIS-F3).

#### Scenario: Choose a day
- **WHEN** the user taps 14 October, which had attacks at 09:10 and at 18:00
- **THEN** "Wednesday, 14 October" lists the 09:10 attack first and then the 18:00 attack

#### Scenario: Day with nothing logged
- **WHEN** the user taps 6 October, when nothing was logged
- **THEN** History shows "No migraine logged on this day.", the day's marks and "Add a past migraine"

#### Scenario: Back to the month
- **WHEN** a day is chosen and the user taps "Show all of October"
- **THEN** the list shows all of October's attacks again

### Requirement: Month by month
History SHALL show one month at a time with "Previous month" and "Next month" buttons. It SHALL NOT go before the
earlier of the month in which the first attack started, in that attack's zone, and the month of the first day mark, nor
past the current month. With nothing logged or marked, only the current month SHALL be shown (BRD EC-HIS-03, D51).

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
