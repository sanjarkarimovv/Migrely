# Tasks

## 1. Project identity and build configuration

- [x] 1.1 Set `minSdk = "26"` in `gradle/libs.versions.toml`, the app label to "Migrely" and `versionName` to "0.1.0"; verify `aapt dump badging` on the debug APK shows `sdkVersion:'26'`, `application-label:'Migrely'` and `package: name='com.migrely.app'`
- [x] 1.2 Add the Kotlin serialization plugin and library to the version catalog and `:app` (needed by type-safe routes); verify `./gradlew :app:assembleDebug` succeeds
- [x] 1.3 Remove any `INTERNET` or runtime permission from the app and library manifests; verify `aapt dump permissions` on the debug APK lists no `INTERNET` and no runtime (dangerous) permission. The AndroidX-injected signature permission `com.migrely.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` is expected: it is app-internal, never prompted, and removing it breaks `ContextCompat.registerReceiver` below API 33

## 2. Project profile

- [x] 2.1 Write `docs/ARCHITECTURE.md` covering module layout (D3), navigation binding (D2), network binding (none), async failure routing (D4), event collection (D5), UI state holder (D6), naming extras, lint gate (D9), test libraries and known deviations from the sample; verify every item of the skill's Step 0 list has a section
- [x] 2.2 Update `docs/BRD.md`: ADR-03 to Nav2 with the D1 upgrade follow-up, the section 12 module map to the D3 layout, and add the `upgrade-toolchain` change to the section 13 plan; verify `grep -n "Nav3\|:datasource:<x>" docs/BRD.md` shows no stale statements

## 3. Replace the sample feature with the home feature

- [x] 3.1 Delete `features/samplefeature`, its entries in `settings.gradle.kts`, its `:app` dependencies and `SampleFeatureModule`; verify `./gradlew projects` lists no `samplefeature` module
- [x] 3.2 Generate the home feature with `cag --new-feature --name=Home --package=com.migrely.app.home --ktlint --detekt` (`--package` is required, see `docs/ARCHITECTURE.md` section 4) and delete any generated stub models, use cases and repositories; verify `./gradlew :features:home:domain:build :features:home:presentation:build :features:home:data:build :features:home:ui:build` succeeds
- [x] 3.3 Implement `HomeViewState` (`Loading`, `Ready`) and `HomeViewModel.onEnter()` in `:features:home:presentation`, with a `HomeViewModelTest` based on `BaseViewModelTest`; verify `./gradlew :features:home:presentation:test` passes
- [x] 3.4 Implement `HomeScreen` (stateful `Home()` plus stateless `HomeContents()`) showing the app name and a subtitle from string resources, with `HomeViewStateUiModel`, `HomeViewStateUiMapper` (parameterized test), a `@Preview` per state and `HomeDependencies`; verify `./gradlew :features:home:ui:build` succeeds

## 4. App shell: navigation, DI and activity

- [x] 4.1 Align `BaseComposeHolder` with `bindings/nav2.md` (`ViewModelObserver(navController)` collecting navigation events and notifications in `LaunchedEffect`), and `UiDestination` to take a `NavController`; verify `./gradlew :architecture:ui:build` succeeds
- [x] 4.2 Add `Routes.kt` (`@Serializable object Home`), `AppNavHost`, `AppNavHostDependencies` and `HomeNavigationEventDestinationMapper` with a mapper test (home has no navigation events yet, so the test covers the unhandled-event path; parameterized cases arrive with the first event in `add-attack-logging`); verify `./gradlew :app:testDebugUnitTest --tests '*NavigationEventDestinationMapper*'` passes
- [x] 4.3 Add Hilt modules `HomeModule` (ViewModel, presentation mappers) and `HomeUiModule` (UI mappers, `HomeDependencies`); verify `./gradlew :app:assembleDebug` succeeds (Hilt graph compiles)
- [x] 4.4 Make `MainActivity` an `@AndroidEntryPoint` that hosts `AppNavHost` inside the theme, edge-to-edge; verify the app launches on the `Medium_Phone_API_36.0` emulator to the home screen and stays there after rotation

## 5. Theme

- [x] 5.1 Replace the generated theme with the D7 palette: dark scheme always, dynamic colour off, no `Color.White` anywhere in `ui/theme`, dark system bars and a dark window/splash background (no white flash); verify `grep -rn "Color.White\|0xFFFFFFFF" app/src/main` finds nothing in app code (the generated launcher icon keeps `#FFFFFF` until the real icon in `prepare-release`) and the emulator in system light mode still shows the dark theme

## 6. Architecture tests

- [x] 6.1 Add Konsist 0.17.3 to the catalog and `:app` test dependencies, copy the skill's six `tools/konsist/*.kt` tests to `app/src/test/java/com/migrely/app/architecture/` with the package fixed, and adapt regexes only as the skill README allows (record any adaptation in `docs/ARCHITECTURE.md`); verify `./gradlew :app:testDebugUnitTest --tests '*Architecture*'` passes

## 7. Continuous integration

- [x] 7.1 Add `.github/workflows/ci.yml` running the D9 gate on push and pull request (Temurin JDK 17, Gradle cache); verify the workflow file parses (`python3 -c "import yaml,sys; yaml.safe_load(open('.github/workflows/ci.yml'))"`) and runs the same command used in 8.1

## 8. Integration verification

- [x] 8.1 Run `./gradlew assembleDebug testDebugUnitTest ktlintCheck detekt`; verify it ends with BUILD SUCCESSFUL and record the output summary
- [x] 8.2 Walk the `app-shell` spec scenarios on the emulator: cold start shows home with the app name, rotation keeps home, system light mode still shows the dark theme, no permission dialog on first launch; verify each scenario and note the result (API 26 install is covered by the `sdkVersion:'26'` check in 1.1, since no API 26 system image is installed)
- [x] 8.3 Run `openspec validate setup-project-foundation --strict`; verify it reports the change as valid

## Workflow follow-up

- Review the change with the owner, then commit it (attribution per the session's git instructions).
- Archive with `/opsx:archive` so `openspec/specs/app-shell/spec.md` becomes the main spec.
