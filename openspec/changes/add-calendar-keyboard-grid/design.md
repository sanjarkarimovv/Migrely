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

- `HomeContents` measures the message area (`onSizeChanged` on its `Box`) and passes its height to `AttackModeContents`
  as `messageAreaHeight`.
- The host's height includes the top inset and 8 dp of padding on each side. `SideBySideAttackLayout` computes the
  message's bottom inside the safe area as `messageAreaHeight - top inset - 8 dp`. The guidance column's top space is
  then `max(16 dp, bottom + 8 dp)`.
- The space animates over 250 ms, the message's own enter time, or jumps with `reduceMotion`. With no message the
  space is 16 dp, as now.
- The controls column is untouched. The stacked (portrait) layout ignores the height.

## Risks

| Risk | Mitigation |
|---|---|
| The calendar is the last focusable item on screen | Compose's Next wraps to the first item, as without the grid |
| The guidance moves while the user reads | it moves once, at the same time as the message, softly, or not at all with "Reduce motion" |
