# Proposal

## Why

History's "Last 30 days" section says "Simple painkillers: 6 days (limit 15)", "at the limit" and "over the limit". The
Doctor report says "30-day spans at the medication-overuse limit". These numbers are the ICHD-3 8.2 counts linked to the
diagnosis of medication-overuse headache. They are not safe amounts. A person can read "limit 15" as "up to 15 days is
safe or allowed", and a diary must never imply that (issue #135, BRD D151).

Changing the word is not enough. A number beside a lower count still reads as room left, whatever it is called: 9 days
in 30 is already above the NHS advice of no more than 2 days a week. So the number also stops showing until it is reached.

The numbers and the counting are right. The words, and when the number shows, are wrong.

## What Changes

The text below is final. `medical-reviewer` gave PASS on exactly this text, and any changed word goes back to it first.

- **History rows below the number:** the count only, "Simple painkillers: 6 days".
- **A row that reached its number:** "Triptans: 10 days. Doctors watch for 10 or more days a month." The criterion is "N or
  more", so "at" and "over" are no longer told apart. Two strings become one.
- **The line under the rows:** "Taking medication this often, month after month, can make headaches more frequent. It's
  worth talking to your doctor." A reached row and this line always show together.
- **Home's soft card:** "Taking them often can lead to more headaches. Worth keeping an eye on." It states no number, so
  the `thresholdDays` argument goes, and with it a field nothing else reads.
- **Doctor report:** the label becomes "30-day spans reaching an ICHD-3 8.2 medication-day threshold".
- **A rule for later text:** no overuse number shows before it is reached, and no word near one reads as a safe, allowed
  or recommended amount.

**This deliberately changes issue #135.** The issue expected every row to show the day count and the threshold. Rows below
the number now show the count only, for the reason above. `qa-engineer` retests against this text, not the issue's.

## Out of scope

- The threshold numbers (15, 10, 10, 10) and the counting logic (D45).
- The amber colour and when it shows. When the soft and doctor cards show.
- Internal identifiers such as `row.limit`, `limit` in the domain and presentation models, the domain's `thresholdDays`,
  and code comments that do not quote visible text.
- `overuse_title_*` and `overuse_body_doctor`, which do not change. `chronic_notice` does not change for 1.1.0.
- `message_limit_reached` ("You can keep up to 50 medications") and `message_tag_limit`. They are list caps, not medical
  numbers. The `medication-list` spec's "at the limit" means the same cap and stays.
- The website, the store listing and `marketing/`. What the sweep saw there is listed in `design.md`.
- `.maestro/` and `qa/`. `qa-engineer` updates them from the final text.
- Translations: the app ships English only (D148).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `medication-overuse`: the standing indicator's text, the soft card's text, and a new rule for overuse numbers.
- `doctor-report`: the summary page's overuse label.

## Impact

- `features/medication/ui/src/main/res/values/strings.xml`: the `indicator_row_*` plurals, `indicator_reached` and
  `overuse_body_soft`; `indicator_at_limit` and `indicator_over_limit` become `indicator_row_reached`.
- `features/medication/ui/.../view/OveruseIndicator.kt`: the `when` goes, and `row.limit` goes only to the reached string.
- `features/medication/ui/.../view/MedicationPromptCard.kt`: the `thresholdDays` argument goes.
- `thresholdDays` leaves `OveruseWarningPresentationModel`, `MedicationPromptPresentationMapper`,
  `MedicationPromptUiModel.OveruseWarning`, `MedicationPromptUiMapper`, the card's preview and their tests. Nothing else
  reads it once the argument goes (design D3). The domain's `thresholdDays` stays.
- `features/report/data/.../mapper/ReportDocumentMapper.kt` and its test: the label.
- `docs/BRD.md`: D151, the MED-F5 note, the D59 pointer, §13 row 27.
- No schema, backup format, permission, price or dependency change, so no upgrade path is affected.
- `qa-engineer` must update `.maestro/` and `qa/` (the list is in task 6.7). Until then those checks fail on the old text,
  as they should.

BRD ids: D151, D59, D82, D45, MED-F5, REP-F2, EC-MED-06. No new EC: no new state exists. The rows, the line and the cards
appear under the same conditions as before.
