# Tasks

## 1. Page

- [x] 1.1 Helpers `urgent_help()`, `reviewed_block()` and `medical_ld()`. Move `/headache-diary/` onto them (D2).
  Verify: `/headache-diary/` text is unchanged apart from the new link.
- [x] 1.2 `medication_overuse()` with `MOH_REVIEWED`, the table and the Play button. Add it to `PAGES` (D1).
- [x] 1.3 Links from `/headache-diary/` and `/about/` (D4).
- [x] 1.4 `.prose table` style (D3).

## 2. Docs

- [x] 2.1 `website/README.md`: the medical pages and their review dates.

## 3. Verify

- [x] 3.1 Static checks: titles, descriptions, canonicals, links, campaigns and JSON-LD.
- [x] 3.2 Headless Chrome behind the CSP: no violations, and no sideways scroll at 320 px in light, dark and 200%
  text.
- [x] 3.3 Every medical sentence compared with ICHD-3 8.2, NICE CG150 and the NHS.
- [x] 3.4 `openspec validate add-website-moh-page --strict`.
- [x] 3.5 Verification record.

## 4. Review and archive

- [ ] 4.1 Independent review. Fix the findings and record them.
- [ ] 4.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge.

## Verification record

Run on 2026-10-29 at `feature/add-website-moh-page`, built from `develop` 06bb1be6.

- **Refactor:** after `/headache-diary/` moved onto the shared helpers, its HTML differs from before in only three
  places:
  - the stylesheet hash;
  - the new "More on medication-overuse headache" link;
  - the shared urgent-help sentence, which no longer says "page 1 of the template prints them". The template section
    still says so.
- **Build:** the site builds with 8 sitemap pages, and two builds in a row leave `lastmod.json` unchanged.
- **Static checks** found nothing:
  - the title is 52 characters, the description 143, and the canonical is correct;
  - links resolve;
  - every Play link carries its campaign (`medication-overuse-headache`);
  - the JSON-LD is a `MedicalWebPage` about "Medication-overuse headache" (alternate name "Rebound headache"),
    with `lastReviewed` 2026-10-29.
- **Headless Chrome:**
  - 0 CSP violations on the new page, `/headache-diary/` and `/about/`.
  - No sideways scroll at 320 px in light, dark and 200% text. The table wraps.
  - Screenshots 22–23 are in `migrely-work/walks/website/`.
- **Medical wording,** compared with the sources read on 2026-10-29:
  - **ICHD-3 8.2:** the description, the criteria, "previously used terms: … rebound headache", "usually, but not
    invariably, resolves", and both diagnoses alongside chronic migraine;
  - **ICHD-3 8.2.3, 8.2.5 and 8.2.6:** simple painkillers are one class; combination means two or more classes or
    an adjuvant; a mix of classes counts toward 10;
  - **NICE CG150:** 1.2.7, 1.3.6 and 1.3.42–1.3.48;
  - **NHS:** 2 days a week.

  The check corrected one row: "Combination painkillers: two or more drugs" became "drugs of two or more kinds…",
  because aspirin with paracetamol and no adjuvant is not a combination painkiller.
- **App claims:** they match BRD D45: the last 30 days, a note 2 days before and one at the count, and it is free.
- **OpenSpec:** `openspec validate add-website-moh-page --strict` passes.
- **Not run:** the Android gate (no Gradle input changed), the live site (the owner deploys), and a clinician's review
  (the page says so).
