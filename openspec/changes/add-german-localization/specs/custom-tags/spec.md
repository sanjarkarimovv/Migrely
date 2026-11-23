## ADDED Requirements

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
