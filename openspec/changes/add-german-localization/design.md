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

As built (603c58b2, accepted): `FormattingLocaleResolver.resolve(phoneLocales: List<Locale>)` takes a Kotlin list, not
Android's `LocaleList`, so the resolver stays a plain JVM class and its tests need no Robolectric; `FormattingLocales`
converts the configuration's list.

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

As built (febaf0df, accepted, D195 (15)): the doctor-visit notification's date is now formatted with the app's
formatting locale from `localizedContext`, not with the phone's own `locales[0]`. Before, an English app on a Russian
phone titled the notification with a Russian date while every screen said English, against D117, which already names
notifications. Visible change for English users on non-English phones: the date becomes English with their region
(EC-L10N-26).

Expanded text (404c989b, lead decision on ux E6): the daily reminder, "Did it help?" and the visit reminder set
`BigTextStyle().bigText(text)`, so longer German, and any language at 200%, is readable in full when expanded. Each German
text stays short enough to carry its meaning collapsed. `NotificationExpandedTextTest` 3 of 3 red before, green after.

## D3. Android 13+ app language

- `app/src/main/res/xml/locales_config.xml` with `en` and `de`; `android:localeConfig="@xml/locales_config"` on
  `<application>`. Explicit file, not AGP's `generateLocaleConfig`, so the list is reviewable and matches `APP_LANGUAGES`.
- `MainActivity` is a `ComponentActivity`; on API 33+ the system applies the per-app locale to the app's configuration,
  `attachBaseContext` receives it, and D1 picks it up. No AppCompat, no `AppLocalesMetadataHolderService`.
- API 26 to 32: follow the phone. No in-app picker (out of scope; it needs AppCompat or our own storage and a new screen).
- A unit test reads `locales_config.xml` and asserts its languages equal `APP_LANGUAGES` and the set of `values-<lang>`
  folders plus `en`, so a future `values-fr` without config, or config without strings, fails.
- Launcher shortcut (e31ac30e, lead decision on ux E7): static shortcut labels are resolved by the launcher from the
  system's list, not by D1, and the default `values/` is not tagged English, so `[ru-RU, en-US, de-DE]` would show the
  German label beside an English app. `app/src/main/res/values-en/strings.xml` holds only the two shortcut labels
  (`shortcut_start_attack`, `shortcut_start_attack_long`), a test asserts they equal `values/`, and `app/lint.xml` ignores
  only "not translated in en" for `MissingTranslation`; every other language stays fully checked (EC-L10N-25). With
  per-app German on an English phone the launcher may still show English: recorded, observed in 8.2 and 12.1 (ux N04).

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
- **Licence and library names** (as built, accepted): `settings_licence_apache`, `settings_licences_apache`,
  `settings_licence_ofl`, `settings_licences_ofl` are `translatable="false"`. They are proper names of licences and
  libraries ("Apache License 2.0", "SIL Open Font License 1.1"), which are not translated. `stat_no_peak` ("—") was
  already not translatable.

## D5. Translation, glossary and voice

Who: `android-engineer` drafts every `values-de/strings.xml` from this glossary; `ux-designer` owns the German copy and may
rewrite any string (findings back to the engineer); `medical-reviewer` passes every health and safety key;
`legal-finance-analyst` checks subscription and privacy keys. No external translation service: strings are public, but
the team does not add a vendor for this.

Voice: informal "du", **confirmed by `ux-designer`** (`migrely-work/de/ux-checks-verdict.md`, voice section): of 18
German migraine apps in the German App Store, 13 use "du" (personal diaries), 4 use "Sie" (clinics and institutions).
Migrely is a private diary with no clinician's voice, and "du" is shorter at 200% ("Wähle" against "Wählen Sie"). Lowercase
"du" and "dein" in running text, never mixed with "Sie". Calm and adult: no slang, exclamation marks, emoji, diminutives
or cheerleading. Buttons may be infinitives ("Zeit ändern") with no pronoun.

Typography: „…“ quotes and the "…" character. A **no-break space (U+00A0) between a number and its unit** in every
duration, percent, "x/10" and price string ("1 Std. 20 Min.", "25 %", "7/10"), because these lines wrap at 200% (ux E4;
this replaces the earlier "only if the line already wraps"). **No soft hyphen (U+00AD) and no zero-width character** in
any string: they slip past the long-word check and TTS reads them oddly. Decimals come from the locale, never typed into a
string.

