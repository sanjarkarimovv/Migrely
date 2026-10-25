# Proposal

## Why

QA found that a person who logs attack medication but no migraine never sees History's "Last 30 days" overuse
indicator: History shows "No migraines logged yet" with no summary, and months with only intakes cannot be reached.
Medication-overuse headache is most likely in exactly that person, so the indicator must not depend on a logged
migraine (BRD D99).

## What Changes

- History's first month also counts the first attack-medication intake.
- The "No migraines logged yet" state shows only when there is no attack, no day mark and no attack-medication intake.
- Otherwise the month and its summary show as usual, with the current month's indicator.
- Preventive intakes still do not count.

## Capabilities

### Modified Capabilities

- `attack-history`: the first month, and when the empty state shows.
- `medication-overuse`: the standing indicator does not need a logged migraine.

## Impact

- **Medication datasource:** the earliest attack-medication intake.
- **History:** domain (the earliest month, and "anything logged"), presentation (Ready or NoAttacks), and their tests.
