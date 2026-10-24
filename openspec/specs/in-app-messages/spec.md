# in-app-messages Specification

## Purpose
Defines how short messages (snackbars) appear, how long they stay, how they are dismissed and announced, and how
they survive rotation, so that confirmations, undo offers and errors stay calm, readable and accessible (BRD EC-UX-16).

## Requirements

### Requirement: Messages appear at the top and stay dim
Short messages SHALL appear at the top of the screen, inside the system bars and display cutouts, and never cover the
large controls. In landscape attack mode, where the controls fill the right column, messages SHALL stay over the left
column. A message SHALL never be the brightest element on screen: text no brighter than `#C8CCD2`, on a dark surface
with a thin outline (BRD EC-UX-01, EC-UX-16, EC-UX-20, D37).

#### Scenario: Message during attack mode
- **WHEN** an attack is started and the "Migraine started" message appears
- **THEN** it is shown at the top of the screen, below the status bar
- **AND** the intensity choices and "Migraine ended" stay fully visible and usable

#### Scenario: Message during landscape attack mode
- **WHEN** the phone is in landscape and the user taps "Migraine started"
- **THEN** the "Migraine started" message with "Undo" appears over the left column, under the status bar
- **AND** "Mild", "Moderate", "Severe", "+ Medication taken" and "Migraine ended" stay fully visible and usable for
  as long as the message shows, including a longer "Time to take action"

#### Scenario: Landscape with a display cutout
- **WHEN** a message appears while the phone is in landscape
- **THEN** the message stays clear of the status bar and the display cutout and is at most 560 dp wide

#### Scenario: Landscape normal mode
- **WHEN** a message appears in landscape with no attack in progress
- **THEN** it is centred at the top of the screen, as in portrait

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
The user SHALL be able to dismiss any message by swiping it sideways. Messages without "Undo" SHALL also have a close
button with the spoken label "Dismiss" (BRD EC-UX-16).

#### Scenario: Swipe away
- **WHEN** the user swipes a message sideways
- **THEN** it disappears and its action is not performed

#### Scenario: Close an error
- **WHEN** the user taps the close button on "storage full"
- **THEN** the message disappears

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
