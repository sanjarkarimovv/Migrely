## MODIFIED Requirements

### Requirement: Days reachable with a keyboard
The month SHALL be one Tab stop: Tab enters it on the chosen day, otherwise today, otherwise the first day, and the
next Tab leaves it; Shift+Tab does the same backwards. Arrow keys SHALL move a day or a week on or back, with left and
right mirrored in right-to-left layouts, and SHALL do nothing off the selectable days; Enter SHALL choose the
focused day. Each selectable day SHALL stay focusable for accessibility services such as Switch Access, with its spoken
description and state (BRD EC-UX-19, EC-UX-15, D38).

#### Scenario: Tab through the days
- **WHEN** History is open and the user presses Tab on a hardware keyboard until the calendar is reached
- **THEN** focus lands on one day, the arrow keys move it through the selectable days, and Enter chooses the focused
  day

#### Scenario: One Tab stop
- **WHEN** focus is on today in home's calendar and the user presses Tab
- **THEN** focus leaves the calendar for "Migraine started", without visiting the other days

#### Scenario: Entering on today
- **WHEN** focus is on the month's title and the user presses Tab, with no day chosen
- **THEN** focus lands on today

#### Scenario: Arrow keys
- **WHEN** focus is on 14 October and the user presses the right arrow and then the down arrow
- **THEN** focus is on 22 October

#### Scenario: Arrow keys stay on selectable days
- **WHEN** focus is on today, 24 October, in home's calendar and the user presses the right arrow or the down arrow
- **THEN** focus stays on 24 October, because the days after it cannot be chosen yet

#### Scenario: Accessibility services
- **WHEN** an accessibility service inspects a past day
- **THEN** the day is reported as focusable and clickable, with its description
