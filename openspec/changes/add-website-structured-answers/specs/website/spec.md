## ADDED Requirements

### Requirement: Help page answers

`/support/` SHALL have a title of 40 to 60 characters, a description of 120 to 160 and the H1 "Migrely help". Each question SHALL be an H2 whose answer starts with the direct answer, and the page SHALL publish them as a `FAQPage`. Premium prices SHALL come from the same constant as `/compare/`.

#### Scenario: A searcher asks how to cancel

- **WHEN** a crawler reads `/support/`
- **THEN** "How do I cancel my subscription?" is an H2, its first sentence answers it, and the same question and
  answer are in the `FAQPage`

#### Scenario: Prices change

- **WHEN** Premium's prices change and the constant is updated
- **THEN** `/support/` and `/compare/` show the same new prices

### Requirement: Home page definition and questions

The home page SHALL say what Migrely is in one paragraph of 40 to 60 words under the H2 "What is Migrely?". It SHALL answer five questions asked before installing, each under its own heading, link to `/support/` for more, and publish them as a `FAQPage` in its `@graph`. No question SHALL appear on both the home page and `/support/`.

#### Scenario: An answer engine asks what Migrely is

- **WHEN** a crawler reads the home page
- **THEN** the paragraph under "What is Migrely?" names it a migraine and headache diary for Android, says it has no
  account and no internet permission, and says what is free and what Premium adds, in 40 to 60 words

#### Scenario: A visitor wonders whether it needs the internet

- **WHEN** a visitor reads "Questions before you install"
- **THEN** they find whether it is free, whether it needs the internet, whether it is only for Android, whether the
  diary is backed up and what Premium adds, and a link to the help page

#### Scenario: The same question on both pages

- **WHEN** a question in the home page's list is also in `/support/`'s list
- **THEN** the build stops and names the question

### Requirement: Breadcrumbs

Every sitemap page except the home page SHALL show a breadcrumb navigation, "Home › page name", above its H1, and publish the same trail as a `BreadcrumbList`. Both SHALL come from one list of page names.

#### Scenario: A visitor on a guide

- **WHEN** a visitor opens `/medication-overuse-headache/`
- **THEN** a navigation labelled "Breadcrumb" shows a link to Home, then "Medication-overuse headache" marked as the
  current page

#### Scenario: Search engines read the trail

- **WHEN** a crawler parses the page's JSON-LD
- **THEN** it finds a `BreadcrumbList` with Home at position 1 and the page at position 2, with absolute URLs

### Requirement: Structured data on every page

Every sitemap page SHALL publish one JSON-LD `@graph` with the `Organization` and a node for the page itself: `WebPage` or a more specific type (`AboutPage`, `FAQPage`, `MedicalWebPage`). The page node SHALL name the page and link its breadcrumbs. Pages that say "Written by Migrely" SHALL name the `Organization` as `author`. No reviewer, author or rating SHALL be stated that the page does not show.

#### Scenario: The privacy policy

- **WHEN** a crawler parses `/privacy/`
- **THEN** it finds a `WebPage` named "Migrely privacy policy" whose publisher is the `Organization`

#### Scenario: A medical guide

- **WHEN** a crawler parses `/headache-diary/`
- **THEN** it finds a `MedicalWebPage` with `lastReviewed`, with the `Organization` as author and publisher, and no
  `reviewedBy`

### Requirement: Steps as structured data

`/privacy-check/` and `/delete-data/` SHALL publish their numbered steps as a `HowTo`. Each `HowToStep` SHALL be built from the same text as the visible list, so the two can't differ. The phone check SHALL give `totalTime` as two minutes, as the page says.

#### Scenario: Checking the permission list

- **WHEN** a crawler parses `/privacy-check/`
- **THEN** it finds a `HowTo` whose four steps are the four steps of "How can I check it on my phone?", in order

#### Scenario: A step is reworded

- **WHEN** a step's text changes in `website/build.py`
- **THEN** the visible list and the `HowTo` both change

### Requirement: A site summary for AI tools

The site SHALL serve `/llms.txt` in the llmstxt.org format. It SHALL include the site name, the 40–60-word definition, the app's facts (no internet permission, no account, what is free, what Premium adds and costs, Android 8 and later, English only, not a medical device), and every sitemap page with one sentence. It SHALL be built from the same lists as the pages.

#### Scenario: A new guide

- **WHEN** a guide is added to `GUIDES`
- **THEN** it is listed in `/llms.txt` with its summary at the next build

### Requirement: The stated Android version matches the app

The build SHALL read `minSdk` from `gradle/libs.versions.toml` and stop when it differs from the value behind the site's "Android 8 and later".

#### Scenario: minSdk is raised

- **WHEN** `minSdk` changes from 26 to 28 and the site is built
- **THEN** the build stops, says the site still states Android 8, and writes nothing
