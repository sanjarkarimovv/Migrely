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

- [ ] 4.1 Presentation models and mappers; ViewModels unchanged apart from mapping.
- [ ] 4.2 History's `MonthSummary` and home's progress line; strings with plurals.
  Verify: `./gradlew :features:history:presentation:test :features:history:ui:test`

## 5. Verify

- [ ] 5.1 Gate.
- [ ] 5.2 Emulator walk on emulator-5570 with seeded data: a past month, the current month, the chronic card, the
  progress message (better, worse, too few entries), screen readers, 200% font, landscape.
- [ ] 5.3 Independent review; fix and record.
