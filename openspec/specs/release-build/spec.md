# release-build Specification

## Purpose
Defines how a release build of Migrely is made and checked before it goes to Play (BRD D44, D102, D172).

## Requirements

### Requirement: A shrunk release build
The release build SHALL be minified and resource-shrunk with R8. It SHALL keep every feature working: start,
logging and ending an attack, History, medications, reminders, backups and import, the PDF report, Insights, Settings
and deleting all data. Keep rules SHALL be added only for code that R8 breaks, each with a comment saying why
(BRD D102).

#### Scenario: Release smoke
- **WHEN** the release build is installed on the emulator and the smoke flows are walked
- **THEN** every flow works as in the debug build, and no crash shows in logcat

### Requirement: Signing without secrets in git
The release build SHALL be signed with the upload key named in a git-ignored `keystore.properties`. When the file is
absent, it SHALL be signed with the debug key so that it can be smoke-tested locally, and such a build SHALL never be
uploaded (BRD D102).

#### Scenario: No keystore file
- **WHEN** `keystore.properties` is absent and the release build is assembled
- **THEN** the build succeeds, signed with the debug key

### Requirement: Release permissions
The release build SHALL pass the same permission check as the debug build: no INTERNET, no Wi-Fi state, no change of network state, no exact
alarms and no foreground service. `ACCESS_NETWORK_STATE` is allowed, only to see that the phone is offline before a Google Play sheet opens
(BRD D44, D166).

#### Scenario: The release manifest
- **WHEN** the release build is assembled
- **THEN** its merged manifest's permission check passes, and its permission report lists `ACCESS_NETWORK_STATE` as the only network permission

### Requirement: The release build carries no debug-only components
A release build SHALL declare no debug-only component in its merged manifest: `androidx.compose.ui.tooling.PreviewActivity`, the
`androidx.activity.ComponentActivity` entry from ui-test-manifest, or the simulated billing receiver. The check SHALL read the merged manifest, not
library names, and SHALL fail both the APK and the bundle build. Debug builds SHALL keep these components (BRD D172).

#### Scenario: The release manifest is clean
- **WHEN** `assembleRelease` and `bundleRelease` run on this change
- **THEN** both pass, and the release merged manifest declares none of the three components

#### Scenario: A debug-only library on the release classpath
- **WHEN** a feature UI module declares `ui-tooling` or `ui-test-manifest` with `implementation` and a release build runs (negative control)
- **THEN** `assembleRelease` and `bundleRelease` fail, naming `PreviewActivity` or `ComponentActivity` and saying to use `debugImplementation`

#### Scenario: The same library under another artifact name
- **WHEN** a module declares `androidx.compose.ui:ui-tooling-android` with `implementation`, or another library brings it in transitively
- **THEN** the release build fails the same way, because the check reads the merged manifest

#### Scenario: The app's own debug component
- **WHEN** the simulated billing receiver is declared in a manifest that the release variant merges
- **THEN** the release build fails, naming `SimulatedBillingReceiver`

#### Scenario: Debug keeps previews
- **WHEN** the debug build is assembled
- **THEN** it passes, its merged manifest still declares `PreviewActivity`, and the check is not run for it

#### Scenario: The manifest cannot be read
- **WHEN** the release merged manifest is missing or unreadable when the check runs
- **THEN** the release build fails; the check never passes without reading it

#### Scenario: Removed by a manifest rule
- **WHEN** a listed component is removed from the release manifest with `tools:node="remove"`
- **THEN** the check passes, because the component does not ship
