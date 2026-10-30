# Tasks

## 1. BRD

- [x] 1.1 D122 in BRD §17

## 2. Simulated store (debug)

- [x] 2.1 Test first: with `no-products`, the store connects, reads and acknowledges owned purchases, lists no products
  and answers a purchase with Unavailable; `reset` brings the products back. It fails before the change
- [x] 2.2 `SimulatedStoreMode.NoProducts` and its answers in `SimulatedBillingDataSourceImpl`
- [x] 2.3 The new command in `SimulatedBillingReceiver`'s documentation

## 3. Verify

- [x] 3.1 Gate: `assembleDebug test ktlintCheck detekt`
- [x] 3.2 `openspec validate add-simulated-no-products --strict`
- [x] 3.3 On emulator-5570, debug build: set `no-products` by adb broadcast, tap a premium feature, and see the paywall
  with "Premium isn't available in your region yet"; an owned purchase stays Premium; `available` brings the
  products back

## 4. Review

- [x] 4.1 Independent review (`superpowers:code-reviewer`) of `git diff develop...feature/add-simulated-no-products`

## Verification record

- **Tests:**
  - The no-products test was written first. It failed on the old code: an unknown command was ignored, so all
    products were still listed.
  - After the change, `SimulatedBillingDataSourceImplTest` passes 15/15. That includes the review's two additions:
    the acknowledgement is saved, and `reset` brings the products back.
- **Gate:** `assembleDebug test ktlintCheck detekt` passes. The first run failed only on ktlint, for a test name over
  100 characters.
- **Validation:** `openspec validate add-simulated-no-products --strict` passes.
- **Emulator (emulator-5570, API 36, debug build), 0 FATAL EXCEPTION:**
  - **Baseline:** the paywall lists Yearly ("7 days free, then $29.99/year."), Monthly and Lifetime. Lifetime bought
    through the simulated sheet gives "Premium · lifetime".
  - **Owned + `no-products`:** Settings still shows "Premium · lifetime". Doctor report opens with no paywall.
  - **Nothing owned + `no-products`:** Doctor report and Insights both open the paywall with "Premium isn't
    available in your region yet" and "Everything else in Migrely works as before.". No price and no buy button.
  - **`available`:** the three prices and "Start free trial" are back.
- **Found on the walk (existing behaviour, not changed):** after `reset`, Premium stays until the next check. The app
  asks the store again only when its saved answer is older than 15 minutes (`CHECK_INTERVAL`), or on "Restore
  purchases". "Restore purchases" then gave "Free" and "No purchases were found for this Google account." QA steps
  that take Premium away with `reset` should restore or move the clock.
- **Review findings and fixes:**
  - **The running app remembers Play's answer for an hour:** D122 and the receiver's documentation now say to
    force-stop the app after a mode switch.
  - **D4's reason:** corrected. Real Play answers Failed for a product it has no details for.
  - **Test gaps:** the acknowledgement is now saved and checked, and a `reset` test was added.
  - **The long documentation line:** rewrapped.
  - **For QA:** `qa/device_scenarios.sh` does not check the `no-products` mode. QA owns that file.
  - **Scope:** commit 57b6b8f also records the manual Publish of 1.0.0 in D124. It is listed in the PR.
- **Not checked:** the self-shown paywall staying away in `no-products`. That needs the 7-day and 5th-migraine
  conditions, and QA's scenario covers it.
