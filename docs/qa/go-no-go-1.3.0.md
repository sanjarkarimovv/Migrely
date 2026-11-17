# Release Go/No-Go: Migrely 1.3.0 (versionCode 12)

## Decision

**QA's verdict, 2026-11-17 04:30: GO, with the residual risks below open, for `release/1.3.0` @
294c10c60c5a268f901a554f95b14827ffa0e604** (code 2f7745c3; the head adds only `docs/release/whats-new.md`).

- **No open S1 or S2** (`gh issue list --state open`, 2026-11-17). Three new S3 issues were filed from this check; none
  blocks a release by the Go rule, and each is stated here so that shipping with it is a known choice:
  - **#238 (S3, accessibility) is a regression in 1.3.0:** with TalkBack on, closing the keyboard on a migraine's note moves
    TalkBack's focus to the "Stress" chip (3 of 3); 1.2.1 stays on the note (2 of 2).
  - **#237 (S3):** after a storage-full failure and freed space, a successful medication log still shows "Couldn't save"
    over "Medication taken". Production 1.2.1 does the same.
  - **#239 (S3, accessibility):** Edit medication's Save with a name error says nothing under TalkBack and leaves the error
    off screen (ux F5, T63 8.3). 1.2.1 not run.
- **R1 (T61 stop condition) holds on API 36:** every late daily reminder (9 of 9) posted in full, with its status bar
  icon, and silent; on-time posts sound; the develop control sounded 4 of 4.
- **#217 is fixed and closed:** TalkBack reads "Skip this step. Button" once on all four onboarding steps (1.2.1: "Skip this
  step. Skip. Button").
- **The update from production 1.2.1 keeps everything** on API 36 and API 26: the database, the pending "Did it help?",
  the next day's daily reminder, and (API 36) the 1.2.1 backup imports into 1.3.0 with every count equal.
