# medication-effect Specification

## Purpose
Defines how the app asks whether a medication helped, inside the app, and where the answer can be given or changed
later, so the doctor report and insights can tell which medications work (BRD MED-F4, ATK-F2, D39).

## Requirements

### Requirement: Ask whether a medication helped
In the normal mode, home SHALL ask "How much did <name> help?" about an acute intake taken 2 to 24 hours ago that has
no answer and was not skipped. The card SHALL show the medication, its dose and when it was taken, and offer "Not at
all", "A little", "A lot" and "Completely", and a close button that skips the question for that intake. Preventive
medications SHALL NOT be asked about (BRD MED-F4, EC-MED-05, D39).

#### Scenario: Two hours later
- **WHEN** Ibuprofen 400 mg was logged at 14:00 and the user opens home at 16:05
- **THEN** a card asks "How much did Ibuprofen help?" with "Taken at 2:00 PM · 400 mg" and the four answers

#### Scenario: Too early
- **WHEN** Ibuprofen was logged at 14:00 and the user opens home at 15:30
- **THEN** no question is shown

#### Scenario: Too late
- **WHEN** Ibuprofen was logged 25 hours ago and never answered
- **THEN** no question is shown for it

#### Scenario: Preventive medication
- **WHEN** only Topiramate, a preventive medication, was logged 3 hours ago
- **THEN** no question is shown

#### Scenario: Answer
- **WHEN** the user taps "A lot"
- **THEN** the answer is stored with the intake and the card goes away

#### Scenario: Skip
- **WHEN** the user taps the close button
- **THEN** the card goes away and that intake is never asked about again

#### Scenario: Attack in progress
- **WHEN** an attack is in progress
- **THEN** attack mode shows no question

### Requirement: One question at a time
When several intakes wait for an answer, home SHALL ask about one at a time, the oldest first. After an answer or a
skip, the next one SHALL be asked (BRD EC-MED-11).

#### Scenario: Two intakes waiting
- **WHEN** Sumatriptan was logged at 09:00 and Ibuprofen at 10:00, and the user opens home at 13:00
- **THEN** home asks about Sumatriptan
- **AND** after the answer, home asks about Ibuprofen

### Requirement: Answer in the attack's details
In an attack's details, each medication row SHALL show its answer, if any. Tapping the row SHALL open a choice of the
four answers, which sets or changes it at any time and closes the question. Removing an intake and tapping "Undo" SHALL
keep its answer (BRD ATK-F2, EC-MED-12, D39).

#### Scenario: Answer later
- **WHEN** an attack's Sumatriptan row has no answer, two days later, and the user taps it and chooses "Completely"
- **THEN** the row shows "Helped completely"
- **AND** home never asks about that intake

#### Scenario: Change an answer
- **WHEN** a row shows "Helped a little" and the user chooses "Not at all"
- **THEN** the row shows "Did not help"

#### Scenario: Undo keeps the answer
- **WHEN** the user removes a row that shows "Helped a lot" and taps "Undo"
- **THEN** the row comes back showing "Helped a lot"

### Requirement: Accessible question
The question card SHALL be readable by screen readers as a heading with the time and dose, and each answer and the
close button SHALL be a separate control of at least 48 dp with its own label. At 200% font the answers SHALL wrap
instead of being cut off (BRD EC-UX-03, EC-UX-04).

#### Scenario: Screen reader
- **WHEN** a screen reader moves through the card
- **THEN** it reads "How much did Ibuprofen help?", "Taken at 2:00 PM, 400 mg", each answer, and "Don't ask about this
  one"

#### Scenario: Largest font
- **WHEN** the font size is 200%
- **THEN** the four answers wrap onto more lines and every label is complete
