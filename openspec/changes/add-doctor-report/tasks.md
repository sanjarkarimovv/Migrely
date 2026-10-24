# Tasks

## 1. Datasources

- [x] 1.1 Records: reuse `BackupDataSource.records()`, which already reads every record in one transaction (design
  D1).
- [x] 1.2 `ReportDocumentDataSource` (`PdfDocument`, partial then rename, clean-up, FileProvider URI), with tests.

## 2. Report feature

- [x] 2.1 Domain: `ReportSummaryCalculator`, `ReportDocumentBuilder` (the page budget), use cases, tests.
- [x] 2.2 Data: `ReportRepository`, mappers, tests.
- [ ] 2.3 Presentation: `ReportViewModel`, tests.
- [ ] 2.4 UI: the report screen, the period chips and range picker, share.

## 3. History and app

- [ ] 3.1 History's "Doctor report" action; the route; DI; the FileProvider; clean-up at start.

## 4. Verify

- [ ] 4.1 Gate, and instrumented tests.
- [ ] 4.2 Emulator walk:
  - each period and a custom range, and an empty period;
  - notes on and off;
  - the PDF's pages, read back with `pdftotext` on the host or rendered;
  - share to Files and to Drive;
  - a 12-month report on a heavy seed;
  - the app closed mid-report;
  - Letter paper with a US locale;
  - 200% font, TalkBack.
- [ ] 4.3 Independent review; fix and record.
