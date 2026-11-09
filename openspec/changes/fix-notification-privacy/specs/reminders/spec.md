## MODIFIED Requirements

### Requirement: Answering from the notification
"Pain-free day" SHALL mark today pain-free by History's rule, without opening the app. If an attack touches today, it
SHALL not mark it, and the notification SHALL read "Not marked. Today already has an entry.", which does not say that
a migraine was logged. "Log a migraine" SHALL open home (BRD EC-REM-06, EC-REM-21, EC-DAY-01, D66, D169).

#### Scenario: Pain-free day
- **WHEN** the user taps "Pain-free day" on a day without an attack
- **THEN** today is marked pain-free and the notification goes away

#### Scenario: An attack today
- **WHEN** the user taps "Pain-free day" and an attack is logged today
- **THEN** no mark is set, and the notification reads "Not marked. Today already has an entry."

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

## ADDED Requirements

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
