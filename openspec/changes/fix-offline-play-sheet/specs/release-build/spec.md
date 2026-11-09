## MODIFIED Requirements

### Requirement: Release permissions
The release build SHALL pass the same permission check as the debug build: no INTERNET, no Wi-Fi state, no change of
network state, no exact alarms and no foreground service. `ACCESS_NETWORK_STATE` SHALL be the only permission added since
1.1.0, for the offline check (BRD D44, D166).

#### Scenario: The release manifest
- **WHEN** the release build is assembled
- **THEN** its merged manifest's permission check passes

#### Scenario: One permission more than 1.1.0
- **WHEN** `:app:checkReleasePermissions` runs on 1.1.1
- **THEN** its report lists the permissions of 1.1.0 plus `android.permission.ACCESS_NETWORK_STATE`, and nothing else

#### Scenario: INTERNET comes back
- **WHEN** a library or a manifest line adds `android.permission.INTERNET`
- **THEN** the permission check fails the build
