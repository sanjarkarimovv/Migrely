# Tasks

## 1. One formatting locale (D117)

- [x] 1.1 `FormattingLocaleResolver` in `:app`: English with the phone's region, the phone's first day kept; tests.
- [x] 1.2 `FormattingLocales`: `MainActivity`'s configuration through `attachBaseContext`, and the process default
  from `MigrelyApplication` at start and on configuration changes.
- [x] 1.3 The DI formatters read it from the application's configuration at every call, and composables from
  `LocalConfiguration`, never from the default, which Android resets; the CSV export is handed the phone's own locale.
- [x] 1.4 Locale audit of features, `:app`, `:designsystem` and the datasources.
- [x] 1.5 BRD D117, EC-L10N-03, EC-TIM-05; ARCHITECTURE.

## 2. A paywall that shows itself needs Play's products (D110)

- [x] 2.1 `SubscriptionRepository.billingAvailability()`: available only when billing connects and Play offers the
  products; the answer kept an hour, an unanswered one asked again; the repository one for the app; tests.
- [x] 2.2 BRD D110.

## 3. Verify

- [x] 3.1 Gate: `./gradlew assembleDebug test ktlintCheck detekt`, `openspec validate one-language-dates --strict`.
- [x] 3.2 Emulator check in ru-RU and en-US: home, History, the day sheet, the doctor report and its PDF, Insights, the
  attack time pickers and the reminder time.

## Verification record

**Gate:** green on each commit; 1595 unit tests (2102 runs with the release variant), none failing, 22 of them new
(13 for the resolver, 9 for the availability and its memory). `openspec validate one-language-dates --strict` passes.

**Emulator check** on emulator-5570 (API 36) with the debug build. `cmd locale` on this image has no
`set-system-locales`, so Russian was set as Migrely's own language (`cmd locale set-app-locales com.migrely.app
--locales ru-RU`, what Settings' app languages do), while the app was open; the system stayed en-US with the time
format left to the locale.

- **ru-RU:** home "Sunday, October 25" and "October. Opens History."; History "October 2026", initials M T W T F S S
  from Monday, "Thu, Oct 22, 21:00 – Fri 23:59"; Insights "September 2026", "October 2026"; Material's date picker in
  English from Monday and its time picker in 24 hours; the trial end "November 1, 2026"; the report period
  "Jul 26, 2026 to Oct 25, 2026"; the reminder "20:00". The PDF (pdftotext): weeks from Monday, 24-hour times,
  "26 Jul 2026", A4, no Cyrillic.
- **The default reset:** the first build set only the process default for the `:app/di` formatters; after the app was
  left for the launcher and opened again, home read "воскресенье, 25 октября". Android had set the default back without
  telling the application. With the locale read from the configuration, the same steps keep English.
- **en-US:** home and History weeks from Sunday, "9:00 PM to Fri 11:59 PM", the date picker from Sunday, the time
  picker with AM and PM, the reminder "8:00 PM", the PDF from Sunday with "9:00 PM" on Letter paper.
- Afterwards the app's language was cleared and its data restored to `/data/local/tmp/baseline12/` (dump md5
  c3cfd32487171b7886baf9725dce7594), and adb unrooted.

