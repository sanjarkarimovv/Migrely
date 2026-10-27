# Proposal

## Why

QA #89 (S3), found by dev's review of the QA #78 fix in 1.0.1. The 5-minute clock tolerance (EC-ATK-03) applied to
starts as well as ends:

- the details screen and a merge accepted a start up to now + 5 minutes;
- "Add a past migraine" capped its own start picker, but its use case did not.

So an ended attack could lie wholly in the next few minutes. A "Migraine started" tapped before it then stored an
overlap, which 1.0.1's start transaction does not catch, because that transaction only trims attacks the start falls
inside.

## What Changes

- **Domain:** `UpdateAttackTimesUseCase`, `MergeAttacksUseCase` and `CreatePastAttackUseCase` reject a start after now
  as `InFuture(Start)`. The tolerance stays for ends only. It exists for a clock a little behind, and an attack cannot
  have started later than now.
- **Details screen:** the start picker ends at now, or just before the end when that is earlier.
- **BRD** EC-ATK-03 and two spec requirements: attack-details "Exact start and end times" and past-attacks "Times in
  the past only".

**Unchanged:**
- the end's tolerance;
- the in-progress attack, whose start was always now or earlier;
- existing data. A start already stored after now (only possible within 5 minutes) becomes past by itself.

## Capabilities

### Modified Capabilities

- `attack-details`, `past-attacks`: a start can no longer be after now.

## Impact

- `:features:attack:domain`: the three use cases and their tests.
- `:features:attack:ui`: `AttackTimeSection.kt` (start picker bound).
- `docs/BRD.md`: EC-ATK-03.
