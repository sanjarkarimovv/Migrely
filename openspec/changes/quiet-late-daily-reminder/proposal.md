# Proposal

## Why

Tracker T61, ux-designer's follow-up (c) from #213, left out of `keep-late-daily-reminder` (D176) on purpose. Pre-existing
behaviour, no issue severity. BRD D183.

The person: the daily reminder is set for 8:00 PM. At 8:00 PM they are lying in a dark room with a migraine, the phone face
down beside them, not touched, so Android puts it into Doze. Doze holds the run, and AOSP's deep-idle windows grow from about
an hour to about six. At 11:10 PM a maintenance window opens and "How was today?" posts with the "daily" channel's sound.
It wakes or startles them, hours after the moment they chose, for a question that can wait.

The same happens on the catch-up paths D176 added: a reboot at 11 PM, or a clock or zone change after the time with today not
shown, runs "now, for today" and posts with sound.

Code read on `develop` e4b1fbcd, not run:
- `RunDailyReminderUseCase` never looks at how late the run is; it shows the reminder whenever the date is today and
  unanswered.
- `NotificationDataSourceImpl.showDailyReminder` always posts on the "daily" channel (`IMPORTANCE_DEFAULT`), so the
  channel's sound and vibration play.

A quiet late reminder is not a missed one. It still posts, still shows in the shade with its icon, text and actions, and
still marks the date shown. Only the sound, vibration and the pop-up over the screen go.

## What Changes

- **A late daily reminder posts quietly.** When a daily reminder posts more than 60 minutes after that date's set time,
  measured in the phone's zone at that moment against the current setting, it posts with `setSilent(true)`: no sound, no
  vibration, no heads-up. Text, actions, lock-screen version, status bar icon and the "shown" mark do not change.
- **60 minutes or less alerts as before.** Normal WorkManager slack (about 15 minutes, EC-REM-03), JobScheduler batching
  (7 minutes seen in #213) and the first on-screen postpone (about 30 minutes, D176) keep their sound.
- **Domain decides, the data source obeys.** `RunDailyReminderUseCase` works out lateness with the same `DailyReminderTime`
  arithmetic the plan uses and passes a quiet flag down `ShowDailyReminderRepository` to `NotificationDataSource`.
- **BRD:** D183, EC-REM-29, EC-REM-30, §13 row 51.

## Out of scope

- **"Did it help?" and the visit reminder.** Migrely has no medication-taking reminder (REM-F1 to F4). "Did it help?" asks
  about a dose already taken, and its dark-room problem is the same on time, not a lateness problem; D69 already holds it
  during an attack. The visit reminder is a daytime 10:00 nudge two days ahead, once per visit, where being noticed matters
  more. Both keep their sound. Whether "Did it help?" should ever sound is a separate question for ux-designer.
- A quiet-hours setting, a bedtime, or any new setting, toggle or text.
- A cut-off that stops late reminders posting (D176 (4) stands: the day stays answerable until midnight).
- A new or changed notification channel, channel importance, or channel name.
- Exact alarms, a Do Not Disturb bypass, a new permission, a manifest entry, a dependency, a stored value or a Room schema
  change.
- Android 14's flash notifications and paired watches: Android decides how they treat a silent notification.
- Real phones and vendor Doze variants (emulators only, as for D176).

## Capabilities

- Modified: `reminders` (ADDED: "A late daily reminder posts quietly", "Quiet only removes an alert, only for the daily
  reminder").

## Impact

- Domain `features/reminders/domain`: `DailyReminderTime` (how late a run is for a date), `RunDailyReminderUseCase` (the quiet
  decision), `ShowDailyReminderRepository` (a quiet flag).
- Data `features/reminders/data`: `RemindersRepository` passes the flag.
- Data source: `NotificationDataSource.showDailyReminder(date, quiet)` and `NotificationDataSourceImpl` (`setSilent`).
- Tests: domain unit tests red-first; a Robolectric data-source test that fails with `setSilent` removed; existing
  `NotificationLockScreenTest` and `NotificationDataSourceImplTest` stay green.
- QA: device rows on API 36 and API 26 reading the events log's `notification_alert` line, with develop e4b1fbcd as the
  negative control. Go/No-Go covers only what this diff reaches (owner rule 2026-11-09).
- No user-facing string or health sentence changes.
- Issues: write `Refs #213`, never `Fixes`.

BRD ids: D183, D66, D69, D168, D169, D176, REM-F1, EC-REM-03, EC-REM-04, EC-REM-05, EC-REM-21, EC-REM-25, EC-REM-27,
EC-REM-29, EC-REM-30.
