## ADDED Requirements

### Requirement: Home Today's message order holds with nothing after Settings
On Home Today with a message, Shift+Tab from the first control after Settings in Tab order SHALL go to the message's
control, then Settings, whichever column that control is in. With nothing focusable after Settings in its column, Tab
SHALL go Settings, message, "Migraine started", and Shift+Tab the exact reverse, with no stop added or skipped (BRD
EC-UX-47, EC-UX-36, D178 (4), D181).

#### Scenario: Nothing after Settings
- **WHEN** a message with "Undo" shows on Home Today, nothing focusable follows Settings in its column, and the user
  presses Tab three times from nothing focused
- **THEN** focus goes Settings, the message's "Undo", "Migraine started"
- **AND** Shift+Tab twice goes back to "Undo", then Settings

#### Scenario: The month card is not ready yet
- **WHEN** a message shows right after a fresh start, before the month card has loaded, and the user presses Shift+Tab
  from "Migraine started"
- **THEN** focus goes to the message's control, not past it to Settings

#### Scenario: A short window
- **WHEN** the phone is in a short landscape window at 200% font, the start column holds only the date and Settings,
  and the user presses Shift+Tab from the first control of the end column
- **THEN** focus goes to the message's control, then to Settings

#### Scenario: A control after Settings
- **WHEN** a focusable control, such as the trial banner, follows Settings in its column and the user presses Shift+Tab
  from it
- **THEN** focus goes to the message's control, then Settings, as before
