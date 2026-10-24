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
  - the records come from change 7's `BackupDataSource.records()`. It reads every health record in one transaction,
    without attacks being deleted or their intakes. The domain keeps the period's: ten years are a few megabytes, and
    a second read path would only repeat its rules.
  - `ReportDocumentDataSource`:
    - `write(document: ReportDocumentDataModel): String` renders and returns the content URI;
    - `cleanUp(now)` removes leftover `.partial` files and reports older than a day.
  - The document model is plain data: rows of text and the calendar's day levels. The datasource draws it with
    `PdfDocument` and knows no rules. It is told the page limit and which table may be shortened, with the line to
    say so, and it keeps as many of that table's first rows as fit. Only it can measure the text.
- **The FileProvider** authority is `${applicationId}.reports`, over `cache/reports/`. Its URIs are built in the
  datasource with `FileProvider.getUriForFile`.

### D2. Domain (`:features:report:domain`)

- **`GetReportOptionsUseCase(period)`** returns the dates the period covers and whether anything was logged in them, so
  "Create report" can be off with its reason (EC-REP-01).
- **`CreateReportUseCase(request: period, includeNotes)`:**
  - reads the records and computes everything with `ReportCalculator`, which is pure and unit-tested;
  - the repository turns the result into the document's words with `ReportDocumentMapper`, in data, as the CSV does;
  - writes it.
  - It returns `Created(uri)`, `NothingLogged`, `StorageFull` or `Failed`.
- **The summary:**
  - migraine days = dates in the period that an attack touches, in its zone;
  - a month's average = count × 30 / days in the period, to one decimal;
  - attack-medication days by the intake's zone;
  - overuse by home's rule (D45): 30-day spans back from the period's last day, each counted whole from every record,
    by the limits of 15 days for simple painkillers and 10 for triptans, combinations or two together;
  - bed rest = attacks whose functional impact is 3;
  - the average peak, by History's 1–3 split;
  - chronic, by History's rule: a calendar month the period touches, counted whole, with 15 or more migraine days;
  - a period under 28 days gives totals, not a month's average.
- **The page budget** (EC-REP-02) is kept by the renderer, which alone can measure text. Each table marked
  shortenable (migraines, then notes) keeps, in turn, as many latest rows as fit. Past that, the last pages are cut.
  A custom range is at most 24 months.

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
  - `onEnter(period)` and `onPeriodAction(period)` re-read the options, and only the latest read is shown;
  - `onCreateAction(period, includeNotes)` shows Working, then emits `Share(uri)` or a message; a second tap while one
    is made does nothing;
  - it lives as long as the app and resends an outcome the screen missed, as Your data does. The notes switch and the
    custom range stay in the UI.
- **The UI:**
  - keeps the chosen period and the notes switch in `rememberSaveable`;
  - draws a row of period chips and a date-range picker for the custom range;
  - shows the covered dates, the switch, "Create report", and a progress indicator while working;
  - on `Share(uri)`, opens `Intent.createChooser(ACTION_SEND, "application/pdf")` with `FLAG_GRANT_READ_URI_PERMISSION`.
- **History** gets a top-bar icon button "Doctor report", which emits `OnOpenReport`. `:app` maps it to the route.

### D5. Clean-up

- `MigrelyApplication` calls the clean-up on a background thread at start, next to the reminder reschedule.
- Every new report cleans up first.

### D6. After the review

- An unknown zone (a restore from a phone with newer zone data) reads as UTC, as the rest of the app does.
- Each report gets its own folder in `cache/reports`, so a second report the same day never takes the first one's URI.
- Pieces are measured once, so the page cap's search does not lay the text out again.
- Cells and paragraphs stop at 40 lines with an ellipsis, so no row is taller than a page.
- Pain-free days carry a dot, so they differ from days with nothing logged in black and white.
- Numbers in the page label and notes use English digits.
- The share sheet opens only while the screen is started.

## Risks

| Risk | Mitigation |
|---|---|
| A big year on an old phone | each piece is measured once, drawing runs on the background dispatcher, and the 50-page cap bounds the work |
| The share target cannot read the file | `FLAG_GRANT_READ_URI_PERMISSION` and a FileProvider URI; the file stays until the next day's clean-up |
| Fonts without a glyph | the platform's default typeface covers Latin, Cyrillic and Uzbek Latin letters, used in names and notes |
