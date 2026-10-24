## MODIFIED Requirements

### Requirement: Today's sky reflects today's data only
The home screen SHALL show a sky card for today derived only from attacks that touch today: attacks whose days,
worked out in the zone where each one started, include the device's current date. The calendar and History use the
same days. The card SHALL show a clear sky with a full moon and "Pain-free today" only when the user marked today
pain-free and no attack touches it, and SHALL never guess a clear sky (BRD EC-UX-09, EC-UX-10, EC-TIM-02, G6, D36, D49).

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

#### Scenario: Marked pain-free
- **WHEN** no attack touches today and the user marked today pain-free in History
- **THEN** the card shows a clear sky with a full moon, "Pain-free today" and "Marked by you"

#### Scenario: Pain-free mark and an attack
- **WHEN** today is marked pain-free and the user then starts and ends an attack
- **THEN** the card shows the attack's sky, not a clear sky
