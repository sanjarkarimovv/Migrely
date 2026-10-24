## MODIFIED Requirements

### Requirement: The Reminders screen
Home SHALL show a "Settings" button in its header in normal mode only, and Settings SHALL list "Reminders". It SHALL
open a screen with "Daily reminder", off by default, with its time (20:00 by default), and "Ask if a medication helped"
(BRD D65, D66, D67, D70, EC-UX-13).

#### Scenario: Opening Reminders
- **WHEN** the user taps "Settings" on home, then "Reminders"
- **THEN** the Reminders screen shows "Daily reminder" off at 8:00 PM and "Ask if a medication helped"

#### Scenario: Attack mode
- **WHEN** an attack is in progress
- **THEN** home shows no "Settings" button
