# attack-mode Specification

## Purpose
Defines how the app behaves while an attack is in progress. The app becomes a "dark room" for someone in pain and
sensitive to light: darker, almost nothing to read, large controls, gentle motion, and nothing that asks for attention.

## Requirements

### Requirement: Dark room while an attack is in progress
While an attack is in progress the home screen SHALL be shown in attack mode: a background darker than the normal home
screen, a single line of guidance text, the start time with the elapsed time, and only these controls: pain intensity
(Mild, Moderate, Severe), moving the start earlier, "+ Medication taken", "Migraine ended", and a dim "Details" link
that opens the attack's details (BRD section 18.3 B, D24, D34).

#### Scenario: Attack mode content
- **WHEN** an attack started at 14:05 has been in progress for 42 minutes
- **THEN** the home screen shows "Rest now. We'll keep time." and "Started 14:05 · 42 min"
- **AND** shows the intensity choices, the option to move the start earlier, "+ Medication taken", "Migraine ended" and
  the "Details" link, and no other controls

#### Scenario: Elapsed time keeps up
- **WHEN** the attack mode screen stays open
- **THEN** the elapsed time is updated at least once every minute

#### Scenario: Describe a long attack
- **WHEN** an attack has been in progress for two days and the user taps "Details"
- **THEN** the details screen of that attack opens

#### Scenario: Medication taken
- **WHEN** the user taps "+ Medication taken"
- **THEN** the "Medication taken" screen opens for the attack in progress

### Requirement: Large controls within thumb reach
In attack mode, the intensity choices and "Migraine ended" SHALL each have a touch target of at least 64 dp and SHALL
be placed in the lower half of the screen (BRD EC-UX-02).

#### Scenario: One-handed use
- **WHEN** the user holds the phone in one hand during an attack
- **THEN** every intensity choice and "Migraine ended" is in the lower half of the screen with a target of at least 64 dp

### Requirement: Nothing that asks for attention
While an attack is in progress the app SHALL NOT show rewards, a paywall, rating requests, progress messages or
promotional content (BRD EC-UX-13, G4).

#### Scenario: Attack in progress
- **WHEN** an attack is in progress
- **THEN** none of a reward, paywall, rating request, progress message or promotion is shown

### Requirement: Gentle transitions
Entering and leaving attack mode SHALL take no longer than 1.5 seconds and SHALL NOT change brightness more than three
times per second. When the system setting that removes animations is on, the change SHALL happen at once with no
animation, and any rain shown SHALL be still (BRD EC-UX-07, EC-UX-08).

#### Scenario: Entering attack mode
- **WHEN** the user taps "Migraine started" and system animations are on
- **THEN** the screen settles into attack mode within 1.5 seconds without flashing

#### Scenario: Animations removed in system settings
- **WHEN** the system "Remove animations" setting is on and the user starts or ends an attack
- **THEN** the screen switches immediately with no animation and no moving rain

### Requirement: Opening the app during an attack
When the app is opened while an attack is in progress, it SHALL show attack mode immediately, without a transition from
the normal home screen (BRD EC-UX-12).

#### Scenario: Reopening during an attack
- **WHEN** the user opens the app while an attack is in progress
- **THEN** the first screen shown is attack mode, with no transition animation

### Requirement: Haptic confirmation
Starting and ending an attack SHALL be confirmed with a short vibration when the device's touch feedback is enabled, so
the user knows it worked without reading the screen.

#### Scenario: Confirm without looking
- **WHEN** touch feedback is enabled on the device and the user taps "Migraine started"
- **THEN** the device gives a short vibration once the attack is stored

### Requirement: Accessible attack mode
Every control in attack mode SHALL have a spoken label for screen readers, the selected intensity SHALL be announced as
selected, and all controls SHALL remain usable with system font size at 200% (BRD EC-UX-03, EC-UX-04).

#### Scenario: Screen reader
- **WHEN** TalkBack is on and the user moves focus to the "Moderate" choice after selecting it
- **THEN** TalkBack announces it as "Moderate, selected"

#### Scenario: Largest font size
- **WHEN** the system font size is 200%
- **THEN** every attack mode control is fully visible and can be tapped

### Requirement: Readable dim text
Every text in attack mode SHALL reach a contrast of at least 4.5:1 against its background, while staying dim and never
pure white (BRD §10, EC-UX-01).

#### Scenario: Start time line
- **WHEN** attack mode shows "Started 14:05 · 42 min"
- **THEN** that line has a contrast of at least 4.5:1 against the card

### Requirement: Elapsed time follows a moved start
When the start of the attack in progress changes, the elapsed time SHALL be computed again at once, not at the next
periodic update (BRD ATK-F1).

#### Scenario: Started an hour earlier
- **WHEN** an attack started just now and the user taps "1 h" under "Started earlier?"
- **THEN** attack mode shows "1 h 0 min" at once
