# Design

The code is read from `main` 50a6775e. `SafetyCard` is the first child of `ReadyContents`' `verticalScroll(rememberScrollState())` column. It is shown when
`FirstMaximumRecorded` sets `safetyCardVisible` (`rememberSaveable(attackId)`), and it is an assertive live region.

## D1. The scroll

- Hoist the column's `ScrollState`.
- A `LaunchedEffect` on the event, not on restored state, waits until the card is laid out and reads its bounds in the viewport.
- If the card is not fully inside, call `scroll(MutatePriority.PreventUserInput) { … }`. Inside it, drive `scrollBy` from a 300 ms ease-in-out animation
  (for example `androidx.compose.animation.core.animate`) to bring the card's top to the viewport's top, or call `scrollBy` once with reduced motion. A taller card gets its top aligned.
- If the card is already inside, do nothing.
- `PreventUserInput` holds a drag off for those 300 ms, so the scroll always ends with the card in view. That is safety over UX for 0.3 s.
- A restore does not scroll, because `rememberScrollState` already restores the position and the event does not repeat.

## D2. TalkBack once in view (the hard acceptance)

QA heard nothing when the card was off-screen: TalkBack ignores a live region change on a node that is not visible to the user. The card is announced when
it is inserted, which is before the scroll, so a plain scroll could stay silent. The announcement must happen once the card is in view.

The engineer picks one of these and proves it with QA's speech log:
- (a) Set the card's `liveRegion` only once it is fully in view, after the scroll ends, or at once when it was already in view.
- (b) Insert the card with the scroll already done, as a jump, then animate nothing. This conflicts with the 300 ms ease, so use it only if (a) fails.

Focus is never moved. `requestFocus` and `performAccessibilityAction(FOCUS)` are not used. D163 (3)'s order (card event first, then the message) does not
change. On a failed save at 100%, the message still lies over the title (#166, 1.2), so the sentence is spoken and the title is not, as in 1.1.0.

## D3. Edge cases

| Case | Result |
|---|---|
| Card already in view | No scroll, announced as today |
| Out of view, saved or failed | Scroll, then announce |
| 200% landscape, taller than the viewport | Top aligned |
| Reduced motion | Jump |
| A touch during the scroll | Held off for 300 ms (D1) |
| Rotation or process restore | No scroll |
| An AddPainDialog or picker still closing | The effect runs after layout on the main window. The dialog's own window does not change the bounds |
| The attack is gone | No card (D163 (5)), no scroll |
| Free and Premium, during an attack | The same. Safety is never Premium |
| RTL | Vertical only |

## Acceptance (QA)

| # | Build | Rows | Pass |
|---|---|---|---|
| S1 | Hotfix debug and release | QA's `t53` rows, scrolled down to Pain: a first 10 saved and failed, 100% and 200%, animations on and off | The card's top at the content's top within 1 s, 16 of 16 |
| S2 | Hotfix | The same with TalkBack, saved | Title and sentence spoken in full (`action=SPEAK` log) |
| S3 | Hotfix | TalkBack at 200%, failed | Title, sentence and the message spoken |
| S4 | **Negative control:** 1.1.0 or the hotfix without D1 and D2 | S1 and S2 | The card is not visible, and the title is not spoken, as in #171 |
| S5 | Hotfix | No scroll (card in view) | Nothing moves, title spoken (control) |
| S6 | Hotfix | Landscape 100% and 200% | Top aligned |
| S7 | Hotfix | Rotate with the card showing, scrolled away | No jump back |

## What this does not prove

A human hand, a real phone, and other TalkBack versions. The title on a failed save at 100% is still covered (#166).
