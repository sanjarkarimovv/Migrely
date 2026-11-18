# in-app-messages Specification

## Purpose
Defines how short messages (snackbars) appear, how long they stay, how they are dismissed and announced, and how
they survive rotation, so that confirmations, undo offers and errors stay calm, readable and accessible (BRD EC-UX-16).

## Requirements

### Requirement: Messages appear at the top and stay dim
Messages SHALL appear at the top, inside the system bars and cutouts, whole inside the safe area, never covering the large
controls. With a top bar the top edge SHALL sit below it; on the paywall below Close; on Home Today below the measured Settings
row, and in a short window over the start column. A message SHALL never be the brightest element: text no brighter than
`#C8CCD2` on a dark outlined surface (BRD EC-UX-01, EC-UX-16, EC-UX-20, EC-UX-38, D158, D178).

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
- **WHEN** a message appears in landscape with no attack in progress, on a screen other than Home Today
- **THEN** it is centred at the top of the screen, as in portrait, below the top bar

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

#### Scenario: Home Today with a wrapped date
- **WHEN** a message with "Undo" shows on Home Today in portrait on a 360 dp wide phone at 200% font, on a day whose date wraps to
  two lines, with the screen not scrolled
- **THEN** its top edge is 10 dp below the Settings row, so the second date line and Settings' whole target stay uncovered

#### Scenario: Home Today in a short window
- **WHEN** a message with "Undo" shows on Home Today in a window 411 or 274 dp high, short enough to pick the two-column layout,
  at 100%, 130% or 200% font, with the screen not scrolled
- **THEN** it lies over the start column, 10 dp below the Settings row, and ends before the column with "Migraine started"
- **AND** a tap anywhere on "Migraine started", its top edge included, starts a migraine

#### Scenario: A long error in the smallest short window
- **WHEN** "Couldn't save. Your phone's storage is full." shows on Home Today in a window 274 dp high at 200% font
- **THEN** the whole message and its Dismiss are on screen and reachable by touch, Tab and TalkBack, rising no higher than the
  top of the safe area plus 8 dp
- **AND** it may cover Settings in this case only, and "Migraine started" stays uncovered

#### Scenario: Scrolling the start column
- **WHEN** a message shows in a short window and the user scrolls the start column
- **THEN** the message stays where it is

#### Scenario: Settings under a message in a short window
- **WHEN** a message with "Undo" shows on Home Today in a window 411 or 274 dp high, at 100%, 130% or 200% font, and the user
  taps Settings
- **THEN** Settings opens at the first tap, the tap does not fire "Undo", and Settings stays in TalkBack's tree while the message
  shows

#### Scenario: Short window, right to left
- **WHEN** the same message shows in a short window in a right-to-left language
- **THEN** it lies over the start column, which is on the right, and "Migraine started" and Settings stay uncovered

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
A message with "Undo" SHALL stay for 5 seconds of unpaused time, or for Android's interactive accessibility timeout when
that is longer, and never past its Undo ceiling. A thin line along its bottom edge SHALL show the time left to the real
end, the earlier of timeout and ceiling, SHALL stop while paused, SHALL never grow, and SHALL reach 0 at that end. With
reduce motion on, it SHALL be hidden (BRD ATK-F1, ATK-F2, EC-UX-08, EC-UX-16, D182).

#### Scenario: Default undo window
- **WHEN** an attack is started and the accessibility timeout is not set
- **THEN** the "Migraine started" message with "Undo" disappears 5 seconds later

#### Scenario: Longer accessibility timeout
- **WHEN** the user has set Android's "Time to take action" to 30 seconds and starts an attack
- **THEN** the message with "Undo" stays for 30 seconds

#### Scenario: Reduce motion
- **WHEN** reduce motion is on and a message with "Undo" is shown
- **THEN** no shrinking line is drawn and the message still disappears when its time is up

#### Scenario: The line stops while paused
- **WHEN** keyboard focus rests on "Undo" with reduce motion off
- **THEN** the time-left line stops where it was
- **AND** once focus has left it goes on from where it stopped and shrinks to 0 over the new time left

#### Scenario: The line never grows
- **WHEN** focus leaves a paused "Undo" with 0.5 seconds left, so the 3 second floor applies
- **THEN** the line goes on from where it stopped, with no jump back, and shrinks to 0 over the 3 seconds

#### Scenario: The line meets the ceiling
- **WHEN** an Undo's ceiling comes before its timeout
- **THEN** the line reaches 0 exactly at the ceiling, when the message leaves

### Requirement: Message durations by kind
Information messages, "something went wrong" and "Saved." SHALL stay 8 seconds, extended by Android's interactive
accessibility timeout on every screen, since each message has a control. "Storage full" SHALL stay until dismissed or
replaced, as the last action was not saved. On Attack details and the screens of D178 a later successful save or leaving
the screen SHALL also take it away (BRD EC-UX-16, EC-UX-34, EC-UX-35, EC-UX-52, EC-STO-01, D165, D178, D182).

#### Scenario: Information message
- **WHEN** a start move is rejected
- **THEN** the message naming the previous attack's end stays for 8 seconds

#### Scenario: Storage full
- **WHEN** an action fails because storage is full on Home, History, a medication screen, Reminders, Doctor report, Your data or
  Add past migraine
- **THEN** the "storage full" message stays until the user closes it, swipes it away, a newer message replaces it, a later save
  succeeds, or the user leaves the screen

#### Scenario: Storage full on Attack details
- **WHEN** an action fails because storage is full on Attack details
- **THEN** the "storage full" message stays until the user closes it, a later save succeeds, or the user leaves the screen

#### Scenario: Saved
- **WHEN** "Saved." shows on any screen
- **THEN** it stays for 8 seconds, or longer if the interactive accessibility timeout asks for it

