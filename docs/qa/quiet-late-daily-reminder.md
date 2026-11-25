# QA record: `quiet-late-daily-reminder` (T61, #213 (c); BRD D183, EC-REM-29, EC-REM-30), task 4 on API 26

## Decision

**API 26, 2026-11-16: GO for API 26, with API 36 pending.** The product code tested is `feature/quiet-late-daily-reminder`
at **ee9094e5**. The branch head a574598d changes only `docs/BRD.md` and `openspec/` (`git diff --stat ee9094e5 a574598d`:
4 files, no code), so this result carries over to it.

- **R1 holds on API 26.** Every late post checked in the shade (16 of 16 head rows) was a full notification: shade entry with the
  app name, "How was today?", "Mark a pain-free day, or log a migraine.", both "Pain-free day" and "Log a migraine",
  and the status bar icon (`ic_notification`, `0x7f0800b4`, visible in SystemUI). It joined androidx's group "silent" and no
  summary was posted, as the Robolectric test predicted. On a locked, secure lock screen that hides sensitive content it
  shows "Migrely · Reminder" with no title and no action (screenshot `runs/h-lock-1/keyguard.png`).
- **Quiet when late, sound when on time.** Every head post more than 60 minutes late logged no `notification_alert`. Every
  post at 0, 30, 59 and 59.75 minutes logged one beep (`[key,0,1,0]`).
- **The check can fail.** develop 50c784e5 logged a beep for each of the same 6 late rows, raised a heads-up on the late
  pop row, and sounded through Do Not Disturb when Migrely was allowed through.
- **No defect found, no issue filed.**
- **API 36 not run.** Rows 4.1, 4.2, 4.3, 4.3b and 4.4 on API 36 wait for host room. emulator-5560 (another project) was
  running and swap was below 2 GB, so 5580 was not booted.

## Builds and device

| APK | SHA | sha256 | Note |
|---|---|---|---|
| `debug-ee9094e5.apk` | ee9094e5 (head) | `f2f03e9eaa5fd7ab03d02a9b90d65a346aac86b9f7c6b99ee75bf1f35340fdaa` | versionCode 11; the hash matched on the device after each install |
| `debug-50c784e5.apk` | develop 50c784e5 (control) | `d9c94e91c16e640d3127b1cea42074cad90c11c6271905869bcbb99bab4d7098` | the same |

- **Build.** QA built both itself, in QA worktrees `migraine-app-qa-t61` (ee9094e5) and `migraine-app-qa-t61c` (50c784e5),
  with `nice -n 19 ./gradlew --max-workers=2 :app:assembleDebug` (exit 0 each).
- **Permissions.** `aapt2 dump badging` of the head build lists POST_NOTIFICATIONS, ACCESS_NETWORK_STATE, BILLING,
  WAKE_LOCK, RECEIVE_BOOT_COMPLETED and DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION. It has no INTERNET.
- **Device.** `Migrely_QA_API_26` (emulator-5582, Android 8.0, `sdk_phone_arm64` userdebug), cold boot 16:38, zone
  Asia/Tashkent.
- **Evidence.** Under `migrely-work/qa-t61/`:
  - `results.txt`: every RESULT line, in order, with the voids;
  - `runs/<label>/`: steps, the events buffer, logcat, `dumpsys notification`, the shade and status bar XML, the SystemUI
    dump, and screenshots where named;
  - `bin/`: the scripts;
  - `baseline-5582.txt` and `appdata-before/`: the state found before the walk.

## How the rows were driven

- **Clock and zone.** API 26 has no `cmd alarm`. The clock is set through `service call alarm 2 i64 <ms>`
  (`IAlarmManager.setTime`), which broadcasts TIME_SET like a person changing the clock. The zone is set through
  `service call alarm 3 s16 <zone>`. Auto time and auto zone were off for the fake-clock rows.
