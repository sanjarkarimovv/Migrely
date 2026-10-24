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
- **Arrow keys:** they keep Compose's 2D focus search between cells. At the edge they may leave the grid, as with any
  2D focus.
- Cells stay `clickable` and focusable, so accessibility services still reach every day (EC-UX-19). TalkBack and
  Switch Access do not use Tab.
- **Tests:** Compose instrumented tests in `:designsystem` (`MonthSkyCalendarTest`), with a button before and after
  the calendar. They cover Tab in and out, Shift+Tab, the anchor, and arrow moves.

### D2. The guidance moves below the message (D52)

- **Where the message ends:** `MigrelySnackbarHost` gains `onMessageBottomChanged: (Dp) -> Unit`, measured on its
  `AnimatedContent`. It reports the bottom of the message from the top of the window, or 0 dp while none shows.
  Window coordinates spare any inset arithmetic.
- **The state:** `HomeScreen` keeps the value and passes it as `messageBottom` through `HomeContents` to
  `AttackModeContents`.
- **The column:** `SideBySideAttackLayout` measures its guidance column's own top. The column gets a top padding of
  `messageBottom - columnTop - 8 dp`, at least 0, outside its scroll. So the visible part of the column starts below
  the message, whether it is scrolled or not, and no line is ever covered.
- **Motion:** the padding follows the message area's own size animation (`SizeTransform`), so both move together.
  With `reduceMotion` the host now turns off its size animation too (`using null`), so the column moves at once.
- The messages stay above the tap guard, so Undo is always tappable. Moving them into the column's layout would put
  them under the guard.
- The controls column is untouched, and the stacked (portrait) layout ignores the value.
- **Tests:** `MigrelySnackbarHostTest` checks the reported bottom against the message's bounds, 0 dp once the message
  goes, and at once with "Reduce motion".

## Risks

| Risk | Mitigation |
|---|---|
| The calendar is the last focusable item on screen | Compose's Next wraps to the first item, as without the grid |
| The guidance moves while the user reads | it moves once, with the message, and at once with "Reduce motion" |
| A tall message at 200% leaves little of the column | the column still scrolls, below the message; the message goes after its time |
