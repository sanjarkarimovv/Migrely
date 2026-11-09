# Design

The facts here come from reading `develop` c2761cba and the sources of Compose UI 1.9.3 and Material 3 1.4.0 (BOM 2025.10.00). The measurements come
from #161. Nothing here has been run on a device yet.

## Context

- **Popups in the app.** `git grep -nP '\b(DropdownMenu|ExposedDropdownMenu\w*|Popup|TooltipBox|ModalBottomSheet)\b' -- '*.kt'` finds exactly one,
  `ChoiceChip` in `features/attack/ui/.../AttackChoiceSections.kt`. Its `DropdownMenu` has one item, Remove ("Remove from my list"). It opens with
  `onLongClick`. `menuOpen` is a plain `remember`. Remove sets `menuOpen = false` and calls `onArchive()`, which runs
  `setCustomTagArchived(id, archived = true)` and then reloads the details. A second call does the same again and shows no message.
- **What `DropdownMenu` is.** Material's Android `DropdownMenu` puts a `Popup` with `PopupProperties(focusable = true)` around `DropdownMenuContent`.
  The caller's `modifier` goes first on the `Column` inside the `Surface`, inside the popup's own window, so a modifier there sees every press on the
  menu's card, its padding included. The popup stays composed while its exit transition runs (`expandedState.currentState`), so for a moment after Remove
  it is still in the window and can take a tap.
- **Outside taps.** `PopupLayout.onTouchEvent` dismisses on an `ACTION_DOWN` outside its bounds, or on `ACTION_OUTSIDE`, and returns true. A focusable
  popup is touch modal, so the tap does not reach the chip beneath. It never reaches the Compose content either, so a node on the card never sees it.
- **The rule that exists.** `DialogLiftRule` (`designsystem/.../dialog/DialogLiftRule.kt`) keeps the last lift from a window with a non-null key. The main
  window (`TouchesOnlyWhile.kt`, key `null`) already asks it about every press, and reports the lifts of the presses it ignored. `DialogWaitNode` in
  `MigrelyDialogs.kt` does two jobs in one node: D160's wait, and D161's lift (press, then `lift` on an up event that was let through, in the initial
  pass, with `localToScreen`). `ScreenNavHost` provides one instance of the rule.

## Decisions

### D1. The menu joins D161's rule, with no new rule (BRD D164)

The path is #159's: a tap closes a window, and the next one lands in the window beneath. In the handbook's risk order: safety and privacy are not
touched. On data, a false tag written with no message outranks one lost tap. On store policy and accessibility, nothing shows and semantics do not
change. So the menu gets the same answer, with the same numbers: 500 ms for the first press, the burst timeout for the next, and 100 dp in screen
coordinates. One rule means one place to reason about, and QA's D161 tables carry over.

| Option | Why not |
|---|---|
| Keep the menu open briefly after Remove, so it takes the second tap | It looks slow, and a control that does not seem to answer is what Google rejected 1.0.1 for. It also needs a timer |
| A debounce on each chip | It misses routes, as D159's D1 says, and every new control has to remember it |
| D160's wait on the menu | It stops a tap landing *inside* a new window, and this tap lands *beneath* one. See D2 |
| A confirmation or Undo for Remove | More taps for a person in pain, and the chip beneath still toggles |
| A new, shorter limit for menus | A second number invites more of them. #161 wrote at 457 ms, so a limit under 500 ms would not cover the measured gaps |

### D2. No wait on the menu

D160's wait exists because the tap that opens a dialog can be followed by a second tap that lands inside it. Nothing like that happens with a menu
opened by a long press. The finger is still down when the menu appears, so the person sees it before any next tap. A wait would only make Remove ignore
a deliberate tap. So the helper reports lifts and asks the rule about its own presses, the same as any window, but it has no timer of its own.

### D3. The helper, and the class of popups

In `designsystem`, package `dialog`:

| Part | What |
|---|---|
| `MigrelyDropdownMenu(expanded, onDismissRequest, content: ColumnScope.() -> Unit)` | Material's `DropdownMenu` with the parameters this app uses, and a lift modifier on the card. New file, for example `MigrelyMenus.kt` |
| The lift part of the node | The lift half of `DialogWaitNode`: on a press, ask `DialogLiftRule.press(windowKey, …, windowAppearedAtMillis = attach time)`; on an up event that was not ignored, call `lift`; consume what was ignored, in `PointerEventPass.Initial`, with no semantics. One implementation, shared by the dialog node and the menu node (DRY). How the code is split is the engineer's choice, as long as every existing dialog test passes unchanged |

**Why a press on the menu is asked too.** A menu appears after any lift that came before it, so `windowAppearedAtMillis` keeps it out of reach, and
today the rule never ignores a press on the menu. Asking it costs one call and keeps the node the same as a dialog's, so a menu opened over a dialog
later behaves as a dialog would.

