# Proposal

## Why

Ad test `ig-ads-test1` sent 78 landing-page views to `/android/`, and at most 16 of them reached Google Play (26% or
less, 95% interval 17 to 38%; audit `marketing/ads/audits/2026-11-23.md`, call FIX). Nothing counts taps on the Play
button, so "did not tap" (a page problem) cannot be told from "tapped, Play lost it" (a link or technology problem). The
hand-off works on the owner's Xiaomi through Instagram's in-app browser, so the loss is somewhere we cannot see.

Test 2 (planned 8 to 15 December) needs a page that gets people to the button with fewer words, and a count of the taps.
Owner decision 2026-11-23 (chat, "Buni ham Professional tarzda Bajaraylik"): count taps too. It covers designing,
building and checking; deploying stays the owner's click.

**Measured before building:** `/android/`'s Play button is already above the fold at 320x568, 360x640 and 412x915 (button
bottom at 345 px of 640), so "the button was off screen" is not the cause of test 1's loss. The new page moves the
button up by only 11 to 40 px; its value is the tap count, the shorter lead-in and the Android-only line first.

## What Changes

- **`/android-t2/`** (new, `noindex`, off the sitemap and every navigation, no script): app name, headline, "For Android
  8 and later. Not available for iPhone.", one full-width Play button, then the rest of the cleared lines. Every text
  segment already exists on `/android/` or `/instagram/`; 17 words before the button (32 on `/android/`).
- **`/go/android-t2`** (new): one Cloudflare Pages Function that answers `302` to the test-2 Play link (`utm_*` and
  `referrer` unchanged) and reads, stores and calls nothing. The count is Cloudflare's own Functions metrics.
- **`T2_COUNT`** in `website/build.py`: one value that returns the two buttons to the direct Play link and requires
  `website/functions/` to be empty.
- A desktop QR code that encodes the Play link directly (decoded and negative-controlled).
- `robots.txt` gets `Disallow: /go/`.
- Build checks for all of it (exact link, one Function only, no reading or storing in it, cleared words only, at most 20
  words before the button, no other page links to `/go/`), each with a negative control.
- `docs/release/play-links.md` gets the `ig-ads-test2` row; `website/README.md` gets the deploy rule for the Function.

**Out of scope:** the privacy-policy text (`legal-finance-analyst`'s), any deploy, Play Console, the ad and its copy,
`/android/` (stays byte for byte), any app code.

## Capabilities

Modified: `website`.

## Impact

`website/build.py`, `website/functions/go/android-t2.js`, `website/public/` (new page, QR, `robots.txt`),
`website/lastmod.json`, `website/README.md`, `docs/release/play-links.md`, `docs/BRD.md`. BRD: D196, EC-WEB-03 (changed),
EC-WEB-27 to EC-WEB-29, change plan row 59.
