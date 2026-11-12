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

- [x] 0.1 `ux-designer`, then `medical-reviewer`, give the copy. Record (2026-11-12): heading PASS; Insights lead: PASS, old FAIL
  (causation), new matches the benefit line and the site; benefit row PASS; fallback PASS (BRD D174 (1)–(3)).
- [x] 0.2 `product-manager`: BRD INS-F4, §7.1, REM-F2, D174, EC-INS-04…06, §13 row 43; this change; the `medication-effect`
  Purpose. Verify: `openspec validate fix-medication-ratings-copy --strict` exits 0.

## 1. UI strings and comments (`android-engineer`)

- [x] 1.1 Domain, presentation and data: none. Verify: `git diff --stat origin/develop -- '*/domain/src/main/*'
  '*/presentation/*' '*/data/*' '*.kts' '*.toml' app/src/main` is empty.
  Done (2026-11-12): no domain, presentation or data behaviour changed. The verify diff is not empty: it lists
  `InsightsCalculator.kt`, a KDoc-only change that 1.4 requires (its grep finds "often it helped" there).
- [x] 1.2 `features/insights/ui/src/main/res/values/strings.xml`: `insights_medications_title` = "How you rated your
  medications"; the comment above it (line 94) names the section by its new title and adds D174. `insights_helped` and
  `insights_needs_answers` do not change. Verify: `grep -n 'How you rated your medications' features/insights/ui/src/main/res/values/strings.xml`
  prints one line, and `git diff origin/develop -- features/insights/ui/src/main/res/values/strings.xml | grep -c '^[-+] '` is 4.
  Done: the grep prints 2 lines, the comment and the string, because the comment names the section by its new title as
  asked; the diff count is 4.
- [x] 1.3 `features/subscription/ui/src/main/res/values/strings.xml`: `subscription_lead_insights` and
  `subscription_benefit_timing` exactly as design D1. Use the fallback for the benefit row only under design D2, after 4.2, and
  record which string shipped here. Verify: `grep -c 'how you rated' features/subscription/ui/src/main/res/values/strings.xml` is 2.
  Done: both strings as design D1, count 2. Shipped: the full benefit row "When migraines come, and how you rated your
  medications". At 200% font and 360 dp it wraps to 4 lines in portrait and 2 in landscape. It is not clipped, has no "…", and the
  close button and plan list stay reachable, so D2 does not apply. Screenshots: `migrely-work/walks/205/paywall-insights-200-portrait-benefit.png`,
  `paywall-insights-200-landscape-benefit.png`.
- [x] 1.4 KDoc of `MedicationsSection` (`features/insights/ui/.../view/InsightsSections.kt:221`): "how often it helped" → what is
  counted, e.g. "how the person rated it: the share of "Did it help?" answers that were "A lot" or "Completely"". The
  `InsightsCalculator.medications` KDoc (line 164) already names the answers; align it only if it reads as a claim. Verify:
  `git grep -n -i 'often it helped' -- features` prints nothing.
  Done: both KDocs (`MedicationsSection`, `InsightsCalculator.medications`) now say "how many of its effect answers were
  "A lot" or "Completely"". They avoid the literal "Did it help", so the 8.2 grep stays a check on the copy. The grep prints nothing.
- [x] 1.5 Rename the test at `features/insights/domain/src/test/.../InsightsCalculatorTest.kt:455` to describe answers, e.g.
  `Attack medications in the period show how they were rated once they have three answers`. Body unchanged. Verify:
  `git diff origin/develop -- '*InsightsCalculatorTest.kt' | grep -c '^[-+] '` is 2.
  Done: count 2; the renamed test passes.

## 2. Wiring

- [x] 2.1 None. Verify: `git diff --stat origin/develop -- '*.kt'` lists only `InsightsSections.kt`, `InsightsCalculatorTest.kt`
  and, if 1.4 touched it, `InsightsCalculator.kt`.
  Done: the three .kt files only.

## 3. Gate (`android-engineer`)

- [x] 3.1 `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`. Verify: exit 0, with the task count.
  Done: exit 0, 2058 actionable tasks, 3017 unit tests, 0 failures, rerun after the last KDoc change. `:app:checkReleasePermissions
  :app:checkReleaseDebugComponents` exit 0.
- [x] 3.2 Negative controls. Verify, each exit 1 with no output:
  `git grep -n -i 'what helped' -- features app core` and `git grep -n -i 'shows your triggers' -- features app core`.
  Done: both greps exit 1 with no output (the combined grep prints 5 lines on e788d68e).

## 4. Walk (`emulator-5570`, `android-engineer`)

- [x] 4.1 Insights with seeded Premium data (Ibuprofen rated, one medication with fewer than 3 answers): 100% and 200% font at
  360 dp, portrait and landscape; the heading wraps, never clips or ends in "…"; TalkBack reads it as a heading. Screenshots in
  `migrely-work/205/`. Restore font scale and rotation.
  Done on emulator-5570 with seed `insights-patterns` and Premium bought via the simulated sheet. The heading reads "How you
  rated your medications"; Ibuprofen "Helped a lot or completely: 7 of 9 answers (78%)"; Sumatriptan "Needs 3 answers (has 2)". At
  360 dp it fits one line at 100% (portrait and landscape) and at 200% landscape; it wraps to 2 lines at 200% portrait. None is
  clipped or ends in "…". A top-to-bottom scan of Insights finds "what helped" 0 times. Heading: the title keeps
  `semantics { heading() }` (`InsightsSections.kt:47`, untouched); uiautomator does not expose the heading flag, and no TalkBack
  run was done. Screenshots: `migrely-work/walks/205/insights-100-portrait.png`, `insights-200-portrait.png`,
  `insights-200-landscape.png`. Font, rotation and density restored.
