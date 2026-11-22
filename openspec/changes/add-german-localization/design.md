# Design

BRD D195, EC-L10N-01 to EC-L10N-24, section 13 row 59. Base: `origin/develop` 224dad3d. Release 1.4.0, after #255 and
before T72 (`feature/add-word-of-mouth-loops`).

## The person

A German-speaking person in a migraine, in a dark room, opens Migrely with one hand. Today every word is English. After
this change the same three taps say "Migräne hat begonnen" or its glossary equivalent, the warning signs say "Notruf", and
nothing else about the app changes: no new screen, no new setting, no new tap. The language comes from the phone, or on
Android 13 and newer from the system's own "App language" page, so the app needs no picker.

## D1. One language rule (`:app`, `locale/`)

Today `FormattingLocaleResolver.resolve(phoneLocale: Locale)` reads only `locales[0]` and turns any non-English language
into English with the phone's country (D117), and `MainActivity.attachBaseContext` forces that locale onto the activity,
so a `values-de` folder alone would never show.

New rule, still in one place:

1. Walk the configuration's whole `LocaleList` and take the first locale whose language is in `APP_LANGUAGES = {en, de}`.
2. If it is German, the formatting locale is that locale (`de-DE`, `de-AT`, `de-CH`), with the phone's first day of the
   week (from `locales[0]`, as today) carried as the `fw` keyword when it differs.
3. Otherwise call today's rule on `locales[0]` unchanged. So `[ru-RU, en-US, de-DE]` stays `en-RU`, as in 1.3.3: no one
   without German ahead of English sees any change.

| Phone list | App language | Formatting locale |
|---|---|---|
| de-DE | German | de-DE |
| de-AT / de-CH | German | de-AT / de-CH |
| ru-RU, de-DE | German | de-DE (Monday, same as ru-RU) |
| ru-RU, en-US, de-DE | English | en-RU (unchanged) |
| en-DE | English | en-DE (unchanged) |
| ru-RU | English | en-RU (unchanged) |
| en-US (per-app) on a de-DE phone, API 33+ | English | en-US |
| de-DE (per-app) on an en-US phone, API 33+ | German | de-DE |

`FormattingLocales.of(configuration)` passes `configuration.locales`. `activityOverride`, `applyToProcess` and the DI
formatters keep reading `of(...)`, so nothing else in the features changes.

Why not let Android resolve the list: for `[ru, en, de]` Android's resource matching picks `values-de` (the default
`values/` is not tagged English), while our formatting would say English. One rule for words and dates avoids a German
screen with English dates or the reverse.

## D2. Strings read outside the activity

The application context keeps the phone's configuration, so for `[ru, en, de]` it would resolve `values-de` while screens
show English. Add `FormattingLocales.localizedContext(context): Context` =
`context.createConfigurationContext(activityOverride(context.resources.configuration))`. Every string read outside an
activity uses it: `NotificationDataSourceImpl` (titles, texts, channel names and description) and anything else the
engineer finds with `git grep -n 'getString\|getQuantityString' -- app/src/main datasource/*/src/main` outside UI code.

Channel names: `NotificationDataSource` gets `refreshChannelNames()`, which re-registers only the channels that already
exist (`manager.notificationChannelsCompat`) with the localized name and description; Android keeps the user's importance.
`MigrelyApplication` calls it in `onCreate` and in `onConfigurationChanged` when the locale list changed. No channel is
created there (EC-L10N-07).

## D3. Android 13+ app language

- `app/src/main/res/xml/locales_config.xml` with `en` and `de`; `android:localeConfig="@xml/locales_config"` on
  `<application>`. Explicit file, not AGP's `generateLocaleConfig`, so the list is reviewable and matches `APP_LANGUAGES`.
- `MainActivity` is a `ComponentActivity`; on API 33+ the system applies the per-app locale to the app's configuration,
  `attachBaseContext` receives it, and D1 picks it up. No AppCompat, no `AppLocalesMetadataHolderService`.
- API 26 to 32: follow the phone. No in-app picker (out of scope; it needs AppCompat or our own storage and a new screen).
- A unit test reads `locales_config.xml` and asserts its languages equal `APP_LANGUAGES` and the set of `values-<lang>`
  folders plus `en`, so a future `values-fr` without config, or config without strings, fails.

## D4. What stays English

- **PDF doctor report** (`features/report/data` `ReportDocumentMapper`, `ReportRecordsDomainMapper`, `datasource`
  `ReportFitting`/`ReportPageLayout`): about 55 fixed strings and `Locale.ENGLISH` date patterns. Unchanged in this change.
  Follow-up `add-german-doctor-report`: move them behind a strings port provided by `:app`, re-fit columns for German.
  The report screen (`features/report/ui`) shows `report_pdf_language_note` when
  `LocalConfiguration.current.locales[0].language != "en"` (UI-only fact, no presentation change). English text:
  "The PDF is in English." German per glossary.
