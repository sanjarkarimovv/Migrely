# Tasks

## 1. Test first

- [x] 1.1 `DeletionOfferTest`:
  - a deletion just made, or 30 s old, is offered;
  - one more than 30 s old is not;
  - one stamped 1 ms or more after now is not.
  The future cases failed with the old rule.
- [x] 1.2 `AttackDaoDetailsTest`:
  - a deletion stamped ahead does not hide a newer real one;
  - one stamped ahead is purged for good.
- [x] 1.3 `AttackDataSourceDetailsTest`: the recent deletion and the purge use now plus the 2 s tolerance

## 2. Fix

- [x] 2.1 `AttackDao.recentlyDeleted(since, until)` and `purgeDeletedOutside(cutoff, ahead)`, used by `AttackDataSourceImpl`
- [x] 2.2 Shared `isFreshDeletion` in the design system; home and History use it
- [x] 2.3 BRD EC-ATK-21

## 3. Verify

- [ ] 3.1 Gate: `assembleDebug test ktlintCheck detekt lint`
- [ ] 3.2 `:datasource:implementation:connectedDebugAndroidTest` on emulator-5570
- [ ] 3.3 `openspec validate fix-undo-future-deletion --strict`
- [ ] 3.4 On emulator-5570:
  - with the clock 3 days ahead, delete a migraine;
  - set the clock back and cold start twice: no Undo on home, and none on History;
  - a new deletion then offers Undo, and Undo restores that migraine.

## 4. Review

- [x] 4.1 Independent review (`superpowers:code-reviewer`) of 3b442503. Its Important findings (History, the datasource) are fixed here.
- [ ] 4.2 Review of the follow-up