- [x] 4.2 The Insights paywall as a free user: the lead and the benefit row at 200% font, 360 dp, portrait and landscape.
  Decide the benefit string by design D2 and record it in 1.3. Screenshots in `migrely-work/205/`. Restore settings.
  Done: as a free user the lead reads the spec's text exactly. At 200% and 360 dp it wraps and is not clipped, in portrait and
  landscape (`paywall-insights-200-portrait-lead.png`, `paywall-insights-200-landscape-lead.png`). The onboarding paywall shows the
  same benefit row. Settings restored.

## 5. Docs (`android-engineer`)

- [x] 5.1 Nothing in `docs/release/` in this PR (store copy is a follow-up for `aso-seo-specialist`, D174). Verify:
  `git diff --stat origin/develop -- docs/release` is empty.
  Done: empty.

## 6. Review

- [ ] 6.1 `code-reviewer` on the diff. Verify: a packet with findings or "none", and every finding closed.

## 7. QA files (`qa-engineer`)

- [x] 7.1 `.maestro/subscription/paywall-and-purchase.yaml:62`: assert the new lead (escape the final `.`).
  Done (qa-engineer, 2026-11-12): the new lead, then the benefit row, each followed by an `assertNotVisible` of
  `(?i).*(what\s+helped|shows\s+your\s+triggers).*`; Covers "The Insights lead" and "The benefit row". 2 of 2 pass on
  8dd0c04c; fails on e788d68e. Record: `docs/qa/fix-medication-ratings-copy.md`.
- [x] 7.2 `qa/device_scenarios.sh:2013` `# Covers: trigger-insights › How you rated your medications › Ibuprofen`, and line 2019's
  echo; `qa/seed_backup.py:724` the section label. Verify: `git grep -n -i 'what helped' -- .maestro qa` prints nothing.
  Done: the grep exits 1. Also `qa/seed_expect.py:11-12` (the phrase split by a line break, which `git grep` misses); the
  heading is now an expected screen text, with a case-insensitive guard against the old one; new on-demand scenario
  `insights_upgrade` (Covers "Upgrade or restore"). `e2e_coverage.py` exit 0; `seed_backup.py --self-test` ok.

## 8. Acceptance (`qa-engineer`, scope: this diff only)

- [x] 8.1 Strings: `git grep -n -i 'what helped' -- features app core` and `git grep -n -i 'shows your triggers' -- features app
  core` each exit 1. Negative control: the same greps on `origin/develop` (e788d68e) print 5 and 1 lines.
  Done: exit 1 and 1 on 8dd0c04c; 5 and 1 lines on e788d68e. The APK scan (resources and dex, any case) finds 0 in the
  candidate debug and R8 APKs and 3 in the base APK.
- [x] 8.2 Unchanged on purpose: `git diff origin/develop -- features | grep -E '^[-+].*(insights_helped|insights_needs_answers|Did it help)'`
  prints nothing, and the Ibuprofen line reads "Helped a lot or completely: 7 of 9 answers (78%)" on the device.
  Done: the grep prints nothing (exit 1); on emulator-5580 the insights scenario sees the Ibuprofen line and "Needs 3
  answers (has 2)" for Sumatriptan.
- [x] 8.3 Insights heading "How you rated your medications" on device, at 100% and 200% font, 360 dp, portrait and landscape,
  not clipped; TalkBack reads it as a heading and reads each medication with its answers line.
  Done: 1 line at 100% (portrait and landscape) and at 200% landscape, 2 lines at 200% portrait; none clipped or cut with
  "…". TalkBack 16 (100% and 200%): "How you rated your medications. Heading"; right swipes give "Ibuprofen. Helped a lot
  or completely: 7 of 9 answers (78%)", "Sumatriptan. Needs 3 answers (has 2)", then the next heading. This is the real
  TalkBack check that task 4.1 did not run.
- [x] 8.4 Insights paywall (free user): the lead reads exactly the spec's "The Insights lead"; the benefit row reads the string
  recorded in 1.3; the Maestro paywall flow passes on this SHA and fails on e788d68e (negative control).
  Done: 2 of 2 pass on 8dd0c04c; on e788d68e it fails at the new lead, the old flow passes, and each guard fails alone.
- [x] 8.5 Upgrade: install e788d68e, seed rated intakes, update to this SHA without clearing data; the same counts and percentages
  show under the new heading.
  Done: `QA_OLD_APK=<e788d68e> qa/device_scenarios.sh insights_upgrade`, 2 of 2 pass: the same 23 texts before and after,
  with "What helped" before and "How you rated your medications" after, data and Premium kept. Control: e788d68e over
  itself fails after the update.
- [x] 8.6 Go/No-Go packet, saying what these checks do not prove (other surfaces: store, site, video, posts are follow-ups).
  Done: `docs/qa/fix-medication-ratings-copy.md`, PASS, with what the checks do not prove.

## 9. Close (`product-manager`)

- [ ] 9.1 Record in BRD D174 which benefit string shipped (from 1.3), mark EC-INS-04…06 ✅, archive the change. Verify:
  `openspec archive fix-medication-ratings-copy -y` leaves `openspec/specs/trigger-insights/spec.md` with "How you rated your
  medications" and no "What helped", and `openspec validate --all --strict` exits 0.
