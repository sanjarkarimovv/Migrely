# reminders Specification

## Purpose
Defines the reminders Migrely sends and how the user controls them, so that a pain-free day and a medication's effect
can be recorded without the user remembering to open the app, and nothing is sent before the user allows notifications
on the Reminders screen (BRD REM-F1, REM-F2, EC-REM-01–06, D65–D69).

## Requirements

### Requirement: The Reminders screen
Home SHALL show a "Settings" button in its header in normal mode only, and Settings SHALL list "Reminders". It SHALL
open a screen with, first, a "Doctor visit" section: its heading, "A reminder two days before, to make a report for
your doctor", and "Next visit" reading "Not set" until a date is saved; then "Daily reminder", off by default, with
its time (20:00 by default), and "Ask if a medication helped" (BRD D65, D66, D67, D70, D168, REM-F4, EC-UX-13).

#### Scenario: Opening Reminders
- **WHEN** the user taps "Settings" on home, then "Reminders"
- **THEN** the screen shows the "Doctor visit" heading with "Next visit" reading "Not set", then "Daily reminder" off at
  8:00 PM and "Ask if a medication helped"

#### Scenario: A heading for navigation
- **WHEN** a TalkBack user moves by headings on the Reminders screen
- **THEN** "Doctor visit" is a heading

#### Scenario: Attack mode
- **WHEN** an attack is in progress
- **THEN** home shows no "Settings" button

### Requirement: Notification permission
The app SHALL ask for notification permission only when the user turns a reminder on or sets a doctor visit, never at
first launch, and at most once in 30 days. When it is refused, a reminder SHALL stay off, a doctor visit SHALL stay
saved, and the screen SHALL explain that notifications are off and link to the system settings (BRD EC-REM-01,
EC-REM-02, EC-REM-11, D68, D168).

#### Scenario: Turning the daily reminder on
- **WHEN** the user turns "Daily reminder" on without having allowed notifications
- **THEN** the system asks for permission, and the reminder is on only if it is allowed

#### Scenario: Refused
- **WHEN** the user refuses the permission
- **THEN** "Daily reminder" stays off, and the screen says notifications are off for Migrely, with "Open settings"

#### Scenario: Setting a visit without permission
- **WHEN** the user sets a doctor visit without having allowed notifications, and refuses when asked
- **THEN** the visit is saved, and the screen says notifications are off for Migrely, with "Open settings"

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
SHALL not mark it, and the notification SHALL read "Not marked. Open Migrely to see today.", which does not say that
a migraine was logged. "Log a migraine" SHALL open home (BRD EC-REM-06, EC-REM-21, EC-DAY-01, D66, D169).

#### Scenario: Pain-free day
- **WHEN** the user taps "Pain-free day" on a day without an attack
- **THEN** today is marked pain-free and the notification goes away

#### Scenario: An attack today
- **WHEN** the user taps "Pain-free day" and an attack is logged today
- **THEN** no mark is set, and the notification reads "Not marked. Open Migrely to see today."

### Requirement: "Did it help?" notification
Two hours after an attack-medication intake, if its question is still open and "Ask if a medication helped" is on with
notifications allowed, a notification "Did it help?" SHALL arrive, naming no medication, and tapping it SHALL open
home, where the card asks about that medication. It SHALL be cancelled when the question is answered, skipped or the
intake undone, and SHALL not be sent during an attack (BRD REM-F2, MED-F4, EC-MED-05, EC-REM-21, D39, D67, D69, D169).

#### Scenario: Two hours later
- **WHEN** Ibuprofen is logged at 2:00 PM and nothing is answered
- **THEN** at about 4:00 PM a notification "Did it help?" arrives, with "Tap to answer in Migrely." and no medication
  name

#### Scenario: The card names it
- **WHEN** the user taps that notification
- **THEN** home opens and its card asks how much Ibuprofen helped

#### Scenario: During an attack
- **WHEN** the two hours pass while an attack is still in progress
- **THEN** no notification arrives, and home's card asks once the attack ends

#### Scenario: Answered first
- **WHEN** the user answers "Did it help?" on home before 4:00 PM
- **THEN** no notification arrives

#### Scenario: No permission
- **WHEN** notifications are not allowed and the two hours pass
- **THEN** no notification arrives, and home's card still asks

### Requirement: Offered during onboarding
The reminders feature SHALL provide the daily reminder offer that onboarding shows. It SHALL turn the reminder on at
its saved time, 8:00 PM by default, by the same permission rules as the Reminders screen (BRD REM-F1, EC-REM-01,
D80).

#### Scenario: Permission refused
- **WHEN** the user taps "Turn on" in onboarding and refuses notifications
- **THEN** the reminder stays off, onboarding goes on, and the Reminders screen later shows the notice

### Requirement: Notifications on the lock screen
No Migrely notification SHALL show the person's own health data where the lock screen can show it: a medication, a
symptom, a pain level, a count, a logged attack or a visit's purpose. Generic text, the same for everyone every day,
is allowed. Every notification SHALL be `VISIBILITY_PRIVATE`, with a public version titled "Reminder" and no text, and
no channel name SHALL show there (BRD EC-REM-16, EC-REM-21, D169).

#### Scenario: Hide sensitive content
- **WHEN** the lock screen hides sensitive content and the daily reminder, "Did it help?" or the visit reminder
  arrives
- **THEN** each shows Migrely with "Reminder" only, and no action or text

#### Scenario: Show all content
- **WHEN** the lock screen shows all content and "Did it help?" arrives for a Sumatriptan intake
- **THEN** it reads "Did it help?" and "Tap to answer in Migrely.", and "Sumatriptan" shows nowhere on the lock screen

#### Scenario: The daily reminder under show all
- **WHEN** the lock screen shows all content and the daily reminder arrives
- **THEN** it shows the same generic text and actions as on every day for every user

#### Scenario: Negative control
- **WHEN** the same check runs on the 1.1.1 build under show all content
- **THEN** the medication name shows, so the check can fail
