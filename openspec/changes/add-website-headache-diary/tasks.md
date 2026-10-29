# Tasks

## 1. Template and PDFs

- [ ] 1.1 `website/make_diary_pdf.py`: the six pages (D2), each size printed with headless Chrome (D3).
  Verify: `pdfinfo` shows 6 pages, landscape A4 (842×595 pt) and Letter (792×612 pt), each under 300 KB.
  `pdftotext -layout` shows every heading and footer.
- [ ] 1.2 A visual check of every page of both PDFs at print size, rendered to PNG, and in greyscale (EC-WEB-07).

## 2. Page

- [ ] 2.1 `headache_diary()` in `build.py`, with `DIARY_REVIEWED`, `MedicalWebPage` JSON-LD, the download links and
  the Play button (D1). Add it to `PAGES`. Stop the build if a PDF is missing.
  Verify: `python3 website/build.py` builds; with a PDF moved away, it stops with the pointer.
- [ ] 2.2 Footer: "Free headache diary" on every page.
- [ ] 2.3 `site.css`: `.play.alt` and `.meta` (D4).

## 3. Docs

- [ ] 3.1 `website/README.md`: the diary page and how to remake the PDFs.

## 4. Verify

- [ ] 4.1 Static checks over `website/public`, as in change 16:
  - titles, descriptions, canonicals and one `h1`;
  - internal links, the PDFs included;
  - no running script;
  - every Play link with its campaign;
  - the JSON-LD.
- [ ] 4.2 Headless Chrome behind the CSP: no violations, and no sideways scroll at 320 px in light, dark and 200%
  text.
- [ ] 4.3 Every medical sentence on the page compared with its source (NICE CG150, ICHD-3, NHS, Migraine Trust).
- [ ] 4.4 `openspec validate add-website-headache-diary --strict`. No Gradle input changes, so the Android gate is not
  run.
- [ ] 4.5 Verification record.

## 5. Review and archive

- [ ] 5.1 Independent review of the diff, the PDFs and the medical wording. Fix the real findings and record them.
- [ ] 5.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge.
