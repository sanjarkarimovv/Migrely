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

- [ ] 1.1 Confirm the rule as planned: more than 60 minutes after that date's set time, current setting, current zone
  (design D2, D3); quiet = no sound, vibration or heads-up, everything else unchanged (D1); only the daily reminder (D5);
  no copy, screen, setting or channel changes. Any change to the rule goes back to the product-manager before code.
  Verify: a line in this task naming the decision, and `git diff origin/develop -- '*.xml' '*strings*'` empty.
- [ ] 1.2 Separate item, not this change: should "Did it help?" ever sound, given the person may be lying down after a dose
  (design D5)? Answer to team-lead as a new tracker item or "no change".

## 2. Code (`android-engineer`)

Write each test first. For every test marked **red**, show it failing with its production change reverted (minimal stubs to
compile are fine), then passing. Record both runs in the handoff.

- [ ] 2.1 Domain, `DailyReminderTime`: a function giving how late a run is for `date` (for example `lateBy(date, now, time):
  Duration`), using the existing private `at()`. **Red** tests in `DailyReminderTimeTest`: 59 min, 60 min exactly, 61 min,
  3 h 10 min; `now` before the time (clock moved back) → negative or zero; DST gap (2:30 AM on the jump night, now 3:35 AM →
  5 min); DST overlap (1:30 AM, now the second 1:30 AM → 60 min); a zone change (time read in `now.zone`). Verify:
  `./gradlew :features:reminders:domain:test --tests '*DailyReminderTimeTest'`.
- [ ] 2.2 Domain, `ShowDailyReminderRepository.showDailyReminder(date, quiet)` (one name across layers) and
  `RunDailyReminderUseCase`: after every existing check and the on-screen rule, `quiet = lateBy(...) > 60 min`; show with it,
  then `setDailyShown` as before. **Red** tests in `RunDailyReminderUseCaseTest`: 8:07 PM → shown, not quiet; 9:00 PM →
  not quiet; 9:01 PM → quiet; 11:10 PM → quiet and `setDailyShown(today)`; a postponed run at 8:31 PM → not quiet; at
  9:40 PM → quiet; a catch-up run (`request = today`) at 11:00 PM → quiet; on screen at 11:10 PM → postponed, no show (D176
  unchanged); setting changed to 10:00 PM, run at 10:05 PM → not quiet; date not today, answered, notifications off,
  already shown → no show (unchanged). Existing tests stay green. Verify: `./gradlew :features:reminders:domain:test
  --tests '*RunDailyReminderUseCaseTest'`.
- [ ] 2.3 Domain KDoc: `RunDailyReminderUseCase` and `DailyReminderTime` name D183 and the threshold in one line each.
- [ ] 2.4 Data, `RemindersRepository` passes `quiet` to `NotificationDataSource`. **Red** test in `RemindersRepositoryTest`:
  quiet and not quiet each reach the data source unchanged. Verify: `./gradlew :features:reminders:data:test --tests
  '*RemindersRepositoryTest'`.
- [ ] 2.5 Data source, `NotificationDataSource.showDailyReminder(date, quiet)` KDoc and `NotificationDataSourceImpl`:
  `.setSilent(quiet)` on the existing builder, nothing else. **Red** Robolectric test beside `NotificationLockScreenTest`:
  the posted daily notification with `quiet = true` is silent by androidx's own marker (record which field: group-alert
  behaviour, sound/vibration, or the platform flag, per API level Robolectric runs), and with `quiet = false` it is not;
  both keep `VISIBILITY_PRIVATE`, the "Reminder" public version, the title, the text and both actions. Red with `setSilent`
  removed. `NotificationLockScreenTest` stays green. Update the call in `NotificationDataSourceImplTest` (androidTest).
  Verify: `./gradlew :app:testDebugUnitTest --tests '*Notification*'` and `./gradlew :app:compileDebugAndroidTestKotlin`.
- [ ] 2.6 Wiring: every caller and fake of `showDailyReminder` compiles; no other notification changes. Verify:
  `git diff origin/develop --stat` lists only the files in 2.1–2.5 plus tests and this change's docs; `git grep -n
  "setSilent" -- '*.kt'` shows one production line.
- [ ] 2.7 Gate. Verify: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0, and
  `./gradlew :app:checkReleasePermissions` shows no new permission.
- [ ] 2.8 Device walk on emulator-5570 (API 36), auto time off, DND off, ringer normal; restore all after. Daily reminder on
  for a time T, today not answered, Migrely in the background. (a) Set the clock to T + 2 h: the catch-up posts once, in the
  shade with the icon, text and both actions; `adb logcat -b events -d -s notification_alert` has no line for it; tapping
  "Pain-free day" marks today. (b) Next date, set the clock to T + 30 min: posts with a `notification_alert` line (sound).
  (c) Negative control: (a) on a develop e4b1fbcd build shows a `notification_alert` line. Record androidx's mechanism (R1).
  If the shade entry, icon or actions are missing in (a), stop and send it to the product-manager.

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
  sound on the update), "Reboot after the time" (quiet, once). Verify: the same record.
- [ ] 4.3 TalkBack on API 36: the quiet reminder is reachable in the shade and reads title, text and both actions. Record,
  without pass or fail, whether TalkBack announced its arrival with the screen on and with it off. Verify: the record.
- [ ] 4.4 Untouched paths, one row each on API 36: "Did it help?" late and the visit reminder late still log a
  `notification_alert`; Android's notification settings list the same three channels; the permission report equals
  develop e4b1fbcd's. Verify: the record.
- [ ] 4.5 Optional, if time allows: one real Doze-held run (`dumpsys deviceidle force-idle`) released more than 60 minutes
  late, posted quietly. Not required (design R5).

## 5. Fixes (`android-engineer`)

- [ ] 5.1 Fix review and QA findings on this branch; re-run the gate. Verify: the gate command in 2.7 exits 0.

## 6. Close (`product-manager`)

- [ ] 6.1 Acceptance against this spec, archive the change, sync `openspec/specs/reminders/spec.md`, mark §13 row 51 done.
  Verify: `openspec validate --all --strict` exits 0 and `openspec list` no longer shows the change.
