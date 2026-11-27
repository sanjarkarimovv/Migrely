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

- [x] 2.6 Code review ee02ad70 S1: in the 140 dp case also assert that the weekday initials have no visual overflow and are
  no wider than their column (red on develop's code: the initials keep the person's size); in the landscape cases assert
  that one px more than the chosen size no longer fits ("largest size that fits"). Verify:
  `./gradlew :designsystem:testDebugUnitTest --tests '*MonthSkyCalendarDayNumberTest*'`.
- [x] 2.7 Code review N2: clamp the floor in `FittedLabelStyle.kt` to min(100% size, the person's size) (one line), with a
  test at font scale 0.85 and a narrow width where the person's size does not fit: the result is never larger than the
  person's size. Named negative control: red on the current branch code (ee02ad70) before the clamp.
- [x] 2.8 Code review N3: re-run every red run against the COMMITTED test files (2.1, 2.2, 2.6, 2.7, 3.1, 3.3, 3.6, each on
  develop's product code with only the test hook), and keep the logs in `migrely-work/275/` with the commit SHA in the
  name. The test names and failure lines in the log must match the committed files.
- [x] 2.9 Code review N5: "420–480 dpi unchanged" is pinned by the row tests at xxhdpi (480 dpi) and on the device by walk
  (d). Add one 420 dpi (`w360dp-h640dp-port-420dpi`) case to the portrait negative controls of
  `HistoryAttackRowStackTest`; it is one more `@Config` line.
- [ ] 2.10 Instrumented on emulator-5570 (the calendar's focus modifiers moved onto a `BoxWithConstraints`): Verify:
  `ANDROID_SERIAL=emulator-5570 ./gradlew :designsystem:connectedDebugAndroidTest` with `MonthSkyCalendarTest` and
  `MonthSkyCalendarKeyboardTest` green (one Tab stop, Tab enters on the anchor day, arrows, Shift+Tab leaves the grid).

## 3. History UI: pane guard and list rows

- [x] 3.1 Red first: `HistoryPaneGuardTest` (Robolectric, `features/history/ui/src/testDebug`): `w360dp-h380dp` at 100%
  gives one column and 1-line day numbers (red on develop: two panes, wrapped); `w640dp-h360dp-land` and
  `w800dp-h360dp-land` keep two panes; both sides of the 456 dp threshold. Verify:
  `./gradlew :features:history:ui:testDebugUnitTest --tests '*HistoryPaneGuardTest*'`.
- [x] 3.2 Implement the guard in `HistoryScreen.kt` (design D4).
- [x] 3.3 (as built: stacking cases at `w606dp-h360dp-land-xxhdpi`, R25's 275 dp pane, and one `w640dp-h360dp-land` case for "no word breaks" only, design R6) Red first: `HistoryAttackRowStackTest`: `w640dp-h360dp-land`, 200%, EN and DE, peaks "7/10" and "Not recorded" /
  "Nicht erfasst", and an attack in progress: every line break of the date at a space (`PickerTopRowTest` pattern) and
  the peak's top below the date's bottom. Named negative control: 360 dp portrait at 100% and at 200% (411 dp too), where
  the peak stays beside the date. Red on develop for the landscape cases (record). Verify:
  `./gradlew :features:history:ui:testDebugUnitTest --tests '*HistoryAttackRowStackTest*'`.
- [x] 3.4 Implement in `AttackRow` (design D5), in the month list and the chosen day's list; the merged label and
  `Role.Button` unchanged (assert it).
- [x] 3.5 `HistoryTouchTargetAuditTest` still passes (no pitch change). Verify: the module's test task.

- [x] 3.6 Ruling 2026-11-27 (product-manager): add one test to `HistoryAttackRowStackTest` at
  `en-rUS-w360dp-h640dp-port-mdpi`, 200%, with an attack in progress: the row stacks and no word breaks. Named negative
  control: red on develop ("In progres|s"). No product code change. Verify:
  `./gradlew :features:history:ui:testDebugUnitTest --tests '*HistoryAttackRowStackTest*'`.

## 4. Docs

- [x] 4.1 Phase 1 verification recorded below (2.x, 3.x, 5.1, 5.2). No user doc change.
- [ ] 4.2 Final record after 2.6–2.10, 5.3 and 5.5, with the scenario map below filled in.

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
  - (h) In (a) at the fitted size, by eye and zoomed screenshot: the digits stay clear of the period drop, the chosen ring
    and the focus ring (the line height stays at the person's size while the font shrinks).
  - (i) A cutout on each side (Developer options › Display cutout) in landscape at 200%: panes and numbers as in (b).
  - (j) uiautomator dump in (a): a stacked row and an unstacked row are each one node with the same label as on develop,
    and no extra empty nodes appear; the days likewise.
  - (k) "Force RTL layout direction" in (a): numbers at the top start, mirrored, whole; Tab and arrows unchanged.
  - (l) Enter and leave split screen with History open on a chosen day in a past month: the month and the chosen day stay;
    the scroll position starts again at the top, as on develop.
  - (m) Display size Largest, portrait and landscape at 200%: the layout follows the 456 dp rule, numbers whole.
  - (n) A pop-up (freeform) window, if the emulator offers one: the same rule; else record "not available".
- [ ] 5.4 Independent code review (`code-reviewer`) on `git diff origin/develop...HEAD`, briefed with this spec and risks
  R1–R5. Fix real findings and record them here.
- [ ] 5.5 QA independent check (only what this diff reaches, with negative controls):
  - API 36 (emulator-5580): R25 and R29 rows EN and DE, split screen in portrait, side navigation; the develop build as the
    negative control shows "1" for 16 in (a).
  - API 26 (emulator-5582, linear scale): (a) and (d) at 200%.
  - TalkBack day labels and the stacked row; a uiautomator dump with no extra empty nodes.
  - API 36: Force RTL (k), entering and leaving split screen (l), Display size Largest (m), a cutout (i).
- [ ] 5.6 Archive: `openspec archive fix-history-day-numbers --yes`; `openspec validate --specs --strict` passes.

## Scenario map

| Scenario | Proof |
|---|---|
| month-sky: landscape at 200%; all one size; largest that fits; floor; smaller font | 2.1, 2.2, 2.6, 2.7; walk (a), (b) |
| month-sky: portrait stays (negative control); check can fail | 2.1, 2.4 goldens, 2.8; walk (d) |
| month-sky: paging and rotating | walk (g) |
| month-sky: rings, drops, future days | walk (h) |
| month-sky: first use and loading | 2.1 (same drawing path); walk (a) on an empty diary is optional |
| month-sky: TalkBack, keyboard, RTL | 2.5, 2.10; walk (f), (k); QA |
| month-sky: dark, light, Premium, attack | No row: the fit has no theme, entitlement or attack branch |
| attack-history: landscape; split screen; threshold; check can fail | 3.1; walk (b), (c), (i) |
| attack-history: pop-up window or larger display size | walk (m), (n); QA |
| attack-history: switching layout keeps the place | walk (l); QA |
| attack-history rows: landscape; longest peak; room enough; portrait break; in progress; check can fail | 3.3, 3.6, 2.9; walk (e) |
| attack-history rows: TalkBack and keyboard | 3.3 label case; walk (f), (j) |

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

### android-engineer, phase 1b (2026-11-27, no device)

- 3.6, red on develop's History code plus the test hook only: `Portrait 360 dp mdpi at 200 percent - an attack in
  progress stacks, no word breaks` fails with "breaks inside a word after "Fri, Oct 23, 12:27 PM – In progres"".
  Green on this branch: `HistoryAttackRowStackTest` 9 passed, 0 failed. No product code change.
- Merged origin/develop f83dda71 (#293, tests only) with a merge commit; no conflicts.
- Gate on the merged tree (load 7.87 at start): exit 0, BUILD SUCCESSFUL in 3m 27s; 4037 JVM tests in 68 result sets,
  0 failures, 0 errors, 0 skipped (develop's `DialogPaneEventTest` 6 and `PainDialogPaneEventTest` 3 included).
- `:app:checkReleasePermissions` exit 0 here and on a detached f83dda71 worktree; `cmp` of `release.txt` and of the
  merged release manifest both exit 0; INTERNET 0. Goldens: `git diff --stat origin/develop --
  designsystem/src/testDebug/resources/calendar/` empty.

### android-engineer, phase 1c (2026-11-27, no device): review ee02ad70 fixes, commit 7df72cc5

- 2.7 (N2), the only product-code change: `FittedLabelStyle.kt` floor is `minOf(100% size, the person's size)`.
  Test `At 85 percent a cell too narrow keeps the person's own size, never larger`: red on ee02ad70's fit,
  "expected 30.600002 but was 36.0" at `MonthSkyCalendarDayNumberTest.kt:259`; green after.
- 2.6 (S1): mutants on 7df72cc5's tests (`migrely-work/275/mutants-2.6.txt`, `.log`): with the initials fit removed
  the 140 dp case fails at `:181` from `:245` ("M (1 lines, 67.0 px of 44.57)", T, W, S too); with a fit that keeps
  one px less than the largest that fits both landscape cases fail at `:166` ("78 px at one px more still fits 78 px").
  Restored; green on the branch.
- 2.8 (N3): `migrely-work/275/red-runs.sh` re-ran every red run against the committed test files of 7df72cc5. These
  files replace `red-final.txt`, `red-final.log` and `red-3.6.txt`:
  - `red-7df72cc5-develop-hook.txt` and `.log` (develop's product code plus only the test hook): DayNumber 3 fail
    (`:181` EN and DE landscape "10 (2 lines, 45.0 px of 78.43)", `:241` floor "expected 36.0 but was 72.0"; the 85%
    case passes there, as develop never fits); PaneGuard 2 fail (`:80` 360 × 380, `:93` 455 dp, "two panes");
    RowStack 5 fail (`:133` EN after "Mon", DE after "Mo., 19. Okt., 12:2", chosen day's list, mdpi portrait after
    "In progres"; `:241` "the row is not stacked", so the label still equals develop's). Every line named points at
    the same assertion in the commit.
  - `red-7df72cc5-ee02ad70-fit.txt` and `.log` (ee02ad70's fit): only the 85% case fails, at `:259`.
- 2.9 (N5): `Portrait 360 dp 420 dpi at 200 percent - the peak stays beside (negative control)` passes on the
  branch and on develop.
- Gate on 7df72cc5 (load under 8): exit 0, BUILD SUCCESSFUL; 4039 JVM tests in 68 result sets, 0 failures, 0 errors,
  0 skipped. `:app:checkReleasePermissions` exit 0 here and on a detached f83dda71 worktree; `cmp` of `release.txt`
  and of the merged release manifest both exit 0; INTERNET 0. Goldens `git diff --stat origin/develop --
  designsystem/src/testDebug/resources/calendar/` empty.
