# today-sky Specification

## Purpose
Defines the sky card for today on the home screen: how today's migraine data becomes a sky (calm, rain, haze, cloud or
storm), and the rule that the app never shows more than the data says.

## Requirements

### Requirement: Today's sky reflects today's data only
The home screen SHALL show a sky card for today derived only from attacks that touch today, using the device's current
day. The card SHALL NOT claim today is pain-free unless the user has marked it so (marking arrives in a later change),
and SHALL never show a clear sky as a guess (BRD EC-UX-09, EC-UX-10, G6).

#### Scenario: Nothing logged today
- **WHEN** no attack touches today
- **THEN** the card shows a calm night sky with "Calm so far today" and "No migraine logged"

#### Scenario: Attack in progress
- **WHEN** an attack is in progress
- **THEN** the card shows a rain sky as part of attack mode

#### Scenario: Attack from last night
- **WHEN** an attack started yesterday at 23:30 and ended today at 02:00
- **THEN** today's card reflects that attack

### Requirement: Sky by peak intensity after attacks end
When every attack touching today has ended, the card SHALL show the sky for the highest peak intensity among them:
1–3 hazy, 4–6 cloudy, 7–10 storm. Attacks with unknown intensity SHALL count as cloudy. The card SHALL state how many
attacks touched today and their highest intensity (BRD EC-UX-09, EC-ATK-07, EC-ATK-08).

#### Scenario: One moderate attack
- **WHEN** one attack today ended with a peak intensity of 6
- **THEN** the card shows a cloudy sky with "1 migraine today" and "Peak: moderate"

#### Scenario: Several attacks
- **WHEN** two attacks today ended with peak intensities 3 and 9
- **THEN** the card shows a storm sky with "2 migraines today" and "Peak: severe"

#### Scenario: Intensity never recorded
- **WHEN** the only attack today ended without any intensity recorded
- **THEN** the card shows a cloudy sky with "1 migraine today" and "Peak: not recorded"

### Requirement: Sky readable without colour
Each sky state SHALL be distinguishable by shape as well as colour (stars, waves, cloud, lightning, rain) and SHALL have
a spoken description for screen readers. Lightning SHALL be drawn as a still, dim line and never flash
(BRD EC-UX-15, EC-UX-07).

#### Scenario: Screen reader on the card
- **WHEN** TalkBack focuses today's card after a severe attack
- **THEN** it reads the sky and the summary, for example "Today: storm. 1 migraine today. Peak: severe."

### Requirement: Card stays current
The card SHALL update as soon as today's data changes and when the day changes at local midnight while the app is open,
without restarting the app.

#### Scenario: Attack ends
- **WHEN** the user ends an attack that peaked at 9
- **THEN** the card changes from rain to storm without leaving the screen

#### Scenario: Midnight passes
- **WHEN** the home screen is open across local midnight and nothing touches the new day
- **THEN** the card changes to "Calm so far today" for the new day

### Requirement: Today's attacks open their details
Below today's sky, the home screen SHALL list every ended attack that touched today, oldest first, each with its start
and end time and its peak (or "Not recorded"). An attack that started on an earlier day SHALL show that day. Tapping an
attack SHALL open its details (BRD 18.3 A).

#### Scenario: Two attacks today
- **WHEN** attacks ran today 09:10–10:00 peaking at 4 and 14:05–16:30 peaking at 9
- **THEN** the home screen lists "09:10 – 10:00 · 4/10" and "14:05 – 16:30 · 9/10"

#### Scenario: Attack from last night
- **WHEN** an attack ran from yesterday 23:30 to today 02:00
- **THEN** it is listed with yesterday's day before its start time

#### Scenario: Open from the list
- **WHEN** the user taps a listed attack
- **THEN** that attack's details screen opens

### Requirement: Deleted attack can be undone from home
After an attack is deleted from its details screen, the home screen SHALL show "Migraine deleted" with "Undo" for the
undo window of `in-app-messages`. Undo SHALL restore the attack. When another attack has started since, so that
restoring would create two attacks in progress or an overlap, the home screen SHALL say "This can no longer be undone."
(BRD ATK-F7, EC-ATK-14, EC-UX-16).

#### Scenario: Undo from home
- **WHEN** the user deletes today's only attack and taps "Undo"
- **THEN** today's card and list show that attack again

#### Scenario: Rotation keeps the undo
- **WHEN** the user rotates the phone 2 seconds after the deletion
- **THEN** "Migraine deleted" with "Undo" is still shown for the rest of the window

### Requirement: Add a past migraine from home
When no attack is in progress, the home screen SHALL offer "Add a past migraine" under "Migraine started", in portrait
and in landscape. It SHALL open the add screen of `past-attacks`. Attack mode SHALL NOT offer it (BRD ATK-F3, 18.3 A,
18.4 G4, D26).

#### Scenario: Open from home
- **WHEN** no attack is in progress and the user taps "Add a past migraine"
- **THEN** the add screen opens

#### Scenario: Not in attack mode
- **WHEN** an attack is in progress
- **THEN** the home screen does not show "Add a past migraine"

#### Scenario: Landscape
- **WHEN** the phone is in landscape with no attack in progress
- **THEN** "Add a past migraine" is shown under "Migraine started"

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

### Requirement: Today's attacks reachable with a keyboard
Each row under "Logged today" SHALL be focusable for keyboards and accessibility services such as Switch Access, keep
its spoken description, and open the attack's details when activated (BRD EC-UX-19, EC-UX-04).

#### Scenario: Tab to a row
- **WHEN** an attack is listed under "Logged today" and the user presses Tab on a hardware keyboard
- **THEN** focus reaches the row, and Enter opens its details

#### Scenario: Accessibility services
- **WHEN** an accessibility service inspects the row
- **THEN** the row is reported as focusable and clickable, with its description
