## MODIFIED Requirements

### Requirement: The paywall
The paywall SHALL show:
- a headline for where it was opened from;
- the three products, with the yearly plan chosen first;
- a button that names the action: "Start free trial", "Subscribe" or "Buy once";
- the terms: renews automatically, cancel anytime in Google Play, and cancelling before the trial ends costs nothing;
- "Restore purchases", then what Premium adds;
- a close button visible from the first frame.

(BRD §7.3, D109, D153.)

#### Scenario: From the doctor report
- **WHEN** a free user taps "Doctor report" in History
- **THEN** the paywall opens with a doctor-report headline, and closing it returns to History

#### Scenario: Restore comes before what Premium adds
- **WHEN** the paywall shows, whatever the state of its offers
- **THEN** "Restore purchases" comes above "What Premium adds", and the terms stay directly under the buy button

## ADDED Requirements

### Requirement: What a purchase depends on
Wherever the paywall shows a buy button, it SHALL show directly under the terms: "Purchases need Google Play and an
internet connection. Your diary works offline." The line SHALL not depend on the connection or on any answer from Google
Play, and the app SHALL NOT add a permission or a dialog for it (BRD §7.3, EC-BIL-14, EC-BIL-17, D153).

#### Scenario: Google Play's own error
- **WHEN** a free user taps the buy button, Google Play shows its own "No internet connection" sheet, the user taps OK, and
  Google Play still returns the products
- **THEN** the same paywall is back, its button works again, and the line is under the terms, whichever code Google Play
  returned

#### Scenario: Every plan
- **WHEN** the user chooses Yearly, Monthly or Lifetime
- **THEN** the line reads the same under that plan's terms

#### Scenario: With a connection
- **WHEN** the phone has a working connection
- **THEN** the line shows just the same, because the app does not know the connection

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

### Requirement: When a purchase sheet does not complete
After Google Play's sheet closes without a purchase, the app SHALL NOT try to tell Play's error from a cancel. A cancel or
no answer SHALL show no message. `Failed` SHALL keep its message. `Unavailable` SHALL show "Google Play can't be reached.
Try again later." only while Play still offers the products. If reading the products fails, the notice SHALL say "Check your connection, then try again. Your diary works offline." (BRD EC-BIL-14…16, D153).

#### Scenario: A person's own cancel
- **WHEN** the user closes Google Play's sheet themselves
- **THEN** no message shows, nothing changes, and the line is on screen as before

#### Scenario: Google Play answers its error as a cancel
- **WHEN** Google Play returns `USER_CANCELED` after its own error sheet
- **THEN** the paywall behaves as for a person's own cancel

#### Scenario: Failed
- **WHEN** Google Play returns any code that maps to `Failed`
- **THEN** the paywall says "The purchase didn't go through. Try again."

#### Scenario: Unavailable while the products are still offered
- **WHEN** Google Play returns `BILLING_UNAVAILABLE` or `ITEM_UNAVAILABLE` for the sheet and still returns the products
- **THEN** the cards show again and the paywall says "Google Play can't be reached. Try again later."

#### Scenario: Unavailable with no products
- **WHEN** Google Play returns `BILLING_UNAVAILABLE` for the sheet and then returns no products
- **THEN** the paywall shows "Premium isn't available in your region yet", with no other message

#### Scenario: The reload after a sheet fails
- **WHEN** Google Play's sheet closes without a purchase and reading the products then fails, whichever code the sheet
  returned
- **THEN** the cards and the line leave, and the notice "Google Play can't be reached right now" says "Check your connection, then try again. Your diary works offline.", with "Try again"

#### Scenario: A rotation during the sheet
- **WHEN** the phone rotates while the sheet is open, and the sheet then closes as `Unavailable` with the products still
  offered
- **THEN** the message shows once on the rotated screen

### Requirement: Restore and Manage without Google Play
"Restore purchases" SHALL open no Google Play sheet. When Google Play gives no answer, it SHALL say "Google Play can't be
reached. Try again later.", on the paywall and in Settings. "Manage subscription" SHALL open Google Play's page outside
the app, and the app SHALL add no message of its own (BRD EC-BIL-18, D153).

#### Scenario: Restore on the paywall
- **WHEN** the user taps "Restore purchases" on the paywall and Google Play gives no answer
- **THEN** the paywall says "Google Play can't be reached. Try again later."

#### Scenario: Restore in Settings
- **WHEN** the user taps "Restore purchases" in Settings and Google Play gives no answer
- **THEN** the section shows the note "Google Play can't be reached. Try again later."

#### Scenario: Manage subscription
- **WHEN** a subscriber taps "Manage subscription"
- **THEN** Google Play's subscriptions page opens outside the app, and what it says about a connection is Google Play's
  own
