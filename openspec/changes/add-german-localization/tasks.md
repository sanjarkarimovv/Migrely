# Tasks

Base: `feature/add-german-localization` (worktree `migraine-app-de`) from `origin/develop` 224dad3d. Release 1.4.0, after
#255 and before T72. Write `Refs T23` in commits; no issue is closed by this change. BRD D195, EC-L10N-01 to EC-L10N-24.
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

## 1. The language rule (`:app`, `locale/`, domain of this change)

- [ ] 1.1 Red first: extend `FormattingLocaleResolverTest` with every row of design D1's table (input a `LocaleList`),
  including German with a regional Sunday first day. Verify: `./gradlew :app:testDebugUnitTest --tests
  '*FormattingLocaleResolverTest'` fails on the German rows and passes the unchanged English rows.
- [ ] 1.2 `FormattingLocaleResolver.resolve(phoneLocales: LocaleList)` and `APP_LANGUAGES = setOf("en", "de")` (D1);
  `FormattingLocales.of` passes `configuration.locales`. KDoc names D195. Verify: the 1.1 tests green; `git grep -n
  'locales\[0\]' app/src/main/java/com/migrely/app/locale` shows only the first-day and fallback reads.

## 2. Strings outside the activity (`:app` datasource)

- [ ] 2.1 `FormattingLocales.localizedContext(context)` (D2) and a Robolectric test: `[ru-RU, en-US, de-DE]` reads an
  English string, `[ru-RU, de-DE]` the German one, using a key of `app/src/main/res/values-de/strings.xml` (write the
  `app` module's German file of 5.2 first). Verify: `./gradlew :app:testDebugUnitTest --tests '*FormattingLocales*'` green, red
  with `localizedContext` returning the plain context.
- [ ] 2.2 `NotificationDataSourceImpl` reads every string through `localizedContext`; `refreshChannelNames()` re-registers
  only existing channels; `MigrelyApplication` calls it in `onCreate` and on a locale-list change. Grep and convert any
  other non-UI `getString`. Verify: `git grep -n 'context.getString\|getQuantityString' -- app/src/main datasource` lists
  only localized reads; a Robolectric test shows no channel is created by the refresh and an existing one is renamed.

## 3. CSV separator (data wiring)

- [ ] 3.1 `BackupModule`: `locale = { Resources.getSystem().configuration.locales[0] }` with a comment naming D195 (5) and
  D72. Verify: `./gradlew :features:backup:data:test :app:testDebugUnitTest` green; `git grep -n 'getSystem()'
  app/src/main/java/com/migrely/app/di/BackupModule.kt` shows the line.

## 4. Android 13+ app language (manifest and resources)

- [ ] 4.1 `res/xml/locales_config.xml` (en, de) and `android:localeConfig` on `<application>`. Verify: `./gradlew
  :app:processDebugMainManifest` then `find app/build/intermediates -path '*merged_manifest*' -name AndroidManifest.xml
  -exec grep -l localeConfig {} +` names the merged manifest; `./gradlew :app:checkReleasePermissions` passes with no new permission.

## 5. German strings (resources, all 12 main modules)

- [ ] 5.1 `translatable="false"` on `app_name` and every string in `app/src/debug/res/values/strings.xml`. Verify:
  `grep -c 'translatable="false"' app/src/debug/res/values/strings.xml` equals its string count (15).
- [ ] 5.2 `values-de/strings.xml` for `app` and the 11 feature `ui` modules, same key order as English, glossary D5, "du",
  „ “ quotes, plurals `one` and `other`. One commit per two or three modules. Verify per commit: `./gradlew
  :features:<m>:ui:lint` shows 0 `MissingTranslation`, 0 `StringFormatMatches`, 0 `ExtraTranslation` for those modules.
- [ ] 5.3 Write `migrely-work/de/review-medical.tsv` and `review-legal.tsv` (design D8) and send them to
  `medical-reviewer` and `legal-finance-analyst` with the glossary. Verify: `wc -l` of both files; every key named in
  design D8's prefixes is present (`grep -c`).

## 6. Report screen line (UI)

- [ ] 6.1 `features/report/ui`: `report_pdf_language_note` (English "The PDF is in English.", German per glossary), shown
  only when `LocalConfiguration.current.locales[0].language != "en"`, wrapping, read once by TalkBack. Verify: a Compose
  test in `features/report/ui` with `de` and `en` configurations shows and hides the line.

## 7. The translation gate (tests)

- [ ] 7.1 `LocalizationCompletenessTest` with checks 1 to 6 of design D6, and the two lists in
  `app/src/test/resources/l10n/` (every long word with the screen where it was seen at 200% on 360 dp; filled after 8.1).
  Verify: `./gradlew :app:testDebugUnitTest --tests '*LocalizationCompletenessTest'` green.
- [ ] 7.2 Negative controls, each red then reverted, logged in `migrely-work/de/7.2-controls.log`: remove one German
  string; `%1$d` to `%1$s`; copy one English value; add a 17-letter word; delete one module's `values-de`; add an empty
  `values-fr/strings.xml`. Verify: six failing runs named in the log, then a green run on the clean tree.

## 8. Device walk (`android-engineer`, 5570)

- [ ] 8.1 Walk design D7's list in German at 200%, 360 x 640 dp portrait and landscape, and at 100% 411 dp; fix or report
  every break, cut or ellipsis; fill the long-word list. Restore font scale, size and rotation. Verify: screenshots and a
  table in `migrely-work/de/8.1-walk.md` (state, portrait, landscape, result); `adb shell settings get system font_scale`
  back to 1.0.
- [ ] 8.2 Language rule on device: system de-DE; `[ru-RU, en-US, de-DE]`; per-app `cmd locale set-app-locales` de-DE on
  an English system and back; change during attack mode and with an unsaved note; a reminder fired in German; channel names
  in system settings; CSV separator on an en-US system with per-app German. Verify: rows in `migrely-work/de/8.2-rule.md`
  with the exact `adb` commands and results; system locale restored.

## 9. Gate (`android-engineer`)

- [ ] 9.1 Verify: `set -o pipefail; ./gradlew assembleDebug test ktlintCheck detekt lint 2>&1 | tail -30` exits 0, and
  `./gradlew :app:checkReleasePermissions` exits 0 with the same permission list as v1.3.3.

## 10. Reviews

- [ ] 10.1 `ux-designer`: review every German string (voice, length, glossary, TalkBack words), rewrite findings go to the
  engineer, confirm "du" or switch totally to "Sie". Verify: a verdict file `migrely-work/de/ux-copy-verdict.md` with 0
  open findings.
- [ ] 10.2 `ux-designer`: confirm or extend design D7's matrix and state list before 8.1 and 12; check 8.1's table.
  Verify: verdict in `migrely-work/de/ux-checks-verdict.md`.
- [ ] 10.3 `medical-reviewer`: PASS or FAIL per medical key, against English, its citation and a back-translation; final
  wording for the overuse term. Verify: `migrely-work/de/medical-verdict.md` lists every key of `review-medical.tsv` with
  PASS; no FAIL left.
- [ ] 10.4 `legal-finance-analyst`: subscription, renewal, cancellation and privacy keys. Verify:
  `migrely-work/de/legal-verdict.md` with 0 open findings.

## 11. Code review (`code-reviewer`)

- [ ] 11.1 Review the diff against design D1 to D6 and this spec; check that no locale read bypasses `FormattingLocales`.
  Verify: review verdict with 0 open blocking findings; fixes re-run 9.1.

## 12. QA (`qa-engineer`, Go/No-Go rows for this diff)

- [ ] 12.1 API 36 (5580): system de-DE walk of the a11y tour via per-app switching (design D7) at 100%, 200% and
  landscape; TalkBack in German on home, attack mode and Log medication; notifications in German; CSV bytes (English
  headers, `;`); backup made in English restored in German and back; update from v1.3.3 on de-DE with a planned reminder;
  `[ru-RU, en-US, de-DE]` stays English. Verify: rows in the 1.4.0 Go/No-Go with commands and counts.
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
