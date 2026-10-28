# Tasks

## 1. Test first

- [x] 1.1 `DeletionOfferTest`:
  - a deletion just made, or 30 s old, is offered;
  - one more than 30 s old is not;
  - one up to 2 s ahead of now, as after an automatic correction, is;
  - one further ahead is not.
  The future cases failed with the old rule.
- [x] 1.2 `AttackDaoDetailsTest`:
  - a deletion stamped ahead does not hide a newer real one;
  - one stamped ahead is purged for good;
  - the window's edges: live attacks and deletions inside it stay, and only those outside go.
- [x] 1.3 `AttackDataSourceDetailsTest`:
  - the recent deletion is read up to now plus the 2 s tolerance;
  - the purge keeps the undo window on both sides of now, so a correction of a few seconds never breaks an Undo already offered (review).

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
- [x] 4.2 Review of the follow-up (e8b3226e). Its Important finding (the ahead purge) and its minors are fixed.
