## MODIFIED Requirements

### Requirement: When the paywall shows itself
Without a tap, the paywall SHALL show only:
- once after the last onboarding step;
- once on returning home after the 5th migraine, when Insights can show patterns by their own rule (D88).

It SHALL show at most once in any 7 days, only once Google Play billing is known to be available and Google Play
offers Migrely's products, and never during attack mode or in the dark room. A tap on a premium feature SHALL always
open it (BRD §7.3, EC-BIL-11, D110).

#### Scenario: The 5th migraine
- **WHEN** a free user logs their 5th migraine in the last 6 months, the first was logged at least 30 days ago, and no
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
