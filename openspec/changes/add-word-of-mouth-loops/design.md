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
in `try/catch` for `ActivityNotFoundException` and `SecurityException` (managed profiles). Share uses `Intent.createChooser(ACTION_SEND text/plain, title)` with
`EXTRA_TEXT`, `EXTRA_SUBJECT` (email apps) and `EXTRA_TITLE` (Android 10+ preview). No `resolveActivity`, no `<queries>`:
on API 30+ `startActivity` on an implicit intent needs no package visibility, and adding `<queries>` would change the
merged manifest. The share intent carries no `EXTRA_STREAM`, no `ClipData` URI and no grant flag (medical-reviewer W3).

**Tap guard (EC-SHR-05).** ux-designer checked: `LinkRow` has no guard, `TapGuard` only swallows touches after something
appears, and D159 (`SecondTapRule`) ignores a second tap only on a newly opened navigation entry. Share and Rate stay on
Settings, so D159 does not apply. A small helper in the settings ui module, shared by both rows: after an accepted
activation, both rows ignore activation until Settings is resumed again after leaving or, if it never pauses (split
screen, a failure), until `ViewConfiguration.getDoubleTapTimeout()` has passed. It sees touch, TalkBack double tap,
Switch Access and key repeat (filter `KeyEvent.repeatCount > 0`). No new design-system component.

**Messages.** Settings' host today holds only `notSavedAt: Long?`. It becomes a small message model (kind + stamp, like
`ShownReportMessageUiModel`): Error tone, 8 s base stretched by `messageTimeoutMillis()`, "Dismiss", saved across
rotation, cleared by Back (D178). A new message replaces the one showing. If an attack starts while one shows, it
finishes its time.

**Rows.** Existing `LinkRow` with `opensScreen = false` (no chevron: these leave the app) and a new optional
`onClickLabel` parameter for the TalkBack action names; existing rows pass null.

### D2. The credit line geometry fits the bottom margin

On the last page only, the credit line is a second footer line drawn at line 1's top plus line 1's measured
`StaticLayout.height` (not a hardcoded number), from `MARGIN`, width
`contentWidth` (A4 515 pt, Letter 532 pt), same `smallPaint`. With 7.5 pt type and 1.1 spacing a line is about 9–10 pt,
so line 2 ends about 35 pt above the page edge: inside the 40 pt bottom margin, clear of printers' unprintable band
(typically 4–6 mm, about 12–17 pt). ux-designer measured the final text (86 characters) at 287.3 pt in Roboto Regular
7.5 pt, line height 9.67 pt with the 1.1 spacing, credit bottom 35.5–36.4 pt above the edge on both papers: 54–56% of
the content width, so a wider OEM font still fits. Accepted residual: a printer whose unprintable bottom band is 12.7 mm
or more may clip the credit at 100% scale; the disclaimer (bottom about 45 pt) is unchanged and nothing clinical is
lost. Pagination is untouched: `bottom` stays
`height − MARGIN − FOOTER_HEIGHT`, so the page count cannot change. The renderer gets `credit: String?` on
`ReportDocumentDataModel` (null = off); the text comes from `ReportDocumentMapper` like `footer`. A test lays out the
final approved text on A4 and Letter and asserts `lineCount == 1` and no ellipsis, so a longer approved text fails the
build instead of the doctor's page.

### D3. The switch is a preference with its own small repositories

`report_credit_shown` in the preferences file, default true (absent = true, so upgrades and "Delete all data" give on).
Report domain: `GetReportCreditRepository`, `SetReportCreditRepository`, one use case each; `ReportViewModel` exposes the
value in its state and an action to change it; `CreateReportUseCase` passes it into the document. The switch reuses the
report's `NotesRow` pattern, made to take a title and a summary (DRY). It is hidden while loading, disabled while a
report is made, and a failed write reverts it and shows the new report message "Couldn't save that. Try again."
(EC-REP-12, EC-REP-13). The summary is built from the same constant the mapper uses, or written out with a test asserting
equality (medical-reviewer W6). The preferences file is
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

`SettingsViewState` gets `showShareAndRate` from the attack-in-progress flow it already reads for the theme; false while
that state is unknown (loading). The header and both rows go together, so no empty header is left. Motion: fade plus
collapse or expand, 200 ms, none on first composition, instant with "Remove animations"; if this feels like two changes
next to the dark theme flip on the device, remove the section instantly instead (ux §8.4).

Focus (ux-designer §4): when the section leaves and TalkBack or keyboard focus was on its header or a row, focus moves to
"Open-source licences" (both TalkBack and the keyboard can land there; "Version" is not keyboard-focusable), never to
"Delete all data", which slides into the gap. No announcement. Fallback if Compose cannot move accessibility focus there:
the "Settings" title. When the section returns, focus stays put.

### D6. The share link is a single constant with a test against the table

The URL string lives once, in the settings ui module (non-translatable). A unit test asserts it equals the table's
"In-app Settings, Share Migrely" URL and that the decoded `referrer` equals the top-level tags. The test copies the
string from the table at implementation time; if `aso-listing` changes the table later, it must update the app in a
change of its own.

### D7. Promo-code verification protocol (phone only)

