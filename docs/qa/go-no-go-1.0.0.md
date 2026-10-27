# Release Go/No-Go: Migrely 1.0.0 (versionCode 4)

- **Candidate:** `release/1.0.0` @ 29aadb3, versionName 1.0.0, versionCode 4.
  - It adds two QA fixes to build 3 (0914bcb): #66 (PR #68, home's Period toggle) and #67 (PR #69, the report's notes
    cap).
  - 0914bcb..29aadb3 changes home, History's month list and the report's layout. It changes no billing file.
- **Earlier candidates:**
  - 195fff2 (versionCode 2) passed lines 1–10 on 2026-10-25. A real Play purchase test then found that "Change plan"
    from monthly to yearly failed with Play's DEVELOPER_ERROR (fixed by D120, PR #65).
  - 0914bcb (versionCode 3) passed lines 1–10 on 2026-10-26 and went to the internal track.
- **Checked:** 2026-10-26, by the QA session in `migraine-app-qa`, with the tools on feature/add-premium-e2e-coverage.
- **Builds:** made in a detached QA worktree at the candidate, with `--no-daemon`:
  - the debug APK, for the device checks;
  - the R8 release APK, minified and signed with the debug key, since QA has no upload key (D102);
  - builds 1 (b40c963) and 3 (0914bcb), for the upgrade checks.
- **Emulators:** API 36 (`Migrely_QA_API_36`, emulator-5580) and API 26 (`Migrely_QA_API_26`, emulator-5582).
- **Rules:** `docs/QA.md` §5 and BRD D44.

## Decision

**GO for the internal testing track, with the risks of "What these checks do not prove" open**: lines 1–10 pass,
and line 1a holds the stated #74 exception.

**Publishing:** build 4 is not published yet, as of 2026-10-27.
- **In review:** submitted to Google on 2026-10-27 at 14:01, for production, the closed track, the listing, App content
  and the store settings.
- **Who gets it:** every country, the EEA included, at 100% of production.
  - Play has no staged rollout for an app's first release, so there is no smaller first audience.
  - Once Publish is pressed, a defect reaches every user, and only a new build can fix it. QA's verdict is the last
    gate before that.
- **Managed publishing is on.** Google's approval publishes nothing; "Publish changes" is a separate step.
- **Publish waits for:**
  - QA's verdict on the closed track's pre-launch report (line 11);
  - QA's verdict on the build 4 sweep (QA 13c).
