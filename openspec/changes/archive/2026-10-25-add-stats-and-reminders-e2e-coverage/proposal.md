## Why

Dev changes 5 (`add-basic-stats`, PR #32), 5a (`add-overuse-indicator`, PR #34) and 6 (`add-reminders`, PR #35) were
merged without QA. Their 35 scenarios have no end-to-end check, and neither do their edge cases (EC-UX-14, EC-MED-06,
EC-REM-01…06):
- `month-stats`: 17 scenarios;
- `medication-overuse` › "A standing indicator in History": 6 scenarios;
- `reminders`: 12 scenarios.

Notifications are a new kind of behaviour for QA. They are driven by WorkManager and by a runtime permission, and no QA
tool reaches them yet. Change 7 (`add-data-export-import`) replaced home's bell with a "Settings" gear, so the
keyboard check is stale.

## What Changes

- **Baseline.** Merge develop d0e0214 (changes 5–7 and the #36 fix) and run the full suite. Fix the checks that
  changes 5–7 made stale, such as the first Tab stop, which is now "Settings".
- **Month summary flows** (STA-F*, D53–D58):
  - a past month and the current month ("October so far");
  - nothing logged yet (EC-HIS-01);
  - an attack from the month before, and an attack over two months (Migraines started against Migraine days);
  - a pain-free mark under an attack;
  - preventive medication not counted;
  - the average peak rounded to a level, and "no pain level logged";
  - the chronic card at 15 migraine days and not at 14.
- **Progress message** (EC-UX-14, D55):
  - better, which is green, against worse and the same, which are grey;
  - "this time last month" against the whole month;
  - too few entries (9 against 10);
  - on home in normal mode, and none in attack mode.
- **Overuse indicator** (EC-MED-06, D59):
  - below the limit, at the limit (amber, ", at the limit", the doctor line);
  - two categories and "All attack medication";
  - after an undo;
  - a past month has none;
  - none without attack medication;
  - the day-30 boundary.
- **Reminders** (REM-F*, EC-REM-01…06, D65–D69):
  - the Reminders screen through Settings, and none in attack mode;
  - the permission asked only when a switch is turned on;
  - after a refusal, "Open settings", and the 30-day re-ask;
  - the daily reminder on a calm day; none when already answered, when the day is marked pain-free, or on an attack
    day;
  - travel and zone re-planning; a reboot;
  - the notification's "Pain-free day" action, including on an attack day ("A migraine is logged today.");
  - "Log a migraine";
  - "Did it help?" two hours after an intake, none during an attack, and cancelled when answered first.
- **Picker guard.** Log medication's "Taken at" date and time dialogs say "When did you take it?". The guard covers
  both entry points, home and attack mode.
- **Duplicate-resource check.** A static check finds string resource names used in more than one module. That is the
  cause of the picker-title bug class.
- **BRD:** §13 row 6b, D63 (verification depth) and D64 (notifications black-box).

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `quality-gates`:
  - how deep each QA change and each QA cycle verifies (ADDED, D63);
  - notifications checked black-box (ADDED, D64).

## Impact

- **Files:**
  - `.maestro/`: new `month-stats/`, `reminders/` and `medication-overuse/` folders, plus `device/` and `subflows/`;
  - `qa/device_scenarios.sh`;
  - a new `qa/resource_names.py`;
  - `docs/QA.md`, `docs/BRD.md`.
- **BRD IDs:** STA-F*, EC-UX-14, EC-HIS-01, EC-MED-06, REM-F*, EC-REM-01…06, D53–D59, D65–D69, D63, D64.

**Out of scope:**
- Product code. Defects are filed for the dev session.
- Change 7 (`data-backup`), which gets its own QA change.
- Change 8 (onboarding), which is not merged yet.
