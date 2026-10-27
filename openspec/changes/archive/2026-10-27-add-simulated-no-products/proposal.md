# Proposal

## Why

QA asked for a way to check the subscription scenario "Google Play offers no products" (BRD D110) on the QA emulator.
In that case Google Play billing connects but returns none of Migrely's products, as before they are set up in Play
Console. The debug build's simulated store has no such state: it can be available, unavailable or unreachable, and
when available it always sells the three products. So the scenario could only be checked against the real Play Store
with an empty Play Console, which no longer exists for Migrely.

## What Changes

- **Debug only:** the simulated store gets a fourth state, `no-products`, set from adb like the others. In this state:
  - billing connects;
  - the purchases already owned are read and acknowledged as usual;
  - the product list is empty;
  - a purchase attempt answers "unavailable", as Google Play answers ITEM_UNAVAILABLE.
- `available` and `reset` bring the products back.
- The adb receiver's documentation lists the new command.
- BRD D122.

**Unchanged:**
- the app's own rules for an empty product list (D110), which this change only makes checkable;
- the release build, which holds no simulated store.

**Out of scope:** QA's scenario and `docs/QA.md`, which QA owns and writes in its next change.

## Capabilities

### Modified Capabilities

- `subscription`: the store for testing can also answer with no products.

## Impact

- `:app` debug source set: `SimulatedBillingDataSourceImpl`, `SimulatedStoreMode`, `SimulatedBillingReceiver`
  (documentation).
- `:app` testDebug: `SimulatedBillingDataSourceImplTest`.
- `docs/BRD.md`: D122.
