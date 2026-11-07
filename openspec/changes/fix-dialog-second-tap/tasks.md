# Tasks

Base: `feature/fix-dialog-second-tap` from `origin/develop` (46ccbb70). Independent of `feature/fix-double-tap-writes`, which merges first. No domain,
presentation or data layer change: the rule is about touch and lives in `designsystem`.

## 1. Plan

- [x] 1.1 BRD: D160, EC-UX-27 to 29, §13 row 31. Verify: `grep -c "^| D160 " docs/BRD.md` prints 1, and `git grep -c "^| D160 " origin/develop origin/bugfix/146-paywall-play-failure origin/feature/fix-double-tap-writes -- docs/BRD.md` prints nothing.
- [x] 1.2 Proposal, design, delta, tasks. Verify: `openspec validate fix-dialog-second-tap --strict`.

## 2. The rule

- [ ] 2.1 In `designsystem`, a pure rule, design D2: asked about a press by event time, the time the dialog appeared and the pointer id, with `DIALOG_WAIT_MILLIS = 500L` as the one constant. A press inside the wait stays ignored until its pointer lifts. Verify: `./gradlew :designsystem:compileDebugKotlin`.
- [ ] 2.2 JVM tests first: 0, 100, 499, 500, 501 and 800 ms; a press held across the end; two pointers; before the dialog has appeared. Verify: `./gradlew :designsystem:testDebugUnitTest --tests '*DialogWait*'` passes, and fails first on an empty implementation.

## 3. The helper

- [ ] 3.1 The modifier node, with no semantics, consuming in the initial pass, noting when it appears when it is attached in the dialog's window. `MigrelyAlertDialog`, `MigrelyDatePickerDialog` and `MigrelyDialog`, with the parameters this app uses. During the wait `dismissOnClickOutside` is false and is restored when it ends. `dismissOnBackPress` and `onDismissRequest` are not touched. `MigrelyDialog` keeps a full-screen dialog's properties as they are. Verify: `./gradlew :designsystem:assembleDebug`, and `git diff` shows no semantics modifier and no change of colour or size.
- [ ] 3.2 Compose tests through each wrapper, in Robolectric if dialog windows work there, otherwise `designsystem/src/androidTest` on `emulator-5570`; say which. A press inside the wait does nothing; a press just after answers; a press held across the end does nothing; a click action inside the wait runs; a keyboard activation inside the wait answers; the semantics tree and bounds match a raw dialog. One per kind of control: button, radio row, slider, clock dial, calendar day, text field. For the slider, dial and day, record whether a consumed press still moves it. Text field: a short tap does not focus, and the next does. Verify: the command used exits 0, and the slider, dial and day results are in the packet.
- [ ] 3.3 Outside the card and the date range picker: a press outside during the wait does not close the dialog; one held across the end does not; a tap outside after it does; Back during the wait closes it. The date range picker keeps `SOFT_INPUT_ADJUST_RESIZE` after its wait. Verify: the command of 3.2 exits 0.
- [ ] 3.4 Negative control, local only: remove the modifier and then the outside-tap rule, run 2.2 and 3.2 to 3.3, record which tests fail, and restore. Never commit it. Verify: the failing test names are in the packet, and `git diff --stat` is empty for that change afterwards.

## 4. Wiring

- [ ] 4.1 Every dialog goes through the helper: `EffectDialog`, `AddPainDialog`, `CustomTagDialog`, `ThemeDialog`, `ImportDialog`, `RestoreDialog`, `TimeDialog` in `RemindersScreen.kt`, both dialogs in `MigrelyDateTimePicker.kt`, and `MigrelyDateRangePicker.kt`. Verify: `git grep -nP '\b(AlertDialog|BasicAlertDialog|Dialog|DatePickerDialog|TimePickerDialog|ModalBottomSheet)\s*\(' -- '*.kt' ':!*Test*.kt'` lists only the helper's file. Use `-P`: on this Mac `-E` with `\b` matches nothing. Control: at 46ccbb70 the same command lists 10 calls in 8 files.
- [ ] 4.2 A Konsist test in `app/src/test/java/com/migrely/app/architecture`, `Konsist.scopeFromProduction()`: outside the helper's file, no file imports `androidx.compose.ui.window.Dialog`, or `AlertDialog`, `BasicAlertDialog`, `DatePickerDialog`, `TimePickerDialog` or `ModalBottomSheet` from `androidx.compose.material3`. Verify: `./gradlew :app:testDebugUnitTest --tests '*Architecture*'` passes. Add a raw `AlertDialog` import to one feature locally: it fails and names the file. Restore. Record it in the packet.

## 5. Gate and walk

