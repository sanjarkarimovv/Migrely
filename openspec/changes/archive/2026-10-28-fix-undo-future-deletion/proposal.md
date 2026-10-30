# Proposal

## Why

#112 (S3, found by the #105 walk): after the phone's clock was set back past a migraine's deletion, home offered
"Migraine deleted / Undo" at every cold start. On this layout the Undo button sits over the Settings gear (#88), so a
tap meant for Settings brought the deleted migraine back. The review found the same on History, which offered it at
every visit. It also found the cause at its root, in the datasource.
- **Datasource:**
  - The latest deletion is looked for from the undo window's start, with no upper bound, so one stamped in the future
    was "recent" for days. It also hid a real deletion made after it: the Undo offered for that one restored the wrong
    migraine, or after the UI check, nothing at all.
  - The purge removes only deletions older than the window, so a future one stayed restorable.
- **Home and History:** each offered a deletion when its age was at most 30 seconds. A negative age passed.

## What Changes

- **Datasource:**
  - `AttackDao.recentlyDeleted(since, until)` looks only up to now plus a 2-second clock tolerance.
  - `purgeDeletedOutside(cutoff, ahead)` also removes deletions stamped further ahead than the undo window (2 minutes).
    They can never be offered, and the user asked for them to go. One only a little ahead, as after an automatic clock
    correction of a few seconds, stays restorable, so an Undo already offered still works (review).
- **UI:**
  - One rule for both screens, `isFreshDeletion` in the design system's `snackbar` package: a deletion is offered only
    when it is at most 30 s old and at most 2 s ahead of the clock.
  - Home and History use it in place of their own copies.
- **BRD:** EC-ATK-21.
- **Spec:** `attack-details` › Delete with undo gets two scenarios.

**Unchanged:**
- the undo window of 2 minutes;
- the 30-second offer;
- a normal deletion and its Undo.

## Capabilities

### Modified Capabilities
- `attack-details`: a deletion stamped after now is never offered, and is purged.

## Impact

- `:datasource:implementation`: `AttackDao`, `AttackDataSourceImpl`, their tests (`AttackDaoDetailsTest`, `MedicationDaoTest`, `AttackDataSourceDetailsTest`).
- `:designsystem`: new `snackbar/DeletionOffer.kt` and `DeletionOfferTest`.
- `:features:home:ui`: `HomeMessages.kt`.
- `:features:history:ui`: `HistoryMessages.kt`.
- `docs/BRD.md`: EC-ATK-21.
