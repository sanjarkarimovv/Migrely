## ADDED Requirements

### Requirement: VA migraine log page

The site SHALL serve `/va-migraine-log/`. Before anything else it SHALL state that Migrely is not affiliated with or endorsed by VA, that the page is general information and not legal or medical advice, that no log or app can promise a rating, and that accredited VSO representatives help for free. It SHALL quote DC 8100 from 38 CFR 4.124a with the date it was read, and VA's DBQ definition of "prostrating".

#### Scenario: A veteran reads the page

- **WHEN** a visitor opens `https://migrely.health/va-migraine-log/`
- **THEN** the first block after the heading says Migrely is not affiliated with or endorsed by VA, that this is not
  legal or medical advice, that no app can promise a rating, and that VSO representatives help for free
- **AND** a table gives the four DC 8100 levels (50, 30, 10 and 0 percent) in the regulation's own words, with the
  date the regulation was read

#### Scenario: The rating schedule changes

- **WHEN** VA changes 38 CFR 4.124a
- **THEN** the page has already said that VA can change it and links to the current text, and the review date
  (`VA_REVIEWED`) shows how old the quote is

#### Scenario: "Bed rest" is not a legal finding

- **WHEN** the page describes Migrely's "Impact on your day" labels
- **THEN** it says they are the user's own record and that VA decides what counts as prostrating

#### Scenario: Export

- **WHEN** a veteran wants to share the log
- **THEN** the page says the spreadsheet export is free, with one row per attack and its columns, and that the PDF
  report is part of Premium

### Requirement: Page-specific disclaimer in the review block

`reviewed_block()` SHALL accept the page's own disclaimer. The medical pages SHALL keep "not reviewed by a clinician. Educational only, not medical advice", and the VA page SHALL say it was not reviewed by a lawyer or by VA and is general information, not legal or medical advice.

#### Scenario: Medical pages unchanged

- **WHEN** the site is built
- **THEN** `/headache-diary/`, `/medication-overuse-headache/` and `/menstrual-migraine/` show the same review line as
  before
