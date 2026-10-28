## Why

Build 4 (`release/1.0.0` @ 29aadb3) is waiting to be published. As of 2026-10-27:
- a closed testing release of build 4 is made for Play's pre-launch report (Go/No-Go line 11);
- managed publishing is on, so nothing is published until "Publish changes" is pressed;
- the dev session holds that step for QA's verdict;
- the week of internal testing (line 12) is waived, on the user's decision of 2026-10-27.

QA's own checks and the pre-launch report are therefore all that stand before users. Four gaps remain:
- **The release fixes' new scenarios have no check.** D110, D119, #66 and #67 added them to the specs, and QA 11b's D61
  list names them.
- **Most screens were never in the accessibility audit.** The tour has 10 states: home, attack mode, History, attack
  details and the past-migraine screen. It has none for these screens:
  - onboarding, Settings and its pages;
  - medications, reminders and Your data;
  - the paywall, the doctor report and Insights.
  None of those has been checked for contrast, touch targets, labels or keyboard reach, at 200% font or in landscape.
- **The release-candidate checks leave out whole test levels.** An outside quality audit of 2026-10-27 showed this, and
  QA confirmed every finding in the code:
  - Android Lint was never run. On build 4 it fails with 3 errors (#74).
  - The instrumented tests (Room DAOs, migrations, design-system components) are run only by the dev session, when a
    datasource changes. They are never run on a release candidate.
  - Users install the split APKs that Play builds from the bundle: language, density and ABI. QA tests only the
    universal APK.
  - Go/No-Go line 9 says "exit 0" while only 257 of 502 scenarios (51%) had a check when this change began. That exit code proves only that
    no flow names a stale scenario.
  - No Go/No-Go names the risk that is left: the scenarios without a check, the deferred QA changes (D42's visual
    regression) and the checks that could not run.
- **Build 4 needs exploratory testing.** It is the build users have: the user published it on 2026-10-27, before
  QA's verdict. QA keeps testing it, now after the release. Anything found is judged against the dev's launch-blocker
  rule:
  - a launch blocker goes into hotfix 1.0.1 (versionCode 5), cut from `main`;
  - anything else goes into the hotfix or into `develop`, as the dev session decides.

## What Changes

- **The 1.0.1 hotfix candidate (design D11):** build 4's sweep found #77 to #81, and `hotfix/1.0.1` fixes them. QA gives
  it a Go/No-Go in `docs/qa/go-no-go-1.0.1.md`, with retests of the 5 issues. This change's device checks run on it.

- **Base:** develop 438be90. Its product code is the same as build 4's. The checks run on build 4's APKs and bundle,
  built in `../migraine-app-qa-rc` (`QA_APK`, D201).
- **Go/No-Go record (`docs/qa/go-no-go-1.0.0.md`):**
  - line 11 is the closed track's pre-launch report, read when it is out (build 4 went to closed testing first);
  - line 12 is waived by the user on 2026-10-27;
  - build 4's new lines: lint (#74), the instrumented tests, the split APKs;
  - a residual-risk section.
- **Release-candidate checks (D204):**
  - Android Lint and the instrumented tests on the QA emulator join the Go/No-Go;
  - a new `qa/split_apks.sh` installs the split APKs of a device profile from the candidate's bundle with bundletool,
    and the smoke suite and the D117 checks run on them;
  - the profiles are ru on Android 8.0, and he, uz and en on the target version.
- **Residual risk (D204):** every Go/No-Go record states what each check does not prove. It also lists:
  - each capability with under half of its scenarios checked;
  - each deferred QA change;
  - each check that could not run.
- **Test strategy (`docs/qa/test-strategy.md`, D204):** the test levels, with their tool, owner and when they run. A
  risk register holds, per capability:
  - its checks at each level;
  - what is not checked;
  - the follow-up.
- **Release-fix scenarios:**
  - `app-settings` › Privacy and about: "The full privacy policy" and "No browser" (D119). A device scenario disables the
    browser on the QA emulator and enables it again.
  - `day-markers`, with "Track periods" off (#66, D79):
    - Mark a chosen day › "Period tracking off" and "Removing a mark with tracking off";
    - Period today from home › "Period tracking off" and "Removing today's mark with tracking off".
  - `doctor-report` › Large periods › "No note among the migraines shown" (#67, D84), with a new seed.
  - `subscription` › When the paywall shows itself › "Google Play offers no products" (D110), once the dev's
    simulated-store command (D122) is on develop. Until then it stays a D61 gap.
- **Accessibility audit on every screen (D203):** tour states for these screens:
  - onboarding;
  - Settings, with Warning signs, Privacy and Theme;
  - My medications;
  - Reminders;
  - Your data;
  - the paywall;
  - the doctor report's period screen;
  - Insights.
  Each is captured at 100% font, at 200% font and in landscape. A failure is filed as an issue (`accessibility`).
- **Exploratory testing on build 4:**
  - a sweep of the 92 core-flow scenarios with no check (attack details, past migraines, medication intakes, attack
    mode, logging, History), with note, interruption and repeated-tap edge cases;
  - charters: rotation and process death on the paywall, the report and Insights; the report and Insights at 200% font
    and in dark theme; app restarts during a purchase.
- **Spec `quality-gates`:**
  - the Go/No-Go gate adds lint, the instrumented tests, the split APKs and the residual-risk statement (MODIFIED,
    D204);
  - the accessibility audit holds a state for every screen of the app (ADDED, D203);
  - the test strategy keeps a risk register (ADDED, D204).
- **BRD:** §13 row 13c, §17 D203 and D204.

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `quality-gates`:
  - the release Go/No-Go gate (MODIFIED, D204);
  - every screen in the accessibility audit (ADDED, D203);
  - the test strategy and its risk register (ADDED, D204).

## Impact

- **Files:**
  - `.maestro/`:
    - new `a11y-tour/` states;
    - a `day-markers/` flow;
    - `device/` steps;
  - `qa/device_scenarios.sh`, `qa/seed_backup.py`, and the new `qa/split_apks.sh`;
  - `docs/qa/go-no-go-1.0.0.md`, the new `docs/qa/test-strategy.md`, `docs/QA.md` and `docs/BRD.md`.
- **BRD IDs:** D44, D79, D84, D110, D117, D119, D122, D203, D204; EC-UX-03 and EC-UX-04; D42 and §14.1.

**Out of scope:**
- **Product code.** Defects go to GitHub issues, marked launch blocker or not, and the dev session fixes them.
  - Hotfix 1.0.1 carries the blockers.
  - The hotfix or `develop` carries the rest, including #74 and the audit's S2, S3 and S5.
- **E2E flows for the swept core-flow scenarios.** A later QA change (13d) turns what the sweep confirms into flows.
- **The audit's other changes:**
  - C1 (build logic), C2 (coverage gate) and C6 (architecture rules) belong to the dev session, after launch.
  - C3 (screen-state tests) and C4 (D42's visual regression) are QA changes that follow C1, which adds their test
    dependencies to the convention plugin.
  - C5 (device tests in CI) waits for GitHub Actions, which the account's billing lock stops today.
- **Google Play itself:** the grace period, account hold, and the renewal-date charge of a plan switch.
- **Screenshot comparison of drawings (D42).**
- **"Accessible marks › Disabled switch"** (an older D61 gap).
