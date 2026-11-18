# QA record: keep-page-when-keyboard-closes (#238, T74, BRD D189) on the merged head with T64 (D182)

## Verdict

**QA (qa-238), 2026-11-18: GO with recorded risks** for merging `bugfix/238-talkback-focus-on-keyboard-close` @
47f939918dc7fe6c97cd39396e4ad5ee2108aa95 (merge of 6825e1ff and `develop` bedd73b4) into `develop` as the 1.3.1
content.

- Design "Acceptance" KR1 to KR16, this diff only (owner rule: what the diff reaches, with negative controls). Every
  row that ran passes on the candidate, and every new check fails on at least one negative control: v1.3.0 code,
  M0 (the room removed) or M1 (a room that never shrinks). Rows that could not run, or whose control cannot fail,
  say so below and are not counted as passes.
- **The control reproduces #238, with data written.** On v1.3.0 and on M0, closing the keyboard on a migraine's note
  pulls the page 820 px with 3 `TYPE_VIEW_SCROLLED` events. TalkBack moves to a trigger chip ("Skipped meal") or
  the "Impact on your day" radio, and the next double tap writes it to the database: 6 of 6 control rows. On the
  candidate, the same set-up gives 0 px, 0 scroll events, TalkBack stays on the note, the double tap reopens the
  keyboard in the note, and the database is unchanged: 3 of 3 rows, plus 2 hide-key rows.
- **New finding for the control: v1.3.0 also jumps on Edit medication.** Closing from Usual dose moved TalkBack to the
  Name field (v1.3.0 2/2, M0 2/2), so the next double tap opened the keyboard in Name. The engineer saw no jump there.
  The candidate keeps Usual dose (3/3 Back, 1/1 hide key, 2/2 Done).
- T64 on the merged head: the message countdown pauses for keyboard focus and for TalkBack focus on Attack details, and
  for keyboard focus in Log medication's dark room, then resumes. The dark-room Dismiss is 64 dp. v1.3.0 fails each.
- One defect found, **pre-existing and outside this diff**: #245 (S3, accessibility). In a 360 dp window at 200%
  in landscape, the note's typed line is hidden while the keyboard is up, on v1.3.0 too. Not a blocker for 1.3.1.
- KR13 (hardware keyboard, floating keyboard, split screen) is **not reachable** on these AVDs. That is not a pass.
  Also not run: KR8's TalkBack navigation order (tooling), KR9 on Delete all data, KR15 at 360 dp, KR16 with
  TalkBack, any windowed TalkBack run. None of these blocks the merge: the room has no node to navigate to (KR6), and
  the geometry of KR15 and KR16 holds in the rows that ran.

## Builds and devices

| Build | Source | sha256 (APK) | Read back on device |
|---|---|---|---|
| candidate | `migraine-app-qa-238` at 47f93991, clean tree, `:app:assembleDebug` | `e1ea736709872f1639465a504319a822827c09ae0188438488bd492fc8bd2cc5` | `e1ea736709872f16` |
| control | v1.3.0 code: QA's 1.3.0 debug from 2f7745c3 (`v1.3.0` and 294c10c6 have equal trees) | `782cf3d3e80efc1a5b0cd86e34d65a5d99a3d9b65d48378c7b5c53f7a0bd0199` | `782cf3d3e80efc1a` |
| M0, no room | detached worktree at 47f93991, `.then(keyboardRoom.modifier)` removed on all four screens | `cb8ea5b10be96f3961c91a72d9e36a15ce10a965448c5d932a922586f84bb28e` | `cb8ea5b10be96f39` |
| M1, room never shrinks | same worktree, `KeyboardRoom.onChange`: `peak = maxOf(bottom, peak)` | `bd695590e51e1eddc3087c4d98cc34ac80e427f958b36034e747ec263a14fb01` | `bd695590e51e1edd` |

- The mutations are local and uncommitted, built in `migraine-app-qa-238m` and reverted after each build (`git status`
  clean); nothing was pushed. Diffs: `migrely-work/qa-238/logs/m0.diff`, `m1.diff`. They are in the dex
  (`logs/mutation-dex-check.txt`): `KeyboardRoom.getModifier` has 4 call sites in the candidate and M1 and 0 in M0;
  M1's `onChange` keeps only `max`.
