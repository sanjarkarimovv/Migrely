## ADDED Requirements

### Requirement: The diary survives an update after Clear storage
At every start of the app's process, whatever component starts it, the app SHALL make sure its `code_cache` folder exists and that installd's
`user.inode_code_cache` mark, when present, holds that folder's inode. A later app update SHALL then clear only `code_cache`, never `databases/`,
`no_backup/`, `files/` or another folder. This SHALL hold on every Android version, with no version branch, and SHALL show nothing to the user
(BRD EC-STO-06, EC-STO-08, EC-STO-09, D170, #193, #189).

#### Scenario: Clear storage, reopen, then an update keeps the diary (Android 8.0)
- **WHEN** on API 26 the user has a diary, clears storage, one other folder is created on the phone (QA's churn1 method), the user opens Migrely, and
  the app is updated to the fix build
- **THEN** `databases/migrely.db` is kept and the diary opens with its entries, 3 of 3 runs
- **AND** before the update the mark equals the inode of `code_cache`

#### Scenario: Clear storage, reopen, then an update keeps the reminders (Android 8.0)
- **WHEN** on API 26 the user turns the daily reminder on, clears storage, opens Migrely, turns the reminder on again, and the app is updated to the fix
  build
- **THEN** WorkManager's database in `no_backup/` and the planned reminder jobs are kept, and the daily reminder arrives at its time

#### Scenario: Negative control without the fix
- **WHEN** the same steps run with 1.2.0 R8 (`r8-9d453bb5`) as the build before the update
- **THEN** the update deletes `migrely.db` (churn1) or WorkManager's database (plain clear), as QA saw on 2026-11-10

#### Scenario: The first start after the clear comes from a receiver or a job
- **WHEN** after Clear storage the process is first started by a broadcast receiver (for example a clock change), a WorkManager job or a provider,
  not by an activity
- **THEN** the check runs in that start too, and the mark equals the inode of `code_cache` before any update

#### Scenario: The folder exists and the mark is right
- **WHEN** `code_cache` exists and the mark already holds its inode
- **THEN** nothing is created or written, and the folder's contents are untouched

#### Scenario: The folder is missing
- **WHEN** `code_cache` is missing at a process start, with or without a mark
- **THEN** the app re-creates it and the mark holds the new folder's inode

#### Scenario: No mark and the folder exists
- **WHEN** the data folder has no `user.inode_code_cache` mark and `code_cache` exists
- **THEN** the app writes no mark, because installd then finds `code_cache` by its name

#### Scenario: An update with no clear before it
- **WHEN** the app is updated and storage was never cleared since the last update
- **THEN** nothing changes: `migrely.db`, WorkManager's database, settings and automatic backups are kept, on API 26 and API 36

#### Scenario: Android 16 control
- **WHEN** QA runs the same arms on API 36 (Android 16), with and without a clear, before and after the fix
- **THEN** the diary and WorkManager's database are kept in every run, and after a reopen on the fix build the mark equals the inode of `code_cache`

#### Scenario: A work profile or a second user
- **WHEN** Migrely is installed for the owner and in a work profile or for a second user, and storage is cleared in one of them
- **THEN** a process start in that user repairs only that user's data folder, the other user's data is not touched, and a user that never reopened
  the app after its clear has no new folder at risk

#### Scenario: Delete all data in the app
- **WHEN** the user deletes all data from Settings and the app is later updated
- **THEN** nothing is deleted by the update, because "Delete all data" empties tables and removes no folder (BRD EC-STO-03)

#### Scenario: The documented residual, a clear made before 1.2.1
- **WHEN** on API 26 storage was cleared and the app reopened while 1.1.1 was installed, and the app is then updated to the fix build
- **THEN** the update may still delete `migrely.db` or WorkManager's database, because installd deletes before any new code runs; QA records this as a
  known residual, not a failure (BRD EC-STO-10, D170 (2))
- **AND** after that update the mark is right, so later updates delete nothing

### Requirement: The code-cache check is safe
The `code_cache` check SHALL never crash the app or stop its start, whether a folder cannot be created, the mark cannot be read or written, or the
platform throws. It SHALL run without network, account or new permission, SHALL not wait on disk beyond a few file-system calls, and SHALL log at most
the kind of failure, never diary content (BRD EC-STO-07, D170).

#### Scenario: Writing the mark is denied
- **WHEN** writing the mark fails, for example by SELinux or a file system without user xattrs
- **THEN** the app starts normally, the reminders are re-planned, and at most a warning with the failure's kind is logged

#### Scenario: The folder cannot be created
- **WHEN** `code_cache` cannot be created, for example while credential-encrypted storage is locked or storage is full
- **THEN** the app starts normally, and the next process start tries again

#### Scenario: The platform throws
- **WHEN** any call inside the check throws
- **THEN** the exception is caught inside the check, and the rest of `onCreate` runs

#### Scenario: Nothing private in the log
- **WHEN** the check logs a failure
- **THEN** the line holds no diary content, no medication or attack data, and no setting value
