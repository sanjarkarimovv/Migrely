# Proposal

## Why

Migrely has a 30-second promo video made only from real app screens (launch plan T25). A visitor who reaches the
home page from Google Play or search can see the app working before installing it. A YouTube embed would load
Google's scripts and cookies on a site whose promise is "no scripts, nothing from elsewhere" (D119), and its CSP would
block it. The rules are BRD D146 and EC-WEB-20.

## What Changes

- **A video section on the home page,** after the hero: "See Migrely in 30 seconds".
  - `<video controls preload="none" playsinline>`, with a poster (the video's first frame). There is no autoplay, no
    loop and no muted autoplay.
  - Below it, a text alternative that says what each scene shows. Music only, no speech.
- **Self-hosted files:** `/assets/video/migrely-promo-v3.mp4` (1280×720, about 2 MB) and its poster. The version is in
  the name because `/assets/` is cached for a year (EC-WEB-20).
- **CSP:** adds `media-src 'self'`. Nothing else changes.
- **`VideoObject`** in the home page's JSON-LD.
- **The build stops** when the video or the poster is missing.

## Out of scope

- A YouTube embed, autoplay, or a dedicated `/video/` page.
- The 9:16 cut for social media.
- Changes to the other pages.

## Capabilities

### Modified Capabilities

- `website`: adds the promo video.

## Impact

- `website/build.py`, `website/public/` (the home page, `_headers`, `assets/video/`, `site.css`), `lastmod.json` and
  `README.md`.
- `docs/BRD.md`: D146, EC-WEB-20, §13 row 23, §14.4.
