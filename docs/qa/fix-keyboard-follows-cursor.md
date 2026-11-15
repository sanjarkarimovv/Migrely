# QA record: `fix-keyboard-follows-cursor` (T63, BRD D180), task 8.1

## Decision

**QA's verdict, 2026-11-15: NO-GO for the merge of `bugfix/141-keyboard-follows-cursor` at 76e5afcd (code f05814f2) into
`develop`.** This is a change verdict, not a release verdict.

Two reasons, either one is enough:

1. **Row F1 fails on a device (the change's own scenario "Closing after scrolling away", review 7.1 F1).** On the note, when
   the last word typed is still being composed by the keyboard (underlined, the normal state while typing with
   suggestions), closing the keyboard pulls the page back to the note after the person scrolled away. Head: 5 of 5 runs
   (100% portrait 4/4, one of them with "Remove animations"; 200% landscape 1/1). With the same text committed (a trailing
   space) the page stays: 2 of 2. Severity S3 (the page jumps and the person loses their place; nothing is lost), but it is
   the review blocker that f05814f2 was meant to close, and the row's pass rule fails. Steps, evidence and the likely cause
   are under "Finding 1".
2. **The API 36 half of 8.1 did not run.** emulator-5580 died at boot twice (13:05 and 13:06), each time with qemu's hang
   detector killing a hung vCPU thread ("detected a hanging thread 'QEMU2 CPU0 thread'", crash reports
   `qemu-system-aarch64-headless-2026-11-15-130507.ips` and `-130609.ips`). The brief's rule (two failed boots: stop) was
   followed. emulator-5582 (API 26) booted 3 of 3 times in the same hour. So K2 with TalkBack, K4 (messages), the 360 dp
   window, Gboard, the U9 API 36 matrix, and 8.3 have no QA device evidence on the final code.

What passed is below. No S1 or S2 was found. No issue was filed: finding 1 belongs to this change, and the two smaller
observations are listed for the engineer.

## Candidate, builds and device

| APK | Source | sha256 (first 16) | Use |
|---|---|---|---|
| `app-debug-76e5afcd` | head, built by QA in the detached worktree `migraine-app-qa-kbd8` | 78cb48c0bfe3487c | all head rows, read back from the device with `sha256sum` of `pm path`: equal |
| `app-release-76e5afcd` | head, `assembleRelease` (R8, signed with the debug key: no `keystore.properties` in a QA worktree, D102) | 86e164e73143665 | K8 only (not installed) |
| `app-debug-24109541` | the engineer's build of 24109541; 51e22355 changes only `tasks.md` against it, so its code is 51e22355's | 76a74939db63ef18 | negative control for F1 and F2 |
| `app-debug-ea41869a` | `develop`, QA's build from task 1.3 | aff4e8b8485fa39f | negative control for K2, K3 and K5; it was on the device before and after |

- **Build:** `perl -e 'alarm 1500; exec @ARGV' nice -n 19 ./gradlew --max-workers=2 :app:assembleDebug :app:assembleRelease
  :app:checkReleasePermissions` -> BUILD SUCCESSFUL in 2m 10s, exit 0 (`migrely-work/qa-kbd8/build-76e5afcd.log`). The gate
  was run by `android-engineer` on f05814f2 (exit 0); QA did not rerun it.
- **Device:** `Migrely_QA_API_26` (emulator-5582, Android 8.0, AOSP LatinIME, 1080x2400, 420 dpi, cold boots with
  `-no-snapshot -memory 2048`). The keyboard's top is 1499 px in portrait and 496 px in landscape (from the InputMethod
  window's frame and given content insets). 1 dp = 2.625 px.
- **Data:** app data was saved with `run-as tar` before the run (`appdata26-before.tar`) and restored at the end. Row counts,
  the `attack` and `medication` rows and the preferences file are byte-equal to the backup. Rows that needed it seeded the
  one migraine's note by SQL (original 17 characters, or 996 characters), or emptied the medication tables for the
  first-medication form (F4). "DELETE" was never typed; the Delete all data field got "qaqa".
- **Driver:** `migrely-work/qa-kbd8/`: `kb.py` (adb toolbox; keys and text go only to a focused EditText), `cell.py` (one U9
  cell, checks C1 to C5), `away8.py` (row F1), `f2row.py` (row F2), `autosave8.py` (W-AUTOSAVE and the first-line tap),
  `life8.py` (rotation, process death), `drive8.py` (K5, derived from 3.6's `drive36.py`: swipes scale with the window and
  Home targets are scrolled to), and the chains `chain*.sh` with 300 s per run. Raw output: `chain*.out`, per-run logs,
  dumps and screenshots in `5582/`.
- **Host load:** 1.4 to 11.7 (one spike during a portrait run; that cell's result matched its rerun). Swap was 8.4 to 8.8 GB
  used throughout.
- **Driver errors found and fixed during the run (no rows counted from them):** `monkey -p … 1` launches the app and then
  injects one random event, which here reset the rotation lock, so the first two "landscape" cells ran in portrait; all
  launches now use `am start -n`. Landscape swipes now run in Home's left pane. A refocus tap landed under the top bar where a
  field's unclipped bounds still reach; taps now start below it.

## Rows

C1 to C5 are the ux matrix checks (U9): C1 the cursor or input line at least 8 dp above the keyboard (judged on a crop of the
strip above the keyboard), C2 Back's bounds as with no keyboard and a first tap leaves, C3 nothing under the status bar
(title node unchanged), C4 the control below can be reached, C5 closing leaves no blank space and no jump.

### Review 7.1 rows added by 7.3 (the f05814f2 change)

| Row | Head (76e5afcd) | Control |
|---|---|---|
| **F1** note, 100% portrait: keyboard open, type, scroll up to Time and Pain by hand, close with Back | **FAIL when the last word is composing: 4/4** (top node `Started@352` before, `Skipped meal@384` after, the note back in view at 1719-2053); **PASS when committed: 2/2** (top node unchanged). With "Remove animations": composing 1/1 FAIL | 51e22355: composing 1/1 moved, **committed 1/1 moved** (the old pull-back; the head fixes this case). `develop`: 2/2 the window un-pans and the note does not come back into view |
| **F1** note, 200% landscape | composing 1/1 FAIL (top `1 · Less active@257`, after `Notes@294`) | 51e22355 committed 1/1 stayed: the control cannot fail here, the scroll away was short |
| **F1** Edit medication's name, 100% portrait | not decisive: the whole page fits once the keyboard closes (Save ends at 1888 of 2274), so the page settles to its top whatever the code does | |
| **F1** Edit medication's name, 200% landscape, composing | PASS 1/1: top node `Preventive@322-413` unchanged, more nodes shown below | |
| **F1** reopen by a tap in the field | PASS 7/7: the line well above the keyboard (crops `away*-reopen-crop.png`) | |
| **F2** note focused, keyboard up, scroll to Pain, add a first 10 | PASS: composing 2/2, committed 1/1, "Remove animations" 2/2. The D167 card ("Sudden, extremely bad pain?", Got it at 534-597) inside the viewport at +2.0, 3.1, 4.5 and 6.2 s; the note not in view; screenshot `f2A1-head-card-small.png` | 51e22355: also PASS 2/2. **Did not reproduce on this device**: the pain dialog hides the keyboard, so the "keyboard closes and opens again" path of the 7.2 test never happens here. The row rests on 7.2's mutation test. TalkBack's reading of the card was not checked (no TalkBack on the API 26 image) |

### U9 matrix, API 26 (part B) and extra cells

| Cell | C1 | C2 | C3 | C4 | C5 |
|---|---|---|---|---|---|
| F1 100% portrait | ✅ ENDQA line 32 px (12 dp) above | ✅ clickable Back [12,85][138,211] open and closed; first tap leaves | ✅ | n/a | ✅ settles to the page end, no blank (screenshot) |
| F1 200% portrait | ✅ ~25-40 px (10-15 dp) | ✅ [12,98][138,224]; leaves | ✅ | n/a | ✅ |
| F1 100% landscape | ✅ ~36 px (14 dp) | ✅; leaves | ✅ | n/a | ✅ |
| F1 200% landscape | ✅ ~42 px (16 dp); 265 px (101 dp) between the bar and the keyboard | ✅; leaves | ✅ | n/a | ✅ |
| F4 200% landscape | ✅ input line and "2/60" shown, the label gives way | ✅ | ✅ | ✅ "Add and log" 328-475, keyboard top 496 | ✅ |
| F5 200% landscape (D7, no room) | ✅ "qaqa" line 66 px above, field box 6 px above | ✅; leaves | ✅ | ✅ row stays behind the keyboard, shown after Back at 912-1059, disabled | ✅ |
| F5 100% portrait (D7, room), extra | ✅ | ✅; leaves | ✅ | ✅ row rides at 1384-1447 above 1499 | ✅ |
| F2 100% portrait, extra | ✅ | ✅; leaves to My medications, the name not saved | ✅ | ✅ Save 1376-1433 | ✅ |
| F3 100% portrait, extra | ✅ field with "0/20" ends at 1499 | ✅ | ✅ | ✅ Save | ✅ |
| F3 200% landscape, extra | ✅ "2/20" shown | ✅; leaves | ✅ | ✅ Save 354-449 | ✅ |

Controls (`develop` ea41869a, API 26): F5 200% landscape: Back `[0,0][0,0]` and the title gone with the keyboard open (the
window pans, #83), the Delete row at 708-813 under the keyboard, a tap on Back's place stays on the screen. F4 200% landscape:
Back `[0,0][0,0]`. So K2 and K3 can fail on API 26.

### K5, #145 on API 26 (adds 130% and landscape to 3.6's 100% runs)

Pass = after the keyboard closes, Home and History show their full content: Home's scroll viewport, "Migraine started" and the
day cells equal the baseline, History's list reaches the window's bottom.

| Run | Head | `develop` control |
|---|---|---|
| 130% portrait, note COPY | ✅ Home viewport 63-1814, "Migraine started" 1917, 7 day cells = baseline; History to 2274 | ❌ History ends at 1499, Home viewport 63-1039, "Migraine started" at 1142 (775 px up) |
| 130% portrait, Edit medication COPY | ✅ | |
| 130% portrait, Delete all data COPY | ✅ | |
| Landscape, note COPY | ✅ Home viewport to 1080, History to 1080 | ❌ History viewport ends at 496, Home 63-496, "Migraine started" 470 -> 178 |
| Landscape, Edit medication COPY | ✅ | ❌ Home 63-496 |
| Landscape, Delete all data COPY | ✅ | |
| Landscape, History -> details COPY -> History | ✅ row (1168,299)-(2232,446) unchanged | |

Every COPY passed the guard (toolbar shown, gone after the tap, text unchanged). Head 7/7, control 3/3 stale.

### Single rows on API 26

| Row | Result |
|---|---|
| W-AUTOSAVE, 200% portrait, 996-unit note: tap, "zz", 1.2 s, "yy", 1.2 s | ✅ 2/2: the page (Notes header and field bounds) unchanged across the three reads; the saved note has "zzyy" together at index 106 of 1000 |
| Tap the first lines of a 996-unit note | ✅ cursor where tapped: "zzyy" at index 2 (200%) and 4 (100%), page unchanged at 200%. ⚠️ At 100% the page moved up 234 px (89 dp) as the keyboard opened, though the tapped line was above the keyboard (2/2): see observation 2 |
| Light theme, F1 100% portrait | ✅ three frames during the close (0, ~0.3 and ~1.3 s): no band, the light page fills the strip (`light-close-strip.png`) |
| Rotation with the keyboard open, 200%, portrait -> landscape -> portrait | ✅ no crash (same pid), note kept, no blank. The keyboard closes and the note loses focus on rotation (not compared with `develop`) |
| Process death, 100%: keyboard open, type, HOME, `run-as kill -9`, launcher | ✅ the details screen comes back, the note kept ("pdx" saved), no blank |
| Hardware keyboard, 100% landscape (`show_ime_with_hard_keyboard` 0): Tab into a 996-unit note, Up x6, Down x12 | ✅ note focused after 40 Tabs with the cursor at the end, line in view; the cursor stays in view on the arrows; note still 996 units (no stray characters) |
| F6 custom trigger dialog, 100% portrait and 200% landscape | ✅ still usable. At 200% landscape Cancel and Add sit under the keyboard and are reachable after Back; identical on `develop` |
| F7 date picker text input, 100% portrait and 200% landscape | ✅ still usable, same pattern as F6 at 200% landscape, identical on `develop`; the dates were not changed |

### K8, release manifest

✅ `:app:checkReleasePermissions` exit 0. `aapt2 dump permissions` of the R8 APK equals 1.2.1's R8 APK
(`qa-195/apk/r8-v121-5a15b3f8.apk`): 8 lines, `diff` empty. `MainActivity` has `windowSoftInputMode=0x10` (adjustResize).
`git diff v1.2.1 76e5afcd -- app/src/main/AndroidManifest.xml` adds only that attribute.

## Finding 1 (NO-GO): closing the keyboard mid-word pulls the page back to the note

- **Steps (API 26, emulator-5582, head 76e5afcd debug, 100% portrait):** open a migraine's details from History, scroll to
  Notes, tap the note, type a word without a trailing space (`adb shell input text "aura%sonce"`; the keyboard keeps "once"
  underlined as a composing word), scroll the page up to Time and Pain by hand, press Back to close the keyboard.
- **Expected (app-shell "Closing after scrolling away", D180, U6):** the page stays where it was left.
- **Actual:** the page scrolls to the note (it ends at 1719-2053, the page's end). 5/5 with composing text. With "once "
  (committed) the page stays 2/2. 51e22355 moves in both cases; `develop` does not bring the note back.
- **Evidence:** `migrely-work/qa-kbd8/chainaway-head.out`, `chainA-head.out`, `chainCtl.out`; dumps `5582/awayA*-away.xml` and
  `-closed.xml`.
- **Likely cause (from reading the code, not proven by a mutation):** `CursorFollowingTextField` keys its bring-into-view
  effect on `layout` (`LaunchedEffect(focused, value.selection, layout)`). When the keyboard closes, it finishes the composing
  word; the composing underline is part of the text layout, so a new `TextLayoutResult` arrives, the effect runs again,
  brings the cursor line into view and sets `following` again. `KeyboardFollow` is not consulted on that path. The
  single-line fields did not show it (Edit medication's name stayed, 1/1).
- **Likely also on API 36:** Gboard composes the current word the same way. Not checked (5580 did not boot).

## Observations (no issue filed)

1. **Back's icon node is 11 px shorter (finding 1 of task 6.2) also with the keyboard closed.** On API 26 the `Back`
   description node reads [43,116][106,168] instead of [106,179] whenever page content is scrolled under the top bar: with
   the keyboard open, and after it closes at the same scroll position (`a26-F1-100P-closed.xml`, `away*-closed.xml`). The
   clickable Back node [12,85][138,211] never changes, and the first tap left the screen 10/10 (head cells). Content scrolled under the top
   bar keeps semantics nodes there (for example trigger chips reported at [42,168][511,210] while drawn hidden), so
   TalkBack's explore-by-touch over that strip may land on a hidden chip. Not checked with TalkBack and not compared with
   `develop` at the same scroll position, so not filed.
2. **U4 at 100%:** a tap on the first line of a 996-unit note that fills the window moves the page up 234 px when the keyboard
   opens, though the tapped line was already above the keyboard. The cursor stays where tapped and the line stays in view.
   It looks like Compose's own "keep the focused field in view when the viewport shrinks" for a field taller than the
   viewport. S4 at most.

## What these rows do not prove

- **Nothing on API 36:** no Gboard, no TalkBack (K2 "TalkBack reaches Back", U8, F5 order, the D167 card's reading), no K4
  message rows, no 360 dp window, no split screen or floating keyboard, no RTL (this app does not mirror), and the 10 U9 cells
  the engineer did not walk. The engineer's 6.2 walk was on 24109541, before f05814f2 changed both helpers.
- **No R8 build on a device:** the R8 APK was built and read (K8) but not walked.
- **8.3 (ux F5, the Edit medication error with TalkBack):** not run.
- **F2's negative control did not fail on this device**, so the row cannot tell the two helpers apart here; it rests on 7.2's
  mutation test.
- **C1 margins** are read by eye from crops (cursor line to the keyboard's top), not measured from the cursor rectangle.
- A real Android 8 phone, other keyboards, API 27 to 35, foldables and tablets.

## Device state at the end

emulator-5582: the `develop` ea41869a debug APK reinstalled (sha256 read back), app data restored from the backup (equal),
font 1.0, rotation 0 with auto-rotate on, animator scale unset, transition and window 1.0, `show_ime_with_hard_keyboard` 1,
no accessibility service, theme preference as before (no `theme` key). Shut down with `sync` and `reboot -p`.
emulator-5580: never finished booting, nothing was changed on it, off. QA's own idle Gradle and Kotlin daemons (started by
this build at 12:17, 5 GB) were stopped by PID before the second 5580 boot; it did not help.

## Next

`android-engineer`: fix finding 1 (and decide on observation 2), with a red test that finishes a composing word on close.
`qa-engineer`: rerun row F1 (composing and committed, head and control) and the API 36 rows of 8.1 once a QA emulator on API
36 boots; then 8.2 (retest and close #141, #83, #145) and 8.3.
