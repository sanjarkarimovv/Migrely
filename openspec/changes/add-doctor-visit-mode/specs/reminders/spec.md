## MODIFIED Requirements

### Requirement: The Reminders screen
Home SHALL show a "Settings" button in its header in normal mode only, and Settings SHALL list "Reminders". It SHALL
open a screen with "Daily reminder", off by default, with its time (20:00 by default), "Ask if a medication helped",
and a "Doctor visit" section whose "Next visit" reads "Not set" until a date is saved (BRD D65, D66, D67, D70, D168,
REM-F4, EC-UX-13).

#### Scenario: Opening Reminders
- **WHEN** the user taps "Settings" on home, then "Reminders"
- **THEN** the Reminders screen shows "Daily reminder" off at 8:00 PM, "Ask if a medication helped", and "Next visit"
  reading "Not set"

#### Scenario: Attack mode
- **WHEN** an attack is in progress
- **THEN** home shows no "Settings" button

### Requirement: Notification permission
The app SHALL ask for notification permission only when the user turns a reminder on or sets a doctor visit, never at
first launch, and at most once in 30 days. When it is refused, a reminder SHALL stay off, a doctor visit SHALL stay
saved, and the screen SHALL explain that notifications are off and link to the system settings (BRD EC-REM-01,
EC-REM-02, EC-REM-11, D68, D168).

#### Scenario: Turning the daily reminder on
- **WHEN** the user turns "Daily reminder" on without having allowed notifications
- **THEN** the system asks for permission, and the reminder is on only if it is allowed

#### Scenario: Refused
- **WHEN** the user refuses the permission
- **THEN** "Daily reminder" stays off, and the screen says notifications are off for Migrely, with "Open settings"

#### Scenario: Setting a visit without permission
- **WHEN** the user sets a doctor visit without having allowed notifications, and refuses when asked
- **THEN** the visit is saved, and the screen says notifications are off for Migrely, with "Open settings"