**The check.** A second test in `app/src/test/java/com/migrely/app/architecture`, next to `ArchitectureDialogsTest`, the same way:
`Konsist.scopeFromProduction()`, the file's text, and a `\b` after each name. Outside the helper's file, no file names:

```
androidx.compose.material3.DropdownMenu
androidx.compose.material3.ExposedDropdownMenuBox
androidx.compose.material3.TooltipBox
androidx.compose.ui.window.Popup
android.widget.PopupMenu
android.widget.PopupWindow
android.widget.ListPopupWindow
```

The `\b` lets `DropdownMenuItem`, `PopupProperties` and `PopupPositionProvider` through, because the word goes on after the name. The test's KDoc says
what to do when it fails: add a helper in `designsystem` that uses the lift part (D164). Tooltips, exposed menus and View popups get no helper now,
because nothing uses them yet.

### D4. When the lift is recorded

Remove's `onClick` runs from the up event in the Main pass, or later. The node reads the same up event in the Initial pass, so the lift is in the rule
before Remove runs, before `menuOpen` turns false, and before the archived tag leaves the list. After that, nothing about the screen beneath matters:
a recomposition, a chip that slides into the freed place, a scroll, the popup's window going away, or the chip's whole `Box` leaving composition. The
next press is judged by the lift's place on the screen and its event time.

### D5. Edge cases

| # | Case | Decision | Why |
|---|---|---|---|
| 1 | First press beneath vs a burst | **The first press is judged by 500 ms from the lift on Remove. A burst's next press is judged by the burst timeout from the last ignored lift, and the place moves with it** | Exactly D161 (7). Re-tapping at an ordinary pace must answer. Accepted cost: a third tap from 300 ms after the second's lift acts |
| 2 | Menu dismissed by a tap outside | **Not covered, residual, measured** | The popup takes the tap at the View level, so the card never sees it, and `onDismissRequest` does not say outside or Back. Covering it means taking Back over, which D160 and D161 refused. The outside tap itself does not reach beneath |
| 3 | Back with the menu open | Closes it, and the rule does not act | No finger lifted on the card |
| 4 | Menu closing while the screen recomposes or scrolls | Covered | D4 |
| 5 | Second tap on the menu while it fades out | Remove can run again. Accepted, and a test pins it | The same window, so the rule does not ignore it. Archiving is `archived = true`, so a second call only reloads, with no message. The effect equals one Remove |
| 6 | TalkBack | **No timing cost** | TalkBack removes through the chip's own "Remove from my list" custom action, with no menu. If TalkBack opens the menu (its long-click action) and activates Remove, that is a semantics click with no pointer, so it is no lift. D161 already exempts screen readers and keyboards ("Screen readers and keyboards" scenario), so nothing new is needed |
| 7 | Switch access | Click action: as TalkBack. Point scan: real touches, treated as any touch | A point scan tap within 500 ms and 100 dp of the last one is not plausible. Not run on a device |
| 8 | Keyboard | Activates Remove with no lift | Key events, not pointer events |
| 9 | Rotation with the menu open | **The menu closes, as today, nothing is written, and the lift is not kept** | `menuOpen` is `remember` and the activity is recreated (no `configChanges`). `ScreenNavHost`'s rule is new. Making the menu survive is out of scope |
| 10 | Process death with the menu open | As rotation | Nothing is saved |
| 11 | Landscape at 200% font | Covered by construction | The rule asks about the place on the screen, wherever the menu opens, above or below the tag. QA runs 100% and 200%, portrait and landscape |
| 12 | Under the finger: a chip | Ignored under the limit | #161's row |
| 13 | Under the finger: a switch, or a button that writes or navigates | Ignored under the limit | The details screen has no switch today, and the rule is per window, so any future control is covered |
| 14 | Under the finger: nothing | The press does nothing anyway. A drag or fling that starts there is dead until it lifts | D161 (9): the cost of a whole-gesture rule |
| 15 | Remove right after the long press | Answers on the first tap | D2 |
| 16 | "Remove animations" | Same rule | The rule counts from the lift, not from an animation. With no exit animation, row 5 cannot happen |
| 17 | Slow frame, clock or zone change, DST | Same rule | Event uptime |
| 18 | A tap beyond 100 dp, or at or after 500 ms | Answers | D161 |
| 19 | Empty, first use, Free and Premium, offline, full storage | Not touched | With no custom tag there is no menu. A failed archive shows today's message, and the rule does not depend on the result |
| 20 | During an attack | The details of an active migraine have the same chips. Nothing in the menu is a safety control. Back is free | |
| 21 | Right-to-left | No direction in the rule | English only (D148) |
| 22 | Upgrade, backup and restore | Nothing is stored | |

