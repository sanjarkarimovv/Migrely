# Tasks

Base: `feature/add-german-localization` (worktree `migraine-app-de`) from `origin/develop` 224dad3d. Release 1.4.0, after
#255 and before T72. Write `Refs T23` in commits; no issue is closed by this change (also `Refs #276`, never a closing
keyword: its reporter retests and closes it). BRD D195, EC-L10N-01, EC-L10N-04 to EC-L10N-30.
Design: this change's `design.md` (D1 to D9, glossary in D5).

Ownership:
- `product-manager`: 0 (done) and 13;
- `android-engineer`: 1 to 9;
- `ux-designer`: 10.1 (copy owner) and 10.2 (checks);
- `medical-reviewer`: 10.3 (read only, PASS or FAIL per key);
- `legal-finance-analyst`: 10.4;
- `code-reviewer`: 11;
- `qa-engineer`: 12 (emulator-5580 API 36, then emulator-5582 API 26, one at a time).

Every new test is shown red before its code (or by its negative control), then green. Logs go to `migrely-work/de/`.
Under load use `nice -n 19 ./gradlew --max-workers=2 …`; device tasks need `ANDROID_SERIAL=emulator-5570`. Before step 5,
merge the latest `origin/develop` (with #255) so the translated set is final; if T72 merged first, translate its strings.

## 0. Plan (`product-manager`)

- [x] 0.1 BRD D195, EC-L10N-01 updated, EC-L10N-04 to EC-L10N-24, D117 pointer, section 13 row 59; proposal, design,
  `app-language` spec, `app-shell`, `doctor-report` and `data-backup` deltas, tasks. Verify:
  `openspec validate add-german-localization --strict` and `openspec validate --all --strict` exit 0.

- [x] 0.2 Fold in the medical pre-check glossary, `ux-designer`'s D7 verdict (8 points, "du" confirmed), the lead's E6
  and E7 decisions and eng-de's deviations: BRD D195 (15) to (17), EC-L10N-07, -12, -14 updated, EC-L10N-25 to -27, D117
  line; design D1 to D7; `app-language` scenarios. Verify: both `openspec validate` commands exit 0.

- [x] 0.3 Fold in code-review finding 5 and the walk's layout fixes (6f96d002..cb11fb9c) and the lead's custom tag
  width rule of 2026-11-23: design D6 check 5 (the gate's word count and its known limits) and D10; `app-language`
  long-word scenarios; `app-shell`, `doctor-report` and `custom-tags` ADDED requirements; BRD D195 (18), EC-L10N-14,
  EC-L10N-25 (8.2 observation), EC-L10N-28 to EC-L10N-30, row 59. Verify: both `openspec validate` commands exit 0.

## 1. The language rule (`:app`, `locale/`, domain of this change)

- [x] 1.1 Red first: extend `FormattingLocaleResolverTest` with every row of design D1's table (input a `LocaleList`),
  including German with a regional Sunday first day. Verify: `./gradlew :app:testDebugUnitTest --tests
  '*FormattingLocaleResolverTest'` fails on the German rows and passes the unchanged English rows.
  Done 603c58b2: red on the German rows (`migrely-work/de/1.1-red.log`, exit 1).
- [x] 1.2 `FormattingLocaleResolver.resolve(phoneLocales: LocaleList)` and `APP_LANGUAGES = setOf("en", "de")` (D1);
  `FormattingLocales.of` passes `configuration.locales`. KDoc names D195. Verify: the 1.1 tests green; `git grep -n
  'locales\[0\]' app/src/main/java/com/migrely/app/locale` shows only the first-day and fallback reads.

  Done 603c58b2: green (`migrely-work/de/1.2-green.log`, exit 0). Built as `resolve(phoneLocales: List<Locale>)`, accepted
  (design D1, D195 (15)).
## 2. Strings outside the activity (`:app` datasource)

- [x] 2.1 `FormattingLocales.localizedContext(context)` (D2) and a Robolectric test: `[ru-RU, en-US, de-DE]` reads an
  English string, `[ru-RU, de-DE]` the German one, using a key of `app/src/main/res/values-de/strings.xml` (write the
  `app` module's German file of 5.2 first). Verify: `./gradlew :app:testDebugUnitTest --tests '*FormattingLocales*'` green, red
  with `localizedContext` returning the plain context.
  Done febaf0df: red then green (`migrely-work/de/2.1-red.log`, `migrely-work/de/2.1-green.log`).
- [x] 2.2 `NotificationDataSourceImpl` reads every string through `localizedContext`; `refreshChannelNames()` re-registers
  only existing channels; `MigrelyApplication` calls it in `onCreate` and on a locale-list change. Grep and convert any
  other non-UI `getString`. Verify: `git grep -n 'context.getString\|getQuantityString' -- app/src/main datasource` lists
  only localized reads; a Robolectric test shows no channel is created by the refresh and an existing one is renamed.

  Done febaf0df: `NotificationLanguageTest` red on the visit date and the channel rename, then green
  (`migrely-work/de/2.2-red.log`, `migrely-work/de/2.2-green.log`). The visit date now uses the app's formatting locale (design D2, EC-L10N-26).
- [x] 2.3 Expanded notification text (ux E6, lead decision): `BigTextStyle` on the daily, "Did it help?" and visit
  notifications. Verify: `NotificationExpandedTextTest` 3 of 3 red then green.
  Done 404c989b (`migrely-work/de/E6-red.log`, `migrely-work/de/E6-green.log`).

## 3. CSV separator (data wiring)

- [x] 3.1 `BackupModule`: `locale = { Resources.getSystem().configuration.locales[0] }` with a comment naming D195 (5) and
  D72. Verify: `./gradlew :features:backup:data:test :app:testDebugUnitTest` green; `git grep -n 'getSystem()'
  app/src/main/java/com/migrely/app/di/BackupModule.kt` shows the line.

  Done (`migrely-work/de/3-4-verify.log`). Superseded by cr-de round 1, finding 1 (ca7a32c9): on API 33+
  `Resources.getSystem()` carries the per-app language, so `BackupModule` now reads `app/locale/SystemLocale`
  (`LocaleManager.getSystemLocales()` on 33+, the system configuration below). `SystemLocaleTest` 4 of 4 green, its two
  API 33+ rows red with the configuration-only read (`migrely-work/de/cr1-1-red.log`, `migrely-work/de/cr1-1-green.log`).
## 4. Android 13+ app language (manifest and resources)

- [x] 4.1 `res/xml/locales_config.xml` (en, de) and `android:localeConfig` on `<application>`. Verify: `./gradlew
  :app:processDebugMainManifest` then `find app/build/intermediates -path '*merged_manifest*' -name AndroidManifest.xml
  -exec grep -l localeConfig {} +` names the merged manifest; `./gradlew :app:checkReleasePermissions` passes with no new permission.

  Done 265e4fab (`migrely-work/de/3-4-verify.log`): `localeConfig` in the merged manifest; release permissions unchanged.
- [x] 4.2 Launcher shortcut labels (ux E7, lead decision): `values-en` with the two shortcut labels, `app/lint.xml`
  ignoring only "not translated in en", a test that they equal `values/`. Verify: `./gradlew :app:lintDebug` exit 0.
  Done e31ac30e (`migrely-work/de/E7-lint.log`, exit 0). **Reversed** by the lead after cr-de round 1, finding 2 (45811ea5): Android
  picks the label's language from every language in the APK (AndroidX ships about 80), so `values-en` changed nothing;
  `values-en`, `app/lint.xml` and the equality check are removed. Known limit EC-L10N-25, walked in 8.2.

## 5. German strings (resources, all 12 main modules)

- [x] 5.1 `translatable="false"` on `app_name` and every string in `app/src/debug/res/values/strings.xml`. Verify:
  `grep -c 'translatable="false"' app/src/debug/res/values/strings.xml` equals its string count (15).
  Done; also the four licence and library names (design D4, D195 (15)). Style run `migrely-work/de/1-4-style.log`, exit 0.
- [x] 5.2 `values-de/strings.xml` for `app` and the 11 feature `ui` modules, same key order as English, glossary D5, "du",
  „ “ quotes, plurals `one` and `other`. One commit per two or three modules. Verify per commit: `./gradlew
  :features:<m>:ui:lint` shows 0 `MissingTranslation`, 0 `StringFormatMatches`, 0 `ExtraTranslation` for those modules.
  Done 11a0c809, f88ef88d, e3f88675, 8abb6516, a5d8fd61: lint 0 translation issues (`migrely-work/de/5.2-lint.log`, exit 0).
  medical pre-check glossary applied: no "mittel", "Arztbericht", "Mittel gegen" or "Nackensteife" in any
  `values-de`; the 14 overuse and indicator strings hold none of the banned words (0 hits; the same pattern finds 19
  elsewhere, so it can fail).
- [ ] 5.3 (partial) Write `migrely-work/de/review-medical.tsv` and `review-legal.tsv` (design D8) and send them to
  `medical-reviewer` and `legal-finance-analyst` with the glossary. Verify: `wc -l` of both files; every key named in
  design D8's prefixes is present (`grep -c`).

  Partial: `migrely-work/de/review-medical.tsv` (281 keys) and `migrely-work/de/review-legal.tsv` (106 keys) written, with the pre-check's
  extra keys (sample of 10 checked present); `medical-reviewer`'s pre-check done (`migrely-work/de/medical-precheck.md`, no key
  passed). Open: per-key verdicts (10.3, 10.4).
## 6. Report screen line (UI)

- [x] 6.1 `features/report/ui`: `report_pdf_language_note` (English "The PDF is in English.", German per glossary), shown
  only when `LocalConfiguration.current.locales[0].language != "en"`, wrapping, read once by TalkBack. Verify: a Compose
  test in `features/report/ui` with `de` and `en` configurations shows and hides the line.

  Done a5d8fd61: `ReportPdfLanguageNoteTest` red then green (`migrely-work/de/6.1-red.log`, `migrely-work/de/6.1-green.log`).
## 7. The translation gate (tests)

- [ ] 7.1 (partial) `LocalizationCompletenessTest` with checks 1 to 6 of design D6, and the two lists in
  `app/src/test/resources/l10n/` (every long word with the screen where it was seen at 200% on 360 dp; filled after 8.1).
  Verify: `./gradlew :app:testDebugUnitTest --tests '*LocalizationCompletenessTest'` green.
  Partial (e31ac30e): the test runs checks 1 to 8 of design D6 and is green in the gate (`migrely-work/de/7.1-red.log` red with 11
  modules missing; `migrely-work/de/9.1-gate.log`). Open: the four long words in `long-german-words.txt` are "not yet seen" until
  the 8.1 walk. Since cr-de round 1 (45811ea5) every check reads values as compiled (escapes decoded, invisible breaks
  removed before measuring); the `values-en` check is gone. The rule that each long-word line ends with
  `seen YYYY-MM-DD` is ready (`migrely-work/de/item4-long-words-walk-date.patch`, red control `migrely-work/de/item4-red.log`) and lands as the
  last commit of 8.1, with the dates of the walk. "Sicherheitshinweis" joined the list in 2a4f2261, also not yet seen.
- [x] 7.2 Negative controls, each red then reverted, logged in `migrely-work/de/7.2-controls.log`: remove one German
  string; `%1$d` to `%1$s`; copy one English value; add a 17-letter word; delete one module's `values-de`; add an empty
  `values-fr/strings.xml`. Verify: six failing runs named in the log, then a green run on the clean tree.

  Done: nine controls (the six above plus soft hyphen, plain space before a unit, a drifting `values-en` label), each
  red, then the clean tree green (`migrely-work/de/7.2-controls.log`, `migrely-work/de/7.2-last-run.log`). After cr-de round 1 the `values-en`
  control went with `values-en`; control 10, an escaped soft hyphen (`\u00AD`) inside a 17-letter word, passed before
  the decoding and fails now (`migrely-work/de/cr1-3-red.log`, `migrely-work/de/cr1-23-green.log`).
## 8. Device walk (`android-engineer`, 5570)

- [ ] 8.1 Walk design D7's list (the verdict's R01 to R29 and N01 to N04, plus the T01 scan) in German at 200%, 360 x 640
  dp portrait and landscape, 130% for the threshold layouts, and 100% at 411 dp; dark theme plus one light screen;
  compare every failure with `en-US` at the same settings; fix every German-only failure (R01, R02, R05 predicted) and
  file shared ones as issues (safety paths block); check 13 to 16-letter words in titles and buttons; fill the long-word
  list. Restore font scale, size and rotation. Verify: screenshots and a
  table in `migrely-work/de/8.1-walk.md` (row ID, state, 200% portrait, 200% landscape, 130%, 100% at 411, English
  comparison, result, screenshot); `adb shell settings get system font_scale`
  back to 1.0.
  Known gaps the walk covers, not unit tests (cr-de round 2, F6): the keyboard seen through the root window insets in
  the custom tag dialog; 7037cc0e (only the field brought into view at rest); b5e2a08e (asked again when the area
  resizes); the edge fade's alpha and its reduced-motion snap.
  Open device rows after cr-de round 2 (each at 640 x 360 dp landscape unless named; record EN and DE, the font scale
  and the screenshot in `8.1-walk.md`; run them after 8.3 lands):
  - [ ] F2: tap the custom tag field, press Back, tap again and type. Is the typed line visible above the keyboard? At
    EN 100% and DE 200%.
  - [ ] F2 under host load: the same steps with the host load noted (`uptime`) at 20 or more; a slow keyboard must not
    leave the dialog wide under it.
  - [ ] Drag on the custom tag field to scroll the dialog: the dialog's width must not change. EN 100% and DE 200%.
  - [ ] With the custom tag field already focused and the keyboard down, long-press it to paste: the dialog's width
    must not change. EN 100% and DE 200%. (On a field without focus the long press gives focus and the keyboard
    rises: narrowing there is correct.)
  - [ ] TalkBack: double-tap the already-focused custom tag field after Back, then type. Is the typed line visible above
    the keyboard? EN 100% and DE 200%. If not, it is a finding for `android-engineer` (idea: count TalkBack's
    accessibility click as an intent).
  - [ ] Switch Access: the same steps (select the already-focused field after Back, then type). Is the typed line
    visible? EN 100% and DE 200%. If not, a finding for `android-engineer`, as above.
  - [ ] A Bluetooth (or emulated hardware) keyboard attached, with "Use on-screen keyboard" forced on: tap the field.
    The on-screen keyboard rises and the dialog narrows, the typed line visible above it; close the keyboard and the
    dialog is wide again. With the setting off: the dialog stays wide while typing on the hardware keyboard.
  - [ ] F3: Material's `BasicAlertDialog` names its pane "Dialog" and `ScrollingDialogContent` names it by the title.
    Is each of the pain, effect and custom tag dialogs announced once when it opens, in EN and DE (TalkBack on,
    `adb logcat` of the spoken text or a screen recording)? A double announcement is a finding for `android-engineer`.
  - [ ] `PageScroll`'s TalkBack step inside a dialog while a message shows behind it (for example an Undo message on
    Attack details, then the pain dialog opened): each answer reached once, by name, and the step keeps its place.
