# in-app-messages Specification

## Purpose
Defines how short messages (snackbars) appear, how long they stay, how they are dismissed and announced, and how
they survive rotation, so that confirmations, undo offers and errors stay calm, readable and accessible (BRD EC-UX-16).

## Requirements

### Requirement: Messages appear at the top and stay dim
Short messages SHALL appear at the top of the screen, inside the system bars and display cutouts, and never cover the
large controls. On a screen with a top bar, the top edge SHALL sit below the bar; on the paywall, below the Close row; on
Home Today, below the Settings row unless the window is short. A message SHALL never be the brightest element on screen:
text no brighter than `#C8CCD2`, on a dark surface with a thin outline (BRD EC-UX-01, EC-UX-16, EC-UX-20, D158).

#### Scenario: Message during attack mode
- **WHEN** an attack is started and the "Migraine started" message appears
- **THEN** it is shown at the top of the screen, below the status bar
- **AND** the intensity choices and "Migraine ended" stay fully visible and usable

#### Scenario: Portrait attack mode keeps its place
- **WHEN** a message with "Undo" shows in portrait attack mode
- **THEN** it lies where it did before, over the rain card, which is accepted because that card is the screen's content and
  not a top-bar control

#### Scenario: Landscape with a display cutout
- **WHEN** a message appears while the phone is in landscape
- **THEN** the message stays clear of the status bar and the display cutout and is at most 560 dp wide

#### Scenario: Landscape normal mode
- **WHEN** a message appears in landscape with no attack in progress
- **THEN** it is centred at the top of the screen, as in portrait, below the top bar
- **AND** on Home Today it is below the Settings row only if the window is not short

#### Scenario: Close on the paywall
- **WHEN** a message shows on the paywall, at 100% or 200% font, in portrait or landscape
- **THEN** Close is not covered, moved or guarded, and a first tap on it closes the paywall

#### Scenario: A screen with a top bar
- **WHEN** a message shows on a screen with a top bar, such as History, My medications or Your data
- **THEN** its top edge is below the bar, and Back and the bar's actions answer a first tap

#### Scenario: Home Today
- **WHEN** a message with "Undo" shows on Home Today in a window that is not short, such as portrait, with the date on one
  line and the screen not scrolled
- **THEN** its top edge is below the Settings row, and a tap on Settings opens Settings and does not undo

#### Scenario: Home Today in a short window
- **WHEN** a message with "Undo" shows on Home Today in a window 411 dp high, short enough to pick the two-column layout, at
  100%, 130% or 200% font
- **THEN** it lies at the top where it did before, not below the Settings row, and clear of "Migraine started"
- **AND** a tap on the top edge of "Migraine started" starts a migraine
- **AND** a smaller window is the known exception, #153: at 274 dp high and 200% font the message lies over the top of the
  button, and only a tap below the message starts a migraine

#### Scenario: Settings under a message in a short window
- **WHEN** a message with "Undo" shows on Home Today in a window 411 or 274 dp high, at 100% or 200% font, and the user taps
  Settings
- **THEN** the tap does not fire "Undo"
- **AND** if the message's body lies over Settings, nothing happens, which is the accepted cost of leaving the message at the
  top, and Settings answers once the message has gone and its guard has ended

#### Scenario: A screen with no top bar
- **WHEN** a message shows on a screen with no top bar, such as onboarding
- **THEN** it lies where it did before

#### Scenario: Reduce motion
- **WHEN** reduce motion is on and a message shows on a screen with a top bar
- **THEN** it appears at once at the same place below the bar

### Requirement: Landscape attack mode keeps messages beside the controls
In landscape attack mode, where the controls fill the end column, messages SHALL stay over the start column. That
column's content SHALL move below the message while it shows, and SHALL ignore taps while it moves (BRD EC-UX-20, D37,
D52).

#### Scenario: Message during landscape attack mode
- **WHEN** the phone is in landscape and the user taps "Migraine started"
- **THEN** the "Migraine started" message with "Undo" appears over the start column (the left one in left-to-right
  languages), under the status bar
- **AND** "Mild", "Moderate", "Severe", "+ Medication taken" and "Migraine ended" stay fully visible and usable for
  as long as the message shows, including a longer "Time to take action"
- **AND** "Rest now. We'll keep time." and the start time move below the message, no line is cut, and they move back
  when the message goes; with "Reduce motion" they move at once

#### Scenario: A tap while the guidance moves
- **WHEN** in landscape attack mode a message appears over the start column and the user taps a control in that
  column, such as "2 h earlier", within half a second of the column moving
- **THEN** the tap does nothing, so it cannot land on a control that has just moved

### Requirement: Message kinds are told apart by shape
Every message SHALL have one of three kinds, each shown by an icon shape and a colour: confirmation (a check mark),
information (an "i" in a circle) and error (an "!" in a circle). The kind SHALL never be shown by colour alone
(BRD EC-UX-15, EC-UX-16).

