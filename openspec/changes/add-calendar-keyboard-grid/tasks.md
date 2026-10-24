# Tasks

## 1. Calendar keyboard (design D1)

- [x] 1.1 `MonthSkyCalendar`: Tab leaves the grid, entering focuses the anchor day, a visible focus ring; Compose
  instrumented tests (`MonthSkyCalendarKeyboardTest`, 6 tests; 5 of them fail without the grid's handling, and the
  sixth was made to as well).
  Verify: `ANDROID_SERIAL=emulator-5570 ./gradlew :designsystem:connectedDebugAndroidTest`
- [x] 1.2 History and the month card pass the anchor day. Not needed: the anchor comes from `CalendarDay`'s own
  flags (design D1).

## 2. Landscape message (design D2)

- [ ] 2.1 `HomeContents` measures the message area; `SideBySideAttackLayout` moves the guidance below it.
  Verify: `./gradlew :features:home:ui:test`

## 3. Verify

- [ ] 3.1 Gate and designsystem instrumented tests.
- [ ] 3.2 Emulator check on emulator-5570:
  - a hardware keyboard on home and History: Tab in and out, Shift+Tab, the anchor, arrows, Enter;
  - landscape attack mode with "Migraine started" at 100% and 200%, and with "Reduce motion";
  - `uiautomator dump` still reports the days as focusable.
- [ ] 3.3 Independent review; fix and record.
