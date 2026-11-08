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

**Order:** the message first, then the card. The card is the instruction the person must keep, so it is spoken last and in full. Both stay
on screen: "storage full" has no timeout and the card stays until dismissed, so TalkBack can reach either again.

**Code:** first keep both as they are, notify in that order, and measure on a device (task 3.2). If the card cuts off the message, the one
change allowed is that the card's live region is polite in this failed case only, so TalkBack queues it after the message. The card on a
saved 10 keeps its assertive region. No other announcement code.

## Edge cases

| Case | Result |
|---|---|
| Storage full, generic failure, time refused | Card, and the outcome's message |
| Reading the saved 10s fails | Card |
| Attack gone | Not-found state, no card |
| A failed first 10, then a saved one | Card twice. Accepted |
| Later 10 once one is saved | No card, as today |
| Rotation, process death | The card's flag is `rememberSaveable` today; the message is saved as today |
| TalkBack | D2 |

## What the check will not prove

An emulator's full disk and TalkBack. Not a real phone or a person.
