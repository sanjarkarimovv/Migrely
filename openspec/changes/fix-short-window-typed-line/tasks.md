# Tasks

Base: `bugfix/245-short-window-line` (worktree `migraine-app-245`) from `origin/develop` 76c38c38, with
`origin/ux/245-short-window-cursor` 11f26d76 merged (fast-forward). Release: 1.3.2, tracker T75. Write `Refs #245` in every
commit and in the PR into `develop`, never a closing keyword: QA filed #245 and closes it after the retest. BRD D192, EC-UX-39,
EC-UX-40, EC-UX-43, EC-UX-72 to EC-UX-77. Design: `docs/ux/245-short-window-cursor.md` (R1 to R12, SW1 to SW13) and this
change's `design.md` (D1 to D6, rows K1, K2). QA's numbers: #245 comment 6970307531.

Ownership:
- `product-manager`: 0 (done) and 8;
- `ux-designer`: 1 (done);
- `android-engineer`: 2 to 5;
- `code-reviewer`: 6;
- `qa-engineer`: 7 (emulator-5580 API 36 Gboard, then emulator-5582 API 26, one at a time).

No `medical-reviewer`: no string and no health sentence changes. Layers: `designsystem`, then the `ui` modules of
`features/attack`, `features/medication` and `features/settings`. Every test is written first and shown red on `develop`
76c38c38, then green; each mutation must turn its test red again. Logs go to `migrely-work/245/eng/`. IME insets are driven
by hand in Robolectric, as `KeyboardRoomTest` does.

## 0. Plan (`product-manager`)

- [x] 0.1 BRD D192, EC-UX-72 to EC-UX-77, EC-UX-39, EC-UX-40 and EC-UX-43 pointers, §13 row 56; proposal, design, `app-shell`
  delta, tasks. Verify: `openspec validate fix-short-window-typed-line --strict` and `openspec validate --all --strict` exit 0.

## 1. Design (`ux-designer`)

- [x] 1.1 R1 to R12 and SW1 to SW13, corrected with QA's re-measure: `docs/ux/245-short-window-cursor.md` (11f26d76). Scope by
  `team-lead` 2026-11-18: R5, R3, R1 kept; R4 defined, not built; R2 reduced and conditional on 2.0. Verify:
  `git log --oneline -1 -- docs/ux/245-short-window-cursor.md` on this branch.

## 2. The line and the trigger (`android-engineer`, `designsystem`)

- [ ] 2.0 **First, before any fix (team-lead condition):** a red test on the code as on v1.3.1 (`develop` 76c38c38) that shows
  the line being typed cut in the R2 band: a note field at 200% in a scroll whose visible part H is between L and L + M (for
  example H = L + 10 px), keyboard inset applied, cursor at the end and then at the start; assert the cursor line's bounds lie
  fully inside the visible part. It must fail on 76c38c38 because the follow aligns the line-plus-margins rect with the edge.
  Verify: `migrely-work/245/eng/2.0-red.log` with the failing assertion and the measured overlap in px. **If it cannot be made
  red honestly (no tuned numbers, no fake layout), stop:** report to `product-manager`, who removes the requirement "Margins
  give way before the line is cut", EC-UX-73 and SW13; skip 2.2.
- [ ] 2.1 The decision (D1, R5, R9). A shared state in `designsystem` that, at the keyboard's final height, decides
  `barAside = H < L` with H measured under the bar and L from the focused field's layout, holds it until the keyboard closes,
  and decides again on a new opening, rotation or font change. Verify: `./gradlew :designsystem:testDebugUnitTest --tests
  '*TopBarAside*'` (or the chosen name) with cases: H 61 and 87 px with L 112 → aside; H 137 → stays; H 156 → stays; cursor
  moves, keyboard height change and a message while aside → no change; no IME inset → never. Red where new. Mutation:
  constant 64 dp trigger → the 156 px case red; re-deciding on every layout → the height-change case red.
- [ ] 2.2 Reduced R2 (D4), only if 2.0 was red. Verify: the 2.0 test green; a case at H = 137 px asserting margins stay 4 and 21
  px by cursor direction (not evened). Mutation: even out the margins → red; restore today's rect → 2.0 red again.
- [ ] 2.3 The bar's height follows the inset fraction (D2, R6) and its semantics leave while aside (D3, R11). Verify: a test
  that steps the IME inset through 0, 25, 50, 100% and asserts the bar's height goes full → 0 in proportion, no alpha below 1
  on its content, and with the bar aside no semantics node of Back, the title or an action exists (unmerged tree). One step
  when the inset jumps (API 26–29 path) and with animations off. Mutation: hide with alpha only → the semantics case red.

## 3. Screens (`android-engineer`, `ui` modules)

- [ ] 3.1 Attack details (note) through `MessageLayout`'s top bar slot. Verify: a screen test at 200%, 891x360 dp-equivalent
  window with Gboard's measured inset (H 61 px under a 194 px bar at density 2.625): after typing, the cursor line's bounds are
  whole with ≥ 8 dp on both sides and no Back node; red on 76c38c38 (40 of 112 px). Same at the 411 dp window: Back present.
