# Tasks

## 1. Links

- [x] 1.1 `GUIDES` (D1, D6), the footer navigation (D3, D4) and the home section (D2).
- [x] 1.2 `check_orphans()` in `main()` (D5), and the README.

## 2. Verify

- [x] 2.1 Inbound link counts before and after; the orphan check stops a build with an unlinked page.
- [x] 2.2 Static checks, headless Chrome (CSP, 320 px in light, dark and 200% text), and a stable rebuild.
- [x] 2.3 `openspec validate add-website-guide-links --strict`, and the verification record.

## 3. Review and archive

- [ ] 3.1 Independent review. Fix the findings and record them.
- [ ] 3.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge, and a new deploy zip.

## Verification record

Run on 2026-10-29 at `feature/add-website-guide-links`, built from `develop` f4318cca.

- **Inbound internal links** (pages other than the 404 page, counting each linking page once):

  | Page | Before (deployed site) | After |
  |---|---|---|
  | `/va-migraine-log/` | 0 | 10 |
  | `/compare/` | 1 | 10 |
  | `/menstrual-migraine/` | 1 | 10 |
  | `/medication-overuse-headache/` | 3 | 10 |
  | every other page | 10–11 | 10 |

- **Orphan check:** a build with an extra `/orphan/` page in `PAGES` and no link to it stopped with "No page links to
  /orphan/: add it to GUIDES or link it from another page." and wrote nothing.
- **Build:** 11 sitemap pages. Two builds in a row leave `lastmod.json` and the HTML unchanged. Every page's `lastmod`
  is now 2026-10-29, because every footer changed.
- **Static checks:** nothing found.
- **Headless Chrome:**
  - 0 CSP violations on all 12 pages.
  - At 320 px in light, dark and 200% text, no element reaches past the right edge on any of the 36 page and mode
    pairs. 33 report a document width of 305 px, because the vertical scrollbar takes 15 px of the 320.
  - The first walk found the footer's guide rows 58 px apart: the menu's 8 px link padding plus the `.links` gap. One
    rule, `.foot nav.links { row-gap: 0 }`, brings them to 40 px, in step with the link row below.
  - Screenshots 28–30 in `migrely-work/walks/website/`: the home page's "Free guides", the VA page's footer in dark,
    and the comparison page's footer at 200% text.
- **OpenSpec:** `openspec validate add-website-guide-links --strict` passes.
- **Not run:** the live site, which changes only after the owner uploads the new zip.
