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
- [x] 3.3 Independent review; fix and record.

## Review (3.3)

An independent `superpowers:code-reviewer` pass found nothing critical. It confirmed:
- the extraction behaves exactly as before: filtering before grouping, `Multiple` only above the top single level,
  ties, "Not sure", preventive and being-deleted intakes left out;
- the window, History's isolation from medications, the Konsist names, the amber contrast (about 8.6:1), and that
  the copy does not blame.

| Finding | Outcome |
|---|---|
| **I1** the indicator did not reload with History (an undo, a resume) | fixed: `overuseIndicator(refreshKey)` with History's load count, as with home's slots; an "After an undo" scenario |
| **I2** ARCHITECTURE did not describe History's slot | §3 and §13 updated |
| `reached` was a business rule in presentation | computed in the domain row |
| DRY: `SINGLE_CATEGORIES` and the read were in both use cases | `OveruseDayCounter` owns both |
| No counter tests at the window edges or in other zones | `OveruseDayCounterTest`: the first and last days, the day before, Berlin and Tokyo near the edges, failure |
| Reached rows were told apart by colour only | they add "at the limit" or "over the limit" (WCAG 1.4.1) |
| Naming and KDoc (`currentMonthExtra`, History's KDoc, the repository's KDoc, D45's "change 5") | fixed |
| No indicator before any attack | accepted: History's first-use card stands in, and home's warning still covers that user |
| The section can appear a moment late after a rotation | accepted: a local read and a new view model |
