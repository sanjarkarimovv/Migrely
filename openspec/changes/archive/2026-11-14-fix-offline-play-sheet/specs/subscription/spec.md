## ADDED Requirements

### Requirement: No Google Play sheet when the phone is offline
When the user taps a buy button, "Restore purchases" or "Manage subscription", the app SHALL first check, at that tap, whether
any network on the phone has internet. When none has, it SHALL NOT call Google Play, and SHALL show its own offline message at
once: an information message, polite, for 8 seconds stretched by the accessibility timeout. Prices, the D153 line and the
buttons SHALL stay as they are (BRD EC-BIL-19, D166).

#### Scenario: Airplane mode
- **WHEN** airplane mode is on with Wi-Fi off, and a free user taps the Yearly, Monthly or Lifetime button
- **THEN** no Google Play sheet opens, and "You're offline. Connect to the internet to buy Premium. Your diary works offline."
  shows at once

#### Scenario: Wi-Fi and mobile data off
- **WHEN** Wi-Fi and mobile data are both off and the user taps a buy button
- **THEN** the same offline message shows, and no sheet opens

#### Scenario: Restore offline
- **WHEN** the phone has no network with internet and the user taps "Restore purchases" on the paywall or in Settings
- **THEN** "You're offline. Connect to the internet to restore purchases. Your diary works offline." shows, and "No purchases
  were found for this Google account." does not

#### Scenario: Manage offline
- **WHEN** the phone has no network with internet and a subscriber taps "Manage subscription" on the paywall, in Settings or
  on the trial banner
- **THEN** Google Play does not open, and "You're offline. Connect to the internet to manage your subscription. Your diary
  works offline." shows

#### Scenario: Online
- **WHEN** a network with internet is present and the user taps a buy button
- **THEN** Google Play's purchase sheet opens as before this change

#### Scenario: Offline, then online
- **WHEN** the offline message has shown, the user turns Wi-Fi on, and taps the buy button again
- **THEN** Google Play's sheet opens, because each tap is checked on its own

#### Scenario: Many taps offline
- **WHEN** the user taps a buy button several times while offline
- **THEN** each tap answers at once with the offline message, the newest replacing the one on screen

#### Scenario: TalkBack
- **WHEN** TalkBack is on and a buy button, Restore or Manage is activated while offline
- **THEN** TalkBack speaks the offline message in full

#### Scenario: Prices from the cache
- **WHEN** the paywall opens offline and Google Play still returns cached prices
- **THEN** the plans and prices show as before, and the buy buttons are enabled

#### Scenario: The paywall's guard
- **WHEN** the offline message appears on the paywall and the user taps Close straight away
- **THEN** Close answers on the first tap, because the message is below the Close row and keeps the paywall's 0.4 second guard

### Requirement: The offline check never blocks a purchase that could work
The app SHALL treat the phone as offline only when no network on it has internet. It SHALL NOT ask that a network is validated.
When its default network is blocked for Migrely but another network has internet, or the check fails or throws, the app SHALL
open Google Play as before this change (BRD EC-BIL-20, D166).

#### Scenario: Airplane mode with Wi-Fi on
- **WHEN** airplane mode is on and Wi-Fi is connected
- **THEN** the purchase sheet opens

#### Scenario: A captive portal or an unvalidated network
- **WHEN** the phone is on a network that has internet but is not validated, such as a hotel portal before sign-in
- **THEN** the purchase sheet opens, and what it shows is Google Play's own

#### Scenario: A VPN
- **WHEN** a VPN is on
- **THEN** the app follows what Android reports for its networks, and when any of them has internet the sheet opens

#### Scenario: A work profile
- **WHEN** Migrely runs in a work profile
- **THEN** the check is the same, and online the sheet opens

#### Scenario: Data Saver
- **WHEN** Data Saver is on and Migrely is in the foreground with a network that has internet
- **THEN** the purchase sheet opens

#### Scenario: The check fails
- **WHEN** reading the network state throws, including a `SecurityException` because the permission is missing
- **THEN** Google Play's sheet opens as before this change, and no offline message shows

#### Scenario: The network drops while the sheet is open
- **WHEN** Google Play's sheet is already open and the network goes away
- **THEN** what the sheet shows is Google Play's own, Back leaves it, and the paywall answers as D153 says

