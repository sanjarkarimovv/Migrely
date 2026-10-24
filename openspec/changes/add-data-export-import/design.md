# Design

## Context

- The data lives in Room (DB v6): attacks with intensity points, tags and custom tags, medications and intakes, day
  marks, overuse notices and reminder settings. There is no network.
- Home's header has a bell to Reminders (change 6). There is no settings screen yet.

## Decisions

### D1. Modules

- **`:features:settings:{presentation,ui}`:** a screen of links, with no domain or data. `SettingsViewModel` emits
  `OnReminders`, `OnYourData` and `OnBack`. Home's header button becomes "Settings" (a gear), emitting
  `OnOpenSettings` in place of `OnOpenReminders`.
- **`:features:backup:{domain,data,presentation,ui}`.**
- **Datasources, all implemented in `:datasource:implementation`, which has a `Context`:**
  - `backup` (`BackupDataSource`): `records()` reads every health record; `replaceAll(records)` and
    `add(records): AddedDataModel` write in one Room transaction. New DAO queries read every row and delete them all,
    and inserts ignore a conflicting ID (`OnConflictStrategy.IGNORE`).
  - `backupfile` (`BackupFileDataSource`): `encode(records): String` and `decode(text): BackupDecodedDataModel`
    (`Records(records, schemaVersion)` | `NotABackup` | `Newer`), with kotlinx.serialization JSON.
    - `ignoreUnknownKeys`, and defaults for every field, so older files import (EC-BAK-03).
    - `saveAutomatic(text)` writes `files/backups/migrely-auto-<epoch>.json` and keeps the newest 3.
  - `document` (`DocumentDataSource`): `write(uri, text)` and `read(uri): String` through the `ContentResolver`.
    Failures are `DataException`s.

### D2. The file format (BRD D71)

- The top level has `format` ("migrely-backup"), `schemaVersion` (6), `exportedAt` (ISO), `attacks`, `customTags`,
  `medications`, `intakes` and `dayMarks`.
- Each record carries the stored fields of its table, with times as epoch millis and zones as IDs.
- Attacks being deleted are left out. Reminder settings and overuse notices are device state, so they are not
  exported.

### D3. The CSV (BRD D72)

- A pure mapper in `:features:backup:data` writes the CSV: a UTF-8 BOM, a header row, then one row per attack, newest
  first.
- Fields are quoted when they hold a comma, a quote or a newline, with quotes doubled (RFC 4180).
- Times are written in the attack's own zone as `yyyy-MM-dd HH:mm`.
- Medications are listed as "name dose", and tags as their English labels, both joined by "; ".

### D4. Domain (`:features:backup:domain`)

- **Use cases:**
  - `ExportBackupUseCase(uri)` and `ExportCsvUseCase(uri)` return `Saved`, `StorageFull` or `Failed`;
  - `ReadBackupUseCase(uri)` returns `Readable(summary, phoneHasRecords)`, `NotABackup`, `Newer` or `Failed`;
  - `ImportBackupUseCase(uri, Replace|Add)` reads the file again. It refuses a file that is not readable. For
    `Replace` it first saves an automatic backup; if that fails, it stops and changes nothing. It then writes the
    records and returns `Imported(summary)`, `Added(added, skipped)`, `StorageFull` or `Failed`.
- **The view model stays stateless:** the UI keeps the chosen URI across the dialog.
- **Single-function repositories:** write backup, write CSV, read backup, phone has records, save automatic backup,
  replace with backup, and add backup. All are implemented by `BackupRepository`.

### D5. Import of medications by name (EC-MED-07)

- In "Add", a medication whose `name_key` exists under another ID is not inserted. Its intakes are written with the
  existing medication's ID. The mapping is built inside the transaction.

### D6. UI (`:features:backup:ui`, `:features:settings:ui`)

- **Settings:** two rows, "Reminders" and "Your data", each with a chevron.
- **Your data:**
  - three rows: "Export a backup (JSON)", "Export for Excel (CSV)" and "Import a backup";
  - SAF launchers: `CreateDocument("application/json")` with the name `migrely-YYYY-MM-DD.json`,
    `CreateDocument("text/csv")`, and `OpenDocument(["application/json", "application/octet-stream", "text/plain"])`;
  - a dialog that shows the file's numbers, with "Replace" and "Add", or "Import" when the phone is empty;
  - a progress indicator while a file is written or read;
  - messages: "Backup saved.", "Saved for Excel.", "Imported." / "Added N attacks. M were already here.", "This file
    isn't a Migrely backup.", "Update Migrely to open this file.", storage full, and something went wrong.

### D7. Auto Backup (BRD D74)

- `backup_rules.xml` (Android 11 and older) and `data_extraction_rules.xml` (Android 12 and later), for cloud backup
  and device transfer, both include `database`, `sharedpref` and `file` `backups/`.
- The cache is excluded by Android itself.

## Risks

| Risk | Mitigation |
|---|---|
| A large file and the transaction time | records are written in batches inside one transaction on the background dispatcher; 10 years is about 4,000 attacks, a few MB |
| Room WAL files and Auto Backup | Android backs up the database with its `-wal`, and Room recovers on open |
| A file edited by hand | strict decoding of types; any error is `NotABackup` and nothing changes |
