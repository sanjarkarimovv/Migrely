## ADDED Requirements

### Requirement: Menstrual migraine page

The site SHALL serve `/menstrual-migraine/`. It opens with a short answer (attacks from day −2 to day +3 of menstruation, in at least 2 of 3 cycles; pure when only then, menstrually-related when also at other times; a doctor confirms it), shows the five days, says how long to track, and gives NICE's contraception advice for migraine with aura as general information, with no drug or dose named.

#### Scenario: A visitor asks what menstrual migraine is

- **WHEN** a visitor opens `https://migrely.health/menstrual-migraine/`
- **THEN** the first paragraph gives the −2 to +3 window (day 1 is the first day of bleeding, no day 0), the 2-of-3
  cycles rule, the two kinds, and that a doctor confirms it

#### Scenario: On the pill

- **WHEN** a reader takes a combined pill or cyclical HRT
- **THEN** the page says that withdrawal bleeds count as a period for this purpose

#### Scenario: Migraine with aura and contraception

- **WHEN** a reader with aura reads about contraception
- **THEN** the page says NICE advises doctors not to routinely offer combined hormonal contraception for contraception
  to people who have migraine with aura, and tells the reader to tell whoever prescribes their contraception
- **AND** it names no drug or dose for treating menstrual migraine

#### Scenario: Period data stays private

- **WHEN** a reader asks whether Migrely shares period data
- **THEN** the page says Migrely has no internet access or account, so it cannot send period days anywhere, and names
  where they are: the phone, Android's own backup if used, and files the user exports
- **AND** it says what the app shows: period marks are free, and Premium Insights counts attacks that started in the
  window without drawing a conclusion

### Requirement: Link to the menstrual migraine page

`/headache-diary/` SHALL link to `/menstrual-migraine/` where it mentions period-linked headaches. The page SHALL be in the sitemap, and its Play button SHALL carry `utm_campaign=menstrual-migraine`.

#### Scenario: From the diary page

- **WHEN** a visitor reads "How long should I keep it?" on `/headache-diary/`
- **THEN** a link takes them to `/menstrual-migraine/`
