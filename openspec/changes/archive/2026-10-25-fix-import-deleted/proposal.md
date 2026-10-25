# Proposal

## Why

QA #58 (S3). A deleted migraine is hidden first, so that it can be undone, and purged for good after 2 minutes (BRD
D23). The purge ran only when another migraine was deleted or a deletion was looked up, so a hidden row could stay for
days. Import's "Add" matches migraines by ID. It counted the hidden row as "already here" and skipped the file's copy,
so a person restoring a migraine they deleted by mistake did not get it back and was told it was already there.

## What Changes

- **Import treats deletions as absent (BRD D118):**
  - "Add": a migraine being deleted with the ID of one in the file is removed for good, with its points, tags and
    intakes, and the file's copy is added by the usual rules. It counts as added.
  - An intake hidden with a migraine being deleted is absent too, matched by ID or by medication and time.
  - "Replace" already deleted every row, hidden ones included; a test now proves it.
- **Purge at start:** the app purges the deletions past their undo window when it starts, beside the report clean-up,
  so hidden rows no longer linger.
- **Record kinds checked:** only migraines are soft-deleted. Intakes, intensity points and tags are hidden with their
  migraine. Medications and custom tags are archived, never deleted, and are exported and matched as before. Day
  marks and intakes on their own are deleted at once.

**Out of scope:** the undo window itself (D23), and how the import's outcome is worded.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `data-backup`: "Add" brings back a migraine deleted on the phone.

## Impact

- **Changed modules:**
  - `:datasource:implementation`: `BackupDao.add`, and `AttackDataSourceImpl.purgeExpiredDeletions`;
  - `:datasource:source`: `AttackDataSource.purgeExpiredDeletions`, and the `BackupDataSource.add` contract;
  - `:features:attack:domain` and `:data`: `PurgeExpiredDeletionsUseCase` and its repository;
  - `:app`: DI, `StartEntryPoint` and `MigrelyApplication`.
- **Docs:** BRD D118; `docs/ARCHITECTURE.md` section 6a, soft delete.
- **Tests:** instrumented `BackupDaoTest` and `BackupRoundTripTest`; unit tests of the attack datasource, the
  repositories and the use cases.
- **BRD references:** BAK-F3, D23, D73, D118.
