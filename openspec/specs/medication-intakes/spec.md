# medication-intakes Specification

## Purpose
Defines logging a medication taken: one screen used from attack mode, the details screen and home, which medication
and time an intake records, how it links to an attack, and how intakes follow their attack when it is deleted or
merged, so that every pill is on record with the least effort, even during an attack (BRD MED-F3, D34, EC-MED-08 to
EC-MED-10).

## Requirements

### Requirement: One tap logs a medication
The "Medication taken" screen SHALL list the active medications, acute first. Tapping one SHALL log it at the shown time
with its default dose, and SHALL then show what was logged with "Undo" and "Done". The time SHALL default to now, or to
the start of an ended attack when opened from its details, and SHALL be changeable but never set more than 5 minutes in
the future (BRD MED-F3, D34, EC-ATK-03).

#### Scenario: Log during an attack
- **WHEN** an attack is in progress, the user taps "+ Medication taken" and then "Ibuprofen · 400 mg"
- **THEN** an intake of Ibuprofen 400 mg at the current minute is linked to the attack, and "Ibuprofen 400 mg logged at
  14:20" is shown with "Undo" and "Done"

#### Scenario: Undo a logged intake
- **WHEN** the user taps "Undo" after logging
- **THEN** the intake is removed and the list of medications is shown again

#### Scenario: Time in the future
- **WHEN** the user picks a time 10 minutes from now
- **THEN** the picker does not allow it

### Requirement: No medication yet
When the user has no active medication, the screen SHALL offer to add one, with its name and category, and SHALL then
log it at once (BRD EC-MED-08).

#### Scenario: First medication during an attack
- **WHEN** no medication exists and the user taps "+ Medication taken" during an attack
- **THEN** the screen asks for the name and category, and after "Add and log" the intake is logged

### Requirement: Intakes link to their attack
An intake logged from attack mode or from an attack's details SHALL be linked to that attack, and its time SHALL be
shown, picked and stored in the zone where that attack started. An intake logged from home SHALL be linked to the
attack whose time overlaps the intake's minute, if any, in the device's zone. Undo on the screen SHALL keep it for the
attack it was opened for, or for none. Changing an attack's times later SHALL NOT change its intakes (BRD EC-MED-09,
D34, EC-TIM-02).

#### Scenario: Logged from home during a past attack's time
- **WHEN** an attack ran 09:00–11:00 and the user logs Ibuprofen from home at 10:15
- **THEN** that attack's details list Ibuprofen at 10:15

#### Scenario: Logged from home in an attack's first minute
- **WHEN** an attack started at 12:20:40 and the user logs Ibuprofen from home at 12:20
- **THEN** that attack's details list Ibuprofen

#### Scenario: Logged from home outside any attack
- **WHEN** no attack contains 16:00 and the user logs Ibuprofen at 16:00
- **THEN** the intake is stored on its own

#### Scenario: Undo, then a new time from home
- **WHEN** from home the user logs Ibuprofen at 10:15 inside an attack, taps "Undo", picks 16:00 and logs it again
- **THEN** the 16:00 intake is stored on its own

#### Scenario: Later, for an attack recorded in another zone
- **WHEN** an attack started at 14:00 in Tokyo and the user, now in London, adds a medication from its details
- **THEN** the time shows "14:00 (Tokyo)", and picking 14:30 stores 14:30 Tokyo time with the Tokyo zone

### Requirement: Intakes follow their attack
When an attack is deleted, its intakes SHALL be hidden with it, Undo SHALL bring them back, and they SHALL be removed
for good with the attack. When attacks are merged, their intakes SHALL move to the remaining attack. When "Undo" removes
an attack that was started by mistake, a medication logged meanwhile SHALL stay, on its own (BRD EC-MED-10, D27, D23).

#### Scenario: Delete and undo
- **WHEN** an attack with two intakes is deleted and the user taps "Undo"
- **THEN** the attack and both intakes are back

#### Scenario: Merge
- **WHEN** an attack with Ibuprofen is merged into an attack with Sumatriptan
- **THEN** the remaining attack lists both

#### Scenario: Undo a start after logging a medication
- **WHEN** the user starts an attack, logs Ibuprofen, and taps "Undo" on "Migraine started"
- **THEN** the attack is gone and Ibuprofen stays as an intake on its own

### Requirement: Usable in the dark room
Opened from attack mode, the screen SHALL keep attack mode's dim colours, use controls at least 64 dp tall and show no
more text than needed (BRD EC-UX-01, EC-UX-02, 18.3 B).

