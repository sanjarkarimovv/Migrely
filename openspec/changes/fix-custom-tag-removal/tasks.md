# Tasks

Base: `feature/fix-custom-tag-removal` from `origin/develop` 476f7a27 (team-lead decision 2026-11-27: 1.4.1, its own
branch, parallel to `fix-history-day-numbers`). BRD D199, EC-UX-91, EC-ATK-25, EC-ATK-26, EC-ATK-27, §13 row 64, D194 H2
reworded (#262). Commits say `Refs #179` / `Refs #262`, never a closing keyword. One writer per branch: android-engineer
takes it after this plan. Phase 1 = groups 2, 3.1–3.3, 4.1–4.2; phase 2 (Undo) = 3.4–3.6, 4.3 (design D6).
Amended 2026-11-27 after the device walk and ux rulings §5 (team-lead scope decision): IN = placement, message and Undo as
built, plus 4.4 (spoken on every path), 4.5 (German word), 4.6 (archived chip offers no remove). OUT = every focus target
after a removal or an Undo (ux K1, K3, K4, K5), moved to #296 for 1.5.0 (design D4). DROPPED = ux N1, no code (design
D2). Commits also say `Refs #296` where they touch the focus wording.

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
- [x] 4.2 Red first, then implement: Compose test (`features/attack/ui/src/testDebug`): the menu item and the custom action
  "Remove from my list" each lead to "Removed from your list" shown and announced once (polite live region). Red on
  develop (no message; record). The keyboard-focus part moved to #296 (design D4); done as recorded below.
- [x] 4.3 Phase 2: the Undo action on the message (`attack_snackbar_undo`), spoken name `snackbar_undo_tag_description` EN
  "Undo removing %1$s", DE as built "Rückgängig: %1$s zurück in die Liste" (changed by 4.5); Compose test: Undo restores
  a chip with the same label and selected state and shows "Undone."; full-list refusal shows `message_tag_limit`; a
  40-character name with an emoji is spoken whole. The keyboard-focus part moved to #296; done as recorded below.
