# Proposal

## Why

The MVP promises simple monthly numbers and a progress message (BRD §6.1 item 5, STA-F1, STA-F2, EC-UX-14). Today a
user sees skies but no counts: how many migraines, how many migraine days, and whether this month is better than the
last. The chronic-migraine indicator (≥15 migraine days) is a safety prompt to see a doctor, and it is missing.

## What Changes

- **A month summary in History** (D53), under the calendar, for the month shown, titled "October so far" or
  "September". It shows five numbers:
  - "Migraines started": attacks started in the month;
  - "Migraine days": days an attack touches;
  - "Pain-free days";
  - "Average peak", as a word (D56);
  - "Days with attack medication".
- **The chronic indicator** (D54): with 15 or more migraine days in the month, the summary shows a calm information
  card: "That's a lot. Talk to your doctor about it."
- **The progress message** (D55):
  - it compares migraine days with the same days of the previous month, so a month in progress is never compared with
    a whole month;
  - better is warm, worse or equal is neutral, and nothing blames;
  - it shows only when both periods have at least 10 days with entries;
  - it appears in the summary, and on home's month card under its title for the current month.
- **One rule for an attack's days** (D57): the rule moves from History's domain, and from home's copy, into a shared
  domain module, `:features:attackdays:domain`.

**Out of scope:** EC-MED-06's standing overuse indicator goes into the summary in change 5a (`add-overuse-indicator`).

## Capabilities

### New Capabilities

- `month-stats`: the month summary, the chronic indicator and the progress message.

### Modified Capabilities

None. The calendar and today's card keep their behaviour, and only their day rule moves module.

## Impact

- **New module** `:features:attackdays:domain` (pure Kotlin), used by home's and History's domains.
- **`:features:history`:**
  - domain: `MonthStatsDomainModel` and its calculation in `GetHistoryMonthUseCase`, with a new
    `GetAttackMedicationIntakesRepository`; the previous month is read for the comparison;
  - data: the repository reads `MedicationDataSource.acuteIntakesTakenBetween`;
  - presentation and UI: the summary card in History, and the progress line on home's month card.
- **No database change.**
- **Docs:** BRD §13 (rows 5 and 5a) and §17 (D53–D57); ARCHITECTURE for the new module.
