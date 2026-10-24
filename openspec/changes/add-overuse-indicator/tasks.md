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
- [x] 3.2 Emulator walk with seeded intakes: below, at and over each limit, two categories, a past month, none.
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

## Verification record

**Gate:** BUILD SUCCESSFUL. All unit tests pass.

**Emulator walk** on emulator-5570, seeded through the app and SQL. At the end the database dump was byte-identical
to the baseline. All 7 scenarios passed:
- no section without an acute intake in 30 days;
- "Simple painkillers: 6 of 15 days" (neutral), with two intakes on one day counted once and day 30 left out. In other
  zones, 01:00 in Tokyo and 23:00 in New York count on their own dates;
- triptan on 10 days: the amber rows, the union "All attack medication: 14 of 10 days", then the doctor line;
- "Not sure" counts as combination or other;
- preventive intakes never count;
- no section in a past month;
- one node per row; nothing clipped at 200% font, in portrait or landscape;
- home's warning and "Got it" behave as before, and the History section stays after "Got it".

**Re-walk after the review fixes** (3d0cb63):
- "Triptans: 10 of 10 days, at the limit" and "All attack medication: 14 of 10 days, over the limit" in amber;
- an intake added while the app was in the background showed when History resumed, still on October (4 → 5 of 15).
