# Tasks

Base: `feature/add-word-of-mouth-loops` from `origin/develop` 50c784e5; `origin/develop` 03a8900e (D184 listing, the Play
link table `docs/release/play-links.md`) merged in at bb243a75. Release: 1.4.0, the minor after 1.3.0 (team-lead). It must
not hold 1.3.0's Go/No-Go. BRD D185–D188.

Ownership:
- `product-manager`: 0, 1.3 and 9;
- `ux-designer`: 1.1;
- `medical-reviewer` and `legal-finance-analyst`: 1.2;
- `android-engineer`: 2–6 and 7 (7.3 with the owner, main session);
- `code-reviewer`: 8.1; `qa-engineer`: 8.2 (emulator-5580 API 36, emulator-5582 API 26, one at a time);
- `content-creator`: 10.1 (the owner sends); `aso-seo-specialist`, `legal-finance-analyst` and the owner: 10.2.

Gate for every code task: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0 (run
with `set -o pipefail`; never `testDebugUnitTest` alone).

Strings: use design.md "Final copy" word for word. The medical verdicts lapse if `strings.xml`, the mapper constant or the
switch summary drift from it.

## 0. Plan (`product-manager`)

- [x] 0.1 BRD D185–D188, SET-F2, REP-F5, SUB-F3, EC-SHR-01…11 (§9.16), EC-REP-05…11, EC-BIL-21…25, EC-REV-14, §13 row 52,
  §14.4. Verify: `openspec validate add-word-of-mouth-loops --strict` exits 0.

## 1. Copy and flow (before code)

- [x] 1.1 `ux-designer`: done 2026-11-16, `migrely-work/word-of-mouth/ux-spec.md` (section name "Share and rate" and
  position confirmed, rows without chevron, TalkBack action names, the two failure messages, the report switch "Mention
  Migrely in the report" after "Include notes", focus rule, the shared tap guard, PDF placement by measured line height,
  final strings in §7). Code read, not run.
- [x] 1.2 `medical-reviewer`: `migrely-work/word-of-mouth/medical-verdict-strings-2026-11-16.md`, "All 9 strings PASS. No
  CHANGE, no FAIL.", with three conditions: W3 text-only share intent asserted, W6 switch summary equals the credit
  constant asserted, and a switch-off negative control that the disclaimer stays on every page (tasks 5.1, 5.2, 8.2).
  `legal-finance-analyst`: `reports/2026-11-16-legal-loops-strings.md` (DRAFT), strings acceptable; gift-message elements
  (10.1) and policy additions P1–P3 (10.2). team-lead decisions: share option A verbatim, credit on by default, "Not a
  medical device" kept (D104), section name stays "Share and rate", shares never rewarded, unlocked or counted.
- [x] 1.3 `product-manager`: final strings and rules in the BRD (D185 (8)–(9), D186 (5), D187 (5), EC-SHR-05 reworded,
  EC-SHR-12, EC-SHR-13, EC-REP-12, EC-REP-13), the spec deltas, design and these tasks. Verify:
  `openspec validate add-word-of-mouth-loops --strict` exits 0.

## 2. Domain

- [x] 2.1 review: `GetLastSafetyCardShownRepository`; `ShouldRequestReviewUseCase` adds the 3-day safety gap (design D4),
  future stamp = recent, unreadable = recent. Tests first, red then green: within 3 days, exactly 3 days, future stamp,
  unreadable, absent. Verify: `./gradlew :features:review:domain:test`.
  Done 2026-11-16 (`eng-loops`, 5b4fb49f). Red first: with the repository injected and the rule absent, 5 of 31 review domain tests failed (an hour ago, just under 3 days, future stamp, unreadable, crash/migraines not read); green after. Unreadable is a sealed `Unknown`, not null. Also re-checked in `RequestReviewUseCase` just before the flow opens (a card that appears while Google Play prepares). `Refs #233`.
- [x] 2.2 attack, home, medication: `MarkSafetyCardShownRepository` (one function each) and the call where the card
  becomes visible (presentation decides, domain marks). Tests per feature. Verify:
  `./gradlew :features:attack:domain:test :features:home:domain:test :features:medication:domain:test`.
  Done (410bd38f): one `MarkSafetyCardShownRepository` + use case per feature, `SafetyCardRepository` per data module (stamp from the clock, a failed write dropped). Domain and data tests per feature.
