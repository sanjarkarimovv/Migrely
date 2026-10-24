# month-sky Specification

## Purpose
Defines the month calendar of small skies that home and History show: how the attacks of a month become one sky per
day, which days they mark, and how the calendar reads without colour or sight, so that a month can be understood at a
glance and nothing is shown that the data does not say (BRD HIS-F1, EC-UX-09, EC-UX-15, G6, D29).

## Requirements

### Requirement: One sky per day from data
A month calendar SHALL show each day as a small sky made only from the attacks touching it. The worst attack decides:
1–3 hazy, 4–6 cloudy, 7–10 storm, no pain level cloudy; an attack in progress shows rain. A day without attacks SHALL be
clear and starry only when marked pain-free; other past days have a dashed outline, future days are empty. A period day
SHALL show a small drop in its corner (BRD HIS-F1, EC-UX-09, EC-ATK-07, EC-ATK-08, G6, D47, D49).

#### Scenario: Worst attack decides
- **WHEN** two attacks on 14 October peaked at 3 and 8
- **THEN** 14 October shows a storm

#### Scenario: Pain level never recorded
- **WHEN** the only attack on 14 October has no pain level
- **THEN** 14 October shows a cloudy sky

#### Scenario: Unrecorded and mild
- **WHEN** one attack on 14 October peaked at 2 and another has no pain level
- **THEN** 14 October shows a cloudy sky

#### Scenario: Nothing logged
- **WHEN** no attack touches 6 October and today is 23 October
- **THEN** 6 October has a dashed outline and no sky

#### Scenario: Future day
- **WHEN** today is 23 October
- **THEN** 25 October is empty

#### Scenario: Attack in progress
- **WHEN** an attack started on 23 October and has not ended
- **THEN** 23 October shows rain

#### Scenario: Pain-free day
- **WHEN** nothing was logged on 6 October and the user marked it pain-free
- **THEN** 6 October shows a clear, starry sky

#### Scenario: Period day
- **WHEN** 14 October is marked as a period day and has a storm
- **THEN** 14 October shows the storm with the period drop in its corner

#### Scenario: Logged ahead of the phone's date
- **WHEN** an attack ran from 01:00 to 02:00 Tashkent time on 25 October and the phone is now in Berlin on 24 October
- **THEN** 25 October shows the attack's sky, in the zone where it was logged
- **AND** 25 October cannot be chosen until that date arrives on the phone (BRD D50)

### Requirement: Days follow where the attack was logged
An attack SHALL mark every day it touches from its start to its end, in the zone where it started. An attack in progress
SHALL mark every day from its start to today. An attack that ends exactly at midnight SHALL NOT mark the next day
(BRD EC-ATK-06, EC-TIM-02).

#### Scenario: Past midnight
- **WHEN** an attack ran from 22:00 on 14 October to 03:00 on 15 October
- **THEN** both 14 and 15 October show its sky

#### Scenario: Ends at midnight
- **WHEN** an attack ran from 21:00 to 00:00 on 14 October
- **THEN** only 14 October shows its sky

#### Scenario: Logged in another zone
- **WHEN** an attack was logged in Tokyo at 08:00 on 15 October, which was 19:00 on 14 October in New York, and the
  phone is now in New York
- **THEN** the attack marks 15 October, not 14 October

### Requirement: Calendar follows the locale
Weeks SHALL start on the locale's first day of the week, with the weekday initials in the locale's language. Today
SHALL be outlined (BRD EC-TIM-05).

#### Scenario: Week starts on Monday
- **WHEN** the phone's locale is English (United Kingdom)
- **THEN** the weeks start on Monday

#### Scenario: Week starts on Sunday
- **WHEN** the phone's locale is English (United States)
- **THEN** the weeks start on Sunday

#### Scenario: Today outlined
- **WHEN** the calendar shows the current month
- **THEN** today's day is outlined

