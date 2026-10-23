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
| `:datasource:source` | JVM | `com.migrely.app.datasource.<resource>.{datasource,model,exception}` | datasource interfaces, `*DataModel` / `*LocalModel`, `*DataException` |
| `:datasource:implementation` | JVM as generated; becomes an Android library with the first Android-backed datasource (Room, change 1) | `com.migrely.app.datasource.<resource>.{datasource,...}` | `*DataSourceImpl`, Room/DataStore/SDK code, `*ApiModel` / `*LocalModel` mappers |
| `:features:<feature>:domain` | JVM | `com.migrely.app.<feature>.domain` | `model/`, `repository/` (interfaces), `usecase/`, `exception/` |
| `:features:<feature>:presentation` | JVM | `com.migrely.app.<feature>.presentation` | `viewmodel/`, `model/`, `navigation/`, `mapper/` |
| `:features:<feature>:data` | JVM | `com.migrely.app.<feature>.data` | `repository/` (implementations), `mapper/` |
| `:features:<feature>:ui` | Android | `com.migrely.app.<feature>.ui` | `view/`, `model/`, `mapper/`, `di/` (`*Dependencies`, plain classes, no Hilt) |

- Datasources share one `source`/`implementation` pair, one package per resource (`database`, `preferences`, `billing`,
  `scheduler`, `pdf`, `file`, `analytics`, `clock`), as in the sample. The pair may be split later only for build-time
  reasons.
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

- The theme lives in `:app/ui/theme`. The app is always dark: a fixed `darkColorScheme`, no dynamic colour, no pure
  white anywhere (BRD EC-UX-01).
- Feature previews wrap content in `MaterialTheme(colorScheme = darkColorScheme())`.
- User-facing strings live in the owning feature's `ui` module resources. Screens never hard-code strings.

## 11. Testing

| What | Where | Libraries |
|---|---|---|
| Use cases, domain logic | `:features:<f>:domain` unit tests | JUnit 4, Mockito-Kotlin, kotlinx-coroutines-test |
| ViewModels, presentation mappers | `:features:<f>:presentation` unit tests | `BaseViewModelTest`, parameterized mapper tests |
| Repositories, data mappers | `:features:<f>:data` unit tests | happy path and failure-as-value path |
| Datasources | `:datasource:implementation` unit tests | |
| UI mappers, navigation mappers | `:features:<f>:ui` and `:app` unit tests | parameterized |
| Architecture rules | `:app` unit tests, `com.migrely.app.architecture` | Konsist 0.17.3 (skill templates) |

## 12. Quality gate and Definition of Done

```bash
./gradlew assembleDebug testDebugUnitTest ktlintCheck detekt
```

The same command runs in `.github/workflows/ci.yml`. A change is done when:

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
| Konsist rule adaptations | recorded here when made | — |
