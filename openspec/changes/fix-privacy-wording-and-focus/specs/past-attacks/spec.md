## ADDED Requirements

### Requirement: Add past migraine with a keyboard
The draft SHALL go Back, [message], Started, Ended, Add migraine; with an overlap, Back, [message], Started, Ended, Merge
them, Change the times. Shift+Tab SHALL reverse it, and disabled controls are not stops. After "Change the times", focus
SHALL move to Started. A picker SHALL return focus to the row that opened it (BRD EC-UX-51, D181).

#### Scenario: The draft
- **WHEN** the user opens "Add a past migraine" and presses Tab from Back
- **THEN** focus goes Started, Ended, Add migraine
- **AND** Shift+Tab visits the same stops in reverse

#### Scenario: An overlap
- **WHEN** the chosen times overlap a saved migraine and the user presses Tab from Back
- **THEN** focus goes Started, Ended, Merge them, Change the times

#### Scenario: Change the times
- **WHEN** the user presses Enter on "Change the times"
- **THEN** the card leaves, keyboard focus is on Started, and nothing is saved or merged

#### Scenario: A picker
- **WHEN** the user opens Ended with Enter and then confirms or cancels the picker
- **THEN** focus is back on Ended

#### Scenario: A message shows
- **WHEN** a message shows on the screen and the user presses Tab from Back
- **THEN** the message's control comes next, then Started

#### Scenario: Largest font size and landscape
- **WHEN** the font size is 200%, in portrait or landscape, in the dark or the light theme
- **THEN** the same order holds and each stop scrolls fully into view with its focus indicator
