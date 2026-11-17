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
the actions "Pain-free day" and "Log a migraine", unless Migrely is on screen then. It SHALL be skipped when today is
already marked pain-free or has an attack. It SHALL be scheduled again after each run, after a reboot, and when the time
zone or the clock changes, by the new local time (BRD REM-F1, EC-REM-03, EC-REM-04, EC-REM-05, D66, D176).

#### Scenario: A calm day
- **WHEN** the daily reminder is on for 8:00 PM, nothing is logged today, and Migrely is not on screen at 8:00 PM
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

### Requirement: An app start keeps a planned daily reminder
A start of the app's process, whatever starts it, SHALL keep a daily run that is already planned or running. Only setting
the time or turning the reminder on, a clock or time zone change, and a daily run planning its next run SHALL replace a
planned run. When the reminder is off, a start SHALL cancel any planned daily run (BRD D176, EC-REM-24, #213).

#### Scenario: Opened after the time while the run is late
- **WHEN** the daily reminder is on for 8:00 PM, nothing is logged today, the run is held by Doze, the person opens
  Migrely at 8:01 PM, and the run comes while Migrely is opening
- **THEN** the run is kept, nothing posts while Migrely is on screen, and the date is not marked
- **AND** if they leave without logging today, "How was today?" posts once at the postponed run, about 30 minutes after
  the skipped run (about 8:31 PM), not at the moment they leave

#### Scenario: Another start before a late run
- **WHEN** the run for 8:00 PM is held by Doze and, before it comes, the process is started by another Migrely job or by a
  restart of the phone
- **THEN** that run is still planned after the start, and "How was today?" posts once when it comes, if it is still today

#### Scenario: The run starts the process itself
- **WHEN** the process is dead at 8:00 PM and the daily run's job starts it
- **THEN** "How was today?" posts once, whichever of the job and the start-up plan comes first

#### Scenario: The time is changed
- **WHEN** a run is planned for 8:00 PM and the person sets 9:00 PM at 7:00 PM
- **THEN** only a run for 9:00 PM today stays planned

#### Scenario: Turned on or moved after the time
- **WHEN** at 9:00 PM the person turns the reminder on, or moves it to a time already passed today
- **THEN** the next run is at that time tomorrow, as before (BRD D66, EC-REM-28)

#### Scenario: Turned off
- **WHEN** the reminder is turned off, or a start finds it off with a run still planned
- **THEN** no daily run is planned and nothing posts

#### Scenario: Update from a shipped version
- **WHEN** the app is updated from any shipped version while a daily run is planned
- **THEN** the first start keeps that run, and it posts once at about its time

### Requirement: A day that has not had its reminder is not skipped
When a start finds no daily run planned, and when the clock or time zone changes, the daily reminder SHALL be planned for
today's time if it is still ahead; otherwise at once for today, if today's reminder has not posted; otherwise for
tomorrow's time. A run SHALL NOT post about a date that is no longer today (BRD D176, EC-REM-25, EC-REM-26).

#### Scenario: Nothing planned, after the time
- **WHEN** a start after 8:00 PM finds no daily run planned, today's reminder has not posted, Migrely is not on screen,
  and nothing is logged today
- **THEN** "How was today?" posts once, within a few minutes

#### Scenario: Nothing planned, before the time
- **WHEN** a start at 7:00 PM finds no daily run planned
- **THEN** a run is planned for 8:00 PM today

#### Scenario: Nothing planned, today already posted
- **WHEN** a start after 8:00 PM finds no daily run planned and today's reminder has already posted
- **THEN** a run is planned for 8:00 PM tomorrow and nothing posts today

#### Scenario: Clock or zone change after the time, today not posted
- **WHEN** at 8:30 PM the clock is set or the time zone changes, the new local time is still after 8:00 PM, and today's
  reminder has not posted
- **THEN** a run is planned at once for today and "How was today?" posts once, unless today is answered

#### Scenario: Clock moved back after a post
- **WHEN** today's reminder posted at 8:00 PM and the clock is then set back to 7:30 PM
- **THEN** no second "How was today?" posts that day

#### Scenario: A small network time correction
- **WHEN** a run held by Doze is pending after 8:00 PM and the phone corrects its clock by a few seconds
- **THEN** today's reminder still posts once

#### Scenario: Travel east past midnight
- **WHEN** today's reminder has not posted and a zone change makes the local date tomorrow
- **THEN** nothing posts about the date that ended, and the next run is at 8:00 PM on the new date

#### Scenario: Daylight saving time
- **WHEN** the reminder time falls in an hour skipped by daylight saving time
- **THEN** it moves forward by the length of the skipped hour that day, as before

#### Scenario: Midnight between plan and run
- **WHEN** a run planned for today comes after midnight
- **THEN** it does not post, and the next run is planned

### Requirement: No daily reminder while Migrely is on screen
A daily run that comes while Migrely is on screen with the screen on, including while it is opening, SHALL NOT post and
SHALL NOT mark the date as handled. It SHALL plan that date's run again 30 minutes later, replacing the planned run (BRD
D176, EC-REM-27).

