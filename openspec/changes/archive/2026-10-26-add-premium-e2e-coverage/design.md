# Design

## Context

See proposal.md (Why).
- **Tools:** the existing ones, plus:
  - D60 (clock), D62 (keys), D63 (verification depth), D64 (notifications);
  - D200 (files through the picker).
- **Build:** `release/1.0.0` at cedc4c4:
  - versionName 1.0.0 and versionCode 2;
  - the debug variant, which holds the simulated store (D113).
- **Store state:** the simulated store keeps its state in `shared_prefs/migrely_simulated_store.xml`.
  - "Delete all data" leaves it alone; `pm clear` and Maestro's `clearState` empty it.
  - The entitlement cache is `shared_prefs/migrely_entitlement.xml`. Every backup rule excludes it (D115).
- **Simulated sheet:**
  - on a purchase, "Complete the purchase", "Pay later (pending)" and "Cancel";
  - with a payment pending, "Complete the pending payment" and "Decline the pending payment";
  - on Manage, "End the subscription", "Refund the lifetime purchase" and "Close".
- **adb broadcast:** `am broadcast -p com.migrely.app -f 0x20 -a com.migrely.app.debug.SIMULATED_STORE --es command
  <c>`. The commands are available, unavailable, unreachable, complete-pending, decline-pending and reset.

## Goals / Non-Goals

**Goals:**
- every scenario of `subscription`, `doctor-report` and `trigger-insights` is checked, except those only Google Play
  can show;
- the paywall after onboarding and Premium in Settings are checked;
- the existing regression passes on the release branch's build.

**Non-Goals:**
- the RC Go/No-Go, the R8 APK and the API 26 smoke (`verify-release-candidate`);
- a real Play purchase;
- product fixes.

## Decisions

### D1. The build under test

`QA_APK` names the APK that `qa/maestro.sh --install` and the scenarios' reinstalls use. It defaults to the worktree's
own debug APK.
- **Where it is built:** the release branch's APK is built in a detached worktree, `../migraine-app-qa-rc`. That
  worktree has no `keystore.properties`, so release variants are signed with the debug key (D102).
- **What stays untouched:** the dev worktrees (`migraine-app`, `-release`, `-edge`, `-qafix`).
- **The RC commit:** when the dev session sends it, the detached worktree moves to that commit.

### D2. Self-showing paywalls in the existing flows

With the store available, the onboarding paywall "Get more from your diary" shows after the last onboarding step, Skip
included.
- **`skip-onboarding.yaml`:** closes it when it shows. Its own check lives in the new onboarding flow, so this is not
  where it is tested.
- **The patterns paywall:** it needs a first migraine at least 30 days old, and no flow but the new ones logs five.
  Any flow it does show in gets an optional Close.
- **`message_tap_guard`:** its control tap on "Doctor report" now opens the report's paywall for a free user. So its
  "doctor report" screen also accepts that paywall.

### D3. Premium through the simulated store (D201)

- **Buying:** `subflows/buy.yaml` opens the paywall's plan (PLAN = Yearly, Monthly or Lifetime) and taps the action
  button. On the simulated sheet it then chooses SHEET:
  - "Complete the purchase";
  - "Pay later (pending)";
  - "Cancel".
- **adb:** `qa_store <command>` in the scenarios sends the broadcast and waits 1 s. Maestro flows cannot run adb, so
  every step that needs a store command is a device scenario.
- **Time rules use the emulator clock (D60):**
  - offline: unreachable, then the clock 2 days on (Premium stays) and 4 days on (Premium locks);
  - the 7-day rule: the onboarding paywall, then the clock 3 days on and 8 days on;
  - the trial banner: a trial bought, then the clock 6 days on.
  - The app re-checks at start and on return (at most every 15 minutes). After each clock change the app is
    force-stopped and opened again.
- **New phone:** `run-as rm shared_prefs/migrely_entitlement.xml`, with the store's file kept. Premium must come back
  at the first check.
- **Another account (D115):** the scenario reads the shared-preferences files the app wrote after a purchase. It
  confirms `migrely_entitlement.xml` is one of them, and that `data_extraction_rules.xml` (cloud-backup and
  device-transfer) and `backup_rules.xml` exclude exactly that name. A rename then fails the check.
- **Prices:** the app locale de-DE (`cmd locale set-app-locales`) shows euros with a decimal comma. The simulated store
  formats in the phone's locale.

### D4. Seeded data through backup files (D202)

`qa/seed_backup.py <seed> <out.json> [--today YYYY-MM-DD]` writes a v6 backup. The format is the one `backup_files.py`
checks, with the same field names and enums: ACUTE or PREVENTIVE; SIMPLE, TRIPTAN or COMBINATION; and the effect
values.
- **Seeds are named,** so each check knows its numbers:
  - `report-three-months`: 92 days, 18 migraine days, 4 "Bed rest" attacks;
  - `report-partial-month`: triptans on 11 days from 26 September;
  - `report-tokyo`: one attack logged in Asia/Tokyo on the evening of 14 October, Tashkent time;
  - `report-heavy`: 900 attacks in 12 months, with notes;
  - `insights-patterns`: 10 migraines in 6 months, Stress with 6, Mondays 3, Ibuprofen 7 of 9 answers helped;
  - `insights-three`: 3 migraines;
  - `four-old` (the 5th is logged in the app) and `five-new`: migraines in 6 months, the first at least 30 days old or
    not; `five-old` stays for a manual walk.
- **Dates:** all are relative to `--today`, the emulator's date, so a seed never goes stale.
- **Import:** an empty phone shows "Import" on onboarding's restore; a phone with entries uses Replace. The result
  message is waited for as in D200.

### D5. The report PDF (D202)

`qa/report_pdf.py <pdf> [--pages-at-most N] [--text T ...] [--regex R ...] [--absent T ...] [--every-page T]` runs
`pdfinfo` and `pdftotext -layout`. Values are checked as whole numbers ("bed rest 4" is not "bed rest 40"), and a
migraine row's start as a pattern, since the cell wraps and an en-US phone writes 12-hour times.
- **Where the file comes from:** the newest `cache/reports/<id>/*.pdf`, through `run-as … cat` into the run's folder.
- **The partial file:** "Closed while making it" force-stops the app during the progress. After the next start,
  `run-as ls cache/reports` must list no temporary file.

### D6. What Google Play alone can show

These are D61 gaps, each with its follow-up in `verify-release-candidate`:
- "Grace period": Play's in-app messages, which the simulated store does not show;
- "Release": the release APK, checked statically for the absence of the simulated store classes and receiver;
- the release half of "No Google Play billing service on the phone".

The debug half of that last scenario is checked here: unavailable shows the line at once, and no paywall shows itself.

## Risks / Trade-offs

- **The emulator under memory pressure:** it drops off adb. Runs are restarted, and the infrastructure is recorded,
  as in 12b.
- **Clock jumps:** WorkManager and the 15-minute re-check can race the jump. Every time step force-stops the app and
  re-opens it, and each assertion waits.
- **Store state is not reset by "Delete all data":** every scenario starts with Maestro's `clearState`, which empties
  the store, and `restore()` sets it available again on exit.
