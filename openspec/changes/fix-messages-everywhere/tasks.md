# Tasks

Base: `bugfix/181-messages-everywhere` from `origin/develop` 52c12ce5. Every commit and the PR say `Refs #181`, `Refs #185`,
`Refs #183` and `Refs #88` (and `Refs #153` where the short-window work touches it). Never use a closing keyword, because QA filed
these issues and retests them. No domain, data or permission change.

**Work from the UX spec.** `migrely-work/t63/ux-spec.md` §2 is the engineer's rule list (rules 1–8). §3 holds the state tables QA
and the engineer cover: O1–O20 (order), F-a–F-h (focus), M1–M12 (motion and announcements), P1–P7 (placement), S1–S4 and R1–R5
("Saved." and the review). Each task below names the rules and rows it must satisfy. If a rule there and this file disagree, ask
`product-manager` before you code.

## 1. Plan (product-manager)

- [x] 1.1 BRD: D178, EC-UX-35…38, EC-REV-13, §13 row 46; EC-UX-16 and EC-UX-34 updated. Verify: `grep -c "^| D178 " docs/BRD.md`
  prints 1 and `git show origin/develop:docs/BRD.md | grep -c "^| D178 "` prints 0.
- [x] 1.2 Proposal, design, the `in-app-messages` and `in-app-review` deltas, tasks. Verify:
  `openspec validate fix-messages-everywhere --strict` exits 0.

## 2. UX (ux-designer, before any code)

- [x] 2.1 Answer design Q1–Q8 in a ux spec. `team-lead` stores it in `migrely-work/t63/ux-spec.md`. Answer at least:
  - the Tab and TalkBack order: Back and the bar's actions, then the message control, then the content; on Home and onboarding,
    the message first (Q1, Q2);
  - where focus goes when the focused message leaves (Q3);
  - "Saved." per screen: Home in both modes, History and Reminders, plus Doctor report's silent clear (Q7);
  - the Home review request while "Couldn't save" or "Saved." shows (Q8);
  - the arrival fade's look and replacement in place (Q4, Q5);
  - the short-window Home Today placement (Q6).

  Verify: the spec file exists and answers each Q by number. Any answer that departs from a PM default comes back to
  `product-manager`, who updates D178 and the deltas before code starts. Done: `migrely-work/t63/ux-spec.md` (276 lines), with
  Q1–Q8 in §1 and findings F1–F9 in §6. Q5, Q6 and Q8 depart from the PM defaults.
- [x] 2.2 PM folds ux's answers into the design and deltas where they differ. Verify:
  `openspec validate fix-messages-everywhere --strict` exits 0 after the update. Done: `team-lead` accepted F1–F8, including F6
  (the measured offset in portrait too). They are in D178 (1), (3)–(6), (10), EC-UX-16, 35–38, EC-REV-13, design D1, D3–D6, Q1–Q8,
  A11, A12, A15, A17, A18, A20 and N8, and in both deltas. F9 is listed under out of scope in the proposal only.

## 3. Presentation (android-engineer)

- [ ] 3.1 `HomeViewModel`, `HistoryViewModel` and `RemindersViewModel` emit one "write saved" event on each success in the design's
  table (middle column), including Reminders' visit date `Saved` with `askForPermission = true` (F4), and none on a result that
  writes nothing. Write the tests first, one per write path, plus one for each
  non-write result (`NotInProgress`, `Unchanged`, `NothingToRemove`). Verify:
  `./gradlew :features:home:presentation:test :features:history:presentation:test :features:reminders:presentation:test`
  passes, and the new tests fail first on 52c12ce5.

## 4. UI models, mappers and strings

- [ ] 4.1 `Saved` in the home, history and reminders message models: confirmation, polite, `READING_MILLIS`, no action, no
  haptic. The string is "Saved." in each module's `strings.xml`. Verify:
  `./gradlew :features:home:ui:testDebugUnitTest :features:history:ui:testDebugUnitTest :features:reminders:ui:testDebugUnitTest --tests '*MessageBehaviour*'`
  passes, with a case for `Saved` in each.

## 5. designsystem

- [ ] 5.1 Never transparent (design D4, ux rules 4 and 8). First write a red test: in no frame of an arrival, and of a replacement,
  is the message's node in the semantics tree while fully transparent. Its negative control at `initialAlpha = 0f` fails. Then make
  in-place replacement the host's behaviour, give every posted message its own identity, and start the arrival fade at
  `ENTER_START_ALPHA = 0.02f` (or use the fallback). Invert `ReplaceInPlaceTest`'s default-fade case and say so in the PR. Verify:
  `./gradlew :designsystem:testDebugUnitTest` passes, and the new test fails on 52c12ce5's host.
- [ ] 5.2 The order mechanism (design D3, ux rules 1–3). First write red tests on a sample screen: Tab from the top goes bar,
  message control, content; Shift+Tab reverses; the semantics traversal order matches; a message's appearance does not move focus;
  focus clears on leave and on replacement and never moves into the content; a leaving message is out of the focus and semantics
  order from its first exit frame (F8). Verify:
  `./gradlew :designsystem:testDebugUnitTest` passes, and the tests fail with the message placed after the content, as on
  52c12ce5.
- [ ] 5.3 Geometry is unchanged: the host's bounds and `onMessageBottomChanged` match the old placement within 1 px at 100% and 200%
  wherever the message fits. Verify: `MessagePlaceAndTapTest`, `MessageClearanceLayoutTest` and a new bounds test pass in
  `./gradlew :designsystem:testDebugUnitTest`.
