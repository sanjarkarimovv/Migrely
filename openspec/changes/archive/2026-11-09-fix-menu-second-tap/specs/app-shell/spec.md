## ADDED Requirements

### Requirement: A lift on a popup menu counts for what is beneath it
A touch that lifts on a popup menu's card SHALL count as the first tap of a double tap for every other window, under the same
rule, 500 ms constant, burst timeout and distance as a lift on a dialog's card. The lift SHALL be recorded before the item's
action runs. What lies beneath SHALL NOT change the rule, which asks only about the place on the screen and the time (BRD
EC-UX-31, D164).

#### Scenario: Remove over a chip, at the measured gaps
- **WHEN** the user long presses a custom tag and double taps Remove, with about 150, 300 or 450 ms from the first lift to the
  second press, and a chip such as "Sensitivity to smell" lies under the finger on the details screen
- **THEN** the tag is removed from the list, the chip stays as it was, and no message shows

#### Scenario: A single tap on Remove
- **WHEN** the user taps Remove once
- **THEN** the tag is removed from the list and nothing else changes

#### Scenario: A chip that slides under the finger
- **WHEN** removing the tag moves the next chip into the place under the finger, and the second press lands on it under the limit
- **THEN** that press does nothing, because the rule asks about the place, not the control

#### Scenario: A switch or a button beneath
- **WHEN** the second press under the limit lands on a switch, or on a button that writes or opens something
- **THEN** the switch does not change, nothing is written, and nothing opens

#### Scenario: Nothing beneath
- **WHEN** the second press under the limit lands on empty space, or a drag starts there
- **THEN** nothing happens, the drag is dead until the finger lifts, and the next drag scrolls

#### Scenario: The screen changes while the menu closes
- **WHEN** the screen beneath recomposes, moves or scrolls between the lift on Remove and the second press
- **THEN** the press is judged by the place on the screen and the event time of that lift, and does nothing under the limit

#### Scenario: Landscape and 200% font
- **WHEN** the phone is in landscape, at 200% font, or both, so the menu opens above or below the tag and Remove lies over another
  chip
- **THEN** the second press under the limit does nothing, as in portrait at 100%

#### Scenario: A burst under the burst timeout
- **WHEN** a third tap follows the ignored second one under the platform's double-tap timeout, 300 ms today, after the second's lift
  and within the distance of it
- **THEN** it does nothing either, and the next press after that timeout answers

#### Scenario: A burst over the burst timeout
- **WHEN** a third tap comes at or after the platform's double-tap timeout after the second's lift, at the same place
- **THEN** it acts, even under 500 ms from the first lift, and it can change a chip, which is accepted as in D161

#### Scenario: A deliberate tap after the limit
- **WHEN** the user taps the chip beneath, at the same point, at or after the limit, such as 600 ms or more after the lift on
  Remove, with no press ignored in between
- **THEN** the chip answers on the first tap

#### Scenario: A slower double tap still changes a chip
- **WHEN** the second tap comes at or after the limit and lands on a chip
- **THEN** the chip changes, which is the residual risk: the change shows, and one tap puts it back

#### Scenario: A tap farther away
- **WHEN** a press lands beneath under the limit but beyond the distance from the lift
- **THEN** the control answers

#### Scenario: A rotation or a restored process with the menu open
- **WHEN** the activity is recreated while the menu is open, or just after a lift on it
- **THEN** the menu is closed, nothing is written, the lift is not kept, and the next press answers

#### Scenario: Nothing to see
- **WHEN** a press beneath is ignored
- **THEN** no text, sound, haptic or layout change shows, and the menu looks and moves as before

### Requirement: A popup menu answers at once
A popup menu SHALL have no wait: an item SHALL answer the first tap however soon after the menu appears. The guard SHALL act
only on pointer input, so semantics stay, and TalkBack, the click action of switch access and a keyboard SHALL work at once and
SHALL NOT count as a lift. A tap outside and Back SHALL close the menu as before (BRD EC-UX-32, D164).

#### Scenario: Remove straight after the long press
- **WHEN** the user lifts the finger of the long press and taps Remove at once
- **THEN** the tag is removed, on the first tap

#### Scenario: TalkBack removes a tag
- **WHEN** TalkBack runs the chip's "Remove from my list" action, and then activates another chip at once
- **THEN** the tag is removed, and the other chip answers at once

