# Design

## Context

`SimulatedBillingDataSourceImpl` (debug source set of `:app`) stands in for Google Play in debug builds (D113). Its
mode, kept in its own preferences file, decides what each call answers. QA sets the mode through
`SimulatedBillingReceiver` with an adb broadcast (D201).

## Goals and non-goals

- **Goal:** a state in which the app sees exactly what Play shows before its products exist, so that QA can check
  D110's "Google Play offers no products" scenario.
- **Non-goal:** any change to how the app treats an empty product list. Those rules exist and are unit-tested in the
  subscription layers.

## Decisions

| # | Decision | Why |
|---|---|---|
| D1 | A new `SimulatedStoreMode.NoProducts("no-products")` | the store's other states are modes too, set and reset the same way |
| D2 | `connect()` answers Available; `purchases()` reads the store | Play's billing connection and a user's purchases do not depend on products being listed |
| D3 | `products()` answers an empty list | Play returns no product details for IDs it does not know |
| D4 | `purchase()` answers Unavailable | `PlayBillingDataSourceImpl` maps Play's ITEM_UNAVAILABLE to Unavailable. The paywall offers nothing to buy in this state anyway |
| D5 | `acknowledge()` works whenever the store connects | an owned purchase still has to be acknowledged; the check now asks `connect()`, so it follows any future mode too |

## Risks

- **None for release:** the debug source set is not in release builds, and `qa/release_apk.py` checks that.
