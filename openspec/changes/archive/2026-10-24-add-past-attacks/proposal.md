# Proposal

## Why

Many attacks are never logged as they start. The user is asleep, at work or too unwell to reach the phone, and only
remembers the attack later.
- Today the only way to log one is "Migraine started", which always starts now. A forgotten attack from last night
  cannot be logged at all, so the history, the stats and the doctor report (changes 2, 5, 10) would miss it.
- A past attack is the attack most likely to collide with one already logged. Since change 1b, a time change that would
  overlap another attack is refused (BRD D22), and the user has no way to say "these were the same migraine".

## What Changes

- **Home link (BRD 18.3 A, D26):** in the normal mode, "Add a past migraine" sits under "Migraine started". It is not
  offered in attack mode (BRD 18.4 G4).
- **Add a past migraine screen (BRD ATK-F3, D26)**, a second screen of the `attack` feature:
  - "Started" and "Ended" rows open the shared date-and-time picker. They start at two hours ago and now, rounded down to
    the minute.
  - Changing the start keeps the duration, as in a calendar. The end can be anything between the start and now.
  - "Add migraine" stores the attack and opens its details screen, so the pain level and the rest can be added next.
    Back from the details returns home.
- **Validation (BRD EC-ATK-03, EC-ATK-04, EC-ATK-17, EC-ATK-18):**
  - a time more than 5 minutes in the future and an end before the start are refused with an inline error;
  - an overlap of less than a minute snaps to the other attack's boundary;
  - a double tap stores one attack.
- **Overlaps (BRD EC-ATK-05, EC-ATK-15):** a past attack that overlaps other attacks is not stored. A card names every
  overlapping migraine and offers "Merge them" or "Change the times".
- **Merging (BRD D27, D28, EC-ATK-16):**
  - one attack remains: the one being edited, or the earliest overlapping one;
  - its times cover every merged attack and pain entry, and never shrink;
  - pain entries and tags are combined, the highest impact is kept, and notes are joined oldest first (at most 1,000
    characters);
  - the result is in progress when any merged attack was;
  - the other attacks are removed in the same step, and "Migraines merged." confirms it. There is no undo (D28).
- **Details screen (MODIFIED):** a time change that overlaps other attacks now names all of them, and the inline error
  offers "Merge them".

**Out of scope:**
- **Past attacks still in progress:** an attack that is still going is logged with "Migraine started" and "Started
  earlier?" (change 1).
- **Pain level on the add screen:** it is added on the details screen that opens next.
- **Undo after a merge** (D28), and splitting an attack in two.
- **Change 2:** adding a past attack from the history calendar for a chosen day.
- **Change 3:** medications, which will join the merge rules when they exist.

## Capabilities

### New Capabilities

- `past-attacks`: adding an attack that has already ended, its validation, and merging when it overlaps other attacks.

### Modified Capabilities

- `attack-details`: overlapping times name every overlapping attack and can be merged instead of only refused.
- `today-sky`: the normal home mode offers "Add a past migraine".

## Impact

- **Changed modules:**
  - `:features:attack:*`: the add screen (ViewModel, view state, notifications, navigation events, UI), the create and
    merge use cases, list-based overlap lookups, and merging on the details screen.
  - `:features:home:*`: the link and its navigation event.
  - `:datasource:*`: create an ended attack only when nothing overlaps, list every overlapping attack, and merge
    attacks in one transaction. There is no schema change: the database stays at version 2.
  - `:app`: the `AddPastAttack` route, the `announceMerge` argument of `AttackDetails`, DI and navigation mappers.
- **Docs:** BRD 9 (EC-ATK-15 to 18), 13 (row 1c), 17 (D26–D28), 18.3 A. `docs/ARCHITECTURE.md` §3 and §6a cover
  the second attack screen and merging.
- **BRD references:** ATK-F3, EC-ATK-03, EC-ATK-04, EC-ATK-05, EC-ATK-09, EC-ATK-10, EC-ATK-13, EC-ATK-15, EC-ATK-16,
  EC-ATK-17, EC-ATK-18, EC-TIM-01, EC-UX-03, EC-UX-04, EC-UX-13, EC-UX-16, D22, D26, D27, D28.
