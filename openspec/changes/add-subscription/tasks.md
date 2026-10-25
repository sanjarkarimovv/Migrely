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

- [ ] 4.1 Gate, and instrumented tests.
- [ ] 4.2 Emulator walk with the simulated store:
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
- [ ] 4.3 Independent review; fix and record.
