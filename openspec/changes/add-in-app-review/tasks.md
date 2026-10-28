# Tasks

## 1. Domain

- [x] 1.1 `:features:review:domain` (scaffolded with `cag`, build files as `features/home/*`):
  - models: `ReviewMarksDomainModel`, `ReviewFlowResultDomainModel`;
  - the eight single-function repositories;
  - `ShouldRequestReviewUseCase` with the D130 constants, and `RequestReviewUseCase`;
  - tests for every rule and edge (EC-REV-01…12).
  Verify: `./gradlew :features:review:domain:test`.

## 2. Presentation

- [x] 2.1 `ReviewPromptViewModel` and `ReviewPromptPresentationNotification`, with tests: one question at a time,
  launched once, never again after a launch, and asked again after a "no".
  Verify: `./gradlew :features:review:presentation:test`.

## 3. Datasources and data

- [x] 3.1 `ReviewDataSource`, `ReviewFlowDataModel` and `PlayReviewDataSourceImpl` with `PlayReviewTimings`. Tests:
  completed; request failed; launch failed; no activity; request timeout; thrown exception.
  Verify: `./gradlew :datasource:implementation:testDebugUnitTest`.
- [x] 3.2 Preferences: `reviewRequests()` and `markReviewRequested()`, with tests, including that `clear()` erases
  them.
- [x] 3.3 `AppInfoDataSource.previousExitCrashed()`, with tests: crash, native crash, ANR, a normal exit, none,
  Android 10, and an exception.
- [x] 3.4 `:features:review:data`: `ReviewRepository` and `ReviewFlowDomainMapper`, with tests.
  Verify: `./gradlew :features:review:data:test`.

## 4. UI and home

- [x] 4.1 `:features:review:ui`: `ReviewPromptDependencies` and `ReviewPrompt` (the RESUMED-gated 2-second settle).
- [x] 4.2 Home: the `reviewPrompt` slot, drawn only in today's mode.
  Verify: `./gradlew :features:home:ui:testDebugUnitTest`.

## 5. App wiring

- [x] 5.1 The version catalog `play-review`; `settings.gradle.kts`; `:app` dependencies; `ReviewModule` (with a
  singleton view model); `AppNavHost` fills the slot; `AppInfoDataSourceModule` passes the `ActivityManager`.
- [x] 5.2 The permission check: `assembleDebug assembleRelease` pass, and a merged manifest without INTERNET.

## 6. Docs

- [x] 6.1 `docs/ARCHITECTURE.md`: the review feature, the slot and the datasource package.
- [x] 6.2 Privacy policy (md and html): the rating sentence (design D6).

## 7. Verify

- [ ] 7.1 Gate: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`; instrumented tests
  of `:datasource:implementation`.
- [ ] 7.2 Emulator walk on this session's own emulator: every scenario that a device can show. The marks are read
  from `migrely_preferences`, and the previous exit is made with `am crash`.
- [ ] 7.3 Independent review (`superpowers:code-reviewer`); findings fixed and recorded.
- [ ] 7.4 Verification record below.
