## MODIFIED Requirements

### Requirement: Messages appear at the top and stay dim
Short messages SHALL appear at the top of the screen, inside the system bars and display cutouts, and never cover the
large controls. In landscape attack mode, where the controls fill the right column, messages SHALL stay over the left
column. A message SHALL never be the brightest element on screen: text no brighter than `#C8CCD2`, on a dark surface
with a thin outline (BRD EC-UX-01, EC-UX-16, EC-UX-20, D37).

#### Scenario: Message during attack mode
- **WHEN** an attack is started and the "Migraine started" message appears
- **THEN** it is shown at the top of the screen, below the status bar
- **AND** the intensity choices and "Migraine ended" stay fully visible and usable

#### Scenario: Message during landscape attack mode
- **WHEN** the phone is in landscape and the user taps "Migraine started"
- **THEN** the "Migraine started" message with "Undo" appears over the left column, under the status bar
- **AND** "Mild", "Moderate", "Severe", "+ Medication taken" and "Migraine ended" stay fully visible and usable for
  as long as the message shows, including a longer "Time to take action"

#### Scenario: Landscape with a display cutout
- **WHEN** a message appears while the phone is in landscape
- **THEN** the message stays clear of the status bar and the display cutout and is at most 560 dp wide

#### Scenario: Landscape normal mode
- **WHEN** a message appears in landscape with no attack in progress
- **THEN** it is centred at the top of the screen, as in portrait
