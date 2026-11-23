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
- Launcher shortcut (ux E7): static shortcut labels are resolved by Android from the system's list, not by D1. A first
  fix put the two labels in `values-en` (e31ac30e); the lead reversed it after cr-de round 1 (45811ea5 removed
  `values-en`, `app/lint.xml` and the equality check). Reason, from the framework source and `aapt2 dump configurations`:
  Android picks one best language from every language in the APK, and the AndroidX libraries ship about 80 (ru, tr and
  more), so `values-en` never changed the label. **Known limit (EC-L10N-25):** on `[tr-TR, de-DE]` or `[ru-RU, de-DE]`
  the app is German (D1) while the static shortcut label is English, because Android settles on the first list language
  the APK has at all. 8.2 and 12.1 walk `[ru-RU, en-US, de-DE]` and `[tr-TR, de-DE]` and record what the launcher shows,
  also with per-app German on an English phone (ux N04). A real fix (dynamic shortcuts set from D1) is out of scope.

## D4. What stays English

- **PDF doctor report** (`features/report/data` `ReportDocumentMapper`, `ReportRecordsDomainMapper`, `datasource`
  `ReportFitting`/`ReportPageLayout`): about 55 fixed strings and `Locale.ENGLISH` date patterns. Unchanged in this change.
  Follow-up `add-german-doctor-report`: move them behind a strings port provided by `:app`, re-fit columns for German.
  The report screen (`features/report/ui`) shows `report_pdf_language_note` when
  `LocalConfiguration.current.locales[0].language != "en"` (UI-only fact, no presentation change). English text:
  "The PDF is in English." German per glossary.
