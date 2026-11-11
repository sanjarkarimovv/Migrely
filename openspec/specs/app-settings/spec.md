# app-settings Specification

## Purpose
Defines Settings: its sections, the theme, the warning signs, deleting all data, and the privacy, about, licence and
version pages (BRD SET-F1, SAF-F1, EC-STO-03, EC-STO-04, D93–D97).

## Requirements

### Requirement: The settings list
Settings SHALL list, in sections:
- "Your diary": Reminders, Track periods, Your data;
- "Look": Theme;
- "Help": Warning signs, About Migrely;
- "Privacy and about": Privacy, Open-source licences, Version;
- then "Delete all data".

It SHALL offer no analytics switch, since Migrely sends nothing (BRD SET-F1, D93).

#### Scenario: Opening Settings
- **WHEN** the user taps the gear on home
- **THEN** the sections show in that order, each item one labelled node

### Requirement: Theme
Theme SHALL offer "Dark", the default, "Light" and "Match the phone". The light theme SHALL use no pure white. Attack
mode and the dark room SHALL stay dark in every theme, and while an attack is in progress every screen SHALL be dark,
because light hurts during an attack. The choice SHALL apply at once and at every start, with no frame in the other
theme (BRD SET-F1, EC-UX-01, D94).

#### Scenario: Light
- **WHEN** the user chooses "Light"
- **THEN** Settings turns light at once, and home is light at the next start

#### Scenario: An attack in progress
- **WHEN** the theme is Light and an attack is in progress
- **THEN** every screen is dark, attack mode and the screens opened from it included
- **AND** once the attack ends, the screens are light again

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

### Requirement: Delete all data
"Delete all data" SHALL be confirmed by typing DELETE. It SHALL delete:
- every health record and the device state;
- the preferences;
- the automatic copies and the reports.

It SHALL cancel planned reminders and remove notifications, and then start the app again at onboarding, whichever screen is
current. The subscription SHALL be untouched (BRD EC-STO-03, EC-STO-05, D96, D154).

#### Scenario: Not typed
- **WHEN** the user opens the confirmation and types "delete"
- **THEN** the button stays off and nothing is deleted

#### Scenario: Deleted
- **WHEN** the user types DELETE and confirms
- **THEN** the app starts again at onboarding with no records, no copies and no reminders

#### Scenario: The deletion ends while the screen fades out
- **WHEN** the deletion ends while the delete screen is leaving
- **THEN** the app starts again once

#### Scenario: The deletion ends after the screen is gone
- **WHEN** the deletion ends after a launcher shortcut or a fast Back has taken the delete screen away
- **THEN** the app starts again

#### Scenario: Recreated after the deletion ended
- **WHEN** the screens are composed again after the deletion has ended
- **THEN** the app starts again

#### Scenario: Back before deleting
- **WHEN** the user presses Back on the delete screen before confirming
- **THEN** the screen leaves and nothing is deleted

### Requirement: Privacy and about
Settings SHALL offer:
- **Privacy:** no internet, no account, the records stay on the phone and in Android's own backup, and files leave
  only when exported. The page SHALL end with a link to the full privacy policy at https://migrely.health/privacy/,
  with the address written out, which opens in the phone's browser;
- **About Migrely:** the full disclaimer;
- **Open-source licences:** the libraries and their licences;
- **Version:** the app's version.

(BRD SET-F1, EC-STO-04, D97, D119.)

#### Scenario: The version
- **WHEN** the user looks at Settings
- **THEN** the Version item shows the installed version

#### Scenario: The full privacy policy
- **WHEN** the user taps "Read the full privacy policy" on the Privacy page
- **THEN** the phone's browser opens https://migrely.health/privacy/

#### Scenario: No browser
- **WHEN** the user taps the link on a phone with no browser
- **THEN** nothing opens, the app keeps running, and the address stays on the page

### Requirement: Premium in Settings
Settings SHALL have a "Premium" section at the top. It SHALL show:
- the status: Free, Premium with its plan, Free trial, or Payment pending;
- the trial's end when known;
- "Restore purchases";
- for a subscription, "Manage subscription", which opens Google Play's subscriptions page.

A free user's status row SHALL open the paywall. Delete all data SHALL leave the purchase in Google Play, and Premium
SHALL be found again at the next check (BRD SUB-F2, D96, D112).

#### Scenario: A subscriber
- **WHEN** a yearly subscriber opens Settings
- **THEN** the section reads "Premium · yearly", with "Restore purchases" and "Manage subscription"

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
- **THEN** it stops 18 times, in this order: back, "Warning signs" (heading), the first heading, the nine signs, the
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
