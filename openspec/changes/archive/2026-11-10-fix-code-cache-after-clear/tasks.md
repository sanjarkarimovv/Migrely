# Tasks

Base: `develop` ad422d7f, branch `bugfix/193-code-cache-after-clear`. `Refs #193`, `Refs #189` (QA retests and closes). Ships in 1.2.1. Only the
`code_cache` mark fix (BRD D170 (3a)); no reminder, receiver or manifest change (D170 (3b)).

## 1. Plan and source check

- [x] 1.1 BRD D170, EC-STO-06…10, EC-REM-22, EC-REM-23, §13 row 39; this change. Verify: `grep -c "^| D170 " docs/BRD.md` prints 1, and
  `openspec validate fix-code-cache-after-clear --strict` passes.
- [x] 1.2 Read API 26's `ContextImpl.getCodeCacheDir` and `ensurePrivateCacheDirExists` (`sdkmanager "sources;android-26"`, or AOSP `oreo-release`)
  and record whether it writes `user.inode_code_cache` when it creates the folder. Verify: the quoted lines and the file path are in the packet; if it
  does not write the mark, design D2 is in scope.

## 2. Tests first (JVM)

- [x] 2.1 `CodeCacheMark` tests over a fake `CodeCacheFolder` (design D3): ensure is called; every throwing step is caught; and, if D2 is in scope, no
  mark → no write, equal → no write, different → one write of the folder's inode, stat failure → no write. Verify: `./gradlew :app:testDebugUnitTest
  --tests '*CodeCacheMark*'` fails first (no class), then passes.

## 3. Platform and wiring

- [x] 3.1 The Android `CodeCacheFolder` (`Context.codeCacheDir`; for D2, `Os.getxattr`, `Os.stat`, `Os.setxattr`, 8 bytes native order) and
  `CodeCacheMark` in `:app`. Verify: 2.1 passes; Konsist (`./gradlew :app:testDebugUnitTest --tests '*Architecture*'`) passes.
- [x] 3.2 Call it from `MigrelyApplication.attachBaseContext` after `super`, or from the first line of `onCreate` (design D3). Verify: `git diff
  origin/develop -- app/src/main/AndroidManifest.xml app/src/main/java/com/migrely/app/reminder` is empty.
- [x] 3.3 Instrumented test on `emulator-5570` (API 36): delete `codeCacheDir`, run the check, then `Os.getxattr(dataDir, …)` equals `Os.stat
  (codeCacheDir).st_ino`. Verify: `ANDROID_SERIAL=emulator-5570 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments
  .class=<the test>` passes; with the call removed it fails (negative control, then put it back).

## 4. Docs

- [x] 4.1 KDoc on the new class cites BRD D170 and EC-STO-06. No release notes here: they are written on `release/1.2.1`, where the 1.2.0 block is
  already 495 of 500 characters, so it needs trimming and a `medical-reviewer` recheck. Verify: `git diff --stat origin/develop` shows no `whats-new`
  file.

## 5. Verify

- [x] 5.1 The gate. Verify: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exit 0 (with `set -o pipefail`).
- [x] 5.2 No new permission. Verify: `./gradlew :app:checkReleasePermissions` exit 0.
- [x] 5.3 Device walk on `emulator-5570` (API 36), debug build: `pm clear com.migrely.app`; launch; then the inode of `code_cache` (`adb shell run-as
  com.migrely.app stat -c %i code_cache`) and the mark (`getfattr -n user.inode_code_cache -e hex .` if toybox has it, else the test from 3.3)
  match, and the app opens normally. Negative control: the develop build shows no mark after the same clear and reopen (QA saw none in 4 of 4).
  Restore any device setting changed.
- [x] 5.4 Independent review (`code-reviewer`) on `git diff origin/develop...HEAD`, briefed with this change and the Risks in `design.md`. Verify:
  findings and fixes recorded below.
- [x] 5.5 `qa-engineer` acceptance (only this diff's reach, owner rule 2026-11-09), on R8 builds:
  - API 26 (`emulator-5582`), churn1: `migrely.db` kept 3 of 3 with the fix build; negative control 1.2.0 R8 deletes it (today 3 of 3);
  - API 26, plain clear: WorkManager's database and the planned reminder jobs are kept;
  - API 36 (`emulator-5580`): the same arms as a control, everything kept, mark true after a reopen;
  - the residual: a clear and reopen on 1.1.1 R8, then an update to the fix build, recorded as not protected (EC-STO-10).
  Verify: rows in QA's log and the 1.2.1 Go/No-Go.
- [x] 5.6 Archive: `openspec archive fix-code-cache-after-clear --yes`, then `openspec validate --specs --strict`. Verify: exit 0.

## Verification record

(filled by `android-engineer`, 2026-11-10, branch `bugfix/193-code-cache-after-clear`, code at a026f241)

**1.2 API 26 source.** `sdkmanager "sources;android-26"` (sources-26_r01), file
`~/Library/Android/sdk/sources/android-26/android/app/ContextImpl.java`. `getCodeCacheDir()` (l. 651–657) calls
`ensurePrivateCacheDirExists(mCodeCacheDir, XATTR_INODE_CODE_CACHE)`, and `ensurePrivateDirExists` (l. 558–587), when
`!file.exists()`, does `Os.mkdir`, then:

```java
final StructStat stat = Os.stat(file.getAbsolutePath());
final byte[] value = new byte[8];
Memory.pokeLong(value, 0, stat.st_ino, ByteOrder.nativeOrder());
Os.setxattr(file.getParentFile().getAbsolutePath(), xattr, value, 0);
```

So API 26 writes the mark when it creates the folder, as API 29 and 36 do. **D2 is out of scope**; 3.3 and 5.3 found no
case of an existing folder with a wrong mark.

**Code.** `app/.../storage/CodeCacheMark.kt` (catches any `Exception`, logs only `code_cache check failed: <kind>`),
`CodeCacheFolder.kt` (seam), `ContextCodeCacheFolder.kt` (`context.codeCacheDir`), called from
`MigrelyApplication.attachBaseContext` after `super`, with the base context. Deviation from design D3: `keep()` returns
`Unit`. The failure result is the one warning line, which the JVM tests read, because no caller acts on a result.

**Tests.**
- 2.1 red: `:app:testDebugUnitTest --tests '*CodeCacheMark*'` exit 1, "Unresolved reference 'CodeCacheMark'".
  Green: 5/5 `CodeCacheMarkTest` (ensure called once and nothing logged; IllegalStateException, SecurityException and
  IOException caught with only the kind logged; a message with a path and a drug name never reaches the log). Konsist
  17/17.
- 3.3 `ANDROID_SERIAL=emulator-5570 ./gradlew :app:connectedDebugAndroidTest
  -Pandroid.testInstrumentationRunnerArguments.class=com.migrely.app.storage.ContextCodeCacheFolderTest`. After review
  (Minor 2) the missing-folder test first writes a stale mark from the app process (the data folder's own inode, which
  `code_cache` can never take), asserts it is there, then asserts after `keep()` that the mark changed and equals the
  new folder's inode. The existing-folder test checks that inode, a file inside and the mark are unchanged. An `@After`
  re-creates the folder and writes the true mark, so a failure never leaves 5570 with no `code_cache` and a stale mark.
  Exit 0, 2/2. Negative control, `ensure()` emptied: exit 1, 1 of 2 failed, on the mark assertion itself
  (`ContextCodeCacheFolderTest.kt:48`, `assertNotEquals`: "Values should be different. Actual: 591201"). The
  existing-folder test passes either way, correctly (installd made the folder at install). Call put back: exit 0, 2/2.
  (The first version, at a026f241, had no stale mark: its control failed on `isDirectory`, not on the mark.)
- 5.1 `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` (pipefail): exit 0; 466 suites,
  3009 tests, 0 failures, 0 errors, 0 skipped (debug and release unit-test variants).
- 5.2 `:app:checkReleasePermissions` exit 0. `git diff origin/develop -- app/src/main/AndroidManifest.xml
  app/src/main/java/com/migrely/app/reminder` is empty. No `whats-new` file in the diff.

**5.3 walk, emulator-5570 (API 36).** Script and output: `migrely-work/walks/193/` (`walk.py`, `*.out`). Mark read as
root with `getfattr --only-values -n user.inode_code_cache <data> | od -tu8` (toybox has no `-e hex`). toybox prints the value up to
its first zero byte (QA, `migrely-work/qa-193/HANDOVER.md`) and `od` pads with zeros, so the reads are right for every
inode seen here (591201–591239, `0x0905xx`: no zero byte below the top).

| Arm | Fix debug a026f241 | Fix R8 a026f241 (debug-signed) | Develop control `debug-9d453bb5` (code == develop) |
|---|---|---|---|
| Fresh install, launch | mark == code_cache | mark == code_cache | mark == code_cache |
| `pm clear` | folder and mark gone | folder and mark gone | folder and mark gone |
| Launch after clear | **MATCH**, MainActivity resumed | **MATCH**, onboarding shown (screenshot) | code_cache absent, no mark; `databases/` took the old inode |
| Clear, then start by explicit `TIME_SET` broadcast, no activity | **MATCH** | **MATCH** | absent, no mark |
| `kill -9`, relaunch | folder inode and mark unchanged | unchanged | — |
| Stale mark (old inode put back after clear, as on 8.0), launch, `install -r` | mark == code_cache 4/4 (1 full-arm run + 3), but `code_cache` took back the exact marked inode (591205) each time, so this shows **no repair**: the mark would match with no write. No "Mismatched", `migrely.db` kept | **real overwrite** 1/1: mark 591203 → 591212 (the new `code_cache`); `databases/` did not take the old inode (591215); no "Mismatched", `migrely.db` kept | mark stays stale 3/3, `databases/` took it 3/3, installd "Mismatched … overwriting" 3/3, `migrely.db` kept |
| `code_cache check failed` warnings / crashes | 0 / 0 | 0 / 0 | 0 / 0 |

What each arm shows, and what it does not:
- Launch after clear and the receiver start: on API 36 the clear already removed the mark, so nothing was stale. These
  arms show only that the fix re-creates `code_cache` and writes a mark where there was none (develop leaves both
  absent). In the R8 plain run `databases/` took the freed inode (591205) and the mark named the new `code_cache`
  (591203). With no mark before, that is not design D1's "order does not matter" case.
- Stale arm, fix debug (4 runs): no repair proven, because the mark already named the inode that `code_cache` got back.
- Stale arm, fix R8 (1 run): the one real overwrite of a stale mark on a device (591203 → 591212). `databases/` did not
  take the old inode in that run, so D1's ordering case was still not shown on a device.
- Instrumented test 3.3 (after review): a stale mark written from the app process is replaced by the new folder's
  inode, and its negative control fails on that assertion. That is the deterministic repair proof on API 36.
- Develop stale arm: **API 36's installd does not delete by a stale mark**. It re-creates `code_cache` by name and
  overwrites the mark, so 5570 cannot reproduce the deletion.
- The R8 dex still calls `getCodeCacheDir`.

The proof that the repair prevents the deletion on API 26 is QA's (5.5).

**Not checked here:** API 26 at all (no device for this role; 5.5); Android 8.1–15; an exception on a real device (the
catch is JVM-tested only); a work profile or second user; a start from a WorkManager job or a provider (only an
activity and a receiver); timing of the extra syscalls (not measured; it adds no thread and no I/O beyond one `stat`,
and a `mkdir` plus `setxattr` only when the folder is missing); the real upload-signed AAB.

**Device state.** 5570 was off at the start with a broken `com.migrely.app` record (vc9, no APK on disk, empty data).
After the walk: fix debug build installed, `baseline12` restored with `walks/fix66/restore.sh` (dump md5
c3cfd32487171b7886baf9725dce7594), mark == code_cache, no setting changed (font 1.0, auto-rotate 1, auto_time 1), adbd
unrooted, emulator off. Same again after the review fixes: the connected test uninstalled the app, so the debug build
(main code unchanged since a026f241) and `baseline12` were put back (md5 c3cfd324…), mark == code_cache, adbd unrooted,
emulator off. After a cold boot `pm path` printed nothing for a while although the data was there; the connected test
reinstalled cleanly.

**5.4 review.** `code-reviewer` on `origin/develop...93f8c00f`: no Critical or Important findings, four Minors. Fixed
on this branch with no `app/src/main` change, so QA's R8 rows on 93f8c00f stand (`git diff 93f8c00f 62185b46 --
app/src/main` is empty):
- 1: the walk wording above now says what each arm shows (62185b46);
- 2: the instrumented test writes a stale mark and its negative control fails on the mark assertion (8c9e3d96);
- 3: design D3 says `keep()` returns `Unit` (62185b46).
Minor 4 was left unfixed by the lead's decision (fix 1–3 only). The lead gate on 62185b46: exit 0, permissions the same as 1.2.0.

**5.5 QA acceptance.** By `qa-engineer` on R8 `r8-93f8c00f` (sha256 4876b65cb708f98c…, debug-signed R8 like the
controls), against the 1.2.0 control `r8-9d453bb5` and the 1.1.1 residual base `r8-eeb6`. Evidence:
`migrely-work/qa-193/` (`rows.jsonl`, `count.py`, `HANDOVER.md`); comments #193 issuecomment-6766525545 and #189
issuecomment-6766526120. The lead recounted the rows by APK hash.
- API 26 (emulator-5582), fix: churn1 kept 3/3; hold (forced dangerous state, mark rewritten old → new) kept 3/3; plain
  kept 3/3 (`migrely.db`, WorkManager's database, WorkSpec rows and jobs); first start by a receiver kept 2/2; no clear
  kept 2/2; the timed daily reminder was posted on time 1/1. churn1 kept because `code_cache` took the old inode back, so
  it proves existence, not the write; hold, plain and receiver prove the rewrite.
- API 26, 1.2.0 control: lost 3/3 (churn1, `migrely.db`), 2/2 (hold), 3/3 (plain, WorkManager's database), 1/1
  (receiver) and 1/1 (timed: no reminder).
- API 36 (emulator-5580), fix: everything kept, mark true after every reopen. 1.2.0 control: kept, no mark after reopen.
- Residual EC-STO-10 (clear and reopen on 1.1.1, then update to the fix): `migrely.db` lost 3/3, as expected. A second
  update lost nothing 3/3.
- FATAL 0, ANR 0, "code_cache check failed" 0.
- The 1.2.1 Go/No-Go reuses these rows if the app code on `release/1.2.1` equals 93f8c00f's.

**5.6 archive.** `openspec archive fix-code-cache-after-clear --yes` and `openspec validate --specs --strict` (results
in the PR).
