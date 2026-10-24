# Tasks

## 1. Domain (`:features:history:domain`, `:features:attack:domain`)

- [x] 1.1 Scaffold `:features:history:*` with `cag --new-feature --name=History --package=com.migrely.app.history --ktlint --detekt`, copy the build files from `features/home/*` and delete the stubs; verify `./gradlew :features:history:domain:build`
- [x] 1.2 Add the history domain models and the repository interfaces of design D2:
  - `GetCurrentTimeRepository`;
  - `GetEarliestAttackRepository`;
  - `GetAttacksTouchingRepository`;
  - `GetRecentlyDeletedAttackRepository`;
  - `RestoreDeletedAttackRepository`.

  Verify the domain module builds
- [x] 1.3 Implement `GetHistoryMonthUseCase` with tests for:
  - the current month for a null request;
  - clamping to the earliest and the current month, and no attacks;
  - the read range with its 2-day margin;
  - an attack past midnight and one ending at midnight;
  - the zone of record;
  - an attack in progress up to today;
  - an attack across two months;
  - day order and newest-first month order;
  - each failure as a value, and the recent deletion.

  Verify `./gradlew :features:history:domain:test`
- [x] 1.4 Implement `RestoreDeletedAttackUseCase` (history) with tests, and give `GetPastAttackDraftUseCase` a `LocalDate?` request (design D6) with tests for:
  - null, today and a future day;
  - a past day;
  - yesterday late at night, where the end is capped at now.

  Verify `./gradlew :features:history:domain:test :features:attack:domain:test`

## 2. Presentation (`:features:history:presentation`, `:features:attack:presentation`)

- [x] 2.1 Add the presentation models, `HistoryViewState`, `MonthSkyViewState`, the notifications and navigation events, and `HistoryMonthPresentationMapper`, with tests for:
  - the sky rule, including rain, a missing level counting as moderate, nothing logged and future days;
  - the peak;
  - the month arrows.

  Verify `./gradlew :features:history:presentation:test`
- [x] 2.2 Implement `HistoryViewModel` (enter, resume, month, attack, add, undo, leave) and `MonthSkyViewModel` (enter, month, day) with `BaseViewModelTest` tests, and pass the day through `AddPastAttackViewModel.onEnter(day)`; verify presentation tests pass

## 3. Datasources (`:datasource:source`, `:datasource:implementation`)

- [x] 3.1 Add the `earliestStart` DAO query (design D5) with instrumented tests for no attacks, several attacks with their zones, and a deleted first attack; the month read reuses `overlappingAll`, already covered for in-progress, touching and deleted attacks; verify `ANDROID_SERIAL=emulator-5570 ./gradlew :datasource:implementation:connectedDebugAndroidTest`
- [x] 3.2 Add `AttackDataSource.earliestStart` with `EarliestAttackDataModel`, and unit tests with a mocked DAO, including the failure mapping; verify `./gradlew :datasource:implementation:test`

## 4. Data (`:features:history:data`)

- [ ] 4.1 Implement `HistoryRepository` and `HistoryAttackDomainMapper` with tests for every interface, each happy path and each failure as a value; verify `./gradlew :features:history:data:test`

## 5. Design system (`:designsystem`)

- [ ] 5.1 Add `DaySkyAppearance`, `CalendarDay`, `MonthSkyCalendar` and `MonthSkyLegend` (design D7) with previews, and a Compose UI test for:
  - descriptions;
  - future days not clickable;
  - the selected state;
  - leading empty days.

  Verify `ANDROID_SERIAL=emulator-5570 ./gradlew :designsystem:connectedDebugAndroidTest`

## 6. UI (`:features:history:ui`, `:features:home:ui`, `:features:attack:ui`)

- [ ] 6.1 Add the history UI models, `HistoryViewStateUiMapper`, `MonthSkyViewStateUiMapper`, `HistoryNotificationUiMapper`, `HistoryMessageBehaviourUiMapper`, `CalendarDayUiMapper` (day descriptions and weekday layout by locale) and `HistoryTimeUiFormatter`, each with unit tests; verify `./gradlew :features:history:ui:test`
- [ ] 6.2 Build the `History` screen and its contents: month header, calendar, legend, the month or day list, "Add a past migraine", the empty state, landscape and messages; add previews; verify `./gradlew :features:history:ui:assembleDebug`
- [ ] 6.3 Build the `MonthSky` card and add home's `monthCalendar` slot under today's list in both layouts; pass the day into `AddPastAttack(navController, day)`; verify `./gradlew :features:home:ui:test :features:attack:ui:test`

## 7. App (`:app`)

- [ ] 7.1 Add the `History(epochDay)` route and `AddPastAttack(epochDay)`, the nav host entries and the slot, the Hilt modules (`HistoryModule`, `HistoryDataModule`, `HistoryUiModule`) and the `History` and `MonthSky` navigation mappers with parameterized tests; check that Konsist covers the new modules; verify `./gradlew :app:test`

## 8. Docs

- [ ] 8.1 Update `docs/ARCHITECTURE.md`:
  - §3: the history feature and home's slot;
  - §6a: the range queries;
  - §13: deviations, if any.

  Verify the document reads consistently with the code

## 9. Verify

- [ ] 9.1 Run the gate `./gradlew assembleDebug test ktlintCheck detekt` and the instrumented tests per module (`:datasource:implementation`, `:designsystem`) on `emulator-5570`; verify all pass
- [ ] 9.2 Walk every spec scenario on `emulator-5570`:
  - portrait, landscape and 200% font;
  - TalkBack labels via `uiautomator dump`;
  - rotation and process death on History;
  - restore every setting afterwards.

  Fix what the walk finds and record a verification record here
- [ ] 9.3 Run an independent code review (`superpowers:code-reviewer`) on `git diff develop...feature/add-attack-history`, fix the real findings, record them here and run `openspec validate add-attack-history --strict`
