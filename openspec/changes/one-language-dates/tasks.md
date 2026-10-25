# Tasks

## 1. One formatting locale (D117)

- [x] 1.1 `FormattingLocaleResolver` in `:app`: English with the phone's region, the phone's first day kept; tests.
- [x] 1.2 `FormattingLocaleApplier`: the process default from `MigrelyApplication` at start and on configuration
  changes, and `MainActivity`'s configuration through `attachBaseContext`.
- [x] 1.3 The DI formatters read the default; the CSV export is handed the phone's own locale.
- [x] 1.4 Locale audit of features, `:app`, `:designsystem` and the datasources.
- [x] 1.5 BRD D117, EC-L10N-03, EC-TIM-05; ARCHITECTURE.

## 2. Verify

- [ ] 2.1 Gate: `./gradlew assembleDebug test ktlintCheck detekt`, `openspec validate one-language-dates --strict`.
- [ ] 2.2 Emulator check in ru-RU and en-US: home, History, the day sheet, the doctor report and its PDF, Insights, the
  attack time pickers and the reminder time.
