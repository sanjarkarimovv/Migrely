# Tasks

## 1. Template and PDFs

- [x] 1.1 `website/make_diary_pdf.py`: the six pages (D2), each size printed with headless Chrome (D3).
  Verify: `pdfinfo` shows 6 pages, landscape A4 (842×595 pt) and Letter (792×612 pt), each under 300 KB.
  `pdftotext -layout` shows every heading and footer.
- [x] 1.2 A visual check of every page of both PDFs at print size, rendered to PNG, and in greyscale (EC-WEB-07).

## 2. Page

- [x] 2.1 `headache_diary()` in `build.py`, with `DIARY_REVIEWED`, `MedicalWebPage` JSON-LD, the download links and
  the Play button (D1). Add it to `PAGES`. Stop the build if a PDF is missing.
  Verify: `python3 website/build.py` builds; with a PDF moved away, it stops with the pointer.
- [x] 2.2 Footer: "Free headache diary" on every page.
- [x] 2.3 `site.css`: `.play.alt` and `.meta` (D4).

## 3. Docs

- [x] 3.1 `website/README.md`: the diary page and how to remake the PDFs.

## 4. Verify

- [x] 4.1 Static checks over `website/public`, as in change 16:
  - titles, descriptions, canonicals and one `h1`;
  - internal links, the PDFs included;
  - no running script;
  - every Play link with its campaign;
  - the JSON-LD.
- [x] 4.2 Headless Chrome behind the CSP: no violations, and no sideways scroll at 320 px in light, dark and 200%
  text.
- [x] 4.3 Every medical sentence on the page compared with its source (NICE CG150, ICHD-3, NHS, Migraine Trust).
- [x] 4.4 `openspec validate add-website-headache-diary --strict`. No Gradle input changes, so the Android gate is not
  run.
- [x] 4.5 Verification record.

## 5. Review and archive

- [x] 5.1 Independent review of the diff, the PDFs and the medical wording. Fix the real findings and record them.
- [x] 5.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge.

## Verification record

Run on 2026-10-29 at `feature/add-website-headache-diary`, built from `develop` e640c546.

- **PDFs:**
  - `pdfinfo` shows 6 pages each: A4 at 841.92×594.96 pt and Letter at 792×612 pt, both landscape.
  - Each file is 84 KB after the review fixes.
  - The title is "Headache diary · 8 weeks · Migrely".
  - `pdftotext -layout` shows every heading and footer.
- **Template:**
  - All 6 A4 pages and 2 Letter pages were looked at as PNGs, and the Letter ones also in greyscale. Nothing is clipped,
    and the lines and labels stay readable without colour.
  - The first render caught two layout problems: the "Headache?" heading touched its neighbour, and the key's first
    column was too wide. Both were fixed.
  - It also caught "V vision changes" among the symptom codes. It was removed, because loss of vision is an
    emergency sign, not a diary code.
  - "any mix of them" was changed to "any mix of acute medicines (simple painkillers included)", per ICHD-3 8.2.6.
- **Build:**
  - The site builds with 7 sitemap pages.
  - With the Letter PDF moved away, it stops with "Missing migrely-headache-diary-letter.pdf: run python3
    website/make_diary_pdf.py first." (exit 1).
- **Static checks** found nothing:
  - `/headache-diary/` has a 49-character title, a 157-character description and a correct canonical;
  - every internal link resolves, the PDFs included;
  - every Play link carries its campaign, here `headache-diary`;
  - the JSON-LD is a `MedicalWebPage` with `lastReviewed` 2026-10-29;
  - every page links to `/headache-diary/` in its footer.
- **Headless Chrome:**
  - 320 px in light, dark and 200% text: no sideways scroll on `/`, `/headache-diary/`, `/about/` or `/support/`.
  - 0 CSP violations.
  - The PDF is served as `application/pdf`.
  - Screenshots 20–21 are in `migrely-work/walks/website/`.