### Requirement: Legend without a guess
A legend under the calendar SHALL explain each state: pain-free (clear), mild (hazy), moderate (cloudy), severe (storm),
not logged, and the period mark. The clear sky SHALL mean only a day the user marked pain-free (BRD G6, D29, D49).

#### Scenario: Legend
- **WHEN** the calendar is shown
- **THEN** the legend lists "Pain-free", "Mild", "Moderate", "Severe", "Not logged" and "Period", each with its sky or
  mark

### Requirement: Accessible days
Each day SHALL be told apart by shape as well as colour (stars, waves, cloud, lightning, rain, dashed outline, drop).
Each day SHALL have a spoken description with its date, its sky, the number of migraines, the peak and its marks, and
today SHALL be announced as today. Future days SHALL NOT be selectable. Day numbers SHALL stay readable at 200% font
(BRD EC-UX-15, EC-UX-04, EC-UX-03, D49).

#### Scenario: Screen reader on a storm day
- **WHEN** TalkBack focuses 15 October, when one attack peaked at 9
- **THEN** it reads "Thursday, 15 October: storm. 1 migraine. Peak: severe."

#### Scenario: Screen reader on an empty day
- **WHEN** TalkBack focuses 6 October, when nothing was logged
- **THEN** it reads "Tuesday, 6 October: nothing logged."

#### Scenario: Screen reader on today during an attack
- **WHEN** TalkBack focuses today, 23 October, while an attack is in progress
- **THEN** it reads "Today, Friday, 23 October: rain. Migraine in progress."

#### Scenario: Large font
- **WHEN** the font size is 200%
- **THEN** every day number is readable and none is clipped

#### Scenario: Screen reader on a pain-free period day
- **WHEN** TalkBack focuses 6 October, marked pain-free and as a period day
- **THEN** it reads "Tuesday, 6 October: pain-free. Period."

### Requirement: Only the shown month is loaded
The calendar SHALL read only the attacks that touch the month it shows, through indexed queries, so a long history does
not slow it down (BRD EC-HIS-02).

#### Scenario: Ten years of history
- **WHEN** 3,600 attacks are stored and October is shown
- **THEN** only the attacks touching October, plus a margin of two days on each side for other zones, are read

### Requirement: Days reachable with a keyboard
The month SHALL be one Tab stop: Tab enters it on the chosen day, otherwise today, otherwise the first day, and the
next Tab leaves it; Shift+Tab does the same backwards. Enter SHALL choose the focused day. Each selectable day SHALL
stay focusable for accessibility services such as Switch Access, with its spoken description and state (BRD EC-UX-19,
EC-UX-15, D38).

#### Scenario: Tab through the days
- **WHEN** History is open and the user presses Tab on a hardware keyboard until the calendar is reached
- **THEN** focus lands on one day, the arrow keys move it through the selectable days, and Enter chooses the focused
  day

#### Scenario: One Tab stop
- **WHEN** focus is on today in home's calendar and the user presses Tab
- **THEN** focus leaves the calendar for "Migraine started", without visiting the other days

#### Scenario: Entering on today
- **WHEN** focus is on the month's title and the user presses Tab, with no day chosen
- **THEN** focus lands on today

#### Scenario: Accessibility services
- **WHEN** an accessibility service inspects a past day
- **THEN** the day is reported as focusable and clickable, with its description

### Requirement: Arrow keys move between days
In the month, the arrow keys SHALL move focus a day or a week on or back, with left and right mirrored in
right-to-left layouts, and SHALL do nothing off the selectable days, so focus stays in the month (BRD D38).

#### Scenario: Arrow keys
- **WHEN** focus is on 14 October and the user presses the right arrow and then the down arrow
- **THEN** focus is on 22 October

#### Scenario: Arrow keys stay on selectable days
- **WHEN** focus is on today, 24 October, in home's calendar and the user presses the right arrow or the down arrow
- **THEN** focus stays on 24 October, because the days after it cannot be chosen yet
