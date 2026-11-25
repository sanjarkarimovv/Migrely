# Proposal

## Why

**#238 (S3, accessibility, a 1.3.0 regression shipped as a known issue by `team-lead`).** With TalkBack on, a person adds a
few words to a migraine's note near the end of the details page and presses Back. The keyboard closes and TalkBack's focus
jumps to the first trigger chip: "Not selected. Stress". The person believes they are still on the note. A double tap now
marks a trigger they did not have: a wrong entry in their diary, made without noticing. QA found it in the release/1.3.0
Go/No-Go (API 36, TalkBack 16, VERBOSE log): headless 3 of 3, control 1.2.1 clean 2 of 2; windowed 0 of 9.

**Cause (proven from TalkBack's VERBOSE log).** Since #225 (`fix-keyboard-follows-cursor`, D180 (2)) the scroll's visible part
is padded by the keyboard's height. When the keyboard closes, the visible part grows; at the page's end the scroll position is
clamped, Compose sends `TYPE_VIEW_SCROLLED`, and TalkBack treats it as the person's own scroll and refocuses on the first item
in view.

The same structure exists on the three other screens with a text field (read on `develop` c49b9e09, not run):
Edit medication (`EditMedicationScreen.kt:230-232`), Log medication (`LogMedicationScreen.kt:333-335`) and Delete all data
(the visible part ends at the Delete row, `DeleteAllDataScreen.kt:210-243`). Edit medication fits the screen at 100% without
the keyboard, so there the pull is the largest and the first item in view can be a Kind radio: a double tap changes the kind
of medication. For a sighted person the page slides down by up to the keyboard's height (about 40% of a phone screen), in one
jump on Android 8 to 10, into the area where the thumb was just typing.

The design decision is `ux-designer`'s `docs/ux/238-keyboard-room.md` (rules R1 to R14, acceptance KR1 to KR16), accepted by
`team-lead` (risk order: accessibility before UX). The BRD decision is D189.

## What Changes

- **Closing the keyboard never moves the page (D189 (1)).** On Attack details, Edit medication, Log medication and Delete all
  data, the scroll position stays the same to the pixel when the keyboard closes or gets shorter. If less than the growth of
  the visible part lies below it, a "keyboard room" of the difference appears after the last item. No scroll happens, so no
  scroll event, so TalkBack, Switch Access and Voice Access keep their place.
- **The room is the page (D189 (2)).** Only the screen's own background (theme, or the dark room in attack mode). No line,
  shadow, marker or lighter colour.
- **It goes only with the person (D189 (3)).** It shrinks in step with the person's own scroll, a reopened or taller keyboard,
  or grown content; it never animates away and never grows back from scrolling.
- **It is not kept (D189 (4), (5)).** Rotation, process death and leaving the screen bring the screen back without it. No
  bottom keyboard inset: no room, ever.
- **It is invisible (D189 (6)).** No semantics, focus, Tab stop or ripple; nothing is announced; input and TalkBack focus stay
  in the field.
- **Delete all data (D189 (8)).** The field stays still; only the Delete row moves with the keyboard as D180 (8) already says.
- **Records.** BRD EC-UX-43 and D180 (5) no longer promise "no blank space"; `fix-keyboard-follows-cursor`'s requirement
  "Closing the keyboard and other keyboards" is rewritten by this change's MODIFIED delta.

**Existing part of the work:** commit a1efa2bc on this branch adds `designsystem/.../input/KeyboardRoom.kt` with the Robolectric
test `KeyboardRoomTest` and wires it on Attack details only. This change applies the rules to the other three screens and
rechecks Attack details against R2, R8 and R11.

## Out of scope

- The three clamp cases `ux-designer` found outside #238 (`docs/ux/238-keyboard-room.md` §10): a card-mode message that leaves
  at the page's end, a split-screen resize that grows the window, and content that shrinks at the page's end. None is
  reproduced; QA reproduces before anyone files them.
- TalkBack versions other than TalkBack 16, or Samsung's build, moving focus on the keyboard close itself rather than on a
  scroll. That would be a different bug.
- The keyboard's opening behaviour, the cursor-follow rule, the message's fit rule (D180 (2), (4), (7), (8), (9)): unchanged.
- Any new string, announcement, setting, permission, data or migration. No health sentence is involved, so no
  `medical-reviewer`.
- The light system keyboard over a dark page (D180 (10) F7) and the Edit medication error line that is not a live region
  (F5): unchanged.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `app-shell`: MODIFIED "Closing the keyboard and other keyboards"; ADDED "Closing the keyboard never moves the page", "The
  keyboard room looks like the page", "The keyboard room goes only with the person's scroll", "The keyboard room is not kept",
  "The keyboard room is invisible to assistive technology", "Delete all data keeps the field still".

**How this relates to `fix-keyboard-follows-cursor`.** That change (1.3.0, merged on `develop`) is not archived yet, so the
requirement "Closing the keyboard and other keyboards" lives only in its ADDED delta, not in `openspec/specs/app-shell/spec.md`.
This change's MODIFIED delta is written against the requirement as that delta defines it, keeps all twelve scenario names, and
rewrites "The keyboard closes", "The strip the keyboard uncovers", "Background and process death" and "A floating keyboard or
split screen". `openspec validate --strict` passes now; `openspec archive` of this change refuses until
`fix-keyboard-follows-cursor` is archived first. That order is intended (tasks 8.1 and 8.2).

## Impact

- **Code:** `designsystem` (`KeyboardRoom`, may need a visible-part source that is not a window inset for Delete all
  data), `features/attack/ui` (`AttackDetailsScreen`), `features/medication/ui` (`EditMedicationScreen`,
  `LogMedicationScreen`), `features/settings/ui` (`DeleteAllDataScreen`). UI layer only: no domain, data, DI or navigation.
- **Tests:** `KeyboardRoomTest` (exists), screen tests for the three new screens with a red run on the room removed.
- **No change to:** manifest, permissions, strings, Room schema, backup format, Free or Premium.
- **BRD:** D189 (new), D180 (5) and EC-UX-43 (rewritten), EC-UX-61 to EC-UX-65 (new), §13 row 53.
- **Issue:** `Refs #238` in every commit and the PR, never a closing keyword: QA filed it and closes it after the retest.