- M0 removes the room on all four screens at once. Each screen's room is independent, so for the screen under test
  this is the per-screen mutation the design asks for, with one build instead of four.
- `emulator-5580` `Migrely_QA_API_36` (Android 16, Gboard, TalkBack 16 at VERBOSE for the run, headless
  `swiftshader_indirect`), then `emulator-5582` `Migrely_QA_API_26` (Android 8.0, AOSP keyboard, no TalkBack on the
  image), then 5580 again for KR8. One at a time. Every row: fresh install, APK hash read back, Maestro seed (a
  past migraine with details; a medication where needed), `am start` launches.
- Evidence: `migrely-work/qa-238/` (`walk/rows.jsonl` one JSON line per row with the device APK hash; `walk/raw/`
  TalkBack logs; `walk/shots/`, `shots/` screenshots; `raw/frames-*` screen recordings; `logs/`; scripts in `bin/`).

## How a TalkBack row is made (and two harness traps found on the way)

`bin/tbrow.py`. With TalkBack off: open the screen, focus the field, type, drag the page to its end. TalkBack on.
Then two-finger emulator drags bring the page back to its end (TalkBack passes two-finger drags to the page). The
speech since TalkBack came on must include "Editing", or the row is invalid. Screenshot A, close (Back key, or the
hide key or Done by TalkBack touch and double tap), 3 s of TalkBack's log (speech, `TYPE_VIEW_SCROLLED` from the
app), screenshot B, one TalkBack double tap anywhere, then whether the keyboard is up. Only after that: TalkBack
off, a uiautomator dump (which field has focus) and the database (attack impact, tags, medications, intakes) compared
with before. Page shift: the vertical offset that best maps A's page rows onto B (row-signature votes); 0 means still.

- **Trap 1, voided:** the first batch (engineer's `w.row` driver, rows `c36-*`, `v36-*`) dumped the screen between
  TalkBack on and the close. A dump turns TalkBack off and on; turning TalkBack on can make Gboard hide and show again,
  which on v1.3.0 is itself a close. The v1.3.0 set-ups then were no longer at the page's end (field 599 px off,
  "Delete migraine" not in view), and 2 of 3 candidate double taps found no keyboard. Those control rows are void.
  The 6 candidate rows there (details 3, Log 3) still showed 0 px and 0 scroll events, kept as support only.
- **Trap 2:** Delete all data at 200% with the page at its very end has the field out of view, so TalkBack never
  focuses it ("Editing" absent). Those rows are invalid on every build (the next double tap hit "Back"). The valid
  Delete rows keep the field in view, short of the end (`del2f`).

## Results

### KR1, KR3: TalkBack on the field, Back once (API 36, 100% portrait; Delete at 200% portrait, field in view)

| Screen | Candidate | v1.3.0 | M0 |
|---|---|---|---|
| Attack details, note | **3/3**: 0 px, 0 scroll events, speech only "keyboard hidden", double tap reopens the keyboard in the note, database unchanged | 3/3 fail: 820 px, 3 events, focus to "Not selected. Skipped meal" or "Radio button. 4 of 4", the double tap writes the trigger or impact (database changed 3/3) | 3/3 fail: the same, impact written 3/3 |
| Log medication, first use, Name | **3/3** as above | 2/2: 373 px, 2 events, focus kept: **no jump observed** | 2/2: 373 px, 2 events, no jump observed |
| Edit medication, Usual dose | **3/3** as above, the reopened keyboard is in Usual dose | 2/2 fail: 502 px, 2 events, focus to "Ibuprofen. Edit box. Name"; the double tap opens the keyboard in **Name** | 2/2 fail: the same |
| Delete all data, the word, field in view | **3/3**: 0 px, 0 events (page rows 356–358) | 2/2: 0 px, 0 events in a different resting position (page rows 368): this control cannot fail | 1 of 2 at the candidate's position: **265 px, 2 events**; the other at the 368 position: 0 |

- Scroll events (KR3) come from TalkBack's own log (`onScrollEvent … TYPE_VIEW_SCROLLED … package="com.migrely.app"`).
  Candidate total: 0 in 12 valid Back rows; v1.3.0 and M0: 2 or 3 in every row that moved.
