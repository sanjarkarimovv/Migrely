# Proposal

## Why

#197 (S3, pre-existing, found by QA). On Android 8.0 a person who cleared Migrely's storage and reopened it on a build before
1.2.1, and then got an update, loses WorkManager's database: installd empties `no_backup/` before the new code runs (BRD
EC-STO-10, the residual D170 (2) accepts). The planned reminder runs are gone. D170 (3b) left a `MY_PACKAGE_REPLACED` re-plan out
of 1.2.1 after the product-manager's objection, and the issue asks again.

Reading the code on develop 74095b8d changes the picture:

- **No data is lost.** The diary and every open "Did it help?" question live in Room. `GetMedicationPromptUseCase` asks the
  question on home for 2 to 24 hours after the intake (EC-MED-05). Only WorkManager's rows go, that is, when a notification
  would post.
- **Reminders recover without the app being opened.** JobScheduler keeps the old jobs. The first one that comes due starts
  the app's process, which is where QA saw WorkManager log "Didn't find WorkSpec". `MigrelyApplication.onCreate` runs
  `RescheduleRemindersUseCase` at every process start, which plans the daily reminder for its next time and the visit reminder
  (a passed visit time posts at once on the visit day, `VisitReminderPlan`). So at most one daily reminder is missed.
- **A fix helps almost nobody and costs everyone.** After an update to 1.2.1, the first process start corrects installd's mark
  (D170 (3a)) and later updates are safe. A receiver in a later build helps only people who skip 1.2.1. It also starts the
  process at every update for every user, and `onCreate` re-plans with REPLACE, which can push a passed, Doze-held daily
  reminder to tomorrow. That is the old objection, and it still holds.

Decision BRD D175: no receiver and no re-planning of "Did it help?" from Room. This change pins the behaviour the decision
rests on, so a later refactor cannot break it quietly. QA confirms it once on API 26.

## What Changes

- **Spec `reminders`:** ADDED requirement "Reminders recover when their planned runs are lost". The daily and visit reminders are
  planned again at every process start, whatever starts it, including a stale job whose WorkManager record is gone. An open
  "Did it help?" question stays on home when its notification is lost. No receiver and no permission are added for this.
- **Code:** one comment in `MigrelyApplication.onCreate` that names D175 and EC-REM-22, so the re-plan is not moved into an
  activity or behind a condition. No behaviour, manifest or Gradle change.
- **BRD:** D175, EC-REM-22 and EC-REM-23 rewritten (they now point to #197 and D175, and EC-REM-23 no longer says the questions
  are lost), EC-STO-10's reminder sentence corrected, and §13 row 44.

## Out of scope

- A `MY_PACKAGE_REPLACED` receiver, with or without KEEP (D175 (4)).
- Re-planning "Did it help?" notifications from Room, and any "notification sent" column or Room migration (D175 (5)).
- Changing `RescheduleRemindersUseCase`, `DailyReminderTime` or `ExistingWorkPolicy.REPLACE`. The displacement of a Doze-held
  daily run by a process start is a separate, unproven suspicion (design R2). It gets its own issue only if QA reproduces it.
- Any message telling the user that reminders were lost, and any UI or copy change. There is no text, so no ux-designer or
  medical-reviewer pass.
- Getting back what installd deleted (EC-STO-10).
- Android 8.1–15 (no emulator image), real phones and vendor installd.

## Capabilities

- Modified: `reminders` (ADDED requirement).

## Impact

- `app/src/main/java/com/migrely/app/MigrelyApplication.kt`: a comment only.
- No manifest, permission, dependency or schema change. The release permission list stays the same 6.
- QA: a one-time check of the premise on emulator-5582 (API 26), with an API 36 control. This is not a release Go/No-Go row:
  a comment-only diff reaches no runtime path (owner rule 2026-11-09).
- Issue #197 closes as "not planned" after QA confirms it. Write `Refs #197`, never `Fixes #197`.

BRD ids: D175, D170, EC-REM-22, EC-REM-23, EC-STO-10, EC-REM-04, EC-REM-05, EC-REM-14, EC-MED-05, EC-MED-11.
