# Tasks

Base: `bugfix/195-preview-activity-release` 24bdf68b (+ this plan). `Refs #195` (the lead retests and closes). Ships in the next normal release, not
1.2.1. Only `app/build.gradle.kts` and `docs/ARCHITECTURE.md` change in code and docs; the 11 `debugImplementation` lines from 24bdf68b stay.
Gradle under load: `nice -n 19 ./gradlew --max-workers=2 …`, never `--stop`. Use `set -o pipefail` for every piped check.

## 1. Plan

- [x] 1.1 BRD D172, §9.14 EC-REL-01…06, §13 row 41; this change. Verify: `grep -c "^| D172 " docs/BRD.md` prints 1, and
  `openspec validate fix-release-debug-components --strict` passes.

## 2. The guard (`app/build.gradle.kts`)

- [ ] 2.1 Replace `debugOnlyLibraries` with `debugOnlyComponents`: the three fully qualified names of design D4, one per line, each with a comment
  naming its source and BRD D172. Verify: `grep -c 'debugOnlyLibraries\|CheckDebugOnlyLibrariesTask\|DebugOnlyLibraries' app/build.gradle.kts`
  prints 0.
- [ ] 2.2 Replace `CheckDebugOnlyLibrariesTask` with a task that reads `SingleArtifact.MERGED_MANIFEST` (`@InputFile`), finds `activity`,
  `activity-alias`, `service`, `receiver` and `provider` elements whose `android:name` equals a list entry (design D3), fails with every match named
  and the fix ("debugImplementation", or keep it in `src/debug`), and otherwise writes the release component names to
  `build/reports/debug-components/<variant>.txt`. Verify: 5.2.
- [ ] 2.3 Register it as `check<Variant>DebugComponents` for every non-debug build type and make `assemble<Variant>` and `bundle<Variant>` depend on it,
  next to `check<Variant>Permissions` (design D2). Verify: `./gradlew :app:assembleRelease --dry-run` and `./gradlew :app:bundleRelease --dry-run`
  each list `:app:checkReleaseDebugComponents`; `./gradlew :app:assembleDebug --dry-run` does not list any `DebugComponents` task.

## 3. Docs

- [ ] 3.1 `docs/ARCHITECTURE.md` §4 step 4: keep the `debugImplementation` rule, name `:app:checkReleaseDebugComponents`, and say it fails the
  release build when the release merged manifest declares a listed debug-only component (issue #195, BRD D172). Verify:
  `grep -c 'checkReleaseDebugOnlyLibraries' docs/ARCHITECTURE.md` prints 0 and `grep -c 'checkReleaseDebugComponents' docs/ARCHITECTURE.md` prints 1.

## 4. Gate

- [ ] 4.1 Verify: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exit 0.
- [ ] 4.2 Verify: `./gradlew :app:checkReleasePermissions` exit 0, and `app/build/reports/permissions/release.txt` equals develop's (six entries,
  `ACCESS_NETWORK_STATE` the only network permission).

## 5. Release build and negative controls

- [ ] 5.1 Clean release: `./gradlew :app:assembleRelease :app:bundleRelease` exit 0 (debug key is fine, D102; the bundle needs
  `-Pmigrely.playLicenseKey`, any well-formed test key, never the real one in logs). Verify: the debug-components report has no listed name, and
  `bundletool dump manifest --bundle app/build/outputs/bundle/release/app-release.aab | grep -c 'PreviewActivity\|androidx.activity.ComponentActivity\|SimulatedBillingReceiver'`
  prints 0.
- [ ] 5.2 Negative controls, each a temporary edit in one feature UI module (for example `features/home/ui/build.gradle.kts`), reverted right after:
  - a. `implementation(libs.compose.ui.tooling)`: `assembleRelease` fails, message names `androidx.compose.ui.tooling.PreviewActivity`;
  - b. `implementation("androidx.compose.ui:ui-tooling-android:<the BOM's version>")`: `assembleRelease` and `bundleRelease` both fail, naming
    `PreviewActivity` (the case 24bdf68b missed);
  - c. `implementation(libs.compose.ui.test.manifest)`: `assembleRelease` fails, naming `androidx.activity.ComponentActivity`;
  - d. the `SimulatedBillingReceiver` `<receiver>` entry copied into a new `app/src/release/AndroidManifest.xml`: `assembleRelease` fails, naming it
    (if another release task such as lint fails first on the missing class, run `:app:checkReleaseDebugComponents` alone; delete the file
    afterwards).
  Verify: each run's exit code is non-zero and the failing task is `:app:checkReleaseDebugComponents` (quote the one error line); afterwards
  `git status --short` shows only the intended changes of tasks 2 and 3.
- [ ] 5.3 Debug keeps previews: after 4.1, `app/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml` declares
  `androidx.compose.ui.tooling.PreviewActivity`. Verify: `grep -c 'androidx.compose.ui.tooling.PreviewActivity'` on it prints 1 or more.
- [ ] 5.4 Independent review (`code-reviewer`) on `git diff origin/develop...HEAD`, briefed with this change and design Risks. Verify: findings and
  fixes recorded below.
- [ ] 5.5 `qa-engineer` acceptance, only this diff's reach (owner rule 2026-11-09): rerun 5.1 and one of 5.2 b or c on its own worktree; a smoke
  launch of the release build on `emulator-5580` shows the app starts. Verify: rows in QA's log.
- [ ] 5.6 Archive: `openspec archive fix-release-debug-components --yes`, then `openspec validate --specs --strict`. Verify: exit 0.

## Verification record

(filled by `android-engineer`)
