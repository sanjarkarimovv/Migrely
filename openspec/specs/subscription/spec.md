# subscription Specification

## Purpose
Defines Migrely Premium: its products, purchasing through Google Play, the entitlement, the paywall and when it shows,
the trial reminder, and the store used for testing (BRD §7, SUB-F1, EC-BIL-01…12, D105–D111, D113).

## Requirements

### Requirement: Products from Google Play
The paywall SHALL offer three products with the local prices Google Play gives: the yearly plan, with a 7-day free
trial for new subscribers, the monthly plan, and lifetime as one payment. No price SHALL be written into the app
(BRD §7.2, SUB-F1, D107).

#### Scenario: Prices shown
- **WHEN** the paywall opens on a phone in Germany
- **THEN** each product shows Google Play's price in euros, and the yearly plan reads "7 days free, then" its price per
  year when the trial is offered

#### Scenario: No trial left
- **WHEN** the user had the yearly trial before
- **THEN** the yearly plan shows its price with no trial line, as Google Play offers it

### Requirement: Buying
Buying SHALL go through Google Play's purchase sheet. A completed purchase SHALL be acknowledged and checked on the
phone, and SHALL unlock Premium at once. A pending payment SHALL show "Payment pending" and unlock nothing until it
completes. A cancelled sheet SHALL change nothing (BRD EC-BIL-08, D107).

#### Scenario: A purchase
- **WHEN** the user buys the monthly plan and Google Play confirms it
- **THEN** Premium unlocks and the paywall returns to the feature it was opened from

#### Scenario: Pending
- **WHEN** the user pays with a method that completes later
- **THEN** the paywall shows "Payment pending" and the premium features stay locked until Google Play confirms it

### Requirement: The entitlement
Premium SHALL be on while Google Play reports an active subscription or the lifetime purchase, checked at start and on
return (at most every 15 minutes), after a purchase and on "Restore purchases". When Google Play cannot be reached, an
active subscription SHALL stay on for 3 days after the last successful check, and lifetime until Google Play says
otherwise. When Premium ends, the premium features SHALL lock and no data SHALL be touched (BRD EC-BIL-01, EC-BIL-04…07,
D106, D115).

#### Scenario: Offline
- **WHEN** the subscription was confirmed yesterday and Google Play cannot be reached today
- **THEN** Premium stays on

#### Scenario: Refunded
- **WHEN** Google Play no longer reports the purchase after a refund
- **THEN** the doctor report and Insights lock, and every entry stays

#### Scenario: New phone
- **WHEN** the user installs Migrely on a new phone with the same Google account
- **THEN** Premium is found again at the first check, and "Restore purchases" checks again on demand

#### Scenario: Reinstall or backup on another account
- **WHEN** a lifetime buyer reinstalls Migrely, or restores it from a backup on a phone signed in to another Google
  account
- **THEN** Premium comes only from what Google Play reports for the phone's account, since the entitlement cache is
  never backed up (D115)

### Requirement: Changing plans
Moving from monthly to yearly SHALL start the yearly plan at once, with Google Play charging its price on the next
billing date (`WITHOUT_PRORATION`). Monthly and yearly are base plans of one subscription, where Play allows only that
mode or charging the full price at once. Buying lifetime while a subscription is active SHALL then show how to cancel
the subscription in Google Play, since the app cannot cancel it (BRD EC-BIL-09, EC-BIL-10, D107, D120).

#### Scenario: Monthly to yearly
- **WHEN** a monthly subscriber chooses yearly in "Change plan" and confirms in Google Play's sheet
- **THEN** Google Play accepts the switch, Settings shows Premium on the yearly plan, and the yearly price is charged on
  the date the monthly plan would have renewed

#### Scenario: Lifetime over a subscription
- **WHEN** a monthly subscriber buys lifetime
- **THEN** Premium continues, and the app says to cancel the monthly plan in Google Play with a "Manage subscription"
  button

### Requirement: Billing unavailable
When Google Play billing is not available, the paywall SHALL say "Premium isn't available in your region yet" and every
free feature SHALL work (BRD EC-BIL-11, D107).

#### Scenario: No billing
- **WHEN** Google Play reports billing unavailable
- **THEN** the paywall shows that line and no buy button

#### Scenario: No Google Play billing service on the phone
- **WHEN** the phone has no Google Play billing service at all
- **THEN** the paywall shows that line at once, without waiting on Google Play, and no paywall shows itself

### Requirement: Payment problems
At start, the app SHALL ask Google Play to show its own messages about a payment that failed, during a grace period or
an account hold (BRD EC-BIL-03, EC-BIL-04, D107).

#### Scenario: Grace period
- **WHEN** a renewal payment failed and the subscription is in its grace period
- **THEN** Premium stays on and Google Play's message to update the payment method shows

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

### Requirement: When the paywall shows itself
Without a tap, the paywall SHALL show only:
- once after the last onboarding step;
- once on returning home after the 5th migraine, when Insights can show patterns by their own rule (D88).

It SHALL show at most once in any 7 days, only once Google Play billing is known to be available and Google Play
offers Migrely's products, and never during attack mode or in the dark room. A tap on a premium feature SHALL always
open it (BRD §7.3, EC-BIL-11, D110).

#### Scenario: The 5th migraine
- **WHEN** a free user logs their 5th migraine in the last 6 months, the first started at least 30 days ago, and no
  paywall showed itself in the last 7 days
- **THEN** the paywall opens once on returning to home, with a patterns headline that names no number

#### Scenario: Patterns not ready yet
- **WHEN** a free user logs their 5th migraine within 30 days of their first
- **THEN** no paywall shows itself until Insights could show their patterns

