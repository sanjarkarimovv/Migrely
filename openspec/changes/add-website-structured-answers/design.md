# Design

## Context

`website/build.py` writes every page from Python strings. The site has no JavaScript and a strict CSP, and every
sentence has to be true of the shipped app (D133–D146). The 2026-11-04 audit asks for:

- plain answers at the top of each section;
- structured data on the pages that have none;
- breadcrumbs, `HowTo` and `llms.txt`.

## Goals and non-goals

**Goals:**
- Every page answers its main questions in its first sentence, under question headings.
- Every page describes itself in JSON-LD.
- Visible text and structured data come from one source, so they can't drift apart.

**Non-goals:**
- Longer guides: a separate change.
- Other languages: D148.
- Signals only the owner can supply: a reviewer, a named founder, social profiles, ratings.

## Decisions

### D1. Two FAQ lists, no shared questions

| List | Where | Questions |
|---|---|---|
| `HOME_FAQ` | Home page, "Questions before you install" | Is it free? Does it need the internet? Only Android? Is my diary backed up? What does Premium add? |
| `FAQ` | `/support/` | What can leave my phone? What does Premium cost? Moving to a new phone, restoring Premium, cancelling, refunds, deleting everything, medical device |

- Google asks that a repeated question be marked up once. A visitor also gains nothing from two different answers.
- `check_faq_unique()` compares the two lists and stops the build on a repeat (EC-WEB-21).
- Home answers are short and link out; support answers are the full how-to.

### D2. `/support/` uses headings, not `<details>`

- Each question becomes an `<h2>` with a `<p>`, in the `prose` layout the guides use.
- Every answer is readable without a click, and crawlers see the questions as headings.
- The `<details>` styles are removed. The `.faq` class is reused for the home page's plain question list.

### D3. One JSON-LD graph per page

`page_ld(path, kind, name, *extra, **props)` returns a graph of:
- the `Organization`;
- the page node (`@id` `<url>#webpage`, `url`, `name`, `inLanguage`, `publisher`, `breadcrumb`, then `props`);
- the `BreadcrumbList` (`@id` `<url>#breadcrumb`);
- any extra nodes (`HowTo`).

`medical_ld()`, `/va-migraine-log/`, `/compare/` and `/about/` move onto it.

- **The `Organization` is repeated on each page.** Google reads each page alone, so an `@id` that points to the home
  page would not resolve.
- **`author`** is the `Organization` only where the page says "Written by Migrely" (`reviewed_block`).
- **No `reviewedBy`:** nobody reviewed the pages, and the visible text says so.
- **The home page keeps its own graph** (Organization, WebSite, MobileApplication, VideoObject) and gains the
  `FAQPage`.

### D4. Breadcrumbs from one dict

- `CRUMBS` maps each inner path to its short name.
- `page()` renders `<nav class="wrap narrow crumbs" aria-label="Breadcrumb"><ol>` before the body, and `page_ld()`
  writes the matching `BreadcrumbList`.
- The current page is a `<span aria-current="page">`, not a link.
- The separator is drawn in CSS with `content: "›" / ""`, so screen readers don't read it out.
- `.crumbs + .block` trims the section's top padding, so the trail sits just above the H1.
- There is no "Guides" level, because the site has no guides index page.

### D5. HowTo from the visible steps

- `CHECK_STEPS` (privacy check, `totalTime` PT2M, as "about two minutes" on the page) and `DELETE_STEPS` (delete
  data) are lists of strings.
- The `<ol>` and the `HowToStep`s are both built from them.
- Each `HowTo` has the `@id` `<url>#howto`, is the page node's `mainEntity`, and is named with the page's H1.

### D6. `llms.txt`

- It follows the llmstxt.org format:
  - `# Migrely`;
  - a `>` summary, which is the home page's definition;
  - a facts list;
  - `## Guides` from `GUIDES`;
  - `## About the app` from `TRUST_PAGES` (about, privacy check, help, privacy policy, delete data).
- It is plain ASCII text, with absolute URLs.
- Every sitemap page must be in it, or the build stops. That covers a new trust page that is not in `TRUST_PAGES`.

### D7. Shared facts

| Constant | Used by |
|---|---|
| `WHAT_IS` | home "What is Migrely?", `llms.txt` summary |
| `PREMIUM_PRICES` | `/compare/` Migrely card, `/support/` "What does Premium cost?", `llms.txt` |
| `MIN_SDK` = 26; `MIN_ANDROID` ("Android 8") is derived from it | home FAQ, `llms.txt`; checked against `gradle/libs.versions.toml` (EC-WEB-22) |
| `BACKUP_RULE` | every answer about Android's backup, and `llms.txt`: on Android 9 and later only end-to-end encrypted, which needs a screen lock; without one, only direct transfer; Android 8 like other apps |

### D8. No Twitter-specific tags

- X falls back to `og:title`, `og:description` and `og:image` when the `twitter:` versions are missing.
- The image would be the same, so `twitter:card` alone stays.

## Risks

| Risk | Mitigation |
|---|---|
| Rich results won't show: Google limits FAQ rich results to well-known authoritative government and health sites, and has dropped HowTo rich results | Expected and stated in D147. The markup is for AI answer engines and is accurate, so it costs nothing |
| An answer drifts from the app | Answers reuse wording already reviewed on `/privacy-check/`, `/about/` and the privacy policy. Prices come from one constant |
| Every `lastmod` moves to the build day | True: every inner page's HTML changed (breadcrumbs). D136 dates by HTML hash |
| The home page gets longer | The definition is about 55 words, and the five answers sit near the end, before the last call to action |
