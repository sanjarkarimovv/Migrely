# Release Go/No-Go: Migrely 1.2.1 (versionCode 11)

## Decision

**QA's verdict, 2026-11-11: GO, with the risks below open, for `release/1.2.1` @
0aa3bf4a5e5a77028dbe4317927f261d9f4d4ac4** (PR #198 into `main`, open, not merged by QA).

- **What 1.2.1 ships:** the fix for **#193 (S1)**: on Android 8.0, the first update after "Clear storage" and a reopen
  deleted the diary. It shares a cause with #189 (S3, WorkManager's database). It also ships the new warning signs,
  **#194 (S1 on QA's scale)**, on the NHS emergency and urgent lists, plus the 72-hour card's new line.
- **Earlier candidate e7e6a002: no record of a NO-GO.** That head had the #193 fix only, and #194 was open. Line 8
  would have failed on it. The lead decided on 2026-11-10 to fix #194 inside 1.2.1 (safety first), so no verdict was
  written on e7e6a002. Its rows carry over where the #194 diff cannot reach them (see "Carried over").
- **Open S1 or S2 at the time of this record:** #194, fixed in this candidate; its reporter closes it after the merge into
  `develop`; none other (`gh issue list --state open`).
- **#194 severity:** QA rates #194 S1 ("wrong or missing safety warning"). The issue still carries the label S2: the
  system blocked QA from relabelling or commenting on an issue QA did not file, and team-lead agreed to S1.
- **Counts:** below, every row with its build hash. No row failed on the candidate. Every negative control failed as
  it should.
- **"Did it help?" after an update with no clear on Android 8.0** (the lead's question; it was open since 1.2.0, whose
  API 26 rows were confounded, see the amendment in `go-no-go-1.2.0.md`): **yes**. In real time, with no clock change after
  the update and the app not opened, it posted at 11:00:00 for the 09:00 intake. JobScheduler started the app's process
  (WorkManager's SystemJobService), and the 09:05 intake's job ran at 11:05. This is one run.

## Candidate, scope and builds

- **Candidate:** 0aa3bf4a = merge of PR #201 (#194, `bugfix/194-warning-signs`) into `release/1.2.1` e7e6a002.
  - e7e6a002 = develop bdba0d52 (PR #196, the #193 fix) + `app/build.gradle.kts` (versionCode 10 → 11, versionName
    1.2.0 → 1.2.1) + `docs/release/whats-new.md`.
  - `git diff e7e6a002 0aa3bf4a`: two `strings.xml` (settings/ui; home/ui `long_check_body`),
    `SettingsPages.kt` `WarningSignsContents`, a Robolectric test, BRD and OpenSpec. Nothing under storage, reminders
    or workers; no manifest, no Gradle.
  - `git diff 93f8c00f e7e6a002 -- app/src/main gradle '*.toml' app/proguard-rules.pro` is empty: the #193
    acceptance rows (task 5.5, built at 93f8c00f) are on the same product code.
- **Scope (owner rule 2026-11-09):** only what `v1.2.0..0aa3bf4a` can reach:
  - the #193 fix (`MigrelyApplication.attachBaseContext` → `getCodeCacheDir`), on API 26 and 36;
  - the version change: the update path from the version in review (1.1.1), identity and permissions;
  - the #194 page and the 72-hour card (OpenSpec `fix-warning-signs-list` tasks 6.1 to 6.5).
  Everything else carries over from the 1.2.0 GO (826887f2).
- **Builds** (QA's own, from detached QA worktrees `migraine-app-qa-193`, `-qa-121`, `-qa-121b`, clean before and after;
  no `keystore.properties`, so release builds are signed with the debug key, D102; every APK sha256-checked on the host
  before each install and on the device after it, and rows counted by the device hash):

  | APK | SHA | sha256 | Use |
  |---|---|---|---|
  | `r8-0aa3bf4a` | candidate | 5990d018c90d165b… | every candidate row |
  | `debug-0aa3bf4a` | candidate | 1455aad531ac932c… | `release_apk.py` control only |
  | `r8-e7e6a002` | first head | 56e2d97a4ebbff6f… | carry-over rows; #194 negative control |
  | `r8-93f8c00f` | fix branch | 4876b65cb708f98c… | #193 task 5.5 rows |
  | `r8-9d453bb5` | 1.2.0 | 79a0941932eb839e… | #193 negative control |
  | `r8-eeb6` | 1.1.1 tree | b36a3fcbd4522793… | upgrade base; EC-STO-10 residual |

- **What the R8 output shows:**
  - e7e6a002 vs 93f8c00f: 768 entries, only `AndroidManifest.xml` differs (the version), so `classes.dex` is byte for
    byte the same.
  - 0aa3bf4a vs e7e6a002: `classes.dex`, `resources.arsc`, one res file and the baseline profile differ. The new
    strings move resource ids, which R8 inlines, and one new class shifts R8's names. So dex identity is not available,
    and the reach-critical rows were run again on the candidate APK (below).
  - In every candidate dex, `MigrelyApplication.attachBaseContext` still runs `super`, then `getCodeCacheDir`, then
    the catch that logs "code_cache check failed: <kind>".
- **Devices:** `Migrely_QA_API_26` (emulator-5582, Android 8.0 AOSP) and `Migrely_QA_API_36` (emulator-5580, Android
  16), one at a time, cold-booted. Root was used only to read (stat, getfattr, cat) and for the #193 rows' one `mkdir`
  outside app data, plus the hold row's folders in the app's data dir, removed after.
- **Evidence** (outside the repo): `migrely-work/qa-193/` (`HANDOVER.md`, `rows.jsonl`, `raw/`, `acc.py`, `count.py`)
  and `migrely-work/qa-121/` (`PLAN.md`, `rows.jsonl`, `rows194.jsonl`, `logs/rows.jsonl`, `logs/`, `shots/`,
  `up121.py`, `t4q.py`, `wsp.py`, `tbw.py`, `lay.py`).

## The rows

### #193: the code_cache mark (task 5.5 at 93f8c00f, carried; re-run on e7e6a002 and 0aa3bf4a)

The mark is installd's `user.inode_code_cache` on `/data/data/com.migrely.app`, read with root `toybox getfattr`. On
API 26 and 36 toybox prints it only up to its first zero byte, so the printed bytes are compared with each folder's
inode (little-endian). Every run first checks the reader on a fresh install, where the mark must equal `code_cache`.

| Arm (API 26 unless named) | Fix | 1.2.0 R8 control |
|---|---|---|
| churn1: clear, one root `mkdir` outside app data, reopen, `install -r` | `migrely.db` kept 3/3 (93f8c00f), 1/1 (e7e6a002) | deleted 3/3 |
| hold: after the clear, root folders **in** the app's data dir until one takes the old inode (forced dangerous state) | sentinel kept 3/3 (93f8c00f), 1/1 (e7e6a002), 1/1 (0aa3bf4a); mark rewritten to the new `code_cache` each time | held folder emptied 2/2, 1/1 (on e7e6a002's boot) |
| plain clear, reopen, reminder on | `migrely.db`, WorkManager db, WorkSpec rows, JobScheduler job kept 3/3 | WorkManager db deleted 3/3 |
| plain clear; daily due 5 min after the update, hands off | posted on time 1/1 | not posted 1/1, "Didn't find WorkSpec" |
| first start after the clear from a receiver (no activity) | mark rewritten, nothing lost 2/2 | WorkManager db deleted 1/1 |
| no clear, plain update | everything kept 2/2, mark unchanged | — |
| **residual EC-STO-10:** clear + reopen on 1.1.1, then update to the fix | `migrely.db` deleted 3/3 (expected, not protected); mark true afterwards; a second update deleted nothing 3/3 | — |
| API 36, every arm above but resid and timed | nothing lost 9/9 (93f8c00f), 2/2 (e7e6a002), 1/1 (0aa3bf4a); mark true after every reopen that followed a clear | nothing lost 5/5; no mark after the reopen |
| Instrumented `ContextCodeCacheFolderTest` (missing folder → re-created and mark == inode) | 2/2 on API 26 and 2/2 on API 36 (e7e6a002) | engineer's negative control on 5570 (call removed → 1 of 2 fails) |

In churn1 with the fix, `code_cache` took back its old inode 3/3, so those rows prove the folder exists, not that the
mark was written. The rewrite itself is shown by hold, plain, receiver and timed. In those rows another folder held
the old inode (`databases/` with no forcing in plain, receiver and timed), and the mark moved to the new `code_cache`.

### The version: identity, permissions, the update from 1.1.1

| Row | e7e6a002 | 0aa3bf4a |
|---|---|---|
| R1 identity: `com.migrely.app`, versionCode 11, versionName 1.2.1, not debuggable, minified, the debug-key signer | pass | pass |
| R1 permissions: `aapt2` badging and `:app:checkReleasePermissions` equal to 1.2.0's six; no INTERNET | pass | pass |
| R1 `qa/release_apk.py`: no simulated store (control: the debug APK has it, 57/2/4) | pass | pass |
| R2 1.1.1 R8 → 1.2.1 R8 on API 36: mark true before the update; DB 6 → 7, every table equal (only `doctor_visit` new); kept-flow; "Did it help?" posts (clock to 11:06); the next day's daily posts; the 1.1.1 backup imports with every count equal | 2/2 | 1/1 |
| R2 the same on API 26 (no backup import: the file picker fails under Maestro on 8.0, a harness limit) | 2/2 | 1/1 |
| Smoke (`qa/maestro.sh --tags smoke` on the R8 APK) | 3/3 on 26, 3/3 on 36 | 3/3 on 36, 3/3 on 26 |

The upgrade seed never clears (`qa-121/flows/upgrade-seed-noclear.yaml`), and the driver voids a row whose mark is
stale before the update. 1.2.0's API 26 upgrade rows did clear (Maestro `clearState`), see that record's amendment.
This PR also removes `clearState` from `.maestro/device/upgrade-seed.yaml`. The edited flow passes on API 26 with 1.1.1 R8
(`qa/maestro.sh`, a fresh install), and the mark still points to `code_cache` after it. `qa/device_scenarios.sh upgrade`
itself was not run on API 26: the script pins `emulator-5580` (a QA tooling follow-up).

### "Did it help?" after an update with no clear, Android 8.0 (the lead's question)

| Row | Build | Result |
|---|---|---|
| R2-121-26: the clock moves to the due after the update (TIME_SET wakes the app) | 1.2.1 | posts 2/2 |
| T4Q quiet: the clock moves to 10:57 **before** the update; no change after it | 1.2.1 | 0/3 |
| T4Q quiet, control 1.1.1 → 1.1.1 | 1.1.1 | 0/1 |
| T4Q method control, no update | 1.1.1 | posts 1/1 (11:00:01, in process) |
| T4Q after the update, the app's jobs run by force at about 10:57 | 1.2.1 | the worker runs (SUCCESS) and asks nothing 2/2: the intakes are under 2 h old, and the window is 2 h to 24 h (`EffectQuestionWindow.kt`) |
| T4Q real time: intakes at 09:00 and 09:05, update about 09:07, no clock change after, wait to 11:12 | 1.2.1 (0aa3bf4a) | **posts 1/1 at 11:00:00**; jobs "earliest=+1h53m/+1h58m" right after the install; both ran from JobScheduler |

Why the quiet rows miss:
- The clock jump moves the wall clock. The JobScheduler job keeps its due on elapsed time: after the update,
  `dumpsys jobscheduler` shows "earliest=+1h53m", which is the intake plus 2 h of real time.
- Without an update, WorkManager's in-process scheduler catches the wall-clock due; an update kills that process.
- So those rows measure the clock method, not a fault. The real-time row is the end-to-end answer.

### #194: Warning signs and the 72-hour card (OpenSpec `fix-warning-signs-list` tasks 6.1 to 6.5)

| Task | Check | 0aa3bf4a | e7e6a002 (negative control) |
|---|---|---|---|
| 6.1 | `app-settings/sections-and-pages.yaml`, `a11y-tour/settings-warning-signs.yaml` (`qa/maestro.sh`, R8, API 36) | pass, pass | fail, fail (the new heading is missing) |
| 6.1 | `device/long-attack-still-going.yaml` in `qa/device_scenarios.sh long` (72 h by the clock, R8, API 36) | All scenarios passed | fail at `long-attack-still-going` |
| 6.2 | UI tree, top to bottom: title, H1, 9 signs, "Don't drive…", H2, 3 urgent items, last line; About's last paragraph; character for character with the spec (`wsp.py`) | 18 nodes in order, About exact, no old line, no glyph node: API 36 and API 26 | old 8 nodes and the old About line: differs, as it must (API 36 and 26) |
| 6.2 | 72-hour card text `long_check_body` | shown by the flow above ("If it.s still going after 72 hours, get urgent medical advice\.") | old text |
| 6.3 | TalkBack 16 on API 36, focus moved by real touch events (emulator console), speech from TalkBack's VERBOSE log (`tbw.py`) | 18 stops in the spec's order, then it wraps to Back; the title, H1 and H2 end in "Heading"; heading navigation (reading control Headings): title → H1 → H2 → "No next heading"; no glyph spoken | 8 stops; heading navigation: no part heading |
| 6.4 (a) | 320 dp wide (`wm density 540`), 200% font, dark (36) | H1 whole on the first screen (bottom at 915 of 2400 px), API 36 and 26 | — |
| 6.4 (b) | Landscape (`adb emu rotate`), 200% | the app bar title "Warning signs" whole (screenshots, 36 and 26) | — |
| 6.4 (c) | Scroll to the last line | whole, no node past the right edge (36, 26) | — |
| 6.4 (d) | The gap before H2 | 81 px, against 30 to 54 px between the other lines (36, 26) | — |
| 6.4 (e) | `ar-XB` app locale (36) | **not provable:** the app is not mirrored (no pseudo-locales in the build, the English-only app stays LTR; as in earlier records). The bullet stays on the left | — |

Settings restored after each run and checked:
- TalkBack: off, log level ERROR, `touch_exploration_enabled` 0;
- display: density reset, font 1.0, night off, rotation 0 with auto-rotate on;
- app locales cleared.

Two harness notes:
- `input` taps, swipes and key combinations do not reach TalkBack on this image; emulator-console touches do.
- `user_rotation` alone does not rotate the app on API 36; `adb emu rotate` does.

## Voids (harness slips, each rerun; not counted)

- API 36 #193 first attempt (3 rows): the reader expected 8 bytes; toybox prints up to the first zero byte. The
  self-check caught it, the reader was fixed, and the rows were rerun.
- API 26 `noclear` (2 rows): the driver tried to skip onboarding twice. Fixed and rerun.
- R2 dry run on API 26: a flow path pointed at a removed worktree. Fixed; that run used the fix APK and is not counted.
- T4Q: two runs launched under zsh with unsplit arguments died before touching the device.
- 6.4 (b) first two runs: the screenshot was portrait (`user_rotation`, then `wm user-rotation`, did not hold). Rerun
  with `adb emu rotate`.
- Host disk under 1 GB at about 17:10 on 2026-11-10: no row ran in that window. The 1.2.1 build had finished at 17:04,
  and its APK passes `apksigner verify` and `unzip -t`. 5582 was cold-booted before any row.

## Carried over, and why this diff cannot reach them

| Go/No-Go line (`docs/QA.md`) | Here | Why it carries |
|---|---|---|
| 1 Build gate | not run by QA: engineer's gate on e7e6a002 exit 0 (3009 tests); lead gate on db1a23ad (= 0aa3bf4a code) exit 0, and on 0aa3bf4a itself running when this was written; QA's own `assembleDebug assembleRelease :app:checkReleasePermissions` exit 0 at both SHAs | the gate is the engineer's and lead's; QA's builds show it compiles |
| 1a Lint warnings | not reviewed | the lead gate runs Lint; the #194 diff is one composable |
| 1b Instrumented | `ContextCodeCacheFolderTest` 2/2 on 26 and 36; the full suite not run | the #194 diff adds no instrumented test and touches no instrumented area |
| 2 Smoke on 8.0 | run (above) | — |
| 2a Split APKs | not run | no AAB yet; the diff touches no locale or split config |
| 3 Full regression | the three changed flows only | nightly on `develop`; the diff reaches one page and one card |
| 4 Accessibility audit | the Warning signs page by 6.3 and 6.4; `a11y-tour/settings-warning-signs` passes | no other screen changed |
| 5 Device scenarios | `long` (above) | no other scenario reaches the diff |
| 6 Monkey | not run | no new interaction; the start path is covered by smoke and the upgrade |
| 6a Upgrade | R2 (above) | — |
| 7 Permissions | equal to 1.2.0 (above) | — |
| 8 Open defects | #194 fixed in the candidate; no other open S1 or S2 | — |
| 9 Coverage | `qa/e2e_coverage.py --below 50 --register docs/qa/test-strategy.md` exit 0 after this PR updates the register (app-shell 6/190, attack-details 6/66, in-app-messages 7/81, subscription 22/74, doctor-visit 12/76, release-build 0/5; 290 of 1074) | — |
| 10 R8 | every row ran on R8 | — |
| 11 Pre-launch report, 12 internal testing | not run | Play steps are the owner's; no uploads while 1.1.1 is in review |

## What these checks do not prove

- **Whether the warning-signs wording is clinically right** (T31: a clinician must sign every safety sentence); QA
  checked the text against the spec, not against medicine. **Whether a person in pain understands it.** Behaviour on a
  real phone with real TalkBack users.
- **Real phones, OEM installd and f2fs**: emulator images only (Android 8.0 AOSP, Android 16 google_apis). Android 8.1
  to 15 were not run, for #193 or #194.
- **The upload-key signed AAB**: QA's R8 APKs use the debug key; split APKs from the bundle were not installed.
- **A work profile or second user** (EC-STO-08), **an exception inside the check on a device** (the catch is
  JVM-tested only), **a provider-first start**, **Auto Backup restore mode**.
- **Dark mode on 8.0**: there is no system dark theme before API 29, so 6.4 (a) dark ran on API 36 only. **RTL**
  (6.4 e): not provable.
- **Counts of one**: the candidate re-runs (hold, upgrade, smoke on 0aa3bf4a) are one run each; the e7e6a002 and
  93f8c00f rows carry the counts, on byte-identical (e7e6a002) or same-source (0aa3bf4a) code.
- **The mark read on API 26 is a prefix** (toybox stops at a zero byte). A mark whose low byte is 0 would read as
  ambiguous; every printed value here had 3 bytes.
- **Capabilities under 50% E2E coverage** (line 9): app-identity, app-shell 3%, attack-details 9%, attack-mode,
  brand-typography, custom-tags, doctor-visit 16%, in-app-messages 9%, in-app-review, medication-intakes,
  medication-list, month-sky, past-attacks, quality-gates, release-build, subscription 30%, website. Their follow-ups
  stay in `docs/qa/test-strategy.md`. The #193 requirement is covered by device scripts, not flows (noted on the
  app-shell row).

## Residual risks

- **EC-STO-10, not protected by design.** A "Clear storage" and reopen made while 1.1.1 (in review) or 1.2.0 was
  installed is still unprotected at the update to 1.2.1. On 8.0 the update then deletes the diary (3/3) or WorkManager's
  database. The user lands on Home with an empty diary and no message. Later updates are safe. Proven on 8.0, absent
  on 16, unknown on 8.1 to 15. The reminder side is #197 (S3).
- **T31:** the new safety text is the medical-reviewer's wording on the NHS lists, not yet signed by a clinician. The
  lead accepted this as a residual for all safety copy; it is not a blocker.
- **The REPLACE re-plan suspicion** (design.md of `fix-code-cache-after-clear`) is unverified and on QA's list.
- **Production must get a build whose tree equals the tag:** after the tag, `git diff 0aa3bf4a v1.2.1` must be empty.
