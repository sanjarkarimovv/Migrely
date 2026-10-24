## Why

Changes 4 (`add-day-markers`, PR #28) and 4a (`add-calendar-keyboard-grid`, PR #30) were merged without QA:
- `day-markers` has 0 of its 13 scenarios covered end to end;
- `month-sky` has 2 of 28;
- the fixes for #22 (the month grid as one Tab stop with arrows, D38) and #26 (the landscape message band, D52) are not
  retested;
- the audit's keyboard rule assumes one Tab stop per day, which D38 replaced.

## What Changes

- **Baseline.** Merge develop (changes 4, 4a and 5) and fix the flows that changes 4a and 5 made stale. Their layout
  changed: one Tab stop for the month, History's month summary card, and home's progress line.
- **Retest of #22** with real key events on home and History (D62): Tab, Shift+Tab, the arrows (±1 and ±7 days),
  stopping at the edge of the selectable days, and Enter.
- **Retest of #26** in landscape attack mode:
  - the guidance starts below the message and moves back when it goes;
  - the same holds with Reduce motion (animator scale 0);
  - `landscape-message-clear` turns its "Partly checks" line into a Covers line.
- **Day-marker flows** (DAY-F1, DAY-F2, EC-DAY-01, 02, 03 and 05):
  - Period and Pain-free on a chosen day, removing them, and a future day;
  - an attack day disables "Pain-free day";
  - an attack added later on a pain-free day, then deleted;
  - the home "Period" chip, and no chip in attack mode;
  - the spoken labels.
- **Month-sky flows:** the pain-free and period cells, the legend's 6 entries, and History's earliest month when the first
  mark comes before the first attack (D51).
- **Device scenario** `mark_travel` (EC-DAY-04): a period day marked in Tashkent is still marked in Berlin and in Pago
  Pago.
- **Audit:** its keyboard rule keeps checking every day cell, which stays focusable for accessibility services. The
  Tab order is left to the key-event checks (D62).
- **Restored settings:** the device scenarios also save and restore the animator scale, the rotation and the app
  language.
- **BRD:** §13 row 4b and D62.

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `quality-gates`:
  - keyboard paths are checked with key events (ADDED, D62);
  - the audit's keyboard rule for one-stop groups, and the animator scale among the restored settings (MODIFIED);
  - a day-mark travel device scenario (MODIFIED, EC-DAY-04).

## Impact

- **Files:**
  - `.maestro/`: new `day-markers/` and `month-sky/` folders, plus changes in `in-app-messages/`, `attack-history/`
    and `device/`;
  - `qa/device_scenarios.sh` and `qa/e2e_coverage.py`;
  - `docs/QA.md` and `docs/BRD.md`.
- **BRD IDs:** DAY-F1, DAY-F2, EC-DAY-01…05, EC-UX-03, EC-UX-04, D38, D47–D52 and D62.

**Out of scope:**
- "A tap while the guidance moves" (D52). It cannot be timed black-box; a Compose UI test is suggested to the dev
  session.
- Product code. Defects are filed as issues for the dev session.
- Change 5 (`month-stats`: summary, chronic card, progress line). It gets its own QA change, `add-basic-stats-e2e-coverage`
  (row 5b). Here only the flows it made stale are fixed.
- The Android 8.0 smoke run and monkey. They are release checks, not change checks.
