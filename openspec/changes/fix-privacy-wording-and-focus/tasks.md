# Tasks

Base: `bugfix/217-privacy-wording-and-focus` from `origin/develop` 40536f49 (the plan), merged with `develop` after D180
before any code (task 0.1). Write `Refs #217` in every commit and in the PR, never a closing keyword: QA filed #217 and
closes it after a retest.

Ownership:

- `team-lead`: 0.2 and 7.3;
- `android-engineer`: 0.1, sections 1 to 6, 7.2, 8.1 to 8.3 and 8.6;
- `qa-engineer`: 7.1 and 8.5;
- `code-reviewer`: 8.4;
- `product-manager`: 7.4.

Every Verify must be able to fail. Where a task says "red first", record the failing run on the pre-merge base (or on
40536f49 when the item is untouched by D180) before the fix, with its count. Use `nice -n 19 ./gradlew --max-workers=2`
under load. Never `--stop` the daemon.

## 0. Before code

- [x] 0.1 Wait until `fix-keyboard-follows-cursor` (D180) is merged into `develop`. Then `git fetch origin && git merge
  origin/develop` into this branch.
  - Resolve `docs/BRD.md` by keeping both sides in numeric order: D180 before D181, EC-UX-39…44 before EC-UX-45…51, §13
    row 48 before 49.
  - Verify:
    - `git merge-base --is-ancestor origin/develop HEAD; echo $?` prints 0;
    - `git log origin/develop --oneline | grep -c "keyboard-follows-cursor"` is at least 1;
    - `grep -c -E '^\| D18[01] ' docs/BRD.md` prints 2;
    - `openspec validate fix-privacy-wording-and-focus --strict` passes.
- [x] 0.2 `team-lead` records the owner's approval of the 5 privacy strings, as legal-t66 asked ("Decision needed from
  the owner"), including whether the phone-transfer additions stay. This gates the merge (8.6), not the coding.
  - Verify: a dated approval line here. Without one, 8.6 does not run.
  - Owner approved in chat 2026-11-16 (team-lead). The approved strings are the 5 as built, phone-transfer additions
    included (recorded by product-manager, 7.4).

## 1. Domain, presentation and data

- [ ] 1.1 None. No use case, view model, repository, data source, DI or navigation change.
  - Verify: at the end, `git diff origin/develop --stat -- '*/domain/*' '*/presentation/*' '*/data/*' '*/datasource/*'
    '*/di/*'` prints nothing.

## 2. UI: privacy wording (D181 (1), EC-STO-11, EC-STO-12)

- [x] 2.1 Red first: add a Compose test in `features/settings/ui/src/testDebug`.
  - It renders `PrivacyContents` and asserts 7 text nodes in order.
  - It compares the first four with legal's literal strings, written in the test, not read from `R.string`.
  - It asserts that the last node is the policy link with its address.
  - Verify: `./gradlew :features:settings:ui:testDebugUnitTest --tests '*Privacy*'` fails before 2.2 (old text, 6
    stops) and passes after 2.3.
- [x] 2.2 Strings in `features/settings/ui/src/main/res/values/strings.xml`:
  - replace `settings_privacy_offline`, `settings_privacy_phone` and `settings_privacy_files` with D181 (1)'s text,
    verbatim, apostrophes escaped as `\'`;
  - add `settings_privacy_purchase`.
  - In `features/backup/ui/src/main/res/values/strings.xml`, replace `your_data_note`.
  - Verify:
    - `git grep -n -i -E "internet access|sends nothing|never leaves|nothing leaves|100% offline" --
      '*/res/values*/*.xml' | wc -l` prints 0 (on 40536f49: 2);
    - `git grep -c -F "Premium is bought through Google Play, which goes online for the purchase." --
      features/settings/ui/src/main/res/values/strings.xml` prints 1.
- [x] 2.3 `PrivacyContents` (`SettingsPages.kt`): add `Paragraph(stringResource(R.string.settings_privacy_purchase))`
  right after `_offline`. No heading.
  - Verify: the 2.1 test passes.
