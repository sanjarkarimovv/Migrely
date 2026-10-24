# Tasks

## 1. UI (`:features:home:ui`, `:features:history:ui`, `:designsystem`)

- [ ] 1.1 Add the mode switch tap guard to `HomeContents` (design D1) with a unit test for `guardsTaps`; verify `./gradlew :features:home:ui:test`
- [ ] 1.2 Use the full attack-mode text colour for the start-time line (D3); verify `./gradlew :features:home:ui:assembleDebug`
- [ ] 1.3 Key `rememberNowMillis` on the attack's start (D4); verify `./gradlew :features:home:ui:assembleDebug`
- [ ] 1.4 Merge semantics instead of clearing them (D2) in `AttackSummaryRow`, `MonthSkyCalendar`'s day cell, the History rows and the month card title, and extend the calendar Compose test to require focus semantics; verify `ANDROID_SERIAL=emulator-5570 ./gradlew :designsystem:connectedDebugAndroidTest`

## 2. Docs

- [x] 2.1 BRD EC-UX-18, EC-UX-19, D32 and row 2a; verify the BRD reads consistently

## 3. Verify

- [ ] 3.1 Run the gate `./gradlew assembleDebug test ktlintCheck detekt` and the designsystem instrumented tests; verify all pass
- [ ] 3.2 Walk on `emulator-5570`, restoring every setting afterwards:
  - the QA repros for #12 (both cases, at y = 2100 px, with gaps of 0.15 to 0.7 s);
  - the #13 colour;
  - #14 through `uiautomator dump` (`focusable="true"`) and Tab with Enter;
  - #15 with "1 h".

  Record the results here
- [ ] 3.3 Run an independent review of `git diff develop...feature/fix-home-qa-findings`, fix the real findings and validate strictly