#### Scenario: A click action or a keyboard on Remove
- **WHEN** an accessibility service's click action, switch access's click or a keyboard activates Remove, and a touch then presses a
  chip beneath
- **THEN** the tag is removed, and the chip answers, because no finger lifted on the menu

#### Scenario: Switch access point scan
- **WHEN** switch access's point scan taps Remove, which injects a real touch
- **THEN** that lift counts as any touch does, and a later point scan tap answers once the limit has passed

#### Scenario: A tap outside the menu
- **WHEN** the user taps outside the menu
- **THEN** the menu closes and the control under that tap does not change
- **AND** a second fast tap beneath is not covered, because the tap outside never reaches the card, and what it does is measured
  and is not called safe

#### Scenario: Back
- **WHEN** the user presses Back with the menu open, and then taps a control beneath
- **THEN** the menu closes, this rule does not act, and the tap lands once the menu's window has closed

#### Scenario: A second tap on the closing menu
- **WHEN** the second tap lands on Remove while the menu is still fading out
- **THEN** Remove may run again, the tag is removed once in effect, no message shows, and nothing else changes

#### Scenario: Nothing added to the tree
- **WHEN** a menu goes through the helper
- **THEN** its semantics tree and its size and place are the same as without it

#### Scenario: Remove animations
- **WHEN** "Remove animations" is on, so the menu closes at once
- **THEN** a press beneath under the limit still does nothing, because the rule counts from the lift and not from an animation

#### Scenario: A slow frame or a moved clock
- **WHEN** the phone stalls, or the date, the time zone or the clock changes, between the two taps
- **THEN** the rule is unchanged, because it reads the touch events' own times

### Requirement: Every popup goes through a helper
One helper in `designsystem` SHALL open the app's popup menus and apply the lift rule. A check in the gate SHALL fail, naming
the file, when a production file outside the helper names a raw popup: Material's `DropdownMenu`, `ExposedDropdownMenuBox` or
`TooltipBox`, Compose's `Popup`, or a View popup. `DropdownMenuItem` and `PopupProperties` SHALL stay free (BRD EC-UX-32, D164).

#### Scenario: The custom tag menu
- **WHEN** the gate runs on this change
- **THEN** the custom tag menu opens through the helper and the check passes

#### Scenario: A new raw popup
- **WHEN** a production file outside the helper names a raw popup, by import or fully qualified
- **THEN** the check fails and names the file

#### Scenario: A menu item or popup properties
- **WHEN** a production file names `DropdownMenuItem` or `PopupProperties`
- **THEN** the check passes, because neither is a window

### Requirement: The menu check can fail
The check SHALL include a negative control: with only the menu's lift removed, the tests on the helper SHALL fail and QA's menu
rows SHALL write again on debug and on R8. QA SHALL read each trial by its measured gap. Reports SHALL say what emulator runs
with injected taps do not prove (BRD D164).

#### Scenario: Lift removed
- **WHEN** QA's menu rows run on a build with only the menu's lift removed, at about 150, 300 and 450 ms
- **THEN** the chip beneath changes again on each gap, and if it does not, the run proves nothing and goes back to `team-lead`

#### Scenario: Lift in place
- **WHEN** QA's menu rows run on the build under test
- **THEN** no row changes a chip under the limit, and a tap from 600 ms answers on the first tap

#### Scenario: A raw popup added locally
- **WHEN** a raw `DropdownMenu` is added to a feature file, locally and never committed
- **THEN** the popup check fails and names that file

#### Scenario: What is not proved
- **WHEN** the report is written
- **THEN** it says that a human hand, a real phone and how often this happens are not proved

## MODIFIED Requirements

### Requirement: A lift in a dialog counts for what is beneath it
A touch that lifts on a dialog's card, once the wait has let it through, SHALL count as the first tap of a double tap for every
window already there. A press in another window, the main screen or an older dialog, SHALL be ignored as a whole gesture under the
500 ms constant and within Android's double-tap distance of that lift, on a screen touched or not. The distance SHALL come from the
platform and the times from the touch events. Every other press SHALL answer (BRD EC-UX-30, D161, D164).

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
- **WHEN** the user long presses a custom tag and double taps Remove in its menu, with a gap under the limit
- **THEN** the second press does nothing, because a lift on a popup menu's card counts the same as a lift on a dialog's card (D164)

