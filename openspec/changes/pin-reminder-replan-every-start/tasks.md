# Tasks

Base: `bugfix/197-replan-after-update` from `origin/develop` (74095b8d). Write `Refs #197` in every commit and in the PR into
`develop`. Never write `Fixes #197`: QA filed it, QA retests, and the lead closes it as "not planned". BRD D175.

Ownership:
- `product-manager`: 0 (done) and 5;
- `android-engineer`: 1 and 2;
- `code-reviewer`: 3;
- `qa-engineer`: 4 (emulator-5582, API 26; control on emulator-5580, API 36).

No ux-designer or medical-reviewer: no screen, no copy.

## 0. Plan (`product-manager`)

- [x] 0.1 BRD D175, EC-REM-22, EC-REM-23, EC-STO-10, §13 row 44; this change. Verify:
  `openspec validate pin-reminder-replan-every-start --strict` exits 0.

## 1. Domain, presentation, data, UI: none (`android-engineer`)

- [ ] 1.1 No behaviour change. Verify: `git diff --stat origin/develop -- '*.kts' '*.toml' '**/AndroidManifest.xml' features
  datasource` prints nothing.

## 2. Wiring: one comment (`android-engineer`)

- [ ] 2.1 In `app/src/main/java/com/migrely/app/MigrelyApplication.kt`, extend the comment above the
  `rescheduleRemindersUseCase()` call: this start may come from a stale job after an update emptied WorkManager's database,
  so the re-plan must stay here, at every process start, not in an activity (BRD D175, EC-REM-22). Verify:
  `git diff origin/develop -- app/src/main/java/com/migrely/app/MigrelyApplication.kt | grep '^[-+] ' | grep -v '^[-+] *//'`
  prints nothing, and `grep -c 'D175' app/src/main/java/com/migrely/app/MigrelyApplication.kt` is 1.
- [ ] 2.2 Gate: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint :app:checkReleasePermissions`.
  Verify: exit 0, with the task count, and `checkReleasePermissions` lists the same 6 as 1.2.1: `POST_NOTIFICATIONS`,
  `ACCESS_NETWORK_STATE`, `BILLING`, `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`, `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`.
- [ ] 2.3 Build the R8 release APK from this branch for QA and record its SHA in the handoff. No device walk: there is no
  runtime change.

## 3. Review (`code-reviewer`)

- [ ] 3.1 The diff outside `docs/` and `openspec/` is one comment in `MigrelyApplication.kt`. The comment is true of the code
  (the re-plan runs at every process start, before any activity). The spec requirement matches `RescheduleRemindersUseCase`,
  `VisitReminderPlan`, `GetMedicationPromptUseCase` and the manifest. Verify: review verdict with file:line references.

## 4. Premise check on device (`qa-engineer`)

One-time check of D175's premise, not a release Go/No-Go row. Builds: "old" = 1.1.1 R8 (or 1.2.0 R8 `r8-9d453bb5`, as in
`qa-193`), "new" = the 2.3 APK. Use the plain-clear residual method of `migrely-work/qa-193` rows `26-plain-ctl-*` and
`26-timed-ctl-1`. Record each run in `migrely-work/qa-197/rows.jsonl`. Restore the clock, rotation, font and airplane mode after.

Acceptance (each must be able to fail, and the negative control shows it can):

- [ ] 4.1 **Residual, daily (API 26).** Old build: daily reminder on for R = about 15 minutes ahead, clear storage, reopen, turn
  the reminder on again; update to new before R; do not touch the device. Expect: `androidx.work.workdb` gone after the update
  (else the run is void, not a pass); at R no "How was today?" and the log has "Didn't find WorkSpec"; within 5 minutes after R a
  Migrely job with about 24 h delay in `adb shell dumpsys jobscheduler`, or `workdb` back with a `daily` row. 3 runs.
  **Negative control:** between the update and R, the same probe shows no `workdb` and no new job. That proves the probe tells
  "re-planned" from "not re-planned". If another stale job starts the process before R, the re-plan comes earlier: record it,
  it is not a failure.
- [ ] 4.2 **Residual, the next day (API 26).** On one 4.1 run, leave the emulator untouched until the next day's R. Expect
  "How was today?" posts once at about R. 1 run. If 24 hours is not practical, record it as not checked; 4.1's planned job
  stays the evidence.
- [ ] 4.3 **Residual, visit (API 26).** Set the emulator clock before the setup so that a visit's reminder time is about
  15 minutes after the update, then the same residual. Expect the visit reminder posts once within a few minutes of its time, with
  no app open. 1 run.
- [ ] 4.4 **Residual, "Did it help?" (API 26).** Before the update, log an attack medication whose 2 hours end about 15 minutes after the update (set its time, or log it
  and update within 2 hours). After the
  residual update: no notification at 2 h; open Migrely within 24 h: home's card asks how much it helped. **Control:** the same
  steps with no clear: the notification posts at 2 h, once. 1 + 1 runs.
- [ ] 4.5 **Normal update (API 26 and API 36).** No clear. Daily reminder R about 15 minutes after the update, process dead at R.
  Expect: `workdb` kept, "How was today?" posts once at about R. 5 runs on API 26 (this also probes design R2), 1 on API 36.
  Any run where it does not post goes into the report with its log; do not rerun it away.
- [ ] 4.6 **Reminder off.** Residual with the daily reminder off and a visit set. Expect no daily job and no daily post. 1 run.
- [ ] 4.7 **Old build check (the "fails on 74095b8d" rule).** D175 changes no runtime path, so the old build (develop
  74095b8d) and the new build must behave the same in 4.1. Run 4.1 once with 74095b8d's R8 as "new". Expect the same result.
  A difference means the branch changed behaviour and blocks the merge.

Report in the handoff packet: counts per row, the probe output lines, and what was not checked (Android 8.1–15, Play updates,
real phones). If 4.1 or 4.3 fails, send it to `product-manager`: D175 reopens.

## 5. Close (`product-manager`)

- [ ] 5.1 After 4: archive the change. Verify: `openspec archive pin-reminder-replan-every-start` and
  `openspec validate --specs --strict` exit 0; tell `team-lead` to close #197 as "not planned", citing D175 and QA's rows.
