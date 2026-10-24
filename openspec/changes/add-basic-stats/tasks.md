# Tasks

## 1. Shared rule (design D1)

- [x] 1.1 `:features:attackdays:domain` with `AttackDays` and its tests; History and home use it, and their copies go.
  Verify: `./gradlew :features:attackdays:domain:test :features:history:domain:test :features:home:domain:test`

## 2. Domain (design D2)

- [x] 2.1 `MonthStatsDomainModel`, `ProgressDomainModel`, `MonthStatsCalculator` with tests.
- [x] 2.2 `GetAttackMedicationIntakesRepository`; `GetHistoryMonthUseCase` reads the extra data and fills `stats`;
  tests for the failure paths.
  Verify: `./gradlew :features:history:domain:test`

## 3. Data (design D3)

- [x] 3.1 `HistoryRepository` reads attack-medication intakes; DI.
  Verify: `./gradlew :features:history:data:test :app:assembleDebug`

## 4. Presentation and UI (design D4)

- [x] 4.1 Presentation models and mappers; ViewModels unchanged apart from mapping.
- [x] 4.2 History's `MonthSummary` and home's progress line; strings with plurals.
  Verify: `./gradlew :features:history:presentation:test :features:history:ui:test`

## 5. Verify

- [x] 5.1 Gate.
- [ ] 5.2 Emulator walk on emulator-5570 with seeded data: a past month, the current month, the chronic card, the
  progress message (better, worse, too few entries), screen readers, 200% font, landscape.
- [x] 5.3 Independent review; fix and record.

## Review (5.3)

An independent `superpowers:code-reviewer` pass found nothing critical. It confirmed:
- the start-zone counting, months crossed, attacks in progress, pain-free days under attacks, intakes by their zone and
  the union of entry days;
- January against December, the window margins, the failure routing, the shared rule matching the old copies, DI, and
  accessibility;
- the contrast of the progress colour (about 7.7:1).

| Finding | Outcome |
|---|---|
| **I1** on a month's last day the current month read as a whole month ("than September") | fixed: the calculator takes today and decides by the month; a test for 31 October |
| **I2** the 10-day threshold and month lengths were not pinned | added tests: 9 against 10 days, 30 March against February, a past January against December, overlapping entries counted once, failed reads of the month before's attacks and marks |
| Home showed progress before any attack (EC-HIS-01) | fixed in the domain: no previous period before the first attack |
| `PeriodEntries` was a domain model outside `model/` | now `PeriodEntriesDomainModel` in `model/` |
| DRY: the medication prompt kept its own zone margin; start dates and migraine dates were computed twice | the prompt uses `AttackDays`; the calculator reuses both |
| The average peak had two ways to say "none" | its own `PainLevelUiModel`, null for none |
| The orphaned KDoc in `GetTodayUseCase`; "—" was translatable; `progress`'s KDoc | fixed |
| A zone-ahead attack shows on a future cell but is not counted | accepted and documented (design D2): future dates never count |
| The chronic copy hard-codes 15 | accepted: it is the clinical definition (ICHD-3) |
