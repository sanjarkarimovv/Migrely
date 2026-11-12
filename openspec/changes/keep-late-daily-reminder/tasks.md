# Tasks

Base: `bugfix/197-replan-after-update` from `origin/develop` 74095b8d (develop is now 30cb6ac3: site and store art only, no
app code). Write `Refs #197` and `Refs #213` in every commit and in the PR into `develop`. Never `Fixes`. BRD D176, D175.

Ownership:
- `product-manager`: 0 (done) and 5;
- `ux-designer`: 1;
- `android-engineer`: 2;
- `code-reviewer`: 3;
- `qa-engineer`: 4 (emulator-5580, API 36; emulator-5582, API 26; one at a time).

No medical-reviewer: no health sentence changes. No new copy.

## 0. Plan (`product-manager`)

- [x] 0.1 BRD D176, D175 restated, EC-REM-22, EC-REM-24…28, EC-STO-10, §13 row 44; change renamed to
  `keep-late-daily-reminder`. Verify: `openspec validate keep-late-daily-reminder --strict` exits 0.

## 1. Flow check (`ux-designer`)

- [x] 1.1 Confirm the on-screen rule (design D3) and that no copy, screen or setting changes. Done: ux-designer changed the
  rule (a skip does not mark the date; it postpones 30 minutes), team-lead accepted it, and the plan now uses its text word
  for word. Follow-up (c), a silent post for a run Doze held for hours, is out of scope (team-lead tracks it).

## 2. Code (`android-engineer`)

Write each test first. For every test marked **red**, show it failing with its production change reverted (minimal stubs to
compile are fine), then passing. Record both runs in the handoff.

- [ ] 2.1 Domain, `DailyReminderTime.due(now, time, handledOn)` per design D2. **Red** tests in `DailyReminderTimeTest`:
  before the time → today at the time; after the time, not handled → now, date today; after the time, handled → tomorrow;
  exactly at the time → now for today if not handled; DST gap; `handledOn` in the future (clock moved back) → today's
  rule. Verify: `./gradlew :features:reminders:domain:test --tests '*DailyReminderTimeTest'`.
- [ ] 2.2 Domain, two repository interfaces: plan the daily reminder only if none is planned; is Migrely on screen.
  `RescheduleRemindersUseCase` takes a request (app start or clock change). App start: `due()` through the plan-if-none
  repository. Clock change: `due()` through `ScheduleDailyReminderRepository` (REPLACE). Off: cancel, both. Visit planning
  unchanged. **Red** tests in `RescheduleRemindersUseCaseTest`: app start after the time with today not handled plans
  "now, today" through plan-if-none and never calls the REPLACE repository (fails on 74095b8d, which plans tomorrow with
  REPLACE); clock change after the time, not handled, plans now through REPLACE; handled → tomorrow; off → cancel; the visit
  is planned exactly as before for both requests. Verify: `./gradlew :features:reminders:domain:test --tests
  '*RescheduleRemindersUseCaseTest'`.
- [ ] 2.3 Domain, `RunDailyReminderUseCase` (design D3): after the existing checks, when on screen plan the same date again
  at now + 30 minutes through `ScheduleDailyReminderRepository` (REPLACE), never call `setDailyShown`, and return `Skipped`.
  **Red** tests: on screen → no show, no `setDailyShown`, the last daily plan is (now + 30 min, today) with REPLACE; a
  postponed run (request = today), off screen, not answered → shows once and marks today; a postponed run, answered → no
  show; a postponed run, still on screen → postponed again; a postponed run after midnight → no show, next day planned;
  already shown → no show. Existing tests stay green. Verify: `./gradlew :features:reminders:domain:test --tests
  '*RunDailyReminderUseCaseTest'`.
- [ ] 2.4 Domain KDoc: `RescheduleRemindersUseCase`, `DailyReminderTime` and `RunDailyReminderUseCase` name D176.
  `dailyLastShownOn` keeps its meaning ("shown"); its KDoc does not change. Verify: `git grep -n 'D176'
  features/reminders/domain/src/main` lists the three files, and `git diff origin/develop --
  features/reminders/domain/src/main/java/com/migrely/app/reminders/domain/model/ReminderSettingsDomainModel.kt` is empty.
- [ ] 2.5 Data, `RemindersRepository` implements both interfaces. Tests in `RemindersRepositoryTest` that each delegates to
  its data source. Verify: `./gradlew :features:reminders:data:test --tests '*RemindersRepositoryTest'`.