- [ ] 3.2 Edit medication (name, dose) and Log medication (name, normal and dark room). Verify: the same assertions with a
  168 px bar and H 87 px; red on 76c38c38; dark-room case asserts the area the bar left is `AttackModeBackground`.
- [ ] 3.3 Delete all data: the row decision first (D180 (8)), then the bar. Verify: a test in `DeleteKeyboardTest` where the
  row stays behind and the input line still does not fit → bar aside, line whole; where the line fits with the row behind →
  bar stays. Red where the first case is reachable on 76c38c38; if not reachable, say so in the record.
- [ ] 3.4 Messages (D6, R10). Verify: with a message shown, its bounds are identical with the bar present and aside. Mutation:
  offset the message by the bar's height → red.

## 4. #238 guards, merge and gate (`android-engineer`)

- [ ] 4.1 Keyboard-room guards (D5, team-lead condition). Verify: `KeyboardRoomTest`, `AttackDetailsKeyboardRoomTest`,
  `EditMedicationKeyboardTest`, `LogMedicationKeyboardTest` and `DeleteKeyboardTest` all green with no assertion changed. New
  cases with the bar aside: on close the scroll value is unchanged, the field is lower by exactly the bar's height, and no
  scroll happens (scroll state's value and `isScrollInProgress` checked). Mutation: keep the field's screen position on close
  by `scrollBy(barHeight)` → red.
- [ ] 4.2 If #239 has merged into `develop` by now, merge `origin/develop` and resolve `EditMedicationScreen` and
  `LogMedicationScreen`. Verify: both changes' test classes green on the merged tree; record `git log --oneline -1
  origin/develop`.
- [ ] 4.3 Full gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`
  exit 0, with the counts of tests run (never `testDebugUnitTest` alone).
- [ ] 4.4 No permission, string or manifest change. Verify: `./gradlew :app:checkReleasePermissions` exit 0 and
  `git diff origin/develop -- '*.xml' '*strings*' '*AndroidManifest*'` empty except tests.

## 5. Device walk (`android-engineer`, emulator-5570, headless first)

- [ ] 5.1 SW1 and SW2 (Edit name, Log name) at `wm size 945x2100`, API 36 Gboard, 200% landscape, QA's method (no drag after
  the keyboard opens; cursor at end, start, middle). Verify: per cell the visible part, the caret top and bottom and the bar's
  absence from a `uiautomator dump`, on the candidate (hash read back) and v1.3.1.
- [ ] 5.2 SW5 / K2 with TalkBack VERBOSE: close by Back and by the hide key. Verify: the TalkBack lines and 0 TYPE_VIEW_SCROLLED
  in 3 s; field bounds before and after (lower by the bar's height).
- [ ] 5.3 SW3 at 411 dp and SW6 in portrait: the bar stays. Verify: Back's bounds in the dump.
- [ ] 5.4 Restore font scale, rotation, `wm size`, density, animator scales, TalkBack and keyboard. Verify: settings read back.

## 6. Review (`code-reviewer`)

- [ ] 6.1 Review sections 2 to 4 against design D1 to D6: one shared mechanism, decision once per opening from measured H and L,
  no dp constant, height follows the inset, semantics removed while aside, no scroll on close, KeyboardRoom untouched in
  behaviour, messages unmoved, no string. Verify: a review packet with findings, each fixed or answered before QA.

## 7. Acceptance (`qa-engineer`, this diff only)

- [ ] 7.1 SW1 to SW13 (ux section 7) and K1, K2 (design "Acceptance") on the PR head, with v1.3.1 as the negative control (SW1:
  40 of 112 px; SW2 Edit and Log name: 68 of 112 px; dose and Delete all data measured on v1.3.1 first), the mutations named
  there, SW4 as an API 26 guard, SW13 only if 2.0 was red. APK hashes read back on the device. Verify: a QA record
  `docs/qa/fix-short-window-typed-line.md` with each row, build SHA, result and control result; an unreachable cell is "not
  reachable", never a pass.
- [ ] 7.2 Restore every device setting. Verify: the settings read back in the QA record.

## 8. Close (`product-manager`, then `android-engineer`)

- [ ] 8.1 Before archive: diff this change's MODIFIED "The window does not pan for the keyboard" and "Closing the keyboard never
  moves the page" against the live `openspec/specs/app-shell/spec.md`; sync if another change rewrote them since 2026-11-18.
  Verify: the diff shows only this change's body edits and the two rewritten scenarios.
- [ ] 8.2 BRD D192 updated with QA's results (what was proved, what stays open, the R2 outcome), then archive. Verify:
  `openspec archive fix-short-window-typed-line --yes` and `openspec validate --all --strict` exit 0.
- [ ] 8.3 PR into `develop` with `Refs #245`, merge commit, worktree removed after merge, tracker T75 updated by `team-lead`.
  Verify: `gh pr view <n> --json state,mergeCommit` shows MERGED.
