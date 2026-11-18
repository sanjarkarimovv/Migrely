# Tasks

Base: `bugfix/237-error-stays-after-success` (worktree `migraine-app-237`) from `origin/develop` 76c38c38. Release: 1.3.2,
tracker T75. Write `Refs #237` in every commit and in the PR into `develop`, never a closing keyword: QA filed #237 and closes
it after the retest. BRD D190, EC-MED-16, EC-MED-17, EC-UX-35; D178. Design: this change's `design.md` (D1 to D4, rows A1 to A9).

Ownership:
- `product-manager`: 0 (done) and 8;
- `ux-designer`: 1;
- `android-engineer`: 2 to 5;
- `code-reviewer`: 6;
- `qa-engineer`: 7 (emulator-5580, API 36).

No `medical-reviewer`: the two new strings (ux 4.4) carry no health claim. Layers: presentation and UI of `features/medication` only.
Every test below is written first and shown red on `develop` 76c38c38 (or the branch before the fix), then green; each
mutation is a deliberately broken build that must turn the test red again. Logs go to `migrely-work/237/eng/`.

## 0. Plan (`product-manager`)

- [x] 0.1 BRD D190 (with ux L3, L4, 4.4 and the team-lead condition on rule 2), EC-MED-16, EC-MED-17, EC-UX-35 pointer, §13
  row 54; proposal, design, `in-app-messages` and
  `medication-intakes` deltas, tasks. Verify: `openspec validate clear-message-when-intake-logged --strict` and
  `openspec validate --all --strict` exit 0.

## 1. Design check (`ux-designer`)

- [x] 1.1 Confirm or object to D190 (1) and (2) in `docs/ux/239-237-validation-and-save-feedback.md` (branch
  `ux/239-237-feedback`). Verify: the ux file's #237 section names D190 and says "agree" or lists the change; PM records the
  answer here.
  - Record (2026-11-18, product-manager): ux-239 agrees with (1) and (2) (4001103f, section 4) and adds L3 (same frame, no
    fade), L4 (only the logged line is heard) and the failed-Undo copy "Still logged. …" (4.4). All accepted into D190,
    the deltas and design D2, D3; the ux file is merged into this branch (c7d78595).

## 2. Presentation (`android-engineer`, `features/medication/presentation`)

- [x] 2.0 **First, before any fix (team-lead condition):** a red test on the code as on v1.3.1 (`develop` 76c38c38) that shows
  a failed Undo: with the use case returning `StorageFull`, the screen shows the medication list (no logged result) while
  the intake was never removed. Verify: the test's red run log in `migrely-work/237/eng/2.0-red.log`. If it cannot be made
  red, stop: report to `product-manager`, who removes D190 (2), EC-MED-17, the second ADDED requirement, the two strings and
  rows A6 and A7; skip 2.1, 3.4 and the strings in 3.7.
  - Record (2026-11-18, android-engineer): RED. `LogMedicationUndoTest` "Undo with full storage keeps the logged result
    while the intake is still stored" (full `LogMedication` screen, real ViewModel and use cases over `FakeDiary`,
    `deleteIntake` -> `StorageFull`) on 5388f77e, whose product tree equals `v1.3.1`: exit 1, 1 test, 1 failed. The
    intake was still stored and "Couldn't save…" showed, but "Ibuprofen 400 mg logged at" was gone (the list came back).
    `migrely-work/237/eng/2.0-red.log`. `product-manager` confirmed rule 2 stays.
- [x] 2.1 Undo's outcome can be told apart by the screen (design D3): a success outcome that both drops the logged result and
  shows "Removed. Nothing was logged.", and failures that keep it. The ViewModel stays stateless (the screen passes the
  intake id). Verify: `./gradlew :features:medication:presentation:test --tests '*LogMedication*'`
  green, with cases for `Done`, `StorageFull` and `Failed` from `UndoIntakeUseCase`; red before the change
  where the outcome type is new.
  - Record (2026-11-18, android-engineer, a0f8fdd9): new `LogMedicationPresentationNotification.IntakeKeptStorageFull`
    and `IntakeKeptFailed` for Undo's failures; `IntakeUndone` and the reload stay for `Done`. ViewModel still stateless.
    Red: 2 new cases did not compile (`Unresolved reference 'IntakeKeptStorageFull'`, 2.1-red.log). Green:
    `:features:medication:presentation:test` in green-1.log. `Done` case existed and stays green.

