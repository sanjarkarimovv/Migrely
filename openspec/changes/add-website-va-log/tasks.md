# Tasks

## 1. Page

- [x] 1.1 `reviewed_block(…, disclaimer)` with `MEDICAL_DISCLAIMER` and `VA_DISCLAIMER` (D2). Verify that the
  medical pages' HTML is unchanged.
- [x] 1.2 `va_migraine_log()` with `VA_REVIEWED`, the DC 8100 table, the notice, the Play button and the `WebPage`
  JSON-LD (D1, D3). Add it to `PAGES`.
- [x] 1.3 `website/README.md`.

## 2. Verify

- [x] 2.1 Static checks, headless Chrome (CSP, 320 px in light, dark and 200% text), and a stable rebuild.
- [x] 2.2 The regulation, DBQ and FAQ quotes compared word for word with the sources. The app sentences compared with
  `AttacksCsvMapper`, the strings and D108.
- [x] 2.3 `openspec validate add-website-va-log --strict`, and the verification record.

## 3. Review and archive

- [ ] 3.1 Independent review. Fix the findings and record them.
- [ ] 3.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge.

## Verification record

Run on 2026-10-29 at `feature/add-website-va-log`, built from `develop` 5ca2bea1.

- **Shared block:** after `reviewed_block()` gained the disclaimer parameter, the three medical pages render the same
  text. Their HTML differs only by a line break inside the review line, so their `lastmod` hashes moved (to the same
  date).
- **Build:** the site builds with 10 sitemap pages, and two builds in a row leave `lastmod.json` unchanged.
- **Static checks** found nothing:
  - the title is 53 characters, the description 152 (first 163, then shortened);
  - links resolve;
  - every Play link carries its campaign (`va-migraine-log`);
  - the JSON-LD is a `WebPage` with `lastReviewed`.
- **Headless Chrome:**
  - 0 CSP violations.
  - 33 measurements (11 pages; light, dark and 200% text) show 0 sideways scroll.
  - The first render broke "Rating" inside the word. `table.short-first` keeps the first column whole.
  - Screenshot 25 is in `migrely-work/walks/website/`.
- **Quotes,** compared word for word:
  - DC 8100 from the eCFR API's current text of 38 CFR 4.124a (no 2026 change found);
  - the DBQ's Section IV definition (PDF updated 2024-07-09);
  - VA's FAQ: "The services that an accredited VSO representative provides on your VA benefit claim are always free",
    and the "Find a VA-accredited representative or VSO" link.
- **App wording:**
  - the "Impact on your day" labels, from the attack strings;
  - the CSV columns, from `AttacksCsvMapper.HEADER`: one row per attack, free;
  - the PDF report is Premium (D108).
- **OpenSpec:** `openspec validate add-website-va-log --strict` passes.
- **Not run:** the Android gate (no Gradle input changed), the live site, and a lawyer's or VA's review (the page says
  so).
