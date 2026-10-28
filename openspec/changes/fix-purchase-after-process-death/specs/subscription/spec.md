## ADDED Requirements

### Requirement: A purchase finished while the app was closed
When the app was closed while Google Play's purchase sheet was open, the next check of the entitlement SHALL ask Google
Play at once, even within 15 minutes of the last answer. A purchase completed meanwhile SHALL be acknowledged and SHALL
unlock Premium with the plan and trial the sheet was opened for. A pending one SHALL show "Payment pending" (BRD
EC-BIL-13).

#### Scenario: Completed while closed
- **WHEN** the app is closed with the yearly free trial's sheet open, Google Play completes the purchase, and the app
  is started again a minute later
- **THEN** Settings shows the free trial with its end date, and the purchase is acknowledged

#### Scenario: Pending while closed
- **WHEN** the same happens with a payment that completes later
- **THEN** the app shows "Payment pending" and Premium stays locked
