# Design

## Context

- `MonthSkyCalendar` (design system) draws each day as a `clickable` cell. Compose gives every cell its own Tab stop,
  and moves focus between cells with the arrow keys (2D focus search).
- `HomeContents` draws the messages over both modes. In landscape attack mode they keep to the start half (D37), over
  the guidance column of `SideBySideAttackLayout`, which starts 16 dp below the safe area.

## Decisions

### D1. One Tab stop for the month

- The grid is a `focusGroup()`, and each selectable day has a `FocusRequester`.
- **Leaving:** on Tab, or Shift+Tab, a key handler on the grid (`onPreviewKeyEvent`) focuses the last, or first,
  selectable day and moves focus Next, or Previous. That one move leaves the grid. The key event is consumed.
- **Entering:** the group's `focusProperties { onEnter }` redirects a Next or Previous entry to the anchor day.
- **The anchor** comes from the days themselves: the chosen day, otherwise today, otherwise the first selectable day.
  So `MonthSkyCalendar`'s callers, History and home's month card, do not change.
- **A visible focus ring (WCAG 2.4.7):** the focused day gets a two-tone ring, `onSurface` outside and `surface` inside,
  or `primary` inside on the chosen day. Compose's own focus layer is too faint on the skies.
- **Arrow keys:** the same handler moves focus a day (left, right) or a week (up, down), mirrored in RTL. A move off the
  selectable days does nothing, so focus stays in the month, as in a date picker. Compose's 2D search is not used: next
  to the last selectable day it jumped to another week (review).
- The focused day is kept in a plain holder (`FocusedDay`), read when a key is pressed, so quick presses never act on
  a day from an older composition.
- Cells stay `clickable` and focusable, so accessibility services still reach every day (EC-UX-19). TalkBack and
  Switch Access do not use Tab.
- **Tests:** Compose instrumented tests in `:designsystem` (`MonthSkyCalendarKeyboardTest`), with a button before and
  after the calendar. They cover Tab in and out, Shift+Tab, the anchor (chosen, today, first day, a chosen day that
  cannot be chosen), arrow moves, the edge of the selectable days, RTL, and a month with no selectable day.

### D2. The guidance moves below the message (D52)

- **Where the message ends:** `MigrelySnackbarHost` gains `onMessageBottomChanged: (Dp) -> Unit`, measured on its
  `AnimatedContent` in root coordinates (`positionInRoot`). It reports the bottom of the message, or 0 dp while none
  shows. Root coordinates on both sides spare any inset arithmetic.
- **The state:** `HomeScreen` keeps the value in a state and passes a reader, `messageBottom: () -> Dp`, through
  `HomeContents` to `AttackModeContents`. Only the layout phase reads it, so a moving message never recomposes the
  screen (review).
- **The column:** `SideBySideAttackLayout` measures its guidance column's own top, in root coordinates. A layout
  modifier outside the scroll pads the column by `guidanceTopPadding(messageBottom, columnTop)`: from the column's
  top to the message's bottom, at least 0, and 0 before the column is placed (no first-frame jump). So the visible
  part of the column starts at the message's bottom, scrolled or not, and its own 16 dp top space separates the first
  line from the message.
- **Taps while it moves:** the column holds "Details", the long-attack check and the start-time buttons, which move
  with it. A `TapGuard` over the column, keyed on the message's bottom in whole dp, swallows taps while it moves and
  for 500 ms after. So a quick second "2 h earlier" after a rejected move cannot land on another button (review).
- **Motion:** the host grows with the message's 250 ms slide in, and shrinks only after its 200 ms fade out, over
  250 ms. So the guidance never slides under a message still on screen. With `reduceMotion` the host turns off its size
  animation (`using null`), so the column moves at once.
- The messages stay above the mode tap guard, so Undo is always tappable. Moving them into the column's layout would
  put them under the guard.
- The controls column is untouched, and the stacked (portrait) layout ignores the value.
- **Tests:**
  - `MigrelySnackbarHostTest` checks the reported bottom against the message's bounds, 0 dp once it goes, and at once
    with "Reduce motion";
  - `GuidanceTopPaddingTest` covers the padding rule.

## Risks

| Risk | Mitigation |
|---|---|
| The calendar is the last focusable item on screen | Compose's Next wraps to the first item, as without the grid |
| The guidance moves while the user reads | it moves once, with the message, and at once with "Reduce motion" |
| A tall message at 200% leaves little of the column | the column still scrolls, below the message; the message goes after its time |
