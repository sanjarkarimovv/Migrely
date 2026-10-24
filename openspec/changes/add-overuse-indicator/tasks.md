# Tasks

## 1. Domain

- [ ] 1.1 `OveruseDayCounter` used by `GetMedicationPromptUseCase`; `GetOveruseStatusUseCase` and its models with tests.
  Verify: `./gradlew :features:medication:domain:test`

## 2. Presentation and UI

- [ ] 2.1 `OveruseIndicatorViewModel`, view state and mapper with tests.
- [ ] 2.2 `OveruseIndicator` composable and `Dependencies`; strings; History's slot; DI and the route in `:app`.
  Verify: `./gradlew :features:medication:presentation:test :app:assembleDebug`

## 3. Verify

- [ ] 3.1 Gate.
- [ ] 3.2 Emulator walk with seeded intakes: below, at and over each limit, two categories, a past month, none.
- [ ] 3.3 Independent review; fix and record.
