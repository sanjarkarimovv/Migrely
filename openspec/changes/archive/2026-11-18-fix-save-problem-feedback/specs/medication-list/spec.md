## ADDED Requirements

### Requirement: Save problems on Edit medication are heard and seen
When Save on Edit medication (Add or Edit) finds a problem, the field with it SHALL scroll fully into view, the app SHALL
announce it once, and every press SHALL make it heard, from the app or the field's own reading. The soft keyboard SHALL stay
as it was; focus SHALL move to the field only with a hardware keyboard or the soft keyboard open on another field. The field
SHALL read its real error, never "Invalid input", with no extra stop after 1 s (BRD EC-UX-66 to EC-UX-71, D191).

#### Scenario: Empty name with TalkBack (the issue)
- **WHEN** Name is cleared with the keyboard up, TalkBack is on Save, and the user double taps
- **THEN** nothing is saved, TalkBack says "Enter a name." once within 3 seconds, TalkBack's focus stays on Save, the page
  scrolls so Name and "Enter a name." are fully inside the visible part, the keyboard stays open, and input focus stays in
  Name

#### Scenario: Duplicate or archived name
- **WHEN** the user saves "Ibuprofen" while Ibuprofen exists, or a name that is in the archive
- **THEN** the same happens with "You already have Ibuprofen." or "Ibuprofen is in your archive. Add it as a new medication
  to bring it back."

#### Scenario: Invalid dose
- **WHEN** Save finds the usual dose invalid and the name valid
- **THEN** the same happens for Usual dose with "Keep the dose to 20 characters."

#### Scenario: One field at a time
- **WHEN** the name is empty and the dose is invalid
- **THEN** only Name shows an error, because Name comes first

#### Scenario: Already in view
- **WHEN** Name and its error line are already fully visible when the problem arrives
- **THEN** the page does not move

#### Scenario: TalkBack moves its own focus after the scroll
- **WHEN** TalkBack moves its focus to the first item in view after the scroll and reads Name with its error
- **THEN** that reading comes from TalkBack, not from the app, and a double tap there only puts the cursor in Name

#### Scenario: Hardware keyboard
- **WHEN** the user presses Enter on Save with a hardware keyboard and the name has a problem
- **THEN** focus moves to Name with its focus indicator, Name and its error are in view, no soft keyboard opens, and with
  TalkBack on the field's own reading is the one announcement

#### Scenario: Soft keyboard open on Usual dose
- **WHEN** the name is empty, the cursor is in Usual dose with the soft keyboard open, the user taps Save and then types "a"
- **THEN** input focus moves to Name with the cursor at its end, the keyboard never closes, Name reads "a", and the dose is
  unchanged

#### Scenario: Soft keyboard closed
- **WHEN** the name is empty, the keyboard is closed, and the user scrolls to Save and taps it
- **THEN** no field takes focus, the keyboard stays closed, Name and its error scroll into view, and the error is announced
  once

#### Scenario: Swiping through the form
- **WHEN** after a name problem the user swipes from Back to Save and back with TalkBack
- **THEN** Name is one stop that says its label, its value and the error once, "Invalid input" is never spoken, and from 1
  second after the press no separate stop repeats the error

#### Scenario: A swipe within 1 second (accepted residual)
- **WHEN** TalkBack is on Name, the user presses Save with an empty name, and swipes left within 1 second
- **THEN** TalkBack may land on the announcement and say "Enter a name." a second time; a swipe 1.5 seconds after the press
  goes past it to the content above, and v1.3.1 has no such node

#### Scenario: Every press is heard
- **WHEN** the keyboard is closed, TalkBack is on, and the user presses Save with an empty name several times
- **THEN** on every press "Enter a name." is heard, from the app's announcement or from TalkBack's reading of Name; TalkBack
  may also say "Migrely"; a press with no error heard at all is a fail

#### Scenario: Switch Access and Voice Access
- **WHEN** a name problem shows and the user reaches Name with Switch Access, or says "Tap Name" with Voice Access
- **THEN** Name is reached and described with its real error, as with TalkBack

#### Scenario: Save again with the same problem
- **WHEN** the user saves again without editing anything
- **THEN** the error is announced again, once