### Requirement: The offline check can fail
The check SHALL include a negative control: with only the permission removed, the offline taps SHALL open Google Play's sheet
again, as on 1.1.0. Reports SHALL say what emulator and one-phone runs do not prove (BRD D166).

#### Scenario: Permission removed
- **WHEN** the offline rows run on a build of the same commit with only `ACCESS_NETWORK_STATE` removed again
- **THEN** Google Play's sheet opens offline as on 1.1.0, which also shows the check fails open

#### Scenario: The owner's phone
- **WHEN** the release build is on the owner's phone, offline and online, with the owner present
- **THEN** the offline rows show the offline message and no sheet, and online the sheet opens

## MODIFIED Requirements

### Requirement: What a purchase depends on
Wherever the paywall shows a buy button, it SHALL show directly under the terms: "Purchases need Google Play and an
internet connection. Your diary works offline." The line SHALL not depend on the connection or on any answer from Google
Play, and the line SHALL add no dialog. The offline check of D166 SHALL NOT change the line (BRD §7.3, EC-BIL-14, EC-BIL-17, D153,
D166).

#### Scenario: Google Play's own error
- **WHEN** a network is present but Google Play still shows its own "No internet connection" sheet, as behind a captive portal or
  when the network drops after the sheet opened, the user leaves it, and Google Play still returns the products
- **THEN** the same paywall is back, its button works again, and the line is under the terms, whichever code Google Play
  returned

#### Scenario: Every plan
- **WHEN** the user chooses Yearly, Monthly or Lifetime
- **THEN** the line reads the same under that plan's terms

#### Scenario: With a connection
- **WHEN** the phone has a working connection, or has none
- **THEN** the line shows just the same, because it does not depend on the connection

#### Scenario: A subscriber changing plan
- **WHEN** a monthly subscriber opens the paywall from Settings
- **THEN** the line is under the terms and above "Manage subscription"

#### Scenario: Payment pending
- **WHEN** a payment is pending and a plan can still be chosen
- **THEN** "Payment pending" and the line both show

#### Scenario: No buy button
- **WHEN** the paywall is loading, shows "Premium isn't available in your region yet" or "Google Play can't be reached
  right now", or the user owns lifetime
- **THEN** the line does not show

#### Scenario: 200% font on a small phone
- **WHEN** the font is at 200% on a small phone, in portrait or landscape
- **THEN** the buy button is where it was without the line, the line and the terms are not cut, every part can be reached
  by scrolling, and "Restore purchases" is no lower than it was before this change

#### Scenario: TalkBack
- **WHEN** TalkBack moves through the paywall
- **THEN** it reads the line once, as a stop of its own after the terms, and does not announce it again when the paywall
  reloads

### Requirement: Restore and Manage without Google Play
"Restore purchases" SHALL open no Google Play sheet. When Google Play gives no answer, it SHALL say "Google Play can't be
reached. Try again later.", on the paywall and in Settings. "Manage subscription" SHALL open Google Play's page outside
the app, and the app SHALL add no message of its own. When the phone has no network with internet, neither SHALL reach
Google Play, and each SHALL show its offline message (BRD EC-BIL-18, EC-BIL-19, D153, D166).

#### Scenario: Restore on the paywall
- **WHEN** the user taps "Restore purchases" on the paywall and Google Play gives no answer
- **THEN** the paywall says "Google Play can't be reached. Try again later."
- **AND** when the phone has no network with internet, it says "You're offline. Connect to the internet to restore purchases.
  Your diary works offline." and never "No purchases were found for this Google account."

#### Scenario: Restore in Settings
- **WHEN** the user taps "Restore purchases" in Settings and Google Play gives no answer
- **THEN** the section shows the note "Google Play can't be reached. Try again later."
- **AND** when the phone has no network with internet, the note is the restore offline message

#### Scenario: Manage subscription
- **WHEN** a subscriber taps "Manage subscription"
- **THEN** Google Play's subscriptions page opens outside the app, and what it says about a connection is Google Play's
  own
- **AND** when the phone has no network with internet, the page does not open and the offline message shows instead
