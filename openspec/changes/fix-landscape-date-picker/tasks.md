# Tasks

## 1. Test first

- [x] 1.1 Extract the date step's calendar into `DateStepCalendar` without changing it
- [x] 1.2 `DateStepCalendarTest`: in a box 250 dp high, every day of September 2026 can be scrolled into view, sits
  below the weekday letters and inside the box, and keeps its 40 dp height; it fails before the fix

## 2. Fix

- [x] 2.1 `DateStepCalendar`: the `DatePicker` scrolls vertically

## 3. Verify

- [x] 3.1 Gate: `assembleDebug test ktlintCheck detekt lint`
- [x] 3.2 `:designsystem:connectedDebugAndroidTest` on emulator-5570
- [x] 3.3 `openspec validate fix-landscape-date-picker --strict`
- [ ] 3.4 On emulator-5570, in landscape:
  - "Add a past migraine" › Started: every day of the current and the previous month can be scrolled to and picked;
  - the year list opens, a year can be picked, and text input mode still works;
  - one more place (attack details or Log medication);
  - portrait at 100% looks as before, and portrait at 200% font shows every week;
  - the Doctor report's date range picker in landscape still works.

## 4. Review

- [ ] 4.1 Independent review (`superpowers:code-reviewer`)
