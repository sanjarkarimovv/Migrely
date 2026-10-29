# Tasks

## 1. Links

- [x] 1.1 `GUIDES` (D1, D6), the footer navigation (D3, D4) and the home section (D2).
- [x] 1.2 `check_orphans()` in `main()` (D5), and the README.

## 2. Verify

- [x] 2.1 Inbound link counts before and after; the orphan check stops a build with an unlinked page.
- [x] 2.2 Static checks, headless Chrome (CSP, 320 px in light, dark and 200% text), and a stable rebuild.
- [x] 2.3 `openspec validate add-website-guide-links --strict`, and the verification record.

## 3. Review and archive

- [x] 3.1 Independent review. Fix the findings and record them.
- [x] 3.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge, and a new deploy zip.

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

- **Orphan check:** a build with an extra `/orphan/` page in `PAGES` and no link to it stopped before writing anything.
  After the review's fix, the check was run again on the real site, which passed, and on four broken cases, which
  all stopped and named the page: a page with no link to it; two pages that link only to each other; a page linked
  only inside an HTML comment; and a page linked only by a `<link>` in the head.
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

## Review

An independent review (`superpowers:code-reviewer`) found nothing Critical. It confirmed:
- every summary is backed by its page;
- each page has two navs, "Main" and "Guides", and at most one `aria-current`;
- the heading order is h1, h2, h3;
- the existing spec requirements still hold;
- a rebuild leaves `git status` clean.

| # | Finding | Outcome |
|---|---|---|
| 1 Important | BRD D145 said the CSS does not change, but one rule was added | Fixed: D145 names the rule and why |
| 2 Minor | The orphan check counted any `href`, including `<link>` and comments, and passed two pages that link only to each other | Fixed: it follows `<a href>` links from `/`, with comments removed. The spec gains a scenario |
| 3 Minor | The message said "No page links to" and suggested GUIDES for every page | Fixed: it now reads "No link path from the home page reaches …: if it is a guide, add it to GUIDES; otherwise link it from another page" |
| 4 Minor | The VA summary said "Not affiliated with VA" without a subject, while the spec says Migrely | Fixed: "Migrely is not affiliated with VA." The home page was checked again at 320 px in all three modes: no overflow, 0 CSP violations |
| 5 Minor | Guide `href`s were not escaped | Fixed: `esc(href)` |
| 6 Minor | The footer guides use the header menu's style (grey, bold), and the bare `nav` rules now style both | Kept: the guides are navigation, like the header, and the current page is marked with `aria-current`. Scoping the header rules is a separate clean-up |
