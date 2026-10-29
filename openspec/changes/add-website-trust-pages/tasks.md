# Tasks

## 1. Site build

- [ ] 1.1 `PERMISSIONS` and `check_permissions()` (D2, EC-WEB-01): the five entries with Android's labels as the
  emulator shows them; stop on a difference with the release report, and print a note when it is missing.
  Verify: `python3 website/build.py` builds. A temporary report with an extra permission stops it with both lists. No
  report prints the note.
- [ ] 1.2 `play_link(campaign)` and the campaign from `page()`'s path (D5). Every Play `href` goes through `esc()`.
  Verify: every `play.google.com` link in `website/public` carries its page's campaign, and `downloadUrl` has none.
- [ ] 1.3 `/about/` with `AboutPage` JSON-LD (D1, D3).
  Verify: it has 6 question headings, the ICHD-3 link, the `/privacy-check/` link and a Play button.
- [ ] 1.4 `/privacy-check/` (D2).
  Verify: the steps, the airplane-mode note, the 5 permission cards, "what can still leave", `adb` and the
  `/privacy/` link.
- [ ] 1.5 Home: an `@graph` with `Organization`, `WebSite` and `MobileApplication`, and "Check it yourself" in the
  privacy band. Menu: "About". Footer: About, "Check our privacy".
  Verify: the JSON-LD parses, and the links are on every page, 404 included.
- [ ] 1.6 `website/lastmod.json` and the sitemap's `lastmod` (D4, EC-WEB-04).
  Verify: a second build leaves `git status` clean. A changed privacy policy (temporary) moves only `/privacy/`.
- [ ] 1.7 `site.css`: `code`, `pre`, `.perms` and `.steps` (D6).

## 2. Docs

- [ ] 2.1 `website/README.md`: the new pages in the check list, the permission report step before a build,
  `lastmod.json`, and the tagged Play links.

## 3. Verify

- [ ] 3.1 Facts on emulator-5590 (API 36): the Settings path, Android's labels for the five permissions, and that a
  networked app shows "Have full network access" while Migrely does not. Also the `dumpsys` output. Screenshots go in
  `migrely-work/walks/website/`.
- [ ] 3.2 Static checks over `website/public`:
  - each page has one `h1`, a title of at most 60 characters, a description of 70–160 characters and a canonical URL;
  - every internal link resolves to a built file;
  - no `<script>` except `application/ld+json`, and no `style=` attribute;
  - no `src` or stylesheet from another origin;
  - every JSON-LD block parses.
- [ ] 3.3 Visual checks of the new pages and the home page in headless Chrome, behind a local server that sends the
  `_headers` CSP:
  - widths 320 and 1280, light and dark themes, and 200% text at 320;
  - no horizontal scroll;
  - no CSP violation in the console.
- [ ] 3.4 `openspec validate add-website-trust-pages --strict`. The Android gate is not needed: no Gradle input
  changes.
- [ ] 3.5 Verification record (below).

## 4. Review and archive

- [ ] 4.1 Independent review (`superpowers:code-reviewer`) of `git diff origin/develop...`. Check the facts against
  the sources in D1 and the build logic. Fix the real findings and record them here.
- [ ] 4.2 `openspec archive add-website-trust-pages --yes`. Write the `website` spec's Purpose, then run
  `openspec validate --specs --strict`.