- **Line 11:** a closed testing release of build 4 is made for the pre-launch report, which is read before Publish.
- **Line 12:** the week of internal testing is waived, on the user's decision of 2026-10-27.
- **The launch-blocker rule** (the dev session's):
  - Blockers:
    - a crash or ANR in a core flow;
    - data loss or corruption;
    - a wrong or missing safety warning;
    - a broken purchase or entitlement;
    - data leaving the device.
  - A blocker is fixed on `release/1.0.0` as build 5 (still 1.0.0), and build 5 replaces build 4 before Publish.
  - Every other defect ships in 1.0.1.

## The lines (on 29aadb3)

| # | Check | Result |
|---|---|---|
| 1 | Build gate (`assembleDebug assembleRelease test ktlintCheck detekt`) | ✅ BUILD SUCCESSFUL |
| 1a | Android Lint (`./gradlew lint`, D204, new on 2026-10-27) | ⚠️ 3 errors, none reachable at runtime: #74 (S3), fixed in 1.0.1 by D123. See the note below |
| 1b | Instrumented tests on emulator-5580 (`connectedDebugAndroidTest`, D204) | ✅ datasource 100/100, designsystem 37/37, app 1/1 |
| 2 | Smoke on Android 8.0 (API 26), `--tags smoke` | ✅ debug 12/12 + 3/3; R8 release 12/12 + 3/3 (versionCode 4, not debuggable) |
| 3 | Full regression on API 36 (debug) | ✅ 43/43 + 9/9 |
| 4 | Accessibility audit | ✅ 30 states, `0 failure(s)`, run again on this build |
| 5 | Device scenarios | ✅ all 53 passed. See the note below |
| 6 | Random input (`qa/monkey.sh 20261024 5000`) | ✅ 0 crashes, 0 ANRs |
| 6a | Upgrade to build 4 from build 3, and from build 1 | ✅ both keep every attack, pain level and symptom |
| 7 | Permissions (`qa/permission_audit.sh`, debug and release APK) | ✅ passes. See the note below |
| 8 | Open defects (`gh issue list --label qa --state open`) | ✅ none |
| 9 | Coverage report (`qa/e2e_coverage.py`) | ✅ exit 0. 253 of 482 scenarios (52%) |
| 10 | R8 release APK: lines 3 and 7 | ✅ regression 41/41 + 9/9 on API 36; permissions as line 7; not debuggable |
| 11 | Play pre-launch report (closed testing track) | ⏳ for build 4, read before Publish |
| 12 | Internal testing with people, at least a week (BRD §14.1) | waived by the user on 2026-10-27 |

**Line 1a:** lint was run on a candidate for the first time on 2026-10-27, after the outside audit. Two of its 3
errors are NewApi in `AppInfoDataSourceImpl.kt:15`: the API 33 `getPackageInfo` overload. The call sits behind
`sdkInt() >= TIRAMISU`, and lint cannot see through that lambda. The third is in test code (`TapGuardTest`).
- Build 4 takes this as a stated exception, because the requirement is new (D204).
- From build 5 on, a lint error makes the candidate No-Go.

**Line 1b:** before 2026-10-27 the dev session ran these tests only on its own emulator, when a datasource changed.
They cover the DAO SQL, the Room migrations and the design-system components. The run installs and then removes the
app, so it comes before the device lines.

**Line 3:** 43 flows, 2 more than build 3: the premium purchase flow and the paywall after onboarding (QA 11b).
- `day-markers/track-periods-off-live.yaml`, the `known-bug` flow for #66, now passes.
- It loses the tag and joins the regression.

**Line 5:** the first run stopped after 30 scenarios.
- The emulator's system server died at 12:56, during `reminder_effect`, so that scenario and the next failed to find
  the screen.
- After a restart, the remaining 23 passed, `reminder_effect` included.
- The 13 scenarios new in QA 11b passed again in a second run.

**Line 10:** 41 flows, since the 2 flows tagged `simulated-store` need the debug build (D113).

**Line 7:** the permissions are POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, WAKE_LOCK and BILLING (allowed for D107),
plus AndroidX's dynamic-receiver permission. There is no INTERNET, ACCESS_NETWORK_STATE or exact alarm.

## Retests and spot checks

| Item | Result |
|---|---|
| #66: home keeps "Period today" after Track periods is off | ✅ gone within 5 s, without a restart. Passes on debug and R8, and 3 of 3 in a repeat. Closed |
| #67: a large report keeps every note and cuts the table instead | ✅ 900 migraines with notes on: "Showing the latest 308 of 900 migraines" and "Notes from the latest 308 migraines", the notes are exactly the 308 newest, 50 pages. 2 of 2 runs. Closed |
| #58: import Add brings back a deleted migraine | ✅ History 2 → 3 after Add, on 195fff2, 0914bcb and 29aadb3 |
| #56: Your data's messages last 8 s, longer with the accessibility timeout | ✅ 8.0 s at the default; 20.0 s with both timeouts at 20 s (±0.2 s), on 29aadb3. Earlier also on 195fff2 and 0914bcb, R8 and debug |
| #45: "Triptans: 1 day (limit 10)" | ✅ in `med_overuse` |
| D117: English dates; the region decides the first weekday | ✅ ru-RU: English, Monday first. ru-US and en-US: Sunday first. No Cyrillic. A Hebrew phone gets the left-to-right layout (`keyboard_rtl`) |
| D119: Privacy ends with "Read the full privacy policy" | ✅ VIEW intent to https://migrely.health/privacy/ in Chrome. The URL was also read from the R8 APK |
| D110: no paywall shows itself without Play products (release build) | ✅ none after onboarding, in 3 runs. A tap on Doctor report says Premium cannot be bought, and shows no buy button (0914bcb; no billing change since) |
| D113: the release build has no simulated store | ✅ the R8 APK has 0 hits in its dex, resources and manifest; the debug APK, the control, has 57, 2 and 4 (`qa/release_apk.py`, 29aadb3) |
| D115: the entitlement cache is never backed up | ✅ `qa/backup_rules.py` reads the R8 APK: `migrely_entitlement.xml` is excluded in full-backup-content, cloud-backup and device-transfer (29aadb3) |

## Real Google Play, on the user's phone (dev session, build 3, license tester)

All 10 pass on build 3. They carry over to build 4, which changes no billing file:
1. a declined card;
2. the yearly trial, which unlocks the report;
3. acknowledgement: Premium is still active after Play's 5-minute auto-refund window;
4. a refund with the entitlement removed: Premium closes in about 1 minute;
5. no second trial: yearly shows its price and "Subscribe";
6. a monthly purchase;
7. lifetime over a subscription with the slow card: Premium continues while pending, then shows "Premium · lifetime"
   with the cancel note;
8. a reinstall: Premium is found at the first start, and no paywall shows after onboarding;
9. D120, monthly → yearly: Play accepts, and Settings shows "Premium · yearly";
10. cancelled in Play: Premium stays until the paid period ends, then Settings shows "Free".

## What these checks do not prove

This section was added on 2026-10-27, after the outside quality audit (D204). Until then the record above read as a
stronger verdict than its checks supported.

| Line | Passing it does not show |
|---|---|
| 1 Build gate | that the logic is tested: code coverage is not measured (audit C2) |
| 1a Lint | anything until lint passes; see #74 |
| 1b Instrumented | the screens: it covers DAO SQL, migrations and design-system components only |
| 2, 3, 10 Smoke and regression | the scenarios without a flow: 249 of 507 (49%) |
| 4 Accessibility audit | the screens outside its 10 states: onboarding, Settings, medications, reminders, Your data, the paywall, the report, Insights (D203, QA 13c task 3) |
| 5 Device scenarios | anything beyond its 53 named scenarios |
| 6 Random input | anything beyond 5,000 events from one seed |
| 6a Upgrade | an upgrade with an Android backup on a new phone |
| 7 Permissions, `release_apk` | what the Play app does with billing data, which Google Play's own terms govern |
| 9 Coverage report | that coverage is enough: "exit 0" means only that no flow names a stale scenario |

### Capabilities with under half of their scenarios checked end to end

`python3 -I qa/e2e_coverage.py --below 50`, with the follow-up from `docs/qa/test-strategy.md`:

| Capability | Checked | User risk | Follow-up |
|---|---|---|---|
| attack-details | 6/37 | high | build 4 sweep (QA 13c 5.1), then flows in QA 13d |
| past-attacks | 7/29 | high | build 4 sweep, then QA 13d |
| medication-intakes | 4/14 | high | build 4 sweep, then QA 13d |
| attack-mode | 5/14 | high | build 4 sweep, then QA 13d |
| in-app-messages | 7/24 | high (undo of a delete) | sweep 2, then QA 13d |
| month-sky | 12/36 | medium | unit tests hold the day rules; sweep 2 samples them |
| app-shell | 5/16 | high (the network promise) | line 7 and `release_apk` hold the promise; split APKs (D204) |
| custom-tags | 2/7 | medium | sweep 2, then QA 13d |
| app-identity, brand-typography, release-build | 0 of 3, 5 and 3 | low, low, high | release-build is checked by lines 7 and 10 without Covers lines; QA 13d adds them |
| quality-gates | 13/52 | low for users | QA's own rules |

`medication-overuse` is at exactly half (11/22). Its open scenarios are safety warnings, so sweep 2 takes them
first.

### Deferred QA changes and checks that could not run

- **Deferred QA changes:**
  - visual regression with Roborazzi and pseudo-locales (D42; audit C4);
  - screen-state tests (audit C3).
  Both wait for the dev session's build logic (audit C1).
- **Checks that could not run:**
  - **Device tests in CI (audit C5):** GitHub Actions starts no job while the account's billing lock lasts.
  - **Code coverage:** not measured until audit C2.
  - **Play's grace period and account hold:** they need a failed renewal (a D61 gap of QA 11b).
  - **"A new phone" from Android's cloud backup:** it needs a phone with a screen lock and a Google account backup.

## Still open, not blocking internal testing

- **Play's grace-period and account-hold messages:** they need a failed renewal (a D61 gap of QA 11b).
- **"A new phone" from Android's cloud backup:** it needs a phone with a screen lock and a Google account backup.
