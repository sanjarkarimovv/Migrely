# Proposal

## Why

#200 (S3) holds three safety-copy follow-ups that #194 (BRD D171, release 1.2.1) left out on purpose:

1. **The first-10 card names the sign differently from the page.** The card (SAF-F2) says "If this pain came on suddenly and is the
   worst of your life, call emergency services now." Help › Warning signs says "Extremely bad pain that came on suddenly" and "your
   local emergency number". The NHS migraine page says "have a headache that came on suddenly and is extremely painful". "Worst
   ever" comes only from MedlinePlus, and there it is on the "seek medical help right away" list, not the 911 one. "The worst of your
   life" is also a narrower condition: a migraine patient who has had worse may read a sudden 10 as "not me".
2. **The rash sign relies on a glass test that is harder to read on brown or black skin.** The NHS meningitis page adds the note
   "(this may be harder to see on brown or black skin)" to the rash in its list of meningitis symptoms, and again under its rash
   pictures. Its "Call 999" list says only "has a rash that does not fade when you press it". Migrely's audience is worldwide.
3. **Numbness alone.** The NHS migraine page counts it as aura; the NHS headaches page lists it under 999. D171 left it out. It
   needs an explicit decision so that it does not drift in or out by accident.

Safety comes first in the team's risk order. These are not on a release branch, so the release-scope rule of D171 (4) does not
bind them.

## What Changes

- **SAF-F2 card:** the title becomes "Sudden, extremely bad pain?" and the sentence becomes "Call your local emergency number now
  if this is extremely bad pain that came on suddenly." It repeats the page's first sign word for word, sends the person to the
  same "local emergency number", and leads with the action. When the card shows, its layout, "Got it" and scroll-into-view (D167)
  do not change.
- **Warning signs, rash sign:** "A rash that doesn't fade when you press a glass against it (this may be harder to see on brown or
  black skin)". The bracket is the NHS meningitis page's own words.
- **Numbness alone:** stays off the page until the clinician answers (T31). No string changes; the spec now says why and what
  would change it.
- **A test guards the match:** it fails if the card's sentence stops containing the page's first sign, and on the old text.
- **The pending `fix-safety-card-into-view` spec** (1.1.1, not yet archived) quoted the old card text in two scenarios. They now
  say "the card's title and sentence", so archiving it later cannot bring the old words back into the main spec.
- **The site follows:** it carries the Warning signs text word for word (#199), so `aso-seo-specialist` adds the rash note there
  and in the printable diary. The owner deploys.

## Out of scope

- When the card shows, its colours, its "Got it" button, the D167 scroll and the D165 message position.
- A link from the card to the Warning signs page, a dial button or any country's number (D95).
- Where to look for a rash on darker skin (palms, soles, roof of the mouth, eyes). It would triple the item, and the NHS symptom
  bullet itself carries only the short note. A question for the clinician (T31).
- Numbness alone as an emergency sign (T31).
- Any other Warning signs, About or 72-hour string.
- `docs/release/whats-new.md` for the next release: the release owner decides.
- Clinician sign-off (T31). Nothing may say "medically reviewed".
- Other languages: the app is English only (D148).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `attack-details`: "First maximum pain safety card" gets the new title and sentence, the same-words rule, and scenarios for
  TalkBack, 200% font, the dark room and an upgrade.
- `app-settings`: "Warning signs" gets the rash note and an explicit numbness-alone scenario.
- `website`: an added requirement that the site's urgent-help section and the printable diary follow the app's Warning signs
  word for word, the rash note included. "Headache diary page" is modified so that "Last reviewed" moves only after a full
  re-check, not whenever a source is added.

## Impact

- `features/attack/ui`: `strings.xml` (`safety_title`, `safety_body`), the KDoc comment in `AttackPainSection.kt` that says the text
  is unchanged, and the `TITLE` constants in three `testDebug` tests.
- `features/settings/ui`: `strings.xml` (`settings_warning_signs_list` item 8) and `WarningSignsContentsTest`.
- No domain, data, database, backup, manifest, permission or Gradle change. Free and Premium alike. Works offline.
- Site (`aso-seo-specialist`): `website/build.py` (`urgent_help`), `website/make_diary_pdf.py`, both PDFs in
  `website/public/headache-diary/`, the four generated pages, and the pages' sources. "Last reviewed" dates move only after a full
  re-check of a page; the PDF footer's "Updated <date>" may move.
- QA: `.maestro/app-settings/sections-and-pages.yaml` (line 45 matches the old rash text) and
  `.maestro/a11y-tour/settings-warning-signs.yaml` if it reads the rash item.
- BRD: SAF-F1, SAF-F2, D173, EC-SAF-05, EC-SAF-10 (pointers), EC-SAF-11…17, §13 row 42.
