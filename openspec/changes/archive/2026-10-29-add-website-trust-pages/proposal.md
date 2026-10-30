# Proposal

## Why

The launch plan of 2026-10-28 positions Migrely as "private by proof, not by promise". "Private, offline, no account"
is now a crowded claim, and Migrely's real difference is one a visitor can check: the app has no internet permission.
migrely.health says so in one line and never shows how to check it.

The site also has only four pages. It has no page saying who makes Migrely or how it is paid for, which is what search
engines and wary visitors look for on a health app. It has no Organization data, and its sitemap has no `lastmod`. Its
Play links carry no tag, so Play Console cannot tell installs that came from the site. Task T08 of the launch tracker
asks for these. The rules are BRD D133–D137 and EC-WEB-01…06.

## What Changes

- **`/about/`** (D133). It says:
  - who publishes Migrely and why it keeps entries on the phone;
  - how it is paid for: Premium only, with no ads and no data sales;
  - what stays free and what Migrely will never do;
  - which classification its medication note follows (ICHD-3, D45);
  - how to get in touch.

  Headings are questions. The page states only facts that can be checked: no invented story, patients, reviews,
  credentials or numbers.
- **`/privacy-check/`** (D134). It shows how to check that Migrely has no internet permission:
  - on the phone: Settings → Apps → Migrely → Permissions → ⋮ → All permissions;
  - what airplane mode shows, and what it does not;
  - with `adb`.

  It explains each of the app's five permissions in plain words. It also names what can still leave the phone without
  the internet permission: Android's own backup, Google Play's purchase and rating windows, and files the user shares.
- **The permission list cannot go stale** (EC-WEB-01). The site build compares it with the release build's permission
  report (`app/build/reports/permissions/release.txt`) and stops on a difference.
- **Links:**
  - "About" in the menu;
  - both pages in the footer;
  - a "Check it yourself" link in the home page's privacy section.
- **Structured data** (D135):
  - the home page gets an `@graph` with `Organization`, `WebSite` and the existing `MobileApplication`;
  - `/about/` is an `AboutPage` about that `Organization`;
  - no `sameAs` until the social profiles exist (EC-WEB-06).
- **Sitemap `lastmod`** (D136). `website/lastmod.json` keeps each page's SHA-256 hash and last change date, and only
  a changed page gets a new date (EC-WEB-04).
- **Tagged Google Play links** (D137). Every Play link on the site carries
  `referrer=utm_source=migrely.health&utm_medium=website&utm_campaign=<page>`, URL-encoded, so Play Console counts
  installs that came from the site (EC-WEB-03). The structured data's `downloadUrl` stays untagged.
- The site stays static: no scripts, cookies or third-party requests, and the CSP is unchanged (D119). The new pages
  read at 320 px and 200% text, in both themes (EC-WEB-05).

## Out of scope

- An Exodus Privacy report link (task T10, once the report exists), social profiles in `sameAs` (T05), and the content
  pages of tasks T13–T15, T18 and T19.
- Deploying the site: the owner uploads `website/public` to Cloudflare Pages (task T09).
- `llms.txt`. No major engine reads it.
- Any change to the app. Its permissions, code and privacy policy stay as they are.

## Capabilities

### New Capabilities

- `website`: the migrely.health trust pages, their links, structured data, sitemap dates and tagged Play links.

### Modified Capabilities

None.

## Impact

- `website/build.py`, `website/public/` (rebuilt), `website/public/assets/site.css`, the new `website/lastmod.json`
  and `website/README.md`.
- `docs/BRD.md`: D133–D137, §9.12 EC-WEB-01…06, §13 row 16, §14.4.
- There is no app, Gradle or permission change. After merging, the owner deploys the site (T09).
