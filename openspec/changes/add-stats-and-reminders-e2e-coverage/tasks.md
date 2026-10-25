# Tasks

## 1. Requirements and baseline

- [x] 1.1 BRD §13 row 6b and §17 D63 and D64. Verify: `grep -n "| 6b |\|| D63 \|| D64 " docs/BRD.md`
- [x] 1.2 Retest #36 on d0e0214: `qa/permission_audit.sh` passes, and a mutation test (INTERNET added locally and
  reverted) fails `:app:checkDebugPermissions`. Verify: commented on PR #39
- [x] 1.3 Baseline on d0e0214: full `qa/maestro.sh --install` and all device scenarios. Triage each failure as a stale
  test or an app defect; fix the stale checks (the "Settings" gear is the first Tab stop); file defects. Verify: the
  stale fixes pass 3 times or twice. Result:
  - on d0e0214: pass 1 32/33 and pass 2 9/9; the device scenarios all passed except `keyboard_home` and
    `keyboard_rtl`, because the "Settings" gear is now first;
  - `open-from-day` could not reach today's cell on the 25th: it sits below the screen late in the month, so the
    flow now scrolls to it;
  - develop 7df86c6 (change 8) added onboarding: `launch-clean` skips its 4 steps through "Skip this step";
  - on 7df86c6: 33/33, 9/9 and all device scenarios pass

## 2. Month summary and progress (STA-F*, EC-UX-14, D53–D58)

- [ ] 2.1 Flows `month-stats/summary.yaml`:
  - nothing logged yet;
  - the current month ("October so far") and a past month;
  - the rows read "Migraine days: N";
  - preventive medication not counted;
  - the average peak, and "no pain level logged".
  Verify: passes 3 times
- [ ] 2.2 Device scenarios `stats_counting`:
  - an attack from the month before;
  - an attack over two months;
  - a pain-free mark under an attack;
  - a rounded average peak.
  Verify: passes twice
- [ ] 2.3 Device scenario `stats_chronic`: the card at 15 migraine days, none at 14. Verify: passes twice
- [ ] 2.4 Device scenario `stats_progress`:
  - fewer (warm, read from the pixels) and more (neutral);
  - "than this time last month" compares the same dates, not the whole month;
  - too few entries in the comparison month (8 days);
  - on home under the month title;
  - none in attack mode.
  Verify: passes twice

## 3. Overuse indicator (EC-MED-06, D59)

- [ ] 3.1 Device scenario `med_indicator`:
  - below the limit;
  - at the limit (amber, ", at the limit", the doctor line);
  - two categories and "All attack medication";
  - after an undo;
  - a past month has none;
  - none without attack medication;
  - day 29 counts and day 30 does not.
  Verify: passes twice

## 4. Reminders (REM-F*, EC-REM-01…06, D65–D69, D64)

- [ ] 4.1 Flows `reminders/screen.yaml`: Settings → "Reminders"; the defaults (off, 8:00 PM, time row shown); no
  Settings in attack mode. Verify: passes 3 times
- [ ] 4.2 Device scenario `reminder_permission`:
  - asked only when a switch is turned on;
  - "Don't allow" leaves it off, with "Open settings";
  - no new ask within 29 days, and asked again after 31;
  - allowed turns it on;
  - the permission state is restored.
  Verify: passes twice
- [ ] 4.3 Device scenario `reminder_daily` (D64):
  - a calm day posts "How was today?";
  - "Pain-free day" marks the date;
  - on an attack day it says "A migraine is logged today.";
  - "Log a migraine" opens home and clears the notification;
  - nothing on a pain-free day or an attack day. (A day already shown is not checked: a forced run after the day's
    reminder runs the next day's job, so "nothing" would not prove the rule; it is a D61 gap.)
  Verify: passes twice
- [ ] 4.4 Device scenario `reminder_replan`:
  - a zone change and a clock change re-plan to local 8:00 PM with the app in the background, not restarted;
  - after a reboot (a new boot_id) the daily job is planned for the next 8:00 PM, before the app is opened
    (EC-REM-04).
  Verify: passes twice
- [ ] 4.5 Device scenario `reminder_effect` (D69):
  - "Did <name> help?" two hours after an intake;
  - none during an attack, and home's card after the attack ends;
  - answering first cancels it.
  Verify: passes twice

## 5. Guards

- [ ] 5.1 Log medication's "Taken at" date and time dialogs say "When did you take it?", from home and from attack mode.
  Verify: the flow passes 3 times
- [x] 5.2 `qa/resource_names.py` (design D5). Verify: it passes on d0e0214, and it fails on a local duplicate that is
  then reverted

## 6. Documents

- [x] 6.1 `docs/QA.md`: verification depth (D63), notifications and permissions (D64), the reboot, and the resource
  check. Verify: `grep -n "D63\|D64" docs/QA.md`

## 7. Verify (end of cycle, D63)

- [ ] 7.1 Full regression, all device scenarios, the audit, monkey, the permission audit, the resource check and
  `qa/e2e_coverage.py` (exit 0). Record the results, the coverage, and the gaps with reasons (D61) in a "Verification
  record"
- [ ] 7.2 Gate `./gradlew assembleDebug test ktlintCheck detekt` and `openspec validate
  add-stats-and-reminders-e2e-coverage --strict`
