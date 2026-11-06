# Tasks

The gate for all code tasks is `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`. Use
`set -o pipefail` with any pipe.

## 1. Copy sign-off (before any code)

- [ ] 1.1 `ux-designer` and `medical-reviewer` review the draft in `design.md`, including its notes. Verify: their findings are recorded here.
- [ ] 1.2 `product-manager` applies the result to `design.md`, both deltas, D151 and §13 row 27, and removes the "DRAFT" mark. Verify: `openspec validate fix-overuse-wording --strict` passes.

## 2. Domain and presentation

- [ ] 2.1 No change: the numbers, the counting and `row.reached` stay. Verify: `git diff origin/develop --stat -- 'features/*/domain' 'features/*/presentation'` is empty at the end.

## 3. Data

- [ ] 3.1 `ReportDocumentMapper`: the new label. `ReportDocumentMapperTest`: assert the label and that no fact's label contains "limit". Verify: run `./gradlew :features:report:data:test`, and it must fail against the old label first (show this negative control).

## 4. UI

- [ ] 4.1 `strings.xml`: the eight `indicator_row_*` items; add `indicator_row_reached`; remove `indicator_at_limit` and `indicator_over_limit`. Verify: `git grep -nP "\blimit\b" -- features/medication/ui/src/main/res/values/strings.xml` finds 0 lines (it finds 10 on `develop`, which is the control), and `message_limit_reached` is unchanged.
- [ ] 4.2 `OveruseIndicator.kt`: one reached branch (design D2) and the comment on line 42. Colour and `row.limit` unchanged. Verify: `git grep -nE "indicator_at_limit|indicator_over_limit" -- features` finds 0 lines.

## 5. Docs

- [ ] 5.1 `docs/BRD.md` holds D151, the MED-F5 note, the D59 pointer and §13 row 27 (done with the plan). Verify: `grep -c "^| D151 " docs/BRD.md` prints 1, checked again against `origin/develop` before the PR.

## 6. Verify

- [ ] 6.1 The gate. Verify: exit code 0, with counts recorded here.
- [ ] 6.2 Walk on `emulator-5570`: 6 days, 14 days, 1 day, 10 days, 16 days, and two categories at 12 days. Read each row, its colour and the doctor line. Verify: one `uiautomator dump` per state.
- [ ] 6.3 Access: 200% font and landscape with "Combination or other: 16 days" (nothing cut). TalkBack: the row's node holds the whole text including "reached"; listen once to how "10+" is spoken and record it. Restore the font scale and rotation. Verify: dumps, plus a note on the spoken text.
- [ ] 6.4 Doctor report: make one with a span that reached the number and one without. Read the label and "None" in the PDF. Verify: the label is whole, and the first page still fits.
- [ ] 6.5 No permission change. Verify: `./gradlew :app:checkReleasePermissions` passes.
- [ ] 6.6 Independent review by `code-reviewer`, then fixes.
- [ ] 6.7 Handoff to `qa-engineer` with the final text and this list, then their independent check:
  - rows and at/over logic: `qa/seed_backup.py` lines 97, 626, 628, 650 (at and over become one word);
  - rows: `qa/device_scenarios.sh` lines 723–741, `.maestro/medication-overuse/history-medication-only.yaml:57`, `.maestro/month-stats/medication-days.yaml:55`;
  - new scenarios without coverage yet: "One day below the number", "Reached is said in words", "Largest font", "No row says limit", "A list cap is not an overuse number", "The label does not say limit". The existing `# Covers:` lines keep working (design D5);
  - `+` must be escaped in regex matchers as the brackets are.
- [ ] 6.8 Check store screenshots and promo video for "limit". Verify: `team-lead` routes any hit to `aso-seo-specialist`. It is not a blocker for this change.
- [ ] 6.9 `openspec archive fix-overuse-wording --yes`, then `openspec validate --specs --strict`. PR into `develop` with `Refs #135` (QA retests and closes).
