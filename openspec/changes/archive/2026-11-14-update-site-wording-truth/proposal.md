# Proposal

## Why

On 2026-11-13 and 14 `medical-reviewer` and `legal-finance-analyst` tightened the Instagram ad wording: "calm" reads as a
promise, "while you rest" and "your migraines share" drop the app's own hedges, and "no internet access" or "nothing
leaves" overstate privacy next to Android's backup and the user's own exports. The same words were still on the site,
in the Play listing, in the privacy policy and in the store art. Truth does not depend on the channel, so the T65
branch aligned every channel (BRD D179, EC-WEB-25, EC-WEB-26, §13 row 47). The `website` spec still said "no internet
access" on the menstrual page and had no rule for re-rendered images, so this delta records both.

## What Changes

- **A truth rule for every page,** `/llms.txt` and alt text: no "calm" or "quiet" for Migrely or its sky, triggers keep
  "often logged with", the privacy claim is "no internet permission, so the app cannot send entries over the
  internet", and what is free is listed with no time promise (owner's option B, 2026-11-14).
- **Second person stays allowed on organic pages.** The stricter ad rule (`BANNED_AD`) stays on `/android/` only.
- **Re-rendered images get new names:** `01-log-v2`, `02-sky-v3`, `04-insights-v2` and `og-v2`. The store PNG keeps its
  Play name, the site drops the `-vN` suffix to find it, and old files stay for caches.
- **The menstrual page** says "no internet permission" and names Insights' 30-day gate.
- **The promo transcript** keeps describing promo v3.3 until v3.4 replaces it (EC-WEB-26).

Already on the branch (aso-seo-specialist): `website/build.py` and its output, `docs/release/listing.md`,
`privacy-policy.md` and `.html` (effective date 14 November 2026), `data-safety.md`, and the store art PNGs.

## Out of scope

- The Play listing edit and the store art upload: held until the 1.2.1 review result and the owner's go.
- Promo v3.4 and its transcript (`content-creator`).
- The app's Settings privacy string ("sends nothing anywhere"), its Maestro assertion and the slide 06 re-capture:
  1.3.0, T63.
- The `/privacy-check/` path walk on current Android and Samsung phones (`qa-engineer`).
- Any clinical or legal sign-off (T31).

## Capabilities

### Modified Capabilities

- `website`: adds "Truthful wording on every page" and "Changed images get new names", and modifies "Menstrual
  migraine page".

## Impact

- `website/build.py`, `website/public/`, `website/lastmod.json`.
- `docs/release/listing.md`, `privacy-policy.md`, `privacy-policy.html`, `data-safety.md`, `store/`.
- `docs/BRD.md`: D179, EC-WEB-25, EC-WEB-26, §13 row 47, §14.2.
- No app code, no permission, no network.
