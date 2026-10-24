# Tasks

## 1. Datasources

- [x] 1.1 `reminder_settings` (DB v6) with `ReminderSettingsDataSource`; migration tests 5→6 and 1→6, DAO and
  datasource tests.
- [x] 1.1b Add `daily_last_shown_date` (ISO date, null) to `reminder_settings` and `setDailyShown(date)`, so an early
  run followed by a reschedule never reminds twice in one day. v6 is not pushed yet, so `6.json` is regenerated.
- [x] 1.2 Interfaces `ReminderScheduleDataSource` and `NotificationDataSource` in `:datasource:source`. Their
  implementations live in `:app` (task 4.1): they need `MainActivity`, the receivers and the notification copy. This
  is recorded in ARCHITECTURE §13.

## 2. Reminders feature (`:features:reminders:{domain,presentation,ui,data}`)

- [x] 2.1 Domain.
  - Models: `ReminderSettingsDomainModel` (daily on, daily time, effect on, notifications allowed, may ask), and a
    write result.
  - Single-function repositories: settings, notifications allowed, set daily, set effect, set permission asked, set
    daily shown, schedule daily, cancel daily, show daily, today's entries (attacks touching today and today's mark),
    current time.
  - Use cases:
    - `GetRemindersUseCase`; may ask is true when permission was never asked, or asked 30 days ago or more;
    - `SetDailyReminderUseCase`, which saves, then schedules the next occurrence or cancels;
    - `SetEffectReminderUseCase`;
    - `RecordPermissionAskedUseCase`;
    - `RescheduleRemindersUseCase`: the next occurrence strictly after now, in the device zone;
    - `RunDailyReminderUseCase`: shows unless off, notifications are off, it was already shown today, or today has a
      pain-free mark or an attack (the `AttackDays` rule); it then schedules tomorrow's occurrence;
    - `ShowAttackLoggedTodayUseCase` and `DismissDailyReminderUseCase` for `:app`'s action receiver.
  - Tests.
- [x] 2.2 Data: `RemindersRepository` over `ReminderSettingsDataSource`, `ReminderScheduleDataSource`,
  `NotificationDataSource`, `AttackDataSource`, `DayMarkDataSource` and `ClockDataSource`, with tests.
- [x] 2.3 Presentation: `RemindersViewModel` with `onEnter`, `onDailyReminderAction(on)`, `onDailyTimeAction(time)`,
  `onEffectReminderAction(on)`, `onPermissionResultAction(granted)` and `onOpenSettingsAction`; view state and
  notifications; tests.
- [x] 2.4 UI: the Reminders screen (switch rows, time row with `MigrelyDateTimePicker`'s time picker, a notice when
  notifications are off with "Open settings", the runtime permission launcher).
- [x] 2.5 Home: the header bell "Reminders" (normal mode only) emits `OnOpenReminders`; `:app` navigates.

## 3. Medication link (the "Did it help?" notification belongs to the medication feature)

- [ ] 3.1 `LogIntakeUseCase` schedules `effect-<id>` at takenAt + 2 h for an acute intake. `AnswerEffectUseCase`,
  `SkipEffectQuestionUseCase` and `UndoIntakeUseCase` cancel it, and clear a shown notification. Each step goes through
  a single-function repository over the schedule and notification datasources.
- [ ] 3.2 `RunEffectQuestionUseCase(intakeId)` shows "Did <name> help?" only while the question is open, the setting is
  on (it reads `ReminderSettingsDataSource` through medication's data), and notifications are allowed. Tests.

## 4. Android components in `:app` (`com.migrely.app.reminder`)

- [x] 4.1 Implementations (the effect-question run is wired in 3.2):
  - `ReminderScheduleDataSourceImpl`: WorkManager unique one-time work, `daily` and `effect-<id>`, with input data
    giving the kind and the intake id;
  - `NotificationDataSourceImpl`: channels for the daily reminder and the medication question; "Pain-free day" goes
    to the action receiver, a tap opens `MainActivity`;
  - `ReminderWorker`, which runs the use cases through a Hilt `EntryPoint`;
  - `ReminderActionReceiver`: "Pain-free day" calls History's `SetDayMarkUseCase`, then dismisses the reminder or
    shows "A migraine is logged today.";
  - `TimeChangeReceiver`: `TIMEZONE_CHANGED` and `TIME_SET` call `RescheduleRemindersUseCase`; there is no
    `BOOT_COMPLETED`, because WorkManager keeps planned work across a reboot (EC-REM-04);
  - the manifest's `POST_NOTIFICATIONS`, and DI;
  - the two datasource implementations sit in `:app` under `com.migrely.app.datasource.<resource>.datasource`, as the
    architecture tests require.

## 5. Verify

- [ ] 5.1 Gate, and instrumented datasource tests.
- [ ] 5.2 Emulator walk: permission, daily reminder (advance the clock), actions, skip rules, travel, effect question.
- [ ] 5.3 Independent review; fix and record.
