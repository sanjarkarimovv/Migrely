## MODIFIED Requirements

### Requirement: Warning signs
Help SHALL show the warning signs in two headed parts. The first SHALL say to call the local emergency number if a
headache comes with any of the eight signs on the NHS "Call 999" list, in that order, with no condition that asks the
person to wait, and without dialling a number. It SHALL not list a first headache after 50 or numbness alone, and
SHALL not name a country, "999", "GP" or "NHS 111" (BRD SAF-F1, D95, D171, EC-SAF-01, EC-SAF-04, EC-SAF-05).

#### Scenario: Reading them
- **WHEN** the user opens Settings, then "Warning signs"
- **THEN** the first heading reads "Call your local emergency number if a headache comes with any of these:"
- **AND** under it, in this order: "It came on suddenly and is extremely painful", "Problems speaking or remembering
  things", "Losing your vision, or blurred or double vision", "Feeling drowsy or confused", "A seizure or fit", "A very
  high temperature and signs of meningitis, such as a stiff neck or a rash that doesn't fade", "Weakness, or you can't
  move, on one side of your body or face", "A recent head injury"

#### Scenario: Nothing tells the person to wait
- **WHEN** the user reads the emergency signs
- **THEN** no sign carries "doesn't pass" or any other time condition
- **AND** "A first headache after the age of 50" and numbness on its own are not on the page

#### Scenario: Outside the UK
- **WHEN** the user reads the page in any country
- **THEN** it says "your local emergency number" and shows no "999", "GP", "NHS 111" and no dial button

## ADDED Requirements

### Requirement: Medical advice today
Under a second heading, Warning signs SHALL say to get medical advice today if the person has a migraine attack and it
has lasted longer than 72 hours, aura lasts longer than 1 hour at a time, or they are pregnant or have just had a baby,
as on the NHS urgent list. These signs SHALL not be sent to the emergency number (BRD SAF-F1, D171, EC-SAF-02,
EC-SAF-03).

#### Scenario: Reading the second part
- **WHEN** the user scrolls past the emergency signs
- **THEN** the second heading reads "Get medical advice today if you have a migraine attack and:"
- **AND** under it, in this order: "It has lasted longer than 72 hours", "Aura lasts longer than 1 hour at a time",
  "You're pregnant or have just had a baby"

#### Scenario: Pregnancy without an attack
- **WHEN** the user reads the line about pregnancy
- **THEN** it sits under the heading that starts "if you have a migraine attack and", never on its own

### Requirement: The About page points to the emergency signs only
About Migrely's urgent line SHALL send to the local emergency number only the signs in the emergency part of Warning
signs, so that a sign from the second part never reads as a reason to call it (BRD D171, EC-SAF-02).

#### Scenario: Reading About
- **WHEN** the user opens Settings, then "About Migrely"
- **THEN** the last paragraph reads "If a headache comes with any of the emergency signs in Warning signs, call your
  local emergency number."

### Requirement: Warning signs are readable for everyone
Each part's heading SHALL be an accessibility heading, and each sign SHALL be one node read in screen order with no
bullet glyph spoken. At 200% font, in landscape and on small screens, the page SHALL scroll and show every sign whole,
the last included. It SHALL work offline, during an attack in the dark theme, and for Free and Premium alike (BRD
EC-SAF-06, EC-SAF-07, EC-SAF-08, EC-UX-03).

#### Scenario: TalkBack
- **WHEN** TalkBack reads the page from the top
- **THEN** it reads the page title, the first heading, the eight emergency signs, the second heading, and the three
  signs of the second part, in that order, each sign once and no bullet glyph
- **AND** moving by headings goes from the first heading to the second in one step

#### Scenario: 200% font and landscape
- **WHEN** the font scale is 200% and the phone is in landscape
- **THEN** the user can scroll to "You're pregnant or have just had a baby" and every sign shows whole, none cut

#### Scenario: During an attack, offline, Free
- **WHEN** a Free user with no network opens the page while an attack is in progress
- **THEN** the page shows in the dark theme with every sign and no paywall

#### Scenario: After the update
- **WHEN** a user of 1.1.1 updates to 1.2.1 and opens the page
- **THEN** only the new text shows, and the diary and settings are unchanged