Glossary (binding; a change needs `ux-designer`, and for health terms `medical-reviewer`). The health terms come from
`medical-reviewer`'s pre-check (`migrely-work/de/medical-precheck.md`, 2026-11-22; sources: DGN/DMKG S1 guideline AWMF
030/131, DMKG Kopfschmerzkalender, gesundheitsinformation.de, Duden, BfArM). The pre-check passes no key; the per-key PASS
is task 10.3.

| English (app) | German | Note |
|---|---|---|
| migraine (the condition) | Migräne | |
| migraine / attack (one episode, countable) | Migräneattacke; short: Attacke | DMKG term. Never "Anfall": "Krampfanfall" is a warning sign and must stay unmistakable |
| Migraine started (button) | Migräne beginnt | `ux-designer` confirms at 64 dp |
| Migraine ended | Migräne vorbei | |
| migraine day(s) | Migränetag(e) | |
| headache | Kopfschmerz(en) | |
| headache diary | "Tagebuch für Kopfschmerzen" in app copy | `medical-reviewer` suggested "Kopfschmerztagebuch" (19 letters: long-word list or hyphenated). The engineer used the phrase to avoid a 19-letter word at 200%. Pending `medical-reviewer` (10.3) and `ux-designer` (10.1) |
| pain level, peak pain | Schmerzstärke, stärkster Schmerz | |
| mild / moderate / severe | **leicht / mäßig / stark** | Not "mittel": colloquial, and "Mittel" means a remedy (IQWiG "mäßige bis starke Kopfschmerzen") |
| pain-free | schmerzfrei | |
| trigger | Auslöser; section title "Mögliche Auslöser" | Insights keep co-occurrence wording ("Oft zusammen mit deiner Migräne eingetragen"), never a cause (D174) |
| symptom | Symptom | |
| aura | Aura | Identical-value list |
| nausea | Übelkeit | |
| sensitivity to light / sound / smell | lichtempfindlich / lärmempfindlich (or geräuschempfindlich, one only) / geruchsempfindlich | Adjectives keep words short |
| neck pain (`symptom_neck_pain`) | **Nackenschmerzen** | Never "Nackensteife": that is the meningitis sign |
| stiff neck (warning signs) | steifer Nacken | |
| meningitis | Hirnhautentzündung (Meningitis) | |
| seizure, fit | Krampfanfall | |
| attack medication, acute medication | **Akutmedikation** (uncountable) / **Akutmedikamente** (plural) | Never "Mittel gegen Attacken": "Mittel gegen X" implies the medicine works (D174) |
| kind: acute / preventive | Bei Attacken / Vorbeugend | Not "prophylaktisch" in UI |
| days with attack medication | Tage mit Akutmedikation; legend "Nur Akutmedikation" | |
| simple painkiller | einfaches Schmerzmittel | Examples "Ibuprofen, Paracetamol, ASS", not "Aspirin" (a brand) |
| triptan | Triptan | Identical-value list |
| combination or other | Kombipräparat oder anderes | |
| medication overuse headache (PDF, store, site; no current app string) | Kopfschmerz bei Übergebrauch von Schmerz- oder Migränemitteln | Title of AWMF 030/131. Never Missbrauch, Schmerzmittelkopfschmerz, medikamenteninduziert, arzneimittelbedingt; no "MOH" in the UI |
| Did it help? | Hat es geholfen? | Answers Gar nicht / Etwas / Deutlich / Vollständig, the same words in `medication_helped_*` and `insights_helped` |
| impact: No impact / Less active / Had to stop / Bed rest | Keine Einschränkung / Weniger aktiv / Musste aufhören / Bettruhe | |
| Warning signs | Warnzeichen | |
| your local emergency number | den Notruf; verb "Wähle den Notruf" | No digits (D95, D171). Never Notarzt, Krankenwagen or 112. "jetzt" only where English says "now" |
| get urgent medical advice | Hol dir dringend ärztlichen Rat | Never sofort, heute or Notaufnahme (D171) |
| medical advice | medizinischer Rat | |
| not a medical device | kein Medizinprodukt | Never "medizinisches Gerät" |
| doctor report | **Bericht für die Arztpraxis** | Not "Arztbericht": Duden defines it as a report written *by* a doctor, which contradicts "patient-entered, not a diagnosis". `ux-designer` may reword, but it must say *for* a doctor |
| doctor visit | Arzttermin | The lock-screen visit notification (`notification_visit_title`, `notification_visit_text`) never says "Arzt" (D168, D169) |
| diary | Tagebuch | |
| reminder | Erinnerung | |
| notification | Benachrichtigung | 18 letters: long-word list once seen in place |
| History | Verlauf | |
| Insights | Auswertung | Not "Erkenntnisse" (claim) |
| period (menstrual) | Periode | A time span (`report_period`) is "Zeitraum" |
| Custom (period chip) | Eigener Zeitraum | Never "Benutzerdefiniert" (17 letters, ux R07) |
| Your data | Deine Daten | |
| backup / restore / export | Sicherung / wiederherstellen / exportieren | No "wiederherstellen" in a dialog title (ux R03): "zurückholen" there |
| Premium, Free | Premium, Kostenlos | "Premium" identical-value list |
| Undo | Rückgängig | Spoken names keep the visible word ("Löschen rückgängig machen", ux T02) |
| Save / Done / Cancel / Delete | Speichern / Fertig / Abbrechen / Löschen | |
| Not a diagnosis | Keine Diagnose | |
| DELETE (Delete all data keyword) | DELETE, unchanged | The keyword stays ASCII "DELETE" in 1.4.0 (`DeleteConfirmationResolver` is outside this diff; typeable on QWERTZ). Label short, e.g. "DELETE eingeben" (ux R19) |

