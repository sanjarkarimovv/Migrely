# Tasks

Base: `feature/quiet-late-daily-reminder` from `origin/develop` e4b1fbcd. Write `Refs #213` in every commit and in the PR
into `develop`, never `Fixes`. BRD D183, EC-REM-29, EC-REM-30. Release: 1.3.0.

Ownership:
- `product-manager`: 0 (done) and 6;
- `ux-designer`: 1;
- `android-engineer`: 2 and 5;
- `code-reviewer`: 3;
- `qa-engineer`: 4 (emulator-5580, API 36; emulator-5582, API 26; one at a time).

No medical-reviewer: no user-facing string and no health sentence changes. No new copy.

## 0. Plan (`product-manager`)

- [x] 0.1 BRD D183, EC-REM-29, EC-REM-30, §13 row 51; proposal, design, spec delta, tasks. Verify:
  `openspec validate quiet-late-daily-reminder --strict` and `openspec validate --all --strict` exit 0.

## 1. Behaviour check (`ux-designer`)

- [x] 1.1 Confirm the rule as planned: more than 60 minutes after that date's set time, current setting, current zone
  (design D2, D3); quiet = no sound or vibration, everything else unchanged (D1); only the daily reminder (D5);
  no copy, screen, setting or channel changes. Any change to the rule goes back to the product-manager before code.
  Verify: a line in this task naming the decision, and `git diff origin/develop -- '*.xml' '*strings*'` empty.
  1.1 ux-designer 2026-11-16: confirmed, the rule matches the #213 (c) intent: more than 60 min after that date's set time,
  current setting and zone; setSilent only; no copy, screen, setting or channel change. `git diff origin/develop -- '*.xml'
  '*strings*'` is empty.
- [x] 1.2 Separate item, not this change: should "Did it help?" ever sound (design D5)? ux-designer proposed it always
  quiet; team-lead opens a backlog row. Nothing of it is in this change.

## 2. Code (`android-engineer`)

Write each test first. For every test marked **red**, show it failing with its production change reverted (minimal stubs to
compile are fine), then passing. Record both runs in the handoff.

- [x] 2.1 Domain, `DailyReminderTime`: a function giving how late a run is for `date` (for example `lateBy(date, now, time):
  Duration`), using the existing private `at()`. **Red** tests in `DailyReminderTimeTest`: 59 min, 60 min exactly, 61 min,
  3 h 10 min; `now` before the time (clock moved back) → negative or zero; DST gap (2:30 AM on the jump night, now 3:35 AM →
  5 min); DST overlap (1:30 AM, now the second 1:30 AM → 60 min); a zone change (time read in `now.zone`). Verify:
  `./gradlew :features:reminders:domain:test --tests '*DailyReminderTimeTest'`.
  2.1 android-engineer 2026-11-16 (ea7ebf2d): red on a `Duration.ZERO` stub, 17 run, 5 failed (all five new); green 17/0.
  Mutations, each red: wall-clock difference (DST gap and overlap, 2 failed), UTC instead of `now.zone` (5), later offset
  in `at()` (overlap, 1). Logs `migrely-work/walks/t61/2.1-*.log`.
- [x] 2.2 Domain, `ShowDailyReminderRepository.showDailyReminder(date, quiet)` (one name across layers) and
  `RunDailyReminderUseCase`: after every existing check and the on-screen rule, `quiet = lateBy(...) > 60 min`; show with it,
  then `setDailyShown` as before. **Red** tests in `RunDailyReminderUseCaseTest`: 8:07 PM → shown, not quiet; 9:00 PM →
  not quiet; 9:01 PM → quiet; 11:10 PM → quiet and `setDailyShown(today)`; a postponed run at 8:31 PM → not quiet; at
  9:40 PM → quiet; a catch-up run (`request = today`) at 11:00 PM → quiet; on screen at 11:10 PM → postponed, no show (D176
  unchanged); setting changed to 10:00 PM, run at 10:05 PM → not quiet; date not today, answered, notifications off,
  already shown → no show (unchanged). Existing tests stay green. Verify: `./gradlew :features:reminders:domain:test
  --tests '*RunDailyReminderUseCaseTest'`.
  2.2 android-engineer 2026-11-16 (3422a815): 16 new tests. Red with the use case passing `false`: 115 run, 6 failed (every
  quiet case: 9:01 PM, 9:40 PM, 11:00 PM catch-up, 11:00 PM run without a date, 11:10 PM, Berlin 3 h); the not-quiet and
  no-show cases pass on the stub by design. Green 30/0 (module 115/0). Mutations, each red: `>=` (exactly 60, 1 failed),
  fixed 8 PM instead of the setting (10 PM and 11 PM, 2), 30 min (4), 120 min (2), Tashkent instead of `now.zone` (Berlin
  8:30 PM, 1). Extra rows: early run 7:50 PM not quiet, 11 PM set time at 11:59 PM not quiet, 11:15 PM held past
  midnight no show, Berlin 8:30 PM not quiet, late and answered/shown/notifications off no show.
