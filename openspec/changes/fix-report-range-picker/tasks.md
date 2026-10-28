# Tasks

## 1. Decision

- [x] 1.1 BRD D127: the custom range picker is full screen, with a smaller headline

## 2. Test first

- [x] 2.1 `DateRangePickerScreenTest` (designsystem, instrumented):
  - in a landscape-sized box at 200% font, at least two weeks of the month can be seen at their full height; it fails
    with Material's default title and headline;
  - two days and Done return the range;
  - a start alone and Done return one day;
  - Done is off with nothing chosen;
  - Cancel dismisses.

## 3. Implement

- [x] 3.1 `MigrelyDateRangePicker` and `DateRangePickerScreen` in `:designsystem`
- [x] 3.2 Doctor report: `RangeDialog` uses it; the rules for the range stay in the feature

## 4. Verify

- [x] 4.1 Gate: `assembleDebug test ktlintCheck detekt lint`
- [x] 4.2 `:designsystem:connectedDebugAndroidTest` on emulator-5570
- [x] 4.3 `openspec validate fix-report-range-picker --strict`
- [ ] 4.4 On emulator-5570, Doctor report › Custom:
  - portrait and landscape at 100% and at 200%: the weeks show, a range is picked, and the screen shows it;
  - the range chosen before is preselected;
  - text input mode;
  - Back and Cancel close it without changing the period;
  - TalkBack reads the headline (the "Start date" and "End date" description).

## 5. Review

- [ ] 5.1 Independent review (`superpowers:code-reviewer`)
