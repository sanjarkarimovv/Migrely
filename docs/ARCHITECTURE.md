# Migrely — Architecture Profile

This is the project profile that the `clean-arch-for-android-developers` skill reads before any code is written
(skill "Step 0"). It fixes the project-specific bindings. It never overrides the skill's twelve rules.

## 1. Sources of truth and precedence

1. The skill's **twelve rules**: always apply and are never overridden.
2. **This profile**: project bindings, naming extras and recorded deviations.
3. The **sample** `EranBoudjnah/CleanArchitectureForAndroid` (reference commit `9d99920`, 2026-10-20): code shapes and
   conventions where this profile is silent.
4. The **book** *Clean Architecture for Android*, 2nd edition: explanations and rationale.
5. **`cag`** (CleanArchitectureGenerator 0.4.1): the way new boilerplate is created. Generated code is then brought in
   line with 1–3 (section 4).

Product requirements are in `docs/BRD.md`. Change plans are in `openspec/`.

## 2. Toolchain

| Item | Version | Notes |
|---|---|---|
| Gradle | 8.14.3 | wrapper |
| Android Gradle Plugin | 8.13.0 | classic `android {}` DSL plus the `kotlin.android` plugin (what `cag` generates) |
| Kotlin | 2.2.20 | JVM target 17 |
| Compose BOM | 2025.10.00 | Material 3 |
| Hilt | 2.57.2 | KSP |
| Navigation Compose | 2.9.5 | type-safe routes with kotlinx.serialization 1.9.0 |
| compileSdk / targetSdk | 36 / 36 | |
| minSdk | 26 | BRD section 10 |
| JDK to run Gradle | 17 or 21 | CI uses Temurin 17 |

**Planned upgrade.** An `upgrade-toolchain` change moves the project to AGP 9 (built-in Kotlin), Kotlin 2.4, SDK 37 and
Navigation 3, matching the sample. It runs when `cag` generates AGP 9-compatible modules, or before Google Play requires
target API 37, whichever comes first. Until then, do not mix AGP 9 idioms into build files.

## 3. Module layout and packages

| Module | Kind | Package | Contents |
|---|---|---|---|
| `:app` | Android app | `com.migrely.app` | `MainActivity`, `MigrelyApplication`, `di/` (all Hilt modules), `navigation/mapper/`, `ui/main/` (nav host, routes), `ui/theme/` |
| `:architecture:domain` | JVM | `com.migrely.app.architecture.domain` | `UseCase`, `BackgroundExecutingUseCase`, `ContinuousExecutingUseCase`, `UseCaseExecutor`, `DomainException` |
| `:architecture:presentation` | JVM | `com.migrely.app.architecture.presentation` | `BaseViewModel`, `PresentationNavigationEvent`, `PresentationNotification` |
| `:architecture:presentation-test` | JVM | same | `BaseViewModelTest` |
| `:architecture:ui` | Android | `com.migrely.app.architecture.ui` | `BaseComposeHolder`, `UiDestination`, `NavigationEventDestinationMapper`, `ScreenEnterObserver`, notification contracts |
| `:architecture:instrumentation-test` | Android | `com.migrely.app.test` | E2E test helpers (first used by a feature change) |
| `:coroutine` | JVM | `com.migrely.app.coroutine` | `CoroutineContextProvider` |
| `:designsystem` | Android | `com.migrely.app.designsystem.{theme,sky,motion,snackbar,picker,icon,input}` | `MigrelyTheme(dark)`, `MigrelyColors`, `MigrelyDarkRoom` / `DarkScreens`, `SkyCanvas` / `SkyAppearance`, `MonthSkyCalendar` / `MonthSkyLegend` / `DaySkyAppearance`, `rememberReduceMotion()`, `MigrelySnackbar`, `MigrelyDateTimePicker`, `MigrelyIcons`, `TapGuard`. Compose only; depends on no feature, presentation or domain module |
| `:datasource:source` | JVM | `com.migrely.app.datasource.<resource>.{datasource,model,exception}` | datasource interfaces, `*DataModel` / `*LocalModel`, `*DataException` |
| `:datasource:implementation` | Android (Room via KSP) | `com.migrely.app.datasource.<resource>.{datasource,model,dao,mapper}` and `com.migrely.app.datasource.database` | `*DataSourceImpl`, Room entities (`*LocalModel`), DAOs, `MigrelyDatabase`, `*LocalMapper` |
| `:features:<feature>:domain` | JVM | `com.migrely.app.<feature>.domain` | `model/`, `repository/` (interfaces), `usecase/`, `exception/` |
| `:features:attackdays:domain` | JVM | `com.migrely.app.attackdays.domain` | `AttackDays`: the one rule for which dates an attack touches, and the zone margin for reading them (BRD D57). A shared domain module, used by home's and History's domains; it has no models, repositories or use cases of its own |
| `:features:reminders:*` | JVM / Android | `com.migrely.app.reminders.*` | The Reminders screen, the daily reminder and its runs (change add-reminders, BRD D65–D68), like any feature |
| `:features:backup:*` | JVM / Android | `com.migrely.app.backup.*` | Your data: JSON and CSV exports and the import (change add-data-export-import, BRD D71–D73), like any feature |
| `:features:settings:*` | JVM / Android | `com.migrely.app.settings.*` | The Settings screen in sections, with "Track periods", the theme and "Delete all data"; and the Warning signs, About Migrely, Privacy and Open-source licences pages, which share one view model (BRD D70, D79, D93–D97), like any feature |
| `:features:onboarding:*` | JVM / Android | `com.migrely.app.onboarding.*` | Onboarding on a new user's first start (change add-onboarding, BRD D76–D80), like any feature |
| `:app` `start/` | Android | `com.migrely.app.start` | `StartEntryPoint`, through which `MainActivity` asks, behind the splash, whether onboarding comes first and which theme to draw in (BRD D76, D94); `restartApp`, which starts a new process after all data is deleted (D96) |
| `:app` `reminder/` and `datasource/` | Android | `com.migrely.app.reminder`, `com.migrely.app.datasource.{schedule,notification}.datasource` | `ReminderWorker`, `ReminderActionReceiver`, `TimeChangeReceiver`, `ReminderEntryPoint`; and the implementations of `ReminderScheduleDataSource` (WorkManager) and `NotificationDataSource` (NotificationManagerCompat) |
| `:features:<feature>:presentation` | JVM | `com.migrely.app.<feature>.presentation` | `viewmodel/`, `model/`, `navigation/`, `mapper/` |
| `:features:<feature>:data` | JVM | `com.migrely.app.<feature>.data` | `repository/` (implementations), `mapper/` |
| `:features:<feature>:ui` | Android | `com.migrely.app.<feature>.ui` | `view/`, `model/`, `mapper/`, `di/` (`*Dependencies`, plain classes, no Hilt) |

