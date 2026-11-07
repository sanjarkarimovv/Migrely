# Tasks

Base: `feature/fix-double-tap-writes` from `origin/bugfix/142-leaving-screen-input` (2f800c71). No domain, presentation or data
layer change: the rule is about touch and lives in `:app`.

## 1. Plan

- [x] 1.1 BRD: D159, EC-UX-24 to 26, a note on D155, §13 row 30. Verify: `grep -c "^| D159 " docs/BRD.md` prints 1, and `git grep -c "^| D159 " origin/develop origin/bugfix/146-paywall-play-failure -- docs/BRD.md` prints nothing.
- [x] 1.2 Proposal, design, delta, tasks. Verify: `openspec validate fix-double-tap-writes --strict`.

## 2. The rule

- [ ] 2.1 A pure class in `app/src/main/java/com/migrely/app/ui/main`, design D2: told about lifts, asked about presses, given the entry id, the event time, the position, the timeout and the distance. An ignored press stays ignored until its pointer lifts. Its lift updates the last lift and does not mark the entry. Verify: compiles with `./gradlew :app:compileDebugKotlin`.
- [ ] 2.2 JVM tests first: gaps 0, 39, 40, 60, 100, 150, 299, 300, 301, 700 ms; under and over the distance; a new entry and a touched one; a burst of three, then a pause; no lift yet; two pointers. Verify: `./gradlew :app:testDebugUnitTest --tests '*SecondTap*'` passes, and fails first on an empty implementation.

## 3. UI and wiring

- [ ] 3.1 `TouchesOnlyWhile.kt`: the node asks the class in the initial pass and consumes what it refuses. Consumption and hit testing stay as they are. No semantics. Verify: `git diff` shows no semantics modifier, and `./gradlew :app:testDebugUnitTest --tests '*LeavingScreenInputTest*'` passes or each failure is listed in the packet with its reason (design, Test plan).
- [ ] 3.2 `ScreenDestination.kt`: one instance for the nav host; `screen()` passes the entry id, `LocalViewConfiguration.current.doubleTapTimeoutMillis` and `ViewConfiguration.get(context).scaledDoubleTapSlop`. No literal 300 or 100. Verify: `git grep -nE "\b(300|100)\b" -- app/src/main/java/com/migrely/app/ui/main/ScreenDestination.kt app/src/main/java/com/migrely/app/ui/main/TouchesOnlyWhile.kt` prints nothing, and `git grep -c "screen<" -- app/src/main/java/com/migrely/app/ui/main/AppNavHost.kt` still prints 20.
- [ ] 3.3 Compose tests beside `LeavingScreenInputTest`, real `screen()` and nav host, a counter on a control under the finger: a second tap at 100, 150 and 299 ms does nothing; at 300 and 500 ms it counts; beyond the distance at 100 ms it counts; a touched screen after Back takes it as before; a screen with no lift before takes a press at once; a held press and a drag; the next gesture answers; `performSemanticsAction(OnClick)` at 50 ms runs; the semantics tree with and without the guard. Verify: `./gradlew :app:testDebugUnitTest --tests '*DoubleTap*'` passes.
- [ ] 3.4 Negative control, local only: remove the node's call to the class, run 3.3, record which tests fail, and restore. Never commit it. Verify: the failing test names are in the packet, and `git diff --stat` is empty for that change afterwards.

## 4. Docs

- [ ] 4.1 `docs/ARCHITECTURE.md` section 8: the rule beside `touchesOnlyWhile`, and that a new destination is covered by `screen()`. Verify: `git grep -n "double-tap" docs/ARCHITECTURE.md` finds it.

## 5. Engineer's gate and walk

- [ ] 5.1 Gate. Verify: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0.
- [ ] 5.2 Walk on `emulator-5570`, debug: #156's steps, with an injected double tap at about 150 ms, then a single tap at 500 ms on the same point; a tap on the Back arrow of the new screen at 500 ms; "Medication taken" in attack mode. Restore every setting. Verify: results in the packet, with what they do not prove.
- [ ] 5.3 Permissions. Verify: `./gradlew :app:checkReleasePermissions` exits 0.

## 6. `qa-engineer`, independently (emulators, debug and R8)

- [ ] 6.1 Build a negative control: the same SHA with only the guard removed, debug and R8, not pushed. Verify: `git diff <sha>` shows only that change, kept with the evidence.
- [ ] 6.2 Run `t5_write.py` on debug: every row of #156's table, with the guard and without. Asks of 100, 150, 200, 250, 280, 320, 350, 400, 500 and 700 ms, so that both sides of the timeout have real samples, read by measured gap. Verify: with the guard no write under the timeout. Without it writes on every row. From 320 ms up with the guard the tap answers. Cells within about 20 ms of the timeout are reported and not judged.
- [ ] 6.3 Run an extended `t5r8.py` on R8: the false intake (all three starting points), Remove pain level, the location chip and the Reminders switch, read from the screen, with the guard and without. Verify: as 6.2, at 150 and 500 ms.
- [ ] 6.4 Back and returns. `t8_148.py` with the same asks as #148's table, against 2f800c71: no worse. A double tap on Back, on Done on Log medication and on Save on the editor: record what lands. Verify: the table is in the report. A write there is a new issue and is not called a pass.
- [ ] 6.5 No first tap. Open from the reminder notification and from the "start migraine" shortcut, and tap a control straight away. Verify: the control answers, or the only wait is attack mode's own D32, shown by the same result without the guard.
- [ ] 6.6 Animator scale 0, 200% font, landscape, TalkBack and switch access on a device: the click action on a new screen under the timeout. Restore every setting. Verify: results in the report.
- [ ] 6.7 Retest and close #156 only under the reading in the design's Test plan. Verify: the issue's closing comment names what was measured and what was not.

## 7. Human hand, internal track

- [ ] 7.1 `team-lead` adds this to the release guide's internal-track step (`reports/2026-11-06-chiqish-kuni-yoriqnomasi.md`, step 2). On a real phone, by hand, with a test migraine: double tap Add medication on details, Log medication on Home, the Reminders row and a row on Home's today list, five times each, as fast as natural. Note every write, and Undo it. Then on each new screen, tap one control as soon as it can be read: it must answer the first time. Verify: the step is in the guide, in Uzbek.
- [ ] 7.2 `qa-engineer` puts the result in the Go/No-Go for 1.1.0, with what it does not prove: one person, one phone, few trials. Verify: it is in `docs/qa/`.

## Not done here

- Double taps from 300 ms up, a slower hand, dialogs (#149), #148. See the proposal.