- Landscape with TalkBack (details, 200%, 411 dp, Back key) is under KR8: candidate 2/2 clean, v1.3.0 51 px and 1
  scroll event with focus kept.

### KR2: closing with the hide key, then Done (API 36, TalkBack)

| Row | Candidate | v1.3.0 |
|---|---|---|
| Details, hide key | **2/2**: 0 px, 0 events; TalkBack speaks "Back. Button. Window English (US)" for the key, then "Editing. qa…" for the note; the double tap reopens the keyboard in the note | 2/2: 820 px, 3 events, focus returns to the note: no jump on this path, the control fails on the move only |
| Edit medication, hide key | **1/1** as above, back in Usual dose | not run |
| Edit medication, Done | **2/2**: 0 px, 0 events, "Done" then "Editing. 400. Edit box. Usual dose", keyboard reopens in Usual dose | 1/1: 502 px, 2 events, focus back on Usual dose |

### KR4: field and page end before and after the close (dump bounds, no TalkBack)

API 36, 411 dp: candidate 0 px in every cell: details 100% and 200% portrait, details 200% landscape, Log 100% and
200% (portrait and landscape), Edit 100% and 200% (portrait and landscape), Delete 200% short of the end, field in
view, landscape row behind. v1.3.0, the same set-ups: details 820, details landscape 51, Log 373, Edit 502, Delete
short of the end 421, Delete field in view 264; Delete landscape, row behind: 0 (cannot fail on this set-up).

API 26 (AOSP keyboard): candidate 0 px in all 10 rows (details 2, Log 2, details 200% landscape, Edit 2, Delete
short of the end, field in view, landscape). v1.3.0: details 775, Log 268, details landscape 156, Edit 455, Delete
short 421, Delete field in view 318, Delete landscape row behind 0. M0: details 775, Log 268, landscape 156, Edit 455,
Delete short 421. These equal the engineer's numbers.

### KR5: the band is the page's background (API 36, screenshots after the close)

Every 4th pixel of every 2nd row from below the end control's box to the visible part's end, against the page
background sampled at the right edge:

| Shot | Band pixels differing | Control: keyboard area of the keyboard-open shot |
|---|---|---|
| Attack details, app theme Dark | 0 of 110,700 (15,17,21) | 70,740 of 70,740 differ |
| Attack details, app theme Light | 0 of 110,700 (245,243,238) | 70,740 of 70,740 differ |
| Log medication from attack mode (dark room) | 0 of 113,400 (8,9,12) | 70,740 of 70,740 differ |

The app keeps its own theme setting (default Dark), so the system night mode did not change the app. The Light row
sets the app's Theme to Light and back. A first pass started the band 30 px below the end label, inside the "Add and
log" button, and counted the button's own surface (rows 1471 to 1495). Corrected and rechecked:
`logs/kr5-band-recheck.txt`.

### KR6: the band is inert (details)

Candidate: after the close, the only app node reaching below the end label is the "Delete migraine" button's own box
(ends at y 1517, where the band starts). No node at the hide key's former place (164, 2336). A tap in the band
(540, 1617) changed nothing: focus stays in the note, no keyboard, no state change. v1.3.0: the "Delete migraine"
button sits at the hide key's former place after the pull. My driver also tapped it there, which opened the delete
dialog (not confirmed; the app was force-stopped). The design asked for a dump only on the control: a harness
deviation, no data touched.

### KR7: the band shrinks with the person's scroll, and only with it

Drag up a third of the band, back to the end, then more than the band, back to the end. Room = gap below the end node
minus the gap on a fresh visit.

| Screen | Candidate | M1 (never shrinks) |
|---|---|---|
| Attack details | 820 → 565 after a 256 px drag → **0** at the end | 820 → 820 → 820 |
| Log medication | 373 → 269 after 104 px → **0** | 373 → 373 → 373 |
| Edit medication | 502 → 353 after 149 px → **0** | 502 → 502 → 502 |
| Delete all data, 200% | 577 → 404 after the drag → **−6** (no band; 6 px under the fresh-visit gap) | 577 → 577 → 577 |

The first candidate run of Log, Edit and Delete hit a driver error (a page that fits has no scroll container in the
dump). They were rerun after the fix and are the rows above.