- Datasources share one `source`/`implementation` pair, one package per resource (`database`, `attack`, `tag`, `medication`,
  `preferences`, `billing`, `scheduler`, `pdf`, `file`, `analytics`, `clock`, `appinfo`), as in the sample. The pair may be split later only for build-time
  reasons.
- Features: `home` (today's sky, attack mode, quick actions), `attack`, `history` and `medication`. They read the
  shared `AttackDataSource` and `MedicationDataSource` through their own models and repositories.
- `medication` (change `add-medication-tracking`) has three screens: Medication taken, My medications and the editor.
  - Home and `attack` open Medication taken only through navigation events, which `:app` maps to
    `LogMedication(attackId, darkRoom)`.
  - The details screen reads its intakes through the attack feature's own repository and models.
- `attack` has two screens, each with its own ViewModel, view state, notifications, navigation events and
  `Dependencies`: one attack's details, and adding a past attack (change `add-past-attacks`). They share the feature's
  domain, models and UI pieces such as `TimeRow`.
- `history` (change `add-attack-history`) owns the month sky calendar. It has the History screen and a month card, each
  with its own ViewModel and `Dependencies`. Home shows the card through a slot,
  `Home(monthCalendar: @Composable (refreshKey: Int) -> Unit)`, that `:app` fills; home never sees the history
  feature. The key counts home's view states, so the card loads again whenever home does.
- `medication` owns home's medication card the same way (change `add-medication-effect-and-overuse`, BRD D46):
  - `MedicationPromptViewModel` and `MedicationPromptDependencies` fill the slot
    `Home(medicationPrompt: @Composable (refreshKey: Int) -> Unit)` under today's card;
  - `GetMedicationPromptUseCase` picks the one card: an overuse warning first, then "Did it help?";
  - the card neither navigates nor shows messages, so its `Dependencies` has no observer;
  - History's standing overuse indicator works the same way (change `add-overuse-indicator`, BRD D59):
    `OveruseIndicatorDependencies` fills the slot `History(overuseIndicator: @Composable (refreshKey: Int) -> Unit)`,
    which History's month summary shows for the current month only. The key counts History's view states, so the
    indicator loads again whenever History does, an undo or a resume included. The warning and the indicator count
    through one `OveruseDayCounter`.
- Features never depend on other features. Something two features need goes into a datasource, `:app`, or a dedicated
  shared domain module with its own reason to exist.

## 4. Generating code with `cag`

| Need | Command (run from the repository root) |
|---|---|
| New feature | `cag --new-feature --name=<Feature> --package=com.migrely.app.<feature> --ktlint --detekt` (always pass `--package`: without it `cag` 0.4.1 joins the base package and the feature name without a dot, e.g. `com.migrely.apphome`) |
| New datasource | `cag --new-datasource --name=<Resource>` |
| New use case | `cag --new-use-case --name=<Verb><Entity> --path=features/<feature>/domain/src/main/java/com/migrely/app/<feature>/domain/usecase` |
| New ViewModel | `cag --new-view-model --name=<Screen> --path=features/<feature>/presentation/src/main/java/com/migrely/app/<feature>/presentation/viewmodel` |

After generating a feature:

1. Delete the generated `Stub*` models, mappers and dummy use cases and repositories that the feature does not need.
2. Rename the generated `<Feature>Screen()` and `<Feature>ScreenContent()` composables to `<Screen>()` and
   `<Screen>Contents()` (sample naming).
3. Register the feature in `:app`: DI modules, navigation mapper, route and nav host entry (section 5).
4. Add the tests that skill rule 12 requires before the change is considered done.

## 5. Navigation binding: Navigation Compose 2, type-safe

The binding follows the skill's `bindings/nav2.md`.

- `UiDestination` is `fun interface UiDestination { fun navigate(navController: NavController) }` in
  `:architecture:ui`.
- Routes are `@Serializable` objects and data classes in `:app/ui/main/route/Routes.kt`.
- `AppNavHost` (`:app/ui/main/AppNavHost.kt`) is an extension of `AppNavHostDependencies`. It hosts one `composable<Route>`
  per screen and calls the feature's `<Screen>(navController)`.
- ViewModels emit sealed `<Screen>PresentationNavigationEvent`s describing intent (`On…`). `:app/navigation/mapper/` maps
  them to `UiDestination`s with an exhaustive `when` and has a parameterized test.
- Features never import routes, `NavHost` or `NavHostController`. They receive a `NavController` only to pass it to
  `ViewModelObserver`.

## 6. Network binding

None. The app declares no `INTERNET` permission. The billing (RevenueCat) and analytics (TelemetryDeck) SDKs are the
only code allowed to use the network. The changes that add them also add `INTERNET` and update the `app-shell` spec.
Health data must never be passed to either SDK.

## 6a. Persistence binding (Room)

- One database, `MigrelyDatabase` in `:datasource:implementation`, provided as a singleton by `:app/di/DatabaseModule`.
- Entities are `*LocalModel` classes, used only inside `:datasource:implementation`. `*LocalMapper` classes turn them
  into the `*DataModel`s exposed by `:datasource:source`.
- Times are stored as epoch milliseconds plus the IANA zone id where the event started (BRD EC-TIM-01).
  - The domain assigns days in that zone, and "today" is the device's date (D36).
  - UI formatters read the device zone when called. `MainActivity` recreates itself when the device zone changes, so
    every screen draws its times again (change fix-today-zone-and-landscape-message, design D5).
- Every schema version is exported to `datasource/implementation/schemas` and committed. Migrations are written and
  tested; `fallbackToDestructiveMigration` is forbidden (BRD EC-STO-01).
- Rules that must be atomic run in one DAO `@Transaction`: "only one attack in progress", "store a past attack only
  when nothing overlaps it" (`insertEndedIfNoOverlap`) and a merge (`mergeInto`).
- **Schema changes.** Additive versions use `@AutoMigration`; anything else gets a hand-written `Migration`. Every step
  has an instrumented `MigrationTestHelper` test that migrates a database holding data (`MigrationTest`). Version 2
  (change `add-attack-details`) added impact, note and deleted-at columns and the `attack_tag` and `custom_tag` tables.
- **Tags.** `attack_tag` stores locations, symptoms and triggers as `(kind, tag_key)`. Built-in keys are the stable
  `key` values of the `*DataModel` enums in `:datasource:source` (BRD D25) and must never change. Custom tags are
  `custom:<id>` rows in `custom_tag`, which are archived but never deleted. Unknown keys are skipped when read.
- **Soft delete.** Deleting an attack sets `deleted_epoch_millis`. Every read except `recentlyDeleted` and the restore
  check hides deleted attacks. Rows deleted more than 2 minutes ago are purged by the datasource when it deletes or
  lists deletions (BRD D23). The hard `delete` remains only for "undo start".
- **Overlaps and merges.** Stored attacks never overlap; touching at one instant is allowed, and an attack in progress
  is open-ended. `overlapping(excludingId?, start, end)` lists every overlapping attack, oldest first.
  - The domain decides what a merge means (BRD D27). `AttackDataSource.merge` then writes it in one transaction:
    1. the absorbed attacks' intensity points move to the survivor;
    2. their tags are copied with `IGNORE`, so a tag on both is kept once;
    3. the long-attack check takes the strictest state;
    4. the survivor takes the new fields;
    5. the absorbed rows are deleted for good (no undo, BRD D28).
  - The schema did not change for this.
- **Medications (version 3, change `add-medication-tracking`).**
  - `medication` rows are never deleted, only archived (BRD EC-MED-01). Their unique `name_key` is the trimmed,
    lower-case name.
  - `medication_intake` points to its medication with RESTRICT, and to its attack, if any, with CASCADE. A purged
    attack therefore takes its intakes with it; a soft-deleted one hides them (EC-MED-10).
  - `mergeInto` moves intakes to the survivor in the same transaction. `discard`, which undoes a mistaken start,
    detaches them first, so a medication logged meanwhile stays on its own.
  - Version 3 is an additive `AutoMigration(2, 3)`, covered by `MigrationTest`.
- **Answers and overuse notices (version 4, change `add-medication-effect-and-overuse`).**
  - `medication_intake.effect` holds the answer to "Did it help?". `effect_closed` is set by an answer or a skip, so
    the question is asked once (BRD D39).
  - `overuse_notice` keeps the last "Got it" per category and level, primary key `(category, level)`, written with
    `@Upsert` (BRD EC-MED-06).
  - The pending question and the acute intakes are read with a `LEFT JOIN` on `attack` that leaves out attacks being
    deleted. Preventive medications are left out by `kind`.
  - Version 4 is an additive `AutoMigration(3, 4)`, covered by `MigrationTest` 3→4 and 1→4.
- **Day marks (version 5, change `add-day-markers`).**
  - `day_mark` keys a row by its ISO date (`yyyy-MM-dd`), so a range is a text `BETWEEN`. It holds `period` and
    `pain_free`. A date has no zone (BRD EC-DAY-04).
  - `DayMarkDao.update` changes one flag in a `@Transaction` and deletes the row when no mark is left.
  - The domain decides the rest: a pain-free mark counts only while no attack touches the day, and is refused on such
    a day (EC-DAY-01).
  - Version 5 is an additive `AutoMigration(4, 5)`, covered by `MigrationTest` 4→5 and 1→5.
- **Range reads (History).** A month is read with `overlapping(null, from, to)`, which returns every attack touching
  the range, the one in progress included, with a margin of two days on each side so that attacks logged in other
  zones are found. The first month that can be shown comes from `earliestStart()`. Both use the start index; the
  domain assigns days in each attack's zone (BRD EC-TIM-02).
- `SQLiteFullException` becomes `StorageFullDataException`. Any other `SQLiteException` becomes
  `DatabaseDataException`. Repositories turn both into failure values (section 7).
- DAO SQL is covered by instrumented tests (`connectedDebugAndroidTest` on the `Migrely_API_36` emulator). Datasource
  implementations are unit-tested with a mocked DAO.

## 7. Failure routing (skill rule 9)

`UseCaseExecutor` catches only exceptions thrown synchronously by `execute()`. `BackgroundExecutingUseCase` runs
`executeInBackground` in a coroutine, so an exception thrown there **escapes the executor and crashes the app**. Rules:

1. Repositories (`:features:<f>:data`) catch every `*DataException` and every other datasource failure.
2. Expected failures come back to the ViewModel through `onResult`, as a sealed domain model, for example:

   ```kotlin
   sealed interface SaveAttackResultDomainModel {
       data class Saved(val attack: AttackDomainModel) : SaveAttackResultDomainModel
       data object StorageFull : SaveAttackResultDomainModel
       data object Overlaps : SaveAttackResultDomainModel
   }
   ```

3. `executeInBackground` never throws. Each repository has a unit test that proves a failure comes back as a value.
4. `onException` is used only for synchronous use cases.
5. `ContinuousExecutingUseCase` flows catch inside the repository and emit failure values.
6. `ContinuousExecutingUseCase` launches in a scope that is never cancelled, and ViewModels are unscoped. Use it only with
   a cancellable scope. Screens load data with one-shot use cases on `onEnter`, on `onResume()` and after every
   action (change `add-attack-logging`, design D11).

## 8. UI event collection and state

- `BaseComposeHolder.ViewModelObserver(navController)` collects `navigationEvent` and `notification` inside one
  `LaunchedEffect(Unit)` each. This deliberately deviates from the generated `collectAsState`, which drops two identical
  events in a row.
- ViewModels are stateless (skill rule 4). Text and selections the user is editing live in the composable
  (`rememberSaveable`) and are passed to `on…Action(...)` as parameters.
- Every screen has a stateful `<Screen>()` (with `ScreenEnterObserver` and `ViewModelObserver`) and a stateless
  `<Screen>Contents()` with a `@Preview` per view state.

## 9. Naming extras (on top of the skill)

| Element | Pattern |
|---|---|
| ViewModel public functions | `onEnter()`, `on<Subject>Action(...)` only |
| View state | `sealed interface <Screen>ViewState` naming *what*, not *how* (`Ready`, `Empty`, `AttackInProgress`) |
| Screen composables | `<Screen>()` and `<Screen>Contents()` |
| UI holder | `<Screen>Dependencies : BaseComposeHolder<…>` |
| Hilt modules (`:app/di`) | `<Feature>Module` (use cases, presentation mappers, ViewModels), `<Feature>DataModule` (repositories), `<Feature>UiModule` (UI mappers, `Dependencies`), `<Resource>DataSourceModule` |
| Navigation mapper | `:app/navigation/mapper/<Screen>NavigationEventDestinationMapper` |

## 10. Theme and resources

- The theme lives in `:designsystem` (`MigrelyTheme`, `MigrelyColors`). It is dark by default, or light by the user's
  choice in Settings (BRD EC-UX-01, D94): fixed schemes, no dynamic colour, no pure white anywhere.
  - `MainActivity` reads the choice behind the splash and follows it live; "Match the phone" follows
    `isSystemInDarkTheme()`. Screens take every colour from `MaterialTheme.colorScheme`.
  - Attack mode and the dark room wrap themselves in `MigrelyDarkRoom`, which keeps them, their dialogs and their
    messages in the dark scheme in either theme, and counts itself in `DarkScreens` so the system bars stay dark too.
  - Skies are dark in every theme; what is drawn on a sky takes the dark scheme's colours.
- Feature previews wrap content in `MigrelyTheme`, with a light preview for new screens.
- Feature UI mappers convert their own UI models into design-system parameters (for example `SkyAppearance`). No
  feature model is passed into `:designsystem`.
- Motion: every animation checks `rememberReduceMotion()` (true when the system animator scale is 0) and snaps
  instead of animating (BRD EC-UX-08). Animations never flash more than three times per second (EC-UX-07).
- User-facing strings live in the owning feature's `ui` module resources. Screens never hard-code strings.
- Type (BRD D20): Fraunces (soft serif) for display, headline and `titleLarge`, and Figtree for every other style.
  - Both are bundled variable fonts in `designsystem/src/main/res/font`, and their OFL texts are in
    `designsystem/src/main/assets/licenses/fonts`. Fonts are never downloaded.
  - Screens use `MaterialTheme.typography` roles, never a font family directly.
  - Times that tick add `fontFeatureSettings = "tnum"` at the call site.
- Messages (BRD D21, EC-UX-16): screens show snackbars only through `MigrelySnackbarHost` and `MigrelySnackbar` in
  `com.migrely.app.designsystem.snackbar`, never Material's `SnackbarHost`.
  - A feature maps each message to a `SnackbarTone`, an optional `SnackbarAction` and a base duration (UI mapper).
  - The feature keeps the shown message in `rememberSaveable` and stretches the base duration with
    `LocalAccessibilityManager.calculateRecommendedTimeoutMillis`. The home feature's `HomeMessages` is the reference.

## 11. Testing

| What | Where | Libraries |
|---|---|---|
| Use cases, domain logic | `:features:<f>:domain` unit tests | JUnit 4, Mockito-Kotlin, kotlinx-coroutines-test |
| ViewModels, presentation mappers | `:features:<f>:presentation` unit tests | `BaseViewModelTest`, parameterized mapper tests |
| Repositories, data mappers | `:features:<f>:data` unit tests | happy path and failure-as-value path |
| Datasources | `:datasource:implementation` unit tests | |
| UI mappers, navigation mappers | `:features:<f>:ui` and `:app` unit tests | parameterized |
| Architecture rules | `:app` unit tests, `com.migrely.app.architecture` | Konsist 0.17.3 (skill templates) |
| Design-system components | `:designsystem` instrumented tests (`connectedDebugAndroidTest`) | Compose UI test (`createComposeRule`) |

End-to-end flows, the device accessibility audit, device scenarios and the release Go/No-Go list are not part of the
gate. They run on the QA emulators and are described in `docs/QA.md` (BRD D40–D44).

## 12. Quality gate and Definition of Done

```bash
./gradlew assembleDebug test ktlintCheck detekt
```

The gate uses `test`, not `testDebugUnitTest`: the domain, presentation and data modules are plain JVM modules
with no `testDebugUnitTest` task, so `testDebugUnitTest` alone would skip their tests. The same command runs in
`.github/workflows/ci.yml`. A change is done when:

- the gate passes;
- `openspec validate <change> --strict` passes;
- every task in the change's `tasks.md` is checked with its verification met.

## 13. Known deviations from the sample

| Deviation | Reason | Ends |
|---|---|---|
| Navigation Compose 2 instead of Navigation 3 | `cag` scaffolds Nav2; feature code is identical | `upgrade-toolchain` |
| AGP 8.13 / Kotlin 2.2.20 / SDK 36 instead of AGP 9.4 / Kotlin 2.4 / SDK 37 | `cag` templates need the `kotlin.android` plugin | `upgrade-toolchain` |
| Features grouped under `features/` | `cag` layout | permanent |
| `minSdk` 26 instead of 24 | BRD section 10 | permanent |
| `ViewModelObserver` collects in `LaunchedEffect` | skill `bindings/nav2.md` | permanent |
| `:designsystem` module holds the theme (the sample keeps it in `:app`) | home and history both draw skies; profile section 3 allows a shared module with its own reason | permanent |
| Konsist: the four "models are suffixed with …Model" rules scan production code only (`scopeFromProduction()`) | unit tests for a model belong in the model's package (for example `AttackDomainModelTest`); the skill forbids renaming classes to dodge a rule | permanent |
| The add-past-attack screen asks for its defaults with `LaunchedEffect` while its saveable times are empty, instead of `ScreenEnterObserver` | after process death before the defaults arrived, `ScreenEnterObserver` would never ask again and leave the screen empty; once picked, the saved times win | permanent |
| The add screen carries the end along with a new start and caps it at the current minute in `PastAttackDraftUiModel` | an editing convenience of the form, like the picker bounds; the domain validates the times again | permanent |
| Home hosts History's month card and the medication card, and History hosts the overuse indicator, through composable slots that `:app` fills; each loads when its refresh key changes instead of using `ScreenEnterObserver` | one calendar implementation without a feature depending on another; the sample has no screen composed of two features. The key is home's view-state count, so the card follows every home load, including an undo on home | permanent |
| The schedule and notification datasource implementations live in `:app`, not `:datasource:implementation` | they need `MainActivity`, the worker, the receivers and the notification copy, which `:datasource:implementation` cannot see; they keep the datasource package names, so the architecture tests still place them | permanent |
| Onboarding hosts the reminders feature's daily reminder offer through a slot that `:app` fills. The offer is not a screen, so it reads its view model's state and notifications itself, through its own UI mappers, and loads with `LaunchedEffect(viewModel)` | the offer reuses the Reminders screen's permission rules without onboarding depending on the reminders feature (BRD D80) | permanent |
| `YourDataViewModel`, `ReportViewModel` and `SettingsViewModel` are `@Singleton`s that keep, only for delivery, whether work is going on and its outcome: the last outcome the screen has not taken, or, for Settings, that all data was deleted. `YourDataViewModel` also keeps the automatic copy it read last, so its row stays while work goes on | writing or reading a file, making a report or deleting all data can outlast a rotation; the base view model's flows do not replay, so a screen rebuilt mid-work would show its actions enabled and miss the outcome, and Settings would miss the restart. The screen asks for both once it listens, and acknowledges each outcome | permanent |
| `AttacksCsvMapper` (`:features:backup:data`) and `ReportDocumentMapper` and `ReportRecordsDomainMapper` (`:features:report:data`) hold the English words of the CSV and of the PDF | the CSV and the PDF are documents whose words must not change with the screen; resources are UI only | the app's first translation |
| Further Konsist rule adaptations | recorded here when made | — |
