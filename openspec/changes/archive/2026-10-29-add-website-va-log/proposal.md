# Proposal

## Why

US veterans with migraine are often told to keep a log for a VA disability claim. VA rates migraine (diagnostic code
8100) by how often "prostrating" attacks happen over several months, which is exactly what a day-by-day diary
records. Migrely already records an attack's start, end, peak pain, medicines and "Impact on your day", including
"Bed rest", and exports them free as a spreadsheet. A plain, honest page meets those readers where they search.
Task T18 of the launch plan. The rules are BRD D143 and EC-WEB-15…16.

## What Changes

- **`/va-migraine-log/`** (D143):
  - **First, a notice:**
    - Migrely is not affiliated with or endorsed by VA;
    - this is general information, not legal or medical advice;
    - no log or app can promise a rating;
    - accredited VSO representatives help for free.
  - **DC 8100's four levels, quoted from 38 CFR 4.124a** with the date read.
  - **VA's own definition of "prostrating"** from its headaches questionnaire (DBQ, July 2024).
  - **What a log should record.**
  - **What Migrely records:** the CSV export is free; the PDF report is Premium; the labels are the user's own record,
    and VA decides what counts as prostrating.
  - **Where to find free accredited help,** then urgent help, Sources, "Last reviewed", and a `WebPage` JSON-LD.
- **`reviewed_block()`** takes the page's own disclaimer. The medical pages keep theirs.

## Out of scope

- Legal strategy, case law, rating percentages beyond the regulation's text, and any claim-filing guidance.
- Any app change.

## Capabilities

### Modified Capabilities

- `website`: adds the VA migraine log page.

## Impact

- `website/build.py` and `website/public/` (the new page, the sitemap), `website/lastmod.json` and
  `website/README.md`.
- `docs/BRD.md`: D143, EC-WEB-15…16, §13 row 20, §14.4.
