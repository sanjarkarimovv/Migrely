# Tasks

Base: `bugfix/161-menu-second-tap` from `origin/develop` c2761cba. There is no domain, presentation or data change: the rule is about touch and lives
in `designsystem`. `Refs #161` in every commit and in the PR, never a closing keyword: QA filed it and retests it.

## 1. Plan

- [x] 1.1 BRD: D164, EC-UX-31, EC-UX-32, EC-UX-30's last sentence, §13 row 33. Verify: `grep -c "^| D164 " docs/BRD.md` prints 1, and
  `git show origin/develop:docs/BRD.md | grep -c "^| D164 "` prints 0.
- [x] 1.2 Proposal, design, delta and tasks. Verify: `openspec validate fix-menu-second-tap --strict` exits 0.
- [ ] 1.3 `ux-designer` confirms there is no visual change, no copy, and the states and a11y rows of design D5. Verify: their packet names this
  change and says either no change or the rows to change.

## 2. The helper (designsystem)

- [x] 2.1 Compose tests first, through `MigrelyDropdownMenu`, with an item over a clickable in the main window, in Robolectric if popup windows work
  there, otherwise `androidTest`; say which. The rows are in design "Test plan", Compose row: under 500 ms; at 500; beyond the distance; burst under
  and over the burst timeout; the item's first tap at once; a semantics click is no lift; tree and bounds as a raw menu; a second tap on the closing
  menu. Verify: `./gradlew :designsystem:testDebugUnitTest --tests '*Menu*'` fails first against a helper that only calls `DropdownMenu`.
  Done: Robolectric, popup windows work there (the menu is found with `isPopup()`, tapped and closed). `MigrelyDropdownMenuTest`, 11 tests, over an
  older full-screen dialog because the main window's side is in `app`; plus `MenuLiftGuardTest` in `app/src/testDebug`, 2 tests, over the real
  `ScreenNavHost` main window. Against a helper that only called `DropdownMenu`: 11 run, 5 failed (every "does nothing" row). The rows that pass on
  both are the "answers" rows, the tree, the first tap, the click action and the closing menu.
- [x] 2.2 Share the lift part of `DialogWaitNode` (design D3), and add `MigrelyDropdownMenu` in package `dialog`, with no wait, no semantics, Initial
  pass. Dialog behaviour does not change. Verify: `./gradlew :designsystem:testDebugUnitTest` passes, the 2.1 tests included, and every existing
  `DialogWait*` and `DialogLift*` test is unchanged in `git diff --stat origin/develop -- designsystem/src/test*`.
  Done: the dialog node takes the wait as a flag (`CardTouchesNode(withWait)`); `Modifier.liftOnCard()` is the lift half alone, used by
  `MigrelyMenus.kt`. `:designsystem:testDebugUnitTest` 141 tests, 0 failed. `git diff --stat origin/develop -- designsystem/src/test*` lists only
  `MigrelyDropdownMenuTest.kt`.
- [x] 2.3 Negative control, local only: remove the lift modifier from the helper, run 2.1, record which tests fail, and restore it. Never commit it.
  Verify: the failing test names are in the packet, and `git status --short` is clean afterwards.
  Done: helper with `modifier = Modifier`: 5 of 11 menu tests fail (under the limit, just under, item took the menu away, burst under, burst over's
  precondition) and `MenuLiftGuardTest`'s under-the-limit row. Also the menu given the dialog wait: 9 of 11 fail, `theItemAnswersItsFirstTapAtOnce`
  among them. Restored; `git status --short` clean.

## 3. UI wiring

- [x] 3.1 `ChoiceChip` in `AttackChoiceSections.kt` calls `MigrelyDropdownMenu` and keeps `DropdownMenuItem`. `menuOpen`, the long press and the
  chip's custom action stay as they are. Verify: `git grep -nP 'material3\.DropdownMenu\b' -- '*.kt'` finds only the helper's file, and
  `./gradlew :features:attack:ui:testDebugUnitTest` passes.
  Done: only `MigrelyMenus.kt` names `material3.DropdownMenu` in production (an untracked file is not in `git grep`, so checked with `grep -r`).
  `:features:attack:ui:testDebugUnitTest` 50 tests, 0 failed.

