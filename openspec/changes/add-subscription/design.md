# Design

## Decisions

### D1. The billing datasource

- **The interface:** `BillingDataSource` in `datasource/source`, feature-agnostic. It covers:
  - connecting, with the result Available, Unavailable or Failed;
  - the products, with their offer tokens, Play's formatted prices and any free-trial phase;
  - the owned purchases: product, state, purchase time, auto-renewing, token, acknowledged, and whether the signature is
    valid;
  - a flow of purchase updates;
  - launching a purchase, an upgrade (`WITH_TIME_PRORATION`) and acknowledging;
  - showing Play's in-app messages.
- **Two implementations:**
  - `PlayBillingDataSourceImpl`, over Play Billing Library 8.x, using `PendingPurchasesParams`;
  - `SimulatedBillingDataSourceImpl`, in-memory, with its own sheet: Complete, Pending or Cancel.
- **Wiring:**
  - DI in `:app` binds the Play implementation in `src/release` and the simulated one in `src/debug`, so the release
    APK never contains the simulator;
  - launching needs an Activity, given by an `:app` holder of the current resumed activity, never by a feature.
- **Signatures:** checked with the Play licence public key, from `BuildConfig.PLAY_LICENSE_KEY`, filled from the gradle
  property `migrely.playLicenseKey`. Empty means skipped in debug; `bundleRelease` fails while it is empty.

### D2. The entitlement

- **The cache:** in `migrely_preferences`. It holds the kind (none, subscription, lifetime, pending), the product and
  base plan, the purchase time, auto-renewing, the trial's end when known, and the last successful check.
- **Checking:** `RefreshPremiumUseCase` asks Play at most every 15 minutes, unless forced after a purchase or on restore.
- **The rules:**
  - pending never unlocks;
  - Unreachable keeps a subscription for 3 days after the last check, and lifetime until Play answers otherwise;
  - an unverified signature never unlocks;
  - an unacknowledged PURCHASED purchase is acknowledged.
- **Observing:** `ObservePremiumUseCase` exposes the status as a flow for every screen.

### D3. The subscription feature

`:features:subscription:{domain,data,presentation,ui}` holds:
- the paywall screen, which takes the context it was opened from (onboarding, patterns, report, insights or settings);
- the Premium section for Settings;
- the trial banner;
- the home trigger;
- the gate after onboarding.

Following the app's slot pattern (D46, D80), `:app` places the Premium section into Settings and the trial banner and
trigger into home, so no feature depends on another.

### D4. Gating

- **History** reads the entitlement through its own single-function repository over the preferences cache. Its
  ViewModel then emits `OnOpenReport` or `OnOpenInsights` when premium, and `OnOpenPaywall(context)` when not.
- **The paywall:** on success it emits `OnPremiumUnlocked(context)`, which `:app` maps to the report or Insights
  (replacing the paywall), or simply back.
- **Onboarding's finish** goes to a gate route. Its ViewModel asks `ShouldShowPaywallUseCase(Onboarding)`, then shows the
  paywall or continues home.

### D5. Paywalls that show themselves

- **`ShouldShowPaywallUseCase`:** true only when:
  - the user is free;
  - billing is not known to be unavailable;
  - no attack is in progress;
  - the trigger has not fired before;
  - no self-shown paywall appeared in the last 7 days.
- **The 5th migraine:** counted from the attack datasource (started attacks, not deleted). The home trigger checks it on
  resume.
- **Marking:** when one shows, its trigger and the time are recorded.

### D6. The trial banner

Trial end = purchase time + 7 days, recorded only when the purchase used the trial offer on this phone. The banner shows
on home from 24 hours before the end until dismissed; the dismissal is stored with that end date.

### D7. Copy

Plain and calm. Prices are always Play's formatted strings. The terms line under the button: "Renews automatically.
Cancel anytime in Google Play." and, with the trial, "Cancel before the trial ends and you won't be charged."
