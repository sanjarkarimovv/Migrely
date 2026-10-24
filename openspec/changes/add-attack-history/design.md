# Design

## Context

- **Data:** attacks live in one Room table with start and end indices. Each attack stores its start zone, and the
  attack in progress has no end.
- **Home today:** home reads today's ended attacks with `endedTouching(from, to)` in the device's zone. It draws today's
  sky with `SkyCanvas`, and it offers undo for a deletion made on the details screen.
- **Navigation:** routes and navigation live in `:app` (Nav2, type-safe routes). Features never depend on each other
  (ARCHITECTURE §3). ViewModels are stateless and unscoped, and screens load with one-shot use cases on `onEnter`, on
  `onResume()` and after every action (§7).

## Goals and non-goals

**Goals**
- Build the month calendar once. Home and History show the same calendar, built from the same domain rules.
- Attribute days correctly: zone of record, past midnight, and the attack in progress (EC-TIM-02, EC-ATK-06).
- Keep the calendar and list fast with ten years of data (EC-HIS-02).
- Keep the month and the chosen day across rotation and process death.

**Non-goals**
- Period and pain-free marks, the clear sky (change 4) and stats (change 5).
- A schema change.
- History in attack mode.

## Decisions

### D1. A `history` feature that owns the calendar, and a slot on home

| Option | Verdict |
|---|---|
| Copy the calendar logic into home and history | Two copies of the day rules drift apart. Rejected |
| A shared `:features:calendar:domain` module | Removes the domain copy, but home would still need its own presentation, UI and repository for the month. Rejected for now; a later reuse (report, stats) can extract it |
| **The history feature also provides a "month sky" composable, and `:app` puts it into a slot on home** | One implementation. Home knows only a `@Composable (refreshKey: Int) -> Unit` slot, never the history feature |

The month card has its own `MonthSkyViewModel` and `MonthSkyDependencies`. Home passes `refreshKey`, the number of home
view states received. Home loads again after every action and on resume, so the card follows home's data, for example
after "Undo" of a deletion on home. The card's taps emit navigation events that `:app` maps to the `History` route.

### D2. Day rules live in the domain; the sky is a presentation mapping

`GetHistoryMonthUseCase(month: YearMonth?)` runs these steps.

| Step | Rule |
|---|---|
| Current month | `now` in the device zone |
| Earliest month | The month of the earliest start in that attack's zone, capped at the current month. With no attacks, the current month (EC-HIS-03) |
| Month shown | The request, or the current month when the request is null, clamped between the two |
| Read | `touching(from, to)`, where `from` is the first day's 00:00 in the device zone minus 2 days and `to` is the day after the last day plus 2 days. Offsets differ by at most 26 hours (EC-HIS-02) |
| Days of an attack | From its start date to the date of `end − 1 ns`, both in the start zone. An attack in progress runs to today in its zone (EC-ATK-06, EC-TIM-02) |
| Result | Every day of the month with its attacks, oldest first; the month's attacks, newest first; `today`; the earliest month; any recent deletion |

The presentation mapper turns each day into a sky, using the same rule as today's card (`TodayPresentationMapper`):
- rain when any attack on the day is in progress;
- otherwise the sky of `max(highest recorded level, Moderate if any level is missing)`;
- with no attacks, "nothing logged" up to today and "future" after it.

The peak shown is the highest recorded level, or "not recorded".

### D3. The month as a page

A month is the page of the list (EC-HIS-02). Even with chronic migraine, one month holds a few dozen attacks at most.
Paging 3 would add a library and carry `PagingData` through every layer. The arrows already fetch one month at a time.

### D4. State ownership

| State | Owner |
|---|---|
| Month asked for, chosen day | `History()` composable, `rememberSaveable` as epoch values; it follows the month in the view state after clamping |
| Month data | Database, loaded on `onEnter`, `onResume()` and after undo |
| Shown message, deletions already offered | `HistoryMessages`, `rememberSaveable`, as in `HomeMessages` |

The `History(epochDay)` route argument seeds the chosen day and its month.