- [x] 2.4 Red first: a test on Your data in `features/backup/ui/src/testDebug` asserts the note against legal's literal
  text.
  - Verify: `./gradlew :features:backup:ui:testDebugUnitTest --tests '*YourData*'` fails before 2.2 and passes after.
- [ ] 2.5 TalkBack and 200% check (device, emulator-5570, API 36).
  - TalkBack reads the Privacy page in 7 stops, in order, each once.
  - At 200% on a 360 dp window, portrait and landscape, every paragraph and the policy address can be scrolled into
    view.
  - Restore font scale and rotation afterwards.
  - Verify: a filtered TalkBack `action=SPEAK` log with 7 lines in order, and 2 screenshots in
    `migrely-work/walks/t66/`.

## 3. UI: one spoken name per control (D181 (2), EC-ONB-07, EC-UX-45, #217)

- [x] 3.1 Sweep. List every clickable in `*/src/main/*.kt` that sets `contentDescription` through `semantics` while a
  child `Text` stays in its semantics.
  - Include ux's sites: `MigrelySnackbar.kt:225`, `AttackModeContents.kt:293` and `:340`, `DoctorVisitSection.kt:81`,
    `RemindersScreen.kt:551`.
  - Include ux's check list: `AttackPainSection.kt:202`, `AttackMedicationsSection.kt:123`, `AttackTimeSection.kt:208`,
    `DoctorVisitSection.kt:120`, `RemindersScreen.kt:424`, `PaywallTrigger.kt:45`.
  - Add `AttackChoiceSections.kt:242`.
  - Line numbers are from 40536f49; re-find them after 0.1.
  - Verify: the list is recorded here, one line per site, with its visible text and spoken label, and the grep command
    used.
  - Recorded 2026-11-15 on d12d1ef0 (android-engineer). Grep: `git grep -n -E 'semantics.*contentDescription|contentDescription
    = (spoken|label|description|it\b)' -- '*/src/main/*.kt'`, then each hit read by hand. Format: site, visible text ->
    spoken label, red result.
    - `MigrelySnackbar.kt:221` `SnackbarEndControl`: "Undo" -> "Undo the start" (and every `*_undo_*_description`): read twice, fixed.
    - `OnboardingScreen.kt:214` `StepHeader`: "Skip" -> "Skip this step": read twice, fixed (#217).
    - `AttackModeContents.kt:294` Details: "Details" -> "Details of this migraine": read twice, fixed.
    - `AttackModeContents.kt:341` chips: "15 min", "30 min", "1 h", "2 h" -> "Move the start … earlier": read twice, fixed.
    - `DoctorVisitSection.kt:81` Remove: "Remove" -> "Remove the next visit": read twice, fixed.
    - `RemindersScreen.kt:550` Open settings (channel notice): "Open settings" -> "Open notification settings": read
      twice and label not in name; fixed, string per 3.5.
    - `AttackPainSection.kt:202` pain dialog time: "At 14:30" -> "Change the time, now 14:30": read twice and label not
      in name; fixed, string "At %1$s, change the time" by ux-designer (2026-11-15).
    - `AttackTimeSection.kt:208` Started and Ended rows: "Started" + date -> "Change when it started, now …": read
      twice, fixed (the in-progress Ended row has no description and no click: unchanged).
    - `AttackChoiceSections.kt:242` "+ Your own": "+ Your own" -> "Add your own symptom" or "… trigger": read twice,
      fixed.
    - `TodayContents.kt:335` Period chip: "Period" -> "Period today" (description sat on the child Text): read twice,
      fixed; description moved to the chip, selected state kept.
    - `AttackMedicationsSection.kt:123` medication row, `DoctorVisitSection.kt:120` visit row, `RemindersScreen.kt:423`
      time row: texts already cleared, no double reading, unchanged (tests pass before and after).
    - `PaywallTrigger.kt:45`, `MedicationPromptCard.kt:196`, `MigrelyDatePicker.kt:176`, progress bars: not clickable,
      out of scope. Surface rows (Today, History, Medications, Log medication), `MonthSkyCard`, `MonthSkyCalendar`
      days: inner texts already cleared.
- [x] 3.2 Red first: one Compose test per listed site, on the merged tree.
  - The control's node has `ContentDescription == [spoken label]` and no `SemanticsProperties.Text`.
  - The spoken label contains the visible label, case-insensitive and in order.
  - Verify: the module test runs fail on the pre-fix base for each site that reads twice. Record the count per module.
    A site whose test already passes is recorded as "no double reading" and is not changed.
- [x] 3.3 #217: in `StepHeader` (`OnboardingScreen.kt`), keep the description on the `TextButton` and add
  `Modifier.clearAndSetSemantics {}` to the child `Text`.
  - Verify:
    - `./gradlew :features:onboarding:ui:testDebugUnitTest` passes, including the 3.2 test, which failed before;
    - `uiautomator dump` on step 1 shows the Skip node with content-desc "Skip this step" and no child text node.
- [x] 3.4 Apply the same fix at each site that 3.2 proved: `SnackbarEndControl`, attack mode "Details" and the "Started
  earlier?" chips, the visit's "Remove", "Open settings", and any further site from 3.1.
  - Do not use `clearAndSetSemantics` on the clickable itself.
  - Verify: every 3.2 test passes. `./gradlew :designsystem:testDebugUnitTest :features:home:ui:testDebugUnitTest
    :features:reminders:ui:testDebugUnitTest :features:attack:ui:testDebugUnitTest` exits 0.
- [x] 3.5 `reminders_open_settings_description` becomes "Open settings for notifications". Update
  `DoctorVisitScreenTest.kt:217`.
  - Verify:
    - `git grep -n "Open notification settings" -- '*/src/*'` prints nothing;
    - the 3.2 label-in-name test for "Open settings" failed with the old string and passes now.
- [ ] 3.6 TalkBack check (device, API 36).
  - Onboarding steps 1 to 4 read "Skip this step, Button" with no "Skip" after it.
  - Home attack mode: "Undo the start, Button", "Details of this migraine, Button", "Move the start 15 min earlier,
    Button".
  - Reminders: "Remove the next visit, Button", "Open settings for notifications, Button".
  - Verify: filtered `action=SPEAK` lines per control, saved in `migrely-work/walks/t66/`. The negative control is the
    same log on a 40536f49 build, which shows "Skip this step. Skip. Button".

## 4. UI: keyboard order on Home and History (D181 (3)–(5), EC-UX-46, EC-UX-47, EC-UX-48)

- [ ] 4.1 History proof, before any History code.
  - Setup: emulator-5570 on the pre-fix base. Choose a day, Tab to the first control of its section, then Shift+Tab with
    `adb shell input keycombination KEYCODE_SHIFT_LEFT KEYCODE_TAB`.
  - After each key, record the focused node's class and bounds from `uiautomator dump` (`focused="true"`) and whether
    they lie inside the viewport.
  - Cells: 100% and 200%, a day with and without migraines, Period shown and hidden.
  - If no accessibility node reports focus, log the Compose focused rect with a debug-only probe that is never
    committed.
  - Verify: the evidence file `migrely-work/walks/t66/history-stop.txt` names the node. This task records which of
    design 3's causes (a), (b) or another it is. The fix in 4.3 targets that node.
- [ ] 4.2 Red first: a History keyboard test in `features/history/ui/src/testDebug`.
  - For each first-control variant (migraine row, Period, Pain-free with Period hidden), Shift+Tab lands on the chosen
    day.
  - The day's bounds lie inside the viewport at font scale 2.0.
  - Tab from the day returns to the control.
  - Verify: `./gradlew :features:history:ui:testDebugUnitTest --tests '*Keyboard*'` fails on the pre-fix base, in the way
    4.1 recorded.
  - 2026-11-15, android-engineer: `HistoryKeyboardOrderTest` (4 cases: migraine row, Period, Pain-free with Period
    hidden at 200%, Period at 100%; 360x640 dp) passes on the pre-fix code in Robolectric: Shift+Tab reaches the chosen
    day, which was off-screen before (y -141 and -93 dp) and is fully in view after. It can fail: without the
    calendar's `onEnter` all 4 land on today instead. So the JVM does not reproduce the stop; 4.1's device proof was
    not run (emulator-5570 died on both boots, logs in `migrely-work/walks/t66/emu-boot*.log`). Not ticked: 4.1, 4.2's
    red, 4.3. No History code changed. The test stays as a guard.
- [ ] 4.3 Fix the cause 4.1 proved (design 3). No group, scroll container, legend or spacer may be a stop.
  - Verify: 4.2 passes. `./gradlew :features:history:ui:testDebugUnitTest :designsystem:testDebugUnitTest` exits 0.
  - Device: the ring is visible on the chosen day after Shift+Tab, at 100% and 200%, portrait and landscape (2 panes).
    Save screenshots.
  - 2026-11-16, product-manager (7.4, review F3, team-lead ruling): no History code changed and none is claimed. 4.1 ran
    on API 26 by QA (qa-t66): 0 stops in 22 cells, 11 on head f846c899 and 11 on `develop` (migraine row, Period,
    Pain-free with Period hidden; 100% and 200%; portrait and landscape; tap and arrows+Enter). `HistoryKeyboardOrderTest`
    stays as a guard, green before and after, so 4.2 has no red. API 36 has not run: it is a blocking line in the
    release/1.3.0 Go/No-Go. If the stop reproduces there, a follow-up change does 4.1 to 4.3 for the node it finds.
    4.1 to 4.3 stay unticked; BRD D181 (3), EC-UX-46, §13 row 49 and the `attack-history` delta now say this.
- [x] 4.4 Red first: Home tests (next to `HomeMessageOrderTest.kt`).
  - (a) All slots empty and a message shown: Tab ×3 goes Settings, message control, "Migraine started"; Shift+Tab ×2
    goes message control, Settings.
  - (b) The same in a short window, where the end column's first control comes first.
  - (c) Regression: one focusable slot after Settings, as today.
  - Verify: `./gradlew :features:home:ui:testDebugUnitTest --tests '*MessageOrder*'`. (a) and (b) fail on the pre-fix
    base. (c) passes before and after.
- [x] 4.5 Move the Shift+Tab redirect to the first control after Settings in Tab order (design 4). Rewrite the content
  `onExit` comment in `TodayKeyboardOrder.kt` so it states what the code does (follow-up 4).
  - Verify:
    - 4.4 (a) to (c) pass, and every existing Home keyboard test passes;
    - negative control: removing the new `previous` link makes (a) fail;
    - code-reviewer confirms the comment in 8.4.
- [x] 4.6 Red first: a quiet-links test with the row at a fixed 360 dp.
  - At font scale 2.0 the two links share x, differ in y, each is at least 48 dp tall and has the full row width.
  - At 1.0 they share y.
  - The Tab order is "Add a past migraine", then "Log medication", in both layouts.
  - Verify: `./gradlew :features:home:ui:testDebugUnitTest --tests '*QuietLinks*'`. The 2.0 case fails on the pre-fix
    base.
- [ ] 4.7 `QuietLinks` measures each label's min intrinsic width and stacks when either value plus 24 dp exceeds half
  the row (design 5). `lastModifier` stays on "Log medication".
  - Verify:
    - 4.6 passes;
    - screenshots at 360 dp 200% in portrait and in a short landscape window show whole words;
    - at 100% the bounds are within 1 px of the pre-fix base (uiautomator).

## 5. UI: three screens (D181 (6), EC-UX-49, EC-UX-50, EC-UX-51)

- [x] 5.1 Red first: order tests for Edit medication (`features/medication/ui/src/testDebug`).
  - Add with "For an attack": the full order and its reverse.
  - "Preventive" chosen under focus.
  - The down arrow does not select.
  - Tab in Name types no tab.
  - With a message.
  - Save pending clears focus.
  - Not found: Back only.
  - Save with a duplicate name in keyboard mode moves focus to Name; invalid dose moves it to Usual dose.
  - Save with a duplicate name in touch mode moves nothing.
  - Verify: `./gradlew :features:medication:ui:testDebugUnitTest --tests '*EditMedication*Order*'`. The focus-move tests
    fail on the pre-fix base; record which order tests already pass. Negative control: dropping one stop from an
    expected list fails that test.
- [x] 5.2 Red first: order tests for Log medication.
  - The list, first use, logged, and the dark room (64 dp) with the same order.
  - Logging by Enter leaves nothing focused, and the next Tab goes Back, Done, Undo.
  - Taken at's picker returns focus to Taken at.
  - Verify: `./gradlew :features:medication:ui:testDebugUnitTest --tests '*LogMedication*Order*'`. The logged-clears test
    fails on the pre-fix base if focus moves today; record the result either way. Negative control as in 5.1.
- [x] 5.3 Red first: order tests for Add past migraine (`features/attack/ui/src/testDebug`).
  - The draft and the overlap.
  - "Change the times" puts focus on Started, with nothing saved or merged.
  - Ended's picker returns focus to Ended.
  - With a message.
  - Verify: `./gradlew :features:attack:ui:testDebugUnitTest --tests '*PastAttack*Order*'`. The "Change the times" test
    fails on the pre-fix base. Negative control as in 5.1.
- [x] 5.4 Implement what 5.1 to 5.3 proved missing (design 6):
  - focus requesters on Name and Usual dose, used only in keyboard input mode;
  - clear focus when a focused Save turns disabled;
  - clear focus after a keyboard log;
  - focus to Started after "Change the times";
  - pickers return focus to their row, where Compose does not already.
  - Verify: 5.1 to 5.3 pass. `./gradlew :features:medication:ui:testDebugUnitTest :features:attack:ui:testDebugUnitTest`
    exits 0.
- [ ] 5.5 Device walk at 200% portrait with a hardware keyboard on each of the three screens. Include a touch Save with a
  duplicate name (no soft keyboard opens) and a TalkBack pass on the Name error.
  - Verify: the walk notes in `migrely-work/walks/t66/three-screens.md` list each stop seen, and they match the tests.

## 6. Wiring

- [ ] 6.1 None. No DI, navigation or manifest change.
  - Verify: `git diff origin/develop --stat -- '*/AndroidManifest.xml' '*/navigation/*'` prints nothing, and
    `./gradlew :app:checkReleasePermissions` passes with the same list as `develop`.

## 7. Docs and other owners

- [ ] 7.1 `qa-engineer`: `.maestro/a11y-tour/settings-privacy.yaml:13` asserts the new first paragraph, `visible: "Migrely
  has no internet permission, so the app itself cannot send anything over the internet\\..*"`.
  - Verify: the flow passes on this branch's debug build, and fails on a 40536f49 build (negative control).
- [x] 7.2 KDoc: `TodayKeyboardOrder` and `QuietLinks` describe the new behaviour with BRD D181. The `StepHeader` and
  `SnackbarEndControl` comments say why the child text is cleared.
  - Verify: `git grep -n "D181" -- '*.kt' | wc -l` prints at least 3.
- [ ] 7.3 `team-lead` adds tracker rows for:
  - `aso-seo-specialist`: re-capture slide 06 as `06-private-v2` from the 1.3.0 build, shown on Play only after 1.3.0
    is live;
  - `aso-seo-specialist`: the `ACCESS_NETWORK_STATE` line in `docs/release/data-safety.md`, and the wording "the app
    has no internet access" in `docs/release/content-rating.md:18`;
  - backlog: the disabled Pain-free switch is not a keyboard stop (ux A3);
  - `qa-engineer`: Android 8 backup behaviour with `bmgr` on API 26 (legal-t66, not blocking).
  - Verify: the four rows exist in `tracker/tasks.js` (grep their ids).
- [ ] 7.4 `product-manager`: after the code, re-read the BRD and the deltas against what was built. Fix any drift
  before the archive.
  - Verify: `openspec validate fix-privacy-wording-and-focus --strict` passes on the final head.
  - 2026-11-16, product-manager on f846c899: re-read against the code. Changed: History (D181 (3), EC-UX-46, row 49,
    `attack-history`, proposal, design 3) no longer claims a fix; the pain dialog's "At %1$s, change the time" is in
    D181 (2), EC-UX-45 and an `app-shell` scenario; design 4 to 6 describe what was built (Settings' `onEnter`, the
    measured `Layout`, focus parking, the NotSaved count, pickers needing no code); proposal's QA task number is 7.1;
    owner approval recorded (0.2). QA's 8.5 on f846c899 (`qa/217-acceptance` 34190925): GO for API 26, API 36
    pending, #229 S4 and #230 S3 pre-existing. Team-lead decision: the API 36 rows (2.5, 3.6, 8.3, History and Home
    with Gboard, the 5.5 TalkBack pass on the Name error, History on API 36) are a blocking line in the release/1.3.0
    Go/No-Go; design "What these checks do not prove" lists them. 8.5's own note is QA's, on its branch. Strings checked against the deltas:
    `reminders_open_settings_description` "Open settings for notifications", `pain_dialog_change_time` "At %1$s,
    change the time", and the 5 privacy strings verbatim. Validate passed on this commit; rerun on the final head.

