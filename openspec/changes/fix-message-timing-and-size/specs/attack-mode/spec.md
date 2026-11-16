## ADDED Requirements

### Requirement: A message's control meets the attack-mode size
In attack mode, the "Undo" or Dismiss of a message SHALL have a visible target of at least 64 by 64 dp, the same bar as the
intensity choices and "Migraine ended". The message's text size, colour and place SHALL NOT change (BRD EC-UX-02,
EC-UX-59, section 18.3 B, D182).

#### Scenario: Undo the start in the dark
- **WHEN** the user starts an attack and "Migraine started" with "Undo" appears, in portrait or landscape, at 100% or 200%
  font
- **THEN** "Undo" is at least 64 dp tall and 64 dp wide
- **AND** the intensity choices and "Migraine ended" stay fully visible and usable

#### Scenario: Dismiss in the dark
- **WHEN** an information message with Dismiss shows in attack mode
- **THEN** Dismiss is at least 64 by 64 dp and its icon stays the same 20 dp
