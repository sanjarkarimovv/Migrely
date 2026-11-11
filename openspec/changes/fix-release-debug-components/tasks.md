# Tasks

Base: `bugfix/195-preview-activity-release` 24bdf68b (+ this plan). `Refs #195` (the lead retests and closes). Ships in the next normal release, not
1.2.1. Only `app/build.gradle.kts` and `docs/ARCHITECTURE.md` change in code and docs; the 11 `debugImplementation` lines from 24bdf68b stay.
Gradle under load: `nice -n 19 ./gradlew --max-workers=2 …`, never `--stop`. Use `set -o pipefail` for every piped check.

## 1. Plan

- [x] 1.1 BRD D172, §9.14 EC-REL-01…06, §13 row 41; this change. Verify: `grep -c "^| D172 " docs/BRD.md` prints 1, and
  `openspec validate fix-release-debug-components --strict` passes.

## 2. The guard (`app/build.gradle.kts`)

- [x] 2.1 Replace `debugOnlyLibraries` with `debugOnlyComponents`: the three fully qualified names of design D4, one per line, each with a comment
  naming its source and BRD D172. Verify: `grep -c 'debugOnlyLibraries\|CheckDebugOnlyLibrariesTask\|DebugOnlyLibraries' app/build.gradle.kts`
  prints 0.
- [x] 2.2 Replace `CheckDebugOnlyLibrariesTask` with a task that reads `SingleArtifact.MERGED_MANIFEST` (`@InputFile`), finds `activity`,
  `activity-alias`, `service`, `receiver` and `provider` elements whose `android:name` equals a list entry (design D3), fails with every match named
  and the fix ("debugImplementation", or keep it in `src/debug`), and otherwise writes the release component names to
  `build/reports/debug-components/<variant>.txt`. Verify: 5.2.
- [x] 2.3 Register it as `check<Variant>DebugComponents` for every non-debug build type and make `assemble<Variant>` and `bundle<Variant>` depend on it,
  next to `check<Variant>Permissions` (design D2). Verify: `./gradlew :app:assembleRelease --dry-run` and `./gradlew :app:bundleRelease --dry-run`
  each list `:app:checkReleaseDebugComponents`; `./gradlew :app:assembleDebug --dry-run` does not list any `DebugComponents` task.

## 3. Docs

- [x] 3.1 `docs/ARCHITECTURE.md` §4 step 4: keep the `debugImplementation` rule, name `:app:checkReleaseDebugComponents`, and say it fails the
  release build when the release merged manifest declares a listed debug-only component (issue #195, BRD D172). Verify:
  `grep -c 'checkReleaseDebugOnlyLibraries' docs/ARCHITECTURE.md` prints 0 and `grep -c 'checkReleaseDebugComponents' docs/ARCHITECTURE.md` prints 1.

## 4. Gate

- [x] 4.1 Verify: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exit 0.
- [x] 4.2 Verify: `./gradlew :app:checkReleasePermissions` exit 0, and `app/build/reports/permissions/release.txt` equals develop's (six entries,
  `ACCESS_NETWORK_STATE` the only network permission).

## 5. Release build and negative controls

- [x] 5.1 Clean release: `./gradlew :app:assembleRelease :app:bundleRelease` exit 0 (debug key is fine, D102; the bundle needs
  `-Pmigrely.playLicenseKey`, any well-formed test key, never the real one in logs). Verify: the debug-components report has no listed name, and
  `bundletool dump manifest --bundle app/build/outputs/bundle/release/app-release.aab | grep -c 'PreviewActivity\|androidx.activity.ComponentActivity\|SimulatedBillingReceiver'`
  prints 0.
- [x] 5.2 Negative controls, each a temporary edit in one feature UI module (for example `features/home/ui/build.gradle.kts`), reverted right after:
  - a. `implementation(libs.compose.ui.tooling)`: `assembleRelease` fails, message names `androidx.compose.ui.tooling.PreviewActivity`;
  - b. `implementation("androidx.compose.ui:ui-tooling-android:<the BOM's version>")`: `assembleRelease` and `bundleRelease` both fail, naming
    `PreviewActivity` (the case 24bdf68b missed);
  - c. `implementation(libs.compose.ui.test.manifest)`: `assembleRelease` fails, naming `androidx.activity.ComponentActivity`;
  - d. the `SimulatedBillingReceiver` `<receiver>` entry copied into a new `app/src/release/AndroidManifest.xml`: `assembleRelease` fails, naming it
    (if another release task such as lint fails first on the missing class, run `:app:checkReleaseDebugComponents` alone; delete the file
    afterwards).
  Verify: each run's exit code is non-zero and the failing task is `:app:checkReleaseDebugComponents` (quote the one error line); afterwards
  `git status --short` shows only the intended changes of tasks 2 and 3.
- [x] 5.3 Debug keeps previews: after 4.1, `app/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml` declares
  `androidx.compose.ui.tooling.PreviewActivity`. Verify: `grep -c 'androidx.compose.ui.tooling.PreviewActivity'` on it prints 1 or more.
- [ ] 5.4 Independent review (`code-reviewer`) on `git diff origin/develop...HEAD`, briefed with this change and design Risks. Verify: findings and
  fixes recorded below.
- [x] 5.5 `qa-engineer` acceptance, only this diff's reach (owner rule 2026-11-09): rerun 5.1 and one of 5.2 b or c on its own worktree; a smoke
  launch of the release build on `emulator-5580` shows the app starts. Verify: rows in QA's log.
- [ ] 5.6 Archive: `openspec archive fix-release-debug-components --yes`, then `openspec validate --specs --strict`. Verify: exit 0.

## Verification record

(filled by `android-engineer`)