## 8. Verify

- [ ] 8.1 Gate: `./gradlew assembleDebug test ktlintCheck detekt lint`.
  - Verify: exit 0, with the log's last lines recorded. Use `set -o pipefail` with any pipe.
- [ ] 8.2 Instrumented tests in the touched modules that have `androidTest` (`designsystem`, `features:attack:ui`,
  `features:medication:ui`) on emulator-5570.
  - Verify: `ANDROID_SERIAL=emulator-5570 ./gradlew :designsystem:connectedDebugAndroidTest
    :features:attack:ui:connectedDebugAndroidTest :features:medication:ui:connectedDebugAndroidTest` exits 0.
- [ ] 8.3 Device walk (engineer): 2.5, 3.6, 4.3, 4.7 and 5.5 are done, in dark and light themes. All device settings are
  restored: font scale, rotation, TalkBack, animator scale.
  - Verify: the walk index in `migrely-work/walks/t66/` and `adb shell settings get system font_scale` printing 1.0.
- [ ] 8.4 `code-reviewer` reviews the diff from the merge base to the head. This includes the follow-up 4 comment, that
  no clickable lost its role or action, and that no test reads the string it checks.
  - Verify: a review packet with no open blocker and its SHA range recorded here.
- [x] 8.4a Review fixes (`android-engineer`, cr-t66 on 78eb13ed).
  - F2 (S3): Edit medication's focus after a Save problem is keyed on `pending` too, so a second Save with the same
    problem brings focus back to Name or Usual dose. Red on 78eb13ed: 2 of 13 in `EditMedicationKeyboardOrderTest`
    (focus null after the second press); green after: medication 88 of 88.
  - F6 (S4): a test that the in-progress Ended row keeps "Ended" and "In progress" and is not a control. Green on
    78eb13ed (a guard); a one-line mutation that always clears the row's texts fails it (1 of 5).
  - F2 round 2 (cr-t66 on 24066a1e): keying on `pending` failed when the result came in the press's own frame
    (InvalidName, InvalidDose). The screen now counts NotSaved results (`problemArrivals`) and the effect is keyed on
    that count; the pending and touch-only guards stay. Red on 24066a1e: 2 of 15 (instant second Save, name and dose:
    focus null); green after: medication 90 of 90.
  - F4 (backlog T68) and F5 (#224) are out of this change, by team-lead.
- [ ] 8.5 `qa-engineer` gives Go/No-Go on the head SHA, limited to what the diff reaches, with negative controls on
  40536f49.
  - Scope: TalkBack logs for #217 and the sites from 3.2 on API 36 and 26, the Privacy page, keyboard walks on History,
    Home and the three screens, and the 7.1 Maestro flow.
  - Verify: a QA report in `docs/qa/fix-privacy-wording-and-focus.md` with a GO and no open S1 or S2.
- [ ] 8.6 Archive and merge after 0.2 and 8.5. Run `openspec archive fix-privacy-wording-and-focus`, then open a PR into
  `develop` (`Refs #217`) and self-merge with a merge commit.
  - Verify: `openspec list` no longer shows the change, `openspec validate --specs --strict` passes, and the PR is
    merged with a merge commit.