#### Scenario: Typing after the error
- **WHEN** the user types "Ib" in Name after the error
- **THEN** the error leaves silently, the counter comes back silently, and only TalkBack's own key echo is heard

#### Scenario: Two quick presses
- **WHEN** the user presses Save twice quickly
- **THEN** one result arrives, it is announced once, and the press while Save waits does nothing and says nothing

#### Scenario: No haptic and no flash
- **WHEN** a field problem arrives
- **THEN** there is no haptic and no sound, and only the field's outline and line change colour, in one step

#### Scenario: Largest font size and landscape
- **WHEN** the same steps run at 200% font in portrait and in landscape, in windows 411 and 360 dp tall
- **THEN** the field region comes into view; where it is taller than the visible part with the keyboard open, the input line
  comes first, then the error line, then the label, and nothing is cut off

#### Scenario: Remove animations
- **WHEN** animations are removed and a field problem arrives
- **THEN** the page reaches its place in one step

#### Scenario: Right to left and light theme
- **WHEN** the steps run in a right-to-left language and in the light theme
- **THEN** the error line sits at the field's start edge, mirrored, and its colour has at least 4.5:1 contrast

#### Scenario: Free and Premium
- **WHEN** the same steps run on Free and on Premium
- **THEN** the result is the same

## MODIFIED Requirements

### Requirement: Edit medication with a keyboard
Add and Edit SHALL go Back, [message], Name, For an attack, Preventive, then only with For an attack Simple painkiller,
Triptan, Combination or other, Not sure, then Usual dose, Save; Shift+Tab the exact reverse. Each option is a stop;
arrows move between options; Space or Enter selects; focus never selects. Tab leaves a single-line field. Disabled
controls are not stops (BRD EC-UX-49, D181, D191).

#### Scenario: Tab order
- **WHEN** the user opens "Add a medication" with "For an attack" chosen and presses Tab from Back
- **THEN** focus goes Name, For an attack, Preventive, Simple painkiller, Triptan, Combination or other, Not sure, Usual
  dose, Save
- **AND** Shift+Tab from Save visits the same stops in reverse

#### Scenario: A message shows
- **WHEN** a message shows on the screen and the user presses Tab from Back
- **THEN** the message's control comes next, then Name

#### Scenario: Focus never selects
- **WHEN** focus is on "For an attack" and the user presses the down arrow
- **THEN** focus moves to "Preventive" and "For an attack" stays chosen, until the user presses Space or Enter

#### Scenario: Kind changes under focus
- **WHEN** focus is on "Preventive" and the user presses Space
- **THEN** the type options leave, focus stays on "Preventive", and the next Tab goes to Usual dose
- **AND** after choosing "For an attack" again, the next Tab from it reaches Preventive, then Simple painkiller

#### Scenario: Tab leaves a field
- **WHEN** focus is in Name and the user presses Tab
- **THEN** focus moves on and no tab character is typed

#### Scenario: Save with a name problem
- **WHEN** the user presses Enter on Save with a hardware keyboard, and the name is a duplicate, archived or invalid
- **THEN** keyboard focus moves to Name, Name and its error line are in view, and TalkBack reads the field once with its
  real error, never "Invalid input"

#### Scenario: Save with an invalid dose
- **WHEN** the user presses Enter on Save with a hardware keyboard, and the usual dose is invalid
- **THEN** keyboard focus moves to Usual dose, and TalkBack reads its error

#### Scenario: Save problem by touch
- **WHEN** the user taps Save with a duplicate name, with no hardware keyboard in use and the soft keyboard closed
- **THEN** focus does not move and the soft keyboard does not open, the error shows under Name, Name and the error scroll
  into view, and the error is announced once (requirement "Save problems on Edit medication are heard and seen")

#### Scenario: Saving
- **WHEN** focus is on Save and Save becomes disabled while the save is pending
- **THEN** focus is cleared, Save is not a stop, and the next Tab goes to Back

#### Scenario: Not found
- **WHEN** the medication being edited no longer exists
- **THEN** Back is the only stop

#### Scenario: Largest font size and landscape
- **WHEN** the font size is 200%, in portrait or landscape, in the dark or the light theme
- **THEN** the same order holds and each stop scrolls fully into view with its focus indicator