Round 2, 2026-11-11, on 959f3bb5 plus this change; logs in `migrely-work/195/`. Debug key only (no `keystore.properties` in the worktree);
the bundle used the public licence key already in `gradle.properties`.

| Task | Command | Result |
|---|---|---|
| 2.1 | `grep -c 'debugOnlyLibraries\|CheckDebugOnlyLibrariesTask\|DebugOnlyLibraries' app/build.gradle.kts` | 0 |
| 2.3 | `./gradlew :app:{assembleRelease,bundleRelease,assembleDebug} --dry-run \| grep -c DebugComponents` | 1, 1, 0 |
| 3.1 | `grep -c` old / new task name in `docs/ARCHITECTURE.md` | 0 / 1 |
| 4.1 | `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` | exit 0 (`r2-gate.log`); 467 suites, 3013 tests, 0 failures, 0 errors, 0 skipped. Unit tests were up to date: product code is unchanged since the round 1 gate on 24bdf68b, which ran them all |
| 4.2 | `:app:checkReleasePermissions`, `app/build/reports/permissions/release.txt` | exit 0; ACCESS_NETWORK_STATE, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, WAKE_LOCK, BILLING, DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION: same as develop's report, no INTERNET |
| 5.1 | `./gradlew :app:assembleRelease :app:bundleRelease :app:checkReleasePermissions` | exit 0. Forbidden names: AAB `bundletool dump manifest` 0, APK `aapt2 dump xmltree` 0, report `build/reports/debug-components/release.txt` 0. The report lists 24 components, the AAB manifest declares 24 |
| 5.2 a | home `implementation(libs.compose.ui.tooling)`, `assembleRelease` | exit 1 at `:app:checkReleaseDebugComponents`: "…debug-only components: [androidx.compose.ui.tooling.PreviewActivity]. Declare the library with debugImplementation, or keep the component in src/debug." |
| 5.2 b | home `implementation("androidx.compose.ui:ui-tooling-android:1.9.3")`, `assembleRelease`, then `bundleRelease` | both exit 1 at `:app:checkReleaseDebugComponents`, naming `PreviewActivity` |
| 5.2 c | home `implementation(libs.compose.ui.test.manifest)`, `assembleRelease` | exit 1, naming `androidx.activity.ComponentActivity` |
| 5.2 d | the receiver entry in a new `app/src/release/AndroidManifest.xml`, `assembleRelease` | exit 1, naming `com.migrely.app.datasource.billing.datasource.SimulatedBillingReceiver`; file deleted |
| 5.2 | `git status --short` after the controls | only `app/build.gradle.kts`, `docs/ARCHITECTURE.md` |
| EC-REL-05 | merged manifest deleted, then replaced by "not xml", running `:app:checkReleaseDebugComponents -x :app:processReleaseMainManifest` | missing: exit 1, "property 'mergedManifest' specifies file … which doesn't exist"; corrupt: exit 1, "Content is not allowed in prolog". Rebuilt clean afterwards (0 forbidden names) |
| CC | `--configuration-cache :app:checkReleaseDebugComponents :app:checkReleasePermissions`, twice | exit 0, "Configuration cache entry reused". `bundleRelease` with the cache reports one problem in the existing `checkPlayLicenseKey` (its `doLast` calls the script's `malformedKey`); this change does not touch it, and the project does not turn the cache on |
| 5.3 | `grep -c androidx.compose.ui.tooling.PreviewActivity` on the debug merged manifest | 1 |

Not checked: the EC-REL-06 `tools:node="remove"` case was not built (the merged manifest leaves removed nodes out, so they never reach the
check); a device walk (the shipped code equals 24bdf68b's walk on emulator-5570; only the build guard changed); the AAB signed with the upload key.

## QA record (5.5)

(filled by `qa-engineer`; full record `docs/qa/fix-release-debug-components.md`, evidence `migrely-work/qa-195/`)

**PASS on ceb6bb30, 2026-11-11.** R8 release APKs debug-signed (D102): candidate `fb41d716…`, v1.2.1 control `7f41f34c…` (entries equal to
the 1.2.1 GO APK, so R8 is deterministic); debug APK `1455aad5…` byte-identical to v1.2.1's.

| Row | Result |
|---|---|
| 5.1 rerun (QA worktree `assembleRelease`) | exit 0; `debug-components/release.txt` 24 names, 0 listed; permissions unchanged (6, no INTERNET); `qa/release_apk.py` passes |
| 5.2 b/c | not rerun: waived by the lead's brief (engineer round 2 and code-reviewer proved a–d); QA's control is the device row below |
| `am start` PreviewActivity, candidate R8 | "does not exist" 2/2 on API 36, 2/2 on API 26 |
| Control, v1.2.1 R8 | process started for PreviewActivity 2/2 + 2/2, then "Application is not debuggable", closes |
| Debug build | PreviewActivity, ComponentActivity, SimulatedBillingReceiver declared; PreviewActivity resumed on 36 and 26 |
| Release classpath | only M2 `material` and `ui-tooling`/`ui-tooling-data` 1.9.3 removed; no other version moved; keep rules differ only by PreviewActivity's |
| Full regression on R8, API 36 | 56/56 (3 simulated-store flows skipped); monkey 5000 events 0 crash 0 ANR |
| Smoke on R8, API 26 | 15/15 (2 geometry flows skipped) |
| Update v1.2.1 R8 → candidate R8, app not opened | 36 and 26: DB v7 and every table equal, "Did it help?" and daily notifications post, kept flow passes; 36: v1.2.1 backup imports equal; launcher shortcut labels resolve after 486 string IDs shifted |

Not proved: Premium and the simulated-store flows on R8, the full regression on API 26, the AAB (engineer's 5.1), the upload key, real
phones, an update from versions other than v1.2.1.
