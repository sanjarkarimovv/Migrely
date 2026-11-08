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

#### Scenario: A tap just after the wait on a dialog opened from a dialog
- **WHEN** a dialog opens from a lift on another dialog, as the time step does from Next, a second tap is ignored by its wait, and a
  tap comes just after the wait
- **THEN** the control answers, because a dialog that appeared after the lift is not beneath it and its own wait alone decides
- **AND** the wait counts from when the dialog appears, which is after the lift, so its end is later than the lift plus 500 ms, and
  later still under load: on an emulator the time step attached 62 to 113 ms after the lift, and one tap at 587 ms after the first
  lift did nothing at load 11

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
The wait SHALL be the same whatever opened a dialog, with a tap or without, because it gives a person a moment to see it. It
SHALL start again when a dialog is shown again by a rotation or a restored process. It SHALL NOT start again on a return from
Home or Recents to a dialog still open. What was typed or chosen SHALL be kept as it is today (BRD EC-UX-29, D160).

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
- **THEN** the name stays, and the wait reaches only the card
- **AND** nothing in that dialog requests focus, so whether a keyboard is still open after the rotation is what QA records

#### Scenario: A return from Recents
- **WHEN** the user leaves a dialog open, comes back from Home or Recents, and taps a control straight away
- **THEN** the control answers, because the dialog was already seen, and a double tap on a Recents card landing on a dialog's
  button is residual risk

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

### Requirement: A lift in a dialog counts for what is beneath it
A touch that lifts on a dialog's card, once the wait has let it through, SHALL count as the first tap of a double tap for every
window already there. A press in another window, the main screen or an older dialog, SHALL be ignored as a whole gesture under the wait's
500 ms constant and within Android's double-tap distance of that lift, on a screen touched or not. The distance SHALL come from the
platform and the times from the touch events. Every other press SHALL answer (BRD EC-UX-30, D161).

#### Scenario: The OK of a picker over a Log button
- **WHEN** the user double taps OK in the "Taken at" picker on Log medication, with a gap under the limit, in landscape with 4
  medications or wherever a Log button lies under the finger
- **THEN** the time is picked and no intake is written

#### Scenario: The OK of a picker over "Add migraine"
- **WHEN** the user double taps OK in the "Change when it started" or "Change when it ended" picker on Add past, in landscape, with a
  gap under the limit, and "Add migraine" lies under the finger
- **THEN** the time is picked, no migraine is created, and Add past stays open

#### Scenario: An option over a location chip
- **WHEN** the user double taps "Not at all" on the effect question with a gap under the limit, at 100% or 200% font, and a
  location chip lies under the finger on the screen beneath
- **THEN** the effect is written and no location is added

#### Scenario: A theme over a setting
- **WHEN** the user double taps a choice on the Theme dialog with a gap under the limit, and the "Track periods" switch or
  another row lies under the finger
- **THEN** the theme changes, and the setting stays on and no screen opens

#### Scenario: Restore over its own row
- **WHEN** the user double taps Restore with a gap under the limit, and "Restore the copy" lies under the finger
- **THEN** the copy is restored once, and the dialog does not open again

#### Scenario: Cancel
- **WHEN** the user double taps Cancel with a gap under the limit
- **THEN** the dialog closes and the second press does nothing, because a lift on Cancel is a lift on the card like any other

#### Scenario: A screen just opened from a dialog
- **WHEN** a dialog's button opens a new screen and the second press lands on it under the limit and within the distance
- **THEN** that press does nothing

#### Scenario: A return from a dialog's button
- **WHEN** a dialog's button returns to a touched screen and the second press lands on it under the limit and within the
  distance
- **THEN** that press does nothing, which is the one case where a press on a touched screen is ignored, and Back and returns by a
  control of the main window stay as they are

#### Scenario: A whole gesture beneath
- **WHEN** a drag, a hold or a fling starts beneath under the limit and within the distance
- **THEN** it is dead until the finger lifts, with no time limit, and writes nothing

#### Scenario: A tap after the limit
- **WHEN** the second tap comes at or after the limit and lands on a chip, a switch or a Log button
- **THEN** it writes, including a false intake with its Undo, because that is two taps, which is the residual risk

#### Scenario: A deliberate tap just after the limit
- **WHEN** the user taps a control beneath, at the same point, just after the limit, with no press ignored in between
- **THEN** the control answers, on the first tap

#### Scenario: A tap farther away
- **WHEN** a press lands beneath under the limit but beyond the distance from the lift
- **THEN** the control answers

#### Scenario: Taps inside one dialog
- **WHEN** the user taps twice quickly inside one dialog, such as two days in a calendar, or a lift closes nothing
- **THEN** both taps count as before, because the rule asks only about another window

