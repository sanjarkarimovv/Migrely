# Proposal

## Why

QA #78 (S2): a live migraine can be started inside a stored migraine, so two overlapping migraines are stored.

- **How it happens:** "Add a past migraine" accepts an end up to the 5-minute clock tolerance after now (EC-ATK-03).
  Tapping "Migraine started" within those minutes stores a new attack whose start lies before that end.
- **Where the gap is:** every other way of setting times rejects overlaps (EC-ATK-05). The one-tap start checked only
  for an attack in progress.
- **Effect:** home counts "2 migraines today", and the Doctor report counts both.

## What Changes

- **The start transaction** (`AttackDao.startIfNoneInProgress`) still adds no attack while one is in progress. When it
  adds one, an ended attack that runs past the new start by at most the 5-minute clock tolerance first gets its end
  moved to the start, and its pain entries after the start move to the start. Both happen in the same transaction, so
  the attacks only touch at one instant.
- **A longer run past the start**, which only a phone clock moved back can leave, is not touched (EC-TIM-04). A silent
  cut of a real attack would be worse than an overlap the user can see and fix.
- **BRD** EC-ATK-19, and an attack-logging requirement with its scenarios.
- **Instrumented DAO tests.**

**Unchanged:**
- the clock tolerance itself;
- attacks that ended before or exactly at the start;
- an attack in progress, which still blocks a second start (EC-ATK-01).

**Undo of the start:** it removes the new attack and does not give the earlier one its old end back. That end was still
in the future when the new start replaced it.

**Out of scope, for develop:**
- **An ended attack that lies wholly after the start.**
  - The details screen and a merge accept a start up to the end, and the end may be up to 5 minutes after now. So a
    user who moves an attack's start into the next few minutes and then taps "Migraine started" still gets an overlap.
  - The fix is to cap those starts at now, as "Add a past migraine" already does. That is a validation change in two
    use cases and the picker, so it goes to develop.
- **Undo of an end** reopens the attack without checking ended attacks added since (`reopenIfNoneInProgress`). This
  existed before 1.0.0.
- Repairing overlaps already stored by 1.0.0. QA is checking whether a backup that holds one still imports, and that
  result decides whether a follow-up is needed.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `attack-logging`: a start never overlaps an ended attack.

## Impact

- `:datasource:implementation`: `AttackDao` (two update queries and the start transaction), `AttackDaoTest`
  (instrumented).
- `docs/BRD.md`: EC-ATK-19.
