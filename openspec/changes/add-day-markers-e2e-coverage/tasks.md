# Tasks

## 1. Requirements and baseline

- [x] 1.1 BRD §13 row 4b and §17 D62. Verify: `grep -n "| 4b |\|| D62 " docs/BRD.md`
- [ ] 1.2 Baseline on develop c8b4e7c: full `qa/maestro.sh --install` and `qa/a11y_audit.sh`. Triage each failure as
  stale test or app defect, fix stale flows, and file defects. Verify: the regression passes except for filed issues

## 2. Keyboard (#22, D38, D62)

- [x] 2.1 Check that Maestro's `pressKey: Tab` and the D-pad keys reach the app on emulator-5580; otherwise use adb.
  Verify: a probe flow moves focus from "Period" to the month title. Result: Maestro's Tab works, but its
  `focused: true` selector cannot see the label, which Compose keeps on a child of the focused node. So every key goes
  through adb (`input keyevent`, `input keycombination 59 61` for Shift+Tab) in `qa/device_scenarios.sh`, which reads
  the labels under the focused node (design D1, fallback)
- [x] 2.2 Scenario `keyboard_home` on the 24th: Tab order "Period today" → month title → today → "Migraine started";
  Shift+Tab back the same way; today stays focused on right and down; arrows ±1 and ±7 days (14 → 15 → 22); Enter on
  the 17th opens History with it chosen. Verify: passes twice (done 2026-10-24 22:10)
- [x] 2.3 Scenario `keyboard_history`: Tab reaches the month on today; Enter chooses the 23rd; Tab leaves the month and
  Shift+Tab comes back to the chosen day. Verify: passes twice (done 2026-10-24 22:10)
- [x] 2.4 Scenario `keyboard_rtl` (app language Hebrew): the right arrow goes back a day, the left one forward, and
  stops at today. The app language is restored, also by the exit trap. Verify: passes twice (done 2026-10-24 22:10)
- [x] 2.5 `qa/e2e_coverage.py` reads `# Covers:` lines from `qa/device_scenarios.sh` and the delta specs of open
  changes. Verify: exit 0 with the keyboard Covers lines

## 3. Landscape message band (#26, D52)

- [ ] 3.1 `in-app-messages/landscape-message-clear.yaml`: the guidance's first line is below the message while it
  shows and comes back after it goes; the "Partly checks" line becomes Covers. Verify: passes 3 times
- [ ] 3.2 Device scenario `reduce_motion`: with animator scale 0 the guidance moves at once, and the scale is restored.
  Also check a tap on "2 h earlier" while the guidance moves. Verify: passes twice; `settings get global
  animator_duration_scale` shows the original value

## 4. Day markers and the month sky

- [ ] 4.1 `day-markers/mark-and-remove.yaml`: Period on a past day shows the drop and "Period."; turning it off removes
  it; Pain-free shows a clear sky "…: pain-free."; a future day cannot be chosen. Verify: passes 3 times
- [ ] 4.2 `day-markers/attack-day.yaml`: on an attack day "Pain-free day" is disabled with "A migraine is logged on this
  day."; Period and a storm on one day; an attack added later on a pain-free day wins and deleting it brings the clear
  sky back. Verify: passes 3 times
- [ ] 4.3 `day-markers/home-chip.yaml`: "Period today" checked and not checked, today's cell shows the mark, no chip in
  attack mode, "Pain-free today" / "Marked by you". Verify: passes 3 times
- [ ] 4.4 `month-sky/legend.yaml`: 6 entries. Verify: passes 3 times
- [ ] 4.5 Device scenarios `mark_travel` (EC-DAY-04) and `mark_earliest_month` (D51). Verify: each passes twice

## 5. Audit

- [ ] 5.1 `qa/a11y_audit.py`: the grid rule (design D3); `qa/a11y_audit.sh`: landscape attack states with and without
  Reduce motion, saving and restoring the animator scale. Verify: `qa/a11y_audit.sh` shows 0 failures, and a
  non-focusable clickable outside the grid still fails (unit check with a recorded dump)

## 6. Documents

- [ ] 6.1 `docs/QA.md`: key-event checks (D62), Reduce motion, the new scenarios; `.maestro/README.md` timing rules
  for keys. Verify: `grep -n "D62" docs/QA.md`

## 7. Verify

- [ ] 7.1 Full regression, all device scenarios, the audit, `python3 -I qa/e2e_coverage.py` (exit 0), the permission
  audit. Record the results, the coverage, and the day-markers and month-sky gaps with reasons (D61) in a
  "Verification record"
- [ ] 7.2 Comment the #22 and #26 retests on PR #30 and close them when they pass
- [ ] 7.3 Gate `./gradlew assembleDebug test ktlintCheck detekt` and `openspec validate add-day-markers-e2e-coverage
  --strict`
