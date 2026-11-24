## MODIFIED Requirements

### Requirement: The settings list
Settings SHALL list, in sections:
- "Your diary": Reminders, Track periods, Your data;
- "Look": Theme;
- "Help": Warning signs, About Migrely;
- "Privacy and about": Privacy, Open-source licences, Version;
- "Share and rate": Share Migrely, Rate Migrely on Google Play, only while no attack is in progress;
- then "Delete all data".

It SHALL offer no analytics switch, since Migrely has no internet permission and no analytics (BRD SET-F1, SET-F2, D93,
D181, D185).

#### Scenario: Opening Settings
- **WHEN** the user taps the gear on home and no attack is in progress
- **THEN** the sections show in that order, each item one labelled node

## ADDED Requirements

### Requirement: Rate Migrely on Google Play
"Rate Migrely on Google Play", summary "Opens Migrely's store page, outside the app", SHALL open Migrely's page with
`ACTION_VIEW`: `market://details?id=com.migrely.app` in the Google Play app, else
`https://play.google.com/store/apps/details?id=com.migrely.app` in a browser. It SHALL never start the in-app review
flow, show a pop-up, reward anything or record anything (BRD SET-F2, D185).

#### Scenario: Opening Google Play
- **WHEN** the user taps "Rate Migrely on Google Play" on a phone with the Google Play app
- **THEN** Migrely's page opens in Google Play, and the in-app review flow is not started

#### Scenario: No Google Play app
- **WHEN** the Google Play app is missing or disabled and a browser is installed
- **THEN** `https://play.google.com/store/apps/details?id=com.migrely.app` opens in the browser (EC-SHR-01)

#### Scenario: Nothing can open it
- **WHEN** both `market://` in Google Play and the https page in a browser fail with `ActivityNotFoundException` or a
  `SecurityException`, as on a work profile or managed phone that blocks them
- **THEN** Settings shows "This phone has no app that can open Google Play." in its message host, the app keeps running,
  and the tap is never silent (EC-SHR-01, EC-SHR-02)

#### Scenario: Offline
- **WHEN** the phone has no connection and the user taps the row
- **THEN** Google Play opens and shows its own offline state; Migrely adds nothing (EC-SHR-04)

### Requirement: Share Migrely
"Share Migrely", summary "Send someone the link to Migrely. Your entries aren't included.", SHALL open the system share
sheet (`ACTION_SEND`, `text/plain`) with "Migrely is a private migraine diary for Android. No internet permission, no
account, no ads.", a newline and the "In-app Settings, Share Migrely" link of `docs/release/play-links.md`. No file,
stream or `ClipData` URI. A share SHALL never be rewarded, unlock anything or be counted (BRD SET-F2, D184, D185).

#### Scenario: Sharing
- **WHEN** the user taps "Share Migrely" and picks an app
- **THEN** `EXTRA_TEXT` equals the sentence, a newline and the link, the link equal byte for byte to
  `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=app&utm_medium=share&utm_campaign=settings_share&referrer=utm_source%3Dapp%26utm_medium%3Dshare%26utm_campaign%3Dsettings_share`
- **AND** `EXTRA_SUBJECT` and `EXTRA_TITLE` are "Migrely, a migraine diary for Android", and the chooser title is "Share
  Migrely"

#### Scenario: Text only
- **WHEN** the share intent is built
- **THEN** it has no `EXTRA_STREAM`, no `ClipData` with a URI and no grant flag, so "Your entries aren't included" stays
  true; a test asserts this (medical-reviewer condition W3)

#### Scenario: A debug build
- **WHEN** a debug build, or any build with another package name, shares
- **THEN** the link still names `com.migrely.app`, never the running package (EC-SHR-09)

#### Scenario: No outcome promise and no reward
- **WHEN** the share text is read, and after any number of shares
- **THEN** it promises no health outcome and says nothing about what stays on or leaves the phone beyond "No internet
  permission"; nothing is rewarded, unlocked or counted, so a share stays the person's own choice (EC-SHR-08)

#### Scenario: No app can take it
- **WHEN** the share sheet cannot be started
- **THEN** Settings shows "This phone has no app to share the link with.", and the app keeps running (EC-SHR-02)

#### Scenario: A messenger drops the referrer
- **WHEN** the receiving app or its in-app browser drops the `referrer` or UTM parameters
- **THEN** the person still lands on Migrely's Play page, and Migrely neither checks nor records it (EC-SHR-10)

