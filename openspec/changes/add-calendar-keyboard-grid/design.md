# Design

## Context

- `MonthSkyCalendar` (design system) draws each day as a `clickable` cell. Compose gives every cell its own Tab stop,
  and moves focus between cells with the arrow keys (2D focus search).
- `HomeContents` draws the messages over both modes. In landscape attack mode they keep to the start half (D37), over
  the guidance column of `SideBySideAttackLayout`, which starts 16 dp below the safe area.

## Decisions

### D1. One Tab stop for the month

- **Leaving:** the grid tracks whether one of its cells has focus, through `onFocusChanged` on the grid. On Tab, or
  Shift+Tab, a key handler on the grid (`onPreviewKeyEvent`) moves focus Next, or Previous, until focus leaves the
  grid, at most once per cell. The key event is consumed.
- **Entering:** when the grid gains focus from outside, focus moves to the anchor day's `FocusRequester`.
  `MonthSkyCalendar` takes a new `focusAnchorDay: Int?`; History passes the chosen day, otherwise today, otherwise 1.
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
| The Tab loop meets a focus that never leaves (a grid alone on screen) | at most one move per cell |
| The guidance moves while the user reads | it moves once, at the same time as the message, softly, or not at all with "Reduce motion" |
