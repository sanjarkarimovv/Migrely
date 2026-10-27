# Tasks

## 1. Fix

- [x] 1.1 `AppInfoDataSourceImpl`: `@ChecksSdkIntAtLeast` check function
- [x] 1.2 `TapGuardTest`: `remember` the chip's state

## 2. Verify

- [x] 2.1 `./gradlew lint`: 0 errors
- [x] 2.2 Gate: `assembleDebug test ktlintCheck detekt`
- [x] 2.3 `AppInfoDataSourceImplTest` (unit) and `:designsystem:connectedDebugAndroidTest` on emulator-5570

## Verification record

- **Lint:** before, 3 errors (QA #74). After, `./gradlew lint --continue` gives BUILD SUCCESSFUL with no error (3 min
  24 s).
- **Instrumented:** `:designsystem:connectedDebugAndroidTest` on emulator-5570 passes 37/37, including TapGuardTest.
- **Gate and unit tests:** run with the 1.0.1 build in the next commit. `AppInfoDataSourceImplTest` covers both API
  branches through the injected `sdkInt`.
