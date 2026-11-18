# Design

Release 1.3.2, tracker T75, issue #239 (S3, accessibility, pre-existing). BRD D191, EC-UX-49, EC-UX-50, EC-UX-66 to
EC-UX-71; D178 (4), D180 (2) and ux U1, D181 (6), D189. The behaviour is `ux-designer`'s
`docs/ux/239-237-validation-and-save-feedback.md` section 3 (F1 to F12) and section 5 (V1 to V16), merged into this branch
(4001103f). This file binds what the person sees and what QA checks; the code shape is the engineer's call within it.

## Context

| Screen | Field problems | Spoken today | In view today | Focus today |
|---|---|---|---|---|
| Edit medication (Add, Edit) | Name: empty ("Enter a name."), duplicate, archived; dose over 20 | no; field semantics say "Invalid input" | no scroll | keyboard mode only (`rememberProblemFocus`) |
| Log medication first use | Name: empty | no | no scroll | none; "Add and log" parks focus (`rememberParkWhenPressed`) |
| Add past migraine | time rows, overlap card | yes (polite live regions) | card yes, row not scrolled | none for a row |

`SaveMedicationUseCase` returns one result (Name, then dose, then duplicates), so at most one field error shows. Editing
clears the problem (`onFormChange` → `problem = null`). `problemArrivals` counts `NotSaved` results.

## Goals and non-goals

Goals: F1 to F12 on Edit medication and Log medication's first-use form; one shared mechanism for both; a check for each rule
that can fail on `develop`.

Layers: UI only (`features/medication/ui`). No domain, presentation, data, DI or navigation change.

Non-goals: Add past migraine's F2 and F3 (backlog), the message host, strings, #237.

## Decisions

### D1. One effect per arrival (F2, F3, F5, F7, F12)

Everything that reacts to a problem runs once per `NotSaved` arrival, keyed on the arrival count (the pattern of
`rememberProblemFocus`), after the result, never on the press and never while pending:

| Condition at arrival | Focus | Scroll | Announcement |
|---|---|---|---|
| keyboard mode | to the problem field (today) | via focus | the field's own reading (F6) |
| touch, soft keyboard open, problem field focused | unchanged | bring the field region into view | one polite announcement |
| touch, soft keyboard open, another field focused | to the problem field, cursor at end, no selection | via focus, region in view | the field's own reading |
| touch, soft keyboard closed | none | bring the field region into view | one polite announcement |

"Soft keyboard open" is read from the IME inset (`WindowInsets.isImeVisible`), as the keyboard room does. Log medication
first use: "Add and log" keeps its park-on-press (D181 (6)); in keyboard mode the move to Name follows in the same result
frame.

Note on the two "open" rows: when input focus moves, TalkBack may or may not read the newly focused field. QA's V1 counts
utterances; if the device shows zero app-caused utterances in the "another field focused" row, the engineer adds the polite
announcement there too and records it. The rule is exactly one, never zero.

### D2. The announcement is its own node, not the field (F5, F6, F8)

A `liveRegion` on the supporting text would make the whole merged field a live region and speak every keystroke (V7's
mutation). The announcement needs its own mechanism that fires once per arrival with the problem text (for example an
invisible polite live-region node whose text is set on arrival and cleared after, or a stamped text so a repeat arrival
re-announces). It must not be a swipe stop (F6). Android's deprecated announce call is not used (ux 3.5 E).

### D3. The field's semantics carry the real error (F6)

The field's error semantics are set to the problem text, replacing Material's default "Invalid input"; the error stays one
stop with the field. Check with the merged and unmerged semantics trees.

### D4. Field problem clears older messages (F9)

`MedicationMessages` clears what it shows on `MedicationEventUiModel.NotSaved`, in the same frame, with no haptic and no fade.
Both screens share the host. #237 adds the same for `Logged`; the two changes touch the same collector, so this branch merges
`develop` after #237 lands and resolves there (task 4.0).

### D5. Scroll region and priority (F2, ux U1)

The region is the field outline with its floating label down to the bottom of the supporting line. Where it is taller than
the visible part (200% landscape with the keyboard), U1's order applies: input line, then error line, then label. This is
the existing `SingleLineFieldInView` behaviour for a focused field; the touch rows reuse it with a bring-into-view request.
With Remove animations it is one step. The keyboard room (D189) is not touched.

## Risks

- **Double speech.** TalkBack may also read Name when its own focus follows the scroll (ux 3.3 step 4, the #238 mechanism).
  Accepted: it is TalkBack's reading, lands on the field to fix, and a double tap there writes nothing. QA records it
  separately (ux section 5 "Counting speech").
- **Save's short disabled state** may be announced ("dimmed") before the error (ux 9.2). If V1 shows it, the engineer may skip
  the disabled state for results that read no database, and records the choice.
- **Gboard flicker** when input focus moves from Usual dose to Name (ux 9.3). V4 shows it; not a fail unless text goes to
  the wrong field.
- **Shared file with #237** (`MedicationMessages`): merge order in task 4.0.

## Acceptance (for `qa-engineer`, this diff only)

Rows V1 to V16 of `docs/ux/239-237-validation-and-save-feedback.md` section 5, with its speech-counting rule. Build: the PR
head. Negative control: v1.3.1 (`v1.3.1` tag); V1, V2, V3, V4, V9, V10 and V11 must show the defect there, or the row proves
nothing and says so. V7 needs its mutation build (a live region on the whole field) to fail. V15 is a guard: Add past
migraine's announcement must be the same on both builds; a row out of view at 200% landscape is evidence for the backlog
item, not a 1.3.2 fail. API 36 for all rows, API 26 for V1 and V2.
