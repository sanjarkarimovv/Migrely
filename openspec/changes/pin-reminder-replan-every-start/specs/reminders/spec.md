## ADDED Requirements

### Requirement: Reminders recover when their planned runs are lost
The daily and visit reminders SHALL be planned again at every start of the app's process, whatever starts it: an activity, a
receiver, or a job, even a stale job whose WorkManager record is gone. An open "Did it help?" question SHALL stay on home's
card when its notification is lost. The app SHALL add no receiver and no permission for this (BRD EC-REM-22, EC-REM-23,
EC-STO-10, D175, #197).

#### Scenario: An update emptied WorkManager's database (Android 8.0)
- **WHEN** on API 26 the daily reminder is on for a time R, storage was cleared and Migrely reopened on 1.1.1, the app is
  updated to the build under test before R, and nobody opens it
- **THEN** WorkManager's database is gone after the update, the run at R posts nothing, and WorkManager logs "Didn't find
  WorkSpec"
- **AND** within 5 minutes after R a daily run for the next day's R is planned, while the app is still not opened

#### Scenario: The next day's reminder posts
- **WHEN** the steps above have run and the next day's R comes, with the app still not opened
- **THEN** "How was today?" posts once at about R

#### Scenario: A visit reminder lost the same way
- **WHEN** after the same kind of update a next visit is set, its reminder has not been shown, and its reminder time comes
- **THEN** the reminder posts once that day, within a few minutes, without the app being opened (BRD EC-REM-14)

#### Scenario: "Did it help?" after the same update
- **WHEN** an attack medication was logged less than 2 hours before the same kind of update
- **THEN** no "Did it help?" notification posts at 2 hours
- **AND** when Migrely is opened within 24 hours of the intake, home's card asks how much that medication helped

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

#### Scenario: A clock change or a restart in the gap
- **WHEN** the clock, the time zone or a restart starts the process before the first stale job comes due
- **THEN** that start plans the reminders by the new local time, and no reminder posts twice for one date

#### Scenario: The process dies while planning
- **WHEN** the process is killed while the reminders are being planned
- **THEN** the next start plans them again, and each kind has at most one planned run

#### Scenario: Free and Premium
- **WHEN** the same steps run for a free user and for a Premium user
- **THEN** the reminders and the card behave the same

#### Scenario: Android 16 control
- **WHEN** QA runs the same steps on API 36
- **THEN** the update keeps WorkManager's database, and the daily reminder posts at about R on the day of the update

#### Scenario: No new receiver or permission
- **WHEN** the release manifest is merged
- **THEN** it declares no `MY_PACKAGE_REPLACED` receiver, and its permissions are the same 6 as 1.2.1's
