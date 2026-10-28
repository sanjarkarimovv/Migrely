# Release Go/No-Go: Migrely 1.0.1 (versionCode 6)

- **Candidate:** `hotfix/1.0.1` @ ed695424, versionName 1.0.1, versionCode 6. The branch was cut from `main` at
  `v1.0.0` (a1cf745), as D125 requires.
- **What it fixes:** the defects that QA's build 4 sweep found while 1.0.0 was on closed testing only (QA 13c):
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

**QA's verdict on lines 1–10, 2026-10-28 at 05:55: Go, with these risks open.**
- **No line found a blocker.** Every failure on the way was in QA's own flows or scripts, and each fix is in the line's row.
- **One new defect:** #101 (S3, accessibility): in landscape, the date picker hides the first week of the month. It
  is for develop and is not a blocker: portrait works, and so does the dialog's text input.
- **The risks still open:**
  - the defects in line 8;
  - "What these checks do not prove" below.
- **Line 11:** not waited for, by the user's decision of 2026-10-28.
- **Line 12:** is the user's decision below.

**After the verdict: QA 13c's charters on 1.0.1** (2026-10-28, 07:02–08:55, emulator-5580). They found 3 defects, all
in 1.0.1. None is a launch blocker, because none is a crash, data loss, a safety warning, a lost entitlement or data
leaving the phone. The two S2s should be fixed before the next release:
- #104 (S2): Insights keeps the previous period's numbers after a period chip is tapped, about 1 time in 2. A reload
  corrects it.
- #105 (S2): a purchase completed while the app was closed is not seen at the next start for up to 15 minutes. The
  entitlement comes late but is not lost, and Restore purchases finds it at once.
- #106 (S4): Settings' Restore purchases result line disappears on rotation.

The rest of the charters passed:
- rotation and process death on the paywall, the purchase sheet, the report and Insights;
- restarts around a pending payment;
- a 200% font with the dark theme on the report, the PDF and Insights;
- the R8 APK's first launch, through to the privacy link: 0 crashes.

**The user's decision of 2026-10-27, 20:30, to QA directly. It replaces the gates below:**
- **The decision:** 1.0.1 goes to production now, on the evidence at hand. It waits neither for QA's full Go nor for the first signal.
- **The user's reason:** 1.0.0 has no users yet.
  - 1.0.0 went only to closed testing, so 1.0.1 is the first production release.
  - So it goes out at 100%: Play has no staged rollout for a first release.
  - The user sent it to review on 2026-10-27, and presses "Publish changes" after Google approves.
- **The evidence at that time:**
  - lines 1 and 1a;
  - every retest on the debug build, twice each, with the dev's ANR path 5 times clean;
  - on R8: #74, #77 and #78, plus #79's first run.
- **QA:** keeps running every line below on ed695424.
- **If a line finds a blocker:** a 1.0.2 hotfix from `main` follows.
- **`v1.0.1`:** tagged on `main` at 599a7b2f, merge commit of PR #95, parents a1cf7458 and ed695424.
  - QA's check: its tree (2e546e95) is ed695424's, and `git diff ed695424 v1.0.1` is empty.
  - So `Migrely-1.0.1-6.aab` is `v1.0.1`.

