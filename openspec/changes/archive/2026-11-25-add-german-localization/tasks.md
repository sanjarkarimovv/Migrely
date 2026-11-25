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
- [x] 5.3 Write `migrely-work/de/review-medical.tsv` and `review-legal.tsv` (design D8) and send them to
  `medical-reviewer` and `legal-finance-analyst` with the glossary. Verify: `wc -l` of both files; every key named in
  design D8's prefixes is present (`grep -c`).

  `migrely-work/de/review-medical.tsv` (281 keys) and `migrely-work/de/review-legal.tsv` (106 keys) written, with the pre-check's
  extra keys (sample of 10 checked present); `medical-reviewer`'s pre-check done (`migrely-work/de/medical-precheck.md`, no key
  passed). Closed by pm-de3, 2026-11-25: both tables got per-key verdicts (10.3, 10.4).
## 6. Report screen line (UI)

- [x] 6.1 `features/report/ui`: `report_pdf_language_note` (English "The PDF is in English.", German per glossary), shown
  only when `LocalConfiguration.current.locales[0].language != "en"`, wrapping, read once by TalkBack. Verify: a Compose
  test in `features/report/ui` with `de` and `en` configurations shows and hides the line.

  Done a5d8fd61: `ReportPdfLanguageNoteTest` red then green (`migrely-work/de/6.1-red.log`, `migrely-work/de/6.1-green.log`).
## 7. The translation gate (tests)

