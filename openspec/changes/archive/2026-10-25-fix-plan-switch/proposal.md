# Proposal

## Why

On a real phone with build 1.0.0 (2), a monthly subscriber choosing yearly got Google Play's "A technical error
occurred" sheet, every time. The billing library logged `responseCode: 5` (DEVELOPER_ERROR). The app asked Play to
switch with `WITH_TIME_PRORATION`. Google Play's documentation says that switching plans within the same subscription,
from an auto-renewing plan to an auto-renewing plan, allows only `CHARGE_FULL_PRICE` and `WITHOUT_PRORATION`; any other
mode fails. `monthly` and `yearly` are two base plans of one subscription, `migrely_premium` (D107), so a monthly
subscriber could never move to yearly.

## What Changes

- A plan switch uses `WITHOUT_PRORATION` (D120). It matches the default left in Play Console, "Charge at next billing
  date":
  - the new plan starts at once;
  - its price is charged on the next billing date;
  - nothing is charged twice and no partial credit is worked out.
- The switch still names the subscription it replaces with `setOldPurchaseToken`. The app stays on
  `SubscriptionUpdateParams` rather than `SubscriptionProductReplacementParams`, the newer API, which has an open
  Google issue (#561369347) for plan changes.
- A unit test pins the mode to one Play allows within a subscription.
- BRD EC-BIL-10 and the products guide say the same.
- versionCode 3.

## Capabilities

### Modified Capabilities

- `subscription`: moving from monthly to yearly starts the yearly plan at once and charges it on the next billing date.

## Impact

- `:datasource:implementation`: the replacement mode, as a named constant.
- Comments in `:datasource:source` and `:features:subscription:domain`.
- `docs/BRD.md`, `docs/release/play-products.md`.
- `app/build.gradle.kts`: versionCode 3.