Who: `android-engineer` with the owner present, on the owner's phone. Build: the live
Play build installed from Play (no upload, owner order of 2026-11-06). Account: a Google account on the phone that owns no
Migrely purchase. The owner creates one promotion with lifetime codes in Play Console (owner step). Cases:

1. Migrely force-stopped, code redeemed in Play Store, Migrely opened: Premium on? acknowledged (Play Console order shows
   acknowledged)?
2. Migrely in the background (opened less than 15 minutes earlier), code redeemed, returned: Premium on without Restore?
3. Optional, needs a second code and 3+ days: redeemed, Migrely not opened for 3 days: refunded and Premium off at the
   next open, data intact.

Record the result in `tasks.md`. A failure becomes a GitHub issue with steps and evidence; it does not block the rest of
this change.

## Final copy (ux-designer §7; medical-reviewer: 9 of 9 PASS; legal DRAFT: acceptable)

| Key | Final text | Reviewed by |
|---|---|---|
| `settings_section_share` | Share and rate | ux, team-lead |
| `settings_share` | Share Migrely | ux |
| `settings_share_summary` | Send someone the link to Migrely. Your entries aren't included. | ux, medical (W3), legal |
| `settings_share_action` (onClickLabel) | choose an app to share with | ux |
| `settings_rate` | Rate Migrely on Google Play | ux |
| `settings_rate_summary` | Opens Migrely's store page, outside the app | ux |
| `settings_rate_action` (onClickLabel) | open Google Play | ux |
| `settings_share_text` (non-translatable, `%1$s` = link) | Migrely is a private migraine diary for Android. No internet permission, no account, no ads.\n%1$s | ux, medical (W1), legal; option A, team-lead |
| `settings_share_subject` (`EXTRA_SUBJECT`, `EXTRA_TITLE`) | Migrely, a migraine diary for Android | ux, medical (W2) |
| `settings_share_chooser` | Share Migrely | ux |
| `settings_play_unavailable` | This phone has no app that can open Google Play. | ux, medical (W7) |
| `settings_share_unavailable` | This phone has no app to share the link with. | ux, medical (W8) |
| PDF credit (`ReportDocumentMapper` constant) | Made with Migrely, a personal migraine diary app. Not a medical device. migrely.health | ux, medical (W4), legal; "Not a medical device" kept (D104), team-lead |
| `report_credit_switch` | Mention Migrely in the report | ux, medical (W5) |
| `report_credit_switch_summary` | One small line on the last page: "%1$s" (`%1$s` = the credit constant) | ux, medical (W6) |
| `report_message_not_saved` | Couldn't save that. Try again. | ux, medical (W9) |

The share text stays third person (Migrely puts no words in the sender's mouth) and reuses store wording approved under
D179. "Stays on your phone" stays rejected (D179 (2)). The final `strings.xml` and the mapper constant must match this
table word for word; the medical verdicts lapse if they drift.

### D8. Work outside the app code, tracked in this change

- **Gift message (D187 (5)).** Lifetime codes only, never for or after a rating or review. The message carries legal's
  four elements: (a) a gift, nothing expected, no rating or review asked, a rating earns nothing; (b) disclose the gift
  clearly, at the start, naming the giver; (c) never describe Migrely as treating, preventing or reducing migraines;
  (d) how to redeem, open Migrely within 3 days, "Restore purchases". The message, recipients and sending are kept
  outside the repo; the owner sends.
- **Privacy policy and Data safety (D185 (9)).** `aso-seo-specialist` drafts legal's P1–P3 (the PDF line and its switch,
  the two new stored marks, a "Sharing and rating Migrely" section), a new effective date, the regenerated HTML and the
  `data-safety.md` "Ratings" bullet, in a reviewed site PR. `legal-finance-analyst` re-reads, the **owner approves the
  policy text before merge**, and the owner's deploy makes it live before 1.4.0 reaches users. The Data safety form answer
  ("No data collected / shared") does not change.

## Risks

- **Doctors may read the credit as an endorsement or as clutter.** No evidence either way. Mitigation: last page only,
  footer band, same grey as the disclaimer, "not a medical device", user switch, reversible default (D186 (2)).
- **The PDF line is unmeasured.** Accepted; only Play-link UTM is counted.
- **Share attribution may be lost** in messengers (EC-SHR-10). Accepted; Play folds small values into "Other" anyway.
- **The safety mark touches three features.** Each write is one call at the moment the card becomes visible; a missed
  host means a review could still follow that card. QA must walk all three.
- **Promo verification depends on the owner and the phone.** If not done before the 1.4.0 Go/No-Go, the release note
  says gift codes are untested; gifts wait.
- **String drift.** The medical verdicts hold only while `strings.xml`, the mapper constant and the switch summary match
  the final copy table; the W3 and W6 tests guard the two that can silently go false.
- **TalkBack focus move** when the section leaves may not be possible with `FocusRequester` alone (ux §4); the fallback
  is the "Settings" title, never "Delete all data".
- **The policy must be live before users get 1.4.0.** The site PR and the owner's approval and deploy are outside the
  app; the 1.4.0 Go/No-Go checks the live policy text.
- **Legal is a DRAFT** by an AI analyst; a real lawyer (legal L1–L9) is still advised before 1.4.0 and before the first
  gift. Owner decision, through team-lead.
