# medication-list Specification

## Purpose
Defines the user's own list of medications: what a medication records, how it is added, edited and archived, and the
rules that keep the list clean, so that every intake can name what was taken and later be counted for overuse (BRD
MED-F1, MED-F2, EC-MED-01, EC-MED-07, D35).

## Requirements

### Requirement: Add a medication
The user SHALL be able to add a medication with a name of 1 to 60 characters, a kind (acute or preventive) and, for an
acute medication, a category: simple painkiller, triptan, combination or other, or "Not sure". The user SHALL also be
able to give an optional default dose of at most 20 characters. Surrounding spaces SHALL be trimmed (BRD MED-F1,
MED-F2, D35).

#### Scenario: Add an acute medication
- **WHEN** the user adds "Ibuprofen", acute, simple painkiller, default dose "400 mg"
- **THEN** "Ibuprofen · 400 mg" appears in My medications

#### Scenario: Preventive has no category
- **WHEN** the user chooses preventive
- **THEN** no category is asked

#### Scenario: Name too long
- **WHEN** the user types a 61st character in the name
- **THEN** the field stops at 60 characters and a counter shows the limit

### Requirement: Names are unique
Two medications SHALL NOT share a name, ignoring case and surrounding spaces. Adding the name of an active medication
SHALL be refused with a message naming it. Adding the name of an archived medication SHALL bring that medication back
with its history (BRD EC-MED-07).

#### Scenario: Duplicate name
- **WHEN** "Ibuprofen" exists and the user adds " ibuprofen "
- **THEN** nothing is added and "You already have Ibuprofen." is shown

#### Scenario: Renaming to an archived name
- **WHEN** "Sumatriptan" is archived and the user renames another medication to "Sumatriptan"
- **THEN** nothing is saved and "Sumatriptan is in your archive. Add it as a new medication to bring it back." is shown

#### Scenario: Archived name
- **WHEN** "Sumatriptan" (Triptan, 50 mg) was archived and the user adds "sumatriptan" with "Not sure" and no dose
- **THEN** Sumatriptan is active again as it was, Triptan with 50 mg, and keeps its past intakes

### Requirement: Edit a medication
The user SHALL be able to change a medication's name, kind, category and default dose under the same rules. Past
intakes SHALL show the new name (BRD D35).

#### Scenario: Rename
- **WHEN** the user renames "Ibuprofen" to "Ibuprofen 400"
- **THEN** past intakes show "Ibuprofen 400"

### Requirement: Archive instead of delete
Removing a medication SHALL archive it. An archived medication SHALL disappear from My medications and from the
choices when logging, and SHALL stay in the history of every intake. Archiving SHALL be undoable from the message
(BRD EC-MED-01, EC-UX-16).

#### Scenario: Archive
- **WHEN** the user archives "Paracetamol", which was taken last week
- **THEN** Paracetamol is no longer offered, "Medication removed" with "Undo" is shown, and last week's attack still
  lists it

### Requirement: Order and limit
My medications SHALL list the most recently used medications first, then by name; the choices when logging put acute
medications before preventive ones in that order (see medication-intakes). At most 50 medications SHALL be active at
once; at the limit, adding, and "Undo" after archiving, SHALL be refused with an explanation (BRD D35).

#### Scenario: Recently used first
- **WHEN** Sumatriptan was taken yesterday and Ibuprofen last month
- **THEN** Sumatriptan is listed before Ibuprofen

#### Scenario: Limit reached
- **WHEN** 50 medications are active and the user tries to add another
- **THEN** it is refused with "You can keep up to 50 medications. Remove one you no longer take."

### Requirement: Accessible medication screens
The medication screens SHALL work with TalkBack, keyboards and switch access, and at 200% font and in landscape,
without clipped text (BRD EC-UX-03, EC-UX-04, EC-UX-19).

#### Scenario: Screen reader on a medication
- **WHEN** TalkBack focuses "Ibuprofen · 400 mg"
- **THEN** it reads the name, the dose and the category, for example "Ibuprofen, 400 mg, simple painkiller. Opens it to
  edit."

### Requirement: Names and doses at their caps
An edit that does not fit the medication name (60 characters, in the edit form or when adding one while logging) or the
dose (20 characters) SHALL insert only the whole characters that fit. It SHALL never remove text elsewhere in the field
or split a character such as an emoji. When nothing fits, the text and the cursor SHALL stay as they were. Text and
cursor SHALL survive recreation of the screen (BRD EC-MED-13, D35).

#### Scenario: Typing in the middle of a full name
- **WHEN** the name has 60 characters, in the edit form or when adding one while logging, and the user types a letter in
  the middle
- **THEN** the name is unchanged, its last character included, and the cursor stays where it was

#### Scenario: Typing in the middle of a full dose
- **WHEN** the dose has 20 characters and the user types a letter in the middle
- **THEN** the dose is unchanged, its last character included, and the cursor stays where it was

#### Scenario: An emoji that does not fit
- **WHEN** the name has 59 characters or the dose has 19 and the user types "👍", which takes two
- **THEN** the field is unchanged, and no half of the emoji is stored

