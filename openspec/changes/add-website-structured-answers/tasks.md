# Tasks

## 1. Answers

- [ ] 1.1 `WHAT_IS`, `HOME_FAQ`, the home page's "What is Migrely?" and "Questions before you install" sections and their `FAQPage` (D1, D7). Verify: the word count of `WHAT_IS` is 40–60.
- [ ] 1.2 `/support/`: the title, description and H1; H2 questions; the new and expanded answers; `PREMIUM_PRICES` shared with `/compare/`; remove the `.faq` styles (D1, D2, D7). Verify: the title is 40–60 characters and the description 120–160.
- [ ] 1.3 `check_faq_unique()` (EC-WEB-21) and `check_min_sdk()` (EC-WEB-22). Verify: each stops the build when fed a repeated question or `minSdk` 28.

## 2. Structure

- [ ] 2.1 `CRUMBS`, the breadcrumb navigation in `page()` and its styles (D4). Verify: every inner page has one, and the home and 404 pages have none.
- [ ] 2.2 `page_ld()`; move `medical_ld()`, VA, compare and about onto it; `WebPage` for `/privacy-check/`, `/privacy/` and `/delete-data/`; `FAQPage` for `/support/` (D3). Verify: every page's JSON-LD parses and has one page node and a `BreadcrumbList`.
- [ ] 2.3 `CHECK_STEPS`, `DELETE_STEPS` and their `HowTo` (D5). Verify: the `HowToStep` texts equal the `<li>` texts.
- [ ] 2.4 `llms.txt` and its check that it lists every sitemap page (D6). Verify: 11 page URLs, ASCII only.

## 3. Docs and verify

- [ ] 3.1 `website/README.md`.
- [ ] 3.2 Build twice: the second build changes nothing. Run the static checks: one H1, title and description lengths, JSON-LD, breadcrumbs, `HowTo`, `llms.txt`, internal links and UTM Play links.
- [ ] 3.3 Headless Chrome under the CSP: 0 violations, and no overflow at 320 px in light, dark and 200% text, on the home page, `/support/`, `/privacy-check/` and one guide.
- [ ] 3.4 `openspec validate add-website-structured-answers --strict` and the verification record.

## 4. Review and archive

- [ ] 4.1 Independent review. Fix the findings and record them.
- [ ] 4.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge, and a new deploy zip.
