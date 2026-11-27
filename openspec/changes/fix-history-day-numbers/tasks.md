# Tasks

Base: `feature/fix-history-day-numbers` from `origin/develop` 476f7a27 (team-lead decision 2026-11-27: 1.4.1, its own
branch; #179 is a parallel change). BRD D198, EC-HIS-05, EC-HIS-06, EC-HIS-07, §13 row 63. Commits say `Refs #275`, never
a closing keyword. One writer per branch: android-engineer takes it after this plan.

## 1. Plan

- [x] 1.1 BRD and this change. Verify: `openspec validate fix-history-day-numbers --strict`.

No domain, data or datasource layer changes, and no strings.

## 2. Design system: the calendar (D198 (1)–(3), (5))

- [ ] 2.1 Red first: `MonthSkyCalendarDayNumberTest` (Robolectric, `src/testDebug` of `designsystem`), qualifiers
  `w640dp-h360dp-land`, `fontScale = 2f`, the cell width History gives at 640 × 360 (about 34 dp), EN and DE: every day
  number has `lineCount == 1` and width ≤ cell − 8 dp; all numbers share one font size; the weekday initials too. Named
  negative control: red on develop (days 10–31 on 2 lines; record the run). Verify:
  `./gradlew :designsystem:testDebugUnitTest --tests '*MonthSkyCalendarDayNumberTest*'`.
- [ ] 2.2 Floor case in the same test: a cell narrower than the 100% need gives exactly the 100% `labelSmall` size, never
  smaller.
- [ ] 2.3 Implement in `MonthSkyCalendar.kt`: `maxLines = 1`, `softWrap = false`; fitted size by `TextMeasurer` over the
  widest two-digit day in Bold with `tnum`; one size per grid; `remember` keyed on cell width, font scale and density,
  never the month (design D2). Expose the number for the test per design D3 without changing TalkBack's label.
- [ ] 2.4 Goldens unchanged: `MonthSkyCalendarPitchTest` passes with `designsystem/src/testDebug/resources/calendar/*.png`
  untouched. Verify: `git diff --stat origin/develop -- designsystem/src/testDebug/resources/calendar/` is empty, and
  the test passes.
- [ ] 2.5 TalkBack unchanged: the existing calendar label and order tests pass, plus one assertion that a day's merged
  label equals develop's ("…, chosen" included).

## 3. History UI: pane guard and list rows

- [ ] 3.1 Red first: `HistoryPaneGuardTest` (Robolectric, `features/history/ui/src/testDebug`): `w360dp-h380dp` at 100%
  gives one column and 1-line day numbers (red on develop: two panes, wrapped); `w640dp-h360dp-land` and
  `w800dp-h360dp-land` keep two panes; both sides of the 456 dp threshold. Verify:
  `./gradlew :features:history:ui:testDebugUnitTest --tests '*HistoryPaneGuardTest*'`.
- [ ] 3.2 Implement the guard in `HistoryScreen.kt` (design D4).
- [ ] 3.3 Red first: `HistoryAttackRowStackTest`: `w640dp-h360dp-land`, 200%, EN and DE, peaks "7/10" and "Not recorded" /
  "Nicht erfasst", and an attack in progress: every line break of the date at a space (`PickerTopRowTest` pattern) and
  the peak's top below the date's bottom. Named negative control: 360 dp portrait at 100% and at 200% (411 dp too), where
  the peak stays beside the date. Red on develop for the landscape cases (record). Verify:
  `./gradlew :features:history:ui:testDebugUnitTest --tests '*HistoryAttackRowStackTest*'`.
- [ ] 3.4 Implement in `AttackRow` (design D5), in the month list and the chosen day's list; the merged label and
  `Role.Button` unchanged (assert it).
- [ ] 3.5 `HistoryTouchTargetAuditTest` still passes (no pitch change). Verify: the module's test task.

## 4. Docs

- [ ] 4.1 Record the verification below. No user doc change.

## 5. Verify

- [ ] 5.1 Gate. Verify: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exit 0 (check
  the load first; a timeout under load is rerun, not a result).
- [ ] 5.2 Permissions. Verify: `./gradlew :app:checkReleasePermissions` exit 0; `reports/permissions/release.txt` equals
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
