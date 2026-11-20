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

- **A layout the app or Compose makes does not end the follow.** Only a layout at a keyboard height the follow has already
  acted on records what the person sees; a scroll away by the person while the keyboard holds still still ends it.
- **The line comes into view in step with the keyboard.** Inside a typing space, the page moves in the same frames as the
  keyboard, not by a spring after it, so the line never leaves the tree and TalkBack finds the field when it hears of the
  scroll. This is for everyone, TalkBack on or off: one path. The motion follows ux-259's verdict C1 to C6, with one
  accepted limit: a note that starts wholly in view at the page's end settles once, upward, by at most 32 px (L1 to L5).
- **Unchanged:** the bar still steps aside (D192 R3 to R11); closing the keyboard never scrolls (#238, D189); the rest
  position of the line (D192's acceptance, 112/112 with TalkBack off); fields outside a typing space keep the old bring.
- **Records.** BRD D194, EC-UX-83 to EC-UX-87 (new), EC-UX-39 (pointer), section 13 row 58.

## Out of scope

- #255 (D193, TalkBack scroll order): release 1.3.4.
- Removing the page-end settle (D4 limit L1 to L5): QA files an S4 follow-up (ux/motion, 1.3.4 or later).
- The custom tag dialog's field on Attack details: a plain dialog field with no keyboard follow.
- Moving between fields while the keyboard is up (no follow; the old path applies) and the follow while typing new lines
  (D180 (2)): kept as they are.
- Any new rule for the bar (R1 to R12), the keyboard room (#238), messages, strings, the Free and Premium split.
- Domain, presentation, data, DI, navigation, manifest, permission, Room schema, backup, migration. No health sentence, so no
  `medical-reviewer`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `app-shell`: ADDED "Opening a field with TalkBack keeps focus on it", "The line being typed comes into view with the
  keyboard" and "Only the person ends the keyboard follow". No existing requirement changes: D192's rules hold as written.

## Impact

- **Code:** `designsystem` (`input/KeyboardFollow.kt`, `input/TypingSpace.kt` with `page(scrollState)`, `KeptField` and
  `scrollToShow`, `input/CursorFollowingTextField.kt`, `input/SingleLineFieldInView.kt`), and one line on each of the four
  screens (`AttackDetailsScreen`, `LogMedicationScreen`, `EditMedicationScreen`, `DeleteAllDataScreen`): `pageModifier`
  becomes `page(scrollState)` with the page's own `ScrollState` (design D3, explicit route, 4d1d0430). Prototype 2's nested
  post-scroll side channel was tried and removed (3 of 17 openings still moved focus).
- **Tests:** the red `KeyboardFollowTest` cells (2bf5857c), `TypingSpaceScrollToShowTest`, `KeyboardFollowFramesTest` (C1 to
  C3), per-screen `PageScrollParent` tests, the fallback outside a typing space; `KeyboardRoom` tests and D192's tests stay
  green, unedited, as guards.
- **No change to:** manifest, permissions, strings, Room schema, backup, Free or Premium.
- **BRD:** D194, EC-UX-39, EC-UX-83 to EC-UX-87, section 13 row 58.
- **Issue:** `Refs #259` in every commit and the PR, never a closing keyword: QA retests and closes it.
- **Release:** 1.3.3 = `develop` 4d141abe plus this fix, its only content.
