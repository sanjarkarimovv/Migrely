# Proposal

## Why

The diary holds the answers to the questions migraine brings:
- what often comes with an attack;
- when attacks come;
- whether they follow the cycle;
- which medication works;
- whether things are getting better.

The MVP promises these as insights in plain words that never claim a cause (BRD §6.1 item 11, INS-F1–F6, EC-INS-01–03).

## What Changes

- **"Insights"** (D87), from History's top bar. The period is 3, 6 (default) or 12 months.
- **Enough data first** (D88): at least 5 migraines in the period and 30 days of history. Until then, a progress card says
  what is missing.
- **The sections:**
  - **Triggers** (D89): the five most often logged, as a share of migraines, with no causal words.
  - **Patterns** (D90): by weekday and time of day, in the zone each was logged.
  - **The cycle** (D91): the share of migraines in the days around a period, next to the share those days are of the
    period. It shows only with Track periods on and at least two cycles.
  - **Medications** (D92): how often each attack medication helped a lot or completely, from at least 3 answers.
  - **Trends** (D92): migraine days, migraines, medication days and bed-rest migraines for each of the last 12 months.

**Out of scope:** the premium gate (change 9).

## Capabilities

### New Capabilities

- `trigger-insights`: the Insights screen, its thresholds and its sections.

### Modified Capabilities

- `attack-history`: History's top bar offers "Insights".

## Impact

- **New** `:features:insights:{domain,data,presentation,ui}`. Records come from `BackupDataSource`, and Track periods
  from `PreferencesDataSource`.
- **History:** a second top-bar action.
- **`:app`:** the route and DI.