- [ ] 2.6 Data source: `ReminderScheduleDataSource.scheduleDailyIfNone(at, date)` with `ExistingWorkPolicy.KEEP` on the same
  unique name "daily" and the same input data; `scheduleDaily` stays REPLACE. A data source for "Migrely is on screen" in
  `app`, using `ActivityManager.getMyMemoryState` and `IMPORTANCE_FOREGROUND` (design D3), wired through `datasource/source`.
  **Red** test in `ReminderScheduleDataSourceImplTest`: `scheduleDailyIfNone` enqueues "daily" with KEEP; `scheduleDaily`
  with REPLACE. Verify: `./gradlew :app:testDebugUnitTest --tests '*ReminderScheduleDataSourceImplTest'`.
- [ ] 2.7 Wiring: `MigrelyApplication.onCreate` passes "app start", `TimeChangeReceiver` passes "clock change";
  `RemindersModule` and `ReminderEntryPoint` provide the new pieces. The comment above the start-up call names D175, D176
  and EC-REM-22: the plan stays here, at every process start, and keeps a planned run. Verify: `git grep -n 'D176'
  app/src/main/java/com/migrely/app/MigrelyApplication.kt` prints 1 line.
- [ ] 2.8 Gate: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint
  :app:checkReleasePermissions`. Verify: exit 0, the task count, and the same 6 release permissions as 1.2.1:
  `POST_NOTIFICATIONS`, `ACCESS_NETWORK_STATE`, `BILLING`, `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`,
  `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. `git diff --stat origin/develop -- '**/AndroidManifest.xml' '*.toml' '*.kts'`
  prints nothing.
- [ ] 2.9 Device walk on emulator-5570 (API 36), debug build, with `migrely-work/walks/r2/bin/iter.sh`:
  `late` mode 3 runs → posted=1 each (74095b8d gave 0/3); `kill` mode 3 runs → posted=1; `alive` 1 run → posted=1. Two new
  probes: (a) a `late` run from a copy of `iter.sh` without the second HOME key, so the app stays on screen when idle is
  unforced → posted=0, `daily_last_shown_date` is not that date (read with `adb shell run-as com.migrely.app`), and
  jobscheduler shows a daily job about 30 minutes ahead; then HOME → posted=1 once when that job runs; (b) R1: log
  `getMyMemoryState().importance` in a debug-only line, at `Application.onCreate` and in the daily run, for: a tap start
  (expect 100), a job start in the background (expect above 100), the lock screen right after the power key with Migrely
  behind it (expect above 100), split screen with Migrely visible (expect 100), the notification shade pulled over
  Migrely (expect 100). Remove the log after. Verify: the RESULT lines and the probe outputs in the handoff. If any of (b)
  differs from the expectation, stop and send it to `product-manager`.
- [ ] 2.10 Build for QA: the debug APK (for the r2 scripts) and the R8 release APK from this branch, plus 74095b8d's debug
  and R8 APKs as the negative control. Record the four SHAs and paths in the handoff.

## 3. Review (`code-reviewer`)

- [ ] 3.1 Check: the only REPLACE callers of the daily plan are `SetDailyReminderUseCase`, the clock-change request and
  `RunDailyReminderUseCase` (its next run and its on-screen postpone) (`git grep -n 'scheduleDailyReminder\|scheduleDaily(' -- '*.kt'`); the start-up path reaches only
  KEEP; the on-screen check cannot block a post when the screen is off, and
  an on-screen skip never calls `setDailyShown`; visit, effect and import paths are unchanged
  (`git diff origin/develop -- features/reminders/domain/src/main/java/com/migrely/app/reminders/domain/usecase/VisitReminderPlan.kt`
  is empty); each **red** test really fails without its production change. Verify: verdict with file:line references.

## 4. Independent check on device (`qa-engineer`)

Copy `migrely-work/walks/r2/bin/` to `migrely-work/qa-213/bin/` and change only the serial and the app uid in the job grep
(read it with `adb shell dumpsys package com.migrely.app | grep userId`). On API 26 `pm revoke POST_NOTIFICATIONS` does not
exist: clear notifications with `adb shell cmd notification` or a force stop before the run, and record the change.
Builds from 2.10. Record every run in `migrely-work/qa-213/rows.jsonl`. Restore the clock (`auto_time 1`), battery,
deviceidle, rotation, font and airplane mode after each session. Any failed run is reported with its log, never rerun away.

- [ ] 4.1 **The bug, fixed (API 36 and API 26).** `late` mode, app sent home before idle is unforced. New build: posted=1, 3
  runs on each API. **Negative control:** 74095b8d, same script: posted=0, 1 run on each API.
