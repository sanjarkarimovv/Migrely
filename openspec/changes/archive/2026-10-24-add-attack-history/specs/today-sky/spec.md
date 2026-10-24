## ADDED Requirements

### Requirement: This month under today's sky
In the normal mode, the home screen SHALL show this month's calendar of `month-sky` under today's card and list, in
portrait and in landscape. Its month name and its days SHALL open History (`attack-history`). The calendar SHALL update
whenever home's data changes. Attack mode SHALL NOT show it (BRD 18.3 A, HIS-F1, D29, G4).

#### Scenario: Calendar on home
- **WHEN** no attack is in progress and today is 23 October
- **THEN** home shows "October" and October's calendar under today's card

#### Scenario: Opens History
- **WHEN** the user taps 14 October in home's calendar
- **THEN** History opens with 14 October chosen

#### Scenario: Undo on home updates the calendar
- **WHEN** the user deletes the 8 October storm from History, goes back home within 30 seconds and taps "Undo" there
- **THEN** 8 October shows its storm again without leaving home

#### Scenario: Not in attack mode
- **WHEN** an attack is in progress
- **THEN** home does not show the month calendar

#### Scenario: Landscape
- **WHEN** the phone is in landscape with no attack in progress
- **THEN** the month calendar is shown under today's card and list, and the column scrolls to it
