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

- [x] 4.1 Implement `HistoryRepository` and `HistoryAttackDomainMapper` with tests for every interface, each happy path and each failure as a value; verify `./gradlew :features:history:data:test`

## 5. Design system (`:designsystem`)

- [x] 5.1 Add `DaySkyAppearance`, `CalendarDay`, `MonthSkyCalendar` and `MonthSkyLegend` (design D7) with previews, and a Compose UI test for:
  - descriptions;
  - future days not clickable;
  - the selected state;
  - leading empty days.

  Verify `ANDROID_SERIAL=emulator-5570 ./gradlew :designsystem:connectedDebugAndroidTest`

## 6. UI (`:features:history:ui`, `:features:home:ui`, `:features:attack:ui`)

- [x] 6.1 Add the history UI models, `HistoryViewStateUiMapper`, `MonthSkyViewStateUiMapper`, `HistoryNotificationUiMapper`, `HistoryMessageBehaviourUiMapper`, `CalendarDayUiMapper` (day descriptions and weekday layout by locale) and `HistoryTimeUiFormatter`, each with unit tests; verify `./gradlew :features:history:ui:test`
- [x] 6.2 Build the `History` screen and its contents: month header, calendar, legend, the month or day list, "Add a past migraine", the empty state, landscape and messages; add previews; verify `./gradlew :features:history:ui:assembleDebug`
- [x] 6.3 Build the `MonthSky` card and add home's `monthCalendar` slot under today's list in both layouts; pass the day into `AddPastAttack(navController, day)`; verify `./gradlew :features:home:ui:test :features:attack:ui:test`

## 7. App (`:app`)

- [x] 7.1 Add the `History(epochDay)` route and `AddPastAttack(epochDay)`, the nav host entries and the slot, the Hilt modules (`HistoryModule`, `HistoryDataModule`, `HistoryUiModule`) and the `History` and `MonthSky` navigation mappers with parameterized tests; check that Konsist covers the new modules; verify `./gradlew :app:test`

## 8. Docs

- [x] 8.1 Update `docs/ARCHITECTURE.md`:
  - §3: the history feature and home's slot;
  - §6a: the range queries;
  - §13: deviations, if any.

  Verify the document reads consistently with the code

## 9. Verify

- [x] 9.1 Run the gate `./gradlew assembleDebug test ktlintCheck detekt` and the instrumented tests per module (`:datasource:implementation`, `:designsystem`) on `emulator-5570`; verify all pass
- [x] 9.2 Walk every spec scenario on `emulator-5570`:
  - portrait, landscape and 200% font;
  - TalkBack labels via `uiautomator dump`;
  - rotation and process death on History;
  - restore every setting afterwards.

  Fix what the walk finds and record a verification record here
- [x] 9.3 Run an independent code review (`superpowers:code-reviewer`) on `git diff develop...feature/add-attack-history`, fix the real findings, record them here and run `openspec validate add-attack-history --strict`

## Verification record

**Gate and tests (2026-10-24)**
- `./gradlew assembleDebug test ktlintCheck detekt`: BUILD SUCCESSFUL. 566 unit tests in the debug variants (459
  before), 0 failures:
  - history: domain 18, presentation 35, data 15, ui 22;
  - attack: domain 98 (+5), presentation 51 (+1);
  - datasource 61 (+3);
  - app 31 (+8).
- Instrumented on `Migrely_API_36` (emulator-5570), per module:
  - datasource 34/34, with 3 new DAO tests for the earliest start;
  - designsystem 12/12, with 5 new calendar tests.

**Emulator walk** (en-US locale, so weeks start on Sunday; 12-hour clock; Asia/Tashkent). Data was seeded with SQL:
peaks 2 to 9, one without a pain level, past midnight, ending at midnight, logged in Tokyo, across September and
October, and the first attack in June.
- **month-sky:**
  - the worst attack decides (8 October: 3 and 8 make a storm), no pain level gives cloudy, unrecorded and mild give
    cloudy;
  - nothing logged is dashed, future days are empty, and today has a ring;
  - past midnight marks both days and an attack ending at midnight only one;
  - the legend has no clear sky;
  - with the phone moved to America/New_York (adb root, restored and unrooted afterwards), the Tokyo attack still
    marks 15 October.
- **TalkBack** (`uiautomator dump`):
  - every day reads as in the spec, for example "Thursday, October 8: storm. 2 migraines. Peak: severe." and "Today,
    Saturday, October 24: nothing logged.";
  - future days have no action;
  - rows read "Wednesday, October 14, 9:10 AM to 10:00 AM, peak 4 of 10. Opens its details.";
  - the arrows read "Previous month" and "Next month", and the disabled one is disabled on the button node.
