# Tasks

Base: `bugfix/141-keyboard-follows-cursor` from `origin/develop` 046efe95. `Refs #141`, `Refs #83`, `Refs #145` in every commit
and the PR, never a closing keyword: QA filed all three and closes them after a retest. Ownership: `ux-designer` section 1,
`qa-engineer` 1.3 and section 8, `android-engineer` sections 2 to 6, `code-reviewer` section 7, `product-manager` 1.4 and
section 9.

## 1. Before code

- [x] 1.1 `ux-designer` answers U1 to U9 in `design.md` ("Questions for ux-designer"), with states for 100% and 200% font,
  portrait and landscape, TalkBack and "Remove animations". Verify: a ux packet that answers each U-number with a choice and
  a reason.
  - Record (2026-11-15, product-manager): `migrely-work/t63/ux-keyboard.md` (ux-kbd, 104 lines) answers U1 to U9, each with a
    choice and a reason, plus states S1 to S13 and findings F1 to F7. `team-lead` accepted U1 to U9 as written, including the
    departures U5 (facts corrected: the keyboard covers the message), U7 (short-window rule) and U9 (wider matrix), and
    findings F1 to F4; F5 becomes task 8.3, F6 a QA note, F7 a later note.
- [x] 1.2 `ux-designer` names the walk matrix (U9): each field, font, orientation and API that the engineer and QA must run.
  Verify: the matrix is in the ux packet.
  - Record (2026-11-15): "Walk matrix (U9)" in the same file. Fields F1 to F7. API 36: F1 to F5 at 100% and 200%, portrait
    and landscape (20 cells, checks C1 to C5), F6 and F7 (4 cells), a 360 dp landscape window for F1 and F5 (2 cells). API 26:
    F1 (4 cells), F4 and F5 at 200% landscape. Single rows: TalkBack, messages, Remove animations, RTL, hardware keyboard,
    rotation, background and process death, W-AUTOSAVE, light theme, split screen, floating keyboard, screens without a field
    (onboarding included).
- [x] 1.3 `qa-engineer` measures #145 on emulator-5582 (design D6): `WindowInsets.ime` bottom and `safeDrawing` bottom on Home
  before the note, with the keyboard open, after COPY and Back, and after a restart, on develop 046efe95. Verify: a log with the
  four values, and a verdict "stale ime" or "other cause".
  - Record (2026-11-14, develop ea41869a, no `.kt` or manifest change since 046efe95; debug build plus a local log probe,
    never committed). **Verdict: stale `ime`.** Compose bottoms in px (`ime` / `safeDrawing`), API 26, AOSP keyboard: Home
    before 0 / 126; keyboard open 0 / 126 (the window pans, a fresh root read says 901); toolbar shown 901 / 901; after COPY
    901 / 901; after Back with the keyboard closed **901 / 901** while the window manager and a fresh root read give 126 and
    0; after a restart 0 / 126. Home's "Migraine started" moves from y 1982 to 1207, exactly the keyboard's 775 px; History
    ends at y 1499.
  - Cause: under the resolved `adjustPan`, `WindowInsetsCompat` on API < 30 takes `ime` from the root view's visible insets,
    which it caches only when insets are dispatched. Showing or hiding the text toolbar dispatches; closing the keyboard
    does not. COPY hides the toolbar while the keyboard is still up, so the last dispatch keeps the keyboard. What
    dispatches on the toolbar was not traced.
  - Runs: unmodified build 2/2, probe 2/2 at 100% and 1/1 at 130% (775 px again), from Edit medication 1/1 and Delete all
    data 1/1 (Home stale), History entry 1/1. HOME and return does not clear it; a new activity does. Controls: the same
    steps without the toolbar 6/6 clean (`ime` stays 0); the toolbar shown, then the keyboard closed, then Back closing the
    toolbar clears it (0); API 36 (Gboard) 2/2 clean, `ime` 883 open and 0 after close.
  - For D1 to D3 and K5: D3's bars inset avoids this value on API 26 (`navigationBars` there is capped at the root stable
    bottom), but screens that keep `ime` (D2) also read it, so K5 must run the COPY steps on API 26 from the note, Edit
    medication and Delete all data, with this build as the negative control. Side note: on API 26 with no toolbar,
    Compose never sees the keyboard today, so Delete all data's fixed row stays under it (y 2159).
  - Not proven: a real Android 8 phone, Gboard on API 26, API 27 to 29, CUT, PASTE and SELECT ALL (same toolbar, not run).
    Evidence: `migrely-work/qa-145/` (`MEASUREMENTS.log`, `probe.diff`, per-step dumps and shots).
