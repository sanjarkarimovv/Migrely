# QA record: `fix-release-debug-components` (#195, D172), task 5.5

## Decision

**QA's verdict, 2026-11-11 at 17:15: PASS for `bugfix/195-preview-activity-release` at ceb6bb30.** Nothing new was filed.

- **The fix works on a device.**
  - The candidate's R8 release APK has no PreviewActivity. `am start -n com.migrely.app/androidx.compose.ui.tooling.PreviewActivity` gets
    "Activity class … does not exist" 4 of 4 times: twice on API 36 and twice on API 26.
  - **Control:** the v1.2.1 R8 APK starts it 4 of 4 times. Any caller starts the app's process for it ("Start proc … for activity
    …PreviewActivity"). The activity then logs "Application is not debuggable. Compose Preview not allowed." and closes.
  - So in 1.2.1 the entry point was real, but it showed nothing. This supports keeping #195 at S3.
- **Debug is unchanged.** The candidate's debug APK has the same sha256 as v1.2.1's debug APK (1455aad5…). It still declares
  PreviewActivity, ui-test-manifest's ComponentActivity and SimulatedBillingReceiver, and PreviewActivity opens and is resumed on both APIs.
- **The release app still works after the change in R8's output.**
  - API 36: the full Maestro regression on the R8 APK passes 56 of 56 flows (45 in pass 1, 11 in pass 2). The 3 `simulated-store` flows
    cannot run on a release build. Monkey: 5000 events, seed 20261024, 0 crashes, 0 ANRs.
  - API 26: smoke passes 15 of 15 (12 + 3). The 2 `geometry` flows are not valid below API 29.
- **An update from v1.2.1 (R8) to this build keeps the data** on API 36 and API 26. Details are under "Rows".
  - The database stays at v7, and every table's row count is equal.
  - The app is not opened after the update. The pending "Did it help?" still posts at 11:06, and "How was today?" posts at 20:00.
  - The seeded records flow passes.
  - API 36 also passes this: a v1.2.1 JSON backup imported into a clean candidate gives equal tables.

## Candidate, scope and builds

- **Candidate:** ceb6bb30932870a67de6661da1217135310ea4f5, merge base with `develop` 3afbf7af.
  - `develop` has since moved to 22f64a97, with website and docs commits only. `git merge-tree` with ceb6bb30 is clean.
  - `git diff --stat 3afbf7af origin/develop` outside `website/`, `docs/`, `openspec/`, `.maestro/` and `qa/` is empty.
- **Product diff since v1.2.1:** 12 build files, no Kotlin, manifest or resource change.
  - 11 feature ui modules move `ui-tooling` to `debugImplementation`.
  - `app/build.gradle.kts` gets the `checkReleaseDebugComponents` guard.
- **Builds:** in detached QA worktrees with `--no-daemon`. No `keystore.properties`, so the APKs are signed with the debug key (D102).
  The same key signs both builds, so the update installs.

