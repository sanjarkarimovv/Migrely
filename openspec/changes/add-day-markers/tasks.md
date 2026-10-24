# Tasks

## 1. Datasource (design D1)

- [x] 1.1 `day_mark` entity, `DayMarkDao`, `DayMarkDataSource` and its impl, DB v5 `AutoMigration(4, 5)`, schema
  `5.json`; unit tests, DAO tests and `MigrationTest` 4→5.
  Verify: `./gradlew :datasource:implementation:test` and
  `ANDROID_SERIAL=emulator-5570 ./gradlew :datasource:implementation:connectedDebugAndroidTest`

## 2. Design system (design D4)

- [x] 2.1 `DaySkyAppearance.Clear`, the period mark on `CalendarDay`, `LegendEntry.periodMark`, `SkyAppearance.Clear`.
  Verify: `./gradlew :designsystem:assembleDebug`

## 3. History (design D2, D3)

- [x] 3.1 Domain: marks on days, the effective pain-free rule, `SetDayMarkUseCase`, the shared day rule; tests.
  Verify: `./gradlew :features:history:domain:test`
- [x] 3.2 Data: `HistoryRepository` reads and writes marks; tests.
  Verify: `./gradlew :features:history:data:test`
- [x] 3.3 Presentation: clear sky, marks, `onPeriodAction`, `onPainFreeAction`; tests.
  Verify: `./gradlew :features:history:presentation:test`
- [x] 3.4 UI: the switches under the chosen day, the cell mark, descriptions and legend; tests.
  Verify: `./gradlew :features:history:ui:test`

## 4. Home (design D5)

- [x] 4.1 Domain and data: today's marks, `SetPeriodTodayUseCase`; tests.
  Verify: `./gradlew :features:home:domain:test :features:home:data:test`
- [x] 4.2 Presentation and UI: the clear calm card and the "Period" toggle; tests.
  Verify: `./gradlew :features:home:presentation:test :features:home:ui:test`

## 5. App wiring

- [x] 5.1 DI for the day mark datasource, repositories and use cases.
  Verify: `./gradlew :app:assembleDebug`

## 6. Docs

- [x] 6.1 ARCHITECTURE §6a (DB v5, day marks).

## 7. Verify

- [x] 7.1 Gate and instrumented datasource and designsystem tests.
- [x] 7.2 Upgrade from v4 with data; emulator walk of every scenario on emulator-5570. Check:
  - marking and unmarking period and pain-free in History;
  - the disabled pain-free switch on an attack day;
  - an attack added later on a pain-free day, then deleted;
  - home's toggle and the clear today card;
  - the legend and screen reader descriptions;
  - 200% font, landscape and rotation with a day chosen.
- [ ] 7.3 Independent review; fix and record.

## Verification record

**Gate** `./gradlew assembleDebug test ktlintCheck detekt`: BUILD SUCCESSFUL, 831 unit tests (787 before), 0 failures.

**Instrumented tests** on emulator-5570:
- datasource 59/59: new `DayMarkDaoTest` 4 and `MigrationTest` 4→5 and 1→5;
- designsystem 16/16.

**Upgrade:** a version 4 database with 29 attacks, an answered intake and a notice was upgraded in place. It became
version 5 with every row kept and an empty `day_mark`.

**Emulator walk** on emulator-5570. The first half was done by hand; the second, from the attack override on, by a
background agent that reported evidence:

| Scenario | Result |
|---|---|
| Home "Period" | the chip marks today (`2026-10-24\|1\|0`); today's cell shows the drop and reads "…: nothing logged. Period."; off and on again keeps the pain-free flag (`0\|1` then `1\|1`) |
| History, mark a day | 6 October: both switches on, the cell shows stars and the drop and reads "Tuesday, October 6: pain-free. Period." |
| Attack day | 14 October: the "Pain-free day" row is `enabled=false` with "A migraine is logged on this day."; the period row stays enabled |
| Attack added later | a past migraine on 6 October turns it "cloudy. 1 migraine … Period."; the stored pain-free flag stays; after deleting the attack, 6 October is "pain-free. Period." again |
| Pain-free today | home reads "Today: clear. Pain-free today. Marked by you." under a full moon; after an attack is started and ended, "1 migraine today"; after that attack is removed, "Pain-free today" again |
| Accessibility | each switch row is one toggle node (`checkable`, `checked`, `enabled`); the home chip is one checkable node whose label reads "Period today" |
| Rotation with a day chosen | landscape keeps 6 October chosen with both switches on; back to portrait the same |
| 200% font | switch labels, the 3-line legend and "✓ Period" complete; "Show all of October" reachable |
| Unmark | period off leaves `0\|1`; pain-free off removes the row; the cell shows the dashed outline and "nothing logged." |

**Found and fixed in the walk:**
- the lowest star of a pain-free cell sat under the period drop; moved clear;
- the home chip's description was a separate node; it now sits in the label and merges into the chip;
- "Nothing logged on this day." read as contradicting the marks; it is now "No migraine logged on this day." (delta
  `attack-history` › *Choose a day*). QA's `add-for-empty-day.yaml` needs the new text.

**Noted for change 4a:** at 200% the day numbers overlap the cell glyphs. Cloud and storm cells did so before this
change; the numbers stay readable and the drop is never covered.

**Not checked:** the RTL mirror of the drop. No RTL language ships in the MVP (EC-UX-05), and the drawing code mirrors
by `layoutDirection`.

**Settings restored:** zone Asia/Tashkent, auto_time_zone 1, font 1.0, rotation 0, accelerometer 1, the timeout
deleted, adbd unrooted. Walk marks and attacks deleted. emulator-5570 was restarted once, after the shared netsim
daemon went away at 18:17 (reported by QA); its data was intact.
