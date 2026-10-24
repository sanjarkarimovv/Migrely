## Purpose

Defines the reminders Migrely sends and how the user controls them, so that a pain-free day and a medication's effect
can be recorded without the user remembering to open the app, while nothing is sent the user did not turn on (BRD
REM-F1, REM-F2, EC-REM-01–06, D65–D68).

## ADDED Requirements

### Requirement: The Reminders screen
Home SHALL show a "Reminders" button in its header in normal mode only. It SHALL open a screen with "Daily reminder",
off by default, with its time (20:00 by default), and "Ask if a medication helped" (BRD D65, D66, D67, EC-UX-13).

#### Scenario: Opening Reminders
- **WHEN** the user taps "Reminders" on home
- **THEN** the Reminders screen shows "Daily reminder" off at 8:00 PM and "Ask if a medication helped"

#### Scenario: Attack mode
- **WHEN** an attack is in progress
- **THEN** home shows no "Reminders" button

### Requirement: Notification permission
The app SHALL ask for notification permission only when the user turns a reminder on, never at first launch. When it
is refused, the reminder SHALL stay off, the screen SHALL explain why and link to the system settings, and the app SHALL
ask again at most once in 30 days (BRD EC-REM-01, EC-REM-02, D68).

#### Scenario: Turning the daily reminder on
- **WHEN** the user turns "Daily reminder" on without having allowed notifications
- **THEN** the system asks for permission, and the reminder is on only if it is allowed

#### Scenario: Refused
- **WHEN** the user refuses the permission
- **THEN** "Daily reminder" stays off, and the screen says notifications are off for Migrely, with "Open settings"

### Requirement: The daily reminder
When on, the daily reminder SHALL arrive at about its time each day, within about 15 minutes, as "How was today?" with
the actions "Pain-free day" and "Log a migraine". It SHALL be skipped when today is already marked pain-free or has an
attack. It SHALL be scheduled again after each run, after a reboot, and when the time zone or the clock changes, by the
new local time (BRD REM-F1, EC-REM-03, EC-REM-04, EC-REM-05, D66).

#### Scenario: A calm day
- **WHEN** the daily reminder is on for 8:00 PM and nothing is logged today
- **THEN** at about 8:00 PM "How was today?" arrives with "Pain-free day" and "Log a migraine"

#### Scenario: Already answered
- **WHEN** today is already marked pain-free, or an attack is logged today
- **THEN** no daily reminder arrives today

#### Scenario: Travel
- **WHEN** the phone moves from Tashkent to Berlin
- **THEN** the next reminder arrives at about 8:00 PM Berlin time

### Requirement: Answering from the notification
"Pain-free day" SHALL mark today pain-free by History's rule, without opening the app. If an attack touches today, it
SHALL not mark it, and the notification SHALL read "A migraine is logged today." "Log a migraine" SHALL open home (BRD
EC-REM-06, EC-DAY-01, D66).

#### Scenario: Pain-free day
- **WHEN** the user taps "Pain-free day" on a day without an attack
- **THEN** today is marked pain-free and the notification goes away

#### Scenario: An attack today
- **WHEN** the user taps "Pain-free day" and an attack is logged today
- **THEN** no mark is set, and the notification reads "A migraine is logged today."

### Requirement: "Did it help?" notification
Two hours after an attack-medication intake, if its question is still open and "Ask if a medication helped" is on with
notifications allowed, a notification "Did <medication> help?" SHALL arrive, and tapping it SHALL open home, where the
card asks. It SHALL be cancelled when the question is answered, skipped or the intake undone. Without permission the
in-app card SHALL still ask (BRD REM-F2, MED-F4, EC-MED-05, D39, D67).

#### Scenario: Two hours later
- **WHEN** Ibuprofen is logged at 2:00 PM and nothing is answered
- **THEN** at about 4:00 PM "Did Ibuprofen help?" arrives

#### Scenario: Answered first
- **WHEN** the user answers "Did it help?" on home before 4:00 PM
- **THEN** no notification arrives