- **The R8 build:** identity vc12/1.3.0, permissions equal to 1.2.1, the same signer as QA's 1.2.1 build, no debug-only
  component, smoke and monkey pass on both levels; instrumented tests 227/227 on API 36 and 218/219 on API 26 (the one
  failure is the known #229, S4, on develop too).
- **Not proven, accepted by team-lead:** the T61 reboot row (VOID); see "What these checks do not prove".
- **The blocking API 36 rows of T63 and T66** ran; what they could not show (F1's control on Gboard, the visit section's
  name, 200% landscape Privacy) is listed below.

## Candidate, scope and builds

- **Verdict SHA:** `release/1.3.0` @ **294c10c60c5a268f901a554f95b14827ffa0e604**. It is 2f7745c3 plus
  `docs/release/whats-new.md` only (`git diff --name-only 2f7745c3 294c10c6` lists that one file, checked by QA at 23:40).
  2f7745c3 is `develop` e46f2b22 plus the version bump in `app/build.gradle.kts` (versionCode 11 → 12, versionName 1.2.1 →
  1.3.0). Production is 1.2.1 (vc11, `v1.2.1` = `main` 5a15b3f8).
- **What 1.3.0 ships (PRs merged since `v1.2.1`):** #207 (debug-only components out of the release manifest, #195), #208
  (safety copy, #200), #211 (medication ratings copy, #205), #212 (Home outcome line, #209), #216 (keep a late daily
  reminder after an update, #197, D176), #218 (onboarding goal wording, #192), #221 (messages everywhere, D178, #181 #183
  #185 #88 #153), #225 (keyboard follows the cursor, D180, #141 #83 #145), #231 (privacy wording and focus, D181, #217),
  #235 (quiet late daily reminder, D183, T61), plus site and listing PRs that do not reach the app.
- **Scope (owner rule 2026-11-09):** what `v1.2.1..294c10c6` reaches and what no earlier record proved on a device:
  1. the API 36 rows each change's QA record left as a blocking line for this release: T63 8.1 and 8.3
     (`fix-keyboard-follows-cursor.md`), T66 2.5, 3.6, 4.1/4.3 History, 5.5 and the #217 retest
     (`fix-privacy-wording-and-focus.md`), T61 4.1 to 4.4 (`quiet-late-daily-reminder.md`);
  2. #221's hosts that were never run on a device, chosen by data risk (below);
  3. the update from production 1.2.1 with data and pending reminders kept, on API 26 and API 36;
  4. the R8 release build on both levels, the release manifest, instrumented tests per module, and Lint.
  Everything else carries over from the change records and the 1.2.1 GO (see "Carried over").
- **Builds** (QA's own, detached worktree `migraine-app-qa-rel130` at 2f7745c3, clean before and after; no
  `keystore.properties`, so the release build is signed with the debug key, D102, the same signer as QA's 1.2.1 build):

  | APK | Source | sha256 | Use |
  |---|---|---|---|
  | `r8-2f7745c3` | candidate code | `c91b0854517d8ce3…` | every R8 row; read back from the device before each row |
  | `debug-2f7745c3` | candidate code | `782cf3d3e80efc1a…` | rows that need `run-as` (keyboard, messages, reboot) |
  | `r8-v121-5a15b3f8` | `v1.2.1` tree (QA's qa-195 build) | `7f41f34c13a717ae…` | update base; #217 control |
  | `debug-50c784e5` | `develop` before T61 (has #216) | `d9c94e91c16e640d…` | T61 negative control |
  | `app-debug-76e5afcd` | T63 head with finding 1 | `78cb48c0bfe3487c…` | F1 negative control |

  Build: `nice -n 19 ./gradlew --max-workers=2 :app:assembleDebug :app:assembleRelease :app:checkReleasePermissions` →
  BUILD SUCCESSFUL in 2m 2s, exit 0 (`migrely-work/qa-rel130/logs/build-2f7745c3.log`).
- **Devices:** `Migrely_QA_API_36` (emulator-5580, Android 16 google_apis, Gboard) and `Migrely_QA_API_26` (emulator-5582,
  Android 8.0), one at a time, cold-booted. For the soft-keyboard rows 5580 ran with `hw.keyboard=no` (restored after).
- **Evidence** (outside the repo): `migrely-work/qa-rel130/` (`HANDOVER.md`, `rows.jsonl`, `t61/`, `k36/`, `logs/`,
  `shots/`, `bin/`).

## Static rows on the candidate

| Row | Result |
|---|---|
| Identity (`aapt2 dump badging`) | `com.migrely.app`, versionCode 12, versionName 1.3.0; R8 not debuggable; debug debuggable |
| Permissions | `aapt2 dump permissions` of the R8 APK equals 1.2.1's R8 APK: POST_NOTIFICATIONS, ACCESS_NETWORK_STATE, BILLING, WAKE_LOCK, RECEIVE_BOOT_COMPLETED, DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION; no INTERNET. `:app:checkReleasePermissions` exit 0 |
| Signer | the 1.3.0 R8 and 1.2.1 R8 APKs share certificate SHA-256 `7eae4e48…`, so the update path is valid |
| Manifest diff `v1.2.1..294c10c6` | only `android:windowSoftInputMode="adjustResize"` on `MainActivity` |
| `qa/release_apk.py` | release 0/0/0, not debuggable; control: the debug APK has the simulated store 57/2/4 |
| Lint (line 1a), `./gradlew lint --continue` at 2f7745c3 | exit 0, 0 errors. Warnings: app 50 (Gradle, dependency versions, UseKtx), designsystem 1 (+2 hints), attack 4, history 2, home 5, medication 2, reminders 1, report 1. Three could reach a user and all three predate 1.2.1: MutableCollectionMutableState (the list is replaced, never mutated), PluralsCandidate "%1$d out of 10", InlinedApi POST_NOTIFICATIONS. No new warning with a user-visible effect |
| Open defects (line 8) | `gh issue list --state open`: no S1 or S2 |

## T61 quiet late daily reminder on API 36 (tasks 4.1 to 4.4; R1 is a stop condition)

Driver: `bin/row36.sh` (a port of the API 26 `row.sh`): `cmd alarm set-time` (it broadcasts TIME_SET), a reminder at
8:00 PM, each row on its own date. Read from the raw files by `bin/t61sum.py`.

**API 36 logs `notification_alert` for every post**, with the fields key, buzz, beep, blink, politeness and mute reason.
On API 26 a line appeared only when the post made a sound. So here "sounded" means buzz or beep equal to 1. "Full" means:
the shade shows the app name, "How was today?", "Mark a pain-free day, or log a migraine." and both actions, and
SystemUI's dump shows `StatusBarIconView(slot='com.migrely.app/…' … visible … visibleState=ICON)`.

| Task | Row | Build | Runs | Result |
|---|---|---|---|---|
| 4.1 | Clock moved forward, T+2 h | R8 candidate | 3/3 | posted once, full, `flags=AUTO_CANCEL\|SILENT`, beep 0 buzz 0 (mute reason 2048), date marked, next run tomorrow |
| 4.1 | Same, negative control | develop 50c784e5 | 2/2 | posted, full, no SILENT flag, **beep 1** |
| 4.1 | About on time (natural 8:00 PM run) | R8 candidate | 2/2 | lateness 0.0, no SILENT flag, **beep 1** |
| 4.1 | Boundary T+59 min | R8 candidate | 2/2 | lateness 59.0, beep 1 |
| 4.1 | Boundary T+61 min | R8 candidate | 2/2 | lateness 61.0, SILENT, beep 0, full |
| 4.1 | T+61, negative control | develop 50c784e5 | 1/1 | beep 1 |
| 4.1 | Clock moved back after a quiet post (11 PM, then 7:58 PM, watched to 8:03:30 PM) | R8 candidate | 1/1 | one post, one enqueue, date marked |
| 4.2 | Time zone change after the time (Tashkent 7 PM → Tokyo, local 11 PM) | R8 candidate | 2/2 | lateness 180.2, SILENT, beep 0, full |
| 4.2 | Same, negative control | develop 50c784e5 | 1/1 | beep 1 |
| 4.2 | Lock screen: PIN, "hide sensitive content", screen off, late post, wake | R8 candidate | 1/1 | the lock screen shows "Reminder · now" only, no title, text or action (screenshot `t61/runs/h-lock-1/keyguard.png`); beep 0; PIN and setting restored |
| 4.2 | "Pain-free day" tapped on a quiet post | R8 candidate | 1/1 | `day_mark(2027-03-02).pain_free = 1`, the post gone, no sound |
| 4.2 | Do Not Disturb on (`cmd notification set_dnd priority`), late | R8 candidate | 1/1 | posted, beep 0 (mute reason 512); DND off after |
| 4.2 | Reboot after the time | debug candidate | VOID | see Voids; team-lead allowed recording it VOID |
| 4.3 | TalkBack, screen on: arrival | R8 candidate | 1/1 | record only: TalkBack spoke "How was today? Mark a pain-free day, or log a migraine." when the quiet post arrived |
| 4.3 | TalkBack, screen off: arrival | R8 candidate | 1/1 | record only: nothing spoken; the screen stayed asleep |
| 4.3 | TalkBack in the shade (console touch on the post, then next ×8) | R8 candidate | 1/1 | **pass**: "expanded. Migrely. Bullet. now. How was today?. Mark a pain-free day, or log a migraine.", then "Collapse. Button", "Pain-free day. Button", "Log a migraine. Button" |
| 4.3b | Screen off, late post and on-time post | R8 candidate | 1/1 each | record only: the screen stayed off for both (`mWakefulness=Asleep`, `mScreenState=OFF`). The emulator has no always-on display; "wake screen for notifications" was not set |
| 4.4 | Visit reminder 90 min late (visit set through the UI) | R8 candidate | 1/1 | posted, **beep 1**: the quiet rule did not spread to it |
| 4.4 | "Did it help?" 90 min late | R8 candidate | 1/1 | posted "Did it help?" at once after the clock change, `flags=AUTO_CANCEL` (no SILENT), importance 3. The `notification_alert` line was not captured (the log was cleared after the post), so the sound itself is inferred from the flags |
| 4.4 | The app's channels | R8 candidate | 1/1 | on that install `effect` and `visit`; Android creates a channel the first time the app posts to it (as in 1.2.1, see the T61 API 26 record), so `daily` appears after a daily post (it did in every 4.1 row) |
| 4.4 | Permission report | | | equal to 1.2.1 (static rows) |

Totals for T61 on API 36: candidate 24 valid rows (plus 4 record-only), 0 failed; negative controls 4 of 4 sounded.

**Why the control is develop 50c784e5 and not 1.2.1:** production 1.2.1 posted nothing when the clock jumped past 8 PM
(`c-fwd2h-1`: no post in 7 min, the date not marked, the next run planned for the next day). That is 1.2.1's behaviour from
before #216 (D176), not a 1.3.0 finding, but it cannot show a sound, so the controls were run on develop from just before
T61, as in the API 26 record.

## T66 privacy wording and focus on API 36

TalkBack 16 on 5580, log level VERBOSE (it was ERROR after the boot; set back to ERROR at the end). Focus moved by
emulator-console touches and swipes (`input` does not reach TalkBack on this image). Each control was focused by a touch,
and what TalkBack said was read from its log (`bin/tb217b.py`, `bin/tbpriv.py`).

| Task | Row | Candidate (R8) | Control: 1.2.1 R8 |
|---|---|---|---|
| 3.6, #217 | Onboarding steps 1 to 4, "Skip this step" | **"Skip this step. Button"** on all 4 steps | "Skip this step. Skip. Button" on all 4 (the #217 defect) |
| 3.6 | Home attack mode | "Undo the start. Button", "Details of this migraine. Button", "Move the start 15 min earlier. Button" | "Undo the start. Undo. Button", "Details of this migraine. Details. Button", "Move the start 15 min earlier. 15 min. Button" |
| 3.6 | Reminders, a visit set | "Remove the next visit. Button" | "Remove the next visit. Remove. Button" |
| 3.6 | Reminders, notifications off: the notice's "Open settings" | "Open settings. Button", once (its spoken name equals its text) | |
| 2.5 | Privacy page at 100% | title "Privacy. Heading", then 7 stops in order, each once: the 4 legal paragraphs, "Delete all data, at the end of Settings…", "Android's own backup of Migrely is replaced…", "Read the full privacy policy. migrely.health/privacy. Button"; then Back | |
| 2.5 | 200% in a 360 dp window (`wm size 945x2100`), portrait | the same 7 stops in order, each once; every one scrolled into view by TalkBack | |
| 2.5 | 200% landscape | not run: the driver did not find the Privacy row in landscape Settings, twice | |

Observation, not filed: at 200% (360 dp), after the last stop TalkBack wraps to Back and the title, and the next swipe
lands on the fourth paragraph (the first one on screen), not the first. At 100% it lands on the first. That is TalkBack's
wrap on a page scrolled to its end; it repeats nothing and hides nothing on the first pass.

## T63 keyboard follows the cursor on API 36 (8.1's API 36 rows, 8.3)

Device: 5580 with `hw.keyboard=no`, Gboard full keyboard (top 1517 px portrait, 394 px landscape) after its stylus
writing was turned off (see Voids). Debug candidate (`782cf3d3…`), data seeded through the UI (two migraines, Ibuprofen),
the note reset by SQL before each row. Drivers: `qa-kbd8/cell.py`, `away8.py`, `f2row.py` and QA's copies in
`qa-rel130/bin` (`gbaway.py`, `k4.py`, `k4x.py`, `tbkbd3.py`). C1 to C5 as in the T63 record.

| Row | Candidate | Control |
|---|---|---|
| F1 note, close the keyboard after scrolling away, last word **composing on Gboard's keys** ("aura once", Gboard suggests "once"/"one"), 100% P | **PASS 3/3** (top node unchanged; the note not pulled back) | 76e5afcd (finding 1): **also stayed 1/1**. The row cannot fail on Gboard/API 36: finding 1 does not show there. It rests on the API 26 rows (control moved 2/2) and the 7.4 test |
| F1, text injected with `input text` (100% P ×2, committed ×1, 200% P ×1) | PASS 4/4 | 76e5afcd stayed 1/1 (no composing word); no regression shown, not decisive |
| F1 200% landscape (real rotation) | PASS 1/1 | |
| F1 reopen by a tap after each run | the note's line and counter above the keyboard (crop `k36/f1c-1-reopen-crop.png`) | |
| F2 Edit medication's name, 200% portrait | not decisive: the page is only ~60 px taller than the viewport once the keyboard closes, so the scroll is clamped and the name returns into view whatever the code does (as in the API 26 record) | |
| F2 the D167 safety card after a first 10, note focused, keyboard up (Gboard keeps it up through the dialog) | **PASS 1/1**: card and Got it inside the viewport at 2.9, 4.9, 7.2 and 9.3 s, the note not pulled back. First run of this path on a device (on API 26 the dialog hid the keyboard) | not run |
| U9 cells, portrait: F2 100%/200%, F3 100%/200%, F4 100%/200%, F5 200%, F1 100% in a 360 dp window | C1 pass (input line, and the supporting line where it fits, above the keyboard; judged on crops), C2 pass (Back's node unchanged; the first tap with the keyboard up leaves) in every cell that measured it (11 of 11, counting the "L"-labelled runs that stayed in portrait), C3 pass, C4 the control below reachable (Save, "Add and log", "Delete everything" as D7), C5 no blank space | 1.2.1 debug (run by a harness slip, see Voids): F3 100% P **Back and title `[0,0][0,0]`, the window panned** (#83), and F1 200% pulled back |
| U9 cells, landscape (real rotation): F3, F2, F5 at 100%; F3 100% ×2 more | C1, C2 (Back 116–179, first tap leaves), C3 pass, 5/5 | |
| 360 dp window, F1 200% | not run (the driver did not find "Opens History" in that window) | |
| K4 a message and the keyboard, 100% P | K4a pass: the storage-full error under the bar (328–433), Back unchanged, above the keyboard. After the keyboard closes the error stays (screenshots, 3 runs) | |
| K4, 200% landscape | **PASS a to e**: error shown (the keyboard covers its lower part, accepted U5); stays, same bounds, when the keyboard closes and opens; a sideways swipe dismisses it; with a new error and the keyboard up, the first tap on Back leaves | |
| K2 TalkBack reaches Back with the keyboard up (from the note, TalkBack swipes to previous items) | **PASS 1/1**: "Back" reached after 34 swipes with the keyboard still up; the double tap leaves the screen. (Touches near the bar did not focus Back in 2 earlier runs; harness, see Voids) | 1.2.1 debug: also reached (24 swipes), so on API 36 this row shows no regression, not a fix |
| U8 TalkBack on, keyboard up on the note, Back | the keyboard closes and the screen stays, but **TalkBack's focus jumps to the first trigger chip: "Not selected. Stress"**, 3 of 3 → **#238 (S3, accessibility, a 1.3.0 regression)** | 1.2.1 debug: stays on the note ("qa … Edit box. 2/1000"), 2 of 2 |
| 8.3 (ux F5) and T66 5.5: Edit medication, name emptied, keyboard up, TalkBack to "Save. Button", double tap | **reproduces**: nothing about the error is spoken in 8 s, the keyboard stays, and "Enter a name." stays under Name, off screen (proved present by scrolling up, run r4) → **#239 (S3, accessibility)** | 1.2.1 not run |

## #221 messages: hosts never run on a device

The #221 record ran 5 of 13 hosts on a device. The diff moved every host onto the shared message host, so the hosts where a
message stands between the person and their data were run here: A5 (Log medication) and A6 (Add past migraine, Edit
medication), each with `/data` full, then space freed (`bin/msgrows.py`, debug candidate). Paywall, Doctor report, Your
data, My medications, Onboarding: not run (no diary write rides on their message; unit tests and the shared host).

| Row | Candidate | Control (1.2.1 debug, harness slip) |
|---|---|---|
| A6 Add past migraine: disk full → Add migraine → error on that screen; space freed → Add migraine | **PASS**: the error shows and the screen stays; the second press saves (attacks 2 → 3), the screen closes to the new migraine's details with no error | |
| A6 Edit medication: name + "Q", disk full → Save → error; freed → Save | **PASS**: error shown, screen stays; then saved ("IbuprofenQ"), closes to My medications, no error | |
| A5 Log medication: disk full → Log → error; freed → Log again | **FAIL 2/2**: the intake is stored (1 → 2) and the screen turns to "Medication taken" with Done and Undo, but "Couldn't save. Your phone's storage is full." stays over it at 2 s and at 8 s → **#237 (S3)** | the same, 1/1: it predates 1.3.0 (#221's A5 row had never run on a device) |

## Update from production 1.2.1

Driver `bin/uprel.py` (from the 1.2.1-era `qa-195/up195.py`): install 1.2.1 R8 (`7f41f34c…`) fresh, seed through the UI
without any clear (2 migraines, Severe, Nausea, a custom symptom, Ibuprofen with intakes at 09:00 and 09:05, the daily
reminder on), export a 1.2.1 backup, let the system kill the app, check installd's code_cache mark, then `install -r` the
1.3.0 R8 (`c91b0854…`, hash checked on host and device) **and do not open it**.

| Row | API 36 | API 26 |
|---|---|---|
| code_cache mark true before the update | pass | pass |
| Database kept: schema 7 → 7, every table's row count equal (attack 2, attack_tag 2, custom_tag 1, intensity_point 1, medication 1, medication_intake 2, reminder_settings 1) | pass | pass (schema 7 → 7, no table differs) |
| Pending "Did it help?" across the update (#189/#197 history): the clock to 11:06, app not opened | posted at 11:06:01 | posted at 11:06:04 |
| The kept-records flow (`upgrade-kept.yaml`) on 1.3.0 | pass | pass |
| The next day's daily reminder (clock to 7:58 PM, app not opened) | posted at 8:01:32 PM | posted at 8:00:02 PM |
| The 1.2.1 backup imported into a clean 1.3.0 | every count equal, file schema 7 | not run (the file picker under Maestro fails on 8.0, as in 1.2.1) |
| Launcher shortcut after the update (string ids move) | "Migraine started" / "Log that a migraine started" before, after `install -r` and after the first open | the shortcut stays registered before, after `install -r` and after the first open (Android 8's dump does not print the label) |

Two void attempts on API 36 before the counted run (see Voids): the launch helper sent Migrely to the launcher, and a stale
backup file from another session was picked for the import. On API 26, QA set font scale 1.0 (the device was found at
2.0) while that step was seeding; the kept-data checks compare before and after the update, so this cannot make them pass.

## R8 smoke, monkey, instrumented tests

| Line | API 36 | API 26 |
|---|---|---|
| Debug-only components (#195): `am start` of `androidx.compose.ui.tooling.PreviewActivity` on the R8 APK | not declared, refused, no crash | "does not exist", no crash |
| Smoke, `qa/maestro.sh --install --tags smoke`, R8 APK | 3/3 | 12/12, and 3/3 in the 20 s accessibility-timeout pass |
| Monkey, seed 20261024, 5000 events, R8 | 0 crashes, 0 ANRs (zen mode it toggled restored) | 0 crashes, 0 ANRs (1 crash of another app, the AOSP keyboard, as in earlier records) |
| Instrumented, `connectedDebugAndroidTest` per module | app 7, architecture/ui 13, datasource/implementation 130, designsystem 58, attack/ui 5, medication/ui 14: **227/227 pass** | app 7, architecture/ui 13, datasource/implementation 130, attack/ui 5, medication/ui 14 pass; designsystem 49 of 50 (`DialogWaitTextFieldTest.aShortTapOnATextFieldDoesNotFocusAndTheNextDoes`, the known #229, S4; `DateRangePickerScreenTest` excluded for #144): **218/219** |

QA tooling defect found: `qa/monkey.sh` opens the app with `monkey -p <app> -c LAUNCHER 1`, which on API 36 aborts
("SYS_KEYS has no physical keys but with factor 2.0%", exit 251) and, under `set -e`, ends the run before any event. The
counted API 36 run used a temporary copy with `--pct-syskeys 0` on that one line (removed after; the release tree is
clean). A QA follow-up fixes `qa/monkey.sh`.

## Voids (harness slips, rerun; not counted)

- **The first three Gboard F1 rows (`f1c-1`, `f1c-2`, `f1m-1`):** they "passed" with no keyboard on screen. On this image every
  touch device reports stylus support (`mStylusIds`), and Gboard's "Use stylus to write in text fields" (on by default)
  then shows only a small floating toolbar; the IME inset had zero height (`type=ime frame=[0,2400][1080,2400]`). Fixed by
  resetting Gboard's data and turning that option off; from then on the full keyboard (top 1517 px) showed on every focus.
  The rows were rerun. QA's keyboard-top reader (`qa-kbd8/kb.py`) also read 2400 on API 36 and now reads the IME insets.
- **T61 control `c-fwd2h-1` (production 1.2.1):** not a valid control for clock-jump rows (see T61); the controls ran on
  develop 50c784e5.
- **T61 reboot row (4.2 "Reboot after the time"):** VOID twice; not run on a device. The app planned 8 PM instead of the
  reminder time set by SQL (the stored minute is not reread while planning), and the boot check passed before the device
  went down, so the fake clock never returned to host time (skew −4321 s). Team-lead allowed recording it VOID.
- **#217 / Privacy first attempts:** the app was left in attack mode, and a pattern matched the "Privacy and about"
  heading. Fixed scripts (`tb217b.py`, `tbpriv.py`) and reran; the first control's onboarding readings were lost because
  the row was written only at the end (now in `finally`).
- **4.4 "Did it help?" first attempts:** a clock jump cannot bring it due (its job keeps an elapsed delay and the clock
  re-plan covers only the daily and visit reminders, in 1.2.1 too), and the Maestro flow ended with a force stop, which drops
  the app's JobScheduler jobs. The counted row opens the app once after the flow.
- **K4 first runs and the "error gone" suspicion:** on a full `/data`, `uiautomator dump` to `/sdcard` fails, and a dump to
  `/data/local/tmp` is cut off at the bytes left (20480 and 16384 bytes). The truncated dumps read as "no message" after
  the keyboard closed. Screenshots of the same moments show the error still on screen (two head runs). Disproved, not filed.
  Dumps now reuse a 256 KB placeholder.
- **"Landscape" U9 cells in the keyboard chain:** `user_rotation` did not rotate the app in those runs (window 1080x2400);
  they count as portrait cells. Landscape was rerun with `wm user-rotation lock 1`.
- **F1 rows typed with `input text`:** Gboard does not compose text injected that way, so neither the candidate nor the
  76e5afcd control moved; those runs show no regression but cannot fail. The counted rows type on Gboard's keys.

- **Rows that ran on 1.2.1 by a harness slip (02:22 to 02:30):** after a disprove run ended by installing the 1.2.1 debug build
  as its control, the next chain resumed without reinstalling. The device hash in each row (`1455aad5…`) shows it. Those rows
  (three landscape-labelled U9 cells, two F1 rows and the first #221 rows) are counted only as 1.2.1 controls; every
  candidate row was rerun in a chain that checks the device hash before each step.
- **Monkey on API 36, first two runs:** `qa/monkey.sh` stopped before any event (exit 251, see the R8 table). The counted run
  used a temporary copy with the launch fix.
- **API 36 update, first two runs:** `monkey -p … 1` as a launch helper left the launcher on top, then a stale export from
  another session was imported. Fixed (`am start`; old exports removed first) and rerun; every row passes.

## Carried over, and why this diff cannot reach them

| Go/No-Go line (`docs/QA.md`) | Here | Why it carries |
|---|---|---|
| 1 Build gate | QA's own builds exit 0 (`assembleDebug assembleRelease :app:checkReleasePermissions`, the androidTest APKs, and `lint`); the full gate (`assembleDebug test ktlintCheck detekt lint`) on the release head is team-lead's run of 2026-11-16 23:45, whose result QA did not see | QA's builds show the release code compiles and lints; the unit-test result is team-lead's to confirm |
| 1a Lint | run (static rows) | — |
| 1b Instrumented | per module (below) | — |
| 2 Smoke on 8.0, 10 R8 | R8 smoke on both levels (below) | — |
| 2a Split APKs | not run | no AAB in this check; the diff touches no locale or split configuration |
| 3 Full regression | not run per release | owner rule 2026-11-09: nightly on `develop`; each change's record ran the flows its diff reached |
| 4 Accessibility audit | TalkBack rows above (T66 3.6, 2.5, T61 4.3, T63 K2/U8/8.3) | the other screens are not reached by the diff except through the shared message host and the keyboard helpers, which have their own rows |
| 5 Device scenarios | the upgrade (below) | — |
| 6 Monkey | R8, both levels (below) | — |
| 6a Upgrade | 1.2.1 → 1.3.0 (below) | — |
| 7 Permissions | equal to 1.2.1 (static rows) | — |
| 8 Open defects | no open S1 or S2 | — |
| 9 Coverage | not rerun | no flow or register change in this record |
| 11 Pre-launch report, 12 internal testing | not run | Play steps are the owner's |
| Changes before T61/T63/T66 (#207, #208, #211, #212, #216, #218) | their own records (`fix-release-debug-components`, `fix-safety-copy-followups`, `fix-medication-ratings-copy`, `fix-onboarding-goal-wording`, `keep-late-daily-reminder`) | text-only or reminder changes already accepted on a device; their deferred "update from production" row is the upgrade below |

## What these checks do not prove

- **Real phones, real keyboards, real TalkBack users.** Emulator images only: Android 8.0 AOSP and Android 16 google_apis with
  Gboard and TalkBack 16. Android 8.1 to 15, other keyboards (Samsung, SwiftKey), OEM notification shades, foldables and
  tablets were not run.
- **Real sound and vibration.** The emulator has no audio output and no vibrator. "Sounded" means Android's
  `notification_alert` decided beep or buzz; nobody heard it. The late "Did it help?" post's sound is inferred from its
  flags (its alert line was not captured).
- **A real reboot after the reminder time** (T61 4.2): VOID; it rests on `RunDailyReminderUseCaseTest` and the clock-forward
  and zone rows, which reach the same use case.
- **Finding 1 of T63 on API 36:** the 76e5afcd control did not move on Gboard either, so the API 36 F1 rows show no
  regression but cannot fail. Finding 1's fix rests on the API 26 rows (control moved) and its test.
- **The F2 card's negative control** (51e22355) was not run on API 36.
- **TalkBack reaching Back with the keyboard up (K2), U8, 8.3:** see the T63 table for what ran.
- **#221 hosts not run on a device:** Paywall, Doctor report, Your data, My medications, Onboarding (and A4, A13, A14, A17,
  M rows, Switch Access, 60 fps frames), as in the #221 record.
- **The visit section's own "Open settings for notifications"** (shown when only the visit channel is off) was not read on a
  device; its Compose test covers the name.
- **Landscape U9 cells F4 at 100%** and the 360 dp F1 cell at 200% did not run.
- **The upload-key signed AAB and split APKs:** QA's R8 APKs use the debug key (D102); the bundle was not installed.
- **Always-on display or "wake screen for notifications"** (4.3b) is not available on the emulator image.
- **The "daily" channel turned off or set to silent by the person** ran on API 26 only (T61 record).

## Residual risks

- **#238 ships in 1.3.0 if not fixed:** a TalkBack user who closes the keyboard on a migraine's note is moved to the
  "Stress" chip; a double tap there marks a trigger they did not mean. S3, accessibility. A fix needs its own change.
- **#237 and #239** (S3): misleading or silent errors after a full disk or an empty medication name; #237 is in production.
- **T61 on real phones:** OEM shades and Android 8.1 to 15 may show a FLAG_SILENT post differently; R1 is shown on two
  emulator images only. People who rely on sound or vibration (design R2) get no sound for a reminder over an hour late,
  by design.
- **The reboot path** of T61 is unit-tested only.
- **T63 finding 1 on Gboard** cannot be shown either way on API 36 (the control does not fail).
- **The #221 hosts not run** (Paywall, Doctor report, Your data, My medications, Onboarding) rely on unit tests and the shared
  message host; #237 shows the hosts can differ.
- **QA tooling:** `qa/monkey.sh` fails on API 36 before sending events (the launch line needs `--pct-syskeys 0`); a QA
  follow-up fixes it. Until then a green "monkey" line on API 36 needs the log, not just the exit code.
- **Production must get a build whose tree equals the tag:** after the tag, `git diff 294c10c6 v1.3.0` must be empty.

## Device state at the end

- **emulator-5580 (API 36):** restored to the boot baseline and read back: every recorded setting equal (font 1.0,
  auto-rotate on, rotation 0, animator unset, `show_ime_with_hard_keyboard` 0, no accessibility service, accessibility
  timeouts unset, auto time and zone on, Asia/Tashkent, clock skew 0, DND off, private notifications on, no lock); TalkBack's
  log level back to ERROR (it was ERROR at boot); Gboard's data reset, so its stylus writing is back to the default (on);
  no filler files. The app left installed is the 1.3.0 R8 candidate (`c91b0854…`); at the start an older Migrely build
  was installed. Powered off with `emu kill`. The AVD's `config.ini` is restored (`hw.keyboard=yes`).
- **emulator-5582 (API 26):** every recorded setting equal to how it was found, including the two values away from the QA
  baseline that earlier sessions left (font 2.0, DND "Alarms only" zen 3); clock skew under 1 s. Migrely is not installed at
  the end (the instrumented run uninstalls it); at the start 1.2.1 was installed. Powered off with `emu kill`.
- **Host:** QA worktree `migraine-app-qa-rel130` clean at 2f7745c3 (the temporary monkey copy removed); no QA lock left.

## Addendum, 2026-11-17 12:20: T66 4.1/4.3 History on API 36 (evidence found after the record)

The scope above lists "T66 4.1/4.3 History" for API 36, but the record had no History row. The rows ran during the
check (chain `s9`, 02:35 to 02:42) and were left out of the write-up. QA (qa-t64) read the evidence back:
`migrely-work/qa-rel130/logs/s9.progress`, `hist36/hist-head/kb66.log`, one dump per key and one screenshot per cell.

- **Build on the device:** APK sha256 prefix `782cf3d3e80efc1a` before and after the step (`s9.progress`), the
  candidate's debug build from 2f7745c3 (`apk/SHA256SUMS`). Not the R8 build.
- **Device:** emulator-5580, API 36, portrait. Driver `bin/hist36.sh` (qa-t66 `hist.sh`): Maestro setup per cell, tap
  the day, then Tab from no focus, Shift+Tab, Tab, Shift+Tab, a `uiautomator` dump after every key.

| Cell | Day chosen | Tab ×5 from no focus | Shift+Tab from the day section's first control | Result |
|---|---|---|---|---|
| migraine row, 100% | today, 1 migraine | Back, Insights, Doctor report, the day, the migraine row | the chosen day, inside the window; Tab, Shift+Tab repeat it | **pass** |
| Period shown, 100% | Nov 2, nothing logged | … the day, "Period" | the chosen day, inside | **pass** |
| Period hidden, 100% | Nov 2 | … the day, "Pain-free day" | the chosen day, inside | **pass** |
| migraine row, 200% | today | … the day, the migraine row | the chosen day, inside (the page scrolled so the day sits at y 296–424, under the top bar's 276) | **pass** |
| Period shown, 200% | Nov 2 | … the day, "Period" | the chosen day, inside | **pass** |

- **5 of 5 cells: no stop.** Shift+Tab never landed on a group, a scroll container, the legend or a spacer, which is the
  stop T66 4.1 looked for. The focus ring is visible on the chosen day in the end screenshot of the cells checked
  (`head-per-2.0-P-end.png`).
- **Not run on API 36:** Period hidden at 200%, landscape (two panes) at 100% and 200%, and a day chosen by the
  keyboard (arrows and Enter). These cells exist only on API 26 (0 stops in 22 cells, head and develop,
  `fix-privacy-wording-and-focus.md`).
- **No control on API 36.** 1.3.0 changed no History code (T66 4.3 note), so there is no "fixed" claim to prove
  against an older build; the row shows only that the stop does not reproduce on API 36 in these 5 cells.
- **Effect on the verdict:** none. The rows pass, and the 1.3.0 GO stands as recorded.
