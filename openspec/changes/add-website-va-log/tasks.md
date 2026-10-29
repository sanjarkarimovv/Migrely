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

- [x] 3.1 Independent review. Fix the findings and record them.
- [x] 3.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge.

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

## Review

An independent review (`superpowers:code-reviewer`) of 9b055c7a..581bb880 read the eCFR API (up to date as of
2026-10-27; § 4.124a last amended 2016-12-14), the DBQ, VA's FAQ and the Federal Register. It confirmed:
- the four DC 8100 levels and the DBQ definition match word for word;
- the notice comes first and is complete;
- there is no VA seal or "official" wording;
- the app claims match the strings, `AttacksCsvMapper` and D108;
- the medical pages render the same text.

It found nothing Critical, 5 Important issues and 7 Minor. All were fixed except the ones listed under "Not done".

**Important:**
1. The page summarised all of DC 8100 as "how often". Now the 10% and 30% levels are tied to frequency, and the 50%
   level also to "completely prostrating and prolonged" attacks and "severe economic inadaptability". The description
   was rewritten.
2. "Your own words" became "You choose these labels for your own record. They are Migrely's words, not VA's terms".
3. The severe-only example ("lying down in a dark room or leaving work") became a neutral range: "carried on, slowed
   down, stopped, or lay down". "And the examiner" was dropped.
4. The notice now says Migrely is not a VA-accredited representative and cannot help prepare or file a claim. "How can
   Migrely help?" became "What does Migrely record?".
5. The broken README sentence was split into a medical-pages item and a VA-page item.

**Minor:**
- The design records the NPRM (RIN 2900-AQ73, 89 FR 88917, 12 November 2024). It restates DC 8100 unchanged, and its
  final action is planned for April 2027. The BRD and the README say to check it before updating `VA_REVIEWED`.
- "Byte-identical" became "renders the same text".
- The DBQ is described as "the form doctors complete for VA".
- The source labels give the section's real title and the FAQ's date.
- "Export for Excel, CSV" is named.
- `urgent_help(extra)` adds the Veterans Crisis Line (988, then 1; text 838255) on this page only, and its docstring
  is updated.
- The FAQ's fee facts were added: attorneys and claims agents may charge once VA has decided the initial claim.

**Not done:**
- A link to this page from `/headache-diary/`. It stays reachable through the sitemap; deciding where a link fits
  belongs to a later content review.
- Folding the `WebPage` JSON-LD into `medical_ld()`. With two types, a single helper would need a type switch for one
  use.

**Re-verified after the fixes:**
- The quotes still match. The normalised text contains the DBQ definition and the 50% wording.
- The static checks find nothing, and two builds in a row change nothing.
- 9 overflow measurements show 0 sideways scroll, and there are 0 CSP violations.
- The three medical pages render the same text as on `develop`.
