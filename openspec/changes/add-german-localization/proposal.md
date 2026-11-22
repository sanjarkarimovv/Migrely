# Proposal

## Why

**T23, release 1.4.0.** Germany is one of Migrely's five main revenue markets (BRD §1), and Stiftung Warentest rated none
of the 16 migraine apps it tested above "befriedigend". Migrely is English only today: `develop` 224dad3d has 13
`values/strings.xml` files (about 690 strings, 40 plurals, 4 arrays in 12 main modules, 15 debug strings) and no
`values-*` translation. On a German phone the activity is even forced to English (D117), so a German person in a migraine
reads every word in a second language. The owner approved German as part of 1.4.0 and wants speed: this change is sized
for about one day of engineering, and the larger PDF work is split out.

## What Changes

- **One language rule (D195 (1)).** The app's language is the first supported language (English or German) in the phone's
  list. German gives a German formatting locale; anything else keeps D117 exactly, so no English user sees a change.
  Screens, notifications, channel names and the default locale read the same rule.
- **Android 13+ app language (D195 (2)).** `locales_config` (en, de) and `android:localeConfig`, so the system's "App
  language" page offers English and Deutsch. Android 8 to 12 follow the phone; no in-app picker.
- **German strings in all 12 main modules (D195 (3)):** every string, plural, array, notification text, channel name,
  content description and shortcut label, written from one glossary (design D5), "du" voice. `app_name` and debug strings
  become `translatable="false"`.
- **German dates and numbers (D195 (7)):** month and weekday names, order and decimal comma from the German locale; first
  day and 12/24 hours from the phone, as D117.
- **What stays English:** the PDF doctor report in this release (the report screen says so in one German line; follow-up
  `add-german-doctor-report`); the CSV's column names and values permanently (D72), with its separator now read from the
  system's language rather than Migrely's per-app language; the backup format, untouched.
- **A gate that fails on untranslated text (D195 (9)):** Android Lint's translation checks plus a JVM test for missing
  modules, placeholders, arrays, copied English and over-long German words, with negative controls.
- **Reviews (D195 (8)):** `ux-designer` owns and reviews the German copy and defines the 200% / 360 dp / landscape /
  TalkBack checks; `medical-reviewer` passes every health and safety key (blocks release); `legal-finance-analyst`
  checks subscription and privacy keys. A German-speaking clinician (T31) and a native linguist are recorded limits.
- **Notifications and the launcher (D195 (15), (16)):** the text notifications expand to their full text
  (`BigTextStyle`); the visit notification's date follows the app's formatting locale, a visible fix for English users on
  non-English phones (D117 already named notifications); the two static shortcut labels also live in `values-en`, so the
  launcher matches the app for `[ru-RU, en-US, de-DE]`.
- **Layout fixes German needs:** where a layout passes in English and fails in German at the same settings (predicted:
  the onboarding header, the date pickers' top row, two-line top bar titles), it is fixed in this change, shorter copy
  first, never smaller type. Failures English shares become issues, except on safety paths.
- **Records:** BRD D195 (with (15) to (17) after the build and reviews), EC-L10N-01 (updated), EC-L10N-04 to EC-L10N-27
  (new), a pointer in D117, section 13 row 59.

## Out of scope

- Any language other than English and German; right-to-left layout; a Swiss (`de-CH`) variant without ß.
- An in-app language picker (Android 8 to 12 users follow the phone).
- The German PDF doctor report: change `add-german-doctor-report` (1.4.x).
- German CSV headers (English on purpose, D72).
- German TTS voice when Migrely alone is German on an English phone (recorded limit, follow-up).
- Robolectric or Roborazzi screenshots per screen (D42); layout is checked on devices.
- The store listing and the site page: separate steps after the app, owned by `aso-seo-specialist`; uploading,
  "Publish changes" and deploys are the owner's. The site's German page opens only after the German app is live (D148).
- Any change to data, Room schema, backup format, Free and Premium, permissions or navigation; screen changes beyond the
  report screen's one line and the German-only layout fixes above.
- Layout failures English shares at the same settings (pre-existing): filed as issues, unless on a safety path.
- Changing the "DELETE" keyword of Delete all data (stays ASCII in 1.4.0).

## Capabilities

### New Capabilities

- `app-language`: which language the app shows and how it is chosen, Android 13+ app language, language changes while
  open, notifications, the translation gate, German layout at 200%, German health, safety and subscription words, TalkBack,
  typed German names, and German outside the app.

### Modified Capabilities

- `app-shell`: RENAMED "Dates and times in English" to "Dates and times in the app's language" and MODIFIED it for German
  (D117 unchanged for every other language).
- `doctor-report`: ADDED "The report screen names the PDF's language".
- `data-backup`: ADDED "Exports stay language-neutral" (CSV words and separator, backup across languages).

## Impact

- **Code:** `:app` `locale/FormattingLocaleResolver.kt`, `locale/FormattingLocales.kt` (`localizedContext`),
  `MigrelyApplication.kt` (channel refresh), `datasource/notification/NotificationDataSourceImpl.kt`, `di/BackupModule.kt`
  (system locale for the CSV separator), `res/xml/locales_config.xml`, `AndroidManifest.xml` (`android:localeConfig`),
  `res/values/strings.xml` (`app_name` not translatable), `res/values-en/strings.xml` (two shortcut labels),
  `app/lint.xml`, `src/debug/res/values/strings.xml` (not translatable);
  `values-de/strings.xml` in `app` and the 11 feature `ui` modules; `features/report/ui` (one line and its string).
- **Tests:** `FormattingLocaleResolverTest`, a `FormattingLocales` Robolectric test, `LocalizationCompletenessTest` with
  two reviewed lists in `app/src/test/resources/l10n/`.
- **No change to:** permissions (proved by `:app:checkReleasePermissions`), INTERNET (none), Room schema, backup, Free and
  Premium, navigation, domain and presentation layers.
- **Process:** every later change that adds a string must add its German value (the gate enforces it). T72 adds 15.
- **BRD:** D195, EC-L10N-01, EC-L10N-04 to EC-L10N-27, D117 pointer, section 13 row 59.
- **Owner steps (after the app):** upload of the 1.4.0 AAB, the de-DE listing translation and German screenshots in Play
  Console, and the later site deploy. No money, no account.
