# Tasks

## 1. Page

- [x] 1.1 `COMPARE_APPS`, `COMPARE_REVIEWED` and `compare()` (D1–D3), with `.apps` styles (D4). Add it to `PAGES`.
- [x] 1.2 A link from `/about/`, and the README.

## 2. Verify

- [x] 2.1 Every card fact traced to the research file's "safe to publish" list, and none from "do not publish".
- [x] 2.2 Static checks, headless Chrome (CSP, 320 px in light, dark and 200% text), and a stable rebuild.
- [x] 2.3 `openspec validate add-website-compare --strict`, and the verification record.

## 3. Review and archive

- [ ] 3.1 Independent review. Fix the findings and record them.
- [ ] 3.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge.

## Verification record

Run on 2026-10-29 at `feature/add-website-compare`, built from `develop` 566c5882.

- **Facts:** every card row was traced to the research file `marketing/research/2026-10-29-compare-facts.md` (outside
  the repo):
  - its "safe to publish" items 1–16 and the confirmed cells of its summary and price tables;
  - nothing from its "do not publish" list: no Exodus counts, no "only app without internet", no Migraine Buddy
    free-tier or export claims, no Migraine Insight account claim, no Dark Logger unlock contents, and no Nagi widget
    price.

  Two phrases with no source were removed before the commit: Bearable's "habits", and Migraine Insight's "among other
  features".
- **Migrely's card:** the prices come from `docs/release/play-products.md`. The free and Premium features match
  `/about/`, and the limits are stated.
- **Build:** the site builds with 11 sitemap pages, and two builds in a row leave `lastmod.json` unchanged.
- **Static checks:** nothing found. The checker now applies the UTM rule only to Migrely's own Play links;
  competitor links stay untagged.
- **Headless Chrome:**
  - 0 CSP violations.
  - No sideways scroll at 320 px in light, dark and 200% text. The cards stack `dt` over `dd` under 520 px.
  - Screenshots 26–27 are in `migrely-work/walks/website/`.
- **OpenSpec:** `openspec validate add-website-compare --strict` passes.
- **Not run:**
  - The live site.
  - A re-read of every competitor source today by a second reader. That is the review's job, below.
