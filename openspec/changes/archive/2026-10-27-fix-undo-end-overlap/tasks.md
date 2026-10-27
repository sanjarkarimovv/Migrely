# Tasks

## 1. Datasource

- [x] 1.1 Test first (instrumented `AttackDaoTest`): reopening is refused when an ended attack added later overlaps
  or starts at the end, allowed when another attack only touches the start or a deleted one overlaps, and refused for
  an attack that no longer exists
- [x] 1.2 `AttackDao.reopenIfNothingOverlaps` with the `overlapping` rule and an open end
- [x] 1.3 `ReopenDataModel.NotPossible`, mapped in `AttackDataSourceImpl.reopen`, with its test

## 2. Data and domain

- [x] 2.1 `ReopenResultDomainModel.NotPossible`, mapped in `HomeRepository.reopenAttack` and `UndoAttackEndUseCase`,
  with their tests

## 3. BRD and spec

- [x] 3.1 BRD EC-ATK-20
- [x] 3.2 Spec delta for `attack-logging`

## 4. Verify

- [x] 4.1 Gate: `assembleDebug test ktlintCheck detekt lint`
- [x] 4.2 Instrumented `AttackDaoTest` on emulator-5570
- [x] 4.3 `openspec validate fix-undo-end-overlap --strict`
- [x] 4.4 Emulator walk with a long accessibility timeout: end an attack through the 72-hour "It has ended" at a
  picked earlier end, add a past migraine after that end within the undo window, tap Undo: the attack stays ended and
  "This can no longer be undone." shows; with nothing added, Undo still reopens
- [x] 4.5 Independent review

## Verification record

- **Tests first:**
  - The new `AttackDaoTest` cases and the renamed unit tests failed to compile before the fix: "Unresolved reference
    'reopenIfNothingOverlaps'" and "'WouldOverlap'".
  - The review then renamed `WouldOverlap` to `NotPossible` and added the "starts at the end" case.
- **Unit tests:** `:datasource:implementation` 189, `:features:home:data` 30 and `:features:home:domain` 68, all pass.
- **Instrumented:** `AttackDaoTest` on emulator-5570 (API 36) passes 17/17, including all 7 reopen cases.
- **Gate:** `assembleDebug test ktlintCheck detekt lint` passes, before and after the review fixes.
- **Review** (independent) found nothing blocking. Changes made from it:
  - the spec scenario now uses a picked end; the first one could not happen, since a one-tap end puts the past
    migraine inside the attack, so Merge is offered;
  - the BRD row no longer lets an attack that touches the end through;
  - a test for an attack starting at the end;
  - the neutral name `NotPossible`, as in `RestoreDataModel`;
  - KDoc and the proposal mention the clock-moved-back case.
- **Emulator walk** (emulator-5570, debug build installed before the review's renames, so the same behavior; 23:12–23:38):
  - With the accessibility timeout raised, the "Migraine ended" message stays up (120 s at 120000).
  - The message survives going to "Add a past migraine" and back, and saving a migraine there.
  - **One-tap end (PASS):** attack 23:18–23:21, then a past migraine 23:25–23:30 added while Undo showed. Undo gave
    "This can no longer be undone.", and both attacks kept their times.
  - **72-hour "It has ended" with a picked end (PASS):** the emulator clock was set three days ahead. The end was
    picked at 00:30 and a past migraine 00:40–00:50 added. Undo was refused the same way, and both kept their times.
    The test attacks were deleted and the clock restored afterwards.
  - **Normal undo (PASS):** "Undone.", and the attack is in progress again from its original start, also after
    visiting "Add a past migraine" without saving.
  - No FATAL EXCEPTION or ANR in logcat.
- **Not checked:**
  - Another attack started during the window cannot be reached through the UI. "Migraine started" replaces the
    ended message, so its Undo is gone; the DAO test covers that case.
  - A past migraine starting exactly at the end's minute: the clock dial snaps to 5-minute marks.
