# Design

## Context

- `GetHistoryMonthUseCase` already loads a month's attacks, with a two-day zone margin, and its day marks. Both
  History and home's month card (`MonthSkyViewModel`) use it.
- The rule for an attack's days lives in History's `AttackDays.kt`. Home's `GetTodayUseCase` has a copy of it.
- `MedicationDataSource.acuteIntakesTakenBetween(from, to)` returns intakes of attack medications with their zone.
  Change 3a added it for the overuse rule.

## Decisions

### D1. A shared rule for an attack's days (BRD D57)

- New pure-Kotlin module `:features:attackdays:domain` with `object AttackDays`:
  - `datesTouched(start, startZone, end, now)`;
  - `windowStart(from, zone)` and `windowEnd(to, zone)`, with the two-day margin.
- It is an object because the rule is pure, and no test needs to replace it.
- History's and home's domains depend on it, and their copies go. Its tests move with it.

### D2. The numbers are computed in the domain

- `HistoryMonthDomainModel` gains `stats: MonthStatsDomainModel`:
  - `migraines`, `migraineDays`, `painFreeDays` and `attackMedicationDays`;
  - `averagePeak`, an intensity level or null;
  - `chronic`, true at 15 migraine days or more;
  - `progress`, a `ProgressDomainModel` or null.
- `ProgressDomainModel` has a `difference` (negative is fewer) and `comparedWith`: `WholeMonth(YearMonth)` or
  `SameDaysLastMonth`.
- A pure class, `MonthStatsCalculator`, computes them from the month's data and the previous period's data. It has its
  own unit tests, kept to about ten cases each.
- **Reads:** `GetHistoryMonthUseCase` reads the month as now, plus:
  - the month's attack-medication intakes, through a new single-function `GetAttackMedicationIntakesRepository`
    (`Loaded` or `Failed`);
  - for the previous month's first N days: its attacks, marks and intakes.
- **Failures:** a failed read for the month shown fails the month, as with marks today. A failed read for the previous
  period only drops the progress message.
- Days are counted to today in the current month. Future dates never count.

### D3. Data

- `HistoryRepository` implements the new repository through `MedicationDataSource.acuteIntakesTakenBetween`. A
  `DataException` becomes `Failed`.
- No database change.

### D4. Presentation and UI

- **The month's presentation and UI models** carry the summary:
  - the five numbers;
  - the average peak as a level, or none;
  - chronic;
  - the progress: `Fewer`, `More` or `Same`, with the count and what it was compared with.
- **History:** `MonthSummary` sits under the calendar and before the attack list. It is a titled card with five
  label–value rows, then the chronic card, then the progress line. Each row is one node for screen readers, for
  example "Migraine days: 6".
- **Home:** the month card shows the progress line under the calendar. Home's attack mode has no month card, so it
  never shows there (EC-UX-13).
- **Colours:** the warm tone is used only for "fewer", and the neutral tone is `onSurfaceVariant`. The chronic card
  uses the information surface of the messages, not an error colour.
- **Copy:** plurals come from string resources ("1 fewer migraine day", "3 fewer migraine days").

## Risks

| Risk | Mitigation |
|---|---|
| Reading two months makes History slower | at most about 60 attacks and 62 days of marks and intakes, read on the background dispatcher |
| A user who logs only migraines never sees a progress message | intended (D55): the daily reminder in change 6 adds entries |
| The rule moves module and changes behaviour by mistake | its tests move with it, and home's and History's own tests stay unchanged |
