# Tasks

## 1. Fix

- [x] 1.1 Tests first: the calculator gives each note its migraine's row; the mapper links the notes table to the very
  migraines table; the fitting keeps only the shown migraines' notes with the line, all notes without it when the
  report fits, and shortens a report without notes as before. They failed to compile before the fix
- [x] 1.2 Domain: `ReportNoteDomainModel.attackIndex`, set by `ReportCalculator.notes`
- [x] 1.3 Datasource model: `ReportBlockDataModel.Table.follows` and `ReportFollowedTableDataModel`
- [x] 1.4 Data: `ReportDocumentMapper` builds one migraines table and links the notes to it, with "Notes from the
  latest %1$d migraines."; "Showing the latest N of M notes." goes
- [x] 1.5 Datasource: the fitting moves to the pure `fittedBlocks`, which cuts a followed table with the table it
  follows

## 2. Verify

- [x] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [ ] 2.2 `:datasource:implementation:connectedDebugAndroidTest` on emulator-5570 (the writer's instrumented tests)
- [x] 2.3 `openspec validate fix-report-notes-cap --strict`
- [ ] 2.4 On emulator-5570, with a seeded heavy year: the report with notes on, read with `pdftotext`: pages within
  50, the notes only those of the migraines shown, and the line; a small report keeps every note and has no line
