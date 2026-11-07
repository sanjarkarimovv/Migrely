## ADDED Requirements

### Requirement: A dialog waits before it takes a touch
Every dialog SHALL ignore touches on its content for 500 ms from when it appears, and a press begun in that time SHALL stay
ignored until it lifts. One shared helper and one constant SHALL set this, so a new dialog has it by default. Nothing SHALL
change on screen during the wait or when it ends, and no text SHALL be added (BRD EC-UX-27, D160).

#### Scenario: Restore in landscape
- **WHEN** an automatic copy exists, the phone is in landscape, and the user double taps "Restore the copy" over where Restore
  will appear, with the second tap inside the wait
- **THEN** the dialog opens and waits, and nothing on the phone is replaced

#### Scenario: How well did it help
- **WHEN** the user double taps an intake's row, at 100% or 200% font or in landscape, with the second tap inside the wait
- **THEN** the dialog opens and waits, and no effect is written

#### Scenario: Add pain level at 200% font
- **WHEN** the user double taps Add pain level at 200% font, with the second tap inside the wait
- **THEN** the dialog opens and waits, and no pain level is added

#### Scenario: Theme
- **WHEN** the user double taps the Theme row, at 100% or 200% font or in landscape, with the second tap inside the wait
- **THEN** the dialog opens and waits, and the theme stays

#### Scenario: Add your own symptom
- **WHEN** the user double taps "Add your own", with the second tap inside the wait on Cancel
- **THEN** the dialog stays open

#### Scenario: From the date to the time
- **WHEN** the user double taps Next on the date step, with the second tap inside the wait
- **THEN** the time step opens and waits, and no time is picked

#### Scenario: Any other dialog
- **WHEN** a new or changed dialog is added, or another font size or layout puts a button under the finger
- **THEN** it waits too, because a check fails if a dialog does not use the helper

#### Scenario: A press held past the wait
- **WHEN** a press begins inside the wait and lifts after it
- **THEN** it does nothing, and the next press answers

#### Scenario: A tap just after the wait
- **WHEN** the user taps a control in the dialog just after the wait
- **THEN** the control answers, on the first tap

#### Scenario: A person who is really fast
- **WHEN** a person who has read nothing yet, or knows the dialog, taps a control inside the wait
- **THEN** that tap does nothing, with no ripple, text or sound, and the next tap works

#### Scenario: A slower double tap
- **WHEN** the second tap comes after the wait and lands on a dialog's button
- **THEN** the button runs, because from there it is a plain tap on a visible control, which is the residual risk

#### Scenario: Nothing to see
- **WHEN** a dialog is in its wait, or the wait ends
- **THEN** it looks the same: no dimming, flicker, change of colour or change of layout

#### Scenario: A drag inside the wait
- **WHEN** a drag begins inside the wait on a dialog that scrolls
- **THEN** that gesture does nothing, and the next one scrolls

#### Scenario: Controls that read a press themselves
- **WHEN** a press inside the wait lands on a slider, a clock dial or a day in a calendar
- **THEN** nothing is written, and any of these that can still move is recorded with what it did

#### Scenario: A short tap on a text field
- **WHEN** a short tap inside the wait lands on the name field of "Add your own"
- **THEN** the field takes no focus and no keyboard opens, and the next tap focuses it

#### Scenario: A press held on a text field
- **WHEN** a press inside the wait is held still on a text field for about half a second
- **THEN** long-press selection can still start, with a haptic, which is accepted as in D159, and the text does not change

#### Scenario: Remove animations
- **WHEN** "Remove animations" is on
- **THEN** the wait is the same, because it does not depend on an animation

#### Scenario: A slow frame
- **WHEN** the phone stalls while a dialog opens or during the wait
- **THEN** the wait counts from when the dialog appears, and a press is judged by the time on its own touch event

#### Scenario: The clock moves
- **WHEN** the date, the time zone or the clock changes during the wait
- **THEN** the wait is unchanged, because it does not follow the clock

### Requirement: A dialog with no first tap waits the same
The wait SHALL be the same however a dialog came to appear, because it gives a person a moment to see the dialog. It SHALL
start again when a dialog is shown again by a rotation or a restored process. What was typed or chosen in the dialog SHALL be
kept as it is today (BRD EC-UX-29, D160).

