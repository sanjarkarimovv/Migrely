# Tasks

## 1. Site build

- [x] 1.1 `PERMISSIONS` and `check_permissions()` (D2, EC-WEB-01): the five entries with Android's labels as the
  emulator shows them; stop on a difference with the release report, and print a note when it is missing.
  Verify: `python3 website/build.py` builds. A temporary report with an extra permission stops it with both lists. No
  report prints the note.
- [x] 1.2 `play_link(campaign)` and the campaign from `page()`'s path (D5). Every Play `href` goes through `esc()`.
  Verify: every `play.google.com` link in `website/public` carries its page's campaign, and `downloadUrl` has none.
- [x] 1.3 `/about/` with `AboutPage` JSON-LD (D1, D3).
  Verify: it has 7 question headings, the ICHD-3 link, the `/privacy-check/` link and a Play button.
- [x] 1.4 `/privacy-check/` (D2).
  Verify: the steps, the airplane-mode note, the 5 permission cards, "what can still leave", `adb` and the
  `/privacy/` link.
- [x] 1.5 Home: an `@graph` with `Organization`, `WebSite` and `MobileApplication`, and "Check it yourself" in the
  privacy band. Menu: "About". Footer: About, "Check our privacy".
  Verify: the JSON-LD parses, and the links are on every page, 404 included.
- [x] 1.6 `website/lastmod.json` and the sitemap's `lastmod` (D4, EC-WEB-04).
  Verify: a second build leaves `git status` clean. A changed privacy policy (temporary) moves only `/privacy/`.
- [x] 1.7 `site.css`: `code`, `pre`, `.perms`, and `overflow-wrap: anywhere` on `body` (D6). `.steps` was not
  needed: the pages use `.prose` lists.

## 2. Docs

- [x] 2.1 `website/README.md`: the new pages in the check list, the permission report step before a build,
  `lastmod.json`, and the tagged Play links.

## 3. Verify

- [x] 3.1 Facts on emulator-5590 (API 36): the Settings path, Android's labels for the five permissions, and that a
  networked app shows “have full network access” while Migrely does not. Also the `dumpsys` output. Screenshots go in
  `migrely-work/walks/website/`.
- [x] 3.2 Static checks over `website/public`:
  - each page has one `h1`, a title of at most 60 characters, a description of 70–160 characters and a canonical URL;
  - every internal link resolves to a built file;
  - no `<script>` except `application/ld+json`, and no `style=` attribute;
  - no `src` or stylesheet from another origin;
  - every JSON-LD block parses.
- [x] 3.3 Visual checks of the new pages and the home page in headless Chrome, behind a local server that sends the
  `_headers` CSP:
  - widths 320 and 1280, light and dark themes, and 200% text at 320;
  - no horizontal scroll;
  - no CSP violation in the console.
- [x] 3.4 `openspec validate add-website-trust-pages --strict`. The Android gate is not needed: no Gradle input
  changes.
- [x] 3.5 Verification record (below).

## 4. Review and archive

- [ ] 4.1 Independent review (`superpowers:code-reviewer`) of `git diff origin/develop...`. Check the facts against
  the sources in D1 and the build logic. Fix the real findings and record them here.
- [ ] 4.2 `openspec archive add-website-trust-pages --yes`. Write the `website` spec's Purpose, then run
  `openspec validate --specs --strict`.

## Verification record

Run on 2026-10-29 at `feature/add-website-trust-pages`, built from `develop` 1ffe8b33.

- **Facts on emulator-5590** (Android 16, API 36), screenshots in `migrely-work/walks/website/` 01–05:
  - Settings → Apps → Migrely → Permissions → ⋮ → All permissions lists only “show notifications” (Notifications),
    and “prevent phone from sleeping” and “run at startup” (Other app capabilities).
  - Chrome's list, in the same place, includes “have full network access” and “view network connections”.
  - BILLING is not listed, because the emulator has no Play Store to define it, and neither is the AndroidX
    permission. The page therefore gives those two descriptive titles and does not claim a label for them.
  - `dumpsys package com.migrely.app` prints "requested permissions:" followed by the same five, so
    `grep -A5` shows exactly them.
- **Permission report:** `./gradlew :app:checkReleasePermissions` passed, and the report is the five permissions on
  the page.
  - A report with an extra permission stopped the build with both lists (exit 1).
  - Without a report, the build printed the note and built the site.
- **`lastmod`:**
  - A second build left `lastmod.json` byte for byte the same.
  - With every date set to 2026-10-01 and a test line added to the privacy policy, only `/privacy/` moved to
    2026-10-29. Both files were then restored.
- **Static checks** over `website/public` found nothing:
  - one `h1` per page; titles of 14–50 characters; descriptions of 84–145 characters;
  - canonicals correct;
  - every internal link resolves;
  - no running script, `style` attribute or foreign `src`;
  - every Play link carries its page's campaign (`home`, `about`, `privacy-check`, `support`, `privacy`,
    `delete-data`, `404`);
  - the home `@graph` is Organization, WebSite and MobileApplication, with the WebSite published by the
    organization's `@id`, no `sameAs`, and an untagged `downloadUrl`;
  - `/about/` is an `AboutPage` about the same Organization.

  `/delete-data/`'s description was 52 characters and became 128.
- **Headless Chrome**, behind a local server that sends the `_headers` CSP:
  - **CSP:** 0 violations on all 7 pages. A control page with an inline script and a style attribute was caught.
  - **Width:** at 320 px, light and dark, no page scrolls sideways (`scrollWidth` ≤ 320).
  - **200% text:** with every font size doubled at 320 px, every page, old and new, first scrolled sideways (437–557
    px). Long words did not wrap: `support@migrely.health`, and headings at their minimum size. `overflow-wrap:
    anywhere` on `body` fixed it, and then every page measured ≤ 320. Long permission names then broke inside words
    at 1280 px, which `<wbr>` after dots and underscores fixed.
  - Screenshots 10–15 (light 1280, dark 500, 200% text) look right in both themes.
- **OpenSpec:** `openspec validate add-website-trust-pages --strict` passes.
- **Not run:**
  - The Android gate, because no Gradle input changed: only `website/`, `docs/BRD.md` and `openspec/`.
  - The live site, because deploying is the owner's task T09. After deploying, check the new pages answer 200 and
    send the CSP header.
  - The Play Store app's own permission list, because the emulator has no Play Store.
  - Whether Play Console reports the UTM tags, which needs real traffic. Check it at the day-30 review (T22).
