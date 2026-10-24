# Migrely

Migraine tracker for Android: log an attack in three taps, keep every record private on the device, and bring a
doctor-ready report to appointments.

## Documents

| Document | Purpose |
|---|---|
| [`docs/BRD.md`](docs/BRD.md) | Business requirements, edge-case decisions and the release plan (Uzbek) |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Clean Architecture project profile: modules, bindings, naming, quality gate |
| [`openspec/`](openspec/) | Spec-driven change plans (OpenSpec): proposals, specs, designs and tasks |

## Build and verify

Requirements: JDK 17 or 21 and the Android SDK (API 36).

```bash
./gradlew assembleDebug testDebugUnitTest ktlintCheck detekt
```

This is the same quality gate CI runs (`.github/workflows/ci.yml`). It includes the Konsist architecture tests.

## Architecture

Strict Clean Architecture after Eran Boudjnah's *Clean Architecture for Android* and its sample
[`CleanArchitectureForAndroid`](https://github.com/EranBoudjnah/CleanArchitectureForAndroid). New features are scaffolded
with [`cag`](https://github.com/EranBoudjnah/CleanArchitectureGenerator); see `docs/ARCHITECTURE.md` section 4.
