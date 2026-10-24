# Proposal

## Why

Migrely so far shows only today. A person with migraine, and the doctor they see, needs the month at a glance: which
days hurt, how badly, and how often (BRD 6.1 item 2, HIS-F1 to HIS-F3).
- The "Your sky" concept (BRD 18.3 A) puts a month of small skies under today's card. It is also the main store visual
  (18.7), and it is still missing.
- Attacks from earlier days can only be reached right after they are logged. There is no way back to last week's attack
  to add a symptom, fix a time or delete it.
- Change 1c left "add a past attack for a chosen day" to this change.

## What Changes

- **Month sky calendar (BRD HIS-F1, EC-UX-09, EC-UX-15, D29):** one small sky per day.
  - A day takes the sky of its worst attack: 1–3 hazy, 4–6 cloudy, 7–10 storm. An attack without a pain level counts as
    cloudy.
  - A day with an attack in progress shows rain.
  - A past day with nothing logged has a dashed outline and is never shown as clear. Future days are empty.
  - Days follow the zone each attack was logged in, and an attack past midnight marks every day it touches (EC-TIM-02,
    EC-ATK-06, EC-ATK-07).
  - Weeks start on the locale's first day (EC-TIM-05).
  - TalkBack reads each day, for example "Wednesday, 15 October: storm. 1 migraine. Peak: severe."
- **Home (BRD 18.3 A, D29):** this month's calendar sits under today's card and list. The month name and every day
  open History.
- **History screen (BRD HIS-F2, HIS-F3, D29),** a new `history` feature:
  - month by month, never before the month of the first attack and never past this month (EC-HIS-03);
  - under the calendar, the month's attacks, newest first. A month is one page, which is how the list is paged
    (EC-HIS-02);
  - tapping a day lists that day's attacks, each opening its details to view, edit or delete;
  - "Add a past migraine" for the chosen day (D30).
  - **No attacks yet (EC-HIS-01):** a short explanation and "Log your first migraine".
  - **Deleting from History (EC-HIS-04, D31):** an attack deleted from details opened here can be undone on History.
- **Add a past migraine for a chosen day (D30):** the add screen of `past-attacks` accepts a day. For a past day, the
  usual default times move to that day; for today, nothing changes.

**Out of scope:**
- **Change 4 (`add-day-markers`):** period and pain-free marks (the rest of HIS-F1 and HIS-F3), and with them the
  "clear" sky and its legend entry (G6).
- **Change 5 (`add-basic-stats`):** monthly numbers and the progress message "3 fewer migraine days than September"
  (EC-UX-14).
- History during attack mode: the dark room stays as it is (G4).
- Jumping to a month with a picker or a swipe: the arrows are enough for the MVP.
- The calendar in the doctor report (change 10).

## Capabilities

### New Capabilities

- `month-sky`: the month calendar itself, which turns attacks into one sky per day. Home and History both show it.
- `attack-history`: the History screen, which covers month navigation, the month's attacks, choosing a day, adding a
  past attack for it, the empty state and undoing a deletion.

### Modified Capabilities

- `today-sky`: the home screen shows this month's calendar under today's card and opens History.
- `past-attacks`: the add screen accepts a chosen day.

## Impact

- **New modules:** `:features:history:{domain,presentation,data,ui}`. They hold the History screen and the month card
  that home shows.
- **Changed modules:**
  - `:designsystem`: the month calendar grid with its day skies and legend (`sky` package).
  - `:datasource:*`: the earliest start. The month reuses the existing overlap read, which returns every attack touching
    a range, the one in progress included. Both use the start index, so there is no schema change and the database
    stays at version 2.
  - `:features:home:ui`: a slot under today's list that `:app` fills with the month card.
  - `:features:attack:*`: the add screen's draft for a chosen day.
  - `:app`:
    - the `History` route and the `day` argument of `AddPastAttack`;
    - DI;
    - navigation mappers;
    - the month card in home's slot.
- **Docs:**
  - BRD 9 (EC-HIS-03, EC-HIS-04), 13 (rows 2, 4 and 5), 17 (D29–D31) and 18.3 A;
  - `docs/ARCHITECTURE.md` §3 (the history feature, the home slot) and §13.
- **BRD references:** HIS-F1, HIS-F2, HIS-F3, EC-HIS-01, EC-HIS-02, EC-HIS-03, EC-HIS-04, EC-ATK-06, EC-ATK-07,
  EC-ATK-08, EC-ATK-14, EC-TIM-02, EC-TIM-05, EC-UX-03, EC-UX-09, EC-UX-15, EC-UX-16, 18.3 A, G4, G6, D26, D29, D30,
  D31.
