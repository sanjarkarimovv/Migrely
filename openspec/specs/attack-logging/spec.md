# attack-logging Specification

## Purpose
Defines how a migraine attack is recorded from the moment it starts until it ends: starting and ending it in one tap,
recording how strong the pain is over time, correcting a late start, checking attacks left open for days, and how times
and days are stored so the record stays correct across midnight, daylight-saving changes and travel.

## Requirements

### Requirement: Start an attack in one tap
The user SHALL be able to start an attack with a single tap on the home screen or from the launcher shortcut. A started
attack SHALL record the current time and the device's current time zone as its start. For 5 seconds after starting,
the app SHALL offer to undo the start (BRD ATK-F1, 6.1 item 13).

#### Scenario: Start from the home screen
- **WHEN** no attack is in progress and the user taps "Migraine started"
- **THEN** an attack in progress is stored with the current time and time zone as its start
- **AND** the home screen switches to attack mode

#### Scenario: Start from the launcher shortcut
- **WHEN** no attack is in progress and the user chooses "Migraine started" from the app icon's shortcut menu
- **THEN** the app opens with an attack in progress, started at the current time

#### Scenario: Undo a start
- **WHEN** the user taps "Undo" within 5 seconds of starting an attack
- **THEN** the attack is removed and nothing about it remains stored
- **AND** the home screen leaves attack mode

#### Scenario: Undo window passes
- **WHEN** 5 seconds pass after starting an attack without the user tapping "Undo"
- **THEN** the undo option disappears and the attack stays in progress

### Requirement: One attack in progress at a time
At most one attack SHALL be in progress at any time. A start request while an attack is in progress SHALL NOT create a
new attack; it SHALL show the attack already in progress (BRD EC-ATK-01).

#### Scenario: Shortcut used during an attack
- **WHEN** an attack is in progress and the user chooses "Migraine started" from the launcher shortcut
- **THEN** no new attack is created and the app shows the attack in progress

#### Scenario: Rapid double tap
- **WHEN** the user taps "Migraine started" twice in quick succession
- **THEN** exactly one attack is in progress

### Requirement: Record pain intensity during an attack
While an attack is in progress the user SHALL be able to record the current pain as Mild, Moderate or Severe, stored as
intensity points of 3, 6 and 9 on the 1–10 scale with the current time. An attack's peak intensity SHALL be the highest
of its points. An attack with no intensity points SHALL be stored with unknown intensity and still count as an attack
(BRD ATK-F1, ATK-F4, EC-ATK-08).

#### Scenario: First intensity
- **WHEN** the user selects "Moderate" during an attack
- **THEN** an intensity point of 6 is stored with the current time

#### Scenario: Pain changes later
- **WHEN** the user selected "Moderate" at 14:10 and selects "Severe" at 15:30
- **THEN** both points are kept (6 at 14:10 and 9 at 15:30) and the attack's peak intensity is 9

#### Scenario: Correction within two minutes
- **WHEN** the user selects "Severe" and then "Moderate" less than 2 minutes later
- **THEN** the later choice replaces the earlier point instead of adding a second one

#### Scenario: No intensity recorded
- **WHEN** an attack ends without any intensity having been selected
- **THEN** the attack is stored with unknown intensity and counts as one attack

### Requirement: Move the start of an attack earlier
While an attack is in progress the user SHALL be able to move its start earlier by 15 minutes, 30 minutes, 1 hour or
2 hours. The new start SHALL NOT be earlier than the end of the previous attack. A rejected move SHALL leave the start
unchanged and say why.

#### Scenario: Pain began before logging
- **WHEN** an attack started at 14:00 and the user chooses "1 hour earlier"
- **THEN** the attack's start becomes 13:00

#### Scenario: Move would overlap the previous attack
- **WHEN** the previous attack ended at 13:30, the current attack started at 14:00, and the user chooses "1 hour earlier"
- **THEN** the start stays 14:00
- **AND** the app explains that the previous attack ended at 13:30

### Requirement: End an attack in one tap
The user SHALL be able to end the attack in progress with a single tap, which records the current time as its end. For
5 seconds after ending, the app SHALL offer to undo the end (BRD ATK-F2).

#### Scenario: End an attack
- **WHEN** an attack is in progress and the user taps "Migraine ended"
- **THEN** the attack's end is stored as the current time
- **AND** the home screen leaves attack mode

#### Scenario: Undo an end
- **WHEN** the user taps "Undo" within 5 seconds of ending an attack
- **THEN** the attack is in progress again with its original start and intensity points

### Requirement: Check attacks open for 72 hours
An attack SHALL never be closed automatically. When the app is opened while an attack has been in progress for 72 hours
or more, the app SHALL ask whether it is still going and show that attacks longer than 72 hours should be discussed
with a doctor. The user SHALL be able to answer "Still going", "It has ended" or "Don't ask again for this attack"
(BRD EC-ATK-02, SAF-F3).

#### Scenario: Prompt after 72 hours
- **WHEN** the app is opened and the attack in progress started 73 hours ago
- **THEN** the app asks whether the attack is still going and shows the 72-hour doctor note

#### Scenario: Still going
- **WHEN** the user answers "Still going"
- **THEN** the attack stays in progress and the question is not asked again until another 72 hours have passed

#### Scenario: Don't ask again
- **WHEN** the user answers "Don't ask again for this attack"
- **THEN** the question is never asked again for this attack

#### Scenario: It has ended
- **WHEN** the user answers "It has ended" and picks a date and time
- **THEN** the attack ends at the picked time
- **AND** only times between the attack's start and now can be picked

### Requirement: Time storage and day attribution
Every attack time SHALL be stored as an instant together with the time zone in effect where the attack started.
The calendar days an attack belongs to SHALL be determined in that time zone. Durations SHALL be real elapsed time
(BRD EC-TIM-01, EC-TIM-02, EC-TIM-03, EC-ATK-06, EC-ATK-07).

#### Scenario: Attack across midnight
- **WHEN** an attack starts at 23:30 and is still in progress at 00:30 the next day
- **THEN** it is one attack that belongs to both days

#### Scenario: Several attacks in one day
- **WHEN** two attacks that do not overlap both start on the same day
- **THEN** both are kept as separate attacks on that day

#### Scenario: Daylight-saving change
- **WHEN** an attack runs from 01:30 to 03:30 local time on the night clocks move forward by one hour
- **THEN** its duration is 1 hour

#### Scenario: Travel to another time zone
- **WHEN** an attack started on 5 March at 23:00 in Tashkent (UTC+5) and the user later views it in Berlin (UTC+1)
- **THEN** the attack still belongs to 5 March and its duration is unchanged

### Requirement: Durable logging
Every logging action SHALL be stored as soon as it is made, so closing or killing the app never loses it. Updating the
app SHALL keep every logged attack. If the device has no free storage, the app SHALL say so, keep what is on screen and
let the user try again (BRD ATK-F6, EC-ATK-10, EC-STO-01, EC-STO-02).

#### Scenario: App killed during an attack
- **WHEN** the user starts an attack, records "Severe", and the system kills the app
- **THEN** after reopening, the attack is in progress with its start and the intensity point of 9

#### Scenario: App update
- **WHEN** a new version of the app is installed over an older one that has logged attacks
- **THEN** all previously logged attacks are still present

#### Scenario: Storage full
- **WHEN** the user taps "Migraine started" and the device storage is full
- **THEN** the app shows that the attack could not be saved because storage is full
- **AND** the "Migraine started" control remains available to try again
