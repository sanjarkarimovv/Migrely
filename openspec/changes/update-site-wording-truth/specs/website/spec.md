## ADDED Requirements

### Requirement: Truthful wording on every page

Every page, `/llms.txt` and image alt text SHALL say what Migrely does, not how the reader will feel: no "calm" or "quiet" for Migrely or its sky, and trigger wording SHALL keep "often logged with". The privacy claim SHALL say Migrely has no internet permission, so the app cannot send entries over the internet, never "no internet access" or "nothing leaves". What is free SHALL be listed with no time promise (BRD D179).

#### Scenario: The privacy claim

- **WHEN** any page or `/llms.txt` says why entries stay private
- **THEN** it says Migrely has no internet permission, so the app itself cannot send them over the internet
- **AND** it never says "no internet access", "nothing leaves your phone" or that nothing can leave through the app
- **AND** where it lists what can leave the phone, it names Android's own backup to the Google account, if backup is
  on, and files the user exports

#### Scenario: No promise of calm

- **WHEN** a page, its JSON-LD description or an alt text describes Migrely or the month sky
- **THEN** it says "a private migraine diary" and "each month drawn as a night sky", with no "calm" or "quiet" and no
  "while you rest"
- **AND** the only exception is the promo video's text alternative, which follows the video actually shown

#### Scenario: Triggers keep their hedge

- **WHEN** a heading or alt text names what Insights shows
- **THEN** it says what is "often logged together" or "often logged with migraines", never what the migraines
  "share", what causes them or a trigger the reader will "spot"

#### Scenario: What stays free

- **WHEN** a page says what is free
- **THEN** it lists logging, the history, medications, reminders, backup and spreadsheet export and every safety
  warning, and says Premium adds only the doctor report and Insights
- **AND** it makes no time promise such as "for good", and it never calls every export free, because the PDF doctor
  report is Premium

#### Scenario: The promo video is older than the page text

- **WHEN** the page text has new wording but the promo video still shows the old words on screen
- **THEN** the home page's text alternative keeps describing the video that is actually shown until the new video
  replaces it under a new file name
- **AND** the old video is never placed on `/android/` or in a paid placement

#### Scenario: Second person on organic pages

- **WHEN** an organic page or store art says "your migraines" with the hedge kept
- **THEN** the wording is allowed
- **AND** `/android/` still stops the build on any `BANNED_AD` phrase, including "your migraine", "your month" and
  "while you rest"

### Requirement: Changed images get new names

When a screenshot, the link-preview image or the video changes, the site SHALL publish it under a new versioned name and SHALL never overwrite a file under `/assets/`, because `/assets/` is cached for a year as immutable. The store PNG keeps its Google Play name, and the site derives its source by dropping the `-vN` suffix. Old files stay so cached pages and shared previews still load (BRD D179, EC-WEB-25).

#### Scenario: A store caption changes

- **WHEN** slide 02's caption is re-rendered into `docs/release/store/screenshots/02-sky.png`
- **THEN** the build writes `02-sky-v3.webp` from `02-sky.png` and the home page links only the new name
- **AND** `02-sky-v2.webp` stays in `/assets/img/` unlinked

#### Scenario: The link preview changes

- **WHEN** the feature graphic changes
- **THEN** the build writes the link preview as `og-v2.png` and every page's `og:image` points to it
- **AND** a preview that a social app cached from `og.png` still loads

#### Scenario: A visitor with a cached home page

- **WHEN** a browser holds last week's home page and its images in cache
- **THEN** it shows the old page with the old images, all of which still load, and the new page loads the new names
  with no mixed caption

## MODIFIED Requirements

### Requirement: Menstrual migraine page

The site SHALL serve `/menstrual-migraine/`. It opens with a short answer (attacks from day −2 to day +3 of menstruation, in at least 2 of 3 cycles; pure when only then, menstrually-related when also at other times; a doctor confirms it), shows the five days, says how long to track, and gives NICE's contraception advice for migraine with aura as general information, with no drug or dose named.

#### Scenario: A visitor asks what menstrual migraine is

- **WHEN** a visitor opens `https://migrely.health/menstrual-migraine/`
- **THEN** the first paragraph gives the −2 to +3 window (day 1 is the first day of bleeding, no day 0), the 2-of-3
  cycles rule, the two kinds, and that a doctor confirms it

#### Scenario: On the pill

- **WHEN** a reader takes a combined pill or cyclical HRT
- **THEN** the page says that the withdrawal bleed between packs, or during cyclical HRT, counts as a period for this
  purpose

#### Scenario: Migraine with aura and contraception

- **WHEN** a reader with aura reads about contraception
- **THEN** the page says NICE advises doctors not to routinely offer combined hormonal contraception for contraception
  to people who have migraine with aura, and tells the reader to tell whoever prescribes their contraception soon,
  including when aura appears for the first time while using it
- **AND** it says that a stopped method no longer protects from pregnancy
- **AND** it names no drug or dose for treating menstrual migraine

#### Scenario: Period data stays private

- **WHEN** a reader asks whether Migrely shares period data
- **THEN** the page says Migrely has no internet permission and no account, so the app itself cannot send period days
  over the internet, and that they can leave the phone only in Android's own backup to the user's Google account (if
  backup is on) and in files the user exports and shares
- **AND** it says what the app shows: period marks are free, and Premium Insights counts attacks that started in the
  window without drawing a conclusion, once the months shown include at least 2 period starts and 5 migraines and the
  first migraine was at least 30 days ago
