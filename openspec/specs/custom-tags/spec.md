# custom-tags Specification

## Purpose
Defines how a person adds their own symptoms and triggers, the limits on their names and number, and how removing
one from the list keeps it on past attacks, so that the record fits each person's own migraine (BRD ATK-F5, EC-ATK-12).

## Requirements

### Requirement: Add your own symptom or trigger
On an attack's details screen the user SHALL be able to add their own symptom or trigger by name. The name SHALL be
trimmed and SHALL be 1 to 40 characters. An edit that does not fit SHALL insert only the whole characters that fit,
never removing text elsewhere in the name or splitting a character such as an emoji. When nothing fits, the name and the
cursor SHALL stay as they were. The new tag SHALL be added to the user's list for that kind and selected on the attack
(BRD ATK-F5, EC-ATK-12).

#### Scenario: Add a symptom
- **WHEN** the user adds the symptom "Jaw pain"
- **THEN** "Jaw pain" appears in the symptom choices of every attack and is selected on this one

#### Scenario: Name too long
- **WHEN** the user enters a 41-character name
- **THEN** input stops at 40 characters

#### Scenario: Empty name
- **WHEN** the user enters only spaces
- **THEN** nothing can be added

#### Scenario: Typing in the middle of a full name
- **WHEN** the name has 40 characters and the user types a letter in the middle
- **THEN** the name is unchanged, its last character included, and the cursor stays where it was

#### Scenario: An emoji that does not fit
- **WHEN** the name has 39 characters and the user types "👍", which takes two
- **THEN** the name is unchanged, and no half of the emoji is stored

#### Scenario: Pasting across the cap
- **WHEN** the name has 36 characters and the user pastes "abc👍👍", where each "👍" takes two
- **THEN** "abc" is inserted, the name has 39 characters, and the cursor is after "abc"

#### Scenario: The screen is recreated
- **WHEN** the dialog holds a typed name with the cursor in the middle and the screen is recreated
- **THEN** the name and the cursor are as they were

### Requirement: No duplicate names
A name that matches an existing tag of the same kind, ignoring case and surrounding spaces, SHALL NOT create a new tag.
Instead the existing tag SHALL be selected on the attack, and an archived match SHALL return to the list (BRD
EC-ATK-12).

#### Scenario: Same name, different case
- **WHEN** "Jaw pain" exists and the user adds "jaw PAIN "
- **THEN** no new tag is created and "Jaw pain" is selected on the attack

#### Scenario: Archived match
- **WHEN** "Jaw pain" was removed from the list and the user adds "Jaw pain" again
- **THEN** the original "Jaw pain" returns to the list and is selected

### Requirement: At most 100 per kind
The user SHALL have at most 100 custom symptoms and 100 custom triggers in their list. Adding one more SHALL be refused
with a message (BRD EC-ATK-12).

#### Scenario: Limit reached
- **WHEN** the user has 100 custom triggers and adds another
- **THEN** a message says the list is full and nothing is added

### Requirement: Remove from the list without losing history
The user SHALL be able to remove a custom tag from their list. It SHALL then no longer be offered on attacks that do
not use it, but SHALL stay stored and shown on the attacks that do (BRD EC-ATK-11).

#### Scenario: Remove a used tag
- **WHEN** "Jaw pain" is selected on last week's attack and the user removes it from the list
- **THEN** new attacks no longer offer "Jaw pain"
- **AND** last week's attack still shows "Jaw pain" as selected

### Requirement: The custom tag dialog narrows only to type
In a short window the custom tag dialog SHALL be wide while no one types. A completed tap on its name field, or the
field gaining focus, SHALL narrow it to the platform width, which Android pans. A cancelled drag, a long
press on the focused field, or a tap or focus with a hardware keyboard attached SHALL NOT. With no on-screen keyboard up
3000 ms after the latest intent it SHALL widen. An on-screen keyboard rising SHALL narrow it, closing widen it (BRD
EC-L10N-30, D192, D195 (18)).

#### Scenario: Typing with the on-screen keyboard
- **WHEN** the window is 640 x 360 dp in landscape and the person taps the name field and the keyboard rises
- **THEN** the dialog has the platform width before the keyboard is up, and the line being typed shows above the
  keyboard

#### Scenario: The keyboard closes
- **WHEN** the keyboard is up and the person presses Back, so the keyboard closes and the field keeps focus
- **THEN** the dialog is wide again, its buttons share one row and the field's label is whole

#### Scenario: A second tap after Back
- **WHEN** after Back closed the keyboard the person taps the still-focused field again and types
- **THEN** the dialog narrows when the finger lifts, before the keyboard rises, and the typed line shows above the
  keyboard

#### Scenario: A drag on the field to scroll
- **WHEN** the person puts a finger on the field and drags to scroll the dialog, so the press ends cancelled
- **THEN** the dialog's width does not change

#### Scenario: A long press to paste
- **WHEN** the field already has focus and the keyboard is down, and the person long-presses it to paste
- **THEN** the dialog's width does not change

#### Scenario: A hardware keyboard is attached
- **WHEN** a hardware keyboard is attached, no on-screen keyboard rises, and the person taps the field or the field
  takes focus
- **THEN** the dialog stays wide, and stays wide while the person types on the hardware keyboard

#### Scenario: An on-screen keyboard rises beside a hardware keyboard
- **WHEN** a hardware keyboard is attached and an on-screen keyboard rises anyway, as with "Use on-screen keyboard"
  turned on while the physical keyboard is active
- **THEN** the dialog narrows, so the typed line shows above that keyboard, and widens again when it closes

#### Scenario: No on-screen keyboard comes
- **WHEN** no hardware keyboard is attached, the field takes focus and no on-screen keyboard is up 3000 ms after the
  latest tap or focus
- **THEN** the dialog is wide again

#### Scenario: A tall window
- **WHEN** the window is 480 dp tall or taller, as a phone in portrait
- **THEN** the dialog keeps the platform width at rest and while typing, as before