### D5. Datasource additions, no schema change

| Function | Query |
|---|---|
| Month read: the existing `overlapping(null, from, to)` | Undeleted attacks with `start < to AND (end IS NULL OR end > from)`, oldest first, with their points. It is exactly "every attack touching the range, the one in progress included", so no new query is needed |
| `earliestStart()` (new) | `start` and `start_zone_id` of the undeleted attack with the smallest start (`ORDER BY start LIMIT 1`, which uses the start index) |

The database stays at version 2.

### D6. Adding for a chosen day (D30)

`GetPastAttackDraftUseCase` takes a `LocalDate?`. For null, today or a later day it returns the usual defaults. For a
past day, the start is that day at the clock time of the usual start, and the end is `min(start + 2 h, now)` rounded to
the minute.

The route becomes `AddPastAttack(epochDay: Long? = null)`, and the screen's `onEnter(day)` passes the day through. The
existing `LaunchedEffect` that asks for defaults while the saveable times are empty is kept.

### D7. Design-system calendar

`com.migrely.app.designsystem.sky` gains:
- `MonthSkyCalendar(weekdayInitials, leadingEmptyDays, days)`;
- `CalendarDay(dayOfMonth, appearance, isToday, isSelected, description, onClick?)`;
- `DaySkyAppearance` (`NothingLogged`, `Future`, `Hazy`, `Cloudy`, `Storm`, `Rain`);
- `MonthSkyLegend`.

| Element | How it is drawn |
|---|---|
| Day cell | The `SkyCanvas` gradients, with simplified still glyphs: waves, cloud, cloud and lightning, cloud and three still streaks. Rain never moves in a cell (EC-UX-07) |
| Nothing logged | A dashed outline |
| Future day | A faint outline, not clickable |
| Today | A thin ring |
| Chosen day | A thick primary ring, with `selected` semantics |

Feature UI mappers build `CalendarDay` from their own UI models, and the locale's first weekday sets
`leadingEmptyDays` (EC-TIM-05).

### D8. Undo on History (D31)

History repeats home's deletion handling:
- `onEnter` and `onResume` announce a restorable deletion;
- `HistoryMessages` offers it once, if it is younger than 30 s;
- undo calls `RestoreDeletedAttackUseCase` and then loads again.

Returning home within 30 s offers it once more there, which BRD D31 accepts.

## Modules touched (new-feature checklist order)

1. `:features:history:domain`: models, five single-function repository interfaces, `GetHistoryMonthUseCase` and
   `RestoreDeletedAttackUseCase`.
2. `:features:history:presentation`:
   - `HistoryViewModel` and `MonthSkyViewModel`;
   - view states, notifications and navigation events;
   - `HistoryMonthPresentationMapper`.
3. `:datasource:*`: `earliestStart` with its DAO query and tests; the month read reuses `overlapping`.
4. `:features:history:data`: `HistoryRepository` and `HistoryAttackDomainMapper`.
5. `:designsystem`: `MonthSkyCalendar`, `MonthSkyLegend` and a Compose UI test.
6. `:features:history:ui`:
   - the `History` screen and the `MonthSky` card;
   - UI models and mappers, a time formatter, messages and `Dependencies`.
7. `:features:attack:*`: the draft for a chosen day.
8. `:features:home:ui`: the slot.
9. `:app`:
   - routes, DI modules and navigation mappers with tests;
   - the slot wiring;
   - Konsist coverage of the new modules.

## Risks

- **An attack logged in a zone ahead of the device can fall in a day or month the device has not reached.** Its day
  still shows its sky (data wins, G6). A month the device has not reached cannot be opened until it arrives, which is
  hours at most.
- **Home's calendar reloads on every home view state, about one extra query per action.** It is cheap, because one
  month is read through an index.
- **Two copies of the deletion-undo UI, in home and history.** This follows skill rule 6: identical UI in two features
  stays separate.
- **Unscoped ViewModels** (upgrade-toolchain): a load that finishes while the screen is recreated is lost. It is
  repeated on resume, so the screen never stays stale.
