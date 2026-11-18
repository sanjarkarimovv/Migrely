# Proposal

## Why

**#237 (S3, pre-existing: 1.2.1 does the same).** A person logs a painkiller on Log medication while the phone's storage is
full. "Couldn't save. Your phone's storage is full." shows, which is right. They free some space and tap the medication
again. The intake is stored (`medication_intake` 1 → 2) and the page turns to "Ibuprofen 400 mg logged at 14:20" with "Done"
and "Undo", but the error stays on top of it, 2 and 8 seconds later. The logged line is covered and missing from the
accessibility tree. The screen says two opposite things, and the one that is read first says "not saved". A person in pain
can log the dose again. QA found it in the release/1.3.0 Go/No-Go (T73, row D178 A5): 2 of 2 on the candidate, 1 of 1 on 1.2.1.

**Why it matters for the diary.** A duplicate intake raises the intake count in the doctor report and in the attack's details.
Overuse counts days, not intakes (EC-MED-02), so a same-day duplicate does not change the day count, but a re-log after
midnight or with another picked time adds a medication day that never happened.

**Cause (code read on `develop` 76c38c38).** D178 (1) and the `in-app-messages` scenario "A write with its own message"
assumed that a log on Log medication shows "the logged message with Undo", which would replace the error under "Newest
message wins". No such message exists: a stored intake swaps the page to `LoggedContents` (`LogMedicationScreen.kt:135-138`,
`347`) and posts nothing, and `MedicationMessages.kt:52-58` ignores every event that is not a `Message`. A timeless error
never leaves by itself. The A5 row of #221 was never run on a device.

**A neighbour found in the same code (not reproduced).** "Undo" sets `logged = null` before the removal runs
(`LogMedicationScreen.kt:182-186`). If the removal fails (storage full, or any `DataException`), the list comes back with
"Couldn't save" over it while the intake is still stored. The page then says the dose is gone; the person may log the right
medication and leave the wrong one in the diary. Same screen, same principle (the screen tells what is stored), same files,
so it is fixed here.

## What Changes

- **A stored intake clears the message (D190 (1)).** On Log medication, an intake stored by a tap or by "Add and log" removes
  whatever message shows (error, "Removed. Nothing was logged.", "Pick a time up to now.", "You can keep up to 50
  medications…"), silently and in the frame the logged result appears, with no fade (ux L3): no "Saved.", no haptic. Only the
  logged line, already a polite live region, is heard (ux L4); it is the outcome, as the share sheet is on Doctor report. Only
  a stored intake clears; every failure keeps today's behaviour.
- **Undo waits for its outcome (D190 (2), conditional).** The logged result stays until the removal is stored, then the list
  shows with "Removed. Nothing was logged." as today. A failed removal keeps the logged result and says "Still logged. Your
  phone's storage is full." or "Still logged. Something went wrong. Please try again." (ux 4.4: "Couldn't save" on a page
  titled "Medication taken" would read as "the dose was not logged"), and "Undo" works again. A second "Undo" before the
  outcome does nothing. **Condition (team-lead, 2026-11-18):** the engineer's first task is a red test that shows the failed
  Undo on v1.3.1's code. If it cannot be shown red, rule 2 and its two strings leave 1.3.2 and become a backlog note.
- **Records.** BRD D190 (new), EC-MED-16 and EC-MED-17 (new), EC-UX-35 (Log medication pointer), §13 row 54.
  `in-app-messages` "An error on the other screens goes when a later save succeeds" is corrected for Log medication.

## Out of scope

- The message's place and the first-row rule (D158): a message may still lie over the first row of any page; this change only
  makes sure none is left over the logged result.
- Any string other than the two "Still logged." messages, and the other 8 screens of D178 (their behaviour stays as D178
  says).
- Attack details' own intake rows and their Undo (its own message path, D165), Home's "+ Medication taken" card.
- #239 (Edit medication's Save feedback) and #245 (short-window cursor): separate changes.
- Domain, data, DI, navigation, permission, Room schema, backup format, migration. No health sentence changes, so no
  `medical-reviewer`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `in-app-messages`: MODIFIED "An error on the other screens goes when a later save succeeds" (body; scenarios "A write with
  its own message" and "Undo or information on screen" rewritten; new scenario "Log medication, a dose logged after freeing
  space"; every other scenario unchanged).
- `medication-intakes`: ADDED "A logged intake clears the message on Log medication", "Undo on Log medication keeps the logged
  result until the intake is removed".

## Impact

- **Shared file with #239:** `MedicationMessages` also changes in `fix-save-problem-feedback` (clear on `NotSaved`). Merge this
  change first; #239 merges `develop` before its review.
- **Code:** `features/medication/ui` (`LogMedicationScreen`, `MedicationMessages`, the event model and its mapper) and,
  for Undo's outcome, `features/medication/presentation` (`LogMedicationViewModel`, `LogMedicationPresentationNotification`).
  The ViewModel stays stateless. No domain, data, datasource, DI or navigation change expected.
- **Tests:** presentation unit tests for Undo's outcomes; Robolectric screen tests on `LogMedicationScreen` with the message
  host, each red on `develop` first.
- **Strings:** two new ones in `features/medication/ui` (ux 4.4), only with rule 2. No health claim, so no `medical-reviewer`.
- **No change to:** manifest, permissions, Room schema, backup, Free or Premium.
- **Design source:** `docs/ux/239-237-validation-and-save-feedback.md` section 4 (ux-239, 4001103f), merged into this branch.
- **BRD:** D190, EC-MED-16, EC-MED-17, EC-UX-35, §13 row 54.
- **Issue:** `Refs #237` in every commit and the PR, never a closing keyword: QA filed #237 and closes it after the retest.
- **Release:** 1.3.2 (tracker T75), with #239 and #245.
