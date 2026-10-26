# Proposal

## Why

QA #66: with build 1.0.0 (3), turning "Track periods" off in Settings left home's "Period today" toggle in place after
going back to home, after waiting and after a visit to History. It went away only when the app restarted. Turning it on
showed the toggle at once, and the switch was saved (`period_tracking=false`).

Home reads the setting on every load: entering, every resume and after every action (design D11 of add-attack-logging).
On the way back from Settings, `GetTodayUseCase` answered `periodOffered = false`. The toggle stayed because of
`TodayContents`: since add-onboarding, a "kept once shown" rule held `periodShown` in `rememberSaveable` and only ever
set it to `true` (`if (today.periodOffered) periodShown = true`). The rule was meant to keep the toggle after the user
removes today's mark with tracking off, so a mark removed by mistake can be put back. But home stays on the back stack
while Settings is open, so the saved `true` outlived the trip to Settings and every load after it, until the process
was restarted. Turning tracking on worked because the latch only listens for `true`.

## What Changes

- The view state alone decides whether the toggle shows. `TodayContents` draws it when `periodOffered` is true, with no
  state of its own.
- The "kept once shown" rule moves to `HomeViewModel`, which stays stateless: the load that follows the user's own
  change on home keeps the toggle (`loadToday(keepPeriodToggle = true)`). Every other load offers it only as saved: on
  entering, on returning to home, after an undo, at midnight, and after a rotation or the screen turning off, since
  each resumes home.
  - Decision: the kept toggle no longer survives a rotation, which the latch did. Keeping it through a rotation would
    need state the ViewModel may not hold (skill rule 4). A mark removed by mistake and rotated away can still be put
    back by turning "Track periods" on.
- Unit tests in `HomeViewModelTest` for both directions and for the kept toggle.
- The day-markers spec says when the toggle stays and when it goes.

Out of scope:
- History's switch keeps its own "while the day is chosen" rule: History cannot open Settings, so the setting cannot
  change while a day is chosen.
- No BRD change: D79 already says "No" hides the ways of adding a period mark.

## Capabilities

### Modified Capabilities

- `day-markers`: home's "Period" toggle follows "Track periods" both ways as soon as the user returns to home; it stays
  after the user's own change only until home is next loaded on entering or returning.

## Impact

- `:features:home:presentation`: `HomeViewModel` and its test.
- `:features:home:ui`: `TodayContents` loses its latch.
- No change to the domain, data, datasource or `:app`.