- **CSV** (`AttacksCsvMapper`): English column names and values, permanently (D72, data portability). The separator
  follows the system's language, never Migrely's own: `BackupModule` reads `app/locale/SystemLocale` (ca7a32c9). On
  API 33+ it uses `LocaleManager.getSystemLocales()`, which ignores the app-specific language; below 33 it reads the
  system configuration, where no per-app language exists. `Resources.getSystem()` is not enough: on API 33+ it carries
  the per-app language too (cr-de round 1, finding 1, from `ResourcesManager`). `SystemLocaleTest` covers both API paths
  and both directions (en-US system with German app gives a comma, de-DE system with English app a semicolon); the two
  API 33+ rows were red with the configuration-only read. The CSV bytes on a device stay in 8.2 and 12.1.
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
5. A German word longer than 16 characters (`LONGEST_UNLISTED_WORD = 16`: 16 passes, 17 fails), counted by
   `wordsOver()`, fails unless it is in `app/src/test/resources/l10n/long-german-words.txt`, each line
   `word<TAB>screen<TAB>seen YYYY-MM-DD`, the date of the 8.1 walk where it was seen whole at 200% on 360 dp. Until 8.1
   the lines say "not yet seen"; the dated format becomes mandatory in the last commit of 8.1, after which "not yet seen"
   or a line without a date fails.

   How `wordsOver()` counts a word (5ceb0fdd, cr-de round 2 cb11fb9c), in this order, on the value as compiled (below):
   - it composes the value (Unicode NFC), so an umlaut written as a base letter plus a combining diaeresis is one
     character and does not split the word;
   - it removes the invisible breaks (U+00AD, U+200B, U+200C, U+200D, U+2060, U+FEFF), which check 7 forbids anyway;
   - it replaces each format placeholder (`%1$s`, `%d`, `%%` and the like) by as many letters as the placeholder has
     characters, so `%1$s` counts as 4 letters and joins the letters next to it;
   - a word is a run of letters (`\p{L}`) in which a hyphen-minus (U+002D), a non-breaking hyphen (U+2011) or a
     no-break space (U+00A0) standing between two letters joins them, because Android breaks a line at none of them
     (ux R16, R18). A hyphenated compound is one word: "Open-Source-Bibliotheken" is 24, though no part is over 16;
   - the length counts every character of the word, its joiners included ("Migrely-Sicherung" is 17);
   - anything else ends a word: a plain space, a digit, an en dash, a slash, a full stop. A number with a no-break space
     before its unit ("12 Min.") is no word over the limit, because a digit is not a letter.

   A word is reported, and matched against the list, as written after composing and removing the breaks, with its
   placeholder as typed ("%1$s-Sicherungsdatei" is one 20-character word). `WordsOverTest` holds one fixture per rule,
   each over 16 only when counted this way; the four new fixtures fail on the earlier counter and pass now.

   Known limits of the count (team-lead 2026-11-23; none occurs in today's `values-de`):
   - a placeholder counts at its written length (`%1$s` is 4), not at the length it takes at runtime. "%1$s-Sicherung"
     (14) cannot fail, though at runtime it reads "Migrely-Sicherung" (17). For that reason "Migrely-Sicherung" is on
     the long-word list, and the list with its walked screen stays the check for such words;
   - a run joined by a full stop and a no-break space ("Std. …") counts as separate words, though Android does not
     break a line there. The device walk, not the gate, checks such runs.
6. `locales_config.xml` languages equal `APP_LANGUAGES` and the `values-<lang>` folders plus `en` (D3).
7. As built (e31ac30e, accepted): no soft hyphen (U+00AD) or zero-width character (U+200B, U+200C, U+200D, U+2060, U+FEFF) in
   any German value, and no plain space between a number and its unit (Min., Std., Tg., %, mg, €; D5 typography, ux E4).

Every check reads a value as aapt2 compiles it (45811ea5, cr-de round 1, finding 3): Android escapes (`\uXXXX`, `\n`,
`\'`, `\"`, `\@`, `\?`) are decoded and unescaped quotes dropped first, and invisible breaks are removed before words are
measured. Before this, an escaped soft hyphen inside a 17-letter word passed.

Android Lint (already in the gate, default severity Error): `MissingTranslation`, `ExtraTranslation`,
`StringFormatMatches`, `StringFormatCount`, `MissingQuantity`. Lint does not see a module with no translation at all,
hence check 1.

Negative controls (each shown red, then reverted): remove one German string; change `%1$d` to `%1$s`; copy an English
value; add a 17-letter word; delete one module's `values-de`; add an empty `values-fr`. Built with three more: a soft
hyphen, a plain space before a unit, a `values-en` label that differs. All nine were red, then the clean tree green
(`migrely-work/de/7.2-controls.log`). After cr-de round 1 the `values-en` control went with `values-en`, and control 10
was added: an escaped soft hyphen (`\u00AD`) inside a 17-letter word, red before the decoding and red after it for the
right reason (`migrely-work/de/cr1-3-red.log`, `cr1-23-green.log`).

`NotificationLockScreenGermanTest` (`de-rDE`, f9fd99b6), the twin of `NotificationLockScreenTest`: every notification is
private with the public version "Erinnerung", and the visit reminder's title, text and public version name no "arzt",
"migräne", "kopfschmerz" or "premium" (D168, D169). Mutations went red: a visit text naming "Arztpraxis", a public title
"Migräne-Erinnerung" (`migrely-work/de/cr1-5-red-green.log`).

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
6. **E6**: the full notification text is readable when expanded (done, D2). **E7**: the static launcher shortcut label
   cannot follow D1 (D3, EC-L10N-25): the walk records the label for `[ru-RU, en-US, de-DE]`, `[tr-TR, de-DE]` and
   per-app German, and an English label on a German app is the known limit, not a failure.
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
- EC-L10N-28 to EC-L10N-30 were free on every remote branch on 2026-11-23 (same scan, after a fetch over HTTPS). No new
  D-number: the walk's layout fixes extend D195 as item (18).

## D10. Layout fixes from the walk that English users see too (BRD D195 (18))

German broke these layouts in the 8.1 walk (rows R02, R03, R04). The fixes live in the design system or the shared
dialog, so English gets them as well. Each is a behaviour change for English users and has a scenario. Refs #276 (the
pain and effect dialogs at 200% in landscape, English and German; its reporter retests and closes it after merge).

