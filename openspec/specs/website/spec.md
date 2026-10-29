# website Specification

## Purpose
Defines migrely.health's trust pages and content: `/about/` and `/privacy-check/` with only verifiable claims, a
permission list kept equal to the release build's, the medical pages `/headache-diary/` (with its printable 8-week
template) and `/medication-overuse-headache/` (sources, review dates, shared urgent-help text), links from every page,
structured data, honest sitemap dates, UTM-tagged Google Play links, and the site's static, script-free rules (BRD
D119, D133–D141, EC-WEB-01…12).

## Requirements

### Requirement: About page

The site SHALL serve `/about/`. It states, under question headings, who publishes Migrely, why it keeps entries on the phone, how it is paid for (Premium only, no ads, no data sales), what stays free, what Migrely will never do, which classification its medication note follows (ICHD-3) and how to get in touch. It SHALL state only facts that can be checked, with no invented story, patient, review, credential or number.

#### Scenario: A visitor wants to know who is behind the app

- **WHEN** a visitor opens `https://migrely.health/about/`
- **THEN** the page answers "Who makes Migrely?", "How does Migrely make money?", "What stays free?", "What will
  Migrely never do?", "Where do the medication limits come from?" and "How can I contact you?"
- **AND** it names Sulton UzDev as the Google Play publisher, links to `/privacy-check/` and the ICHD-3 page on
  medication-overuse headache, and ends with a Google Play button

#### Scenario: The medication note's limits

- **WHEN** the page explains the medication note
- **THEN** it gives the limits Migrely uses: 15 or more days a month for simple painkillers, and 10 or more for
  triptans, combination and other acute medicines, counted over the last 30 days
- **AND** it says Migrely suggests talking to a doctor and does not diagnose

### Requirement: Privacy check page

The site SHALL serve `/privacy-check/`. It explains that Migrely has no internet permission, shows how to check this in the phone's settings and with `adb`, says what airplane mode does and does not prove, explains each of the app's permissions in plain words, and names what can still leave the phone without it: Android's own backup, Google Play's purchase and rating windows, and files the user shares.

#### Scenario: Checking on the phone

- **WHEN** a visitor follows the steps Settings → Apps → Migrely → Permissions → ⋮ → All permissions
- **THEN** the page tells them that “have full network access” is not in the list

#### Scenario: The phone's maker uses other names

- **WHEN** a visitor's phone names the settings differently
- **THEN** the page has already said the names can differ between phone makers
- **AND** it offers the `adb shell dumpsys package com.migrely.app` check, which is the same on every phone

#### Scenario: Airplane mode

- **WHEN** a visitor turns on airplane mode and Migrely works as usual
- **THEN** the page says this shows Migrely does not need the internet, while the permission list shows it cannot use
  it

#### Scenario: Honest edges

- **WHEN** a visitor reads what can still leave the phone
- **THEN** the page names Android's own backup, Google Play's purchase and rating windows, and files the user shares,
  as the privacy policy does, and links to `/privacy/`

### Requirement: The permission list matches the release build

The site build SHALL compare the permissions listed on `/privacy-check/` with the release build's permission report (`app/build/reports/permissions/release.txt`) and stop with an error naming both lists when they differ. When the report is missing, it SHALL print a note and build the site.

#### Scenario: The lists match

- **WHEN** the report lists the same five permissions as the page
- **THEN** the site builds

#### Scenario: A library adds a permission

- **WHEN** the release build's report lists a permission that the page does not explain
- **THEN** the site build stops with an error naming both lists, and nothing is deployed with a stale list

#### Scenario: No app build yet

- **WHEN** the report does not exist
- **THEN** the build prints a note telling the reader how to create the report, and builds the site

### Requirement: Links to the trust pages

Every page SHALL have "About" in its main menu, and `/about/` and `/privacy-check/` in its footer. The home page's privacy section SHALL link to `/privacy-check/`.

#### Scenario: From the home page

- **WHEN** a visitor reads "Private by design" on the home page
- **THEN** a "Check it yourself" link takes them to `/privacy-check/`

#### Scenario: From any page

- **WHEN** a visitor is on any page, the 404 page included
- **THEN** the menu links to `/about/`, and the footer links to `/about/` and `/privacy-check/`

### Requirement: Organization structured data

The home page SHALL publish one JSON-LD `@graph` with an `Organization` (Migrely: url, logo, email, contactPoint), a `WebSite` published by it and the `MobileApplication`, whose publisher stays Sulton UzDev. `/about/` SHALL publish an `AboutPage` about the same `Organization`. `sameAs` SHALL list only profiles that exist.

#### Scenario: Search engines read the home page

- **WHEN** a crawler parses the home page's JSON-LD
- **THEN** it finds an `Organization` with `@id` `https://migrely.health/#organization`, a `WebSite` whose publisher
  is that `@id`, and the `MobileApplication`

#### Scenario: No social profiles yet

- **WHEN** Migrely has no social profiles yet
- **THEN** the `Organization` has no `sameAs`

### Requirement: Accurate sitemap dates

The sitemap SHALL give every page except the 404 page a `lastmod`. It is the date the page's HTML last changed, kept with the HTML's SHA-256 hash in `website/lastmod.json`. A build SHALL change a page's date only when its hash changes.

#### Scenario: Rebuilt without changes

- **WHEN** the site is built again and no page changed
- **THEN** every `lastmod` and `website/lastmod.json` stay as they were

#### Scenario: The privacy policy changed

- **WHEN** `docs/release/privacy-policy.md` changes and the site is built
- **THEN** only `/privacy/` gets that day's date

#### Scenario: A new page

- **WHEN** a page is built for the first time
- **THEN** it gets that day's date

### Requirement: Tagged Google Play links

Every Google Play link a visitor can follow on the site SHALL carry a `referrer` parameter holding `utm_source=migrely.health`, `utm_medium=website` and `utm_campaign` set to the page it is on (`home` for `/`, otherwise the path's name). The structured data's `downloadUrl` SHALL stay untagged.

#### Scenario: Installing from the about page

- **WHEN** a visitor taps "Get it on Google Play" on `/about/`
- **THEN** the link is `https://play.google.com/store/apps/details?id=com.migrely.app&referrer=utm_source%3Dmigrely.health%26utm_medium%3Dwebsite%26utm_campaign%3Dabout`
- **AND** the site sets no cookie and runs no script, so only that tag tells Play Console where the visit came from

#### Scenario: Few visits

- **WHEN** a campaign has too few visits for Play Console's threshold
- **THEN** Play Console may show it as "Other". The site does not try to measure it any other way

### Requirement: The trust pages keep the site's rules

The new pages SHALL keep the site's rules (D119): no scripts, cookies or third-party requests, every asset served by the site, and the Content-Security-Policy unchanged. They SHALL read without horizontal scrolling at 320 px wide and at 200% text, in light and dark themes, with long permission names and commands wrapping.

#### Scenario: A small phone with large text

- **WHEN** `/privacy-check/` is shown 320 px wide with text at 200%
- **THEN** the page does not scroll sideways, and the long permission name and the `adb` command wrap inside their
  boxes

#### Scenario: Dark theme

- **WHEN** the visitor's system uses a dark theme
- **THEN** the new pages use the site's dark colours, and their text keeps AA contrast

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