- **Medical wording,** compared sentence by sentence:
  - NICE CG150 1.1.3, 1.1.4, 1.2.6 and 1.3.1;
  - ICHD-3's definition of headache days, its 1.3 note on the day-by-day diary, and 8.2 with 8.2.6;
  - the NHS ("Try not to take painkillers for more than 2 days a week");
  - the Migraine Trust (3 months for period-linked headaches).

  This is the same wording that the independent review of the launch's social posts checked.
- **OpenSpec:** `openspec validate add-website-headache-diary --strict` passes.
- **Not run:**
  - The Android gate, because no Gradle input changed.
  - The live site, because deploying is the owner's task.
  - A clinician's review. The page says so.

## Review

An independent review (`superpowers:code-reviewer`) of 9e45a48a..87dec7f6 checked the page and the template against
the live sources: NICE CG150, ICHD-3 terms, 1.3, 8.2, 8.2.3, 8.2.5 and 8.2.6, the NHS migraine and headaches pages,
and the Migraine Trust. It found 1 Critical issue, 6 Important and 11 Minor. All were fixed except the three noted
below.

**Critical, fixed.** The template's emergency box mixed ordinary migraine symptoms with emergency signs, and it was
incomplete. The NHS migraine page lists numbness, pins and needles and a stiff neck as ordinary migraine symptoms. The
box now follows that page's 999 list unsoftened:
- sudden, extremely painful headache;
- unable to move, or weakness in, a limb or one side of the face;
- problems speaking or remembering, or loss of vision, blurred or double vision;
- drowsy or confused, or a seizure;
- very high temperature with a stiff neck, or a non-fading rash;
- a recent head injury.

It also lists the same-day signs: an attack over 72 hours, aura over 1 hour, pregnancy or a new baby. The key now
explains what aura is.

**Important, fixed:**
- **I1:** a mix of simple painkillers alone stays at 15 days, because ICHD-3 8.2.3 treats them as one class. Only a
  mix of kinds counts toward 10. This was corrected on the page, on the template, in the spec, the design and the BRD,
  and in the launch's social post 01 and its fact pack.
- **I2:** the page and the template now give the other conditions: headache on 15+ days a month, and more than 3
  months of overuse.
- **I3:** the template now says "diagnostic thresholds, not safe limits" and quotes the NHS advice (no more than 2
  days a week).
- **I4:** the Migraine Trust's "3 months" comes from its Menstrual migraine page, which is now in the Sources.
- **I5:** the summary's "Migraine days (if known)" became "Days with N, L, S or A", so patients don't label their own
  days.
- **I6:** pain-free days get N under "Headache?" with the rest left blank, and page 6 states the counting rule (days,
  not doses).

**Minor, fixed:**
- The design now matches the symptom codes.
- C and O are separate summary columns.
- O means "other medicine for the attack (write its name)".
- The source label is the page's real title.
- `ICHD_MOH` and the title are shared constants, and source URLs are escaped.
- The second download link reads "Download US Letter (PDF)".
- In `make_diary_pdf.py`:
  - the line length is fixed;
  - `CHROME` can be overridden;
  - Chrome's error output is shown;
  - the 6-page count is checked.
- "tick" replaces ✓, so no Type 3 font remains.
- The PDF footer carries "Reviewed <date>".
- The how-to covers attacks past midnight and painkillers taken for other pain.
- The page has a short "When should I get help straight away?" section.
- `site.css` is versioned, because the review noticed `/assets/` is cached for a year.
- `_headers` detaches the CSP from the PDFs.

**Not done:**
- The file size in the link text: KISS.
- An optional summary of headache days around the period: it belongs to a future menstrual-migraine page (T15).
- `/about/` gives the MOH thresholds without the 15-day and 3-month conditions. That sentence describes what the app's
  note counts, not MOH, and is left for T14.

**Re-verified after the fixes:**
- Both PDFs have 6 pages (checked by the script and `pdfinfo`) and are 84 KB, with no Type 3 fonts. Pages 1 and 6
  were looked at again in greyscale.
- Two builds in a row change nothing, and the static checks find nothing.
- 24 overflow measurements show 0 sideways scroll (8 pages; light, dark and 200% text).
- 0 CSP violations on 8 pages.
