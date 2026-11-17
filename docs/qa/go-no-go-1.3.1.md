# Release Go/No-Go: Migrely 1.3.1 (versionCode 13)

## Decision

**QA's verdict, 2026-11-18 04:20: GO pending the PENDING-PERSON rows, for `release/1.3.1` @
f626c2d09d0cc18502594e45628c3c4c2107102d.** The real-phone TalkBack rows are the blocking line.

- **Blocking line: the two PENDING-PERSON rows** (T64's TalkBack menu, and EC-UX-60 self-focus on Home, History and
  dark-room Log medication) on the owner's phone `14634fa97d7c`, run by the main session with the owner present. Steps,
  expected results and what counts as a fail are below. If they pass, this is a plain GO for f626c2d0. If one fails, it
  is filed with its severity and the Go rule decides (no open S1 or S2). Which build the phone runs, and therefore whether
  they gate the tag or the production promotion, is in step P0.
- **Everything QA can run on emulators passes on the exact SHA:** release-variant (R8, minified, debug-signed) build
  with versionCode 13 / versionName 1.3.1 in the APK and on both devices, sha256 `702cd448…0385` read back on the
  device; the full local gate exit 0 (3383 unit tests, 0 failures; ktlint, detekt, lint 0 errors).
- **R8 does not break the KeyboardRoom modifier (#238):** TalkBack on Attack details, Back near the end: 0 px,
  0 `TYPE_VIEW_SCROLLED`, focus kept, nothing written (3/3). Control, 1.3.0 R8: 820 px, 3 scroll events, focus to the
  impact radio, the next double tap writes it (2/2).
- **R8 does not break the message clock (T64):** "Medication removed" held by TalkBack focus and by keyboard focus past
  30 s with a 20 s setting, then left with its time left (1/1 each). Control, 1.3.0 R8: gone by 30.5 s (1/1 each).
- **The update from production 1.3.0 keeps everything** on API 36 and API 26: every table's content, the settings file
  (theme Light kept), the pending "Did it help?", the next day's daily reminder, no crash; API 36 also imports the 1.3.0
  backup with every count equal.
- **Permissions and manifest:** no INTERNET; the release permissions equal 1.3.0's; the merged manifest differs from
  1.3.0's only in the version; PreviewActivity absent from the release (not declared, refused on both devices).
- **No open S1 or S2.** No defect found. #238 and #226 retested and closed by QA.
- **Not proven:** see "What these checks do not prove"; above all the Play-signed build (pairip) and real phones.

## Candidate, scope and builds

- **Verdict SHA:** `release/1.3.1` @ **f626c2d09d0cc18502594e45628c3c4c2107102d** (T74). Production is 1.3.0 (vc12,
  `v1.3.0` = c9776b5c).
- **What 1.3.1 ships since `v1.3.0`:** #244 T64 message timing and size (D182; QA GO with risks on 58e62072,
  `fix-message-timing-and-size.md`), #246 #238 closing the keyboard never moves the page (D189; QA GO with recorded
  risks on 47f93991, which holds both, `keep-page-when-keyboard-closes.md`). On the release branch: the version bump
  (fa8303d3), `docs/release/whats-new.md` (56b9189a, 6ff62a72) and a BRD wording fix of D189 (e) (f626c2d0).
- **What QA checked about the diff, 2026-11-18 02:47:**
  - `git diff 47f93991 9075aff8 -- . ':!docs' ':!openspec'`: 0 lines. The code QA accepted is the code on `develop`.
  - `git diff --name-only 9075aff8 f626c2d0`: `app/build.gradle.kts`, `docs/BRD.md`, `docs/release/whats-new.md`. The
    one code line pair is `versionCode 12 → 13`, `versionName "1.3.0" → "1.3.1"`.
  - `v1.3.0..f626c2d0` changes no manifest, no Room schema or migration, no ProGuard/R8 rules (`git diff --name-only`
    filtered for them: 0 files).
- **Scope (owner rule 2026-11-09):** only what the release diff reaches, with negative controls. The two acceptance
  records ran on **debug** builds; the release is an R8 build. So the new questions are: (1) does the minified build
  still carry #238's keyboard room and T64's message clock, (2) the update from production 1.3.0 with data, (3) the
  release manifest and permissions, (4) the R8 build at large (smoke, monkey). Everything else carries over (table
  below).