- [ ] 8.2 Language rule on device: system de-DE; `[ru-RU, en-US, de-DE]`; `[tr-TR, de-DE]` (the app German, the static
  shortcut label recorded as observed, EC-L10N-25); the launcher label in every row; per-app `cmd locale set-app-locales` de-DE on
  an English system and back; change during attack mode and with an unsaved note; a reminder fired in German; channel names
  in system settings; CSV separator on an en-US system with per-app German. Verify: rows in `migrely-work/de/8.2-rule.md`
  with the exact `adb` commands and results; system locale restored.

- [x] 8.3 (`android-engineer`) Build the lead's custom tag width rule (design D10 item 6, `custom-tags` spec): narrow on
  `PressInteraction.Release` or focus gained, not on `PressInteraction.Cancel` or a long press, never while
  `Configuration.hardKeyboardHidden == HARDKEYBOARDHIDDEN_NO`, wide again 3000 ms after the latest intent with no
  on-screen keyboard up; a rising keyboard narrows, a closing one widens. Red first for each new case (cancelled drag,
  long press, hardware keyboard, the 3000 ms wait). Verify: `./gradlew :features:attack:ui:testDebugUnitTest --tests
  '*KeyboardWidthRule*' --tests '*CustomTagTypingIntent*' --tests '*CustomTagDialogWidth*'` green, each new case shown
  red on 87c3b424's rule (`migrely-work/de/8.3-*.log`); then 9.1.
