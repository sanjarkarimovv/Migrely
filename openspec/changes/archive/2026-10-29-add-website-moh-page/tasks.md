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

- [x] 4.1 Independent review. Fix the findings and record them.
- [x] 4.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge.

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

## Review

An independent review (`superpowers:code-reviewer`) of 8c52c39a..11cae0dd read ICHD-3 8.2 to 8.2.8, NICE CG150 and the
NHS live. It confirmed:
- every threshold;
- that the treatment section avoids NICE's "stop abruptly" (1.3.43), which is right for a consumer page;
- the app description against D45 and the app's strings;
- the refactor (the diary page's JSON-LD, Sources and review line are byte-identical);
- the escaping and the build's reproducibility.

There was nothing Critical, 5 Important findings and 9 Minor. All were fixed except M9 (optional style).

**Important:**
- **I1:** the table's second column broke inside words at 320 px ("Days a mon/th"). Fixed with `overflow-wrap:
  normal` on the last column, `hyphens: auto` on the table and smaller cell padding under 400 px. At simulated 200%
  text on 320 px, long words in the first column still wrap, but now mostly at hyphens, and nothing scrolls sideways.
- **I2:** the treatment text now follows NICE 1.3.43, 1.3.44 and 1.3.47 as written: at least a month; headaches
  likely to get worse; withdrawal symptoms; close follow-up and support; strong opioids may need a specialist.
- **I3:** days taking these medicines for other pain count too (ICHD-3 8.2 criterion B), as the template already said.
- **I4:** the lead ends with "simple painkillers alone, one or several".
- **I5:** a row "Any other medicine taken for attacks: 10 or more" (ICHD-3 8.2.8) was added as the last row, and 8.2.8
  joined the Sources.

**Minor:**
- the table caption gives the 3-month duration;
- "How can I keep count?" replaces "getting close";
- the app's note is described in its own words, without quotes;
- the description says "how it is treated";
- the combination row says "in one medicine";
- `medical_ld(…, condition)` no longer shadows `about()` and has a docstring;
- `/about/` says "day counts", not "limits";
- the design table matches the output.

**Not done:**
- M9, deriving the display date from one `date` and moving the MOH constants to the top: optional style.
- The app's indicator labels ("limit 15", "at the limit"): an app change, noted for the dev session.

**Re-verified after the fixes:**
- The static checks find nothing.
- 27 and then 12 overflow measurements show 0 sideways scroll.
- 0 CSP violations.
- The table was screenshotted at 320 px at 100% and simulated 200% text.