- [x] 2.3 Domain KDoc: `RunDailyReminderUseCase` and `DailyReminderTime` name D183 and the threshold in one line each.
- [x] 2.4 Data, `RemindersRepository` passes `quiet` to `NotificationDataSource`. **Red** test in `RemindersRepositoryTest`:
  quiet and not quiet each reach the data source unchanged. Verify: `./gradlew :features:reminders:data:test --tests
  '*RemindersRepositoryTest'`.
  2.4 android-engineer 2026-11-16: red with the repository passing `false`, 8 run, 1 failed; green 8/0; mutation `true`
  red (1).
- [x] 2.5 Data source, `NotificationDataSource.showDailyReminder(date, quiet)` KDoc and `NotificationDataSourceImpl`:
  `.setSilent(quiet)` on the existing builder, nothing else. **Red** Robolectric test beside `NotificationLockScreenTest`:
  the posted daily notification with `quiet = true` carries androidx's group trick (group "silent", `GROUP_ALERT_SUMMARY`,
  no sound or vibration; design D4), and with `quiet = false` it has no group and none of that;
  both keep `VISIBILITY_PRIVATE`, the "Reminder" public version, the title, the text and both actions. Red with `setSilent`
  removed. `NotificationLockScreenTest` stays green. Update the call in `NotificationDataSourceImplTest` (androidTest).
  Verify: `./gradlew :app:testDebugUnitTest --tests '*Notification*'` and `./gradlew :app:compileDebugAndroidTestKotlin`.
  2.5 android-engineer 2026-11-16: `NotificationQuietDailyTest`, Robolectric SDK 26 and 35. Red without `setSilent`: 8 run,
  2 failed (quiet on 26 and 35, `expected:<silent> but was:<null>`); green 8/0 with `NotificationLockScreenTest`. Mutation
  `setSilent(true)`: not-quiet red on both SDKs. androidx core 1.17.0 (`NotificationCompatBuilder.java:278-297`, read from
  the sources jar): on API 26+ it sets group "silent" when no group is set and `GROUP_ALERT_SUMMARY`, clears sound,
  vibration and their defaults; no summary is posted. The test confirms exactly that on SDK 26 and 35, and that channel
  "daily", small icon, title, text, both actions, `VISIBILITY_PRIVATE`, the "Reminder" public version and no
  group-summary flag are the same quiet or not. The not-quiet post has no group and `GROUP_ALERT_ALL`.
  `:app:compileDebugAndroidTestKotlin` exit 0.
