# Tasks

Base: `bugfix/141-keyboard-follows-cursor` from `origin/develop` 046efe95. `Refs #141`, `Refs #83`, `Refs #145` in every commit
and the PR, never a closing keyword: QA filed all three and closes them after a retest. Ownership: `ux-designer` section 1,
`qa-engineer` 1.3 and section 8, `android-engineer` sections 2 to 6, `code-reviewer` section 7, `product-manager` 1.4 and
section 9.

## 1. Before code

- [ ] 1.1 `ux-designer` answers U1 to U9 in `design.md` ("Questions for ux-designer"), with states for 100% and 200% font,
  portrait and landscape, TalkBack and "Remove animations". Verify: a ux packet that answers each U-number with a choice and
  a reason.
- [ ] 1.2 `ux-designer` names the walk matrix (U9): each field, font, orientation and API that the engineer and QA must run.
  Verify: the matrix is in the ux packet.
- [ ] 1.3 `qa-engineer` measures #145 on emulator-5582 (design D6): `WindowInsets.ime` bottom and `safeDrawing` bottom on Home
  before the note, with the keyboard open, after COPY and Back, and after a restart, on develop 046efe95. Verify: a log with the
  four values, and a verdict "stale ime" or "other cause".
- [ ] 1.4 `product-manager` folds 1.1 to 1.3 into the BRD (D180) and the specs: the margin of U1, the motion of U3, and, if 1.3
  says "other cause", drops the scenario "Android 8 after a copy" and leaves #145 open. Verify:
  `openspec validate fix-keyboard-follows-cursor --strict` exits 0.

## 2. Domain, presentation and data

- [ ] 2.1 No change in domain, presentation or data: no use case, ViewModel, repository or DAO is touched. Verify:
  `git diff --stat origin/develop -- '*/domain/*' '*/presentation/*' '*/data/*' datasource` is empty at the end.

## 3. UI tests first (red)

- [ ] 3.1 Robolectric, `features/attack/ui`: Attack details with a dispatched 250 dp `ime` inset and the note focused at 200%
  font. Type until the text passes the keyboard's top; assert that the cursor's rectangle lies inside the scroll's visible area
  above the inset. Cases: typing, Tab focus with the cursor at the end, a tap in the first lines, a paste. Verify:
  `./gradlew :features:attack:ui:testDebugUnitTest --tests '*NoteKeyboard*'` fails on develop, then passes.
- [ ] 3.2 Robolectric, the same screen: with the inset, Back's bounds equal those without it, and a shown message's top equals
  its top without the inset (D4). Run at `sdk = [26]` and `[35]`. Verify: `--tests '*MessageKeyboard*'` passes, and a local
  mutation that adds `imePadding()` to `MigrelySnackbarHost`'s modifier makes it fail (paste the failing output in the packet,
  then revert).
- [ ] 3.3 Robolectric, `features/medication/ui`: Edit medication at 200% in a landscape-sized window with the inset: the focused
  field and, after scrolling, Save lie above the inset. Verify: `./gradlew :features:medication:ui:testDebugUnitTest --tests
  '*Keyboard*'` fails on develop, then passes.
- [ ] 3.4 Robolectric, `features/home/ui` and `features/history/ui`: with the inset, Home Today's day cells and History's list end
  at the same place as without it. Verify: the two tests fail on develop (they pad for `ime` today), then pass.
- [ ] 3.5 A test that `MainActivity`'s window has `SOFT_INPUT_ADJUST_RESIZE` (Robolectric reads the merged manifest). Verify:
  `./gradlew :app:testDebugUnitTest --tests '*SoftInput*'` fails on develop, then passes.

## 4. UI

- [ ] 4.1 `designsystem`: the shared bars inset (design D3), used by `MigrelySnackbarHost`'s fit rule in place of its inline
  union. Verify: `./gradlew :designsystem:testDebugUnitTest` passes, `MessageFitInsetsTest` included.
- [ ] 4.2 Attack details: keyboard padding before `verticalScroll` on the content column, the end spacer adjusted so the bottom is
  the larger of the keyboard and the navigation bar. Add the cursor helper (design D2) only if 3.1 stays red without it.
  Verify: 3.1 and 3.2 pass, and `--tests '*SafetyCard*'` (D167) passes unchanged.
- [ ] 4.3 Edit medication and Log medication: the bottom keyboard inset moves before `verticalScroll`. Verify: 3.3 passes.
- [ ] 4.4 The 10 screens without a field (11 files: `TodayContents.kt`, `AttackModeContents.kt`, `HistoryScreen.kt`,
  `InsightsScreen.kt`, `SettingsScreen.kt`, `SettingsPages.kt`, `PaywallScreen.kt`, `ReportScreen.kt`, `MedicationsScreen.kt`,
  `YourDataScreen.kt`, `RemindersScreen.kt`) use the bars inset on the same sides as today. Delete all data unchanged unless the
  walk says otherwise. Verify: 3.4 passes, and
  `git grep -n "safeDrawing" -- 'features/*/ui/src/main' 'designsystem/src/main'` lists only the screens with a field, the
  dialogs and the horizontal or top insets.

## 5. Wiring

- [ ] 5.1 `app/src/main/AndroidManifest.xml`: `android:windowSoftInputMode="adjustResize"` on `MainActivity`. Verify: 3.5 passes,
  and `./gradlew :app:checkReleasePermissions` passes with a report equal to 1.2.1's.

## 6. Gate and walk

- [ ] 6.1 The gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`
  exits 0.
- [ ] 6.2 Walk on emulator-5570 (API 36) with the on-screen keyboard: K1 to K4, K6 and K7 from design "Acceptance" at 100% and
  200%, portrait and landscape, with TalkBack for K2. Negative controls: K1 without 4.2 and K2 without 5.1 on a local build.
  Restore font scale, rotation, TalkBack and animator scale afterwards. Verify: shots and uiautomator bounds in
  `migrely-work/walks/kbd/`, listed in the packet with what they do not prove.

## 7. Review

- [ ] 7.1 `code-reviewer`, with the reviewer's own D178 (6) warning as a check item: no keyboard inset above the content column,
  and the fit rule reads the shared bars inset. Verify: no open blocker.

## 8. QA

- [ ] 8.1 `qa-engineer` runs K1 to K8 on the PR head, this diff only (owner rule 2026-11-09), with the negative controls named in
  design "Acceptance", on emulator-5580 (API 36) and, for K1, K2 and K5, emulator-5582 (API 26). Verify: a record in
  `docs/qa/fix-keyboard-follows-cursor.md` with counts, SHAs and what the rows do not prove.
- [ ] 8.2 QA retests #141, #83 and, if 1.3 found a stale `ime`, #145, and closes those that pass. Verify: the issues' comments.

## 9. Close

- [ ] 9.1 BRD D180 updated with the results (what was proved, what stays open), `docs/QA.md` or the release notes untouched
  unless `team-lead` asks. Verify: `grep -c "^| D180 " docs/BRD.md` prints 1.
- [ ] 9.2 Archive and validate. Verify: `openspec archive fix-keyboard-follows-cursor --yes` and
  `openspec validate --specs --strict` exits 0.
- [ ] 9.3 PR into `develop` with `Refs #141`, `Refs #83`, `Refs #145`, merge commit, the worktree removed, tracker T63 updated by
  `team-lead`. Verify: `git log --oneline -1 origin/develop` shows the merge.
