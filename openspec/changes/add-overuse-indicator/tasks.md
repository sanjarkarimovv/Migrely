# Tasks

## 1. Domain

- [x] 1.1 `OveruseDayCounter` used by `GetMedicationPromptUseCase`; `GetOveruseStatusUseCase` and its models with tests.
  Verify: `./gradlew :features:medication:domain:test`

## 2. Presentation and UI

- [x] 2.1 `OveruseIndicatorViewModel`, view state and mapper with tests.
- [x] 2.2 `OveruseIndicator` composable and `Dependencies`; strings; History's slot; DI and the route in `:app`.
  Verify: `./gradlew :features:medication:presentation:test :app:assembleDebug`

## 3. Verify

- [x] 3.1 Gate (893 unit tests).
- [ ] 3.2 Emulator walk with seeded intakes: below, at and over each limit, two categories, a past month, none.
- [ ] 3.3 Independent review; fix and record.