- [ ] 5.1 Gate. Verify: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0.
- [ ] 5.2 Walk on `emulator-5570`, debug, at #157's worst case: a double tap at about 150 ms on "Restore the copy" in landscape, then Restore after the wait. The Theme row at 100%, and the effect row at 200%. Timing evidence needs the emulator started with `-no-window -gpu swiftshader_indirect`; `-gpu host` on 5570 gives slow frames and does not count as timing evidence. By eye: nothing changes on screen when the wait ends. Restore every setting. Verify: results in the packet, with how the emulator was started and what they do not prove.
- [ ] 5.3 Permissions. Verify: `./gradlew :app:checkReleasePermissions` exits 0.

## 6. Docs

- [ ] 6.1 `docs/ARCHITECTURE.md` section 8: every dialog goes through the helper, why, and that Konsist checks it. Verify: `git grep -n "DIALOG_WAIT\|MigrelyAlertDialog" docs/ARCHITECTURE.md` finds it.
- [ ] 6.2 After the code, re-read `openspec/specs/` for a line this makes false. Verify: `git grep -n -i "dialog" openspec/specs` reviewed, and the result in the packet.
- [ ] 6.3 `product-manager`, when this branch takes `develop` with #156 merged: the `app-shell` scenario "A dialog" from `fix-double-tap-writes` says the path has not been measured. Correct it, keep the BRD in numeric order and re-run validation. Verify: `openspec validate fix-dialog-second-tap --strict` exits 0, and `grep -oE '^\| D1[5-9][0-9]' docs/BRD.md` is in order.

## 7. `qa-engineer`, independently (emulators, debug and R8)

- [ ] 7.1 Build a negative control: the same SHA with only the guard removed, debug and R8, not pushed. Verify: `git diff <sha>` shows only that change, kept with the evidence.
- [ ] 7.2 Rerun `t9_dialog.py`'s rows that wrote in #157 (Restore in landscape; effect at 100%, 200% and landscape; Add pain level at 200%; Theme at 100%, 200% and landscape; Add your own at 100% and 200%), with the guard and without, debug and R8. Asks of 100, 150, 300, 450, 550, 600, 700 and 800 ms, read by measured gap, so that both sides of the wait have real samples. R8 has no database read: read it from the screen. Then a tap just after the wait, on each row. Verify: with the guard no write and no closed dialog inside the wait; without it writes come back on every row; a tap just after the wait answers; the table says at what measured gap writes come back on each row. Restore and any other row writing from a natural gap goes in the report to `team-lead`.
- [ ] 7.3 Outside taps, and what does not wait. The date picker's second tap outside at 100% and 200%. Back during the wait. Rotation and process death with a dialog open. Animator scale 0. TalkBack and switch access on a device: the click action inside the wait; a keyboard. By eye or frame capture: no flicker or change of look when the wait ends. Restore every setting. Verify: results in the report.
- [ ] 7.4 The rows #157 lists as not proved: `MigrelyDateTimePicker` at 200% and in landscape, the time step's OK from a double tap on Next, Reminder time in landscape, and `ImportDialog` after a double tap on a file in the picker. Verify: a table in the report. A write is a new issue and is not called a pass.
- [ ] 7.5 A double tap on a dialog's own button, where the second tap lands on the screen under it: Add on Add pain level, an effect row, a Theme row, and Restore. The issue's gaps, on the guarded build, with the first tap after the wait. `team-lead` asks for this now because the result could change the scope; it stays open only if QA's run does not cover it. Verify: a table of what the second tap landed on and what it did. A write is a new issue and is not called a pass.
- [ ] 7.6 Retest #157 under the reading in the design's Test plan. QA judges the closure, and if it disagrees it brings evidence to `team-lead`. Verify: the issue's closing comment names what was measured and what was not, and the writes after the wait are in the 1.1.0 Go/No-Go as residual risk.

## 8. Human hand, internal track

- [ ] 8.1 `team-lead` adds a row to the owner's hand check in the release guide (`reports/2026-11-06-chiqish-kuni-yoriqnomasi.md`, step 2). On a real phone, by hand: in landscape, double tap the Theme row and an intake's row five times each, as fast as natural. Note anything that changes. Then open each dialog and tap a control as soon as it can be read: it must answer, at worst on the next tap. Restore only if an automatic copy already exists. A dialog that feels dead, or a write from a natural double tap, goes back to `team-lead` before Production. Verify: the row is in the guide, in Uzbek.
- [ ] 8.2 `qa-engineer` puts the result in the Go/No-Go for 1.1.0, with what it does not prove: one person, one phone, few trials. Verify: it is in `docs/qa/`.

## Not done here

- A second tap after the wait, a double tap on a dialog's own button (measured by task 7.5), `DropdownMenu` and dialogs of a leaving screen (#149), #156. See the proposal.