- **Builds** (QA's own; worktree `migraine-app-qa-rel131` at f626c2d0, clean before and after the build; no
  `keystore.properties` there, so the release variant is **signed with the debug key**, D102, as in the 1.3.0 record;
  the upload-key AAB is the engineer's step and is not what QA installed):

  | APK | Source | sha256 | Use |
  |---|---|---|---|
  | `r8-f626c2d0` | candidate, `:app:assembleRelease` (R8, minified) | `702cd44818330a222a0cfb51bc283c74621dc5ff428624e4771e679827180385` | every candidate row; read back from the device in every row |
  | `debug-f626c2d0` | candidate, `:app:assembleDebug` | `fde165dbc3a1c56b63cfd6b56955b9f631e4a6ca91da9220b790c3e003a0e012` | static control only (debuggable, PreviewActivity present) |
  | `r8-v130-2f7745c3` | QA's 1.3.0 R8 from the 1.3.0 Go/No-Go (2f7745c3; its tree equals `v1.3.0` but for `whats-new.md`) | `c91b0854517d8ce3b64d685f03aece1f2681f81ff324facabefcd1c1e397c6c3` | negative control; update base |

  `nice -n 19 ./gradlew --max-workers=2 :app:assembleDebug :app:assembleRelease :app:checkReleasePermissions` →
  BUILD SUCCESSFUL in 1m 56s, exit 0; `:app:checkReleasePermissions` ran (log line 836);
  `git status --porcelain` 0 before and after (`migrely-work/qa-rel131/logs/build-f626c2d0.log`).
- **Devices:** `Migrely_QA_API_36` (emulator-5580, Android 16 google_apis, Gboard, TalkBack 16, headless), then
  `Migrely_QA_API_26` (emulator-5582, Android 8.0), one at a time, cold-booted. No other API 36 emulator was running.
- **Evidence** (outside the repo): `migrely-work/qa-rel131/` (`walk/rows.jsonl` one line per TalkBack and message row
  with the device APK hash, `rows.jsonl` and `logs/up*.log` for the update, `raw/` settings snapshots, TalkBack logs,
  table dumps and preference files, `shots/`, `logs/static.txt`, scripts in `bin/`).

## Static rows on the candidate

| Row | Result |
|---|---|
| Identity (`aapt2 dump badging`) | R8: `com.migrely.app`, **versionCode 13, versionName 1.3.1**, targetSdk 36, not debuggable. Debug: same identity, `application-debuggable` |
| Identity on the device | after the update: `dumpsys package` versionCode=13, versionName=1.3.1 on API 36 and API 26; the installed `base.apk` sha256 read back on the device = `702cd44818330a22…` on both |
| Permissions | `aapt2 dump permissions` of the R8 candidate **equals** the 1.3.0 R8 (sorted `diff`, exit 0): POST_NOTIFICATIONS, ACCESS_NETWORK_STATE, BILLING, WAKE_LOCK, RECEIVE_BOOT_COMPLETED, DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION. **INTERNET: 0 lines.** `:app:checkReleasePermissions` exit 0 |
| Merged manifest | `aapt2 dump xmltree AndroidManifest.xml`, candidate R8 vs 1.3.0 R8: the only difference is `versionCode` and `versionName` |
| Debug-only components (#195) | `PreviewActivity` (and `Simulated`, `tooling`) in the R8 manifest: **0**; control: the debug APK's manifest has it (1). On the device: `am start -n com.migrely.app/androidx.compose.ui.tooling.PreviewActivity` on the installed R8 build: "Activity class … does not exist", no crash, on API 36 and API 26 |
| `qa/release_apk.py` | R8: "code 0, resources 0, manifest 0, debuggable False", exit 0. Control: the debug APK has the simulated store 57/2/4, debuggable, exit 1 |
| Signer | candidate R8 and 1.3.0 R8 share certificate SHA-256 `7eae4e48…bfbabd` (QA's debug key), so the update row below is a real `install -r` |
| Open defects (line 8) | `gh issue list --state open`, 2026-11-18: **no S1 or S2.** Open from earlier: S3 #245, #239, #237, #233, #230, #179, #148; S4 #236, #229, #224, #153, #150, #149, #144, #118, #109, #82. Closed by QA in this check after the retest: **#238** (the R8 TalkBack rows above) and **#226** (T64's A4 Undo-ceiling rows; the code is unchanged in the release). No new issue: no defect was found |

## #238 on the R8 build: TalkBack, keyboard closed near the page's end (KR1, KR3)

Driver `bin/tbrow.py` (QA's #238 driver, unchanged but for paths; `QA_R8=1` reads the database as root because an R8
build has no `run-as`; root is `adb root` on the google_apis image). Attack details, 100% portrait, a seeded past
migraine; TalkBack off: focus the note, type "qa", drag to the page's end; TalkBack on; two-finger drags to the end;
the row is valid only if TalkBack said "Editing" on the note. Then the Back key once; 3 s of TalkBack's log; a
screenshot before and after (page shift by row-signature votes); one TalkBack double tap; TalkBack off; database
(impact, triggers, medications, intakes) compared with before. Gboard full keyboard (top 1517 px), with
`show_ime_with_hard_keyboard` 1 and `stylus_handwriting_enabled` 0 for the run (restored).

| Build | Rows | Page shift | `TYPE_VIEW_SCROLLED` from the app | Spoken after Back | Double tap | Database |
|---|---|---|---|---|---|---|
| **R8 candidate** `702cd448` | **3/3 valid** | **0 px** (153 votes of 273 rows, every row) | **0** | only "keyboard hidden" | the keyboard reopens **in the note** ("Showing English (US)"), note at 42,962–1038,1296 before and after | **unchanged** |
| R8 v1.3.0 `c91b0854` (control) | 2/2 valid | **820 px** | **3** | "Radio button. 4 of 4. In list. 4 items" (the "Impact on your day" radio) | no keyboard; "Selected" / "Not selected" | **changed both times**: `functional_impact` written 3, then cleared by the next row's tap |

Pass rule fixed before the run (from the acceptance record): 0 px, 0 scroll events, focus kept, nothing written.
**R8 keeps the KeyboardRoom modifier working**; the control reproduces #238 on the minified 1.3.0 with data written.

## T64 on the R8 build: one message pause row each way (Attack details)

Driver `bin/t64.py` (QA's #238/T64 driver). "Time to take action" 20 s. Remove the logged Ibuprofen on Attack
details → "Medication removed" with "Undo removing the medication". Presence read from screen frames (the control's
label colour in its box), never from dumps while timing.

| Row | R8 candidate | R8 v1.3.0 (control) |
|---|---|---|
| TalkBack focus on the message text at 2.7 s, held; focus moved at 30.8 s | **still shown at 30.6 s**; gone **18.9 s** after focus left (the 17.3 s left when focus came, plus about 1 s capture lag) — **1/1** | **gone by 30.6 s**; at the move it was already gone (0.4 s) |
| Hardware Tab to Undo (2 Tabs, found focused at 9.3 s), held; Tab away at 30.6 s | **still shown at 30.6 s**; gone **14.1 s** after leaving — **1/1** | **gone by 30.5 s** |

TalkBack spoke "Medication removed" on the focus touch on both builds. **R8 keeps the message clock's pause for
TalkBack focus and for keyboard focus.** One row each; the full T64 matrix stands on its own record (debug build).

## Update from production 1.3.0 (vc12) to 1.3.1 (vc13), R8 to R8

Driver `bin/uprel131.py` (the 1.3.0 record's `uprel.py`, plus: every table's **content** compared, not only its row
count; the `shared_prefs` files compared; the app theme set to Light on 1.3.0 as a setting away from its default; the
crash buffer read after the update). On a fake date 2026-11-10: install 1.3.0 R8 fresh, seed through the UI with no
clear (two migraines, Severe, Nausea, a custom symptom, Ibuprofen with intakes at 09:00 and 09:05, the daily reminder
on, theme Light), export a 1.3.0 backup, let the system kill the app, check installd's code_cache mark,
then `install -r` the 1.3.1 R8 **and do not open it**.

| Row | API 36 (run r4) | API 26 (run r1) |
|---|---|---|
| Builds read back | before: `c91b0854…` (1.3.0 R8), not debuggable; after `install -r`: `702cd448…` (1.3.1 R8), "Success" | the same |
| code_cache mark true before the update (the #189/#193 path) | pass | pass |
| Database kept: schema 7 → 7; **every table's content equal** (sha256 of `select * … order by 1`, 10 tables: attack 2, attack_tag 2, custom_tag 1, intensity_point 1, medication 1, medication_intake 2, reminder_settings 1, three empty) | pass | pass |
| Settings kept: `shared_prefs/migrely_preferences.xml` byte-equal, and it holds `theme = light` and `onboarding_finished = true` after the update | pass | pass |
| Pending "Did it help?" across the update (clock to 11:06, app not opened) | both posted at 11:06:01 | both posted at 11:06:05 |
| The kept-records flow (`upgrade-kept.yaml`) on 1.3.1 | pass | pass |
| The next day's daily reminder (clock to 7:58 PM, app not opened) | posted 8:01:32 PM, "How was today?" with both actions | posted 8:00:02 PM, the same |
| Crash buffer after the update | 0 lines | 0 lines |
| The 1.3.0 backup imported into a clean 1.3.1 | every count equal, file schema 7 | not run (the file picker under Maestro fails on 8.0, as in 1.3.0) |

Three void attempts on API 36 before r4, all in QA's new theme step, none reached the update: r1 pressed Home and
then tapped the **launcher's** "Settings" (Android's own Settings app; it only scrolled there, and the settings
snapshot taken right after equals the boot one), r2 and r3 did not find Migrely's Settings row (the app reopened on its
last screen, then a bounds check was too tight). Fixed so every node must belong to the app; r4 is the counted run
(`logs/up36-r1.log` … `up36-r4.log`). On API 26 QA set font scale 1.0 for the run (the image boots at 2.0 from earlier
sessions) and set it back to 2.0 after.

## R8 smoke and monkey

| Line | API 36 | API 26 |
|---|---|---|
| Smoke, `QA_APK=r8-f626c2d0.apk qa/maestro.sh --install --tags smoke` | **14/14**, and **3/3** in the 20 s accessibility-timeout pass, exit 0 | **12/12**, and **3/3** in the 20 s pass (API 26 has no timeout setting; the flows say so), exit 0 |
| Monkey, seed 20261024, 5000 events, R8 | **0 crashes, 0 ANRs, finished**, crashes of other apps 0; the zen mode it toggled (0 → 1) restored by the script | not run (carried: the API 26 R8 build is the same APK, and 1.3.0's API 26 run passed) |

`qa/monkey.sh` in the tree still opens the app with `monkey -p … 1`, which aborts on API 36 before any event (the
1.3.0 record's tooling defect, not fixed yet). The counted run used an out-of-tree copy
(`migrely-work/qa-rel131/bin/monkey131.sh`) whose only changes are that launch line (now `am start`) and its root path;
the release tree is untouched. The smoke's Maestro `launchApp` grants permissions; no permission flow is judged here.

## Carried over, and why this release diff cannot reach them

The release diff is the version bump, release notes and BRD wording, over code QA already accepted in two records.
A row carries when that diff cannot change what it proved. The column says whether the release's own diff reaches the
row. The R8 rebuild touches every row's code in the sense that it minifies all of it; that is the one thing the records
did not cover, and it has its own rows above (one TalkBack row, two message rows, smoke and monkey). Where a row was
rerun on R8 the column says so.

**`docs/qa/keep-page-when-keyboard-closes.md` (#238, on 47f93991 = the release code):**

| Row | Can the release diff reach it? | Carried because |
|---|---|---|
| KR1, KR3 TalkBack + Back on the four screens (details 3/3, Log 3/3, Edit 3/3, Delete 3/3; controls v1.3.0 and M0) | No; rerun on R8 | Same source; R8 rerun on details above (3/3, control 2/2) |
| KR2 hide key and Done | No | Same source; same modifier as KR1 |
| KR4 0 px in every cell, API 36 and API 26 | No | Same source |
| KR5 band colour, KR6 band inert, KR7 band shrinks with the person's scroll (M1 control) | No | Same source |
| KR8 landscape TalkBack (navigation order not run) | No | Same source; the "not run" part stays not run (below) |
| KR9 reopen, KR10 not saved across rotate/HOME/process death, KR11 sizes, KR12 frames | No | Same source |
| KR13 hardware/floating keyboard, split screen | No | Not reachable on the AVDs: still not proven |
| KR14 message with the band, KR15 Delete all data + Done, KR16 far from the end | No | Same source |
| T64 on the merged head (details Tab, details TalkBack, dark-room Log medication 64 dp) | No; two of three rerun on R8 | Same source; the dark-room 64 dp size was not rerun on R8 |
| #245 (S3, pre-existing) | No | Open; planned for 1.3.2 |

**`docs/qa/fix-message-timing-and-size.md` (T64, on 58e62072; its code is in 47f93991 unchanged):**

| Row | Can the release diff reach it? | Carried because |
|---|---|---|
| A1 "Saved." follows "Time to take action" (Home, History, Reminders) | No | Same source |
| A2 keyboard pause P1, P2, P5, P6, API 36 and API 26 | No; one R8 row above | Same source; one R8 keyboard row above |
| A3 TalkBack pause, `MessageTalkBackPauseTest` 10/10 with a mutation control; real TalkBack on Home; the menu row on the emulator | No; one R8 row above | Same source; one R8 TalkBack row above. The real-phone part is the person row below |
| A4 Undo ceiling (Home, History late offer, History 117.7 s) | No | Same source; #226 retest |
| A5 64 dp in the dark room (7 cells), A6 nothing new spoken | No | Same source |
| EC-UX-60 self-focus on Home (emulator) | No | Same source; History, dark-room Log medication and a real phone are the person row |

**Go/No-Go lines (`docs/QA.md`):**

| Line | Here | Why |
|---|---|---|
| 1 Build gate | QA ran it at f626c2d0: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint --continue` → BUILD SUCCESSFUL in 4m 5s, exit 0; unit tests 3383, 0 failures, 0 errors, 0 skipped (from the `TEST-*.xml` files); `git status` clean but for this record (`logs/gate.log`) | |
| 1a Lint | 0 errors; 68 warnings, by module and id the same set and the same count as the 1.3.0 record (app 50, designsystem 1 + 2 hints, attack 4, history 2, home 5, medication 2, reminders 1, report 1). No new warning | |
| 1b Instrumented | not rerun | The release diff adds no test and no code; the engineer's runs for #238 and T64 and QA's `MessageTalkBackPauseTest` rerun (10/10 twice, control 0/10) are on the same code. Debug-only by nature, so R8 does not reach them |
| 2 Smoke | R8, see above | |
| 3 Full regression | not run per release | owner rule 2026-11-09: nightly on `develop` |
| 4 Accessibility | the TalkBack rows above; the person rows below | |
| 5, 6a Device scenarios, upgrade | 1.3.0 → 1.3.1 above | |
| 6 Monkey | see above | |
| 7 Permissions | equal to 1.3.0, no INTERNET | |
| 8 Open defects | no S1 or S2 | |
| 11, 12 Pre-launch report, internal testing | not run | Play steps are the owner's |

## PENDING-PERSON rows: the blocking line of this verdict

From `docs/qa/fix-message-timing-and-size.md` (T64, D182, EC-UX-60). They need real TalkBack on a real phone. **Only the
main session runs them, only on the owner's phone `14634fa97d7c` (Xiaomi, Android 15), only with the owner present.**
QA never touched that phone. About 15 minutes. Each step says what to write down; P3 says what
QA needs back.

### P0. Before anything: which build, and the owner's data

1. `adb -s 14634fa97d7c shell dumpsys package com.migrely.app | grep -E "versionName|installerPackageName"` and write
   both down.
2. **If Migrely there was installed by Play and holds the owner's own diary, do not uninstall it without the owner's
   explicit yes.** QA's candidate APK (`migrely-work/qa-rel131/apk/r8-f626c2d0.apk`, sha256 `702cd448…0385`) is signed
   with the debug key, so it cannot install over a Play install (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`); only an
   uninstall makes room for it, and an uninstall deletes the diary. Two ways, team-lead and the owner choose:
   - **A, before the tag (QA's preference when the phone's diary is test data only):** in Migrely, Your data, "Export
     a backup (JSON)"; copy the file to the Mac (`adb pull`) and check it opens; then uninstall, `adb -s 14634fa97d7c
     install migrely-work/qa-rel131/apk/r8-f626c2d0.apk`, read back `versionName=1.3.1`, run P1 to P3, then uninstall,
     reinstall from Play and import the backup. Deleting data with a copy: the owner says yes first (handbook §5).
   - **B, after the tag:** the tagged 1.3.1 AAB on Play's internal testing track updates the Play install and keeps the
     diary. That is a Play upload (owner only, §5). Then these rows gate **promotion to production**, not the tag.
3. Phone settings to note now and restore at the end: TalkBack on or off, "Time to take action" (Xiaomi: Settings,
   Additional settings, Accessibility, Interaction or "Timing controls"), font size, dark mode.
4. Set "Time to take action" to **1 minute**. Turn TalkBack on. Keep a stopwatch beside the phone.

### P1. The TalkBack menu (about 2 minutes)

1. Open Migrely. On Home, touch "Migraine started" once (TalkBack reads it), then double tap. Start the stopwatch.
   A message "Migraine started" with "Undo the start" appears at the top.
2. Touch the words "Migraine started" in the message once. TalkBack reads them. Write down the stopwatch time.
3. Open the TalkBack menu: swipe down then right in one stroke (or a three-finger tap).
4. Leave the menu open until the stopwatch shows **80 s**. Close the menu (Back).
5. Write down: is the message still there?
6. End the migraine ("Migraine ended", double tap), so P2 starts clean.

**Expected:** gone after the menu closes (the menu takes the window; the time runs, design D3).
**Counts as a fail:** the message disappeared in steps 2 to 3, while TalkBack's focus was on it and before the menu
opened (the pause does not work on this TalkBack): S3, accessibility. **Not a fail, write it down:** the message is
still there after step 4 (this TalkBack's menu does not take the window); QA records it as a difference from the
emulator. If it is still there at **2 min 10 s** from step 1, that is a fail (the Undo outlived its 115 s ceiling): S3.

### P2. Does a new message take TalkBack's focus by itself (EC-UX-60), about 8 minutes

For each screen: make the message, then **do not touch the phone**. Start the stopwatch at the double tap that makes it.

| # | Screen | How to make the message | Message |
|---|---|---|---|
| a | Home | touch "Migraine started", double tap | "Migraine started", Undo |
| b | History | open a migraine from History, "Delete migraine", confirm; you are back on History | "Migraine deleted", Undo |
| c | Log medication in the dark room | start a migraine; on Home touch "Medication taken", double tap; choose a medication (or add one), log it; on "Medication taken" double tap "Undo" | "Removed. Nothing was logged.", Dismiss |

For each, write down: (1) what TalkBack read by itself after the double tap, without your touch; (2) the stopwatch
time when the message left (stop at 3 min if it is still there); (3) for a message still there at 3 min: swipe right
twice to move TalkBack away, and write the seconds until it leaves.

**Expected:**
- a and b (Undo): if TalkBack read the Undo by itself, the message stays, but never past **about 2 min** (115 s plus
  the exit); if not, it leaves at about **1 min** (60 to 70 s).
- c (information, no Undo): if TalkBack read it by itself, it stays until you move away; then it leaves **3 to 10 s**
  after the move. If not, it leaves at about 1 min.

**Counts as a fail:**
- a or b still on screen at **2 min 10 s** (an Undo past its ceiling: the #226 lie "This can no longer be undone." comes
  back): S3, accessibility.
- any message gone **before 50 s** with TalkBack's focus on it or with no touch at all (shorter than the person asked
  for): S3, accessibility.
- after moving away, a message gone in **under 2 s**, or still there **30 s** after the move: S3.
- the app closes or freezes: S1, stop and keep the phone as it is.

### P3. Restore and report

Restore every setting written down in P0.3 and turn TalkBack off if it was off. Report one line per row:
`P1: gone yes/no at N s`, `P2a/b/c: read by itself yes/no, left at N s, after moving away N s`, plus the phone's
Android and TalkBack versions (Settings, Apps, Android Accessibility Suite).

## What these checks do not prove

- **Real phones and real TalkBack users.** Every row here ran on emulator images (Android 16 google_apis with Gboard
  and TalkBack 16; Android 8.0 AOSP). The person rows exist for that; until they run, real-phone TalkBack behaviour of
  T64 (self-focus, the TalkBack menu) is an emulator result only.
- **The Play build.** QA installed its own R8 APK signed with the debug key. The Play build is made from the AAB,
  signed by Play, and carries Play's automatic protection (pairip), which QA has never run. The tree equality rule
  (`git diff f626c2d0 v1.3.1` empty) ties the source, not the binary.
- **R8 on the other screens and paths of #238 and T64.** One TalkBack row (Attack details) and two message rows ran on
  the R8 build. Log medication, Edit medication, Delete all data, the dark-room 64 dp size, the Undo ceiling and
  "Saved." timing were proven on debug builds only. R8 cannot change layout constants, but it does rewrite code; the
  shared `KeyboardRoom` modifier and the shared message host are the same classes on every screen, which is why one
  screen each was chosen.
- **Not run at all, carried from the acceptance records as not proven:** KR13 (hardware or floating keyboard, split
  screen), KR8's TalkBack navigation order, KR9 on Delete all data, KR15 at 360 dp, KR16 with TalkBack, Switch Access,
  Voice Access, D-pad, Samsung TalkBack and other keyboards; T64's EC-UX-57/58 on a device, deep sleep, the 300 ms
  grace on a device.
- **The upgrade's reminders** are shown by a clock moved forward, not by a day passing, and on an emulator that does not
  deep-sleep.
- **Monkey on API 26** and the 1.3.0 backup import on API 26 were not run.
- **Instrumented tests and Lint warnings** were not rerun on device for this release (see line 1b); the gate's lint ran.

## Residual risks

- **T64 on real TalkBack is unconfirmed** (the person rows): a phone's TalkBack might not take the window for its menu,
  or not self-focus a new message; the effect is a message that stays longer or shorter than the emulator showed, bounded
  by the 115 s Undo ceiling. Accepted in EC-UX-60, but not yet seen on hardware.
- **The keyboard room reads the inset before layout** (design "Unordered reads"): 0 scroll events in every row on both
  API levels and on R8, but a slower phone could order them differently.
- **#245** (S3, pre-existing): 360 dp, 200%, landscape: the note's typed line is hidden while the keyboard is up. In
  1.3.0 too; planned for 1.3.2.
- **#237, #239** (S3) stay open from 1.3.0; this release does not touch them.
- **Production must get a build whose tree equals the tag:** after the tag, `git diff f626c2d0 v1.3.1` must be empty.

## Device state at the end

- **emulator-5580 (API 36):** every recorded setting equal to its boot snapshot (`raw/settings-emulator-5580-{boot,end}.json`;
  only uptime differs): `show_ime_with_hard_keyboard` 0 → 1 → 0, `stylus_handwriting_enabled` 1 → 0 → 1, TalkBack on
  and off by the drivers, TalkBack's log level ERROR → VERBOSE → ERROR (read back), "Time to take action" unset → 20 s →
  unset, zen mode toggled by monkey and restored, clock moved by the update driver and restored (device date equal to the
  host at 03:45). adbd was made root for the R8 database reads and set back to non-root (`id` read back). The app left
  installed is the 1.3.1 R8 candidate. Powered off with `emu kill`.
- **emulator-5582 (API 26):** every recorded setting equal to its boot snapshot, including the values away from the QA
  baseline that earlier sessions left (font 2.0, zen 3): font set to 1.0 for the update row and back to 2.0; clock
  restored (device date equal to the host at 04:04); adbd back to non-root. The app left installed is the 1.3.1 R8
  candidate. Powered off with `emu kill`.
- **Host:** QA worktree `migraine-app-qa-rel131` at f626c2d0 holding only this record; no QA lock left; no Gradle
  daemon stopped.
