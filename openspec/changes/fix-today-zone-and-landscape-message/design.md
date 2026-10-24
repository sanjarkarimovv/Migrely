# Design

## Context

- **#19.** `HomeRepository.today()` works out today's bounds in the device's current zone and asks the datasource for
  the ended attacks that overlap them. The rule that decides which day an attack belongs to therefore lives in the
  data layer, and it is a different rule from the one `GetHistoryMonthUseCase` applies. History reads the month with
  a margin of two days on each side, then keeps an attack on every date from its start date to the date just before its
  end, both worked out in the attack's start zone.
- **#19 side note.** Home's `TimeUiFormatter.formatClockTime` and History's `HistoryTimeUiFormatter.formatClockTime`
  add "(City)" to every time in another zone. A row formats its start and its end separately, so the city appears
  twice.
- **#20.** `HomeContents` draws `messages()` over both modes, so a message survives the mode switch and Undo stays above
  the `TapGuard` (D32). `MigrelySnackbarHost` centres the message at the top, at most 560 dp wide. In a short window
  (`isCompactHeight()`, under 480 dp) attack mode is `SideBySideAttackLayout`: a `Row` with two columns of equal weight,
  24 dp apart, inside the safe-drawing padding and 16 dp side padding. The right column starts with the intensity chips,
  under the message.

## Goals and non-goals

- **Goals:** one rule for "which day" across home, the calendar and History; the city once per range; landscape attack
  controls never covered.
- **Non-goals:** the keyboard grid (#22, D38, change 4); a reload when the zone changes while home is open; a shared
  "attack days" module (see D2).

## Decisions

### D1. The domain decides today's attacks

| Layer | Before | After |
|---|---|---|
| `GetTodayRepository` | `today()` read today's device-zone bounds | `today(from, to)` reads the ended attacks touching the window it is given |
| `TodaySnapshotDomainModel` | `endedAttacksTouchingToday`, `now` | `endedAttacks`; no clock (the use case already has it) |
| `GetTodayUseCase` | used the list as it came | reads `GetCurrentTimeRepository`, asks for today ± 2 days in the device zone, keeps the attacks whose start-zone dates include today's device date |

- The margin of two days is the one History uses (`ZONE_MARGIN_DAYS`): zone offsets differ by at most 26 hours.
- An attack's dates run from its start date to the date of `end - 1 ns`, both in its start zone, so an attack that
  ends exactly at midnight does not touch the next day. This matches History (EC-ATK-06).
- The attack in progress and the deletion offer are unchanged.
- The clock is read once, by the use case, so the window and the 72-hour check use the same instant.

### D2. The day rule is repeated in home, not shared yet

`GetHistoryMonthUseCase.dates()` and the new private function in `GetTodayUseCase` are three lines each. Features never
depend on each other (rule 6), and a shared domain module for three lines would add a module, a Konsist rule and DI
wiring. Stats (change 5) and the doctor report (change 10) need the same rule. Change 5 is the third user, so it
moves the rule into a shared pure-Kotlin module. This is recorded in `tasks.md` and in the progress memory.

### D3. The city once per range

| Formatter | Change |
|---|---|
| Home `TimeUiFormatter` | `formatClockTime` and `formatStartTime` take `withCity: Boolean = true` |
| History `HistoryTimeUiFormatter` | `formatStart` and `formatClockTime` take `withCity: Boolean = true` |

- A "Logged today" row formats its start without the city and its end with it: "11:15 PM – 11:45 PM (Berlin)".
- A History row does the same when the attack has ended. A row for an attack in progress has no end, so its start
  keeps the city.
- The spoken descriptions use the same texts, so a screen reader says the city once too.
- Attack mode's "Started …" line, the messages, the details screen and "Taken at" show one time each and keep their
  city.

### D4. Messages beside the controls in landscape attack mode

- `HomeContents` wraps `messages()` in a `Box`. When the target mode is attack mode and the window is short, the box is
  half the screen wide at the start edge. Otherwise it has no size of its own, and the host keeps its full width and
  centring.
- The decision is a pure function, `messagesBesideControls(mode, compactHeight)`, unit tested like `guardsTaps`.
- **Why half the width is always clear of the controls.** With window width W, left inset L and right inset R, the
  right column starts at W/2 + (L − R)/2 + 12 dp. The host pads its content by L and R and by 16 dp more, so the message
  ends at W/2 − R − 16 dp at most. That is left of the column for every L and R ≥ 0. The message starts where the left
  column starts (L + 16 dp).
- **Why not lower the controls or move the message into the left column.** Moving the controls down would shift them
  under a finger that is reaching for them, for 5 s to 2 min. A message drawn inside the attack layout would sit below
  the tap guard, so Undo would ignore taps for its first second (D32). It would also lose its saved state when the mode
  changes.
- The guidance line in the left column may be covered while the message shows, as the top of the card is in portrait.
  The message is the confirmation at that moment (D37).
- Normal mode in landscape, portrait and tablets (height ≥ 480 dp) keep the centred message.

## Risks

| Risk | Mitigation |
|---|---|
| A narrower message wraps more at 200% font | the snackbar already moves the action below the text when it does not fit (EC-UX-03); walked at 200% in landscape |
| An attack logged west of the device's date line looks missing today | it is on its own date in the calendar, which the card now agrees with; scenario "Logged before flying west" |
| Two clock reads (use case, then repository) | the repository no longer reads the clock for today; only the use case does |
