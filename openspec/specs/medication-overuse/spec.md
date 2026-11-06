# medication-overuse Specification

## Purpose
Defines how the app counts the days on which acute medications were taken and when it shows a calm, informative
warning about medication overuse, based on ICHD-3 8.2, without diagnosing (BRD MED-F5, D45).

## Requirements

### Requirement: Count overuse days
The app SHALL count, for the last 30 days including today (the device's date), the days with at least one acute intake,
per category: simple painkillers, triptans, and combination or other, where "Not sure" counts as combination or other.
An intake's day SHALL be its date in the zone where it was taken. Preventive medications and the intakes of an attack
being deleted SHALL NOT count (BRD MED-F5, EC-MED-02 to EC-MED-04, EC-TIM-02, D45).

#### Scenario: Several intakes in one day
- **WHEN** Ibuprofen was taken three times on 10 October
- **THEN** 10 October counts as one day for simple painkillers

#### Scenario: Two categories in one day
- **WHEN** Ibuprofen and Sumatriptan were both taken on 10 October
- **THEN** 10 October counts once for simple painkillers and once for triptans

#### Scenario: Not sure
- **WHEN** a medication with the type "Not sure" was taken
- **THEN** its days count as combination or other

#### Scenario: Preventive
- **WHEN** Topiramate, a preventive medication, was taken every day
- **THEN** none of those days count

#### Scenario: Thirty days back
- **WHEN** today is 31 October and a triptan was taken on 1 and 2 October
- **THEN** 2 October counts and 1 October does not

### Requirement: Soft and doctor warnings
Home SHALL show a soft card when a category reaches 2 days below its threshold (simple 15, triptans 10, combination or
other 10) and a "talk to your doctor" card at the threshold. With two or more categories, all acute days together count
against 10, shown only when that is higher than every single category's level (ICHD-3 8.2.6). Of the warnings not
hidden by a "Got it", only the highest level SHALL show. The soft card's body SHALL state no number (BRD MED-F5, D45,
D151).

#### Scenario: Soft warning
- **WHEN** simple painkillers were taken on 13 of the last 30 days
- **THEN** home shows "Painkillers on 13 of the last 30 days" with the note "Taking them often can lead to more headaches.
  Worth keeping an eye on."

#### Scenario: Doctor warning
- **WHEN** triptans were taken on 10 of the last 30 days
- **THEN** home shows "Triptans on 10 of the last 30 days" and suggests talking to a doctor

#### Scenario: Below the soft level
- **WHEN** triptans were taken on 7 of the last 30 days and nothing else
- **THEN** no warning is shown

#### Scenario: Several categories together
- **WHEN** simple painkillers were taken on 6 days and triptans on 4 other days of the last 30
- **THEN** home shows "Acute medications on 10 of the last 30 days" and suggests talking to a doctor

#### Scenario: A single category already warned
- **WHEN** simple painkillers were taken on 15 days and triptans on 2 other days
- **THEN** home shows the painkiller doctor card, not a second card for all medications together

### Requirement: A warning once in 30 days
A warning card SHALL stay until the user taps "Got it". After that, the same or a lower level for that category SHALL NOT
show again for 30 days, but a higher level SHALL. The text SHALL inform and SHALL NOT diagnose (BRD EC-MED-06, MED-F5,
D45).

#### Scenario: Got it
- **WHEN** the user taps "Got it" on the triptan doctor card
- **THEN** the card goes away and the triptan cards stay hidden for 30 days, even if more triptan days are added

#### Scenario: From soft to doctor
- **WHEN** the user tapped "Got it" on the soft painkiller card at 13 days and the count reaches 15
- **THEN** the painkiller doctor card is shown

#### Scenario: Another category's Got it
- **WHEN** the user tapped "Got it" on the triptan doctor card 3 days ago and painkillers reach 13 days
- **THEN** the soft painkiller card is shown

#### Scenario: After 30 days
- **WHEN** 30 days have passed since "Got it" on the triptan doctor card and triptans are still at 10 days
- **THEN** the card is shown again

### Requirement: A standing indicator in History
For the current month, History's summary SHALL show a "Last 30 days" section after "Days with attack medication": a row
per category with an attack-medication intake in the last 30 days, and one for all attack medication with two or more
categories. Each row SHALL give its days, plus the number doctors watch for once reached. It SHALL count as the warning
does, ignore "Got it", not show without such an intake, and not need a logged migraine (BRD EC-MED-06, D45, D59, D99,
D151).

#### Scenario: Below the limit
- **WHEN** simple painkillers were taken on 6 of the last 30 days
- **THEN** the section reads "Simple painkillers: 6 days" in the neutral colour

#### Scenario: One day below the number
- **WHEN** simple painkillers were taken on 14 of the last 30 days
- **THEN** the section reads "Simple painkillers: 14 days" in the neutral colour, with no number to compare it to and no
  line about the doctor

#### Scenario: One day
- **WHEN** a triptan was taken on 1 of the last 30 days
- **THEN** the section reads "Triptans: 1 day"

#### Scenario: At the limit
- **WHEN** a triptan was taken on 10 of the last 30 days
- **THEN** "Triptans: 10 days. Doctors watch for 10 or more days a month." reads in amber, followed by "Taking medication
  this often, month after month, can make headaches more frequent. It's worth talking to your doctor."

#### Scenario: Two categories
- **WHEN** simple painkillers were taken on 7 days and triptans on 5 other days
- **THEN** the section reads "Simple painkillers: 7 days" and "Triptans: 5 days" in the neutral colour, and "All attack
  medication: 12 days. Doctors watch for 10 or more days a month." in amber

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
- **THEN** History's summary reads "Simple painkillers: 16 days. Doctors watch for 15 or more days a month." in amber

#### Scenario: Reached is said in words
- **WHEN** a row has reached its number and TalkBack reads it
- **THEN** the spoken text includes "Doctors watch for" and the number, so the state does not depend on the colour

#### Scenario: Largest font
- **WHEN** the font is at 200% and "All attack medication: 16 days. Doctors watch for 10 or more days a month." shows,
  upright or in landscape
- **THEN** the row wraps and none of its words are cut off

### Requirement: An overuse number is not a safe amount
A medication-overuse number SHALL show only once it is reached, as the number doctors watch for or a diagnostic threshold,
and a reached row SHALL always show with the line about talking to a doctor. The words SHALL NOT read as a safe, allowed
or recommended amount, so "limit", "safe", "allowed", "under", "within" and "maximum" SHALL NOT be used there. This rule
SHALL NOT change the numbers or the list caps (BRD D151).

#### Scenario: No row says limit
- **WHEN** any row of the "Last 30 days" section shows, reached or not
- **THEN** its text has none of the words "limit", "safe", "allowed", "under", "within" or "maximum"

#### Scenario: Before the number is reached
- **WHEN** triptans were taken on 9 of the last 30 days
- **THEN** History reads "Triptans: 9 days" and home's soft card has no number but the count in its title

#### Scenario: A reached row brings its line
- **WHEN** two rows have reached their numbers
- **THEN** "Taking medication this often, month after month, can make headaches more frequent. It's worth talking to your
  doctor." shows once, after the rows

#### Scenario: A list cap is not an overuse number
- **WHEN** the user tries to add a medication with 50 already in the list
- **THEN** the message still reads "You can keep up to 50 medications. Remove one you no longer take."
