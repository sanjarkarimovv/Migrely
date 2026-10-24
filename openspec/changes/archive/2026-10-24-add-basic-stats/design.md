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
  - for the previous period, its attacks, marks and intakes. For a past month the period is the whole previous month;
    for the current month it is the previous month's same dates, up to today's date or its last day.
- **Failures:** a failed read for the month shown fails the month, as with marks today. A failed read for the previous
  period only drops the progress message.
- Days are counted to today in the current month. Future dates never count.
  - So an attack logged in a zone ahead of the phone, whose own date is still tomorrow, shows on that future cell
    (D50) but enters the numbers only once that date arrives.
- **The current month** is decided by the month, not by its last day. So on 31 October the message still reads
  "… than this time last month" (review).
- **Before any attack** there is no progress message, on home as in History, where the first-use card stands in
  (EC-HIS-01). The use case passes no previous period then.
- `PeriodEntriesDomainModel` is a domain model in `model/`. `MonthStatsCalculator` stays internal to the use case,
  as a pure helper.
- **Accepted:** the chronic copy names "15", because that is the clinical definition of chronic migraine (ICHD-3),
  not a setting.

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
- **Colours:** "fewer" uses `MigrelyColors.Positive`, the design system's colour for progress messages; more or as
  many use the neutral `onSurfaceVariant`. The amber `tertiary` stays reserved for warnings such as overuse. The
  chronic card is a `surfaceContainerHigh` notice with the information icon, not an error colour.
- **Pain levels:** `PainLevelDomainModel.of` is the one place for the 1–3 / 4–6 / 7–10 split (EC-UX-09). History's
  `IntensityLevelPresentationMapper` now maps through it, so the skies and the average peak cannot disagree.
- **Copy:** plurals come from string resources ("1 fewer migraine day", "3 fewer migraine days").

## Risks

| Risk | Mitigation |
|---|---|
| Reading two months makes History slower | at most about 60 attacks and 62 days of marks and intakes, read on the background dispatcher |
| A user who logs only migraines never sees a progress message | intended (D55): the daily reminder in change 6 adds entries |
| The rule moves module and changes behaviour by mistake | its tests move with it, and home's and History's own tests stay unchanged |
