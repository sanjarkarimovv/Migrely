# Change Go/No-Go: doctor visit mode (T21, D168) and notification privacy (#178, D169), PR #182

## Decision

**QA's verdict, 2026-11-10: GO for PR #182 at d29ffb82f9713b5bdc7f5542ebe0d409ab9b0906** (plus QA's own commits,
which change no product file), with the risks below open. **API 26 was added on 2026-11-10** (section "API 26").

- **No S1 or S2 found.** One S3 was filed: **#187**, a stale "Remove" on the visit day that does nothing (no data loss).
  It does not block a GO.
- **Counts** (`recount.py`, valid passes over valid runs): 133 of 134 valid runs pass on API 36; the one fail is #187 (`DL-no-remove-on-visit-day`). Harness slips were voided and rerun; each
  void names its reason in `rows.jsonl`.
- **The reminder works as specified on API 36:** it fires by itself at about 10:00 two days before (10:03 and 10:01
  device time), once, private with a public "Reminder" version. It stays away when too close or when nothing is
  logged, waits through an attack until 10:00 the next day, follows zone, clock and DST changes, and posts at once
  after a phone was off until the visit day.
- **No data loss on save:** a visit dated today or earlier is kept (D168 (13)), and its control shows deletions are
  seen. Upgrades from 1.0.2, 1.1.0 and 1.1.1 keep every record (DB 6 → 7). Backup Add and Replace follow D7, and
  schema 6 files import.
- **Privacy:** no permission added (and the check fails when one is added). The lock screen shows no medication, no
  "migraine is logged" and no channel name; under "Hide sensitive content" one notification shows "Reminder" only.
  1.1.1 shows "Did Ibuprofen help?" (the control).
