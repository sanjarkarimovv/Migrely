# Proposal

## Why

QA found that a person who logs attack medication but no migraine never sees History's "Last 30 days" overuse
indicator: History shows "No migraines logged yet" with no summary, and months with only intakes cannot be reached.
Medication-overuse headache is most likely in exactly that person, so the indicator must not depend on a logged
migraine (BRD D99). QA's review then found that a past day with such an intake still looks like "nothing logged" in
the calendar (BRD D100).

## What Changes

- History's first month also counts the first attack-medication intake.
- The "No migraines logged yet" state shows only when there is no attack, no day mark and no attack-medication intake.
- Otherwise the month and its summary show as usual, with the current month's indicator.
- Preventive intakes still do not count.
- In the month calendar, on History and home, a past day with an attack-medication intake and no attack has a solid
  outline and no sky, and TalkBack says "attack medication taken, no migraine logged" (BRD D100).

## Capabilities

### Modified Capabilities

- `attack-history`: the first month, and when the empty state shows.
- `medication-overuse`: the standing indicator does not need a logged migraine.
- `month-stats`: the summary shows with nothing logged but a day mark or an attack-medication intake; the progress
  message still needs a logged attack.
- `month-sky`: a day with attack medication only, its legend entry and its spoken description.

## Impact

- **Medication datasource:** the earliest attack-medication intake.
- **History:** domain (the earliest month, "anything logged", and each day's attack medication), presentation (Ready
  or NothingLogged, and the day's sky), UI (the day's description and the legend), and their tests.
- **Design system:** a solid-outline day in `MonthSkyCalendar` and its legend.
