# Design

## Context

Facts read at `develop` 50c784e5 (code read, not run):

- Settings already observes "attack in progress" (`GetAttackInProgressRepository`, used by `GetAppearanceUseCase` for the
  dark theme) and has a message host (`settings_not_saved`). External links today go through
  `UriHandler.openInBrowserIfAny` in `SettingsPages.kt`, which swallows a missing browser; that is fine for the privacy
  link, whose address is written out, but a silent no-op row is what Google rejected 1.0.1 for (D153).
- The doctor PDF is drawn by `ReportPageLayout` (`datasource/implementation`) with `PdfDocument`: `MARGIN` 40 pt,
  `FOOTER_HEIGHT` 28 pt, footer text 7.5 pt dark grey, line spacing 1.1. The footer's first line starts at
  `height − MARGIN − FOOTER_HEIGHT / 2` (= height − 54). Left cell: "Patient-entered data. Not a diagnosis." at 2/3 of
  the content width; right cell: "Page N of M" at 1/3. Paper is A4 (595 × 842) or Letter (612 × 792, US and CA). The text
  comes from `ReportDocumentMapper` (`footer`, `pageLabel`), English only.
- `ShouldRequestReviewUseCase` checks requests, paywall gap, previous crash, migraine count and attack in progress. It
  does not know about safety cards. Hosts of the three cards: `AttackDetailsScreen` (first-10, SAF-F2), home's attack mode
  (`long_check_*`, SAF-F3), `MedicationPromptCard` in home's today mode (overuse, EC-MED-06).
- Billing: `PlayBillingDataSourceImpl` has a `PurchasesUpdatedListener` that emits to `PurchaseUpdatesWatcher`
  (application scope); `PremiumStartChecks.onResume` refreshes, throttled to once per 15 minutes (D106);
  `EntitlementRules` marks an unacknowledged purchase and acknowledges it. No code path is specific to purchases made
  outside the app, and none has been tested with one.
- The Play links: `docs/release/play-links.md` on `feature/listing-migraine-diary` (D184), owned by `aso-seo-specialist`.

## Goals and non-goals

Goals: two honest, permanent, quiet Settings rows; one factual provenance line on the PDF the person can switch off; no
review request just after a safety card; proof that a gifted lifetime code works.

Non-goals: measuring anything in the app, prompting, rewarding, QR codes, custom listings, sky-image sharing, new review
moments.

## Decisions

### D1. Intents live in the ui layer, behind a tiny result

| Option | Verdict |
|---|---|
| Intents in the Settings ui module, called from the screen; the ViewModel only emits "open rate page" / "open share" navigation events | **Chosen.** Same as the report's share (`ReportScreen.share`). Intents are Android, not domain. |
| A domain use case and a datasource for "open Play" | Rejected: no business rule, no data. Over-built. |

The ui helper returns `Opened` or `NothingCouldOpen`; the screen shows Settings' message on the second. Rate tries
`market://details?id=com.migrely.app` with `setPackage("com.android.vending")`, then the https URL without a package, each
in `try/catch (ActivityNotFoundException)`. Share uses `Intent.createChooser(ACTION_SEND text/plain, title)` with
`EXTRA_TEXT`, `EXTRA_SUBJECT` (email apps) and `EXTRA_TITLE` (Android 10+ preview). No `resolveActivity`, no `<queries>`:
on API 30+ `startActivity` on an implicit intent needs no package visibility, and adding `<queries>` would change the
merged manifest. Double taps use the existing 300 ms guard of D159 (check that `LinkRow` already has it; if not, use the
design-system helper the other rows use).

### D2. The credit line geometry fits the bottom margin

On the last page only, the credit line is a second footer line drawn at `top + lineHeight(line 1)`, from `MARGIN`, width
`contentWidth` (A4 515 pt, Letter 532 pt), same `smallPaint`. With 7.5 pt type and 1.1 spacing a line is about 9–10 pt,
so line 2 ends about 35 pt above the page edge: inside the 40 pt bottom margin, clear of printers' unprintable band
(typically 4–6 mm, about 12–17 pt). The draft text (about 88 characters) measures roughly 330 pt at 7.5 pt sans, so it
fits one line on both papers with room for small wording changes. Pagination is untouched: `bottom` stays
`height − MARGIN − FOOTER_HEIGHT`, so the page count cannot change. The renderer gets `credit: String?` on
`ReportDocumentDataModel` (null = off); the text comes from `ReportDocumentMapper` like `footer`. A test lays out the
final approved text on A4 and Letter and asserts `lineCount == 1` and no ellipsis, so a longer approved text fails the
build instead of the doctor's page.

### D3. The switch is a preference with its own small repositories

`report_credit_shown` in the preferences file, default true (absent = true, so upgrades and "Delete all data" give on).
Report domain: `GetReportCreditRepository`, `SetReportCreditRepository`, one use case each; `ReportViewModel` exposes the
value in its state and an action to change it; `CreateReportUseCase` passes it into the document. The preferences file is
already in Auto Backup and already cleared by "Delete all data" (check `DeletePreferencesRepository` clears everything,
not a key list; if it is a list, add the key). JSON export is built from Room records, so it does not change: prove it
with the existing backup round-trip test, unchanged.