**How 1.0.1 was to be released (the user's earlier decision of 2026-10-27, through the dev session):**
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
| 1b | Instrumented tests on emulator-5580 (`connectedDebugAndroidTest`) | ✅ 00:18–00:20: datasource 104/104, designsystem 37/37, app 1/1, 0 failed. Build 4 had 100 datasource tests; the 4 new ones came with the fixes |
| 2 | Smoke on Android 8.0 (API 26), debug and R8 | ✅ emulator-5582, 23:31–23:58: 15 of 15 on the debug APK, and 15 of 15 on the bundle's universal APK (not debuggable) |
| 2a | Split APKs from the upload-signed bundle: ru-26, he-36, uz-36, en-36 | ru-26 ✅, on emulator-5582 at 00:03–00:15. Base, ABI, density and ru splits, all 4 installed. With ru-RU read back as the system language, home, History and the date picker are in English (36, 53 and 49 texts) and the week starts on Monday. Smoke on the splits: 15 of 15.<ul><li>The first try never set the language: Android 8's Custom Locale wants "ru_RU". `qa/lib.sh` is fixed.</li><li>The second try failed 1 of 15: its migraine was logged at 23:59, and home then showed 28 October.</li></ul>he-36, uz-36 and en-36 ✅, on emulator-5580 at 01:06–01:45. Each has the base, arm64-v8a, xxhdpi and language splits (he's is `base-iw.apk`), all 4 installed, and the app language read back. The orders are D117's (English with the phone's region; en-UZ has no CLDR data and falls back to month first):<ul><li>he-IL: the texts are English (35, 53 and 49), today's date is day first and the week starts on Sunday;</li><li>uz-UZ: English (36, 53, 49), month first, the week starts on Monday;</li><li>en-US: English (35, 53, 49), month first, the week starts on Sunday.</li></ul>Smoke on each set of splits: 14 of 14, and 3 of 3 with the 20 s accessibility timeout |
| 3 | Full regression on API 36 (debug) | ✅ 21:29–22:15, host load average 8–10. Pass 2 (`a11y-timeout`): 9 of 9. Pass 1: 43 of 45, and both failures were the flows, not the app:<ul><li>`track-periods-off-marks` (new in 13c) read the toggle's state from its label, but the state is on the parent. Fixed, then 3 of 3;</li><li>`undo-window-passes` found the 5 s offer already gone under load: 2 of 2 on rerun, unchanged.</li></ul> |
| 4 | Accessibility audit (22 states) | ✅ `0 failure(s)` on all 66 captures: 22 states at 100% and 200% font and in landscape. The full audit ran 00:21–01:06. 59 captures were taken; 7, of 3 states new in 13c, were not, because of their tour flows:<ul><li>`insights`: `${DAY}, [0-9]{4}` — Maestro read the script up to the last brace. In landscape, the date picker then hid day 3 (#101), so the tour now picks days 15–21. OK is below the time dial, so the dialog is scrolled to it.</li><li>`medications-list`: it waited for Undo to time out, but "Medication taken" keeps Undo and Done until one is tapped. The tour taps Done, and scrolls to "Add and log" at 200% and in landscape.</li><li>`settings-theme`: at 200%, Theme is below the fold.</li></ul>`buy.yaml` also scrolls to the buy button, which is below the plans in landscape. After the fixes, each of the 3 states passed twice in all 3 configurations, with 0 failures. The 6 medication flows that share `add-first-medication` passed, and so did the purchase flow. Every "not judged" line was looked at:<ul><li>**Cut off by scrolling:** 27 labels are cut off in every capture of their screen. Each is part of a component that is judged in full elsewhere: 7 trigger chips under the details header, 14 day cells in the week under home's buttons, 4 times Settings' "Privacy" row, and 2 History attack rows (judged in full in history-month at 200% and on home).</li><li>**Text contrast in merged nodes:** the audit's own method, applied to the node bounds, measured 459. 449 are at least 4.5:1. The other 10 are History's month arrows at 2.65:1 (#55585D on #0F1115), and both arrows are disabled in those states, so they are exempt. The method is approximate: the brightest pixel can be an icon instead of the text.</li></ul>New defect: #101 (S3, accessibility), the landscape date picker. It is not a blocker |
| 5 | Device scenarios | ✅ emulator-5580, 01:48–04:03: 53 of 54 passed. The one failure is `premium_no_products` (new in 13c): "the simulated store did not take no-products". That command is D122, which is on develop only (PR #85), so this build cannot run it; it is checked on develop instead (13c task 2.4). The first try, at 00:19, did not start: `connectedDebugAndroidTest` (line 1b) uninstalls the app when it ends, and the scenarios need the build under test installed |
| 6 | Random input (`qa/monkey.sh 20261024 5000`) | ✅ twice (00:19 and 04:28): 5,000 events, 0 crashes, 0 ANRs. Both runs turned on airplane mode and Do Not Disturb from the shade. A plain `settings put` does not turn Do Not Disturb off, as the notification service writes it back. `qa/monkey.sh` now uses `cmd notification set_dnd off`, and the second run left both off |
| 6a | Upgrade to 1.0.1 from build 4 (1.0.0); only closed testers had build 4 | ✅ in line 5's `upgrade` scenario: build 4's debug APK (versionCode 4) installed with data, 1.0.1 installed over it, every attack kept |
| 7 | Permissions (`qa/permission_audit.sh`, debug and release APK) | ✅ The debug APK, the R8 APK and the bundle's universal APK each have the 5 permissions of 1.0.0, and none has INTERNET |
| 8 | Open defects (`gh issue list --label qa --state open`) | ✅ The retests passed. #74 and #77–#81 are fixed in ed695424 and closed with PR #96, and no S1 or S2 is open. Still open, all for develop:
<ul>
<li>S3: #92, and #101 (found in line 4 at 05:00);</li>
<li>S4: #82, #83, #88 and #91.</li>
</ul>#84, #87, #89 and #90 are fixed on develop and closed. #91 and #92 came from the dev session's review; the dev session labelled them at 05:59 (#92 S3 with accessibility, #91 S4). |
| 9 | Coverage report (`qa/e2e_coverage.py --below 50 --register docs/qa/test-strategy.md`) | ✅ exit 0. 267 of 508 scenarios (53%); the register matches the flows |
| 10 | R8: lines 3 and 7 on the universal APK of the upload-signed bundle | ✅ 22:33–23:19, not debuggable. Pass 2: 9 of 9. Pass 1: 41 of 43; the simulated-store flows need a debug build. Both failures were timing under load, and the app did the right thing:<ul><li>`move-start-rejected`: the start stayed and the message had gone. The tap took 7.5 s to settle, so the 8 s message ran out before the check;</li><li>`undo-window-passes`: as in line 3.</li></ul>Both flows were made load-proof: a tap that does not wait to settle, and the 20 s timeout pass. Each then passed 2 of 2 at load average 18–20, during the dev session's emulator walk. Line 7 on this APK: ✅ |
| 11 | Play pre-launch report (closed testing track, ed695424) | Not waited for: the user's decision of 2026-10-28 (morning): Play has produced no report yet, and Publish does not wait for one. Neither session could open Play Console (no Chrome connection) |
| 12 | Internal testing with people, at least a week (BRD §14.1) | Not waited for: the user's decision of 2026-10-27 at 20:30 (see Decision). 1.0.0 has no users yet, so 1.0.1 went to production review without a week of people testing it |

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
| #74 Settings' version, API 36 | ✅ | ✅ | ✅ | ✅ |
| #74 Settings' version, API 26 | ✅ | ✅ | ✅ universal APK | ✅ universal APK |
| #77 privacy link with no browser | ✅ | ✅ | ✅ by hand | ✅ by hand |
| #78 a start inside an ended migraine | ✅ | ✅ | ✅ | ✅ |
| #79 landscape time pickers | ✅ | ✅ | ✅ | ✅ |
| #80 typing in a full note | ✅ | ✅ | ✅ | ✅ |
| #81 emoji at the limit, and paste-then-DEL | ✅ | ✅ | ✅ | ✅ |

The retests ran on emulator-5580 from 17:00 to 21:40, each run from a clean install. The evidence is in the job
scratchpad (`sweep/retest-101.md`).
- **#74:** Settings reads "Version 1.0.1", with no crash. On API 26 (emulator-5582), the row reads "1.0.1" on the debug
  APK and on the universal APK, twice each, and `logcat -b crash` stays empty.
- **#77:** the `privacy_link` scenario failed 2 of 2 before it checked anything.
  - Its flow tapped "Settings" during home's 1 s mode guard right after launch, so Settings never opened.
  - The flow now waits for home and the guard. Run that way, the scenario passed 2 of 2.
  - On R8, by hand with Chrome disabled: no FATAL, Migrely stays in front, and the page shows migrely.health/privacy.
- **#78:**
  - The earlier migraine now ends at the new start, so the two only touch.
  - Its pain point moved to the new end.
  - Its details and History show the trimmed end.
  - An end 6 or more minutes ahead is refused.
- **#79:** on all 3 screens in landscape:
  - The accessibility tree has 1–12 o'clock, AM, PM and the minutes.
  - Every hour was picked, and 3:47 PM was saved.
  - It also passed with rotation while the picker is open, on the 24-hour clock, and at font 2.0.
  - On this screen size, 5, 6 and 7 o'clock show after the dialog scrolls.
- **#80:**
  - A "Q" in the middle of a full note is refused, the text stays the same, and the cursor stays where it was.
  - A longer paste over a selection is cut to fit.
  - A suggestion that rewrites the word before the cursor keeps the text after it. It ran in 3 of the 4 runs: Gboard's
    strip was in the way once. Gboard's own autocorrect never fired on this emulator.
- **#81:** no lone surrogate or "�" anywhere: not in the field after reopening, the database or the JSON backup.
  - 999 characters + 👍 and 996 + 👨‍👩‍👧 are refused whole.
  - The paste that ends in 👍👍👍👍👍 stores 4 of them.
  - The paste-then-DEL case stores "ab…" at 998.
- **R8 core flows, once:** log, add a past migraine, a medication, History and its month view, and a JSON backup. No
  crash.
- **ANR watch:** the dev session saw one ANR on develop's #89 debug build. Its path was run 10 times on R8 and 5 times on
  debug, plus about 40 other picker openings.
  - No `am_anr`, no crash, and `lastanr` says no ANR has occurred since boot.
  - The slowest frame from Next to the picker was 85 ms; the date dialog took up to 0.9 s at load average 93.

## What these checks do not prove

| Line | Passing it does not show |
|---|---|
| 1 Build gate | that the logic is tested: code coverage is not measured (audit C2) |
| 1a Lint | anything about behaviour; lint is not yet in the gate (D123), so a later change can bring errors back |
| 1b Instrumented | the screens: it covers DAO SQL, migrations and design-system components only |
| 2, 3, 10 Smoke and regression | the scenarios without a flow: 241 of 508 (47%) |
| 4 Accessibility audit | what a screen reader says: TalkBack itself is not driven (the audit reads labels, contrast and sizes). Also the dialogs: no state captures an open picker. #79 and #101 were found by flows that pass through them, not by the audit (QA 13d adds those states) |
| 5 Device scenarios | anything beyond its named scenarios. "Google Play offers no products" (`premium_no_products`) cannot run on this build, as its store command is on develop only |
| 6 Random input | anything beyond 5,000 events from one seed |
| 6a Upgrade | an upgrade through Android's backup on a new phone |
| 7 Permissions | what the Play app does with billing data, which Google Play's own terms govern |
| Retests | the fixes' neighbours beyond the cases tried. The dev session's review of #78 and #81 left these, all for `develop`, none a launch blocker:
<ul>
<li>#89 (S3): a start up to 5 minutes ahead, through the details screen or a merge, can overlap a migraine wholly after it;</li>
<li>#90 (S3): undoing "Migraine ended" can reopen a migraine over a newer one, as in 1.0.0;</li>
<li>#91 (S4): the tag name and the medication fields still cut text the way #80 and #81 did;</li>
<li>#92 (S3): the time picker's AM/PM choice is not exposed to accessibility services.</li>
</ul> |

### Capabilities with under half of their scenarios checked end to end

From line 9, with the follow-up in `docs/qa/test-strategy.md`:

| Capability | Checked | User risk | Follow-up |
|---|---|---|---|
| attack-details | 6/37 | high | sweep A passed 79 of 92 core scenarios by hand on build 4; QA 13d turns them into flows |
| past-attacks | 7/29 | high | as above |
| medication-intakes | 4/14 | high | as above |
| attack-mode | 5/14 | high | as above |
| in-app-messages | 7/24 | high (undo of a delete) | sweep 2's delete retries passed 14 of 14; the rest on the 1.0.1 build, then QA 13d |
| month-sky | 12/36 | medium | unit tests hold the day rules; the rest of sweep 2 samples them |
| app-shell | 5/16 | high (the network promise) | line 7 and the universal APK's permissions hold the promise; line 2a the language rules |
| custom-tags | 2/7 | medium | the rest of sweep 2, then QA 13d |
| app-identity, brand-typography, release-build | 0 of 3, 5 and 3 | low, low, high | release-build is checked by lines 7 and 10 without Covers lines; QA 13d adds them |
| quality-gates | 13/52 | low for users | QA's own rules |

### Deferred QA changes and checks that could not run

- **Deferred QA changes:**
  - visual regression and pseudo-locales (D42, audit C4);
  - screen-state tests (audit C3).
  Both wait for the dev session's build logic (audit C1).
- **Checks that could not run:**
  - **Device tests in CI (audit C5):** GitHub Actions starts no job while the billing lock lasts.
  - **Code coverage:** not measured until audit C2.
  - **Play's grace period and account hold:** they need a failed renewal.
  - **"A new phone" from Android's cloud backup:** bmgr's local transport rejects the app's backup, so it needs a real
    Google backup.