- [x] 1.4 `product-manager` folds 1.1 to 1.3 into the BRD (D180) and the specs: the margin of U1, the motion of U3, and, if 1.3
  says "other cause", drops the scenario "Android 8 after a copy" and leaves #145 open. Verify:
  `openspec validate fix-keyboard-follows-cursor --strict` exits 0.
  - Record (2026-11-15): 1.3 said "stale ime", so the scenario stays, now run from the note, Edit medication and Delete all
    data, with develop ea41869a as its negative control. BRD: D180 gains (6) results and (7) to (10); EC-UX-39 to EC-UX-42
    rewritten (8 dp, the message exception, the Delete all data rule, #145 confirmed, onboarding); new EC-UX-44 (autosave);
    row 48 updated. Deltas: attack-details gains "A message may lie over the cursor line" and "An autosave does not move the
    cursor or the page"; app-shell gains "Delete all data with the keyboard open" (replacing the old scenario, ux F3), the
    single-line field rule, onboarding (ux F2) and the API 26 copy rows; in-app-messages states that the keyboard may cover a
    message's lower part. Design: D2, D5, D6 rewritten, D7 and D8 added, the ux table replaced by the accepted answers, K1 and
    K3 to K6 widened. `openspec validate fix-keyboard-follows-cursor --strict` exits 0.

## 2. Domain, presentation and data

- [x] 2.1 No change in domain, presentation or data: no use case, ViewModel, repository or DAO is touched. Verify:
  `git diff --stat origin/develop -- '*/domain/*' '*/presentation/*' '*/data/*' datasource` is empty at the end.
  - Record (2026-11-15, android-engineer, HEAD 24109541): the command prints nothing (0 lines).

## 3. UI tests first (red)

- [x] 3.1 Robolectric, `features/attack/ui`: Attack details with a dispatched 250 dp `ime` inset and the note focused at 200%
  font. Type until the text passes the keyboard's top; assert that the cursor's rectangle lies inside the scroll's visible area
  above the inset with at least 8 dp. Cases: typing, Tab focus with the cursor at the end, a tap in the first lines (the cursor
  stays where tapped), a paste, and W-AUTOSAVE (tap the middle of a 996-unit note, type, advance the clock past the 600 ms
  autosave, type again: neither the cursor nor the scroll moves). Verify:
  `./gradlew :features:attack:ui:testDebugUnitTest --tests '*NoteKeyboard*'` fails on develop, then passes.
  - Record (2026-11-15): `NoteKeyboardTest`, 8 cases at 200% font, 250 dp `ime`, native graphics: typing past the
    keyboard, typing inside a long note, focus with the cursor at the end (Tab or TalkBack), a tap in the first lines, a
    paste, the cursor moved down and up, W-AUTOSAVE. Develop's code: **7/8 red** (cursor line at 521..564 dp with the
    keyboard's top at 450; after a tap on line 1 the page went to the note's end, line 1 at -1235 dp). D2 padding alone:
    7/8 red. The cursor helper alone: 5/8 red. Both: **8/8 green**. W-AUTOSAVE was green on develop too, so D8 needs no
    code; "Note with a limit" (`NoteSectionTest`, 8) stays green. Found on the way: Compose's text field brings into view
    the cursor's place from before the tap that focused it (b/216790855), which with `opensAtEnd` sent the page to a long
    note's end (ux U4); the helper drops the field's own requests. Evidence: `3.1-red-develop.txt`,
    `3.1-mutation-no-padding.txt` in `migrely-work/walks/kbd/`.

- [x] 3.2 Robolectric, the same screen: with the inset, Back's bounds equal those without it, and a shown message's top equals
  its top without the inset (D4). Run at `sdk = [26]` and `[35]`. Verify: `--tests '*MessageKeyboard*'` passes, and a local
  mutation that adds `imePadding()` to `MigrelySnackbarHost`'s modifier makes it fail (paste the failing output in the packet,
  then revert).
  - Record (2026-11-15): `MessageKeyboardTest` on Attack details at 200% in an 800x360 dp window with "storage full", a
    250 dp keyboard, sdk 26 and 35: the message's top, Back's bounds and "not over Back" unchanged, and a control that
    the content ends at the keyboard's top. Green 2/2. Mutation `imePadding()` on `MigrelySnackbarHost`'s modifier: **both
    fail**, "the message moved expected:<76.0> but was:<19.0>"; reverted. `MessageFitInsetsTest` 2/2 green. On sdk 26
    the keyboard is modelled as Android 8 delivers it under `adjustResize`: inside the system window insets the root
    dispatches (a first version that dispatched below the decor never reached Compose and passed vacuously; the control
    now guards that). Evidence: `3.2-mutation-imePadding.txt`.