- [x] 7.1 `LocalizationCompletenessTest` with checks 1 to 6 of design D6, and the two lists in
  `app/src/test/resources/l10n/` (every long word with the screen where it was seen at 200% on 360 dp; filled after 8.1).
  Verify: `./gradlew :app:testDebugUnitTest --tests '*LocalizationCompletenessTest'` green.
  Partial (e31ac30e): the test runs checks 1 to 8 of design D6 and is green in the gate (`migrely-work/de/7.1-red.log` red with 11
  modules missing; `migrely-work/de/9.1-gate.log`). Open: the four long words in `long-german-words.txt` are "not yet seen" until
  the 8.1 walk. Since cr-de round 1 (45811ea5) every check reads values as compiled (escapes decoded, invisible breaks
  removed before measuring); the `values-en` check is gone. The rule that each long-word line ends with
  `seen YYYY-MM-DD` is ready (`migrely-work/de/item4-long-words-walk-date.patch`, red control `migrely-work/de/item4-red.log`) and lands as the
  last commit of 8.1, with the dates of the walk. "Sicherheitshinweis" joined the list in 2a4f2261, also not yet seen.
  Closed by pm-de3, 2026-11-25: f935623b landed the rule; all 7 words read `seen 2026-11-24` (eng-de3's 8.1 "Long words"
  row, DE 200%, 360 dp, portrait and landscape); gate on 6a1ca60a GATE_EXIT=0 with 4008 tests
  (`migrely-work/de/6a1ca60a-gate.log`); QA's own gate on 2b51ae7e exit 0, 4008 tests.
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
  Open by design (pm-de3, 2026-11-25): the engineer walked the rows in `migrely-work/de/8.1-walk.md` (86378dae to
  5024ecae) and QA walked the moved rows in 12.5 at 2b51ae7e. Not done: the light-theme screen (moved to the 1.4.0
  release Go/No-Go) and three sub-rows below (F2 under load, Switch Access, `PageScroll` with a message, #279).
  Font scale, size and rotation restored (`walk/eng-de3-final-state.txt`).
  Open device rows after cr-de round 2 (each at 640 x 360 dp landscape unless named; record EN and DE, the font scale
  and the screenshot in `8.1-walk.md`; run them after 8.3 lands):
  - [x] F2: tap the custom tag field, press Back, tap again and type. Is the typed line visible above the keyboard? At
    EN 100% and DE 200%.
    eng-de, 5570 at ab68a90d, 2026-11-23: PASS at both (`8.1-walk.md` F2, `walk/f2.log`, `shots/F2-*`).
  - [ ] F2 under host load: the same steps with the host load noted (`uptime`) at 20 or more; a slow keyboard must not
    leave the dialog wide under it.
    Not taken (pm-de3, 2026-11-25): the host load stayed 1.6 to 5.6 in the walk, and no later session reached 20. The
    3000 ms wait rests on `KeyboardWidthRule` unit tests (8.3). Residual risk in D195 (21).
  - [x] Drag on the custom tag field to scroll the dialog: the dialog's width must not change. EN 100% and DE 200%.
    eng-de, 5570 at ab68a90d, 2026-11-23: PASS, width unchanged (`8.1-walk.md` F2-drag, `walk/f2.log` G1, H3).
  - [x] With the custom tag field already focused and the keyboard down, long-press it to paste: the dialog's width
    must not change. EN 100% and DE 200%. (On a field without focus the long press gives focus and the keyboard
    rises: narrowing there is correct.)
    eng-de, 5570 at ab68a90d, 2026-11-23: PASS, three real long presses; negative control (short tap) narrows
    (`8.1-walk.md` F2-long and F2-control).
  - [x] TalkBack: double-tap the already-focused custom tag field after Back, then type. Is the typed line visible above
    the keyboard? EN 100% and DE 200%. If not, it is a finding for `android-engineer` (idea: count TalkBack's
    accessibility click as an intent).
    eng-de2, 5570 at 39f441ce, 2026-11-24: PASS at both, the dialog narrows and "KieferNacken" shows above the keyboard
    (`8.1-walk.md` TB row 4, `walk/tb/row4-*.log`).
  - [ ] Switch Access: the same steps (select the already-focused field after Back, then type). Is the typed line
    visible? EN 100% and DE 200%. If not, a finding for `android-engineer`, as above.
    Not taken (pm-de3, 2026-11-25): the emulator harness cannot drive Switch Access inside the dialog (eng-de2, three
    attempts, `8.1-walk.md` SA row 4). Needs a person with a switch; residual risk in D195 (21).
  - [x] A Bluetooth (or emulated hardware) keyboard attached, with "Use on-screen keyboard" forced on: tap the field.
    eng-de3, 5570 at 5024ecae (`8.1-walk.md`, HW-kbd): forced on PASS (narrow with the keyboard, typed line visible,
    wide after Back). Setting off: not produced on this image (Gboard rises beside the emulated keyboard and the dialog
    narrows, as specified for a rising keyboard); "wide while typing on the hardware keyboard alone" rests on
    `CustomTagDialogWiringTest`.
    The on-screen keyboard rises and the dialog narrows, the typed line visible above it; close the keyboard and the
    dialog is wide again. With the setting off: the dialog stays wide while typing on the hardware keyboard.
  - [x] F3: Material's `BasicAlertDialog` names its pane "Dialog" and `ScrollingDialogContent` names it by the title.
    Is each of the pain, effect and custom tag dialogs announced once when it opens, in EN and DE (TalkBack on,
    `adb logcat` of the spoken text or a screen recording)? A double announcement is a finding for `android-engineer`.
    Found: announced once, but by whichever pane name appeared last ("Dialogfeld"/"Dialog" for pain and custom tag,
    the title for effect). Fixed: the dialog names its one pane by the title (`MigrelyAlertDialog(paneTitle)`); on the
    fix each dialog is announced once, by its title, in EN and DE at 100% portrait (`8.1-walk.md`, F3 rows).
  - [ ] `PageScroll`'s TalkBack step inside a dialog while a message shows behind it (for example an Undo message on
    Attack details, then the pain dialog opened): each answer reached once, by name, and the step keeps its place.
    Known limit #279 (S3, team-lead): at DE 200% landscape, TalkBack's first pass skips the pain dialog's time row
    (eng-de2, `8.1-walk.md` row 2). Stays open until #279 is fixed.
