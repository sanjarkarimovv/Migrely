# Release Go/No-Go: Migrely 1.0.0 (versionCode 3)

- **Candidate:** `release/1.0.0` @ 0914bcb, versionName 1.0.0, versionCode 3.
  - It replaces 195fff2 (versionCode 2), which passed lines 1–10 on 2026-10-25.
  - A real Play purchase test then found that "Change plan" from monthly to yearly failed with Play's DEVELOPER_ERROR.
    The fix is D120, PR #65.
- **Checked:** 2026-10-25 and 26, by the QA session in `migraine-app-qa`, with the tools on
  feature/add-premium-e2e-coverage.
- **Builds:** made in a detached QA worktree at the candidate, with `--no-daemon`:
  - the debug APK, for the device checks;
  - the R8 release APK, minified and signed with the debug key, since QA has no upload key (D102);
  - builds 1 (b40c963) and 2 (195fff2), for the upgrade checks.
- **Emulators:** API 36 (`Migrely_QA_API_36`, emulator-5580) and API 26 (`Migrely_QA_API_26`, emulator-5582).
- **Rules:** `docs/QA.md` §5 and BRD D44.

## Decision

**GO for the internal testing track**: lines 1–10 pass.

The production track still waits for:
- lines 11 and 12;
- the privacy policy page at https://migrely.health/privacy/. The app opens it (D119), but the domain does not answer
  yet.

## The lines (on 0914bcb)

| # | Check | Result |
|---|---|---|
| 1 | Build gate (`assembleDebug assembleRelease test ktlintCheck detekt`) | ✅ BUILD SUCCESSFUL |
| 2 | Smoke on Android 8.0 (API 26), `--tags smoke` | ✅ debug 12/12 + 3/3; R8 release 12/12 + 3/3 (versionCode 3, not debuggable) |
| 3 | Full regression on API 36 (debug) | ✅ 41/41 + 9/9. See the note below |
| 4 | Accessibility audit | ✅ carried over from 195fff2, where 30 states had `0 failure(s)`, none uncaptured. 195fff2..0914bcb changes no `res/` or `ui/` file, only billing (`PlayBillingDataSourceImpl`, `BuyPremiumUseCase`, a billing model) and versionCode |
| 5 | Device scenarios | ✅ all 41 passed |
| 6 | Random input (`qa/monkey.sh 20261024 5000`) | ✅ 0 crashes, 0 ANRs |
| 6a | Upgrade to build 3 from build 2, and from build 1 | ✅ both keep every attack, pain level and symptom |
| 7 | Permissions (`qa/permission_audit.sh`, debug and release APK) | ✅ passes. See the note below |
| 8 | Open defects (`gh issue list --label qa --state open`) | ✅ none |
| 9 | Coverage report (`qa/e2e_coverage.py`) | ✅ exit 0 |
| 10 | R8 release APK: lines 3 and 7 | ✅ regression 41/41 + 9/9 on API 36; permissions as line 7; not debuggable |
| 11 | Play pre-launch report (internal testing) | ⏳ for build 3 on the internal track |
| 12 | Internal testing with people, at least a week (BRD §14.1) | ⏳ starts with build 3 |

**Line 3:** the first debug run had 40/41. The failing flow was "History's month summary … counts this month and last
month", and it ran across midnight.
- Its past migraine on the 12th of last month takes the current time of day, so it ran from 23:59 into 00:02.
- History correctly counted the 12th and the 13th (D57), and the check expected 1 day.
- Re-run 2 of 2 at 06:33 on the same build. The flow gets a fixed time in 11b.

**Line 6:** the monkey switched Do Not Disturb on, and it was turned off again.

**Line 7:** the permissions are POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, WAKE_LOCK and BILLING (allowed for D107),
plus AndroidX's dynamic-receiver permission. There is no INTERNET, ACCESS_NETWORK_STATE or exact alarm.

## Retests and spot checks

| Item | Result |
|---|---|
| #58: import Add brings back a deleted migraine | ✅ History 2 → 3 after Add, on 195fff2 and 0914bcb. Closed |
| #56: Your data's messages last 8 s, longer with the accessibility timeout | ✅ 8.0–8.1 s at the default; 19.9–20.1 s with both timeouts at 20 s. Measured from back-to-back screenshots to ±0.3 s, on R8 and debug, 195fff2 and 0914bcb. Closed |
| #45: "Triptans: 1 day (limit 10)" | ✅ Closed |
| D117: English dates; the region decides the first weekday | ✅ ru-RU: English, Monday first. ru-US and en-US: Sunday first. No Cyrillic. A Hebrew phone gets the left-to-right layout (`keyboard_rtl`) |
| D119: Privacy ends with "Read the full privacy policy" | ✅ VIEW intent to https://migrely.health/privacy/ in Chrome. The URL was also read from the R8 APK |
| D110: no paywall shows itself without Play products (release build) | ✅ none after onboarding, in 3 runs. A tap on Doctor report says Premium cannot be bought, and shows no buy button |
| D113: the release build has no simulated store | ✅ the R8 APK has 0 hits in its dex, resources and manifest; the debug APK, the control, has 57, 2 and 4 (`qa/release_apk.py`) |
| D115: the entitlement cache is never backed up | ✅ `qa/backup_rules.py` reads the APK: `migrely_entitlement.xml` is excluded in full-backup-content, cloud-backup and device-transfer |

## Real Google Play, on the user's phone (dev session, build 3, license tester)

All 10 pass:
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

## Still open, not blocking internal testing

- **Play's grace-period and account-hold messages:** they need a failed renewal.
- **"A new phone" from Android's cloud backup:** it needs a phone with a screen lock and a Google account backup.
- **The full premium E2E coverage on the simulated store:** it is QA change 11b, in progress.
