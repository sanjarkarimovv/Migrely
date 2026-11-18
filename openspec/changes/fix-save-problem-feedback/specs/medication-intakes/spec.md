## ADDED Requirements

### Requirement: First-use name problems are heard and seen
On Log medication's first-medication form, normal and in the dark room, "Add and log" with a name problem SHALL follow the
rules of Save on Edit medication: Name scrolls fully into view, the app announces the problem once and every press makes it
heard, the soft keyboard stays as it was, focus moves to Name only with a hardware keyboard, and Name reads its real error,
never "Invalid input", with no extra stop after 1 s (BRD EC-UX-66 to EC-UX-71, D191).

#### Scenario: Empty name with TalkBack
- **WHEN** no medication exists, Name is empty with the keyboard up, TalkBack is on "Add and log", and the user double taps
- **THEN** nothing is saved, TalkBack says "Enter a name." once within 3 seconds, Name and the error come fully into view
  above the keyboard, and the keyboard stays open

#### Scenario: Hardware keyboard
- **WHEN** the user presses Enter on "Add and log" with a hardware keyboard and the name is empty
- **THEN** the press parks focus as today and, in the same result frame, focus moves to Name with its focus indicator, so the
  next Tab does not start from the top, and no soft keyboard opens

#### Scenario: Keyboard closed
- **WHEN** the name is empty, the keyboard is closed, and the user taps "Add and log"
- **THEN** no field takes focus, the keyboard stays closed, Name and its error scroll into view, and the error is announced
  once

#### Scenario: The dark room
- **WHEN** the form was opened from attack mode and "Add and log" finds the name empty
- **THEN** the same happens on the dark room background, with the dark theme's error colour, 64 dp controls and no light
  frame at any point

#### Scenario: Again, and typing
- **WHEN** the user taps "Add and log" again without editing, and then types a name
- **THEN** the error is announced again once, and typing is quiet apart from TalkBack's own key echo

#### Scenario: Swiping to Name
- **WHEN** after the problem the user swipes to Name with TalkBack
- **THEN** Name says its label, its value and "Enter a name." once, never "Invalid input", and from 1 second after the
  press no separate stop repeats the error; within that second a swipe may land on the announcement (accepted residual)

#### Scenario: Largest font size and landscape
- **WHEN** the same steps run at 200% font in portrait and in landscape, in windows 411 and 360 dp tall
- **THEN** the input line, then the error line, then the label come into view, and nothing is cut off

## MODIFIED Requirements

### Requirement: A logged intake clears the message on Log medication
When an intake is stored on Log medication, by a tap on a medication or by "Add and log", every message there SHALL go in
the frame the logged result appears, with no fade, no new message and no haptic; only the logged line SHALL be heard. A
future time, full storage or another failure SHALL keep the list and their message as before; a name problem on the
first-medication form SHALL clear it as a field problem (BRD EC-MED-16, EC-UX-70, D190, D191).

#### Scenario: Storage full, then logged after freeing space
- **WHEN** "Couldn't save. Your phone's storage is full." shows after a tap on Ibuprofen failed, the user frees space and taps
  Ibuprofen again
- **THEN** one new intake is stored, the error is gone within 1 second, "Ibuprofen 400 mg logged at 14:20" shows with "Done"
  and "Undo", no "Saved." shows, there is no haptic, and TalkBack speaks the logged line

#### Scenario: Another error, then logged
- **WHEN** "Something went wrong. Please try again.", "Pick a time up to now." or "You can keep up to 50 medications…" shows
  and the next tap on a medication or "Add and log" stores an intake
- **THEN** the message is gone and the logged result shows as above

#### Scenario: First medication added and logged
- **WHEN** no medication exists, "Add and log" failed because storage was full, and a later "Add and log" stores the
  medication and its intake
- **THEN** the error is gone, the logged result shows, and Ibuprofen is stored once as a medication with one intake

#### Scenario: An information message on screen
- **WHEN** "Removed. Nothing was logged." shows after an Undo and the user logs another medication before it times out
- **THEN** the message goes at once and the logged line shows with nothing over it

#### Scenario: It fails again
- **WHEN** an error shows, storage is still full, and the user taps Ibuprofen again
- **THEN** no intake is stored, the list stays, and the error shows in the same place and TalkBack speaks it again

#### Scenario: A name problem on the first-medication form
- **WHEN** no medication exists, "Couldn't save. Your phone's storage is full." shows, and "Add and log" then finds the name
  empty
- **THEN** no medication and no intake are stored, the message goes in the same frame and Name shows its error
  (in-app-messages "A field problem replaces the message on the medication forms")

#### Scenario: Not on the tap
- **WHEN** an error shows and the user taps Ibuprofen while the write is still running
- **THEN** the error stays until the write's outcome, and goes only if the intake was stored

#### Scenario: Two quick taps
- **WHEN** an error shows and the user taps Ibuprofen twice quickly after freeing space
- **THEN** exactly one intake is stored and the logged result shows once

#### Scenario: One outcome for the page and the message
- **WHEN** the logged outcome reaches the screen
- **THEN** the logged result and the cleared message show in the same frame, in either order of handling, with no fade-out
  frames, and no frame shows the logged result together with the old message

#### Scenario: Rotation and a restored process after the log
- **WHEN** the logged result shows after an error and the phone rotates, or the process is killed in the background and
  restored
- **THEN** the logged result shows again with no message over it

#### Scenario: The dark room
- **WHEN** Log medication was opened from attack mode, an error shows, and a later tap stores the intake
- **THEN** the error goes the same way, the logged result keeps the dark room's colours, and nothing else changes

#### Scenario: Keyboard and TalkBack
- **WHEN** the user logs by pressing Enter on a medication, or by a TalkBack double tap, while an error shows
- **THEN** the error goes, focus is cleared and moves nowhere, TalkBack reads only the logged line, once, and the next Tab
  goes to Back, then Done

#### Scenario: Largest font size and landscape
- **WHEN** the same steps run at 200% font, in portrait and in landscape
- **THEN** the whole logged line, "Done" and "Undo" are visible with no message over them

#### Scenario: Free and Premium
- **WHEN** the same steps run on Free and on Premium
- **THEN** the result is the same
