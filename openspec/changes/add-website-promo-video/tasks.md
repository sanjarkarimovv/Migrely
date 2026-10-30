# Tasks

## 1. Video

- [x] 1.1 Copy the web cut and poster to `website/public/assets/video/` (D3).
- [x] 1.2 `PROMO_VIDEO`, `check_promo_video()`, the home section with the text alternative, and `VideoObject` (D2, D4, D5, D7).
- [x] 1.3 `media-src 'self'` in `_headers`, the `.video` styles (D6), and the README.

## 2. Verify

- [x] 2.1 Static checks, a stable rebuild, and the build stopping on a missing file.
- [x] 2.2 Headless Chrome under the CSP: no request for the video before play, the video loads with `media-src` and is blocked without it, 0 violations; 320 px in light, dark and 200% text.
- [x] 2.3 `openspec validate add-website-promo-video --strict`, and the verification record.

## 3. Review and archive

- [ ] 3.1 Independent review. Fix the findings and record them.
- [ ] 3.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge, and a new deploy zip.

## Verification record

Run on 2026-10-30 at `feature/add-website-promo-video`, built from `develop` 4f9402ac.

- **Files:**
  - `migrely-promo-v3.mp4`: 2,021,515 bytes, 1280×720, H.264 High, yuv420p, AAC, `+faststart`, 30.0 s.
  - Poster: 57,408 bytes, the video's frame 0.
- **Build:** the site builds with 11 sitemap pages, and two builds in a row change nothing. With the poster removed,
  the build stopped with "Missing migrely-promo-v3-poster.jpg in website/public/assets/video/: …" and wrote nothing.
- **Static checks:** nothing found. The checker now expects `VideoObject` in the home page's `@graph`. Its fields:
  - `contentUrl` and `thumbnailUrl` are absolute;
  - `duration` PT30S;
  - `uploadDate` 2026-10-30.
- **Headless Chrome, with each served path logged:**

  | Test | CSP | Result |
  |---|---|---|
  | Home page as built (`preload="none"`) | new | 0 violations; the poster loaded once; **0 requests for the video** |
  | A copy with `preload="auto"` | new (`media-src 'self'`) | 0 violations; the video loaded |
  | The same copy | old (no `media-src`) | **1 violation**; the video was blocked |

  `media-src 'self'` is needed, and it is enough.
- **Layout:**
  - No element reaches past the right edge at 320 px in light, dark and 200% text, on all 36 page and mode pairs.
    33 report 305 px because of the scrollbar.
  - 0 CSP violations on all 12 pages.
  - Screenshots 31–33 in `migrely-work/walks/website/`: the video section at 320 px in light and dark, and at desktop
    width.
- **OpenSpec:** `openspec validate add-website-promo-video --strict` passes.
- **Not run:**
  - Playback on a real phone.
  - The live site, which changes only after the owner uploads the new zip.