- [x] 2.3 report: `GetReportCreditRepository`, `SetReportCreditRepository` (with a failure result), use cases;
  `CreateReportUseCase` passes the choice. Tests. Verify: `./gradlew :features:report:domain:test`.
  Done (73ecda05): `SetReportCreditUseCase` with `ReportCreditWriteResultDomainModel`; the stored value comes with `GetReportOptionsUseCase` (one Ready state, so the switch shows only once read); `CreateReportRequestDomainModel.includeCredit` is the switch as shown at the tap.

## 3. Presentation

- [x] 3.1 settings: `showShareAndRate` from the attack flow, false while unknown; actions `onShareAction`, `onRateAction`
  emitting navigation events; the two failure notifications with a stamp (design D1 "Messages"). Tests, including
  "loading", "attack starts while open" and "attack ends while open". Verify:
  `./gradlew :features:settings:presentation:test`.
  Done (8c5087ab). Deviation, reason: Share and Rate are notifications to the screen (the report's share pattern), not navigation events, because a failure must reach Settings' own message host and `:app` would hold the intents. The attack state is read with the settings (null = unknown = hidden); `:app` passes its live attack state so Settings reads again on a change; latest read wins (test with out-of-order answers).
- [x] 3.2 report: credit switch in the view state (hidden while loading, disabled while working) and its action; a failed
  save reverts the value and emits the "not saved" message. Tests. Verify: `./gradlew :features:report:presentation:test`.
  Done (73ecda05): `onCreditAction(on, period)`; a failed save notifies `NotSaved` and reloads, so the switch shows the stored value.
- [x] 3.3 attack, home, medication presentation: call the mark when the card's state first becomes visible, once per
  appearance (rotation does not mark again). Tests. Verify: `./gradlew :features:attack:presentation:test
  :features:home:presentation:test :features:medication:presentation:test`.
  Done (410bd38f). First-10: on its one-off event, so once per appearance. "Still going?" and the overuse card: on every load that shows them. Deviation, reason: rotation reloads home and marks again with a newer time; a mark kept only per appearance would go stale while the card stays on screen across days (resume without leaving), and a review could then follow a visible card. Flagged for code-reviewer.

## 4. Data and datasource

- [x] 4.1 preferences: `report_credit_shown` (absent = true) and `safety_card_last_shown`; "Delete all data" removes
  both (check design D3). Tests in the datasource module. Verify: `./gradlew :datasource:implementation:test`.
  Done (5b4fb49f): keys `safety_card_last_shown`, `report_credit_shown` (absent = true, tested with an empty file). `clear()` clears the whole file (existing test), so "Delete all data" removes both.
- [ ] 4.2 `ReportDocumentDataModel.credit: String?`; `ReportDocumentMapper` fills the credit constant when on;
  `ReportPageLayout` draws it on the last page only, at line 1's top plus line 1's measured height (design D2). Tests:
  mapper on and off; instrumented layout test on A4 and Letter: `lineCount == 1`, `getEllipsisCount(0) == 0`, same page
  count on and off, absent on pages before the last. Verify: `./gradlew :features:report:data:test` and
  `ANDROID_SERIAL=emulator-5570 ./gradlew :datasource:implementation:connectedDebugAndroidTest`.
  Code and JVM tests done (73ecda05): mapper on and off (final text word for word), `footerLines()` per page. `ReportCreditLayoutTest` (androidTest, 4 tests: one line and no ellipsis on A4 and Letter, same page count, pixel diff only on the last page and only in the band below the disclaimer) is written but NOT RUN: emulator-5570 was not booted (host swap above 6 GB). Tick after the connected run.
- [ ] 4.3 Disclaimer with the switch off (medical-reviewer condition, unit level): the rendered text of every page of a
  multi-page report with `credit = null` contains "Patient-entered data. Not a diagnosis." and no "Made with Migrely".
  Verify: the same connected test run as 4.2, test name in the record.
  Unit level done: `ReportFooterLinesTest` "Given the switch off then every page has the disclaimer and none the credit" (7-page report). Rendered check written, NOT RUN: `ReportCreditLayoutTest.givenTheSwitchOff_thenEveryPageStillHasTheDisclaimer` (disclaimer band differs from a blank footer on every page, A4 and Letter). Tick after the connected run.
- [x] 4.4 JSON and CSV unchanged: the existing backup round-trip and CSV tests pass unmodified. Verify:
  `git diff origin/develop -- '**/backup/**/test/**'` is empty and `./gradlew :features:backup:data:test` passes.
  Done: `git diff origin/develop -- features/backup` is empty (0 lines); `:features:backup:data:test` passes in the gate.

## 5. UI

- [x] 5.1 settings ui: the section and two rows (`LinkRow`, `opensScreen = false`, new optional `onClickLabel`), the
  intent helper with its result (design D1, `ActivityNotFoundException` and `SecurityException`), the shared tap guard
  (design D1, EC-SHR-05), the share-link constant and its test against the table (design D6), the two messages, the focus
  rule (design D5). Tests:
  - **W3 (medical-reviewer condition):** the built share intent has action `ACTION_SEND`, type `text/plain`, no
    `EXTRA_STREAM`, no `ClipData` with a URI, no `FLAG_GRANT_READ_URI_PERMISSION`, and `EXTRA_TEXT` equals
    `settings_share_text` formatted with the fixed link;
  - the link equals the table's "In-app Settings, Share Migrely" URL and its decoded `referrer` equals the top-level tags;
  - the guard: a second activation within the timeout, Share then Rate, and key repeat start nothing more;
  - Compose: rows absent during an attack and while loading, present otherwise; one node each, name starting with the
    title, role Button and the action label; no chevron; "Warning signs" still first in Help.
  Verify: `./gradlew :features:settings:ui:testDebugUnitTest` (plus the full gate).
  Done (8c5087ab). Tests: `ExternalAppLauncherTest` (W3, 7), `ShareLinkTest` (3), `OpeningGuardTest` (5, with a no-wait negative control), `ShareAndRateTest` (9: loading, attack, one button node with title, summary and action label, order with Warning signs first in Help, double tap, Share then Rate via TalkBack's click, held Enter, focus to "Open-source licences", no-guard negative control). Mutation control: guard removed from the rows -> 2 `ShareAndRateTest` cases fail. Not tested in Compose: no chevron (an icon without description has no semantics), TalkBack accessibility focus (task 7).
- [x] 5.2 report ui: the switch row (`NotesRow` made to take title and summary), one node with state and role, wraps at
  200%; the new `report_message_not_saved`. Tests:
  - **W6 (medical-reviewer condition):** the switch summary contains the mapper's credit constant exactly (shared
    constant or written-out text with an equality test);
  - Compose: hidden while loading, disabled while working, reverted with the message on a failed save.
  Verify: `./gradlew :features:report:ui:testDebugUnitTest`.
  Done (73ecda05): `SwitchRow(title, summary, …)` used twice. W6: `ReportCreditSummaryTest` in `:app` (the summary is written out and must equal `One small line on the last page: "$REPORT_CREDIT"`). `ReportCreditSwitchTest` (5): hidden while loading, one Switch node with title, quoting summary and state, tap asks and shows the stored value until saved, disabled while working, Create takes the shown value. 200% wrap is task 7.

## 6. Wiring and manifest

- [x] 6.1 `:app` DI for the new repositories and use cases. Verify: the gate.
  Done: review, three safety-card features, report and settings wired in `:app/di`; Konsist passes in the gate.
- [x] 6.2 Manifest and permissions unchanged. Verify: `./gradlew :app:checkReleasePermissions` exits 0, and
  `diff` of `app/build/intermediates/merged_manifests/release/.../AndroidManifest.xml` against the same file built from
  `origin/develop` shows no new `uses-permission`, `<queries>` or component. Record both.
  Done 2026-11-16: `:app:checkReleasePermissions` exit 0 on 8528d1c7; `build/reports/permissions/release.txt` identical to `origin/develop` 03a8900e (ACCESS_NETWORK_STATE, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, WAKE_LOCK, BILLING, DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION; no INTERNET). Merged release manifest (`merged_manifest/release/processReleaseMainManifest`) byte-identical to develop's (sha256 1e776dba…33f4 both; develop built in a temporary detached worktree, removed). The one `<queries>` is Play Billing's, already on develop.

## 7. Device walk and promo verification (`android-engineer`)

- [ ] 7.1 emulator-5570: walk every scenario of the four spec deltas, including TalkBack (`uiautomator dump` labels, the
  focus move of EC-SHR-13 with TalkBack on, and report what Compose does if it cannot move there), 200% font, landscape,
  keyboard, rotation and process death on Settings and Doctor report; Play app disabled (`pm disable-user
  com.android.vending`, restore after) and no browser; offline; Free, Premium subscription and lifetime (simulated store);
  a PDF on A4 (locale en-GB) and Letter (en-US), opened and checked page by page; the share preview in at least one
  messenger. Judge the 200 ms fade next to the dark theme flip (design D5). Restore every device setting. Verify: a
  verification record below with counts and what was not checked.
- [ ] 7.2 Review gap: first-10 card, "Still going?" and overuse card each followed by a home load with D130 met; then
  the mark moved back 3 days via preferences in a debug build. Verify: no review flow in the first three, one in the last
  (debug `FakeReviewManager`, observed through the review marks).
- [ ] 7.3 Promo codes on the owner's phone, with the owner, design D7 cases 1 and 2 (3 optional); check the exact Play
  "Redeem code" menu path for 10.1. Verify: result per case recorded below; a failure is filed as an issue with `Refs`
  in this file.

## 8. Review and QA

- [ ] 8.1 `code-reviewer` on `git diff origin/develop...feature/add-word-of-mouth-loops`, briefed with the specs and the
  risks (three safety hosts, PDF geometry, the tap guard, no manifest change, the W3 and W6 tests). Verify: findings
  answered in this file.
- [ ] 8.2 `qa-engineer`: independent check of the diff's reach with negative controls (owner rule 2026-11-09), API 36 and
  26. Must include: (a) **switch off: "Patient-entered data. Not a diagnosis." on every page of a multi-page PDF**
  (medical-reviewer condition); (b) the share intent text-only on a device (no attachment reaches the target); (c) a
  double tap on Share with and without the guard (ux §8.6). Verify: QA verdict recorded with its SHA.

## 9. Docs and archive (`product-manager`)

- [ ] 9.1 Diff each delta against the live specs (stale-delta check), then `openspec archive add-word-of-mouth-loops
  --yes` on the feature branch. Verify: `openspec validate --specs --strict` exits 0.
- [ ] 9.2 Tell `aso-seo-specialist` the share link is live in 1.4.0 so the table's state column changes. Verify: message
  sent with the release tag.

## 10. Outside the app code, before gifts and before 1.4.0 reaches users

- [ ] 10.1 `content-creator`: the gift message from legal's DRAFT §4 (D187 (5)), with all four elements: (a) a gift,
  nothing expected, no rating or review asked anywhere, a rating earns nothing; (b) disclose the gift clearly, at the
  start, naming the giver (for example "Ad: I was given Migrely Premium free by its developer."); (c) any honest view,
  and never describe Migrely as treating, preventing or reducing migraines; (d) how to redeem (code, the Play path from
  7.3, the promotion end date), open Migrely once within 3 days, "Restore purchases" if Premium does not show. Then
  `medical-reviewer` and `legal-finance-analyst` read it. **The owner sends it; no agent sends.** Recipient list only in
  `marketing/`. No gift before 7.3 passes. Verify: a draft in `marketing/` with the four elements ticked by legal.
- [ ] 10.2 Privacy policy and Data safety (D185 (9)): `aso-seo-specialist` drafts legal's P1–P3 (the PDF line and its
  switch; the two new stored marks; a "Sharing and rating Migrely" section), a new effective date, the regenerated
  `privacy-policy.html` (`make_policy_html.py`) and the `docs/release/data-safety.md` "Ratings" bullet, and checks site
  pages that list stored data (`/privacy-check/`, the medical-device FAQ), in a reviewed site PR. `legal-finance-analyst`
  re-reads it. **The owner approves the policy text before merge**, and the owner's deploy makes it live before 1.4.0
  reaches users. The Data safety form answer stays "No data collected / shared". Verify: the PR link, legal's re-read, the
  owner's approval in the tracker, and the live `https://migrely.health/privacy/` showing P1–P3 before the 1.4.0
  production rollout (a 1.4.0 Go/No-Go row).

## Verification record

2026-11-16, `eng-loops`, tasks 2–6 on `feature/add-word-of-mouth-loops` (code 8528d1c7):
- Gate `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint --continue`: exit 0 (second
  run; the first failed only on a test name with ";", fixed). Unit test XML under all `build/test-results`: 3364 tests,
  0 failures, 0 errors, 0 skipped (debug and release variants).
- Not run (device, task 7): `ANDROID_SERIAL=emulator-5570 ./gradlew :datasource:implementation:connectedDebugAndroidTest`
  (`ReportCreditLayoutTest`, 4 new tests); every device scenario of 7.1 and 7.2; 7.3 with the owner.
- Task 7 needs: swap low enough to boot emulator-5570; the connected run above; then the 7.1 walk with particular
  attention to (a) TalkBack accessibility focus when the section leaves (keyboard focus is tested, TalkBack is not),
  (b) the 200 ms fold next to the dark theme flip, (c) the share preview and that Android's own text-only `ClipData`
  carries no attachment, (d) PDF line on A4 and Letter with the device font; 7.2 via the debug review fake.
