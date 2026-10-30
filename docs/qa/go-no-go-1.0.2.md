# Release Go/No-Go: Migrely 1.0.2 (versionCode 7)

- **Candidate:** `release/1.0.2` @ deb41422, versionName 1.0.2, versionCode 7. It was cut from develop ec55b053 and adds
  one commit: the version, BRD D128 and the Play notes in `docs/release/whats-new.md`.
- **What it fixes since 1.0.1:** each fix was retested twice on a develop debug build before the cut.

  | Issue | Severity | What | Retest |
  |---|---|---|---|
  | #84, #87 | S4 | home's row label and the import text | on develop, QA 13c |
  | #89 | S3 | a start could be set after now | on develop, QA 13c |
  | #90 | S3 | undoing an end could reopen a migraine over another | on develop, QA 13c |
  | #101 | S3 | the landscape date picker hid week 1 | 7f65d0e5, landscape and 200%, 2/2 each |
  | #102 | S3 | the report's custom range at 200% showed no calendar | 7f65d0e5, 200% portrait and landscape, 2/2 each |
  | #104 | S2 | Insights kept the previous period's numbers | 7f65d0e5, 0 stale in 58 reads |
  | #105 | S2 | a purchase completed while the app was closed came up to 15 min late | 7f65d0e5, 4/4, plus 5/5 restarts not asking early |
  | #106 | S4 | the Restore purchases line was lost on rotation | 7f65d0e5, 2/2 |
  | #112 | S3 | a deletion stamped in the future was offered for Undo at every start | 7f65d0e5, 2/2 |
  | #116 | S2 | the shortcut with home open could leave home calm over an attack | ec55b053, 10/10 and the scenario 3/3 |

  It also has D122 (Google Play with no products) and D123 (Lint in the gate).
- **Builds:**
  - **Debug and R8 APKs:** QA's builds, in `../migraine-app-qa-rc102` at deb41422. The R8 APK is signed with the debug
    key (D102).
  - **The bundle that goes to Play:** `Migrely-1.0.2-7.aab`, signed with the upload key by the dev session.
    - sha256: e06bb9a5…0c9e438, the same as the dev's `Migrely-1.0.2-7.commit.txt`.
    - Upload certificate: bb45cfb7…445054d1, the same as 1.0.1's.
    - Lines 2a and 10 use it, through bundletool.
    - **Its universal APK:** versionCode 7, 1.0.2, not debuggable. It has the 5 permissions of 1.0.1 and no INTERNET.
- **Emulators:** API 36 (`Migrely_QA_API_36`, emulator-5580) and API 26 (`Migrely_QA_API_26`, emulator-5582).
- **Rules:** `docs/QA.md` §5, BRD D44, D125, D204.
- **Before the cut:** the full regression and every device scenario passed on develop 7f65d0e5. The regression was
  44/44 and 10/10. The scenarios were 52/54, and both failures passed 2/2 on rerun. Then #116 was fixed on top of it.

## Decision

**QA's verdict, 2026-10-30 at 11:00: Go, with these risks open.**
- **Lines 1–10 all pass on deb41422** (versionCode 7), with no S1, S2 or S3 open.
- **No app defect was found on the candidate.** Every failure on the way came from the environment or QA's own flows:
  - emulator-5580 died twice: once mid-line 3, once with system_server down;
  - Maestro hung once;
  - the host's load reached 33 in the night;
  - the day tap in landscape was a QA flow problem, now fixed.
  Each was rerun to a pass.
- **The risks still open:**
  - the S4s in line 8;
  - "What these checks do not prove" below;
  - lines 11 and 12, which are the user's decisions.
- **Release steps (D125):**
  - the user uploads `Migrely-1.0.2-7.aab` to internal and Alpha;
  - Production goes from the finished, tagged `main`, with `git diff deb41422 v1.0.2` empty.


- **2026-10-28, 16:33:** emulator-5580 died during line 3, probably for lack of host memory. Maestro waited on it
  until QA found it the next morning.
- **2026-10-29, 08:45:** the emulator was cold-booted with docs/QA.md's command and the chain resumed from line 3. Lines
  1b and 7 had finished before it died.
- **The chain now:** each line has a time limit, so a dead emulator stops that line instead of the chain.

## The lines (on deb41422)