- [x] 3.3 Robolectric, `features/medication/ui`: Edit medication at 200% in a landscape-sized window with the inset: the focused
  field and, after scrolling, Save lie above the inset. Verify: `./gradlew :features:medication:ui:testDebugUnitTest --tests
  '*Keyboard*'` fails on develop, then passes.
  - Record (2026-11-15): `EditMedicationKeyboardTest` (3): name and dose at 200% landscape 891x411 dp (input line 8 dp above
    the keyboard, Save reachable by scrolling), dose at 100% portrait (whole field with its supporting line above the
    keyboard). Develop: **3/3 red** (Save at 411 dp with the keyboard's top at 161; dose line at 354). Padding back inside
    the scroll: 3/3 red. Padding fixed but fields unwrapped: 1/3 red (a tap left the dose 3 dp clipped, and Compose's
    resize tracking then skips it, so it stayed under the keyboard). With `SingleLineFieldInView`: **3/3 green**.
    Evidence: `3.3-*.txt`.

- [x] 3.3a Robolectric, `features/settings/ui`: Delete all data (design D7) with a final keyboard inset that leaves room (the row
  rides above it) and one that does not (the row stays at the bottom, the input line shows 8 dp above the keyboard), and an
  animated inset that crosses the threshold midway (the row moves once). Verify: `./gradlew :features:settings:ui:testDebugUnitTest
  --tests '*DeleteKeyboard*'` fails on develop, then passes.
  - Record (2026-11-15): `DeleteKeyboardTest` (6) at 200%: room in portrait (row above the keyboard, input line 8 dp above
    the row); short 800x360 dp window with a 200 dp keyboard (row stays, input line above the keyboard; after closing,
    field and row together); two real Android 11+ keyboard animations driven through `dispatchWindowInsetsAnimation*`
    (no room: the row never moves while opening or closing; room: the row rides up with the keyboard, never down).
    Develop: **3/6 red** (row 296 -> 96 dp). Green **6/6**. Mutation: deciding from the final height without holding the
    choice while the keyboard is up fails the closing case (row 146 -> 296 dp). Evidence: `3.3a-*.txt`.

- [x] 3.4 Robolectric, `features/home/ui`, `features/history/ui` and `features/onboarding/ui`: with the inset, Home Today's day cells and History's list end
  at the same place as without it. Verify: the tests fail on develop (they pad for `ime` today), then pass.
  - Record (2026-11-15): `HomeKeyboardTest`, `HistoryKeyboardTest` (portrait, landscape), `OnboardingKeyboardTest` (200%
    landscape, where the step scrolls, with a control box that does pad for the keyboard: a first portrait version
    passed on develop because the step was shorter than the window minus the keyboard). Develop **4/4 red** ("Migraine
    started" 752 -> 502 dp, History's list 891 -> 641 dp, onboarding's step 411 -> 161 dp), green **4/4**. Evidence:
    `3.4-red-develop.txt`, `3.4-green.txt`.

- [x] 3.5 A test that `MainActivity`'s window has `SOFT_INPUT_ADJUST_RESIZE` (Robolectric reads the merged manifest). Verify:
  `./gradlew :app:testDebugUnitTest --tests '*SoftInput*'` fails on develop, then passes.
  - Record (2026-11-15): `MainActivitySoftInputTest` (in `app/src/testDebug`, where Robolectric is): red without the
    attribute, "expected:<16> but was:<0>"; green with it. Evidence: `3.5-red-green.txt`.

- [x] 3.6 #145 on screens with a field (design D6): prove whether `adjustResize` alone clears the stale `ime` after a copy from
  the toolbar on API 26. First a Robolectric test at `sdk = [26]` if it can model the toolbar dispatch; if it cannot, say so
  and ask `qa-engineer` for a device check on emulator-5582 with the probe of task 1.3 (`migrely-work/qa-145/probe.diff`). If
  the value stays, add the smallest fix that clears it and report it to `product-manager` before section 4 ends. Verify: the
  test's red and green output, or QA's log with `ime` 0 after the keyboard closes on the note, Edit medication and Delete all
  data.
  - Record (2026-11-15, android-engineer): **not modellable in Robolectric; a QA device check is needed.** The open
    question is whether Android 8 dispatches window insets when the keyboard closes under `adjustResize`, which the
    framework and the window manager decide; Robolectric's root dispatches only what the test injects, so a test
    would prove the test, not Android. What is proved: once a dispatch carries a keyboard-free system window inset,
    Compose's `ime` is 0 (the sdk 26 cases of 3.2 open and close the keyboard this way). Reading of AOSP 8: under
    `adjustResize` the window manager shrinks the content frame for the keyboard, so its content insets change on both
    open and close and `ViewRootImpl.performTraversals` dispatches; under `adjustPan` only the visible insets change,
    which never dispatch, which is QA's 1.3 finding. So `adjustResize` is expected to clear it, but this is not
    proven. No extra fix was added. Asked of `qa-engineer` (via `team-lead`): on emulator-5582 with the probe of 1.3,
    the COPY steps from the note, Edit medication and Delete all data on this branch's head; pass is `ime` 0 after the
    keyboard closes on each, with develop ea41869a as the negative control.
  - Record (2026-11-15, qa-engineer qa-kbd36, relayed by `team-lead`): **proven on a device, no extra fix needed.**
    emulator-5582 (API 26, AOSP LatinIME, 100% font), 51e22355 plus the probe of 1.3; APK sha256
    aba291f0f91762a93fde0e4bf5e053dc3b8e7cd234d12a61bb7b283760008688, manifest `adjustResize`. Compose `ime` back to 0
    after the keyboard closed in 9/9 runs on the head: note COPY 3/3, Edit medication COPY 2/2, Delete all data COPY
    2/2, History -> details COPY -> History 2/2, the no-toolbar control 1/1. Home's "Migraine started" stayed at y 1982
    (baseline) with 14 day cells; History ended at 2274. Control develop ea41869a with the probe, same boot: stale in
    3/3 valid runs (`ime` 901, Home y 1207, 0 day cells). Evidence: `migrely-work/qa-kbd-36/MEASUREMENTS.log`. Not
    checked: 130% font and landscape (QA 8.x), Gboard on API 26, other makers' Android 8 builds.

## 4. UI

- [x] 4.1 `designsystem`: the shared bars inset (design D3), used by `MigrelySnackbarHost`'s fit rule in place of its inline
  union. Verify: `./gradlew :designsystem:testDebugUnitTest` passes, `MessageFitInsetsTest` included.
  - Record (2026-11-15): `designsystem/layout/MigrelyInsets.kt` (`bars`, `barsAndKeyboard`); the host pads and fits by
    `MigrelyInsets.bars`. `:designsystem:testDebugUnitTest` green, `MessageFitInsetsTest` included.

- [x] 4.2 Attack details: keyboard padding before `verticalScroll` on the content column, the end spacer adjusted so the bottom is
  the larger of the keyboard and the navigation bar. Add the cursor helper (design D2) only if 3.1 stays red without it.
  Verify: 3.1 and 3.2 pass, and `--tests '*SafetyCard*'` (D167) passes unchanged. If W-AUTOSAVE stays red, fix it where the
  draft and the saved note meet (design D8), not by removing `LimitedField`'s outside-change rule; "Note with a limit" tests
  still pass.
  - Record (2026-11-15): the bottom `barsAndKeyboard` inset goes before the safety card's viewport and the scroll; the
    navigation-bar spacer is gone. The helper was needed (3.1 stays red without it): `designsystem/input/
    CursorFollowingTextField.kt`, drawn as Material's outlined field (`BasicTextField` + `OutlinedTextFieldDefaults.
    DecorationBox`, as Material builds it), whose cursor line ±8 dp is brought into view on focus, selection, layout and
    keyboard height changes, and which drops the field's own requests. Motion is the scroll's default bring-into-view;
    "Remove animations" makes it a jump through Compose's motion scale. 3.1, 3.2, `SafetyCard*` (7) and the other
    attack UI tests green (92).

- [x] 4.3 Edit medication and Log medication: the bottom keyboard inset moves before `verticalScroll`. Verify: 3.3 passes.
  - Record (2026-11-15): both screens moved the inset; their single-line fields are wrapped in `designsystem/input/
    SingleLineFieldInView.kt`, which asks for the whole field, the input line with its supporting line, and the input
    line ±8 dp at once, so the scroll shows the largest that fits (U1). 3.3 green.

- [x] 4.4 The 11 screens without a field (12 files: `TodayContents.kt`, `AttackModeContents.kt`, `HistoryScreen.kt`,
  `InsightsScreen.kt`, `SettingsScreen.kt`, `SettingsPages.kt`, `PaywallScreen.kt`, `ReportScreen.kt`, `MedicationsScreen.kt`,
  `YourDataScreen.kt`, `RemindersScreen.kt`, `OnboardingScreen.kt`) use the bars inset on the same sides as today. Verify: 3.4
  passes, and
  `git grep -n "safeDrawing" -- 'features/*/ui/src/main' 'designsystem/src/main'` lists only the screens with a field, the
  dialogs and the horizontal or top insets.
  - Record (2026-11-15): the 12 files use `MigrelyInsets.bars` on the same sides. 3.4 green; 359 UI tests in the ten
    touched modules green. `grep -rn safeDrawing features/*/ui/src/main designsystem/src/main` now lists only
    `AddPastAttackScreen.kt` (top bar and horizontal, no field) and the two date pickers (dialog windows). Note: `git grep` with the pathspec as written in this task lists nothing, as its `*`
    does not match the module paths; plain `grep -r` was used.

- [x] 4.5 Delete all data: the rule of design D7, decided once per keyboard opening from the final height. If the rule proves
  fragile in the walk, use D7's fallback and tell `product-manager`, who updates the delta. Verify: 3.3a passes.
  - Record (2026-11-15): implemented as D7 says, not the fallback: the rule needs the input line plus 8 dp above and below
    (`inputLineRoom`, the same room the field asks for, slightly stricter than "plus 8 dp" so the line never shows
    clipped), from `imeAnimationTarget`, held while a keyboard is up. 3.3a green. Walk: see 6.2.

## 5. Wiring

- [x] 5.1 `app/src/main/AndroidManifest.xml`: `android:windowSoftInputMode="adjustResize"` on `MainActivity`. Verify: 3.5 passes,
  and `./gradlew :app:checkReleasePermissions` passes with a report equal to 1.2.1's.
  - Record (2026-11-15): done first (commit c65120ae); 3.5 green. `:app:checkReleasePermissions` on 24109541 exits 0;
    the merged release manifest has `windowSoftInputMode="adjustResize"` on `MainActivity` and no INTERNET permission
    (the word appears only in two comments).

## 6. Gate and walk

- [x] 6.1 The gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`
  exits 0.
  - Record (2026-11-15): on 241095410aa2 (load 3.4), `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug
    test ktlintCheck detekt lint` -> **BUILD SUCCESSFUL in 4m, exit 0**. Unit-test results present after it: JVM `test`
    1348, `testDebugUnitTest` 1150, `testReleaseUnitTest` 702, 0 failures, 0 errors, 0 skipped. Log:
    `migrely-work/walks/kbd/gate-24109541.log`. Later commits on the branch touch only this file.

- [ ] 6.2 Walk on emulator-5570 (API 36) with the on-screen keyboard: K1 to K4, K6 and K7 from design "Acceptance", over the
  API 36 part of the ux walk matrix (U9: 20 cells with checks C1 to C5, F6 and F7, the 360 dp window, the single rows), with
  TalkBack for K2. Negative controls: K1 without 4.2 and K2 without 5.1 on a local build.
  Restore font scale, rotation, TalkBack and animator scale afterwards. Verify: shots and uiautomator bounds in
  `migrely-work/walks/kbd/`, listed in the packet with what they do not prove.
  - Record (2026-11-15, emulator-5570, API 36, Gboard, debug build of 24109541; keyboard top 1517 px in portrait and
    394 px in landscape, where Gboard takes 686 of 1080 px). **Partial: 10 cells of the 20-cell matrix.** C1 to C5 as
    in the ux matrix; px at 420 dpi.

    | Cell | C1 line above keyboard | C2 Back | C3 | C4 below | C5 close |
    |---|---|---|---|---|---|
    | F1 100% portrait, typing from empty past the keyboard | pass, ~11 dp | first tap leaves; button node unchanged; icon node 181-244 -> 181-233 (finding 1) | pass | n/a | pass |
    | F1 200% portrait, mid-note and at the end of an 850-unit note | pass, ~14 dp both | 194-257 open and closed | pass | n/a | pass, no blank |
    | F1 200% landscape, mid-note | line whole, margin under 8 dp: 52 dp between bar and keyboard ("the tightest window") | 129-192 open and closed | pass | n/a | pass |
    | F1 100% landscape, mid-note | pass, ~11 dp | 116-179 open and closed | pass | n/a | pass |
    | F2 name 200% landscape | pass | 116-179 | pass | not run | not run |
    | F3 dose 200% landscape | pass | 116-179 | pass | Save scrolls to 252-347, keyboard open | not run |
    | F4 Log medication 200% landscape | pass, ~18 dp, label gives way first | 116-179 | pass | "Add and log" scrolls to 271-366 | not run |
    | F5 200% landscape (D7, no room) | pass | 116-179 | pass | row stays 870-975 behind the keyboard | field 231-475 and row together |
    | F5 100% portrait (D7, room) | pass | 181-244 | pass | row rides at 1402-1465 | not run |

    Negative controls: develop ea41869a (local debug build): Back `[0,0][0,0]` with the keyboard open on the note (K2,
    #83), and at 200% the 12 words typed at the note's end all under the keyboard (K1, #141). This commit without
    `adjustResize` (K2 as written): Back stays at 181-241 and the window does not pan, because with D2 the cursor line
    is already in view and Android has nothing to pan to; on API 36 this control cannot fail, so develop is the
    control. The attribute is guarded by 3.5 and matters on API 26 (#145, QA).
    Findings: (1) with the keyboard open, uiautomator reports Back's icon node 11 px shorter (181-233 against 181-244)
    on Attack details at 100%; the 48 dp button node keeps [22,160][127,265] and the first tap leaves the screen. It
    looks like Compose's uncovered-bounds rule with a content node above; TalkBack was not run, not checked further.
    (2) A walk script once sent arrows and Space to a non-text focus, which pressed "Delete migraine" on the walk's
    own migraine (created at 10:50 the same day); no other data was touched (tags, points, day marks counted). The
    script now refuses to send keys unless the note has focus.
    Not walked here: the other 10 cells of the 20, F6, F7, the 360 dp window, TalkBack, the message rows, Remove
    animations, RTL, hardware keyboard, rotation with the keyboard open, background and process death, W-AUTOSAVE and
    the paste on a device (both covered by 3.1), light theme, split screen, floating keyboard, screens without a field
    on a device (covered by 3.4). Device restored: font 1.0, rotation 0 with auto-rotate on, animator 1, no
    accessibility service, the walk's medication and intake removed (0 medications, 0 intakes, 0 migraines, as before).
    Evidence: `migrely-work/walks/kbd/` (`f1-*` to `f5-*`, `k1-*`, `dev-*`, `k2neg-*`: `.png` and `.xml`; APKs of
    24109541, develop ea41869a and the K2 negative).

## 7. Review

- [ ] 7.1 `code-reviewer`, with the reviewer's own D178 (6) warning as a check item: no keyboard inset above the content column,
  and the fit rule reads the shared bars inset. Verify: no open blocker.
- [ ] 7.2 `android-engineer` fixes the review's F1 (and with it F2) and takes F6. Verify: a red test per helper on 51e22355,
  green after; the gate on the new SHA; a short walk on emulator-5570.
  - Record (2026-11-15, android-engineer, f05814f2): both helpers asked for their line on every keyboard height change,
    closing included. New `designsystem/input/KeyboardFollow.kt`: a layout in which the keyboard did not move records
    whether the line is whole in view (clipped by the scroll), so a scroll away, by hand or by the D167 reveal, ends the
    following; a layout in which the keyboard moved keeps the record from before, as Compose's resize tracking does,
    without its clip gap (the line, not the whole field, is what must show). Only a growing keyboard asks, and only
    while following. Focus, an edit and a field size change still ask as before (U1, D7 unchanged). F6: the keyboard
    height is read in `snapshotFlow`, so its frames no longer recompose the note or the medication screens (Delete all
    data still reads it in composition for D7's padding). The message host is untouched (D178 (6)).
  - Red on 51e22355's helpers, green on f05814f2: `NoteKeyboardTest` "Closing the keyboard after scrolling away from the
    note leaves the page there" (pulled back 3957 px); F2 model "A keyboard that closes and opens again keeps the top
    of the page in view" (top left by 4207 px; it uses a programmatic scroll to the top in place of the real reveal);
    `EditMedicationKeyboardTest` "Closing the keyboard after scrolling to Save does not scroll back to the name" (8 px
    against the page's new end, 106 px: with Save at the page's end the larger area may settle it down, U6). Mutation
    "ask on every growth": the F2 model fails again (4207 px). Evidence: `7.2-red-51e22355.txt`,
    `7.2-mutation-grow-only.txt`.
  - Gate on f05814f2: `BUILD SUCCESSFUL in 1m 48s`, exit 0; JVM 1348, debug 1153, release 702 tests, 0 failures, 0
    skipped. `:app:checkReleasePermissions` exit 0. Logs: `gate-f05814f2.log`, `perms-f05814f2.log`.
  - Walk: **not run, blocked by the host.** emulator-5570 did not boot in 6 starts between 11:52 and 12:05 (`-gpu
    host` twice, `swiftshader_indirect`, cold boot without the snapshot, normal priority, and once more after
    emulator-5582 went off): each time all four QEMU vCPU threads hung within 20 s at 0% CPU ("detected a hanging
    thread"), then the process exited or stayed hung. emulator-5560 and another project's headless Pixel_6 (about 80 to
    100% CPU, up 4 days) were running; host load 3.7 to 5.6. Logs: `emu5570.log`, `emu5570-2.log` to `emu5570-6.log`.
    5570 is off, its settings untouched, no test data written. Still to walk on a device: F1 on the note and on Edit
    medication at 100% portrait, and F2 (safety card after a first pain 10 with the note focused, also with Remove
    animations); `away.sh` in the same folder is ready for F1, with the focus guard.

- [x] 7.3 `product-manager` rules on the plan changes made during build and review, and makes plan and code agree (design D2,
  D6, D7, K2, Risks; deltas `app-shell` and `attack-details`; proposal; BRD D180). Verify: `openspec validate
  fix-keyboard-follows-cursor --strict` passes.
  - Record (2026-11-15, product-manager pm-t66, on 60cb208f, code final at f05814f2):
    - (a) `SingleLineFieldInView` with U1's priority, in place of "single-line fields keep Compose's own behaviour":
      **accepted.** Evidence 3.3: without it 1/3 red (dose 3 dp clipped stayed under the keyboard); with it 3/3 green. It is
      U1 as written. New `app-shell` scenario "A field 3 dp under the scroll's edge".
    - (b) the note drops Compose's own bring-into-view requests (b/216790855, U4): **accepted.** Without it a tap in the first
      lines sent the page to a long note's end (3.1). The reviewer confirmed TalkBack scrolls through `ScrollBy`, so it is
      unaffected; QA's TalkBack row (K2 and the "TalkBack moves on from the note" scenario) still checks it on a device.
    - (c) D7 needs the input line with 8 dp above and below: **accepted.** Stricter than "plus 8 dp", the same room the field
      asks for, so the line never shows clipped; it can only move the row behind the keyboard in a few more windows, where
      the type, Done, Delete flow already applies. Spec and BRD D180 (8) reworded.
    - (d) follow the keyboard only while the line was in view; closing never scrolls; only a growing keyboard asks
      (`KeyboardFollow.kt`, review 7.1 F1, F2): **accepted.** It makes U6 ("the page stays where it is") exact and removes
      two regressions (3957 px pull-back on close, the safety card left on reopen). Requirement "A focused field stays above
      the keyboard" and "Closing the keyboard and other keyboards" reworded; new scenarios "Closing after scrolling away",
      "The keyboard grows after scrolling away", and in `attack-details` "The safety card with the keyboard open". The F2
      test uses a programmatic scroll, so the real reveal is QA row 8.1 F2.
    - F3 (review 7.1): the `app-shell` scenario "Without the change" and design K2 claimed that this commit without
      `adjustResize` shows Back at `[0,0][0,0]`. False on API 36: Back stays at [43,181][106,241] (`walks/kbd/k2neg-typed.xml`),
      because with D2 Android has nothing to pan to. Reworded as "The checks can fail": develop ea41869a on API 36 (Back
      `[0,0][0,0]`, `walks/kbd/dev-typed.xml`), test 3.5, and the API 26 copy rows (head 9/9 clear, develop 3/3 stale,
      `qa-kbd-36/MEASUREMENTS.log`).

- [x] 7.4 F1 composing word (QA 8.1 finding 1, NO-GO on 76e5afcd): `android-engineer` makes the keyboard ending the word it
  composes leave the page where the person scrolled it. Verify: a red test on 76e5afcd that ends a composition with text and
  cursor unchanged after a scroll away, green after; a mutation that turns it red again; the gate on the new SHA. The device
  rerun of row F1 is QA's (8.1).
  - Record (2026-11-15, android-engineer, code 2b24b915): cause confirmed. `CursorFollowingTextField` keyed its
    bring-into-view effect on the text layout. The keyboard's composing underline is part of that layout, so ending the word
    (on Back) gave a new layout with the same text and cursor, the effect ran again and brought the line back without asking
    `KeyboardFollow`. The effect now runs on focus, a text change and a cursor move only, and waits for that edit's own layout
    before it reads the line. `SingleLineFieldInView` runs on focus and a size change, which ending a word does not cause: no
    change there. `KeyboardFollow`, D7 and the message host are untouched.
  - Red on 76e5afcd, green on 2b24b915: `NoteKeyboardTest` "Closing the keyboard mid-word after scrolling away leaves the page
    there", driven through the field's real `InputConnection` (`setComposingText("once")`, scroll to the top,
    `finishComposingText()`, close). Red: pulled back 4035 px right at `finishComposingText()`, with the keyboard still open
    (13 tests, 1 failed); green 13/13. Guards, green before and after: "Typing again after scrolling away" (the keyboard
    composes on) and "Moving the cursor after scrolling away" bring the line back above the keyboard. Mutations: the layout
    key back in the effect: red again, 4035 px; a focus-only trigger: 7 of 13 red (both guards among them); dropping the
    wait for the edit's layout survives under Robolectric, where the effect starts after the frame's layout, so the wait
    guards only the device ordering. Logs: `walks/kbd/7.4-red-76e5afcd.log`, `7.4-green.log`, `7.4-mutation-*.log`.
  - Gate on 2b24b915: `BUILD SUCCESSFUL in 2m 27s`, exit 0; JVM 1348, debug 1156, release 702 tests, 0 failures, 0 skipped.
    `:app:checkReleasePermissions` exit 0; no INTERNET in the merged release manifest. Logs: `gate-2b24b915.log`,
    `perms-2b24b915.log`.
  - Walk: **not run, blocked by the host.** emulator-5570 hung twice at boot (14:07 and 14:08, `-memory 2048 -no-snapshot`,
    the second with `-cores 2`; qemu "detected a hanging thread" on every vCPU, crash reports
    `qemu-system-aarch64-headless-2026-11-15-140828.ips` and `-140922.ips`), with swap at 8.3 of 9.2 GB. 5570 is off and
    unchanged. `walks/kbd/away.sh` now takes the text to type (default `aura%sonce`, composing; `aura%sonce%s` committed) and
    deletes stale dumps before each read.
  - QA observation 2 (U4, page up 234 px at 100% as the keyboard opens): not settled. The fix removes one possible path (a
    keyboard that sets a composing region around the tapped word after the tap), but the likelier cause, Compose's own
    tracking of the focused field when the viewport shrinks, is untouched. QA's F1 rerun can read it at the same time.

- [x] 7.5 Review finding 1 on c3e2368e (S4, test gap): no test failed with `value.text` dropped from the effect's keys, as
  every edit in `NoteKeyboardTest` also moved the cursor. Verify: a test with a same-length rewrite, red under that
  mutation, green on the code.
  - Record (2026-11-15, android-engineer): `NoteKeyboardTest` "A word the keyboard rewrites in place after scrolling away
    brings the line back": after `composeThenScrollAway("once")`, `setComposingText("ones")` keeps the cursor and must bring
    the line above the keyboard. Mutation `LaunchedEffect(focused, value.selection)`: 14 tests, 1 failed, this one ("line
    4434.0..4477.0, keyboard at 450.0"; `walks/kbd/7.5-mutation-text-key.log`). Code restored: 14/14, ktlint and detekt
    of the module clean (`7.5-green.log`). No product code changed.

## 8. QA

- [ ] 8.1 `qa-engineer` runs K1 to K8 on the PR head, this diff only (owner rule 2026-11-09), with the negative controls named in
  design "Acceptance" and the ux walk matrix (U9), on emulator-5580 (API 36) and emulator-5582 (API 26: F1 four cells, F4 and
  F5 at 200% landscape, K5's COPY steps from the note, Edit medication and Delete all data, with develop ea41869a as the
  negative control). A row that cannot be reached, such as the full-screen editor, is "not reachable", not a pass (ux F6). Verify: a record in
  `docs/qa/fix-keyboard-follows-cursor.md` with counts, SHAs and what the rows do not prove.
  Rows added by 7.3 for the 7.2 change (API 36, emulator-5580; 7.2's own walk did not run):
  - **F1:** the keyboard open on the note, then on Edit medication's name, at 100% portrait and 200% landscape; scroll the
    page away from the field by hand, then close the keyboard (Back, hide key). Pass: the page stays where it was left (top
    node's bounds equal before and after closing), no blank space. Then reopen the keyboard by a tap in the field: its line
    shows 8 dp above the keyboard. Negative control: the 51e22355 build pulls the page back to the field on close.
  - **F2:** the note focused with the keyboard up, scroll up to Pain, add a first 10, dismiss the dialog, with and without
    "Remove animations". Pass: the D167 safety card comes into view and stays in view (its bounds inside the viewport after
    1 s), the page does not return to the note, the card's TalkBack reading is D167's. Negative control: the 51e22355 build
    (expected to pull the page from the card; if it does not reproduce on a device, record that, and the row then rests on
    the mutation test of 7.2).
- [x] 8.2 QA retests #141, #83 and #145, and closes those that pass. Verify: the issues' comments.
  - Record (2026-11-15, QA, `develop` d3d98673, QA's debug build sha256 1af5b0cf…ff8067 read back from emulator-5582,
    API 26, AOSP keyboard): all three pass and are closed with a comment each. #141 typing past the keyboard 5/5, the
    composing last word 3/3 and the committed one 2/2 (control ea41869a 1/1 under the keyboard); #83 Back and title on
    screen with the keyboard open and a first tap leaves 5/5 (control 1/1 `[0,0][0,0]`); #145 after COPY, Home and
    History whole at 130% 2/2 and landscape 2/2 (control 2/2 stale). The API 36 rows stay deferred to the
    release/1.3.0 Go/No-Go as a blocking line (`team-lead`, 2026-11-15); an issue is reopened if they fail there.
    Details: `docs/qa/fix-keyboard-follows-cursor.md`, "Task 8.2".
- [ ] 8.3 ux F5, separate from this change: on a device, Edit medication with the keyboard open and TalkBack on, Save with a name
  that gives an error; does the error line stay off screen and silent? Only if it reproduces, file an issue with severity and
  steps. Verify: a yes or no with evidence in the QA record, and the issue number if one was filed.

## 9. Close

- [ ] 9.1 BRD D180 updated with the results (what was proved, what stays open), `docs/QA.md` or the release notes untouched
  unless `team-lead` asks. Verify: `grep -c "^| D180 " docs/BRD.md` prints 1.
- [ ] 9.2 Archive and validate. Verify: `openspec archive fix-keyboard-follows-cursor --yes` and
  `openspec validate --specs --strict` exits 0.
- [ ] 9.3 PR into `develop` with `Refs #141`, `Refs #83`, `Refs #145`, merge commit, the worktree removed, tracker T63 updated by
  `team-lead`. Verify: `git log --oneline -1 origin/develop` shows the merge.
