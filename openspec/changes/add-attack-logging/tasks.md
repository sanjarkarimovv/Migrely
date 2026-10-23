# Tasks

## 1. Foundations

- [ ] 1.1 Create the `:designsystem` Android library (Compose, ktlint, detekt) and move `MigrelyTheme`, `Color.kt` and `Type.kt` from `:app/ui/theme` into it; make `:app` and `:features:home:ui` depend on it; verify `./gradlew :designsystem:build :app:assembleDebug` succeeds and the app still opens with the dark theme on the emulator
- [ ] 1.2 Convert `:datasource:implementation` to an Android library with KSP, Room 2.8.5 (`room-runtime`, `room-ktx`, `room-compiler`) and the Room Gradle plugin writing schemas to `datasource/implementation/schemas`; verify `./gradlew :datasource:implementation:build` succeeds
- [ ] 1.3 Update `docs/ARCHITECTURE.md` for `:designsystem` (§3, §10), the Room binding and schema policy, `rememberReduceMotion()` and the `attack` and `clock` datasource packages; verify each item has a section and §13 lists the designsystem deviation from the sample

## 2. Domain (`:features:home:domain`)

- [ ] 2.1 Add `AttackDomainModel`, `IntensityPointDomainModel`, `LongAttackCheckDomainModel`, `IntensityLevelDomainModel` (Mild 3, Moderate 6, Severe 9), `TodayDomainModel` and the result models from design D2 (including `EndOutOfRange` for ends outside `[start, now]`); verify `./gradlew :features:home:domain:build`
- [ ] 2.2 Add one single-function repository interface per use case plus `CurrentTimeRepository`; verify the module builds and Konsist `ArchitectureDomainTest` passes
- [ ] 2.3 Implement `ObserveTodayUseCase` with tests for calm, in progress, ended (count, peak, unknown intensity), an attack crossing midnight, and the 72-hour due rule with `confirmedAt` and `disabled`; verify `./gradlew :features:home:domain:test` passes
- [ ] 2.4 Implement `StartAttackUseCase` and `UndoAttackStartUseCase` with tests for started, already in progress, storage full, and the undo guard (only an attack still in progress is removed); verify domain tests pass
- [ ] 2.5 Implement `RecordIntensityUseCase` with tests for level mapping, replacement within 2 minutes, a new point after 2 minutes, and no attack in progress; verify domain tests pass
- [ ] 2.6 Implement `MoveAttackStartEarlierUseCase` with tests for a move, a rejection that reports the previous attack's end, and no attack in progress; verify domain tests pass
- [ ] 2.7 Implement `EndAttackUseCase` (now or a picked time) and `UndoAttackEndUseCase` with tests for ending now, a picked time inside and outside `[start, now]`, and the reopen guard when another attack is in progress; verify domain tests pass
- [ ] 2.8 Implement `AnswerLongAttackCheckUseCase` with tests for "Still going" (sets `confirmedAt`) and "Don't ask again" (sets `disabled`); verify domain tests pass

## 3. Datasources (`:datasource:source`, `:datasource:implementation`)

- [ ] 3.1 Add `AttackDataSource`, `ClockDataSource`, `AttackDataModel`, `IntensityPointDataModel`, `AttackStartDataModel`, `StorageFullDataException` and `DatabaseDataException` in `com.migrely.app.datasource.{attack,clock}`; verify `./gradlew :datasource:source:build` and Konsist `ArchitectureDataSourceTest` pass
- [ ] 3.2 Add `AttackLocalModel`, `IntensityPointLocalModel` (cascade delete, value CHECK 1–10), `AttackDao` (in-progress, touching-range and previous-end queries; transactional start-if-none) and `MigrelyDatabase` version 1 with `exportSchema = true`; verify the build writes `schemas/.../1.json` and the file is committed
- [ ] 3.3 Implement `AttackDataSourceImpl` and `AttackLocalMapper` with unit tests for mapping both ways, `SQLiteFullException` to `StorageFullDataException`, and other `SQLiteException`s to `DatabaseDataException`; verify `./gradlew :datasource:implementation:testDebugUnitTest` passes
- [ ] 3.4 Implement `ClockDataSourceImpl` with an injected `java.time.Clock` and tests that `observeDate()` emits the current date and the next date right after local midnight; verify datasource unit tests pass
- [ ] 3.5 Add the instrumented `AttackDaoTest` covering single in-progress start under concurrent calls, cascade delete of intensity points, the touching-range query across midnight, and the previous-end query; verify `./gradlew :datasource:implementation:connectedDebugAndroidTest` passes on the `Medium_Phone_API_36.0` emulator

## 4. Data (`:features:home:data`)

- [ ] 4.1 Add data-to-domain and domain-to-data mappers with parameterized tests; verify `./gradlew :features:home:data:test` passes
- [ ] 4.2 Implement `HomeRepository` for every home repository interface, with `observeToday()` combining the in-progress flow, the touching-today flow and the clock's date flow, and with every failure returned as a value; add a test for each function's success and failure path; verify `./gradlew :features:home:data:test` passes

