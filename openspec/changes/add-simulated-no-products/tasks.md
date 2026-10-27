# Tasks

## 1. BRD

- [ ] 1.1 D122 in BRD §17

## 2. Simulated store (debug)

- [ ] 2.1 Test first: with `no-products`, the store connects, reads and acknowledges owned purchases, lists no products
  and answers a purchase with Unavailable; `reset` brings the products back. It fails before the change
- [ ] 2.2 `SimulatedStoreMode.NoProducts` and its answers in `SimulatedBillingDataSourceImpl`
- [ ] 2.3 The new command in `SimulatedBillingReceiver`'s documentation

## 3. Verify

- [ ] 3.1 Gate: `assembleDebug test ktlintCheck detekt`
- [ ] 3.2 `openspec validate add-simulated-no-products --strict`
- [ ] 3.3 On emulator-5570, debug build: set `no-products` by adb broadcast, tap a premium feature, and see the paywall
  with "Premium isn't available in your region yet"; an owned purchase stays Premium; `available` brings the
  products back