#### Scenario: Kinds of the home messages
- **WHEN** the home screen shows a message
- **THEN** "Migraine started" and "Migraine ended" are confirmations
- **AND** "Undone", "This can no longer be undone", a rejected start move and an end time out of range are information
- **AND** "storage full" and "something went wrong" are errors

### Requirement: Undo window follows the accessibility timeout
A message with "Undo" SHALL stay for 5 seconds. When the user's Android accessibility timeout asks for longer, the
message SHALL stay that long instead. While it is shown, a thin line along its bottom edge SHALL shrink to show the time
left. With reduce motion on, the line SHALL be hidden (BRD ATK-F1, ATK-F2, EC-UX-08, EC-UX-16).

#### Scenario: Default undo window
- **WHEN** an attack is started and the accessibility timeout is not set
- **THEN** the "Migraine started" message with "Undo" disappears 5 seconds later

#### Scenario: Longer accessibility timeout
- **WHEN** the user has set Android's "Time to take action" to 30 seconds and starts an attack
- **THEN** the message with "Undo" stays for 30 seconds

#### Scenario: Reduce motion
- **WHEN** reduce motion is on and a message with "Undo" is shown
- **THEN** no shrinking line is drawn and the message still disappears when its time is up

### Requirement: Message durations by kind
Information messages and "something went wrong" SHALL stay for 8 seconds, extended by the accessibility timeout in the
same way. "Storage full" SHALL stay until the user dismisses it, because it means the last action was not saved
(BRD EC-UX-16, EC-STO-01).

#### Scenario: Information message
- **WHEN** a start move is rejected
- **THEN** the message naming the previous attack's end stays for 8 seconds

#### Scenario: Storage full
- **WHEN** an action fails because storage is full
- **THEN** the "storage full" message stays until the user closes it or swipes it away

### Requirement: Newest message wins
A new message SHALL replace the message on screen at once. Messages SHALL NOT queue (BRD EC-UX-16).

#### Scenario: End right after start
- **WHEN** the user starts an attack and ends it within the 5-second undo window
- **THEN** the "Migraine ended" message replaces "Migraine started"
- **AND** "Undo" now undoes the end

### Requirement: Messages survive rotation and short interruptions
A message on screen SHALL stay, with the time it has left, when the screen rotates or the activity is recreated, and
when the app returns from the background before that time is up (BRD EC-UX-16).

#### Scenario: Rotate during the undo window
- **WHEN** the user rotates the phone 2 seconds after starting an attack
- **THEN** the message with "Undo" is still shown and disappears about 3 seconds later

#### Scenario: Return after the time is up
- **WHEN** the user leaves the app during the undo window and returns after it has passed
- **THEN** no message is shown

### Requirement: Messages can be dismissed
The user SHALL be able to dismiss any message by swiping it sideways. Messages without "Undo" SHALL also have a close button
with the spoken label "Dismiss". A tap anywhere on a message with a timeout and no action SHALL dismiss it, by touch only
with no new semantics. A tap on the body of a message with an action or with no timeout SHALL do nothing, so a stray tap
cannot remove the offer to undo or the word that nothing was saved (BRD EC-UX-16, D158).

#### Scenario: Swipe away
- **WHEN** the user swipes a message sideways
- **THEN** it disappears and its action is not performed

#### Scenario: Close an error
- **WHEN** the user taps the close button on "storage full"
- **THEN** the message disappears

#### Scenario: A tap on a message with a timeout and no action
- **WHEN** the user taps the text of a message with a timeout and no action
- **THEN** the message disappears

#### Scenario: A stray tap on "storage full"
- **WHEN** a tap lands on the body of the "storage full" message
- **THEN** the message stays, and its close button and a swipe still close it

#### Scenario: A tap on the body of a message with Undo
- **WHEN** the user taps the text of a message with "Undo"
- **THEN** the message stays with the time it has left, and "Undo" still works

#### Scenario: A control under a message
- **WHEN** a message with a timeout and no action lies over a control, the user taps the message, and then taps the control
  after the guard has ended
- **THEN** the first tap dismisses the message, and the second reaches the control

#### Scenario: A screen reader
- **WHEN** TalkBack moves through a message with no action
- **THEN** it reads the text and the "Dismiss" button as before, and no new clickable item is added

### Requirement: Undo outcomes are confirmed
After the user taps "Undo", the message SHALL disappear at once. When the undo succeeds, an information message
"Undone." SHALL appear. When the undo can no longer run, an information message "This can no longer be undone." SHALL
appear instead of nothing happening (BRD EC-UX-16).

#### Scenario: Undo succeeds
- **WHEN** the user taps "Undo" on "Migraine started"
- **THEN** the attack is removed and "Undone." is shown

