# Proposal

## Why

QA found two problems that sit around the calendar and attack mode.

- **#22 [S4]:** with a hardware keyboard, Tab visits every day of the month before "Migraine started". That is about 30
  presses to log an attack. BRD principle 1 asks for at most three taps.
- **#26 [S4]:** in landscape attack mode, the message now sits over the guidance column (change
  fix-today-zone-and-landscape-message, D37). It ends partway through "Started 6:04 PM · 0 min", so only the lower
  halves of the letters show for the whole undo window.

## What Changes

- **One Tab stop for the month (D38, EC-UX-19):** on home and in History, Tab enters the calendar once and the next
  Tab leaves it. Focus lands on the chosen day, otherwise on today, otherwise on the month's first day. The arrow keys
  move between days, and Enter opens or chooses the focused day. Shift+Tab works the same way backwards. Screen readers
  and Switch Access keep reaching every day, as before.
- **The guidance moves under the message (D52):** in landscape attack mode, while a message shows over the guidance
  column, that column's content moves down below the message and comes back when it goes. It moves at once with
  "Reduce motion". The controls column never moves. This replaces D37's acceptance of covered guidance text.

**Out of scope:** the day numbers overlapping the glyphs at 200% (walk note of change 4). They stay readable, and the
cells keep their fixed size.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `month-sky`: the month is one Tab stop, with arrow keys between days.
- `in-app-messages`: in landscape attack mode the guidance moves below the message.

## Impact

- **`:designsystem`:** `MonthSkyCalendar` handles Tab, the focus anchor and a visible focus ring, with Compose
  instrumented tests. Its callers do not change.
- **`:designsystem`:** `MigrelySnackbarHost` reports where its message ends.
- **`:features:home:ui`:** that bottom reaches `AttackModeContents`, whose guidance column moves below it.
- **Docs:** BRD 13 (row 4a) and 17 (D38 detailed, D52).
- **QA retest:** the a11y keyboard order on home and History (#22), and the landscape message state (#26).