#### Scenario: The same rule on every screen
- **WHEN** the interactive accessibility timeout is 20 seconds and the non-interactive one is 8 seconds, and "Saved.", an
  information message or a timed error shows on Home, History, Attack details, a medication screen or Reminders
- **THEN** it stays for 20 seconds

#### Scenario: Saved on Home in attack mode
- **WHEN** "Time to take action" is 30 seconds and "Saved." shows on Home in attack mode
- **THEN** it stays about 30 seconds, dim, and one tap on its body or its Dismiss closes it sooner

#### Scenario: The setting changes while a message shows
- **WHEN** the user changes "Time to take action" while a timed message is on screen
- **THEN** the new value applies, counted against the unpaused time already shown, not from when the message appeared

### Requirement: Newest message wins
A new message SHALL replace the message on screen at once. Messages SHALL NOT queue (BRD EC-UX-16).

#### Scenario: End right after start
- **WHEN** the user starts an attack and ends it within the 5-second undo window
- **THEN** the "Migraine ended" message replaces "Migraine started"
- **AND** "Undo" now undoes the end

### Requirement: Messages survive rotation and short interruptions
Except on Settings, Onboarding and Paywall (known limit, #236), a message SHALL keep its time left across rotation,
recreation, process restore and a return from the background in time. The host SHALL save the running time shown and the
save's elapsed stamp. A pause SHALL end at the save, with the 3 s floor applied then; time from save to restore SHALL
count as running time. A restored message SHALL have no focus and never pass its Undo ceiling (BRD EC-UX-16, EC-UX-57,
D182).

#### Scenario: Rotate during the undo window
- **WHEN** the user rotates the phone 2 seconds after starting an attack
- **THEN** the message with "Undo" is still shown and disappears about 3 seconds later

#### Scenario: Return after the time is up
- **WHEN** the user leaves the app during the undo window and returns after it has passed
- **THEN** no message is shown

#### Scenario: Rotate while paused
- **WHEN** keyboard focus has held "Undo" for 20 seconds with 2 seconds left and the user rotates the phone
- **THEN** the pause ends at the save with at least 3 seconds left, and the message comes back with no focus and about 3
  seconds left, less the time the rotation took

#### Scenario: Process death while paused
- **WHEN** the process dies while focus holds a message, and Android restores the screen before the time left at the save
  has run out
- **THEN** the message comes back with no focus and the time left at the save, less the time until the restore

#### Scenario: Restored after the time ran out
- **WHEN** a screen is restored after the time left at the save has run out on the elapsed clock, even though the message
  was paused when it was saved
- **THEN** no message is shown

#### Scenario: Restored after the ceiling
- **WHEN** a screen with a deletion Undo is restored more than 115 seconds after `deletedAt`
- **THEN** no Undo is shown

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
TalkBack SHALL read each message when it appears: politely for confirmations and information, at once for errors, except while
the safety card shows, when an error SHALL be polite. The "Undo" action SHALL have a specific spoken label ("Undo the start" or
"Undo the end"). At any font size up to 200% the whole message text SHALL be visible; when the action does not fit beside the
text, it SHALL move below it (BRD EC-UX-03, EC-UX-04, D165).

#### Scenario: Largest font size
- **WHEN** the system font size is 200% and "Couldn't save. Your phone's storage is full." is shown
- **THEN** the whole text is visible on several lines and the close button is still reachable

#### Scenario: Spoken undo label
- **WHEN** TalkBack focuses the "Undo" action after ending an attack
- **THEN** it reads "Undo the end"

#### Scenario: An error while the safety card shows
- **WHEN** an error appears while the safety card shows
- **THEN** TalkBack speaks it politely, after the card

### Requirement: Gentle motion and haptics for messages
A message arriving when none shows SHALL slide and fade in within 250 ms and one leaving SHALL fade out within 200 ms. One
replacing another SHALL switch in place; what follows its bottom SHALL ease up over 250 ms if it is shorter and move at once if
taller. No flashing. Reduce motion SHALL make all of it instant. Errors SHALL give one short "reject" haptic; start and end keep
their confirm haptic (BRD EC-UX-07, EC-UX-08, EC-UX-37, D178).

#### Scenario: Reduce motion
- **WHEN** reduce motion is on and a message appears
- **THEN** it is shown in its final position at once

#### Scenario: A replacement
- **WHEN** "Migraine ended" replaces "Migraine started" with motion on
- **THEN** the text and icon change in place, the surface stays where it is, and nothing cross-fades

#### Scenario: A shorter replacement
- **WHEN** "Saved." replaces a taller error in landscape attack mode or on Attack details while the safety card shows, with motion
  on
- **THEN** the guidance column or the content moves up over 250 ms instead of jumping

#### Scenario: A taller replacement
- **WHEN** a taller error replaces a message with "Undo" in the same places
- **THEN** the content moves down in the same frame, and in no frame does the message cover the safety card's title or the
  guidance

#### Scenario: Error haptic
- **WHEN** "something went wrong" is shown
- **THEN** the phone gives one short reject vibration

### Requirement: A message never passes taps to what lies under it
While a message arrives or goes, and for 0.8 seconds after, a touch on its place SHALL not reach the controls under it, such
as the first row under the top bar. On the paywall this SHALL be 0.4 seconds. The message itself SHALL stay tappable at once.
Screen readers, keyboards and switch access SHALL be unaffected (BRD EC-UX-16, D114, D158).

#### Scenario: Undo just as the message goes
- **WHEN** a migraine is deleted in History and the user taps "Undo the deletion" just as the message fades out
- **THEN** the month shown does not change: neither "Next month" nor "Previous month" answers

#### Scenario: Afterwards
- **WHEN** the message has been gone for more than 0.8 seconds
- **THEN** the controls under it work as usual

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
below is measured with a message with "Undo". The safety card SHALL never lie under a message (BRD D158, D165).

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
- **WHEN** any message shows on Attack details with the safety card on screen, at any font and orientation
- **THEN** the message does not cover the card, because the content starts below the message

#### Scenario: "Storage full" over the safety card
- **WHEN** "storage full" shows while the safety card is on screen, at 100% or 200% font
- **THEN** it does not lie over the card, which starts below it, and its close button and a swipe still close it

### Requirement: A message never covers the safety card
While the safety card shows on Attack details, no message SHALL lie over the content. The message SHALL stay in its place
under the top bar, and the scrolling content SHALL start below its bottom edge. This SHALL hold until that message leaves, even
if "Got it" closes the card first. The message SHALL never move while it shows. With no card, D158 SHALL stay as it is (BRD
EC-UX-33, D165).

#### Scenario: A failed first 10
- **WHEN** a first 10 fails to save because storage is full, at 100%, 130% or 200% font, in portrait or in landscape
- **THEN** "Couldn't save. Your phone's storage is full." shows under the top bar, and the card's title, sentence and "Got it"
  are all below it and in the accessibility tree
- **AND** the message's bounds and the card's bounds do not intersect, and nothing of the content is above the message's bottom
  edge

#### Scenario: A message with Undo while the card shows
- **WHEN** the card shows and a message with "Undo" appears, such as "Pain level removed"
- **THEN** the card is not covered, and "Undo" works and shows "Undone."

#### Scenario: Scrolled away from the card
- **WHEN** the card shows, the user has scrolled it out of view, and a message with "Undo" appears
- **THEN** the message is visible at the top and "Undo" works
- **AND** scrolling back to the top while it shows, the card is not covered

#### Scenario: "Got it" while a message shows
- **WHEN** the user taps "Got it" while a message shows
- **THEN** the card goes, the message does not move, and the content still starts below the message until it leaves

#### Scenario: The message leaves
- **WHEN** a message leaves while the content is below it
- **THEN** it fades out, the space then closes over 250 ms, and a tap on its former place within 0.8 seconds opens nothing, such
  as the "Started" row's picker
- **AND** the same tap after 0.8 seconds answers

#### Scenario: A long message at 200% in landscape
- **WHEN** a 200% "Couldn't save" shows with the card in a 411 dp high landscape window
- **THEN** nothing is covered or cut off, what does not fit scrolls, and "Dismiss" frees the space

#### Scenario: No safety card
- **WHEN** a message shows on Attack details with no safety card
- **THEN** it lies over the first row exactly as before this change

#### Scenario: Remove animations
- **WHEN** "Remove animations" is on and a message comes or goes while the card shows
- **THEN** the content moves at once, with no frames in between

#### Scenario: Right to left and both themes
- **WHEN** the layout is right to left, or the theme is light or dark
- **THEN** the message and the card mirror or recolour with the layout, and neither covers the other

### Requirement: TalkBack hears the safety card before a message
While the safety card shows, TalkBack SHALL speak the card's title and sentence before a message, and each in full. The card
SHALL stay assertive, and while it shows a message SHALL be polite. When one action brings both, the message SHALL appear after
the card is on screen. The card's title SHALL be a heading. Outside card mode, errors SHALL stay assertive (BRD EC-UX-33, D165).

#### Scenario: A failed first 10 with TalkBack
- **WHEN** TalkBack is on and a first 10 fails to save
- **THEN** it speaks the card's full title and sentence first and then the full message, and neither cuts the other off

#### Scenario: A first 10 that saves
- **WHEN** TalkBack is on and a first 10 saves, with no message
- **THEN** it speaks the card's title and sentence

#### Scenario: An error with no card
- **WHEN** an error shows on a screen with no safety card
- **THEN** TalkBack speaks it at once, as before this change

#### Scenario: A tap while the message waits for the card
- **WHEN** one action brings the card and a message, and the user taps the message's area during the 200 ms wait
- **THEN** the tap does nothing: the message shown before, if any, neither closes nor undoes, and the new message shows when the
  wait ends

#### Scenario: A replaced message in card mode has text
- **WHEN** the card shows and a new message replaces the one on screen
- **THEN** the new one appears in place with no cross-fade, and TalkBack speaks its text, never an empty announcement

#### Scenario: Heading navigation
- **WHEN** a TalkBack user moves by headings while the card shows
- **THEN** the card's title is one of the headings, and its text is unchanged

#### Scenario: Swipe order
- **WHEN** a TalkBack user swipes through the screen with the card and a message showing
- **THEN** the order is Back, the screen title, the message's text, its control, the card's title, its sentence, "Got it", then
  the sections

#### Scenario: Switch access and a keyboard
- **WHEN** switch access scans, or a keyboard tabs, through the screen with the card and a message showing
- **THEN** the order is Back, the message's control, "Got it", then the content, one stop each, with no overlapping highlights,
  and each control works

### Requirement: An error on Attack details goes when a later save succeeds
While an error shows on Attack details, the next write there that succeeds SHALL replace it with that write's own message,
or else with "Saved.", a polite confirmation for 8 seconds stretched by the accessibility timeout. "Saved." SHALL show only to
replace an error. The error SHALL clear on success, never on the attempt. Undo and information messages SHALL stay (BRD
EC-UX-34, D165).

#### Scenario: The same 10 after freeing space
- **WHEN** a 10 failed because storage was full, the user frees space without closing the message, and adds the 10 again
- **THEN** within 1 second "Saved." replaces "Couldn't save", the 10 is stored, TalkBack speaks "Saved.", and it leaves after 8
  seconds or the accessibility timeout

#### Scenario: Another write saves
- **WHEN** an error shows and a note, a tag or a medication effect then saves on the same screen
- **THEN** "Saved." replaces the error

#### Scenario: A write with its own message
- **WHEN** an error shows and the user removes a pain level, which saves
- **THEN** "Pain level removed" with "Undo" replaces the error, and no "Saved." shows

#### Scenario: No error on screen
- **WHEN** a write saves and no error shows
- **THEN** no "Saved." shows

#### Scenario: Undo or information on screen
- **WHEN** a message with "Undo" or an information message shows and a later write saves
- **THEN** that message stays with the time it has left

#### Scenario: It fails again
- **WHEN** an error shows, storage is still full, and the user tries again
- **THEN** the error shows in the same place and TalkBack speaks it again, with no flash and no change of surface colour

#### Scenario: A different action saved
- **WHEN** a 10 failed, and a note then saves
- **THEN** "Saved." shows, and Pain still shows no new 10, which is accepted because the message is about the last action

#### Scenario: Calm
- **WHEN** "Saved." replaces an error
- **THEN** only the icon and text change, the surface keeps its dim colour, and no haptic plays

#### Scenario: A tap on "Saved."
- **WHEN** the user taps the text of "Saved."
- **THEN** it closes

### Requirement: An error on Attack details belongs to the visit
When Attack details stops being the current screen, by Back or by going forward, its error message SHALL be cleared and SHALL
NOT show on return. Undo and information messages SHALL behave as before. A dialog or picker on the screen, the background,
the screen turning off, Android Settings, a rotation and a restored process SHALL NOT clear it. The safety card SHALL stay
(BRD EC-UX-34, D165).

#### Scenario: Forward and back
- **WHEN** an error shows, the user opens Log medication or Edit medication, and comes back
- **THEN** no error shows on Attack details, and the safety card is still there if it was not closed

#### Scenario: Back and reopened
- **WHEN** an error shows, the user goes back to History and opens the same migraine again
- **THEN** no error shows

#### Scenario: Undo across a trip
- **WHEN** a message with "Undo" shows and the user goes forward and comes back within its time
- **THEN** it behaves as before this change

#### Scenario: A message after the return
- **WHEN** a message is posted after the user has come back to Attack details
- **THEN** it shows as usual

#### Scenario: Android Settings to free space
- **WHEN** an error shows, the user presses Home, frees space in Android Settings, and returns
- **THEN** the error still shows until a later save succeeds

#### Scenario: A dialog or a picker
- **WHEN** an error shows and the user opens a dialog or a picker on Attack details
- **THEN** the error still shows

#### Scenario: Rotation and a restored process
- **WHEN** an error shows and the phone rotates, or the process is killed in the background and restored
- **THEN** the error still shows and the card is not covered

### Requirement: An error on the other screens goes when a later save succeeds
On Home, History, My medications, Edit medication, Log medication, Reminders, Doctor report, Your data and Add past migraine,
while an error shows, the next write there that succeeds SHALL replace it with that write's own message, or else with "Saved."
on Home, History and Reminders. A created report on Doctor report and a logged intake on Log medication SHALL clear it with no
message. It SHALL clear on success, never on the attempt (BRD EC-UX-35, D178, D190).

#### Scenario: Home Today, a day mark after freeing space
- **WHEN** marking today's period failed because storage was full, the user frees space without closing the message, and marks
  it again
- **THEN** within 1 second "Saved." replaces "Couldn't save", the mark is stored, TalkBack speaks "Saved.", and it leaves after
  8 seconds or the accessibility timeout

#### Scenario: Home in attack mode
- **WHEN** recording a pain level during an attack failed, and a later pain level, a start moved earlier or a long-attack answer
  saves
- **THEN** "Saved." replaces the error, dim like attack mode, with no haptic, and the attack-mode controls stay uncovered as before

#### Scenario: History, a day mark
- **WHEN** an error shows on History and a period or pain-free mark then saves
- **THEN** "Saved." replaces the error

#### Scenario: Reminders
- **WHEN** an error shows on Reminders and the daily reminder, its time, the effect reminder or the visit date then saves
- **THEN** "Saved." replaces the error

#### Scenario: Reminders, a visit date that asks for permission
- **WHEN** an error shows on Reminders and a visit date saves and the app then asks for the notification permission
- **THEN** the error is gone, "Saved." replaces it even if it sits behind the system dialog or times out there, and the row shows
  the date

#### Scenario: Doctor report
- **WHEN** creating a report failed because storage was full, and a later attempt creates it
- **THEN** the error is gone, the share sheet opens, and no "Saved." shows, because the app saved nothing

#### Scenario: A write with its own message
- **WHEN** an error shows and the next successful write brings its own message, such as "Backup saved" on Your data, the archive
  message with "Undo" on My medications, "Removed. Nothing was logged." after an Undo on Log medication, "Migraine started" or
  "Undone."
- **THEN** that message replaces the error, and no "Saved." shows

#### Scenario: Log medication, a dose logged after freeing space
- **WHEN** logging Ibuprofen failed with "Couldn't save. Your phone's storage is full.", the user frees space without closing
  the message, and taps Ibuprofen again
- **THEN** within 1 second the error is gone with no "Saved." and no haptic, "Ibuprofen 400 mg logged at 14:20" shows with
  "Done" and "Undo" and nothing over it, and TalkBack speaks the logged line

#### Scenario: A write that closes the screen
- **WHEN** an error shows on Edit medication or Add past migraine and a later save succeeds
- **THEN** the screen closes as before, and the error does not show on the screen it returns to

#### Scenario: Nothing was written
- **WHEN** an error shows and an action succeeds without writing anything, such as a pain level after the attack was already
  ended or a visit date that did not change
- **THEN** no "Saved." shows and the error stays

#### Scenario: No error on screen
- **WHEN** a write saves on any of these screens and no error shows
- **THEN** no "Saved." shows

#### Scenario: Undo or information on screen
- **WHEN** a message with "Undo" or an information message shows on any of these screens but Log medication, and a later write
  saves
- **THEN** that message stays with the time it has left
- **AND** on Log medication a logged intake clears it, because the logged result replaces the whole page (medication-intakes
  "A logged intake clears the message on Log medication")

#### Scenario: It fails again
- **WHEN** an error shows, storage is still full, and the user tries again
- **THEN** the error shows in the same place and TalkBack speaks it again, with no flash and no change of surface colour

#### Scenario: Two quick saves
- **WHEN** an error shows and two writes succeed one right after the other
- **THEN** one "Saved." shows, and the second write finds no error and adds nothing

#### Scenario: A late success after leaving
- **WHEN** a write succeeds after the user has already left the screen
- **THEN** nothing shows there, and on return no error and no "Saved." show

#### Scenario: Free and Premium
- **WHEN** the same steps run on Free and on Premium
- **THEN** the messages are the same

#### Scenario: Largest font and landscape
- **WHEN** "Saved." shows at 200% font, in portrait or landscape
- **THEN** its whole text is visible and its place follows the screen's usual rules

### Requirement: An error on the other screens belongs to the visit
When one of the screens named above stops being the current screen, by Back or by going forward, its error SHALL be cleared and
SHALL NOT show on return. Undo and information messages SHALL behave as before. A dialog, a picker, the system file picker, the
share sheet, the background, Android Settings, a rotation and a restored process SHALL NOT clear it (BRD EC-UX-35, D178).

#### Scenario: Forward and back
- **WHEN** an error shows on Home and the user opens Settings or a migraine's details and comes back, or an error shows on History
  and the user opens a migraine and comes back
- **THEN** no error shows

#### Scenario: Back and reopened
- **WHEN** an error shows on Reminders, the user goes back and opens Reminders again
- **THEN** no error shows

#### Scenario: Android Settings to free space
- **WHEN** an error shows, the user presses Home, frees space in Android Settings, and returns
- **THEN** the error still shows until a later save succeeds

#### Scenario: The system file picker
- **WHEN** an error shows on Your data and the user opens the system file picker and returns
- **THEN** the error still shows

#### Scenario: The share sheet
- **WHEN** an error shows on Doctor report and a share sheet opened by an earlier report is closed
- **THEN** the error still shows

#### Scenario: A dialog or a picker
- **WHEN** an error shows and the user opens a dialog, a date or time picker or a menu on the same screen
- **THEN** the error still shows

#### Scenario: Rotation and a restored process
- **WHEN** an error shows and the phone rotates, or the process is killed in the background and restored
- **THEN** the error still shows with no duration

#### Scenario: Undo across a trip
- **WHEN** a message with "Undo" shows and the user goes forward and comes back within its time
- **THEN** it behaves as before this change

#### Scenario: A message after the return
- **WHEN** a message is posted after the user has come back
- **THEN** it shows as usual

### Requirement: Keyboard, Switch Access and TalkBack reach a message right after the top bar
On every screen with messages, keyboard and TalkBack order SHALL be the top bar's controls, the message's text and its control,
then the content. On Home Today the keyboard SHALL go Settings, message, content, while TalkBack and Switch Access read the message
first; in attack mode and onboarding the message SHALL come first for all three. A message SHALL NOT take focus on arrival; focus
SHALL clear when it leaves or is replaced. A leaving message SHALL NOT be a stop (BRD EC-UX-36, D178).

#### Scenario: Attack details with the safety card
- **WHEN** "Couldn't save" shows above the safety card on Attack details and the user presses Tab from the top with a hardware
  keyboard
- **THEN** the stops are Back, Dismiss, "Got it", then the content, and Dismiss closes the message

#### Scenario: A screen with top bar actions
- **WHEN** a message with "Undo" shows on History and the user presses Tab from the top
- **THEN** the stops are Back, Insights, Doctor report, "Undo", then the month and its days

#### Scenario: Shift+Tab
- **WHEN** focus is on the content's first control and the user presses Shift+Tab
- **THEN** focus moves to the message's control, then to the top bar

#### Scenario: TalkBack
- **WHEN** TalkBack is on, a message shows, and the user swipes forward from the top bar's last control
- **THEN** TalkBack reads the message's text, then its Dismiss or Undo, then the first content item

#### Scenario: Home Today with a keyboard
- **WHEN** a message with "Undo" shows on Home Today, in portrait or a short window, with the column not scrolled and nothing
  focused, and the user presses Tab twice
- **THEN** the first Tab reaches Settings and the second the message's control, then the content follows
- **AND** Shift+Tab from the first content control after Settings goes to the message's control, then to Settings

#### Scenario: Home Today scrolled
- **WHEN** the same message shows after the column has scrolled until the Settings row is off screen, and the user presses Tab
- **THEN** the first Tab goes to the first control Android finds from the top, which may be a content control such as the Period
  chip lying partly under the message, and the message's control is one Shift+Tab from it
- **AND** the message stays reachable within 2 key presses, and Shift+Tab from the message's control goes to Settings, scrolled
  into view

#### Scenario: Home Today with TalkBack or Switch Access
- **WHEN** TalkBack or Switch Access moves through Home Today while a message shows
- **THEN** the message's text and its control come first, then the date and Settings, then the rest of the content

#### Scenario: Attack mode and onboarding
- **WHEN** a message with "Undo" shows in attack mode, or a message shows on onboarding, and the user presses Tab from nothing
  focused, swipes with TalkBack or scans with Switch Access
- **THEN** the message's control is the first stop for all three

#### Scenario: Switch Access
- **WHEN** Switch Access scans a screen with a message
- **THEN** it highlights the message's control in the same order, separately from the controls under it, and selecting it works

#### Scenario: A message appears while focus is elsewhere
- **WHEN** keyboard or TalkBack focus is on a content control and a message appears
- **THEN** focus stays on that control, and TalkBack speaks the message through its live region

#### Scenario: The focused message leaves
- **WHEN** focus is on a message's control and the message times out, is undone or is dismissed
- **THEN** focus clears, the app does not crash, the next Tab goes to the screen's first stop and Shift+Tab to its last
- **AND** focus never moves into the content by itself, so an Enter cannot start a migraine or record pain

#### Scenario: A replacement clears focus
- **WHEN** focus is on "Undo" and a new message replaces that message
- **THEN** focus clears and is not carried to the new message's control, so a waiting Enter cannot act on the new message

#### Scenario: A leaving message is not a stop
- **WHEN** the user presses Tab, or swipes with TalkBack, during a message's 200 ms fade-out
- **THEN** focus never lands on the leaving message, and touch on it behaves as before

#### Scenario: Nothing focused
- **WHEN** focus has cleared and the user presses Enter or Space
- **THEN** nothing happens

#### Scenario: Large font, landscape and right to left
- **WHEN** the font is 200%, the phone is in landscape or the language is right to left, and a message shows with its action
  below its text
- **THEN** the order is the same: the top bar, the message's text, its control, then the content

#### Scenario: The message does not move
- **WHEN** a message shows on any of the 13 screens at 100% and 200% font
- **THEN** its bounds match the build before this change within 1 pixel, except on Home Today where D178's measured offset and the
  fit rule apply, its drawing over the content is unchanged, and a tap where it was within 0.8 s still reaches nothing under it

#### Scenario: A shorter replacement at 200%
- **WHEN** at 200% font an error with its Dismiss below the text is replaced by "Saved.", and a tap lands where that Dismiss was
  within 0.8 seconds
- **THEN** the tap reaches nothing under it, because the guard covers the taller of the old and the new message

### Requirement: A message is never announced empty
On every screen, a message that appears or replaces another SHALL never be fully transparent in any frame while it is in the
accessibility tree, so TalkBack always speaks its text. A replacement SHALL switch in place with no cross-fade. A message that
arrives when none shows SHALL still slide in, looking as before, with no new flashing or faster motion (BRD EC-UX-37, D178).

#### Scenario: A failed retry is spoken again
- **WHEN** "Couldn't save" shows on Reminders, Home or History, storage is still full, and the user tries again after more than
  100 ms of quiet
- **THEN** TalkBack speaks the full text again, and no announcement is empty

#### Scenario: A message after a quiet moment
- **WHEN** no message shows, the screen has been still for more than 100 ms, and a message appears on any screen
- **THEN** TalkBack speaks its full text

#### Scenario: One message at a time
- **WHEN** a message replaces another on any screen
- **THEN** in no frame do two messages show, and the new one is never transparent

#### Scenario: Arrival looks as before
- **WHEN** a message arrives when none shows
- **THEN** it slides in within 250 ms as before, and no frame is visibly brighter than its neighbours

#### Scenario: Reduce motion
- **WHEN** "Remove animations" is on and a message appears or replaces another
- **THEN** it shows in its final place at once

### Requirement: A message ignores the keyboard
Opening or closing the keyboard SHALL NOT move a message, change how it fits (D178 (6)), or let it cover the top bar or Back.
The keyboard MAY cover a message's lower part; the message SHALL stay dismissible. No keyboard padding SHALL apply to
`MessageLayout`, the message host or their ancestors, and a test SHALL fail when the host is padded (BRD EC-UX-41, D180).

#### Scenario: A message, then the keyboard
- **WHEN** a message shows on Attack details and the user taps the note, at 100% or 200% font, in portrait or landscape
- **THEN** the message stays where it was, to the pixel, below the top bar, and a first tap on Back leaves the screen

#### Scenario: The keyboard, then a message
- **WHEN** the keyboard is open on a field and a message appears, such as "Couldn't save. Your phone's storage is full."
- **THEN** it appears at the same place as with the keyboard closed, below the top bar, and does not cover Back

#### Scenario: The keyboard closes under a message
- **WHEN** a message shows and the keyboard closes
- **THEN** the message does not move

#### Scenario: The keyboard covers the message's button
- **WHEN** at 200% font in a short landscape window the keyboard covers a message's lower part, its Dismiss or Undo included
- **THEN** the message never lies over the keyboard, and a sideways swipe on its visible part, TalkBack's Dismiss or Undo action,
  or closing the keyboard each still clears or reaches it

#### Scenario: A test with a keyboard inset
- **WHEN** a test shows a message on Attack details and then gives the window a keyboard inset of 250 dp
- **THEN** the message's top and Back's bounds are the same as before the inset, and `MessageFitInsetsTest` still passes

#### Scenario: The host padded for the keyboard
- **WHEN** the same test runs with keyboard padding put on the message host as a mutation
- **THEN** the test fails, which shows it can fail; the mutation is reverted

#### Scenario: The safety card
- **WHEN** the safety card shows with a message and the keyboard closed
- **THEN** D165 holds as before: the content starts below the message, and the message covers no part of the card

### Requirement: Home Today's message order holds with nothing after Settings
On Home Today with a message, Shift+Tab from the first control after Settings in Tab order SHALL go to the message's
control, then Settings, whichever column that control is in. With nothing focusable after Settings in its column, Tab
SHALL go Settings, message, "Migraine started", and Shift+Tab the exact reverse, with no stop added or skipped (BRD
EC-UX-47, EC-UX-36, D178 (4), D181).

#### Scenario: Nothing after Settings
- **WHEN** a message with "Undo" shows on Home Today, nothing focusable follows Settings in its column, and the user
  presses Tab three times from nothing focused
- **THEN** focus goes Settings, the message's "Undo", "Migraine started"
- **AND** Shift+Tab twice goes back to "Undo", then Settings

#### Scenario: The month card is not ready yet
- **WHEN** a message shows right after a fresh start, before the month card has loaded, and the user presses Shift+Tab
  from "Migraine started"
- **THEN** focus goes to the message's control, not past it to Settings

#### Scenario: A short window
- **WHEN** the phone is in a short landscape window at 200% font, the start column holds only the date and Settings,
  and the user presses Shift+Tab from the first control of the end column
- **THEN** focus goes to the message's control, then to Settings

#### Scenario: A control after Settings
- **WHEN** a focusable control, such as the trial banner, follows Settings in its column and the user presses Shift+Tab
  from it
- **THEN** focus goes to the message's control, then Settings, as before

### Requirement: The countdown pauses while the message holds keyboard focus
A timed message's countdown SHALL pause while keyboard focus is on its control. Focus SHALL count as left once it has been
away for 300 ms, or at once when the window loses focus. The countdown SHALL then go on from where it stopped, with at
least 3 seconds left. Nothing SHALL be spoken or shown to mark the pause, and the surface SHALL NOT change (BRD EC-UX-53,
EC-UX-55, EC-UX-56, D182).

#### Scenario: Keyboard holds Undo
- **WHEN** "Migraine started" with "Undo" shows with no accessibility timeout set, keyboard focus reaches "Undo" 1 second
  later and stays there for 30 seconds
- **THEN** the message is still shown 31 seconds after it appeared
- **AND** after focus moves to the content it leaves about 4 seconds later, the time it had left

#### Scenario: Focus leaves with little time left
- **WHEN** keyboard focus leaves a paused message that had 0.5 seconds left
- **THEN** the message leaves about 3 seconds later, not 0.5 seconds later

#### Scenario: The window loses focus
- **WHEN** keyboard focus is on the Dismiss of "Saved." and the user pulls down the notification shade, opens another app,
  a system dialog appears or the screen turns off
- **THEN** the countdown runs again at once, with at least 3 seconds left

#### Scenario: A touch ends keyboard mode
- **WHEN** keyboard focus is on "Undo" and the user touches the content, or presses Back
- **THEN** focus has left, and the countdown goes on with at least 3 seconds left

#### Scenario: What the person sees and hears while paused
- **WHEN** a message's countdown is paused
- **THEN** its time-left line stops moving and moves again on resume
- **AND** no word, sound, flash or change of surface marks the pause, and TalkBack says nothing new

#### Scenario: Undo pressed while paused
- **WHEN** the user presses "Undo" while its countdown is paused
- **THEN** "Undone." replaces it, focus is cleared, and "Undone." runs its own full time, not paused

#### Scenario: A new message replaces a paused one
- **WHEN** a new message arrives while keyboard focus holds "Undo"
- **THEN** focus is cleared, as D178 requires, and the new message runs its own full, unpaused time

#### Scenario: Storage full has nothing to pause
- **WHEN** keyboard focus is on the Dismiss of "Couldn't save. Your phone's storage is full."
- **THEN** the message behaves as before: it stays until it is closed, replaced, cleared by a later save or the user leaves

#### Scenario: Large font, landscape and right-to-left
- **WHEN** the font is 200%, the phone is in landscape, the layout is right-to-left and "Undo" sits below the text
- **THEN** keyboard focus on "Undo" pauses the countdown in the same way

### Requirement: The countdown pauses while the message holds TalkBack focus
While TalkBack's focus is on a timed message's text or control, put there by the person or by TalkBack itself, its
countdown SHALL pause as for keyboard focus, with the same 300 ms grace for the move from text to control and the same 3
second floor. Turning TalkBack off SHALL count as focus leaving. TalkBack being on SHALL NOT make a message indefinite: it
pauses only while focus holds it, and an Undo still leaves at its ceiling (BRD EC-UX-54, EC-UX-55, EC-UX-60, D182).

#### Scenario: From the text to the control
- **WHEN** TalkBack focuses a message's text and the user swipes to its control 2 seconds later
- **THEN** the countdown stays paused through the move and does not resume between them

#### Scenario: TalkBack focuses a new message by itself
- **WHEN** a new "Saved." or "Migraine started" with "Undo" appears and TalkBack moves its own focus onto it before the
  person moves anything
- **THEN** the countdown pauses from that moment, the same as focus the person moved there
- **AND** when focus moves on, it goes on 300 ms later with at least 3 seconds left, and the "Undo" still leaves at its
  ceiling

#### Scenario: TalkBack moves on
- **WHEN** TalkBack focus moves from a paused message to the content
- **THEN** the countdown goes on 300 ms later, with at least 3 seconds left

#### Scenario: TalkBack is turned off
- **WHEN** TalkBack focus is on a paused message and TalkBack is turned off
- **THEN** focus counts as left after the same 300 ms grace, and the countdown goes on with at least 3 seconds left

#### Scenario: TalkBack holds an Undo
- **WHEN** TalkBack focus stays on "Undo the deletion" for 3 minutes
- **THEN** the message leaves at its Undo ceiling, focus is cleared, and the app does not crash

#### Scenario: Which Android versions are checked
- **WHEN** the pause is checked on API 26 and on API 36
- **THEN** the keyboard pause is required and checked on both, and the TalkBack pause, built for every level, is checked
  on API 36 through the real message host with TalkBack on
- **AND** the TalkBack pause on API 26 is a known limit: our API 26 image has no TalkBack and we do not sideload it (BRD
  EC-UX-54, D182)

### Requirement: An Undo never outlives what it restores
An Undo SHALL leave no later than 115 s, D23's restorable window minus 5 s, after the action it undoes, paused or not. Two
bounds apply, whichever ends first: the wall clock from the action (`deletedAt` for a deletion, else when it appeared), and
a clock that counts sleep and cannot be set, from when it appeared. The host SHALL recheck on resume and on window focus,
and leave as on timeout (BRD EC-UX-58, EC-ATK-24, D23, D182).

#### Scenario: A held deletion Undo
- **WHEN** keyboard focus holds "Undo" on "Migraine deleted" with no end
- **THEN** the message leaves 115 seconds after `deletedAt`, focus is cleared, and the app does not crash

#### Scenario: A deletion offered late
- **WHEN** a deletion made on Details is offered on Home 25 seconds after `deletedAt` and "Time to take action" is 2 minutes
- **THEN** the message leaves 115 seconds after `deletedAt`, not 145 seconds

#### Scenario: Lock and unlock at 122 seconds on Home
- **WHEN** a deletion made on Details is offered on Home 20 seconds after `deletedAt`, "Time to take action" is 2 minutes,
  and Home is paused and resumed when the wall clock reads 122 seconds after `deletedAt`
- **THEN** no "Undo" is on screen after the resume, and "This can no longer be undone." is not shown
- **AND** the same test fails on `develop` 40536f49, where "Undo" is still on screen

#### Scenario: Lock and unlock at 122 seconds on History
- **WHEN** a deletion made on Details opened from History is offered on History 20 seconds after `deletedAt`, "Time to take
  action" is 2 minutes, and History is paused and resumed at 122 seconds after `deletedAt`
- **THEN** no "Undo" is on screen after the resume, and "This can no longer be undone." is not shown
- **AND** the same test fails on `develop` 40536f49

#### Scenario: Undo before the ceiling still restores
- **WHEN** the user taps "Undo" on a deletion 114 seconds after `deletedAt`
- **THEN** the migraine is back and "Undone." is shown

#### Scenario: The clock is set back during a pause
- **WHEN** the phone's clock is set back by an hour while focus holds an Undo
- **THEN** the Undo still leaves 115 seconds after it appeared

#### Scenario: The clock is set forward
- **WHEN** the phone's clock is set forward past a deletion's ceiling and the user returns to the app
- **THEN** the Undo is gone on resume, as the hidden copy is purged

#### Scenario: Other Undo messages
- **WHEN** focus holds "Undo" on "Migraine started", a removed intake, an archived medication or a removed visit
- **THEN** the message leaves 115 seconds after it appeared

#### Scenario: The ceiling follows D23
- **WHEN** the test that compares the ceiling with the deleted-attack retention runs with the retention changed as a
  mutation
- **THEN** it fails, which shows the ceiling cannot drift from D23; the mutation is reverted

### Requirement: Message controls are 64 dp in the dark room
Inside an active dark room, a message's Undo SHALL be at least 64 by 64 dp, its Dismiss 64 by 64 dp with the same 20 dp
icon, and the message at least 64 dp tall. The visible target SHALL grow, not only its touch area. Label size and colour,
placement, the 8 dp gap, dim colours and the tap guard SHALL stay. Outside a dark room controls SHALL stay 48 dp (BRD
EC-UX-02, EC-UX-59, D182).

#### Scenario: Portrait attack mode
- **WHEN** a message with "Undo" shows in portrait attack mode at 100% and at 200% font
- **THEN** "Undo" is at least 64 dp in both dimensions
- **AND** the message is whole on screen, the rain card's controls are uncovered and "Migraine ended" is untouched

#### Scenario: Landscape attack mode
- **WHEN** a message with "Undo" shows in landscape attack mode at 100% and at 200% font
- **THEN** "Undo" is at least 64 dp in both dimensions, and the guidance column moves down below the taller message as D52
  requires, with no text covered

#### Scenario: Log medication in the dark room
- **WHEN** "Couldn't save. Your phone's storage is full." shows on Log medication opened from attack mode
- **THEN** its Dismiss is at least 64 by 64 dp with the same 20 dp icon

#### Scenario: Outside the dark room
- **WHEN** a message shows on Home Today in the light theme, or on History
- **THEN** its "Undo" and Dismiss keep their 48 dp size

#### Scenario: The room changes under a message
- **WHEN** a message is on screen and Home enters or leaves attack mode
- **THEN** its control takes the size of the room it is now in, in the same frame as the room's switch, with no size
  animation of its own and no later jump
- **AND** the tap guard follows the message's new height
- **AND** keyboard or TalkBack focus on the control stays, and so does the pause

#### Scenario: Nothing new is spoken
- **WHEN** TalkBack reads a message, in or out of the dark room
- **THEN** it reads the same text and labels as before
- **AND** the pause's focus probe adds no semantics node, label, action or change to the reading order

### Requirement: A field problem replaces the message on the medication forms
On Edit medication and on Log medication's first-medication form, when Save or "Add and log" finds a field problem, any
message on the screen SHALL go in the same frame, silently, so the field error is the only error and no message covers
Name. A later attempt that fails for another reason SHALL show its message again (BRD EC-UX-70, D191).

#### Scenario: Storage message, then an empty name
- **WHEN** "Couldn't save. Your phone's storage is full." shows on Edit medication, the user frees space, clears Name and
  saves
- **THEN** the storage message is gone in the same frame that "Enter a name." appears, Name and its error are in view and
  not covered, and only "Enter a name." is announced

#### Scenario: Storage still full after the fix
- **WHEN** the storage message went with a name problem, storage is still full, and the user fixes the name and saves
- **THEN** "Couldn't save. Your phone's storage is full." shows again and is announced

#### Scenario: Any other message
- **WHEN** "Something went wrong. Please try again.", "You can keep up to 50 medications. Remove one you no longer take." or
  "Pick a time up to now." shows and Save or "Add and log" then finds a field problem
- **THEN** the message goes silently in the same frame and the field problem follows the field rules

#### Scenario: The first-medication form
- **WHEN** a message shows on Log medication's first-medication form and "Add and log" finds the name empty
- **THEN** the message goes in the same frame and nothing covers "Taken at" or Name

#### Scenario: Rotation after the clear
- **WHEN** the message went with a field problem and the phone rotates or the process is restored
- **THEN** the field error shows and the message does not come back
