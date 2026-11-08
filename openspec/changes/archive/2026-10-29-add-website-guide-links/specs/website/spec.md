## ADDED Requirements

### Requirement: Links to every guide

The home page SHALL have a "Free guides" section with a card for each guide in `GUIDES`: a link with the guide's name and a one-sentence summary. Every page's footer SHALL have a navigation labelled "Guides" that links to each guide, with the current page marked `aria-current="page"`. The VA guide's summary SHALL say "Migrely is not affiliated with VA".

#### Scenario: From the home page

- **WHEN** a visitor scrolls the home page
- **THEN** a "Free guides" section links to `/headache-diary/`, `/medication-overuse-headache/`,
  `/menstrual-migraine/`, `/va-migraine-log/` and `/compare/`, each with a sentence saying what it covers

#### Scenario: From any page

- **WHEN** a visitor is on any page of the site, including the 404 page
- **THEN** the footer's "Guides" navigation links to all five guides

#### Scenario: On a guide

- **WHEN** a visitor is on `/compare/`
- **THEN** the footer's "Compare migraine apps" link is marked `aria-current="page"`

#### Scenario: A screen reader user

- **WHEN** a screen reader lists the page's landmarks
- **THEN** it announces two navigations, "Main" and "Guides"

### Requirement: No orphan pages

The build SHALL stop and name the page when a page in the sitemap can't be reached from the home page by following `<a href>` links between the site's pages. Links in HTML comments SHALL NOT count.

#### Scenario: A new page without links

- **WHEN** a page is added to `PAGES` but no other page links to it
- **THEN** the build stops with a message naming that page (EC-WEB-19)

#### Scenario: Two pages that link only to each other

- **WHEN** two new pages link to each other but no reachable page links to either
- **THEN** the build stops and names both

#### Scenario: A new guide

- **WHEN** a guide is added to `GUIDES`
- **THEN** the home page and every footer link to it, and the check passes