- **CSV** (`AttacksCsvMapper`): English column names and values, permanently (D72, data portability). The separator
  moves from the application context's locale to the system's: `BackupModule` `locale = {
  Resources.getSystem().configuration.locales[0] }`. Below API 33 this is the same value as today.
- **Backup JSON**: untouched. Built-in tags are stored by key and named by the UI, so they follow the language already.
- **Debug strings** (`app/src/debug`): `translatable="false"`. `app_name` "Migrely": `translatable="false"`.

## D5. Translation, glossary and voice

Who: `android-engineer` drafts every `values-de/strings.xml` from this glossary; `ux-designer` owns the German copy and may
rewrite any string (findings back to the engineer); `medical-reviewer` passes every health and safety key;
`legal-finance-analyst` checks subscription and privacy keys. No external translation service: strings are public, but
the team does not add a vendor for this.

Voice: informal "du", short sentences, verbs first on buttons, as in English. Reason: shorter strings at 200% and the
app's direct English tone. `ux-designer` may switch to "Sie" with evidence before the copy review closes; the switch is
then total, never mixed.

Typography: „…“ quotes, "…" ellipsis character, a no-break space between a number and its unit where English has none
only if the line already wraps; decimals come from the locale, never typed into a string.

Glossary (binding; a change needs `ux-designer`, and for health terms `medical-reviewer`):

| English (app) | German | Note |
|---|---|---|
| migraine (the condition) | Migräne | |
| migraine / attack (one episode, countable) | Migräneattacke; short: Attacke | DMKG guideline term "Migräneattacke"; plural "Attacken". Not "Anfall" |
| Migraine started (button) | Migräne beginnt | Short, present tense; `ux-designer` confirms at 64 dp |
| Migraine ended | Migräne vorbei | |
| migraine day(s) | Migränetag(e) | |
| headache | Kopfschmerz(en) | |
| pain level, peak pain | Schmerzstärke, stärkster Schmerz | |
| mild / moderate / severe | leicht / mittel / stark | |
| trigger | Auslöser | Never implies cause beyond the English (D174) |
| symptom | Symptom | |
| aura | Aura | Same as English: on the identical-value list |
| nausea | Übelkeit | |
| sensitivity to light / sound / smell | lichtempfindlich / lärmempfindlich / geruchsempfindlich | Adjectives keep words short; nouns ("Lichtempfindlichkeit") only where the layout was seen |
| attack medication, acute medication | Akutmedikation; "Mittel gegen Attacken" where a plain phrase reads better | One term per screen |
| days with attack medication | Tage mit Akutmedikation | |
| preventive | vorbeugend | Not "prophylaktisch" in UI |
| simple painkiller | einfaches Schmerzmittel | |
| triptan | Triptan | Identical-value list |
| combination or other | Kombipräparat oder anderes | |
| medication overuse headache | Kopfschmerz durch Übergebrauch von Schmerz- oder Migränemitteln | `medical-reviewer` fixes the final wording; no abbreviation in UI |
| Did it help? | Hat es geholfen? | Answers: Gar nicht / Etwas / Deutlich / Vollständig, `medical-reviewer` checks the scale |
| impact: No impact / Less active / Had to stop / Bed rest | Keine Einschränkung / Weniger aktiv / Musste aufhören / Bettruhe | |
| Warning signs | Warnzeichen | |
| your local emergency number | den Notruf | No number (D95, D171): users are in many countries |
| urgent medical advice | dringend ärztlichen Rat | Not "heute" (D171) |
| doctor report | Arztbericht | |
| doctor visit | Arzttermin | |
| diary | Tagebuch | |
| reminder | Erinnerung | |
| notification | Benachrichtigung | 18 letters: long-word list after it is seen in place |
| History | Verlauf | |
| Insights | Auswertung | Not "Erkenntnisse" (claim) |
| period (menstrual) | Periode | |
| Your data | Deine Daten | |
| backup / restore / export | Sicherung / wiederherstellen / exportieren | |
| Premium, Free | Premium, Kostenlos | "Premium" identical-value list |
| Undo | Rückgängig | |
| Save / Done / Cancel / Delete | Speichern / Fertig / Abbrechen / Löschen | |
| Not a diagnosis | Keine Diagnose | |

## D6. Checks that can fail

`app/src/test/.../l10n/LocalizationCompletenessTest.kt` (JVM, part of `test`):

