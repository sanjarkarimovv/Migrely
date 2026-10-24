## ADDED Requirements

### Requirement: Today's attacks reachable with a keyboard
Each row under "Logged today" SHALL be focusable for keyboards and accessibility services such as Switch Access, keep
its spoken description, and open the attack's details when activated (BRD EC-UX-19, EC-UX-04).

#### Scenario: Tab to a row
- **WHEN** an attack is listed under "Logged today" and the user presses Tab on a hardware keyboard
- **THEN** focus reaches the row, and Enter opens its details

#### Scenario: Accessibility services
- **WHEN** an accessibility service inspects the row
- **THEN** the row is reported as focusable and clickable, with its description
