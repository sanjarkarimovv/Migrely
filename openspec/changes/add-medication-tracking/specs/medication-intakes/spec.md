## Purpose

Defines logging a medication taken: one screen used from attack mode, the details screen and home, which medication
and time an intake records, how it links to an attack, and how intakes follow their attack when it is deleted or
merged, so that every pill is on record with the least effort, even during an attack (BRD MED-F3, D34, EC-MED-08 to
EC-MED-10).

## ADDED Requirements

### Requirement: One tap logs a medication
The "Medication taken" screen SHALL list the active medications, acute first. Tapping one SHALL log it at the shown time
with its default dose, and SHALL then show what was logged with "Undo" and "Done". The time SHALL default to now, or to
the start of an ended attack when opened from its details, and SHALL be changeable but never set more than 5 minutes in
the future (BRD MED-F3, D34, EC-ATK-03).

#### Scenario: Log during an attack
- **WHEN** an attack is in progress, the user taps "+ Medication taken" and then "Ibuprofen · 400 mg"
- **THEN** an intake of Ibuprofen 400 mg at the current minute is linked to the attack, and "Ibuprofen 400 mg logged at
  14:20" is shown with "Undo" and "Done"

#### Scenario: Undo a logged intake
- **WHEN** the user taps "Undo" after logging
- **THEN** the intake is removed and the list of medications is shown again

#### Scenario: Time in the future
- **WHEN** the user picks a time 10 minutes from now
- **THEN** the picker does not allow it

### Requirement: No medication yet
When the user has no active medication, the screen SHALL offer to add one, with its name and category, and SHALL then
log it at once (BRD EC-MED-08).

#### Scenario: First medication during an attack
- **WHEN** no medication exists and the user taps "+ Medication taken" during an attack
- **THEN** the screen asks for the name and category, and after "Add and log" the intake is logged

### Requirement: Intakes link to their attack
An intake logged from attack mode or from an attack's details SHALL be linked to that attack, and its time SHALL be
shown, picked and stored in the zone where that attack started. An intake logged from home SHALL be linked to the
attack whose time overlaps the intake's minute, if any, in the device's zone. Undo on the screen SHALL keep it for the
attack it was opened for, or for none. Changing an attack's times later SHALL NOT change its intakes (BRD EC-MED-09,
D34, EC-TIM-02).

#### Scenario: Logged from home during a past attack's time
- **WHEN** an attack ran 09:00–11:00 and the user logs Ibuprofen from home at 10:15
- **THEN** that attack's details list Ibuprofen at 10:15

#### Scenario: Logged from home in an attack's first minute
- **WHEN** an attack started at 12:20:40 and the user logs Ibuprofen from home at 12:20
- **THEN** that attack's details list Ibuprofen

#### Scenario: Logged from home outside any attack
- **WHEN** no attack contains 16:00 and the user logs Ibuprofen at 16:00
- **THEN** the intake is stored on its own

#### Scenario: Undo, then a new time from home
- **WHEN** from home the user logs Ibuprofen at 10:15 inside an attack, taps "Undo", picks 16:00 and logs it again
- **THEN** the 16:00 intake is stored on its own

#### Scenario: Later, for an attack recorded in another zone
- **WHEN** an attack started at 14:00 in Tokyo and the user, now in London, adds a medication from its details
- **THEN** the time shows "14:00 (Tokyo)", and picking 14:30 stores 14:30 Tokyo time with the Tokyo zone

### Requirement: Intakes follow their attack
When an attack is deleted, its intakes SHALL be hidden with it, Undo SHALL bring them back, and they SHALL be removed
for good with the attack. When attacks are merged, their intakes SHALL move to the remaining attack. When "Undo" removes
an attack that was started by mistake, a medication logged meanwhile SHALL stay, on its own (BRD EC-MED-10, D27, D23).

#### Scenario: Delete and undo
- **WHEN** an attack with two intakes is deleted and the user taps "Undo"
- **THEN** the attack and both intakes are back

#### Scenario: Merge
- **WHEN** an attack with Ibuprofen is merged into an attack with Sumatriptan
- **THEN** the remaining attack lists both

#### Scenario: Undo a start after logging a medication
- **WHEN** the user starts an attack, logs Ibuprofen, and taps "Undo" on "Migraine started"
- **THEN** the attack is gone and Ibuprofen stays as an intake on its own

### Requirement: Usable in the dark room
Opened from attack mode, the screen SHALL keep attack mode's dim colours, use controls at least 64 dp tall and show no
more text than needed (BRD EC-UX-01, EC-UX-02, 18.3 B).

#### Scenario: Large choices
- **WHEN** the screen is opened from attack mode
- **THEN** each medication is a button at least 64 dp tall on the dark attack-mode background

### Requirement: Accessible logging
The screen SHALL work with TalkBack, keyboards and switch access, at 200% font and in landscape, and SHALL keep the
picked time across rotation and process death (BRD EC-UX-03, EC-UX-04, EC-ATK-10).

#### Scenario: Rotation keeps the time
- **WHEN** the user changes the time to 13:40 and rotates the phone
- **THEN** the time is still 13:40
