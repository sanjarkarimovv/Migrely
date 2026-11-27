# Tasks

Base: `feature/fix-custom-tag-removal` from `origin/develop` 476f7a27 (team-lead decision 2026-11-27: 1.4.1, its own
branch, parallel to `fix-history-day-numbers`). BRD D199, EC-UX-91, EC-ATK-25, EC-ATK-26, EC-ATK-27, §13 row 64, D194 H2
reworded (#262). Commits say `Refs #179` / `Refs #262`, never a closing keyword. One writer per branch: android-engineer
takes it after this plan. Phase 1 = groups 2, 3.1–3.3, 4.1–4.2; phase 2 (Undo) = 3.4–3.6, 4.3 (design D6).

## 1. Plan

- [x] 1.1 BRD and this change; #262 wording in the `app-shell` MODIFIED delta and D194 H2. Verify:
  `openspec validate fix-custom-tag-removal --strict`.

## 2. Design system: menu placement (EC-UX-91, phase 1)

- [x] 2.1 Red first: `MigrelyDropdownMenuPlacementTest` (Robolectric, `designsystem/src/testDebug`): an anchor at 20%, 40%,
  60% and 80% down `w411dp-h343dp` and `w411dp-h267dp` (1080 × 900 and 1080 × 700 px at 420 dpi), font 200%, a menu with
  one 48 dp item: the menu's bounds never intersect the anchor's; a full window (`w411dp-h914dp`) places it under the
  anchor; a keyboard inset counts as the bottom; RTL aligns to the start edge. Named negative control: red on develop at
  ~40% in the 900 px window (record). State in the test whether the "no room for one item" case is reachable. Verify:
  `./gradlew :designsystem:testDebugUnitTest --tests '*MigrelyDropdownMenuPlacementTest*'`.
- [x] 2.2 Implement the placement in `MigrelyMenus.kt` (design D2); the D164 lift guard untouched. Verify: 2.1 green and the
  existing menu lift tests (`app-shell` "The menu check can fail") pass.
- [x] 2.3 A tap on the anchor's place while open only closes: a Compose test in `features/attack/ui` taps the chip's centre
  with the menu open and asserts no archive call and no selection change.

## 3. Domain and presentation

- [x] 3.1 Red first: `AttackDetailsViewModelTest`: after a successful archive the view model emits the removed-tag message
  (tag id, name, kind); a failed archive emits the existing error and no removed-tag message. Red on develop (no message;
  record). Verify: `./gradlew :features:attack:presentation:testDebugUnitTest --tests '*AttackDetailsViewModelTest*'`.
- [x] 3.2 Add the message model case and emit it in `onArchiveCustomTagAction` (design D3).
- [x] 3.3 Mapper and model tests for the new case (exhaustive `when`).
- [x] 3.4 Phase 2, red first: `RestoreCustomTagUseCaseTest` (`features/attack/domain/src/test`): under 100 active of that kind
  → `setCustomTagArchived(id, false)` and Restored; at 100 → LimitReached and no write; storage full and failure mapped.
  Verify: `./gradlew :features:attack:domain:test --tests '*RestoreCustomTagUseCaseTest*'`.
- [x] 3.5 Implement `RestoreCustomTagUseCase` (reusing `CountActiveCustomTagsRepository`, `SetCustomTagArchivedRepository`),
  wire it in `app` DI (`AttackModule`), and `onUndoRemoveCustomTagAction` in the view model: Restored → "Undone."; LimitReached
  → `message_tag_limit`; failures → the existing save errors. View model tests for each.
- [x] 3.6 A second removal replaces the message and Undo restores only the second tag (view model test).

## 4. UI

- [x] 4.1 Strings, phase 1: `message_tag_removed` EN "Removed from your list", DE "Aus deiner Liste entfernt". Verify: `git
  diff origin/develop -- 'features/attack/ui/src/main/res/values*/strings.xml'`.
- [ ] 4.2 Red first, then implement: Compose test (`features/attack/ui/src/testDebug`): the menu item and the custom action
  "Remove from my list" each lead to "Removed from your list" shown and announced once (polite live region); keyboard focus
  moves to the next chip or "+ Your own". Red on develop (no message; record).
- [ ] 4.3 Phase 2: the Undo action on the message (`attack_snackbar_undo`), spoken name `snackbar_undo_tag_description` EN
  "Undo removing %1$s", DE "Rückgängig: %1$s zurück in die Liste"; Compose test: Undo restores a chip with the same label and
  selected state and shows "Undone."; full-list refusal shows `message_tag_limit`; keyboard focus returns to the restored
  chip; a 40-character name with an emoji is spoken whole.

## 5. Docs and verify

- [x] 5.1 Gate. Verify: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exit 0 (check
  the load first; a timeout under load is rerun, not a result).
- [x] 5.2 Permissions. Verify: `./gradlew :app:checkReleasePermissions` exit 0; `reports/permissions/release.txt` equals
  develop 476f7a27's, and the merged release manifest is identical to develop's (`cmp` exit 0). Develop already has one
  `<queries>` block from the Billing library; the proof is identity, not a count.
- [ ] 5.3 Device walk, emulator-5570 (API 36), debug build of this branch; restore font scale, `wm size`, rotation, locale
  and TalkBack afterwards:
  - (a) 1080 × 900 and 1080 × 700 px at 200%: long-press custom chips at several heights; the menu never covers the chip;
    tap the chip's place with the menu open: it only closes. Develop's build first (negative control: covers at ~40%).
  - (b) Remove by the menu item, by TalkBack's custom action and with a keyboard: message shown (and spoken once); Undo
    restores; a selected tag stays on this attack.
  - (c) Fill a kind to 100 after removing one, then Undo: "Your list is full…".
  - (d) EN and DE at 200%, portrait 360 dp and landscape 360 dp tall: the message wraps, covers no control.
  - (e) TalkBack focus after removal and after closing the menu: record where it lands; the next double tap toggles no
    chip. If it does, stop and report to PM (design D4).
  - (f) Keyboard open while long-pressing (custom tag dialog closed, the note focused): the menu stays above the keyboard.
- [ ] 5.4 Independent code review (`code-reviewer`) on `git diff origin/develop...HEAD`, briefed with this spec and risks
  R1–R5. Fix real findings and record them here.
- [ ] 5.5 German check on the shipped `features/attack/ui/src/main/res/values-de/strings.xml` (the diff, not a table), T23
  style, by ux-designer: `message_tag_removed` and `snackbar_undo_tag_description`. If a string changes, the spec and design
  change first. No medical-reviewer: no health statement.
- [ ] 5.6 QA independent check (only what this diff reaches, with negative controls):
  - API 36 (emulator-5580): re-run `n1.py` and `n1rep.py` on the candidate (expected 0 overlaps; develop as the negative
    control); a TalkBack row for the custom action (message spoken, Undo name spoken); the full-list refusal.
  - API 26 (emulator-5582): (a) at 200% and one removal with Undo.
  - #262: re-read the existing #262 rows against `kept_px ≥ max(1, min(shown_px, line_with_clearance_px) − 1)` with the
    measured line; try the 1 px floor case (showed 1 px before a one-step reopen) and file a defect only if the field leaves
    the accessibility tree.
- [ ] 5.7 Archive: `openspec archive fix-custom-tag-removal --yes`; `openspec validate --specs --strict` passes.

## Scenario map

| Scenario | Proof |
|---|---|
| Popup: short window; check can fail; full window; keyboard; RTL; no room | 2.1, 5.3 (a), (f), 5.6 |
| Popup: tap on the chip's place | 2.3, 5.3 (a) |
| Popup: TalkBack, keyboard, reduced motion | existing menu tests (2.2), 5.3 (b) |
| Removing from the menu / TalkBack's action; check can fail | 3.1, 4.2, 5.3 (b), 5.6 |
| Undo; selected tag; list full; second removal; write fails | 3.4–3.6, 4.3, 5.3 (b), (c) |
| Leaving the screen; rotation | existing message-host rules, 4.2 (one assertion each) |
| Keyboard and TalkBack focus | 4.2, 4.3, 5.3 (e) |
| German; long and unusual names | 4.3, 5.5 |
| 200% and landscape | 5.3 (d) |
| During an attack, Free or Premium; backup and restore | No row: the message has no theme, entitlement or data branch; the archive flag and its backup are unchanged |
| #262 reopen wording | 5.6 (#262 rows re-read) |

## Verification record

(Filled by android-engineer, then QA.)

### Group 2, menu placement (android-engineer, 2026-11-27, JVM only, no device)

- 2.1 red on develop's `MigrelyMenus.kt` (476f7a27), Robolectric, native graphics, 420 dpi, font 200%:
  `:designsystem:testDebugUnitTest --tests '*MigrelyDropdownMenuPlacementTest*'` exit 1, 15 tests, 4 failed: 40% of
  700 px, menu (63,154,798,406) covers chip (63,280,429,429), Material's fallback to the anchor's middle; keyboard,
  menu bottom 761 past keyboard top 506; long menu, top 0 (window-top fallback) instead of under the chip; no room,
  menu under the keyboard. Green after 2.2: 15 of 15, and `MigrelyDropdownMenuTest` 11 of 11 (lift guard, and the
  menu's tree and bounds equal Material's raw menu).
- Deviation from the named control: at 40% of the 900 px window develop's menu does not cover the chip in Robolectric.
  On a device Material bounds the menu by the window's visible frame, less the status and navigation bars; Robolectric's
  popup sees the whole window. Two attempts to give Robolectric system bars (`SystemUi`, before and after launch) left
  the frame unchanged. The same failure shows at 40% of 700 px; the 900 px control stays with 5.3 (a), develop first.
- "No room for one item": reachable. 360 dp landscape at 200% with QA's Gboard cell (status 63 px, keyboard top
  318 px): chip 57 dp, about 12 dp a side; the menu is a 12 dp strip that scrolls, off its chip, the keyboard and the
  status bar (asserted).
