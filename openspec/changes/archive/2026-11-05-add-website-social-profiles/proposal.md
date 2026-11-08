# Proposal

## Why

The 2026-11-04 audit found that the `Organization` has no `sameAs`. Search and AI engines use `sameAs` to tie a brand
to its official profiles. Migrely's YouTube channel, `@migrely`, exists and describes the app. The rules are BRD D150
and EC-WEB-06.

## What Changes

- **`SOCIAL_PROFILES`:** one list of Migrely's official profiles, as name and URL. It starts with YouTube.
- **Uses:** the `Organization`'s `sameAs` comes from it, every footer gets a plain link with `rel="me"`, and
  `/llms.txt` lists the profiles.
- **Empty list:** no `sameAs`, and no footer link.

## Out of scope

Creating accounts (the owner does that, T05), embeds, and follow buttons.

## Capabilities

### Modified Capabilities

- `website`: `sameAs` and profile links.

## Impact

- `website/build.py` and `website/public/` (every page's footer, the home and about JSON-LD, `llms.txt`).
- `website/lastmod.json`.
- `docs/BRD.md`: D150, EC-WEB-06, §13 row 26.