- **Fake-clock rows.** Each row uses its own date (2027-02-01 onward on the head, 2027-04-01 onward on the control). It
  goes: force stop (clears notifications), clock to 19:00 (19:59 for an on-time row), open Migrely, HOME, then either the
  clock to the target time (the D176 catch-up run) or wait for the natural 20:00 run.
- **Sound.** The signal is the system's own event log tag `27532 notification_alert (key|3),(buzz|1),(beep|1),(blink|1)`,
  which exists on this image. `alerts` counts Migrely's lines after `logcat -b all -c` at the start of the row.
  "Lateness" is the `notification_enqueue` time minus 20:00 on that date.
- **Do Not Disturb.** DND was on as found (manual "Alarms only", ringer silent). On API 26, `settings put global zen_mode 0`
  does not take effect, so DND is set through Quick Settings' detail panel (`bin/dnd.sh`). Every sound row ran with DND off,
  the ringer normal and the notification stream unmuted, which the script checks.
- **Channel settings.** Importance and "Override Do Not Disturb" for the "daily" channel were set in Android's channel
  settings screen (`bin/chan.sh`, `bin/bypass.sh`).
- **Heads-up (4.6).** SystemUI's `headsUpShowing` was polled about once a second for 20 s after the post. `other_enq` is the
  number of other apps' `notification_enqueue` lines in the row; it was 0, so the heads-up is Migrely's. A screenshot was
  taken at first sight of the post.

## Results (API 26)

Head = ee9094e5; ctl = develop 50c784e5. "Full" means: the shade has the app name, title, text and both actions; the status
bar icon is visible; the enqueue shows `actions=2 vis=PRIVATE` with a public version.