- Mutation controls: develop's menu, 4 of 15 fail (as above); the helper without the `ime` inset, the 2 keyboard tests
  fail.
- 2.3 `CustomChipMenuTapTest` (taps routed by window, as a finger's): 2 of 2; the control in it (the same tap with no
  menu toggles the chip) passes; mutation `PopupProperties(focusable = false)` fails it (the chip toggles).

### Groups 3 and 4, phase 1, message without Undo (android-engineer, 2026-11-27, JVM only)

- 3.1 red: with the new API stubbed (signature and model, no emission),
  `:features:attack:presentation:test --tests '*AttackDetailsViewModelTest*'` exit 1, 52 tests, 1 failed: expected
  `[CustomTagRemoved(a, Jaw pain)]` but was `[Saved]`, which is what develop sends. Green after 3.2: presentation
  79 of 79. A failed or full-storage archive sends only the existing error (tests).
- 3.3 `AttackNotificationUiMapperTest` 9 of 9 and `AttackMessageBehaviourUiMapperTest` 11 of 11 with the new case
  (exhaustive `when`, no `else`).
- 4.1 strings: `message_tag_removed` EN "Removed from your list", DE "Aus deiner Liste entfernt".
- 4.2 `CustomTagRemovalMessageTest` 4 of 4: the menu item and TalkBack's custom action each hand the chip's tag to the
  ViewModel and show "Removed from your list" once, as a polite live region; leaving ends it; a restore keeps it.
  Mutation control: the removal mapped to `Saved` (develop's behaviour) fails 3 of 4 (the leaving test now first
  proves the message showed). Not ticked: its keyboard-focus part. Observed in a probe (not committed): after a
  keyboard removal through the menu, focus goes to the top bar's first control (bounds 8,15 to 48,55 px, likely Back)
  and the next Tab to the first row, not to the next chip or "+ Your own". Writing focus code waits for a
  product-manager decision (stop rule, design D4, R2).
