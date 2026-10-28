# Proposal

## Why

#112 (S3, found by the #105 walk): after the phone's clock was set back past a migraine's deletion, home offered
"Migraine deleted / Undo" at every cold start. On this layout the Undo button sits over the Settings gear (#88), so a
tap meant for Settings brought the deleted migraine back.
- **Cause:** home offers a deletion when its age is at most 30 seconds. After the clock is set back, a deletion
  stamped later than now has a negative age, so it always passed. The offered deletions are kept only within a run,
  so each cold start offered it again.
- **The rule it breaks:** BRD D23 and `attack-details` › Delete with undo. A deletion is offered once, right after the
  details screen closes, for the undo window.

## What Changes

- **Home UI:** `isFreshDeletion(deletedAtMillis, nowMillis)` offers a deletion only when its age is between 0 and
  30 seconds. A deletion stamped after now is never offered.

**Unchanged:** the undo window and the deletion itself.

## Capabilities

None changed.

## Impact

- `:features:home:ui`: `HomeMessages.kt` and a new `DeletionOfferTest`.
