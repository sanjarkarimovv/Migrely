## MODIFIED Requirements

### Requirement: Warning signs
Help SHALL show the warning signs in two headed parts. The first SHALL say to call the local emergency number if a headache comes
with any of nine NHS "Call 999" signs, with no condition to wait, and then not to drive to hospital. The rash sign SHALL say it may
be harder to see on brown or black skin. The page SHALL dial nothing, SHALL not list a first headache after 50 or numbness alone,
nor name "999", "GP" or "NHS 111" (BRD SAF-F1, D95, D171, D173, EC-SAF-01, 04, 05, 13, 14).

#### Scenario: Reading them
- **WHEN** the user opens Settings, then "Warning signs"
- **THEN** the first heading reads "Call your local emergency number if a headache comes with any of these:"
- **AND** under it, in this order: "Extremely bad pain that came on suddenly", "Weakness or loss of movement on one
  side of your body or face", "Problems speaking or remembering things", "Losing your vision, or blurred or double
  vision", "Feeling drowsy or confused", "A seizure or fit", "A very high temperature and signs of meningitis, such as
  a stiff neck", "A rash that doesn't fade when you press a glass against it (this may be harder to see on brown or
  black skin)", "A head injury in the last 3 months"
- **AND** after them, as plain text and not a sign: "Don't drive yourself to hospital. Call your local emergency
  number."

#### Scenario: A rash on brown or black skin
- **WHEN** a person with brown or black skin reads the rash sign
- **THEN** the same line says the rash may be harder to see on brown or black skin
- **AND** the sign stays one item, with no extra lines on where to look

#### Scenario: The rash sign with TalkBack and at 200% font
- **WHEN** TalkBack reads the page, or the font is 200% on a 320 dp screen or in landscape
- **THEN** the rash sign is one stop, read in full with its note, the page keeps its 18 stops, and the sign wraps with no line cut
  off

#### Scenario: Nothing tells the person to wait
- **WHEN** the user reads the emergency signs
- **THEN** no sign carries "doesn't pass", "recent" or any other condition to wait for
- **AND** "A first headache after the age of 50" is not on the page

#### Scenario: Numbness alone waits for the clinician
- **WHEN** the user reads either part of the page
- **THEN** no sign names numbness on its own, because the NHS migraine page counts it as an aura symptom
- **AND** this stays until a named clinician answers the question (T31) and a new BRD decision changes it

#### Scenario: Outside the UK
- **WHEN** the user reads the page in any country
- **THEN** it says "your local emergency number" and shows no "999", "GP", "NHS 111" and no dial button
