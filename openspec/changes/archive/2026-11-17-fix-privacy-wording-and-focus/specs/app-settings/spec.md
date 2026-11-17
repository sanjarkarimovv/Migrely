## MODIFIED Requirements

### Requirement: The settings list
Settings SHALL list, in sections:
- "Your diary": Reminders, Track periods, Your data;
- "Look": Theme;
- "Help": Warning signs, About Migrely;
- "Privacy and about": Privacy, Open-source licences, Version;
- then "Delete all data".

It SHALL offer no analytics switch, since Migrely has no internet permission and no analytics (BRD SET-F1, D93, D181).

#### Scenario: Opening Settings
- **WHEN** the user taps the gear on home
- **THEN** the sections show in that order, each item one labelled node

### Requirement: Privacy and about
Settings SHALL offer:
- **Privacy:** where entries can go, as "The Privacy page says where entries can go" sets out, ending with a link to
  https://migrely.health/privacy/, address written out, which opens in the phone's browser;
- **About Migrely:** the full disclaimer;
- **Open-source licences:** the libraries and their licences;
- **Version:** the app's version.

(BRD SET-F1, EC-STO-04, D97, D119, D181.)

#### Scenario: The version
- **WHEN** the user looks at Settings
- **THEN** the Version item shows the installed version

#### Scenario: The full privacy policy
- **WHEN** the user taps "Read the full privacy policy" on the Privacy page
- **THEN** the phone's browser opens https://migrely.health/privacy/

#### Scenario: No browser
- **WHEN** the user taps the link on a phone with no browser
- **THEN** nothing opens, the app keeps running, and the address stays on the page

## ADDED Requirements

### Requirement: The Privacy page says where entries can go
The Privacy page SHALL show 7 paragraphs in this order, each one TalkBack stop and none a heading: internet permission,
Google Play purchase, phone and backup, files, delete, Android backup, policy link. Its text SHALL be legal's final
wording (D181 (1)), the same for Free and Premium, every Android version and offline. No app string SHALL say "internet
access", "sends nothing", "never leaves", "nothing leaves" or "100% offline" (BRD EC-STO-11, EC-STO-12, D179 (2), D181).

#### Scenario: The first two paragraphs
- **WHEN** the user opens Settings, then "What Migrely keeps, and where"
- **THEN** the first paragraph reads "Migrely has no internet permission, so the app itself cannot send anything over
  the internet. There's no account to make and nothing to sign in to."
- **AND** the second reads "Premium is bought through Google Play, which goes online for the purchase."

#### Scenario: Phone, backup and transfer
- **WHEN** the user reads the third paragraph
- **THEN** it reads "Your entries stay on this phone. If Android's own backup is on, it may also copy them to your Google
  account. On Android 9 and later, it does that only when it can encrypt the copy end to end, which needs a screen lock.
  On Android 8, Migrely cannot require that, so the copy may not be end-to-end encrypted. Android can also move your
  entries to a new phone when you set it up."

#### Scenario: Files
- **WHEN** the user reads the fourth paragraph
- **THEN** it reads "Apart from Android's own backup and moving to a new phone, your entries leave the phone only in a
  file you export yourself: a backup, a spreadsheet or a report for your doctor. That file goes wherever you send it."

#### Scenario: An Android 8 phone, backup off, or no screen lock
- **WHEN** the page is opened on Android 8, on a phone with Android's backup turned off, or on Android 9 or later
  without a screen lock
- **THEN** the same text shows, because it names each case itself, and nothing else on the page changes

#### Scenario: A Free user, offline
- **WHEN** a Free user with no connection opens the page
- **THEN** the same 7 paragraphs show, including the Google Play sentence, and nothing asks for a connection

#### Scenario: After the update
- **WHEN** a user who had 1.2.1 or earlier updates and opens the page
- **THEN** the new text shows, with no data change, migration or permission prompt

#### Scenario: TalkBack order
- **WHEN** TalkBack is on and the user swipes forward from the page's Back through the page
- **THEN** TalkBack reads the 7 paragraphs in the order above, each once, the last as the policy link with its address

#### Scenario: Largest font size and landscape
- **WHEN** the font size is 200% on a 360 dp phone, in portrait or landscape, in the dark or the light theme
- **THEN** every paragraph wraps whole, the page scrolls, and the policy link and its address are reachable by
  scrolling, Tab and TalkBack

#### Scenario: No overclaim left
- **WHEN** the app's `res/values*` strings are searched for "internet access", "sends nothing", "never leaves", "nothing
  leaves" or "100% offline", ignoring case
- **THEN** there is no match
