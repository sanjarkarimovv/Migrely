# Release Go/No-Go: Migrely 1.0.1 (versionCode 6)

- **Candidate:** `hotfix/1.0.1` @ ed695424, versionName 1.0.1, versionCode 6. The branch was cut from `main` at
  `v1.0.0` (a1cf745), as D125 requires.
- **What it fixes:** the defects that QA's build 4 sweep found after 1.0.0 went live (QA 13c):
  - #77: the privacy link crashed with no browser;
  - #78: overlapping migraines;
  - #79: the landscape time pickers;
  - #80 and #81: the note limit;
  - #74: lint's errors.
- **versionCode 5** (5d8c9b39) was never a candidate: its lint had #74's 3 errors, and the dev session then added the
  fix.
- **Builds:**
  - **Debug and R8 APKs:** QA's builds, in `../migraine-app-qa-hotfix` at ed695424. The R8 APK is signed with the
    debug key (D102).
  - **The bundle that goes to Play:** `Migrely-1.0.1-6.aab`, signed with the upload key by the dev session.
    - Lines 2a and 10 use it, through bundletool.
    - Its universal APK: versionCode 6, 1.0.1, and the 5 permissions of 1.0.0, with no INTERNET.
- **Emulators:** API 36 (`Migrely_QA_API_36`, emulator-5580) and API 26 (`Migrely_QA_API_26`, emulator-5582).
- **Rules:** `docs/QA.md` §5, BRD D44, D125, D204.

## Decision

**Pending.** The lines below are filled in as they run.

**How 1.0.1 is released (the user's decision of 2026-10-27, through the dev session):**
- **The first signal:**
  - the retests of #74, #77, #78, #79, #80 and #81, and the paste-then-DEL case;
  - smoke on API 36 and API 26;
  - regression on the debug and R8 builds;
  - the ru split on API 26.
- **When the first signal passes:**
  - the dev session finishes the hotfix by Git-flow: a PR into `main`, `v1.0.1`, and a merge into `develop`;
  - the user sends ed695424's bundle to production review at a 20% staged rollout. Managed publishing holds it.
- **"Publish changes" waits for both:**
  - QA's full Go: every line below;
  - line 11, the pre-launch report of ed695424's closed track.
- **If a later line finds a blocker:** 1.0.1 is never published. A 1.0.2 hotfix from `main` replaces it in review, and
  `v1.0.1` stays where it is.
- **This differs from D44:** D44 has the candidate pass every line before it goes to a track. Here the user chose to
  finish the hotfix and submit before the full line set, and made Publish rest on the full set.

## The lines (on ed695424)

| # | Check | Result |
|---|---|---|
| 1 | Build gate (`assembleDebug assembleRelease test ktlintCheck detekt`) | ✅ exit 0 |
| 1a | Android Lint (`./gradlew lint --continue`) | ✅ 0 errors; 56 warnings, the same kinds as build 4's. See the note below |
| 1b | Instrumented tests on emulator-5580 | ⏳ |
| 2 | Smoke on Android 8.0 (API 26), debug and R8 | ⏳ |
| 2a | Split APKs from the upload-signed bundle: ru-26, he-36, uz-36, en-36 | ⏳ |
| 3 | Full regression on API 36 (debug) | ⏳ |
| 4 | Accessibility audit (22 states) | ⏳ |
| 5 | Device scenarios | ⏳ |
| 6 | Random input (`qa/monkey.sh 20261024 5000`) | ⏳ |
| 6a | Upgrade to 1.0.1 from build 4 (1.0.0) | ⏳ |
| 7 | Permissions (`qa/permission_audit.sh`, debug and release APK) | ⏳ |
| 8 | Open defects (`gh issue list --label qa --state open`) | ⏳ |
| 9 | Coverage report (`qa/e2e_coverage.py --below 50 --register docs/qa/test-strategy.md`) | ⏳ |
| 10 | R8: lines 3 and 7 on the universal APK of the upload-signed bundle | ⏳ |
| 11 | Play pre-launch report (closed testing track, ed695424) | ⏳ before Publish |
| 12 | Internal testing with people, at least a week (BRD §14.1) | ⏳ the user's decision |

**Line 1a:** these warnings could affect users, and each one was looked at:
- **`PluralsCandidate`, twice:**
  - "%1$d out of 10" is not a plural;
  - "Taking them on %1$d days or more" only shows from 8 days up, so it never reads "1 days".
- **`ConfigurationScreenWidthHeight` and `MutableCollectionMutableState`, twice each:** no user-visible effect found in
  the regression or the audit.
- **`AppBundleLocaleChanges`:** the app sets its own formatting locale (D117) while Play splits the bundle by language.
  Line 2a checks exactly this, on the bundle itself.
- **The rest:** build-tool versions and code style.

## Retests

| Issue | Debug, run 1 | Debug, run 2 | R8, run 1 | R8, run 2 |
|---|---|---|---|---|
| #74 Settings' version, API 36 | ⏳ | ⏳ | ⏳ | ⏳ |
| #74 Settings' version, API 26 | ⏳ | ⏳ | ⏳ | ⏳ |
| #77 privacy link with no browser | ⏳ | ⏳ | ⏳ | ⏳ |
| #78 a start inside an ended migraine | ⏳ | ⏳ | ⏳ | ⏳ |
| #79 landscape time pickers | ⏳ | ⏳ | ⏳ | ⏳ |
| #80 typing in a full note | ⏳ | ⏳ | ⏳ | ⏳ |
| #81 emoji at the limit, and paste-then-DEL | ⏳ | ⏳ | ⏳ | ⏳ |

## What these checks do not prove

Filled in with the verdict, as `docs/QA.md` §5 asks.
