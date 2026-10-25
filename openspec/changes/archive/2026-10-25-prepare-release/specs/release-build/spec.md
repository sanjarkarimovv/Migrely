## Purpose

Defines how a release build of Migrely is made and checked before it goes to Play (BRD D44, D102).

## ADDED Requirements

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
The release build SHALL pass the same permission check as the debug build: no INTERNET, no network state, no exact
alarms and no foreground service (BRD D44).

#### Scenario: The release manifest
- **WHEN** the release build is assembled
- **THEN** its merged manifest's permission check passes