- [x] 4.4 Spoken on every path (design D3). Red first: a Compose test that pins the chosen mechanism's observable
  condition, for example: on the menu path the message's live-region node first appears only after the menu's popup root
  is gone (no popup root in the rule's roots in that frame), and on the action path at once. Named negative control: red
  on 2d3ca53f's code (the message appears while the popup is still closing). Then implement; the touch path still shows
  the message once and Undo still works (rerun `CustomTagRemovalMessageTest`, `CustomTagUndoTest`). Verify:
  `./gradlew :features:attack:ui:testDebugUnitTest --tests '*CustomTag*'`. Acceptance is on the device, 5.3 (g).
- [x] 4.5 German: `snackbar_undo_tag_description` in `values-de/strings.xml` becomes "Rückgängig: %1$s zurück in deine
  Liste" (spec and design already say so); update the German assertion in `CustomTagUndoTest` first, so it is red on
  2d3ca53f, then the string. Verify: `git diff origin/develop -- 'features/attack/ui/src/main/res/values-de/strings.xml'`
  shows the two new keys with these values, and the test passes.
- [x] 4.6 Archived chip offers no remove (EC-ATK-28, design D7). Red first: a Compose test with a tag archived and selected
  on this attack: a long press opens no menu, the chip has no `onLongClickLabel`, and its semantics have no "Remove from my
  list" custom action; after Undo (un-archived) all three return; a past attack's archived chip likewise offers none.
  Named negative control: red on 2d3ca53f (menu, label and action present). Then pass no `onArchive` for an archived
  tag in `ChoiceGroup`. Verify: `./gradlew :features:attack:ui:testDebugUnitTest --tests '*ArchivedChip*'`.
- [ ] 4.7 Code review d4b2dcac S1 (design D7): a long press on an archived chip does nothing. Red first in
  `ArchivedChipTest`: after a finger long press on the archived, selected chip the selection is unchanged and nothing is
  sent to the ViewModel; the chip's semantics have no long-click action; a past attack's archived chip likewise. Named
  negative control: red on d4b2dcac (the long press acts as a tap and deselects). Then implement; a plain tap still
  toggles (assert). Verify: `./gradlew :features:attack:ui:testDebugUnitTest --tests '*ArchivedChip*'`.
- [ ] 4.8 Code review S2 (design D8): run the item's effect only when the closing finished, e.g.
  `onDispose { if (!expandedState.currentState) currentOnClosed() }` in `MigrelyMenus.kt`; invert
  `MigrelyDropdownMenuClosedTest` "It tells when it leaves the screen while open" so that leaving the screen while
  open or closing does not run the effect (red on d4b2dcac); update the KDoc at `MigrelyMenus.kt` (~line 68). Verify:
  `./gradlew :designsystem:testDebugUnitTest --tests '*MigrelyDropdownMenu*'`.
- [ ] 4.9 Optional, review N1: theme motion specs instead of the copied springs, only if the motion stays the same;
  otherwise keep and document (design D8).

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
  - (b) Remove by the menu item, by TalkBack's custom action and with a keyboard: message shown; Undo restores; a selected
    tag stays on this attack. Done in the first walk. R6 touch rows after 4.4 and 4.8: the message shows once, Undo works,
    and the D164 fast double tap on "Remove from my list" removes exactly once.
  - (c) Fill a kind to 100 after removing one, then Undo: "Your list is full…".
  - (d) EN and DE at 200%, portrait 360 dp and landscape 360 dp tall: the message wraps without "…" and sits where the
    screen's other messages sit; Undo inside the window. Take the same shot of develop's "Pain level removed" at 200% to
    show whether the top-bar overlap is pre-existing (design D3).
  - (e) Focus after removal: record only, for #296 (design D4); no stop rule in this change. Done in the first walk.
  - (f) Keyboard open while long-pressing (the note focused): the keyboard closes by itself and the menu opens full size,
    never on its chip. Done in the first walk (design D2).
  - (g) After 4.4 and 4.8, TalkBack on (log level VERBOSE, restored after): the menu path (double-tap-and-hold, double tap
    "Remove from my list") 3 runs and the custom-action path 3 runs. Pass: the TTS log shows "Removed from your list"
    actually spoken, once per run, not only an event handled and not flushed by the window-change speech ("Migrely", then
    the landing node). Record whether it comes before or after the landing node. Develop has no message, so no develop
    control; the before is 0 of 6 on the menu path and 1 of 1 on the action path (2d3ca53f).
  - (h) After 4.6 and 4.7: remove a tag selected on this attack; it offers no menu and TalkBack's actions offer no "Remove
    from my list". A finger long press and TalkBack's double-tap-and-hold on it each leave its selection unchanged and the
    chip on the attack (check the database). Undo: menu and action back. A past attack's archived chip: the same.
  - (i) After 4.8: tap "Remove from my list" and rotate within about 0.3 s (also one split-screen resize): the tag is still
    listed, the database shows it not archived, and no message shows. Then remove it normally: message and Undo.
- [ ] 5.4 Independent code review (`code-reviewer`) on `git diff origin/develop...HEAD`, briefed with this spec and risks
  R1–R5. Fix real findings and record them here. First full review: APPROVE WITH FIXES at d4b2dcac
  (`migrely-work/179/code-review-d4b2dcac.md`): S1 → 4.7, S2 → 4.8, S3 and N2 → spec, N1 → 4.9 (optional). A delta review
  of 4.7–4.9 before QA.
- [x] 5.5 German check on the shipped `features/attack/ui/src/main/res/values-de/strings.xml` (the diff, not a table), T23
  style, by ux-designer: done on 6a60c850 (ux rulings §5.3): `message_tag_removed` PASS; `snackbar_undo_tag_description`
  "die Liste" → "deine Liste" (task 4.5). ux confirms the shipped line after 4.5 in one line. No medical-reviewer: no
  health statement.
- [ ] 5.6 QA independent check (only what this diff reaches, with negative controls):
  - API 36 (emulator-5580): re-run `n1.py` and `n1rep.py` on the candidate (expected 0 overlaps; develop as the negative
    control); TalkBack rows for the menu path and the custom-action path (message spoken once each, from the TalkBack log;
    Undo name spoken); the archived chip of 5.3 (h), including a finger long press and double-tap-and-hold leaving its
    selection unchanged; rotation just after Remove (5.3 (i)); the full-list refusal.
  - API 26 (emulator-5582), without TalkBack: (a) at 200%, one removal with Undo, and the archived chip's long press (no
    menu, selection unchanged).
  - #262: re-read the existing #262 rows against `kept_px ≥ max(1, min(shown_px, line_with_clearance_px) − 1)` with the
    measured line; try the 1 px floor case (showed 1 px before a one-step reopen) and file a defect only if the field leaves
    the accessibility tree.
