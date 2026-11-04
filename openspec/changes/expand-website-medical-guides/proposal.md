# Proposal

## Why

The 2026-11-04 audit found the three medical guides short: 650–790 words, while a pillar page needs about 1,500. Many
of their sections also do not open with a direct answer that an answer engine can quote.

Three research agents read the primary sources and checked every quote word for word against the source text:
- 127 quotes for `/headache-diary/`;
- 80 for `/medication-overuse-headache/`;
- 168 for `/menstrual-migraine/`.

They are kept in `marketing/research/2026-11-04-*-facts.md`, outside the repo. The rules are BRD D149, EC-WEB-23 and
EC-WEB-24.

## What Changes

- **`/headache-diary/`** gets four new sections:
  - which possible triggers to note, with the caution that a trigger seen with an attack is not proof;
  - how to keep a diary going;
  - what to do with more than one kind of headache;
  - what a doctor looks for in a diary, including how to sum it up before an appointment.
- **`/medication-overuse-headache/`** gets five new sections:
  - how common it is and who gets it;
  - the signs;
  - how to lower the risk;
  - how long headaches get worse after stopping;
  - whether it goes away and can come back.
- **`/menstrual-migraine/`** gets four new sections:
  - why it happens;
  - whether period attacks are worse;
  - what to bring to an appointment;
  - perimenopause and menopause, with a sub-question on HRT.
- **Every question heading** on these pages opens with a 40–60-word direct answer. The build checks this; the
  unchanged app and privacy sections are exempt.
- **`urgent_help()`** gives the NHS migraine page's whole 999 list, on every medical page and the VA page.
- **The source lists grow.** The review dates move to 4 November 2026, and the diary PDFs are made again with the new
  date.

## Out of scope

- A clinician reviewer: the pages keep saying "not reviewed by a clinician".
- Named drugs with doses, how to stop a medicine, pregnancy (cut as off-topic for a period page), and other languages.
- The app sections and the privacy section, which stay word for word.

## Capabilities

### Modified Capabilities

- `website`: deeper medical guides with direct answers and the full urgent-help list.

## Impact

- `website/build.py`, `website/public/` (the three guides, `/va-migraine-log/` for `urgent_help()`, the diary PDFs),
  `website/lastmod.json` and `website/README.md`.
- `docs/BRD.md`: D149, EC-WEB-23, EC-WEB-24, §13 row 25, §14.4.
