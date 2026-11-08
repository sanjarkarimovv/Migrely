# Design

## D1. Where the decision moves (BRD D163)

`AddIntensityPointUseCase` today works out `firstMaximum` only after the time checks, and returns it only inside `Added`. The fix:

- It works it out for a 10 before any outcome except `NotFound`: true when `maximumRecorded()` is `NotRecorded` or `Failed`. Today
  `Failed` counts as false, so an unreadable history hides the card. Safety first: unknown shows the card.
- Every outcome but `NotFound` carries it: `Added`, `StorageFull`, `Failed`, `OutsideAttack` and `InFuture`. Today only `Added`
  does. A lookup of the attack that fails returns `Failed`, and carries it too.
- `AttackDetailsViewModel.onAddPointAction` notifies `FirstMaximumRecorded` whenever it is true, and then the outcome's own
  notification as today. `NotFound` shows the not-found state, with no card.

Nothing is saved for a failed 10, so the next first 10 that saves shows the card again. That is the decision, not a defect.

## D2. TalkBack

Today both announce themselves through a live region, both assertive: the card (`AttackPainSection.kt`, `LiveRegionMode.Assertive`) and an
error message (`MigrelySnackbar.kt`, assertive for `SnackbarTone.Error`, which "storage full" and the generic failure use). An assertive
announcement can cut off the one before it, so two at once can lose one.

**Order in the code:** the card's notification first, then the outcome's message (`AttackDetailsViewModel.kt:191-193`, task 2.2, four
tests that assert it).

**Order heard, observed and not set by the code:** on one headless emulator run (task 3.2), TalkBack spoke the message first, then the
card's body sentence, each in full. The message's event came 36 ms before the card's, and the card's speech was queued behind it, not
cut. Another device or a slower frame could change that order. No code makes it so.

**What stays on screen:** "storage full" has no timeout and the card stays until dismissed. At 100% font the message covers the card's
title, "Sudden, worst-ever pain?", which is then out of the accessibility tree until the message is dismissed: the walk records `title
absent`, and TalkBack's log has no line with "Sudden". The body sentence, which is the warning itself, is visible and spoken in full. At
200% the title is not covered (`team-lead`'s review; not in the engineer's walk files). **Accepted for 1.1.0 by `team-lead`**, because
the warning sentence is seen and heard. A later issue moves the message off the card. QA files it after measuring.

**Code:** both live regions stay as they were, assertive. The polite fallback was not needed.

## Edge cases

| Case | Result |
|---|---|
| Storage full, generic failure, time refused | Card, and the outcome's message |
| Reading the saved 10s fails | Card |
| Attack gone | Not-found state, no card |
| A failed first 10, then a saved one | Card twice. Accepted |
| Later 10 once one is saved | No card, as today |
| Rotation, process death | The card's flag is `rememberSaveable` today; the message is saved as today |
| TalkBack | D2: the message and the card's body sentence are spoken; at 100% the title is covered and not spoken, accepted for 1.1.0 |

## What the check will not prove

An emulator's full disk and TalkBack. Not a real phone or a person.
