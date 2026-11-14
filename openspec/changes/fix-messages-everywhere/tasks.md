# Tasks

Base: `bugfix/181-messages-everywhere` from `origin/develop` 52c12ce5. Every commit and the PR say `Refs #181`, `Refs #185`,
`Refs #183` and `Refs #88` (and `Refs #153` where the short-window work touches it). Never use a closing keyword, because QA filed
these issues and retests them. No domain, data or permission change.

## 1. Plan (product-manager)

- [x] 1.1 BRD: D178, EC-UX-35…38, EC-REV-13, §13 row 46; EC-UX-16 and EC-UX-34 updated. Verify: `grep -c "^| D178 " docs/BRD.md`
  prints 1 and `git show origin/develop:docs/BRD.md | grep -c "^| D178 "` prints 0.
- [x] 1.2 Proposal, design, the `in-app-messages` and `in-app-review` deltas, tasks. Verify:
  `openspec validate fix-messages-everywhere --strict` exits 0.

## 2. UX (ux-designer, before any code)

- [ ] 2.1 Answer design Q1–Q8 in a ux spec. `team-lead` stores it in `migrely-work/t63/ux-spec.md`. Answer at least:
  - the Tab and TalkBack order: Back and the bar's actions, then the message control, then the content; on Home and onboarding,
    the message first (Q1, Q2);
  - where focus goes when the focused message leaves (Q3);
  - "Saved." per screen: Home in both modes, History and Reminders, plus Doctor report's silent clear (Q7);
  - the Home review request while "Couldn't save" or "Saved." shows (Q8);
  - the arrival fade's look and replacement in place (Q4, Q5);
  - the short-window Home Today placement (Q6).

  Verify: the spec file exists and answers each Q by number. Any answer that departs from a PM default comes back to
  `product-manager`, who updates D178 and the deltas before code starts.
- [ ] 2.2 PM folds ux's answers into the design and deltas where they differ. Verify:
  `openspec validate fix-messages-everywhere --strict` exits 0 after the update.

## 3. Presentation (android-engineer)

- [ ] 3.1 `HomeViewModel`, `HistoryViewModel` and `RemindersViewModel` emit one "write saved" event on each success in the design's
  table (middle column), and none on a result that writes nothing. Write the tests first, one per write path, plus one for each
  non-write result (`NotInProgress`, `Unchanged`, `NothingToRemove`). Verify:
  `./gradlew :features:home:presentation:test :features:history:presentation:test :features:reminders:presentation:test`
  passes, and the new tests fail first on 52c12ce5.

## 4. UI models, mappers and strings

- [ ] 4.1 `Saved` in the home, history and reminders message models: confirmation, polite, `READING_MILLIS`, no action, no
  haptic. The string is "Saved." in each module's `strings.xml`. Verify:
  `./gradlew :features:home:ui:testDebugUnitTest :features:history:ui:testDebugUnitTest :features:reminders:ui:testDebugUnitTest --tests '*MessageBehaviour*'`
  passes, with a case for `Saved` in each.

## 5. designsystem

- [ ] 5.1 Never transparent (design D4). First write a red test: in no frame of an arrival, and of a replacement, is the message's
  node in the semantics tree while fully transparent. Then make in-place replacement the host's behaviour and start the arrival
  fade above 0 (or use the fallback). Invert `ReplaceInPlaceTest`'s default-fade case and say so in the PR. Verify:
  `./gradlew :designsystem:testDebugUnitTest` passes, and the new test fails on 52c12ce5's host.
- [ ] 5.2 The order mechanism (design D3). First write red tests on a sample screen: Tab from the top goes bar, message control,
  content; Shift+Tab reverses; the semantics traversal order matches; a message's appearance does not move focus. Verify:
  `./gradlew :designsystem:testDebugUnitTest` passes, and the tests fail with the message placed after the content, as on
  52c12ce5.
- [ ] 5.3 Geometry is unchanged: the host's bounds, `onMessageBottomChanged` and the `TapGuard` match the old placement within 1 px
  at 100% and 200%. Verify: `MessagePlaceAndTapTest`, `MessageClearanceLayoutTest` and a new bounds test pass in
  `./gradlew :designsystem:testDebugUnitTest`.

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
- [ ] 6.4 Home gates `reviewPrompt()` on "no error and no `Saved` shown" (design D5). Verify: a Home UI test shows the slot
  leaving and coming back with the message, and `./gradlew :features:home:ui:testDebugUnitTest :features:review:ui:testDebugUnitTest`
  passes.
- [ ] 6.5 Home Today in a short window: the message sits in the start column, below the Settings row (design D6), and the offset
  or measured row height follows ux Q6. Verify: `HomeScreen` placement tests pass for 411 and 274 dp at 100% and 200%, LTR and RTL,
  with `messagesBesideControls` and `messageTopOffset` cases updated in `./gradlew :features:home:ui:testDebugUnitTest`.

## 7. Docs

- [ ] 7.1 `docs/ARCHITECTURE.md`, in the messages part: one line for the order mechanism, one for replacement in place and the
  never-transparent rule, and one for the "write saved" event on the other screens. Verify:
  `grep -n "write saved" docs/ARCHITECTURE.md` prints a line.

## 8. Gate and walk (android-engineer)

- [ ] 8.1 The gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`
  exits 0.
- [ ] 8.2 No permission added. Verify: `./gradlew :app:checkReleasePermissions` exits 0.
- [ ] 8.3 A walk on `emulator-5570`, debug: design checks A1, A7, A8, A12 (Attack details, History, Home), A16 (one screen) and
  A18 (411 dp) at 100% and 200%. Restore storage, font scale, animator scale, rotation and locale. Verify: the results and what the
  walk does not prove are in the packet.

## 9. Review (code-reviewer)

- [ ] 9.1 Review `git diff origin/develop...bugfix/181-messages-everywhere` against D178 and the deltas. Areas at risk:
  - geometry of the 13 hosts;
  - D165's card mode and hold;
  - the leave clear (navigation, not lifecycle);
  - the review slot's cancel path;
  - the inverted `ReplaceInPlaceTest` case.

  Fixes go on the same branch. Verify: no open blocker in the review packet.

## 10. QA (qa-engineer, independent, this diff only)

- [ ] 10.1 Run design checks A1–A19 and negative controls N1–N7 on the branch head SHA, debug and R8 release. Scope is what the diff
  reaches (owner rule 2026-11-09). The whole-app regression stays nightly on `develop`. Verify: the table is in `docs/qa/` with the
  SHA, the counts and what it does not prove. The retest results go on #181, #185, #183 and #88, and on #153 for its message part.

## 11. Close (product-manager)

- [ ] 11.1 Acceptance against D178: every EC-UX-35…38 and EC-REV-13 scenario is covered by a passing test or a QA row. Verify: the
  coverage list is in this file's verification record.
- [ ] 11.2 Archive on the branch with `openspec archive fix-messages-everywhere --yes`, then validate. Verify:
  `openspec validate --specs --strict` exits 0.
- [ ] 11.3 Hand to `team-lead`: PR into `develop`, tracker T63, and a note that `fix-keyboard-follows-cursor` branches after this
  merge. Verify: the PR number is in the packet.

## Verification record

To be filled in by the engineer, the reviewer and QA.
