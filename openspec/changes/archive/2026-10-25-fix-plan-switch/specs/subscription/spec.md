## MODIFIED Requirements

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
