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
Help SHALL show the warning signs that need urgent care: sudden pain that is the worst of one's life, pain after a head
injury, a stiff neck with fever, weakness, numbness or trouble speaking that does not pass, and a first headache after
50. It SHALL say to call the local emergency number, without dialling one (BRD SAF-F1, D95).

#### Scenario: Reading them
- **WHEN** the user opens Settings, then "Warning signs"
- **THEN** the five signs show with the advice to call the local emergency number

### Requirement: Delete all data
"Delete all data" SHALL be confirmed by typing DELETE. It SHALL delete:
- every health record and the device state;
- the preferences;
- the automatic copies and the reports.

It SHALL cancel planned reminders and remove notifications, and then start the app again at onboarding. The
subscription SHALL be untouched (BRD EC-STO-03, D96).

#### Scenario: Not typed
- **WHEN** the user opens the confirmation and types "delete"
- **THEN** the button stays off and nothing is deleted

#### Scenario: Deleted
- **WHEN** the user types DELETE and confirms
- **THEN** the app starts again at onboarding with no records, no copies and no reminders

### Requirement: Privacy and about
Settings SHALL offer:
- **Privacy:** no internet, no account, the records stay on the phone and in Android's own backup, and files leave
  only when exported;
- **About Migrely:** the full disclaimer;
- **Open-source licences:** the libraries and their licences;
- **Version:** the app's version.

(BRD SET-F1, EC-STO-04, D97.)

#### Scenario: The version
- **WHEN** the user looks at Settings
- **THEN** the Version item shows the installed version

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
