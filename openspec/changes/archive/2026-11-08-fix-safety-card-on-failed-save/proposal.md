# Proposal

## Why

On a full disk, the first pain 10 shows "Couldn't save. Your phone's storage is full." and no safety card (#155). The card comes only
from a saved 10: `AttackDetailsViewModel.onAddPointAction` shows it from `Added(firstMaximum = true)`, and `StorageFull` only notifies.
The card answers what the person just told us about their pain, not whether the diary stored it, and safety ranks first. `team-lead`
put it in 1.1.0, as a fix on `release/1.1.0`.

## What Changes

- **The card shows for a first 10 whatever the outcome.** When the person enters a 10 on the details screen and no saved attack has a 10,
  the card shows, whether the entry is saved, refused for its time, or fails to save.
- **When the app cannot tell,** because reading the saved 10s fails, the card shows.
- **A failed 10 is not stored,** so the next successful first 10 shows the card again. Accepted: twice beats missing it once.
- **The message still shows** as it does today, and both are spoken to TalkBack (design D2).

The card's text and its dismissal do not change. When the card shows is a behaviour question, so `team-lead` sent D163 to
`medical-reviewer`: NOT BLOCKING on all three questions, recorded in D163 (4). It is not clinical sign-off, which T31 still needs.

## Out of scope

- Attack mode's own pain buttons and Add past migraine, which have no card today (spec: the details screen).
- The card's text, look, place and dismissal.
- A full disk in general (EC-STO-02).

## Capabilities

### Modified Capabilities

- `attack-details`: "First maximum pain safety card".

## Impact

- `docs/BRD.md`: D163, EC-ATK-22, §13 row 32.
- `:features:attack:domain` and `:presentation`. No string, no permission, no schema.
- BRD ids: D163, EC-ATK-22, SAF-F2. Issue #155.