| # | Check | Result |
|---|---|---|
| 1 | Build gate (`assembleDebug assembleRelease test ktlintCheck detekt lint`) | ✅ exit 0, 16:25–16:29 |
| 1a | Android Lint's warnings, every module's report | ✅ 0 errors, 57 warnings: the app's 48 and 9 from 5 UI modules. Against 1.0.1's 56, the only new one says a newer KSP (2.3.12) is out. It is a build plugin with no user effect. The rest are the same kinds as 1.0.1's |
| 1b | Instrumented tests on emulator-5580 | ✅ 16:30–16:32: datasource 112/112, designsystem 48/48 and app 1/1, 0 failed. 1.0.1 had 104 and 37; the new tests came with #101, #102 and #112 |
| 2 | Smoke on Android 8.0 (API 26), debug and R8 | ✅ emulator-5582, 2026-10-30 10:25–10:44. Debug APK: 12/12, then 3/3. The bundle's universal APK (not debuggable): 12/12, then 3/3 |
| 2a | Split APKs from the upload-signed bundle: ru-26, he-36, uz-36, en-36 | ✅ Each profile has 4 APKs installed: base, ABI, density and language. Each smoke passed 14/14 and 3/3 (ru-26: 12/12 and 3/3). The D117 orders hold:<ul><li>ru-26: system language ru-RU, the texts English (36, 53 and 49), month first, Monday first;</li><li>he-36: the app language he-IL, day first, Sunday first;</li><li>uz-36: month first, Monday first;</li><li>en-36: month first, Sunday first.</li></ul>The uz and en runs were redone on 2026-10-30. On the night of 2026-10-29 the host was at load 33, and those runs took hours and were cut off by their time limits |
| 3 | Full regression on API 36 (debug) | ✅ 08:46–09:23 on 2026-10-29: 44/44, then 10/10 with the 20 s accessibility timeout |
| 4 | Accessibility audit (22 states) | ✅ `0 failure(s)` on all 66 captures. The full audit (10:06–10:55) took 65.<ul><li>`insights.landscape` failed in its tour flow: since #101 the landscape calendar scrolls, and Maestro tapped a third-row day that sat cut at the calendar's bottom edge beside Cancel and Next. Nothing was selected, so 10 AM today was refused ("Pick a time before now", #89's rule).</li><li>`add-past-attack-on.yaml` now centres the day before the tap. `insights` then passed 2/2 in all 3 configurations (2026-10-30).</li></ul>The 27 "not judged" lines are the kinds looked at for 1.0.1: rows cut at a scroll edge, of components judged in full elsewhere, and text in merged nodes |
| 5 | Device scenarios, with the release APK | ✅ all 55 pass. They ran in groups with time limits after two environment failures:<ul><li>on 2026-10-29 at 11:31, Maestro hung for 3 h on one step of `med_overuse`, with the emulator answering; it was rerun and passed;</li><li>at about 16:10 emulator-5580's system_server died ("Can't find service: settings"). It was cold-booted, and its group was rerun and passed.</li></ul>Two first-run failures each passed 2/2 on rerun:<ul><li>`med_effect_skip`;</li><li>`report_heavy`, where the import's start screen did not show: "the latest 382 of 900 migraines", no HEAVY- note.</li></ul>`release_apk`: no simulated store, not debuggable |
| 6 | Random input (`qa/monkey.sh 20261024 5000`) | ✅ 5,000 events: 0 crashes, 0 ANRs. Do Not Disturb, turned on from the shade, was restored with no warning |
| 6a | Upgrade to 1.0.2 from 1.0.1, the build users have | ✅ in line 5's `upgrade`: 1.0.1's debug APK (versionCode 6) with data, then 1.0.2 (versionCode 7) over it. Every attack was kept |
| 7 | Permissions (`qa/permission_audit.sh`: debug, R8 and universal APKs) | ✅ all 3 exit 0: the 5 permissions of 1.0.1, and no INTERNET |
| 8 | Open defects (`gh issue list --state open`) | ✅ No S1, S2 or S3 is open. Open, all S4 and for develop: #82, #83, #88, #91, #109 and #118 |
| 9 | Coverage report (`qa/e2e_coverage.py --below 50 --register docs/qa/test-strategy.md`) | ✅ exit 0. 267 of 528 scenarios (51%). The register now names the new scenarios with no flow: #102's custom range and #105's closed-app purchase, both retested by hand, with flows in QA 13d |
| 10 | R8: lines 3 and 7 on the universal APK of the upload-signed bundle | ✅ 09:23–10:06: 42/42, then 10/10. The 2 simulated-store flows need a debug build. Line 7 on this APK: ✅ |
| 11 | Play pre-launch report | Not waited for, under the user's decision of 2026-10-28 that Publish does not wait for one. If Play produces a report on the internal or closed track, read it before Production |
| 12 | Internal testing with people, at least a week (BRD §14.1) | The user's decision. The plan: internal and Alpha first, then Production. The app still has no users beyond closed testing |

## What these checks do not prove

| Line | Passing it does not show |
|---|---|
| 1 Build gate | that the logic is tested: code coverage is not measured (audit C2) |
| 1b Instrumented | the screens: it covers DAO SQL, migrations and design-system components only |
| 2, 3, 10 Smoke and regression | the scenarios without a flow: 261 of 528 (49%) |
| 4 Accessibility audit | what a screen reader says: TalkBack itself is not driven. Nor the dialogs: no state captures an open picker (QA 13d) |
| 5 Device scenarios | anything beyond its named scenarios |
| 6 Random input | anything beyond 5,000 events from one seed |
| 6a Upgrade | an upgrade through Android's backup on a new phone |
| 7 Permissions | what the Play app does with billing data, which Google Play's own terms govern |
| Retests | real Google Play for #105, which the simulated store stands in for |
