# Tasks

## 1. Test first

- [x] 1.1 `InsightsShownStateTest`:
  - an answer for the new months that comes right after the tap is shown;
  - a late answer for the earlier months is not;
  - with two quick taps, only the last one's answer shows;
  - loading shows first;
  - the saver restores the months and reads the insights again.
  It does not compile before `InsightsShownState` exists. It checks the holder, not the screen's wiring: the walk does
  that, and QA's Insights scenario gets a chip-switch loop (review).

## 2. Fix

- [x] 2.1 `InsightsShownState`, with a `Saver` for the months
- [x] 2.2 `InsightsScreen` uses it in place of `rememberUpdatedState`

## 3. Verify

- [x] 3.1 Gate: `assembleDebug test ktlintCheck detekt lint`
- [x] 3.2 `openspec validate fix-insights-stale-period --strict`
- [ ] 3.3 On emulator-5570, with migraines across 12 months:
  - tapping 6 → 3 → 12 → 6 months shows each period's numbers, ten taps in a row;
  - after a rotation the chosen months and their numbers stay.

## 4. Review

- [ ] 4.1 Independent review (`superpowers:code-reviewer`)
