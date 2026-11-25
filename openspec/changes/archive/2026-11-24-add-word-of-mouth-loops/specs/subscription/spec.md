## ADDED Requirements

### Requirement: A purchase made outside the app
A purchase completed in Google Play outside Migrely, such as a redeemed promo code, SHALL turn Premium on and be
acknowledged at the next check: Google Play's update while the app runs, else the next start or return. "Restore
purchases" SHALL always find it. Nothing in the app SHALL ask for, or reward, a rating or a review in return (BRD SUB-F3,
D106, D187).

#### Scenario: Redeemed while Migrely is closed
- **WHEN** a lifetime promo code is redeemed in the Play Store app while Migrely is not running, and Migrely is opened
- **THEN** Premium is on without "Restore purchases", and the purchase is acknowledged (EC-BIL-21)

#### Scenario: Redeemed while Migrely is in the background
- **WHEN** the code is redeemed while Migrely is in the background, and the user returns within 15 minutes
- **THEN** Premium is on without "Restore purchases"; if the 15-minute check spacing keeps it off, that is a defect
  filed as an issue, and "Restore purchases" turns it on (EC-BIL-22)

#### Scenario: Not opened for 3 days
- **WHEN** the code is redeemed and Migrely is not opened for more than 3 days, so Google Play refunds the unacknowledged
  purchase
- **THEN** Migrely shows no Premium when opened, and every entry stays (EC-BIL-23)

#### Scenario: Another Google account
- **WHEN** the code was redeemed on a Google account other than the one Play gives Migrely
- **THEN** Premium stays off until the user switches the account in Play and taps "Restore purchases" (EC-BIL-24)

#### Scenario: A subscription code
- **WHEN** a subscription promo code is redeemed, which gifts never use
- **THEN** Migrely shows it as a subscription with its free trial and trial end date, under the existing rules
  (EC-BIL-25)
