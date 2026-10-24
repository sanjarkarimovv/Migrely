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

`HomeContents` decides the guard from the target mode in the same composition as the switch:
- `remember(mode) { guardsTaps(shown, mode) }`, then `SideEffect { shown = mode }` with a plain holder, so a quick switch
  back, for example Undo within the fade, never leaves a stale previous mode behind (review M1);
- one `TapGuard` from `:designsystem` (`input` package) lies above both modes and below the messages, so Undo stays
  tappable. It swallows pointer events in the initial pass for 1 s, or until the 1.2 s fade ends when animations are on,
  because the leaving mode stays touchable until then (review L1).

There is no guard after Loading, so launch and rotation, where `HomeContents` starts again, need no wait. `guardsTaps` has
unit tests, and `TapGuard` has a Compose UI test: taps within the guard do nothing, taps after it work, and a new key arms
it again, for a Button, a TextButton and a selectable.

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