- `ArchitectureScrollsTest`: the menu's card now names Material's `verticalScroll` in `MigrelyMenus.kt`; listed as a
  known site with its reason (no page, Material's tree kept), for code review.

### Groups 3 and 4, phase 2, Undo (android-engineer, 2026-11-27, JVM only)

- 3.4 red: `RestoreCustomTagUseCaseTest` against a stub that answers Restored and writes nothing,
  `:features:attack:domain:test --tests '*RestoreCustomTagUseCaseTest*'` exit 1, 6 tests, 5 failed (LimitReached,
  Failed twice, StorageFull, and the write never made). Green after 3.5: 6 of 6, domain 121 of 121. Mutation `>` for
  `>=` at the limit: the full-list test fails.
- 3.5 red: `AttackDetailsViewModelTest` with an empty `onUndoRemoveCustomTagAction`: 57 tests, 5 failed (Undone,
  TagLimitReached, StorageFull, SomethingWentWrong, and 3.6). Green: presentation 84 of 84. `RestoreCustomTagUseCase`
  reuses `CountActiveCustomTagsRepository` and `SetCustomTagArchivedRepository`; provided in `AttackModule`.
- 3.6 a second removal sends a second message with the second tag, and Undo restores only that tag: the restore use
  case is never run for the first tag (verified on the executor).
- 4.3 red: `CustomTagUndoTest` before any UI change: 6 of 6 failed, no Undo button (the German one first failed on my
  own helper, the German action label; fixed). Green after 4.3: 6 of 6 (Undo spoken "Undo removing Jaw pain"; Undo hands the tag back and
  "Undone." shows; full list shows `message_tag_limit`; a second removal replaces the first and Undo restores only the
  second; a 40-unit name with an emoji and accents spoken whole; German "Aus deiner Liste entfernt", "Rückgängig",
  "Rückgängig: Jaw pain zurück in die Liste"). Undo is triggered by its click action (as TalkBack does), not a touch;
  the touch path is the message host's existing one. Mutation: the screen's Undo wiring removed fails 3 of 6.
  Not ticked: 4.3's keyboard-focus part (focus back on the restored chip), same stop rule as 4.2.
- Test hang (fixed): an earlier run of `CustomTagRemovalMessageTest` called `performScrollTo` under a held Compose
  clock; the scroll waits for its own frames, so it never ended and ran the test JVM out of memory for about 20 min.
  Guard: in both classes every scroll and touch goes through `withClockRunning`, which restores the clock's previous
  state; JUnit's timeout rule cannot run with Robolectric. Rerun alone: 4 of 4 in 10 s; `CustomTagUndoTest` 6 of 6 in
  10 s.
- Gate (phase 2 tree): `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exit 0 in
  140 s; 4052 tests, 0 failed, 0 skipped. `:app:checkReleasePermissions` exit 0: ACCESS_NETWORK_STATE,
  POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, WAKE_LOCK, BILLING, DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION; no
  INTERNET.

### 5.1 and 5.2 (android-engineer, 2026-11-27)

- Gate, each phase on its own tree, load under 8: phase 1 (de91fa42, detached) exit 0 in 103 s, 4035 tests, 0 failed;
  phase 2 (da388b25 tree) exit 0 in 140 s, 4052 tests, 0 failed, 0 skipped.
- `:app:checkReleasePermissions` exit 0 on the branch and on develop 476f7a27 (same worktree, detached): `release.txt`,
  the merged release manifest (`merged_manifests/release`) and the merged main manifest (`merged_manifest/release`)
  are identical (`cmp` exit 0 each). No INTERNET. The diff touches no manifest, Gradle file, catalog or Room schema.

