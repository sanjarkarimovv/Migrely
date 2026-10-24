# Tasks

## 1. Requirements and baseline

- [x] 1.1 BRD §13 row 4b and §17 D62. Verify: `grep -n "| 4b |\|| D62 " docs/BRD.md`
- [x] 1.2 Baseline on develop c8b4e7c: full `qa/maestro.sh --install` and `qa/a11y_audit.sh`. Triage each failure as
  stale test or app defect, fix stale flows, and file defects. Verify: the regression passes except for filed issues. Result on c8b4e7c: pass 1
  26/29 and pass 2 9/9; audit 0 failures. The 3 failures were stale tests: `open-from-day`, `rotation` and
  `add-for-empty-day` looked for the chosen day's controls, which changes 4 and 5 moved below the month summary. On the
  device the controls are there. Fixed with scrolls (9138673); each passes on 399934a

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

- [x] 3.1 Scenario `landscape_band`: while the start message shows, the guidance ("Rest now.") starts at or below the
  message's bottom, read from uiautomator; once the message has gone, the guidance is back up. `landscape-message-clear`
  turns its "Partly checks" line into a Covers line. Verify: passes twice (done 2026-10-24 22:19: message ends at
  y=215, guidance at y=263, back at y=105)
- [x] 3.2 The same with Reduce motion (animator scale 0), which is saved and restored, also by the exit trap together with
  the rotation. Verify: passes twice; `settings get global animator_duration_scale` shows the original value (null).
  "A tap while the guidance moves" is not checked black-box: a successful "2 h earlier" shows no message and does not
  move the column (measured), and the 500 ms guard after a rejected move cannot be told apart from timing on this
  emulator. It is listed as a gap, with a Compose UI test (`mainClock`) suggested to the dev session

## 4. Day markers and the month sky

- [x] 4.1 `day-markers/mark-and-remove.yaml`: Period on a past day shows the drop and "Period."; turning it off removes
  it; Pain-free shows a clear sky "…: pain-free."; a future day cannot be chosen. Verify: passes 3 times
- [x] 4.2 `day-markers/attack-day.yaml`: on an attack day "Pain-free day" is disabled with "A migraine is logged on this
  day."; Period and a storm on one day; an attack added later on a pain-free day wins and deleting it brings the clear
  sky back. Verify: passes 3 times
- [x] 4.3 `day-markers/home-chip.yaml`: "Period today" checked and not checked, today's cell shows the mark, no chip in
  attack mode, "Pain-free today" / "Marked by you". Verify: passes 3 times
- [x] 4.4 `month-sky/legend.yaml`: 6 entries. Verify: passes 3 times
- [x] 4.5 Device scenarios `mark_travel` (EC-DAY-04) and `mark_earliest_month` (D51). Verify: each passes twice (done 2026-10-24 22:35). `mark_earliest_month` traces to `attack-history` › "A mark before any attack"

## 5. Audit

- [x] 5.1 The audit's keyboard rule stays (design D3): every day cell is focusable and clickable for accessibility
  services, measured on the device, and the Tab order is checked by key events. No Reduce-motion audit states: Reduce
  motion changes only the animation, and `landscape_band` checks both modes. Verify: `qa/a11y_audit.sh` shows 0
  failures on the new base (baseline 21:26: 0 failures; rerun in 7.1)

## 6. Documents

- [x] 6.1 `docs/QA.md`: key-event checks (D62), Reduce motion, the new scenarios; `.maestro/README.md` timing rules
  for keys. Verify: `grep -n "D62" docs/QA.md`

- [x] 6.2 Review fixes (`superpowers:code-reviewer`: 4 important and 6 minor findings, plus 5 suspicions):
  - the future-day check is unconditional and limited to the grid;
  - "Attack day" is proven by behaviour: after the attack is deleted, the day has no pain-free mark;
  - a severe attack is used for "Period and migraine";
  - `mark_travel` runs at noon in Tashkent, so it does not depend on the host's zone;
  - `landscape_band` also checks the start line and the message's right edge, and is marked "Partly checks";
  - the home chip's checked state is read through the keyboard;
  - attack mode and the card after an attack are checked;
  - dumps are removed before reading, the animator scale is read back, and the locale parse is safe;
  - the docs were aligned.
  Verify: each changed flow passes 3 times, and each changed scenario passes

- [x] 6.3 Found in verification: the audit's `history-month` state at 200% could not be captured, because the month
  summary (change 5) pushes the list down; `history_death` and `travel_west` had the same stale History checks, and
  `travel-west-after`'s absence check had become a false pass. Restores are read back (`qa_restore_setting`), each
  script warns when it starts away from the baseline (`qa_check_baseline`), and `qa/reset_device.sh` resets it.
  Cause: auto-rotate was left off after the emulator stopped answering under host load 56 and crashed. Verify:
  `history_death` and `travel_west` pass twice; the audit captures every state; smoke, audit and monkey leave the
  baseline

## 7. Verify

- [x] 7.1 Full regression, all device scenarios, the audit, `python3 -I qa/e2e_coverage.py` (exit 0), the permission
  audit. Record the results, the coverage, and the day-markers and month-sky gaps with reasons (D61) in a
  "Verification record"
- [ ] 7.2 Comment the #22 and #26 retests on PR #30 and close them when they pass
- [x] 7.3 Gate `./gradlew assembleDebug test ktlintCheck detekt` and `openspec validate add-day-markers-e2e-coverage
  --strict`

