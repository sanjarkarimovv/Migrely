## MODIFIED Requirements

### Requirement: Calendar follows the locale
Weeks SHALL start on the phone's first day of the week, with the weekday initials in English, the language of the
app's words, whatever the phone's language. Today SHALL be outlined (BRD EC-TIM-05, D117).

#### Scenario: Week starts on Monday
- **WHEN** the phone's locale is English (United Kingdom)
- **THEN** the weeks start on Monday

#### Scenario: Week starts on Sunday
- **WHEN** the phone's locale is English (United States)
- **THEN** the weeks start on Sunday

#### Scenario: A phone in Russian
- **WHEN** the phone's locale is Russian (Russia)
- **THEN** the weeks start on Monday, and the weekday initials read "M T W T F S S"

#### Scenario: Today outlined
- **WHEN** the calendar shows the current month
- **THEN** today's day is outlined