#### Scenario: Pasting across the cap
- **WHEN** the dose has 16 characters and the user pastes "abc👍👍", where each "👍" takes two
- **THEN** "abc" is inserted, the dose has 19 characters, and the cursor is after "abc"

#### Scenario: The screen is recreated
- **WHEN** a name or dose holds typed text with the cursor in the middle and the screen is recreated
- **THEN** the text and the cursor are as they were

### Requirement: The edit form shows what it saves
A stored name or dose SHALL open with the cursor at its start, because a person recognises a medication by the start of
its name. What the form shows SHALL be what it saves: when two inputs race in one frame, the losing edit SHALL be dropped
from the field as well as from the save. A cursor move or a refused edit SHALL NOT change the form, so it SHALL NOT clear
a shown save problem (BRD EC-MED-13, D152).

#### Scenario: A long stored name
- **WHEN** a medication with a name wider than the field is opened for editing
- **THEN** the cursor is at the start of the name and dose, and the start of the name is on screen

#### Scenario: Typing and tapping a kind in one frame
- **WHEN** a letter typed in the name or dose and a tap on a kind arrive before the next frame
- **THEN** the kind changes, the field shows the same text that "Save" would store, and typing the letter again keeps it

#### Scenario: Deleting and retyping in one frame
- **WHEN** the cursor was moved, and a deletion and a retype in the name or dose arrive before the next frame
- **THEN** the field shows the same text that "Save" would store

#### Scenario: A cursor move or a refused edit
- **WHEN** a save problem shows and the user moves the cursor, or types in a full field
- **THEN** the form does not change and the save problem stays

### Requirement: The editor belongs to one medication
The medication editor SHALL hold one medication's id: another id SHALL start it afresh, and the form and the row Save writes
SHALL always be the same medication. A read of another medication SHALL NOT fill the form. A loaded form SHALL win over
NotFound, which carries no id (BRD EC-MED-14, D157).

#### Scenario: Two touches on My medications
- **WHEN** the user touches two rows within a fade, by two taps or a resting finger, then renames and saves
- **THEN** only the medication on the form changes, and the other row, its intakes and its overuse warnings stay

#### Scenario: A late read of another medication
- **WHEN** a read of medication A arrives while the editor is open for medication B
- **THEN** the form stays on B, and Save writes B

#### Scenario: Two reads in one frame
- **WHEN** the right read and a late read of another medication arrive in one frame
- **THEN** the form shows the right medication and is not blank

#### Scenario: A late read on Add
- **WHEN** a late read of a medication arrives on the Add form
- **THEN** the form does not take it

#### Scenario: A late empty read on Edit
- **WHEN** a late empty read from Add arrives on the Edit form
- **THEN** the form does not take it

#### Scenario: A second read of the same medication
- **WHEN** a second read of the same medication arrives after the user has typed
- **THEN** what was typed stays

#### Scenario: A late NotFound before the right read
- **WHEN** a NotFound of another medication arrives before the right read
- **THEN** the form shows once the right read arrives

#### Scenario: A late NotFound after the right read
- **WHEN** a NotFound of another medication arrives after the right read
- **THEN** the form stays on screen

#### Scenario: A medication that no longer exists
- **WHEN** the edited medication no longer exists
- **THEN** the editor says so and offers no Save

#### Scenario: Recreation
- **WHEN** the editor is rotated or its process killed after the form has loaded
- **THEN** what was typed stays, and Save writes the same medication

### Requirement: Kind and Category while the form opens
On Add and Edit, Kind and Category SHALL ignore touches for 700 ms after the form appears. Name, Dose, Save and Back SHALL
take a first touch at once, unless it is ignored as the second tap of a double tap. A deliberate first touch on Kind or Category in that time SHALL be ignored with no feedback, and
a rotation SHALL arm it again. Screen readers, switch access and keyboards SHALL NOT be affected (BRD EC-MED-15, D157, D159).

#### Scenario: A stray tap on Edit
- **WHEN** the user taps a row of My medications, and a second tap lands on "Preventive" within 700 ms of the form appearing
- **THEN** the kind does not change

#### Scenario: A stray tap on Add
- **WHEN** a tap lands on "Preventive" within 700 ms of the Add form appearing
- **THEN** it is not chosen

#### Scenario: A stray tap on a category
- **WHEN** a tap lands on a category within 700 ms of the form appearing
- **THEN** the category does not change

#### Scenario: After the guard
- **WHEN** the user taps Kind or Category after 700 ms
- **THEN** the choice changes

#### Scenario: Name, Dose, Save and Back are free
- **WHEN** the user's first tap just after the form appears is on Name, Dose, Save or Back
- **THEN** that control answers, unless the press is ignored as the second tap of a double tap (D159)

#### Scenario: A deliberate tap in the guard
- **WHEN** the user deliberately taps Kind within 700 ms of the form appearing
- **THEN** nothing changes and nothing says so, and a second tap after 700 ms works

#### Scenario: A rotation
- **WHEN** the editor is rotated
- **THEN** Kind and Category ignore touches for 700 ms again, and the form is kept

#### Scenario: A screen reader in the guard
- **WHEN** TalkBack activates Kind within 700 ms of the form appearing
- **THEN** the choice changes

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
