# Design

## Context

- The app has no notifications, no background work and no settings yet. The manifest has no INTERNET permission, and
  none is added.
- Day marks (change 4) and the "Did it help?" question (change 3a, `MedicationDataSource.pendingEffectQuestion`)
  already exist. History's `SetDayMarkUseCase` applies EC-DAY-01.

## Decisions

### D1. Modules (skill rules 10, 11)

- **New** `:features:reminders:{domain,presentation,ui,data}`, shaped like `medication`.
- **Datasources:**
  - `datasource.reminder`: `ReminderSettingsDataSource` over Room table `reminder_settings`, a single row with
    `daily_enabled`, `daily_minute_of_day` (default 1200), `effect_enabled` (default true),
    `permission_asked_epoch_millis`. DB v6 is an additive `AutoMigration(5, 6)`.
  - `datasource.schedule`: `ReminderScheduleDataSource` (`scheduleDaily(at)`, `cancelDaily()`,
    `scheduleEffectQuestion(intakeId, at)`, `cancelEffectQuestion(intakeId)`), implemented with WorkManager unique
    work: `daily`, and `effect-<intakeId>`.
  - `datasource.notification`: `NotificationDataSource` (`areEnabled()`, `showDaily()`,
    `showAttackLoggedToday()`, `showEffectQuestion(intakeId, medicationName)`, `cancel...`), implemented with
    NotificationManagerCompat and one channel per kind.
- **Android components live in `:app`:**
  - the `ReminderWorker`;
  - a `ReminderActionReceiver` for "Pain-free day";
  - a `TimeChangeReceiver` for `TIMEZONE_CHANGED`, `TIME_SET` and `BOOT_COMPLETED`, which WorkManager's own reboot
    handling covers too.
  Each calls a use case through Hilt's `EntryPoint`.

### D2. Use cases

- `GetReminderSettingsUseCase` and `SetDailyReminderUseCase(enabled, minuteOfDay)`, which saves and then schedules or
  cancels; `SetEffectReminderUseCase(enabled)`.
- `RescheduleRemindersUseCase` runs after boot or a time change, and after each daily run. It works out the next local
  occurrence from the device zone and stores nothing about time zones.
- `RunDailyReminderUseCase` decides at run time whether to show the reminder. It shows nothing when today has a
  pain-free mark or an attack touching it (the `AttackDays` rule).
- `MarkPainFreeFromReminderUseCase` reuses the History rule. `:app` calls History's `SetDayMarkUseCase`, and an
  `AttackLogged` result shows "A migraine is logged today."
- **The effect question:** `LogIntakeUseCase` (medication) schedules `effect-<id>` at takenAt + 2 h through a new
  single-function repository. `AnswerEffectUseCase`, `SkipEffectQuestionUseCase` and `UndoIntakeUseCase` cancel it.
  The worker checks the question is still open and that the setting is on before it shows anything.

### D3. Permission and UI

- The Reminders screen (Compose):
  - two switch rows ("Daily reminder" with its time row and picker, "Ask if a medication helped");
  - a notice when notifications are off, with "Open settings" (`Settings.ACTION_APP_NOTIFICATION_SETTINGS`).
- The runtime permission request is UI work (`rememberLauncherForActivityResult`). The ViewModel only receives
  `onDailyReminderAction(on)` and `onPermissionResultAction(granted)`. The 30-day rule for asking again is in the
  domain.
- Home's header gets a bell `IconButton` ("Reminders"), in normal mode only, which emits `OnOpenReminders`.

## Risks

| Risk | Mitigation |
|---|---|
| WorkManager drifts by up to 15 minutes | accepted by EC-REM-03 |
| The OEM kills background work | WorkManager's own constraints and rescheduling; the in-app card still asks about the medication |
| A notification action without the app's process | the receiver uses `goAsync` and an `EntryPoint`, and runs the use case off the main thread |
