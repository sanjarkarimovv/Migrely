# Proposal

## Why

A long press on a custom tag opens a small menu with one item, Remove. On a quick double tap on Remove, the first tap removes the tag and closes the menu.
The second tap lands on the details screen beneath and toggles whatever chip is under the finger (#161, S3). No message shows.

QA measured it on `feature/fix-dialog-second-tap` 49251530, debug, emulator API 36, with injected taps and gaps read from `getevent` (`t12_s7.jsonl`,
part `menu_remove_double`):

| Gap, first lift to second press | Tag removed | Chip beneath toggled |
|---|---|---|
| 147 ms | yes | "Sensitivity to smell" added |
| 306 ms | yes | "Sensitivity to smell" added |
| 457 ms | yes | "Sensitivity to smell" added |

Each time the database shows the `custom_tag` archived and `attack_tag ('atk-t1', 'SYMPTOM', 'smell_sensitivity')` added. The negative-control build was not
run on this row.

D161 does not cover it. The menu is a Material `DropdownMenu`, which is a popup and not one of the dialogs in `MigrelyDialogs.kt`, so a lift on it never
reaches `DialogLiftRule`. The archived change `2026-11-08-fix-dialog-second-tap` listed this under "what D161 does not cover" (task 7.10, design D5
"The menu"). It is the same mechanism as #159: the first tap closes a window, and the second lands in the window beneath.

Wrong data with no message outranks one lost tap, so the menu joins D161's rule. It is S3, so it goes into `develop` for the next release, and no release
branch is opened for it.

## What Changes

- **A lift on a popup menu's card counts like a lift on a dialog's card (D164).** It is the same `DialogLiftRule` and the same numbers: `DIALOG_WAIT_MILLIS`
  (500 ms) for the first press, the platform's double-tap timeout (300 ms today) for a burst, and `scaledDoubleTapSlop` (100 dp today) for distance, in
  screen coordinates. No new constant and no new rule. The main window's side is not touched: it already asks the rule.
- **One helper for the class, not this one menu.** `MigrelyDropdownMenu` in `designsystem`, package `dialog`, next to `DialogLiftRule`. It reports each lift
  on the menu's card to the rule and has **no** D160 wait. The custom tag menu in `AttackChoiceSections.kt` uses it.
- **A gate check for every popup.** A Konsist test fails, and names the file, when a production file outside the helper names a raw popup: Material's
  `DropdownMenu`, `ExposedDropdownMenuBox`, `TooltipBox`, Compose's `Popup`, `android.widget.PopupMenu`, `PopupWindow` or `ListPopupWindow`.
  `DropdownMenuItem` and `PopupProperties` stay free, because neither is a window. A popup that is not used today gets a helper when someone adds it,
  and the failing test points at D164.
- No visual change, no text, no semantics change, no data, no permission.

## Out of scope

- **A second tap from 500 ms up, and a third tap from 300 ms after the second's lift.** These still act and can toggle a chip. That is the residual risk,
  as in D161. The change shows on screen, and one tap puts it back. It is a symptom or trigger tag, not an intake, so it does not feed the overuse count.
- **A tap outside the menu, then a fast tap beneath.** The popup takes the outside tap and closes. The tap never reaches the card, and
  `onDismissRequest` cannot tell it from Back, as with D161 (5). QA measures it, and it is not called safe. If it writes, it goes back to `team-lead`.
- **A D160 wait on the menu.** The menu opens on a long press while the finger is still down, so the person sees it before any next tap. A wait would
  make Remove feel dead, which is the class of problem Google rejected 1.0.1 for.
- **#149** (a long press through a navigation opens the menu over the next screen). That is another mechanism: a long-press timer, not a lift, and it
  needs two fingers. It is S4 and stays open.
- **Undo or a confirmation for Remove.** Either one adds taps, and neither stops the second tap from toggling a chip.
- **Keeping the menu open across a rotation.** `menuOpen` is `remember` today, so a rotation closes the menu. Nothing is written, and that stays as it is.
- **Changing D159, D160 or D161 for dialogs,** or the main-window code.
- **Helpers for popups not used today** (tooltips, exposed menus, View popups). The check makes the next author add one.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `app-shell`: a lift on a popup menu's card counts for what is beneath it, a popup menu has no wait, and every popup goes through a helper that a
  gate check enforces.

## Impact

- `docs/BRD.md`: D164, EC-UX-31, EC-UX-32, EC-UX-30's last sentence, §13 row 33.
- `designsystem`, package `dialog`: `MigrelyDropdownMenu`, and the lift part of the dialog node shared with it. Dialog behaviour does not change.
- `features/attack/ui`: `AttackChoiceSections.kt` calls the helper instead of the raw `DropdownMenu`.
- `app/src/test/.../architecture`: the popup check, next to `ArchitectureDialogsTest`.
- `docs/ARCHITECTURE.md`: the dialog bullet gains the menu.
- No health claim and no visible text changes, so `medical-reviewer` is not needed. No Premium change. No manifest change.
- BRD ids: D164, EC-UX-30, EC-UX-31, EC-UX-32. Issue #161 (`Refs #161`).
