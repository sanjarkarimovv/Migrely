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
