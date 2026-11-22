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
