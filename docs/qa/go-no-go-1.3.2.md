# Release Go/No-Go: Migrely 1.3.2 (versionCode 14)

## Post-tag verdict: NO-GO (2026-11-19)

**QA's verdict after the tag: NO-GO.** It supersedes the GO below; that section is kept as it was.

- **Cause: #259 (S2, accessibility, filed by QA).** This is a 1.3.2 regression from #245's bar stepping aside. On
  Attack details, in a 360 dp landscape window at 200% font, a TalkBack user opens the note with the usual double
  tap. Three things then happen:
  1. The keyboard opens and the top bar steps aside.
  2. About 1 s later TalkBack's focus moves to "Radio button, 3 of 4" (Impact on your day), and the page scrolls
     there. 0 of 112 px of the typed line are visible.
  3. The user's next double tap records an impact level.
- **Rows** (`migrely-work/qa-245c/rows.jsonl`, `x1`–`x7`; emulator-5580, API 36, Gboard, TalkBack 16 at VERBOSE
  during the cells and ERROR after, both read back):
  - **The jump.** On the v1.3.2 code (QA's debug `c1c0afab`, sha256 `9f280590…`, the same product code as tag
    v1.3.2), opening the keyboard under TalkBack at 360 dp jumped **8 of 8** times over 4 fresh installs (7 by double
    tap, 1 by `input tap`).
  - **The write.** After the jump, the next double tap says "Selected". The database read with `sqlite3` shows the
    past migraine's impact going from **empty to 2**, with its updated-at time changed (`x6-tbdt2`). The second
    run (`x4`) also said "Selected".
  - **Control v1.3.1** (debug `f626c2d0`, `fde165db…`): **0 of 4** jump. 40 of 112 px of the line visible, TalkBack
    stays on the note, database unchanged (`x7-tbdt2`).
  - **Cause isolated:** the same v1.3.2 build at 411 dp, where the bar stays, does not jump (0 of 1) and keeps the
    line whole (`x4-tbdt411`). Rotation 1 against 3, the note empty or holding text, the notification shade, and
    Home-and-back do not produce it.
