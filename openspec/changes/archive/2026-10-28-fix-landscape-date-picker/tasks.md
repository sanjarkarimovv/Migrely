# Tasks

## 1. Test first

- [x] 1.1 Extract the date step's calendar into `DateStepCalendar` without changing it
- [x] 1.2 `DateStepCalendarTest`: in a box 250 dp high, every day of September 2026 can be scrolled into view, sits
  below the weekday letters and inside the box, and shows its full 40 dp height; a day and a year can be picked; it
  fails before the fix. The locale is fixed to en-US, so it does not depend on the device's (review finding).

## 2. Fix

- [x] 2.1 `DateStepCalendar`: the `DatePicker` scrolls vertically

## 3. Verify

- [x] 3.1 Gate: `assembleDebug test ktlintCheck detekt lint`
- [x] 3.2 `:designsystem:connectedDebugAndroidTest` on emulator-5570
- [x] 3.3 `openspec validate fix-landscape-date-picker --strict`
- [x] 3.4 On emulator-5570, in landscape:
  - "Add a past migraine" › Started: every day of the current and the previous month can be scrolled to and picked;
  - the year list opens, a year can be picked, and text input mode still works;
  - one more place (attack details or Log medication);
  - portrait at 100% looks as before, and portrait at 200% font shows every week;
  - the Doctor report's date range picker in landscape, at 100% and at 200% font, still works;
  - the calendar's scroll is exposed to accessibility in landscape (`scrollable`), and nothing scrolls in portrait.

## 4. Review

- [x] 4.1 Independent review (`superpowers:code-reviewer`)

## Verification record

- **Gate:** `assembleDebug test ktlintCheck detekt lint` passes.
- **Instrumented:** `:designsystem:connectedDebugAndroidTest` on emulator-5570 (API 36) is 40/40.
  - `DateStepCalendarTest` 3/3 passes with the fix.
  - With the `verticalScroll` line removed, all 3 fail:
    - September 1 is drawn over the weekday letters;
    - tapping it does not pick it;
    - picking 2025 in the year list does not change the year.
- **Emulator (emulator-5570, debug build of 3945909c), landscape:**
  - **Add a past migraine › Started:** the calendar is a ScrollView [727,63][1672,870].
    - October 1–3 sit below the weekday row. Scrolled down, October 25–31 sit above Cancel and Next (902).
    - Every cell is 126 px (48 dp).
    - September 1 was picked and, after the time step, shown as the start. September 30 scrolls fully into view.
  - **Year list:** 2025 was picked, and the calendar showed September 2025. No crash.
  - **Text input mode:** August 15 was typed and accepted. The dialog shrinks to fit, with no empty area.
  - **Log medication › Taken at:** the same ScrollView, and every week can be reached.
  - **Portrait at 100%:** nothing scrolls and the whole month fits, a six-week month (August 2026) included.
  - **Portrait at 200%:** every week can be reached by scrolling, and none is drawn over the weekday row or under the
    buttons.
  - **Landscape at 200%:** every week can be reached.
  - **Accessibility:**
    - in landscape, one `scrollable` node holds the calendar;
    - in portrait at 100%, there is none.
  - **Crashes:** 0 crashes and 0 ANRs.
- **Review findings fixed:**
  - the test's texts depended on the device's locale (Russian, Polish and Persian would fail falsely), so it now fixes en-US and takes its texts from Material's own formatter;
  - the test now checks the visible 40 dp height;
  - the year cell is scrolled into view before the tap;
  - the proposal names `medication-intakes`.
- **Found, not changed:**
  - **#102 (S3, new):** at 200% font the Doctor report's date range picker shows no calendar, in landscape and in portrait. It is Material's `DateRangePicker` header, not this dialog.
  - **Landscape scroll cue:** the date step opens at the top with weeks 1–2 visible, so the selected day can start out of view. The headline still shows the date.
  - **Keyboard in landscape:** with text input and the keyboard open, Cancel and Next sit under the keyboard until it is closed. This is standard dialog behaviour.
  - **Month arrows at 200%:** the ‹ arrow moves with the month name's length. This is Material's layout.
