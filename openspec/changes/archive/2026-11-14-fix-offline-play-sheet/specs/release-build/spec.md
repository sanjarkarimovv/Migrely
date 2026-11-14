## MODIFIED Requirements

### Requirement: Release permissions
The release build SHALL pass the same permission check as the debug build: no INTERNET, no Wi-Fi state, no change of network state, no exact
alarms and no foreground service. `ACCESS_NETWORK_STATE` is allowed, only to see that the phone is offline before a Google Play sheet opens
(BRD D44, D166).

#### Scenario: The release manifest
- **WHEN** the release build is assembled
- **THEN** its merged manifest's permission check passes, and its permission report lists `ACCESS_NETWORK_STATE` as the only network permission

#### Scenario: INTERNET comes back
- **WHEN** a library or a manifest line adds `android.permission.INTERNET`
- **THEN** the permission check fails the build
