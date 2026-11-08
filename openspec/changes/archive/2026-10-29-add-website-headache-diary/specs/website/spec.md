## ADDED Requirements

### Requirement: Headache diary page

The site SHALL serve `/headache-diary/`. Under question headings it explains why to keep a headache diary, what to record and for how long (NICE CG150), what counts as a headache day (ICHD-3), and why medicine days are counted (ICHD-3 8.2, 8.2.6). Every fact SHALL cite a source listed on the page, which ends with a "Last reviewed" date and says that no clinician reviewed it.

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

#### Scenario: Honest authorship and review date

- **WHEN** a visitor or a crawler reads the end of the page
- **THEN** it finds the sources with links, "Last reviewed: 29 October 2026", and a note that the page was written by
  Migrely from those sources and not reviewed by a clinician
- **AND** the JSON-LD is a `MedicalWebPage` with the same `lastReviewed` date

#### Scenario: The sources change

- **WHEN** a source changes and the page is updated
- **THEN** `DIARY_REVIEWED` is updated, which changes the "Last reviewed" date, `lastReviewed` and the date in the PDFs'
  footer, and the PDFs are made again
- **AND** the sitemap's `lastmod` moves, because the HTML changed

### Requirement: Printable headache diary template

The site SHALL offer an 8-week printable headache diary as two landscape PDFs, A4 and US Letter, of 6 pages each: how to use it with a key and the warning signs, four two-week logs with one row a day, and a summary page for the doctor. It SHALL be readable when printed in black and white, and each PDF SHALL be smaller than 300 KB.

#### Scenario: Warning signs on paper

- **WHEN** a reader looks at page 1
- **THEN** it lists the NHS migraine page's emergency signs unsoftened, and its same-day signs (an attack over 72
  hours, aura over 1 hour, pregnancy or a new baby)
- **AND** it does not list numbness or a stiff neck alone as emergencies, because the NHS lists them as ordinary
  migraine symptoms

#### Scenario: Printing in black and white

- **WHEN** the template is printed on a black-and-white printer
- **THEN** every line, label and box is visible, and no meaning depends on colour

#### Scenario: Paper size

- **WHEN** a visitor in the US downloads the US Letter PDF, or anyone else downloads the A4 one
- **THEN** the PDF has 6 landscape pages of that size, and nothing is cut off at the page edges

#### Scenario: A day without headache

- **WHEN** the reader has no headache on a day
- **THEN** the instructions tell them to fill in that row too, because pain-free days count

#### Scenario: Opening on a phone

- **WHEN** a visitor taps a PDF link on a phone
- **THEN** the browser opens a file of under 300 KB

### Requirement: Link to the headache diary

Every page SHALL link to `/headache-diary/` from its footer as "Free headache diary". The page SHALL be in the sitemap, and its Google Play button SHALL carry `utm_campaign=headache-diary`.

#### Scenario: From any page

- **WHEN** a visitor is on any page of the site
- **THEN** the footer links to `/headache-diary/`
