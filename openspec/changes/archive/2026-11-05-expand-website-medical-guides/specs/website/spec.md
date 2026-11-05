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

- **WHEN** a source changes and the page is updated
- **THEN** `DIARY_REVIEWED` is updated, which changes the "Last reviewed" date, `lastReviewed` and the date in the PDFs'
  footer, and the PDFs are made again
- **AND** the sitemap's `lastmod` moves, because the HTML changed

### Requirement: Shared parts of the medical pages

`/headache-diary/`, `/medication-overuse-headache/` and `/menstrual-migraine/` SHALL each end with their sources, a "Last reviewed" date and a note that no clinician reviewed them. Each SHALL have the same urgent-help paragraph, which gives every item of the NHS migraine page's "Call 999" list, and a `MedicalWebPage` JSON-LD whose `lastReviewed` matches the date shown.

#### Scenario: Same urgent-help text

- **WHEN** a visitor reads "When should I get help straight away?" on any medical page or the VA page
- **THEN** the text is identical and links to the NHS migraine page

#### Scenario: Every NHS 999 sign

- **WHEN** the urgent-help paragraph is read
- **THEN** it names a sudden, extremely painful headache, one after a recent head injury, weakness on one side of the
  body or face, problems speaking or remembering, loss of vision or blurred or double vision, drowsiness or
  confusion, a seizure, and a very high temperature with signs of meningitis

## ADDED Requirements

### Requirement: Direct answers on the medical guides

On `/headache-diary/`, `/medication-overuse-headache/` and `/menstrual-migraine/`, the first paragraph after each question heading SHALL be a direct answer of 40 to 60 words. The app sections, the privacy section and the sources are exempt. The build SHALL stop and name the page and heading when an answer falls outside that range.

#### Scenario: An answer grows too long

- **WHEN** a direct answer is edited to 64 words and the site is built
- **THEN** the build stops and names the page and the heading

### Requirement: Follow-up questions on the medical guides

`/medication-overuse-headache/` SHALL also answer how common it is, its signs, how to lower the risk, how long headaches get worse after stopping, and whether it comes back. `/menstrual-migraine/` SHALL also answer why it happens, whether period attacks are worse, what to bring to an appointment, and perimenopause, menopause and HRT. Figures from one study SHALL name it in the text; where sources disagree on how to stop a medicine, the page SHALL leave that to the doctor.

#### Scenario: How long it gets worse

- **WHEN** a reader asks how long headaches get worse after stopping the overused medicine
- **THEN** the page gives the duration a 2017 review found, attributed to that review, and the American Migraine
  Foundation's wording, and names no method of stopping

#### Scenario: Relapse figures

- **WHEN** the page gives relapse figures
- **THEN** it attributes them to the review, gives the full range, and says most studies were done in specialist
  headache centres

#### Scenario: HRT with migraine

- **WHEN** a reader asks whether they can take HRT with migraine
- **THEN** the page says, citing the Migraine Trust, that migraine, aura included, does not rule HRT out, that the
  doctor goes through the benefits and risks, and names no product or dose
