# Tasks

## 1. Datasources

- [x] 1.1 DAO additions (read all, delete all, insert-ignore) and `BackupDataSource` with one-transaction replace and
  add; instrumented tests.
- [x] 1.2 `BackupFileDataSource` (kotlinx.serialization JSON, decode results, automatic backups keeping 3) with unit
  tests.
- [x] 1.3 `DocumentDataSource` (ContentResolver).

## 2. Backup feature

- [x] 2.1 Domain: models, single-function repositories, use cases, tests.
- [x] 2.2 Data: `BackupRepository`, CSV mapper (RFC 4180, BOM), tests.
- [ ] 2.3 Presentation: `YourDataViewModel`, tests.
- [ ] 2.4 UI: the Your data screen, SAF launchers, the import dialog.

## 3. Settings

- [ ] 3.1 `:features:settings:{presentation,ui}`; home's header shows "Settings" in place of the bell; routes and DI.

## 4. Auto Backup

- [ ] 4.1 `backup_rules.xml` and `data_extraction_rules.xml`.

## 5. Verify

- [ ] 5.1 Gate, and instrumented datasource tests.
- [ ] 5.2 Emulator walk:
  - export JSON and CSV to Downloads, and read them back;
  - import Replace and Add, a broken file, a newer file and an older file;
  - the automatic backup;
  - Settings and Reminders;
  - accessibility.
- [ ] 5.3 Independent review; fix and record.
