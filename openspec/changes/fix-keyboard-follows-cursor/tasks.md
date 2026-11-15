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

- [ ] 2.1 No change in domain, presentation or data: no use case, ViewModel, repository or DAO is touched. Verify:
  `git diff --stat origin/develop -- '*/domain/*' '*/presentation/*' '*/data/*' datasource` is empty at the end.

## 3. UI tests first (red)

- [ ] 3.1 Robolectric, `features/attack/ui`: Attack details with a dispatched 250 dp `ime` inset and the note focused at 200%
  font. Type until the text passes the keyboard's top; assert that the cursor's rectangle lies inside the scroll's visible area
  above the inset with at least 8 dp. Cases: typing, Tab focus with the cursor at the end, a tap in the first lines (the cursor
  stays where tapped), a paste, and W-AUTOSAVE (tap the middle of a 996-unit note, type, advance the clock past the 600 ms
  autosave, type again: neither the cursor nor the scroll moves). Verify:
  `./gradlew :features:attack:ui:testDebugUnitTest --tests '*NoteKeyboard*'` fails on develop, then passes.
- [ ] 3.2 Robolectric, the same screen: with the inset, Back's bounds equal those without it, and a shown message's top equals
  its top without the inset (D4). Run at `sdk = [26]` and `[35]`. Verify: `--tests '*MessageKeyboard*'` passes, and a local
  mutation that adds `imePadding()` to `MigrelySnackbarHost`'s modifier makes it fail (paste the failing output in the packet,
  then revert).
- [ ] 3.3 Robolectric, `features/medication/ui`: Edit medication at 200% in a landscape-sized window with the inset: the focused
  field and, after scrolling, Save lie above the inset. Verify: `./gradlew :features:medication:ui:testDebugUnitTest --tests
  '*Keyboard*'` fails on develop, then passes.
- [ ] 3.3a Robolectric, `features/settings/ui`: Delete all data (design D7) with a final keyboard inset that leaves room (the row
  rides above it) and one that does not (the row stays at the bottom, the input line shows 8 dp above the keyboard), and an
  animated inset that crosses the threshold midway (the row moves once). Verify: `./gradlew :features:settings:ui:testDebugUnitTest
  --tests '*DeleteKeyboard*'` fails on develop, then passes.
- [ ] 3.4 Robolectric, `features/home/ui`, `features/history/ui` and `features/onboarding/ui`: with the inset, Home Today's day cells and History's list end
  at the same place as without it. Verify: the tests fail on develop (they pad for `ime` today), then pass.
- [ ] 3.5 A test that `MainActivity`'s window has `SOFT_INPUT_ADJUST_RESIZE` (Robolectric reads the merged manifest). Verify:
  `./gradlew :app:testDebugUnitTest --tests '*SoftInput*'` fails on develop, then passes.
- [ ] 3.6 #145 on screens with a field (design D6): prove whether `adjustResize` alone clears the stale `ime` after a copy from
  the toolbar on API 26. First a Robolectric test at `sdk = [26]` if it can model the toolbar dispatch; if it cannot, say so
  and ask `qa-engineer` for a device check on emulator-5582 with the probe of task 1.3 (`migrely-work/qa-145/probe.diff`). If
  the value stays, add the smallest fix that clears it and report it to `product-manager` before section 4 ends. Verify: the
  test's red and green output, or QA's log with `ime` 0 after the keyboard closes on the note, Edit medication and Delete all
  data.

## 4. UI

- [ ] 4.1 `designsystem`: the shared bars inset (design D3), used by `MigrelySnackbarHost`'s fit rule in place of its inline
  union. Verify: `./gradlew :designsystem:testDebugUnitTest` passes, `MessageFitInsetsTest` included.
- [ ] 4.2 Attack details: keyboard padding before `verticalScroll` on the content column, the end spacer adjusted so the bottom is
  the larger of the keyboard and the navigation bar. Add the cursor helper (design D2) only if 3.1 stays red without it.
  Verify: 3.1 and 3.2 pass, and `--tests '*SafetyCard*'` (D167) passes unchanged. If W-AUTOSAVE stays red, fix it where the
  draft and the saved note meet (design D8), not by removing `LimitedField`'s outside-change rule; "Note with a limit" tests
  still pass.
- [ ] 4.3 Edit medication and Log medication: the bottom keyboard inset moves before `verticalScroll`. Verify: 3.3 passes.
- [ ] 4.4 The 11 screens without a field (12 files: `TodayContents.kt`, `AttackModeContents.kt`, `HistoryScreen.kt`,
  `InsightsScreen.kt`, `SettingsScreen.kt`, `SettingsPages.kt`, `PaywallScreen.kt`, `ReportScreen.kt`, `MedicationsScreen.kt`,
  `YourDataScreen.kt`, `RemindersScreen.kt`, `OnboardingScreen.kt`) use the bars inset on the same sides as today. Verify: 3.4
  passes, and
  `git grep -n "safeDrawing" -- 'features/*/ui/src/main' 'designsystem/src/main'` lists only the screens with a field, the
  dialogs and the horizontal or top insets.

- [ ] 4.5 Delete all data: the rule of design D7, decided once per keyboard opening from the final height. If the rule proves
  fragile in the walk, use D7's fallback and tell `product-manager`, who updates the delta. Verify: 3.3a passes.

## 5. Wiring

- [ ] 5.1 `app/src/main/AndroidManifest.xml`: `android:windowSoftInputMode="adjustResize"` on `MainActivity`. Verify: 3.5 passes,
  and `./gradlew :app:checkReleasePermissions` passes with a report equal to 1.2.1's.

## 6. Gate and walk

- [ ] 6.1 The gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`
  exits 0.
- [ ] 6.2 Walk on emulator-5570 (API 36) with the on-screen keyboard: K1 to K4, K6 and K7 from design "Acceptance", over the
  API 36 part of the ux walk matrix (U9: 20 cells with checks C1 to C5, F6 and F7, the 360 dp window, the single rows), with
  TalkBack for K2. Negative controls: K1 without 4.2 and K2 without 5.1 on a local build.
  Restore font scale, rotation, TalkBack and animator scale afterwards. Verify: shots and uiautomator bounds in
  `migrely-work/walks/kbd/`, listed in the packet with what they do not prove.

## 7. Review

- [ ] 7.1 `code-reviewer`, with the reviewer's own D178 (6) warning as a check item: no keyboard inset above the content column,
  and the fit rule reads the shared bars inset. Verify: no open blocker.

## 8. QA

- [ ] 8.1 `qa-engineer` runs K1 to K8 on the PR head, this diff only (owner rule 2026-11-09), with the negative controls named in
  design "Acceptance" and the ux walk matrix (U9), on emulator-5580 (API 36) and emulator-5582 (API 26: F1 four cells, F4 and
  F5 at 200% landscape, K5's COPY steps from the note, Edit medication and Delete all data, with develop ea41869a as the
  negative control). A row that cannot be reached, such as the full-screen editor, is "not reachable", not a pass (ux F6). Verify: a record in
  `docs/qa/fix-keyboard-follows-cursor.md` with counts, SHAs and what the rows do not prove.
- [ ] 8.2 QA retests #141, #83 and #145, and closes those that pass. Verify: the issues' comments.
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
