## ADDED Requirements

### Requirement: One medication card under today's sky
In the normal mode, home SHALL show at most one medication card, directly under today's card: an overuse doctor card
first, then a soft overuse card, then "Did it help?". Attack mode SHALL show none. The card SHALL update whenever home
loads (BRD D46, G4, EC-UX-13).

#### Scenario: Warning and question together
- **WHEN** a triptan doctor warning is due and an Ibuprofen intake waits for "Did it help?"
- **THEN** home shows the doctor card
- **AND** after "Got it", home shows the question

#### Scenario: Nothing due
- **WHEN** no warning is due and no intake waits for an answer
- **THEN** home shows no medication card

#### Scenario: Attack mode
- **WHEN** an attack is in progress
- **THEN** no medication card is shown
