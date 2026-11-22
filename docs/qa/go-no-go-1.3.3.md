# Release Go/No-Go: Migrely 1.3.3 (versionCode 15)

## Decision

**QA (qa-259), 2026-11-22: GO** for `release/1.3.3` @ **d023ba768b0fa2682bdc6704465595c703a24c0c** (tree f21fa451).
The record lists what these checks do not prove and the open risks.

- **The only new content is #259 (D194).** QA accepted it on 2026-11-22 in `fix-keyboard-follow-under-talkback.md`
  (9967de02), on debug and R8 builds of d7aa097e. The release diff since d7aa097e is docs, openspec and the version bump.
- **Fresh rows on the release build (QA's own R8 of d023ba76), all started and ended at a 1-minute host load under 8:**
  - #259 core:
    - T1 3/3 and reopen U3 3/3: TalkBack focus kept, 0 writes.
    - T6 TalkBack close 2/2 judged on the close itself (194 px = the bar's height).
    - The v1.3.2 R8 control fails the same U3 row: focus to an impact radio, impact written.
  - Update 1.3.1 R8 → 1.3.3 R8 with real data: every check passes.
  - Smoke 17/17 flows.
  - Monkey 5000 events: 0 crashes, 0 ANRs.
- No S1 or S2 is open. #259 (S1) was closed by QA after its retest.

## Candidate, scope and builds

**What the release diff reaches (checked by QA with `git diff --name-only`, 2026-11-22):**

| Range | Paths outside `docs/` and `openspec/` |
|---|---|
| d7aa097e (QA's #259 GO) → d023ba76 | `app/build.gradle.kts` only: `versionCode = 14 → 15`, `versionName = "1.3.2" → "1.3.3"` (4 changed lines) |
| v1.3.2 → d023ba76 | the bump, `designsystem/input` (CursorFollowingTextField, KeyboardFollow, SingleLineFieldInView, TypingSpace), the four screens (AttackDetails, LogMedication, EditMedication, DeleteAllData), and tests (`src/testDebug`, not in the APK) |
| v1.3.1 → d023ba76 | #237, #239, #245 (1.3.2's content) + #259 + `qa/` scripts. **No manifest, Room schema, migration, backup or R8-rule file** (`grep` of the name list: 0) |

**Release notes:** `docs/release/whats-new.md`, the 1.3.3 block = the 1.3.2 en-US text, 497 characters. Its
claims were proven in 1.3.2's GO (#237 A1, #239 V1, #245 SW1). #259 restores expected behaviour and adds no claim.

**Scope (owner rule 2026-11-09):** what the diff reaches, with negative controls. The bump changes no behaviour. The
#259 code was accepted on debug, and on an R8 build of d7aa097e at loads of 13 to 72. That R8 is the same product code
as this release, but it is a different binary, so the #259 core runs again on the release's own R8 at low load. Also
re-run: the update, the smoke and monkey on the release binary.

**Build (QA's own):**
- Worktree `migraine-app-qa-rel133` at d023ba76, clean before and after (`git status --porcelain`: 0).
- `nice -n 19 ./gradlew --max-workers=2 :app:assembleRelease :app:checkReleasePermissions` → BUILD SUCCESSFUL in
  2 m 7 s, exit 0; `checkReleasePermissions` ran. Load 4.9 at the start.
- There is no `keystore.properties` in the QA worktree, so the APK is signed with QA's debug key, as in 1.3.0 to 1.3.2.

| APK | sha256 | Use |
|---|---|---|
| `r8-d023ba76` (candidate) | `0c892c21163f169698e210a7505c8d947d60d56ea04e02c22af3ffc33733f33d` | every candidate row |
| `r8-f626c2d0` (1.3.1 R8, tree = `v1.3.1`) | `702cd44818330a22…` | update base |
| `r8-c1c0afab` (1.3.2 R8, tree = `v1.3.2`) | `c7a027f6f81d8407…` | negative control |

## Static rows

| Row | Result |
|---|---|
| Identity (`aapt2 dump badging`) | `com.migrely.app`, **versionCode 15, versionName 1.3.3**, targetSdk 36, not debuggable |
| Identity on the device | after every step (`devsha.sh`): base.apk sha256 `0c892c21…f33d`, versionCode=15, versionName=1.3.3, debuggable=0. Every TalkBack row logs the installed hash (`0c892c21163f1696`) |
| Permissions | `aapt2 dump permissions` equals the 1.3.1 R8 (sorted diff, exit 0). The 6 uses-permission are POST_NOTIFICATIONS, ACCESS_NETWORK_STATE, BILLING, WAKE_LOCK, RECEIVE_BOOT_COMPLETED and DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION. **No INTERNET** (0 lines) |
| Merged manifest | `aapt2 dump xmltree AndroidManifest.xml` against the 1.3.1 R8: the only difference is versionCode 13 → 15 and versionName 1.3.1 → 1.3.3 (4 lines) |
| `qa/release_apk.py` | "Release APK ok: no simulated store, not debuggable", exit 0 |
| Signer | candidate and 1.3.1/1.3.2 R8 share certificate SHA-256 `7eae4e48…` (QA's debug key), so the update is a real `install -r` |
| OpenSpec | `openspec validate --all --strict` at d023ba76: 30 passed, 0 failed |

## #259 core on the release R8 (emulator-5580, API 36, Gboard, TalkBack 16, 360 dp landscape at 200%)

Driver `migrely-work/qa-259/bin/x259q.py` (#259's acceptance harness, unchanged rules) with `QA_R8=1`: database reads
are read-only, through adbd root. TalkBack's log level went ERROR → VERBOSE → ERROR (read back). Rows are in
`migrely-work/qa-259/rows.jsonl`, prefixes `g1`, `g3`, `g6`, `gd`.

| Row | Result | Load (start/end) |
|---|---|---|
| T1 ×3: TalkBack double tap opens the note, 2nd double tap; DB read before and after each | **3/3 PASS**: focus on the note, 0 writes, line 112/112 | 2.6 to 3.5 |
| U3 ×3: reopen with the note's top about 63 px above the page's edge, caret at the end of 4 lines | **3/3 PASS**: focus kept, 0 writes. H1/H2 hold in every captured frame (kept 70, 70, 71 px of 63, 63, 64 shown). Capture during the motion: median 18 ms, the keyboard rose over 16 to 17 frames | 2.3 to 4.0 |
| T6 ×2: TalkBack on, close by Back and by the hide key, the log cleared before TalkBack starts | **2/2 PASS**: close shift 194 px = the bar's height; TalkBack's own start shift 0; 0 TYPE_VIEW_SCROLLED after the close; no focus move | 2.1 to 2.9 |
| Control: v1.3.2 R8, the same U3 row | **FAIL** as expected: focus to "1 · Less active" (ENSURE_ON_SCREEN) at +0.67 s; the next double tap wrote impact NULL → 1 | 1.6 |

#262 (S4: 1 px less held at a one-step inset) did not show here: at low load the keyboard rose in steps.

## Update from 1.3.1 (vc13) to 1.3.3 (vc15), R8 to R8 (API 36)

- Driver `migrely-work/qa-rel133/bin/uprel133.py`: the 1.3.2 GO's `uprel132.py`, with paths moved to `qa-rel133`;
  NEW = the 1.3.3 R8; checks unchanged. Its row names still say 131/132.
- Seeding: on a fake date 2026-11-10, install 1.3.1 R8 fresh and seed through the UI:
  - two migraines, Severe, Nausea, a custom symptom;
  - Ibuprofen with intakes at 09:00 and 09:05;
  - the daily reminder on;
  - theme Light.
- Export a 1.3.1 backup, then `install -r` the 1.3.3 R8 **without opening it**.

| Row | Result |
|---|---|
| Builds read back | before `702cd448…` (1.3.1 R8), not debuggable; after: `0c892c21…` (1.3.3 R8), "Success", not debuggable |
| code_cache mark before the update (#189/#193 path) | pass |
| Database: schema 7 → 7, **every table's content equal** (sha256 of each table: attack 2, attack_tag 2, custom_tag 1, intensity_point 1, medication 1, medication_intake 2, reminder_settings 1; day_mark, doctor_visit, overuse_notice empty) | pass |
| Settings: `migrely_preferences.xml` byte-equal (`62eb22fc…`, 166 bytes) | pass |
| Pending "Did it help?" across the update (clock 11:06, app not opened) | both posted at 11:06:01 |
| Kept-records flow on 1.3.3 | pass |
| Next day's daily reminder (clock 20:00, app not opened) | posted 20:00:32, "How was today?" with both actions |
| Crash buffer after the update | 0 lines |
| The 1.3.1 backup imported into a clean 1.3.3 | every count equal, file schema 7 |

All rows ran at load 2.2 to 7.6. The clock went back to the host's time with auto_time 1 (read back: device and host
epoch equal).

**API 26 is carried over from 1.3.2's GO.** 1.3.1 → 1.3.2 passed on API 26 on 2026-11-19, and since v1.3.1 the
release changes no data-layer, schema, migration, backup or manifest file.

## Smoke and monkey (R8, API 36)

- **Smoke:** `QA_APK=<1.3.3 R8> qa/maestro.sh --install --tags smoke` (in-tree, d023ba76). **17 of 17 flows passed**
  (14 + 3), 0 failed; exit 0.
- **Monkey:** in-tree `qa/monkey.sh`, seed 20261024, 5000 events, on the 1.3.3 R8. **0 crashes, 0 ANRs, "Monkey
  finished"**, crashes of other apps 0. Monkey switched Do Not Disturb on, and the script restored it (zen_mode 0, read
  back). Started at load 7.0.

## Carried over, and why the release diff cannot reach them

| Record | Carried | Why |
|---|---|---|
| `fix-keyboard-follow-under-talkback.md` (#259, 9967de02) | every row not re-run above: T2, T3 (four screens, 360 and 411), T4, T5, T6 TalkBack off and on the other screens, T7, M1 to M5, K1/K2, 7.3 on API 26, controls | same product code (d7aa097e → d023ba76: docs and the bump) |
| `go-no-go-1.3.2.md`, #237 A1, #239 V1 (R8 rows) | yes | no file of #237 or #239 changed since v1.3.2 (`git diff v1.3.2 d023ba76`: the list above has no medication presentation, mapper, message or strings file) |
| `go-no-go-1.3.2.md`, #245 SW1, K1 and SW4 (R8 rows) | yes, with a note | #259 changed `TypingSpace`, `SingleLineFieldInView` and `CursorFollowingTextField`, which #245 also uses. #259's acceptance re-measured #245's results on the new code: M2 rest positions equal v1.3.2 to the pixel at 360 and 411 dp; T5 line 112/112 with the bar aside on all four screens; the closes keep exactly the bar's height (#238 / K1) |
| `go-no-go-1.3.2.md`, T64 message host | yes | `MessageLayout` is unchanged since v1.3.2 |
| 1.3.2's API 26 smoke and SW4 guard; everything older | yes | no other product file changed |

## Go/No-Go lines (`docs/QA.md`)

| Line | Here | Why |
|---|---|---|
| 1 Build gate | not re-run by QA | the #259 merge gate is team-lead's; QA has not seen its output. The last gate QA read is the engineer's `r9-gate.log` (BUILD SUCCESSFUL, written 2026-11-20 11:49, with the commit d7aa097e at 11:49). The release diff since d7aa097e is the bump and docs. QA built the R8 itself (exit 0) |
| 1b Instrumented | not re-run | the release diff adds none |
| 2 Smoke | R8, API 36: 17/17 | API 26 carried (above) |
| 3 Full regression | not per release | owner rule 2026-11-09 |
| 4 Accessibility | TalkBack rows above; #259's acceptance | |
| 5, 6a Update | 1.3.1 → 1.3.3, API 36 | API 26 carried |
| 6 Monkey | R8, API 36: 0 crashes, 0 ANRs | |
| 7 Permissions | equal to 1.3.1, no INTERNET | |
| 8 Open defects | `gh issue list --state open`, 2026-11-22: **no S1 or S2**. S3: #148, #179, #230, #233, #252, #255, #264. S4: #82, #109, #118, #144, #149, #150, #153, #224, #229, #236, #250, #254, #260, #262 | |
| 11, 12 Pre-launch report, internal testing | not run | Play steps are the owner's |

## Known limits shipped with 1.3.3 (team-lead decisions, 2026-11-22)

- **#264 (S3):** rotating with the keyboard up and TalkBack on closes the keyboard and moves focus to Back. It is the
  same in production v1.3.1; 1.3.3 does not make it worse. Planned for 1.4.0.
- **#262 (S4):** at a one-step keyboard inset the reopen hold keeps 1 px less than showed.
- **H5, K2, K3, #260:** see the #259 record.
- **#255 (S3):** the TalkBack swipe walk on Attack details. Planned for 1.4.0.

## What these checks do not prove

- **Play signing and the Play build.** QA installed its own R8 APK signed with QA's debug key. The production build is
  made from the AAB, signed by Play, with Play's protection, which QA has never run. Tying source to production rests on
  the tree rule: after the tag, `git diff d023ba76 v1.3.3` must be empty. QA checks it when the tag exists.
- **Real phones and real TalkBack users.** Every row ran on emulator images. The TalkBack phone rows from 1.3.1 are
  still open.
- **Rates.** 3 + 3 release openings and #259's 37 debug openings bound a focus-loss rate only below about 7%.
- **The update** ran from QA's 1.3.1 R8 (same tree as `v1.3.1`), not from a Play install. Reminders were shown by
  moving the clock, on an emulator that does not deep-sleep. API 26 was not re-run (carried).
- **Monkey** ran on API 36 only.

## Residual risks

- #264 reaches 1.3.3 users exactly as it reaches 1.3.1 users today.
- **Production must get the tagged tree:** `git diff d023ba76 v1.3.3` must be empty after the tag.

## Device state at the end

- emulator-5580 (only it was used) was read back before power-off:
  - TalkBack off; TalkBack log level ERROR (read back);
  - font 1.0; user_rotation 0, accelerometer 1; animator scales as found (null, 1.0, 1.0); a11y timeouts null;
  - zen_mode 0, airplane 0, auto_time 1; device epoch = host epoch; `wm size` without override;
  - adbd unrooted and read back as shell.
- It was shut down with `emu kill`. emulator-5582 was not started.
- Evidence (outside the repo): `migrely-work/qa-rel133/` (`logs/` build, chain, update, smoke, monkey, end state;
  `raw/` permissions and manifests; `apk/`), and `migrely-work/qa-259/rows.jsonl` and `frames/` for the #259 rows.
