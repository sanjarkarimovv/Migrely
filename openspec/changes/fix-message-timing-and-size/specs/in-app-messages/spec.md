## ADDED Requirements

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
While TalkBack's focus is on a timed message's text or control, its countdown SHALL pause as for keyboard focus, with the
same 300 ms grace, which covers TalkBack's move from the text to the control, and the same 3 second floor. Turning TalkBack
off SHALL count as focus leaving. TalkBack being on SHALL NOT make a message indefinite; a message pauses only while focus
holds it, and an Undo still leaves at its ceiling (BRD EC-UX-54, EC-UX-55, D182).

#### Scenario: From the text to the control
- **WHEN** TalkBack focuses a message's text and the user swipes to its control 2 seconds later
- **THEN** the countdown stays paused through the move and does not resume between them

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

## MODIFIED Requirements

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
