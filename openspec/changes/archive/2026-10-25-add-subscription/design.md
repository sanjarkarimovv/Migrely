# Design

## Decisions

### D1. The billing datasource

- **The interface:** `BillingDataSource` in `datasource/source`, feature-agnostic. It covers:
  - connecting, with the result Available, Unavailable or Failed;
  - the products, with their offer tokens, Play's formatted prices and any free-trial phase;
  - the owned purchases: product, state, purchase time, auto-renewing, token, acknowledged, and whether the signature is
    valid;
  - a flow of the purchase updates that arrive while no purchase sheet waits for them, such as a pending payment
    completed at a shop (a sheet that is open takes its own update, and gives up after 10 minutes);
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

- **The cache:** in its own preferences file, `migrely_entitlement`, behind `PreferencesDataSource`. It holds the kind
  (none, subscription, lifetime, pending), the product and base plan, the purchase time, auto-renewing, the trial's end
  when known, and the last successful check.
  - It is never backed up or moved to a new phone (D115): `backup_rules.xml` and `data_extraction_rules.xml` exclude
    it beside each shared-preferences include. A purchase belongs to a Google account, so a restored backup, or a phone
    on another account, never gets Premium from an old cache; on a new install Premium comes only from Google Play.
  - The paywall marks and the trial banner's dismissal stay in `migrely_preferences`, which is backed up, so a
    restored user does not see the onboarding paywall again.
  - "Delete all data" clears both files.
- **Checking:** `RefreshPremiumUseCase` asks Play at most every 15 minutes, unless forced after a purchase or on restore,
  and every time while a payment is pending or a purchase is not yet acknowledged. `PremiumChecker`, one for the app,
  runs the checks one at a time.
- **The rules:**
  - pending never unlocks;
  - Unreachable keeps a subscription for 3 days after the last check, but not when the clock reads earlier than that
    check, and lifetime until Play answers otherwise;
  - an unverified signature never unlocks;
  - an unacknowledged PURCHASED purchase is acknowledged, with 3 retries after 1, 2 and 4 seconds. One that still fails
    is marked in the cache; while it is, or while a payment is pending, a WorkManager periodic check runs about every
    12 hours with no constraints (unique work `premium-check`), so the purchase is acknowledged within Play's 3 days
    even if the app stays closed. The check cancels the work once neither holds, and "Delete all data" cancels it too;
  - the plan and trial asked of Play's sheet are kept before it opens, so a purchase that completes while the app is
    away still gets them at the next check.
- **Observing:** built differently from the `ObservePremiumUseCase` first planned, since ARCHITECTURE §7 rule 6 allows
  a flow use case only in a scope that is cancelled, and view models are not:
  - screens read the status with one-shot use cases on enter, on resume and after every action;
  - `WatchPurchaseUpdatesUseCase` turns each update Play sends with no sheet open into a check, which acknowledges
    the purchase and keeps the cache at once. `:app`'s `PurchaseUpdatesWatcher`, started by `MigrelyApplication`,
    collects it for as long as the app runs;
  - after each such check the watcher signals the paywall and Settings' Premium section, which read the status
    again, so a payment that completes while either shows unlocks there; the paywall then shows its success.

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
  - billing is known to be available, so a paywall that could only show an error never interrupts (walk B-1);
  - no attack is in progress;
  - the trigger has not fired before;
  - no self-shown paywall appeared in the last 7 days.
- **The 5th migraine:** the paywall shows itself only once Insights could show the patterns it promises, by Insights'
  own rule (D88, `PatternsReadiness` in the shared `:features:patterns:domain`): at least 5 migraines started in the
  6 months Insights open on, the first at least 30 days ago. The home trigger asks after every home load in today's
  mode.
- **Marking:** when one shows, its trigger and the time are recorded.

### D6. The trial banner

Trial end = purchase time + the trial's length as Play gives it (`freeTrialPeriod`, P7D for the yearly offer),
recorded only when the purchase used the trial offer on this phone. The banner shows on home from 24 hours before the
end until dismissed; the dismissal is stored with that end date. It says "today" or "tomorrow" by the phone's date, and
once the subscription is cancelled it says the trial and Premium end, with no advice to cancel.

### D7. Copy

Plain and calm. Prices are always Play's formatted strings. The terms line under the button: "Renews automatically.
Cancel anytime in Google Play." and, with the trial, "Cancel before the trial ends and you won't be charged."
