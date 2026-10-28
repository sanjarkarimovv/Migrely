# Tasks

## 1. Test first

- [x] 1.1 `DeletionOfferTest`:
  - a deletion just made, or 30 s old, is offered;
  - one more than 30 s old is not;
  - one stamped after now is not.
  The last failed with the old rule.

## 2. Fix

- [x] 2.1 `isFreshDeletion` takes ages from 0 to 30 s only

## 3. Verify

- [x] 3.1 Gate: `assembleDebug test ktlintCheck detekt lint`
- [x] 3.2 `openspec validate fix-undo-future-deletion --strict`
- [ ] 3.3 On emulator-5570: delete a migraine with the clock 3 days ahead, set the clock back, and cold start twice. No Undo offer. A normal delete still offers Undo on home.

## 4. Review

- [ ] 4.1 Independent review (`superpowers:code-reviewer`)
