# Tasks

Base: `feature/fix-double-tap-writes` from `origin/bugfix/142-leaving-screen-input` (2f800c71). No domain, presentation or data
layer change: the rule is about touch and lives in `:app`.

## 1. Plan

- [x] 1.1 BRD: D159, EC-UX-24 to 26, a note on D155, §13 row 30. Verify: `grep -c "^| D159 " docs/BRD.md` prints 1, and `git grep -c "^| D159 " origin/develop origin/bugfix/146-paywall-play-failure -- docs/BRD.md` prints nothing.
- [x] 1.2 Proposal, design, delta, tasks. Verify: `openspec validate fix-double-tap-writes --strict`.

## 2. The rule

- [x] 2.1 A pure class in `app/src/main/java/com/migrely/app/ui/main`, design D2: told about lifts, asked about presses, given the event time, the position, the timeout, the distance and whether the entry was touched. An ignored press stays ignored until its pointer lifts. Its lift updates the last lift and does not mark the entry. Verify: compiles with `./gradlew :app:compileDebugKotlin`.
- [x] 2.2 JVM tests first: gaps 0, 39, 40, 60, 100, 150, 299, 300, 301, 700 ms; under and over the distance; a new entry and a touched one; a burst of three, then a pause; no lift yet; two pointers. Verify: `./gradlew :app:testDebugUnitTest --tests '*SecondTap*'` passes, and fails first on an empty implementation.

## 3. UI and wiring

- [x] 3.1 `TouchesOnlyWhile.kt`: the node asks the class in the initial pass and consumes what it refuses. Consumption and hit testing stay as they are. No semantics. Verify: `git diff` shows no semantics modifier, and `./gradlew :app:testDebugUnitTest --tests '*LeavingScreenInputTest*'` passes or each failure is listed in the packet with its reason (design, Test plan).
- [x] 3.2 `ScreenDestination.kt`: one instance for the nav host; `screen()` passes the entry id, `LocalViewConfiguration.current.doubleTapTimeoutMillis` and `ViewConfiguration.get(context).scaledDoubleTapSlop`. No literal 300 or 100. Verify: `git grep -nP "\b(300|100)L?\b" -- app/src/main/java/com/migrely/app/ui/main/ScreenDestination.kt app/src/main/java/com/migrely/app/ui/main/TouchesOnlyWhile.kt` prints nothing, including in comments. Use `-P`: on this Mac `-E` with `\b` matches nothing whatever the file holds. Control: the same pattern on `features/home/ui/src/main/java/com/migrely/app/home/ui/view/AttackModeContents.kt` prints line 281 (`decorationHeight = 100`) at 2f800c71. Also `git grep -c "screen<" -- app/src/main/java/com/migrely/app/ui/main/AppNavHost.kt` still prints 20.
- [x] 3.3 Compose tests beside `LeavingScreenInputTest`, real `screen()` and nav host, a counter on a control under the finger. Done at 9ec33829: a second tap at 100, 150 and 299 ms does nothing; at 300 and 500 ms it counts; beyond the distance at 100 ms it counts; a touched screen after Back takes it as before; a screen with no lift before takes a press at once; a held press and a drag; the next gesture answers; `performSemanticsAction(OnClick)` at 50 ms runs; the semantics tree with and without the guard. Still to add, from review: the distance at xxhdpi, 90 dp ignored and 110 dp answers, so a wrong unit fails (M4); a screen that opens itself just after a tap, the press does nothing and the next answers; a return to a screen never touched, one press can be ignored; "Nothing to see", tree and bounds the same before and after, and a fake haptic records nothing (sound is read from the code, not run); "Another finger" through the real node, beyond the distance while the ignored press is down, it answers; repeated tapping, every tap ignored under the timeout, and the next after a pause answers. Verify: `./gradlew :app:testDebugUnitTest --tests '*DoubleTap*'` passes, and the xxhdpi test fails if the distance is read as dp.
- [x] 3.4 Negative control, local only: remove the node's call to the class, run 3.3, record which tests fail, and restore. Never commit it. Verify: the failing test names are in the packet, and `git diff --stat` is empty for that change afterwards.
- [x] 3.5 Review M1: the mark that an entry was touched survives activity recreation and process death, on the entry's own `SavedStateHandle` (design D3). The last lift and the ignored pointers are not saved. Update the sentence in `docs/ARCHITECTURE.md` section 8 that says the mark is not saved. Test with real state restoration (`StateRestorationTester` or a recreated activity, through the real `screen()` and nav host): tap to leave a screen, restore, tap Back, press under the timeout and within the distance, and the press answers. The test also shows that the entry's id survives. A second test, `aNewEntryOfTheSameDestinationAfterAPopHasNoMark`: a new entry of the same destination after a pop has no mark, so its second tap is ignored. Verify: `./gradlew :app:testDebugUnitTest --tests '*DoubleTap*'` passes, and the restoration test fails with the marks back in `remember`.
## 4. Docs

