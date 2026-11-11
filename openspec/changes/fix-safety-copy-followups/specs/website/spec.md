## ADDED Requirements

### Requirement: The site's warning signs follow the app
The urgent-help section of the medical pages and the VA page, and page 1 of both printable diary PDFs, SHALL carry the app's
Warning signs items word for word, the rash note included. A page that gains a new source SHALL list it and move its "Last
reviewed" date. The site SHALL never warn less than the app (BRD D173, EC-SAF-15).

#### Scenario: The rash note on the site
- **WHEN** a visitor reads "When should I get help straight away?" on any medical page or the VA page
- **THEN** the rash sign reads "A rash that doesn't fade when you press a glass against it (this may be harder to see on brown or
  black skin)"
- **AND** the page lists the NHS meningitis page among its sources

#### Scenario: The rash note on paper
- **WHEN** a reader prints page 1 of the A4 or US Letter diary
- **THEN** the rash sign carries the same bracket, the PDF stays under 300 KB, and nothing is cut off at the page edge

#### Scenario: The site is deployed before the app ships
- **WHEN** the site is live with the note and the installed app is an earlier version without it
- **THEN** the only difference is the added note, and no sign on the site is weaker than in the app
