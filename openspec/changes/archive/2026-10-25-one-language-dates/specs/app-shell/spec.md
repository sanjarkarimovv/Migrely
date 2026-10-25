## ADDED Requirements

### Requirement: Dates and times in English
While the app's words are English only, every name a date or time shows, months, weekdays and AM or PM, SHALL be
English. Only the first day of the week and the system's 12- or 24-hour setting SHALL come from the phone. Dates SHALL be
formatted in the phone's locale when its language is English, otherwise in English with the phone's country, or in
plain English when it has none (BRD EC-L10N-01, EC-L10N-03, EC-TIM-05, D117).

#### Scenario: A phone in Russian
- **WHEN** the phone's locale is Russian (Russia) with the 24-hour setting
- **THEN** home's date names its weekday and month in English, such as "Sunday" and "October", History's weeks start on
  Monday, and times read like "14:30"

#### Scenario: A phone in English (United States)
- **WHEN** the phone's locale is English (United States) with the 12-hour setting
- **THEN** the weeks start on Sunday and times read like "2:30 PM"

#### Scenario: The language changes while the app runs
- **WHEN** the phone's language is changed from English to Russian while Migrely is open
- **THEN** on returning to Migrely its dates are still English, and its weeks start on Monday

#### Scenario: A first day chosen in the phone's settings
- **WHEN** a phone in Russian has Sunday chosen as the first day of the week in its regional preferences
- **THEN** Migrely's weeks start on Sunday

#### Scenario: The CSV on a phone in Russian
- **WHEN** a phone in Russian exports the CSV
- **THEN** its columns are still separated by semicolons, by the phone's own language (D72)
