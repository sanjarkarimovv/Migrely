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
./gradlew assembleDebug test ktlintCheck detekt
```

This is the same quality gate CI runs (`.github/workflows/ci.yml`). It includes the Konsist architecture tests.

## Quality checks

End-to-end flows (Maestro), the accessibility audit, device scenarios and the release Go/No-Go list run locally on
the QA emulators. See [docs/QA.md](docs/QA.md).

```bash
qa/maestro.sh --tags smoke      # needs emulator-5580 (AVD Migrely_QA_API_36) and the Maestro CLI
```

## Architecture

Strict Clean Architecture after Eran Boudjnah's *Clean Architecture for Android* and its sample
[`CleanArchitectureForAndroid`](https://github.com/EranBoudjnah/CleanArchitectureForAndroid). New features are scaffolded
with [`cag`](https://github.com/EranBoudjnah/CleanArchitectureGenerator); see `docs/ARCHITECTURE.md` section 4.

## Branching and releases

The repository follows git-flow.

| Branch | Cut from | Merges into | Holds |
|---|---|---|---|
| `main` | | | Released code only. Every merge into it is tagged `v<version>` |
| `develop` | `main` | | The next release. It is the default branch, and feature pull requests target it |
| `feature/<change>` | `develop` | `develop` | One OpenSpec change, named after it, for example `feature/add-attack-history` |
| `release/<version>` | `develop` | `main` and `develop` | The version bump and release fixes only, for example `release/1.0.0` |
| `hotfix/<version>` | `main` | `main` and `develop` | An urgent fix to a released version, for example `hotfix/1.0.1` |

- A hotfix merges into the open `release/*` branch instead of `develop` while one exists.
- Pull requests merge with a merge commit, never a squash or a rebase, so each change stays one unit in the history.
  Merged branches are deleted.
- `main` and `develop` refuse force-pushes and deletion.
- Before a feature merges, the quality gate above and the instrumented tests of the touched modules pass, and its
  OpenSpec change is archived on the feature branch.
- Versions follow [Semantic Versioning](https://semver.org): `versionName` matches the tag, and `versionCode` grows by
  one with every release.
