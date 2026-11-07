# Proposal

## Why

A quick double tap on a control that opens a dialog: the first tap opens it, and the second lands on whatever in the dialog sits
under the finger (#157, S2). So a confirmation can pass with nobody having read it.

- **Restore the copy, landscape:** replaces what is on the phone, 6 of 6. This dialog exists to stop exactly that.
- **"How well did it help":** an effect answer is written, 3 of 3 in each of three layouts.
- **Add pain level, 200% font:** a pain level is added, 3 of 3.
- **Theme:** the theme changes, 6 of 6 in each of three layouts.
- **Add your own symptom:** the dialog opens and closes at once on Cancel. A date picker closed in 2 of 4 from a second tap outside
  its card. Neither wrote, but a control that seems to do nothing is what Google rejected 1.0.1 for (#146).

QA measured this on an emulator with injected taps, in gaps of about 140 to 500 ms. How often it happens to a person is unknown.
`docs/QA.md` blocks a release on an open S2, and data loss outranks speed, so this ships in 1.1.0 (`team-lead`).

D159 (#156) does not reach this: a dialog is a window of its own, and that rule lives in the main window's `screen()`.

## What Changes

- **Every dialog ignores touches on its content for 500 ms from when it appears.** All 10 of them, not only the measured ones, so a
  new layout or font size cannot reopen this.
- **One helper and one constant in `designsystem`.** A test fails if a file opens a dialog another way.
- **A tap outside the card does not close the dialog during the wait.** Back closes it at once.
- **Nothing changes on screen,** during the wait or when it ends. No dimming, no flicker and no text.
- **Touch only.** TalkBack, the click action of switch access and a keyboard work at once.
- **The wait is the same however the dialog appeared,** and starts again after a rotation or a restored process.

No new string, permission or schema.

## Out of scope

- **A second tap after the wait.** From there it is a plain tap on a visible control, and it runs. QA measures where writes come back,
  and that is residual risk in the Go/No-Go.
- **A double tap on a dialog's own button, where the second tap lands on the screen under it.** Neither rule reaches it. It has not
  been measured, so it is not assumed safe. QA measures it now (task 7.5), and the result could change this scope.
- **`DropdownMenu` on a custom tag.** It opens on a long press, so one tap cannot open it. #149 stays open.
- **Dialogs of a leaving screen** (#149).
- **D159 and #156,** a separate change. Neither depends on the other, and whichever merges second keeps the BRD in numeric order.
- **Changing what a dialog says or asks,** or adding a typed confirmation to Restore.
- **`TapGuard`'s existing uses** (D32, D114, D157) stay as they are.
- **Measuring how often people double tap,** or how fast people act on a dialog.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `app-shell`: every dialog waits before it takes a touch.

## Impact

- `docs/BRD.md`: D160, EC-UX-27 to 29, §13 row 31.
- `designsystem`: the helper, the constant, and `MigrelyDateTimePicker` and `MigrelyDateRangePicker`.
- `features/attack/ui`, `features/backup/ui`, `features/reminders/ui`, `features/settings/ui`: their dialogs go through the helper.
- `app/src/test`: a Konsist test.
- `docs/ARCHITECTURE.md` section 8.
- No health claim changes, so `medical-reviewer` is not needed. No visible text changes.
- BRD ids: D160, EC-UX-27, EC-UX-28, EC-UX-29. Issue #157.