- [x] 8.2 Language rule on device: system de-DE; `[ru-RU, en-US, de-DE]`; `[tr-TR, de-DE]` (the app German, the static
  shortcut label recorded as observed, EC-L10N-25); the launcher label in every row; per-app `cmd locale set-app-locales` de-DE on
  an English system and back; change during attack mode and with an unsaved note; a reminder fired in German; channel names
  in system settings; CSV separator on an en-US system with per-app German. Verify: rows in `migrely-work/de/8.2-rule.md`
  with the exact `adb` commands and results; system locale restored.

  Moved to QA (team-lead decision, 2026-11-24): the 8.1 and 8.2 rows not yet walked go to 12.5 and are QA's to run.
  8.1 and 8.2 stay open here for what the engineer walked; nothing in 12.5 counts as walked until QA records it.

  8.2 closed by pm-de3, 2026-11-25. eng-de, eng-de2 and eng-de3 on 5570 (2be7d8a6 to 5024ecae), `8.2-rule.md`, with
  the exact `adb` commands:
  - PASS: CSV both ways (rows 1 and 2); [ru-RU, en-US, de-DE] English (4, 4b); [tr-TR, de-DE] German app (5); channel
    rename keeps settings (6); attack mode and an unsaved note (7, 8); process death (9 to 9e, two fixes); the system's
    per-app page (11); a reminder fired in German (10, 13); channel names on the system page (12, 14).
  - Recorded as observed: the static shortcut on [tr-TR, de-DE] is English (EC-L10N-25); a per-app change while the
    activity stays started applies at the next start (9d-front).
  - System de-DE alone with the static shortcut: QA's N04 at 2b51ae7e, "Migräne beginnt" (12.5).
  - The system was restored (`walk/eng-de3-final-state.txt`).

- [x] 8.3 (`android-engineer`) Build the lead's custom tag width rule (design D10 item 6, `custom-tags` spec): narrow on
  `PressInteraction.Release` or focus gained, not on `PressInteraction.Cancel` or a long press, never while
  `Configuration.hardKeyboardHidden == HARDKEYBOARDHIDDEN_NO`, wide again 3000 ms after the latest intent with no
  on-screen keyboard up; a rising keyboard narrows, a closing one widens. Red first for each new case (cancelled drag,
  long press, hardware keyboard, the 3000 ms wait). Verify: `./gradlew :features:attack:ui:testDebugUnitTest --tests
  '*KeyboardWidthRule*' --tests '*CustomTagTypingIntent*' --tests '*CustomTagDialogWidth*'` green, each new case shown
  red on 87c3b424's rule (`migrely-work/de/8.3-*.log`); then 9.1.
