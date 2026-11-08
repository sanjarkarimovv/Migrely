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

**The same family the other way (#159, S2).** A double tap on a dialog's own option: the first tap closes the dialog, and the second lands
on the screen beneath. Neither D159 nor the wait stops it. Measured on 4bb7127a, debug, with D159: the OK of Log medication's "Taken at" picker over a
Log button writes a false intake, 6 of 6 in landscape with 4 medications. Add past's picker OK over "Add migraine" in landscape creates a migraine, 4 of 4. "Not at all" over the "Right side" chip adds a false location. Theme over "Track periods" turned that setting off and on, 3 of 3 double taps at 100%. Single taps did neither. By read
of the code, a picker over `AddPainDialog` has the same shape. `team-lead` put this on this branch, for one QA pass.

## What Changes

- **Every dialog ignores touches on its content for 500 ms from when it appears.** All 10 of them, not only the measured ones, so a
  new layout or font size cannot reopen this.
- **One helper and one constant in `designsystem`.** A test fails if a file opens a dialog another way.
- **A tap outside the card does not close the dialog during the wait.** Back closes it at once.
- **Nothing changes on screen,** during the wait or when it ends. No dimming, no flicker and no text.
- **Touch only.** TalkBack, the click action of switch access and a keyboard work at once.
- **The wait is the same whatever opened the dialog,** and starts again after a rotation or a restored process. A return from Home or Recents to
  a dialog still open does not wait again.
- **A lift on a dialog's card counts for whatever is beneath it (D161).** A press in another window, under the wait's 500 ms and within Android's
  double-tap distance (100 dp today) of that lift, is ignored. A tap after that is two taps and writes. After an ignored press the next is judged by
  the platform's double-tap timeout, 300 ms today, so a third tap from then on acts. A dialog that appeared after the lift has only its own wait. D159's 300 ms stays.

No new string, permission or schema.

## Out of scope

- **A second tap after the wait.** From there it is a plain tap on a visible control, and it runs. QA measures where writes come back,
  and that is residual risk in the Go/No-Go.
- **A second tap on a dialog's option from 500 ms up.** It is two taps and writes, including a false intake with its Undo. On `effect_row`, 11 of the
  41 measured writes are here. That is residual risk in the Go/No-Go.
- **A tap outside the card, or Back, as a first tap.** Neither lifts on the card. A double tap outside is measured (task 7.10) and not called safe.
- **`DropdownMenu` on a custom tag.** It opens on a long press, so one tap cannot open it. A double tap on its Remove is measured (task 7.10), and
  the scope widens only if it writes. #149 stays open.
- **Dialogs of a leaving screen** (#149).
- **#156 itself,** a separate change that merges to `develop` first. This branch merges it in, because D161 builds on its rule.
- **Changing what a dialog says or asks,** or adding a typed confirmation to Restore.
- **`TapGuard`'s existing uses** (D32, D114, D157) stay as they are.
- **Measuring how often people double tap,** or how fast people act on a dialog.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `app-shell`: every dialog waits before it takes a touch, and a lift on a dialog's card counts for what is beneath it.

## Impact

- `docs/BRD.md`: D160, D161, EC-UX-27 to 30, §13 row 31.
- `app`: `ScreenDestination.kt` and `TouchesOnlyWhile.kt`, from `feature/fix-double-tap-writes`, ask about a lift in a dialog.
- `designsystem`: the helper, the constant, and `MigrelyDateTimePicker` and `MigrelyDateRangePicker`.
- `features/attack/ui`, `features/backup/ui`, `features/reminders/ui`, `features/settings/ui`: their dialogs go through the helper.
- `app/src/test`: a Konsist test.
- `docs/ARCHITECTURE.md` section 8.
- No health claim changes, so `medical-reviewer` is not needed. No visible text changes.
- BRD ids: D160, D161, EC-UX-27, EC-UX-28, EC-UX-29, EC-UX-30. Issues #157 and #159.
