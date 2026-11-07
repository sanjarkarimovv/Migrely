## MODIFIED Requirements

### Requirement: Delete all data
"Delete all data" SHALL be confirmed by typing DELETE. It SHALL delete:
- every health record and the device state;
- the preferences;
- the automatic copies and the reports.

It SHALL cancel planned reminders and remove notifications, and then start the app again at onboarding, whichever screen is
current. The subscription SHALL be untouched (BRD EC-STO-03, EC-STO-05, D96, D154).

#### Scenario: Not typed
- **WHEN** the user opens the confirmation and types "delete"
- **THEN** the button stays off and nothing is deleted

#### Scenario: Deleted
- **WHEN** the user types DELETE and confirms
- **THEN** the app starts again at onboarding with no records, no copies and no reminders

#### Scenario: The deletion ends while the screen fades out
- **WHEN** the deletion ends while the delete screen is leaving
- **THEN** the app starts again once

#### Scenario: The deletion ends after the screen is gone
- **WHEN** the deletion ends after a launcher shortcut or a fast Back has taken the delete screen away
- **THEN** the app starts again

#### Scenario: Recreated after the deletion ended
- **WHEN** the screens are composed again after the deletion has ended
- **THEN** the app starts again

#### Scenario: Back before deleting
- **WHEN** the user presses Back on the delete screen before confirming
- **THEN** the screen leaves and nothing is deleted
