# Tasks

## 1. Test first

- [x] 1.1 `EntitlementRulesTest`:
  - a sheet launched at or after the last answer makes Play due at once;
  - one launched before it waits for the interval;
  - one ahead of the clock or more than a day old makes nothing due.
- [x] 1.2 `PremiumCheckerTest`:
  - the start a minute after a check, with a sheet launched since and its purchase completed, asks Play, acknowledges the purchase and shows the trial;
  - with the payment pending, it shows pending;
  - after a cancelled sheet, Play is asked once and the launch is kept;
  - a failed write keeps the launch.
  The tests fail before the fix.

## 2. Implement

- [x] 2.1 `EntitlementRules.isDue(entitlement, now, launched)`
- [x] 2.2 `PremiumChecker.current()` passes the stored launch; `check()` forgets a launch only after a written entitlement (review)
- [x] 2.3 BRD EC-BIL-13

## 3. Verify

- [x] 3.1 Gate: `assembleDebug test ktlintCheck detekt lint`
- [x] 3.2 `openspec validate fix-purchase-after-process-death --strict`
- [x] 3.3 On emulator-5570 with the simulated store (QA #105's steps): a purchase completed while the app was closed shows at the next start within 15 minutes, acknowledged, with the trial end; a pending one shows "Payment pending"

## 4. Review

- [x] 4.1 Independent review (`superpowers:code-reviewer`)

## Verification record

- **Gate:** `assembleDebug test ktlintCheck detekt lint` passes.
- **Unit tests:** `EntitlementRulesTest` 23/23 and `PremiumCheckerTest` 17/17. The QA scenario's tests failed before the fix.
- **Emulator walk (emulator-5570, API 36, debug build of f7254e2c, simulated store, QA #105's steps):**
  - **Completed while closed:**
    - Yearly › "Start free trial", then force-stop with the sheet open. `launched_at` was later than `last_check`.
    - A completed, unacknowledged `sim-yearly-trial` purchase was planted 2 s after the launch.
    - The app was started 44 s later. The start asked Play at once: `last_check` was 2 s after the start, 49 s after the previous check.
    - Settings: "Free trial / Trial ends November 4, 2026". `trial_end` is the purchase time plus 7 days.
    - The purchase is acknowledged, and the `launched_*` keys are gone.
  - **Pending while closed:**
    - With Monthly and with Yearly, Settings shows "Payment pending", and Insights opens its paywall with the "Payment pending" block.
    - The prefs had `kind=pending` 1 s after the start, before any tap.
  - **Only once:** after a restore, two cold starts 11 s and 20 s later left `last_check` unchanged, so Play was not asked.
  - **Crashes:** 0.
- **Review:** nothing blocking. Fixed in 4030f5fc:
  - the launch rule is bounded: no launch ahead of the clock, none more than a day old, and the parameter is required;
  - a launch is forgotten only after its entitlement is written;
  - boundary and cancelled-sheet tests;
  - `never()` checks now match null;
  - the spec names the exception and the screens.
- **Left (residual risk):** a check that runs while the sheet is open, before Play completes the payment, moves the last answer past the launch (see the proposal).
- **Seen on the device, not part of this change:**
  - a deletion stamped in the future, after a clock set back, makes home offer "Undo" at every cold start, over the Settings gear;
  - filed separately.