1. Every `*/src/main/res/values/strings.xml` in the repo has a sibling `values-de/strings.xml`.
2. The sets of `string`, `plurals` and `string-array` names match, after dropping `translatable="false"`.
3. Each value's placeholders (`%1$d`, `%2$s`, `%%`) match as a multiset. For plurals, German `other` matches English
   `other`, and German `one` uses no placeholder that English `other` lacks (it may drop the number). Arrays have the
   same item count.
4. A German value equal to its English one fails unless it is in `app/src/test/resources/l10n/same-as-english.txt`.
5. A German word over 16 letters fails unless it is in `app/src/test/resources/l10n/long-german-words.txt`, each line with
   the screen where it was seen at 200% on 360 dp.
6. `locales_config.xml` languages equal `APP_LANGUAGES` and the `values-<lang>` folders plus `en` (D3).

Android Lint (already in the gate, default severity Error): `MissingTranslation`, `ExtraTranslation`,
`StringFormatMatches`, `StringFormatCount`, `MissingQuantity`. Lint does not see a module with no translation at all,
hence check 1.

Negative controls (each shown red, then reverted): remove one German string; change `%1$d` to `%1$s`; copy an English
value; add a 17-letter word; delete one module's `values-de`; add an empty `values-fr`.

`FormattingLocaleResolverTest`: every row of the D1 table, plus the regional first-day keyword in German.
`FormattingLocales` Robolectric test: `localizedContext` with `[ru-RU, en-US, de-DE]` reads English and with
`[ru-RU, de-DE]` German, from a real `values-de` string.

No Robolectric or Roborazzi screenshots per screen: D42 still holds (build change and golden images). Layout is checked on
devices (D7).

## D7. Layout and accessibility checks (`ux-designer` defines, engineer and QA run)

Matrix: German, 360 x 640 dp portrait and 640 x 360 dp landscape, font 200%, plus 100% at 411 dp as a sanity pass.
Per state: no word broken inside, no hyphen, no ellipsis, no clipped text, buttons and chips keep 48 dp (64 dp in attack
mode) and whole words, the layout stacks where it must (D181), TalkBack names in German.

Engineer's walk (5570, before review): home calm and with an attack, attack mode with its message, Attack details top and
bottom, the custom tag dialog, Log medication (normal and Dark room), My medications, Edit medication, History month and a
day, Insights, the paywall, the doctor report period screen with the new line, Settings, Warning signs, About, Privacy,
Reminders, Your data, Delete all data, onboarding pages, the overuse card, "Still going?" at 72 hours.

QA's a11y tour (5580) can reuse the English selectors: drive each state in English, then
`adb shell cmd locale set-app-locales com.migrely.app --locales de-DE`, capture with `qa/a11y_audit.py`, and set it back.
This also exercises EC-L10N-06 on every state. API 26 has no per-app locale: a short walk with the system set to de-DE.

## D8. Medical and legal review packets

The engineer writes `migrely-work/de/review-medical.tsv` and `migrely-work/de/review-legal.tsv` (key, file, English,
German), outside the repo. Medical keys: `safety_*`, `long_check_*`, `settings_warning_signs_*`, `settings_about_*`,
overuse card strings, medication kinds and categories, "Did it help?" answers, every `insights_*` sentence, onboarding and
privacy health lines, "Not a diagnosis" lines. `medical-reviewer` adds any key it finds and returns PASS or FAIL per key
with the English source and citation (NHS migraine page, D171, D173, D174). Legal keys: `subscription_*`, paywall,
privacy and data lines. The release is No-Go while any medical key lacks a PASS.

## D9. Merge order and other branches

- #255 (`bugfix/255-details-talkback-order`) adds no strings: no conflict beyond the BRD.
- T72 (`feature/add-word-of-mouth-loops`) adds 15 strings in `report/ui` and `settings/ui` and takes D185 to D188, row 52.
  After this change merges, T72's gate fails until it adds their German values: intended (D195 (9)). If T72 merges first,
  this change translates those strings before its own merge.
- D195 and row 59 were free on every remote branch on 2026-11-22 (scan of all `refs/remotes/origin/*`).

## Risks

- Translation quality without a native speaker: mitigated by the glossary, `ux-designer`'s copy review and
  `medical-reviewer`'s per-key check; residual risk recorded (D195 (8)). German Play reviews go to `support-agent`.
- Every later change pays for German strings. Accepted: the gate makes it visible instead of shipping English in German.
- Per-app German on an English phone may be read by an English TTS voice (EC-L10N-17): recorded limit.
- Long German words at 200%: the long-word list forces a look at each one; the device walk is the real check.
