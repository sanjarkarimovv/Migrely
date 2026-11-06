## ADDED Requirements

### Requirement: An overuse number is not a safe amount
Where the app shows a medication-overuse number, the words SHALL present it as a number doctors watch for or a diagnostic
threshold. They SHALL NOT read as a safe, allowed or recommended amount, so "limit", "safe", "allowed", "under", "within"
and "maximum" SHALL NOT be used there. This rule SHALL NOT change the numbers or the list caps (BRD D151).

#### Scenario: No row says limit
- **WHEN** any row of the "Last 30 days" section shows, below or at or past its number
- **THEN** its text has none of the words "limit", "safe", "allowed", "under", "within" or "maximum"

#### Scenario: A list cap is not an overuse number
- **WHEN** the user tries to add a medication with 50 already in the list
- **THEN** the message still reads "You can keep up to 50 medications. Remove one you no longer take."

## MODIFIED Requirements

### Requirement: A standing indicator in History
For the current month, History's summary SHALL show a "Last 30 days" section after "Days with attack medication": one
row per category with an attack-medication intake in the last 30 days, giving its days and the number doctors watch for,
and a row for all attack medication when two or more categories were used. It SHALL count as the warning does, SHALL not
be quieted by "Got it", SHALL not show without such an intake, and SHALL not need a logged migraine (BRD EC-MED-06, D45,
D59, D99, D151).

#### Scenario: Below the limit
- **WHEN** simple painkillers were taken on 6 of the last 30 days
- **THEN** the section reads "Simple painkillers: 6 days (doctors watch for 15+)" in the neutral colour

#### Scenario: One day below the number
- **WHEN** simple painkillers were taken on 14 of the last 30 days
- **THEN** the section reads "Simple painkillers: 14 days (doctors watch for 15+)" in the neutral colour, with no
  "reached" and no line about the doctor

#### Scenario: One day
- **WHEN** a triptan was taken on 1 of the last 30 days
- **THEN** the section reads "Triptans: 1 day (doctors watch for 10+)"

#### Scenario: At the limit
- **WHEN** a triptan was taken on 10 of the last 30 days
- **THEN** "Triptans: 10 days (doctors watch for 10+), reached" reads in amber, followed by "At this many days, medication
  can make headaches more frequent. Talk to your doctor about it."

#### Scenario: Two categories
- **WHEN** simple painkillers were taken on 7 days and triptans on 5 other days
- **THEN** the section also reads "All attack medication: 12 days (doctors watch for 10+), reached", in amber

#### Scenario: After an undo
- **WHEN** an attack whose intakes reach a row's number is deleted and then restored with "Undo" in History
- **THEN** the section counts those intakes again, as History's numbers do

#### Scenario: A past month
- **WHEN** History shows September
- **THEN** no "Last 30 days" section shows

#### Scenario: No attack medication
- **WHEN** no attack medication was taken in the last 30 days
- **THEN** no "Last 30 days" section shows

#### Scenario: No migraine logged
- **WHEN** no migraine was ever logged and simple painkillers were taken on 16 of the last 30 days
- **THEN** History's summary reads "Simple painkillers: 16 days (doctors watch for 15+), reached" in amber

#### Scenario: Reached is said in words
- **WHEN** a row has reached its number and TalkBack reads it
- **THEN** the spoken text includes "reached", so the state does not depend on the colour

#### Scenario: Largest font
- **WHEN** the font is at 200% and "Combination or other: 16 days (doctors watch for 10+), reached" shows, upright or in
  landscape
- **THEN** the row wraps and none of its words are cut off