## 4. The check

- [x] 4.1 A popup test next to `ArchitectureDialogsTest`, design D3's list with `\b`, and a KDoc saying what to do when it fails (D164). Verify:
  `./gradlew :app:testDebugUnitTest --tests '*Architecture*'` passes.
  Done: `ArchitecturePopupsTest`. `:app:testDebugUnitTest --tests '*Architecture*'` 17 tests, 0 failed.
- [x] 4.2 Negative control, local only: add `import androidx.compose.material3.DropdownMenu` to a feature file, run 4.1, see it fail and name that
  file, and restore it. Then the same with `DropdownMenuItem` alone, which must pass. Verify: both results are in the packet, and `git status --short`
  is clean.
  Done: on develop's `AttackChoiceSections.kt`, before the migration: fails and names `AttackChoiceSections.kt`. An `import
  androidx.compose.material3.DropdownMenu` in `SettingsDialogs.kt`: fails and names it. `import androidx.compose.ui.window.Popup`: fails and names
  it. `DropdownMenuItem` and `PopupProperties` imports alone: passes. Each restored; `git status --short` clean.

## 5. Real windows

- [ ] 5.1 As `DialogLiftOnScreenTest`: a press just inside and just outside the distance from a lift on the menu's item, through the popup's real window,
  on `emulator-5570`. Verify: `ANDROID_SERIAL=emulator-5570 nice -n 19 ./gradlew --max-workers=2 :app:connectedDebugAndroidTest
  -Pandroid.testInstrumentationRunnerArguments.class=<the new test>` passes, and fails with the lift modifier removed locally.

## 6. Docs

- [x] 6.1 `docs/ARCHITECTURE.md`: the dialog bullet names `MigrelyDropdownMenu` and the popup check. Verify: `grep -n "MigrelyDropdownMenu"
  docs/ARCHITECTURE.md` prints a line.
  Done: the menu bullet after the D161 bullet, and the module table.

## 7. Gate and walk

- [ ] 7.1 The gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0.
- [ ] 7.2 `:app:checkReleasePermissions`. Verify: it exits 0 and no permission is added.
- [ ] 7.3 Walk on `emulator-5570`, debug: #161's steps at about 150 ms; a tap on the chip after about 700 ms; a single Remove; TalkBack's "Remove from my
  list". Restore every device setting you change. Verify: the results are in the packet, with what a walk does not prove.
- [ ] 7.4 `code-reviewer` reviews the branch. Fixes go on the same branch. Verify: the review packet has no open blocker.

## 8. QA (independent)

- [ ] 8.1 `qa-engineer` runs design "Acceptance" A1 to A10, debug and R8, A2 on a negative-control build of the same commit. Verify: the table is in
  `docs/qa/` with trial counts, measured gaps and what it does not prove, and #161 gets the retest comment.
- [ ] 8.2 The residual rows go into the next release's Go/No-Go: a second tap from 500 ms, a third from 300 ms after the second's lift, and A7's tap
  outside. Verify: they are in that Go/No-Go.

## 9. Close

- [ ] 9.1 Archive the change, then open a PR into `develop` with `Refs #161`, and merge it with a merge commit. Verify: `openspec validate --specs
  --strict` exits 0 after the archive, and `gh pr view` shows it merged.
- [ ] 9.2 `team-lead` adds the hand check on the internal track: double tap Remove in a custom tag's menu, naturally, and note anything that changes
  beneath. Verify: the row is in the release guide.

## Not done here

A second tap from 500 ms, a tap outside the menu, #149, Undo or a confirmation for Remove, keeping the menu across a rotation, helpers for popups
not used today. See the proposal.
