# Tasks

## 1. Datasources

- [ ] 1.1 `reminder_settings` (DB v6) with `ReminderSettingsDataSource`; migration test 5→6 and DAO test.
- [ ] 1.2 `ReminderScheduleDataSource` (WorkManager) and `NotificationDataSource` (NotificationManagerCompat), with
  channels.

## 2. Reminders feature

- [ ] 2.1 Domain: settings, scheduling, run-time decision, reschedule, permission rule, with tests.
- [ ] 2.2 Data and presentation: repositories, `RemindersViewModel`, with tests.
- [ ] 2.3 UI: the Reminders screen and the permission flow; home's header button and navigation.

## 3. Medication link

- [ ] 3.1 Schedule and cancel the "Did it help?" notification from the intake use cases; tests.

## 4. Android components in `:app`

- [ ] 4.1 `ReminderWorker`, `ReminderActionReceiver`, `TimeChangeReceiver`, the manifest, and DI.

## 5. Verify

- [ ] 5.1 Gate, and instrumented datasource tests.
- [ ] 5.2 Emulator walk: permission, daily reminder (advance the clock), actions, skip rules, travel, effect question.
- [ ] 5.3 Independent review; fix and record.
