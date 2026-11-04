# Proposal

## Why

An SEO, GEO and AEO audit of migrely.health on 2026-11-04 scored the site 8, 6 and 7 out of 10. The technical base is
clean. What it lacks is plain answers that search and AI answer engines can lift, and structured data on every page:

- `/support/` is titled "Help · Migrely" (14 characters), and its questions sit in `<details>` with no headings;
- the home page never says in one paragraph what Migrely is, and answers no questions;
- `/privacy-check/`, `/privacy/` and `/delete-data/` have no JSON-LD, and no page has breadcrumbs;
- there is no `/llms.txt`.

The rules are BRD D147 and D148, EC-WEB-21 and EC-WEB-22.

## What Changes

- **`/support/`:** a descriptive title and H1. Each question becomes an H2 with an answer that starts with the direct
  answer. There are three new questions: what can leave the phone, what Premium costs, and refunds. It stays a
  `FAQPage`.
- **Home page:**
  - after the hero, "What is Migrely?": a 40–60-word definition;
  - before the last call to action, "Questions before you install": five short answers, a link to `/support/`, and
    `FAQPage` in the home page's `@graph`.
  - No question appears on both pages; the build stops if one does (EC-WEB-21).
- **Breadcrumbs:** "Home › page" on every inner page, visible and as `BreadcrumbList`.
- **One JSON-LD `@graph` per page:**
  - the `Organization`, the page's own type (`WebPage`, `AboutPage`, `FAQPage` or `MedicalWebPage`) and its
    breadcrumbs;
  - `author` is Migrely on the pages that say "Written by Migrely".
- **`HowTo`** for the phone check on `/privacy-check/` and the steps on `/delete-data/`. Each comes from the same list
  as the visible steps.
- **`/llms.txt`:** the app's facts and every page, with one sentence each.
- **Premium prices** come from one constant for `/compare/` and `/support/`.
- **The build stops** when the app's `minSdk` no longer matches the "Android 8 and later" the site states
  (EC-WEB-22).

## Out of scope

- **Longer medical guides:** a separate change, after source research.
- **A Russian or other-language site:** D148. The app is English-only, so the site gets a language when the app does.
- **What only the owner can give:**
  - a clinician reviewer (`reviewedBy`);
  - a named founder on `/about/`;
  - social profiles (`sameAs`);
  - real Play ratings (`aggregateRating`).
- **`twitter:title`, `twitter:description` and `twitter:image`:** X reads the `og:` tags, and the image is the same.

## Capabilities

### Modified Capabilities

- `website`: adds answers, breadcrumbs, per-page structured data, HowTo and `llms.txt`.

## Impact

- `website/build.py` and `website/public/`: every page, `llms.txt` and `site.css`.
- `website/lastmod.json`: every inner page gains breadcrumbs, so every date moves to the build day.
- `website/README.md`.
- `docs/BRD.md`: D147, D148, EC-WEB-21, EC-WEB-22, §13 row 24, §14.4.
