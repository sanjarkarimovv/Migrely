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
- what Premium adds;
- the three products, with the yearly plan chosen first;
- a button that names the action: "Start free trial", "Subscribe" or "Buy once";
- the terms: renews automatically, cancel anytime in Google Play, and cancelling before the trial ends costs nothing;
- "Restore purchases";
- a close button visible from the first frame.

(BRD §7.3, D109.)

#### Scenario: From the doctor report
- **WHEN** a free user taps "Doctor report" in History
- **THEN** the paywall opens with a doctor-report headline, and closing it returns to History

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
