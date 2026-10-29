# Design

## Context

The site is built by `website/build.py` into static HTML. Every page uses `page()`, which writes the header menu
(Home, About, Help, Privacy) and the footer. The five guides were reachable from the sitemap and a few links in other
pages' text, but not from the home page. Counted in the deployed site on 2026-10-29: `/va-migraine-log/` 0 inbound
links, `/compare/` 1, `/menstrual-migraine/` 1, `/medication-overuse-headache/` 3.

## Goals and non-goals

- **Goals:** every guide linked from the home page with descriptive text and from every footer; a build check so a
  future page cannot ship unlinked.
- **Non-goals:** a header item, a hub page, new styles, edits to the guides' text.

## Decisions

| # | Decision | Why |
|---|---|---|
| D1 | `GUIDES = [(path, name, summary), …]` is the single source for the home cards and the footer | One place to add a guide; the two lists cannot drift apart |
| D2 | Home section "Free guides" after "Private by design", as `.features` cards with `<h3><a>` | The home page is the strongest page, and a card's link text plus summary tells Google and the reader what the page covers. Reuses existing styles, so `site.css` and its cache key do not change |
| D3 | Footer `<nav class="links" aria-label="Guides">`, above the existing link row; "Free headache diary" moves into it | A labelled landmark is clear to screen readers. The global `nav a` style gives 8 px of vertical padding, so the links are easier to tap |
| D4 | The current guide is marked `aria-current="page"` in the footer, as in the header | Same pattern as the main menu |
| D5 | `check_orphans(built)` reads every built page's `href="/…"` and stops when a sitemap page other than `/` has no link from another page | A sitemap entry alone does not make a page part of the site (EC-WEB-19) |
| D6 | Summaries come from each page's own meta description, shortened, and make no new claim. The VA summary keeps "Not affiliated with VA"; the medication summary says the day counts are not safe limits | The guides' rules (D141, D143) apply wherever they are described |

## Risks

- **Every page's HTML changes,** so every `lastmod` becomes 2026-10-29. That is correct: the footer did change.
- **A longer footer at 320 px.** Checked in the visual walk at 320 px in light, dark and 200% text.
