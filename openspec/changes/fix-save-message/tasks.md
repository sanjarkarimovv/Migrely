# Tasks

Base: `bugfix/166-save-message` from `origin/develop` c2761cba. Every commit and the PR say `Refs #166` and `Refs #167`, never a closing keyword, because QA
filed both issues and retests them. No domain or data change.

## 1. Plan

- [x] 1.1 BRD: D165, EC-UX-33, EC-UX-34, EC-ATK-22, §13 row 34. Verify: `grep -c "^| D165 " docs/BRD.md` prints 1, and
  `git show origin/develop:docs/BRD.md | grep -c "^| D165 "` prints 0.
- [x] 1.2 Proposal, design, the two deltas, tasks. Verify: `openspec validate fix-save-message --strict` exits 0.
- [x] 1.3 P8 moved to hotfix 1.1.1 as D167 (#171). Verify: `grep -c "D167" docs/BRD.md` prints at least 1.
- [ ] 1.4 `team-lead` files the follow-up issue for the 9 screens and D130's condition (D165 (7), (8)). Verify: its number is in this file's "Not done
  here".

## 2. Presentation

- [x] 2.1 `AttackDetailsViewModel` emits one success event when a write saves with no notification of its own (design D4). Write the tests first, one per
  write path: add a point, times, a note, a tag, an effect, an intake, a custom tag. Verify: `./gradlew :features:attack:presentation:test` passes, and the
  new tests fail first on the old code.

## 3. UI model and mapper

- [x] 3.1 `AttackMessageUiModel.Saved`: confirmation tone, polite, `READING_MILLIS`, no action. String `attack_message_saved` = "Saved.". Verify:
  `./gradlew :features:attack:ui:testDebugUnitTest --tests '*MessageBehaviour*'` passes with a case for `Saved`.

## 4. designsystem

- [x] 4.1 `MigrelySnackbar` gets an optional override of its live region (polite). By default nothing changes for any caller. Verify:
  `./gradlew :designsystem:testDebugUnitTest` passes, with a test that an error is assertive by default and polite when asked.

## 5. UI (Attack details)

- [x] 5.1 Card mode padding from `onMessageBottomChanged` (design D1), held until the message leaves. Compose tests: the message and card bounds do not
  intersect at 100% and 200%; "Got it" with a message keeps the content below it; no card, no padding. Verify:
  `./gradlew :features:attack:ui:testDebugUnitTest --tests '*AttackDetails*'` passes, and fails with the padding removed locally (N5).
- [x] 5.2 Once `hotfix/1.1.1` is in `develop`, merge it here and keep D167's card scroll working with card mode (its target is below the
  message). Verify: D167's tests pass on this branch.
- [x] 5.3 TalkBack order (design D3): the message stored at once and shown 200 ms after the card becomes a live region (after D167's 300 ms
  scroll, or one frame after layout), its time counted from when it shows, polite in card mode, the card's title a heading. Verify: a test that the
  message's state is set after the card's, and a semantics test for `heading()` and the live regions.
- [x] 5.4 The error replaced on success, never on the attempt (design D4), and cleared on leave but not on Home, a dialog or a rotation (design D5).
  Verify: tests for scenarios C1–C5 in the `in-app-messages` delta pass, and the leave test fails with a lifecycle `ON_STOP` clear in its place.

## 6. Docs

- [x] 6.1 `docs/ARCHITECTURE.md`: one line in the messages part about card mode and the success event. Verify: `grep -n "card mode" docs/ARCHITECTURE.md`
  prints a line.

## 7. Gate and walk

- [x] 7.1 The gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0.
- [x] 7.2 `:app:checkReleasePermissions`. Verify: exit 0, no permission added.
- [x] 7.3 A walk on `emulator-5570`, debug: checks 1, 5, 10 and 14 of design "Acceptance" at 100% and 200%, with the disk filled and then freed. Restore
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

## Record (android-engineer, 2026-11-09)

Branch `bugfix/166-save-message`, gate and walk on 7e274503 (develop 6cc48123 merged in, D164). Evidence in
`migrely-work/walks/t53/` (outside the repo): `gate.log`, `gate.exit`, `walk.jsonl`, `tb.jsonl`, `negctl.txt`, `shots/`.

- 2.1: 10 new or changed ViewModel tests failed first on the old code (10 of 10), and the 3 negative ones passed. Then 76 of 76.
- 3.1 and 4.1: red first as a compile error (non-exhaustive `when`). designsystem: `MessageClearanceTest` 4, `MessageClearanceLayoutTest` 3,
  `SnackbarLiveRegionTest` 4. Control: with `clearOfMessage` made a no-op, the push test fails (row at 64 dp under a message ending at 116 dp).
- 5.1, 5.3, 5.4: `SaveMessageTest` 25 of 25. Controls, each run against it and then restored byte-identical: no card-mode padding (N5), 7 fail;
  no polite override (N6), 1 fails; no hold behind the card, 1 fails; an `ON_STOP` clear in place of the back stack, 1 fails (Home); `Saved` ignored, 1 fails.
