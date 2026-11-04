# Tasks

## 1. Answers

- [x] 1.1 `WHAT_IS`, `HOME_FAQ`, the home page's "What is Migrely?" and "Questions before you install" sections and their `FAQPage` (D1, D7). Verify: the word count of `WHAT_IS` is 40–60.
- [x] 1.2 `/support/`: the title, description and H1; H2 questions; the new and expanded answers; `PREMIUM_PRICES` shared with `/compare/`; remove the `.faq` styles (D1, D2, D7). Verify: the title is 40–60 characters and the description 120–160.
- [x] 1.3 `check_faq_unique()` (EC-WEB-21) and `check_min_sdk()` (EC-WEB-22). Verify: each stops the build when fed a repeated question or `minSdk` 28.

## 2. Structure

- [x] 2.1 `CRUMBS`, the breadcrumb navigation in `page()` and its styles (D4). Verify: every inner page has one, and the home and 404 pages have none.
- [x] 2.2 `page_ld()`; move `medical_ld()`, VA, compare and about onto it; `WebPage` for `/privacy-check/`, `/privacy/` and `/delete-data/`; `FAQPage` for `/support/` (D3). Verify: every page's JSON-LD parses and has one page node and a `BreadcrumbList`.
- [x] 2.3 `CHECK_STEPS`, `DELETE_STEPS` and their `HowTo` (D5). Verify: the `HowToStep` texts equal the `<li>` texts.
- [x] 2.4 `llms.txt` and its check that it lists every sitemap page (D6). Verify: 11 page URLs, ASCII only.

## 3. Docs and verify

- [x] 3.1 `website/README.md`.
- [x] 3.2 Build twice: the second build changes nothing. Run the static checks: one H1, title and description lengths, JSON-LD, breadcrumbs, `HowTo`, `llms.txt`, internal links and UTM Play links.
- [x] 3.3 Headless Chrome under the CSP: 0 violations on every page, and no overflow at 320 px on every page; look at the new sections.
- [x] 3.4 `openspec validate add-website-structured-answers --strict` and the verification record.

## 4. Review and archive

- [ ] 4.1 Independent review. Fix the findings and record them.
- [ ] 4.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge, and a new deploy zip.

## Verification record

Run on 2026-11-04 at `feature/add-website-structured-answers`, built from `develop` 08f40563.

- **Build:** 11 sitemap pages, `404.html` and `llms.txt`. Two builds in a row change nothing in `website/`.
- **Build stops** (each run wrote nothing):
  - `minSdk` set to 28 in `gradle/libs.versions.toml`: "…has minSdk 28, but the site says "Android 8 and later"
    (minSdk 26)…" (EC-WEB-22);
  - "Is Migrely free?" added to `FAQ`: "Answered on both the home page and /support/: Is Migrely free?…" (EC-WEB-21).
- **Static checks** (a script over `website/public`): nothing found.
  - Every page: one H1, a title of 30–60 characters, a description of 100–160, its own canonical, no inline script or
    style, and internal links that resolve.
  - Every Migrely Play link has the UTM `referrer`.
  - Every inner page's graph is `Organization`, its page node and a `BreadcrumbList` (Home at 1, the page at 2, absolute
    URLs). The visible trail shows the same names, and the page itself is `aria-current`.
  - `author` is present exactly on the 5 pages that say "Written by Migrely". There is no `reviewedBy` or
    `aggregateRating` anywhere.
  - The home page's graph is Organization, WebSite, MobileApplication, VideoObject and FAQPage. "What is Migrely?" is
    57 words. The 5 questions shown are the 5 in the `FAQPage`, word for word, and the section links `/support/`.
  - `/support/`: 8 H2 questions equal its `FAQPage`; no `<details>` left. No question is on both pages.
  - The `HowTo` steps equal the visible `<li>`s: 4 on `/privacy-check/` (`totalTime` PT2M) and 3 on `/delete-data/`.
  - `llms.txt` is ASCII, starts `# Migrely` and a `>` summary, and links all 11 sitemap pages.
  - The 404 page has no breadcrumbs and no JSON-LD.
  - New titles: `/support/` 54 characters, `/privacy/` 48, `/delete-data/` 51. `/support/`'s description is 155.
- **Headless Chrome with the site's CSP from `_headers`:**
  - all 12 pages showed 0 CSP messages;
  - a control page with an inline script and a `style` attribute showed 2, so the check sees violations.
- **320 px:** each page in a 320 px iframe (305 px inside the scrollbar).
  - Found: the home page overflowed by 15 px. `.two`'s `minmax(300px, 1fr)` columns are wider than a phone under
    340 px, and `develop`'s home page does the same.
  - Fixed: `.two` and `.features` now use `minmax(min(…, 100%), 1fr)`. After the fix, all 12 pages fit (scrollWidth =
    clientWidth).
- **Looked at:** the home page's "What is Migrely?" and "Questions before you install" (moved from a centred narrow
  column to the page's left edge with a 70ch measure), `/support/`, and a guide's breadcrumbs in dark mode.
- **Not checked:**
  - Google's Rich Results Test and the schema.org validator need a public URL; run them after the deploy.
  - The Claude in Chrome browser could not reach the local server (connection refused), so headless Chrome on this Mac
    was used.
  - 200% text: the site sets font sizes in px, so the 320 px run stands for 400% page zoom at 1280 px (WCAG 1.4.10).
    Light mode was not shot: the new parts use the existing `--muted` and `--violet` tokens.
