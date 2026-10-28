# Proposal

## Why

QA #104 (S2): after a period chip is tapped on Insights, the chip changes but the sections often keep the previous
period's numbers. QA saw it in 5 of 10 taps from 6 to 3 months, and nothing reloads them.
- **Cause:**
  - The screen kept the chosen months in a saved state and read a copy of it through `rememberUpdatedState`.
  - The results collector weighed each answer against that copy, through `InsightsShownStateResolver`.
  - The copy changes only when the next composition is applied. An answer for the new months that came before that
    frame was taken for an earlier choice's late answer and dropped, and the chosen months were never asked again.
- **The spec it breaks:** `trigger-insights` › The period: Insights looks at the chosen last 3, 6 or 12 months.

## What Changes

- **Insights UI:** a small state holder, `InsightsShownState`, keeps the chosen months and the insights shown together.
  - A tap changes the months at once.
  - Every result is weighed against the months in the holder, so the order of a tap, its answer and a redraw no
    longer matters. Both the tap and the results collector run on the main thread, so they never interleave.
  - The months are kept across a rotation and process death, as before, and the insights are read again after it.
- `InsightsShownStateResolver` is unchanged: a late answer for an earlier choice is still dropped.

**Unchanged:**
- what is read and when (on entering, on every return and on each new choice);
- the numbers themselves.

## Capabilities

None changed. The fix makes the code meet the existing `trigger-insights` requirement.

## Impact

- `:features:insights:ui`: new `view/InsightsShownState.kt`, `InsightsScreen.kt`, and a new `InsightsShownStateTest`.