- 5.2: D167's `SafetyCardRevealTest` 6 of 6 and `SafetyCardRestoreTest` 1 of 1 on this branch.
- Deviation from design D3: the message waits until the card is a live region, after D167's scroll, plus 200 ms, not one frame. Compose 1.9.3 sends
  accessibility events in 100 ms batches (read from the class), and in one batch TalkBack spoke the message first (#166, 36 ms).
- D52's push moved from Home into `:designsystem` (`Modifier.clearOfMessage`), so both screens share one copy. Home's landscape attack mode was not
  walked on a device after the move; its padding test moved with it.
- 7.1, 7.2: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint :app:checkReleasePermissions` exit 0 on 7e274503, 2832
  tests, 0 failed, `ArchitecturePopupsTest` 1 of 1. Release permissions as on develop, no INTERNET.

### Walk, emulator-5570, API 36, 1080x2400, debug

`/data` filled under 64 KB with `fallocate`. Rows R1–R10 and TB1–TB2 on 22877917 (before the D164 merge), R11–R13 and TB3 on 7e274503. The merge
changed only the custom-tag menu on this screen.

| Row | Check | Font | Result |
|---|---|---|---|
| R1 | 1, failed first 10, unscrolled | 100% | Pass. Message y 328–433, content from 465, title in the tree at 539, "Got it" 834 |
| R2 | 1 + 5, failed first 10, scrolled down | 100% | Pass. Same geometry as R1 to the pixel (the #174 control) |
| R1 | 10, free space, add the 10 again | 100% | Pass. "Saved." in the 1.0 s frame, "Couldn't save" gone, "Peak 10/10"; gone after about 12 s |
| R3 | 16, rotate with error and card | 100% | Pass. Landscape: title below the message; back in portrait, same geometry as R1 |
| R4 | 16, background, `run-as kill -9`, reopen | 100% | Pass. New pid; error and card restored, same geometry |
| — | 15, Home, Android Settings storage, back | 100% | Pass. Error and card still shown |
| R5 | 14, error, Add medication, Back | 100% | Pass. No error, card still there |
| R5 | 14, error, Back to Home, reopen | 100% | Pass. No error |
| R6 | 6, "Got it" while the message shows | 100% | Pass. Message moved 0 px, content stays at 465; Dismiss by touch closes it, content back at 296 |
| R7 | 9, error with no card | 100% | Pass. Overlay as before: content from 296, under a message at 328–433 |
| TB1 | 2, TalkBack, failed first 10, unscrolled | 100% | Pass. Card spoken whole (title, sentence, Got it, Button; INTERRUPT), then the message whole (QUEUE) |
| TB2 | 12 and 10 with TalkBack | 100% | Pass. Failed retry spoken again; "Saved." spoken, QUEUE |
| R8 | 1 + 5, failed first 10, scrolled down | 200% | Pass. Message 328–517, content from 549, title 623; whole card below the message |
| R8 | 10, add the 10 again | 200% | Pass. "Saved." in the 1.0 s frame, "Peak 10/10" |
| R9 | 1, failed first 10, unscrolled control | 200% | Pass. Same geometry as R8 to the pixel |
| R10 | 8, landscape with error and card | 200% | Pass. Content area y 484–1080 (about 227 dp), title below the message, "Got it" scrolls into reach |
| TB3 | 2 + 5, TalkBack, scrolled down, merged build | 100% | Pass. Card whole first, message QUEUE after |
| R11 | 10, merged build | 100% | Pass. "Saved.", "Peak 10/10" |
| R12 | 3, Undo message while the card shows | 100% | Pass for placement (card and content below the Undo). The Undo tap from the driver came after a dump and a screenshot, late in its 5 s, and did nothing, as a message that is leaving should. Repeated in R13 |
| R13 | 3, Undo tapped at 1.2 s while the card shows | 100% | Pass. "Undone.", the point is back |

TalkBack's log level was set to verbose for TB rows and restored byte-identical. TB1's first attempt was void: TalkBack, restarted on a full disk,
crashed in its own WorkManager (`SQLiteFullException`), so the driver now starts TalkBack before filling the disk. Settings restored to the baseline (font
1.0, rotation 0, auto-rotate on, animator 1, TalkBack off, long press 400), app data cleared back to empty as found, temp files removed, 5570 off.

**Not checked on a device**, and why: 130%; heading navigation, switch access and keyboard order (19, 20; heading covered by a Robolectric test);
`ar-XB` (21); "Remove animations" (22); the 60 fps flash capture (23); the 0.8 s guard after Dismiss in card mode (7); light theme; a note, a tag and an
effect as the later save (11, covered per path by ViewModel tests and once on the device by a location chip under TalkBack); D158's top-bar date line
lies under the message's top edge at 200%, as before this change. These are QA's checks in 8.1. A real phone and human hands are not covered.

### Review walk (970de3a6), task 9.1 record

`migrely-work/walks/t53/review-walk.txt`, `emulator-5570`, API 36, 1080x2400, debug APK of 970de3a6.

| Row | Check | Result |
|---|---|---|
| W1 | Check 10 with TalkBack: failed first 10, disk freed, the same 10 again (card and "Saved." from one action) | Pass. Title, sentence, "Got it", then "Saved." (QUEUE). "Saved." spoken 274 ms after the card. "Peak 10/10" stored |
| W2 | Rotation during the hold, scrolled down (D167's 300 ms scroll plus 200 ms) | Pass. "Couldn't save" present after the recreation, in landscape and back in portrait, with the content below it. Inside the hold by timing only, not seen directly |
| W3 | Going forward during the hold | Not walked: three attempts were void (taps too close, then a full disk took down system processes, with no Migrely crash). The proof is the unit test `SaveMessageTest` "Given an error held behind the card when the user goes forward then it is gone after", red on 3f2ca3fb and green on 970de3a6 |

Settings, TalkBack preferences and app data were restored to the baseline, and 5570 is off.

## Not done here

The other 9 screens and D130's condition (follow-up issue: number pending, task 1.4); "Got it" removing the card at once; #88, #153; the "storage full"
wording. See the proposal.
