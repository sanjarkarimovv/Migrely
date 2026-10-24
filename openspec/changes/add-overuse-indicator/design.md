# Design

## Context

- `GetMedicationPromptUseCase` (change 3a) reads the last 30 days of acute intakes, groups their dates by overuse
  category, and turns the counts into a soft or doctor warning, quieted by "Got it".
- Home shows the medication feature's card through a slot that `:app` fills (D46). History's month summary
  (change 5) has no such slot yet.

## Decisions

### D1. One counting rule

- An internal `OveruseDayCounter` in the medication domain holds the 30-day window, through `AttackDays`'s zone
  margin, and the day counts:
  - one per single category with an intake;
  - plus `Multiple`, the union of dates, when two or more categories were used.
- `GetMedicationPromptUseCase` uses it, and its levels and quieting stay where they are. Its tests stay unchanged.

### D2. The indicator's data

- `GetOveruseStatusUseCase` returns `OveruseStatusResultDomainModel`, which is either `Loaded(rows)` or `Failed`.
- Each row is an `OveruseStatusRowDomainModel(category, days, limit)`, in the order simple painkillers, triptans,
  combination or other, then all.
- Categories without an intake are left out. `Failed` hides the section, like the warning.

### D3. Presentation and UI

- `OveruseIndicatorViewModel.onEnter()` loads the status and emits `OveruseIndicatorViewState`: `Hidden` (no attack
  medication, or `Failed`) or `Shown(rows)`. It is a sealed interface, as the architecture tests require of view
  states. Each row carries `reached = days >= limit`.
- `OveruseCategoryPresentationMapper` and `OveruseCategoryUiMapper` now hold the category mapping that home's card
  kept privately, so the card and the indicator share it.
- `OveruseIndicatorDependencies.OveruseIndicator()` draws a small section with a "Last 30 days" label, one row per
  category, and the doctor line when any row is reached.
  - Reached rows use `tertiary` (amber, reserved for overuse); the others use `onSurfaceVariant`.
  - Each row is one node for screen readers, for example "Triptans: 10 of 10 days".
- **History** gains `overuseIndicator: @Composable (refreshKey: Int) -> Unit` on `History(...)`. The key counts
  History's view states, as home's `homeLoads` does for its slots, so the indicator reloads with every History load,
  an undo or a resume included (review). It reaches `MonthSummary(overuseIndicator)`, which places it after "Days with
  attack medication" for the current month only. `:app` fills it on History's route.
- **Not by colour alone:** reached rows add "at the limit" or "over the limit" (WCAG 1.4.1).
- `reached` is computed in the domain row, from the doctor level of home's warning. `OveruseDayCounter` owns the read
  as well as the count, so the two use cases share both.
- **Accepted:**
  - before any attack, History shows its first-use card and no summary, so no indicator. Home's warning still covers
    that user;
  - after a rotation, the section can appear a moment after the summary: it reads the local database, and the view
    model is new.

## Risks

| Risk | Mitigation |
|---|---|
| The extraction changes the warning | the prompt's tests stay unchanged and must pass |
| The section repeats the home warning | it is a quiet row, while the warning is a card; this is the standing view EC-MED-06 asks for |
