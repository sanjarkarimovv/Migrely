# Tasks

## 1. Fix

- [x] 1.1 Unit tests in `HomeViewModelTest`: tracking turned off or on in Settings shows on return to home; removing
  today's mark keeps the toggle; the next return shows only what is saved. The kept-toggle test fails before the fix
- [x] 1.2 `HomeViewModel` keeps the toggle on the load after the user's own change (`keepPeriodToggle`)
- [x] 1.3 `TodayContents` draws the toggle from `periodOffered` only, without its one-way `rememberSaveable` latch

## 2. Verify

- [x] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [x] 2.2 `openspec validate fix-track-periods-off-live --strict`
- [ ] 2.3 On emulator-5570: "Track periods" off, back to home, the toggle is gone at once; on, back, the toggle shows;
  with tracking off and today marked, removing the mark keeps the toggle until home is left
