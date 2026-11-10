## ADDED Requirements

### Requirement: Reminders are planned again after an update
After an app update, Android's `MY_PACKAGE_REPLACED` SHALL start the app's process without the user opening it, and the reminders SHALL be planned
once, by the re-plan the process start already runs; the receiver SHALL NOT plan again. The daily reminder SHALL stay one planned run at its next
time strictly after now, never one for a time already passed. The visit reminder SHALL follow EC-REM-14. No permission is added (BRD EC-REM-22, D170).

#### Scenario: An update before today's reminder
- **WHEN** the daily reminder is on for 8:00 PM and the app is updated at 3:00 PM
- **THEN** exactly one daily run is planned, for 8:00 PM today, and one "How was today?" arrives at about 8:00 PM

#### Scenario: An update after today's reminder
- **WHEN** today's reminder has already arrived at 8:00 PM and the app is updated at 9:00 PM
- **THEN** no reminder arrives at the update, and exactly one is planned for 8:00 PM tomorrow

#### Scenario: The update wiped WorkManager's database (residual, Android 8.0)
- **WHEN** an update from a version before 1.2.1 empties `no_backup/` (BRD EC-STO-10) at 3:00 PM, with the daily reminder on for 8:00 PM
- **THEN** without opening the app, a daily run for 8:00 PM today is planned again soon after the update, and it arrives

#### Scenario: Wiped after the reminder's time
- **WHEN** the same happens at 9:00 PM
- **THEN** no late reminder arrives for today, and one is planned for 8:00 PM tomorrow

#### Scenario: A visit reminder that is due
- **WHEN** after the update a next visit is set, its reminder time has passed, its day has not ended, and it was not reminded of
- **THEN** one visit reminder arrives as soon as it can, as for a phone that was off (BRD EC-REM-14)

#### Scenario: A visit already reminded of
- **WHEN** after the update the next visit was already reminded of
- **THEN** no visit reminder arrives again

#### Scenario: One plan per process start
- **WHEN** `MY_PACKAGE_REPLACED` starts the process
- **THEN** the reminders are planned once, by the start's re-plan, and the receiver does not plan a second time
- **AND** a clock or time zone change still makes the receiver plan again, as before (BRD EC-REM-05, EC-REM-14)

#### Scenario: Updated twice in a row
- **WHEN** the app is updated twice within a minute
- **THEN** one daily run is planned, no reminder is posted twice, and no visit reminder is posted twice

#### Scenario: The app is in the stopped state
- **WHEN** the app was force-stopped, or never opened since install, and is then updated
- **THEN** Android may not deliver the broadcast; nothing is planned until the next process start, as before 1.2.1

#### Scenario: No new permission or exposure
- **WHEN** the release manifest is merged
- **THEN** the receiver stays `exported="false"`, and `:app:checkReleasePermissions` passes with the same permissions as 1.2.0

#### Scenario: Open "Did it help?" questions after a wipe
- **WHEN** an update empties `no_backup/` while a "Did it help?" question is pending
- **THEN** that question is not asked again in 1.2.1; this is the known gap kept in #189 (BRD EC-REM-23)
