## MODIFIED Requirements

### Requirement: The settings list
Settings SHALL list, in sections:
- "Your diary": Reminders, Track periods, Your data;
- "Look": Theme;
- "Help": Warning signs, About Migrely;
- "Privacy and about": Privacy, Open-source licences, Version;
- "Share and rate": Share Migrely, Rate Migrely on Google Play, only while no attack is in progress;
- then "Delete all data".

It SHALL offer no analytics switch, since Migrely sends nothing (BRD SET-F1, SET-F2, D93, D185).

#### Scenario: Opening Settings
- **WHEN** the user taps the gear on home and no attack is in progress
- **THEN** the sections show in that order, each item one labelled node

## ADDED Requirements

### Requirement: Rate Migrely on Google Play
"Rate Migrely on Google Play" SHALL open Migrely's page with `ACTION_VIEW`: `market://details?id=com.migrely.app` in the
Google Play app, else `https://play.google.com/store/apps/details?id=com.migrely.app` in a browser. It SHALL never start
the in-app review flow, show a pop-up, reward anything or record anything (BRD SET-F2, D185).

#### Scenario: Opening Google Play
- **WHEN** the user taps "Rate Migrely on Google Play" on a phone with the Google Play app
- **THEN** Migrely's page opens in Google Play, and the in-app review flow is not started

#### Scenario: No Google Play app
- **WHEN** the Google Play app is missing or disabled and a browser is installed
- **THEN** `https://play.google.com/store/apps/details?id=com.migrely.app` opens in the browser (EC-SHR-01)

#### Scenario: Nothing can open it
- **WHEN** neither Google Play nor a browser can open the page, as on a work profile or managed phone that blocks them
- **THEN** Settings shows its own message that Google Play could not be opened, the app keeps running, and the tap is
  never silent (EC-SHR-01, EC-SHR-02)

#### Scenario: Offline
- **WHEN** the phone has no connection and the user taps the row
- **THEN** Google Play opens and shows its own offline state; Migrely adds nothing (EC-SHR-04)

#### Scenario: A quick double tap
- **WHEN** the user taps the row twice within 300 ms
- **THEN** one Google Play page opens (EC-SHR-05)

### Requirement: Share Migrely
"Share Migrely" SHALL open the system share sheet (`ACTION_SEND`, `text/plain`) with text only: the approved neutral
sentence and the Play link of `docs/release/play-links.md`, with `utm_source=app`, `utm_medium=share` and
`utm_campaign=settings_share` at the top level and inside `referrer=`, package always `com.migrely.app`. No file, image or
health data; nothing rewarded or recorded (BRD SET-F2, D184, D185).

#### Scenario: Sharing
- **WHEN** the user taps "Share Migrely" and picks an app
- **THEN** the chosen app gets the sentence and the link, the link equal byte for byte to the table's "In-app Settings,
  Share Migrely" URL, and no attachment

#### Scenario: A debug build
- **WHEN** a debug build, or any build with another package name, shares
- **THEN** the link still names `com.migrely.app`, never the running package (EC-SHR-09)

#### Scenario: No outcome promise
- **WHEN** the share text is read
- **THEN** it promises no health outcome, says nothing about what leaves or stays on the phone beyond "no internet
  permission", and matches the wording medical-reviewer approved

#### Scenario: No app can take it
- **WHEN** the share sheet cannot be started
- **THEN** Settings shows its own message, and the app keeps running (EC-SHR-02)

#### Scenario: A messenger drops the referrer
- **WHEN** the receiving app or its in-app browser drops the `referrer` or UTM parameters
- **THEN** the person still lands on Migrely's Play page, and Migrely neither checks nor records it (EC-SHR-10)

### Requirement: Share and rate stay out of the way
The "Share and rate" section SHALL be hidden while an attack is in progress and SHALL never appear by itself: no pop-up,
banner, badge, notification or reminder. Both rows SHALL look and behave the same for Free, subscription, lifetime and
promo-code Premium. They SHALL store nothing and add no permission, dependency or manifest entry (BRD SET-F2, D185).

#### Scenario: During an attack
- **WHEN** Settings is opened while an attack is in progress, from attack mode or the dark room
- **THEN** the "Share and rate" header and both rows are absent, and they return once the attack has ended (EC-SHR-03)

#### Scenario: Back from Google Play or the share sheet
- **WHEN** the user returns from Google Play or the share sheet, after a rotation there, or after Android killed Migrely's
  process meanwhile
- **THEN** Settings shows as before, with no message, and nothing was written or counted (EC-SHR-06)

#### Scenario: TalkBack, 200% font, landscape and keyboard
- **WHEN** the rows are reached with TalkBack, at 200% font, in landscape or with a keyboard
- **THEN** each row is one node with its title, summary, button role and action label; the text wraps without "…"; Tab
  reaches the rows in visual order and Enter opens them (EC-SHR-07)

#### Scenario: Free or Premium
- **WHEN** the user is Free, or has Premium by subscription, lifetime or promo code
- **THEN** the same rows and text show, and nothing is rewarded, unlocked or counted (EC-SHR-08)

#### Scenario: Warning signs stay first in Help
- **WHEN** the new section is present
- **THEN** "Warning signs" is still the first row of "Help", free, and in the same place as before

#### Scenario: Delete all data, backup and upgrade
- **WHEN** the user deletes all data, restores a backup, or upgrades from any shipped version
- **THEN** the rows show as on a new install, since they keep nothing (EC-SHR-11)

#### Scenario: No new permission or manifest entry
- **WHEN** the release build is made
- **THEN** `:app:checkReleasePermissions` passes and the release merged manifest has no new permission, `<queries>`
  element or component compared with `develop`
