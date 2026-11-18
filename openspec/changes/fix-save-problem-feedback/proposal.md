# Proposal

## Why

**#239 (S3, accessibility, pre-existing).** Someone renames a medication on Edit medication, clears the name and taps Save
with the keyboard still up. Save is at the bottom, Name at the top, and with the keyboard open the page cannot show both.
Nothing is saved, which is right, but nothing tells them why: "Enter a name." appears under Name, off screen. With TalkBack,
nothing about the error is spoken in 8 seconds (QA, release/1.3.0 Go/No-Go T73, emulator-5580, API 36, TalkBack 16 VERBOSE,
runs r2 to r4). A sighted person sees nothing happen and taps again. During an attack, "nothing happened" reads as "the app
is broken", and the person stops logging.

**Cause (code read on `develop` 76c38c38, ux 3.1).** The error is the field's supporting text, not a live region. Material 3's
`OutlinedTextField` sets the field's error semantics to its default "Invalid input". There is no scroll to the field. Focus
moves to the field only in keyboard mode (`EditMedicationScreen.kt` `rememberProblemFocus`, D181 (6)), so a touch or TalkBack
user gets nothing.

**Same class, found by ux in code (not run):** Log medication's first-medication form ("Add and log" with an empty name, also
in the dark room from attack mode) has no focus, scroll or announcement for its Name error in any mode.

The design is `ux-designer`'s `docs/ux/239-237-validation-and-save-feedback.md` section 3 (rules F1 to F12, acceptance V1 to
V16; 4001103f), merged into this branch. Scope by `team-lead` (2026-11-18). The BRD decision is D191.

## What Changes

- **Heard once (D191 (2), F5, F7).** A field problem after Save or "Add and log" is announced exactly once by the app: by the
  field's own reading when focus moved to it, otherwise by one polite announcement. A repeat attempt is heard again. Typing
  is quiet (F8).
- **Seen (F2).** The field region (label, outline, error line) scrolls into view by the smallest amount, with the existing
  bring-into-view; nothing moves if it is already visible.
- **Keyboard and focus (F3, F4).** The app never opens or closes the soft keyboard for a problem. Focus moves to the problem
  field with a hardware keyboard (as today) and when the soft keyboard is open on another field; never with it closed.
- **Real error (F6).** The field reads its real error, never "Invalid input", as one stop with no repeat stop.
- **Newest result wins (F9).** A field problem clears any older message on the screen in the same frame, silently.
- **Screens:** Edit medication (Add and Edit) and Log medication's first-medication form (normal and dark room).
- **Records.** BRD D191, EC-UX-66 to EC-UX-71 (new), EC-UX-49 and EC-UX-50 (pointers), §13 row 55.

## Out of scope

- **Add past migraine's scroll and focus part (F2, F3).** Its time errors already announce (polite live region); pickers
  prevent most of them; the rows sit near the top of a short page. 1.3.2 carries a verification row only (ux V15); the rest
  goes to the backlog, filed by `team-lead`.
- The custom tag dialog and Delete all data (their submit stays disabled until the input is valid).
- Disabling Save for an empty name (rejected, ux 3.5 D), showing the error as a top message (rejected, ux 3.5 F).
- The message host's place (D158, D178 (4)), any string, haptics, colours, #237 (D190, its own change), #245.
- Domain, data, DI, navigation, permission, Room schema, backup, migration. No health sentence, so no `medical-reviewer`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `medication-list`: ADDED "Save problems on Edit medication are heard and seen"; MODIFIED "Edit medication with a keyboard"
  (body's BRD ids; scenarios "Save with a name problem" and "Save problem by touch" rewritten; the other ten unchanged).
- `medication-intakes`: ADDED "First-use name problems are heard and seen".
- `in-app-messages`: ADDED "A field problem replaces the message on the medication forms".

## Impact

- **Code:** `features/medication/ui` only: `EditMedicationScreen` (`MedicationForm`, `rememberProblemFocus`),
  `LogMedicationScreen` (`FirstMedicationForm`), `MedicationMessages` (clear on `NotSaved`). Possibly a small shared helper
  for "announce once per arrival" and field error semantics if both screens need it (in `features/medication/ui`, or
  `designsystem` if Add past migraine later reuses it).
- **Shared file with #237:** `MedicationMessages` also changes in `clear-message-when-intake-logged` (clear on `Logged`).
  #237 merges first; this branch merges `develop` before review (task 4.0).
- **Tests:** Robolectric screen tests in `features/medication/ui` (`testDebug`), each red on `develop` first.
- **No change to:** manifest, permissions, strings, Room schema, backup, Free or Premium.
- **BRD:** D191, EC-UX-49, EC-UX-50, EC-UX-66 to EC-UX-71, §13 row 55.
- **Issue:** `Refs #239` in every commit and the PR, never a closing keyword: QA filed #239 and closes it after the retest.
- **Release:** 1.3.2 (tracker T75), with #237 and #245.
