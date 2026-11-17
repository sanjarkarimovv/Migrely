## ADDED Requirements

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
