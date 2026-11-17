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
    forces it. Correction (6.1, cr-238): the divider was not checked, on a device or here, and these cases all start
    exactly at the end (b = 0), which hid review finding 1. Logs `migrely-work/238/eng/3.3-*.log`.
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

- [x] 4.1 Full gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`
  exit 0, with the counts of tests run (never `testDebugUnitTest` alone).
  - Record (2026-11-17 10:41, android-engineer, load 5.4): `set -o pipefail; nice -n 19 ./gradlew --max-workers=2
    assembleDebug test ktlintCheck detekt lint` -> **BUILD SUCCESSFUL in 50s, exit 0** on the tree committed with
    this line. Unit tests (result files of every module): JVM `test` 1370, `testDebugUnitTest` 1231,
    `testReleaseUnitTest` 702; 0 failures, 0 errors, 0 skipped. Two earlier runs failed in ktlint only (a signature and
    four test lines over 100 columns, one body expression), fixed in this commit. Log `migrely-work/238/eng/gate.log`.
- [x] 4.2 No permission, string or manifest change. Verify: `./gradlew :app:checkReleasePermissions` exit 0 and
  `git diff origin/develop -- '*.xml' '*strings*' '*AndroidManifest*'` empty except tests.
  - Record (2026-11-17): `:app:checkReleasePermissions :app:checkReleaseDebugComponents` exit 0. Merged release
    manifest uses-permission: ACCESS_NETWORK_STATE, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, WAKE_LOCK, BILLING,
    DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION; INTERNET appears only in two comments. `git diff --stat origin/develop --
    '*.xml' '*strings*' '*AndroidManifest*'` prints nothing. `openspec validate --all --strict`: 31 passed, 0 failed.

## 5. Device walk (`android-engineer`, emulator-5570, headless first)

- [x] 5.1 Each of the four screens, API 36, 100% portrait, near the end, TalkBack on, Back once: focus stays on the field
  (KR1 on the candidate only; QA owns the controls). Verify: the TalkBack log lines per screen in the packet.
  - Record (2026-11-17, android-engineer, emulator-5570 headless `-gpu swiftshader_indirect -no-snapshot -no-window
    -memory 2048`, API 36, Gboard, TalkBack 16 at VERBOSE; candidate = debug of 34036ae1, sha256 5d2ee9bd…, read back from
    the device before each row; control = v1.3.0 code, QA's debug-2f7745c3 782cf3d3…; `v1.3.0` and 294c10c6 have equal
    trees). Each row: field focused, keyboard up, page at its end (or short of it), TalkBack on, the speech before Back
    must include "Editing." (else invalid), Back once, then 3 s of TalkBack's log and one TalkBack double tap.
    Candidate, valid rows: Attack details 3/3, Log medication first use 3/3, Edit medication (dose) 3/3, Delete all
    data at 200% (at 100% its body fits above the keyboard, no scroll container: nothing can move) short of the end
    3/3 and with the field in view 2/2 (row riding), landscape with the field in view 2/2 (row behind), details at
    200% landscape 2/2. In every one: speech after Back only "keyboard hidden" (plus the window title "Migrely" and
    the field itself in some), 0 `TYPE_VIEW_SCROLLED` from the app, and the double tap reopens the keyboard in the same
    field with no chip, radio or kind changed. Control on the same set-ups: details pulled 820 and 856 px, 1 and 4
    scroll events, "Not selected. Stress" spoken in 1 of 2 (the #238 jump); Log medication 373 px, Edit medication 502
    px, Delete short of the end 421 px and field in view 262–264 px, details landscape 51 px, each with TalkBack's focus
    kept: "no jump observed" there (Log and Edit sent no scroll event at all, so TalkBack had nothing to follow). Delete
    landscape, field in view: no move on v1.3.0 either, so that row cannot fail. Invalid, not counted: Delete at the
    very end (200% portrait 3, landscape 2): the field is out of view there, TalkBack starts on Back; the page is
    still all the same (last text's top unchanged, 0 scroll events). Rows `migrely-work/238/walk/rows.jsonl`, raw
    TalkBack logs `walk/raw/<row>.log`, drivers `bin/w.py`, `bin/batch36.py`.
- [x] 5.2 Field bounds before and after the close on the four screens (KR4), and the room at 200% landscape 360 dp (KR11).
  Verify: dump coordinates in the packet.
  - Record (2026-11-17): KR4, field and page-end bounds (px) before Back and after, candidate | v1.3.0. API 36: details
    note top 962 | moved 820/856; Log medication Name 372 and "Add and log" 1394 | moved 373; Edit medication dose 1081
    and Save 1394 | moved 502; Delete 200% short of the end field 233, last text top 1028 | moved 421; Delete 200%
    field in view 386–388 | moved 262–264. API 26 (AOSP keyboard, no TalkBack on that image, KR4 only, 2 rows each):
    details note 944 | 775; Log Name 354 | 268; Edit dose 1063 | 455; Delete short of the end 168 | 421; Delete field
    in view 268–270 | 318–319; details 200% landscape 194 | 156; Delete landscape row behind 267–272 | 0 (no move on
    1.3.0 either). Candidate: 0 px in every row. KR11 at 200% landscape in the device's 411 dp window: details 2/2 and
    Delete (row behind) 2/2 still, the room filling the keyboard's place ("Delete migraine" came into view below the
    unmoved note). **Not run: the 360 dp window** (`wm size 945x2100`): the driver did not reach a focused field with
    the keyboard up there (2 rows per build and one debug run: the keyboard never showed, then a tap on Home's migraine
    row opened Log medication); left to QA 7.1. Divider (cr-238 finding 1): short of the end a 1 dp line at y 1325–1327
    above the row with the keyboard up; after the close, end in view, no line (`shots/delete-short-*.png`).
  - QA (2026-11-18, qa-238, candidate 47f93991, `docs/qa/keep-page-when-keyboard-closes.md` KR4, KR11, commit 5f873c7c):
    KR4 equals the engineer's numbers on API 36 and API 26 (candidate 0 px in every cell; M0, where run, moves as v1.3.0). KR11 at
    411 dp: 0 px in every KR4 cell; at 200% landscape the visible part above the keyboard is 137 px and the field's top
    holds still. **360 dp** (`wm size 945x2100`, reached by QA): details 200% portrait 0 px (2 runs), Log 200% landscape 0,
    Edit 200% landscape 0, Edit and Log 200% portrait 0 for the end node (field out of view at the end with the keyboard
    up), Delete 200% short of the end 0. Details 200% landscape at 360 dp: 0 px on the candidate and on v1.3.0 (visible
    part 61 px, plenty of page below, no room needed): that cell cannot fail. The typed line is hidden there with the
    keyboard up on both builds: #245, pre-existing. API 26 details 200% landscape 0 px (v1.3.0 and M0 156). Room never
    taller than the keyboard (KR7 max 820 px against a keyboard top at 1517 of 2337).
- [x] 5.3 Band screenshots with the page end and the band in view, for `ux-designer`'s review (ux §11.1): Attack details dark,
  Attack details light, Log medication from attack mode (dark room). Verify: three screenshots in `migrely-work/238/shots/`,
  and a one-line ux verdict per shot in this task.
  - Shots (2026-11-17, candidate 5d2ee9bd…, API 36, 100% portrait), each a keyboard-open and a closed pair in
    `migrely-work/238/shots/`: `details-dark-*`, `details-light-*`, `logattack-darkroom-*` (Log medication from the
    attack card, first use), plus `delete-short-*` for the divider. KR5, every 4th pixel of the band (y 1510–2300)
    against the page background at (1060, 1100): dark 0 of 213,300 differ (15,17,21), light 0 differ (245,243,238),
    dark room 0 differ (8,9,12, the attack-mode background); the same check on each keyboard-open shot's keyboard area
    fails on every pixel. The end and the field keep their bounds across the close in all three.
  - ux-designer verdicts (2026-11-18, relayed by team-lead), engineer's shots (API 36, 100% portrait):
    - `details-dark`: OK. The page does not move; the band is plain page background, no line, edge or tint.
    - `details-light`: OK. The band is the warm light page background.
    - `logattack-darkroom`: OK, milder than the accepted cost: only the Name label and the rows above it leave view; the
      input, its counter and "Add and log" stay put.
    - `delete-short`: OK. The field stays; the Delete row returns to the bottom; the divider shows with the keyboard up and
      is gone with the end in view.
  - ux-designer add-on on QA's shots (`migrely-work/qa-238/shots/`):
    - `ux-kr11-api36-details-200-landscape-411dp`: OK. Android's text toolbar from the harness tap shows on v1.3.0 too.
    - `ux-kr11-api36-edit-200-landscape-360dp`: OK. No band, as R1 says.
    - `ux-kr11-api26-details-200-landscape`: **NOT OK.** The room works as specified (field 194–275 px and "Delete
      migraine" 375–470 px identical before and after Back, row a26c-detailsL-0), but with the keyboard up the note was
      already hidden under the top bar (visible part from 257 px). After the close the person sees only "Delete migraine"
      above a band of about 60% of the window. v1.3.0 (a26v) and M0 (a26m) pulled the note back to 350–859 px on close.
    - `ux-kr12-api26-details-anim0`: OK.
  - team-lead decision on the NOT OK cell (2026-11-18): **not a blocker for 1.3.1**, recorded in D189 as a known limit.
    Risk order puts data integrity and accessibility before UX: #238 stops TalkBack's focus jump, which on v1.3.0 wrote a
    trigger chip or the impact radio into the record in 6 of 6 control rows. The cell is narrow (API 26, 200%, landscape,
    details); the bad position starts before the close (the note was already hidden while typing, pre-existing); one
    upward scroll brings the note back and removes the room; nothing is lost. Fix in 1.3.2 with #245: ux-designer designs
    it (in a window shorter than the cursor line plus its margins D180's rules conflict); qa-engineer is classifying
    whether the API 26 cell has #245's cause or a separate one.

## 6. Review (`code-reviewer`)

- [x] 6.1 Review a1efa2bc and sections 2 to 3 against design D1 to D7: one helper, modifier after `verticalScroll`, no
  semantics, `remember` only, stale reads, no change to opening or message fit (D180 (2), (4)). Verify: a review packet with
  findings, each fixed or answered by the engineer before QA.
  - Fixes (2026-11-17, android-engineer, on cr-238's review of f5f8524f; 6.1 stays the reviewer's to close):
    - Finding 1 (S3): Delete all data's "more to read" divider came and went with `canScrollForward`, so its 1 dp
      resized the body's visible part outside the room's source. Closing short of the end (0 < b < K) brought the
      body to its end, the divider left and the scroll clamped by 1 dp, a real scroll event; each scroll back through
      the room ended with an extra 1 dp. Now the divider is always laid out and only its colour changes (transparent
      with the end in view). `DeleteKeyboardTest`, 3 new cases at 200% (the riding row and the row behind, each
      closed 30 dp short of the end; a 30 px scroll back through the room): red on f5f8524f, 3 of 13 (field 43 -> 44
      and -246 -> -245 dp, scroll 158 -> 157 px against 158); green 13/13 after.
    - Finding 2 (S4): `KeyboardRoomTest` closed only at the end. New cases: 70 px short of the end in one step and
      in 50 dp steps (offset kept, and the room ends exactly at the page's end), and a keyboard's place plus a bar
      short (no room). Mutation cutting the whole room once anything lies below: 2 of 9 red (both short cases;
      the old six pass under it, as the reviewer found); green 9/9. With 100 px short, the stepped case did not
      fail under the mutation (every step's growth fitted what lay below), hence 70.
    - Finding 3 (S4): the Delete comment said every value is read in layout; it now names the insets as read in
      layout and says why `row` and `rides`, set in composition, are safe. The helper's KDoc now asks only values that
      move with the keyboard to be read in layout.
    - Logs `migrely-work/238/eng/r1-red-f5f8524f.log`, `r-green.log`, `r2-green.log`, `r2-mutation-below-ignored.log`.
  - cr-238 approved 34036ae1 with no new findings (2026-11-17, relayed by team-lead).

## 7. Acceptance (`qa-engineer`, this diff only)

- [x] 7.1 KR1 to KR16 from design "Acceptance" on the PR head, with the controls: v1.3.0, and the mutation build with the room
  removed from the screen under test. TalkBack rows headless first, windowed recorded separately; a screen where v1.3.0 shows
  no jump says "no jump observed", not a fix proof. Verify: a QA record with each KR row, build SHA, result and control result.
  - Record: `docs/qa/keep-page-when-keyboard-closes.md` (commit 5f873c7c), qa-238, 2026-11-18: **GO with recorded risks**
    on 47f93991 (6825e1ff merged with `develop` bedd73b4, T64). Controls v1.3.0, M0 (room removed) and M1 (room never
    shrinks), APK hashes read back on device. Details: v1.3.0 and M0 wrote a trigger or the impact into the record in 6 of 6
    rows; candidate 3/3 clean plus 2 hide-key rows. New: v1.3.0 also jumps on Edit medication (dose to Name). Not reachable:
    KR13. Not run: KR8's TalkBack navigation order, KR9 on Delete all data, KR15 at 360 dp, KR16 with TalkBack, windowed
    TalkBack. One pre-existing defect filed: #245.
- [x] 7.2 Restore every device setting (font scale, rotation, animator scales, TalkBack, keyboard). Verify: the settings
  read back in the QA record.
  - Record: same file, section "7.2 Device settings, read back" (5f873c7c): emulator-5580 and emulator-5582 end equals
    boot on every key except uptime; 5582's baseline font scale 2.0 restored to 2.0; both emulators off.

## 8. Close (`product-manager`, then `android-engineer`)

- [x] 8.1 Before archive: ~~`fix-keyboard-follows-cursor` is archived first (its tasks 9.1 to 9.3).~~ Met: archived as
  `openspec/changes/archive/2026-11-17-fix-keyboard-follows-cursor` (d4a5c539). Then diff this change's
  MODIFIED "Closing the keyboard and other keyboards" against the live `openspec/specs/app-shell/spec.md`; sync it if that
  requirement changed since 2026-11-17. Verify: the diff shows only this change's four rewritten scenarios and the body.
  - Record (2026-11-18, product-manager, on 585b75da): the requirement extracted from the live spec and from this delta,
    `diff`: only the body and four scenarios differ ("The keyboard closes", "The strip the keyboard uncovers", "Background
    and process death", "A floating keyboard or split screen"); the other eight match line for line. The live spec's last
    change is d4a5c539 (2026-11-17, the 1.3.0 archive that synced `fix-keyboard-follows-cursor`); `git diff 8675d05d
    bedd73b4 -- openspec/specs/app-shell/spec.md` is empty (T64 did not touch it), and `openspec/specs/` equals
    `origin/develop`. No sync needed.
- [x] 8.2 BRD D189 updated with QA's results (what was proved, what stays open), then archive. Verify:
  `openspec archive keep-page-when-keyboard-closes --yes` and `openspec validate --all --strict` exit 0.
  - Record (2026-11-18, product-manager): D189 gets "QA natijasi" (what was proved, what stays open) and known limit (e),
    the API 26 200% landscape cell with team-lead's reason; D182's release corrected to 1.3.1. D180 (5) and EC-UX-43
    already point to D189 since 0.1 (no rewrite). Design D5 and K6 stay in the archived change. Archive results are in
    the commit that moves this file.
- [ ] 8.3 PR into `develop` with `Refs #238`, merge commit, worktree removed after merge, tracker T74 updated by `team-lead`.
  Verify: `gh pr view <n> --json state,mergeCommit` shows MERGED.
