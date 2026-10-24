# attack-history Specification

## Purpose
Defines the History screen: going through the months of the `month-sky` calendar, the attacks of each month, choosing a
day to see, edit or add its attacks, the first-use state and undoing a deletion, so that every logged attack stays
within reach and the record can be completed or corrected later (BRD HIS-F1 to HIS-F3, EC-HIS-01 to EC-HIS-04, D29 to
D31).

## Requirements

### Requirement: Open History
Tapping the month name of home's calendar SHALL open History on the current month. Tapping a day of it SHALL open
History on that day's month with the day chosen. History SHALL have a back button that returns to where it was opened
from (BRD HIS-F1, 18.3 A, D29).

#### Scenario: Open from the month name
- **WHEN** the user taps "October" on home
- **THEN** History opens on October 2026 with no day chosen

#### Scenario: Open from a day
- **WHEN** the user taps 14 October on home
- **THEN** History opens on October 2026 with 14 October chosen

### Requirement: Month by month
History SHALL show one month at a time with "Previous month" and "Next month" buttons. It SHALL NOT go before the month
in which the first attack started, in that attack's zone, nor past the current month. With no attack logged, only the
current month SHALL be shown (BRD EC-HIS-03).

#### Scenario: Previous month
- **WHEN** History shows October 2026 and the first attack was in August 2026, and the user taps "Previous month"
- **THEN** September 2026 is shown

#### Scenario: Not before the first attack
- **WHEN** History shows August 2026, the month of the first attack
- **THEN** "Previous month" is disabled

#### Scenario: Not past this month
- **WHEN** History shows the current month
- **THEN** "Next month" is disabled

### Requirement: The month's attacks
Under the calendar, History SHALL list every attack that touches the shown month, newest first. Each row SHALL show its
day, its start and end, or "In progress", and its peak, or "Not recorded". A title SHALL count them, and tapping a row
SHALL open that attack's details. The list SHALL load one month at a time (BRD HIS-F2, EC-HIS-02).

#### Scenario: Newest first
- **WHEN** October has attacks on 3, 14 and 20 October
- **THEN** History lists 20, 14 and 3 October in that order under "3 migraines in October"

#### Scenario: Attack across two months
- **WHEN** an attack ran from 23:00 on 30 September to 02:00 on 1 October
- **THEN** it is listed both in September and in October

#### Scenario: Empty month
- **WHEN** nothing was logged in September
- **THEN** History shows "No migraines logged in September" under the calendar

#### Scenario: Open an attack
- **WHEN** the user taps a listed attack
- **THEN** its details screen opens, where it can be edited or deleted

### Requirement: Choose a day
Tapping today or a past day SHALL choose it: the day is outlined, and the list shows that day's attacks, oldest first,
under the day's title, with "Show all of <month>" to return to the month. Tapping the chosen day again, or changing the
month, SHALL clear the choice (BRD HIS-F3).

#### Scenario: Choose a day
- **WHEN** the user taps 14 October, which had attacks at 09:10 and at 18:00
- **THEN** "Wednesday, 14 October" lists the 09:10 attack first and then the 18:00 attack

#### Scenario: Day with nothing logged
- **WHEN** the user taps 6 October, when nothing was logged
- **THEN** History shows "Nothing logged on this day." and "Add a past migraine"

#### Scenario: Back to the month
- **WHEN** a day is chosen and the user taps "Show all of October"
- **THEN** the list shows all of October's attacks again

### Requirement: Add a past migraine for the chosen day
While a day is chosen, History SHALL offer "Add a past migraine", which opens the add screen of `past-attacks` for that
day. After the attack is added, its details open, and back from them SHALL return to History, which then shows the new
attack (BRD D30, D26).

#### Scenario: Add for a chosen day
- **WHEN** 20 October is chosen and the user taps "Add a past migraine"
- **THEN** the add screen opens with its times on 20 October

#### Scenario: Back to History after adding
- **WHEN** the user adds the attack and goes back from its details
- **THEN** History shows 20 October with the new attack's sky and lists it

### Requirement: No attacks yet
When no attack has been logged, History SHALL show the current month, a short explanation of the skies and "Log your
first migraine", which opens the add screen of `past-attacks` (BRD EC-HIS-01).

#### Scenario: First use
- **WHEN** no attack has ever been logged and the user opens History
- **THEN** it shows the current month with every past day dashed, the explanation and "Log your first migraine"

### Requirement: Undo a deletion on History
After an attack opened from History is deleted, History SHALL no longer show it and SHALL offer "Migraine deleted" with
"Undo" once, for the undo window of `in-app-messages`. Undo SHALL restore the attack. When restoring would overlap
another attack or make a second one in progress, History SHALL say "This can no longer be undone." (BRD EC-HIS-04,
EC-ATK-14, D31).

#### Scenario: Undo from History
- **WHEN** the user deletes the 14 October attack from its details and taps "Undo" on History
- **THEN** 14 October shows its sky again, the attack is listed again, and "Undone" is shown

#### Scenario: Offered once
- **WHEN** the user dismisses "Migraine deleted" and rotates the phone
- **THEN** History does not offer the undo again

### Requirement: History stays current
History SHALL load again whenever it is shown, so changes made on the details screen (times, pain entries, a merge or a
deletion) appear at once. The shown month and the chosen day SHALL survive rotation and process death (BRD EC-ATK-10).

#### Scenario: Time moved to another day
- **WHEN** the user opens the 14 October attack from History, moves its start and end to 15 October and goes back
- **THEN** 14 October has a dashed outline and 15 October shows the attack's sky

#### Scenario: Rotation and process death
- **WHEN** History shows September with 12 September chosen and the phone is rotated, or the app is killed in the
  background and opened again
- **THEN** History still shows September with 12 September chosen

### Requirement: Accessible History
At 200% font and in landscape, nothing on History SHALL be clipped, and the screen SHALL scroll. The month buttons SHALL
be read as "Previous month" and "Next month". Each listed attack SHALL be read as one item with its day, times and peak
(BRD EC-UX-03, EC-UX-04).

#### Scenario: Screen reader on a listed attack
- **WHEN** TalkBack focuses the attack of 14 October from 09:10 to 10:00 that peaked at 4
- **THEN** it reads "Wednesday, 14 October, 09:10 to 10:00, peak 4 of 10. Opens its details."

#### Scenario: Landscape at 200% font
- **WHEN** the phone is in landscape with the font at 200%
- **THEN** the calendar and the list can be scrolled to their ends and no text is clipped

### Requirement: Listed attacks reachable with a keyboard
Each listed attack on History and the month card's title on home SHALL be focusable for keyboards and accessibility
services, keep their spoken description, and act when activated (BRD EC-UX-19).

#### Scenario: Accessibility services on a listed attack
- **WHEN** an accessibility service inspects a listed attack
- **THEN** it is reported as focusable and clickable, with its description