#### Scenario: Undo no longer possible
- **WHEN** the user taps "Undo" on "Migraine ended" but another attack has started since
- **THEN** nothing changes and "This can no longer be undone." is shown

### Requirement: Accessible messages
TalkBack SHALL read each message when it appears: politely for confirmations and information, at once for errors. The
"Undo" action SHALL have a specific spoken label ("Undo the start" or "Undo the end"). At any font size up to 200% the
whole message text SHALL be visible; when the action does not fit beside the text, it SHALL move below it
(BRD EC-UX-03, EC-UX-04).

#### Scenario: Largest font size
- **WHEN** the system font size is 200% and "Couldn't save. Your phone's storage is full." is shown
- **THEN** the whole text is visible on several lines and the close button is still reachable

#### Scenario: Spoken undo label
- **WHEN** TalkBack focuses the "Undo" action after ending an attack
- **THEN** it reads "Undo the end"

### Requirement: Gentle motion and haptics for messages
A message SHALL slide and fade in within 250 ms and fade out within 200 ms, with no flashing. With reduce motion on, it
SHALL appear and disappear without animation. Error messages SHALL give one short "reject" haptic. Start and end keep
their confirm haptic (BRD EC-UX-07, EC-UX-08).

#### Scenario: Reduce motion
- **WHEN** reduce motion is on and a message appears
- **THEN** it is shown in its final position at once

#### Scenario: Error haptic
- **WHEN** "something went wrong" is shown
- **THEN** the phone gives one short reject vibration

### Requirement: A message never passes taps to what lies under it
While a message arrives or goes, and for 0.8 seconds after, a touch on its place SHALL not reach the controls under it, such
as a top bar's actions. On the paywall this SHALL be 0.4 seconds. The message itself SHALL stay tappable at once. Screen
readers, keyboards and switch access SHALL be unaffected (BRD EC-UX-16, D114, D158).

#### Scenario: Undo just as the message goes
- **WHEN** a migraine is deleted in History and the user taps "Undo the deletion" just as the message fades out
- **THEN** neither "Doctor report" nor "Insights" opens

#### Scenario: Afterwards
- **WHEN** the message has been gone for more than 0.8 seconds
- **THEN** the top bar's actions work as usual

#### Scenario: On the paywall
- **WHEN** a message on the paywall lies over the buy button, the user taps the message, and taps the button 200 ms later
- **THEN** the second tap does nothing

#### Scenario: On the paywall, afterwards
- **WHEN** the same second tap comes more than 0.4 seconds after the message was dismissed
- **THEN** the buy button answers

#### Scenario: Another screen keeps 0.8 seconds
- **WHEN** a message goes on any screen other than the paywall
- **THEN** its place keeps ignoring touches for 0.8 seconds

### Requirement: The first row under the bar may lie under a message
The first row under a top bar or the Settings row SHALL be allowed to lie under a message for the message's duration, only
where it is visible and either reversible or temporary. Messages SHALL NOT move back over the top bar to avoid it. Each case
below is measured with a message with "Undo"; longer messages over the safety card are not measured (BRD D158).

#### Scenario: Remove on row 1 of My medications
- **WHEN** a message with "Undo" shows on My medications, at any font and orientation, and the user taps Remove on row 1
- **THEN** the tap can land on "Undo" instead, which restores the last removal and shows "Undone"

#### Scenario: History's month chevrons
- **WHEN** a message with "Undo" shows on History, in portrait at 100% or 200% font, and the user taps a month chevron under it
- **THEN** "Next month" lies under "Undo", so the tap restores the deletion and shows "Undone"
- **AND** "Previous month" lies under the message's body, so nothing happens and the message stays
- **AND** both answer once the message has gone and its guard has ended

#### Scenario: The trial banner's close button
- **WHEN** "Migraine ended" with "Undo" lies over the close button of the trial banner on Home Today, in portrait at 100% or
  200% font, and the user taps it
- **THEN** the tap reaches "Undo": it shows "Undone" and returns to attack mode, and the banner is not closed
- **AND** the button answers once the message has gone and its guard has ended

#### Scenario: The safety card on Attack details
- **WHEN** a message with "Undo" shows on Attack details, in portrait at 100% or 200% font, with the safety card on screen and
  scrolled to the top
- **THEN** the message covers the card's title at 100% or its first line at 200%, and the body and "Got it" stay clear
- **AND** the card stays until it is dismissed, it can be scrolled, and its live region still reads it

#### Scenario: "Storage full" over the safety card
- **WHEN** "storage full" shows over the safety card
- **THEN** it stays over the card until it is closed, and its close button and a swipe still close it
- **AND** how far it reaches is not measured: at 200% font it is taller than a message with "Undo" and may reach the body
