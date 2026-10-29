# Proposal

## Why

After the T09 deploy, Google indexed all five guides, but the home page linked to none of them. Counting internal
links in the deployed site: `/va-migraine-log/` had none, `/compare/` and `/menstrual-migraine/` one each, and
`/medication-overuse-headache/` three. Google finds a page through the sitemap, but it judges how much the site values
a page by the links to it, so these pages would rank lower than they deserve. The rules are BRD D145 and EC-WEB-19.

## What Changes

- **`GUIDES`:** one list in `website/build.py` with each guide's path, short name and one-sentence summary.
- **A "Free guides" section on the home page:** one card per guide, with a descriptive link and its summary. The VA
  card keeps "Not affiliated with VA" (D143).
- **A "Guides" footer navigation on every page:** `<nav aria-label="Guides">` with all five guides, the current one
  marked `aria-current="page"`. "Free headache diary" moves from the footer's link row into it.
- **An orphan check in the build:** the build stops and names any sitemap page that no other page links to.

## Out of scope

- A "Guides" item in the header menu, or a `/guides/` hub page. Five guides fit on the home page, and the header has no
  room for a fifth item at 320 px.
- New components: the cards use `.features`, and the footer uses `.links` with one row-gap rule.
- New links inside the guides' text.

## Capabilities

### Modified Capabilities

- `website`: adds the guide links and the orphan check.

## Impact

- `website/build.py`, `website/public/` (every page's footer, the home page, one rule in `site.css`), `lastmod.json`
  and `README.md`.
- `docs/BRD.md`: D145, EC-WEB-19, §13 row 22, §14.4.
