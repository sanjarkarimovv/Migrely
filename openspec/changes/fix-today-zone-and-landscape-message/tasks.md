# Tasks

## 1. Domain (#19, D36, design D1–D2)

- [ ] 1.1 `GetTodayRepository.today(from, to)`; `TodaySnapshotDomainModel` with `endedAttacks` and no clock;
  `GetTodayUseCase` reads `GetCurrentTimeRepository`, asks for today ± 2 days in the device zone and keeps the attacks
  whose start-zone dates include today's device date. Tests: flying east, flying west, another zone on the same date,
  across midnight, ending exactly at midnight, the window passed to the repository, the 72-hour check on the use
  case's clock, failure.
  Verify: `./gradlew :features:home:domain:test`

## 2. Data

- [ ] 2.1 `HomeRepository.today(from, to)` reads the window it is given; tests updated.
  Verify: `./gradlew :features:home:data:test`

## 3. App wiring

- [ ] 3.1 `HomeModule` passes `GetCurrentTimeRepository` to `GetTodayUseCase`.
  Verify: `./gradlew :app:assembleDebug`

## 4. UI

- [ ] 4.1 Home `TimeUiFormatter` takes `withCity`; a "Logged today" row names the city once, after the end (D3).
  Tests in `TimeUiFormatterTest`.
  Verify: `./gradlew :features:home:ui:test`
- [ ] 4.2 History `HistoryTimeUiFormatter` takes `withCity`; an ended row names the city once, an attack in progress
  keeps it on its start (D3). Tests in `HistoryTimeUiFormatterTest`.
  Verify: `./gradlew :features:history:ui:test`
- [ ] 4.3 `HomeContents` keeps messages over the left column in landscape attack mode (`messagesBesideControls`, D4),
  with a unit test.
  Verify: `./gradlew :features:home:ui:test`

## 5. Docs

- [ ] 5.1 BRD 9.9 EC-UX-20, 13 row 2c, 17 D36–D38 (done with the plan).
  Verify: `grep -n "D36\|D37\|D38\|EC-UX-20\| 2c " docs/BRD.md`

## 6. Verify

- [ ] 6.1 Gate: `./gradlew assembleDebug test ktlintCheck detekt`.
- [ ] 6.2 Emulator walk on emulator-5570: travel east and west with the zone and clock set by adb (restored after);
  the city once on home and in History; landscape attack mode with the message at 100% and 200% font and with a
  30 s "Time to take action"; rotation while the message shows; landscape normal mode; portrait unchanged; TalkBack
  labels with `uiautomator dump`. Record the results below.
- [ ] 6.3 Independent review of `git diff develop...feature/fix-today-zone-and-landscape-message`; fix and record
  the findings.
