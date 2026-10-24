# Tasks

## 1. Datasources

- [x] 1.1 `reminder_settings` (DB v6) with `ReminderSettingsDataSource`; migration tests 5→6 and 1→6, DAO and
  datasource tests.
- [x] 1.1b Add `daily_last_shown_date` (ISO date, null) to `reminder_settings` and `setDailyShown(date)`, so an early
  run followed by a reschedule never reminds twice in one day. v6 is not pushed yet, so `6.json` is regenerated.
- [x] 1.2 Interfaces `ReminderScheduleDataSource` and `NotificationDataSource` in `:datasource:source`. Their
  implementations live in `:app` (task 4.1): they need `MainActivity`, the receivers and the notification copy. This
  is recorded in ARCHITECTURE §3 and §13.

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

- [x] 3.1 `LogIntakeUseCase` schedules `effect-<id>` at takenAt + 2 h for an acute intake. `AnswerEffectUseCase`,
  `SkipEffectQuestionUseCase` and `UndoIntakeUseCase` cancel it, and clear a shown notification. Each step goes through
  a single-function repository over the schedule and notification datasources.
- [x] 3.2 `RunEffectQuestionUseCase(intakeId)` shows "Did <name> help?" only while the question is open, the setting is
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

- [x] 5.1 Gate (948 unit tests), and instrumented datasource tests (66/66).
- [x] 5.2 Emulator walk: permission, daily reminder (advance the clock), actions, skip rules, travel, effect question.
- [x] 5.3 Independent review; fix and record.

## Verification record

**Emulator walk** on emulator-5570, by a walk agent. It ran first on 3773d83, then scenarios 3–8 again on 3f15aad. At
the end the database dump was byte-identical to the baseline, the permission and the settings were back as before, and
no jobs or notifications were left.

| Scenario | Result |
|---|---|
| The bell | in normal mode only |
| Permission | the dialog shows only when a switch is turned on. After "Don't allow", the switch stays off with the notice and no second dialog; "Allow" turns it on at 8:00 PM |
| The daily reminder | arrived on time unforced, with both actions, and the next run was planned 24 h on. "Pain-free day" marked the day, and History shows it |
| Skips | no reminder on a day with an attack. "Pain-free day" with an attack added after posting reads "A migraine is logged today." and marks nothing |
| Travel and the clock | Berlin, a clock 1 h ahead, and back to Tashkent each moved the run to the new local time |
| "Did it help?" | planned 2 h after the intake. Undo, an answer and "Don't ask about this one" each cancel it. It arrives and opens home |
| Switch off; "Open settings" | nothing is sent; Migrely's notification settings open |
| Accessibility | each switch row is one node with Role switch. The time row reads "Reminder time, 11:45 PM. Change it." and is clickable at 3f15aad. Nothing is clipped at 200% font, in portrait or landscape |

**Found in the walk, and fixed in e74055e:**
- **B1:** two modules named a string `picker_title`, so the medication time picker read "Reminder time". Reminders' strings now carry a `reminders_` prefix.
- **B2:** "Did it help?" during an attack opened a home with no card. Nothing is sent while an attack is in progress (BRD D69); the card asks once it ends.
- **B3:** "Log a migraine" left the reminder in the shade. The app now clears it as it opens.
- The time row now shows while the reminder is off too.

**Accepted:** home does not refresh when "Pain-free day" is tapped while home is on screen (B4). It shows the mark on its next load.

## Review (5.3)

An independent `superpowers:code-reviewer` pass found nothing critical. It confirmed:
- next and DST, the shown-today guard, the broadcasts reaching non-exported receivers, `goAsync`, and work surviving a
  reboot;
- the PendingIntent flags and request codes, the Android 13 permission semantics, the permission flow across rotation
  and process death, the layer rules, and the migration.

| Finding | Outcome |
|---|---|
| **I1** a run after midnight asked about the wrong day and lost the next one, and a late tap marked the wrong day | fixed: the work and the action carry their date. A run past its day asks nothing and plans the next one |
| **I2** the daily chain could end: a failed run, a backup restore, or planning cancelled while notifications were off | fixed: a failed run retries, the reminder stays planned while it is on, and the app plans it again at start |
| **I3** "Did it help?" asked about the oldest open question instead of its own intake, re-asked after a preventive intake, and fired at once for late logs | fixed: `pendingEffectQuestionOf(intakeId)` with a DAO test; late-logged intakes are left to the card |
| **I4** the time row lost its click action for TalkBack and Switch Access | fixed: the semantics are merged into the clickable |
| **I5** the docs disagreed with the code | ARCHITECTURE §3 and §13, design D1 and D2, and the KDoc updated |
| Minor | fixed: `setOnlyAlertOnce`, the settings fallback, the 12/24-hour format, the summary copy, a comment on REPLACE inside the worker, and `LocalTime` in the domain instead of a minute count. Accepted: channels are created on the first post, and an answer from attack details leaves an already shown notification (its planned run checks the question) |

**Re-walk after the fixes** (e74055e): every check passed.
- The medication pickers read "When did you take it?", and the Reminders picker reads "Reminder time".
- No "Did it help?" during an attack. After "Migraine ended", home's card asks; without an attack, the notification
  arrives.
- "Log a migraine" leaves no notification behind.
- While the reminder is off, the time row shows 8:00 PM. Choosing 9:30 PM saves 1290, keeps it off, plans nothing and
  asks for nothing.

The database was restored byte-identical.
