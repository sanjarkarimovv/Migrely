# Proposal

## Why

The owner asked on 2026-11-16 for the three most useful free-growth steps. This change is step 2: word of mouth from inside
an app that has no internet permission, no analytics and no account. The research report (`reports/Migrely bepul auditoriya
kanallari.md`, "Ilova ichidagi uchta halqa…") found three honest loops: a share link that Play Console counts by UTM, a
permanent "rate" link, and a small credit line on the doctor PDF. Play promo codes are a fourth, manual lever.

The person: someone whose migraine diary already helped them at a doctor visit, who wants to tell a friend in the same
situation. Today they have to find Migrely in Play themselves. The person who must never be bothered: someone in an
attack, in a dark room, who opened Settings to switch something off. Nothing here may appear by itself, ask, reward or
count.

The research did not know that In-App Review ships since 1.1.0 (D129–D132, `in-app-review` spec). Reading
`ShouldRequestReviewUseCase` at `develop` 50c784e5 shows one conflict with the research's rule "never right after a safety
warning": the app checks attacks, paywalls, crashes and counts, not safety cards. After the first-10 card or "Still going?"
the attack can end, home returns to today's mode and Google's sheet can open 2 seconds later. The rest of D129–D132
matches Google's current rules (no pre-question, no button, no change to the card, after value, quota respected).

## What Changes

- **Settings, "Share and rate"** (new section after "Privacy and about", before "Delete all data"; name confirmed by
  ux-designer and team-lead):
  - "Share Migrely": the system share sheet with plain text, one neutral sentence and the Play link from `aso-listing`'s
    table (`utm_source=app`, `utm_medium=share`, `utm_campaign=settings_share`, top-level and inside `referrer=`).
  - "Rate Migrely on Google Play": opens Migrely's Play page (`market://` in the Play app, else the web page in a browser).
    Not the review API.
  - Both hidden while an attack is in progress (and while the attack state is still loading). If nothing can open them,
    Settings shows its own message; never silent.
  - One shared tap guard: after a tap on either row, both ignore taps until Settings resumes or the double-tap timeout
    passes. D159 does not cover this (it guards only a newly opened screen).
  - A share is never rewarded, unlocks nothing and is not counted, so it stays the person's own choice and outside
    endorsement rules (team-lead).
- **Doctor report credit line**: on the last page only, a second footer line under "Patient-entered data. Not a
  diagnosis.", same small grey type, full content width, no logo, QR or link: "Made with Migrely, a personal migraine
  diary app. Not a medical device. migrely.health". On by default; the switch "Mention Migrely in the report" on the
  Doctor report screen turns it off, and its summary quotes the line. A failed save reverts the switch and says
  "Couldn't save that. Try again.". Page count unchanged.
- **In-App Review quiet after a safety card**: no request within 3 days after the first-10 card, "Still going?" or the
  overuse card appeared.
- **Promo codes**: a verification on the phone, with the owner, that a lifetime code redeemed in Play Store (outside the
  app) turns Premium on and is acknowledged. Gifts use lifetime codes only, with a gift message that has legal's four
  elements (`content-creator` drafts, the owner sends).
- **Before 1.4.0 reaches users** (not app code, tracked here): privacy policy additions P1–P3 and the `data-safety.md`
  "Ratings" bullet, through a reviewed site PR that the owner approves.
- **BRD**: D185–D188, SET-F2, REP-F5, SUB-F3, EC-SHR-01…13 (new §9.16), EC-REP-05…13, EC-BIL-21…25, EC-REV-14, §13 row
  52, §14.4 line.

## Out of scope

- Any prompt, banner, badge, notification or reminder to share or rate; any reward, unlock or counter for either.
- Two-sided referral rewards (unverifiable without a server), the Play Install Referrer library, `IntentSender` callbacks
  that learn the chosen target, and any `<queries>` manifest entry.
- A QR code or clickable link in the PDF (`PdfDocument` has no link annotations; a QR needs a new encoder). The PDF line
  is therefore unmeasured, accepted by `aso-listing`.
- A `&listing=` custom store listing (none exists; `aso-listing` decides later, D184).
- Sharing the month sky image (§18.7): its own change, it carries health data.
- New review moments (after an export, after a PDF). D130's moments stay; only the safety-card condition is added.
- Changing the existing footer text, the PDF content, JSON or CSV exports, `schemaVersion`, Room schema, permissions or
  dependencies.
- Creating promo codes, choosing recipients, writing gift messages (owner, `team-lead`, `content-creator`).
- Release 1.3.0. This ships in the next minor, 1.4.0 (team-lead). It must not hold 1.3.0's Go/No-Go.

## Capabilities

- **Modified:** `app-settings` (the settings list), plus new requirements "Rate Migrely on Google Play", "Share Migrely",
  "Share and rate stay out of the way".
- **Modified:** `doctor-report`: new requirements "The Migrely credit line", "The credit switch".
- **Modified:** `in-app-review`: new requirement "Quiet after a safety card".
- **Modified:** `subscription`: new requirement "A purchase made outside the app".

## Impact

- Modules: `:features:settings:*` (rows, attack state already observed), `:features:report:*` (switch, credit text),
  `datasource` (`ReportDocumentDataModel` credit field, `ReportPageLayout` last-page line, preferences: credit switch and
  safety-card mark), `:features:review:*` (one condition), the three safety-card hosts (`attack`, `home`, `medication`)
  write the mark, `:app` wiring.
- No new dependency, permission, manifest entry, network call or analytics. Evidence: `:app:checkReleasePermissions` and a
  release merged-manifest diff against `develop`.
- Copy: final English strings from `ux-designer` (`migrely-work/word-of-mouth/ux-spec.md` §7); all 9 health or privacy
  strings PASS `medical-reviewer` (`medical-verdict-strings-2026-11-16.md`), and `legal-finance-analyst`'s DRAFT
  (`reports/2026-11-16-legal-loops-strings.md`) finds them acceptable. Lead decisions: share sentence option A verbatim,
  credit on by default, "Not a medical device" kept (D104).
- Site and store documents: `website/` privacy policy and `docs/release/data-safety.md`, owned by `aso-seo-specialist`,
  in their own reviewed PR.
- BRD ids: D185, D186, D187, D188; SET-F2, REP-F5, SUB-F3; EC-SHR-01…13, EC-REP-05…13, EC-BIL-21…25, EC-REV-14.
