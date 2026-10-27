## MODIFIED Requirements

### Requirement: One sky per day from data
A month calendar SHALL show each day as a small sky made only from the attacks touching it. The worst attack decides:
1–3 hazy, 4–6 cloudy, 7–10 storm, no pain level cloudy; an attack in progress shows rain. A day without attacks SHALL be
clear and starry only when marked pain-free. A past day without attacks but with an attack-medication intake, in the
zone where it was taken, SHALL have a solid outline and no sky; preventive intakes SHALL not count. Other past days
have a dashed outline, future days are empty. A period day SHALL show a small drop in its corner (BRD HIS-F1,
EC-UX-09, EC-ATK-07, EC-ATK-08, G6, D47, D49, D100).

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

#### Scenario: A day with medication only
- **WHEN** ibuprofen (an attack medication) was taken on 20 October, no attack touches it, and today is 23 October
- **THEN** 20 October has a solid outline and no sky

#### Scenario: Medication on a migraine day
- **WHEN** ibuprofen was taken on 14 October during an attack that peaked at 8
- **THEN** 14 October shows the storm

#### Scenario: Medication on a pain-free day
- **WHEN** 6 October is marked pain-free and ibuprofen was taken on it
- **THEN** 6 October shows a clear, starry sky, since the mark is the user's word

#### Scenario: Preventive medication only
- **WHEN** only topiramate (a preventive medication) was taken on 20 October
- **THEN** 20 October has a dashed outline and no sky

### Requirement: Legend without a guess
A legend under the calendar SHALL explain each state: pain-free (clear), mild (hazy), moderate (cloudy), severe (storm),
attack medication only (solid outline), not logged (dashed outline), and the period mark. The clear sky SHALL mean only
a day the user marked pain-free (BRD G6, D29, D49, D100).

#### Scenario: Legend
- **WHEN** the calendar is shown
- **THEN** the legend lists "Pain-free", "Mild", "Moderate", "Severe", "Attack medication only", "Not logged" and
  "Period", each with its sky, outline or mark

### Requirement: Accessible days
Each day SHALL be told apart by shape as well as colour (stars, waves, cloud, lightning, rain, solid outline, dashed
outline, drop). Each day SHALL have a spoken description with its date, its sky, the number of migraines, the peak, an
attack-medication intake on a day without attacks, and its marks, and today SHALL be announced as today. Future days
SHALL NOT be selectable. Day numbers SHALL stay readable at 200% font (BRD EC-UX-15, EC-UX-04, EC-UX-03, D49, D100).

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

#### Scenario: Screen reader on a day with medication only
- **WHEN** TalkBack focuses 20 October, when ibuprofen was taken and no attack touches it
- **THEN** it reads "Tuesday, 20 October: attack medication taken, no migraine logged."

#### Scenario: Screen reader on a pain-free day with medication
- **WHEN** TalkBack focuses 6 October, marked pain-free, when ibuprofen was taken on it
- **THEN** it reads "Tuesday, 6 October: pain-free. Attack medication taken."
