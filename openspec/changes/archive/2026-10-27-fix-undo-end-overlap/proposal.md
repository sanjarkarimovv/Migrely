# Proposal

## Why

Undo of an end reopens the attack through `AttackDao.reopenIfNoneInProgress`. That checks only for another attack in
progress, not for ended attacks added since. Say an attack is ended at 13:10 through the 72-hour question's "It has
ended", and a past migraine from 13:20 to 13:30 is added while the Undo message still shows. Undo then reopens the
attack, its open end overlaps the past migraine, and the app never lets attacks overlap (BRD EC-ATK-05). The same
happens after a one-tap end when the past migraine starts at the end, which the minute snap of EC-ATK-18 makes easy. Dev's review of the QA #78 fix found this (#90). It existed
before 1.0.0, and it has not been seen on a device.

How likely it is: the undo window is 5 seconds, or longer when the phone's accessibility timeout is longer. So it
mostly reaches people who need more time.

## What Changes

- **Datasource:** the DAO reopens the attack only when, open-ended again, it overlaps no other attack. It uses the
  same overlap rule as everywhere else (`overlapping`): attacks that only touch do not overlap, and deleted attacks
  do not count. An attack in progress is open-ended, so the old check is one case of the new one.
  - An attack that no longer exists is refused too. That happens when it was deleted, or merged into another with
    "Merge them", during the undo window. Before, Undo reported "Undone" while reopening nothing, or, for a deleted
    attack, cleared its end.
  - Any other attack that ends after the start blocks Undo, not only one added since. An overlap already left by a
    phone clock moved back (EC-TIM-04) therefore refuses Undo right after the end. That is acceptable: reopening
    would only make that overlap open-ended.
- **Data and domain:** the refusal is named `NotPossible` instead of `AnotherInProgress`, in `ReopenDataModel` and
  `ReopenResultDomainModel`, like the neighbouring `RestoreDataModel.NotPossible`, because it now covers more than
  one reason. It still maps to `UndoResultDomainModel.NotPossible`.
- **UI:** none. The home screen shows the existing "This can no longer be undone.", and the attack stays ended.
- **BRD:** EC-ATK-20.

**Unchanged:**
- Undo of an end when nothing overlaps;
- the undo window;
- the message's words.

**Out of scope:** naming the newer migraine in the message. The case is rare, and the existing message is true.

## Capabilities

### Modified
- `attack-logging`: "End an attack in one tap" says Undo never reopens an attack over another one, with two
  scenarios.

## Impact

- `:datasource:implementation`: `AttackDao.reopenIfNothingOverlaps`, `AttackDataSourceImpl.reopen`, with their tests.
- `:datasource:source`: `ReopenDataModel`.
- `:features:home:domain`: `ReopenResultDomainModel`, `UndoAttackEndUseCase`, with its test.
- `:features:home:data`: `HomeRepository.reopenAttack`, with its test.
