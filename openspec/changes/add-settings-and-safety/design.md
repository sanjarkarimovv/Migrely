# Design

## Decisions

### D1. Settings

- **Sections:** Settings stays one screen with sections. Each item is a row with title and summary. Track periods
  stays a switch row. Theme opens a small dialog with three radio choices.
- **New screens in `:features:settings:ui`:**
  - Warning signs;
  - About Migrely;
  - Privacy;
  - Open-source licences, a static list in resources;
  - Delete all data, a confirmation dialog with a text field.
- **Version:** read through `AppInfoDataSource` (`PackageManager`) in `:datasource:implementation`.

### D2. Theme

- **Datasource:** `PreferencesDataSource.theme()` / `setTheme()`, with keys `dark`, `light` and `system`, `dark` by
  default.
- **`:designsystem`:**
  - `MigrelyTheme(dark: Boolean = true)` chooses the dark scheme or a new light scheme with no pure white;
  - `MigrelyColors` (attack mode) stays dark;
  - attack mode and the dark room keep their own dark colours, so they are dark in either theme. Screens built only
    from `MaterialTheme.colorScheme` follow the theme.
- **`:app`:**
  - `PreferencesDataSource.themeChanges(): Flow<ThemeDataModel>` starts with the current value and follows every change
    (a SharedPreferences listener);
  - `MainActivity` reads the theme in the same splash wait as the start decision, through `StartEntryPoint`, then
    collects the flow into Compose state, so a change in Settings redraws every screen at once with no restart;
  - the status and navigation bars follow the theme: light icons on dark, dark icons on light;
  - "Match the phone" follows `isSystemInDarkTheme()`.

### D3. Delete all data

- **`DeleteAllDataUseCase`** runs these steps in this order, stops at the first that fails, and returns Deleted or
  Failed:
  1. `BackupDataSource.deleteAll()`: every table in one transaction;
  2. `BackupFileDataSource.deleteAll()`: `files/backups`, then `ReportDocumentDataSource.deleteAll()`: `cache/reports`;
  3. `ReminderScheduleDataSource.cancelAll()` and `NotificationDataSource.cancelAll()`;
  4. `PreferencesDataSource.clear()`.
- **Order:**
  - the records go first, so a failure there has changed nothing, and no planned "Did it help?" is lost;
  - a worker that runs between the steps finds no reminder settings (the daily reminder is off without them) or no
    intake, and does nothing;
  - the preferences go last, so the reset that brings onboarding back marks the end;
  - every step is safe to run again, so trying again finishes the job.
- **Before the restart**, `:app` removes every notification once more, in case a worker that was already running posted
  after step 3.
- **The restart:** on Deleted, the ViewModel emits `OnAllDataDeleted`, and `:app` restarts the process
  (`Intent.makeRestartActivityTask` and then `exitProcess(0)`). App-wide singletons, such as Your data's and the
  report's ViewModels, can hold nothing from before. The app then finds no records and no finished onboarding, and
  shows onboarding.

### D4. Restoring the copy

- **Datasource:** `BackupFileDataSource.newestAutomatic(): AutomaticCopyDataModel?`, with its time and text.
- **Backup domain:**
  - `GetAutomaticCopyUseCase` returns the time and summary of the newest copy, or None;
  - `RestoreAutomaticCopyUseCase` saves the current records as a copy first, then replaces from the copy read before.
- **Your data** gets a row "Restore the copy from <date>" and a confirmation dialog like the import's.

## Risks

| Risk | Mitigation |
|---|---|
| A screen with hard-coded dark colours in the light theme | every screen is walked in Light; fixed colours are kept only for attack mode and skies |
| Deleting while a reminder fires | cancel scheduled work first, then delete; a worker that runs finds no settings and does nothing |
