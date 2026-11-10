## MODIFIED Requirements

### Requirement: Warning signs
Help SHALL show the warning signs in two headed parts. The first SHALL say to call the local emergency number if a
headache comes with any of the nine signs from the NHS "Call 999" lists, with no condition that asks the person to
wait, and then not to drive to hospital. It SHALL dial nothing, and SHALL not list a first headache after 50 or numbness
alone, nor name "999", "GP" or "NHS 111" (BRD SAF-F1, D95, D171, EC-SAF-01, EC-SAF-04, EC-SAF-05).

#### Scenario: Reading them
- **WHEN** the user opens Settings, then "Warning signs"
- **THEN** the first heading reads "Call your local emergency number if a headache comes with any of these:"
- **AND** under it, in this order: "Extremely bad pain that came on suddenly", "Weakness or loss of movement on one
  side of your body or face", "Problems speaking or remembering things", "Losing your vision, or blurred or double
  vision", "Feeling drowsy or confused", "A seizure or fit", "A very high temperature and signs of meningitis, such as
  a stiff neck", "A rash that doesn't fade when you press a glass against it", "A head injury in the last 3 months"
- **AND** after them, as plain text and not a sign: "Don't drive yourself to hospital. Call your local emergency
  number."

#### Scenario: Nothing tells the person to wait
- **WHEN** the user reads the emergency signs
- **THEN** no sign carries "doesn't pass", "recent" or any other condition to wait for
- **AND** "A first headache after the age of 50" and numbness on its own are not on the page

#### Scenario: Outside the UK
- **WHEN** the user reads the page in any country
- **THEN** it says "your local emergency number" and shows no "999", "GP", "NHS 111" and no dial button

## ADDED Requirements

### Requirement: Urgent medical advice
Under a second heading, Warning signs SHALL say to get urgent medical advice if the person has a migraine attack and it
has lasted longer than 72 hours, aura symptoms last longer than 1 hour at a time, or they are pregnant or have just had
a baby, as on the NHS urgent list. These signs SHALL not be sent to the emergency number (BRD SAF-F1, D171, EC-SAF-02,
EC-SAF-03).

#### Scenario: Reading the second part
- **WHEN** the user scrolls past the emergency signs
- **THEN** the second heading reads "Get urgent medical advice if you have a migraine attack and:"
- **AND** under it, in this order: "It has lasted longer than 72 hours", "Aura symptoms last longer than 1 hour at a
  time", "You're pregnant or have just had a baby"

#### Scenario: Pregnancy without an attack
- **WHEN** the user reads the line about pregnancy
- **THEN** it sits under the heading that ends "if you have a migraine attack and:", never on its own

### Requirement: Other symptoms
Warning signs SHALL end with one plain line saying that any other worrying symptom needs urgent medical advice, so that
the lists never read as complete (BRD D171, EC-SAF-05).

#### Scenario: The last line
- **WHEN** the user scrolls to the end of the page
- **THEN** the last line reads "If you have any other symptoms you're worried about, get urgent medical advice."

### Requirement: The About page points to the emergency signs only
About Migrely's urgent line SHALL send to the local emergency number only the signs in the emergency part of Warning
signs, so that a sign from the second part never reads as a reason to call it (BRD D171, EC-SAF-02).

#### Scenario: Reading About
- **WHEN** the user opens Settings, then "About Migrely"
- **THEN** the last paragraph reads "If a headache comes with any of the emergency signs on the Warning signs page,
  call your local emergency number."

### Requirement: Warning signs are readable for everyone
Both part headings SHALL be accessibility headings. Each sign and line SHALL be one node read in screen order, with no
bullet glyph spoken. At 200% font, in landscape and on small screens, the page SHALL scroll and show every line whole.
It SHALL work offline, in the dark theme during an attack, and the same for Free and Premium (BRD EC-SAF-06…09,
EC-UX-03).

#### Scenario: TalkBack
- **WHEN** TalkBack reads the page from the top
- **THEN** it stops 17 times, in this order: back, "Warning signs" (heading), the first heading, the nine signs, the
  "Don't drive" line, the second heading, the three signs of the second part, the last line
- **AND** each heading is announced as a heading, no bullet glyph is read, and moving by headings goes from the first
  heading to the second in one step

#### Scenario: 200% font and landscape
- **WHEN** the font scale is 200%, the screen is 320 dp wide, or the phone is in landscape
- **THEN** the first heading shows whole on the first screen in portrait, the app bar title is not cut, and the user
  can scroll to the last line with every line whole

#### Scenario: During an attack, offline, Free
- **WHEN** a Free user with no network opens the page while an attack is in progress
- **THEN** the page shows in the dark theme with every line and no paywall

#### Scenario: After the update
- **WHEN** a user of 1.1.1 updates to 1.2.1 and opens the page
- **THEN** only the new text shows, and the diary and settings are unchanged
