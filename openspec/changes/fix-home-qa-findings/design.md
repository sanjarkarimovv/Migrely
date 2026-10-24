# Design

## Context

QA issues #12 to #15 (develop @ 50ce583), all in the home and calendar UI. No domain or data change is needed.

## Decisions

### D1. Guard taps at the mode switch, in the UI (EC-UX-18, D32)

| Option | Verdict |
|---|---|
| Move the buttons apart | Fragile: font size and screen height decide where they land, and at 200% "Severe" overlaps "Migraine started" too. Rejected |
| A ViewModel guard | It cannot tell a second tap of a double tap from a deliberate action, and the ViewModel is stateless. Rejected |
| **Block pointer input to the new mode's content for 1 s after a switch** | Covers every control of the new mode, also under reduce motion, where there is no fade to rely on |

`HomeContents` remembers the last mode shown. The content of a mode guards itself only when it replaces the other
interactive mode, not after Loading, so launch and rotation (where `HomeContents` starts again) need no wait. While
armed, a pointer-input modifier consumes every event in the initial pass, before any child sees it. The choice is a pure
function, `guardsTaps(previous, next)`, covered by unit tests.

### D2. Merge semantics instead of clearing them (#14, EC-UX-19)

`Modifier.clearAndSetSemantics` before `clickable` replaces the clickable's semantics, its focus semantics included, so
accessibility services see `focusable=false`. Keyboard focus still works; this was checked with Tab and Enter on History
on emulator-5570. The rows, days and the month title now:
- set `semantics { contentDescription; role }` on the clickable node, which merges its descendants;
- clear only the children's own text with `clearAndSetSemantics {}`, so the description is not read twice.

### D3. Contrast (#13)

The start-time line uses `MigrelyColors.AttackModeText` at full alpha (#8A909A on #11151F, about 5.7:1), like
"Details".

### D4. Elapsed time (#15)

`rememberNowMillis` takes the attack's start as a key. A new start restarts the producer, which reads the clock first
and then ticks every 30 s as before.

## Risks

- **A deliberate end within 1 s of a start is ignored.** That is unlikely in practice, and Undo of the start covers it.
- **Visible keyboard focus stays the default overlay.** That is enough for WCAG 2.4.7 at this stage; a stronger ring
  can come with the settings change.