- [x] 4.1 `docs/ARCHITECTURE.md` section 8: the rule beside `touchesOnlyWhile`, and that a new destination is covered by `screen()`. Verify: `git grep -n "double-tap" docs/ARCHITECTURE.md` finds it.

## 5. Engineer's gate and walk

- [x] 5.1 Gate. Verify: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0.
- [x] 5.2 Walk on `emulator-5570`, debug: #156's steps, with an injected double tap at about 150 ms, then a single tap at 500 ms on the same point; a tap on the Back arrow of the new screen at 500 ms; "Medication taken" in attack mode. Timing evidence needs the emulator started with `-no-window -gpu swiftshader_indirect`; `-gpu host` on 5570 gives slow frames and does not count as timing evidence. Restore every setting. Verify: results in the packet, with how the emulator was started and what they do not prove.
- [x] 5.3 Permissions. Verify: `./gradlew :app:checkReleasePermissions` exits 0.
- [x] 5.4 After 3.3, 3.5 and the review's code fixes: the gate, the permissions check and the negative control of 3.4 again on the new head, because 5.1, 5.3 and 3.4 were run on 9ec33829. Verify: the same commands exit 0, and the failing test names are in the packet.

**Engineer's record (9ec33829).** Emulator `-no-window -gpu swiftshader_indirect`, debug, one injected finger at the same point, gaps read from `getevent`, every trial's `dumpsys gfxinfo` kept. This proves one finger on one emulator: not a human hand, not a real phone, not a rate, and not R8.

| Route | Press gap | With the guard | Guard removed, local APK |
|---|---|---|---|
| Add medication, scrolled details, 4 medications | 92-98, 142-150, 244-247 ms | no intake, Log medication waits, 9/9. A later single tap at the point writes, 9/9 | 148-150 ms: intake, 3/3 |
| The same | 491-506 ms | intake with Undo, 3/3 | |
| Medication taken, attack mode, 8 medications | 100, 140-148, 243-245 ms | no intake, 6/6. A later single tap writes, 6/6 | 144-150 ms: intake 1 of 2 |
| The same | 495-499 ms | intake with Undo, 2/2 | |
| Reminders row in Settings | 142-143 ms | Reminders opens, nothing else, 2/2 | |
| Reminders row in Settings | 493-497 ms | the tap lands on the reminder time, which opens its picker, 2/2. In this data the switch is not under the finger, so this says nothing about the switch | |
| Back arrow of a new screen, far from the first tap | 488-499 ms | back on Home, 3/3 | |

Frames while these ran: 2-8 janky of 16-58, 90th percentile 19-48 ms.

**Engineer's record after review.** The marks went onto each entry's own `SavedStateHandle` instead of saved marks with a cap, so task 3.5's "a mark beyond the bound" has no bound to test: `aNewEntryOfTheSameDestinationAfterAPopHasNoMark` covers a popped entry instead. Rerun because the read of the mark changed, same emulator flags, one injected finger:

| Route | Press gap | Result |
|---|---|---|
| Add medication, scrolled details | 148-149 ms | no intake, 3/3, a later single tap writes 3/3 |
| The same | 491-506 ms | intake, 3/3 |
| Medication taken, attack mode, 8 medications | 147-150 ms | no intake, 2/2, a later single tap writes 2/2 |
| The same | 503-505 ms | intake, 2/2 |
| Double tap on Back into a touched Settings, press gap 273-280 ms, no recreation | | lands on Home, 2/2 |
| The same after a rotation | 273-275 ms | lands on Home, 2/2 |
| The same after `kill -9` and a restore | 276-280 ms | lands on Home, 2/2 |
| Control: marks in `remember`, local APK, after a rotation or a kill | 272-278 ms | does not land, 4/4 |
| Control: marks in `remember`, no recreation | 278 ms | lands, 1/1 |

At about 235 ms the press mostly falls on the leaving screen, with or without a recreation. Not a human hand, not a real phone, not R8.

## 6. `qa-engineer`, independently (emulators, debug and R8)

