# Proposal

## Why

People choosing a migraine diary compare apps, and search for "Migraine Buddy alternative" or "private migraine
app". The launch plan's positioning, "private by proof", needs a page that compares on checkable facts and states
other apps' strengths honestly, so it is not marketing dressed as a review. Task T19 of the launch plan. The rules are
BRD D144 and EC-WEB-17…18.

## What Changes

- **`/compare/`** (D144):
  - **A lead that says we make Migrely and are not neutral.** It names the sources (each app's Play listing,
    permission list, Data safety, website and privacy policy), the date they were read, and that prices change.
  - **Two short answers:** which apps request no internet permission (Migrely and the open-source Migraine Log), and
    which need an account.
  - **One card per app, in the same fields:** platforms, account, internet permission, what its Play Data safety says
    it shares, the free tier, Premium price, the doctor report, what it does well, and the sources.
  - Migrely's card is first and lists its own limits too.
  - **Only verified facts.** Anything not verified is left out, not marked unknown (EC-WEB-18).
  - A trademark note, Sources, "Last reviewed", and a `WebPage` JSON-LD.
- **A link from `/about/`.**

## Out of scope

- Exodus tracker counts (the site blocked automated reading). They can be added after a manual check.
- iOS-only apps, and any star rating or "best" verdict.

## Capabilities

### Modified Capabilities

- `website`: adds the comparison page.

## Impact

- `website/build.py`, `website/public/` (the new page, `/about/`, the sitemap), `site.css` (the cards), `lastmod.json`
  and `README.md`.
- `docs/BRD.md`: D144, EC-WEB-17…18, §13 row 21, §14.4.
