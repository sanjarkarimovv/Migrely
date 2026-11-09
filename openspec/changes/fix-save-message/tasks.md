# Tasks

Base: `bugfix/166-save-message` from `origin/develop` c2761cba. Every commit and the PR say `Refs #166` and `Refs #167`, never a closing keyword, because QA
filed both issues and retests them. No domain or data change.

## 1. Plan

- [x] 1.1 BRD: D165, EC-UX-33, EC-UX-34, EC-ATK-22, §13 row 34. Verify: `grep -c "^| D165 " docs/BRD.md` prints 1, and
  `git show origin/develop:docs/BRD.md | grep -c "^| D165 "` prints 0.
- [x] 1.2 Proposal, design, the two deltas, tasks. Verify: `openspec validate fix-save-message --strict` exits 0.
- [ ] 1.3 `qa-t53`'s P8 result (ux check 5 on c2761cba) goes into design D2 and the packet to `team-lead`, who re-rates the severity if the card appears
  off-screen. Verify: the result and its evidence path are in design D2.
- [ ] 1.4 `team-lead` files the follow-up issue for the 9 screens and D130's condition (D165 (7), (8)). Verify: its number is in this file's "Not done
  here".

## 2. Presentation

- [ ] 2.1 `AttackDetailsViewModel` emits one success event when a write saves with no notification of its own (design D4). Write the tests first, one per
  write path: add a point, times, a note, a tag, an effect, an intake, a custom tag. Verify: `./gradlew :features:attack:presentation:test` passes, and the
  new tests fail first on the old code.

## 3. UI model and mapper

- [ ] 3.1 `AttackMessageUiModel.Saved`: confirmation tone, polite, `READING_MILLIS`, no action. String `attack_message_saved` = "Saved.". Verify:
  `./gradlew :features:attack:ui:testDebugUnitTest --tests '*MessageBehaviour*'` passes with a case for `Saved`.

## 4. designsystem

- [ ] 4.1 `MigrelySnackbar` gets an optional override of its live region (polite). By default nothing changes for any caller. Verify:
  `./gradlew :designsystem:testDebugUnitTest` passes, with a test that an error is assertive by default and polite when asked.

## 5. UI (Attack details)

- [ ] 5.1 Card mode padding from `onMessageBottomChanged` (design D1), held until the message leaves. Compose tests: the message and card bounds do not
  intersect at 100% and 200%; "Got it" with a message keeps the content below it; no card, no padding. Verify:
  `./gradlew :features:attack:ui:testDebugUnitTest --tests '*AttackDetails*'` passes, and fails with the padding removed locally (N5).
- [ ] 5.2 The card comes into view (design D2): scrolled away, it scrolls once; already visible, nothing moves; a restore does not scroll; jump with
  reduced motion. Verify: the same command, with tests for each case.
- [ ] 5.3 TalkBack order (design D3): the message one frame after the card, polite in card mode, the card's title a heading. Verify: a test that the
  message's state is set after the card's, and a semantics test for `heading()` and the live regions.
- [ ] 5.4 The error replaced on success, never on the attempt (design D4), and cleared on leave but not on Home, a dialog or a rotation (design D5).
  Verify: tests for scenarios C1–C5 in the `in-app-messages` delta pass, and the leave test fails with a lifecycle `ON_STOP` clear in its place.

## 6. Docs

- [ ] 6.1 `docs/ARCHITECTURE.md`: one line in the messages part about card mode and the success event. Verify: `grep -n "card mode" docs/ARCHITECTURE.md`
  prints a line.

## 7. Gate and walk

- [ ] 7.1 The gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0.
- [ ] 7.2 `:app:checkReleasePermissions`. Verify: exit 0, no permission added.
- [ ] 7.3 A walk on `emulator-5570`, debug: checks 1, 5, 10 and 14 of design "Acceptance" at 100% and 200%, with the disk filled and then freed. Restore
  storage, font scale and animator scale. Verify: the results are in the packet, with what a walk does not prove.
- [ ] 7.4 `code-reviewer` reviews. Fixes go on the same branch. Verify: no open blocker in the review packet.

## 8. QA (independent)

- [ ] 8.1 `qa-engineer` runs design "Acceptance" checks 1–17 and 19–23 and negative controls N1–N8, debug and R8. Verify: the table is in `docs/qa/` with
  trial counts and what it does not prove, and #166 and #167 get the retest comments.
- [ ] 8.2 If TalkBack drops the polite message (check 2), the design D3 fallback is built and check 2 is run again. Verify: check 2 passes on the
  fallback, or the result goes to `team-lead`.

## 9. Close

- [ ] 9.1 Archive the change, then open a PR into `develop` with `Refs #166` and `Refs #167`, and merge it with a merge commit. If
  `bugfix/161-menu-second-tap` merged first and took a number this branch uses, renumber. Verify: `openspec validate --specs --strict` exits 0 after the
  archive, and `gh pr view` shows it merged.

## Not done here

The other 9 screens and D130's condition (follow-up issue: number pending, task 1.4); "Got it" removing the card at once; #88, #153; the "storage full"
wording. See the proposal.
