# Tasks

## 1. Datasources

- [x] 1.1 Records: reuse `BackupDataSource.records()`, which already reads every record in one transaction (design
  D1).
- [x] 1.2 `ReportDocumentDataSource` (`PdfDocument`, partial then rename, clean-up, FileProvider URI), with tests.

## 2. Report feature

- [x] 2.1 Domain: `ReportSummaryCalculator`, `ReportDocumentBuilder` (the page budget), use cases, tests.
- [x] 2.2 Data: `ReportRepository`, mappers, tests.
- [x] 2.3 Presentation: `ReportViewModel`, tests.
- [x] 2.4 UI: the report screen, the period chips and range picker, share.

## 3. History and app

- [x] 3.1 History's "Doctor report" action; the route; DI; the FileProvider; clean-up at start.

## 4. Verify

- [x] 4.1 Gate (1101 unit tests at the review fixes), and instrumented tests (datasource 90/90, designsystem 28/28).
- [x] 4.2 Emulator walk:
  - each period and a custom range, and an empty period;
  - notes on and off;
  - the PDF's pages, read back with `pdftotext` on the host or rendered;
  - share to Files and to Drive;
  - a 12-month report on a heavy seed;
  - the app closed mid-report;
  - Letter paper with a US locale;
  - 200% font, TalkBack.
- [x] 4.3 Independent review; fix and record.

## Verification record

**Emulator walk** on emulator-5570 (acb79de), with the database restored to the baseline afterwards. All 10 scenarios
passed:
- entry from History;
- a 3-month report shared, with the numbers matching SQL;
- notes on and off;
- an empty period;
- a custom range, with future dates refused;
- 1,500 heavy attacks capped at 50 pages, with the shortened line;
- leftovers cleaned at start, and a write killed mid-way;
- Letter paper for en-US and en-CA, and A4 for en-GB;
- rotation while working;
- accessibility at 200% font.

**Fixed after it:** the dates stay while working, the shortened line moved before its table, a migraine begun before
the period says so, and the picker opens on the chosen range.

**Not a bug:** the header repeats on every table page; `pdftotext` lists it on 48 of the 49 table pages, and the last
one holds only the medications.

**Accepted:** the chooser's title is ignored by Android 9 and later.

**Re-walk after the review fixes** (a02ac05): 8 of 10 checks passed at first, and the database was restored to the
baseline. The checks covered:
- overuse spans counted over all records, and chronic for a whole calendar month;
- totals for a short range;
- the heavy year in 9 s instead of 35, with no ANR;
- a folder per report, and the clean-up;
- rotation, leaving and returning, a double tap;
- "began before the period";
- the picker's range;
- no crash for an unknown zone.

**Fixed after the re-walk** (unit-tested): the dates line now survives a rotation, an unknown zone is named as stored
"shown in UTC", short-range labels drop "a month", and History's medication rows read "11 days (limit 10)".

## Review (4.3)

An independent `superpowers:code-reviewer` pass:

| Finding | Outcome |
|---|---|
| **C1** overuse and chronic for months only partly in the period | fixed: overuse by home's 30-day spans back from the last day, chronic by History's whole calendar months |
| **C2** an unknown zone crashed the screen | fixed: UTC, as the rest of the app; the row names the stored zone |
| **I1** the 50-page cap could fail (notes, 1900 ranges) | fixed: notes shorten too, a range is at most 24 months, and the last pages are cut beyond that |
| **I2** the layout was rebuilt for every page count | fixed: each piece is measured once |
| **I3** the share sheet could open in the background | fixed: events are collected while started |
| **I4** two creates at once | fixed: a second tap does nothing |
| I5 records | fixed: §13 rows for the singleton and the document words; design, BRD and spec follow the code |
| Minor | fixed: 40-line cells, PdfDocument closed on failure, English digits, a folder per report, a dot on pain-free days, the empty-table line, a live region, stale loads, totals for short periods. Accepted: paper size from the locale's country, many types per file |
