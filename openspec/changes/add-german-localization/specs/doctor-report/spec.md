## ADDED Requirements

### Requirement: The report screen names the PDF's language
The PDF report SHALL stay in English in this release, whatever the app's language (D82). When the app's language is not
English, the report screen SHALL say in one line, in the app's language, that the PDF is in English. In English the line
SHALL NOT show. Free and Premium rules SHALL NOT change (BRD EC-L10N-09, D195).

#### Scenario: Making a report in German
- **WHEN** a Premium user with the app in German opens the doctor report screen and makes a PDF
- **THEN** the screen says in German that the PDF is in English, and the PDF's words are English, the same as in the
  English app

#### Scenario: Making a report in English
- **WHEN** the app is in English
- **THEN** the report screen shows no line about the PDF's language

#### Scenario: The line at the largest font
- **WHEN** the app is in German at 200% font on a 360 dp phone, in portrait and in landscape
- **THEN** the line wraps at spaces, is not cut, and TalkBack reads it once with the screen's other text

### Requirement: The custom range's headline stays whole and is said once
The custom range picker's headline SHALL show the start and the end, each a date or its placeholder, whole. It SHALL
wrap only between them, the dash kept after the start, in Material's style and colour. TalkBack SHALL say it once, as
one polite node: the start, then the end, each its date or "not chosen", in English and German (BRD EC-L10N-28, D181,
D195 (18)).

#### Scenario: German at the largest font
- **WHEN** the app is German at 200% font on a 360 dp phone and the custom range picker opens with no date chosen
- **THEN** the headline shows "Startdatum –" and "Enddatum", each whole, the end on the next line if both do not fit,
  and no word breaks inside

#### Scenario: English at the largest font
- **WHEN** the app is English at 200% font on a 360 dp phone and the custom range picker opens
- **THEN** "Start date –" and "End date" each stay whole, and "End date" never breaks after "En"

#### Scenario: Chosen dates
- **WHEN** a start and an end are chosen at 200% font on a 360 dp phone
- **THEN** each date, such as "23. Nov. 2026", stays on one line, and the line wraps only between the two dates

#### Scenario: Room for both
- **WHEN** the headline fits on one line, as at 100% font
- **THEN** the start, the dash and the end show on one line

#### Scenario: TalkBack in English
- **WHEN** TalkBack is on, the app is English and no date is chosen
- **THEN** the headline is one stop that says "Start date, not chosen, end date, not chosen" once, and the shown words
  are not read again after it

#### Scenario: TalkBack in German
- **WHEN** TalkBack is on, the app is German and no date is chosen
- **THEN** the headline says "Startdatum, nicht gewählt, Enddatum, nicht gewählt" once

#### Scenario: A date is picked
- **WHEN** TalkBack is on and the person picks the start date
- **THEN** the headline is announced politely, with the start's date in its spoken form and the end "not chosen"
