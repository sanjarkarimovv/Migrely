## MODIFIED Requirements

### Requirement: Today's sky reflects today's data only
The home screen SHALL show a sky card for today derived only from attacks that touch today: attacks whose days,
worked out in the zone where each one started, include the device's current date. The calendar and History use the
same days. The card SHALL NOT claim today is pain-free unless the user has marked it so (marking arrives in a later
change), and SHALL never show a clear sky as a guess (BRD EC-UX-09, EC-UX-10, EC-TIM-02, G6, D36).

#### Scenario: Nothing logged today
- **WHEN** no attack touches today
- **THEN** the card shows a calm night sky with "Calm so far today" and "No migraine logged"

#### Scenario: Attack in progress
- **WHEN** an attack is in progress
- **THEN** the card shows a rain sky as part of attack mode

#### Scenario: Attack from last night
- **WHEN** an attack started yesterday at 23:30 and ended today at 02:00
- **THEN** today's card reflects that attack

#### Scenario: Logged before flying east
- **WHEN** an attack ran from 23:15 to 23:45 Berlin time on 24 October and the user opens the app in Tashkent at
  02:15 on 25 October
- **THEN** today's card shows "Calm so far today" and nothing is listed under "Logged today"
- **AND** the month calendar on the same screen shows the attack on 24 October

#### Scenario: Logged before flying west
- **WHEN** an attack ran from 01:00 to 02:00 Tashkent time on 25 October and the user opens the app in Berlin at
  23:30 on 24 October
- **THEN** today's card does not count it, because the attack belongs to 25 October

#### Scenario: Logged in another zone on the same date
- **WHEN** an attack ran from 08:00 to 09:00 Tashkent time on 24 October and the user opens the app in Dubai later
  that day
- **THEN** today's card counts it

### Requirement: Today's attacks open their details
Below today's sky, the home screen SHALL list every ended attack that touched today, oldest first, each with its start
and end time and its peak (or "Not recorded"). An attack that started on an earlier day SHALL show that day. Times in
another zone than the device's SHALL name that zone's city once, after the end. Tapping an attack SHALL open its
details (BRD 18.3 A, EC-TIM-02, D36).

#### Scenario: Two attacks today
- **WHEN** attacks ran today 09:10–10:00 peaking at 4 and 14:05–16:30 peaking at 9
- **THEN** the home screen lists "09:10 – 10:00 · 4/10" and "14:05 – 16:30 · 9/10"

#### Scenario: Attack from last night
- **WHEN** an attack ran from yesterday 23:30 to today 02:00
- **THEN** it is listed with yesterday's day before its start time

#### Scenario: Logged in another zone
- **WHEN** an attack ran today from 08:00 to 09:00 Tashkent time and the device is now in Dubai
- **THEN** it is listed as "08:00 – 09:00 (Tashkent)", with the city once

#### Scenario: Open from the list
- **WHEN** the user taps a listed attack
- **THEN** that attack's details screen opens
