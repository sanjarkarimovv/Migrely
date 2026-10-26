# Tasks

## 1. Fix

- [x] 1.1 Unit tests in `HomeViewModelTest`: tracking turned off or on in Settings shows on return to home; removing
  today's mark keeps the toggle; the next return shows only what is saved. The kept-toggle test fails before the fix
- [x] 1.2 `HomeViewModel` keeps the toggle on the load after the user's own change (`keepPeriodToggle`)
- [x] 1.3 `TodayContents` draws the toggle from `periodOffered` only, without its one-way `rememberSaveable` latch

## 2. Verify

- [x] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [x] 2.2 `openspec validate fix-track-periods-off-live --strict`
- [x] 2.3 On emulator-5570: "Track periods" off, back to home, the toggle is gone at once; on, back, the toggle shows;
  with tracking off and today marked, removing the mark keeps the toggle until home is left

## Verification record

- **Root cause:** `TodayContents` kept `periodShown` in `rememberSaveable` and only ever set it to `true`. Home stays on
  the back stack while Settings is open, so the latch outlived the trip; `GetTodayUseCase` had already answered
  `periodOffered = false` on the resume.
- **Tests:** `HomeViewModelTest` has four new tests. "Removing today's mark keeps the toggle" failed before the fix
  (`AssertionError`, 45 tests, 1 failed) and passes after. Turning tracking off or on and returning to home, and the
  return after a kept toggle, pin both directions where the decision now lives. Gate: BUILD SUCCESSFUL, 2152 unit test
  runs, none failed.
- **Before, on emulator-5570** with the installed 1.0.0 (2) on the walk baseline: "Track periods" off, back, and home
  still had "Period today", also 6 s later, with `period_tracking=false` saved.
- **After, on emulator-5570** with this branch's debug APK on the same baseline:

  | Step | Result |
  |---|---|
  | Tracking on | home shows "Period today" |
  | Settings, tracking off, back | no "Period today" at once; the month legend keeps its Period key, as accepted in add-onboarding |
  | Settings, tracking on, back | "Period today" at once |
  | Today marked, tracking off, back | the toggle shows, checked, so the mark can be removed |
  | Tap it | the mark is deleted; the toggle stays, unchecked |
  | Settings, back | the toggle is gone |

- Evidence: `migrely-work/walks/fix66/` (node dumps and two screenshots). The baseline was restored afterwards (dump
  md5 `c3cfd32487171b7886baf9725dce7594`).
- **Not checked:** rotation right after removing a mark. The resume that follows a rotation loads home again, so the
  kept toggle is expected to go then; before, it survived a rotation (a decision in the proposal).

## Review

An independent review found nothing critical or important. Minor findings, all fixed:
- the spec and the KDoc said the kept toggle stays "until home is next opened or returned to", while any other load
  ends it (midnight, an undo, a rotation): both now say so;
- the rotation trade-off is now a decision in the proposal;
- `HomeViewStateUiMapperTest` has a case with the toggle not offered;
- History's switch keeps its one-way latch, now with a comment naming why it is safe: Settings opens only from home.

Accepted: the tests guard the ViewModel, where the decision now lives; feature UI has no Compose tests (ARCHITECTURE
§11), so a latch put back into the composable would not be caught by a unit test. History's switch is unchanged: History cannot open
  Settings, so its own "while the day is chosen" latch cannot outlive a change of the setting.
