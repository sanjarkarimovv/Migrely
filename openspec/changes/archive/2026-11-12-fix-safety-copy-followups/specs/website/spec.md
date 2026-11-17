## ADDED Requirements

### Requirement: The site's warning signs follow the app
The urgent-help section of the medical pages and the VA page, and page 1 of both printable diary PDFs, SHALL carry the app's
Warning signs items word for word, the rash note included. A page that gains a new source SHALL list it. Its "Last reviewed" date SHALL move only when
every fact on the page has been re-checked against its sources on that date. The site SHALL never warn less than the app (BRD
D173, EC-SAF-15).

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

#### Scenario: A new source without a full re-check
- **WHEN** the NHS meningitis page is added to a page's sources for the rash note, and the page's other facts are not re-checked
  that day
- **THEN** the page's "Last reviewed" date and `lastReviewed` stay as they were
- **AND** the PDF footer's "Updated <date>" may move, because the PDF changed

## MODIFIED Requirements

### Requirement: Headache diary page

The site SHALL serve `/headache-diary/`. Under question headings it explains why to keep a diary, what to record and for how long (NICE CG150), which triggers to note and why they are not proof, how to keep it going, what a headache day is (ICHD-3), more than one kind of headache, why medicine days count (ICHD-3 8.2) and what a doctor looks for. Every fact SHALL cite a listed source; the page ends with a "Last reviewed" date and says no clinician reviewed it.

#### Scenario: A visitor preparing for an appointment

- **WHEN** a visitor opens `https://migrely.health/headache-diary/`
- **THEN** the page lists what NICE suggests recording (frequency, duration and severity, other symptoms, every
  prescribed and over-the-counter medicine, possible triggers, the link to periods) and says to keep it for at least
  8 weeks
- **AND** it offers the template as A4 and US Letter PDFs near the top

#### Scenario: Medicine days are not read as safe limits

- **WHEN** the page explains medication overuse
- **THEN** it describes medication-overuse headache as headache on 15 or more days a month after more than 3 months
  of: triptans, ergots, opioids or combination painkillers on 10 or more days a month; a mix of these kinds, or of
  these and simple painkillers, on 10 or more days in total; or simple painkillers alone, one or several, on 15 or
  more days
- **AND** it says these are diagnostic thresholds, not safe limits, quotes the NHS advice of no more than 2 days a week,
  and tells the reader not to stop or change medicines without their doctor

#### Scenario: Triggers are not proof

- **WHEN** the page suggests possible triggers to note
- **THEN** it says a trigger seen alongside an attack is not proof that it caused it, that early symptoms of an attack
  can look like triggers, and that several factors often act together

#### Scenario: Honest authorship and review date

- **WHEN** a visitor or a crawler reads the end of the page
- **THEN** it finds the sources with links, "Last reviewed: 4 November 2026", and a note that the page was written by
  Migrely from those sources and not reviewed by a clinician
- **AND** the JSON-LD is a `MedicalWebPage` with the same `lastReviewed` date

#### Scenario: The sources change

- **WHEN** a source changes, the page is updated, and every fact on the page is re-checked against its sources that day
- **THEN** `DIARY_REVIEWED` is updated, which changes the "Last reviewed" date and `lastReviewed`, and the PDFs are made
  again with their footer's "Updated <date>"
- **AND** the sitemap's `lastmod` moves, because the HTML changed

#### Scenario: A change without a full re-check

- **WHEN** the page or the PDFs change, but not every fact on the page is re-checked that day (BRD D173)
- **THEN** `DIARY_REVIEWED` stays, and the PDF footer's "Updated <date>" may move
- **AND** the sitemap's `lastmod` moves if the HTML changed