#### Scenario: A picker over a dialog
- **WHEN** the user double taps OK or Cancel on `MigrelyDateTimePicker` opened from "Add pain level", and the second press lands
  on the older dialog under the limit and within the distance, at 100% or 200% font or in landscape
- **THEN** that press does nothing, and the older dialog's control does not run

#### Scenario: Between the date step and the time step
- **WHEN** the date step closes and a press lands on the older dialog before the time step's window is up, under the limit and
  within the distance of the lift on Next
- **THEN** that press does nothing

#### Scenario: A press ignored by the wait does not count
- **WHEN** a press is ignored by the dialog's wait, lifts, and Back then closes the dialog, and the user presses a control beneath
- **THEN** this rule does not act, because no lift was let through, and the control answers once the dialog's window has closed

#### Scenario: A dialog closed during its wait
- **WHEN** a dialog opened by a tap is closed by Back, and the user then presses a control under it in the main window
- **THEN** this rule does not act, because no lift on the card was let through, and the control answers once the dialog's window
  has closed

#### Scenario: Back
- **WHEN** a dialog is closed by Back after its wait, and the user presses a control beneath
- **THEN** this rule does not act, because Back lifts no finger on the card, and the tap lands once the dialog's window has closed:
  on an emulator 0 of 5 at 0 ms after Back's command, 2 of 5 at 100 ms, and 15 of 15 from 200 ms, with and without the rule alike

#### Scenario: A tap outside the card
- **WHEN** a tap outside the card closes the dialog after its wait, and a second tap lands beneath under the limit
- **THEN** this rule does not act, because that tap never reaches the card, and what it does is measured and is not called safe

#### Scenario: Screen readers and keyboards
- **WHEN** an accessibility service's click action or a keyboard closes a dialog, and a touch presses a control beneath
- **THEN** this rule does not act, because no finger lifted on the card, and semantics are not touched

#### Scenario: A rotation or a restored process
- **WHEN** the activity is recreated after a lift on a dialog's card
- **THEN** the lift is not kept, and the next press answers

#### Scenario: A menu on a custom tag
- **WHEN** the user long presses a custom tag and double taps Remove in its menu
- **THEN** this rule does not act, because a menu is not a dialog, and what the second tap does is measured and is not called safe

### Requirement: What the lift rule asks of a later press
A dialog that appeared after the lift SHALL NOT be covered: its own wait alone decides, and an older dialog still asks. The lift of an
ignored press SHALL move the time and the place, and the next press SHALL be judged by the platform's double-tap timeout, not 500 ms,
so re-tapping at an ordinary pace answers. A third tap from that timeout up SHALL act, and that can write (BRD EC-UX-30, D161).

#### Scenario: A burst's next press under the burst limit
- **WHEN** a third tap follows an ignored second tap, under the platform's double-tap timeout, 300 ms today, after the second's lift
  and within the distance of it
- **THEN** it does nothing either, and the next press after that timeout answers
- **AND** a burst goes on only while every gap stays under that timeout, with no limit on how many taps

#### Scenario: A burst's next press over the burst limit
- **WHEN** a third tap comes at or after the platform's double-tap timeout after the second's lift, at the same place
- **THEN** it acts, even if under 500 ms from the first lift, because a tap that does nothing invites another
- **AND** that can write, which is accepted: a false location at about 350 ms, 4 of 4 on an emulator

#### Scenario: Re-tapping twice a second
- **WHEN** a person re-taps a control beneath at about two taps a second
- **THEN** the tap after the first ignored one answers

#### Scenario: Tapping along a row
- **WHEN** a first ignored press has come under 500 ms, and the user goes on tapping along a row of chips with each tap under the
  platform's double-tap timeout after the last and within the distance of it
- **THEN** every one does nothing, because each ignored lift moves the time and the place, until a pause of that timeout

#### Scenario: A press that reached the older dialog first
- **WHEN** a dialog stays up under a new one, a second tap reaches the older one before the new window is up, and a third lands on the
  new one under the platform's double-tap timeout, inside or just after its wait
- **THEN** it does nothing, because the lift moved after the new window appeared, so that window asks too

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
- **WHEN** a production file outside the helper names a dialog from Compose or Material, by import or fully qualified
- **THEN** the check fails and names the file

#### Scenario: By hand on the internal track
- **WHEN** 1.1.0 is on the internal track
- **THEN** a person double taps the rows on a real phone, double taps a dialog's option, and taps a dialog's control as soon as it
  can be read, and a dialog that feels dead or a write from a natural double tap goes back to `team-lead` before Production

## MODIFIED Requirements

### Requirement: Every other press answers
A press SHALL answer when it comes at or after the timeout, when it lies beyond the distance, when the last lift was on the
same screen, or when no touch lifted recently in this window, unless the rule for a lift on a dialog's card ignores it. No
message or other visible text SHALL be added (BRD EC-UX-25, D159, D161).

