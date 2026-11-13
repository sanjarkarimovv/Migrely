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

- [x] 2.1 Domain, `DailyReminderTime.due(now, time, handledOn)` per design D2. **Red** tests in `DailyReminderTimeTest`:
  before the time → today at the time; after the time, not handled → now, date today; after the time, handled → tomorrow;
  exactly at the time → now for today if not handled; DST gap; `handledOn` in the future (clock moved back) → today's
  rule. Verify: `./gradlew :features:reminders:domain:test --tests '*DailyReminderTimeTest'`.
  Done (android-engineer): six `due()` tests. Red with `due()` returning `next()` (74095b8d's start target): 4 of 6
  fail (after the time not handled, exactly at the time, DST, clock moved back); the 2 that pass are unchanged
  behaviour (before the time, handled → tomorrow). Green: `DailyReminderTimeTest` 12/12.
- [x] 2.2 Domain, two repository interfaces: plan the daily reminder only if none is planned; is Migrely on screen.
  `RescheduleRemindersUseCase` takes a request (app start or clock change). App start: `due()` through the plan-if-none
  repository. Clock change: `due()` through `ScheduleDailyReminderRepository` (REPLACE). Off: cancel, both. Visit planning
  unchanged. **Red** tests in `RescheduleRemindersUseCaseTest`: app start after the time with today not handled plans
  "now, today" through plan-if-none and never calls the REPLACE repository (fails on 74095b8d, which plans tomorrow with
  REPLACE); clock change after the time, not handled, plans now through REPLACE; handled → tomorrow; off → cancel; the visit
  is planned exactly as before for both requests. Verify: `./gradlew :features:reminders:domain:test --tests
  '*RescheduleRemindersUseCaseTest'`.
  Done: `PlanDailyReminderIfNoneRepository`, `GetAppOnScreenRepository`, `RescheduleRemindersRequestDomainModel {
  AppStart, ClockChange }`. Red with the use case unchanged but taking the request: 4 of 11 fail (app start now /
  today's time through plan-if-none, clock change now, shown → tomorrow); off and visit tests pass on both. Green:
  11/11.
- [x] 2.3 Domain, `RunDailyReminderUseCase` (design D3): after the existing checks, when on screen plan the same date again
  at now + 30 minutes through `ScheduleDailyReminderRepository` (REPLACE), never call `setDailyShown`, and return `Skipped`.
  **Red** tests: on screen → no show, no `setDailyShown`, the last daily plan is (now + 30 min, today) with REPLACE; a
  postponed run (request = today), off screen, not answered → shows once and marks today; a postponed run, answered → no
  show; a postponed run, still on screen → postponed again; a postponed run after midnight → no show, next day planned;
  already shown → no show. Existing tests stay green. Verify: `./gradlew :features:reminders:domain:test --tests
  '*RunDailyReminderUseCaseTest'`.
  Done: postpone `now + 30 min` for the same date through `ScheduleDailyReminderRepository`, no `setDailyShown`,
  `Skipped`. Red with the repository unused: 2 of 6 new fail (on screen; still on screen → again); the other 4 encode
  rules 74095b8d already had. Green: `RunDailyReminderUseCaseTest` 14/14 (8 existing unchanged).
- [x] 2.4 Domain KDoc: `RescheduleRemindersUseCase`, `DailyReminderTime` and `RunDailyReminderUseCase` name D176.
  `dailyLastShownOn` keeps its meaning ("shown"); its KDoc does not change. Verify: `git grep -n 'D176'
  features/reminders/domain/src/main` lists the three files, and `git diff origin/develop --
  features/reminders/domain/src/main/java/com/migrely/app/reminders/domain/model/ReminderSettingsDomainModel.kt` is empty.
  Done: D176 in the KDoc of the three files; `git diff origin/develop -- …/ReminderSettingsDomainModel.kt` prints 0 lines.
- [x] 2.5 Data, `RemindersRepository` implements both interfaces. Tests in `RemindersRepositoryTest` that each delegates to
  its data source. Verify: `./gradlew :features:reminders:data:test --tests '*RemindersRepositoryTest'`.
  Done: `RemindersRepository` implements both; 2 new delegation tests. `:features:reminders:data:test`
  `RemindersRepositoryTest` 7/7.
- [x] 2.6 Data source: `ReminderScheduleDataSource.scheduleDailyIfNone(at, date)` with `ExistingWorkPolicy.KEEP` on the same
  unique name "daily" and the same input data; `scheduleDaily` stays REPLACE. A data source for "Migrely is on screen" in
  `app`, using `ActivityManager.getMyMemoryState` and `IMPORTANCE_FOREGROUND` (design D3), wired through `datasource/source`.
  **Red** test in `ReminderScheduleDataSourceImplTest`: `scheduleDailyIfNone` enqueues "daily" with KEEP; `scheduleDaily`
  with REPLACE. Verify: `./gradlew :app:testDebugUnitTest --tests '*ReminderScheduleDataSourceImplTest'`.
  Done: `scheduleDailyIfNone` (KEEP, same name and input); `AppOnScreenDataSource` in `datasource/source`,
  `AppOnScreenDataSourceImpl` in `app`. Red with `scheduleDailyIfNone` delegating to `scheduleDaily`:
  `ReminderScheduleDataSourceImplTest` 1 of 4 fails (KEEP), and the new Robolectric `DailyWorkTest` (real WorkManager)
  1 of 3 fails (a waiting run is kept). Green: 4/4 and 3/3. `AppOnScreenDataSourceImplTest` (Robolectric) 2/2;
  negative control (`<= IMPORTANCE_FOREGROUND_SERVICE`): 1 of 2 fails.
- [x] 2.7 Wiring: `MigrelyApplication.onCreate` passes "app start", `TimeChangeReceiver` passes "clock change";
  `RemindersModule` and `ReminderEntryPoint` provide the new pieces. The comment above the start-up call names D175, D176
  and EC-REM-22: the plan stays here, at every process start, and keeps a planned run. Verify: `git grep -n 'D176'
  app/src/main/java/com/migrely/app/MigrelyApplication.kt` prints 1 line.
  Done: `MigrelyApplication` passes `AppStart`, `TimeChangeReceiver` passes `ClockChange`, `RemindersModule` provides
  the data source and the new arguments; `ReminderEntryPoint` needed nothing new (it hands out the use cases). `git
  grep -n D176 app/src/main/java/com/migrely/app/MigrelyApplication.kt` prints 1 line.
- [x] 2.8 Gate: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint
  :app:checkReleasePermissions`. Verify: exit 0, the task count, and the same 6 release permissions as 1.2.1:
  `POST_NOTIFICATIONS`, `ACCESS_NETWORK_STATE`, `BILLING`, `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`,
  `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. `git diff --stat origin/develop -- '**/AndroidManifest.xml' '*.toml' '*.kts'`
  prints nothing.
  Done: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exit 0, BUILD SUCCESSFUL,
  2058 actionable tasks, 3046 unit test results, 0 failed (first run failed on ktlint function-signature in the new
  `DailyWorkTest`, fixed). `:app:checkReleasePermissions :app:checkReleaseDebugComponents` exit 0; the merged release
  manifest lists exactly the 6 permissions above, no INTERNET. `git diff --stat origin/develop --
  '**/AndroidManifest.xml' '*.toml' '*.kts'` prints nothing.
- [x] 2.9 Device walk on emulator-5570 (API 36), debug build, with `migrely-work/walks/r2/bin/iter.sh`:
  `late` mode 3 runs. Expected (amended by product-manager 2026-11-13 after the finding below; the first text, "posted=1
  each" by 20:07, was wrong, not the code): the 20:01 tap starts the process and the kept, overdue run comes about 1 s
  later while Migrely is opening, so at the end of `iter.sh` (by 20:07) posted=0, `daily_last_shown_date` is not the run
  date, and a "daily" job is planned about 30 minutes ahead; then, with Migrely off screen (HOME), posted=1 exactly once
  by about 20:40, the date marked, the next run tomorrow. Negative control 74095b8d: posted=0 at 20:07 and still at 20:40
  (run lost, 0/3). `kill` mode 3 runs → posted=1; `alive` 1 run → posted=1. Two new
  probes: (a) a `late` run from a copy of `iter.sh` without the second HOME key, so the app stays on screen when idle is
  unforced → posted=0, `daily_last_shown_date` is not that date (read with `adb shell run-as com.migrely.app`), and
  jobscheduler shows a daily job about 30 minutes ahead; then HOME → posted=1 once when that job runs; (b) R1: log
  `getMyMemoryState().importance` in a debug-only line, at `Application.onCreate` and in the daily run, for: a tap start
  (expect 100), a job start in the background (expect above 100), the lock screen right after the power key with Migrely
  behind it (expect above 100), split screen with Migrely visible (expect 100), the notification shade pulled over
  Migrely (expect 100). Remove the log after. Verify: the RESULT lines and the probe outputs in the handoff. If any of (b)
  differs from the expectation, stop and send it to `product-manager`.
  Done (emulator-5570, API 36, debug 909561bd; scripts and runs in `migrely-work/walks/r2/eng213/`, copies of r2 with
  uid u0a336, plus `waitpost.sh`/`full.sh` and `probe.sh`). Finding, accepted by team-lead: in `late` mode the 20:01
  tap starts the process and WorkManager's GreedyScheduler runs the kept, overdue work about 1 s later, while Migrely
  is opening, so D3 skips it and posts about 30 min later; the first expected "posted=1" by 20:07 was wrong
  (amended above). `late` 3/3 (L1214–L1216) posted=0 by 20:07, date not marked, daily job +29m59s; full
  `late` runs that wait for the postponed run 3/3 posted=1 once (L1216 20:31:19, L1217 20:32:39, L1218 20:36:11), date
  marked, next run tomorrow (74095b8d: 0/3, `walks/r2/results.txt` L23, L1130, L1201). `kill` 3/3 posted=1
  (K1220–K1222, 20:00). `alive` 1/1 posted=1 (A1223). (a) `lateon` 1 run (O1219): posted=0 at 20:07 with the app on
  screen, `daily_last_shown_date` 2026-12-18 (not the run date), job +29m59s; HOME, then posted=1 once at 20:33:21,
  date marked. (b) Importance, temporary debug-only log in `Application.onCreate` and `AppOnScreenDataSourceImpl`,
  removed before the APKs below (daily job forced with `cmd jobscheduler run -f -n androidx.work.systemjobscheduler`):
  tap start onCreate 100; tap and job in one command: run 100, postponed; job then tap: onCreate 400, run 100,
  postponed; job cold start in the background: onCreate 400, run 230, posted; screen off after the power key: 230,
  posted; lock screen shown with Migrely behind (swipe lock enabled for the probe, then disabled and the emulator
  rebooted): 230, posted; split screen with Settings, Migrely visible: 100, postponed; shade over Migrely: 100,
  postponed; on home: 100, postponed; HOME then the postponed run: 230, posted once. All as design D3 expects. Two
  voided runs are kept in `probes.txt`: the lock probe with the lock screen disabled (no keyguard), and a tap race
  with a stale keyguard showing; both were redone. Not checked: API 26, real phones, real Doze windows; the postponed
  run was waited for in real time, the probes forced it. Device restored: auto time on, deviceidle unforced, battery
  reset, lock screen disabled, font 1.0, rotation auto; emulator shut down.
- [x] 2.10 Build for QA: the debug APK (for the r2 scripts) and the R8 release APK from this branch, plus 74095b8d's debug
  and R8 APKs as the negative control. Record the four SHAs and paths in the handoff.
  Done, in `migrely-work/walks/r2/eng213/apk/` (R8 = `assembleRelease` without `keystore.properties`, debug-signed
  like QA's earlier R8 builds, cert SHA-256 7eae4e48…): `debug-909561bd.apk` eb6971885e0fcb3c… (the walked APK; its
  dex matches a rebuild of the commit except one Hilt debug-info parameter name), `r8-909561bd.apk` 933fb5ddba46ddcd…
  (mapping beside it), `debug-74095b8d.apk` 1f5fb00d0797c27a…, `r8-74095b8d.apk` 5144c7297d7ee918… (built in a
  detached worktree at 74095b8d, since removed). Full SHA-256 in the handoff.
- [x] 2.11 Review hardening (code-reviewer cr-213, optional): `AppOnScreenDataSourceImpl` starts from `IMPORTANCE_GONE`,
  since a new `RunningAppProcessInfo` reports foreground; a memory state the system leaves unfilled now posts instead of
  postponing until midnight. Commit 84d1f86f. New test in `AppOnScreenDataSourceImplTest` (no entry for this process, so
  nothing is filled): red on 909561bd, 1 of 3 failing; green 3/3. Gate exit 0, 2058 tasks, 3047 unit test results, 0
  failed; `:app:checkReleasePermissions :app:checkReleaseDebugComponents` exit 0, the same 6 permissions. APKs in
  `migrely-work/walks/r2/eng213/apk/`, the 909561bd ones kept: `debug-84d1f86f.apk` c74881584e60fa4b…, `r8-84d1f86f.apk`
  ca29a45384e5d5fb… (debug-signed R8, mapping beside it). No device walk (QA 4.3 probes cover it).

## 3. Review (`code-reviewer`)

- [ ] 3.1 Check: the only REPLACE callers of the daily plan are `SetDailyReminderUseCase`, the clock-change request and
  `RunDailyReminderUseCase` (its next run and its on-screen postpone) (`git grep -n 'scheduleDailyReminder\|scheduleDaily(' -- '*.kt'`); the start-up path reaches only
  KEEP; the on-screen check cannot block a post when the screen is off, and
  an on-screen skip never calls `setDailyShown`; visit, effect and import paths are unchanged
  (`git diff origin/develop -- features/reminders/domain/src/main/java/com/migrely/app/reminders/domain/usecase/VisitReminderPlan.kt`
  is empty); each **red** test really fails without its production change. Verify: verdict with file:line references.

## 4. Independent check on device (`qa-engineer`)

What the engineer learned (2.9), which every row below relies on:
- **The overdue run comes at the tap.** When a tap starts the process after the time, WorkManager runs the kept, overdue
  daily work about 1 s later, while Migrely is opening. Design D3 postpones it 30 minutes. So a person who opens Migrely
  at 20:01 and leaves at once gets "How was today?" at the postponed run, about 20:31 (20:31–20:36 seen), never at the
  moment they leave. Any row that expects a post after an on-screen skip waits to about 20:40 of device time.
- **Scripts.** Copy `migrely-work/walks/r2/eng213/bin/` (r2's `iter.sh` with the `lateon` mode, plus `waitpost.sh`,
  `full.sh`, `shown.sh`) to `migrely-work/qa-213/bin/`. Change only the serial (`ANDROID_SERIAL`), the base paths
  (`walks/r2/eng213` → `qa-213`) and the app uid in every job grep (`u0a336`). `iter.sh <mode> <day> <label>` ends at the
  first WorkManager run or 20:07 and writes `RESULT`; `waitpost.sh <label> [home]` waits to 20:45 for a post and writes
  `RESULT2` (with `home` it presses HOME first); `full.sh <mode> <first day> <count> <prefix>` runs both, one day per run.
- **The uid is read, not typed.** After every install on each emulator: `adb shell dumpsys package com.migrely.app | grep
  -m1 userId` gives `userId=10NNN`, which is `u0aNNN` in `dumpsys jobscheduler`. An uninstall or another emulator changes it.
- **Forcing a job.** Where a row forces a run, take the id from `dumpsys jobscheduler` (`JOB
  androidx.work.systemjobscheduler:u0aNNN/<id>`) and run `adb shell cmd jobscheduler run -f -n
  androidx.work.systemjobscheduler com.migrely.app <id>`. Otherwise use real time and `deviceidle unforce`, as the scripts do.
- **Builds (2.10), in `migrely-work/walks/r2/eng213/apk/`;** check sha256 against the engineer's packet before installing:
  `debug-909561bd.apk` eb697188…, `r8-909561bd.apk` 933fb5dd…, `debug-74095b8d.apk` 1f5fb00d…, `r8-74095b8d.apk`
  5144c729…. `shown.sh` reads `daily_last_shown_date` with `run-as`, which works only on a debug APK: rows that read the
  date use the debug builds; 4.9 and 4.11 use R8. Never install `probe-909561bd.apk` (it carries the removed probe log).

On API 26 `pm revoke POST_NOTIFICATIONS` does not exist: clear notifications with `adb shell cmd notification` or a force
stop before the run, and record the change. Record every run in `migrely-work/qa-213/rows.jsonl`. Restore the clock
(`auto_time 1`), battery, deviceidle, rotation, font and airplane mode after each session. Any failed run is reported with
its log, never rerun away.

- [ ] 4.1 **The bug, fixed (API 36 and API 26).** `full.sh late`: the 20:01 tap, then HOME before idle is unforced. New
  build, expected: `RESULT` (by 20:07) posted=0, `daily_last_shown_date` is not the run date, and a "daily" job about 30
  minutes ahead; then `RESULT2` posted=1 exactly once by about 20:40, `daily_last_shown_date` is the run date, and the next
  job about 23.5 hours ahead. 3 runs on each API. A post before the postponed run (by 20:07) fails design D3; no post by
  20:45 fails D176: both go to `product-manager` with the log. **Negative control:** 74095b8d debug, same script: posted=0
  at 20:07 and still posted=0 at 20:40 (the run is lost, a job about 24 hours ahead), 1 run on each API.
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
