# Proposal

## Why

History's "Last 30 days" section says "Simple painkillers: 6 days (limit 15)", "at the limit" and "over the limit". The
Doctor report says "30-day spans at the medication-overuse limit". These numbers are the ICHD-3 8.2 counts linked to the
diagnosis of medication-overuse headache. They are not safe amounts. A person can read "limit 15" as "up to 15 days is
safe or allowed", and a diary must never imply that (issue #135, BRD D151).

The numbers and the counting are right. Only the words are wrong.

## What Changes

The new text below is a draft until `ux-designer` and `medical-reviewer` have seen it (see `design.md`).

- **History rows:** the row keeps the number and says whose number it is: "Simple painkillers: 6 days (doctors watch for
  15+)".
- **A row that reached its number:** one word for both cases, "Triptans: 10 days (doctors watch for 10+), reached". ICHD-3
  says "N or more", so "at" and "over" are no longer told apart. The day count is in the row. Two strings become one, and
  the view loses one branch.
- **Doctor report:** the label becomes "30-day spans reaching the medication-overuse threshold".
- **A rule for later text:** near an overuse number, no word reads as a safe, allowed or recommended amount.

## Out of scope

- The threshold numbers (15, 10, 10, 10) and the counting logic (D45).
- The amber colour and when it shows.
- Internal identifiers such as `row.limit`, `limit` in the domain and presentation models, and code comments that do not
  quote visible text.
- The Home cards `overuse_title_*`, `overuse_body_soft`, `overuse_body_doctor`, and the line `indicator_reached`. They do
  not use the word. Two notes on them go to `medical-reviewer` (see `design.md`).
- `message_limit_reached` ("You can keep up to 50 medications") and `message_tag_limit`. They are list caps, not medical
  numbers. The `medication-list` spec's "at the limit" means the same cap and stays.
- The website, the store listing and `marketing/`. What the sweep saw there is listed in `design.md`.
- `.maestro/` and `qa/`. `qa-engineer` updates them from the final text.
- Translations: the app ships English only (D148).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `medication-overuse`: the standing indicator's wording, and a new rule for words near an overuse number.
- `doctor-report`: the summary page's overuse label.

## Impact

- `features/medication/ui/src/main/res/values/strings.xml`: the `indicator_row_*` plurals; `indicator_at_limit` and
  `indicator_over_limit` become `indicator_row_reached`.
- `features/medication/ui/.../view/OveruseIndicator.kt`: one reached branch, and the comment that quotes the text.
- `features/report/data/.../mapper/ReportDocumentMapper.kt` and its test: the label.
- `docs/BRD.md`: D151, the MED-F5 note, the D59 pointer, §13 row 27.
- No schema, backup format, permission, price or dependency change. Text only, so no upgrade path is affected.
- `qa-engineer` must update `.maestro/medication-overuse/history-medication-only.yaml`,
  `.maestro/month-stats/medication-days.yaml`, `qa/device_scenarios.sh` and `qa/seed_backup.py`. Until then those checks
  fail on the old text, as they should.

BRD ids: D151, D59, D82, D45, MED-F5, REP-F2, EC-MED-06. No new EC: only text changes and one condition merges, so no new
state exists.