## 3. UI (`android-engineer`, `features/medication/ui`)

- [x] 3.1 A stored intake clears any message on Log medication, silently, in the same frame, with no fade (D1, D2). Verify:
  a Robolectric test (for example `LogMedicationMessagesTest` in `testDebug`) that shows "Couldn't save. Your phone's storage
  is full.", sends `Logged`, and with the main clock paused and advanced one frame asserts: the message text is gone from the
  semantics tree, the logged line is present and not covered, no "Saved." node exists. Mutation: let the exit use the host's
  200 ms fade → red. Repeat for "Something went wrong. Please try again.", "Pick a time up to now." and "Removed.
  Nothing was logged.". Red on 76c38c38 (the error stays). Mutation: clear only error-tone messages → the "Removed…" case red.
  - Record: `LogMedicationMessagesTest` (full screen over the real ViewModel): the real tap after freeing space, and four
    frame cases (storage full, generic, "Pick a time…", "Removed…") stepping 20 frames with the clock stopped: no frame
    has the logged line with the old text, and no node the settled screen lacks takes room in a frame with the result;
    no "Saved." node. All 5 red on 5388f77e (red-3x/). Code: `MedicationMessages` clears on `Logged` with no fade
    (`reduceMotion || leavesAtOnce`) and draws nothing for the leaving message, while the host's tap guard keeps its
    place (it has no semantics). Mutations (mutations.txt): host fade, both parts removed -> the 5 frame cases red; the
    leaving message drawn again (early return removed) -> the same 5 red; clear only error tone -> "Removed…" and "Pick
    a time…" red.
  - Correction after review (cr-237 S4-2, 2026-11-18): the first mutation removed both parts, so it credited
    `|| leavesAtOnce` with nothing of its own. Removing only `|| leavesAtOnce` leaves every test green (s4/mutations.txt),
    and so does a new guard "A message posted right after a log never brings the cleared one back" (also green under
    the "fade" and "draw-leaving" mutations, s4/mutations-2.txt): the host reuses its one in-place slot for the next
    message, so the cleared text cannot come back. What `|| leavesAtOnce` changes is only the 450 ms size animation of an
    empty area with no semantics and no drawing. It is kept as reviewed; removing it is a KISS option for
    `team-lead`/`code-reviewer`.
