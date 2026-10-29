## ADDED Requirements

### Requirement: Fact-based comparison page

The site SHALL serve `/compare/`. Its lead SHALL say Migrely's makers wrote it, name the sources and the date read, and say prices change. Each app SHALL have a card with the same set of fields in the same order, each fact from that app's own listing, website or policy, linked from the card; an unverified fact SHALL be left out. Each card SHALL list the app's strengths, and Migrely's its own limits.

#### Scenario: A visitor wants an app without internet access

- **WHEN** a visitor reads "Which apps have no internet permission?"
- **THEN** the page names Migrely and the open-source Migraine Log as requesting no internet permission, and says the
  others request "full network access" according to their Google Play permission lists
- **AND** it credits Nagi and Dark Logger for saying their diaries work offline, and does not call Migrely the only one

#### Scenario: Fair to competitors

- **WHEN** a visitor reads any competitor's card
- **THEN** it shows the same set of fields as the others, with that app's strengths from its own listing, and links to the
  sources read on the stated date
- **AND** it adds no criticism beyond the shared fields

#### Scenario: Something could not be verified

- **WHEN** a fact (for example a tracker count) could not be read from a primary source
- **THEN** it is not on the page

#### Scenario: Prices change

- **WHEN** a competitor changes its price
- **THEN** the page has already said prices change and shows the date each was read, and `COMPARE_REVIEWED` is
  updated after a full re-check
