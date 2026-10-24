# Design

## Decisions

### D1. Modules and data

- **`:features:insights:{domain,data,presentation,ui}`.** The domain uses `:features:attackdays:domain` for the day
  rule.
- **Records:** the data layer reads `BackupDataSource.records()` (attacks, intakes with medications, day marks, custom
  tags) and `PreferencesDataSource.periodTracking()`. Trigger names come as words: the built-in English labels and the
  user's own.

### D2. Domain

- **`InsightsCalculator(dates, now)`** is pure and unit-tested. It has one function per section:
  - `enough()`: the count of migraines started in the period, and the days since the first migraine;
  - `triggers()`: the five with the highest count, ties broken by name;
  - `weekdays()` and `timesOfDay()`: counts in the logging zone, with the busiest or null on a tie;
  - `cycle(periodDates)`: cycle starts, the window days within the period, and the migraines started in those days;
  - `medications()`: acute only, answered and helped counts;
  - `trends(now)`: the last 12 calendar months with migraine days, migraines started, acute medication days, and
    bed-rest migraines.
- **`GetInsightsUseCase(months)`** returns `Ready(insights)` or `Failed`. Its insights carry
  `NotEnough(missingMigraines, missingDays)` or the sections, plus the trends either way.

### D3. UI

- **The screen:** period chips 3, 6 and 12; the progress card or the sections; then the trends.
- **Bars** are drawn with `Canvas`, each row with its numbers as text and a merged `contentDescription`.
- **Every count is written with its total**, so the shares never mislead on a small number.

## Risks

| Risk | Mitigation |
|---|---|
| A share read as a cause | copy reviewed against INS-F6; the cycle section always gives the base rate next to the share |
| Small numbers | the 5-migraine and 3-answer thresholds, and the count beside every share |