- [x] 3.2 Only a stored intake clears (EC-MED-16 "It fails again", "Not on the tap"). Verify: in the same test class, an error
  followed by another `StorageFull`, `Failed`, `InFuture` or `NotSaved` keeps a message and the list; a tap with the write
  pending keeps the error; a `Logged` followed by an error shows the error. Mutation: clear on the tap (in `onMedication`) →
  the pending case red.
  - Record: fails again (StorageFull, Failed), "Pick a time…", a refused first medication (`NotSaved`, today's
    behaviour; #239 F9 owns its clear), a pending write keeps the error until the stored outcome, an error after a
    log shows. Green on 5388f77e except the pending case's last step (the error never went). Mutation: clear on the tap
    (the message host restarts on each medication tap) -> "While the write runs the error stays…" red.
- [x] 3.3 "Add and log" path (EC-MED-16 "First medication added and logged"). Verify: a test with an empty list, an error,
  then `Logged` after `onAddAndLog`; red on 76c38c38.
  - Record: "The first medication added and logged after freeing space clears the error" (save fails with
    StorageFull, then succeeds): red on 5388f77e (error stayed), green now; 1 medication, 1 intake.
- [x] 3.4 Undo waits for its outcome (D3, EC-MED-17). Verify: tests where Undo's outcome is success (list + "Removed…"),
  storage full (logged result stays, "Done" and "Undo" present and enabled, error shown), failed (logged result stays,
  "Something went wrong…"), and two quick taps on Undo (one `onUndoAction` call). The storage-full case is red on 76c38c38
  (the list shows). Mutation: restore the early `logged = null` → storage-full case red.
  - Record: `LogMedicationUndoTest`, 6 cases (stored; storage full; other failure; Undo again after freeing space;
    two quick taps -> one `deleteIntake`; the result stays while the removal runs). 5 red on 5388f77e (the two-taps case
    red because the second "Undo" no longer existed). Screen: `onUndo` sets `pending` and keeps `logged`; only the new
    `MedicationEventUiModel.IntakeUndone` drops it. Mutations: early `logged = null` -> 6 red incl. storage full; no
    `pending` guard on Undo -> two-taps red.
  - Added after review (cr-237 S4-4): "Done before the outcome closes the screen, and nothing shows on return" (a
    `NavHost` with Done popping to a home destination; the removal released after leaving; on return no "Removed…" and
    no logged line). Mutation: the presenter replays its last event to a new screen -> that case red. EC-MED-16 "Two
    quick taps": two taps on Ibuprofen while the stored write is held -> one intake, one logged line; mutation without
    the `pending` guard and with the button left enabled -> red (with only the lambda guard removed it stays green: the
    disabled button is the first guard).
- [x] 3.5 Rotation and process death (EC-MED-16, EC-MED-17). Verify: `StateRestorationTester` cases: after a log that cleared
  an error, the restored screen shows the logged result and no message; during a pending Undo, the restored screen shows the
  logged result with "Undo" enabled. Mutation: keep the shown message in plain `remember` while it should be cleared → no
  change expected (document the result either way); mutation: drop `rememberSaveable` on `logged` → the second case red.
  - Record: `StateRestorationTester` cases, both red on 5388f77e (error restored; list restored during Undo). Mutation
    `logged` in plain `remember` -> both red. Mutation shown message in plain `remember` -> no change (exit 0): the clear
    already set it to null, so saving it adds nothing for this rule; it stays saveable for D182/rotation of errors.
  - Correction after review (cr-237 S4-1): "Undo" is never disabled, so the first version's `isEnabled()` check could
    not fail (a saveable `pending` left all tests green). The case now taps Undo after the restore and expects a second
    removal of the same intake; mutation `pending` in `rememberSaveable` -> that case red (s4/mutations.txt).
- [x] 3.6 Dark room and accessibility (EC-MED-16 "The dark room", "Keyboard and TalkBack", "Largest font size and
  landscape"). Verify: the 3.1 case with `darkRoom = true`; a keyboard case (Enter on a focused medication) asserting no node
  is focused at the message's old place and the logged line is the only live region; a 200% font case in a 411x891 dp and a
  891x411 dp window asserting the logged line, "Done" and "Undo" are fully inside the window and not under any message node.
  - Record: dark-room frame case (red on 5388f77e); keyboard case: Tab to "Log Ibuprofen, 400 mg", Enter, then the error
    is gone, nothing is focused, the only live region is the logged line, next Tabs Back, Done (red on 5388f77e: the
    error stayed); 200% font in 411x891 and 891x411 dp: no message left and the three parts inside the window (both
    red on 5388f77e). Robolectric does not model TalkBack's speech; that is rows A5 on a device.
  - Correction after review (cr-237 S4-3): Robolectric fakes text metrics, so the bounds are the same at 100% and 200%;
    the two cases prove only "no message left", and are renamed so. The 200% claim (whole logged line, Done and Undo in
    portrait and landscape) rests on walk row A8 (5.2).

- [x] 3.7 Two strings for a failed Undo (only if 2.0 was red): "Still logged. Your phone's storage is full." (error, no time
  limit) and "Still logged. Something went wrong. Please try again." (error, 8 s with D182 timing), exactly as ux 4.4. Verify:
  the 3.4 tests assert these texts; a behaviour-mapper test asserts tone and duration; mutation: map the failure to the old
  "Couldn't save…" message → red.
  - Record: `medication_message_intake_kept_storage_full` and `medication_message_intake_kept_generic` (the module's
    `medication_` prefix instead of ux's suggested `message_intake_kept_*`; texts exactly as ux 4.4). Behaviour mapper
    test: Error/no limit and Error/8 s. Mutation: map to the old "Couldn't save…"/generic messages -> mapper test and 3
    Undo cases red.

## 4. Gate (`android-engineer`)

- [x] 4.1 Full gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`
  exit 0, with the counts of tests run (never `testDebugUnitTest` alone).
  - Record (3ab5d6ec's tree): exit 0, BUILD SUCCESSFUL (`migrely-work/237/eng/gate-2.log`). Result XMLs after the gate:
    3413 test cases, 0 failures, 0 errors, 0 skipped; `:features:medication:ui` 120 (debug) + 23 (release),
    `:features:medication:presentation` 41. The first gate run (gate-1.log) failed on ktlint line length; fixed in
    3ab5d6ec.
- [x] 4.2 No permission or manifest change; strings only the two of 3.7. Verify: `./gradlew :app:checkReleasePermissions`
  exit 0 and `git diff origin/develop -- '*.xml' '*strings*' '*AndroidManifest*'` shows only the two new strings (or nothing
  if rule 2 dropped).
  - Record: `:app:checkReleasePermissions` exit 0 (perms.log). `git diff origin/develop -- '*.xml' '*AndroidManifest*'`:
    only `features/medication/ui/src/main/res/values/strings.xml`, +3 lines (the two strings and their comment). No
    Gradle, manifest or dependency change.

## 5. Device walk (`android-engineer`, emulator-5570, headless first)

- [x] 5.1 Rows A1, A3, A5, A9 and (if rule 2 ships) A6 of design "Acceptance" on the candidate (debug build of the
  branch head, hash read back from the device), API 36, 100% portrait; storage filled as QA did for #237 (`fallocate` as shell, then as the app) and freed after
  each row. Verify: per row the intake count before and after (`sqlite3` on a copy of the database, or the app's own list),
  a `uiautomator dump` 2 s after the tap showing no message node and the logged line present, and TalkBack's log for A5.
  - Record (2026-11-18, android-engineer, emulator-5570 `Migrely_API_36`, headless swiftshader, API 36, 1080x2400 @420,
    100% portrait unless said). Candidate = debug APK of 3ab5d6ec's tree (sha256 c6b26b55…, read back from the device);
    control = QA's debug APK of `develop` 76c38c38 = v1.3.1's product tree (fde165db…, read back). Disk filled with
    `fallocate` (shell, then the app's cache). From A8 landscape on, three full-disk taps in a row were stored, because
    the write-ahead log kept room from earlier writes; from then on the WAL was checkpointed (TRUNCATE, content
    unchanged) on a stopped app before each fill, and every full-disk tap failed as it should. Intake counts from a copy of the database; dumps by `uiautomator dump /dev/tty`. Log:
    `migrely-work/237/eng/walk/walk.log`, shots and videos in `migrely-work/237/shots/`.

    | Row | Candidate | v1.3.1 control |
    |---|---|---|
    | A1 | error on the full-disk tap; after freeing, at 2 s and 8 s no message, logged line in the tree, intakes 0 -> 1 | error still on screen at 2 s and 8 s, logged line not in the tree, intakes 1 -> 2 |
    | A2 (extra) | first use, Add and log: error, then logged, no message at 2 s and 8 s, intakes 0 -> 1 | not run |
    | A3 | log tapped 4 s after "Removed…" showed: message gone at the first dump, logged line present; intakes 1 -> 1 | "Removed…" over the result at 6 s and 8 s, logged line not in the tree; gone at 10 s (its own 8 s) |
    | A5 | TalkBack VERBOSE: after the tap only "Ibuprofen logged at 10:54 AM" is spoken (plus TalkBack's own "Double-tap to activate" hint before it); no "Saved.", nothing about storage | the logged line is never spoken |
    | A6 | "Still logged. Your phone's storage is full.", Done and Undo shown, intakes 1 -> 1; the message lies over the logged line (first-row rule D158, out of scope) and Done is below it | list back with "Couldn't save…", intakes 2 -> 2 (still stored) |
    | A7 | after freeing, Undo: list and "Removed. Nothing was logged.", intakes 1 -> 0 | n/a |
    | A9 | `screenrecord` across the tap: 6 frames, 0 with the logged page and anything in the message's place, no in-between (fade) values | 3 frames, 2 with the logged page and the message |
    | Process death | after A1, Home, `run-as kill -9`, relaunch: logged result, no message | not run |

    A first A3 run (both builds) tapped about 8 s after the message showed, so it expired by itself and the row proved
    nothing; it was rerun with an early tap (`a3fast`), above. The frame check of A9 first compared with the last frame,
    which could not fail on the control; it now compares with an empty-background patch (candidate 0, control 2).
- [x] 5.2 A8 (200% font, portrait and landscape). Verify: one screenshot per orientation in `migrely-work/237/shots/` and the
  logged line's bounds from a dump.
  - Record: 200% font. Portrait: A1 at 200%, logged line [42,359][1038,564], Done text [447,664][633,759], Undo text
    [445,843][635,938] in a 1080x2400 screen, no message (`shots/A8-cand-2s`… and `walk.log`). Landscape: the error was
    set up in portrait, the phone turned (error kept, D178), space freed, logged: logged line [583,294][1617,389], Done
    [1172,489][1358,584], Undo [1170,668][1360,763] in 2400x1080, no message, intakes 0 -> 1
    (`shots/A8-cand-landscape.png`). Bounds are of the texts; the buttons are whole on the shot.
- [x] 5.3 Restore storage, font scale, rotation and TalkBack. Verify: `df /data` free space and the settings read back.
  - Record: storage freed (`df /data`: 8288132 K free), TalkBack off (`enabled_accessibility_services` null,
    `accessibility_enabled` 0) and its log level back to ERROR, font 1.0, `user_rotation` 0, `accelerometer_rotation` 1,
    animator 1, `wm size` 1080x2400 and density 420 unchanged. The app is now the candidate build with its data cleared
    (baseline was a 1.1.1 debug build with no database). Emulator turned off (`adb emu kill`).

## 6. Review (`code-reviewer`)

- [ ] 6.1 Review sections 2 to 3 against design D1 to D4: clears only on `Logged`, both collectors, no ViewModel state, Undo
  keeps the result until success, no double Undo, only the two ux 4.4 strings, no fade on the clear, D178 (2) and D182 untouched. Verify: a review packet with
  findings, each fixed or answered by the engineer before QA.

## 7. Acceptance (`qa-engineer`, this diff only)

- [ ] 7.1 Rows A1 to A9 of design "Acceptance" on the PR head (A6 and A7 only if rule 2 ships), with v1.3.1 as the negative
  control (A1, A2, A3, A4, A5, A6 and A9 must show the defect on v1.3.1, or that row proves nothing and says so). APK hashes read back on the device. Verify: a QA
  record `docs/qa/clear-message-when-intake-logged.md` with each row, build SHA, result and control result.
- [ ] 7.2 Restore every device setting and free the filled storage. Verify: the settings and `df /data` read back in the QA
  record.

## 8. Close (`product-manager`, then `android-engineer`)

- [ ] 8.1 Before archive: diff this change's MODIFIED "An error on the other screens goes when a later save succeeds" against
  the live `openspec/specs/in-app-messages/spec.md`; sync it if another change (for example #239's) rewrote that requirement
  since 2026-11-18. Verify: the diff shows only this change's body edit, two rewritten scenarios and one new scenario.
- [ ] 8.2 BRD D190 updated with QA's results (what was proved, what stays open), then archive. Verify:
  `openspec archive clear-message-when-intake-logged --yes` and `openspec validate --all --strict` exit 0.
- [ ] 8.3 PR into `develop` with `Refs #237`, merge commit, worktree removed after merge, tracker T75 updated by `team-lead`.
  Verify: `gh pr view <n> --json state,mergeCommit` shows MERGED.
