# Proposal

## Why

#193 (S1) and #189 (S3), found by QA on 2026-11-10 (`migrely-work/qa-189/HANDOVER.md`, `logs/rows.jsonl`, `raw/193-API36-*`).

Android's installd stores the inode number of the app's `code_cache` folder as the xattr `user.inode_code_cache` on the app's data folder, and at an
app update it clears "code_cache" by that number. On Android 8.0 (API 26), "Clear storage" deletes `code_cache` but keeps the mark, and nothing in
Migrely re-creates the credential-encrypted `code_cache`, because the app never calls `getCodeCacheDir()`. When the app reopens, a new folder can take
the freed number:
- `no_backup/` 20 of 21 times and `databases/` 1 of 21 with no help; with one other folder created on the phone, `databases/` 3 of 3.
- The next update then empties that folder. `databases/`: `migrely.db` is deleted and the app opens on onboarding (#193, 3 of 3). `no_backup/`:
  WorkManager's database is deleted, so today's daily reminder and pending "Did it help?" questions are lost (#189).
- installd logs "Mismatched inode value; found 115143 on disk but marked value was 115099".
- Controls: an update with no clear kept everything 5 of 5. Every Migrely version is affected.

API 36 (Android 16) is safe: the clear removes the mark (4 of 4), so the update finds `code_cache` by name, and the diary and WorkManager's database
survived 5 of 5. Android 8.1 to 15 are unknown (no images).

When an app calls `getCodeCacheDir()` and the folder is missing, `ContextImpl` creates it and writes the mark with its real inode from the app's own
process (SDK sources android-29 and android-36). So the app can keep the mark true. Data loss ranks first, so this ships as 1.2.1, and 1.2.0 is never
uploaded (BRD D170).

## What Changes

- Every start of the app's process, by an activity, a receiver, a job or a provider, re-creates `code_cache` early in the start of `MigrelyApplication` (`attachBaseContext`, design D3)
  through Android's own `codeCacheDir`, so the mark equals the real folder's inode before any later update. No SDK-version branch. If API 26's
  `ContextImpl` does not write the mark, an explicit check and repair does it (design D2).
- The start check never crashes the app, never blocks it noticeably and never logs health data.
- Nothing else: no reminder, receiver, manifest, UI or copy change.

## Out of scope

- `MY_PACKAGE_REPLACED` and planning reminders right after an update (lead decision 2026-11-10 after the PM objection; BRD D170 (3b),
  EC-REM-22; stays in #189 as S3 follow-up work). Reminders lost in the EC-STO-10 residual come back at the next process start, as today.
- Re-planning open "Did it help?" questions after an update (BRD EC-REM-23; stays in #189 as S3 follow-up work).
- Any notice that the diary vanished; any UI, copy, channel or permission change. No `medical-reviewer` or `ux-designer` work.
- Protecting a clear made on a version before 1.2.1 and followed by an update: installd deletes during the update, before new code runs (BRD
  EC-STO-10, documented residual).
- The suspected, pre-existing chance that a re-plan at any process start moves an overdue, not yet run daily reminder to tomorrow (from code
  reading, not verified; comment on #189).
- Version bump, release notes, the release branch: `android-engineer` on `release/1.2.1`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `app-shell`: the app keeps installd's `code_cache` mark true at every process start, safely.

## Impact

- `app`: `MigrelyApplication.kt` (the start call) and a small class for the check with a JVM-testable seam (design D3). No manifest change, no new
  permission (`:app:checkReleasePermissions`), no new dependency, no
  schema change.
- BRD: D170, EC-STO-06…10, §13 row 39 (EC-REM-22, EC-REM-23 record what stays out). Issues #193 and #189 (`Refs #193`, `Refs #189`; QA retests and closes).
