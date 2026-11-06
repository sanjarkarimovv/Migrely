# Tasks

The gate for all code tasks is `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`. Use
`set -o pipefail` with any pipe.

The text in `design.md`'s "Final copy" is final. `medical-reviewer` gave PASS on exactly that text: any changed word goes
back to it first.

## 1. Copy sign-off

- [x] 1.1 `medical-reviewer` gave PASS on the final text. `ux-designer` was consulted, and the disagreement was resolved by the handbook's risk order (safety over UX).
- [x] 1.2 `product-manager` applied the result to `design.md`, both deltas, D151, the D59 pointer and §13 row 27, and removed the "DRAFT" mark. Verify: `openspec validate fix-overuse-wording --strict` passes.

## 2. Domain and presentation

- [x] 2.1 Domain: no change. The numbers, the counting and `reached` stay. Verify: `git diff origin/develop --stat -- 'features/*/domain'` is empty at the end.
- [x] 2.2 Presentation: remove `thresholdDays` from `OveruseWarningPresentationModel` and `MedicationPromptPresentationMapper`, and update `MedicationPromptMappersTest` and `MedicationPromptViewModelTest`. Before removing the mapper test's numbers, confirm that another test pins 15, 10, 10, 10: change the domain enum's values locally and show a test failing, then restore them. If none fails, add that test in the domain module. Verify: `./gradlew :features:medication:presentation:test :features:medication:domain:test`.

## 3. Data

- [x] 3.1 `ReportDocumentMapper`: the new label. `ReportDocumentMapperTest`: assert the label and that no fact's label contains "limit". Verify: run `./gradlew :features:report:data:test`, and it must fail against the old label first (show this negative control).

## 4. UI

- [x] 4.1 `strings.xml`: the eight `indicator_row_*` items lose the threshold, and `indicator_reached` and `overuse_body_soft` change; add `indicator_row_reached`; remove `indicator_at_limit` and `indicator_over_limit`. `overuse_body_doctor` and `message_limit_reached` stay. In XML, write `It\'s`. Verify: `git grep -nP "\blimit\b" -- features/medication/ui/src/main/res/values/strings.xml` finds 0 lines (it finds 10 on `develop`, which is the control), and each new text matches `design.md` word for word.
- [x] 4.2 `OveruseIndicator.kt`: the `when` goes (design D2); the plural takes the count only; `row.limit` goes only to `indicator_row_reached`; update the comments at lines 29–30 and 42. The colour and the `indicator_reached` condition stay. Verify: `git grep -nE "indicator_at_limit|indicator_over_limit" -- features` finds 0 lines.
- [x] 4.3 `MedicationPromptCard.kt:265`: no argument; the preview at line 336 drops `thresholdDays`. Remove it from `MedicationPromptUiModel.OveruseWarning` and `MedicationPromptUiMapper`, and update `MedicationPromptUiMappersTest`. Verify: `git grep -n thresholdDays -- features/medication/ui features/medication/presentation` finds 0 lines, and `./gradlew :features:medication:ui:test` passes.

## 5. Docs

- [x] 5.1 `docs/BRD.md` holds D151, the MED-F5 note, the D59 pointer and §13 row 27 (done with the plan). Verify: `grep -c "^| D151 " docs/BRD.md` prints 1, checked again against `origin/develop` before the PR.

## 6. Verify

- [x] 6.1 The gate. Verify: exit code 0, with counts recorded here.
- [x] 6.2 Walk on `emulator-5570`: History at 6 days, 14 days, 1 day, 10 days, 16 days, and two categories at 12 days. Read each row, its colour and the line under the rows. Home's soft card at 13 painkiller days and 9 triptan days: read the body. Verify: one `uiautomator dump` per state.
- [x] 6.3 Access: 200% font and landscape with "All attack medication: 16 days. Doctors watch for 10 or more days a month." (nothing cut). TalkBack: the row's node holds the whole text. Rotation and process death with the soft card showing. Restore the font scale and rotation. Verify: dumps.
- [x] 6.4 Doctor report: make one with a span that reached the number and one without. Read the label and "None" in the PDF. Verify: the label is whole, and the first page still fits.
- [x] 6.5 No permission change. Verify: `./gradlew :app:checkReleasePermissions` passes.
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

## Verification record

Head of the code: `2d1da37d`, on `f992f6e8`. Evidence: `migrely-work/walks/fix-overuse-wording/` (outside the repo). 6.6 to 6.9 are still open: `code-reviewer` and `qa-engineer` come next, and this change is not archived.

