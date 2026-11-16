## ADDED Requirements

### Requirement: Quiet after a safety card
The app SHALL not ask for a rating within 3 days after the first-10 safety card, the "Still going?" card or the
medication overuse card appeared. The moment each appears SHALL be kept with the review marks: moved by Android's backup,
removed by "Delete all data", left out of the JSON export. A moment dated after now SHALL count as recent until the clock
passes it (BRD D188, EC-REV-14).

#### Scenario: After the first 10
- **WHEN** the first-10 safety card appeared on an attack, the attack ended, and home returns to today's mode within
  3 days with every D130 condition met
- **THEN** nothing is asked (EC-REV-14)

#### Scenario: After "Still going?"
- **WHEN** "Still going?" appeared on a long attack, and the attack is ended within the next 3 days
- **THEN** nothing is asked until 3 days after that card appeared (EC-REV-14)

#### Scenario: The overuse card on home
- **WHEN** the medication overuse card appears on home
- **THEN** no review flow starts on that visit or in the next 3 days (EC-REV-14)

#### Scenario: Three days later
- **WHEN** the last safety card appeared at least 3 days ago and every D130 condition holds
- **THEN** the next home load in today's mode may ask, as before

#### Scenario: Clock moved back
- **WHEN** the clock is moved back so that the last safety card's moment is in the future
- **THEN** nothing is asked until the clock passes that moment and 3 more days have gone by

#### Scenario: Delete all data and restore
- **WHEN** the user deletes all data, or restores Android's backup on a new phone
- **THEN** the mark is gone after deleting, and travels with the other review marks after a restore; the JSON export has
  no such field