### D4. The safety-card mark

`safety_card_last_shown` (epoch millis) in the preferences file, next to `review_last_requested`. Written when a card
becomes visible (not when dismissed) by each host through its feature's own one-function repository
(`MarkSafetyCardShownRepository` in attack, home and medication domains; one datasource function). Review domain gets
`GetLastSafetyCardShownRepository`; `ShouldRequestReviewUseCase` adds `longSinceSafetyCard(last, now)` with the same
shape and 3-day gap as `longSinceLastPaywall` (a future stamp counts as recent). Read order: after the paywall check,
before the crash read. A read failure counts as "recent" (nothing asked), like the other reads.

Rejected: deriving it from records ("a 10 logged in the last 3 days"): it misses the overuse card and the 72-hour card,
and does not mean "the person saw a warning".

### D5. Hiding the rows during an attack

`SettingsViewState` gets `showShareAndRate` from the attack-in-progress flow it already reads for the theme. The section
header and both rows are removed together, so no empty header is left. If an attack starts while Settings is open (from
the notification), the rows disappear with the theme change; focus, if it was on a removed row, moves to the previous
row (ux-designer to confirm).

### D6. The share link is a single constant with a test against the table

The URL string lives once, in the settings ui module (non-translatable). A unit test asserts it equals the table's
"In-app Settings, Share Migrely" URL and that the decoded `referrer` equals the top-level tags. The test copies the
string from the table at implementation time; if `aso-listing` changes the table later, it must update the app in a
change of its own.

### D7. Promo-code verification protocol (phone only)

Who: `android-engineer` in the main session with the owner present (handbook §6, phone `14634fa97d7c`). Build: the live
Play build installed from Play (no upload, owner order of 2026-11-06). Account: a Google account on the phone that owns no
Migrely purchase. The owner creates one promotion with lifetime codes in Play Console (owner step). Cases:

1. Migrely force-stopped, code redeemed in Play Store, Migrely opened: Premium on? acknowledged (Play Console order shows
   acknowledged)?
2. Migrely in the background (opened less than 15 minutes earlier), code redeemed, returned: Premium on without Restore?
3. Optional, needs a second code and 3+ days: redeemed, Migrely not opened for 3 days: refunded and Premium off at the
   next open, data intact.

Record the result in `tasks.md`. A failure becomes a GitHub issue with steps and evidence; it does not block the rest of
this change.

## Copy for ux-designer and medical-reviewer (drafts; English, final wording theirs)

| Key (suggested) | Draft | Needs |
|---|---|---|
| `settings_section_share` | Share and rate | ux |
| `settings_share` | Share Migrely | ux |
| `settings_share_summary` | Send a link to Migrely on Google Play | ux |
| `settings_rate` | Rate Migrely on Google Play | ux |
| `settings_rate_summary` | Opens Migrely's page in Google Play | ux |
| `settings_share_text` | Migrely is a private migraine diary for Android. No internet permission, no account, no ads. + newline + link | ux, **medical-reviewer**, **legal-finance-analyst** (privacy claim, D179 (2)) |
| `settings_share_subject` | Migrely, a migraine diary for Android | ux, **medical-reviewer** |
| `settings_share_chooser` | Share Migrely | ux |
| `settings_play_unavailable` | Couldn't open Google Play on this phone. | ux |
| `settings_share_unavailable` | No app on this phone can share this. | ux |
| PDF credit (mapper constant) | Created with Migrely, a personal migraine diary app. Not a medical device. migrely.health | ux, **medical-reviewer**, **legal-finance-analyst** |
| `report_credit_switch` | Add a Migrely credit to the report | ux, **medical-reviewer** if it quotes the credit |
| `report_credit_switch_summary` | One small line at the end: "Created with Migrely…" | ux |

The share sentence reuses store wording already approved under D179 (feature graphic, screenshot 06). The research's
"stays on your phone" is rejected: D179 (2) bans "nothing leaves"-type claims.

## Risks

- **Doctors may read the credit as an endorsement or as clutter.** No evidence either way. Mitigation: last page only,
  footer band, same grey as the disclaimer, "not a medical device", user switch, reversible default (D186 (2)).
- **The PDF line is unmeasured.** Accepted; only Play-link UTM is counted.
- **Share attribution may be lost** in messengers (EC-SHR-10). Accepted; Play folds small values into "Other" anyway.
- **The safety mark touches three features.** Each write is one call at the moment the card becomes visible; a missed
  host means a review could still follow that card. QA must walk all three.
- **Promo verification depends on the owner and the phone.** If not done before the 1.4.0 Go/No-Go, the release note
  says gift codes are untested; gifts wait.
- **The section name "Share and rate"** may change after ux-designer; the spec's settings list must then be updated in
  the same PR.