- [ ] 5.4 Replacement size and guard (F1, F2, ux rules 4 and 5). With motion on, a shorter replacement eases the reported bottom
  over 250 ms (non-null to non-null only); a taller one moves it in the same frame; reduce motion is instant. The 0.8 s guard
  covers the taller of the old and new message. Verify: tests for both directions and for a tap at the old Dismiss strip pass in
  `./gradlew :designsystem:testDebugUnitTest`, and the guard test fails with the guard following only the new message (N8).
- [ ] 5.5 Fit rule (F5, ux rule 6). The message is always whole inside the safe area. It rises only as far as needed, never above
  safe top + 8 dp, and does nothing where it fits. Verify: a host test with an over-tall message in a short window, and a no-op
  test at normal sizes, pass in `./gradlew :designsystem:testDebugUnitTest`.

## 6. UI (screens)

- [ ] 6.1 All 13 screens use the order mechanism: Settings, Paywall, Doctor report, Your data, Reminders, History, My
  medications, Edit medication, Log medication, onboarding, Home, Attack details and Add past migraine. Verify: one Compose test
  per module for the Tab order passes in `./gradlew test`, and D165's Attack details tests stay green
  (`./gradlew :features:attack:ui:testDebugUnitTest`).
- [ ] 6.2 Home, History and Reminders replace an error with `Saved` on the event and ignore it otherwise. Doctor report clears
  an error on `Share`. Your data, My medications and Log medication show that their own message replaces the error, with no new
  code where none is needed. Verify: tests for every scenario of "An error on the other screens goes when a later save succeeds"
  pass in each module's `testDebugUnitTest`.
- [ ] 6.3 `ScreenLeaveObserver` on the 9 screens clears an error, and only an error, on leave. Verify: tests for "belongs to the
  visit" pass, and the leave test fails with a lifecycle `ON_STOP` clear in its place.
- [ ] 6.4 Home gates `reviewPrompt()` on "no message shows" and on a `rememberSaveable` "error this visit" flag that the
  `ScreenLeaveObserver` clears (design D5, F3, ux R1–R5). Verify: Home UI tests show the slot leaving with any message and
  returning when it goes; staying absent for the rest of the visit after an error, across rotation; and returning after a
  navigation away and back, and `./gradlew :features:home:ui:testDebugUnitTest :features:review:ui:testDebugUnitTest`
  passes.
- [ ] 6.5 Home Today placement (design D6, F5, F6, ux rule 7, P1–P6). In a short window the message sits in the start column. In
  every window it sits at `max(MESSAGE_OFFSET_BELOW_TOP_BAR, settingsRowHeight + 2.dp)`, from the row's measured height, never its
  position. Verify: `HomeScreen` placement tests pass in `./gradlew :features:home:ui:testDebugUnitTest`, covering:
  - 411 and 274 dp at 100% and 200%, LTR and RTL;
  - portrait 360 dp at 200% with a wrapped date;
  - a scroll that does not move the message;
  - updated `messagesBesideControls` and `messageTopOffset` cases.

## 7. Docs

- [ ] 7.1 `docs/ARCHITECTURE.md`, in the messages part: one line for the order mechanism, one for replacement in place and the
  never-transparent rule, and one for the "write saved" event on the other screens. Verify:
  `grep -n "write saved" docs/ARCHITECTURE.md` prints a line.

## 8. Gate and walk (android-engineer)

- [ ] 8.1 The gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`
  exits 0.
- [ ] 8.2 No permission added. Verify: `./gradlew :app:checkReleasePermissions` exits 0.
- [ ] 8.3 A walk on `emulator-5570`, debug: design checks A1, A7, A8, A12 (Attack details, History, Home), A16 (one screen), A17
  (ux M3 and M4) and A18 (ux P1, P4 and P6) at 100% and 200%. Restore storage, font scale, animator scale, rotation and locale. Verify: the results and what the
  walk does not prove are in the packet.

## 9. Review (code-reviewer)

- [ ] 9.1 Review `git diff origin/develop...bugfix/181-messages-everywhere` against D178 and the deltas. Areas at risk:
  - geometry of the 13 hosts;
  - D165's card mode and hold;
  - the leave clear (navigation, not lifecycle);
  - the review slot's cancel path and its visit flag;
  - the replacement easing, the taller-message guard and the fit rule;
  - focus clearing on leave and on replacement;
  - the inverted `ReplaceInPlaceTest` case.

  Fixes go on the same branch. Verify: no open blocker in the review packet.

## 10. QA (qa-engineer, independent, this diff only)

- [ ] 10.1 Run design checks A1–A20 and negative controls N1–N8 on the branch head SHA, debug and R8 release. Cover the ux spec's
  §3 tables O1–O20, F-a–F-h, M1–M12, P1–P7, S1–S4 and R1–R5. Its font cells apply: 100% and 200% on API 34+, and 130% on API 26. Scope is what the diff
  reaches (owner rule 2026-11-09). The whole-app regression stays nightly on `develop`. Verify: the table is in `docs/qa/` with the
  SHA, the counts and what it does not prove. The retest results go on #181, #185, #183 and #88, and on #153 for its message part.

## 11. Close (product-manager)

- [ ] 11.1 Acceptance against D178 and the ux spec's §3 rows: every EC-UX-35…38 and EC-REV-13 scenario is covered by a passing test or a QA row. Verify: the
  coverage list is in this file's verification record.
- [ ] 11.2 Archive on the branch with `openspec archive fix-messages-everywhere --yes`, then validate. Verify:
  `openspec validate --specs --strict` exits 0.
- [ ] 11.3 Hand to `team-lead`: PR into `develop`, tracker T63, and a note that `fix-keyboard-follows-cursor` branches after this
  merge. Verify: the PR number is in the packet.

## Verification record

To be filled in by the engineer, the reviewer and QA.