#### Scenario: A tap from the timeout up
- **WHEN** the user taps a control on the new screen, at the same point, 300 ms or more after the lift of the tap that opened it
- **THEN** the control answers, even if the fade has not ended

#### Scenario: A slower double tap still writes
- **WHEN** the second tap comes 300 ms or more after the lift and lands on a Log button
- **THEN** the intake is written with Done and Undo, because that is a plain tap on a visible control

#### Scenario: A tap elsewhere on the new screen
- **WHEN** a tap lands on the new screen under the timeout but beyond the distance from the first
- **THEN** the control answers

#### Scenario: Two taps with no new screen
- **WHEN** the user taps twice quickly on a control that opens no screen, such as a location chip
- **THEN** both taps count as before

#### Scenario: Opened from a notification
- **WHEN** a screen opens from a notification and the user taps a control straight away
- **THEN** the control answers, because no touch lifted in this window just before it

#### Scenario: Opened from a shortcut
- **WHEN** the "start migraine" shortcut opens attack mode and the user taps a control straight away
- **THEN** this rule does not act on the press, and any wait of attack mode's own (D32) stays as it was

#### Scenario: A first launch and after process death
- **WHEN** the app starts or the process was killed and the user taps a control straight away
- **THEN** the control answers, because the time of the last lift is not kept across them

#### Scenario: A rotation between two taps
- **WHEN** the phone rotates after the first tap, which recreates the activity, and the user taps a control
- **THEN** the control answers, because the time of the last lift is not kept across it

#### Scenario: A screen that opens itself just after a tap
- **WHEN** a screen opens by itself, such as a paywall due on Home, just after a tap, and a press lands under the timeout and
  within the distance of that tap
- **THEN** that press does nothing, and the next one answers

#### Scenario: Another finger
- **WHEN** another finger presses beyond the distance while the ignored press is down
- **THEN** that finger's press answers

#### Scenario: Nothing to see
- **WHEN** a press is ignored and it is not held on a text field
- **THEN** no text, sound, haptic or layout change shows, and the new screen stays as it entered

### Requirement: Back and returns do not change
The rule SHALL NOT cover a return to a screen where a touch has already lifted, by Back, Done, Save or `popUpTo`, also after the
activity was recreated or the process restored. Touches after such a return SHALL behave as they did before this change, so the
time before a tap lands after Back SHALL not grow. A return from a dialog's button is the one exception, under D161's rule (BRD
EC-UX-26, D159, D161).

#### Scenario: A tap after Back
- **WHEN** the user taps Back and then taps a row of the screen below
- **THEN** the tap lands as it did before this change, and #148's measures do not get worse

#### Scenario: A double tap on Back
- **WHEN** the user double taps Back
- **THEN** the second tap does nothing while the leaving screen takes it, for at most the fade, and after that it lands on the
  screen below as it did before this change

#### Scenario: A double tap on Done or Save
- **WHEN** the user double taps a control that returns to the screen before
- **THEN** this rule does not act, and the leaving screen takes the second tap for at most the fade

#### Scenario: A return after the activity was recreated
- **WHEN** the user tapped to leave a screen, the phone rotates or the process is killed and restored, the user taps Back, and
  presses under the timeout and within the distance of that tap
- **THEN** the press answers, because which screens were touched survives the recreation

#### Scenario: A return to a screen never touched
- **WHEN** the user returns to a screen that a touch has never lifted on, such as one under a screen opened from a shortcut
- **THEN** the rule can act on one press under the timeout and within the distance, which is accepted as harmless

#### Scenario: A return from a dialog's button
- **WHEN** a dialog's button returns to a touched screen, and a press lands there under the dialog lift rule's limit and within the
  distance of that lift
- **THEN** the press does nothing, as D161 says, while a return by Back or by a control of the main window stays as above

### Requirement: The double-tap rule is touch only
The rule SHALL act only on pointer input in this window. Semantics SHALL NOT change, so TalkBack, the click action of switch
access and keyboards SHALL work at any moment. Dialogs and popups are separate windows and SHALL NOT be covered (BRD EC-UX-24,
D155, D159).

#### Scenario: TalkBack on a new screen
- **WHEN** an accessibility service performs the click action on a control of a screen just entered, under the timeout
- **THEN** the action runs

#### Scenario: Keyboard
- **WHEN** a keyboard activates a control of a screen just entered, under the timeout
- **THEN** the control answers

#### Scenario: Nothing added to the tree
- **WHEN** a screen goes through the guard
- **THEN** its semantics tree and its layout are the same as without the guard

#### Scenario: A dialog
- **WHEN** the first tap opens a dialog and the second lands on it
- **THEN** this rule does not act, so the second tap reaches the dialog
- **AND** the dialog's own wait ignores it (D160), and a lift on the dialog's card counts for what is beneath it (D161)