#### Scenario: Within 7 days
- **WHEN** the onboarding paywall showed 3 days ago and the 5th migraine is logged now
- **THEN** no paywall shows itself

#### Scenario: Google Play offers no products
- **WHEN** Google Play billing connects but returns none of Migrely's products, as before they are set up in the Play
  Console
- **THEN** no paywall shows itself after onboarding or the 5th migraine, Google Play is asked again at most once an
  hour, and a tap on a premium feature still opens the paywall with "Premium isn't available in your region yet"

### Requirement: Trial ending reminder
When the trial's end is known, the day before it ends, home SHALL show a banner once, "Your free trial ends tomorrow", with
"Manage". The trial's end is known when the purchase used the trial on this phone (BRD §7.3, EC-BIL-02, D111).

#### Scenario: A day before
- **WHEN** the trial started on this phone 6 days ago
- **THEN** home shows the banner until it is dismissed, and it does not come back

### Requirement: A store for testing
Debug builds SHALL use a simulated store that can complete, cancel or hold a purchase as pending, and end or restore
Premium. It SHALL also be able to answer with no products while billing connects, as Google Play does before the
products are set up in Play Console (BRD D122). Release builds SHALL always use Google Play (BRD D113).

#### Scenario: Release
- **WHEN** a release build starts
- **THEN** it talks to Google Play, and no simulated store is in it

#### Scenario: No products from the store for testing
- **WHEN** a debug build's store is set from adb to answer with no products
- **THEN** the app behaves as when Google Play offers no products: with nothing bought, a tap on a premium feature
  opens the paywall with "Premium isn't available in your region yet", and a Premium bought before stays Premium

### Requirement: A purchase finished while the app was closed
When the app was closed while Google Play's purchase sheet was open, the next check of the entitlement SHALL ask Google
Play at once, as an exception to the 15 minutes between checks. A purchase completed meanwhile SHALL be acknowledged and
SHALL unlock Premium with the plan and trial the sheet was opened for. A pending one SHALL show "Payment pending" (BRD
EC-BIL-13).

#### Scenario: Completed while closed
- **WHEN** the app is closed with the yearly free trial's sheet open, Google Play completes the purchase, and the app
  is started again a minute later
- **THEN** Settings shows the free trial with its end date, and the purchase is acknowledged

#### Scenario: Pending while closed
- **WHEN** the same happens with a payment that completes later
- **THEN** Settings shows "Payment pending", and the doctor report and Insights stay locked

#### Scenario: Only once
- **WHEN** the sheet was cancelled before the app closed, and the app is started twice within a minute
- **THEN** Google Play is asked at the first start only

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

### Requirement: "Try again" answers at once
On a tap on "Try again" the paywall SHALL show "Getting prices from Google Play" at once. If Google Play stays unreachable,
the notice "Google Play can't be reached right now" SHALL return and the message "Google Play can't be reached. Try again
later." SHALL show. If Google Play answers, the paywall SHALL show that answer with no message (BRD EC-BIL-14, D153).

#### Scenario: Still unreachable
- **WHEN** the notice shows, the user taps "Try again", and Google Play gives no answer
- **THEN** "Getting prices from Google Play" shows at once, in place of the notice
- **AND** the notice and "Try again" return, and the message says "Google Play can't be reached. Try again later."

#### Scenario: Try again more than once
- **WHEN** the user taps "Try again" again after that message, and Google Play still gives no answer
- **THEN** that tap gets the same answer: "Getting prices from Google Play" at once, then the notice and the message

#### Scenario: The products come back
- **WHEN** the user taps "Try again" and Google Play returns the products
- **THEN** the cards, the buy button and the line show, with no message

#### Scenario: Google Play answers with no products
- **WHEN** the user taps "Try again" and Google Play reports billing unavailable
- **THEN** the paywall shows "Premium isn't available in your region yet", with no other message

#### Scenario: A quick double tap
- **WHEN** the user taps "Try again" twice quickly and Google Play gives no answer
- **THEN** the message shows once

#### Scenario: A rotation while getting prices
- **WHEN** the phone rotates after a tap on "Try again", and Google Play then gives no answer
- **THEN** the notice shows and the message shows once on the rotated screen

#### Scenario: TalkBack on Try again
- **WHEN** TalkBack is on, the user activates "Try again", and Google Play gives no answer
- **THEN** it reads "Getting prices from Google Play", then the message

### Requirement: A paywall's message answers that paywall
A tap on a buy button SHALL clear any older message on screen, so none is left when the sheet closes. A reload still running
from an earlier paywall SHALL deliver nothing on a new paywall where nothing was tapped (BRD EC-BIL-15, EC-BIL-16, D153).

#### Scenario: A buy tap clears an older message
- **WHEN** a message shows, the user taps the buy button, and then closes Google Play's sheet themselves
- **THEN** the older message is gone when the sheet opens, and no message shows after the sheet closes
- **AND** a rotation during the sheet does not bring it back

#### Scenario: A buy tap that fails again
- **WHEN** a message shows, the user taps the buy button, and Google Play returns a code that maps to `Failed`
- **THEN** the only message after the sheet closes is the one for this tap

#### Scenario: A reload from an earlier paywall
- **WHEN** a reload after a refused sheet or after "Try again" is still running, the user closes the paywall and opens it
  again, taps nothing, and that reload then ends
- **THEN** no message from it shows on the new paywall

#### Scenario: A purchase is not lost with it
- **WHEN** a reload from an earlier paywall delivers nothing on a new one, and Premium has come on meanwhile
- **THEN** Premium stays on, and the new paywall shows what its own reading finds

#### Scenario: A rotation is the same paywall
- **WHEN** the phone rotates while a reload is running
- **THEN** that reload's message still shows, once

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