- [ ] 4.2 **On screen (API 36).** `late` mode without the second HOME key, so the app stays on home when idle is unforced:
  posted=0, `daily_last_shown_date` is not today, and a "daily" job about 30 minutes ahead. 2 runs. Extend the wait past
  the postponed time (or move the clock forward 31 minutes with `cmd alarm set-time` while the app stays on screen): still
  posted=0 and a new job about 30 minutes ahead. **Controls:** `alive` mode with the app on home at 20:00: posted=0;
  `alive` with the app in the background: posted=1.
- [ ] 4.3 **Left after the skip (API 36).** As 4.2, then press HOME (launcher) after the skip, force idle and unforce it
  when the postponed job is due: posted=1, exactly once for that date, and `daily_last_shown_date` is today. 2 runs.
- [ ] 4.3a **Opening when the run comes (API 36).** `late` mode, then `am start` and `deviceidle unforce` in one shell
  command, the app stays on screen: posted=0 and a job about 30 minutes ahead. 5 runs. A post in any run is a failure of
  design D3 (send to `product-manager`).
- [ ] 4.3b **Screen-state probes (API 36), 1 run each, app on screen before 20:00:** lock screen right after the power key
  with Migrely behind it at 20:00 → posted=1; split screen with Migrely visible at 20:00 → posted=0, job about 30 minutes
  ahead; notification shade pulled over Migrely at 20:00 (hold it with `adb shell cmd statusbar expand-notifications`) →
  posted=0, job about 30 minutes ahead. Any other result goes to `product-manager`.
- [ ] 4.4 **Cold start by the job, R2 (API 36 and API 26).** `kill` mode: posted=1. 5 runs on API 36, 3 on API 26.
- [ ] 4.5 **Clock change after the time (API 36).** Plan 20:00, kill the process, force idle; at 20:01 set the clock forward
  by 2 minutes with `cmd alarm set-time`; unforce: posted=1. **Negative control:** 74095b8d: posted=0. 1 + 1 runs.
- [ ] 4.6 **Reboot (API 36).** Plan 20:00, force idle, reboot the emulator at 20:01 (Doze ends with the boot): posted=1 once
  after boot, before 20:15. **Negative control:** 74095b8d. 1 + 1 runs.
- [ ] 4.7 **Already answered / off (API 36).** Mark today pain-free before 20:00, `late` mode: posted=0. Reminder off,
  `late` mode: posted=0 and no daily job. 1 run each.
- [ ] 4.8 **No double post (API 36).** Let 20:00 post, then set the clock back to 19:58 and wait to 20:05: still one
  notification for that date, posted count 1. 1 run.
- [ ] 4.9 **#197 residual (API 26), from the #197 plan.** Use the plain-clear method of `migrely-work/qa-193` rows
  `26-plain-ctl-*` and `26-timed-ctl-1`: old build 1.1.1 R8 (or 1.2.0 R8 `r8-9d453bb5`), reminder R about 15 minutes ahead,
  clear storage, reopen, turn the reminder on again, update to the new R8 build before R, do not touch the device. Expect:
  `androidx.work.workdb` gone after the update (else the run is void, not a pass); at R "Didn't find WorkSpec" in the log;
  "How was today?" posts once within 5 minutes after R. **Negative control:** the same with 74095b8d's R8 as "new": nothing
  posts at R, and a job with about 24 h delay is planned. 2 + 1 runs.
- [ ] 4.10 **#197 visit and "Did it help?" (API 26).** As the #197 plan's 4.3 and 4.4: the visit reminder posts once within a
  few minutes of its time with no app open; no "Did it help?" at 2 h, home's card asks within 24 h; control without clear
  posts at 2 h once. 1 + 1 + 1 runs.
- [ ] 4.11 **Normal update (API 26 and API 36).** No clear, R about 15 minutes after the update, process dead at R: `workdb`
  kept, posted once. 2 runs on API 26, 1 on API 36.

Report in the handoff packet: counts per row, the RESULT lines, the probe outputs, and what was not checked (Android 8.1–15,
real phones, Play updates, vendor Doze).

## 5. Close (`product-manager`)

- [ ] 5.1 After 3 and 4: archive. Verify: `openspec archive keep-late-daily-reminder` and `openspec validate --specs
  --strict` exit 0. Tell `team-lead`: #213 is retested by its reproducer (`android-engineer`) on the merged build, then closed;
  #197 closes as "not planned" for the receiver, citing D175, D176 and rows 4.9–4.11.
