## Why

Build 4 (`release/1.0.0` @ 29aadb3) goes to production review at 100%, on the user's decision of 2026-10-27:
- the week of internal testing (Go/No-Go line 12) is waived;
- there is no closed track, so Play makes no pre-launch report before launch (line 11).

QA's own checks are therefore all that stand before users, and Android vitals after launch. Three gaps remain:
- **The release fixes' new scenarios have no check.** D110, D119, #66 and #67 added them to the specs, and QA 11b's D61
  list names them.
- **Most screens were never in the accessibility audit.** The tour has 10 states: home, attack mode, History, attack
  details and the past-migraine screen. It has none for these screens:
  - onboarding, Settings and its pages;
  - medications, reminders and Your data;
  - the paywall, the doctor report and Insights.
  None of those has been checked for contrast, touch targets, labels or keyboard reach, at 200% font or in landscape.
- **Build 4 needs exploratory testing.** It is the build users will get, so QA keeps testing it in parallel. Anything
  found ships as a quick update release.

## What Changes

- **Base:** develop 438be90. Its product code is the same as build 4's. The checks run on build 4's APKs, built in
  `../migraine-app-qa-rc` (`QA_APK`, D201).
- **Go/No-Go record (`docs/qa/go-no-go-1.0.0.md`):**
  - line 12 waived by the user on 2026-10-27;
  - line 11 not available before launch (no closed track), the user's decision of 2026-10-27. If the production upload
    produces a report, it is read and recorded.
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
- **Exploratory charters on build 4:** rotation and process death on the paywall, the report and Insights. Also the
  report and Insights at 200% font, dark theme, and app restarts during a purchase.
- **Spec `quality-gates`:** the accessibility audit holds a state for every screen of the app (ADDED, D203).
- **BRD:** §13 row 13c and §17 D203.

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `quality-gates`: every screen is in the accessibility audit (ADDED, D203).

## Impact

- **Files:**
  - `.maestro/`:
    - new `a11y-tour/` states;
    - a `day-markers/` flow;
    - `device/` steps;
  - `qa/device_scenarios.sh` and `qa/seed_backup.py`;
  - `docs/qa/go-no-go-1.0.0.md`, `docs/QA.md` and `docs/BRD.md`.
- **BRD IDs:** D79, D84, D110, D119, D122, D203; EC-UX-03 and EC-UX-04; D42, D44 and §14.1.

**Out of scope:**
- Product code. Defects go to GitHub issues, and the dev session fixes them in an update release.
- Google Play itself: the grace period, account hold, and the renewal-date charge of a plan switch.
- Screenshot comparison of drawings (D42).
- "Accessible marks › Disabled switch" (an older D61 gap).