- [ ] 5.7 Archive: `openspec archive fix-custom-tag-removal --yes`; `openspec validate --specs --strict` passes.

## Scenario map

| Scenario | Proof |
|---|---|
| Popup: short window; full window; RTL | 2.1, 5.3 (a), 5.6 |
| Popup: check can fail | 2.1 at 1080 × 700 px (JVM); 1080 × 900 px on the device, 5.3 (a) develop first; QA `n1rep` |
| Popup: the item's effect after the menu has gone | 4.4, 4.8, 5.3 (b), (g) |
| Popup: rotation just after Remove | 4.8, 5.3 (i) |
| Popup: room for no item (arithmetic only) | 2.1 with a pinned inset; not reachable on a device, 5.3 (f) |
| Popup: the keyboard is open | 5.3 (f) |
| Popup: tap on the chip's place | 2.3, 5.3 (a) |
| Popup: TalkBack, keyboard, reduced motion | existing menu tests (2.2), 5.3 (b) |
| Removing from the menu / TalkBack's action; check can fail | 3.1, 4.2, 5.3 (b), 5.6 |
| Removing with TalkBack through the menu (spoken once) | 4.4, 5.3 (g), 5.6 |
| Undo; selected tag; list full; second removal; write fails | 3.4–3.6, 4.3, 5.3 (b), (c) |
| Leaving the screen; rotation | existing message-host rules, 4.2 (one assertion each) |
| Focus after a removal, a known limit | 5.3 (e) record; #296 |
| German; long and unusual names | 4.3, 4.5, 5.5 |
| 200% and landscape | 5.3 (d) |
| Archived chip: selected on this attack; Undo brings remove back; past attack; check can fail | 4.6, 5.3 (h), 5.6 |
| Archived chip: a long press does nothing; a plain tap as before | 4.7, 5.3 (h), 5.6 |
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

### Merge of develop f83dda71 and gate on the merged tree (android-engineer, 2026-11-27)