#### Scenario: Large choices
- **WHEN** the screen is opened from attack mode
- **THEN** each medication is a button at least 64 dp tall on the dark attack-mode background

### Requirement: Accessible logging
The screen SHALL work with TalkBack, keyboards and switch access, at 200% font and in landscape, and SHALL keep the
picked time across rotation and process death (BRD EC-UX-03, EC-UX-04, EC-ATK-10).

#### Scenario: Rotation keeps the time
- **WHEN** the user changes the time to 13:40 and rotates the phone
- **THEN** the time is still 13:40

### Requirement: Log medication with a keyboard
The list SHALL go Back, [message], Taken at, each medication in list order, Manage medications. First use: Back,
[message], Taken at, Name, the four types, Add and log. Logged: Back, [message], Done, Undo. Shift+Tab SHALL reverse it.
After logging by keyboard, focus SHALL clear and not move. A picker SHALL return focus to its row. The dark room keeps
the order (BRD EC-UX-50, D181).

#### Scenario: The list
- **WHEN** the user opens "Medication taken" with two saved medications and presses Tab from Back
- **THEN** focus goes Taken at, the first medication, the second, Manage medications
- **AND** Shift+Tab visits the same stops in reverse

#### Scenario: First use
- **WHEN** no medication is saved yet and the user presses Tab from Back
- **THEN** focus goes Taken at, Name, Simple painkiller, Triptan, Combination or other, Not sure, Add and log, and focus
  on a type never selects it

#### Scenario: Logged by keyboard
- **WHEN** the user presses Enter on a medication and it is logged
- **THEN** focus is cleared and moves nowhere, so TalkBack reads the logged sentence whole
- **AND** the next Tab goes to Back, then Done, then Undo

#### Scenario: The time picker
- **WHEN** the user opens Taken at with Enter and then confirms or cancels the picker
- **THEN** focus is back on Taken at

#### Scenario: The dark room
- **WHEN** the screen is opened from attack mode, with 64 dp controls on the dark background
- **THEN** the order is the same as in the normal screen

#### Scenario: Largest font size and landscape
- **WHEN** the font size is 200%, in portrait or landscape, with or without a message
- **THEN** the same order holds and each stop scrolls fully into view with its focus indicator

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

### Requirement: Undo on Log medication keeps the logged result until the intake is removed
After "Undo" on the logged result, the result with "Done" and "Undo" SHALL stay until the removal is stored; then the list
SHALL show with "Removed. Nothing was logged.". If the removal fails, the result SHALL stay, a message starting "Still logged."
SHALL say so, and "Undo" SHALL work again. A second "Undo" before the outcome SHALL do nothing (BRD EC-MED-17, D190).

#### Scenario: Undo succeeds
- **WHEN** the user taps "Undo" on "Ibuprofen 400 mg logged at 14:20" and the removal is stored
- **THEN** the intake is gone, the list of medications shows, and "Removed. Nothing was logged." shows

#### Scenario: Undo with full storage
- **WHEN** the user taps "Undo" and the removal fails because storage is full
- **THEN** the intake is still stored, the logged result with "Done" and "Undo" stays, "Still logged. Your phone's storage
  is full." shows as an error with no time limit, "Done" is not covered, and TalkBack speaks it

#### Scenario: Undo again after freeing space
- **WHEN** the removal failed, the user frees space and taps "Undo" again
- **THEN** the intake is removed, the list shows, and "Removed. Nothing was logged." replaces the error

#### Scenario: Undo fails for another reason
- **WHEN** the removal fails with any other error
- **THEN** the logged result stays and "Still logged. Something went wrong. Please try again." shows as an error for 8
  seconds or the accessibility timeout

#### Scenario: Two quick taps on Undo
- **WHEN** the user taps "Undo" twice before the outcome
- **THEN** one removal runs and one outcome shows

#### Scenario: Done before the outcome
- **WHEN** the user taps "Undo" and then "Done" before the outcome
- **THEN** the screen closes as before, and no message from the late outcome shows on the screen it returns to

#### Scenario: Rotation or a restored process during Undo
- **WHEN** the phone rotates, or the process is restored, while a removal runs
- **THEN** the screen shows the logged result if the intake may still be stored, a later "Undo" removes it or confirms it is
  gone with "Removed. Nothing was logged.", and no second intake is ever removed

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
