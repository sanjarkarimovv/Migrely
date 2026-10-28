# Proposal

## Why

QA #105 (S2): if the app is closed while Google Play's purchase sheet is open, and Play then completes the payment,
the next start within 15 minutes still shows Free. The purchase also stays unacknowledged until a later check.
- **Cause:** before the sheet opens, the app stores what it asked for: the plan, the trial and the time (review M5).
  `PremiumChecker.boughtAfterLaunch` is there to match that record with the purchase Play reports. But the start asks
  `EntitlementRules.isDue`, which looks only at the time of the last answer (`CHECK_INTERVAL`, 15 minutes). The check
  that would find the purchase never runs.
- **Also from it:**
  - a pending payment made the same way shows "Free" instead of "Payment pending";
  - a user who buys again in the meantime loses the trial's end.

## What Changes

- **Domain:** `EntitlementRules.isDue` also takes the stored launch. Google Play is due at once when a purchase sheet
  was launched at or after the last answer, because that sheet's own answer was never seen.
  - The rule is bounded. After one check, the last answer is newer than the launch and the 15 minutes apply again.
  - The launch record stays as before: up to a day, cleared once its purchase is found, so a later check still gives
    a late purchase its plan and trial.
- **`PremiumChecker.current()`:** passes the stored launch.
- **BRD:** EC-BIL-13.
- **Spec:** `subscription` gets a requirement for a purchase completed while the app was closed.

**Unchanged:**
- a sheet that answers: its record is cleared right after its check, as before;
- the 15-minute interval otherwise;
- pending and unacknowledged purchases, which are still asked about every time.

## Capabilities

### Modified Capabilities
- `subscription`: a purchase completed while the app was closed is found at the next start.

## Impact

- `:features:subscription:domain`: `EntitlementRules.kt`, `PremiumChecker.kt` and their tests.
- `docs/BRD.md`: EC-BIL-13.
