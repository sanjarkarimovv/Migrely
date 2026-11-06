# Proposal

## Why

PR #136 (issue #91) fixed the custom tag name and the medication name and dose fields. Typing in the middle of a full
field used to lose the last character, and an emoji across the cap left a lone surrogate. The behaviour now has tests, but
`custom-tags` and `medication-list` say only "stops at 40" and "stops at 60". So the rule can be broken again with no spec
to say so. This change records what the code already does, using the note's rule (`attack-details`, EC-ATK-13). It adds no
behaviour. The rules are BRD D152, EC-ATK-12, EC-ATK-13 and EC-MED-13.

## What Changes

- **Tag name, medication name and dose, in the edit form and on "Medication taken":** typing in the middle of a full field
  keeps the end of the text and the cursor; an emoji across the cap is refused whole; a paste is cut after a whole
  character. Text and cursor survive recreation.
- **A stored name or dose** opens with the cursor at the start, as before, because a person recognises a medication by the
  start of its name.
- **What the edit form shows is what it saves.** If two inputs race in one frame, the losing edit is dropped visibly. A
  cursor move or a refused edit does not change the form, so it does not clear a shown save problem.
- **The note** keeps opening at the end, and its cursor now survives recreation.

## Out of scope

- Product code, tests, `.maestro/` and `qa/`. The code is done in PR #136.
- Counting visible characters (#82, deferred): the counters stay in UTF-16 units.
- `medication-intakes`: "No medication yet" keeps its text. The log form's name field is covered in `medication-list`,
  beside the other medication fields.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `custom-tags`: the name field at its cap.
- `medication-list`: the name and dose fields at their caps, and what the edit form opens with and saves.
- `attack-details`: where the note's cursor opens and that it survives recreation. The old text was not wrong, only silent.

## Impact

- `docs/BRD.md`: D152, EC-ATK-12, EC-ATK-13, EC-MED-13. No §13 row, as with the earlier small fix `fix-note-limit-edits`.
- `openspec/specs/`: the three capabilities above.
- No code, permission, schema or price change.
