# Tasks

The gate for all code tasks is `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`. Use
`set -o pipefail` with any pipe.

The text in `design.md`'s "Final copy" is final. `medical-reviewer` gave PASS on exactly that text: any changed word goes
back to it first.

## 1. Copy sign-off

- [x] 1.1 `medical-reviewer` gave PASS on the final text. `ux-designer` was consulted, and the disagreement was resolved by the handbook's risk order (safety over UX).
- [x] 1.2 `product-manager` applied the result to `design.md`, both deltas, D151, the D59 pointer and §13 row 27, and removed the "DRAFT" mark. Verify: `openspec validate fix-overuse-wording --strict` passes.

## 2. Domain and presentation

- [ ] 2.1 Domain: no change. The numbers, the counting and `reached` stay. Verify: `git diff origin/develop --stat -- 'features/*/domain'` is empty at the end.
- [ ] 2.2 Presentation: remove `thresholdDays` from `OveruseWarningPresentationModel` and `MedicationPromptPresentationMapper`, and update `MedicationPromptMappersTest` and `MedicationPromptViewModelTest`. Before removing the mapper test's numbers, confirm that another test pins 15, 10, 10, 10: change the domain enum's values locally and show a test failing, then restore them. If none fails, add that test in the domain module. Verify: `./gradlew :features:medication:presentation:test :features:medication:domain:test`.

## 3. Data

- [ ] 3.1 `ReportDocumentMapper`: the new label. `ReportDocumentMapperTest`: assert the label and that no fact's label contains "limit". Verify: run `./gradlew :features:report:data:test`, and it must fail against the old label first (show this negative control).

## 4. UI

- [ ] 4.1 `strings.xml`: the eight `indicator_row_*` items lose the threshold, and `indicator_reached` and `overuse_body_soft` change; add `indicator_row_reached`; remove `indicator_at_limit` and `indicator_over_limit`. `overuse_body_doctor` and `message_limit_reached` stay. In XML, write `It\'s`. Verify: `git grep -nP "\blimit\b" -- features/medication/ui/src/main/res/values/strings.xml` finds 0 lines (it finds 10 on `develop`, which is the control), and each new text matches `design.md` word for word.
- [ ] 4.2 `OveruseIndicator.kt`: the `when` goes (design D2); the plural takes the count only; `row.limit` goes only to `indicator_row_reached`; update the comments at lines 29–30 and 42. The colour and the `indicator_reached` condition stay. Verify: `git grep -nE "indicator_at_limit|indicator_over_limit" -- features` finds 0 lines.
- [ ] 4.3 `MedicationPromptCard.kt:265`: no argument; the preview at line 336 drops `thresholdDays`. Remove it from `MedicationPromptUiModel.OveruseWarning` and `MedicationPromptUiMapper`, and update `MedicationPromptUiMappersTest`. Verify: `git grep -n thresholdDays -- features/medication/ui features/medication/presentation` finds 0 lines, and `./gradlew :features:medication:ui:test` passes.

## 5. Docs

- [ ] 5.1 `docs/BRD.md` holds D151, the MED-F5 note, the D59 pointer and §13 row 27 (done with the plan). Verify: `grep -c "^| D151 " docs/BRD.md` prints 1, checked again against `origin/develop` before the PR.

## 6. Verify

- [ ] 6.1 The gate. Verify: exit code 0, with counts recorded here.
- [ ] 6.2 Walk on `emulator-5570`: History at 6 days, 14 days, 1 day, 10 days, 16 days, and two categories at 12 days. Read each row, its colour and the line under the rows. Home's soft card at 13 painkiller days and 9 triptan days: read the body. Verify: one `uiautomator dump` per state.
- [ ] 6.3 Access: 200% font and landscape with "All attack medication: 16 days. Doctors watch for 10 or more days a month." (nothing cut). TalkBack: the row's node holds the whole text. Rotation and process death with the soft card showing. Restore the font scale and rotation. Verify: dumps.
- [ ] 6.4 Doctor report: make one with a span that reached the number and one without. Read the label and "None" in the PDF. Verify: the label is whole, and the first page still fits.
- [ ] 6.5 No permission change. Verify: `./gradlew :app:checkReleasePermissions` passes.
- [ ] 6.6 Independent review by `code-reviewer`, then fixes.
- [ ] 6.7 Handoff to `qa-engineer` with the final text and this list, then their independent check:
  - rows: `qa/device_scenarios.sh` lines 723, 727, 731, 732, 735, 736, 741 and the comments at 721–722; `.maestro/medication-overuse/history-medication-only.yaml:57`; `.maestro/month-stats/medication-days.yaml:55`. Rows no longer contain brackets or `+`, so the bracket escapes go; a reached row ends in a `.` to escape;
  - `qa/seed_backup.py`: the row at 626 is the count only; the at/over logic at 627–628 becomes the "Doctors watch for" sentence with `LIMITS`; `REACHED` at 83; `SOFT_BODY` at 81 and its `.format(LIMITS[chosen])` at 619 (no number now); the PDF label at 650; the description at 97;
  - the line: `.maestro/device/indicator-check.yaml:41`, and the `At this many days.*` negatives at `indicator-check.yaml:46` and `.maestro/month-stats/medication-days.yaml:59`, which would pass forever on the old wording unless changed;
  - the soft card: `.maestro/device/med-overuse-soft.yaml:11`;
  - new scenarios without coverage yet: "Before the number is reached", "A reached row brings its line", "One day below the number", "Reached is said in words", "Largest font", "No row says limit", "A list cap is not an overuse number", "The label does not say limit". The existing `# Covers:` lines keep working (design D5);
  - retest #135 against this text: rows below the number deliberately do not show the threshold.
- [ ] 6.8 Check store screenshots and promo video for "limit". Verify: `team-lead` routes any hit to `aso-seo-specialist`. It is not a blocker for this change.
- [ ] 6.9 `openspec archive fix-overuse-wording --yes`, then `openspec validate --specs --strict`. PR into `develop` with `Refs #135` (QA retests and closes).
