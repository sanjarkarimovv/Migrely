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
    `add(records): AddedRecordsDataModel` write in one Room transaction. New DAO queries read every row and delete them
    all. "Add" checks that a row is absent before inserting it; only day marks use `OnConflictStrategy.IGNORE`.
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
- The separator is ";" when the phone's language writes decimals with a comma (Uzbek, Russian, German and others),
  since Excel there splits columns at semicolons; elsewhere it is ",". Every number in the file is whole.
- A cell starting with "=", "+", "-", "@", a tab or a return gets a leading "'", so a spreadsheet shows it as text
  instead of running it as a formula.

### D4. Domain (`:features:backup:domain`)

- **Use cases:**
  - `ExportBackupUseCase(uri)` and `ExportCsvUseCase(uri)` return `Saved`, `StorageFull` or `Failed`;
  - `ReadBackupUseCase(uri)` returns `Readable(summary, phoneHasRecords)`, `NotABackup`, `Newer` or `Failed`;
  - `ImportBackupUseCase(uri, Replace|Add)`. The repository reads the file again and refuses one that is not readable.
    For `Replace` over records on the phone, the copy is saved only once the file has been read as a backup, so a
    failed attempt never pushes an older copy out. If the copy fails, nothing changes. It returns `Replaced(summary)`,
    `Added(addedAttacks, skippedAttacks, addedOthers)`, `NotABackup`, `Newer`, `StorageFull` or `Failed`.
- **The view model stays stateless:** the UI keeps the chosen URI across the dialog.
- **Single-function repositories:** write backup, write CSV, read backup, phone has records, replace with backup (with
  or without the copy first), and add backup. All are implemented by `BackupRepository`.

### D5. "Add" (BRD D73, EC-MED-07)

- A medication or custom tag whose name key exists under another ID is not inserted; the backup's records point at
  the one on the phone. The mapping is built inside the transaction, and custom tag keys in `attack_tag` are rewritten.
- An attack is skipped when its ID is on the phone, or when it overlaps an attack there, by the same rule as logging
  one (an attack in progress reaches into the future). The same attack logged on two phones has two IDs.
- An intake is skipped when its ID is on the phone, or when the same medication was taken at the same moment. An
  intake of an attack skipped for overlapping is kept without the link.
- A date already marked keeps its marks.
- An attack being deleted with the same ID counts as on the phone, but the backup's intakes are not linked to it, so its
  purge never takes them along. Two backup tags that stand for one phone tag give one `attack_tag` row.
- **Accepted:** an intake of an attack skipped for overlapping is kept unlinked, and the same dose logged on both
  phones seconds apart stays twice. Overuse and the stats count days, so neither number changes. Add can also take the
  active medications or custom tags past the app's own limits; those limits guard the add forms, and every list
  scrolls.

### D6. UI (`:features:backup:ui`, `:features:settings:ui`)

- **Settings:** two rows, "Reminders" and "Your data", each with a chevron.
- **Your data:**
  - three rows: "Export a backup (JSON)", "Export for Excel (CSV)" and "Import a backup";
  - SAF launchers: `CreateDocument("application/json")` with the name `migrely-YYYY-MM-DD.json`,
    `CreateDocument("text/csv")`, and `OpenDocument(["application/json", "text/*", "application/octet-stream"])`, since some file apps
    label a `.json` file as text or plain bytes;
  - a dialog that shows the file's numbers, with "Replace" and "Add", or "Import" when the phone is empty;
  - a progress indicator while a file is written or read;
  - messages: "Backup saved.", "Saved for Excel.", "Imported." / "Added N attacks. M were already here.", "This file
    isn't a Migrely backup.", "Update Migrely to open this file.", storage full, and something went wrong.

### D7. Auto Backup (BRD D74)

- `backup_rules.xml` (Android 11 and older) and `data_extraction_rules.xml` (Android 12 and later), for cloud backup
  and device transfer, both include `database`, `sharedpref` and `file` `backups/`.
- The cache is excluded by Android itself.

### D8. Checking a file (BRD EC-BAK-01–03)

- The header is read first, so a newer file is told apart from a file that is not a backup at all.
- Kinds, keys, zones and dates are read strictly: anything this version does not know makes the file `NotABackup`.
- `BackupRecordsValidator` checks what the database and the app rely on: unique IDs and names, intakes pointing at a
  medication and an attack in the file, intensity 1–10, impact 0–3, and at most one attack in progress. A backup the
  app wrote always passes. Overlaps inside a file are not refused: "Add" skips them, and a backup of a phone that once
  held them restores what it had.
- A file is read up to 16 MB; ten years are a few megabytes.
- **Not checked:** that a `custom:<id>` key names a tag of that kind in the file (readers drop unknown tags), and the
  length of names, doses and notes (the screens wrap long text).

### D9. Delivery across a rotation

- `YourDataViewModel` is app-wide. It counts the files being worked on and keeps the last outcome until the screen
  acknowledges it. The screen asks for both once it listens (ARCHITECTURE section 13).

### D10. Failures

- A document provider is another app, and its failures cross Binder as any runtime exception; `file {}` maps them all
  to `FileDataException`, since nothing catches an exception thrown inside a background use case.
- A failed export deletes the file the picker created, so a half-written file never passes for a backup.
- The CSV writes an attack whose zone this phone does not know in UTC, and names the zone.

## Risks

| Risk | Mitigation |
|---|---|
| A large file and the transaction time | records are written in batches inside one transaction on the background dispatcher; 10 years is about 4,000 attacks, a few MB |
| Room WAL files and Auto Backup | Android backs up the database with its `-wal`, and Room recovers on open |
| A file edited by hand | strict decoding of types; any error is `NotABackup` and nothing changes |
| A file provider that refuses the "wt" mode | the write falls back to "w"; the picker has just created the file, so it is empty |
| No file picker on the phone | the launch fails with `ActivityNotFoundException`, and the screen says so |
| A phone with no screen lock | cloud backup is off (`disableIfNoEncryptionCapabilities`); the JSON export still works |
| The automatic backup cannot be restored from the app yet | it is in Android's backup; change 12 adds "Restore" (BRD D75) |
