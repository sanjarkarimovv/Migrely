# Design

Release 1.3.2, tracker T75, issue #237 (S3, pre-existing). BRD D190, EC-MED-16, EC-MED-17, EC-UX-35; D178 (1), (2), (4);
D165 (4); D34. This file binds what the person sees and what QA checks; the code shape is the engineer's call within it.

## Context

| Path | Today (`develop` 76c38c38) | Where |
|---|---|---|
| Tap a medication, stored | the page turns to the logged result; no message is posted; an error on screen stays | `LogMedicationScreen.kt:131-143`, `MedicationMessages.kt:52-58` |
| "Add and log", stored | same as above | `LogMedicationViewModel.onAddAndLogAction` → `logIntake` |
| Tap, storage full / failed / in future | message shows over the list | `LogMedicationViewModel.logIntake` |
| "Undo", stored | `logged = null` at once, then "Removed. Nothing was logged." | `LogMedicationScreen.kt:182-186`, `onUndoAction` |
| "Undo", storage full / failed | `logged = null` at once, list shows, error over it, intake still stored | same |

The screen has two collectors of `MedicationEventPresenter.events` (a `MutableSharedFlow` with a buffer of 8, no replay): the
screen's `LaunchedEffect` (page state: `logged`, `problem`, `pending`) and `MedicationMessages` (the message). `logged` and the
shown message are both `rememberSaveable`. `pending` is `remember` only, on purpose (a write still running when the screen is
recreated must not leave every button disabled).

`MedicationDao.deleteIntake(id)` deletes by id and returns nothing, so removing an intake that is already gone is `Done`.

## Goals and non-goals

Goals: EC-MED-16 and EC-MED-17 on Log medication; ViewModel stays stateless; one outcome drives both the page and the message;
each rule has a check that is red on `develop`.

Layers: presentation (Undo's outcome may need its own notification), UI (`features/medication/ui`). No domain, data,
datasource, DI or navigation change. If the engineer finds one is needed, they say why in the task record.

Non-goals: the message host (`designsystem`), message position, other screens, strings.

## Decisions

### D1. A stored intake clears every message, silently (D190 (1))

| Message on screen | After an intake is stored |
|---|---|
| "Couldn't save. Your phone's storage is full." | gone |
| "Something went wrong. Please try again." | gone |
| "Pick a time up to now." | gone |
| "Removed. Nothing was logged." | gone |
| none | none (no "Saved.") |

Why every message and not only errors: the logged result replaces the whole page, so any earlier message is about a page that
is gone and lies over the logged line (the first row under the bar). Log medication has no message with "Undo"; its Undo is a
button of the result. Why no "Saved.": the logged line is the confirmation and is already announced (polite live region);
"Saved." would cover it and say the same thing twice (D178 (1) reason for Doctor report).

The clear happens on the `Logged` outcome only. `InFuture`, `StorageFull`, `Failed` and a refused first medication
(`NotSaved`, inline) behave as today.

### D2. One outcome, both collectors (D190 (3))

`MedicationMessages` reacts to `MedicationEventUiModel.Logged` by clearing what it shows, in the same pass that the screen's
collector sets `logged`. Both are collectors of one `SharedFlow` emission, so their order does not matter: after the outcome is
handled, the logged result never shows with the old message. Alternative accepted if simpler: hoist the clear into the screen
(one collector) and pass it down. Not accepted: a ViewModel flag, a delay, or clearing on the tap.

Focus: as D178 (4), a message that goes clears its focus and moves it nowhere; the tapped button is already parked
(`rememberParkWhenPressed`, D181 (6)), so TalkBack reads the logged line.

### D3. Undo waits for its outcome (D190 (2))

| Undo outcome | Page | Message |
|---|---|---|
| removal stored | list (as today) | "Removed. Nothing was logged." (as today) |
| storage full | logged result stays, "Undo" enabled again | "Couldn't save. Your phone's storage is full." |
| other failure | logged result stays | "Something went wrong. Please try again." |
| second tap before the outcome | nothing | nothing |

The screen keeps `logged` until a success outcome for that Undo arrives. A second tap is blocked the same way the log is
(`pending`, or a guard on the intake being undone). The screen must know which outcome is Undo's: a dedicated outcome from the
ViewModel (for example `IntakeUndone` mapped to its own UI event that both drops the result and shows the message, and the
failures as today) is preferred over the screen reading message types. The ViewModel stays stateless: the screen passes the
intake id, as today.

Rotation or process death while a removal runs: `pending` is not saved, `logged` is, so the logged result shows again with
"Undo" enabled. If the removal was stored before the process died, a later "Undo" deletes nothing and returns `Done`, so the
list and "Removed. Nothing was logged." show: true. No other intake can be removed, because the id is the logged one.

### D4. What does not change

Strings, haptics on errors (still `Reject` for a new error), the message host and its timing (D182), "An error belongs to the
visit" (D178 (2)), the logged line's text and its live region, the list after a successful Undo.

## Risks

- **The Undo neighbour is a code read.** If the red test cannot make Undo fail on `develop` (for example the fake repository
  never returns `StorageFull`), it proves nothing; the engineer shows the red run with a fake that returns `StorageFull`.
- **Two collectors.** If a future change adds replay to the presenter, a `Logged` could clear a message posted after it.
  The test in 3.2 sends a `Logged` and then an error and asserts the error stays.
- **QA on a full disk.** Filling `/data` is environment-heavy (QA used `fallocate`). The device row for Undo may not be
  reachable; then it says so, and the Robolectric checks carry it.

## Acceptance (for `qa-engineer`, this diff only)

Build: the PR head. Negative control: v1.3.1 (`v1.3.1` tag), which must show the defect, or the row proves nothing.

| Row | Steps | Candidate | v1.3.1 control |
|---|---|---|---|
| A1 | Fill storage, tap Ibuprofen (error), free space, tap again | error gone ≤ 1 s; logged line in the a11y tree; 1 new intake | error stays over the result at 2 s and 8 s |
| A2 | Same with "Add and log" on an empty list | error gone; 1 medication, 1 intake | error stays |
| A3 | Undo, then log another medication within 5 s | "Removed…" gone at once | "Removed…" stays over the result |
| A4 | A1 opened from attack mode (dark room) | as A1, dark colours | error stays |
| A5 | A1 with TalkBack | TalkBack speaks the logged line; no "Saved." | logged line not spoken |
| A6 | Log, fill storage, Undo | logged result stays with the error; intake count unchanged | list shows with the error; intake still stored |
| A7 | A6, free space, Undo | list and "Removed…"; intake gone | n/a |
| A8 | A1 at 200% font, portrait and landscape | logged line, Done, Undo fully visible | n/a |
