## ADDED Requirements

### Requirement: Medication-overuse headache page

The site SHALL serve `/medication-overuse-headache/`. It opens with a short answer giving the ICHD-3 conditions (headache on 15 or more days a month; more than 3 months; 10 or more days for triptans, ergots, opioids, combination painkillers or a mix of kinds; 15 or more for simple painkillers alone, one or several), shows them as a table, and explains that days are counted, not doses, and that these are not safe limits.

#### Scenario: A visitor asks how many days is too many

- **WHEN** a visitor opens `https://migrely.health/medication-overuse-headache/`
- **THEN** the first paragraph answers with the ICHD-3 conditions, including headache on 15 or more days a month and
  more than 3 months
- **AND** a table lists each medicine kind with its days a month, and says that a mix of simple painkillers alone
  stays at 15

#### Scenario: Not a safe limit

- **WHEN** the page discusses the thresholds
- **THEN** it says they are diagnostic thresholds, not safe limits, and gives the NHS advice of no more than 2 days a
  week

#### Scenario: Treatment as general information

- **WHEN** the page explains treatment
- **THEN** it summarises NICE CG150: withdrawing the overused medicine, headaches possibly getting worse before they
  improve, possible preventive treatment and a review after 4 to 8 weeks
- **AND** it tells the reader to plan this with their doctor, opioids especially, and not to stop or change medicines
  on their own

#### Scenario: The app's note is not a diagnosis

- **WHEN** the page describes Migrely's medicine-day note
- **THEN** it says the app counts medicine days by kind over the last 30 days, shows a note before and at these
  counts, is free, and that the note is a heads-up, not a diagnosis

#### Scenario: A small phone

- **WHEN** the page is shown 320 px wide at 200% text
- **THEN** the table wraps inside the page and nothing scrolls sideways

### Requirement: Shared parts of the medical pages

`/headache-diary/` and `/medication-overuse-headache/` SHALL each end with their sources, a "Last reviewed" date and a note that no clinician reviewed them. Each SHALL have the same urgent-help paragraph and a `MedicalWebPage` JSON-LD whose `lastReviewed` matches the date shown.

#### Scenario: Same urgent-help text

- **WHEN** a visitor reads "When should I get help straight away?" on either page
- **THEN** the text is identical and links to the NHS migraine page

### Requirement: Links to the medication-overuse headache page

`/headache-diary/` and `/about/` SHALL link to `/medication-overuse-headache/` where they mention medicine days. The page SHALL be in the sitemap, and its Play button SHALL carry `utm_campaign=medication-overuse-headache`.

#### Scenario: From the diary page

- **WHEN** a visitor reads "Why count medicine days?" on `/headache-diary/`
- **THEN** a link takes them to `/medication-overuse-headache/`
