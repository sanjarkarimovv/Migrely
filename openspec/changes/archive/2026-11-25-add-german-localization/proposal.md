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
  system's language rather than Migrely's per-app language; the shared message's text and subject until the de-DE
  listing (T79) is approved, while the chooser and rows are German (D195 (20) (a)); the backup format, untouched.
- **A gate that fails on untranslated text (D195 (9)):** Android Lint's translation checks plus a JVM test for missing
  modules, placeholders, arrays, copied English and over-long German words, with negative controls.
- **Reviews (D195 (8)):** `ux-designer` owns and reviews the German copy and defines the 200% / 360 dp / landscape /
  TalkBack checks; `medical-reviewer` passes every health and safety key (blocks release); `legal-finance-analyst`
  checks subscription and privacy keys. A German-speaking clinician (T31) and a native linguist are recorded limits.
- **Notifications and the launcher (D195 (15), (16)):** the text notifications expand to their full text
  (`BigTextStyle`); the visit notification's date follows the app's formatting locale, a visible fix for English users on
  non-English phones (D117 already named notifications). The static launcher shortcut label is resolved by Android, not
  by our rule: on `[tr-TR, de-DE]` or `[ru-RU, de-DE]` it can stay English beside a German app (known limit; a
  `values-en` attempt was removed after code review because it changed nothing).
- **Layout fixes German needs:** where a layout passes in English and fails in German at the same settings (predicted:
  the onboarding header, the date pickers' top row, two-line top bar titles), it is fixed in this change, shorter copy
  first, never smaller type. Failures English shares become issues, except on safety paths.
- **Layout fixes from the walk that English sees too (D195 (18), design D10; Refs #276):** the range picker's headline
  keeps each side whole and is said once by TalkBack (4 new report strings in English and German); a full-screen
  picker's title moves under its buttons when a word would not fit; the pain, effect and custom tag dialogs grow wide in
  a short window, scroll their title with their content and show an edge fade and divider while more lies beyond; the
  custom tag dialog narrows only for an intent to type (team-lead rule of 2026-11-23). This also fixes #276, the pain
  and effect dialogs at 200% in landscape, in English and German.
- **The long-word gate counts a word as Android breaks it (design D6 check 5):** composed (NFC), a hyphenated compound
  whole, U+2011 and a no-break space between letters as joiners, a placeholder as letters of its written length.
- **Records:** BRD D195 (with (15) to (18) after the build, the walk and the reviews), EC-L10N-01 (updated), EC-L10N-04
  to EC-L10N-30 (new), a pointer in D117, section 13 row 59.

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
  report screen's one line, the German-only layout fixes and the shared picker and dialog fixes above (design D10).
- Other dialogs and pickers: only the pain, effect and custom tag dialogs take the short-window rules, and only the
  full-screen date and date-range pickers take the stacked title. Material's date-time picker and Reminders' time dialog
  are unchanged (#269 stays open).
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
- `app-shell`: ADDED "A full-screen picker's title moves under its buttons when a word would not fit", "A dialog in a
  short window keeps its title and answers in reach" and "A scrolling dialog shows that more lies beyond".
- `doctor-report`: ADDED "The report screen names the PDF's language" and "The custom range's headline stays whole and
  is said once".
- `custom-tags`: ADDED "The custom tag dialog narrows only to type".
- `data-backup`: ADDED "Exports stay language-neutral" (CSV words and separator, backup across languages).

## Impact

- **Code:** `:app` `locale/FormattingLocaleResolver.kt`, `locale/FormattingLocales.kt` (`localizedContext`),
  `MigrelyApplication.kt` (channel refresh), `datasource/notification/NotificationDataSourceImpl.kt`, `di/BackupModule.kt`
  and `locale/SystemLocale.kt` (system language for the CSV separator: `LocaleManager.getSystemLocales()` on API 33+,
  the system configuration below), `res/xml/locales_config.xml`, `AndroidManifest.xml` (`android:localeConfig`),
  `res/values/strings.xml` (`app_name` not translatable), `src/debug/res/values/strings.xml` (not
  translatable);
  `values-de/strings.xml` in `app` and the 11 feature `ui` modules; `features/report/ui` (one line and its string, and
  the range headline's 4 strings). Design system: `picker/RangeHeadline`, `picker/PickerTopRow`,
  `dialog/ScrollingDialogContent`, `dialog/MigrelyDialogs` (`wide`), `layout/CompactHeight` (`isCompactHeight`, shared
  with Home and History); `features/attack/ui` pain, effect and custom tag dialogs (`KeyboardWidthRule`).
- **Tests:** `FormattingLocaleResolverTest`, a `FormattingLocales` Robolectric test, `SystemLocaleTest`,
  `NotificationExpandedTextTest`, `NotificationLockScreenGermanTest`, `LocalizationCompletenessTest` with two reviewed
  lists in `app/src/test/resources/l10n/`, `WordsOverTest`; `RangeHeadlineTest`, `PickerTopRowTest`,
  `PickerTopRowTalkBackTest`, `ScrollingDialogContentTest`, `EdgeCuesTest`, `KeyboardWidthRuleTest`,
  `CustomTagTypingIntentTest`, `CustomTagDialogWidthTest`, `AttackDialogWidthTest`.
- **No change to:** permissions (proved by `:app:checkReleasePermissions`), INTERNET (none), Room schema, backup, Free and
  Premium, navigation, domain and presentation layers.
- **Issues:** Refs #276 (fixed here, closed by its reporter after the retest).
- **Process:** every later change that adds a string must add its German value (the gate enforces it). T72 adds 15.
- **BRD:** D195, EC-L10N-01, EC-L10N-04 to EC-L10N-30, D117 pointer, section 13 row 59.
- **Owner steps (after the app):** upload of the 1.4.0 AAB, the de-DE listing translation and German screenshots in Play
  Console, and the later site deploy. No money, no account.