- [x] 2.6 Wiring: every caller and fake of `showDailyReminder` compiles; no other notification changes. Verify:
  `git diff origin/develop --stat` lists only the files in 2.1–2.5 plus tests and this change's docs; `git grep -n
  "setSilent" -- '*.kt'` shows one production line.
  2.6 android-engineer 2026-11-16: `git grep -n setSilent -- '*.kt'` = `NotificationDataSourceImpl.kt:33` only; no other
  `setGroup`/`GROUP_ALERT` in Kotlin; diff = the 2.1–2.5 files, their tests and this change's docs.
- [x] 2.7 Gate. Verify: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0, and
  `./gradlew :app:checkReleasePermissions` shows no new permission.
  2.7 android-engineer 2026-11-16 on 3422a815: gate exit 0 (`BUILD SUCCESSFUL in 4m 12s`, 0 test failures),
  `:app:checkReleasePermissions` exit 0; merged release manifest uses-permission: ACCESS_NETWORK_STATE,
  POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, WAKE_LOCK, BILLING, DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION; no INTERNET;
  no manifest, Gradle or catalog file changed. Logs `migrely-work/walks/t61/gate.log`, `perms.log`.
- [ ] 2.8 Device walk on emulator-5570 (API 36), auto time off, DND off, ringer normal; restore all after. Daily reminder on
  for a time T, today not answered, Migrely in the background. (a) Set the clock to T + 2 h: the catch-up posts once, in the
  shade with the icon, text and both actions; `adb logcat -b events -d -s notification_alert` has no line for it; tapping
  "Pain-free day" marks today. (b) Next date, set the clock to T + 30 min: posts with a `notification_alert` line (sound).
  (c) Negative control: (a) on a develop e4b1fbcd build shows a `notification_alert` line. Record androidx's mechanism (R1).
  If the shade entry, icon or actions are missing in (a), stop and send it to the product-manager. API 26 is QA's (4.1).
  2.8 android-engineer 2026-11-16: not run, no boot tried. Preconditions failed at 16:24 and 16:34: emulator-5560 (another
  project) running and swap free 1.69 GB, then 1.44 GB (need more than 2 GB). Left to QA 4.1 and 4.2, which cover (a),
  (b) and (c) on API 36 and API 26. R1 is so far proven only in Robolectric (SDK 26 and 35), not on a device.

## 3. Review (`code-reviewer`)

- [ ] 3.1 Review the diff against design D1–D6: lateness from the set time not the planned instant, strictly more than
  60 minutes, the same `at()` as the plan, `setSilent` only on the daily post, no new string, channel, permission or stored
  value, red runs recorded. Verify: review notes with no blocking finding, or fixes and a re-review.

## 4. Independent check (`qa-engineer`)

Go/No-Go covers only what this diff reaches (owner rule 2026-11-09). Restore every device setting you change. Never run adb
by hand during a suite.

- [ ] 4.1 API 36 (emulator-5580) and API 26 (emulator-5582), one at a time, the build under test: the spec's "Clock moved
  forward" (quiet, no `notification_alert`, shade entry with icon and both actions, date marked), "About on time" and "The
  boundary" (T + 59 min alerts, T + 61 min quiet, each on its own date), "Clock moved back after a quiet post" (no second
  post). Negative control: develop e4b1fbcd on the same rows alerts at T + 2 h. Verify: a record in `docs/qa/` with counts
  per row and API.
- [ ] 4.2 Same devices: "Time zone change after the time" (quiet), "Do Not Disturb on" and "Migrely allowed through Do Not
  Disturb" (no sound), "Lock screen" (hide sensitive content: "Reminder" only), "Pain-free day on a quiet reminder" (no
  sound on the update), "Reboot after the time" (quiet, once), "The person's channel settings" with the "daily" channel set
  to silent (on-time and late both silent, no `notification_alert` sound; restore the channel after). Verify: the same
  record.
- [ ] 4.3 TalkBack on API 36: the quiet reminder is reachable in the shade and reads title, text and both actions. Record,
  without pass or fail, whether TalkBack announced its arrival with the screen on and with it off. Verify: the record.
- [ ] 4.3b Record only, not pass or fail, on API 36: with "wake screen for notifications" or always-on display on, does a
  quiet late post light the screen? Compare with an on-time post. Verify: the record.
- [ ] 4.4 Untouched paths, one row each on API 36: "Did it help?" late and the visit reminder late still log a
  `notification_alert`; Android's notification settings list the same three channels; the permission report equals
  develop e4b1fbcd's. Verify: the record.
- [ ] 4.5 Optional, if time allows: one real Doze-held run (`dumpsys deviceidle force-idle`) released more than 60 minutes
  late, posted quietly. Not required (design R5).
- [ ] 4.6 Optional: raise the "daily" channel to "Pop on screen" in Android's settings (restore after). An on-time post
  pops up; a late one (clock moved forward) does not. Only this row is evidence about pop-ups: on the default channel
  nothing pops up on develop either (design D1). Verify: the record.

## 5. Fixes (`android-engineer`)

- [ ] 5.1 Fix review and QA findings on this branch; re-run the gate. Verify: the gate command in 2.7 exits 0.

## 6. Close (`product-manager`)

- [ ] 6.1 Acceptance against this spec, archive the change, sync `openspec/specs/reminders/spec.md`, mark §13 row 51 done.
  Verify: `openspec validate --all --strict` exits 0 and `openspec list` no longer shows the change.