- [ ] 8.4 (`android-engineer`) Hardware keyboard clause, team-lead decision on cr-de finding 2 (design D10 item 6,
  `custom-tags` scenarios "A hardware keyboard is attached" and "An on-screen keyboard rises beside a hardware
  keyboard"): an attached hardware keyboard blocks only the typing intents (tap, focus gained); an on-screen keyboard
  that rises anyway still narrows the dialog and its closing widens it. a115465f's `customTagDialogWide()` vetoes every
  narrowing instead. Red first: hardware keyboard attached plus a risen keyboard reported, expected narrow. Verify:
  `./gradlew :features:attack:ui:testDebugUnitTest --tests '*KeyboardWidthRule*' --tests '*CustomTagDialogWidth*'
  --tests '*CustomTagDialogWiring*'` green, the new case red on a115465f (`migrely-work/de/8.4-*.log`); then 9.1.

## 9. Gate (`android-engineer`)

- [x] 9.1 Verify: `set -o pipefail; ./gradlew assembleDebug test ktlintCheck detekt lint 2>&1 | tail -30` exits 0, and
  `./gradlew :app:checkReleasePermissions` exits 0 with the same permission list as v1.3.3.

  Done at e31ac30e: GATE_EXIT=0 (`migrely-work/de/9.1-gate.log`); 3600 tests, 0 failures, 0 errors (`migrely-work/de/9.1-test-count.txt`);
  release manifest 6 permissions, no INTERNET (`migrely-work/de/9.1-release-permissions.txt`).
## 10. Reviews

- [ ] 10.1 `ux-designer`: review every German string (voice, length, glossary, TalkBack words), rewrite findings go to the
  engineer, confirm "du" or switch totally to "Sie". Verify: a verdict file `migrely-work/de/ux-copy-verdict.md` with 0
  open findings.
- [x] 10.2 `ux-designer`: confirm or extend design D7's matrix and state list before 8.1 and 12; check 8.1's table.
  Verify: verdict in `migrely-work/de/ux-checks-verdict.md`.
  Matrix and state list done (`migrely-work/de/ux-checks-verdict.md`, D7 confirmed and extended, "du" confirmed,
  folded into design D5 and D7). Still open inside it: checking 8.1's table after the walk.
- [ ] 10.3 `medical-reviewer`: PASS or FAIL per medical key, against English, its citation and a back-translation; final
  wording for the overuse term. Verify: `migrely-work/de/medical-verdict.md` lists every key of `review-medical.tsv` with
  PASS; no FAIL left.
- [ ] 10.4 `legal-finance-analyst`: subscription, renewal, cancellation and privacy keys. Verify:
  `migrely-work/de/legal-verdict.md` with 0 open findings.

## 11. Code review (`code-reviewer`)

- [ ] 11.1 Review the diff against design D1 to D6 and this spec; check that no locale read bypasses `FormattingLocales`.
  Verify: review verdict with 0 open blocking findings; fixes re-run 9.1.
  Round 1 (cr-de, at e31ac30e plus fix batch 1, 2a4f2261) answered:
  1. CSV separator followed the per-app language on API 33+: `SystemLocale` (ca7a32c9).
  2. `values-en` did not change the shortcut label: removed, known limit EC-L10N-25 (lead decision, 45811ea5).
  3. Escapes slipped past the gate: values decoded before every check, control 10 (45811ea5).
  4. Long words without a walk date: the `seen YYYY-MM-DD` rule, landing as the last commit of 8.1.
  5. No German lock-screen test: `NotificationLockScreenGermanTest` (f9fd99b6), the scenario "the lock-screen visit
     reminder never says Arzt" (`migrely-work/de/cr1-5-red-green.log`).
  6. `MainActivity`'s locale KDoc named only D117: now D195 too, no code change (749897b2).
  Gate after round 1: GATE_EXIT=0 at 749897b2 (`migrely-work/de/cr1-gate.log`). Open: round 2 after 8.1, on the walk's layout fixes.

## 12. QA (`qa-engineer`, Go/No-Go rows for this diff)

- [ ] 12.1 API 36 (5580): system de-DE walk of the a11y tour via per-app switching (design D7) at 100%, 200% and
  landscape; TalkBack in German on home, attack mode and Log medication; notifications in German; CSV bytes (English
  headers, `;`); backup made in English restored in German and back; update from v1.3.3 on de-DE with a planned reminder;
  `[ru-RU, en-US, de-DE]` stays English. TalkBack speech when each of the pain, effect and custom tag dialogs opens, in
  English and German, at 100% portrait and 200% landscape: spoken once, by its title (design D10, F3). Verify: rows in
  the 1.4.0 Go/No-Go with commands and counts.
- [ ] 12.2 API 26 (5582): system de-DE, short walk (home, attack mode, Log medication, Warning signs, History), no app
  language entry exists, switching the phone to English switches the app. Verify: rows in the Go/No-Go.
- [ ] 12.3 Negative controls: v1.3.3 (English-only) on de-DE shows English on the same states (the check sees language);
  the gate's 7.2 controls re-run once on the release SHA. Verify: control rows in the Go/No-Go.
- [ ] 12.4 Record EC-L10N-17's per-app TalkBack voice as observed, a known limit, not a pass.

## 13. Records and archive (`product-manager`)

- [ ] 13.1 Fold review outcomes into D195 (voice kept or switched, medical PASS date, limits), archive the change, check
  `openspec validate --all --strict`. Verify: exit 0; `openspec list` no longer shows the change.
- [ ] 13.2 Hand-offs after merge, not part of this PR: `aso-seo-specialist` drafts the de-DE listing translation and
  German screenshots (`medical-reviewer`, `legal-finance-analyst` check; no German PDF claim); after 1.4.0 is live, a
  separate site change for `/de/` (D148); `product-manager` plans `add-german-doctor-report`. Owner steps via
  `team-lead`: AAB upload, listing translation and screenshots in Play Console, later site deploy. Verify: tracker rows
  exist for each (team-lead).