- **attack-history:**
  - open from the month name and from a day;
  - previous month through September and an empty July ("No migraines logged in July") to June, where "Previous month"
    is disabled; "Next month" is disabled in October;
  - 13 attacks in October, newest first, with the across-month attack in both months;
  - choose a day (oldest first), choose it again to clear, "Show all of October", and a month change clears it;
  - a day with nothing logged;
  - add for 7 October (07:49 to 09:49 at 09:49, D30), back to History showing the new attack;
  - first use with an emptied database (restored afterwards): both arrows are disabled, and "Log your first migraine"
    opens the add screen with the usual times;
  - delete from details opened here: "Migraine deleted", Undo and "Undone.", with the attack back in the calendar and
    the list;
  - dismissed and rotated, the undo is not offered again;
  - rotation and process death (`run-as kill -9`, cold start) keep September with 12 September chosen;
  - landscape side by side, 200% font in portrait and landscape: nothing is clipped and everything scrolls.
- **today-sky:**
  - the calendar sits under today's card, in landscape in the left column;
  - deleted from History, back home within 30 s: home offers the undo once (D31), and Undo on home brings back 8
    October's sky without leaving home;
  - attack mode shows no calendar.
- **past-attacks:** the chosen-day draft as above; today and yesterday-late are covered by unit tests.

**Found in the walk and fixed**
- **The chosen day used `selected` semantics.** Compose exposes that as a checkbox, so every past day was announced
  "not checked". The calendar now gives only the chosen day a state description, "Chosen".
- **Cell clouds were small and the legend's dashed square was invisible.** The glyphs are larger, and outlines keep a
  minimum width and dash.
- **Spec scenarios used wrong weekdays for 2026.**
- **The today-sky undo scenario named an unreachable path.** It now follows the reachable one, through History.

**Not walked, and why**
- **A day with an attack in progress (rain) on History:** History cannot be reached during attack mode (G4). Covered by
  unit tests and the design-system preview.
- **Moving an attack to another day from details:** covered by the reload on resume, walked after adding and deleting,
  and by unit tests.
- **Weeks starting on Monday:** covered by the en-GB unit test.

**Environmental:** the host load was 10 to 18, from other sessions' emulators.
- One cold start was killed with "start timeout" before any app code ran. A relaunch worked.
- Loads take 3 to 8 s on this emulator.

## Independent review

A `superpowers:code-reviewer` pass on `git diff develop...feature/add-attack-history` found no high-severity
defects, one medium and five low. Each was checked against the code.

**It confirmed as correct:**
- the domain rules: zones, midnight, the attack in progress, the margin and clamping;
- the sky rule, the same as today's card;
- `rememberUpdatedState`, and the refresh key, with no loop and no missed reload;
- the navigation back stack, the Hilt bindings and the semantics order.

**Fixed:**
- **M1:** the screen replaced the month asked for with the clamped month. After deleting the earliest attack, an undo
  brought it back but showed the clamped month. The request now stays as the user made it. Re-walked on the emulator:
  June's only attack deleted, History moved to August, and Undo showed June again with 10 June chosen.
- **L2:** a slower answer to an older month request could replace a newer one. The view state now returns its request,
  and the screen ignores stale answers.
- **L1:** a deletion made from home was offered again on History within 30 s. History now offers only deletions made
  after it was entered.
- **L3:** a device-future day could be chosen when an attack logged in a zone ahead gave it a sky. Selectability is now
  passed explicitly: today and earlier.
- **L5:** the add-screen navigation KDoc and design D7 were out of date.
- **Test gaps:**
  - an attack in progress since an earlier month, shown in both months;
  - no deletion announced on a month change.
- **Style:** `HistoryCalendarMappers` moved to the `mapper` package.

**Accepted and documented (design risks):**
- **L4:** the earliest month comes from the earliest instant, so mixed zones at a month boundary at the very start of
  the history could hide a local month. This is very rare.

**Not changed:**
- **Day cells of about 41 dp at 360 dp width:** a common calendar density, under the 48 dp guideline.
- **Description strings rebuilt on recomposition:** cheap for 31 days.

**After the fixes:**
- Gate: 569 unit tests, 0 failures.
- designsystem 12/12 instrumented.
- `openspec validate add-attack-history --strict`: valid.