## 5. Presentation (`:features:home:presentation`)

- [ ] 5.1 Replace `HomeViewState` with `Loading`, `Today` and `AttackInProgress` (design D7), and add the presentation models, `HomePresentationNotification` and the domain-to-presentation mappers with parameterized tests; verify `./gradlew :features:home:presentation:test` passes
- [ ] 5.2 Implement every `HomeViewModel` action from design D7 with `BaseViewModelTest` tests covering each view state and notification (including the shortcut start in `onEnter`); verify presentation tests and Konsist `ArchitecturePresentationTest` pass

## 6. Design system and UI

- [ ] 6.1 Implement `SkyCanvas` in `:designsystem` for `Calm`, `Hazy`, `Cloudy`, `Storm` and `Rain`, with deterministic outline jitter, a still dim lightning line and rain that stays still when `animate = false`, and a `@Preview` for each appearance; verify `./gradlew :designsystem:build` and compare the previews with `docs/design/style-frames`
- [ ] 6.2 Implement `rememberReduceMotion()` in `:designsystem`, refreshed on resume; verify it returns true on the emulator after `adb shell settings put global animator_duration_scale 0`
- [ ] 6.3 Add the home UI models (Parcelable), `HomeViewStateUiMapper`, the UI-model-to-`SkyAppearance` mapper and `TimeUiFormatter` (locale, 24-hour setting, start-zone city when it differs) with tests; verify `./gradlew :features:home:ui:testDebugUnitTest` passes
- [ ] 6.4 Build `TodayContents`: the sky card with the spec's texts per state, its spoken description, and "Migraine started"; add a preview per state; verify `./gradlew :features:home:ui:build`
- [ ] 6.5 Build `AttackModeContents`: darker background, one guidance line, "Started HH:MM · N min" ticking every 30 seconds, intensity chips at least 64 dp with selected semantics, "Started earlier" options and "Migraine ended" in the lower half; add previews; verify `./gradlew :features:home:ui:build`
- [ ] 6.6 Add the 1,200 ms transitions that snap with reduce motion and never animate the first frame, plus haptic confirmation after start and end; verify on the emulator with animations on and off
- [ ] 6.7 Add the 5-second undo snackbars for start and end, and the messages for storage full, a rejected start move (showing the previous end time) and a generic failure; verify on the emulator that undo works within 5 seconds and the snackbar disappears after it
- [ ] 6.8 Add the 72-hour inline card with "Still going", "It has ended" (Material 3 date and time pickers limited to `[start, now]`) and "Don't ask again for this attack", including the doctor note; verify the UI builds and the pickers reject out-of-range values

## 7. App wiring

- [ ] 7.1 Add `DatabaseModule` (singleton Room database, DAO, datasource bindings), `HomeModule`, `HomeDataModule` and `HomeUiModule`; verify `./gradlew :app:assembleDebug` succeeds (the Hilt graph compiles)
- [ ] 7.2 Change the route to `Home(startAttack: Boolean = false)`, make `MainActivity` `singleTop`, map the `START_ATTACK` launch intent and `onNewIntent` to `Home(startAttack = true)`, and update the navigation mapper test; verify `./gradlew :app:testDebugUnitTest` passes
- [ ] 7.3 Add the static launcher shortcut "Migraine started" (`res/xml/shortcuts.xml`, a vector icon, manifest meta-data); verify `adb shell dumpsys shortcut com.migrely.app` lists it and long-pressing the icon on the emulator shows it
- [ ] 7.4 Confirm the release surface: verify `aapt dump permissions` on the debug APK still lists no `INTERNET` and no runtime permission

## 8. Integration verification

- [ ] 8.1 Run `./gradlew assembleDebug testDebugUnitTest ktlintCheck detekt`; verify it ends with BUILD SUCCESSFUL and record the test count
- [ ] 8.2 Run `./gradlew :datasource:implementation:connectedDebugAndroidTest` on the emulator; verify all instrumented tests pass
- [ ] 8.3 Walk the `attack-logging`, `attack-mode`, `today-sky` and `app-shell` scenarios on the API 36 emulator: start, undo start, double tap, shortcut cold and during an attack, intensity and the 2-minute replacement, moving the start earlier and the overlap rejection, end and undo end, app kill during an attack (`adb shell am kill`), rotation, reduce motion, font scale 2.0, and spoken labels in a `uiautomator dump`; record each result. The 72-hour, midnight, daylight-saving and storage-full scenarios are covered by unit tests, because the Play Store emulator image cannot change the system clock or fill storage safely
- [ ] 8.4 Run `openspec validate add-attack-logging --strict`; verify it reports the change as valid

## Workflow follow-up

- Commit after each task group with the session's attribution, push `feat/add-attack-logging` and open a PR stacked on PR #1.
- Archive with `/opsx:archive` after review, so the four specs merge into `openspec/specs/`.
