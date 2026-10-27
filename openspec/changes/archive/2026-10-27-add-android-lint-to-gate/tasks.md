# Tasks

## 1. Gate

- [x] 1.1 Check first: `./gradlew lint` on `develop` (21f41d08) has 0 errors, so no baseline is needed
- [x] 1.2 Gate command with `lint` in `docs/ARCHITECTURE.md` section 12, `README.md` and `.github/workflows/ci.yml`
- [x] 1.3 Verify task in `openspec/config.yaml` names Android Lint
- [x] 1.4 BRD: Definition of Done, ADR-12 and D123

## 2. Verify

- [x] 2.1 Gate: `assembleDebug test ktlintCheck detekt lint`
- [x] 2.2 `openspec validate add-android-lint-to-gate --strict`

## Verification record

- **Lint before the change:** `./gradlew lint` on `develop` 21f41d08 passes. Every module reports 0 errors: 10 modules
  have no issues, and the others have 1 to 48 warnings. So no baseline is needed.
- **Gate:** `assembleDebug test ktlintCheck detekt lint` passes, and its tasks include each module's `lintDebug`.
  Only docs and config changed, so most tasks were up to date.
- **That a Lint error fails the gate:** AGP's default `abortOnError` is true. QA #74 showed it: `lint` failed on
  three errors.
- **`openspec validate add-android-lint-to-gate --strict`:** passes.
- **Not run:** CI. GitHub Actions cannot run while the account is locked for billing.