- **The merge rows on d29ffb82** pass: "Visit removed", Undo, a second message, the TalkBack speech of both messages
  in full (2/2, #183's empty announcement not seen), the card with a home message, and the picker.
- **API 26 proven since (2026-11-10, see "API 26 (Android 8.0)"):** the clock was fixed with root and
  `IAlarmManager.setTime`. On d29ffb82 the reminder fires by itself at 10:00 two days before, nothing posts when too
  close, the lock screen shows no medication name and "Reminder" under Hide sensitive, and 1.1.1 to the candidate keeps
  every record. The picker rows pass on d29ffb82 and a600ba4f. 80 of 82 valid runs pass; the 2 fails are the #187
  controls on d29ffb82, which must fail.
- **Not proven:** landscape on a device, TalkBack focus speech of the labels, the full-disk mark (b), real OEM
  timing. See below.

## Candidate, scope and builds

- **Candidate:** PR #182, `feature/add-doctor-visit-mode`.
  - Tested heads: **85997737** (859977372d03b7c9ef092dca929e6886e479069b), 25 commits over develop; then
    **d29ffb82** (d29ffb82f9713b5bdc7f5542ebe0d409ab9b0906), develop 6bb4095a (PR #180, T53) merged into 85997737.
  - `git diff 85997737 d29ffb82` holds #180's code only: no file of #182's own diff changed in the merge
    (`team-lead`'s check; `git merge-tree` was clean).
  - QA's commits on top (this record, `qa/`, `.maestro/`) change no product file.
- **Scope (owner rule 2026-11-09):** only what this diff can reach, each check with a negative control. Acceptance is
  design.md "Acceptance for QA" A1–A26 of `add-doctor-visit-mode`, its tasks 8.1 and 8.2, and `fix-notification-privacy`
  tasks 4.1 and 4.2. Everything else carries over from 1.1.1's GO (`docs/qa/go-no-go-1.1.1.md`); the whole-app
  regression runs nightly on `develop`.
- **Rows the merge reaches**, rerun on d29ffb82: the "Visit removed" message, Undo and a second message on Reminders,
  the same with TalkBack, the home card with a home message, and the date picker once. Every other row on 85997737
  stays valid. Rows that ran on d29ffb82 for speed are named so.
- **Builds** (QA's own, from the detached worktree `migraine-app-qa-t21`, clean before and after):
  - `debug-85997737.apk` sha256 8a1ea563…; `r8-85997737.apk` a4c14b53…;
  - `debug-d29ffb82.apk` sha256 5f476810…; `r8-d29ffb82.apk` 46fc590e…;
  - negative controls: the 1.1.1 debug build `debug-eeb6.apk` (eeb69f8a, whose product tree equals tag v1.1.1),
    and for the upgrade the 1.0.2 (`debug-v1.0.2.apk`, from the tag) and 1.1.0 (`debug-110.apk`) debug builds.
- **Devices:** `Migrely_QA_API_36` (emulator-5580, cold boot 20:59) and `Migrely_QA_API_26` (emulator-5582), en-US,
  Asia/Tashkent. The clock was moved with `cmd alarm set-time`, auto time off, and restored after each script.
- **Evidence:** `migrely-work/qa-t21/` (outside the repo): `logs/rows.jsonl` (one line per check, with its SHA and
  host load), `recount.py` (every number below comes from it), `shots/`, `raw/`, and the scripts `s1.py`–`s5.py`,
  `inv1.py`.
- **How a row is counted:** pass or fail only when the check ran as meant. A harness slip (a wrong WorkManager state
  code, a relaunch that landed on Reminders rather than Home, a flow that never typed DELETE) is voided by a later
  `invalid` line that names it, and the row is rerun. Voided lines are listed per row.

## The rows

Counts are valid passes over valid runs, recounted from `logs/rows.jsonl`; "void" lists the runs voided as harness slips.
36 = emulator-5580 (API 36), 26 = emulator-5582 (API 26).

### Build, permissions, instrumented

| Row | SHA | Result | How |
|---|---|---|---|
| Instrumented `:designsystem` | 85997737 | 58/58 | `connectedDebugAndroidTest` on 36; `DatePickerScreenTest` 5/5, including `atTwiceTheFontSizeInLandscapeTheWeekdaysTwoWeeksAndDoneShow` (the "found 7 nodes" failure is gone) |
| Instrumented `:datasource:implementation` | 85997737 | 130/130 | `MigrationTest` 6→7 and 1→7, the visit DAO and backup cases |
| A14 permissions | 85997737, d29ffb82 | pass | the R8 APKs' permissions equal the 1.1.1 R8 APK's (eeb69f8a): `aapt2 dump permissions` diff empty, 6 permissions; `:app:checkReleasePermissions` exit 0 |
| A14 control | d29ffb82 | pass | `SCHEDULE_EXACT_ALARM` added in a scratch worktree: the task exits 1, "forbidden permissions: [android.permission.SCHEDULE_EXACT_ALARM]"; the worktree was removed |

### The reminder (A1–A3, A6, A7, A11)

| Row | SHA, device | Result | What was seen |
|---|---|---|---|
| A1 planned | 85997737 36, d29ffb82 36 | 2/2 | unique work `visit`, tag `visit-<date>`, due 10:00 two days before |
| A1 fires by itself | both, 36 | 2/2 | clock at 09:55 on the reminder day, no forced run: posted at 10:03:48 and 10:01:02 device time |
| A1 content | both, 36 | 2/2 | "Your visit is on Sun, Nov 15" / "Make a report to bring.", channel `visit`, `vis=PRIVATE` with a public version, no actions |
| A1 once / marked | both, 36 | once 2/2; marked 1/1 | a forced run of every job adds none; `visit_reminded_for` set (85997737's two reads void: a wrong WorkManager state code in the harness) |
| A1 control: already reminded | d29ffb82 36 | 1/1 | clock back to 09:55: nothing planned |
| A2 too close: today, tomorrow, +2 after 10:00 | both, 36 | 6/6 | marked at save, nothing planned or posted |
| A2 card instead | d29ffb82 36 | 1/1 | "Your doctor visit is on Thursday, November 12, 2026." (85997737's read void: the relaunch landed on Reminders) |
| A2 control: +2 at 09:00 | both, 36 | 4/4 | planned for 10:00 that day; it arrived at 10:00:32 |
| A3 moved | 85997737 36 | 1/1 | the posted notification goes; new work for 10:00 two days before the new date, tag `visit-2026-11-25` |
| A3 control: same date | 85997737 36 | 1/1 | work id and enqueue time unchanged |
| A6 attack at 10:00 | d29ffb82 36 | 5/5 | no post; `visit_postponed_to` = next day, work at 10:00 then; card hidden in attack mode, shown after the end; the notification at 10:00 the next day; a tap during an attack opens attack mode only |
| A7 nothing logged | d29ffb82 36 | 1/1 | no notification, no card, the date marked handled (85997737 void: card read on Reminders) |
| A7 control: one entry | d29ffb82 36 | 1/1 | notification and card appear |
| A11 zone, clock back, DST | both, 36 | 6/6 | Berlin: 10:00 Berlin; clock back a day: unchanged; New York, visit Nov 3: 10:00 EST on Nov 1 (the DST day) |
| A11 control: no change | both, 36 | 2/2 | time unchanged after a restart |
| A11 phone off until the visit day | both, 36 | 2/2 | posted at once (08:00:01) |
| A11 phone off past the visit | d29ffb82 36 | 1/1 | nothing posted, nothing planned, no card (85997737 void) |

### Removing, the data-loss rule, delete all (A4, D168 (13), A16)

| Row | SHA, device | Result | What was seen |
|---|---|---|---|
| A4 Remove, Undo, "Undone." | 85997737 36 | 1/1 (1 void) | row back, work back at 10:00 two days before |
| A4 killed in the Undo window | 85997737 36 | 2/2 | `run-as kill -9` at 3.2 s: the removal stands, nothing planned |
| A4 control: Undo after 5 s | 85997737 36 | 2/2 | not offered |
| DL: a visit dated today is kept | 85997737 36 | 1/1 | on the visit day a new date leaves both rows |
| DL: a past visit is kept | 85997737 36 | 1/1 | two days later a new date leaves it |
| DL: clock back to the visit day | 85997737 36 | 1/1 | kept |
| DL control: an upcoming row is replaced | 85997737 36 | 2/2 | the check sees a deletion |
| DL: no Remove on the visit day | 85997737 36 | **0/1** | Remove shown: **#187 (S3)**, see Issues |
| A16 control: work listed before | both, 36 | 2/2 | the visit work is in WorkManager |
| A16 delete all data | d29ffb82 36 | 1/1 (85997737's run void: the flow never typed DELETE) | after `delete-everything`: no visit row, no reminder settings, no WorkManager work, no JobScheduler job |

### Screens and accessibility (A15, A17–A24)

| Row | SHA, device | Result | What was seen |
|---|---|---|---|
| A17 labels (tree) | d29ffb82 36 | 1/1 | exactly the copy table: "Next visit, not set. Set a date.", "Next visit, Sunday, November 22, 2026. Change it.", "Remove the next visit", the channel notice |
| A17/A24 TalkBack speech of the labels | d29ffb82 36 | **not proven** | on this image an injected tap activated under TalkBack instead of focusing, so focus speech could not be driven (2 attempts void). The labels TalkBack reads are proven from the tree (A17/A24 labels rows); the "Heading" role is not |
| A15 keyboard | d29ffb82 36 | 3/3 | Tab reaches the row (2 Tabs) and Remove; after Remove focus is on the row; Enter opens the picker |
| A24 card | d29ffb82 36 | labels 1/1, keyboard 1/1 | "Your doctor visit is on Thursday, November 12, 2026.", "Make a report", "Close the visit reminder"; Tab reaches both |
| A18 100/130/200 % portrait | d29ffb82 36 | 3/3 | the merged row node whole on screen; row, Remove and notice do not overlap; at 200 % the value sits under the label ("Wed, Nov 25", shot `A18-2.0-port`) |
| A18 landscape | — | **not proven** | this API 36 image never rotated (`user_rotation`, `wm user-rotation`: screencap stayed 1080x2400 with portrait bounds); 2 runs void |
| A19 notices | d29ffb82 36 | 7/7 | no date: nothing; app off: the line, no button; channel off: the notice, and "Open notification settings" opens the "Doctor visit" channel page; back: gone on resume; both off: the app-level line wins |
| A19 control: everything on | d29ffb82 36 | 1/1 | no line |
| A20 picker | d29ffb82 36 | 4/4 | Done off before a pick (2/2); day +365 enabled, +366 disabled (2/2) |
| A20 200 % landscape | — | **not proven on a device** | same rotation problem, 2 runs void. Held by the instrumented `DatePickerScreenTest` (weekday row, 14 whole day cells and Done at 200 % in an 860×367 dp box, pass) and the engineer's walk W8 on 5570 |
| A21 two taps on Done (~150, ~300 ms), never asked | d29ffb82 36 | 2/2 | the system dialog shows after the wait; the permission stays undecided (no USER_SET) |
| A21 control: a normal Done asks and stamps | d29ffb82 36 | 1/1 | 30-day stamp written after the answer |
| A21 Back during the wait | d29ffb82 36 | 2/2 (1 void) | Back about 100 ms after Done: Reminders closes, no dialog, no 30-day stamp. Back later (two sequential `input` commands, 3/3): the dialog is already starting, Back is dropped, and the dialog shows; the first such run is void because its timing was not measured |
| A22 "Visit removed" placement | d29ffb82 36 | recorded | 100 % portrait: message at y 341–388 px, nothing of the app under it; 200 % portrait: y 328–413, nothing under it |
| A23 card under "Did it help?" | d29ffb82 36 | 1/1 | question at y 1037–1094, card at y 1567–1620 |

### Paywall, report, backup, upgrade (A9, A10, A12, A13, A26)

| Row | SHA, device | Result | What was seen |
|---|---|---|---|
| A9 Free: card tap and notification tap | d29ffb82 36 | Maestro `home-card` 1/1, `visit_reminder` 1/1 | "A report for your doctor"; Close leaves History |
| A9 Premium: card tap and notification tap | d29ffb82 36 | Maestro `premium-report` 1/1, device 1/1 | the Doctor report opens |
| A9 same text for Free and Premium | d29ffb82 36 | 1/1 | identical title and text |
| A10/A26 "Since" chip | d29ffb82 36 | 5/5 | past visit: "Since …" first and chosen, covers "Since your last visit: Oct 20, 2026 to Nov 9, 2026"; a visit last year shows the year ("Since Oct 5, 2025", shot); exactly 24 months: chip; 24 months and a day: no chip, 3 months |
| A10 control: no past visit | d29ffb82 36 | 1/1 | no chip, 3 months |
| A12 upgrade from 1.0.2, 1.1.0, 1.1.1 | d29ffb82 36 | 3/3 | DB `user_version` 6 → 7, attacks, points and tags kept, `upgrade-kept` flow passes, a visit then saves and plans |
| A13 export | d29ffb82 36 | 1/1 | schema 7, `doctorVisits` with id and date only, no device state (`qa/backup_files.py --visits 2`) |
| A13 control: byte check fails | d29ffb82 36 | 1/1 | on an injected `visitRemindedFor` and on schema 6 |
| A13 Add keeps one next visit | d29ffb82 36 | 1/1 | the phone's upcoming visit stays, the file's past visit is added |
| A13 control: Add with no next visit | d29ffb82 36 | 1/1 | the file's upcoming visit is added |
| A13 Replace | d29ffb82 36 | 2/2 | visits = the file's; the reminder planned for its date |
| A13 schema 6 file | d29ffb82 36 | 1/1 | imports, no visit |

### Notification privacy (#178 tasks 4.1, 4.2; A8, A25)

| Row | SHA, device | Result | What was seen |
|---|---|---|---|
| 4.1 `reminder_daily` | d29ffb82 36 | 1/1 | "Not marked. Open Migrely to see today.", no notification says "migraine is logged", every notification private with a public version |
| 4.1 `reminder_effect` | d29ffb82 36 | 1/1 | "Did it help? / Tap to answer in Migrely.", no "Ibuprofen", private |
| 4.1 control on 1.1.1 | v1.1.1 tree | 2/2 fail as required | "A migraine is logged today." and "Did Ibuprofen help?" |
| A25 visit, show all | d29ffb82 36 | 1/1 | "Your visit is on Thu, Nov 12" / "Make a report to bring.", no channel name |
| A25 visit, hide sensitive | d29ffb82 36 | 1/1 | "Reminder" only |
| A25 "Did it help?", show all | d29ffb82 36 | 1/1 | "Did it help?" / "Tap to answer in Migrely.", no medication |
| A25 "Did it help?", hide | d29ffb82 36 | no leak 1/1 | two intakes gave two notifications, which Android bundles: only "Migrely" shows (the "Reminder" line cannot show in a bundle; void as a check of that line) |
| A25 daily update, show all and hide | d29ffb82 36 | 2/2 | "Not marked. Open Migrely to see today." / "Reminder" |
| A8 control on 1.1.1, show all | v1.1.1 tree | 1/1 | "Did Ibuprofen help?" on the lock screen |
| D169 accepted | d29ffb82 36 | recorded | "Mark a pain-free day, or log a migraine." shows under "Show all content" (accepted by D169, #178 F2) |

### Merge rows (the lead's rows on d29ffb82)

| Row | Result | What was seen |
|---|---|---|
| (1) "Visit removed", Undo, then a second message at once | 1/1 (1 void) | "Undone." shows, a second "Visit removed" shows right after, and its Undo works; video `raw/msg1-*.mp4` |
| (2) the same with TalkBack, each message spoken with its full text | 2/2 (1 void) | "Visit removed. Undo removing the visit. Undo. Button" and "Undone." spoken; the same log holds other speech (control). #183's empty announcement was not seen here |
| (3) the home card with a home message | 1/1 (1 void) | "Migraine ended" with "Undo the end" and the card on Home together |
| The date picker once | 4/4 | as A20 above, on d29ffb82 |

### Maestro (task 8.2)

| Row | SHA | Result |
|---|---|---|
| `.maestro/doctor-visit` (row and picker, Remove and Undo, home card, close, Premium report) | d29ffb82 36 | 5/5 (4/4 default pass, 1/1 in the 20 s accessibility-timeout pass) |
| Control on 1.1.1 | v1.1.1 tree | 5/5 fail ("Next visit, .*" not visible) |
| `qa/device_scenarios.sh visit_reminder` | d29ffb82 36 | 1/1 (the first run failed on QA's own regex, a trailing space; fixed) |
| Control on 1.1.1 | v1.1.1 tree | fails at `visit-setup` |


## API 26 (Android 8.0), added 2026-11-10

Run by `qa-engineer` (qa-t21-api26) on `Migrely_QA_API_26` (emulator-5582), en-US, Asia/Tashkent. These are the API 26
rows the first run could not prove (its 5582 rows stay void).

**How the clock was fixed.** The AVD is the AOSP `default` image (`ro.build.type=userdebug`, no Play Store), so `adb root`
works. As root, `service call alarm 2 i64 <epoch ms>` calls `IAlarmManager.setTime`. That is the call `cmd alarm set-time`
makes on newer Android: it sets the clock and broadcasts TIME_SET (logcat shows WorkManager's `RescheduleReceiver` and
the app's receiver get it). The zone is set the same way (`service call alarm 3 s16 <zone>`). On API 26, `cmd alarm` has
no shell implementation, and as the shell user (uid 2000) `setTime` is refused (SET_TIME). No wipe, no snapshot change.
adbd was put back to non-root at the end. The driver is `migrely-work/qa-t21-26/s26.py`, which reuses qa-t21's scripts.

**Builds:** `debug-40172b1f.apk` sha256 5f476810… (byte-identical to `debug-d29ffb82.apk`; `git diff d29ffb82 40172b1f`
changes no product file), `debug-a600ba4f.apk` sha256 74065e8b… (the PR head after the #187 fix; `git diff d29ffb82
a600ba4f` changes product files under `features/reminders/` only), and the 1.1.1 control `debug-eeb6.apk`.
The WorkManager, notification, lock-screen and upgrade rows ran on d29ffb82. The #187 fix does not reach their code:
no worker, notification, DB or lock-screen file changed. The picker and #187 rows ran on a600ba4f.

**Counts** (`qa-t21-26/recount.py`, `logs/rows.jsonl`): **80 of 82 valid runs pass. The 2 fails are the #187 negative
controls on d29ffb82, which must fail.** 9 runs void (below), 0 harness errors.

| Row | SHA | Valid | What was seen |
|---|---|---|---|
| A1 plan | d29ffb82 | 3/3 | visit Nov 15 set on Nov 10 12:00: one `visit` work, due 2026-11-13 10:00 +05, tag `visit-2026-11-15` |
| A1 fires by itself | d29ffb82 | 2/2 | clock to 09:55 on Nov 13, no forced run: posted at **10:00:02** device time both times, nothing before 10:00 |
| A1 content | d29ffb82 | 2/2 | "Your visit is on Sun, Nov 15" / "Make a report to bring.", channel `visit`, `vis=PRIVATE`, public version present, no actions |
| A1 marked, once | d29ffb82 | 3/3, 2/2 | reminded date stored, work gone; jobs forced again: still one notification |
| A1 control: already reminded | d29ffb82 | 3/3 | clock back to 09:55: nothing planned |
| A2 too close (today, tomorrow, +2 after 10:00) | d29ffb82 | 2/2 each | the date is saved; no work, nothing posted after the jobs are forced |
| A2 card | d29ffb82 | 3/3 | "Your doctor visit is on Thursday, November 12, 2026." |
| A2 control: +2 days at 09:00 | d29ffb82 | 3/3 planned, 2/2 arrives | planned for 10:00 today; posted at 10:00:02 |
| A25 visit, Show all / Hide sensitive (PIN) | d29ffb82 | 1/1, 1/1 | "Your visit is on Thu, Nov 12 · Make a report to bring." / "Reminder" only |
| A25 effect, Show all / Hide | d29ffb82 | 1/1, 1/1 | "Did it help? · Tap to answer in Migrely." (no medication name) / "Reminder" only |
| F2 + A25 daily update, Show all / Hide | d29ffb82 | 1/1 each | "Not marked. Open Migrely to see today." (no "migraine is logged") / "Reminder" only |
| A8 control: 1.1.1 under Show all | v1.1.1 tree | 1/1 | "Did Ibuprofen help?" on the lock screen: the leak the fix removes is visible on the old build |
| A20 Done off before a pick (attribute and tap), 100 % and 200 % | d29ffb82, a600ba4f | 1/1 each | the clickable Done container is `enabled=false`; a tap leaves the picker open |
| A20 control: Done on after a pick | d29ffb82, a600ba4f | 1/1 each | `enabled=true` once a day is picked |
| A20 +365 days on, +366 off, a tap on +366 ignored, 100 % and 200 % | d29ffb82, a600ba4f | 1/1 each | Wed Nov 10, 2027 enabled; Thu Nov 11, 2027 disabled and not selected after a tap |
| A12 upgrade 1.1.1 → candidate | d29ffb82 | 2/2 | DB 6 → 7; 2 attacks, 1 intensity point, 1 tag kept; `upgrade-kept` passes; a new visit saves and plans |
| #187 retest (S1 real midnight, S2 background jump, S3 zone change, Remove works and Undo restores) | a600ba4f | 14/14 | see #187; every row asserts the visit is still in `doctor_visit` |
| #187 control | d29ffb82 | S1 0/1, S2 0/1 (intended) | the stale "Remove" is shown after midnight and after the background jump: the bug |

**Void, not counted (harness slips, each rerun):**
- 6 notification rows from the first pass (A1 fires and once, A2 control arrives, A2 too-close ×3). API 26 prints
  `channel=visit:` with a colon, so the harness's channel filter never matched. The too-close passes could not fail, and
  the timing fails were false: A1 marked showed the worker had run. Fixed by stripping the colon; the F2 row's raw
  `channel: "daily:"` is the proof.
- 2 A20 "Done off" attribute rows read the child TextView "Done", which is always enabled. A dump showed the clickable
  parent `enabled=false` before a pick and `true` after. The check now reads the container.
- 1 A12 run lost all records. Disproved as a harness condition (rows `ISO26-*`). On this AVD, when the app's first launch
  after install is Maestro `launchApp: clearState: true`, the next `adb install -r` empties `databases/`, and only that
  folder. This also happens for 1.1.1 installed over itself, with no app process running (wiped 3/3). With one launcher
  launch first: kept 6/6. `pm clear` without Maestro: kept 2/2. Maestro without clearState: kept 4/4. On API 36 the
  same harness kept the data. The cause inside Maestro or installd was not found; no user path reaches it. The run
  still shows the A12 check can fail.

**Controls:** A1 already reminded; A2 +2 days at 09:00 arrives; A8 on 1.1.1; A20 Done on after a pick and +365 vs +366;
#187 S1 and S2 on d29ffb82; A12's voided run shows the count check fails when records go.

**What the API 26 rows do not prove:**
- One emulator image (AOSP arm64, Android 8.0.0), not OEM builds with their own battery or job rules. The clock was set,
  not lived through (except about 75 s across midnight in #187 S1). There was no real reboot.
- The lock-screen privacy rows ran once each (1/1) on API 26. Their wording is checked against the API 36 copy.
- The picker ran in portrait only on API 26. Landscape at 200 % was proven on API 36 only.
- #187's zone-change rows (S3) do not tell the builds apart: d29ffb82 also hides Remove after a zone change. They show
  no regression. The fix itself is shown by S1 and S2.
- The upgrade trap above is unexplained. It is a QA-only path (Maestro clearState as the first launch), but it means
  any future API 26 upgrade row must launch the old build once by the launcher first.

**Device state:** settings diff against `qa-t21/baseline-5582.txt`: 0 lines. A stray `system_locales=en-US` was
removed, and `auto_time` is back to 1 (it boots as 0 on this AVD). No PIN, font 1.0, TalkBack off, Asia/Tashkent.
adbd is non-root and emulator-5582 is off.

## Negative controls

Each check below was shown able to fail:

| Check | Control | Result |
|---|---|---|
| A1 posts once | a date already reminded, clock back to 09:55 | nothing planned |
| A2 too close | +2 days at 09:00 | planned for 10:00, and it arrived |
| A3 move | the same date again | work id and enqueue time unchanged |
| A4 Undo | after 5 s | Undo not offered |
| A7 nothing logged | one entry logged | notification and card appear |
| A10 "Since" | no past visit; 24 months and a day | no chip, 3 months |
| A11 time | no change | unchanged |
| A13 byte check | injected `visitRemindedFor`; schema 6 | `backup_files.py` fails on both |
| A13 Add | no next visit on the phone | the file's visit is added |
| A14 permissions | `SCHEDULE_EXACT_ALARM` added (scratch) | `checkReleasePermissions` exit 1 |
| A16 delete | before the delete | the visit work is listed |
| A19 notices | everything on | no line |
| A21 stamp | a normal Done | asks, and stamps after the answer |
| D168 (13) data-loss rule | an upcoming row | replaced (the check sees deletions) |
| 4.1 texts | 1.1.1 build | `reminder_daily` and `reminder_effect` fail on the old texts |
| 4.2 / A8 lock screen | 1.1.1 build, "Show all content" | "Did Ibuprofen help?" on the lock screen |
| 8.2 flows | 1.1.1 build | 5/5 flows fail, `visit_reminder` fails |
| backup schema check | the seeds (schema 6) against schema 7 | 15 seeds fail until `--schema 6` is given; the self-test passes with it |

Not shown: A12's control (Room `MigrationTestHelper` without `7.json`). The upgrade rows rely on the instrumented
`MigrationTest` passing and on three real upgrades. A20's control (the plain `DatePickerDialog`) and A23's (frames with
animation) were not run.


## Issues

- **#187 (S3), filed by QA:** Reminders offers a dead "Remove" on the visit day when the screen was open before the date
  changed (found on 85997737, reproduced 2/2 on d29ffb82; after a cold start Remove is correctly absent, 2/2). A tap
  does nothing: no message, the visit is kept. No data is lost (the DAO deletes only rows after today).
  `DoctorVisitSection.kt` reads `LocalDate.now()` during composition.
  **Fixed in a600ba4f; retested on API 26 and closed by QA 2026-11-10:** 14/14 (real midnight with the screen open,
  background clock jump, zone change, Remove then Undo); control d29ffb82 shows the stale Remove (S1 0/1, S2 0/1).
- No S1 or S2 found.


## Carried over from 1.1.1's GO, and why the diff cannot reach them

- **Attack logging, attack mode, History, past attacks, medications, overuse warnings, month views, onboarding,
  subscription and paywall states, the report's PDF content, Insights:** no product file of these features changed,
  except History's screen (the one-shot `openReport` entry, A9) and Home's new `visitCard` slot (A23), which are
  checked here. The nightly regression on `develop` covers them.
- **The backup format's other fields:** only `doctorVisits` and the schema number changed. The existing
  `backup_export` and `backup_import` scenarios now check schema 7 (`qa/backup_files.py`) and run nightly.
- **The daily reminder's timing and the "Did it help?" timing:** unchanged code paths. Their texts and lock-screen
  behaviour changed (#178) and are checked here (4.1, 4.2).
- **#180's message host (T53):** its own Go/No-Go belongs to T53's record. Here only the call sites #182 adds were
  rerun on d29ffb82 (rows MSG and TB).

## What these checks do not prove

- **Real phones and OEM battery managers.** Every timing row ran on Google emulator images with the device awake.
  Doze, App Standby buckets and OEM killers (Xiaomi, Samsung) can delay WorkManager past 10:00, or past the visit day.
  The home card is the fallback, and it was checked.
- **Real midnight and real reboots across days.** The clock was moved with `cmd alarm set-time`. "Phone off" was a
  killed process plus a clock jump, not a power-off. A real reboot was not run for the visit work. WorkManager's own
  reschedule after boot is library code, and the daily reminder's reboot row (`reminder_replan`) carries over.
- **The tap from a real lock screen.** The notification was tapped in the open shade on an unlocked phone. The secure
  lock's unlock-then-open path was not driven.
- **Pending, offline and region-unavailable paywall states from the reminder.** The reminder uses the same
  `onReportAction()` gate as History's "Doctor report", which 1.1.1's GO checked. Only Free and Premium were driven from
  the card and the notification here.
- **D110's count after a tapped paywall** (the 5th-migraine paywall still showing) was not driven on a device.
- **TalkBack beyond the named nodes.** Heading navigation was proved by TalkBack speaking "Heading", not by swiping
  through headings. RTL is not applicable: the app is English-only, and neither `ar-XB` nor `iw` mirrors it
  (T53's finding).
- **Animations.** "The card appears and goes without animation" (A23) was not measured frame by frame. The "Visit
  removed" swap was recorded on video (`raw/msg1-*.mp4`), not analysed per frame.
- **Discoverability and real use.** Whether people find the Doctor visit row, and the effect on purchases, are for
  `data-analyst`'s 30-day review.
- **Repo automation.** Most device rows ran from QA scripts in `migrely-work/qa-t21`, which are not in the repo, so
  the nightly regression repeats only the 5 Maestro flows and `visit_reminder` (doctor-visit E2E 12/75 by
  `qa/e2e_coverage.py`).

## Residual risks

- **API 26 (Android 8.0):** now checked on one AOSP emulator image (see its section): a set clock rather than lived
  time, no OEM job rules, lock-screen rows 1/1, the picker in portrait only.
- **(a) The clock set back by 2 or more days** (accepted, D168 (13)), measured on 85997737: clock back to the visit
  day kept a past visit; clock back to the day before it, then a new date, deleted it (`lost: true`). Rare, and the
  report can still use another period. In the register.
- **(b) The import's "too close" mark not written on a full disk** (accepted, design D7): not reproduced. At 156 KB
  free the import itself failed (one attempt, invalid); the case where the import commits and only the mark fails was
  not produced on a device, so the fail-open post (early, but naming the right date) is unmeasured. In the register.
- **#187 (S3):** a stale "Remove" on the visit day does nothing when tapped. No data loss.
- **Back during the permission dialog's start-up is dropped** (A21, 3/3 with Back at roughly 0.5–0.8 s): the dialog
  then shows. This is Android's behaviour, outside D10's wait; the person can still answer or press Back.
- **Landscape at 200 %** is not proven on a device (the image does not rotate); the instrumented picker test holds it.
- **TalkBack focus speech** of the row, heading and card is not proven; their labels are.
- **OEM battery managers and Doze** can delay the reminder; the home card is the fallback (checked).
- **Doctor-visit E2E in the repo is 12/75 scenarios**; most device rows here are QA scripts outside the repo.