- `git merge origin/develop` (f83dda71, #293, tests only): no conflicts; merge commit 2c5b87c9. My tests use neither
  `PaneEvents.kt` nor `TalkBackTree.kt`.
- Each message test class alone first: `CustomTagRemovalMessageTest` 4 of 4, `CustomTagUndoTest` 6 of 6; each build
  11 to 16 s (the first call also waited about 15 min for Gradle to start, before any task ran: shared daemon or lock).
- KSP in the shared daemon then failed with "RestoreCustomTagUseCase could not be resolved" (24 errors) while the
  class was in the domain jar and `:app:compileDebugKotlin -x kspDebugKotlin` passed: the stale-daemon pattern after my
  detached checkouts. No daemon was stopped; the gate ran in a fresh single-use JVM:
  `nice -n 19 ./gradlew --no-daemon --max-workers=2 assembleDebug test ktlintCheck detekt lint` exit 0, 4061 tests,
  0 failed (7063 s wall time: the host slept on and off).
- `:app:checkReleasePermissions` on the branch and on develop f83dda71 (detached, same worktree): `release.txt` and the
  merged release manifest identical (`cmp` exit 0 each). No INTERNET.

### 5.3 device walk, emulator-5570 (API 36), develop f83dda71 first, then the branch 2c5b87c9 (android-engineer)

Evidence: `migrely-work/179/walk/` (`rows.jsonl`, shots, scripts). Fresh install (the app was not on the device),
six custom tags added in the app. Not ticked: rows (b) and (e) below have open findings.

- (a) 1080 x 900 and 1080 x 700 px, 200%, chip "Tingling" at several heights. Develop (control): the menu covers the
  chip at 30% and 40% of 900 px and at 30% to 50% of 700 px (#179 reproduced, `a-develop-1080x900-Tingling-40.png`);
  with the menu over the chip, a tap at the chip's centre removed the tag with no message. Branch: no overlap at 30%,
  37%, 40%, 50%, 60%, 70% (900 px) and 30% to 60% (700 px); below where it fits, else above. A tap on the chip's place
  with the menu open only closes it: the chip stays listed and unselected (2 heights). D164: a fast double tap on
  "Remove from my list" removed once; the second tap opened nothing and toggled nothing.
- (f) and the "no room" strip: in the 360 dp landscape window at 200% with Gboard up (keyboard top 318 px, "Perfume"
  at 121 to 270 px), a long press never shows the 12 dp strip. The menu is a focusable popup (as Material's always
  was), so window focus leaves the note and Gboard closes; the menu then opens full size below the chip. The recording
  shows the menu appearing while the keyboard slides away, partly under it for about 3 frames, never on its chip
  (`strip-360land-200-keyboard.png`, `strip-frames-sheet.png`, `strip-frames-early.png`). The strip that Robolectric
  shows (2.1) is not reachable by a finger here.
- (b) Menu path (no TalkBack): "Removed from your list" with Undo; Undo by finger touch: "Undone." and the chip back.
  A selected tag ("Jaw pain") stays shown and selected while archived; Undo un-archives it. A second removal replaces
  the message (one message, Undo spoken "Undo removing Neck stiffness") and Undo restores only the second tag.
  TalkBack custom action: "Removed from your list" spoken once (1 of 1), Undo spoken "Undo removing Tingling".
  **Finding:** TalkBack's menu path (double-tap-and-hold, double tap on the item): the message shows, but
  "Removed from your list" is not spoken, 0 of 4 runs; the popup's closing moves focus and its speech ("Migrely",
  then the focused node) replaces the polite announcement.
- (c) List full: removed "Tingling", 97 symptoms added behind the app (100 active), Undo by finger: "Your list is full.
  Remove one to add another."; the tag stayed archived (database), and adding its name later brought it back.
- (d) EN and DE at 100% and 200%, portrait and the 360 dp landscape window: full text, no "...", Undo inside the window,
  Undo by finger works in every cell (`m-en-sheet.png`, `m-de-sheet.png`). As the screen's other messages, it lies over
  the page's first row of chips, and at 200% over part of the top bar's date line.
- (e) Focus facts, observed only (no focus code):
  - Keyboard (adb key events): 23 Tabs to "Tingling"; a long Enter opens its menu (focus on the item); Enter removes.
    Focus then sits on the top bar's "Back"; the next Tab goes to "Undo removing Tingling". Enter on Undo: focus on no
    node; the next Tab goes to "Back". The spec's next chip / "+ Your own" / restored chip are not met.
  - TalkBack, custom action: focus ends on "Back" (speech "Undo removing Tingling", "Migrely", "Back"); the next double
    tap pressed Back and left the screen, ending the Undo. No chip toggled.
  - TalkBack, menu path: focus lands on the node that takes the removed chip's place: "Add your own symptom" after
    "Tingling" (2 of 2), "Tingling" after "Neck stiffness". **The next double tap selected "Tingling" on this attack**
    (speech "Selected"; its row appeared in `attack_tag`). This is R2: per design D4 the walk stopped here for a
    product-manager decision.
  - TalkBack, after Undo: "Undone." spoken; focus goes to "Dismiss", then "Back".
- Restored: TalkBack off and its log level back to ERROR, developer settings and the action timeout unset, font 1.0,
  size and density reset, rotation free, app locale reset, app uninstalled; settings snapshot before = after (no
  difference); emulator off. Side effect fixed during the walk: Tab presses typed tab characters into the note twice;
  the note was cleared both times (database length 0) before going on.


### Tasks 4.4 to 4.6 (android-engineer, 2026-11-28, JVM only, no device)

Base 58816faf; its `features/attack` and `designsystem` trees equal 2d3ca53f's (`git diff --stat 2d3ca53f 58816faf --
features/attack designsystem` empty), so each red run below is on 2d3ca53f's code with only the new test. Evidence:
`migrely-work/179/t83/` (red, green, mutants, gate, permissions).

- 4.4 mechanism: `MigrelyDropdownMenu` gets `onClosed`, run by a `DisposableEffect` placed before the `Popup`, so
  Compose forgets it after the popup's own effect has removed the window: it runs once the menu's window is gone, after
  its closing or when it leaves the screen while open. `ChoiceChip`'s "Remove from my list" item now only closes the
  menu and marks the choice; the removal (and so the screen's existing message) runs in `onClosed`. No second message
  path, no delay constant: the wait is the menu's own closing (Material's spring, about 0.3 s; none with "Remove
  animations"). The custom action, which has no popup, removes at once as before.
- 4.4 red: `:features:attack:ui:testDebugUnitTest --tests '*CustomTagRemovalMessageTest*'` exit 1, 6 tests, 1 failed:
  "Through TalkBack's menu the message appears only once the menu's window is gone": `FirstMessageFrame(frame=1,
  popupShown=true)`. The menu is opened by the chip's long-click action and the item picked by its click action, as
  TalkBack does, then the held clock is stepped frame by frame. "Through TalkBack's action the message appears at once"
  passed on 2d3ca53f too (frame 1, no popup) and stays green. Green: 6 of 6.
- 4.4 the touch path (R6): `removeThroughTheMenu` now idles once under the held clock after the touch, since the click
  lands at that idle and the removal then waits for the menu's closing; "Removing from the menu says Removed from your
  list" 6 of 6 with the rest; `CustomTagUndoTest` 6 of 6; `CustomChipMenuTapTest` 2 of 2 (a tap on the chip with the
  menu open still removes nothing). New `MigrelyDropdownMenuClosedTest` (designsystem) 2 of 2: `onClosed` is 0 in every
  frame while the popup is up during the closing, 1 in the first frame without it, still 1 after 120 more frames; and 1
  when the anchor leaves the screen with the menu open.
- 4.4 mutant M2 (`DisposableEffect(expanded)`, so `onClosed` runs as the closing starts): both tests fail ("still
  closing, frame 2 expected 0 but was 1"; `FirstMessageFrame(frame=3, popupShown=true)`).
- 4.5 red: `--tests '*CustomTagUndoTest*'` exit 1, 6 tests, 1 failed (German: no node "Rückgängig: Jaw pain zurück in
  deine Liste"). Green after `values-de` only: 6 of 6. `git diff origin/develop -- '.../values-de/strings.xml'` shows
  the two new keys: "Aus deiner Liste entfernt", "Rückgängig: %1$s zurück in deine Liste". EN unchanged.
- 4.6 red: new `ArchivedChipTest` exit 1, 3 tests, 2 failed, each `expected RemoveOffers(longPressLabel=null,
  actions=[], menu=false) but was RemoveOffers(longPressLabel=remove from my list, actions=[Remove from my list],
  menu=true)`: this attack's archived, selected symptom, and a past attack's archived, selected trigger. Fix:
  `ChoiceGroup` passes no `onArchive` for a `CustomTagUiModel` that is `archived` (the UI model already carried it).
  Green: 3 of 3; after Undo (the same tag un-archived) the label, the action and the menu are back and the chip stays
  selected. Mutant M3 (the `archived` check removed): the same 2 fail. The archived chip's long press is then its tap,
  as on a built-in chip (not asserted, no behaviour change asked).
- Gate: the first run in the shared daemon failed in 7 s at `:app:kspDebugKotlin` ("RestoreCustomTagUseCase could not
  be resolved", the stale-daemon pattern); no daemon was stopped. `--no-daemon` run: exit 1 in 194 s at ktlint (3 long
  lines in `ArchivedChipTest`, wrapped). Then `nice -n 19 ./gradlew --no-daemon --max-workers=2 assembleDebug test
  ktlintCheck detekt lint` exit 0 (load 5.8 at start); 4073 tests, 0 failed, 0 skipped.
- Permissions: `:app:checkReleasePermissions` exit 0 on the branch and on develop 00262358 (a detached worktree, removed
  after): `release.txt` and the merged release main manifest identical (`cmp` exit 0 each); no INTERNET. No manifest,
  Gradle, catalog or Room schema change.
- Not proved by these tests: that TalkBack speaks the message on a device (Robolectric has no TalkBack; walk row (g));
  the order of the window's removal and `onClosed` inside one frame (the JVM sees frames, not that order; it rests on
  Compose forgetting effects in reverse order, commented in the code).