### Requirement: One opening per tap
After a tap on "Share Migrely" or "Rate Migrely on Google Play", both rows SHALL ignore activation until Settings is
resumed after leaving or, if Settings never pauses, until Android's double-tap timeout has passed. This SHALL hold for
touch, TalkBack, Switch Access and key repeat. A failure message SHALL be stamped, so a later deliberate tap shows and
speaks it again (BRD D185, D178, EC-SHR-05).

#### Scenario: A quick double tap
- **WHEN** the user taps "Share Migrely" twice within the double-tap timeout
- **THEN** one share sheet opens (EC-SHR-05)

#### Scenario: Share then Rate in a burst
- **WHEN** the user taps "Share Migrely" and then "Rate Migrely on Google Play" within the double-tap timeout
- **THEN** only the share sheet opens

#### Scenario: Held Enter key
- **WHEN** a keyboard user holds Enter on "Rate Migrely on Google Play" so the key repeats
- **THEN** one Google Play page opens

#### Scenario: A second deliberate tap after a failure
- **WHEN** nothing can open Google Play and the user taps the row again after the timeout
- **THEN** the message is shown and spoken again as a new message

#### Scenario: Without the guard (negative control)
- **WHEN** QA runs the double-tap check on a build with the guard removed
- **THEN** two share sheets or two Play intents start, which shows the check can fail

### Requirement: Share and rate stay out of the way
The "Share and rate" section SHALL be hidden while an attack is in progress or the attack state is unknown, and SHALL
never appear by itself: no pop-up, banner, badge, notification or reminder. Both rows SHALL look and behave the same for
Free, subscription, lifetime and promo-code Premium, store nothing, and add no permission, dependency or manifest entry
(BRD SET-F2, D185).

#### Scenario: During an attack
- **WHEN** an attack is in progress while Settings is shown or loads again (the UI opens Settings only from home's
  today mode, never from attack mode or the dark room)
- **THEN** the "Share and rate" header and both rows are absent, and they return once the attack has ended (EC-SHR-03)

#### Scenario: Loading
- **WHEN** Settings is still loading and the attack state is not yet known
- **THEN** the section is absent, and it appears with the rest of the content once no attack is known (EC-SHR-12)

#### Scenario: An attack starts while Settings is open
- **WHEN** an attack starts while Settings is open and TalkBack or keyboard focus is on the section's header or a row
- **THEN** the section fades and collapses in 200 ms (at once with "Remove animations"), focus moves to "Open-source
  licences" (or, if TalkBack focus cannot be moved there, to the "Settings" title), never to "Delete all data", and
  nothing is announced (EC-SHR-13; unit and Compose tests only, since no UI path changes the attack state while
  Settings stays on screen)

#### Scenario: The attack ends while Settings is open
- **WHEN** the attack ends while Settings is open
- **THEN** the section returns, focus stays where it was, and nothing is announced (EC-SHR-13; unit and Compose tests
  only)

#### Scenario: Back from Google Play or the share sheet
- **WHEN** the user returns from Google Play or the share sheet, after a rotation there, or after Android killed Migrely's
  process meanwhile
- **THEN** Settings shows with no message, and nothing was written or counted; the scroll position after process death
  is not promised, as for all of Settings before this change (EC-SHR-06)

#### Scenario: TalkBack, 200% font, landscape and keyboard
- **WHEN** the rows are reached with TalkBack, at 200% font, in landscape or with a keyboard
- **THEN** each row is one node whose name starts with its visible title, with its summary, the button role and the
  action "choose an app to share with" or "open Google Play"; the text wraps without "…"; the rows have no chevron; Tab
  reaches them in visual order and Enter or Space opens them (EC-SHR-07)

#### Scenario: Free or Premium
- **WHEN** the user is Free, or has Premium by subscription, lifetime or promo code
- **THEN** the same rows and text show, and nothing is rewarded, unlocked or counted (EC-SHR-08)

#### Scenario: Warning signs stay first in Help
- **WHEN** the new section is present
- **THEN** "Warning signs" is still the first row of "Help", free, and in the same place as before

#### Scenario: Delete all data, backup and upgrade
- **WHEN** the user deletes all data, restores a backup, or upgrades from any shipped version
- **THEN** the rows show as on a new install, with no "new" badge, since they keep nothing (EC-SHR-11)

#### Scenario: No new permission or manifest entry
- **WHEN** the release build is made
- **THEN** `:app:checkReleasePermissions` passes and the release merged manifest has no new permission, `<queries>`
  element or component compared with `develop`
