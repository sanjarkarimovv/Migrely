## MODIFIED Requirements

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