### KR8: TalkBack navigation after the close (details, 200% landscape, 411 dp)

**The navigation part (forward to "Delete migraine", the page's end, then back in D178 order) was not run.** TalkBack's
keyboard shortcuts (Alt+Right, Alt+Left through `input keycombination`) produced no speech in 4 tries, and touch
swipes in landscape need rotated emulator coordinates, which I did not build. What stands instead: the room adds no
node (KR6: nothing in the band in the dump; the code adds a layout modifier with no semantics), so TalkBack has nothing
to stop on there.

What did run in this set-up (TalkBack on the note, Back once), as a landscape KR1:

| Build | Result |
|---|---|
| candidate | **2/2**: speech after Back only the usage hint and "keyboard hidden", 0 scroll events, the double tap reopens the keyboard in the note, database unchanged |
| v1.3.0 | 2/2: 51 px, 1 scroll event, focus kept on the note: **no jump observed** in landscape |

A first candidate row here had TalkBack speak "Wednesday, November 18" after Back, but its speech before Back never
said "Editing" (TalkBack was not on the field), so by the design's rule it is invalid. The two valid reruns are above.

### KR9: reopen the keyboard after the close (API 36, 100%)

Tap the field after the close: the keyboard opens and the field does not move (0 px on close, 0 px on reopen) on
details, Log medication and Edit medication. The whole field is above the keyboard (221, 909 and 200 px clear), so the
cursor line is at least 8 dp clear (EC-UX-39). Delete all data: not run as a KR9 row.

### KR10: the room is not saved (details and Edit medication, band visible)

| Step | Attack details | Edit medication |
|---|---|---|
| rotate and back | no band at the end (gap = fresh visit), note kept | no band, "400" kept |
| HOME and back, no recreation | the note keeps focus and the keyboard reopens; the field does not move (R9 then R5) | the same |
| process death (HOME, `run-as kill -9`, launcher) | no band, note kept | no band, "400" kept |
| leave and reopen | no band | no band; the unsaved dose is gone, which is the screen's existing discard on leave, not this change |

Details was run twice (once at load 24.9 after a host sleep, again at load 6). Both runs agree.

### KR11: sizes (200%; 411 and 360 dp; portrait and landscape)

- 411 dp (API 36): 0 px in every cell listed under KR4. In landscape at 200% the visible part above the keyboard is
  137 px; the field's top holds still and its clipped bottom comes into view on close.
- 360 dp (API 36, `wm size 945x2100`, which the engineer's driver could not reach): details 200% portrait 0 px (2 runs),
  Log 200% landscape 0, Edit 200% landscape 0, Edit 200% portrait and Log 200% portrait 0 for the end node (the field
  is out of view at the end with the keyboard up), Delete 200% short of the end 0. Details 200% landscape: 0 px on the
  candidate **and** on v1.3.0: the visible part with the keyboard up is 61 px, the note is at its top and plenty of the
  page lies below, so no room is needed and this cell cannot fail. With the keyboard up there, the typed line is not
  visible at all on either build: #245, pre-existing.
- API 26: details 200% landscape 0 px (v1.3.0 156, M0 156).
- The room is never taller than the keyboard: by construction, and measured as the room in KR7 (≤ 820 px against a
  keyboard top at 1517 of 2337).

### KR12: no frame shows the page lower (screen recordings, animator scales 0)

`bin/frames.py`: `screenrecord` across Back; every frame's page area above the keyboard compared with the first
frame, row by row (the field's own rows left out, where the cursor blinks).

| Recording | Frames | Lowest share of page rows unchanged |
|---|---|---|
| API 36 details, animations 0 | 20 | 0.993 |
| API 36 details, animations on | 21 | 0.987 |
| API 36 Delete, animations 0 | 21 | 0.906: the changed rows are the Delete row moving down with the keyboard (D7) and a few edge rows of the clipped top line; the body text is identical (`logs/kr12-frames-recheck.txt`) |
| API 36 v1.3.0 details, animations 0 (control) | 21 | **0.201** from the close on |
| API 26 details, animations 0 | 20 | 0.943 (field rows included) |
| API 26 details, animations on | 24 | 0.985 |
| API 26 Delete, animations 0 | 17 | 0.975 |
| API 26 v1.3.0 details, animations 0 (control) | 18 | **0.116** from the close on |

My first threshold, set before the run, was 0.90 with the field rows included. One API 36 recording reached 0.897;
the same dips (0.902–0.904) appear in frames before Back on both builds, so I left out the field's rows instead of
moving the threshold. A recording holds the frames the compositor produced (about 20 here), not every 16 ms frame.

### KR13: no bottom keyboard inset (hardware keyboard, floating keyboard, split screen): **not reachable**

The API 36 AVD has a hardware keyboard (`hw.keyboard=yes`; the configuration reads `keysexposed-qwerty`). With
`show_ime_with_hard_keyboard 0`, Gboard restarted, and `stylus_handwriting_enabled` at 0 and at 1, Gboard still raised
its full window (top at y 1517) when the note took focus, so no state without a keyboard inset could be made
(`logs/kr13-config.txt`, rows `pk2-kr13-details`, `pk4-kr13-details`). In those rows nothing moved and Escape did
not close the keyboard. Floating keyboard and split screen were not attempted. R10 stays unproven on a device.

### KR14: a message while the band shows (details, "Time to take action" 120 s)

"Medication removed" with Undo, then the note focused, the page at its end, Back. The message text keeps its box
(169,341 to 536,388) with no band, with the keyboard up and with the band; Undo at 875,301 to 1027,427; Back at
43,181 to 106,244 is not covered. The note does not move (0 px).

### KR15: Delete all data, the word typed, Done

| Case | Candidate | v1.3.0 | M0 |
|---|---|---|---|
| 100% portrait, row rides | field still (0 px); the row moves to the bottom (y 1402 → 2222), field and row together | not run | not run |
| 200% portrait, row rides | field still; row 1370 → 2190; together | the same: field still | the same: field still |
| 200% landscape (411 dp), row behind | field still; row stays at 870; together | the same | the same |

The divider: in the keyboard-open shot a 3 px line at y 1325–1327 above the row; after Done at 200% portrait, with
the page's end in view, no line (`logs/kr15-divider-check.txt`). Done was sent as the Enter key, which closed the
keyboard on the single-line field. **This row's controls cannot fail on these set-ups:** the word field sits near the
page's top, so nothing lies near the end. The Delete room itself is proven by KR1 (M0 265 px), KR4 and KR7. KR15 at
360 dp was not run.

### KR16: far from the end, nothing moves

Covered without TalkBack by the 360 dp landscape cell (KR11): more than a keyboard's height of page below the visible
part, no band after the close (the page continues past the visible part's end), 0 px. A TalkBack run of KR16 was not
done.

### T64 on the merged head (only where the merge reaches)

The merge changed `AttackDetailsScreen` in both branches (T64: the message clock). `LogMedicationScreen` changed only
in #238 but shows its messages through T64's host. "Time to take action" 20 s; presence read from frames (the
control's label colour inside its box), never from dumps while timing.

| Row | Candidate | v1.3.0 |
|---|---|---|
| Details, "Medication removed", hardware Tab to Undo (2 Tabs) | held: still shown at 30.5 s; Tab away at 30.5 s, gone 12.2 s later (the time left when focus came, plus about 1 s of capture lag) — **1/1** | gone by 30.6 s |
| Details, TalkBack focus on the message text at 2.7 s | held to 30.5 s; focus moved at 30.6 s, gone 18.6 s later — **1/1** | gone by 30.7 s |
| Log medication from attack mode (dark room), "Removed. Nothing was logged.", Tab to Dismiss | Dismiss box **168 × 168 px (64 dp)**; held to 30.7 s; gone 15.3 s after Tab away — **1/1** | Dismiss 126 × 126 px (48 dp); gone by 30.5 s |

The first two details rows failed in the harness: the control's label is "Undo removing the medication", and the
driver matched only "Undo". After the fix they ran as above.

## Screenshots for ux-designer (5.3 add-on)

- 200% landscape, closed: `migrely-work/qa-238/shots/ux-kr11-api36-details-200-landscape-411dp-closed.png`,
  `ux-kr11-api36-edit-200-landscape-360dp-closed.png`, `ux-kr11-api26-details-200-landscape-closed.png`
- API 26, animations 0, closed (last frame of the recording): `shots/ux-kr12-api26-details-anim0-closed.png`
- KR5 pairs: `shots/pk-kr5-details-dark-*`, `shots/pk3-kr5-details-applight-*`, `shots/pk2-kr5-logdark-*`

## 7.2 Device settings, read back

Snapshots `migrely-work/qa-238/raw/settings-<serial>-{boot,end}.json` (font, rotation, animation scales, keyboard
settings, accessibility, TalkBack, night mode, zone, size, density, zen, airplane). Changed for the run and restored:
`show_ime_with_hard_keyboard` (5580: 0 → 1 → 0), `stylus_handwriting_enabled` (5580: 1 → 0 → 1), TalkBack on and off,
TalkBack log level (5580: ERROR → VERBOSE → ERROR, read back in TalkBack's settings), "Time to take action" (unset →
20 s / 120 s → unset), font scale, rotation, `wm size`, animator scales, night mode, the app's own theme (Light → Dark).

- emulator-5580: end equals boot on every key; only uptime differs. The same after the second session for KR8
  (cold boot, the same settings changed and restored, read back again).
- emulator-5582: end equals boot on every key; only uptime differs. Its baseline was `font_scale 2.0` and `zen_mode 3`
  from an earlier session: restored to 2.0, not to 1.0. `touch_exploration_enabled` had become 0 (the driver's
  TalkBack-off step) and was deleted back to unset.
- Both emulators are off.

## What these checks do not prove

- **Other TalkBack builds and real phones.** TalkBack 16 on the emulator, Gboard on API 36, the AOSP keyboard on API 26.
  API 27 to 35, Samsung's TalkBack, other keyboards, foldables and tablets are not run. If a screen reader moves focus
  on the keyboard close itself rather than on a scroll, the room does not help (design, "will not prove").
- **Headless only.** Every TalkBack row ran on a headless emulator; no windowed run was recorded. The design notes the
  control reproduced headless only earlier; a windowed pass would prove little on its own.
- **Every frame.** KR12 compares the frames the compositor produced (17 to 24 per recording), not a 60 fps capture.
- **KR13**, the hardware and floating keyboard and split screen: not reachable here.
- **Switch Access, Voice Access, D-pad, a mouse wheel, Page Up**: not run. KR7 covers a finger drag only.
- **KR8's navigation order**, **KR9 on Delete all data**, **KR15 at 360 dp**, **KR16 with TalkBack**: not run.
- **Log medication's jump**: v1.3.0 moved the page there but TalkBack kept its focus, so that screen's TalkBack row
  proves "no jump observed", not a fix. The same holds for Delete all data (no v1.3.0 jump seen) and for the
  hide-key path on details.
- **How people read the band**: ux judges the screenshots; no person used it.
- **T64 beyond the merge**: only the three rows above; the rest of T64 stands on its own record
  (`docs/qa/fix-message-timing-and-size.md`).

## Residual risks

- The room depends on reading the inset before layout in a snapshot flow (design "Unordered reads"). 0 scroll events in
  every candidate row on both API levels, but a slower device could order them differently.
- Delete all data's room is checked at 200% (it has no scroll at 100%). Its KR15 controls cannot fail on the set-ups
  run; its proof rests on KR1 (M0 265 px), KR4 (v1.3.0 421 / 264 px) and KR7.
- #245 (pre-existing): at 360 dp, 200%, landscape, the typed line is hidden while the keyboard is up.

## Harness notes for the next run

- A uiautomator dump turns TalkBack off and on, and that can make Gboard hide and show again, which is itself a
  keyboard close. Never dump between TalkBack on and the measured close. Reach the page end with TalkBack on through
  two-finger emulator drags.
- Read data changes from the database, not from dumps: radio nodes have no labels in uiautomator here.
- Delete all data at 200% at the very end has its field out of view: TalkBack never focuses it, and the next double
  tap lands on Back.
- The app has its own theme setting (default Dark); system night mode does not change it.
- Messages' Undo label is "Undo removing the medication", not "Undo".
- The host slept for about 4.5 h mid-chain (perl alarms do not count sleep). Rows from that window were rerun.