## Day-markers and month-sky scenarios without a flow (D61)

| Scenario | Reason | Follow-up |
|---|---|---|
| day-markers › Accessible marks › Disabled switch | The explanation is asserted and the disabled tap is proven by behaviour (`attack-day`). The disabled state sits on the parent of the label, where Maestro cannot read it | Read the switch's `enabled` through the focused node, as `expect_checked` does for the chip |
| month-sky › One sky per day from data: 8 scenarios (Worst attack decides, Pain level never recorded, Unrecorded and mild, Nothing logged, Future day, Attack in progress, Pain-free day, Period day) | They are about drawings (sky, drop, dashed outline). The spoken labels for the same days are asserted | Screenshot comparison in the visual-regression QA change (D42) |
| month-sky › Days follow where the attack was logged › Past midnight, Ends at midnight | The `midnight` scenario checks the attack, not the month's cells | Add the cell check to `midnight` |
| month-sky › Calendar follows the locale › Week starts on Monday, Week starts on Sunday | Needs another locale | App language through `cmd locale`, as `keyboard_rtl` does |
| month-sky › Calendar follows the locale › Today outlined | A drawing | D42 |
| month-sky › Accessible days › Screen reader on a storm day | Today's storm cell is asserted (`attack-day`), but not a past one | One more cell in `attack-day` |
| month-sky › Accessible days › Screen reader on today during an attack | Not written yet | Next QA change |
| month-sky › Accessible days › Large font | Checked by the audit at 200% (home and History), which has no Covers lines | — |
| month-sky › Only the shown month is loaded › Ten years of history | Needs about 3,600 attacks: seeding (D60 sqlite fallback) and a performance budget | Release QA |
| month-sky › Days reachable with a keyboard › Accessibility services | Measured on the device (each cell's node is focusable and clickable) and enforced by the audit's keyboard rule | — |
| in-app-messages › Landscape attack mode › Message during landscape attack mode | Partly checked: the controls, the band, the start line, the message's right edge and the return. Not checked: "at once" with Reduce motion (timing) and tapping a control under the message | — |
| in-app-messages › Landscape attack mode › A tap while the guidance moves | Cannot be timed black-box (design, task 3.2) | The dev session adds a Compose UI test with `mainClock` after change 6 |

## Verification record (2026-10-25, emulator-5580, AVD `Migrely_QA_API_36`)

Base: develop 399934a (changes 4, 4a, 5 and 5a) merged into the branch. The product code is the same as develop's.

| Check | Result |
|---|---|
| Gate `./gradlew assembleDebug test ktlintCheck detekt` | ✅ exit 0 (016de21) |
| New and changed flows (`day-markers`, `month-sky`, `attack-history`, `landscape-message-clear`) | ✅ 3/3 runs each |
| `qa/maestro.sh` | ✅ pass 1: 33/33; pass 2: 9/9 |
| `qa/device_scenarios.sh` (24 scenarios) | ✅ 23/24 at first. `history_death` failed: a stale History check, the same pattern as the 3 baseline flows. Fixed, then 2/2 together with `travel_west` |
| Keyboard scenarios (`keyboard_home`, `keyboard_history`, `keyboard_rtl`) | ✅ 2/2 each |
| `landscape_band` (animated and Reduce motion) | ✅ 2/2: message ends at y=215 and x=1147; guidance at y=263, start line at y=352, controls from x=1419; back to y=105 |
| `mark_travel` and `mark_earliest_month` | ✅ 2/2 each |
| `qa/a11y_audit.sh` | ✅ 0 failures and every state captured, after the `history-month` 200% fix |
| Smoke (`--tags smoke`) | ✅ 14/14 + 3/3 |
| `qa/monkey.sh 20261025 1500` | ✅ 0 crashes, 0 ANRs |
| `qa/permission_audit.sh` | ✅ passed |
| `python3 -I qa/e2e_coverage.py` | ✅ exit 0. 136 of 361 scenarios (38%); day-markers 12/13, month-sky 10/28, today-sky 18/34 |
| Device settings after every step | ✅ baseline: auto-rotate 1, rotation 0, font 1.0, timeout null, animator null, auto_time 1 |
| `openspec validate add-day-markers-e2e-coverage --strict` | ✅ valid |

**Retests:**
- **#22 passes.** Home and History are one Tab stop each, with arrows, Enter and Shift+Tab; right-to-left is mirrored.
- **#26 passes**, animated and with Reduce motion.
- Both are commented on PR #30 and closed (task 7.2).

**Product bugs:** none found in changes 4, 4a, 5 and 5a within this scope.

**Suspicions disproved by measurement:**
- day cells were not focusable: the parent node is;
- the home card did not show "Pain-free today": it does, as one card label;
- the History controls were missing: they sit below the month summary.

**Observations accepted by the dev session:**
- DPAD_UP enters the grid on the nearest day;
- "peak Not recorded" is capitalised on home.

**QA tooling defects found and fixed:**
- an emulator crash under host load 56 left auto-rotate off, with no warning. Restores are now read back, scripts check the baseline, and `qa/reset_device.sh` was added;
- three History checks were stale, one of them a false pass.

**Review** (`superpowers:code-reviewer`): 4 important and 6 minor findings, plus 5 suspicions. All handled (016de21), as task 6.2 lists.
