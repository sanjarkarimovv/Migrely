# Tasks

Base: `feature/add-word-of-mouth-loops` from `origin/develop` 50c784e5. Release: 1.4.0, the minor after 1.3.0 (team-lead).
It must not hold 1.3.0's Go/No-Go. BRD D185–D188.

Ownership:
- `product-manager`: 0, 1.3 and 9;
- `ux-designer`: 1.1;
- `medical-reviewer` and `legal-finance-analyst`: 1.2;
- `android-engineer`: 2–6 and 7 (7 with the owner, main session);
- `code-reviewer`: 8;
- `qa-engineer`: 8 (emulator-5580 API 36, emulator-5582 API 26, one at a time).

Gate for every code task: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0 (run
with `set -o pipefail`; never `testDebugUnitTest` alone).

## 0. Plan (`product-manager`)

- [x] 0.1 BRD D185–D188, SET-F2, REP-F5, SUB-F3, EC-SHR-01…11 (§9.16), EC-REP-05…11, EC-BIL-21…25, EC-REV-14, §13 row 52,
  §14.4. Verify: `openspec validate add-word-of-mouth-loops --strict` exits 0.

## 1. Copy and flow (before code)

- [ ] 1.1 `ux-designer`: section name and position, row titles and summaries, the two failure messages, the report
  switch label, summary and position, focus when rows vanish at an attack start (design D5), TalkBack labels and action
  names, 200% font and landscape layouts, the credit line placement on the PDF (design D2). Output: a spec file in
  `migrely-work/` and the final strings. Verify: every row of design.md's copy table has a final value.
- [ ] 1.2 `medical-reviewer` (and `legal-finance-analyst` for the privacy claim): the share sentence, the share subject and
  the PDF credit line. Verify: written approval of the exact strings, quoted in this file.
- [ ] 1.3 `product-manager`: put the final strings and any renamed section into the spec deltas. Verify:
  `openspec validate add-word-of-mouth-loops --strict` exits 0.

## 2. Domain

- [ ] 2.1 review: `GetLastSafetyCardShownRepository`; `ShouldRequestReviewUseCase` adds the 3-day safety gap (design D4),
  future stamp = recent, unreadable = recent. Tests first, red then green: within 3 days, exactly 3 days, future stamp,
  unreadable, absent. Verify: `./gradlew :features:review:domain:test`.
- [ ] 2.2 attack, home, medication: `MarkSafetyCardShownRepository` (one function each) and the call where the card
  becomes visible (presentation decides, domain marks). Tests per feature. Verify:
  `./gradlew :features:attack:domain:test :features:home:domain:test :features:medication:domain:test`.
- [ ] 2.3 report: `GetReportCreditRepository`, `SetReportCreditRepository`, use cases; `CreateReportUseCase` passes the
  choice. Tests. Verify: `./gradlew :features:report:domain:test`.

## 3. Presentation

- [ ] 3.1 settings: `showShareAndRate` from the attack flow; actions `onShareAction`, `onRateAction` emitting navigation
  events; the failure notification. Tests, including "attack starts while open". Verify:
  `./gradlew :features:settings:presentation:test`.
- [ ] 3.2 report: credit switch in the view state and its action. Tests. Verify:
  `./gradlew :features:report:presentation:test`.
- [ ] 3.3 attack, home, medication presentation: call the mark when the card's state first becomes visible, once per
  appearance (rotation does not mark again). Tests. Verify: `./gradlew :features:attack:presentation:test
  :features:home:presentation:test :features:medication:presentation:test`.

## 4. Data and datasource

- [ ] 4.1 preferences: `report_credit_shown` (absent = true) and `safety_card_last_shown`; "Delete all data" removes
  both (check design D3). Tests in the datasource module. Verify: `./gradlew :datasource:implementation:test`.
- [ ] 4.2 `ReportDocumentDataModel.credit: String?`; `ReportDocumentMapper` fills the approved text when on;
  `ReportPageLayout` draws it on the last page only (design D2). Tests: mapper on and off; instrumented layout test on A4
  and Letter: one line, no ellipsis, same page count on and off, absent on pages before the last. Verify:
  `./gradlew :features:report:data:test` and `ANDROID_SERIAL=emulator-5570 ./gradlew
  :datasource:implementation:connectedDebugAndroidTest`.
- [ ] 4.3 JSON and CSV unchanged: the existing backup round-trip and CSV tests pass unmodified. Verify:
  `git diff origin/develop -- '**/backup/**/test/**'` is empty and `./gradlew :features:backup:data:test` passes.

## 5. UI

- [ ] 5.1 settings ui: the section and two rows (`LinkRow` style), the intent helper with its result (design D1), the
  share-link constant and its test against the table (design D6), the two messages. Compose tests: rows absent during an
  attack, present otherwise, one node each with role and action label, Warning signs still first in Help. Verify:
  `./gradlew :features:settings:ui:testDebugUnitTest` (plus the full gate).
- [ ] 5.2 report ui: the switch row (approved label), one node with state and role, wraps at 200%. Compose test. Verify:
  `./gradlew :features:report:ui:testDebugUnitTest`.

## 6. Wiring and manifest

- [ ] 6.1 `:app` DI for the new repositories and use cases. Verify: the gate.
- [ ] 6.2 Manifest and permissions unchanged. Verify: `./gradlew :app:checkReleasePermissions` exits 0, and
  `diff` of `app/build/intermediates/merged_manifests/release/.../AndroidManifest.xml` against the same file built from
  `origin/develop` shows no new `uses-permission`, `<queries>` or component. Record both.

## 7. Device walk and promo verification (`android-engineer`)

- [ ] 7.1 emulator-5570: walk every scenario of the four spec deltas, including TalkBack (`uiautomator dump` labels),
  200% font, landscape, keyboard, rotation and process death on Settings and Doctor report; Play app disabled
  (`pm disable-user com.android.vending`, restore after) and no browser; offline; Free, Premium subscription and lifetime
  (simulated store); a PDF on A4 (locale en-GB) and Letter (en-US), opened and checked page by page. Restore every device
  setting. Verify: a verification record below with counts and what was not checked.
- [ ] 7.2 Review gap: first-10 card, "Still going?" and overuse card each followed by a home load with D130 met; then
  the mark moved back 3 days via preferences in a debug build. Verify: no review flow in the first three, one in the last
  (debug `FakeReviewManager`, observed through the review marks).
- [ ] 7.3 Promo codes on the owner's phone, with the owner, design D7 cases 1 and 2 (3 optional). Verify: result per case
  recorded below; a failure is filed as an issue with `Refs` in this file.

## 8. Review and QA

- [ ] 8.1 `code-reviewer` on `git diff origin/develop...feature/add-word-of-mouth-loops`, briefed with the specs and the
  risks (three safety hosts, PDF geometry, no manifest change). Verify: findings answered in this file.
- [ ] 8.2 `qa-engineer`: independent check of the diff's reach with negative controls (owner rule 2026-11-09), API 36 and
  26. Verify: QA verdict recorded with its SHA.

## 9. Docs and archive (`product-manager`)

- [ ] 9.1 Diff each delta against the live specs (stale-delta check), then `openspec archive add-word-of-mouth-loops
  --yes` on the feature branch. Verify: `openspec validate --specs --strict` exits 0.
- [ ] 9.2 Tell `aso-seo-specialist` the share link is live in 1.4.0 so the table's state column changes. Verify: message
  sent with the release tag.

## Verification record

(empty until 7 runs)
