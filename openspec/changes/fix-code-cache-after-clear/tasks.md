# Tasks

Base: `develop` ad422d7f, branch `bugfix/193-code-cache-after-clear`. `Refs #193`, `Refs #189` (QA retests and closes). Ships in 1.2.1. Only the
`code_cache` mark fix (BRD D170 (3a)); no reminder, receiver or manifest change (D170 (3b)).

## 1. Plan and source check

- [x] 1.1 BRD D170, EC-STO-06…10, EC-REM-22, EC-REM-23, §13 row 39; this change. Verify: `grep -c "^| D170 " docs/BRD.md` prints 1, and
  `openspec validate fix-code-cache-after-clear --strict` passes.
- [ ] 1.2 Read API 26's `ContextImpl.getCodeCacheDir` and `ensurePrivateCacheDirExists` (`sdkmanager "sources;android-26"`, or AOSP `oreo-release`)
  and record whether it writes `user.inode_code_cache` when it creates the folder. Verify: the quoted lines and the file path are in the packet; if it
  does not write the mark, design D2 is in scope.

## 2. Tests first (JVM)

- [ ] 2.1 `CodeCacheMark` tests over a fake `CodeCacheFolder` (design D3): ensure is called; every throwing step is caught; and, if D2 is in scope, no
  mark → no write, equal → no write, different → one write of the folder's inode, stat failure → no write. Verify: `./gradlew :app:testDebugUnitTest
  --tests '*CodeCacheMark*'` fails first (no class), then passes.

## 3. Platform and wiring

- [ ] 3.1 The Android `CodeCacheFolder` (`Context.codeCacheDir`; for D2, `Os.getxattr`, `Os.stat`, `Os.setxattr`, 8 bytes native order) and
  `CodeCacheMark` in `:app`. Verify: 2.1 passes; Konsist (`./gradlew :app:testDebugUnitTest --tests '*Architecture*'`) passes.
- [ ] 3.2 Call it from `MigrelyApplication.attachBaseContext` after `super`, or from the first line of `onCreate` (design D3). Verify: `git diff
  origin/develop -- app/src/main/AndroidManifest.xml app/src/main/java/com/migrely/app/reminder` is empty.
- [ ] 3.3 Instrumented test on `emulator-5570` (API 36): delete `codeCacheDir`, run the check, then `Os.getxattr(dataDir, …)` equals `Os.stat
  (codeCacheDir).st_ino`. Verify: `ANDROID_SERIAL=emulator-5570 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments
  .class=<the test>` passes; with the call removed it fails (negative control, then put it back).

## 4. Docs

- [ ] 4.1 KDoc on the new class cites BRD D170 and EC-STO-06. No release notes here: they are written on `release/1.2.1`, where the 1.2.0 block is
  already 495 of 500 characters, so it needs trimming and a `medical-reviewer` recheck. Verify: `git diff --stat origin/develop` shows no `whats-new`
  file.

## 5. Verify

- [ ] 5.1 The gate. Verify: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exit 0 (with `set -o pipefail`).
- [ ] 5.2 No new permission. Verify: `./gradlew :app:checkReleasePermissions` exit 0.
- [ ] 5.3 Device walk on `emulator-5570` (API 36), debug build: `pm clear com.migrely.app`; launch; then the inode of `code_cache` (`adb shell run-as
  com.migrely.app stat -c %i code_cache`) and the mark (`getfattr -n user.inode_code_cache -e hex .` if toybox has it, else the test from 3.3)
  match, and the app opens normally. Negative control: the develop build shows no mark after the same clear and reopen (QA saw none in 4 of 4).
  Restore any device setting changed.
- [ ] 5.4 Independent review (`code-reviewer`) on `git diff origin/develop...HEAD`, briefed with this change and the Risks in `design.md`. Verify:
  findings and fixes recorded below.
- [ ] 5.5 `qa-engineer` acceptance (only this diff's reach, owner rule 2026-11-09), on R8 builds:
  - API 26 (`emulator-5582`), churn1: `migrely.db` kept 3 of 3 with the fix build; negative control 1.2.0 R8 deletes it (today 3 of 3);
  - API 26, plain clear: WorkManager's database and the planned reminder jobs are kept;
  - API 36 (`emulator-5580`): the same arms as a control, everything kept, mark true after a reopen;
  - the residual: a clear and reopen on 1.1.1 R8, then an update to the fix build, recorded as not protected (EC-STO-10).
  Verify: rows in QA's log and the 1.2.1 Go/No-Go.
- [ ] 5.6 Archive: `openspec archive fix-code-cache-after-clear --yes`, then `openspec validate --specs --strict`. Verify: exit 0.

## Verification record

(filled by `android-engineer`)
