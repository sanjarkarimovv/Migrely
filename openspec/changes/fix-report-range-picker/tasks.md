# Tasks

## 1. Decision

- [x] 1.1 BRD D127: the custom range picker is full screen, and Done needs an end

## 2. Test first

- [x] 2.1 `DateRangePickerScreenTest` (designsystem, instrumented):
  - in a landscape-sized box at 200% font, at least two weeks of the month can be seen at their full height; it fails
    with Material's default title and headline;
  - two days and Done return the range;
  - the same day twice and Done return one day;
  - Done is off until the range has an end;
  - Cancel dismisses;
  - the whole picker keeps the range given, and a day after the last date is off.

## 3. Implement

- [x] 3.1 `MigrelyDateRangePicker` and `DateRangePickerScreen` in `:designsystem`
- [x] 3.2 Doctor report: `RangeDialog` uses it; the rules for the range stay in the feature

## 4. Verify

- [x] 4.1 Gate: `assembleDebug test ktlintCheck detekt lint`
- [x] 4.2 `:designsystem:connectedDebugAndroidTest` on emulator-5570
- [x] 4.3 `openspec validate fix-report-range-picker --strict`
- [x] 4.4 On emulator-5570, Doctor report › Custom:
  - portrait and landscape at 100% and at 200%: the weeks show, a range is picked, and the screen shows it;
  - the range chosen before is preselected;
  - text input mode;
  - Back and Cancel close it without changing the period;
  - TalkBack reads the headline (the "Start date" and "End date" description).

## 5. Review

- [x] 5.1 Independent review (`superpowers:code-reviewer`)

## Verification record

- **Gate:** `assembleDebug test ktlintCheck detekt lint` passes.
- **Instrumented:** `:designsystem:connectedDebugAndroidTest` on emulator-5570 (API 36) is 48/48.
  - `DateRangePickerScreenTest` is 8/8.
  - With Material's own title and default headline, the landscape 200% test sees one week in full instead of at least two.
  - `doneIsOffUntilAnEndIsChosen` failed before Done needed an end.
- **Emulator walks (emulator-5570, debug):**
  - **Portrait 100%:**
    - the picker is full screen, with the top row just below the status bar;
    - a range is picked and shown on the report, and is preselected when Custom is opened again;
    - Cancel and Back change nothing;
    - days after today are off;
    - Done is off on a start alone, and the same day twice gives one day.
  - **Landscape 100%:** 3 full weeks show, where the old dialog showed about 2.5.
  - **Landscape 200% (the bug case):** 2 full weeks show, plus most of a third, and a range is picked.
  - **Portrait 200%:** 7 full weeks show.
  - **Text input mode:**
    - a changed start, or an end that is not allowed, turns Done off;
    - the window resizes for the keyboard (`adjust=resize`), so Cancel and Done stay on screen;
    - over the keyboard in landscape the header hides, and it comes back when the keyboard closes;
    - in portrait it stays.
  - **Accessibility:** the headline's "Start date: …, End date: …" description is there, and the day cells are clickable.
  - **Crashes:** 0.
- **Review findings fixed:**
  - the first commit named the wrong cause (Material's range headline is already `titleLarge`, and the header's minimum is 68 dp), so the no-op `ProvideTextStyle` was removed;
  - the window gets a pane title;
  - `SelectableDates` is remembered;
  - the tests count the 48 dp touch height, fix the locale through the configuration and cover the whole picker.
- **Walk findings fixed:**
  - Done needs an end, as in Material's sample;
  - the window resizes for the keyboard instead of panning;
  - the header hides over the keyboard.
- **Found, not changed (follow-up issue):**
  - **Landscape 200%, keyboard open:** about 111 dp is left for a 109 dp field, so in text input mode the typed value is cut while the keyboard is up.
  - **Portrait 200%:** Material's headline changes height with the selection, so the months move between taps, and the end date breaks mid-word.
  - **One-day range:** the report shows it as "Oct 20, 2026 to Oct 20, 2026".
