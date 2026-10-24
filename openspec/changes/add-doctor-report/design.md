# Design

## Context

- History reads a month through `GetHistoryMonthUseCase`. Its rules for the days an attack touches (`AttackDays`, D57)
  and for medication days (D45) live in `:features:attackdays:domain` and in History's `MonthStatsCalculator`.
- The overuse rule (D40, per category) lives in the medication feature.
- There is no FileProvider yet. The cache is not backed up (D74).

## Decisions

### D1. Modules

- **`:features:report:{domain,data,presentation,ui}`.** The domain depends on `:features:attackdays:domain` for the
  day rule.
- **Datasource `report`**, in `:datasource:source` and `:datasource:implementation`:
  - `ReportRecordsDataSource.records(from, to)`: the attacks touching the period that are not being deleted, with
    points and tags; the intakes taken in it, with their medication's name, kind and category; the period's day marks;
    and the custom tag names. Read in one transaction.
  - `ReportDocumentDataSource`:
    - `write(document: ReportDocumentDataModel): String` renders and returns the content URI;
    - `cleanUp(now)` removes leftover `.partial` files and reports older than a day.
  - The document model is plain data: rows of text and the calendar's day levels. The datasource draws it with
    `PdfDocument` and knows no rules.
- **The FileProvider** authority is `${applicationId}.reports`, over `cache/reports/`. Its URIs are built in the
  datasource with `FileProvider.getUriForFile`.

### D2. Domain (`:features:report:domain`)

- **`GetReportOptionsUseCase(period)`** returns the dates the period covers and whether anything was logged in them, so
  "Create report" can be off with its reason (EC-REP-01).
- **`CreateReportUseCase(request: period, includeNotes)`:**
  - reads the records and computes the summary with `ReportSummaryCalculator`, which is pure and unit-tested;
  - builds the document model with `ReportDocumentBuilder`, which is pure and tested;
  - writes it.
  - It returns `Created(uri)`, `NothingLogged`, `StorageFull` or `Failed`.
- **The summary:**
  - migraine days = dates in the period that an attack touches, in its zone;
  - a month's average = count × 30 / days in the period, to one decimal;
  - attack-medication days by the intake's zone;
  - overuse per calendar month in the period, by D40's limits (simple painkillers 15 days, triptans and combinations
    10), listing the months over a limit;
  - bed rest = attacks whose functional impact is 3;
  - the average peak, by History's 1–3 split;
  - chronic when any calendar month had 15 or more migraine days.
- **The page budget** (EC-REP-02): the builder lays out rows by the page height. When the attacks would push past 50
  pages, it keeps the latest that fit and adds the "Showing the latest N of M" line.

### D3. Rendering

- A4 is 595×842 points and Letter 612×792. Letter is used when the phone's country is US or CA.
- Text is the platform's sans-serif, 9–18 pt; tables have a header row repeated on each page.
- **The heat map:** one small month grid per month, each day filled by its level. The levels are none, clear, mild,
  moderate and severe, from white through light to dark grey with a blue accent, so they differ in black-and-white
  print. A legend comes with the map.
- **Every page** ends with the footer line and "Page N of M". Pages are laid out first, so the total is known.
- **Writing:** to `reports/<name>.pdf.partial`, then renamed. A failure deletes the partial file. ENOSPC is
  `StorageFullDataException`.

### D4. Presentation and UI

- **`ReportViewModel`** is stateless:
  - `onEnter`, `onPeriodAction(period)`, `onCustomRangeAction(from, to)` and `onIncludeNotesAction(on)` re-read the
    options;
  - `onCreateAction(period, includeNotes)` shows Working, then emits `Share(uri)` or a message.
- **The UI:**
  - keeps the chosen period and the notes switch in `rememberSaveable`;
  - draws a row of period chips and a date-range picker for the custom range;
  - shows the covered dates, the switch, "Create report", and a progress indicator while working;
  - on `Share(uri)`, opens `Intent.createChooser(ACTION_SEND, "application/pdf")` with `FLAG_GRANT_READ_URI_PERMISSION`.
- **History** gets a top-bar icon button "Doctor report", which emits `OnOpenReport`. `:app` maps it to the route.

### D5. Clean-up

- `MigrelyApplication` calls the clean-up on a background thread at start, next to the reminder reschedule.
- Every new report cleans up first.

## Risks

| Risk | Mitigation |
|---|---|
| A big year on an old phone | the layout is computed once, drawing runs on the background dispatcher, and the 50-page cap bounds the work |
| The share target cannot read the file | `FLAG_GRANT_READ_URI_PERMISSION` and a FileProvider URI; the file stays until the next day's clean-up |
| Fonts without a glyph | the platform's default typeface covers Latin, Cyrillic and Uzbek Latin letters, used in names and notes |
