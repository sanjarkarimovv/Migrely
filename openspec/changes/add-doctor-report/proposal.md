# Proposal

## Why

The diary is worth most in the doctor's office. A neurologist has minutes and needs the numbers that decide treatment:
- headache days a month;
- attacks;
- medication days and overuse;
- disabling attacks.

They also need a picture of the months and the details to look up. The MVP promises a report the patient can share from the phone (BRD §6.1 item 10, REP-F1–F4, EC-REP-01–03).

## What Changes

- **"Doctor report"** (D81), from History's top bar. Pick a period: 1, 3 (default), 6 or 12 months, or any range up to
  today. Notes are included only when the user turns them on. "Create report" is off, with the reason, when nothing
  was logged in the period.
- **The PDF** (D82):
  - a summary page a doctor reads in 30 seconds;
  - a calendar heat map that prints in black and white;
  - the attacks table and the medications table;
  - notes, if chosen;
  - "Patient-entered data. Not a diagnosis." and the page number on every page;
  - A4, or Letter in the US and Canada.
- **Counting** follows the app's own rules (D83).
- **Large periods** (D84): made in the background with progress, at most 50 pages; a shortened attacks table says so.
- **Files** (D85): written to the cache as a partial file and renamed when done. Leftovers and day-old reports are
  removed. Shared through the share sheet with a FileProvider, with no permission.

**Out of scope:** the premium gate, which comes with subscription in change 9.

## Capabilities

### New Capabilities

- `doctor-report`: the report screen, its contents, its counting, and sharing it.

### Modified Capabilities

- `attack-history`: History's top bar offers "Doctor report".

## Impact

- **New** `:features:report:{domain,data,presentation,ui}`.
- **Datasource `report`:**
  - `ReportRecordsDataSource` (Room): the period's attacks, intakes with their medications, and day marks;
  - `ReportDocumentDataSource`: renders with Android `PdfDocument` (ADR-07) into the cache, cleans leftovers, and gives a
    shareable content URI. Both live in `:datasource:implementation`.
- **History:** a top-bar action and its navigation event.
- **`:app`:** a `FileProvider` for `cache/reports`, the route, DI, and cleaning up at start.
