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
- [x] 2.2 `:datasource:implementation:connectedDebugAndroidTest` on emulator-5570 (the writer's instrumented tests)
- [x] 2.3 `openspec validate fix-report-notes-cap --strict`
- [x] 2.4 On emulator-5570, with a seeded heavy year: the report with notes on, read with `pdftotext`: pages within
  50, the notes only those of the migraines shown, and the line; a small report keeps every note and has no line

## Verification record

- **Tests:** written first; they failed to compile before the fix (no `attackIndex`, no `follows`). After it:
  `ReportFittingTest` 5 (900 migraines with notes keep 246 of each with the line; notes on every third migraine keep
  only those shown; notes on the oldest only leave the line over an empty table; a report that fits is returned as
  is; a report without notes is shortened as before), `ReportDocumentMapperTest` 14, `ReportCalculatorTest` 14,
  `CreateReportUseCaseTest` 5. Gate: BUILD SUCCESSFUL, 2160 unit test runs, none failed.
- **Instrumented:** `:datasource:implementation:connectedDebugAndroidTest` on emulator-5570, 100 tests, none failed,
  the writer's 8 included.
- **On emulator-5570**, the walk baseline plus 900 seeded migraines from 1 November 2025, each with a note
  "HEAVY-0001" … "HEAVY-0900" (916 in the 12 months), Premium from the simulated store, a 12-month report with
  "Include notes" on, read with `pdftotext -layout`:

  | | Before (release code) | After (this branch) |
  |---|---|---|
  | Pages | 50 | 50 |
  | Migraines table | "Showing the latest 429 of 916" | "Showing the latest 618 of 916" |
  | Notes | all 900 HEAVY notes, no line | 603 rows, HEAVY-0299 … HEAVY-0900 and the one baseline note, the notes of exactly the 618 shown migraines, after "Notes from the latest 618 migraines." |

  The page count stays at the 50-page limit: the room the notes no longer take goes to migraine rows (618 instead of
  429), as D84 shortens the table only as far as needed. With whitespace collapsed, everything before the migraines
  table and the whole medications section read the same in both reports; only N differs.
- A 3-month report with notes on (16 migraines, one note) has 2 pages, its one note and no shortening line.
- Evidence: `migrely-work/walks/fix67/` (both PDFs, their text and the seed). The baseline was restored afterwards
  (dump md5 `c3cfd32487171b7886baf9725dce7594`).

## Review

An independent review found nothing critical. Fixed:
- Important: with none of the shown migraines noted, the notes section was an unpinned case. It keeps the line over
  an empty table, as a table kept to no rows does; a fitting test and a spec scenario now pin it;
- a use-case test checks that each note's `attackIndex` points at its own migraine;
- the fitting's KDoc read garbled.

Accepted:
- The link to the migraines table is by identity, as the writer already found tables. The mapper test pins that the
  link is the very table in the blocks, and a broken link would bring back the old output rather than lose data.
- No test has two tables that may be shortened, or a follower before its leader: the report has neither, and the
  writer handles both by working from the last position back.
- No BRD change: D84 already says the notes are shortened the same way, and the spec now carries the line's words.