Task 2.1's verify no longer reads empty: `code-reviewer` asked for `OveruseStatusRowDomainModelTest`, which lives in the domain module. `git diff origin/develop --stat -- 'features/*/domain/src/main'` is empty, so the domain's code is unchanged. No device ran after review: the walk below is of `2d1da37d`, and only a test and this record changed since.

### Gate and tests

| Check | Result |
|---|---|
| `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint --rerun-tasks` | exit 0, BUILD SUCCESSFUL, 1841 tasks executed |
| JVM tests in that run | 2319, 0 failed, 0 skipped (debug and release variants of the Android modules both count) |
| The same gate after review's domain test, without `--rerun-tasks`, on `defc7baa` plus the test | exit 0, BUILD SUCCESSFUL, 23 of 1841 tasks executed, 2323 JVM tests, 0 failed |
| `ANDROID_SERIAL=emulator-5570 ./gradlew :datasource:implementation:connectedDebugAndroidTest :designsystem:connectedDebugAndroidTest` | exit 0: 112 and 48 tests, 0 failed. Regression only: no module this change touches has instrumented tests, and `:app`'s one androidTest file does not reference this text, so `:app` was not run |
| `:app:checkReleasePermissions` | exit 0. `android.permission.INTERNET`: 0 lines in the 4 merged debug and release manifests. No manifest or Gradle file changed |

### Negative controls

| Claim | Control | Result |
|---|---|---|
| Domain tests pin 15, 10, 10, 10 (task 2.2) | Each enum value changed alone, up and down, running only `:features:medication:domain:test` (89 tests, 0 failed unmutated) | SimplePainkiller 16: 7 failed, 14: 2. Triptan 11: 13, 9: 5. CombinationOrOther 11: 2, 9: 1. Multiple 11: 4, 9: 1. So no new domain test was needed |
| The label test can fail (task 3.1) | Run against the old label | Failed: `expected:<30-day spans [reaching an ICHD-3 8.2 medication-day threshold]> but was:<30-day spans [at the medication-overuse limit]>` |
| "No label says limit" can fail | A temporary "limit" in another label | Failed: `expected:<[]> but was:<[Migraines with bed rest, limit]>` |
| Word-for-word against `design.md` | A script reads the Final copy table and compares it with every `strings.xml` and the mapper. Run before the change | Before: 14 of 17 differed, 1 of them the script's own bug (it read one module), fixed. After: 0 of 18 differ |
| `reached` is exactly "N or more" (added after review) | `OveruseStatusRowDomainModel.reached` changed to `days >= limit - 1` and to `days > limit`, running only `:features:medication:domain:test` | Before `OveruseStatusRowDomainModelTest`: both survived, 89 tests, 0 failed. With it, 93 tests: the first fails the two "one day below" tests, the second fails the two "exactly" tests |
| Task 4.1's grep | `git grep -nP "\blimit\b"` on `strings.xml` | 10 lines on `develop`, 0 here |

### Walk on `emulator-5570`

API 36, debug build of `2d1da37d`, seeded by SQL into the app's database with intakes counted back from today. Each state has a dump and a screenshot. Colour is the row's text pixels: neutral `#9aa0a9`, amber `#d9b26f`.

| Scenario | State | Read on the device | Result |
|---|---|---|---|
| Below the limit | simple 6 | "Simple painkillers: 6 days", neutral, no line | Pass |
| One day below the number | simple 14 | "Simple painkillers: 14 days", neutral, no line | Pass |
| One day | triptan 1; combination 1 | "Triptans: 1 day"; "Combination or other: 1 day" | Pass |
| Before the number is reached | triptan 9 | "Triptans: 9 days"; soft card "Triptans on 9 of the last 30 days", no other number | Pass |
| At the limit | triptan 10; simple 15; combination 10 | "Triptans: 10 days. Doctors watch for 10 or more days a month.", amber, then the line. Same for the others with 15 and 10 | Pass |
| Past the number | triptan 11 | "Triptans: 11 days. Doctors watch for 10 or more days a month.", amber, then the line | Pass |
| No migraine logged | simple 16, no attack | "Simple painkillers: 16 days. Doctors watch for 15 or more days a month.", amber | Pass |
| Two categories | simple 7, triptan 5 | Both neutral with the count only; "All attack medication: 12 days. Doctors watch for 10 or more days a month.", amber; the line | Pass |
| A reached row brings its line | simple 12, triptan 12 | Two amber rows, one neutral, the line once, after the rows | Pass |
| Soft warning | simple 13 | "Painkillers on 13 of the last 30 days" and "Taking them often can lead to more headaches. Worth keeping an eye on." | Pass |
| Doctor warning | triptan 10 | "Triptans on 10 of the last 30 days" and `overuse_body_doctor`, unchanged | Pass |
| Below the soft level | triptan 7 | No card, read as described below the table | Pass |
| Several categories together | simple 6, triptan 4 | "Acute medications on 10 of the last 30 days", doctor text | Pass |
| A single category already warned | simple 15, triptan 2 | Painkiller doctor card only | Pass |
| Line goes away and comes back; After an undo | 9 triptan days plus 1 in an attack | Before: 10, amber, line. Delete: "Triptans: 9 days", neutral, no number, no line. Undo: 10, amber, line | Pass |
| "Got it" does not touch the row | triptan 9, after "Got it" | Card gone, "Triptans: 9 days" stays | Pass |
| A past month | October and September, with data | No section, read as described below the table. "Days with attack medication" counts 4 and 1 | Pass |
| No attack medication | no intakes; then an attack today and an intake 40 days back only | No section, no card. Second state: "Days with attack medication: 0", no section | Pass |
| A list cap is not an overuse number | 50 medications, saving a 51st | "You can keep up to 50 medications. Remove one you no longer take." | Pass |
| Reached is said in words | dumps of the reached rows | One node holds the whole text, with no separate `content-desc` and no second node. TalkBack was never run or heard | **Partial**: the node text is proven, the speech the scenario asks about is not |
| Largest font, upright | 200%, simple 8, triptan 8 | The row wraps to 3 lines in a 293 px node, whole in the screenshot; the line below it is whole | Pass |
| Largest font, landscape | 200%, same state | The row wraps to 3 lines, whole in the screenshot, in the two-pane layout | Pass |
| Soft card in landscape | triptan 9 | Body whole after scrolling, at 100% and at 200% | Pass |
| Rotation | triptan 9, soft card | Same title and body after turning and turning back | Pass |
| Process death | `run-as com.migrely.app kill -9`, pid 10195 to 10334 | Same title and body | Pass |
| Upgrade with the card up | `develop` build, Home key, `install -r` | The old build's screenshot shows "Taking them on 10 days or more"; after the upgrade the new body, 0 fatal exceptions | Pass |
| The label does not say limit | triptan 11, 1 month; triptan 3, 1 month (`pdftotext`, pages viewed) | "30-day spans reaching an ICHD-3 8.2 medication-day threshold" with "8 Oct 2026 to 6 Nov 2026", and with "None" | Pass |
| Three months | 18 attacks, 4 at Bed rest, 92 days | "5.9 (18 days in all)", "4"; two spans in the value | Pass |
| A month only partly in the period | triptan 11, 1 month, across the month change | Span "8 Oct 2026 to 6 Nov 2026". Same case on today's dates, not the scenario's September dates | Pass |
| First page fits | Letter (2 and 3 pages) and en-GB A4 (2 pages) | The label stays on one line on both papers; summary, calendar, legend and footer fit on page 1 | Pass |

"No card" and "no section" were read twice. One dump in the first pass landed before the card had composed, even with the date header showing, so a first-pass absence proves little. So they were read again on a fresh boot: Home only once its calendar held the seeded marks plus 4 s, History after 6 s plus 3 s, with triptan 8 as the positive control under the same timing (its card showed). All held: triptan 7, simple 6, triptan 1 and combination 1 have no card, and the past months and the no-medication states have no section.

A script read the first pass's 29 dumps that hold the section: 46 rows, 19 reached, 0 with a forbidden word, a number before it is reached, a wrong number, or a line without a reached row. Its negative control caught the old row "Simple painkillers: 6 days (limit 15)".

### Device state afterwards

Data restored from the backup taken before the walk: database dump md5 `85c48a1a1df60501b81fe30b7f381cb8` before and after, entitlement back to `none`, and the app opens with 0 fatal exceptions. Font scale, rotation, animator scales, accessibility and airplane mode read the same before and after; the app locale is empty again. The emulator is shut down. The installed app is now this debug build, not the earlier 1.0.2 debug build.

### Not checked, and why

- **TalkBack speech.** TalkBack was never turned on or heard. The brief asks for labels through `uiautomator dump`, which proves one node holds the text, not what the voice says. So "Reached is said in words" is partial, and every other scenario in the walk passes.
- **The reached amber against a colour-blind view.** The words carry the state, so colour was only read, not simulated.
- **A saved parcel from the old build being read.** `install -r` stops the app, so the upgrade run may have loaded fresh rather than read the old parcel. The upgrade row shows no crash, not that path.
- **The old PDF's page count on the same data.** Only the new build was run. The label is on one line in both, so no extra line comes from it, but page counts were not compared.
- **The soft card upright at 200%.** Not read. The longer doctor card was, whole, and the soft card was at 200% in landscape.
- **API 26 and the owner's phone.** `emulator-5582` and the phone are not this role's. QA's check covers API 26.
- **Rollover across midnight during a walk.** The counting did not change, and no walk crossed midnight.
- **Store screenshots and promo video (task 6.8).** Not inspected: routed to `team-lead`.
- **No automated UI test holds the text.** `OveruseIndicator` and the strings have no JVM or instrumented test in the repo. Until `qa-engineer` updates `.maestro/` and `qa/` (task 6.7), the walk and the script above are the only checks of this text, and those QA checks fail on the old text, as they should.
