## ADDED Requirements

### Requirement: Offered during onboarding
The reminders feature SHALL provide the daily reminder offer that onboarding shows. It SHALL turn the reminder on at
its saved time, 8:00 PM by default, by the same permission rules as the Reminders screen (BRD REM-F1, EC-REM-01,
D80).

#### Scenario: Permission refused
- **WHEN** the user taps "Turn on" in onboarding and refuses notifications
- **THEN** the reminder stays off, onboarding goes on, and the Reminders screen later shows the notice
