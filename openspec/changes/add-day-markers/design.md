# Design

## Context

- History's month comes from `GetHistoryMonthUseCase`. It reads the attacks touching the month, with a margin, and puts
  each attack on the dates it touches in its start zone. `HistoryMonthPresentationMapper` turns a day into a sky.
  Home's month card uses the same use case.
- Today's card comes from `GetTodayUseCase` (home), with the same day rule since D36.
- The design system draws day skies (`DaySkyAppearance`, `MonthSkyCalendar`, `MonthSkyLegend`) and today's sky
  (`SkyAppearance`, `SkyCanvas`). Neither has a clear sky yet (G6, D29).

## Goals and non-goals

- **Goals:** period and pain-free marks stored by date; a clear sky only where the user said so; marks visible and
  spoken in every calendar; one-tap period on home.
- **Non-goals:** a period tracking switch (changes 8 and 12); the keyboard grid (change 4a); stats (change 5).

## Decisions

### D1. Storage, DB v5

| Column | Type | Notes |
|---|---|---|
| `date` | `TEXT` primary key | ISO `yyyy-MM-dd`, so ranges compare as text |
| `period` | `INTEGER NOT NULL DEFAULT 0` | |
| `pain_free` | `INTEGER NOT NULL DEFAULT 0` | |
| `updated_epoch_millis` | `INTEGER NOT NULL` | |

- A row exists only while one mark is on. Setting a mark is a `@Transaction`: read the row, change one flag, then upsert
  it or delete it when both flags are off.
- `DayMarkDataSource` (`datasource.daymark`) has three functions:
  - `marksBetween(from, to)`: the marks of the dates in the closed range;
  - `setPeriod(date, marked)`;
  - `setPainFree(date, marked)`.
- The source module uses `LocalDate`: a mark is a date without a zone (EC-DAY-04).
- Additive, so `AutoMigration(4, 5)` with `MigrationTest` 4→5.

### D2. History domain

- `HistoryDayDomainModel` gains `period` and `painFree`. `painFree` is effective: marked, and no attack touches the day
  (EC-DAY-01). The stored mark stays, so deleting the attack brings it back.
- `GetHistoryMonthUseCase` also reads the month's marks through `GetDayMarksRepository`. A failed read fails the month,
  as a failed attack read does.
- `SetDayMarkUseCase` takes a `SetDayMarkRequestDomainModel(date, kind, marked)`, where the kind is Period or PainFree.
  It returns one of:
  - `Done`;
  - `FutureDay`, when the date is after today's device date;
  - `AttackLogged`, when a pain-free mark goes on a day an attack touches. The check reads the attacks around the date
    and uses the same day rule as the month;
  - `StorageFull` or `Failed`.
- Removing a mark is always allowed.
- The day rule (an attack's dates in its start zone) moves into an internal `datesTouched` function in the history
  domain. `GetHistoryMonthUseCase` and `SetDayMarkUseCase` share it. Home keeps its own copy until change 5 (fix
  design D2).

### D3. History presentation and UI

- `DaySkyPresentationModel` gains `Clear`. A day shows `Clear` when it is pain-free; attacks always win.
- `HistoryDayPresentationModel` and `HistoryDayUiModel` gain `period`, `painFree` and `painFreeAllowed` (no attack
  touches the day).
- `HistoryViewModel` has two new actions, `onPeriodAction(month, date, marked)` and `onPainFreeAction(month, date,
  marked)`. Each writes the mark and loads the month again, so the chosen day stays. `AttackLogged` shows "A migraine is
  logged on this day." The UI prevents that case, so the message is only a fallback.
- Under the chosen day, after its attacks and before "Add a past migraine", two rows each hold a label and a `Switch`.
  Each row is `toggleable`, 56 dp high. When pain-free is not allowed, its row is disabled and its supporting line says
  why.
- `CalendarDay` gains `hasPeriodMark`. The spoken description gains "pain-free" for the sky and "Period." at the end.

### D4. Design system

| Piece | Change |
|---|---|
| `DaySkyAppearance.Clear` | the clear-night gradient with three small four-point stars |
| period mark | a small drop at the bottom end corner in a soft rose (`#D9A3B5`), with a dark edge so it reads on every sky; its shape tells it apart without colour |
| `LegendEntry` | `periodMark: Boolean`; such an entry draws only the drop |
| `SkyAppearance.Clear` | today's clear sky: a lighter night gradient, a full moon instead of the crescent, more stars |

### D5. Home

- **Domain:**
  - `GetTodayUseCase` reads today's mark through `GetDayMarkRepository`;
  - `TodayDomainModel.Calm` gains `markedPainFree`, and `TodayResultDomainModel.Loaded` gains `periodToday`;
  - a pain-free mark matters only when the card is calm. With an ended attack today, the attacks show.
- **Writing:** `SetPeriodTodayUseCase(marked)` writes today's date through `SetPeriodRepository`, then home loads again.
  Only the period mark is written from home (D48).
- **UI:**
  - the calm card reads "Pain-free today" and "Marked by you" with `SkyAppearance.Clear`;
  - under today's card, a `FilterChip` "Period" with a check when on, 48 dp, spoken as "Period today";
  - the chip sits before the medication card and only in the normal mode.

## Risks

| Risk | Mitigation |
|---|---|
| A stored pain-free mark under a later attack | read-time rule; the mark is never shown or counted while an attack touches the day (EC-DAY-01) |
| A date without a zone after travel | intended: a mark is the user's own calendar date (EC-DAY-04) |
| More reads on every home and History load | one indexed range read by primary key |
| Rose mark on light sky colours | the drop has a dark 1 dp edge and a fixed shape |