- **Why the 1.3.2 rows missed it.** #245's SW1, K1 and K2 rows, and this record's R8 rows, opened the keyboard with
  TalkBack **off**. Turning TalkBack on afterwards closes Gboard (#238's known trap), so no row ever opened the
  keyboard by double tap with TalkBack already on. That is the row missing from the acceptance and from this release
  check.
- **Not proven:**
  - the R8 build (`c7a027f6`) and a real phone were not run for #259;
  - Log medication, Edit medication and Delete all data, which use the same bar code, were not run with the
    double-tap opening;
  - overwriting an impact that was already recorded was not tested (the seed had none). If replacing a recorded
    value counts as data loss, #259 is S1.
- **Team-lead's decision (2026-11-19):**
  - v1.3.2 stays tagged and is **never uploaded**; the Desktop AAB copy is removed.
  - versionCode 14 and 1.3.2 are not reused.
  - 1.3.3 = develop + the #259 fix only; #255 moves to 1.3.4.
  - QA retests #259 on the 1.3.3 candidate with the double-tap opening on all four screens, with v1.3.1 as the
    control.

## Decision (superseded by the post-tag NO-GO above)

**QA's verdict, 2026-11-19 16:30: GO with recorded risks, for `release/1.3.2` @
c1c0afab2e9f0c3a9331eb1866ce4458a18987a8** (versionCode 14, versionName 1.3.2).

- **Every row QA can run on emulators passes on the exact SHA**, on QA's own release-variant (R8, minified,
  debug-signed) build, sha256 `c7a027f6…33ac0`, read back on the device in every row. Each fix row has a control that
  fails on the minified 1.3.1 (or 1.3.0, or the #245 mutation, where 1.3.1 cannot fail).
- **#237 on R8:** after a full-disk failure, a successful log clears "Couldn't save…": 3 of 3 valid rows. On 1.3.1 R8
  the message stays over the result at 0–10 s. One void row (no message, no write on the full-disk tap) did not
  reproduce in two reruns from the same start; it is recorded, not filed.
- **#239 on R8:** TalkBack says exactly one "Enter a name." 0.57 and 0.60 s after Save on Edit medication: 2 of 2
  rows. Name is brought into view and focused. 1.3.1 R8 says nothing in 5 s.
- **#245 on R8** (360 dp, 200%, landscape, Details note): the typed line is 112 of 112 px at the end, start and middle,
  with the bar aside. 1.3.1 R8 shows 40 of 112. **K1:** TalkBack + Back moves the page 0 px with 0 scroll events, 3 of 3
  (1.3.0 R8: 820 px, 3 events). **API 26 SW4:** the bar stays and the line is whole at 360 and 411 dp (the M64 mutation
  loses the bar). **#245 is closed by QA retest on release/1.3.2 c1c0afab.**
- **The shared message host on R8** (`MessageLayout`, changed by #245): T64's TalkBack pause holds on Attack details,
  the same as 1.3.1.
- **The update from 1.3.1 keeps everything** on API 36 and API 26: every table's content, the settings file (theme
  Light kept), both pending "Did it help?", the next day's daily reminder, and no crash. On API 36 the 1.3.1 backup
  also imports with every count equal.
- **Static:** the merged manifest differs from 1.3.1's only in the version; permissions are equal and INTERNET is
  absent; PreviewActivity 0; adjustResize 1; the notes are 497 of 500 characters; OpenSpec 30/30. The local gate at
  c1c0afab exits 0 with 3485 tests and 0 failures.
- **Smoke:** 14/14 + 3/3 on API 36, 12/12 + 3/3 on API 26. **Monkey** (API 36, R8, 5000 events): 0 crashes, 0 ANRs.
- **No open S1 or S2. No new defect found.**
- **Recorded risks**, none blocking the tag (details under "Residual risks"):
  1. **Real phones and real TalkBack.** 1.3.1's PENDING-PERSON rows (T64) have not run yet: the lead chose path B, the
     phone after the Play build is live. 1.3.2 adds one phone row, P4 (#239's announcement).
  2. **The Play-signed build** (pairip) is not what QA ran.
  3. **#252** is open with no severity label. Its body says S3, pre-existing: a one-step keyboard on an already focused
     one-line field at 200% can cut the typed line. It sits in the area #245 changed and was not run on a device.
- **Not proven:** see "What these checks do not prove".

## Candidate, scope and builds

- **Verdict SHA:** `release/1.3.2` @ **c1c0afab2e9f0c3a9331eb1866ce4458a18987a8** (T75). Production is 1.3.0 (vc12);
  1.3.1 (vc13, `v1.3.1` = dcb06f9b) is in Play review at 100%.
- **What 1.3.2 ships since `v1.3.1`:** #237 (PR #251, D190, QA record `clear-message-when-intake-logged.md`), #239
  (PR #253, D191, `fix-save-problem-feedback.md`), #245 (PR #256, D192, `fix-short-window-typed-line.md`), and #249
  (`qa/` scripts only). On the release branch: the version bump (92ad4369) and the release notes (c1c0afab).
- **What QA checked about the diff, 2026-11-19:**
  - `git diff --name-only v1.3.1 c1c0afab`, outside `docs/`, `openspec/` and `qa/`: **44 files**. 20 are product
    sources (`app/build.gradle.kts`; `designsystem/input` CursorFollowingTextField, LimitedField, SingleLineFieldInView,
    TopBarAside, TypingSpace; `designsystem/snackbar/MessageLayout`; AttackDetailsScreen; medication presentation 2,
    medication ui 9 with `strings.xml`; DeleteAllDataScreen). 24 are under `src/test*` and are not in the APK.
  - `git diff --quiet 01e301fc c1c0afab -- designsystem features core app/src`: exit 0. The product code QA accepted
    for #245 (01e301fc, which already holds #237 and #239 merged) is the release's product code, byte for byte.
  - `git diff --name-only f70b6450 c1c0afab`: `app/build.gradle.kts`, `docs/release/whats-new.md`. The bump is exactly
    `versionCode = 13 → 14`, `versionName = "1.3.1" → "1.3.2"` (4 changed lines, nothing else).
  - `v1.3.1..c1c0afab` changes no manifest, no Room schema or migration, no ProGuard/R8 or consumer rules (`git diff
    --name-only` filtered for them: 0 files). `.maestro/`: 0 files.
- **Scope (owner rule 2026-11-09):** only what the release diff reaches, with negative controls. The three acceptance
  records ran on **debug** builds, and #237 and #239 on their own branch heads; the release is an R8 build of the three
  together (only #245's record, on 01e301fc, had all three merged). So the new questions are: (1) does the minified build still carry each fix (one row per mechanism, on the
  combined code), (2) the update from 1.3.1 with data, (3) the release manifest and permissions, (4) the R8 build at
  large (smoke, monkey), and (5) what the shared code reaches beyond the three screens: `MessageLayout` is the message
  host of 13 screens, so one T64 message row runs on R8 on a screen that now passes a typing space (Attack details).
- **Builds** (QA's own; detached worktree `migraine-app-qa-rel132` at c1c0afab, clean before and after; no
  `keystore.properties` there, so the release variant is **signed with the debug key**, D102, as in the 1.3.0 and 1.3.1
  records; the upload-key AAB is the engineer's step and is not what QA installed):

  | APK | Source | sha256 | Use |
  |---|---|---|---|
  | `r8-c1c0afab` | candidate, `:app:assembleRelease` (R8, minified) | `c7a027f6f81d8407c3f8366bc1077fb2ccff36fd3243673a8d4ffb8570233ac0` | every candidate row; read back from the device in every row |
  | `debug-c1c0afab` | candidate, `:app:assembleDebug` | `9f280590339bdaf86a094016a440c64cea81a4b77d0c648d5c5fff82bf09c88e` | static control only (debuggable, PreviewActivity present) |
  | `r8-f626c2d0` | QA's 1.3.1 R8 from the 1.3.1 Go/No-Go (`git diff f626c2d0 v1.3.1`: 0 lines) | `702cd44818330a222a0cfb51bc283c74621dc5ff428624e4771e679827180385` | negative control; update base |
  | `r8-v130-2f7745c3` | QA's 1.3.0 R8 from the 1.3.0 Go/No-Go | `c91b0854517d8ce3b64d685f03aece1f2681f81ff324facabefcd1c1e397c6c3` | negative control for the #238 guard (K1), where 1.3.1 cannot fail |
  | `m64` | the #245 record's SW4 mutation (debug, 01e301fc + `TypingSpace.decide` with 64 dp) | `89af83c39dc39e269374a09fa9b5ab3d4b99ddc60949bcd1f8e6661c85ab48a3` | negative control for the API 26 guard |

  `nice -n 19 ./gradlew --max-workers=2 :app:assembleDebug :app:assembleRelease :app:checkReleasePermissions` →
  BUILD SUCCESSFUL in 2m 53s, exit 0; `:app:checkReleasePermissions` ran (log line 835); `git status --porcelain` 0
  after (`migrely-work/qa-rel132/logs/build-c1c0afab.log`). Load 1.3 at the start.
- **Devices:** `Migrely_QA_API_36` (emulator-5580, Android 16 google_apis, Gboard, TalkBack 16, headless), then
  `Migrely_QA_API_26` (emulator-5582, Android 8.0), one at a time, cold-booted. No other QA emulator was running;
  5560 (another project) was up and was not touched.
- **Harness:** the acceptance records' own drivers, unchanged, run on the R8 builds through one wrapper
  (`migrely-work/qa-rel132/bin/rw.py`). It imports the 1.3.1 Go/No-Go's device library first, whose `QA_R8=1` shim maps
  `run-as com.migrely.app X` to root `cd /data/data/com.migrely.app && X` plus a chown back to the app (an R8 build has
  no `run-as`; adbd root on the google_apis image), and it points their output at `qa-rel132/acc/<driver>/`. The only
  addition is to #239's V1: it also keeps every TalkBack line from TalkBack-on, so a capture with no speech is void.
- **Evidence** (outside the repo): `migrely-work/qa-rel132/` (`acc/h237`, `acc/h239`, `acc/q245` row files with the
  device APK hash in every row; `walk/rows.jsonl` for K1 and T64; `logs/` for every step; `raw/` settings snapshots,
  manifests and permission dumps; `static.txt`, `static2.txt`; scripts in `bin/`).

## Static rows on the candidate

| Row | Result |
|---|---|
| Identity (`aapt2 dump badging`) | R8: `com.migrely.app`, **versionCode 14, versionName 1.3.2**, targetSdk 36, not debuggable. Debug: same identity, `application-debuggable`. Control 1.3.1 R8: versionCode 13, 1.3.1 |
| Identity on the device | `dumpsys package`: versionCode=14, versionName=1.3.2, no DEBUGGABLE flag; installed `base.apk` sha256 `c7a027f6…33ac0` read back after the smoke, before and after monkey, in every driver row, and on API 26 after the smoke and after the update |
| Permissions | `aapt2 dump permissions` of the R8 candidate **equals** the 1.3.1 R8 (sorted `diff`, exit 0): ACCESS_NETWORK_STATE, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, WAKE_LOCK, BILLING, DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION. **INTERNET: 0 lines.** `:app:checkReleasePermissions` exit 0 in QA's build |
| Merged manifest | `aapt2 dump xmltree AndroidManifest.xml`, candidate R8 vs 1.3.1 R8: the only difference is `versionCode` 13 → 14 and `versionName` 1.3.1 → 1.3.2 (4 lines) |
| Keyboard mode | `windowSoftInputMode` = 0x10 (**adjustResize**) on the one activity: 1 line, as in 1.3.1 |
| Debug-only components (#195) | `PreviewActivity` in the R8 manifest: **0**; control: the debug APK's manifest has it (1) |
| `qa/release_apk.py` | R8: "code 0, resources 0, manifest 0, debuggable False", exit 0. Control: the debug APK: "code 57, resources 2, manifest 4, debuggable True", exit 1 |
| Signer | candidate R8 and 1.3.1 R8 share certificate SHA-256 `7eae4e48…bfbabd` (QA's debug key), so the update rows are a real `install -r` |
| OpenSpec | `openspec validate --all --strict` at c1c0afab: 30 passed, 0 failed, exit 0 |
| Version bump | `git diff 92ad4369~1 92ad4369`: `app/build.gradle.kts` only, `versionCode = 13 → 14`, `versionName = "1.3.1" → "1.3.2"`; nothing else |
| Release notes | `docs/release/whats-new.md`, 1.3.2 en-US block: **497 characters** (497 UTF-16 units, 4 lines; the only non-ASCII character is "•"), under Play's 500. Each claim has a row below: bullet 1 = #237 A1, bullet 2 = #239 V1, bullet 3 = #245 SW1/SW4 |

## #239 on the R8 build: V1, TalkBack hears exactly one "Enter a name." (Edit medication)

Driver `qa-239/bin/h239.py` row V1, unchanged (through `rw.py`). Edit medication, 100% portrait, Gboard full keyboard
up (IME top 1517 px, read before the press), Name emptied, Save brought above the keyboard by drags; TalkBack on;
console touch on Save (focus), then a double tap. Speech is TalkBack 16's "Speaking fragment" lines at log level
VERBOSE (set and read back before the block: "before: ERROR, after: VERBOSE"). A row is void if nothing at all was
spoken from TalkBack-on (the focus touch on Save must be read). Pass rule from the #239 record: exactly one
"Enter a name." within 3 s of the double tap; never "Invalid input"; the record unchanged.

| Build | Rows | Spoken after the double tap (s) | "Enter a name." within 3 s | Name after | Record |
|---|---|---|---|---|---|
| **R8 candidate** `c7a027f6` | **2/2 valid** (focus touch read "Save", "Button") | 0.57 / 0.60 "Enter a name."; then the app's focus move to Name is read as the field ("Editing. Edit box. Name. Error: Enter a name.", focus event at 0.67 / 0.68, the field's own error at 4.0 / 3.96) | **1 and 1** | in view at y 296–532, keyboard up (1517), input focus in Name | unchanged |
| R8 1.3.1 `702cd448` (control) | 1/1 valid (focus touch read "Save", "Button") | **nothing** in 5 s | **0** | off screen (y 1125–1361), not focused | unchanged |

**R8 carries D191's announcement and the bring-into-view on the combined code.** The field read after the
announcement is the same as on the #245 record's V1 row (01e301fc, debug), which also has #237 and #239 merged.
"Invalid input": 0 on both builds.

## #245's K1 guard on the R8 build: TalkBack, keyboard closed by Back (KR1, Attack details)

Driver `bin/tbrow.py` (#238's driver, as in the 1.3.1 record and the #245 record's K1 row), unchanged. Attack details,
100% portrait, a seeded migraine; TalkBack off: focus the note, type "qa", drag to the end; TalkBack on; two-finger drags
to the end; valid only if TalkBack said "Editing" on the note. Then Back once; 3 s of TalkBack's log; screenshots
before and after (page shift by row-signature votes); one double tap; TalkBack off; database compared. Gboard full
keyboard (top 1517 px) in every row. At 100% portrait the bar never steps aside, so this row guards #238's promise
(closing never moves the page) on the code #245 changed (`CursorFollowingTextField`, `MessageLayout`).

| Build | Rows | Page shift | `TYPE_VIEW_SCROLLED` | Spoken after Back | Double tap | Database |
|---|---|---|---|---|---|---|
| **R8 candidate** `c7a027f6` | **3/3 valid** | **0 px** (153 of 273 row votes, every row) | **0** | only "keyboard hidden" | keyboard reopens in the note ("Showing English (US)") | **unchanged** |
| R8 1.3.0 `c91b0854` (control) | 1/1 valid | **820 px** | **3** | "Delete migraine. Button" (focus pulled off the note) | no keyboard | **changed** (the attack row) |

The control is 1.3.0 because 1.3.1 already holds #238's fix and cannot fail this row. Pass rule fixed before the run
(from #238's record): 0 px, 0 scroll events, focus kept, nothing written.

## Added row: the shared message host on R8 (T64, Attack details with its typing space)

Why it is added: #245 changed `MessageLayout`, the message host of 13 screens (`Slot` now takes a modifier, and a
screen with a text field passes a typing space), and `CursorFollowingTextField`. Attack details is the screen where
both meet. Driver `bin/t64.py` row `dtb`, unchanged (the 1.3.1 record's driver). "Time to take action" 20 s; remove the
logged Ibuprofen on Attack details → "Medication removed" with Undo; TalkBack focus on the message text; held to 30 s;
focus moved away; time to gone. Presence read from screen frames, never from dumps while timing.

| Build | TalkBack focus on the message | Still shown at | Gone after focus left | Spoken on focus |
|---|---|---|---|---|
| **R8 candidate** `c7a027f6` | at 2.72 s | **30.48 s** (focus moved at 30.67 s) | **18.47 s** (17.3 s left when focus came, plus about 1 s capture lag) | "Medication removed" |

Same numbers as the 1.3.1 R8 row (30.6 s, 18.9 s). The row's ability to fail is from the 1.3.1 record, same driver:
1.3.0 R8 was gone by 30.6 s. Not rerun here, because 1.3.1 (the only newer control) holds the pause and cannot fail it.

## #245 on the R8 build: SW1, 360 dp, 200%, landscape (Attack details note)

Driver `qa-245b/bin/q245.py` cell `det-360`, unchanged (the #245 record's method). `wm size 945x2100` (360 dp), font
200%, landscape; a seeded migraine; the page is brought to the note by slow drags before the tap, then only typing and
DPAD keys act. The caret's height (rows that change between two screenshots 0.6 s apart) is the line box: 112 px at
200%. H = keyboard top − page top. "Bar drawn over the page" in pixels: the share of the bar's glyph pixels unchanged
(a bar that is there reads about 1.0). Every setting the cell changed was read back equal afterwards.

| Build | Caret line end / start / middle | Back and the bar | Clearances (top / keyboard) | Keyboard top |
|---|---|---|---|---|
| **R8 candidate** `c7a027f6` | **112 / 112 / 112 of 112 px** | Back gone; bar pixels 0.054 / 0.044 / 0.058; screenshot checked (typed line "Line 9 of a long notex" whole above the keyboard, no title, date or Back over the page) | 122/21, 21/122, 122/21 (the record's numbers) | 318 |
| R8 1.3.1 `702cd448` (control) | **40 / 40 / 40 of 112 px** | Back at [155,129][218,192]; bar pixels 1.0 | visible part 61 px | 318 |

**R8 carries D192's typing space; the control reproduces #245 on the minified 1.3.1.**

## #237 on the R8 build: A1, a successful log clears the storage message (Log medication)

Driver `qa-237/bin/h237.py` rows `prep` and `A1`, unchanged (through `rw.py`). Fresh install, onboarding skipped,
Ibuprofen added and logged once. A1: WAL checkpoint (TRUNCATE) on the stopped app (`0|0|0` every time); Log medication
opened; the list button's place learned; disk filled; tap; dump at 2 s; disk freed; tap again; dumps at 0–2, 2–4 and
8–10 s. Valid only if the app showed its own failure **and** the intake count did not change. On R8 the fill runs as
root (no `run-as`): it stops with 16,380–16,384 KB shown free, which root cannot take; the app still gets "storage
full" there, so validity is judged, as before, by the app's own message and the unchanged database.

| Run | Build | Valid | Intakes before / after the full-disk tap / after | Message after the successful tap (0–2, 2–4, 8–10 s) | Logged line |
|---|---|---|---|---|---|
| m1 row 1 | R8 candidate | **void** (see below) | 1 / 1 / 2 | none, none, none | present |
| m1 row 2 | R8 candidate | **valid** | 2 / 2 / 3 | **none, none, none** | "Ibuprofen logged at 3:08 PM" in all three |
| m1r1 (first row after a fresh install) | R8 candidate | **valid** | 1 / 1 / 2 | **none, none, none** | present in all three |
| m1r2 (the same, again) | R8 candidate | **valid** | 1 / 1 / 2 | **none, none, none** | present in all three |
| m1v | R8 1.3.1 (control) | valid | 1 / 1 / 2 | **"Couldn't save. Your phone's storage is full." at all three** | **not in the tree** at all three |

**3/3 valid candidate rows pass; the control shows #237 on the minified 1.3.1** (the old message stays over the result,
with the dose stored).

**The void row, disproved before filing.** In m1 row 1 the tap on the full disk showed **no** message and wrote
nothing within the 2 s dump (list still shown, intakes 1 → 1). Then the free-space tap logged one intake. If the app
had swallowed a failed save, that would be a defect (a failure with no feedback). Two reruns from the same start (a fresh
install, then A1 as the first row, m1r1 and m1r2, load 2.9–4.7) showed "Couldn't save. Your phone's storage is full."
both times, and m1 row 2 did too. Not reproduced in 3 tries, so not filed; recorded here as a void row with no
explanation. The harness wrote both m1 rows' screenshots under one name, so there is no picture of that tap.

## Update from 1.3.1 (vc13) to 1.3.2 (vc14), R8 to R8

Driver `bin/uprel132.py` (the 1.3.1 record's `uprel131.py`, with OLD = QA's 1.3.1 R8 and NEW = the candidate; nothing
else changed). On a fake date 2026-11-10: install 1.3.1 R8 fresh, seed through the UI with no clear (two migraines,
Severe, Nausea, a custom symptom, Ibuprofen with intakes at 09:00 and 09:05, the daily reminder on, theme **Light** as
a setting away from its default), export a 1.3.1 backup, let the system kill the app, check installd's code_cache mark,
then `install -r` the 1.3.2 R8 **and do not open it**. Every table's content is compared (sha256 of `select * … order
by 1`), and the `shared_prefs` file byte for byte.

| Row | API 36 (run r1) | API 26 (run r1) |
|---|---|---|
| Builds read back | before: `702cd448…` (1.3.1 R8), not debuggable; after `install -r`: `c7a027f6…` (1.3.2 R8), "Success", not debuggable | the same |
| code_cache mark true before the update (the #189/#193 path) | pass | pass |
| Database kept: schema 7 → 7; **every table's content equal** (10 tables: attack 2, attack_tag 2, custom_tag 1, intensity_point 1, medication 1, medication_intake 2, reminder_settings 1, day_mark, doctor_visit, overuse_notice empty) | pass | pass |
| Settings kept: `migrely_preferences.xml` byte-equal, holding `theme = light` after the update | pass | pass |
| Pending "Did it help?" across the update (clock to 11:06, app not opened) | both posted at 11:06:01 | both posted at 11:06:01 |
| The kept-records flow (`upgrade-kept.yaml`) on 1.3.2 | pass | pass |
| The next day's daily reminder (clock to 7:58 PM, app not opened) | posted 8:00:42 PM, "How was today?" with both actions | posted 8:00:02 PM, the same |
| Crash buffer after the update | 0 lines | 0 lines |
| The 1.3.1 backup imported into a clean 1.3.2 | every count equal, file schema 7 | not run (the file picker under Maestro fails on 8.0, as in 1.3.0 and 1.3.1) |

The API 36 run seeded at a host load of about 10 (QA's own gate build ran beside it); the checks are of data and of
notifications that came, not of timing, and every one passed in the first run. The clock was set back to the host's
time and `auto_time` to 1 by the driver (read back: device and host `date +%s` equal at power-off).

## Carried over, and why this release diff cannot reach them

The release's own diff (bump, notes) changes no behaviour. The diff since 1.3.1 is the three fixes, which QA accepted
on debug builds in three records; their product code is byte-equal on the release head (01e301fc vs c1c0afab, above).
What those records could not cover is R8 and the three changes together on one build, which the rows above cover with
one row per mechanism. A row carries when the release diff cannot change what it proved.

**`clear-message-when-intake-logged.md` (#237, D190):**

| Row | Rerun on R8 here? | Carried because |
|---|---|---|
| A1 storage error cleared by a log | **yes**, 3 valid rows, control red | – |
| A2 empty list "Add and log", A3 "Removed…" cleared, A4 dark room, A5 TalkBack speaks the logged line, A6/A7 "Still logged…", A8 200% portrait and landscape, A9 no fade frame | no | Same source; the clearing is one state reduction (`MedicationEventUiModel`/mappers) that A1 exercises on R8 |
| EC-MED-17 E17, E17dd, E17f (removal under a DB write lock), G200, G200L, G200tb2 (800 ms guard) | no | Same source |
| Not shown on a device in that record ("Something went wrong.", "Pick a time…", 50-medication message cleared; rotation or process death during a removal; API 26) | no | Still not proven (below) |

**`fix-save-problem-feedback.md` (#239, D191):**

| Row | Rerun on R8 here? | Carried because |
|---|---|---|
| V1 one "Enter a name." | **yes**, 2 valid rows, control silent | – |
| V2/V3/V3x/V12/V13 Name brought into view, V3T every press heard, V4 cursor in Dose, V5 hardware keyboard, V6/V6-timed, V7 typing quiet (with its mutation), V8 swipe walk, V9 duplicate, V10 message first, V11 dark room, V14 pending, V17, (b) closing race, API 26 rows | no | Same source; V1 on R8 also shows Name in view and focused after the press |
| Recorded limits (V5 with TalkBack on Edit, V6-timed 3 of 4, V12 landscape 200% error line behind the keyboard, V16 RTL, V15) | no | Still limits (below) |

**`fix-short-window-typed-line.md` (#245, D192, on 01e301fc = the release's product code):**

| Row | Rerun on R8 here? | Carried because |
|---|---|---|
| SW1 Details note at 360 dp | **yes**, control red | – |
| K1 KR1 (TalkBack + Back, 0 px) | **yes**, 3/3 on Details, control red (1.3.0) | KR1 on Log, Edit, Delete and K1 KR4 carry: same source, same shared modifier |
| SW4 API 26 guard | **yes** (API 26 below), mutation control red | – |
| SW2 Log/Edit/dose/Delete word at 360 dp, SW3 411 dp bar stays, SW13 reduced margin (with F1 mutation), (a)/(b)/(e), SW6 portrait, (d), SW7, SW8, SW11 dark room, SW12 rotation, SW5/K2 close by Back and hide key, #239 V1 at 360 dp | no | Same source; `TypingSpace` is one shared class for all four screens and SW1 exercises it on R8 |
| SW9 TalkBack walk (#255, S3, pre-existing), SW10 messages and SW12 RTL/hardware keyboard (not run), SW8 frame rate, K2's named control, #254 (S4) | no | Still open or not proven (below) |

**Earlier records:** T64 (`fix-message-timing-and-size.md`) and #238 (`keep-page-when-keyboard-closes.md`) reach the
code #245 changed (`MessageLayout`, `CursorFollowingTextField`); their R8 rows were rerun here (T64 `dtb`, K1). The
rest of those records, and everything older, carries from `go-no-go-1.3.1.md` because 1.3.2 changes no other product
file (20 product files listed above; no manifest, schema, migration, R8 rule, reminder, backup, billing or onboarding
file).

## API 26 (emulator-5582): R8 smoke and the #245 SW4 guard

| Line | Result |
|---|---|
| Smoke, `QA_SERIAL=emulator-5582 QA_APK=r8-c1c0afab.apk qa/maestro.sh --install --tags smoke` | **12/12**, and **3/3** in the 20 s accessibility-timeout pass (API 26 has no timeout setting; the flows run with the default 5 s window and say so); 2 geometry flows skipped below API 29, as designed; exit 0. Device read back: `c7a027f6…`, versionCode 14, 1.3.2, not debuggable |
| SW4 guard, `q245.py det-360,det-411` on the R8 candidate (AOSP keyboard; `show_ime_with_hard_keyboard` 0 → 1 for the cells, put back to 0 and read back) | **360 dp** (H 156): bar stays (Back at [43,129][106,192] before and in every cell, bar pixels 0.997–1.0); line **280–392 (112 px) against the keyboard top at 413**, clearances 23/21, 20/23, 23/21 (113 px at the start). **411 dp** (H 239): bar stays; 112 of 112, clearances 106/21, 21/106, 106/21. The same numbers as the #245 record's debug rows |
| SW4 control, the #245 record's M64 mutation (debug, `89af83c3…`), `det-360` | **the bar is gone** at 360 dp (screenshot `n1m-det-360-error.png`: no Back, no title, the note at the top), and the measurement then stops on a crop at the page top, exactly as in the #245 record. Red, as it must be |

## Go/No-Go lines (`docs/QA.md`)

| Line | Here | Why |
|---|---|---|
| 1 Build gate | QA ran it at c1c0afab: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint --continue` → BUILD SUCCESSFUL in 5m 8s, exit 0; unit tests **3485, 0 failures, 0 errors, 0 skipped** (534 `TEST-*.xml` files; the engineer's count on 92ad4369 is the same); `git status --porcelain` 0 after (`logs/gate.log`) | |
| 1a Lint | 0 errors; **67 warnings + 2 hints** (app 50, designsystem 1 + 2 hints, attack 4, history 2, home 5, medication 2, reminders 1, report 1, **settings 1**). 1.3.1 had 66 + 2. The one new warning is `ModifierParameter` ("Modifier parameter should be named modifier") on `pageModifier` in `DeleteAllDataScreen.kt:332`, added by #245 (8420ceb1). A naming convention, no behaviour; not filed, passed to `android-engineer` as a finding | |
| 1b Instrumented | not rerun | The release diff adds no instrumented test; #245's device proof is the rows above. Debug-only by nature, so R8 does not reach them |
| 2 Smoke | R8 on API 36 and API 26, above | |
| 3 Full regression | not run per release | Owner rule 2026-11-09: nightly on `develop` |
| 4 Accessibility | TalkBack rows above (#239 V1, K1, T64); the phone rows below | |
| 5, 6a Device scenarios, upgrade | 1.3.1 → 1.3.2 above, API 36 and API 26 | |
| 6 Monkey | API 36, R8, the in-tree `qa/monkey.sh` (#249 fixed its launch line and it passes `--pct-syskeys 0`), seed 20261024, 5000 events: **0 crashes, 0 ANRs, "Monkey finished"**, 5000 injected, crashes of other apps 0. Monkey turned on airplane mode and zen mode; the script restored both (read back 0). The device APK read back before and after: `c7a027f6…` | API 26 not run (as in 1.3.1: the same APK, and older API 26 runs passed) |
| 7 Permissions | equal to 1.3.1, no INTERNET | |
| 8 Open defects | `gh issue list --state open`, 2026-11-19 16:20: **no S1 or S2.** S3: #255, #233, #230, #179, #148; S4: #254, #250, #236, #229, #224, #153, #150, #149, #144, #118, #109, #82; **#252 has no label** (its body: S3, pre-existing). Closed by QA in this check after the retest: **#245** | |
| 11, 12 Pre-launch report, internal testing | not run | Play steps are the owner's |

## Phone rows (real TalkBack): open, carried with 1.3.1's

1.3.1's PENDING-PERSON rows P0 to P3 (`go-no-go-1.3.1.md`) have not run. The lead chose path B: they run on the owner's
phone, with the owner present, once a Play build is installed there through Play. The same rows hold for 1.3.2, whose
T64 code is unchanged. 1.3.2 adds one row for #239. Run it in the same session (P0's setup), about 3 minutes:

### P4. TalkBack says "Enter a name." once (#239, D191)

1. With TalkBack on: Migrely, Log medication, Manage medications, open a medication to edit it.
2. Touch Name, double tap. Delete every letter (swipe down then left, or the keyboard's delete key) until Name is
   empty.
3. Swipe right until TalkBack reads "Save, Button". Double tap.
4. Write down everything TalkBack says in the next 5 seconds.

**Expected:** "Enter a name." once, within about 1 second. TalkBack may then read the Name field, including
"Error: Enter a name.". Name is on screen. The medication is not changed.
**Counts as a fail:** nothing about the name in 5 s, "Enter a name." twice before you touch anything, or "Invalid
input": S3, accessibility. A crash or freeze: S1. Restore what P0.3 wrote down.

## What these checks do not prove

- **Real phones and real TalkBack users.** Every row ran on emulator images: Android 16 google_apis with Gboard and
  TalkBack 16, and Android 8.0 AOSP. Real-phone behaviour of T64 (1.3.1's P1/P2) and of #239's announcement (P4) is
  still an emulator result only.
- **The Play build.** QA installed its own R8 APK signed with the debug key. The Play build is made from the AAB, signed
  by Play, and carries Play's automatic protection (pairip), which QA has never run. The tree rule (`git diff c1c0afab
  v1.3.2` empty) ties the source, not the binary.
- **R8 on the other cells of the three records.** One row per mechanism ran on R8: #237 A1 (Log medication), #239 V1
  (Edit medication), #245 SW1 (Details), K1 (Details), SW4 (Details, API 26), T64 `dtb` (Details). The other cells were
  proven on debug builds only. Not on R8: Log medication and Edit at 360 dp, the Delete all data word, the reduced
  margin (SW13), the dark room, and #237's A2 to A9 and EC-MED-17. R8 cannot change layout constants, but it does
  rewrite code. These cells share `TypingSpace`, `SingleLineFieldInView` and the message reduction with the rows that
  ran, which is why one screen each was chosen.
- **R8 for the full-disk path is measured with a root fill.** On an R8 build there is no `run-as`, so the disk was
  filled by root. It stopped with 16 MB shown free, which root could not take. The app reported "storage full" there
  (validity rule met), but this is not the exact 0 KB state of the debug acceptance.
- **The #237 void row** (m1 row 1) is unexplained. It did not reproduce in 3 later rows, 2 of them from the same start.
- **Not run at all, carried as not proven:** #237's "Something went wrong." and "Pick a time…" cleared by a log on a
  device, and rotation or process death during a removal. #239's V5 with TalkBack on Edit, V15 Add past migraine, and
  V16 right to left. #245's SW10 messages with the bar aside, SW12 RTL and hardware keyboard, K2's named control, SW8 at
  60 fps, and #252 on a device. 1.3.1's list: KR13 hardware or floating keyboard and split screen, Switch Access, Voice
  Access, D-pad, Samsung TalkBack and other keyboards.
- **The update's reminders** are shown by a clock moved forward, not by a day passing, on emulators that do not
  deep-sleep. The update ran from QA's 1.3.1 R8 (same tree as `v1.3.1`), not from a Play install.
- **Monkey on API 26** and the backup import on API 26 were not run.

## Residual risks

- **Real-phone TalkBack is unconfirmed for T64 and #239** (P1, P2 and P4). On a phone, TalkBack may order the
  announcement and its own reading of the field differently than on the emulator: in the worst case "Enter a name." is
  said twice or is cut off. Bounded: the field itself also says "Error: Enter a name.".
- **#252 (S3 by its body, pre-existing, unlabelled):** at 200% with a one-step keyboard (API 26–29, or "Remove
  animations" on), an already focused one-line field can keep only 23 of 112 px of the typed line visible. Not made
  worse by #245 (the issue says so); not seen on a device. The 1.3.2 notes promise the line stays above the keyboard
  "when logging or editing a medication" in very large text in landscape. That holds in the cells measured, but #252 is
  a known exception path. Owner of the label: whoever filed it. QA asks `team-lead` to have it labelled.
- **#255 (S3, accessibility, pre-existing):** a swipe-back walk on Attack details cycles among chips. Planned for 1.3.3
  (T76).
- **#254 (S4):** one-line fields lose 1 to 2 px of the line box under the bar in the reduced-margin band. No glyph pixel
  is lost.
- **A new lint warning** (`ModifierParameter`, DeleteAllDataScreen). Style only.
- **Production must get a build whose tree equals the tag:** after the tag, `git diff c1c0afab v1.3.2` must be empty.
  QA checks it when the tag exists.

## Device state at the end

- **emulator-5580 (API 36):** every recorded setting is equal to its boot snapshot
  (`raw/settings-emulator-5580-{boot,end}.json`; only uptime differs). The #237, #239 and #245 drivers' own before/after
  snapshots (10 pairs) show no change. TalkBack's log level went ERROR → VERBOSE → ERROR (read back by `tbll.py`, again just before power-off). TalkBack
  was turned on and off by the drivers. Font 200%, landscape and `wm size` were used by q245 and restored by it (read
  back). Monkey's airplane and zen toggles were restored by the script. The clock moved by the update driver was restored
  (device and host `date +%s` equal at power-off). adbd was made root for the R8 database reads; `adb unroot` was sent
  before power-off, but its `id` line was not captured (root on this image does not survive a cold boot). The app left
  installed is the 1.3.2 R8 candidate. Powered off with `emu kill`.
- **emulator-5582 (API 26):** every recorded setting is equal to its boot snapshot, including the values away from the
  QA baseline that earlier sessions left (zen 3). Font was 1.0 at boot this time. `show_ime_with_hard_keyboard` went
  0 → 1 → 0 for the SW4 cells (read back 0). The clock was restored (device 1795086977, host 1795086978). adbd is back
  to non-root (`id`: uid=2000(shell)). The app left installed is the 1.3.2 R8 candidate. Powered off with `emu kill`.
- **Host:** QA worktree `migraine-app-qa-rel132` detached at c1c0afab, clean (`git status --porcelain` 0). No QA lock
  left. No Gradle daemon stopped. The old worktree `migraine-app-qa-239doc` (merged, clean) was removed. A stale QA
  waiter loop from 2026-11-09 (PID 36577, zsh `until` on a qa-t21 log) was killed by its PID.
