# Proposal

## Why

The user's diary lives only on their phone, and there is no INTERNET permission, so no cloud copy exists. The MVP
promises a way to keep it and move it (BRD §6.1 item 7, BAK-F1–F5): a JSON file to restore from, a CSV for Excel, and
Android's own backup kept deliberate.

## What Changes

- **A Settings screen** (D70): home's header shows "Settings" instead of the bell. The screen lists "Reminders" and
  "Your data"; changes 9 and 12 add their items to it.
- **Your data:**
  - "Export a backup (JSON)": every health record, with its schema version (D71);
  - "Export for Excel (CSV)": one row per attack (D72);
  - "Import a backup": checks the file, shows what it holds, then "Replace" (an automatic backup is taken first) or
    "Add" (records already present are skipped). It runs in one transaction. A broken file changes nothing, and a
    newer file is refused (D73).
  - Files come through the system's file picker, with no storage permission (BAK-F5).
- **Auto Backup rules** (D74): the database, shared preferences and the automatic backups; not the cache.

**Out of scope:** the onboarding link for import on a new phone (EC-BAK-05), which comes in change 8.

## Capabilities

### New Capabilities

- `data-backup`: export, import and Auto Backup.

### Modified Capabilities

- `reminders`: Reminders is now reached through Settings, and home's header shows Settings.

## Impact

- **New** `:features:settings:{presentation,ui}`: a screen of links, no domain.
- **New** `:features:backup:{domain,data,presentation,ui}`.
- **Datasource:**
  - `backup` (`BackupDataSource`): reads every record and writes them in one Room transaction;
  - `backupfile` (`BackupFileDataSource`): the file format, and the automatic backups;
  - `document` (`DocumentDataSource`): reads and writes a content URI's text through the `ContentResolver`.
  - All three are implemented in `:datasource:implementation`, which only needs a `Context`.
- **Dependencies:** `kotlinx-serialization-json` for the file format, in `:datasource:implementation`.
- **`:app`:** routes, DI, `backup_rules.xml` and `data_extraction_rules.xml`.
