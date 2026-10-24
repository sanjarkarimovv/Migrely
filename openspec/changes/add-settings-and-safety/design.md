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
  - `MainActivity` reads the theme in the same splash wait as the start decision, through `StartEntryPoint`;
  - it keeps it as Compose state;
  - Settings changes it through the use case and then asks the activity to apply it. The UI calls
    `(context as? Activity)?.recreate()` after the save succeeds, so every screen redraws in the new theme.
  - "Match the phone" follows `isSystemInDarkTheme()`.

### D3. Delete all data

- **`DeleteAllDataUseCase`** runs these steps, and returns Deleted or Failed:
  - `BackupDataSource.deleteAll()`: every table in one transaction;
  - `PreferencesDataSource.clear()`;
  - `BackupFileDataSource.deleteAll()`: `files/backups`;
  - `ReportDocumentDataSource.deleteAll()`: `cache/reports`;
  - `ReminderScheduleDataSource.cancelAll()` and `NotificationDataSource.cancelAll()`.
- **The UI:** on Deleted, the app restarts its task into `MainActivity`, which finds no records and no finished
  onboarding and shows onboarding.

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
