# Tasks

## 1. Calendar keyboard (design D1)

- [ ] 1.1 `MonthSkyCalendar`: Tab leaves the grid, entering focuses `focusAnchorDay`; Compose instrumented tests.
  Verify: `ANDROID_SERIAL=emulator-5570 ./gradlew :designsystem:connectedDebugAndroidTest`
- [ ] 1.2 History and the month card pass the anchor day.
  Verify: `./gradlew :features:history:ui:assembleDebug`

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
