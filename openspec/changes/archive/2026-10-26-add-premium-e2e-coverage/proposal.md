## Why

Changes 9 (`add-subscription`, PR #55), 10 (`add-doctor-report`) and 11 (`add-trigger-insights`) are merged without
end-to-end QA, and `release/1.0.0` is cut from them. They hold 37 scenarios with no check yet:
- `subscription` 18;
- `doctor-report` 9;
- `trigger-insights` 8;
- one each in `onboarding` (the paywall after onboarding) and `app-settings` (Premium in Settings).

Three kinds of behaviour are new to QA:
- **Paid features.** A purchase goes through a store sheet. Premium depends on what the store reports, with time rules
  such as the 3 days offline, the 7 days between self-shown paywalls, and the day before a trial ends.
- **A generated PDF.** It is written to the app's private cache.
- **Numbers that need many dated entries.** Insights and the report summary count migraine days, overuse spans,
  weekdays and shares.

The self-showing paywalls also change the existing flows: the onboarding paywall shows after every Skip while the store
is available.

The release candidate's Go/No-Go (D44) waits for the RC commit, so it is the next QA change, `verify-release-candidate`.

## What Changes

- **Base:** the debug APK of `release/1.0.0` at cedc4c4, built in a detached QA worktree. `QA_APK` names the build
  under test for `qa/maestro.sh` and `qa/device_scenarios.sh`.
- **Baseline:** the full regression and the device scenarios on that build. The flows are adapted to the self-showing
  paywalls.
- **Subscription** (SUB-F*, EC-BIL-*, D105–D113, D115), through the simulated store (D201):
  - the paywall: its headlines, the products with the yearly plan chosen first, the button names, the terms, "Restore
    purchases" and Close;
  - prices in the phone's currency, and no trial line once the trial was used;
  - buying, pending (completed or declined from adb), and a cancelled sheet;
  - the entitlement:
    - offline, at 2 and at 4 days;
    - refunded;
    - a new phone, with the cache removed;
    - the cache left out of every backup rule;
  - lifetime over a subscription;
  - billing unavailable and unreachable;
  - when the paywall shows itself: after onboarding, the 5th migraine, patterns not ready, within 7 days, never in
    attack mode;
  - the trial-ending banner;
  - Premium in Settings.
- **Doctor report** (REP-F*, EC-REP-01…03, D81–D85, D108):
  - the period screen and an empty period;
  - the summary numbers, checked against the seeded data;
  - notes left out or included;
  - a migraine logged in Tokyo;
  - 12 heavy months, at most 50 pages;
  - a report closed while being made;
  - the paywall for a free user. The PDF is checked by its text and page count (D202).
- **Insights** (INS-F*, EC-INS-01…03, D87–D92, D108):
  - the period;
  - "N more migraines";
  - triggers, never causes;
  - weekday and time of day;
  - tracking off;
  - what helped;
  - the 12-month trends read by a screen reader;
  - the paywall.
- **Tools:**
  - `qa/seed_backup.py` writes v6 backups from named seeds;
  - `qa/report_pdf.py` pulls and checks the newest report;
  - a `qa_store` helper and buy subflows.
- **Spec `quality-gates`:** premium is checked through the simulated store (D201), and generated reports by their text
  (D202).
- **BRD:** §13 row 11b, D201 and D202.

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `quality-gates`: premium through the simulated store, and generated reports checked by their text (ADDED, D201, D202).

## Impact

- **Files:**
  - `.maestro/`: a new `subscription/` folder, plus `device/` and `subflows/` (the report and Insights checks need adb,
    so they are device scenarios);
  - `qa/device_scenarios.sh` and `qa/maestro.sh`;
  - new `qa/seed_backup.py`, `qa/seed_expect.py`, `qa/report_pdf.py`, `qa/backup_rules.py`, `qa/release_apk.py` and
    `qa/message_timing.py`;
  - `docs/QA.md` and `docs/BRD.md`.
- **BRD IDs:** SUB-F*, EC-BIL-*, REP-F*, EC-REP-*, INS-F*, EC-INS-*, D81–D92, D105–D115, D201 and D202.

**Out of scope:**
- Product code.
- Google Play itself, with its grace period and account hold messages and a real purchase, and the release build without
  the simulated store. These go to `verify-release-candidate` on the user's phone and the R8 APK.
- D117 (dates in English), which lands with `bugfix/launch-edge-cases`.
