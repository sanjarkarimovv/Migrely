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

- [x] 3.1 Independent review. Fix the findings and record them.
- [x] 3.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge.

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
  - the Migraine Trust: its page answers 403 to scripts, so it was read from the Wayback Machine copy of 18 September
    2026, which the review also read live with WebFetch. It gives 3 months, fewer than 1 in 100 "are thought to have"
    pure, up to two-thirds period-related, the advice on aura and combined contraception, and that medicine days for
    menstrual migraine count toward overuse.

  The check removed one unsourced sentence ("Usual migraine treatment often works").
- **App wording against D91 and the app's strings:**
  - the same 5-day window;
  - Insights is Premium and appears after 2 cycles and 5 migraines;
  - it counts attacks that started in the window, next to the share of days, without drawing a conclusion;
  - "Track periods" is optional.
- **OpenSpec:** `openspec validate add-website-menstrual-migraine --strict` passes.
- **Not run:** the Android gate (no Gradle input changed), the live site, and a clinician's review (the page says so).

## Review

An independent review (`superpowers:code-reviewer`) of ea634f4d..204bad5c read ICHD-3 A1.1.1, A1.1.2 and the appendix
introduction, NICE CG150 and the Migraine Trust live. It also checked the app claims against `InsightsCalculator`, D88,
D91, D108 and the strings. It found nothing Critical and no false sentence, but 4 Important gaps and 9 Minor.

**Important, all fixed:**
- **I1:** Android's backup goes to the Google account. The page now says period data can leave the phone only in that
  backup (if on, end-to-end encrypted on Android 9+ with a screen lock) and in files the user exports and shares. It
  also says that turning tracking off hides the marks but keeps them.
- **I2:** aura and contraception now follow NICE 1.3.30 and the Migraine Trust:
  - the combined pill, patch or ring;
  - other stroke risk factors;
  - tell the prescriber soon, including when aura first appears while using one;
  - a stopped method no longer protects from pregnancy.

  The page does not say "don't stop it on your own": secondary sources report FSRH advice to stop combined methods
  when aura appears.
- **I3:** standard migraine medicines can be used (Migraine Trust), and the days you take them count toward
  medication-overuse headache, with a link.
- **I4:** the withdrawal bleed is "in the break between packs of a combined pill, or during cyclical HRT".

**Minor, all fixed:**
- the Migraine Trust's hedge ("are thought to have") is restored;
- the ICHD-3 appendix criteria are for migraine without aura;
- NICE's umbrella term is named as NICE's;
- the Insights condition reads "the months you look at include at least 2 period starts and 5 migraines";
- the table rows are complete, with `scope` row and column headers, on the MOH table too;
- day 1 is in the lead;
- the design describes the table correctly;
- the constants are at the top, and the title is built from the H1;
- the NHS "see a GP" line was added, with the NHS in the Sources.

**Re-verified after the fixes:**
- The static checks find nothing, and two builds in a row change nothing.
- 30 overflow measurements (10 pages; light, dark and 200% text) show 0 sideways scroll.
- 0 CSP violations on 10 pages.
