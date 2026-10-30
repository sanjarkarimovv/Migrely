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

- [x] 3.1 Independent review. Fix the findings and record them.
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

## Review

An independent review (`superpowers:code-reviewer`) found nothing Critical. It confirmed:
- the copy is true: the button labels are in `strings.xml`, and the release permission report has no network
  permission;
- the heading order;
- that `media-src 'self'` is the minimum, and the PDFs' header blocks are untouched;
- the required `VideoObject` fields;
- no flashes: brightness changes by at most 11/255 between frames;
- the build order, and that a rebuild changes nothing.

| # | Finding | Outcome |
|---|---|---|
| 1 Important | The text alternative left out the end card and the words on screen (WCAG 1.2.1) | Fixed: six items, each with its scene's on-screen words, the end card included |
| 2 Important | The fallback link inside `<video>` never shows when the MP4 can't be decoded | Fixed: a visible "Download the video (MP4, 2 MB)" link in the `<figcaption>` (design D7, and a spec scenario) |
| 3 Minor | Ten unchanged pages got a new `lastmod` only because the CSS hash changed (D136) | Fixed: the hash leaves out `CSS_VERSION` (design D8). On the switch, every page kept `develop`'s date except `/`, which changed today |
| 4 Minor | The video had no accessible name | Fixed: `aria-labelledby` points to the section heading |
| 5 Minor | `uploadDate` had no time zone | Fixed: `2026-10-30T12:00:00+05:00` |
| 6 Minor | "Real Migrely screens", though one frame has a recoloured chip | Fixed: "Migrely's screens with sample data" |
| 7 Minor | The main spec said the CSP stays "unchanged" | Fixed: a MODIFIED requirement says it allows only the site itself, with `media-src 'self'` for the video |
| 8 Minor | Repeated list styles; long caption lines | Fixed: the caption uses the global list style, at most 65ch wide |
| 9 Optional | Pin a sha256 per video file | Not done: the version in the file name and the missing-file check cover EC-WEB-20, so a hash adds a second place to update |

After the fixes:
- static checks find nothing, and a rebuild changes nothing;
- the sitemap changes only `/`'s date;
- the home page has no sideways scroll at 320 px in light, dark and 200% text;
- 0 CSP violations.
