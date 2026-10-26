# Proposal

## Why

QA #67: a 12-month doctor report of 900 migraines, each with a note, made with "Include notes" on, had 50 pages. Its
migraines table said "Showing the latest 165 of 900 migraines", yet the notes section held all 900 notes, most of them
for migraines the table no longer listed.

BRD D84 and the doctor-report spec say the notes are shortened the same way as the migraines table. The writer
(`ReportDocumentDataSourceImpl`) shortened each table that may be shortened on its own, in document order: the
migraines table first, down to the rows that fit next to every note, and the notes table only if even zero migraine
rows did not fit. Nothing tied a note to its migraine, so the table gave up rows to make room for notes. With notes
off, the same report showed more migraine rows.

## What Changes

The PO decided to follow D84 as written: the notes section holds only the notes of the migraines the table shows, the
same latest N, and says so with "Notes from the latest N migraines.", in the words of the table's own line.

- **Domain:** each `ReportNoteDomainModel` carries `attackIndex`, its migraine's position in the report's migraines,
  which `ReportCalculator.notes` sets.
- **Data:** `ReportDocumentMapper` builds one migraines table and links the notes table to it with
  `ReportFollowedTableDataModel` (the table, each note's row in it, and the line). The notes table is no longer
  shortened on its own, so "Showing the latest N of M notes." goes.
- **Datasource:** the writer still decides N, since only it measures pages. The fitting moves out of
  `ReportDocumentDataSourceImpl` into a pure `fittedBlocks` in the layout package, which takes a page counter, so it
  is unit-tested. Shortening the migraines table to N rows also keeps only the notes whose migraine is among them and
  puts the line before them; N is found with both cuts measured together, so the table keeps more rows than before.
- Unit tests for the calculator, the mapper and the fitting.
- The doctor-report spec says which notes stay and the line.

Unchanged: a report that fits keeps every note and gets no line; a report without notes is shortened exactly as
before; past the migraines table, the last pages are still cut at 50 (D84). Every other word and format of the PDF
stays the same (ARCHITECTURE §13).

Out of scope: no BRD change, since D84 already says the notes are shortened the same way.

## Capabilities

### Modified Capabilities

- `doctor-report`: when the migraines table is shortened, the notes are those of the migraines it shows, under "Notes
  from the latest N migraines."

## Impact

- `:features:report:domain`: `ReportNoteDomainModel`, `ReportCalculator`.
- `:features:report:data`: `ReportDocumentMapper`.
- `:datasource:source`: `ReportBlockDataModel.Table.follows`, `ReportFollowedTableDataModel`.
- `:datasource:implementation`: `ReportDocumentDataSourceImpl`, new `ReportFitting.kt`.
