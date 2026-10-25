## MODIFIED Requirements

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
