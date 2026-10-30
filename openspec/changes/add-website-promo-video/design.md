# Design

## Context

The home page is built by `home()` in `website/build.py`. The site is static and runs no scripts. Its CSP is
`default-src 'none'` with `img-src`, `style-src` and `font-src` set to `'self'`, so a `<video>` source needs
`media-src 'self'`. The poster is an image, so `img-src 'self'` already covers it. `/assets/*` is cached for a year
as immutable.

The video is `Migrely-Promo-v3-1280x720-web.mp4` from `marketing/video/promo-v3/out/` (outside the repo):
- H.264 High, yuv420p, AAC, `+faststart`;
- 30 s, about 2 MB.

The poster is that video's frame 0.

## Goals and non-goals

- **Goals:** show the app working, keep the privacy promise, add no weight until play, and make the video accessible.
- **Non-goals:** a YouTube embed, autoplay, a video sitemap, or the vertical cut.

## Decisions

| # | Decision | Why |
|---|---|---|
| D1 | Self-hosted `<video>`, not a YouTube embed | An embed loads Google's scripts and cookies, breaking D119 and the CSP on the very site that says "check our privacy" |
| D2 | `controls preload="none" playsinline`, a poster, and no `autoplay` or `loop` | Movement and light can be hard during or near a migraine. WCAG 2.2.2 applies to moving content longer than 5 s. With `preload="none"`, the 2 MB file loads only on play |
| D3 | `PROMO_VIDEO = ('migrely-promo-v3.mp4', 'migrely-promo-v3-poster.jpg')`, and `check_promo_video()` in `main()` | The year-long immutable cache needs a new name for every new cut (EC-WEB-20). A missing file stops the build, as the diary PDFs do |
| D4 | A section after the hero, "See Migrely in 30 seconds". The text alternative goes in a `<figcaption>` that the video names with `aria-describedby` | The video only has music, so the scenes need words for people who can't see it, and for search |
| D5 | `VideoObject` in the home `@graph`: name, description, `thumbnailUrl`, `contentUrl`, `uploadDate` 2026-10-30T12:00:00+05:00, `duration` PT30S | It describes the video to search engines. Google shows video thumbnails only when the video is the page's main content, so this is not a promise of a rich result |
| D6 | CSS `.video video { width: 100%; height: auto }`, with the `width`/`height` attributes kept for the aspect ratio | No layout shift, and no sideways scroll at 320 px |
| D7 | A visible "Download the video (MP4, 2 MB)" link in the `<figcaption>` | Content inside `<video>` shows only where `<video>` is not supported at all, not when the MP4 can't be decoded |
| D8 | A page's `lastmod` hash leaves out the stylesheet's version | A CSS change alone doesn't change what a page says (D136); without this, every page got a new date |

## Risks

- **The video goes out of date when the app changes.** The video's README lists its rules. Re-render, give it a new
  name, then update `PROMO_VIDEO`.
- **2 MB in a public repo.** The video is public on YouTube anyway, and the file is small.
