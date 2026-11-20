# Proposal

## Why

**#259 (S1, accessibility and silent data loss; a 1.3.2 regression, v1.3.1 does not do it).** A person with TalkBack on, the
font at 200% and a small phone in landscape (a window 360 dp tall) opens a migraine's note on Attack details by double tap.
About a second later TalkBack's focus jumps to an "Impact on your day" radio, the line being typed is 0 of 112 px, and the next
double tap writes to the database: an empty impact becomes 2, and an impact already recorded as 2 is cleared. QA saw the jump
in 8 of 8 rows; v1.3.1 in 0 of 4. 1.3.2 is NO-GO after its tag and will never be uploaded.

**Cause** (eng-259, proven on emulator-5570 from TalkBack's log and a stack trace):

1. With an accessibility service on, Compose's `semanticsChangeChecker` adds a `measureAndLayout()` pass. It lands between
   the keyboard's first frame and `KeyboardFollow`'s keyboard collector.
2. `KeyboardFollow.onPositioned` takes that layout, at an unchanged keyboard height, as what the person sees, and ends the
   follow before the line is brought into view.
3. The fully hidden note is pruned from the accessibility tree.
4. TalkBack's ENSURE_ON_SCREEN moves focus to the first focusable node.

D192's top bar stepping aside only sets up the geometry: the line is fully hidden on the keyboard's first frame. At 411 dp it
is not, and there is no jump. Prototype 1 (count a layout only at a keyboard height the follow already acted on) was not
enough: the animated bring sends `TYPE_VIEW_SCROLLED` while the note is still pruned, and TalkBack moved focus to the "Notes"
heading in 4 of 7 rows. Log medication, Edit medication and Delete all data showed no jump in 2 of 2 each, but they share the
same follow code, so they carry the same race.

## What Changes

- **Only the person ends the follow.** A layout the app or Compose makes does not end it; the field follows from the start of
  its own bring into view; a scroll away by the person while the keyboard holds still, or a drag that stops the bring, still
  ends it.
- **The line comes into view in step with the keyboard.** Inside a typing space, the page moves in the same frames as the
  keyboard, not by a spring after it, so the line never leaves the tree and TalkBack finds the field when it hears of the
  scroll. The page lands where the system's own focused-field scroll would, so nothing moves once the keyboard stops, from any
  start position. This is for everyone, TalkBack on or off: one path. The motion follows ux-259's verdict C1 to C6.
- **The keyboard never hides a focused field that shows (new path, rule (a')).** The path: the note is focused, Back closed
  the keyboard, and the page was scrolled so the note's top is near the bottom edge. A TalkBack double tap then reopens the
  keyboard without moving the cursor. On develop the note is wholly hidden, focus moves and the next tap writes (4 of 5 on
  5570). The new rule: the page holds the lesser of the field's line and what showed, frame by frame, moving with the
  keyboard and no further (ux HOLD, H1 to H5).
- **Unchanged:** the bar still steps aside (D192 R3 to R11); closing the keyboard never scrolls (#238, D189); the rest
  position of the line (D192's acceptance, 112/112 with TalkBack off); fields outside a typing space keep the old bring; the
  typing follow (D180 (2)).
- **Records.** BRD D194, EC-UX-83 to EC-UX-88 (new), EC-UX-39 (pointer), section 13 row 58.

## Out of scope

- #255 (D193, TalkBack scroll order): release 1.3.4.
- #260 (S4, follow-up): after a hold the caret line may stay cut until the first keystroke (H5), and the unchanged typing
  bring can move up to about 100 px per frame at its peak (K3), one step of about 430 px with Remove animations (K2).
- The custom tag dialog's field on Attack details: a plain dialog field with no keyboard follow.
- Any change to moving between fields while the keyboard is up, or to the follow while typing new lines (D180 (2)).
- Any new rule for the bar (R1 to R12), the keyboard room (#238), messages, strings, the Free and Premium split.
- Domain, presentation, data, DI, navigation, manifest, permission, Room schema, backup, migration. No health sentence, so no
  `medical-reviewer`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `app-shell`: ADDED "Opening a field with TalkBack keeps focus on it", "The line being typed comes into view with the
  keyboard", "Only the person ends the keyboard follow" and "The keyboard never hides a focused field that shows". No existing
  requirement changes: D192's rules hold as written.

## Impact

- **Code:** `designsystem` (`input/KeyboardFollow.kt` with `onBringing`/`onBringEnded`, `input/TypingSpace.kt` with
  `scrollingPage(scrollState)`, `KeptField`, `scrollToShow`, `focusedChildScroll`, `keyboardHides`,
  `input/CursorFollowingTextField.kt`, `input/SingleLineFieldInView.kt`), and on each of the four screens
  (`AttackDetailsScreen`, `LogMedicationScreen`, `EditMedicationScreen`, `DeleteAllDataScreen`) `pageModifier` plus
  `verticalScroll` becomes `scrollingPage(scrollState)` (design D3). Prototype 2's nested post-scroll side channel was tried
  and removed (3 of 17 openings still moved focus); BRING and the N1 hand-over were tried and rejected (design D6).
- **Tests:** the red `KeyboardFollowTest` cells (2bf5857c), `TypingSpaceScrollToShowTest`, `TypingSpaceKeyboardHidesTest`,
  `KeyboardFollowFramesTest` (C1 to C3, F1, F2, F5, H1 to H4, R1), `AttackDetailsKeyboardFramesTest` (the real screen's
  wiring), the fallback outside a typing space. `KeyboardRoom` tests and D192's tests keep every assertion; four harnesses
  moved to `scrollingPage`. The interim `PageScrollParent` tests are removed.
- **No change to:** manifest, permissions, strings, Room schema, backup, Free or Premium.
- **BRD:** D194, EC-UX-39, EC-UX-83 to EC-UX-88, section 13 row 58.
- **Issue:** `Refs #259` in every commit and the PR, never a closing keyword: QA retests and closes it. Follow-up #260.
- **Release:** 1.3.3 = `develop` 4d141abe plus this fix, its only content.
