# Tasks

## 1. Page

- [x] 1.1 `menstrual_migraine()` with `MENSTRUAL_REVIEWED`, the day table, the Play button and the shared helpers
  (D1, D2). Add it to `PAGES`.
- [x] 1.2 Link from `/headache-diary/`.
- [x] 1.3 `website/README.md`: the new page in the check list and the medical pages note.

## 2. Verify

- [x] 2.1 Static checks, headless Chrome (CSP, 320 px in light, dark and 200% text), and a stable rebuild.
- [x] 2.2 Every medical sentence compared with ICHD-3 A1.1.1/A1.1.2, NICE CG150 and the Migraine Trust. Every app
  sentence compared with D91 and the app's strings.
- [x] 2.3 `openspec validate add-website-menstrual-migraine --strict`, and the verification record.

## 3. Review and archive

- [ ] 3.1 Independent review. Fix the findings and record them.
- [ ] 3.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge.

## Verification record

Run on 2026-10-29 at `feature/add-website-menstrual-migraine`, built from `develop` 01a8202f.

- **Build:** the site builds with 9 sitemap pages, and two builds in a row leave `lastmod.json` unchanged.
- **Static checks** found nothing:
  - the title is 53 characters and the description 146 (first 162, then shortened);
  - links resolve;
  - every Play link carries its campaign (`menstrual-migraine`);
  - the JSON-LD is a `MedicalWebPage` about "Menstrual migraine".
- **Headless Chrome:**
  - 0 CSP violations.
  - No sideways scroll at 320 px in light, dark and 200% text.
  - Screenshot 24 is in `migrely-work/walks/website/`.
- **Medical wording,** compared with the sources:
  - ICHD-3 A1.1.1 and A1.1.2, read live: the window, no day 0, 2 of 3 cycles, the two kinds, withdrawal bleeds from
    combined pills or cyclical HRT, and appendix criteria;
  - NICE CG150, read live: 1.2.5 ("predominantly", 2 of 3 consecutive cycles), 1.2.6 (2 cycles), 1.3.30 and 1.3.31.
    No drug or dose is named;
  - the Migraine Trust, from the launch's checked fact pack, because its page answers 403 to scripts: 3 months,
    fewer than 1 in 100 pure, up to two-thirds period-related.

  The check removed one unsourced sentence ("Usual migraine treatment often works").
- **App wording against D91 and the app's strings:**
  - the same 5-day window;
  - Insights is Premium and appears after 2 cycles and 5 migraines;
  - it counts attacks that started in the window, next to the share of days, without drawing a conclusion;
  - "Track periods" is optional.
- **OpenSpec:** `openspec validate add-website-menstrual-migraine --strict` passes.
- **Not run:** the Android gate (no Gradle input changed), the live site, and a clinician's review (the page says so).
