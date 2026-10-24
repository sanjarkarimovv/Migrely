# Proposal

## Why

A migraine diary needs more than attacks. Many migraines follow the menstrual cycle, and the main persona tracks her
period to see it (P1, DAY-F1, INS-F3). A day with no migraine should also count, but only when the user says so: the
calendar never guesses a clear sky (G6). BRD D29 put the period and pain-free marks, and the clear sky, into this change.

## What Changes

- **Day marks (D47, EC-DAY-01 to EC-DAY-05):**
  - each calendar date can be marked "Period" and "Pain-free day", today and past days only;
  - a pain-free mark is refused on a day that an attack touches, and an attack logged later wins over it;
  - a period mark is allowed on any day, an attack day included;
  - marks are dates, without a zone;
  - tapping a mark again removes it.
- **Where to mark (D48):**
  - History shows two switches, "Period" and "Pain-free day", under the chosen day;
  - home shows one "Period" toggle under today's card in the normal mode, so today's period takes one tap;
  - "Pain-free day" is not offered on home: it is an end-of-day judgement. It also comes with the daily reminder in
    change 6.
- **The clear sky (D49, EC-UX-09, EC-UX-10):**
  - a pain-free day shows a clear, starry sky in the calendar;
  - when today is marked pain-free, today's card shows a full moon and "Pain-free today";
  - a period day has a small drop in the corner of its cell;
  - the legend gains "Pain-free" and "Period", and screen readers say both.
- **Storage:** database version 5 adds `day_mark` (date, period, pain-free) through an additive `AutoMigration(4, 5)`.

**Out of scope:**
- turning period tracking off (onboarding in change 8, settings in change 12). Until then it is always on (D47);
- the keyboard grid of #22 (D38), moved to change 4a;
- stats and insights over the marks (changes 5 and 11).

## Capabilities

### New Capabilities

- `day-markers`: marking a day as a period day or pain-free, the rules that bound it, and where it is done.

### Modified Capabilities

- `month-sky`: a pain-free day shows a clear sky; a period day has its mark; the legend and screen readers include
  both.
- `today-sky`: today's card shows a clear sky when today is marked pain-free; home offers the "Period" toggle.

## Impact

- **Datasource:** a `daymark` package with `DayMarkDataSource`, `day_mark` table, DB v5, `MigrationTest` 4→5.
- **History feature:**
  - domain: the month's days carry their marks; `SetDayMarkUseCase` refuses a pain-free mark on an attack day or a
    future day;
  - presentation: the clear sky and marks per day; `onPeriodAction` and `onPainFreeAction`;
  - UI: the two switches under the chosen day, the cell mark and the legend.
- **Home feature:** today's marks in the snapshot; the clear today card; `onPeriodTodayAction` and the toggle.
- **Design system:** `DaySkyAppearance.Clear`, the period mark on `CalendarDay`, a period legend entry, and
  `SkyAppearance.Clear` with a full moon.
- **`:app`:** DI.
- **Docs:** BRD 9.4 (EC-DAY-01 to EC-DAY-05), 13 (rows 4 and 4a), 17 (D38 updated, D47 to D49); ARCHITECTURE §6a.
- **BRD references:** DAY-F1, DAY-F2, HIS-F1, HIS-F3, EC-UX-09, EC-UX-10, EC-UX-15, EC-DAY-01 to EC-DAY-05, G6, D29,
  D47 to D49.