#### Scenario: On home at the time
- **WHEN** the person is on Migrely's home at 8:00 PM and today is not answered
- **THEN** no notification posts while they are on screen, and the date is not marked as handled
- **AND** if they leave without answering, "How was today?" posts once about 30 minutes after that run, that day, not at
  the moment they leave

#### Scenario: Opened, browsed History, left without logging
- **WHEN** a late run comes while the person browses History, and they leave Migrely without logging today
- **THEN** "How was today?" posts once about 30 minutes after that run, that day

#### Scenario: Logging an attack when the late run comes
- **WHEN** a late run comes while the person is logging an attack in Migrely, and they save it
- **THEN** no notification posts, and the run 30 minutes later does not post because today has an attack

#### Scenario: Opening the app when the run comes
- **WHEN** the person's tap starts Migrely's process and the daily run comes before the first screen has appeared
- **THEN** no notification posts, and that date's run is planned about 30 minutes later

#### Scenario: Staying in the app
- **WHEN** the person is still on screen when the postponed run comes
- **THEN** it does not post and is postponed another 30 minutes

#### Scenario: Postponed past midnight
- **WHEN** a postponed run comes after midnight
- **THEN** it does not post, and the next run is at 8:00 PM on the new date

#### Scenario: A clock change after an on-screen skip
- **WHEN** a run was skipped because Migrely was on screen, and later that day the clock or zone changes
- **THEN** the date is not handled, so a run is planned at once for today
- **AND** it does not post if Migrely is on screen (it is postponed again); otherwise it posts once unless today is answered

### Requirement: A postponed daily run follows the normal rules
That run follows the normal rules: no post if the date is answered, notifications are off, or the date is no longer today;
no post if Migrely is on screen, which postpones it again. Otherwise it posts once. With the screen off or locked, or
Migrely in the background, it posts as before. At most one daily reminder SHALL post for a date (BRD D176, EC-REM-27).

#### Scenario: Locked screen
- **WHEN** Migrely was the last app on screen and the screen is off or locked at 8:00 PM
- **THEN** "How was today?" posts once

#### Scenario: Lock screen right after the power key
- **WHEN** the person presses the power key and the lock screen shows with Migrely behind it when the run comes
- **THEN** "How was today?" posts once

#### Scenario: Split screen
- **WHEN** Migrely is visible in split screen with another app when the run comes
- **THEN** it counts as on screen: no post, and the run is postponed 30 minutes

#### Scenario: Notification shade over Migrely
- **WHEN** the notification shade is pulled down over Migrely when the run comes
- **THEN** it counts as on screen: no post, and the run is postponed 30 minutes

#### Scenario: One per date
- **WHEN** a clock change re-plans while today's run is posting, or a postponed run comes after a post
- **THEN** the person sees at most one "How was today?" for that date

#### Scenario: Notifications off, or today answered
- **WHEN** notifications are off for Migrely, or today is marked pain-free or has an attack
- **THEN** nothing posts, as before

