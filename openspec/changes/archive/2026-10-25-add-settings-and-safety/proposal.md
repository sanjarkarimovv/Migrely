# Proposal

## Why

The MVP's settings list (SET-F1) is still mostly missing:
- a choice of theme;
- help that names the warning signs needing urgent care (SAF-F1);
- a way to wipe everything (EC-STO-03);
- the privacy, disclaimer, licence and version pages a Play listing expects;
- the restore of the automatic copy that change 7 promised for here (D75).

## What Changes

- **Settings** (D93), in sections:
  - Your diary: Reminders, Track periods, Your data;
  - Look: Theme;
  - Help: Warning signs, About Migrely;
  - Privacy and about: Privacy, Open-source licences, Version;
  - then Delete all data.
- **Theme** (D94): Dark (default), Light or Match the phone.
  - The light theme has no pure white.
  - Attack mode stays dark.
  - The theme is read behind the splash and applied at once.
- **Warning signs** (D95): the five red flags, with "Call your local emergency number".
- **Delete all data** (D96): confirmed by typing DELETE.
  - It wipes the records, device state, preferences, copies and reports.
  - It cancels reminders and notifications.
  - The app restarts into onboarding.
- **Privacy, About, licences, version** (D97).
- **Restore the automatic copy** (D98) from Your data.

## Capabilities

### New Capabilities

- `app-settings`: Settings, theme, warning signs, deleting all data, and the information pages.

### Modified Capabilities

- `data-backup`: restoring the automatic copy.

## Impact

- **Settings feature:** new screens, or sections of the existing screen, and use cases for the theme and for deleting
  everything.
- **Datasources:**
  - `PreferencesDataSource` gains the theme and `clear()`;
  - `BackupDataSource` gains `deleteAll()`, covering device-state tables too;
  - `BackupFileDataSource` gains the newest automatic copy and deleting every file;
  - a new `AppInfoDataSource` gives the version.
- **Reminders:** cancelling all scheduled work and notifications.
- **`:designsystem`:** a light colour scheme. `MigrelyTheme(dark)`.
- **`:app`:** the theme read at start, a restart into onboarding after deleting, routes and DI.