- [x] 8.4 (`android-engineer`) Hardware keyboard clause, team-lead decision on cr-de finding 2 (design D10 item 6,
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
- [x] 9.2 (eng-de4, 2026-11-24) Merge `origin/develop` 38b14be2 (#278 change 60 D197, T72 change 52 D185 to D188) and
  translate T72's strings.
  - Merge 6a4d0c0d (`--no-ff`, parents 8fca5905 and 38b14be2). Only `docs/BRD.md` conflicted, in two places, both kept
    in numeric order: section 13 rows 59 (T23) and 60 (#278); section 17 D195 (T23) and D197 (#278). D196 is free on
    every remote branch (scan of all `refs/heads/*` on 2026-11-24), so nothing is renumbered. Row 59's "T72 dan oldin"
    now reads "#255 va T72 dan keyin". The strings files and specs merged without conflict; T72's spec requirements
    (`doctor-report`, `app-settings`) do not overlap this change's deltas. `openspec validate --specs --strict`: 30
    passed, exit 0; `openspec validate add-german-localization --strict`: valid.
  - T72 adds 15 keys: 13 get German, 2 stay English only (`translatable="false"`, no values-de entry):
    `settings_share_text` (T72) and `settings_share_subject` (ade1d892, below).
  - a80f1f5e: German for the 13 keys `settings_section_share`, `settings_share`, `settings_share_summary`,
    `settings_share_action`, `settings_rate`, `settings_rate_summary`, `settings_rate_action`, `settings_share_chooser`,
    `settings_play_unavailable`, `settings_share_unavailable`, `report_credit_switch`, `report_credit_switch_summary`,
    `report_message_not_saved` (it also translated `settings_share_subject`, removed in ade1d892). The credit summary
    quotes `REPORT_CREDIT` in English inside „…“ (EC-REP-10); `ReportCreditSummaryTest` checks the German summary too
    (red before the strings, `migrely-work/de/t72-credit-red.log`; green, `t72-credit-green.log`).
    `report_message_not_saved` reuses `settings_not_saved`. The two `*_action` labels are nominalised infinitives for
    German TalkBack's "Zum … doppeltippen". No new word over 16 letters.
  - Bisect warning: 6a4d0c0d and a80f1f5e do not compile their unit tests. T72 gave `ReportActions` a sixth callback
    (`onCreate` with the credit switch, EC-REP-13) and T23's `ReportPdfLanguageNoteTest` still passed five, so
    `:features:report:ui:compileDebugUnitTestKotlin` fails (gate on a80f1f5e: GATE_EXIT=1,
    `migrely-work/de/a80f1f5e-gate.log`). a7d92b3a passes six, as T72's report tests do; gate on a7d92b3a: GATE_EXIT=0
    (`a7d92b3a-gate.log`).
  - ade1d892, lead decision 2026-11-24: the shared message stays English as one message in one language (D185 (4); the
    link opens the English Play page until the de-DE listing, T79). `settings_share_subject` is `translatable="false"`
    like `settings_share_text`, and its German value is gone; the chooser, rows and messages stay German.
    `ExternalAppLauncherTest` "Given German then the shared message stays English and the chooser is German": red before
    the change on the subject (`migrely-work/de/t72-subject-red.log`), green after (`t72-subject-green.log`). Lint and
    `LocalizationCompletenessTest` needed no exclusion: both skip `translatable="false"`, as for `settings_share_text`.
  - Review table from the shipped files: `migrely-work/de/review-t72-strings.tsv`, 13 rows (`review_t72.py write`; the
    script drops non-translatable keys itself and fails if one has a German value). `review_t72.py check` exits 0;
    negative controls exit 1: the old 14-row table, one value changed in a temp copy, „…“ swapped for ASCII quotes
    (`migrely-work/de/t72-review-check-13.log`, `t72-review-check.log`).
  - Gate on the code head ade1d892: GATE_EXIT=0 (`migrely-work/de/ade1d892-gate.log`, header `HEAD=ade1d892…`,
    clean tree); 4004 unit tests, 0 failures, 0 errors, 0 skipped, from 626 XML results
    (`migrely-work/de/ade1d892-test-count.txt`); `:app:checkReleasePermissions` passed, release manifest 6
    permissions, no INTERNET (same list as 9.1).
  - Not checked here: the German T72 rows on a device. That is QA's delta row X-02 on this head.
- [x] 9.3 (eng-de4, 2026-11-24) ux T72 verdict C, team-lead decision (S3, German TalkBack only): German TalkBack reads a
  click label in "Zum %s doppeltippen", and two labels reused visible words.
  - 6cc852a5: new click-label keys, the visible text and names unchanged. `settings_privacy_policy_action` (en "read the
    full privacy policy", de "Lesen der ganzen Datenschutzerklärung") on the privacy link; `custom_remove_from_list_action`
    (en "remove from my list", de "Entfernen aus meiner Liste") on a custom chip's long press, while the actions menu keeps
    `custom_remove_from_list`. English speech differs only in letter case: the old labels were "Read the full privacy
    policy" and "Remove from my list", ux's English proposals lowercased.
  - `PolicyLinkActionLabelTest` and `CustomChipRemoveLabelTest`, German and English: red first, 4 of 4 (German heard the
    visible words, English the capital; `migrely-work/de/c-labels-red.log`), green after (`c-labels-green.log`).
  - Key check from the shipped files: `migrely-work/de/review-action-labels.tsv` (`review_action_labels.py`), exit 0; a
    temp copy with the German long-press label set back to the menu's phrase exits 1 (`c-labels-check.log`).
  - Gate on 6cc852a5: GATE_EXIT=1, ktlint in the new test (`6cc852a5-gate.log`); fixed in 6a1ca60a. Gate on 6a1ca60a:
    GATE_EXIT=0 (`migrely-work/de/6a1ca60a-gate.log`, header `HEAD=6a1ca60a…`, clean tree); 4008 unit tests, 0 failures,
    0 errors, 0 skipped, from 628 XML results (`6a1ca60a-test-count.txt`).
  - Not checked here: what TalkBack speaks on a device (X-02).
## 10. Reviews

- [x] 10.1 `ux-designer`: review every German string (voice, length, glossary, TalkBack words), rewrite findings go to the
  engineer, confirm "du" or switch totally to "Sie". Verify: a verdict file `migrely-work/de/ux-copy-verdict.md` with 0
  open findings.
  Closed by pm-de3, 2026-11-25. ux-de at e31ac30e reviewed 795 values and kept "du"
  (`migrely-work/de/ux-copy-verdict.md`):
  - section A (A1 to A22) is applied in 2a4f2261, 5bfd5896, 8d56c42b, fc4e7244 and ed38f8f2;
  - B went to medical and C to legal, both answered in the lead's `fix-batch-1.md`;
  - A16 was replaced by legal's "Einmalkauf";
  - T72's 13 keys: 13/13 PASS, and section C applied in 6cc852a5 (`ux-verdict-t72.md`).
  Spot check of the shipped `values-de` on this head: `custom_add_chip`, `settings_privacy_policy_link`,
  `long_check_ended`, `report_notes_summary` and `notification_attack_logged` carry the decided text.
- [x] 10.2 `ux-designer`: confirm or extend design D7's matrix and state list before 8.1 and 12; check 8.1's table.
  Verify: verdict in `migrely-work/de/ux-checks-verdict.md`.
  Matrix and state list done (`migrely-work/de/ux-checks-verdict.md`, D7 confirmed and extended, "du" confirmed,
  folded into design D5 and D7). Still open inside it: checking 8.1's table after the walk.
  pm-de3, 2026-11-25: ux-de reviewed the walk rows as they landed and closed R03, R04 and #274's rows (`8.1-walk.md`).
  The remaining rows were judged by QA in 12.5. There is no separate ux pass over the final table.
- [x] 10.3 `medical-reviewer`: PASS or FAIL per medical key, against English, its citation and a back-translation; final
  wording for the overuse term. Verify: `migrely-work/de/medical-verdict.md` lists every key of `review-medical.tsv` with
  PASS; no FAIL left.
  Closed by pm-de3, 2026-11-25. med-de, who is not a clinician (`migrely-work/de/medical-verdict.md`):
  - e31ac30e, 2026-11-22: 279 PASS and 2 FAIL (`reminders_effect_summary`, `insights_cycle_window`);
  - re-check at 2a4f2261, same day: all 281 rows, no open FAIL;
  - T72's keys: med-de2 at dd34f554, 2026-11-24, 13/13 PASS (`medical-verdict-t72.md`).
  The string commits after 2a4f2261 change no key of `review-medical.tsv` (checked key by key), and both FAIL fixes ship
  on this head. There was no German-speaking clinician (T31) and no native linguist (D195 (8)).
- [x] 10.4 `legal-finance-analyst`: subscription, renewal, cancellation and privacy keys. Verify:
  `migrely-work/de/legal-verdict.md` with 0 open findings.
  Closed by pm-de3, 2026-11-25: legal-de checked e31ac30e on 2026-11-22 (`migrely-work/de/legal-verdict.md`): 106 rows
  plus 1, 99 OK, 8 CHANGE, no hard blocker. The lead decided all 8 in `fix-batch-1.md`, applied in 2a4f2261, and they ship
  on this head:
  - the 4 lifetime keys and `settings_privacy_purchase` as legal wrote them;
  - the privacy link as ux C ("auf Englisch"), which legal accepts;
  - `report_notes_summary` as medical B3;
  - `subscription_free_stays` with "Sicherheitshinweis".
  No subscription key changed after 2a4f2261. `settings_privacy_phone` lost one hyphen in fc4e7244, a spacing change
  only.
  The draft is not legal advice. Its section 5 (10 points) needs a lawyer session, which is the owner's to book after
  the 1.4.0 build (money, handbook section 5). So are the German Play Console product texts and any German privacy page.

## 11. Code review (`code-reviewer`)

- [x] 11.1 Review the diff against design D1 to D6 and this spec; check that no locale read bypasses `FormattingLocales`.
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
  Review of 2d99dabd (cr-de) answered:
  1. Important: `FormattingLocales.of` read LocaleManager, two binder calls, for each formatted date and time, on the
     main thread in composition. Now resolved once and cached; refreshed in `MigrelyApplication.onCreate` and
     `onConfigurationChanged` and in `MainActivity.attachBaseContext`, the formatters' own instance through
     `LocaleEntryPoint`. `FormattingLocalesCacheTest`: no list read after the first resolve, a refresh picks up a
     changed per-app list (control: red on the uncached `of`, 8 reads for 2, `migrely-work/de/cache-negative-control.log`).
     Device check still open in 8.2: a live per-app change reaches `onConfigurationChanged` on API 36; rows 7 to 10 again.
  2. Minor: a null LocaleManager fell into a non-null parameter. It now falls back to the configuration's list;
     `PhoneLanguagesTest` (control: red with an NPE on the old code).
  Review of 0a1a4446 (cr-de), Minor 1, DRY: the pain, effect and custom tag dialogs passed the title to the pane and to
  the area. `MigrelyScrollingAlertDialog(title, …)` sets both from one parameter; the three moved to it, the custom tag
  field as `CustomTagNameField` with the area's height from the dialog. `ScrollingDialogContentTest`: one pane and one
  heading, the title (control: without the pane the wrapper's pane was `[Dialog]`,
  `migrely-work/de/wrapper-negative-control.log`).
  Re-review 2d99dabd..d37c75a2 (cr-de): approved. Minor 1: production code outside the design system's dialog folder
  may not name `ScrollingDialogContent` (`ArchitectureDialogsTest`, Konsist); chosen over `internal`, which would stop
  the two area tests from rendering the area alone at the squeezed size (control: an import in `AttackPainSection`
  turns it red, `migrely-work/de/area-rule-negative-control.log`). Minor 4: gate logs start with the head and the load
  again (`migrely-work/de/gate3.sh`). Findings 2 and 3 accepted as limits, covered by device rows.
  8.2 row 9d (eng-de3 on 015e2510, lead decision "fix go"): after a process-death restore, a per-app change back to
  the language the platform held before the death came with no configuration change, so the app stayed in the old
  language and the formatters' cache was not refreshed. Fix: `MainActivity.onStart` resolves again and recreates once
  per activity, never while finishing or changing configuration (`StaleLocaleRecreation`); notifications and channel
  names resolve fresh (`FormattingLocales.localizedContext`). Tests red then green (`migrely-work/de/9d-red.log`,
  `9d-green.log`); flag control: without it the activity is recreated twice (`9d-flag-control.log`).
  cr-de on 9902f069 (3 Minor, lead: fix all): the guard is once per language across instances, through the saved state
  (with no LocaleManager the two reads can differ in every new instance); `localizedContext` resolves without writing
  the formatters' cache; a stale-language recreate also renames the channels. Red then green (`cr3-red.log`, 4 red;
  `cr3-green.log`). Known limit: a per-app change that arrives while the activity stays started (adb with Migrely in
  front, or multi-window) applies at its next start.
  8.2 row 9e on 2756cb9a (eng-de3): the guard's mark outlived its recreate in the saved state and, after the next process
  death, held back a needed recreate: title English, date German (the `onStart` read had refreshed the formatters).
  Fix: an instance that starts in the app's language clears the mark; `onStart` reads with `FormattingLocales.fresh`,
  which leaves the formatters alone. Red then green (`migrely-work/de/9e-red.log`, `9e-green.log`).
  Closed by pm-de3, 2026-11-25 (record from team-lead): cr-de APPROVE through 5024ecae and on f935623b; cr-de2 and
  cr-de3 APPROVE on the develop-merge delta through 2b51ae7e (T72's strings, ade1d892, 6cc852a5, 6a1ca60a). 0 open
  blocking findings. Gate re-run on the code head: 6a1ca60a GATE_EXIT=0 (4008 tests); QA's gate on 2b51ae7e exit 0
  (`migrely-work/qa-140/logs/gate-2b51ae7e.log`).

## 12. QA (`qa-engineer`, Go/No-Go rows for this diff)

- [ ] 12.1 API 36 (5580): system de-DE walk of the a11y tour via per-app switching (design D7) at 100%, 200% and
  landscape; TalkBack in German on home, attack mode and Log medication; notifications in German; CSV bytes (English
  headers, `;`); backup made in English restored in German and back; update from v1.3.3 on de-DE with a planned reminder;
  `[ru-RU, en-US, de-DE]` stays English. TalkBack speech when each of the pain, effect and custom tag dialogs opens, in
  English and German, at 100% portrait and 200% landscape: spoken once, by its title (design D10, F3). Verify: rows in
  the 1.4.0 Go/No-Go with commands and counts.
  Open, partly proven (pm-de3, 2026-11-25). qa-140 gave GO at 7b200368 on 2026-11-24. The code equals the 2b51ae7e APK.
  Evidence: `migrely-work/qa-140/t23/logs/rows.jsonl`, `t23d/logs/X*.log` and `t23/logs/readback-end.txt` (DIFFS 0).
  - PASS:
    - German TalkBack in attack mode, 13 elements;
    - dialogs announced by their title, 12/12 (L-01), never "Dialog"/"Dialogfeld";
    - L-02;
    - CSV with commas and English headers (D-03; the BOM is intended, `AttacksCsvMapper`);
    - an English backup shown in German (D-04);
    - update from v1.3.3 with German set (D-05);
    - D-07 to D-09;
    - the daily reminder in German, vis PRIVATE (N01);
    - X-02 with the system in German.
  - [ru-RU, en-US, de-DE] stays English: rests on the engineer's 8.2 rows 4 and 4b and `FormattingLocaleResolverTest`.
    QA did not run it again.
  - Deferred by the lead (fast path, 2026-11-24): the German a11y tour (D-06) and German TalkBack on Home and Log
    medication.
  - Moved to the 1.4.0 release Go/No-Go: the expanded "Did it help?" and visit notifications in German.
- [x] 12.2 API 26 (5582): system de-DE, short walk (home, attack mode, Log medication, Warning signs, History), no app
  language entry exists, switching the phone to English switches the app. Verify: rows in the Go/No-Go.
  qa-140, 5582 at 2b51ae7e, 2026-11-24/25 (`migrely-work/qa-140/t23-26/logs/`, readback DIFFS 0):
  - #274 and #276 (pain) PASS in German;
  - 18 captures: home, attack mode, Log medication, Warning signs, History, onboarding, Your data, paywall and the visit
    picker. Each was taken at 200% and 100% in German and again in English after the phone was switched, with 0 English
    hits in the German dumps (`cap26-check.log`);
  - the app-language page does not resolve on Android 8 (`no-app-lang.log`), as expected.
- [ ] 12.3 Negative controls: v1.3.3 (English-only) on de-DE shows English on the same states (the check sees language);
  the gate's 7.2 controls re-run once on the release SHA. Verify: control rows in the Go/No-Go.
  Not run (pm-de3, 2026-11-25): the lead accepted the GO without it. What stands in for it:
  - the v1.3.3 controls of #274 and #276 failed as expected;
  - the engineer's T01 control (English dumps give hits);
  - the full gate on 2b51ae7e, whose completeness test was shown red by the 7.2 controls on the engineer's tree.
  Residual risk in D195 (21); whether it joins the 1.4.0 release Go/No-Go is QA's and the lead's call.
- [x] 12.4 Record EC-L10N-17's per-app TalkBack voice as observed, a known limit, not a pass.
  Recorded (pm-de3, 2026-11-25) as a known limit, not a pass. With per-app German on an English system, TalkBack keeps
  its English frame and role words around German labels: "Edit box" on the F3 rows (`8.1-walk.md`, eng-de2). QA's GO at
  7b200368 lists it as a known limit. An English quote inside German text is read by the German voice (X-6, `t23d/logs/X6.log`;
  it needs a human listen). See D195 (20) (b) and (21).
- [ ] 12.5 Rows moved from 8.1 and 8.2 (team-lead decision, 2026-11-24), on the release candidate, German with the
  English comparison of design D7 rule 1. Each gets its own Go/No-Go row; none is ticked until QA has walked it:
  - [x] R06, R07, R09, R10 (200% portrait and landscape, 360 dp);
  - [x] R14 in its logged state;
  - [x] R19 in landscape;
  - [x] R22 (200% portrait and landscape, 360 dp);
  - [ ] R26 (200% portrait and landscape, 360 dp): open until QA names the evidence (pm-de3, 2026-11-25). The GO
    counts R26 as PASS, but the only Insights captures in `qa-140/t23*/` are `t23/caps/R10-insights` (p200, l200,
    p130). They show Premium on with the 3, 6 and 12-month chips, and no data: no medication ratings and no bars.
    Medication ratings and bars at 200% and 130% are not shown there;
  - [x] R27 in landscape;
  - [x] R29;
  - [x] N01, N04: N01, the German daily reminder with its expanded text, vis PRIVATE; N04, with the system in de-DE, the
    static shortcut reads "Migräne beginnt";
  - [ ] N02 ("Did it help?", visit, lock-screen "Erinnerung"): moved to the 1.4.0 release Go/No-Go with the expanded
    notifications below. The lock-screen text is covered by `NotificationLockScreenGermanTest` (f9fd99b6), and the
    daily reminder's public version by the engineer's 8.2 row 10;
  - [x] the 130% rows at 360 dp portrait (design D7: the threshold layouts, 115% for attack mode's intensity row):
    the onboarding header, the "Did it help?" card, Reminders with the visit section, the Insights entry, and attack
    mode at 115%. Insights' bars with data go with R26;
  - [x] the 100% rows at 411 dp (R01, R11, R24, N01);
  - [ ] the light-theme sanity screen (design D7 rule 4): moved to the 1.4.0 release Go/No-Go. B-02's captures were
    taken but not judged as a pass; a migraine in progress is always dark;
  - [x] the launcher label and static shortcut with the system in de-DE (EC-L10N-25): N04 above; the launcher label
    stays "Migrely" (`app_name` is not translatable, the engineer's 8.2 rows 3 and 4b);
  - [ ] the expanded "Did it help?" and visit notifications in German, full text readable (design D2, ux E6): moved
    to the 1.4.0 release Go/No-Go. `NotificationExpandedTextTest` (2.3) proves the BigTextStyle on the JVM only.
  Verify: one Go/No-Go row each, with the font scale and size read back before the screenshot, the English comparison,
  and settings restored.
  Record (pm-de3, 2026-11-25): qa-140 walked the ticked rows on 5580 at 2b51ae7e, 2026-11-24, as part of the GO at
  7b200368 (`migrely-work/qa-140/t23/logs/rows.jsonl`, `cap` rows, plan `t23-plan.md`). T01 found 0 English, no "…" and
  no broken word. Readbacks: `t23/logs/readback-end.txt` and `t23-26/logs/readback-end.txt`, DIFFS 0. Filed: #283 (S4,
  History day cells 45 dp at 360 dp; it predates this change). 12.5 stays open for the three moved rows and R26.

## 13. Records and archive (`product-manager`)

- [x] 13.1 Fold review outcomes into D195 (voice kept or switched, medical PASS date, limits), archive the change, check
  `openspec validate --all --strict`. Verify: exit 0; `openspec list` no longer shows the change.
  pm-de3, 2026-11-25: D195 (21) holds the close-out (the voice kept, the review dates and SHAs, no clinician or
  linguist, the known limits, the Go/No-Go carry-overs and what stays unproven); no new D-number. Stale-delta check
  against the live specs at 7b200368 (with T72 and #278 archived): the one MODIFIED requirement (`app-shell`, "Dates and
  times") keeps every live scenario. The ADDED names do not collide. The change was archived with
  `openspec archive add-german-localization --yes`, and `openspec validate --specs --strict` exits 0.
- [ ] 13.2 Hand-offs after merge, not part of this PR (open by design, team-lead acts after the merge): `aso-seo-specialist` drafts the de-DE listing translation and
  German screenshots (`medical-reviewer`, `legal-finance-analyst` check; no German PDF claim); after 1.4.0 is live, a
  separate site change for `/de/` (D148); `product-manager` plans `add-german-doctor-report`. Owner steps via
  `team-lead`: AAB upload, listing translation and screenshots in Play Console, later site deploy. Verify: tracker rows
  exist for each (team-lead).
