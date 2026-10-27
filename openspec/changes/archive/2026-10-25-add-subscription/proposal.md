# Proposal

## Why

Migrely's premium features (the doctor report and Insights) are built, but nothing sells them. The MVP needs the
subscription (BRD §7, SUB-F1, SUB-F2, EC-BIL-01…12). The user chose Google Play Billing directly over RevenueCat
(D105), so the app stays without internet access and its "sends nothing" promise holds.

## What Changes

- **Play Billing** (D105, D107):
  - products `migrely_premium` (base plans `yearly`, with a 7-day trial for new subscribers, and `monthly`) and
    `migrely_lifetime`;
  - purchases acknowledged and signature-checked on the phone;
  - monthly-to-yearly proration, lifetime over a subscription, billing unavailable, and Play's own in-app messages for
    grace and hold.
- **The premium entitlement** (D106): an active subscription or lifetime, cached with a 3-day offline allowance for
  subscriptions; pending never unlocks.
- **Premium features** (D108): the doctor report and Insights open the paywall for free users. Safety features stay free.
- **The paywall** (D109): three products at Play's local prices, honest terms, restore, and a close button from the first
  frame. **When it shows itself** (D110): at the end of onboarding and after the 5th migraine, at most once in 7 days,
  never during an attack.
- **The trial** (D111): its end date when known, and a home banner a day before it ends.
- **Settings → Premium** (D112): status, the trial's end, "Restore purchases" and "Manage subscription".
- **Testing** (D113): a simulated store in debug builds. Release always uses Play.

## Capabilities

### New Capabilities

- `subscription`: products, purchases, the entitlement, the paywall and when it shows, the trial reminder, and the debug
  store.

### Modified Capabilities

- `doctor-report`: premium only.
- `trigger-insights`: premium only.
- `app-settings`: the Premium section.
- `onboarding`: the paywall after the last step.

## Impact

- **New datasource** `billing`: an interface and two implementations, Play and simulated (debug).
- **Preferences:** the entitlement cache, paywall timestamps, and trial and banner flags.
- **New `:features:subscription:*`:** domain, data, presentation and UI.
- **Gating** in History's report and Insights entries, the home banner, Settings, the onboarding end, and `:app` DI and
  routes.
- **The permission check** keeps INTERNET forbidden; `com.android.vending.BILLING` is allowed.
- **`docs/release/`:** the TODO(change 9) markers are resolved, and the products are listed for Play Console in
  `play-products.md`.
