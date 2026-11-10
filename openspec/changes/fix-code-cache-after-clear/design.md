# Design

Code read from `develop` ad422d7f (= the v1.2.0 tree plus `docs/qa/go-no-go-1.2.0.md`). Facts from QA's probe on 2026-11-10
(`migrely-work/qa-189/`, rows in `logs/rows.jsonl`, API 36 in `raw/193-API36-*`) and BRD D170.

## Context

- installd keeps the inode of `<data>/code_cache` as xattr `user.inode_code_cache` (8 bytes, native byte order) on `<data>` (`/data/user/<id>/
  com.migrely.app`). At an update it clears "code_cache" by looking the inode up in `<data>`; if no entry has it, or there is no mark, it uses the name.
  After the update it marks `code_cache` by name again ("Mismatched inode value ... overwriting"), so only the first update after a clear is exposed.
- API 26: "Clear storage" deletes `code_cache` and keeps the mark. API 36: the clear also removes the mark, so the reused inode is harmless.
- Nothing in Migrely calls `getCodeCacheDir()`; Android's own start code touches only the device-encrypted `code_cache`.
- `ContextImpl.getCodeCacheDir()` → `ensurePrivateCacheDirExists(file, XATTR_INODE_CODE_CACHE)`: when the folder is missing it creates it and writes
  the mark with the new inode, and logs a warning on `ErrnoException` (SDK sources android-29, android-36). API 26 is not checked yet (task 1.1).
- `MigrelyApplication.onCreate` already re-plans the daily and visit reminders on a thread at every process start (`RescheduleRemindersUseCase`).
  Planning is unique work with `ExistingWorkPolicy.REPLACE`; the daily time is the first one strictly after now (`DailyReminderTime.next`).
- `TimeChangeReceiver` (non-exported) re-plans on `TIMEZONE_CHANGED` and `TIME_SET` with `goAsync`.

## Goals and non-goals

Goals: an update never clears anything but `code_cache`, for any clear made on 1.2.1 or later; reminders return right after an update that lost them;
no crash, no UI, no permission. Non-goals: the out-of-scope list in `proposal.md`.

## Decisions

### D1. Keep the mark true by touching `codeCacheDir`

| Option | Verdict |
|---|---|
| Call `codeCacheDir` at every process start; Android re-creates the folder and writes the true mark | **Chosen** (lead decision 3a). One line on the platform's own path |
| Read the mark, stat the folder, write the mark when they differ | Fallback only (D2) |
| Detect a clear and move or copy the diary | Rejected: more code, more ways to lose data |
| Branch on SDK version | Rejected: 8.1–15 unknown; on API 36 the call creates the folder and a true mark, which is harmless |
| Tell the user the diary vanished | Out of scope (D170 (3c)) |

Order does not matter for correctness: if `databases/` or `no_backup/` already took the old inode, the mark moves to the new `code_cache`, so the update
no longer finds the old number in `<data>`.

### D2. Fallback: explicit check and repair

Use it only if task 1.1 or 3.3 shows that API 26 does not write the mark, or that after a clear and a reopen `code_cache` exists while the mark still
names another inode (for example, a library made the folder with a plain `mkdir` before `MigrelyApplication` ran). Then, after the call in D1:
1. read the mark with `Os.getxattr(dataDir, "user.inode_code_cache")`; no mark (`ENODATA`) or not 8 bytes → stop, write nothing;
2. `Os.stat(codeCacheDir).st_ino`; failure → stop;
3. equal → stop; different → `Os.setxattr(dataDir, name, the inode as 8 bytes in ByteOrder.nativeOrder(), 0)`.

Never write a mark that was not there: without one, installd uses the name, which is safe.

### D3. Where it runs and the seam for tests first

- Run it synchronously in `MigrelyApplication.attachBaseContext`, after `super`, so it comes before content providers (androidx.startup creates
  `no_backup/` for WorkManager) and before `onCreate`. That narrows the window in which the process dies with a stale mark. If `attachBaseContext`
  gives trouble (Hilt, tests), the first line of `onCreate` is acceptable: D1 says order does not change correctness. It is a few syscalls, so no
  thread.
- Seam: a small pure-Kotlin class in `:app` (suggested `com.migrely.app.storage.CodeCacheMark`) with one method, for example `keep()`, over an interface
  for the platform (suggested `CodeCacheFolder`: `ensure()`, and for D2 `markedInode(): Long?`, `folderInode(): Long?`, `mark(inode: Long)`). The
  Android implementation uses `Context` and `android.system.Os`. Names must satisfy the Konsist rules in `app/src/test/.../architecture`.
- JVM tests with a fake (`app/src/test`): `ensure()` is called; each step that throws is caught and `keep()` returns normally with a failure result;
  and for D2: no mark → no write; equal → no write; different → exactly one write of the folder's inode; a failed stat → no write.
- Log at most `Log.w(tag, "code_cache check failed: ${e.javaClass.simpleName}")`. No path, no message text from user data.

### D4. `MY_PACKAGE_REPLACED`

- Add `<action android:name="android.intent.action.MY_PACKAGE_REPLACED" />` to `TimeChangeReceiver`'s filter; it stays `exported="false"` (the
  system delivers to it, as for `TIME_SET`). No permission is needed.
- In `onReceive`, the update action plans nothing: the process start that delivered it already ran D3 and the start re-plan. One plan per process
  start avoids two quick `REPLACE`s of a visit run due now, which could post the visit reminder twice. Time actions keep planning as today.
- Update the receiver's KDoc. Renaming the class is optional and not needed.
- Seam: the decision "plan for this action" as a pure function or a small Robolectric test (`testDebug`), so `TIME_SET` and `TIMEZONE_CHANGED`
  plan, `MY_PACKAGE_REPLACED` and anything else do not.
- After a Play update the old process is killed, so `MY_PACKAGE_REPLACED` always arrives in a fresh process. An app in the stopped state may not get
  it; then nothing changes from today (spec scenario).

### D5. Idempotence of the re-plan (checked by reading the code)

- Daily: `REPLACE` keeps one run; `next(now)` is strictly after now, so the re-plan never posts for a passed time and never twice.
- Visit: `VisitReminderPlan` cancels when reminded of or the day has ended; a passed time runs once (EC-REM-14). `setOnlyAlertOnce(true)` and a fixed
  notification id limit a race to one visible notification.

## Risks

- **API 26 `ContextImpl` may not write the mark.** Task 1.1 reads the source; QA's API 26 acceptance proves it. Fallback D2.
- **A folder made before `MigrelyApplication` without the mark.** QA's acceptance reads the mark after a clear and reopen; D2 covers it.
- **Pre-existing, made a little more frequent:** any process start re-plans with `REPLACE`, so a daily run whose time passed but which JobScheduler
  has not run yet (Doze) moves to tomorrow. `MY_PACKAGE_REPLACED` adds one more process start per update. Not fixed here; `product-manager`
  recommends a follow-up issue (keep an unfinished daily run for today instead of replacing it). Not verified on a device.
- **Process killed before the start re-plan finishes** after the receiver returns: the thread takes milliseconds; the next start re-plans.
- **Residual (EC-STO-10):** a clear made before 1.2.1 and followed by an update is not protected. Proven on 8.0, absent on 16, unknown on 8.1–15.
- **Auto Backup restore mode** starts the process without `MigrelyApplication`; the check runs at the next normal start, before any update.
- Not provable here: real phones, vendor installd, f2fs, Android 8.1–15.
