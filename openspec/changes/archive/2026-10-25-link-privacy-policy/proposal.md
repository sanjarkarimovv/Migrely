# Proposal

## Why

Google Play asks apps that handle health data to show their privacy policy inside the app as well as on the store
listing. Migrely's Privacy page sums up the policy in a few sentences, but it does not lead to the full text. The full
policy now lives at https://migrely.health/privacy/ (D119).

## What Changes

- The Privacy page in Settings ends with "Read the full privacy policy". The address `migrely.health/privacy` is
  written under it.
- Tapping it opens the policy in the phone's browser. This needs no permission of Migrely's, so the app still has no
  internet access.
- A phone with no browser does nothing on the tap and does not crash. The written address can still be typed elsewhere.

## Capabilities

### Modified Capabilities

- `app-settings`: the Privacy page links the full privacy policy.

## Impact

- `:features:settings:ui`: the Privacy page and one string.
- BRD: D119 is brought into the release, since 1.0.0 now points to migrely.health.