#### Scenario: Free and Premium
- **WHEN** the same steps run for a free user and a Premium user
- **THEN** the daily reminder behaves the same

### Requirement: Reminders recover when their planned runs are lost
At every start of the app's process, whatever starts it (an activity, a receiver, or a job, even a stale job whose WorkManager
record is gone), a lost daily run SHALL be planned again and the visit reminder SHALL be planned again. An open "Did it help?"
question SHALL stay on home's card when its notification is lost. No receiver and no permission SHALL be added for this
(BRD EC-REM-22, EC-REM-23, EC-STO-10, D175, D176, #197).

#### Scenario: An update emptied WorkManager's database (Android 8.0)
- **WHEN** on API 26 the daily reminder is on for a time R, storage was cleared and Migrely reopened on 1.1.1, the app is
  updated to the build under test before R, and nobody opens it
- **THEN** WorkManager's database is gone after the update, the stale job starts the process, and WorkManager logs
  "Didn't find WorkSpec"
- **AND** "How was today?" posts once as soon as the stale job has started the process, while the app is still not opened
- **AND** the platform decides when that job runs, at R or later; Migrely adds no delay (BRD D175 (2), EC-REM-03)

#### Scenario: The stale job runs after midnight
- **WHEN** after the same kind of update JobScheduler holds the stale job past midnight, and nobody opens Migrely
- **THEN** nothing posts about the date that ended, and the next run is at R on the new date

#### Scenario: Opened before the stale job runs
- **WHEN** after the same kind of update, R has passed, the stale job has not run, and the person opens Migrely
- **THEN** nothing posts while Migrely is on screen, and if they leave without answering, "How was today?" posts once about
  30 minutes after the run that came while they were in the app

#### Scenario: The next day's reminder posts
- **WHEN** the steps above have run and the next day's R comes, with the app still not opened
- **THEN** "How was today?" posts once at about R

#### Scenario: A visit reminder lost the same way
- **WHEN** after the same kind of update a next visit is set, its reminder has not been shown, and its reminder time comes
- **THEN** the reminder posts once when its stale job or any other start comes, if the visit day has not ended, without the
  app being opened (BRD EC-REM-14, D175)

#### Scenario: "Did it help?" after the same update
- **WHEN** an attack medication was logged less than 2 hours before the same kind of update
- **THEN** no "Did it help?" notification posts at 2 hours
- **AND** when Migrely is opened within 24 hours of the intake, home's card asks how much that medication helped (BRD
  EC-REM-23, D175)

#### Scenario: The question's 24 hours passed during the gap
- **WHEN** Migrely is first opened more than 24 hours after that intake
- **THEN** no card asks about it, as for any question left unanswered for 24 hours (BRD EC-MED-05)

#### Scenario: A normal update
- **WHEN** the app is updated and storage was never cleared before it, on API 26 or API 36
- **THEN** WorkManager's database is kept, the daily reminder posts once at about R, and no "Did it help?" notification posts
  twice

#### Scenario: The daily reminder is off
- **WHEN** the daily reminder is off and a stale job starts the process
- **THEN** no daily run is planned and nothing posts

#### Scenario: Restored from a backup
- **WHEN** Migrely is restored on a new phone with the daily reminder on, and first opened after the time
- **THEN** a daily run is planned and nothing posts while the person is in the app
- **AND** if they leave without answering, "How was today?" posts once about 30 minutes after the run that came while
  they were in the app, not at the moment they leave, and the next day's reminder posts at R

#### Scenario: The process dies while planning
- **WHEN** the process is killed while the reminders are being planned
- **THEN** the next start keeps or plans them again, and each kind has at most one planned run

#### Scenario: Android 16 control
- **WHEN** QA runs the same update steps on API 36
- **THEN** the update keeps WorkManager's database, and the daily reminder posts once at about R on the day of the update

#### Scenario: No new receiver or permission
- **WHEN** the release manifest is merged
- **THEN** it declares no `MY_PACKAGE_REPLACED` receiver, and its permissions are the same 6 as 1.2.1's