| Task | Row (spec scenario) | Build | Runs | Result |
|---|---|---|---|---|
| 2.8a, 4.1 | Clock moved forward, T+2 h (22:00) | head | 3/3 | posted once, full, `groupKey=silent`, `sound=null vibrate=null`, alerts 0, date marked, next run tomorrow |
| 2.8c, 4.1 | Same, control | ctl | 2/2 | posted, full, no group, **alerts 1 (beep)** |
| 2.8b | Clock to T+30 min | head | 2/2 | lateness 30.0 min, no group, alerts 1 |
| 4.1 | About on time (natural 20:00 run) | head | 2/2 | lateness 0.0, alerts 1 |
| 4.1 | Boundary T+59 min | head | 2/2 | lateness 59.0, alerts 1 |
| 4.1 | Boundary T+61 min | head | 2/2 | lateness 61.0, `groupKey=silent`, alerts 0, full |
| 4.1 | Boundary T+61 min, control | ctl | 1/1 | alerts 1 |
| 4.1 | Tight bracket 59:45 / 60:15 | head | 1/1 each | lateness 59.75: alerts 1; lateness 60.25: silent, alerts 0, full |
| 4.1 | Clock moved back after a quiet post (23:00, then 19:58, watched to 20:03:30) | head | 1/1 | 1 notification, 1 enqueue, alerts 0, date marked, next tomorrow |
| 2.8a | "Pain-free day" tapped on a quiet post | head | 1/1 | `day_mark(date).pain_free = 1`, notification gone, alerts 0 |
| 4.2 | Time zone change after the time (Tashkent 19:00 → Tokyo, local 23:00) | head | 2/2 | lateness 180.2 min, silent, alerts 0, full |
| 4.2 | Same, control | ctl | 1/1 | alerts 1 |
| 4.2 | DND on ("Alarms only", as found): on time and late | head | 1/1 each | both posted and full; alerts 0 for both (Android's rule); the late one silent |
| 4.2 | Migrely allowed through DND ("Priority only" + "Override Do Not Disturb" on "daily") | head | on time 1/1, late 2/2 | on time **alerts 1** (the exception works); late alerts 0, silent, full |
| 4.2 | Same, late, control | ctl | 1/1 | **alerts 1**: develop sounds through DND |
| 4.2 | The person's channel settings: "daily" set to Medium "No sound" (API 26's silent; added at cr-t61's request) | head | on time 2/2, late 2/2 | all posted, full; alerts 0 for all; the late ones also `groupKey=silent`. Restored to "High: Make sound" |
| 4.2 | Lock screen: PIN, "hide sensitive content", screen off, late post, wake | head | 1/1 | keyguard: "Migrely" + "Reminder", no title, no action; alerts 0. PIN, lock and private-notification settings restored |
| 4.2 | "Pain-free day" on a quiet reminder with an attack touching today | head | 1/1 | update "Not marked. Open Migrely to see today." in the shade, 2nd enqueue, alerts 0 |
| 4.2 | Reboot after the time (real time) | head | **NOT RUN** (2 void attempts) | See "Reboot row" below. Unit coverage only. |
| 4.6 | "daily" raised to Urgent "Make sound and pop on screen": on time | head | 4/4 | alerts 1 in 4 of 4; heads-up shown in 3 of 3 runs with a working probe (run 1's probe matched nothing and was fixed; `headsUpShowing: true` at 17:07:48 in run 2, in 179 and 182 polls in runs 3 and 5; screenshot `runs/h-pop-ontime-5/hu.png`) |
| 4.6 | Same, late (T+2 h) | head | 3/3 | no heads-up (0 polls; screenshot `runs/h-pop-late-3/hu.png` shows only the status bar icon), alerts 0, silent, full |
| 4.6 | Same, late, control | ctl | 1/1 | **heads-up shown** (165 polls), alerts 1 |
| 4.3b | Screen wake | - | - | API 36 only; not run here |

Totals for API 26:
- **head:** 35 valid rows, 0 failed (the reboot row is NOT RUN);
- **control:** 6 rows, 6 alerted as develop should;
- **voids:** 3, not counted.

**Voids (kept in `results.txt`, not counted):**

- `h-silent-ontime-1`: harness. `row.sh` was edited in place while the row ran, and bash failed with a syntax error.
  The row was rerun as `h-silent-ontime-3`.
- `h-pop-ontime-4`: harness. The date was 2027-03-01, after a shown 2027-03-03, so the plan was correctly +3 days, to
  03-04. It was rerun as `h-pop-ontime-5` on 03-05. `h-pop-late-3` (03-02) followed it and is counted: 03-02 had never been
  shown, the catch-up run posted for it, and the post was silent, full and raised no heads-up.
- `rb head 1` and `rb head 2`: see "Reboot row" below.

**Not a valid single-run check:** exactly 60:00 minutes. A post cannot be timed to the millisecond on a device. The device
brackets it at 59.75 (alert) and 60.25 (quiet); `RunDailyReminderUseCaseTest` and its `>=` mutation prove the exact value.

## Reboot row (4.2 "Reboot after the time"): NOT RUN on a device

The fake clock does not survive `adb reboot` on this image: it came back as 1970-01-01. So the row needs real time: the
reminder at R, the run held for more than 61 minutes, then a reboot. Both attempts were void before any reboot happened:

- **Attempt 1:** `am set-inactive` did not hold the job (`get-inactive` answered `Idle=false`). It was stopped by SIGINT
  before R (17:50).
- **Attempt 2:** forced deep Doze with `device_idle_constants idle_to=7200000` held the run at R+1, with nothing posted and
  no process. At 18:32:00 a TIME_SET broadcast started Migrely's `TimeChangeReceiver`, most likely auto-time sync, since
  auto time was on for the real-time row. The catch-up ran in-process, so Doze did not hold it. It posted at 18:32:00.5,
  42 minutes late, with a beep (`notification_alert [..,0,1,0]`) and no group. That is right under the rule, because 42
  minutes is not more than 60, but it is not the reboot path. At R+62 the row was void ("not held to R+62"), and no reboot
  was done.

**Team-lead decision, 2026-11-16 22:10:** there will be no third real-time attempt today without a harness fix shown to
hold, and none exists. Turning auto time off during the hold might avoid the broadcast, but it is not shown to hold to
R+61.

**What covers it instead:** the reboot path reaches the same `RunDailyReminderUseCase` after the run starts (WorkManager
restores the overdue run). The unit tests in `RunDailyReminderUseCaseTest` are:
- "Given a catch-up after a reboot or clock change at 11 PM when run then shown quietly";
- "Given an old run without a date at 11 PM when run then shown quietly";
- "Given Doze held the run until 11-10 PM when run then shown quietly and today marked".

On the device, the same use case and post path were shown quiet by the clock-forward and zone rows. The D176 record shows,
on API 36, that a reboot after the time posts once.

**Not proved:** that a run restored after a real reboot on API 26 or 36 posts quietly. This is a deferred risk for the
release/1.3.0 Go/No-Go, the same as the API 36 rows.

## Not proved here

- **API 36.** Nothing in this record is evidence for Android 16. The group trick's display there is R1's other half.
- **Real sound and vibration.** The emulator runs with `-no-audio` and has no vibrator. `notification_alert` beep=1 means
  the system decided to play the channel sound; whether a speaker plays it is not observed. buzz was 0 in every row,
  because the channel has no vibration on by default. So "no vibration" on a late post is shown by `vibrate=null` and
  Android's group rule, not by a buzz that went away.
- **Real Doze, real phones, vendor skins.** The late path was driven by clock and zone changes (the D176 catch-up). The
  Doze-held run (4.5) and the reboot row were not run.
- **TalkBack (4.3), "Did it help?" and the visit reminder late (4.4), and the channel list (4.4).** These are API 36 rows.
  The diff does not touch those posts (`git grep setSilent` = one production line).
- **A "daily" channel turned off.** Not tested; it is Android's own behaviour (the spec says so).
- **Free versus Premium.** No device row; `RunDailyReminderUseCase` takes no entitlement input.

## Residual risks

- **R1 on API 36** is open until the API 36 rows run.
- **Reboot after the time** is not proved on any device (above).
- **Observation, not filed (outside this diff):** after the clock was moved back across dates past an already shown date,
  the plan pointed to the day after that shown date, while a clock-forward catch-up on an unshown earlier date still posted.
  This was seen once, in harness voids. It is pre-existing D176 behaviour and does not involve the quiet rule.
- **R2** (people who rely on vibration) and **R3** (watches, flash notifications) stay accepted design risks; not
  measurable here.

## Device state at the end

Device state at the end, 22:16. emulator-5582 was restored to `baseline-5582.txt`, then powered off with `emu kill`.

- **Settings:** all 18 recorded settings are equal to the start, including two that are away from the qa/lib.sh baseline
  and were kept as found: `font_scale 2.0` and `zen_mode 3`. Do Not Disturb is a manual "Alarms only" rule with no end
  time, the ringer is silent, the zone is Asia/Tashkent, auto time and auto zone are on, and the device clock equals the
  host's. Deep and light idle are disabled, `device_idle_constants` is null, battery is AC, there is no lock credential,
  and `lockscreen.disabled` is 1.
- **App:** 1.2.1 (`base-1.2.1.apk`, sha256 `aff4e8b8…`) is reinstalled, and its data is restored from the tar taken at
  16:38. The 10 CE files and the DE files are sha256-equal. Owners are remapped to the new uid 10110, and SELinux labels
  are restored. The app was not launched afterwards.
- **What is not as before:**
  - `firstInstallTime` is new;
  - the uid is 10110, not 10106;
  - Migrely has no notification channel until the app next runs. At the start it had one, "visit", with all defaults
    (`mUserLockedFields=0`), which 1.2.1 recreates the first time Reminders opens;
  - the system's notification archive and `AppSettings` keep entries for the uninstalled test installs (uids 10107–10109).
