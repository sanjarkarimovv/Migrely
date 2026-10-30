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
- [x] 3.3 On emulator-5570, with migraines across 12 months:
  - tapping 6 → 3 → 12 → 6 months shows each period's numbers, ten taps in a row;
  - after a rotation the chosen months and their numbers stay.

## 4. Review

- [x] 4.1 Independent review (`superpowers:code-reviewer`)

## Verification record

- **Gate:** `assembleDebug test ktlintCheck detekt lint` passes.
- **Unit test:** `InsightsShownStateTest` is 5/5.
- **Emulator walk:** emulator-5570, API 36, debug build of 8d37a1ec, with QA's `insights-patterns` seed imported for 2026-10-28, Asia/Tashkent, and Monthly bought in the simulated store.
  - **The three periods:**
    - 6 months: Stress 6 of 10 (60%), period days 8%;
    - 3 months: Stress 5 of 8 (63%), "No single day stands out";
    - 12 months: period days 4%.
  - **Taps:** every check showed the chosen period's numbers.
    - slow taps (6 → 3 → 6 → 12 → 3 → 6, read 2.5 s after each tap): 0 stale of 20;
    - quick taps about 300 ms apart: 0 stale of 10 sequences;
    - very fast taps about 100 ms apart: 0 stale of 4.
  - **Rotation:** the chosen months and their numbers stay, also with a tap less than 200 ms before the rotation.
  - **Background:** HOME and back keeps the period and its numbers.
  - **Crashes:** 0.
- **Review:** nothing blocking. Fixed in b040ccc8: the quick-tap and saver tests, and the main-thread note.
  - Suggested to QA: a chip-switch loop in its Insights scenario, as a regression test of the screen's wiring.
- **Not compared:** the pre-fix build was not run on the same emulator (QA saw 5 of 10 stale on 1.0.1).