Fixed wording from the pre-check (`ux-designer` owns the final copy; order and numbers must match English):

- Overuse title: `overuse_title_multiple` "Akutmedikamente an %1$d der letzten 30 Tage"; variants "Schmerzmittel an …",
  "Triptane an …", "Kombipräparate oder andere Medikamente an …".
- `indicator_row_*`: "Einfache Schmerzmittel: %1$d Tage", "Triptane: …", "Kombipräparat oder anderes: …",
  "Akutmedikation gesamt: %1$d Tage". `indicator_row_reached`: "%1$s. Ärztinnen und Ärzte achten auf %2$d oder mehr Tage
  im Monat."
- `overuse_body_soft`: "Häufige Einnahme kann zu mehr Kopfschmerzen führen. Behalte das im Blick." The "kann" is
  mandatory. `overuse_body_doctor` and `indicator_reached` keep "this often" and "month after month" ("So häufige
  Einnahme, Monat für Monat, … kann Kopfschmerzen häufiger machen. Sprich am besten mit deiner Ärztin oder deinem Arzt
  darüber.").
- **Banned in every overuse and indicator string:** Grenze, Grenzwert, Limit, erlaubt, sicher, Höchstmenge, zu viel,
  Übergebrauch, absetzen, reduzieren, Pause. The day counts are numbers linked to a diagnosis, not safe limits (D151).
- History: `chronic_notice` never says "chronisch" (a diagnosis); `progress_*` are neutral counts (D55), no "besser",
  "Erfolg" or "nur noch". `edit_dose_hint` "Zum Beispiel 400 mg" is an example, never a recommendation.
- The safety block (Warning signs list in NHS order, `safety_body` with the same words as list item 1 (D173),
  `long_check_body`, `settings_about_*`) follows the pre-check's reference German.

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
7. As built (e31ac30e, accepted): no soft hyphen (U+00AD) or zero-width character (U+200B, U+200C, U+200D, U+2060, U+FEFF) in
   any German value, and no plain space between a number and its unit (Min., Std., Tg., %, mg, €; D5 typography, ux E4).
8. The two `values-en` shortcut labels equal their `values/` source (D3).

Android Lint (already in the gate, default severity Error): `MissingTranslation`, `ExtraTranslation`,
`StringFormatMatches`, `StringFormatCount`, `MissingQuantity`. Lint does not see a module with no translation at all,
hence check 1.

Negative controls (each shown red, then reverted): remove one German string; change `%1$d` to `%1$s`; copy an English
value; add a 17-letter word; delete one module's `values-de`; add an empty `values-fr`. Built with three more: a soft
hyphen, a plain space before a unit, a `values-en` label that differs. All nine were red, then the clean tree green
(`migrely-work/de/7.2-controls.log`).

`FormattingLocaleResolverTest`: every row of the D1 table, plus the regional first-day keyword in German.
`FormattingLocales` Robolectric test: `localizedContext` with `[ru-RU, en-US, de-DE]` reads English and with
`[ru-RU, de-DE]` German, from a real `values-de` string.

No Robolectric or Roborazzi screenshots per screen: D42 still holds (build change and golden images). Layout is checked on
devices (D7).

## D7. Layout and accessibility checks (`ux-designer` defines, engineer and QA run)

`ux-designer`'s verdict (`migrely-work/de/ux-checks-verdict.md`, task 10.2) confirms and extends this section. Its risk
rows R01 to R29, N01 to N04 and T01 to T09 are the walk list; the rules below are binding.

Matrix: German, 360 x 640 dp portrait and 640 x 360 dp landscape at font 200%; **130% at 360 dp portrait** for the
layouts that switch at a font threshold (`MedicationPromptCard`, `InsightsBars`, `DoctorVisitSection`, the onboarding
header, Reminders, Insights; 115% for attack mode's intensity row), because those thresholds were tuned with English and
130% is the largest font Android 8 to 13 offer; plus 100% at 411 dp as a sanity pass. Confirm the font scale before every
screenshot: a pass at the wrong size proves nothing. Restore size, density, font, rotation and locale afterwards.

Per state: no word broken inside, no hyphen, no ellipsis, no clipped text, buttons and chips keep 48 dp (64 and 72 dp in
attack mode and Dark room) and whole words, the layout stacks where it must (D181), type never shrinks, TalkBack names in
German with the visible words in order.

Binding rules from the verdict:

1. **English comparison.** Every failure is compared with app locale `en-US` at the same settings. A German-only failure
   is a finding of this change and blocks the merge. A failure English shares is pre-existing: file an issue, not a
   blocker, unless it is on a safety path (Warning signs, the first-10 card, "Still going?", the overuse card), where it
   blocks.
2. **Words of 13 to 16 letters** in titles, buttons, chips and dialog titles are checked on the device, though the gate's
   limit is 16: a top bar or dialog title fits about 13 letters at 200%. Run the section-4 scan of the verdict before the
   walk.
3. **Number and unit on one line**: a number never ends a line with its unit on the next; prices never split (D5).
4. **One theme per state**: walk dark (the migraine default, and attack mode is always dark), plus one light screen as a
   sanity check. Strings do not change colour or contrast.
5. **No soft hyphens or zero-width spaces** (D5; the gate enforces it).
6. **E6**: the full notification text is readable when expanded (done, D2). **E7**: the launcher shortcut label matches
   the app's language for the system list (done for `[ru-RU, en-US, de-DE]`, D3).
7. **DELETE stays** as the Delete all data keyword in 1.4.0 (D5).
8. An accessibility-tree scan for English on every walked state (E10), with an `en-US` control that must report hits.

Predicted failures to fix before merge (German-only, from the code): the onboarding step header (R05), the full-screen
date pickers' top row (R02), and top bar titles that need two lines in a 64 dp bar (R01; fix order: a shorter natural
title from 10.1 first, then a bar that grows with its title, never smaller type). These are layout code changes, allowed
by this change because German is what breaks them.

Engineer's walk (5570, before review): every R row at 200% portrait and landscape, the 130% rows, 100% at 411 dp for R01,
R11, R24 and N01, and the T01 scan, in a table with an English-comparison column (`migrely-work/de/8.1-walk.md`).

QA's a11y tour (5580) can reuse the English selectors: drive each state in English, then
`adb shell cmd locale set-app-locales com.migrely.app --locales de-DE`, capture with `qa/a11y_audit.py`, and set it back.
This also exercises EC-L10N-06 on every state. API 26 has no per-app locale: a short walk with the system set to de-DE,
plus R02, R03, R05 and R06, because linear 200% there is the harshest case for long words.

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
