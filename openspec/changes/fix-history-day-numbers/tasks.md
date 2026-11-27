# Tasks

Base: `feature/fix-history-day-numbers` from `origin/develop` 476f7a27 (team-lead decision 2026-11-27: 1.4.1, its own
branch; #179 is a parallel change). BRD D198, EC-HIS-05, EC-HIS-06, EC-HIS-07, §13 row 63. Commits say `Refs #275`, never
a closing keyword. One writer per branch: android-engineer takes it after this plan.

## 1. Plan

- [x] 1.1 BRD and this change. Verify: `openspec validate fix-history-day-numbers --strict`.

No domain, data or datasource layer changes, and no strings.

## 2. Design system: the calendar (D198 (1)–(3), (5))

- [x] 2.1 Red first: `MonthSkyCalendarDayNumberTest` (Robolectric, `src/testDebug` of `designsystem`), qualifiers
  `w640dp-h360dp-land`, `fontScale = 2f`, the cell width History gives at 640 × 360 (about 34 dp), EN and DE: every day
  number has `lineCount == 1` and width ≤ cell − 8 dp; all numbers share one font size; the weekday initials too. Named
  negative control: red on develop (days 10–31 on 2 lines; record the run). Verify:
  `./gradlew :designsystem:testDebugUnitTest --tests '*MonthSkyCalendarDayNumberTest*'`.
- [x] 2.2 Floor case in the same test: a cell narrower than the 100% need gives exactly the 100% `labelSmall` size, never
  smaller.
- [x] 2.3 Implement in `MonthSkyCalendar.kt`: `maxLines = 1`, `softWrap = false`; fitted size by `TextMeasurer` over the
  widest two-digit day in Bold with `tnum`; one size per grid; `remember` keyed on cell width, font scale and density,
  never the month (design D2). Expose the number for the test per design D3 without changing TalkBack's label.
- [x] 2.4 Goldens unchanged: `MonthSkyCalendarPitchTest` passes with `designsystem/src/testDebug/resources/calendar/*.png`
  untouched. Verify: `git diff --stat origin/develop -- designsystem/src/testDebug/resources/calendar/` is empty, and
  the test passes.
- [x] 2.5 TalkBack unchanged: the existing calendar label and order tests pass, plus one assertion that a day's merged
  label equals develop's ("…, chosen" included).

## 3. History UI: pane guard and list rows

- [x] 3.1 Red first: `HistoryPaneGuardTest` (Robolectric, `features/history/ui/src/testDebug`): `w360dp-h380dp` at 100%
  gives one column and 1-line day numbers (red on develop: two panes, wrapped); `w640dp-h360dp-land` and
  `w800dp-h360dp-land` keep two panes; both sides of the 456 dp threshold. Verify:
  `./gradlew :features:history:ui:testDebugUnitTest --tests '*HistoryPaneGuardTest*'`.
- [x] 3.2 Implement the guard in `HistoryScreen.kt` (design D4).
- [x] 3.3 Red first: `HistoryAttackRowStackTest`: `w640dp-h360dp-land`, 200%, EN and DE, peaks "7/10" and "Not recorded" /
  "Nicht erfasst", and an attack in progress: every line break of the date at a space (`PickerTopRowTest` pattern) and
  the peak's top below the date's bottom. Named negative control: 360 dp portrait at 100% and at 200% (411 dp too), where
  the peak stays beside the date. Red on develop for the landscape cases (record). Verify:
  `./gradlew :features:history:ui:testDebugUnitTest --tests '*HistoryAttackRowStackTest*'`.
- [x] 3.4 Implement in `AttackRow` (design D5), in the month list and the chosen day's list; the merged label and
  `Role.Button` unchanged (assert it).
- [x] 3.5 `HistoryTouchTargetAuditTest` still passes (no pitch change). Verify: the module's test task.

## 4. Docs

- [ ] 4.1 Record the verification below. No user doc change.

## 5. Verify

- [x] 5.1 Gate. Verify: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exit 0 (check
  the load first; a timeout under load is rerun, not a result).
- [x] 5.2 Permissions. Verify: `./gradlew :app:checkReleasePermissions` exit 0; `reports/permissions/release.txt` equals
  develop 476f7a27's, and the merged release manifest is identical to develop's (`cmp` exit 0). Develop already has one
  `<queries>` block from the Billing library; the proof is identity, not a count.
- [ ] 5.3 Device walk, emulator-5570 (API 36), debug build of this branch; restore font scale, rotation, `wm size` and
  locale afterwards. Screenshots of every row:
  - (a) Re-take R25: History EN and DE, 640 × 360 dp landscape, 200%: every day number whole, by eye, days 10–31.
  - (b) Landscape with 3-button navigation at the side (about 251 dp pane): whole numbers.
  - (c) Split screen in portrait (`wm size` to a 360 × 380 dp window or real split): one column, whole numbers.
  - (d) Re-take R29: 360 × 640 portrait at 200%, and 411 dp at 100% and 200%: no visible change from develop.
  - (e) List rows in (a): date breaks only between words, peak below; an attack in progress row.
  - (f) TalkBack in (a): days and a stacked row say the same as on develop; keyboard Tab and arrows unchanged.
  - (g) Page months and rotate: numbers never jump between months.
- [ ] 5.4 Independent code review (`code-reviewer`) on `git diff origin/develop...HEAD`, briefed with this spec and risks
  R1–R5. Fix real findings and record them here.
- [ ] 5.5 QA independent check (only what this diff reaches, with negative controls):
  - API 36 (emulator-5580): R25 and R29 rows EN and DE, split screen in portrait, side navigation; the develop build as the
    negative control shows "1" for 16 in (a).
  - API 26 (emulator-5582, linear scale): (a) and (d) at 200%.
  - TalkBack day labels and the stacked row.
- [ ] 5.6 Archive: `openspec archive fix-history-day-numbers --yes`; `openspec validate --specs --strict` passes.

## Verification record

(Filled by android-engineer, then QA.)

### android-engineer, phase 1 (2026-11-27, no device)

Engineer's choices:
- **D3, reaching the text in a test:** `hideFromAccessibility()` would merge the number's Text into the day's merged
  node (Compose 1.9.3 `SemanticsNode.mergeConfig` merges hidden children), so the label would change. Instead each hidden
  Text keeps `clearAndSetSemantics` and sets only `ShownTextLayoutsKey` (designsystem `layout/ShownTextLayouts.kt`): a
  custom key, not important for accessibility, whose merge policy keeps the parent's value, so it never reaches the
  clickable. The row's date and peak moved their clearing from the `Row` to each `Text` for the same reason.
- **D2, the fit:** one place, `sky/FittedLabelStyle.kt` (`rememberFittedStyle`), used by the day numbers (widest of
  "10".."31" in Bold, room = drawn cell − 5 dp − 3 dp) and the weekday initials (widest initial, room = column).
  Binary search in whole px between the 100% size and the person's size; `remember` keyed on width, style, samples and
  density. When the person's size fits, the style object is returned unchanged.
- **D4, width source:** the `BoxWithConstraints` width History's content gets, less the horizontal system-bar and
  cutout insets and the 2 × 16 dp page padding (`LocalConfiguration.screenWidthDp` includes the bars on API 35+).
- **D5, widest unbreakable piece:** Compose's `minIntrinsicWidth` let the side-by-side branch through while the date
  still broke inside "12:27" (Robolectric, 78 px room); the row now measures its widest space-separated word itself
  with the same style (`text/WidestWidth.kt`, shared with the fit), so a no-break space keeps its pieces together.

Red on develop's code plus the test hook only (`migrely-work/275/red-final.txt`):
- `MonthSkyCalendarDayNumberTest` 3 of 5 fail: EN and DE landscape "10 (2 lines, 45.0 px of 78.43)" for days 10–31;
  floor case "expected 36.0 but was 72.0" px. Portrait and TalkBack cases pass (negative controls).
- `HistoryPaneGuardTest` 2 of 6 fail: 360 × 380 and 455 × 360 "two panes". 456, 640, 800 and portrait pass.
- `HistoryAttackRowStackTest` 4 of 8 fail: EN breaks after "Mon", DE after "Mo., 19. Okt., 12:2", the chosen day's list
  the same; the label case fails only at its last line ("the row is not stacked"), so the label equals develop's.
- Golden negative control: `NumberStartInset` 5 → 6 dp makes all four `MonthSkyCalendarPitchTest` pixel tests fail;
  restored.

Green: `:designsystem:testDebugUnitTest` 381 passed, 0 failed; `:features:history:ui:testDebugUnitTest` 83 passed,
0 failed (incl. `HistoryTouchTargetAuditTest` 6 of 6). `git diff --stat origin/develop --
designsystem/src/testDebug/resources/calendar/` is empty.

Deviations from the plan's wording (for product-manager):
- 3.3 names `w640dp-h360dp-land`. Robolectric draws no insets, so 640 dp gives 292 dp panes, wider than R25's measured
  275 dp, and there develop breaks no German word. The stacking cases use `w606dp-h360dp-land-xxhdpi` (275 dp panes,
  R25's geometry); a 640 dp case checks only that no word breaks.
- The attack-history scenario "Landscape at 200%" says the "7/10" row stacks too. By the requirement (beside while the
  room holds the widest piece) it stays beside in R25's geometry and breaks only between words; the code follows the
  requirement. The tests assert stacking for "Not recorded" / "Nicht erfasst" and no broken word for every row.
- 2.5: the instrumented `MonthSkyCalendarTest` and `MonthSkyCalendarKeyboardTest` need a device: phase 2.

Gate (load under 8 at start): `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exit 0,
BUILD SUCCESSFUL in 4m 10s; 4027 JVM tests in 68 result sets, 0 failures, 0 errors, 0 skipped.

Permissions: `./gradlew :app:checkReleasePermissions` exit 0 on this branch and on a detached develop 476f7a27
worktree; `cmp` of `app/build/reports/permissions/release.txt` exit 0 and of the merged release
`AndroidManifest.xml` exit 0 (identical). INTERNET 0; the one `<queries>` block is develop's (Billing).

Not checked in phase 1: anything on a device (5.3), the instrumented tests, API 26.