| APK | Commit | sha256 |
|---|---|---|
| `r8-ceb6bb30.apk` (candidate, R8, not debuggable) | ceb6bb30 | fb41d716c0a9cbde4846cae80a38eb9a81735258ecb2d7b76b2212110b8a4e58 |
| `debug-ceb6bb30.apk` | ceb6bb30 | 1455aad531ac932c6fc75e3eea0b6e0716bc11c00c5ff30aa9e4d69cce27f448 |
| `r8-v121-5a15b3f8.apk` (control and update source) | v1.2.1 (5a15b3f8) | 7f41f34c13a717aeb520860f171faff323f989ffdda9d9ce5cc30f0925489e33 |
| `debug-v121-5a15b3f8.apk` | v1.2.1 | 1455aad5… (identical to the candidate's debug APK) |

- **R8 is deterministic here.** The rebuilt v1.2.1 R8 APK matches the APK of the 1.2.1 GO (`r8-0aa3bf4a`, 5990d018…) in all 664 entries,
  so every difference below comes from this diff.

## What the diff reaches (static analysis, v1.2.1 R8 against the candidate R8)

| Check | Result | Why it matters |
|---|---|---|
| `:app:dependencies --configuration releaseRuntimeClasspath`, resolved versions | Only `compose.material:material(-android)` 1.9.3 and `ui-tooling(-android)` and `ui-tooling-data(-android)` 1.9.3 are removed. **No other module changes version** | A dependency that leaves the classpath can move a shared library's resolved version. None moved |
| App code that uses M2 or ui-tooling | 0 imports of `androidx.compose.material.*` (outside material3) and 0 of `androidx.compose.ui.tooling.*` (outside `.preview`) | No screen can use a removed class. A missing compile-time reference would already fail the build. No reflection on these names |
| R8 keep rules (`configuration.txt`, as a multiset) | Only `-keep class androidx.compose.ui.tooling.PreviewActivity { <init>(); }` is removed. The consumer files of material, ui-tooling and ui-tooling-data held no rules | No keep rule that protects app code was lost |
| Release manifest (`aapt2 xmltree`) | Removed: `activity androidx.compose.ui.tooling.PreviewActivity`, nothing else. Permissions and badging identical (6 permissions, no INTERNET, not debuggable). `qa/release_apk.py`: no simulated store (debug control finds it) | The guard's outcome, plus the privacy line |
| Guard report `app/build/reports/debug-components/release.txt` (5.1 rerun, from this worktree's `assembleRelease`) | 24 components, 0 of the 3 listed names | |
| R8 output of app code | `classes.dex` differs: 116 kept classes gone and 98 new, mostly inlining and merging of lambdas, mappers and use cases in every feature (report, history stats, backup, medication, settings, subscription, reminders). Only 5 are `ui.tooling` classes | R8 optimised the **whole app** differently, not only the removed libraries. This is why the full regression, monkey and the update ran on the R8 build |
| Resources | The same 5330 resource names. **486 string IDs shift by one** (`medication_add` … `your_data_working`) | Persisted resource IDs would break across the update. Workers persist only strings (`KEY_KIND`, `KEY_DATE`). The launcher shortcut is checked on the device below |
| WorkManager worker names | `ReminderWorker` and `PremiumCheckWorker` keep their names in both mappings | WorkManager stores the class name, and `cancelAllWorkByTag` uses `ReminderWorker::class.java.name` |
| Debug APK | All 956 entries identical, the same file hash | Debug, the instrumented tests and the simulated store cannot change |

## Rows

36 = emulator-5580 (Migrely_QA_API_36), 26 = emulator-5582 (Migrely_QA_API_26), one at a time, en-US, Asia/Tashkent. Host load 2.5 to 11.
Evidence is in `migrely-work/qa-195/` (`chain-36.out`, `chain-26.out`, `logs/`, `logs/rows.jsonl`).

| Row | APK | 36 | 26 |
|---|---|---|---|
| PA: `am start -W -n com.migrely.app/androidx.compose.ui.tooling.PreviewActivity` | candidate R8 | ✅ 2/2 "does not exist", no process started | ✅ 2/2 "does not exist" |
| PA control | v1.2.1 R8 | ✅ 2/2 started: "Start proc … for … PreviewActivity", then "not debuggable", closes | ✅ 2/2 "Start proc … for activity …PreviewActivity". `am -W` never returned and hit the 60 s limit, because the activity closes before its first frame |
| PA on debug | candidate debug | ✅ 1/1 `topResumedActivity` = PreviewActivity | ✅ 1/1 "Displayed …PreviewActivity", resumed |
| Launcher shortcut after the update (string IDs shifted) | v1.2.1 R8 → candidate R8 | ✅ "Migraine started" and "Log that a migraine started", before and after the first open. resId 2131886844 → 2131886843 resolved | ✅ the same labels |
| Update with data kept (`up195.py`, from `qa-121/up121.py`) | v1.2.1 R8 → candidate R8 | ✅ 1/1: DB v7 → v7, every table equal; follow-up posted; daily posted; kept flow | ✅ 1/1, the same |
| Backup from v1.2.1 imported into a clean candidate | candidate R8 | ✅ the tables equal, file schema 7 | not run (API 36 only, as in 1.2.1) |
| Full Maestro regression (`qa/maestro.sh --install`, `QA_APK`) | candidate R8 | ✅ 56/56: pass 1 45/45, pass 2 11/11. 3 `simulated-store` flows skipped | — |
| Smoke (`--tags smoke`) | candidate R8 | covered by the regression | ✅ 15/15: 12 + 3. 2 `geometry` flows skipped |
| Monkey (`qa/monkey.sh 20261024 5000`) | candidate R8 | ✅ 0 crashes, 0 ANRs, finished | — |

Details of the update rows:
- **Seed on v1.2.1 R8:**
  - one launcher launch first;
  - Maestro seed without clearState: 2 attacks, Severe, Nausea;
  - a custom symptom;
  - Ibuprofen with 2 intakes at 09:05;
  - the daily reminder on;
  - a JSON backup.
- **Before the update:** the app is opened, then Home, then the process is killed. It is not force-stopped. installd's code_cache mark is
  true (#193 guard). Then `adb install -r` of the candidate, and the app is not opened.
- **Counts before and after**, 36 and 26: attack 2, attack_tag 2, custom_tag 1, intensity_point 1, medication 1, medication_intake 2,
  reminder_settings 1, all other tables 0.
- **Work kept:** `daily` plus 2 `effect-*` jobs. Both "Did it help?" notifications post at 11:06. "How was today?" posts with both actions at
  20:01 (36) and 20:00 (26).

## Not redone, by the lead's brief

- **5.2's negative controls** (a to d) were proved at build level by `android-engineer` (round 2) and `code-reviewer`. Task 5.5's text asks
  QA to rerun one of b or c; the lead's brief for this run waived that. QA's own control is the device one above: v1.2.1 starts the
  activity, and the candidate does not.
- **The AAB:** QA did not build `bundleRelease`. The AAB manifest check is the engineer's 5.1 row (`bundletool dump manifest`, 0 listed names).

## What these checks do not prove

- **R8 changed code across the whole app,** inlining in report, history stats, backup, medication, settings, subscription and reminders.
  - The regression covers every flow that can run on a release build.
  - It does not cover the 3 flows that buy through the simulated store, Premium and the Doctor report on R8 (no Play billing on the
    emulators), or any screen path without a flow. This run did not recount coverage; the capabilities under 50% are the ones `python3 -I qa/e2e_coverage.py --below 50` lists.
- **API 26 ran smoke only,** not the full regression or monkey.
- **The update** was 1 run per API, from v1.2.1 only. No other shipped version was tested: 1.1.1 is in review, and 1.2.0 was never shipped.
  It used the same versionCode 11, so this is `install -r`, not a Play update with a higher versionCode. The version bump lands on the
  release branch, which still gets its own Go/No-Go.
- **The upload key and a real Play install** were not used. Neither were real phones or OEM launchers: the shortcut row ran on the AOSP and
  Pixel launchers only.
- **Only manifest entry points are covered.** A debug-only class kept in the dex without a manifest entry is out of scope (proposal, Out of
  scope).
- **API 26 device state:** `global zen_mode` was 3 before this run and is still 3. `qa/reset_device.sh` cannot set it on API 26. The run did
  not change it.

## Residual risks

- An R8 optimisation difference in a path no flow walks. This is low: the keep rules and every library version are unchanged, so
  only R8's heuristics moved.
- The next release's Go/No-Go must run on its own R8 build, because any later change moves R8's output again.
