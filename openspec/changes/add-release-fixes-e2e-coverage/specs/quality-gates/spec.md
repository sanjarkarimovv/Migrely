## ADDED Requirements

### Requirement: Every screen is in the accessibility audit
The accessibility audit's tour SHALL hold at least one state for every screen the app shows, and a change that adds a
screen SHALL add its state. A screen behind Premium SHALL be reached through the simulated store (D201). A failure
SHALL be filed as an `accessibility` issue, never worked around in the audit (BRD §10, EC-UX-03, EC-UX-04, D42, D203).

#### Scenario: The screens of 1.0.0
- **WHEN** the audit runs on 1.0.0
- **THEN** it captures home, attack mode, History, attack details, the past-migraine screen, onboarding, Settings and
  its pages, My medications, Reminders, Your data, the paywall, the doctor report and Insights

#### Scenario: The premium screens
- **WHEN** the audit runs
- **THEN** the paywall, the doctor report's period screen and Insights are each captured at 100% font, at 200% font and
  in landscape, and checked by the same rules as the other states

#### Scenario: A screen without a state
- **WHEN** a change adds a screen and the tour has no state for it
- **THEN** QA's review of the change finds the missing state, and the change does not pass verification until the state
  is added
