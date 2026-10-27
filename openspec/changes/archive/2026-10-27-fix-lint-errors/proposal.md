# Proposal

## Why

QA #74: `./gradlew lint` fails with 3 errors. Lint was new to the Go/No-Go for build 4, which got a stated exception
(D204). From the next candidate, 1.0.1, a lint error makes the candidate No-Go. None of the three crashes the app.

- **NewApi ×2 in `AppInfoDataSourceImpl`:** `getPackageInfo(String, PackageInfoFlags)` needs API 33. The call is guarded
  by `sdkInt() >= TIRAMISU`, but through an injected lambda that lint cannot read.
- **UnrememberedMutableState in `TapGuardTest`:** a state created without `remember`, in test code.

## What Changes

- **`AppInfoDataSourceImpl`:** the check moves into `isAtLeastTiramisu()`, annotated `@ChecksSdkIntAtLeast(api =
  TIRAMISU)`, so lint sees the guard. The injected `sdkInt` stays for the tests.
- **`TapGuardTest`:** `remember { mutableStateOf(false) }`.

**Unchanged:** behaviour on every API level.

**Out of scope:** adding `lint` to the gate and the Definition of Done. That is the rest of D123, on develop.

## Capabilities

None changed.

## Impact

- `:datasource:implementation`: `AppInfoDataSourceImpl.kt`.
- `:designsystem` (androidTest): `TapGuardTest.kt`.