#### Scenario: After the file picker
- **WHEN** the system file picker returns a file, the import dialog appears, and a tap lands on it inside the wait
- **THEN** that tap does nothing, so a double tap on the file cannot run Replace

#### Scenario: A rotation with a dialog open
- **WHEN** the phone rotates while a dialog is open and a tap lands inside the new wait
- **THEN** that tap does nothing, and the next tap answers

#### Scenario: A restored process
- **WHEN** the process is killed and restored with a dialog open and a tap lands inside the wait
- **THEN** that tap does nothing, and the next tap answers

#### Scenario: What was typed
- **WHEN** a name is typed in "Add your own" and the phone rotates
- **THEN** the name stays and an open keyboard still types during the wait, because the keyboard is a window of its own

### Requirement: Leaving a dialog during the wait
During the wait a tap outside the dialog's card SHALL NOT close it. Back SHALL close it at once. After the wait a tap outside
SHALL close it as before, where the dialog allows that. A press begun outside inside the wait SHALL NOT close it when it lifts
after the wait (BRD EC-UX-28, D160).

#### Scenario: A second tap outside the card
- **WHEN** the user double taps a control that opens a dialog, and the second tap lands outside the card inside the wait
- **THEN** the dialog stays open

#### Scenario: A press outside held past the wait
- **WHEN** a press begins outside the card inside the wait and lifts after it
- **THEN** the dialog stays open

#### Scenario: A tap outside after the wait
- **WHEN** the user taps outside the card after the wait
- **THEN** the dialog closes as before

#### Scenario: Back during the wait
- **WHEN** the user presses Back, by gesture or button, inside the wait
- **THEN** the dialog closes at once and nothing is written

#### Scenario: A full-screen dialog
- **WHEN** the date range picker shows, which has no outside
- **THEN** its handling of the keyboard is unchanged, in portrait and in landscape

### Requirement: The dialog wait is touch only
The wait SHALL act only on pointer input in the dialog's window. Semantics SHALL NOT change, so TalkBack, the click action of
switch access and a keyboard SHALL work at once (BRD EC-UX-27, D160).

#### Scenario: TalkBack inside the wait
- **WHEN** an accessibility service performs the click action on a control of a dialog inside the wait
- **THEN** the action runs

#### Scenario: A keyboard inside the wait
- **WHEN** a keyboard activates a control of a dialog inside the wait
- **THEN** the control answers

#### Scenario: Nothing added to the tree
- **WHEN** a dialog goes through the helper
- **THEN** its semantics tree and its size and place are the same as without it

### Requirement: The main window after a dialog
The dialog wait and the rule for the second tap of a double tap SHALL stay separate: touches in a dialog's window SHALL NOT
count as lifts for the main window's rule. Nothing here SHALL claim that a double tap on a dialog's own button is safe (BRD
EC-UX-27, D159, D160).

#### Scenario: A dialog closed during its wait
- **WHEN** a dialog opened by a tap is closed by Back, and the user then presses a control under it in the main window
- **THEN** the control answers, because a touch has already lifted on that screen

#### Scenario: A double tap on a dialog's own button
- **WHEN** the user double taps a dialog's button after the wait, the first tap closes the dialog, and the second lands on the
  screen under it
- **THEN** neither rule acts, and what it does is measured, and a write there is a new issue and not a pass

### Requirement: The dialog check can fail
The check SHALL include a negative control: with the guard removed, the tests on the guard SHALL fail, and QA's dialog rows SHALL
write again on debug and on R8. A check SHALL fail if a dialog is opened other than through the helper. A person SHALL try it by
hand on the internal track. Reports SHALL say what emulator runs with injected taps do not prove (BRD D160).

#### Scenario: Guard removed
- **WHEN** QA's dialog rows run on a build with only the guard removed, inside the wait
- **THEN** writes come back on every row that wrote in #157

#### Scenario: Guard in place
- **WHEN** QA's dialog rows run on the build under test
- **THEN** no row writes inside the wait, and a tap just after it answers

#### Scenario: A raw dialog
- **WHEN** a file outside the helper calls a dialog from Compose or Material directly
- **THEN** the check fails and names the file

#### Scenario: By hand on the internal track
- **WHEN** 1.1.0 is on the internal track
- **THEN** a person double taps the rows on a real phone, and taps a dialog's control as soon as it can be read, and a dialog that
  feels dead or a write from a natural double tap goes back to `team-lead` before Production