### D6. What a person loses

The same as D161 (9), now under a menu item as well. A tap under 500 ms and within 100 dp of Remove does nothing, with no sign. A drag that starts there
is dead until the finger lifts. Tapping along a row of chips under 300 ms per tap stays dead until a 300 ms pause. None of the lost gestures writes.

## Test plan

| Level | What | Who |
|---|---|---|
| JVM | Nothing new in `DialogLiftRule`. Its tests stay green unchanged | `android-engineer` |
| Compose, Robolectric if popup windows work there (the dialog tests in `designsystem/src/testDebug` did), otherwise `androidTest`; say which | Through `MigrelyDropdownMenu` with an item over a clickable in the main window: a press beneath under 500 ms and within the distance does nothing; at 500 ms it answers; beyond the distance it answers; a burst's next under the burst timeout does nothing and one after it answers; the item answers its first tap at once (no wait); a semantics click on the item leaves the next press beneath answering; the menu's tree and bounds match a raw `DropdownMenu`; a second tap on the closing menu runs the item again and nothing else | `android-engineer` |
| Real windows | As `DialogLiftOnScreenTest`: a press just inside and just outside the distance, through the popup's real window offset, on `emulator-5570`. Not in the gate, so run it and say so | `android-engineer` |
| Konsist | The popup check passes. A raw `DropdownMenu` added locally fails it and names the file. Restore it, never commit it | `android-engineer` |
| Negative control | The menu's lift removed locally: the Compose tests above must fail. Restore it | `android-engineer` |
| Black box, debug and R8 | The acceptance table below | `qa-engineer` |
| Human hand | A real phone on the internal track | `team-lead`, the owner |

### Acceptance (QA)

Setup: details of a migraine with a custom symptom, scrolled so that Remove, once the menu is open, lies over another chip ("Sensitivity to smell" as
in #161). Gaps are measured from the first lift to the second press with `getevent`. Read each trial by its measured gap, not by the gap you asked for.

| # | Build | Steps | Pass |
|---|---|---|---|
| A1 | Under test, debug and R8 | Double tap Remove at about 150, 300 and 450 ms, 3 trials each | Each trial: the tag is archived, no `attack_tag` is added, and no message shows. 9 of 9 |
| A2 | **Negative control:** the same commit with only the menu's lift removed (the helper calls raw `DropdownMenu` with no lift modifier), debug | A1's steps | The chip is added again at each gap. If it is not, A1 proves nothing, and the result goes back to `team-lead` |
| A3 | Under test | After Remove, tap the chip at the same point at about 600, 800 and 1000 ms after the lift on Remove | The chip answers on that first tap, 3 of 3 at each gap |
| A4 | Under test | A single tap on Remove | The tag is archived and nothing else changes |
| A5 | Under test | A1 at about 150 ms in 200% portrait, 100% landscape and 200% landscape. Record what lies under Remove: chip, switch or nothing | No write in any layout. Record which layouts had nothing beneath |
| A6 | Under test | A third tap at under 300 ms, and at about 350 ms, after the second's lift | Under 300 ms: nothing. About 350 ms: record it. That one may write, which is the accepted cost |
| A7 | Under test | Open the menu, then double tap outside it, over a chip, at about 150 and 300 ms | Record what the second tap does. It is not covered and not called safe. A write goes back to `team-lead` |
| A8 | Under test | Rotate with the menu open, then tap a chip | The menu is closed, nothing is written, and the tap answers |
| A9 | Under test | TalkBack: the chip's "Remove from my list" action, then activate another chip at once. Then the same through the menu | The tag is removed, and the next activation answers at once |
| A10 | Under test | Double tap Remove at under 80 ms, which lands on the fading menu | The tag is archived once in effect, nothing else is written, and no message shows |

## What the check will not prove

- **A human hand.** One injected finger at exactly the same point, on an emulator. A real double tap drifts and has its own rhythm.
- **How often this happens.** Nobody has measured how often people double tap a menu item.
- **A real phone,** or API 26 for the popup's window offset.
- **Popups not used today.** The check forces a helper. It does not prove that the helper is right.

## Risks

- **Screen coordinates across windows** fail silently toward writing if they are wrong. The popup's window has its own origin. The real-window test and
  A1 check it.
- **Material moves the `modifier`.** If a Material upgrade stops putting `modifier` on the card's column, the lift stops being seen. The Compose tests fail
  if a press beneath stops being ignored.
- **The menu's open animation scales the card.** `localToScreen` includes layer transforms, and the lift is on a finished menu in every measured row. Not
  proved for a tap during the first frames.
- **Sharing the node** with dialogs must not change a dialog. Every dialog test passes unchanged, or the split is wrong.

## Migration

None. No data and no setting. To back out, make the helper call `DropdownMenu` with no modifier.
