## ADDED Requirements

### Requirement: Edit medication with a keyboard
Add and Edit SHALL go Back, [message], Name, For an attack, Preventive, then only with For an attack Simple painkiller,
Triptan, Combination or other, Not sure, then Usual dose, Save; Shift+Tab the exact reverse. Each option is a stop;
arrows move between options; Space or Enter selects; focus never selects. Tab leaves a single-line field. Disabled
controls are not stops (BRD EC-UX-49, D181).

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
- **THEN** keyboard focus moves to Name, and TalkBack reads the error from its supporting text

#### Scenario: Save with an invalid dose
- **WHEN** the user presses Enter on Save with a hardware keyboard, and the usual dose is invalid
- **THEN** keyboard focus moves to Usual dose, and TalkBack reads its error

#### Scenario: Save problem by touch
- **WHEN** the user taps Save with a duplicate name, with no hardware keyboard in use
- **THEN** focus does not move and the soft keyboard does not open, and the error shows under Name as before

#### Scenario: Saving
- **WHEN** focus is on Save and Save becomes disabled while the save is pending
- **THEN** focus is cleared, Save is not a stop, and the next Tab goes to Back

#### Scenario: Not found
- **WHEN** the medication being edited no longer exists
- **THEN** Back is the only stop

#### Scenario: Largest font size and landscape
- **WHEN** the font size is 200%, in portrait or landscape, in the dark or the light theme
- **THEN** the same order holds and each stop scrolls fully into view with its focus indicator
