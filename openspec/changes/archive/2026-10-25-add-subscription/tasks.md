# Tasks

## 1. Datasources

- [x] 1.1 `BillingDataSource` with its Play and simulated implementations, the current-activity holder, the licence key
  property and the bundle check; tests.
- [x] 1.2 Preferences: the entitlement cache, paywall marks, trial banner dismissal; tests.

## 2. Subscription feature

- [x] 2.1 Domain: entitlement rules, refresh, products, buy, upgrade, restore, should-show, trial reminder; tests.
- [x] 2.2 Data, presentation and UI: the paywall, the Premium section, the trial banner, the home trigger, the
  onboarding gate; tests.

## 3. Wiring

- [x] 3.1 History gating; routes, events and slots in `:app`; Play's in-app messages at start; DI per build type.
- [x] 3.2 `docs/release`: `play-products.md`, and the TODO(change 9) markers in the listing, Data safety, privacy policy
  and content rating.

## 4. Verify

- [x] 4.1 Gate, and instrumented tests.
- [x] 4.2 Emulator walk with the simulated store:
  - paywall contexts and prices;
  - buying each product;
  - pending, cancel and restore;
  - premium unlocking the report and Insights, and locking again;
  - the self-shown triggers and the 7-day limit;
  - never during an attack;
  - the trial banner;
  - Settings → Premium;
  - Delete all data then a fresh check;
  - 200% font and TalkBack.
- [x] 4.3 Independent review; fix and record.

## Verification record

- **Gate:** green at every step, with 1554 unit tests. `:app:assembleRelease` passes, and `bundleRelease` stops without
  the licence key. The release APK holds no simulator code and asks for BILLING but not INTERNET: billing's datatransport
  dependency declared INTERNET, and it is removed with `tools:node="remove"`. **Instrumented**: datasource 97/97 and designsystem 34/34
  (f32d372), app 1/1 (070e1d2).
- **Emulator walk** on emulator-5570 with the simulated store (5a98a49), app data restored to the baseline afterwards.
  All 12 scenarios passed:
  - gating, with the safety features free;
  - the paywall's content, with locale prices;
  - buying each plan;
  - cancel, pending and settling;
  - monthly to yearly, and lifetime over a subscription;
  - restore, Delete all data, and D115's separate unbacked-up entitlement file;
  - unavailable, unreachable, and the 3-day allowance;
  - the self-shown paywalls with the 7-day limit and never during an attack;
  - the trial banner;
  - rotation, double taps and process death;
  - 200% font and TalkBack;
  - the release APK's permissions and dex.

  It found **B1**: a pending payment settled from another plan's sheet took that plan and its trial. Fixed with tests
  (0edd069): only a purchase the sheet made takes its plan. The simulator now uses up the trial on any subscription,
  and its adb command carries `-f 0x20`.
- **Re-walk** of the review fixes (070e1d2). All passed:
  - an update with no sheet open updates the paywall and Settings in place;
  - the 12-hour background check is scheduled with no constraints while a payment is pending or unacknowledged,
    cancelled after, and cancelled by Delete all data;
  - B1;
  - trial eligibility;
  - patterns readiness, with no paywall for 5 migraines within the first 30 days;
  - an unknown plan after a reinstall offers only lifetime and "Manage subscription";
  - a cancelled trial's banner;
  - the backup excludes.
- **The real Play path:** a debug-signed release build on emulator-5570, a Google APIs image with a stub Play Store and
  no account, so billing is unavailable there. No crash and never a price. It found **B-1**: the client's own
  reconnection outlasted the 5-second wait, so billing read as "can't be reached" after about 10 s, and the patterns
  paywall showed itself with only that error. Fixed with tests (d5e2c9e): the datasource checks for Google Play's
  billing service first and answers Unavailable at once, and a paywall shows itself only when billing is known to be
  available.
  - **The first fix put the service check after `isReady`.** With auto-reconnection, Billing 8.3.0 always reports
    `isReady`, so the check never ran; the device check showed it. f32d372 moves it first, with an always-ready test.
  - **Re-checked on the device:**
    - "Premium isn't available in your region yet" in 0.6 s, also from Restore;
    - no self-shown paywall over 3 launches;
    - logcat clean: no FATAL and no reconnection lines.
- **Real purchases** are checked in 13b on Play's internal testing track with licence testers (D113). There, too:
  INTERNET's removal, Play's in-app messages, and the licence key.

## Review (4.3)

An independent `superpowers:code-reviewer` pass against Play Billing's semantics. Nothing Critical.

| Finding | Outcome |
|---|---|
| **I1** Play's updates with no sheet open were dropped (a pending payment paid later) | fixed: an app-wide watcher checks each one at once, and the open paywall or Settings updates in place |
| **I2** the 3-day acknowledgement depended on the app being reopened | fixed: 3 retries with backoff, a stored not-yet-acknowledged flag, and a 12-hour background check while pending or unacknowledged |
| **I3** the patterns paywall promised what Insights might not show yet | fixed: Insights' own readiness rule (D88), shared in `:features:patterns:domain`, and no number in the copy |
| **I4** the Play implementation was untested and never run | fixed: an injected client with unit tests; the release build run on a device (B-1 found and fixed) |
| M5 a race that lost the plan and trial | fixed: one checker at a time, and the launch saved before the sheet |
| M6 a clock set back kept a lapsed subscription | fixed: treated as expired |
| M7 Play's payment messages over an attack | fixed: not shown on attack starts |
| M8 a hard-coded trial length | fixed: from Play's offer |
| M9 another plan offered when the plan is unknown | fixed: lifetime and "Manage subscription" only |
| M10 connection handling | fixed: one shared connect, a retried launch, ITEM_UNAVAILABLE as unavailable |
| M11 the paywall stuck on "Waiting" | fixed: the sheet times out, and working state is true only while a call is out |
| M12 billing logs in backups | fixed: the datatransport database excluded |
| M13 an unchecked licence key | fixed: parsed as an X.509 RSA key in the build |
| M14 an unprotected debug receiver | fixed: DUMP permission |
| M15 the trial banner after cancelling | fixed: "trial and Premium end tomorrow", no cancel advice |
| M16 the docs and the clock in the UI | fixed |
| Test gaps | fixed. Accepted: the Play tests stub Billing 8.3.0's obfuscated `zza()`; revisit on a billing upgrade |
