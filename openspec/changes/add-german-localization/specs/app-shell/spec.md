## RENAMED Requirements

- FROM: `### Requirement: Dates and times in English`
- TO: `### Requirement: Dates and times in the app's language`

## MODIFIED Requirements

### Requirement: Dates and times in the app's language
Every name a date or time shows, months, weekdays and AM or PM, SHALL be in the app's language. In German, dates SHALL be
formatted in the chosen German locale; in English, in the phone's locale when its language is English, otherwise in
English with the phone's country, or plain English with none. The first day of the week and the 12- or 24-hour setting
SHALL come from the phone (BRD EC-L10N-03, EC-L10N-08, EC-TIM-05, D117, D195).

#### Scenario: A phone in Russian
- **WHEN** the phone's locale is Russian (Russia) with the 24-hour setting
- **THEN** home's date names its weekday and month in English, such as "Sunday" and "October", History's weeks start on
  Monday, and times read like "14:30"

#### Scenario: A phone in English (United States)
- **WHEN** the phone's locale is English (United States) with the 12-hour setting
- **THEN** the weeks start on Sunday and times read like "2:30 PM"

#### Scenario: A phone in German (Germany)
- **WHEN** the phone's locale is German (Germany) with the 24-hour setting
- **THEN** home's date reads like "Sonntag, 22. November", History's weeks start on Monday, times read like "14:30" and a
  decimal reads like "2,5"

#### Scenario: German with the 12-hour setting
- **WHEN** the phone's locale is German (Germany) and the system is set to the 12-hour clock
- **THEN** times follow the 12-hour setting, and the month and weekday names stay German

#### Scenario: A phone in German (Austria)
- **WHEN** the phone's locale is German (Austria)
- **THEN** month names are the system's own Austrian ones, such as "Jänner", and the app does not replace them

#### Scenario: The language changes while the app runs
- **WHEN** the phone's language is changed from English to Russian while Migrely is open
- **THEN** on returning to Migrely its dates are still English, and its weeks start on Monday

#### Scenario: Changing to German while the app runs
- **WHEN** the phone's language is changed from English to German while Migrely is open
- **THEN** on returning to Migrely its words and dates are German

#### Scenario: A first day chosen in the phone's settings
- **WHEN** a phone in Russian or German has Sunday chosen as the first day of the week in its regional preferences
- **THEN** Migrely's weeks start on Sunday

#### Scenario: The CSV on a phone in Russian
- **WHEN** a phone in Russian exports the CSV
- **THEN** its columns are still separated by semicolons, by the phone's own language (D72)
