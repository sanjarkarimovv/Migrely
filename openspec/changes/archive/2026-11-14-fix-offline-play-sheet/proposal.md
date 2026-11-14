# Proposal

## Why

Google rejected 1.0.1 for an "unresponsive UI element". On the owner's phone, Play-installed 1.1.0 shows the cause
(`migrely-work/phone-110/EVIDENCE.md`):
- Offline, a buy button opens Google Play's sheet. It shows "Loading..." for about 20 s, then "Error / No internet connection" with an OK.
- OK is a retry. 5 taps gave 5 × Finsky `Acquire UNAVAILABLE net::ERR_INTERNET_DISCONNECTED`, and the sheet never closes. Only Back leaves.
- 1.1.0 still opens this sheet.
- Offline, Play serves cached prices, so billing cannot tell the app it is offline.
- Restore says "No purchases were found for this Google account", which misleads.

On 2026-11-09 the owner approved `ACCESS_NETWORK_STATE` for this, and only this. It is BRD D166, and it reverses D153's "never" and #36 for this one
permission. `INTERNET` stays forbidden.

## What Changes

- **The check.** When a buy button, "Restore purchases" or "Manage subscription" is tapped, the app first asks Android whether any network on the phone has
  `NET_CAPABILITY_INTERNET`. If none has, it does not call Google Play and shows its own message at once. The check does not ask for VALIDATED. If the
  check fails, the app opens Play as today (fail open).
- **Every path that opens Play:**
  - the three buy buttons;
  - Restore on the paywall and in Settings;
  - Manage on the paywall, in Settings and on the trial banner.
  The check sits in the domain use cases, so all three ViewModels get it.
- **Copy**, information kind, polite, 8 s:
  - "You're offline. Connect to the internet to buy Premium. Your diary works offline."
  - "You're offline. Connect to the internet to restore purchases. Your diary works offline."
  - "You're offline. Connect to the internet to manage your subscription. Your diary works offline."
- **Build:**
  - `forbiddenPermissions` drops `ACCESS_NETWORK_STATE`. `INTERNET`, `ACCESS_WIFI_STATE` and `CHANGE_NETWORK_STATE` stay.
  - The manifest's `tools:node="remove"` line for it goes, and the comment is rewritten.
  - The merged manifest gains exactly one permission.
- **Release:** versionCode 9, versionName 1.1.1, and a 1.1.1 block in `docs/release/whats-new.md`.

## Out of scope

- Play's own sheet once it is open: a network that drops then, a captive portal, a VPN with no network under it. These are residual, and Back leaves.
- Disabling buttons, an offline banner, a network listener, any caching of the result.
- `INTERNET` or any other network permission.
- #166, #167 and the push under a message (1.2). The card scroll for #171 is a separate change on this branch, `fix-safety-card-into-view`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `subscription`: no Play sheet offline, a check that never blocks a purchase that could work, and Restore and Manage offline.
- `release-build`: one permission more than 1.1.0.

## Impact

- `datasource`: a network-state data source (Android `ConnectivityManager`), failing open.
- `features/subscription` domain, data, presentation and UI: an offline outcome for buy, restore and manage, and the 3 strings.
- `app`: the manifest, `build.gradle.kts`'s `forbiddenPermissions` and version, and the DI binding.
- `docs/release/whats-new.md`, `docs/ARCHITECTURE.md` (the line about datatransport's permissions).
- Data safety: no change. The permission reads a status on the phone, and nothing is collected, stored or sent.
- BRD: D166, D153 and EC-BIL-14/18 amended, EC-BIL-19, EC-BIL-20, §13 row 35. Issue #36. Owner decision 2026-11-09.