1. **Range picker headline** (`designsystem/picker/RangeHeadline`, ed38f8f2, 6d28caa6; ux A22). Material's one-row
   "Start date - End date" squeezed the end until German "Enddatum" broke letter by letter (English "End date" after
   "En" too). `RangeHeadline` replaces it: each side, a date or its placeholder, stays whole (no-break spaces inside a
   side), the line wraps only between the sides (`FlowRow`, 8 dp apart), and the en dash stays with the start after a
   no-break space. Style and colour stay Material's. TalkBack hears one merged node, announced politely when a date
   changes, named by `report_range_headline_description`; the two shown texts are hidden from accessibility so nothing
   is said twice (D181 (2), #217). English: "Start date, not chosen, end date, not chosen"; German: "Startdatum, nicht
   gewählt, Enddatum, nicht gewählt"; a chosen side speaks its date in the formatter's spoken form. Four new strings in
   `features/report/ui`, English and German: `report_range_start`, `report_range_end`,
   `report_range_headline_description`, `report_range_not_chosen`, carried by `DateRangePickerLabels`.
2. **Full-screen picker title** (`designsystem/picker/PickerTopRow`, 2be7d8a6, used by the date and date-range
   pickers). When the title's longest word (its minimum intrinsic width) does not fit between Cancel and Done, the
   title takes its own line under them, whole and start-aligned; otherwise the row is as before. Type never shrinks.
   The stacked title stays a heading. TalkBack order in the stacked row is Cancel, Done, then the title (eb4de2be,
   `PickerTopRowTalkBackTest`; cr-de accepted it: the buttons keep their places and the heading still names the screen).
3. **Wide dialogs in a short window** (b48d26e7, 938e8e7c). `isCompactHeight()` (window height under 480 dp, one copy in
   `designsystem/layout`, Home and History use it too) makes the pain, effect and custom tag dialogs leave the platform's
   default width, so Material's maximum of 560 dp with 24 dp margins applies and two buttons can share one row.
   Portrait and normal heights keep the platform width.
4. **The title scrolls with the content** (`designsystem/dialog/ScrollingDialogContent`, 938e8e7c). Material's title
   slot does not scroll: a long title (an effect question naming a long typed medication) or stacked buttons left the
   content no height. The three dialogs put their title and content in one `PageScroll` area; the title keeps the
   dialog title's style, is a heading and names the pane (`paneTitle`) for TalkBack. The title scrolls away before an
   answer is cut.
5. **Edge cues** (88011716, 35524bfc; ux F6, R04r). While more lies below, the bottom edge shows a 24 dp fade in the
   dialog's surface colour and Material's scrolling-dialog divider (1 dp, `outlineVariant`); while more lies above, the
   top edge shows both. They ease over 150 ms, at once with reduced motion, draw over the content only and have no
   semantics. Nothing shows when the content fits. The divider is the cue that shows even over a blank-looking peek.
6. **Custom tag dialog narrows only to type** (team-lead decision 2026-11-23, after cr-de round 2 F1, F2; supersedes
   the rule built in 87c3b424, which narrowed on every press and waited 1000 ms; earlier 3202e821, fbbc64f4). A wide
   dialog is not panned for the keyboard, so the typed line would sit behind it. The decided `KeyboardWidthRule`:
   - an intent to type narrows the dialog at once to the platform width, which Android pans: a completed tap on the
     field (`PressInteraction.Release`) or the field gaining focus (also on opening);
   - a press that ends in `PressInteraction.Cancel` (a drag to scroll) is not an intent, and neither is a long press
     on a field that already has focus (to paste): the width stays as it is. On a field without focus, Compose's long
     press requests focus and the on-screen keyboard rises; that is an intent under the focus clause, which is correct
     (cr-de on 5827ef17);
   - an attached hardware keyboard (`Configuration.hardKeyboardHidden == HARDKEYBOARDHIDDEN_NO`) blocks only the typing
     intents, a tap or focus gained (team-lead decision on cr-de finding 2, 2026-11-23). It does not block a keyboard
     that actually rises: with "Use on-screen keyboard" on while the physical keyboard is active, the rising on-screen
     keyboard still narrows the dialog, and its closing widens it;
   - if no on-screen keyboard is up 3000 ms after the latest intent, the dialog is wide again (time, not "the next
     layout", because the dialog's own narrowing relays out before any IME can show; 3000 ms leaves room for a slow
     keyboard under host load);
   - an on-screen keyboard rising narrows it; closing widens it, even with the field still focused.
   The keyboard is read from the root view's insets, because a dialog window that fits its system bars shows Compose
   no IME inset. Built in a115465f (task 8.3, cr-de approved 5827ef17), except the hardware-keyboard clause: there
   `customTagDialogWide()` still vetoes every narrowing while a hardware keyboard is attached, including a risen
   on-screen keyboard. Task 8.4 brings it to the rule above.

Not proved by unit tests, left to the 8.1 walk (tasks 8.1): the root-inset keyboard reading, 7037cc0e and b5e2a08e, the
fade's alpha and its reduced-motion snap, F2 on a device, the dialogs' spoken pane name (F3: Material's
`BasicAlertDialog` already names its pane "Dialog", so TalkBack may say it twice), and `PageScroll`'s TalkBack step
inside a dialog while a message shows behind it.

## Risks

- Translation quality without a native speaker: mitigated by the glossary, `ux-designer`'s copy review and
  `medical-reviewer`'s per-key check; residual risk recorded (D195 (8)). German Play reviews go to `support-agent`.
- Every later change pays for German strings. Accepted: the gate makes it visible instead of shipping English in German.
- Per-app German on an English phone may be read by an English TTS voice (EC-L10N-17): recorded limit.
- Long German words at 200%: the long-word list forces a look at each one; the device walk is the real check.
