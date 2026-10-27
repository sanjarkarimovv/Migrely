# Tasks

## 1. Fix

- [x] 1.1 BRD EC-ATK-03
- [x] 1.2 Tests first: update, merge and create-past each reject a start 2 or 3 minutes after now as `InFuture(Start)`.
  They fail before the change
- [x] 1.3 Domain: the three use cases check the start against now
- [x] 1.4 Details: the start picker ends at now

## 2. Verify

- [ ] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [ ] 2.2 `openspec validate fix-start-after-now --strict`
- [ ] 2.3 On emulator-5570: in a past migraine's details, the start picker offers no time after now, and the end still
  takes up to 5 minutes ahead