- [x] 6.1 Build a negative control: the same SHA with only the guard removed, debug and R8, not pushed. Verify: `git diff <sha>` shows only that change, kept with the evidence. Done by QA at 4bb7127a: the rule's time test made never true, local, not pushed (`qa-t49/negative-control.diff`). It wrote 55 of 56 under 280 ms and 44 of 44 above 320 ms on debug, and at 150 ms in all six kinds on R8. 19 of 42 branch tests fail on it.
- [x] 6.2 Run `t5_write.py` on debug: every row of #156's table, with the guard and without. Asks of 100, 150, 200, 250, 280, 320, 350, 400, 500 and 700 ms, so that both sides of the timeout have real samples, read by measured gap. Verify: with the guard no write under the timeout. Without it writes on every row. From 320 ms up with the guard the tap answers. Cells within about 20 ms of the timeout are reported and not judged. Done by QA at 4bb7127a, every row by measured gap (`qa-t49/logs/recount-t49.txt`, from `recount_t49.py`): with the guard 0 of 70 wrote under 280 ms, 14 of 14 wrote at 280-320 ms (reported, not judged), 56 of 56 above 320 ms.
- [x] 6.3 Run an extended `t5r8.py` on R8: the false intake (all three starting points), Remove pain level, the location chip and the Reminders switch, read from the screen, with the guard and without. Verify: as 6.2, at 150 and 500 ms. Done by QA at 4bb7127a, with one gap: with the guard 0 of 6 wrote at about 150 ms and 5 of 6 at about 500 ms. The sixth, the location chip, did not write on the control either, so the chip above the timeout on R8 is not proved.
- [ ] 6.4 Back and returns. `t8_148.py` with the same asks as #148's table, against 2f800c71: no worse. A double tap on Back, on Done on Log medication and on Save on the editor: record what lands. Verify: the table is in the report. A write there is a new issue and is not called a pass. Moved, not done here: QA carries it to `fix-dialog-second-tap` section 7, whose head holds this code unchanged. #148 on R8 at 4bb7127a was no worse: 12 of 12 at 280-420 ms, 6 of 6 above.
- [ ] 6.5 No first tap, and recreation. Open from the reminder notification and from the "start migraine" shortcut, and tap a control straight away. A first launch: tap a control straight away. A rotation between the two taps of a double tap: the control answers. Process death (`am kill` in the background, then back from recents): tap a control straight away, and a return by Back to a touched screen, pressed under the timeout near the Back tap, answers. Restore rotation. Verify: the control answers, or the only wait is attack mode's own D32, shown by the same result without the guard. Done by QA at 4bb7127a except the notification path, which was not run: the script could not find the notification on the shade. Shortcut, cold start and a rotation between taps answered on debug and R8. After process death, Back to a touched screen then a tap near the Back point landed 8 of 8 at 280-307 ms on both builds, per QA's report.
- [ ] 6.6 Animator scale 0, 200% font, landscape, TalkBack and switch access on a device: the click action on a new screen under the timeout. A hardware keyboard or `adb shell input keyevent`: activate a control on a new screen under the timeout, and it answers. Restore every setting. Verify: results in the report, saying how close to the timeout the keyboard activation came. Moved, not done here: to `fix-dialog-second-tap` section 7, with the #138-#142 regression tables, the full #148 table, the Maestro suite and API 26.
- [ ] 6.7 Retest #156 under the reading in the design's Test plan. QA judges the closure, and if it disagrees it brings evidence to `team-lead`. Verify: the issue's closing comment names what was measured and what was not, and the remaining writes from the timeout up are in the 1.1.0 Go/No-Go as residual risk. Open: QA judges closure after the merge.
- [x] 6.8 Dialogs: `team-lead` asks QA to measure this now, because the result could change the scope. This task stays open only if QA's run does not cover it. Double tap each control that opens a dialog in one tap and record what the second tap lands on: `EffectDialog`, `AddPainDialog`, `CustomTagDialog` (`features/attack/ui`), `ThemeDialog` (`SettingsDialogs.kt`), and the `Dialog`s in `RemindersScreen.kt` and `YourDataScreen.kt`. `MigrelyDateTimePicker.kt` also holds a dialog, so include it. `t5_write.py` has no dialog route, so it needs one. Verify: a table in the report, one row per dialog, saying which have a one-tap write and what a double tap did at measured gaps. A write is a new issue and is not called a pass. Covered by T50 and filed as #157 and #159, planned in `fix-dialog-second-tap`.
- [ ] 6.9 Repeated tapping on attack mode's routes: "Medication taken" into Log medication, 4 medications in landscape and 8 in portrait. Bursts of 3, 5 and 10 taps at the same point, gaps of about 100 and 250 ms, then a pause of about 500 ms and one tap. Verify: a table of measured gaps, what each tap did, and what the tap after the pause did. No write in the burst, and the tap after the pause answers. Anything else goes to `team-lead`. Moved, not done here: to `fix-dialog-second-tap` section 7.

## 7. Human hand, internal track

- [x] 7.1 `team-lead` adds this to the release guide's internal-track step (`reports/2026-11-06-chiqish-kuni-yoriqnomasi.md`, step 2). On a real phone, by hand, with a test migraine: double tap Add medication on details, Log medication on Home, the Reminders row and a row on Home's today list, five times each, as fast as natural. Note every write, and Undo it. Then on each new screen, tap one control as soon as it can be read: it must answer the first time. After repeated tapping on a control of a new screen, the next tap after a short pause answers. If the screen feels frozen, it goes back to `team-lead` before Production. Verify: the step is in the guide, in Uzbek. Done by `team-lead`: guide step 2, row 9, in `reports/2026-11-06-chiqish-kuni-yoriqnomasi.md`, read by `product-manager`.
- [ ] 7.2 `qa-engineer` puts the result in the Go/No-Go for 1.1.0, with what it does not prove: one person, one phone, few trials. A false intake from a natural double tap at or above the timeout goes back to `team-lead` before Production. Verify: it is in `docs/qa/`. Carried to the 1.1.0 Go/No-Go.

## Not done here

- Double taps from 300 ms up, a slower hand, a first tap that opens a dialog (no issue yet, measured by task 6.8), #148. See the proposal.
