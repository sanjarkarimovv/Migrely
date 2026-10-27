# Proposal

## Why

The gate is `./gradlew assembleDebug test ktlintCheck detekt`. Android Lint was not in it, so three Lint errors reached
the 1.0.0 release branch unnoticed (QA #74). QA found them only by running `lint` by hand for its Go/No-Go list, and
1.0.1's release candidate had to be rebuilt for them. Since hotfix 1.0.1, `develop` has 0 Lint errors, so Lint can join
the gate now without a baseline (BRD D123).

## What Changes

- **Gate:** `./gradlew assembleDebug test ktlintCheck detekt lint`. Lint's defaults stay: an error fails the build,
  and a warning is only reported.
- **Where the gate is written:**
  - `docs/ARCHITECTURE.md` section 12;
  - `README.md`;
  - `.github/workflows/ci.yml` (it still cannot run while the account is locked for billing);
  - the verify task in `openspec/config.yaml`;
  - the BRD's Definition of Done, plus D123.

**Unchanged:**
- the Lint checks and their severities: no `lint.xml`, no baseline and no `warningsAsErrors`;
- ktlint and detekt;
- `docs/QA.md`. QA owns it, and its Go/No-Go list already runs `lint` on every release candidate (line 1a).

**Out of scope:** the Lint warnings. They are reported, and fixing them is separate work.

## Capabilities

None changed. This is a build and process change, with no app behavior.

## Impact

Docs, `openspec/config.yaml` and `.github/workflows/ci.yml`. No module changes. A gate run takes longer by the
time of `lint`.
