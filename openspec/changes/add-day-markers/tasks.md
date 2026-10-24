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
- [ ] 4.2 Presentation and UI: the clear calm card and the "Period" toggle; tests.
  Verify: `./gradlew :features:home:presentation:test :features:home:ui:test`

## 5. App wiring

- [ ] 5.1 DI for the day mark datasource, repositories and use cases.
  Verify: `./gradlew :app:assembleDebug`

## 6. Docs

- [ ] 6.1 ARCHITECTURE §6a (DB v5, day marks).

## 7. Verify

- [ ] 7.1 Gate and instrumented datasource and designsystem tests.
- [ ] 7.2 Upgrade from v4 with data; emulator walk of every scenario on emulator-5570. Check:
  - marking and unmarking period and pain-free in History;
  - the disabled pain-free switch on an attack day;
  - an attack added later on a pain-free day, then deleted;
  - home's toggle and the clear today card;
  - the legend and screen reader descriptions;
  - 200% font, landscape and rotation with a day chosen.
- [ ] 7.3 Independent review; fix and record.
