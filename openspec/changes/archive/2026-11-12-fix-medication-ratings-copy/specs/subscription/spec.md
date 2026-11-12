## ADDED Requirements

### Requirement: The paywall names Insights without a claim
The Insights paywall's lead and the "What Premium adds" list SHALL describe Insights as what the person logged: triggers
"often logged with your migraines", when migraines come, and how the person rated their medications. They SHALL never say
what causes migraines or which medication works (BRD INS-F6, D174).

#### Scenario: The Insights lead
- **WHEN** a free user taps "Insights"
- **THEN** under the headline "See what your migraines have in common" the paywall reads "Premium shows triggers often logged
  with your migraines, when they come, and how you rated your medications."

#### Scenario: The benefit row
- **WHEN** the paywall shows, from any entry point
- **THEN** "What Premium adds" lists "When migraines come, and how you rated your medications"

#### Scenario: The benefit row at 200% font
- **WHEN** the font is at 200% on a 360 dp wide screen, in portrait or landscape, and that row would be clipped or cut with "…"
- **THEN** the row reads "When migraines come, and how you rated medications" instead
- **AND** wrapping onto more lines alone does not switch it (BRD EC-INS-04)

#### Scenario: No claim on any paywall
- **WHEN** any paywall shows, for a free user, offline or online
- **THEN** no text on it says "what helped" or "shows your triggers", in any letter case (BRD EC-INS-06)
