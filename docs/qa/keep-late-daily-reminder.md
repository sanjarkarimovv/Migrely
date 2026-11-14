# QA record: `keep-late-daily-reminder` (#213, #197; BRD D176, D175), task 4

## Decision

**QA's verdict, 2026-11-14 at 09:00: GO for `bugfix/197-replan-after-update` at 620a4f18 (product code = 84d1f86f), with the
risks below open.**

- **No defect found, no issue filed.** Every row met its expectation on the candidate. Every negative control on develop
  74095b8d failed in the way it should.
- **#213 is fixed on both APIs.** The late path (a run held by Doze, with Migrely opened at 20:01) ran 3 of 3 times on
  API 36 and 3 of 3 on API 26. The held run is kept. It comes about 0.3–0.6 s after the tap, while Migrely is opening.
  It does not post, does not mark the date, and is postponed 30 minutes. After HOME it posts once, between 20:31 and
  20:33. On 74095b8d it is lost in 1 of 1 run on each API: the held work is cancelled at the start, nothing posts by 20:45,
  and the next run is in 24 hours.
- **No post while Migrely is on screen.** This held in 23 of 23 runs (on screen, opening, split screen, notification
  shade; both APIs). Staying on screen past the postponed run postpones it again. It posts once after leaving, with or
  without Doze.
- **It posts as before when Migrely is not what the person sees.** This held in 23 of 23 runs (background, screen off, lock
  screen, process dead, clock change, zone change, reboot, update), plus 3 #197 residual runs.
- **At most one post per date.** The app logged one `notification_enqueue` in every run that posted, and none in every
  run that did not. That includes the clock moved back after a post.
- **#197 residual (API 26), D176's premise:** with WorkManager's database gone after the update, the new build posted
  "How was today?" once in each of 3 runs where JobScheduler started the process (Start proc → post within 0.1 s). The
  stale job started the process 0 s, 0 s and 6 min 50 s after R. In a 4th run nothing had started the process by R+5,
  and that run was not watched longer. The plan's "within 5 minutes after R" therefore held in 2 of 4. The lateness
  comes from JobScheduler: at R+5 the job showed `Unsatisfied constraints: TIMING_DELAY` with `earliest=-5m12s`. It is
  not app code, and it applies to any delayed WorkManager run (EC-REM-03, inexact by design). On 74095b8d nothing posts,
  and the next run is in 24 hours (1 of 1).
- **Dropped:** 4.10 (visit and "Did it help?" after the residual on API 26). The diff does not reach `VisitReminderPlan`
  or `LogIntakeUseCase` (both unchanged), and the lead's must-have list does not include it. The #197 closure that cites
  4.9–4.11 should cite D175's earlier evidence for those two.

## Candidate, scope and builds

