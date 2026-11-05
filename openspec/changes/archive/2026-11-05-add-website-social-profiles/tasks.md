# Tasks

- [x] 1.1 `SOCIAL_PROFILES`; `sameAs` on the `Organization`; a footer link with `rel="me"`; `/llms.txt`. Verify: the home and about graphs carry `sameAs`; every page's footer links the channel.
- [x] 1.2 Build twice (stable), static checks, CSP walk. Verify: an empty list builds with no `sameAs` and no link.
- [x] 1.3 Review, archive, PR into `develop` and merge, zip.

## Verification record

Run on 2026-11-05, built from `develop` 478beb49.

- **The channel:** `https://www.youtube.com/@migrely` is channel UCin5umjovFt5gWqty8Yyc0w. `@migrelyapp` resolves to
  the same channel. Its description begins "Migrely is a private migraine diary for Android", which is Migrely's own copy.
- **Build:**
  - the `Organization` on the home page and `/about/` has `sameAs: ["https://www.youtube.com/@migrely"]`;
  - all 12 footers link it with `rel="me"`;
  - `/llms.txt` has "Official YouTube profile";
  - the static checks find nothing, and a second build changes nothing.
- **Empty list:** no `sameAs`, no footer link and no `llms.txt` line. Restoring the list rebuilt byte-identical files.
- **Headless Chrome:** 0 CSP messages on 12 pages; the control page shows 2. At 320 px, all pages fit.
- **Review:** a 20-line change with no medical or privacy text, so it was reviewed inline.
  - The link is plain: no embed and no script, so D119 and the CSP are unchanged.
  - `esc()` is applied to the URL and the name.
  - The empty list is covered.
  - No subagent was used.
