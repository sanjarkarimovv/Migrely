# Tasks

Base: `bugfix/238-talkback-focus-on-keyboard-close` (worktree `migraine-app-238`), started at 294c10c6 (1.3.0's head) with
a1efa2bc, then merged with `origin/develop` c49b9e09 and `origin/ux/238-keyboard-room` b6131408. Release: 1.3.1, tracker T74.
Write `Refs #238` in every commit and in the PR into `develop`, never a closing keyword: QA filed #238 and closes it after the
retest. BRD D189, D180 (5), EC-UX-43, EC-UX-61 to EC-UX-65. Design: `docs/ux/238-keyboard-room.md` (R1 to R14, KR1 to KR16).

Ownership:
- `product-manager`: 0 (done) and 8;
- `ux-designer`: 1 (done) and the band review in 5.3;
- `android-engineer`: 2 to 5;
- `code-reviewer`: 6;
- `qa-engineer`: 7 (emulator-5580 API 36, then emulator-5582 API 26, one at a time).

No `medical-reviewer`: no string and no health sentence changes. Layers: UI only (no domain, presentation, data, DI or
navigation task).

## 0. Plan (`product-manager`)

- [x] 0.1 BRD D189, D180 (5), EC-UX-43 rewritten, EC-UX-61 to EC-UX-65, §13 row 53; proposal, design, `app-shell` delta, tasks.
  Verify: `openspec validate keep-page-when-keyboard-closes --strict` and `openspec validate --all --strict` exit 0.

## 1. Design (`ux-designer`)

- [x] 1.1 Rules R1 to R14, per-screen notes, acceptance KR1 to KR16: `docs/ux/238-keyboard-room.md` (b6131408), accepted by
  `team-lead`. Verify: the file is on this branch (`git log --oneline -1 -- docs/ux/238-keyboard-room.md`).

## 2. Helper (`android-engineer`, `designsystem`)

- [x] 2.1 Existing part: a1efa2bc adds `designsystem/.../input/KeyboardRoom.kt` and `KeyboardRoomTest` (Robolectric) and wires
  Attack details. Not yet reviewed (section 6). Verify: `git show --stat a1efa2bc`.
- [x] 2.2 Let the helper take the visible part's bottom padding from a layout, not only from a `WindowInsets`, so Delete all
  data's row (`rides` or `behindKeyboard`) can feed it (design D6). One helper for four screens; no second copy. Verify:
  `./gradlew :designsystem:testDebugUnitTest --tests '*KeyboardRoomTest*'` green, plus a new case for a non-inset source that is
  red when the source is ignored.
  - Record (2026-11-17, android-engineer): `rememberKeyboardRoom(scrollState, viewportBottom: Density.() -> Int)`, read
    in layout; the `WindowInsets` overload delegates to it, so one helper. The source is held by `rememberUpdatedState`,
    so a recomposition never makes a new room (a1efa2bc keyed it on the insets object). New cases in `KeyboardRoomTest`
    with a page ended by a layout-time row padding: offset kept on close, room gone after scrolling back. Red with the
    source ignored (`height(0)`, flow reads 0): 6 tests, 2 failed, the new close case among them; green 6/6
    (`migrely-work/238/eng/2.2-red-source-ignored.log`, `2.2-green.log`). Found on the way: a row padding set in
    composition while the room reads the source in layout fails (1200 -> 948 px under Robolectric): the room grows a
    pass before the visible part, the flow counts it as page below and cuts it. The KDoc now requires layout-time
    values, and Delete all data (3.3) reads only insets and sizes.

## 3. Screens (`android-engineer`, `ui` modules)

- [x] 3.1 Edit medication: hoist the inline `rememberScrollState()`, put the room's modifier right after `verticalScroll`
  (never a child: `spacedBy` would add a gap). R1 to R14. Verify: a test in `EditMedicationKeyboardTest` that closes a 250 dp
  keyboard inset near the end and asserts the Usual dose field's bounds are unchanged; red with the modifier removed.
  - Record (2026-11-17, android-engineer): `EditMedicationContents` hoists its `ScrollState`, pads with the same
    `viewportInsets` it gives the room, and puts `keyboardRoom.modifier` right after `verticalScroll`.
    `EditMedicationKeyboardTest` "Closing the keyboard near the page's end leaves the dose where it is" (100% portrait,
    411x891 dp, dose focused, scrolled to Save, 250 dp keyboard closed): red without the room, the dose pulled from 483
    to 554 dp (5 tests, 1 failed); green with it. Logs `migrely-work/238/eng/3.1-3.2-red.log`, `3.1-3.2-green.log`.
- [x] 3.2 Log medication (first-use Name, also from attack mode): same as 3.1; the room is `AttackModeBackground` in the dark
  room. Verify: a test in the medication `ui` tests with the same assertion; red with the modifier removed.
  - Record (2026-11-17, android-engineer): same wiring in `LogMedicationContents`; the room draws nothing, so it shows
    `palette(darkRoom).background` (`AttackModeBackground` in the dark room) set on the screen. New
    `LogMedicationKeyboardTest`, first use, Name focused at 200%, scrolled to "Add and log", keyboard closed: red without
    the room, Name pulled 242 dp in both the light page and the dark room (2 of 2 failed); green with it. Medication `ui`
    debug unit tests after: 93, 0 failed.
- [x] 3.3 Delete all data: the room over the body above the Delete row, in both `rides` and behind-keyboard cases; the row
  moves as D180 (8) says; the divider stays hidden with the end in view. Verify: a test in `DeleteKeyboardTest` for both
  cases asserting the field's bounds; red with the room removed.
  - Record (2026-11-17, android-engineer): the room's source is the visible part's bottom padding, `row +
    rowInsets.getBottom + bodyInsets.getBottom`, read in layout. The row and the body pad with those same objects
    (`rowInsets`, and `bodyInsets` = `ime.exclude(bars).exclude(row)` while the row stays behind, replacing the
    composition-time `behindKeyboard` Dp by `padding(bodyInsets.asPaddingValues())`, which reads in layout and consumes
    nothing). The room goes after `verticalScroll` in `ConfirmationBody`. `DeleteKeyboardTest`, 4 new cases at 200%
    (riding row 411x891 dp with a 250 dp keyboard, row behind 800x360 dp with 200 dp; each closed at once and in 4
    animation steps, the body scrolled to its end): red with the room removed, the field pulled 188 dp riding and 128 dp
    behind (4 of 10 failed); green 10/10, the six D180 (8) row tests unchanged. Mutation without the body term: the two
    behind cases red (the inset alone is not the source). Mutation with the old composition-time body padding: survives
    (inset dispatch recomposes before layout here), so the layout-time padding follows the helper's contract and no test
    forces it. The divider follows `canScrollForward`, false with the end in view; checked on a device in 5.2, not
    here. Logs `migrely-work/238/eng/3.3-*.log`.
- [x] 3.4 Recheck Attack details (a1efa2bc) against R2 (room under the existing bottom padding, background only), R8 (not
  saved: `remember`, no band after rotation, recreate or re-entry) and R11 (no semantics node, not focusable, no click). Fix
  what fails. Verify: a test that the semantics tree has no node in the room's bounds, and a recreate test with no room after;
  each red against a deliberately broken version.
  - Record (2026-11-17, android-engineer): R2 holds by construction. The room is a layout modifier after
    `verticalScroll`, so it sits below the last item ("Delete migraine"; the column has no bottom padding) and draws
    nothing: the screen's `background(colorScheme.background)` shows, the dark room's in attack mode. R8: a1efa2bc kept
    the room in `remember(viewportInsets)`, so an unequal insets object on recomposition would have reset it; since
    2.2 it is `remember {}` with the source in `rememberUpdatedState`, and never `rememberSaveable`. R11: no child, no
    semantics, no focus, no click. New `AttackDetailsKeyboardRoomTest` (200%, 400x700 dp, note focused, 250 dp
    keyboard closed at the page's end), green 3/3: (a) the note keeps its top; (b) no node of the page (unmerged tree)
    overlaps the room, with a check that the page's nodes have bounds; (c) after `emulateSavedInstanceStateRestore`
    the page ends with no room. Broken versions: the room removed, 3 of 3 red (the note pulled 171 -> 421 dp); a
    described 200 dp child in the room, (b) and (c) red; the room's peak kept outside the composition, (c) red (250 dp
    after the restore). A clickable `Spacer` as the child made no semantics node under Robolectric, so that version
    could not fail (b); not used as the control. Logs `migrely-work/238/eng/3.4-*.log`.

## 4. Gate (`android-engineer`)

- [ ] 4.1 Full gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`
  exit 0, with the counts of tests run (never `testDebugUnitTest` alone).
- [ ] 4.2 No permission, string or manifest change. Verify: `./gradlew :app:checkReleasePermissions` exit 0 and
  `git diff origin/develop -- '*.xml' '*strings*' '*AndroidManifest*'` empty except tests.

## 5. Device walk (`android-engineer`, emulator-5570, headless first)

- [ ] 5.1 Each of the four screens, API 36, 100% portrait, near the end, TalkBack on, Back once: focus stays on the field
  (KR1 on the candidate only; QA owns the controls). Verify: the TalkBack log lines per screen in the packet.
- [ ] 5.2 Field bounds before and after the close on the four screens (KR4), and the room at 200% landscape 360 dp (KR11).
  Verify: dump coordinates in the packet.
- [ ] 5.3 Band screenshots with the page end and the band in view, for `ux-designer`'s review (ux §11.1): Attack details dark,
  Attack details light, Log medication from attack mode (dark room). Verify: three screenshots in `migrely-work/238/shots/`,
  and a one-line ux verdict per shot in this task.

## 6. Review (`code-reviewer`)

- [ ] 6.1 Review a1efa2bc and sections 2 to 3 against design D1 to D7: one helper, modifier after `verticalScroll`, no
  semantics, `remember` only, stale reads, no change to opening or message fit (D180 (2), (4)). Verify: a review packet with
  findings, each fixed or answered by the engineer before QA.

## 7. Acceptance (`qa-engineer`, this diff only)

- [ ] 7.1 KR1 to KR16 from design "Acceptance" on the PR head, with the controls: v1.3.0, and the mutation build with the room
  removed from the screen under test. TalkBack rows headless first, windowed recorded separately; a screen where v1.3.0 shows
  no jump says "no jump observed", not a fix proof. Verify: a QA record with each KR row, build SHA, result and control result.
- [ ] 7.2 Restore every device setting (font scale, rotation, animator scales, TalkBack, keyboard). Verify: the settings
  read back in the QA record.

## 8. Close (`product-manager`, then `android-engineer`)

- [ ] 8.1 Before archive: `fix-keyboard-follows-cursor` is archived first (its tasks 9.1 to 9.3). Then diff this change's
  MODIFIED "Closing the keyboard and other keyboards" against the live `openspec/specs/app-shell/spec.md`; sync it if that
  requirement changed since 2026-11-17. Verify: the diff shows only this change's four rewritten scenarios and the body.
- [ ] 8.2 BRD D189 updated with QA's results (what was proved, what stays open), then archive. Verify:
  `openspec archive keep-page-when-keyboard-closes --yes` and `openspec validate --all --strict` exit 0.
- [ ] 8.3 PR into `develop` with `Refs #238`, merge commit, worktree removed after merge, tracker T74 updated by `team-lead`.
  Verify: `gh pr view <n> --json state,mergeCommit` shows MERGED.
