# Tasks

Base: `bugfix/205-medication-ratings` from `origin/develop` (e788d68e). Write `Refs #205` in every commit and in the PR into
`develop`. Never write `Fixes #205`: the reporter retests and closes it. BRD D174.

Ownership:
- `ux-designer`, `medical-reviewer`, `product-manager`: 0 (done);
- `android-engineer`: 1 to 5;
- `code-reviewer`: 6;
- `qa-engineer`: 7 and 8;
- `product-manager`: 9.

## 0. Copy (before any code)

- [x] 0.1 `ux-designer`, then `medical-reviewer`, give the copy. Record (2026-11-12): heading PASS; Insights lead: old FAIL
  (causation), new matches the benefit line and the site; benefit row PASS; fallback PASS (BRD D174 (1)–(3)).
- [x] 0.2 `product-manager`: BRD INS-F4, §7.1, REM-F2, D174, EC-INS-04…06, §13 row 43; this change; the `medication-effect`
  Purpose. Verify: `openspec validate fix-medication-ratings-copy --strict` exits 0.

## 1. UI strings and comments (`android-engineer`)

- [ ] 1.1 Domain, presentation and data: none. Verify: `git diff --stat origin/develop -- '*/domain/src/main/*'
  '*/presentation/*' '*/data/*' '*.kts' '*.toml' app/src/main` is empty.
- [ ] 1.2 `features/insights/ui/src/main/res/values/strings.xml`: `insights_medications_title` = "How you rated your
  medications"; the comment above it (line 94) names the section by its new title and adds D174. `insights_helped` and
  `insights_needs_answers` do not change. Verify: `grep -n 'How you rated your medications' features/insights/ui/src/main/res/values/strings.xml`
  prints one line, and `git diff origin/develop -- features/insights/ui/src/main/res/values/strings.xml | grep -c '^[-+] '` is 4.
- [ ] 1.3 `features/subscription/ui/src/main/res/values/strings.xml`: `subscription_lead_insights` and
  `subscription_benefit_timing` exactly as design D1. Use the fallback for the benefit row only under design D2, after 4.2, and
  record which string shipped here. Verify: `grep -c 'how you rated' features/subscription/ui/src/main/res/values/strings.xml` is 2.
- [ ] 1.4 KDoc of `MedicationsSection` (`features/insights/ui/.../view/InsightsSections.kt:221`): "how often it helped" → what is
  counted, e.g. "how the person rated it: the share of "Did it help?" answers that were "A lot" or "Completely"". The
  `InsightsCalculator.medications` KDoc (line 164) already names the answers; align it only if it reads as a claim. Verify:
  `git grep -n -i 'often it helped' -- features` prints nothing.
- [ ] 1.5 Rename the test at `features/insights/domain/src/test/.../InsightsCalculatorTest.kt:455` to describe answers, e.g.
  `Attack medications in the period show how they were rated once they have three answers`. Body unchanged. Verify:
  `git diff origin/develop -- '*InsightsCalculatorTest.kt' | grep -c '^[-+] '` is 2.

## 2. Wiring

- [ ] 2.1 None. Verify: `git diff --stat origin/develop -- '*.kt'` lists only `InsightsSections.kt`, `InsightsCalculatorTest.kt`
  and, if 1.4 touched it, `InsightsCalculator.kt`.

## 3. Gate (`android-engineer`)

- [ ] 3.1 `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`. Verify: exit 0, with the task count.
- [ ] 3.2 Negative controls. Verify, each exit 1 with no output:
  `git grep -n -i 'what helped' -- features app core` and `git grep -n -i 'shows your triggers' -- features app core`.

## 4. Walk (`emulator-5570`, `android-engineer`)

- [ ] 4.1 Insights with seeded Premium data (Ibuprofen rated, one medication with fewer than 3 answers): 100% and 200% font at
  360 dp, portrait and landscape; the heading wraps, never clips or ends in "…"; TalkBack reads it as a heading. Screenshots in
  `migrely-work/205/`. Restore font scale and rotation.
- [ ] 4.2 The Insights paywall as a free user: the lead and the benefit row at 200% font, 360 dp, portrait and landscape.
  Decide the benefit string by design D2 and record it in 1.3. Screenshots in `migrely-work/205/`. Restore settings.

## 5. Docs (`android-engineer`)

- [ ] 5.1 Nothing in `docs/release/` in this PR (store copy is a follow-up for `aso-seo-specialist`, D174). Verify:
  `git diff --stat origin/develop -- docs/release` is empty.

## 6. Review

- [ ] 6.1 `code-reviewer` on the diff. Verify: a packet with findings or "none", and every finding closed.

## 7. QA files (`qa-engineer`)

- [ ] 7.1 `.maestro/subscription/paywall-and-purchase.yaml:62`: assert the new lead (escape the final `.`).
- [ ] 7.2 `qa/device_scenarios.sh:2013` `# Covers: trigger-insights › How you rated your medications › Ibuprofen`, and line 2019's
  echo; `qa/seed_backup.py:724` the section label. Verify: `git grep -n -i 'what helped' -- .maestro qa` prints nothing.

## 8. Acceptance (`qa-engineer`, scope: this diff only)

- [ ] 8.1 Strings: `git grep -n -i 'what helped' -- features app core` and `git grep -n -i 'shows your triggers' -- features app
  core` each exit 1. Negative control: the same greps on `origin/develop` (e788d68e) print 5 and 1 lines.
- [ ] 8.2 Unchanged on purpose: `git diff origin/develop -- features | grep -E '^[-+].*(insights_helped|insights_needs_answers|Did it help)'`
  prints nothing, and the Ibuprofen line reads "Helped a lot or completely: 7 of 9 answers (78%)" on the device.
- [ ] 8.3 Insights heading "How you rated your medications" on device, at 100% and 200% font, 360 dp, portrait and landscape,
  not clipped; TalkBack reads it as a heading and reads each medication with its answers line.
- [ ] 8.4 Insights paywall (free user): the lead reads exactly the spec's "The Insights lead"; the benefit row reads the string
  recorded in 1.3; the Maestro paywall flow passes on this SHA and fails on e788d68e (negative control).
- [ ] 8.5 Upgrade: install e788d68e, seed rated intakes, update to this SHA without clearing data; the same counts and percentages
  show under the new heading.
- [ ] 8.6 Go/No-Go packet, saying what these checks do not prove (other surfaces: store, site, video, posts are follow-ups).

## 9. Close (`product-manager`)

- [ ] 9.1 Record in BRD D174 which benefit string shipped (from 1.3), mark EC-INS-04…06 ✅, archive the change. Verify:
  `openspec archive fix-medication-ratings-copy -y` leaves `openspec/specs/trigger-insights/spec.md` with "How you rated your
  medications" and no "What helped", and `openspec validate --all --strict` exits 0.
