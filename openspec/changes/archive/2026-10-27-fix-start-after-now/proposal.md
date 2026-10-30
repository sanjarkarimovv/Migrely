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
- **Details screen:** the start picker ends at now, or just before the end when that is earlier, and its message names
  that limit.
- **BRD** EC-ATK-03 and two spec requirements: attack-details "Exact start and end times" and past-attacks "Times in
  the past only".

**Unchanged:**
- the end's tolerance;
- existing data. A start already stored after now, at most 5 minutes ahead, becomes past by itself. Until then, saving
  that attack's end is refused with the start's "in the future" error, which the user can wait out or fix by picking
  the start again.
- the start picker's message, which now names the limit that applies: "Pick a time before now." or "Pick a time
  before the end." (review finding).

**Out of scope:** a backup import, or a phone clock moved back (EC-TIM-04), can still bring an attack that lies after
now. The import keeps the file's times as they are.

## Capabilities

### Modified Capabilities

- `attack-details`, `past-attacks`: a start can no longer be after now.

## Impact

- `:features:attack:domain`: the three use cases and their tests.
- `:features:attack:ui`: `AttackTimeSection.kt` (start picker bound).
- `docs/BRD.md`: EC-ATK-03.
