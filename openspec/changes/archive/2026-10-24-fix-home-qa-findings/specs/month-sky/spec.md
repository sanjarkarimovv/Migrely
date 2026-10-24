## ADDED Requirements

### Requirement: Days reachable with a keyboard
Each selectable calendar day SHALL be focusable for keyboards and accessibility services such as Switch Access, and
keep its spoken description and state (BRD EC-UX-19, EC-UX-15).

#### Scenario: Tab through the days
- **WHEN** History is open and the user presses Tab on a hardware keyboard
- **THEN** focus moves through the selectable days, and Enter chooses the focused day

#### Scenario: Accessibility services
- **WHEN** an accessibility service inspects a past day
- **THEN** the day is reported as focusable and clickable, with its description
