# Release Go/No-Go: Migrely 1.2.0 (versionCode 10)

## Decision

**QA's verdict, 2026-11-10: GO for `release/1.2.0` @ 826887f22f60d6290e2e51fb533be4854400fa8a** (rows ran on
9d453bb5f1c63a2c127f9c54b6ab585522d010a1; 826887f2 changes one line of `docs/release/whats-new.md`, and its R8 APK has
the same 768 entries byte for byte, see "Candidate").

- **No S1 or S2 found, none open** (`gh issue list --state open`: none S1 or S2). One defect was filed:
  **#189 (S3, pre-existing)**. On Android 8.0, reminders pending across an app update ("Did it help?" and the daily)
  do not post; the in-app question still shows when the app is opened. 1.1.1 installed over 1.1.1 shows the same, so 1.2.0 does not
  introduce it. On API 36 it posts after an update, 4 of 4. Lead's decision 2026-11-10 (made on the follow-up rows):
  it does not block 1.2.0. The daily rows came after that decision; they widen the scope, not the origin, and QA keeps
  S3 (reminders are not a core flow, no data is lost).
- **Counts** (`migrely-work/qa-120/recount.py` over `logs/rows.jsonl`, plus the #161 and #166/#167 drivers' own logs):
  - **The release build 9d453bb5: 87 of 95 valid runs pass.** The 8 fails are all #189 on API 26: the follow-up after an
    update (R2-26 0/3, T2 0/1, T4 0/3) and the daily after a quiet update (T5 0/1).
  - **Controls: d29ffb82 10 of 14.** The 4 fails are the intended stale Remove in #187 S1 and S2.
  - **Controls: 1.1.1 R8 2 of 4.** The 2 fails are #189's old-over-old rows (T4, T5), which show it is pre-existing. The
    recount files a row under the APK hash read on the device, not the script's label.
  - **Single-build checks with sha "x":** 3 of 3 (DAILY-jump on both builds, EFF-26). All: 102 of 116.
  - **The drivers' own logs add:** #161 3/3 (control 0/3) and #166/#167 2/2 (control 0/2).
  - **14 runs void**, each with a reason; harness errors (not rows) are listed under "Voids".
- **R8 is the risk this run covers.** Every T21 and #178 behaviour row before ran on debug APKs. Here the doctor visit,
  the lock screen, #187, the backup, the upgrade from 1.1.1 and the two 1.1.x fixes ran on the **R8 release build**
  (signed with the debug key, not debuggable, sha256 checked on the device before each script).
- **WorkManager under R8 holds:** the visit reminder fires by itself at 10:00 two days before, and after a **real
  reboot** with no clock change after boot (so through WorkManager's own boot path, not the app's TIME_SET receiver).
  On API 36, after an upgrade from 1.1.1 R8 that is never opened, a pending "Did it help?" still posts, and the next
  day's daily reminder posts. On API 26 they do not after a quiet update (#189, pre-existing).
- **Room v7 under R8:** 1.1.1 R8 → 1.2.0 R8 keeps every table's rows (DB 6 → 7), on API 36 and API 26; a 1.1.1 backup
  imports into 1.2.0 with every count equal; a 1.2.0 backup with visits imports back with its visits.
- **Not proven:** the Premium Doctor report on R8 (no Play billing on the emulator image), real OEM phones, the upload-key
  signed AAB. See "What these checks do not prove".

## Candidate, scope and builds

- **Candidate:** `release/1.2.0`, PR #188 into `main` (open, not merged by QA).
  - 9d453bb5 = develop badd53c4 (PR #182 merged) + `app/build.gradle.kts` (versionCode 9 → 10, versionName 1.1.1 → 1.2.0)
    + `docs/release/whats-new.md`. Product code = 3309dfc2 = a600ba4f + test files.
  - 826887f2 = 9d453bb5 + one line of `docs/release/whats-new.md` (`git diff --stat 9d453bb5 826887f2`: 1 file, 1+/1−).
    QA built R8 at 826887f2 too: the APK differs only outside its entries (zip container and signature block); all
    **768 entries are byte-identical** (sha256 per entry). The rows below therefore stand for 826887f2.
- **Scope (owner rule 2026-11-09):** only what this release's diff can reach: R8 behaviour of the T21 doctor visit and
  #178 lock-screen privacy (both ran on debug before), the version bump, and the 1.1.x fixes that ship in 1.2.0 (#161,
  #166/#167), each with a negative control. Everything else carries over (below).
- **Builds** (QA's own, from the detached worktree `migraine-app-qa-120` at 9d453bb5, clean before and after; no
  `keystore.properties`, so release builds are signed with the debug key, D102):
  - `r8-9d453bb5.apk` sha256 79a0941932eb839e62f92950a93860d7d07e10195b69c337ecf48722c023f87f (minified, not debuggable);
  - `debug-9d453bb5.apk` 1c768f3e7e22ecd4… (only to read the DB under the #161 driver, and for one reboot diagnosis);
  - `r8-826887f2.apk` fc3314e69ff7eee6… (identity check only);
  - controls: `r8-eeb6.apk` b36a3fcbd452… (1.1.1's product tree, eeb69f8a), `debug-eeb6.apk`, `r8-d29ffb82.apk` (PR #182
    before the #187 fix).
- **Devices:** `Migrely_QA_API_36` (emulator-5580, google_apis userdebug) and `Migrely_QA_API_26` (emulator-5582, AOSP
  userdebug), one at a time, en-US, Asia/Tashkent. R8 is not debuggable, so the scripts read the database as root
  (`adb root`, then the app's files are given back to its uid and relabelled after each read; `qa-120/r8.py`). The clock:
  `cmd alarm` on 36, root `IAlarmManager.setTime` on 26.
- **Evidence:** `migrely-work/qa-120/` (outside the repo): `logs/rows.jsonl`, `voids.jsonl`, `recount.py`/`recount.txt`,
  `t51/logs/t51.jsonl` (#161), `t53b/logs/*.out` (#166/#167), `shots/`, `raw/` (backups), the scripts.

## The rows

Counts are valid passes over valid runs. 36 = emulator-5580, 26 = emulator-5582. All on `r8-9d453bb5` unless named.

### R1 identity and permissions

| Row | Result | Evidence |
|---|---|---|
| `aapt2 dump badging` of the R8 APK | ✅ versionCode 10, versionName 1.2.0, no `application-debuggable` | `logs/build.out` |
| Permissions equal 1.1.1's R8 | ✅ the same 6: POST_NOTIFICATIONS, ACCESS_NETWORK_STATE, BILLING, WAKE_LOCK, RECEIVE_BOOT_COMPLETED, DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION; **no INTERNET** | `:app:checkReleasePermissions` → `logs/perm-release-9d453bb5.txt`; badging of `r8-eeb6` |

### R2 upgrade 1.1.1 R8 → 1.2.0 R8 (`up120.py`)

Seed on 1.1.1 R8: one launcher launch first, Maestro `upgrade-seed` (2 attacks, an intensity point, Severe, Nausea), a
custom symptom on today's attack, Ibuprofen with two intakes at 09:00 and 09:05 through the UI (so "Did it help?" is
pending for about 11:05), the daily reminder on (20:00), and a 1.1.1 JSON backup exported. The app is then left as a
user leaves it (opened, Home, process killed; not force-stopped) and `adb install -r` puts 1.2.0 R8 over it. **1.2.0 is
not opened** before the follow-up check.

| Row | 36 | 26 | What was seen |
|---|---|---|---|
| DB kept | 4/4 | 3/3 | user_version 6 → 7; attack 2, intensity_point 1, attack_tag 2, custom_tag 1, medication 1, medication_intake 2, reminder_settings 1 equal; `doctor_visit` new and empty |
| "Did it help?" after the update, app not opened | 4/4 | **0/3, #189** | 36: clock to 11:06, posted at 11:06:01. 26: none (see the #189 rows below) |
| Records shown (`upgrade-kept` flow) | 4/4 | 3/3 | today's storm text, Nausea checked, last month's attack |
| Daily reminder the next day | 1/1 | 3/3 | "How was today?" at 20:01:32 (36) and 20:00:02 (26) the next day |
| R6: the 1.1.1 backup into a clean 1.2.0 | 4/4 | not run | file schema 6; every count equal; no visit; no error (36). On 26 the import step fails in the system file picker under Maestro (harness), so it was skipped |

**#189 on API 26 (the follow-up after an update).** Method: on 1.1.1 the clock moves to 10:57 *before* the update, so
1.1.1's own WorkManager re-times the two follow-ups (intakes 09:00 and 09:05). Then a quiet `install -r` follows, with
no clock change after it, and the wait runs to 11:12.

| Row | Path | Result |
|---|---|---|
| T4-26-new-quiet | 1.1.1 → 1.2.0, not opened | 0/2 posted |
| T4-26-new-open | 1.1.1 → 1.2.0, opened after the update | 0/1 posted |
| **T4-26-old-quiet: 1.1.1 R8 control** | **1.1.1 → 1.1.1** (installed APK b36a3fcb; `rows.jsonl` labels it r8-9d453bb5 by the script's default, and the recount files it under 1.1.1 by the hash) | **0/1 posted: pre-existing** |
| T4 in-app (all three) | the app opened afterwards | Home shows "How much did Ibuprofen help?" |
| T2ctl-26 (method control), EFF-26 | no update | posts (11:00:12; both builds) |
| T1, T1ctl, T3pre | `install -r` | the app's 2 JobScheduler jobs stay listed (2 → 2) |
| T5-26 daily, quiet update | 1.1.1 → 1.2.0; **control 1.1.1 → 1.1.1** (installed APK b36a3fcb) | 0/1 and 0/1: no "How was today?" by 20:08. The daily is affected too, also pre-existing |

### R3 doctor visit on R8

| Row | Valid | What was seen |
|---|---|---|
| A1 plan, fires by itself, content, marked, once | 1/1 each | posted at 10:00:32 two days before; "Your visit is on Sun, Nov 15" / "Make a report to bring.", `vis=PRIVATE`, public version |
| A1 control: already reminded | 1/1 | clock back to 09:55: nothing planned |
| A2 too close (today, tomorrow, +2 after 10:00), card | 1/1 each | nothing posted; "Your doctor visit is on Thursday, November 12, 2026." |
| A2 control: +2 days at 09:00 | 1/1 planned, 1/1 arrives | 10:00:47 |
| Card "Make a report" (free) | 1/1 | opens "A report for your doctor" (the paywall; no Play billing on this image) |
| A4 Remove + Undo, kill inside the window, control: Undo after 5 s | 1/1 each | "Visit removed", Undo restores, a kill in the window keeps the removal, no Undo after 5 s |
| DL: visit day and past visits never deleted (D168 (13)), controls | 1/1 each | the visit day kept, Remove absent on the visit day, a past visit kept, a clock back to the visit day kept; controls: an upcoming visit is replaced |
| **Real reboot** with a visit pending | 1/1 fires, 1/1 once | planned at 09:47 for 10:00, `adb reboot`, the clock kept over the reboot (09:48 after boot), **no clock change after boot**: posted at 10:01:58, once |

### R4 #178 lock screen on R8 (PIN 1357)

| Row | Valid | What was seen |
|---|---|---|
| Visit, Show all / Hide sensitive | 2/2, 2/2 | "Your visit is on Thu, Nov 12 · Make a report to bring." / "Reminder" only |
| Effect, Show all | 1/1 | "Did it help? · Tap to answer in Migrely." (no medication name) |
| Effect, Hide sensitive: no leak | 1/1 | two "Did it help?" are bundled by Android 16 under "Migrely"; nothing else shows (see voids) |
| Daily update, Show all / Hide (F2) | 1/1, 1/1 | "Not marked. Open Migrely to see today." / "Reminder" only |
| **Control: 1.1.1 R8 under Show all** | 1/1 | "Did Ibuprofen help?": the name the fix removes |

### R5 #187 on API 36 R8

| Row | 9d453bb5 | control d29ffb82 R8 |
|---|---|---|
| S1 a real midnight with Reminders open: Remove gone, visit kept | 2/2 | **0/2** (the stale Remove) |
| S2 clock moved to the visit day in the background: Remove gone | 2/2 | **0/2** (the stale Remove) |
| S3 zone change to the visit day, back, Remove works, Undo | 2/2 ×4 | 2/2 ×4 (does not tell the builds apart; no regression) |
| Before rows (Remove shown the day before) | 2/2 ×2 | 2/2 ×2 |

### R6 backup on R8 (`openspec/specs/data-backup/spec.md`)

| Row | Valid | What was seen |
|---|---|---|
| Export, schema 7, both visits, no reminder state (A13) | 1/1 | `backup_files.py`: "migrely-backup v7 … 2 doctor visits" |
| Byte check fails on a state field and on schema 6 (control) | 1/1 | |
| Add keeps one next visit; control: no next visit on the phone → the file's is added | 1/1, 1/1 | |
| Replace: the file's visits only, reminder planned | 1/1, 1/1 | |
| A schema 6 file imports with no visit | 1/1 | 14 attacks, no error |
| **Export then import that same file back** (visits deleted first) | 1/1 | both visits return, the next one's reminder planned |
| A 1.1.1 R8 export into a clean 1.2.0 R8 | 4/4 (36) | see R2 |

### R7 smoke: the 1.1.x fixes on R8, with controls

| Row | 1.2.0 R8 | control 1.1.1 R8 | Evidence |
|---|---|---|---|
| #161 (EC-UX-31): a second tap on Remove in the custom tag menu, gaps 148, 296, 460 ms (getevent) | **3/3**: the tag archived, no chip written, no message | **0/3**: `light_sensitivity` written under it each time | `t51/logs/t51.jsonl` (the R8 run reads the DB by installing the same-SHA debug APK over it, as in #172's retest) |
| #166: failed first 10 (full disk), the card in view and clear of "Couldn't save" (P:top) | **1/1**: title, body and "Got it" in view, overlap 0 px | **0/1**: the card's title not in view | `t53b/logs/r8-*-P:top.out`, shots |
| #167: after the failure, a working save clears the error (C10) | **1/1**: "Saved.", no storage error, gone after 10.1 s | **0/1**: the storage error stays | `t53b/logs/r8-*-C10.out` |

### R8 notes truth (826887f2's 1.2.0 block, checked on the R8 build)

| Sentence | True on R8? | Row |
|---|---|---|
| "Doctor visit: set your next visit's date in Reminders." | ✅ | A1 plan, A2, R5 |
| "Two days before, Migrely reminds you to make a report for your doctor." | ✅ | A1 fires: "Your visit is on … / Make a report to bring." at 10:00 two days before |
| "The Doctor report can cover the time since your last visit." | ⚠️ not shown on R8: Premium cannot be bought on the emulator image; true on debug (T21 record, A10/A26 5/5) | carried over |
| "Lock screen reminders no longer name a medication or say that you logged a migraine." | ✅ with its control | R4 |
| "A quick second tap on Remove in a custom tag's menu no longer selects what was under it." | ✅ with its control | R7 #161 |
| "On the Migraine screen, a message no longer covers the safety card, and "Couldn't save" clears once a save works." | ✅ with its controls | R7 #166/#167 |

## Voids (harness slips, each rerun; not counted)

- `R3-reboot-fires`: harness: rebooted ~3 s after the visit was planned; Android had not yet saved WorkManager's enabled RescheduleReceiver (package-restrictions written lazily), so the boot broadcast had no receiver. RB3: 5 s settle -> receiver disabled after boot, no process (1/1); 150 s settle -> process started for RescheduleReceiver, job back (1/1). Rerun with a 150 s settle.
- `R3-reboot-NC-no-second`: harness: consequence of the same quick reboot. Rerun.
- `R3-reboot-fires-nochange`: harness: rb2 probe run 1 (R8), same quick reboot (3 s). Diagnostic only.
- `R3-reboot-fires-nochange`: harness: rb2 probe run 2 (debug), same quick reboot (3 s). Diagnostic only.
- `A25-effect-hide`: harness: two "Did it help?" (the flows log two intakes) bundled by Android on API 36: only "Migrely" shows, so the "Reminder" text cannot be read; precedent qa-t21 23:36:29. Counted as A25-effect-hide-no-leak from the same raw read.
- `R2-36-db-kept`, `R2-36-followup-fires`, `R2-36-kept-flow`, `R2-36-daily-fires`: harness: the app was force-stopped (am force-stop) before install -r, so Android kept it in the stopped state and sent it no broadcast until opened (run 3 records stopped=true after force-stop, false on the user path); the daily check also ran on a day with a migraine logged. Rerun as run 3.
- `R2-36-daily-fires` ×3: harness: up120 force-stopped the app (home(); stop_app()) before moving the clock to the next day 19:58, so TIME_SET was not delivered and the job was cancelled (daily-diag: due still the day before, forced run found no job). The same jump without a force-stop re-plans and fires on 1.2.0 and 1.1.1 (DAILY-jump rows). Rerun with kill_bg.
- `T2ctl-followup-no-jump-past-due-26`: harness threshold: the first intake was at 09:00, so the follow-up is due at 11:00, not 11:05; it posted at 11:00:12 (a pass in substance). Used as the method control for #189.
- `T3-followup-real-time-after-quiet-update-26`: harness: the API 26 emulator clock ran slow (106 device minutes in ~140 host minutes), so the 2 h job was not due when the wait ended. Superseded by T4 (no clock change after the update).

Harness errors (not rows, rerun): the qa-t21 worktree was removed after PR #182 merged, so its Maestro flow paths broke
(lock, v111, A13; repointed to `migraine-app-qa-120`). The upgrade script waited for "Next visit" on 1.1.1, which has
none. The CSV export and the file-picker import step fail under Maestro on API 26 (the JSON export was kept alone). The
#161 control first tried a downgrade (debug vc9 over 1.2.0; rerun after an uninstall). #166's P:top first ran after row S
in the same install, so the 10 was not a first 10, on both builds (rerun on a fresh install per row). C10 hit a
truncated uiautomator dump on a full disk (rerun).

## Carried over, and why this diff cannot reach them

- **1.1.1's GO** (`docs/qa/go-no-go-1.1.1.md`): the offline Premium checks (D166), the card scrolling into view (D167),
  the whole app outside reminders, backup and the Migraine screen. 1.2.0 changes none of their code beyond what R2–R7
  exercise.
- **#172's retest of #161 (D164), R8 9/9** and its controls: here 3/3 on 1.2.0 R8 confirms it ships.
- **T53, PR #180 at febbfad3 (`docs/qa/fix-save-message-8.1.md`), R8 rows**: geometry at other fonts and landscape,
  TalkBack speech, Undo; here P:top and C10 on 1.2.0 R8 confirm it ships.
- **T21's record** (`docs/qa/go-no-go-doctor-visit.md`, debug, API 36 and API 26): the picker, TalkBack and labels, zones
  and DST, the paywall and the Premium report and its "Since" chip, delete all, the full-disk mark; its API 26 section.
- The whole-app regression runs nightly on `develop` (owner rule 2026-11-09).

## What these checks do not prove

- **The Premium Doctor report on R8** (and the "Since your last visit" chip): no Play billing on the emulator images,
  and the simulated store exists only in debug. Proven on debug only.
- **The upload-key signed AAB** that Play delivers: QA's R8 APK is signed with the debug key; split APKs from the bundle
  were not installed.
- **Real phones and OEM job rules**: emulator images only (Android 16 google_apis, Android 8.0 AOSP). One real reboot,
  on API 36 only; on API 26 no reboot ran.
- **Lived time**: the clock was set (except about 75 s across midnight in #187 S1); the reboot row is the only one where
  WorkManager waited on its own after boot.
- **An update that the user never opens and no broadcast wakes**, on API 36: the follow-up row moved the clock, which wakes
  the app through TIME_SET. On API 26 the quiet path was tested (T4) and is #189.
- **#189's root cause** (WorkManager or JobScheduler on Android 8 after a package replace), whether the notification
  comes later, and real Android 8 phones: not pursued (lead's decision).
- **Counts of one**: most R8 rows ran once (smoke scope); the upgrade ran 4 times on 36 and 3 times on 26.
- R5's zone rows do not tell the builds apart (the old build also passes them).

## Residual risks

- **#189 (S3, pre-existing, open):** on Android 8, reminders pending across an app update do not post: "Did it help?"
  and the daily (T4, T5). The visit reminder is likely to behave the same on the next update after 1.2.0 (not tested).
  The follow-up question is not lost: it shows on Home when the app is opened. 1.1.1 over 1.1.1 does the same, so it is
  not new in 1.2.0. Not known: whether the daily resumes on later days or after the app is opened. Android 8.0 is the
  minimum supported version.

- **WorkManager's boot receiver is saved lazily by Android.** A reboot within seconds of the very first plan lost the
  pending reminder until the app ran again (QA's RB3 rows: 5 s settle → no receiver after boot; 150 s → fine). A user who
  sets a visit and reboots at once is rare, and the next app start or clock change re-plans. Not filed: a platform
  behaviour with no data loss, and the reminder comes back on the next app start.
- **Maestro clearState as the first launch on API 26 empties `databases/` at the next `install -r`** (QA-only path,
  T21 record); every API 26 upgrade row here launched the old build from the launcher first.
- The PR #188 merge, the tag and the AAB build still follow; production must get a build whose tree equals the tag.