- **Candidate:** `bugfix/197-replan-after-update` head **620a4f18**, merge base `develop` 74095b8d. `git diff 84d1f86f 620a4f18`
  is `tasks.md` only (7 lines), so the APKs built at 84d1f86f carry the candidate's product code. `develop` has moved on to
  30ac69fa (#212, #214). QA did not test the merge.
- **Scope (owner rule 2026-11-09):** only what the diff reaches:
  - the start-up plan (KEEP);
  - the clock and zone change plan (REPLACE, `due()`);
  - the on-screen postpone in `RunDailyReminderUseCase` and `AppOnScreenDataSourceImpl`;
  - starts after a reboot or an update;
  - reminder off and today answered.

  No whole-app regression was run.
- **Gate:** `android-engineer` ran it on 84d1f86f: exit 0, 3047 unit test results, 0 failed; `:app:checkReleasePermissions`
  exit 0 with the same 6 permissions. QA did not rerun it.
- **Builds** (`android-engineer`'s, in `migrely-work/walks/r2/eng213/apk/`). The sha256 was checked on the host before use.
  For every install by `setup.py` or `rt.py`, it was also checked on the device after `pm path`.

| APK | SHA | sha256 (first 16) | Note |
|---|---|---|---|
| `debug-84d1f86f` | 84d1f86f | c74881584e60fa4b | versionCode 11; dex: `isOnScreen` sets importance 1000 (GONE), calls `getMyMemoryState`, compares with 100 |
| `r8-84d1f86f` | 84d1f86f | ca29a45384e5d5fb | minified, debug-signed (cert 7eae4e48…); the mapping has `AppOnScreenDataSourceImpl.isOnScreen` |
| `debug-74095b8d` | develop 74095b8d | 1f5fb00d0797c27a | negative control; no `AppOnScreenDataSourceImpl` in the dex |
| `r8-74095b8d` | develop 74095b8d | 5144c7297d7ee918 | negative control |
| `r8-9d453bb5` | 1.2.0 | 79a0941932eb839e | the "old" build of the update and residual rows (`migrely-work/qa-120/apk/`) |

  `probe-909561bd.apk` was never installed.
- **Devices:**
  - `Migrely_QA_API_36` (emulator-5580, Android 16): cold boot 2026-11-13 15:15, shut down 03:07;
  - `Migrely_QA_API_26` (emulator-5582, Android 8.0): 03:07 to 08:46.

  Only one ran at a time, both in zone Asia/Tashkent.
- **Evidence root:** `migrely-work/qa-213/`:
  - `results.txt`: every RESULT line, in order;
  - `rows.jsonl`: real-time rows, voids and amendments;
  - `rows-bash.jsonl` and `rows-summary.txt`: `bin/rows.py`;
  - `runs/<label>/`: steps, logcat, jobs and notifications per run;
  - `bin/`, `baseline.txt`, `notes.txt`.

## How the rows were driven

- **Copied scripts.** `bin/` holds the copies of `walks/r2/eng213/bin/` that the plan names (`iter.sh`, `waitpost.sh`,
  `full.sh`, `shown.sh`, `batch.sh`, `ui.sh`), with the serial, base path and uid changed. Every other change is
  measurement or API 26 plumbing:
  - the uid is read from `dumpsys package` (`userId=` on 26, `appId=` on 36);
  - the JobScheduler grep follows each API's dump format;
  - API 26 clears notifications with `am force-stop`, because it has no POST_NOTIFICATIONS;
  - API 26 sets the clock with root `date -u`, because it has no `cmd alarm set-time`. Toybox `date @N` sets the local
    time as UTC; QA found this and fixed it before any row;
  - each run starts with `logcat -b all -c`, so `enqueued=` counts every `notification_enqueue` event of the app in that
    run. A second post with the same id counts too, even though the shade shows one notification.
- **New scripts:**
  - `probe36.sh`: the screen state at the natural 20:00 run, not forced;
  - `waitidle.sh`: stay on screen, HOME, or HOME with Doze holding the run;
  - `clock.sh`: clock forward or zone change while the run is held;
  - `double.sh`, `answered.sh`, `ui213.py`;
  - `rt.py`: real device time with auto time on, for the reboot, normal update and #197 residual rows;
  - `setup.py`: uninstall, install with the hash check, onboarding skipped, daily reminder on at the default 8:00 PM.
- **Fake-clock rows** use one device day each (2027-01-04 onward on 36, 2027-03-03 onward on 26), at 19:59 → 20:00 → 20:45.
- **`shown`** is `reminder_settings.daily_last_shown_date`: read with run-as on debug, and from a root copy on R8.
- **Doze on API 26:** the image ships with deep idle disabled. `deviceidle force-idle` answered "Unable to go deep idle;
  not enabled", and the first two late runs were not held, so they are void (see below). `dumpsys deviceidle enable deep`
  was set for the API 26 rows and disabled again at the end.

## Results

Build "new" = 84d1f86f (debug unless R8 is named), "ctl" = 74095b8d. Times are device times.

| Task | Row | API | Build | Runs | Result |
|---|---|---|---|---|---|
| 4.1 | Late path: held run, tap at 20:01, HOME | 36 | new | 3/3 | 20:01:25 posted=0, date not marked, daily job +29m59s; the run started 0.6 s after `Start proc … for next-top-activity` (GreedyScheduler). Posted once at 20:31:32, 20:31:30 and 20:32:41; date marked; next +23h27–28m; enqueued=1 (2 runs measured) |
| 4.1 | Same | 26 | new | 3/3 | 20:01:23 posted=0, job +29m59s; the run started 0.25 s after `Start proc … for activity`. Posted once by 20:31:47 each; date marked; next +23h28m59s; enqueued=1 |
| 4.1 | Control | 36 | ctl | 1/1 | posted=0 at 20:07 and at 20:45, enqueued=0; `Processor cancelling` the held work at the 20:01 start; next +23h58m |
| 4.1 | Control | 26 | ctl | 1/1 | posted=0 at 20:07 and 20:45, next +23h58m59s |
| 4.2 | On screen from the 20:01 tap (lateon) | 36 | new | 2/2 | posted=0 at 20:07, Migrely on top, job +29m59s, date not marked |
| 4.2 | Stays on screen through the postponed run | 36 | new | 1/1 | at 20:34 still posted=0, enqueued=0, a new job +29m59s (postponed again) |
| 4.2 | Controls: app on its home at 20:00 / in the background | 36 | new | 1/1 + 1/1 | on: posted=0, job +29m59s; background: posted=1, next +24h |
| 4.2 | Same controls | 26 | new | 1/1 + 1/1 | on: posted=0, job +29m59s; background: posted=1 |
| 4.3 | Left after the skip | 36 | new | 2/2 | HOME at 20:34 after a double postpone: posted once at 21:01:31, date marked; HOME + Doze forced until 20:33: posted once at 20:33:04 |
| 4.3a | `am start` and `deviceidle unforce` in one shell command, app stays on screen | 36 | new | 5/5 | posted=0, enqueued=0, Migrely on top, job +29m59s |
| 4.3a | Same | 26 | new | 2/2 | posted=0, job +29m59s |
| 4.3b | Lock screen awake (keyguard over Migrely, power then wake at 19:59:57) | 36 | new | 1/1 | Awake, KeyguardShowing=true at 20:00: posted=1, date marked |
| 4.3b | Lock screen, gone dark by 20:00 | 36 | new | 1/1 | Asleep, keyguard: posted=1 |
| 4.3b | Screen off (power key) | 36 | new | 1/1 | posted=1 |
| 4.3b | Split screen, Migrely top, Phone bottom (both resumed) | 36 | new | 1/1 | posted=0, job +29m59s |
| 4.3b | Notification shade over Migrely | 36 | new | 2/2 | focus NotificationShade at 20:00 (checked in run 2): posted=0, job +29m59s |
| 4.3b | Lock screen awake / lock / screen off / shade | 26 | new | 1/1 each | keyguard (focus StatusBar, Awake) posted=1 ×2; screen off posted=1; shade posted=0, job +29m59s |
| 4.4 | Process dead, the job cold-starts it at 20:00 | 36 | new | 5/5 | posted=1, enqueued=1, date marked, next +23h59m |
| 4.4 | Same | 26 | new | 3/3 | posted=1, enqueued=1 |
| 4.5 | Clock forward 2 min at 20:01 while the run is held | 36 | new | 1/1 | `Start proc … for broadcast TimeChangeReceiver` 20:03:05, the run at once, posted=1, enqueued=1, next +23h56m |
| 4.5 | Zone Tashkent → Dhaka at 20:01 (local 21:01) while held | 36 | new | 1/1 | posted=1 at 21:01, next +22h59m (20:00 Dhaka) |
| 4.5 | Control, clock forward | 36 | ctl | 1/1 | posted=0, next +23h56m |
| 4.6 | Reboot with the run held (real time, R 00:10) | 36 | new | 1/1 | no post or process at R+1; booted 00:11:24; `Start proc … for broadcast RescheduleReceiver` 00:11:32, one post 00:11:32; date marked; next +23h58m |
| 4.6 | Control (R 00:35) | 36 | ctl | 1/1 | no post by R+14; next +23h58m (run lost) |
| 4.7 | Skipped on screen, then today marked pain-free in History, HOME, postponed job forced | 36 | new | 1/1 | posted=0, enqueued=0, date not marked, next tomorrow |
| 4.7 | Reminder off, late path | 36 | new | 1/1 | posted=0, no Migrely job at all |
| 4.8 | Posted at 20:00, clock back to 19:58, waits to 20:05 | 36 | new | 1/1 | ReminderWorker ran twice, 1 notification, enqueued=1, date marked, next tomorrow |
| 4.9 | #197 residual: 1.2.0, clear, reopen, reminder R, update to R8 at R−5 | 26 | r8 new | 4 valid | workdb gone after the update 4/4 ("Mismatched inode value" in installd's log). Posted once at R+0:00, R+0:00 and R+6:50 (enqueue 06:10:00, 08:40:00, 08:06:50) (the stale job's `Start proc … SystemJobService`; "Didn't find WorkSpec"; post within 0.1 s). 1 run: no process start and no post by R+5, not watched longer. Next +24h in each run that posted |
| 4.9 | Control | 26 | r8 ctl | 1/1 | workdb gone; the stale job started the process at R+0:00:00, "Didn't find WorkSpec" ×2, nothing posted, next +23h59m |
| 4.11 | Normal update 1.2.0 → R8, R about 19–21 min after, process dead at R | 36 | r8 new | 1/1 | workdb kept; process started by the job; one post 2 min 1 s after R; next +23h58m |
| 4.11 | Same | 26 | r8 new | 2/2 | workdb kept; one post 12 s and 11 s after R; next +23h59m |

**Voided and harness rows (kept in `rows.jsonl`, not counted):**

- `L26-0306`, `L26-0307`: Doze was not enabled on the API 26 image, so the run was not held. It posted at 20:00 in the
  background. This was the right behaviour, but not the #213 path.
- `LC36-0201`, `C36-fwd-ctl`: the real-time reboot row before them had left the reminder time at 00:35, so no 20:00 run
  existed. Both were rerun after a fresh install at 8:00 PM.
- `rt-36-reboot-new-1`, `rt-26-resid-r8new-4` and `-5`: the time-label check and the picker failed before any update or
  reboot.
- `rt-36-reboot-new-2` was first marked "fail" by a grep that missed the `-v time` format. The events buffer shows exactly
  one enqueue, so it is amended to pass, and the grep is fixed.

## Not proved here

- **Real Doze, real phones and vendor job batching.** Doze was forced (`deviceidle force-idle`). On API 26 deep idle had
  to be enabled first, and Android 8.1–15 have no image. The on-screen check relies on the importance the system reports.
  The 2.9 probes and these rows show 100 for opening, home, split screen and shade on stock Android 8.0 and 16 only.
- **The postponed run's 30 minutes were waited out in real time** in the late-path and on-screen rows. Everywhere else the
  rows used a clock jump or a forced job: 4.5, 4.7, 4.8, and 4.3's Doze variant.
- **Each row type has its limits:**
  - zone change, reboot, double post, today answered and reminder off: 1 run each, API 36 only;
  - the zone change has no control;
  - split screen: API 36 only;
  - the clock moved back to yesterday: not run;
  - travel east past midnight: not run;
  - DST: not run;
  - a postponed run after midnight: not run (the unit tests cover the rule).
- **The R8 build on the on-screen path.** R8 was checked only in background starts (4.9, 4.11). The debug and R8 dex
  share the same `isOnScreen` logic (mapping checked), but no R8 run was on screen.
- **Updates through Play**, and updates from 1.1.1 (the version now live) rather than 1.2.0.
- **4.10** (dropped, see the decision), and Free against Premium (no Premium path in the diff).
- **Accessibility:** no new UI, so none was checked.

## Residual risks

- **R-1 (#197 residual timing):** after a lost WorkManager database, "How was today?" comes when the stale job starts the
  process. On API 26 that was up to 6 min 50 s late, and once more than 5 minutes late (unknown how much). The app reacts
  at once. The plan's "within 5 minutes" depends on JobScheduler. `product-manager` should decide whether the 4.9
  acceptance should read "when the stale job runs" (D175 (2) still holds: recovery without opening the app).
- **R-2 (design R2, store review):** a reviewer who stays in the app sees no reminder at the time. It posts within about 30
  minutes after they leave, as designed (4.3: 21:01 after a double postpone).
- **R-3:** the evidence comes from emulators on a host that slept between 18:30 and 23:03 (lid, "Maintenance Sleep"), during
  rows O36-0124 (the HOME part), O36-0125, C36 and D36. Device time stayed consistent inside each row, and the results
  match the design. The rows were not rerun with the host awake.

## Environment events

- **Host load:** 1.1 to 8.5 at row starts.
- **Host sleep:** about 18:30–23:03 on 2026-11-13, during chain36b. `caffeinate -i -s` ran from 23:45 until the end.
- **Device settings restored:**
  - 5580 was restored to its baseline: auto time on, zone Asia/Tashkent, zen 0, font 1.0, rotation auto, lock screen
    disabled, airplane off, deviceidle unforced, battery reset, adbd unrooted, shut down;
  - 5582 was restored to its baseline: auto time on, zen 3 (as found), font 1.0, rotation auto, lockscreen.disabled 1,
    airplane off, stay-on 1, deep idle disabled again, unforced, battery reset, unrooted, shut down.
- **Installed apps left behind:** 5580 has `debug-74095b8d` (the last control install), and 5582 has `r8-84d1f86f`.
  Both had a 1.2.1 build when QA started.
