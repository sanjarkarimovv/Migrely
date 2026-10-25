## ADDED Requirements

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
