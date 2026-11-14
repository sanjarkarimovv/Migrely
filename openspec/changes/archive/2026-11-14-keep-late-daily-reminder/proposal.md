# Proposal

## Why

Two S3 issues, both pre-existing, both about what happens to a planned daily reminder when the app's process starts.

**#213 (reproduced 3/3 on emulator-5570, develop 74095b8d).** The daily reminder is set for 8:00 PM. The phone is in Doze, so
the run is late. At 8:01 PM the person opens Migrely. `MigrelyApplication.onCreate` runs `RescheduleRemindersUseCase`, which
plans `DailyReminderTime.next(now)`, tomorrow, with `ExistingWorkPolicy.REPLACE`. Today's pending run is cancelled and today's
"How was today?" never posts. Any process start does the same: another Migrely job, a reboot, a clock or zone change
(`TimeChangeReceiver`), an update. Controls: an alive process posts 3/3, and a late run with no start in between posts 1/1.
The cold-start race the product-manager suspected (design R2 of the #197 plan) did not reproduce (0/11), but only by 1–7 ms.

**#197 (Android 8.0 residual, EC-STO-10).** An update can empty WorkManager's database. D175 decided no receiver and no code
fix, and its reasoning rested on "every start re-plans with REPLACE". #213 shows that REPLACE at start-up is itself the bug,
so both are planned together: the start-up re-plan changes once, in one change, and D175 is restated on the new premise.

The person: the reminder is how a tired person keeps the diary without gaps. The fix must not swap a silent loss for a
notification that pops up while they are already in the app, perhaps logging an attack in a dark room.

## What Changes

- **Start-up keeps a planned run.** A process start keeps a daily run that is already planned (KEEP) and plans one only when
  none is. REPLACE stays only for real changes: the time setting and on/off (`SetDailyReminderUseCase`), a clock or zone
  change (`TimeChangeReceiver`), and a daily run planning its own next run (`RunDailyReminderUseCase`).
- **A day that has not had its reminder is not skipped.** When a start finds nothing planned, and when the clock or zone
  changes, the target is today's time if still ahead; otherwise at once for today if today's reminder has not been handled;
  otherwise tomorrow. With #197's lost database, the stale job's own start now brings today's reminder back.
- **No daily notification while Migrely is on screen.** A daily run that comes while Migrely is on screen (screen on,
  including while it is opening) does not post and does not mark the date; it plans that date's run again 30 minutes later
  (REPLACE). That run follows the normal rules and postpones again if Migrely is still on screen. Locked, dark screen or
  background: it posts as before. At most one daily reminder per date (ux-designer's rule, task 1.1).
- **BRD:** D176 (new), D175 restated, EC-REM-22 rewritten, EC-REM-24 to EC-REM-28 added, EC-STO-10 points to D176, §13 row 44
  renamed and rewritten.
- **OpenSpec:** the #197 change `pin-reminder-replan-every-start` is renamed to `keep-late-daily-reminder` and now carries both
  issues. Its requirement is reworded from "planned again at every start" to "kept, or planned again when gone".

## Out of scope

- A `MY_PACKAGE_REPLACED` receiver, and re-planning "Did it help?" from Room (D175 (4), (5)).
- The visit reminder (D168) and "Did it help?": their planning does not change. The visit plan already runs a passed time at
  once on the visit day, so REPLACE loses no day. Their in-app notifications are pre-existing and not touched here.
- A new cut-off for late daily runs. The existing one stays: a run never asks about a day that has ended (D176 (4)).
- `SetDailyReminderUseCase` keeps "the first time strictly after now" (D66). The person is in the app when they change it.
- Any notification text, screen, setting, permission, manifest entry, dependency or Room schema change. `dailyLastShownOn`
  keeps its column and its meaning ("shown").
- Posting a run that Doze held for hours without sound (ux-designer's follow-up (c)); `team-lead` tracks it separately.
- Dismissing an already posted daily notification when the day is answered in the app (pre-existing).
- Android 8.1–15 (no emulator image), real phones, vendor Doze variants.

## Capabilities

- Modified: `reminders` (ADDED requirements: four for #213, one for #197; MODIFIED "The daily reminder", which now names
  the on-screen exception so the merged spec does not promise a post at 8:00 PM while Migrely is on screen).

## Impact

- Domain `features/reminders/domain`: `DailyReminderTime` (a target that can be "now, for today"), `RescheduleRemindersUseCase`
  (a request saying whether this is an app start or a clock change), `RunDailyReminderUseCase` (on-screen postpone), two new
  repository interfaces (plan only if none is planned; is Migrely on screen).
- Data `features/reminders/data`: `RemindersRepository` implements them.
- Data source: `ReminderScheduleDataSource(Impl)` gains a KEEP enqueue for "daily"; a new on-screen data source in `app`.
- Wiring: `MigrelyApplication.onCreate` (app start), `TimeChangeReceiver` (clock change), `RemindersModule`.
- Tests: domain, repository and data source unit tests that fail with the production change reverted.
- QA: device rows on emulator-5580 (API 36) and emulator-5582 (API 26), reusing `migrely-work/walks/r2/bin/`, with 74095b8d
  as the negative control. A release Go/No-Go for the next release covers only what this diff reaches (owner rule
  2026-11-09).
- Issues: write `Refs #213` and `Refs #197`, never `Fixes`.

BRD ids: D176, D175, D66, D168, D170, EC-REM-04, EC-REM-05, EC-REM-14, EC-REM-22, EC-REM-23, EC-REM-24, EC-REM-25, EC-REM-26,
EC-REM-27, EC-REM-28, EC-STO-10, EC-MED-05.
