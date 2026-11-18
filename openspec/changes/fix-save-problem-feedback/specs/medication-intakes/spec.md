## ADDED Requirements

### Requirement: First-use name problems are heard and seen
On Log medication's first-medication form, normal and in the dark room, "Add and log" with a name problem SHALL follow the
rules of Save on Edit medication: Name scrolls fully into view, the problem is announced once by the app, the soft keyboard
stays as it was, focus moves to Name only with a hardware keyboard, and Name reads its real error, never "Invalid input"
(BRD EC-UX-66 to EC-UX-71, D191).

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
- **THEN** Name says its label, its value and "Enter a name." once, with no separate stop for the error and never "Invalid
  input"

#### Scenario: Largest font size and landscape
- **WHEN** the same steps run at 200% font in portrait and in landscape, in windows 411 and 360 dp tall
- **THEN** the input line, then the error line, then the label come into view, and nothing is cut off
