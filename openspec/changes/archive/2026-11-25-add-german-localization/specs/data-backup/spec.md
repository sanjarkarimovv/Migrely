## ADDED Requirements

### Requirement: Exports stay language-neutral
The CSV's column names and fixed values SHALL stay English in every app language, and its separator SHALL follow the
system's language, not Migrely's own app language. The JSON backup's keys and format SHALL NOT change: a file made in
one app language SHALL restore in the other, built-in names SHALL show in the current language and typed names as typed
(BRD EC-L10N-10, EC-L10N-11, D72, D195).

#### Scenario: CSV from the German app on a German phone
- **WHEN** the app and the phone are in German and the CSV is exported
- **THEN** the column names and fixed values are English and the columns are separated by semicolons

#### Scenario: CSV with German chosen for Migrely only
- **WHEN** on Android 13 or newer the phone is in English (United States), Migrely's app language is German, and the CSV
  is exported
- **THEN** the columns are separated by commas, as on that phone before

#### Scenario: CSV with English chosen for Migrely only
- **WHEN** the phone is in German and Migrely's app language is English
- **THEN** the columns are separated by semicolons

#### Scenario: A backup moves between languages
- **WHEN** a backup made in the English app is imported in the German app, and one made in German is imported in English
- **THEN** every entry is restored, built-in triggers and symptoms show in the current app language, and triggers,
  medications and notes the person typed show exactly as typed

#### Scenario: A typed name next to a built-in one
- **WHEN** a person had typed their own trigger "Stress" and the German built-in trigger is also "Stress"
- **THEN** both stay as they were, as in English, and nothing is merged or renamed
