# QA record: fix-short-window-typed-line (#245, T75, BRD D192)

## Verdict

**QA (qa-245), 2026-11-19: GO with recorded limits** for merging `bugfix/245-short-window-line` @
**01e301fc36e9f4a51b799a7e137938ba2c289902** into `develop` as 1.3.2 content (tasks 7.1 and 7.2).

- **The fix works where #245 was found, and the controls show it.**
  - At 360 dp in landscape at 200% (API 36, Gboard), the line being typed is whole: 112 of 112 px on Attack details
    (end, start, middle), Log medication Name, Edit medication Name and Usual dose, and the Delete all data word. The
    top bar has stepped aside, and nothing of it is drawn over the page.
  - On v1.3.1 the same cells show 40 of 112 px (Details) and 68 of 112 px (Log, Name, dose). The Delete word shows
    65 of 112 px. So dose and Delete are fix cells, not guards.
- **The reduced margin (R2, SW13) is proven, and F1 is red.**
  - In the band, v1.3.1 cuts the line by exactly 133 − H px: 96 and 106 of 112 px on Details, 94 and 106 on the
    one-line fields.
  - The candidate keeps the note's line whole (112) with margins 5/0 and 15/0.
  - The F1 mutation (v1.3.1's one-line rect on top of the candidate) gives 94 and 106 of 112 px, the same as
    v1.3.1. On #239's Save path it gives 90 and 102 of 112 px. So the margin code in `SingleLineFieldInView` is not
    inert on a device; it is what keeps the line.
- **Recorded limits** (none is S1 or S2; details under "Limits"):
  1. **#254 (S4, filed).** On one-line fields the input line sits 2 px above where `SingleLineFieldInView` computes
     it. In the band at H = 113 px, and on the Save path at H 113 and 125, 1 to 2 px of the line box go under the bar
     (111 and 110 of 112 px). No glyph pixel is lost. The same offset gives a 19 px clearance instead of 21 px
     everywhere, on v1.3.1 too.
  2. **SW10 (messages) and SW12's RTL and hardware-keyboard cells were not run on a device.** Their proof rests on
     the engineer's Robolectric tests (`MessageBarAsideTest` with its mutation; `TopBarAsideTest` "no inset").
  3. **SW8 "60 fps":** the emulator's compositor produced 14 to 21 distinct frames per second, not 60.
  4. **K2's named control** (a scroll-on-close mutation) was not built for the device. The v1.3.0 control row was void
     (#238's trap 1). The counter's ability to fire is shown by the app's own scroll on Save (13 and 19 events).
  5. **SW12 rotation:** turning to portrait with the keyboard up closes the keyboard (the activity is recreated), so
     "portrait with the keyboard up" was not reached.
  6. **SW9, after closing: Back was not reached by swiping back** (40 swipes, 2 of 2 runs). TalkBack's previous-item
     order in Attack details' chip sections cycles. v1.3.1 shows the same broken order, and so does the candidate at
     411 dp, where the bar never moves. So it is pre-existing and not this diff: **#255 (S3, accessibility, filed)**.
     While the bar is aside, the walk never lands on Back or the title.

## Builds and devices

| Build | Source | sha256 (APK) | Read back on device |
|---|---|---|---|
| candidate | detached worktree `migraine-app-qa-245` at 01e301fc, clean tree, `:app:assembleDebug` at load 2.8 | `a3d74d5591971a97fd911d0e925e450eca3e2f4e46516928c926e3d33c1015e8` | `a3d74d5591971a97` |
| v1.3.1 control | QA's debug APK of `develop` 76c38c38 (product code equals v1.3.1: `git diff v1.3.1 76c38c38 -- features designsystem app core` empty) | `fde165dbc3a1c56b63cfd6b56955b9f631e4a6ca91da9220b790c3e003a0e012` | `fde165dbc3a1c56b` |
| F1 (SW13 control) | 01e301fc + `f1-mut.diff` (the one-line third rect back to `lineTop − marginPx … + marginPx`). **Rebuilt by QA:** same bytes as the engineer's APK | `9365f1dddc659d5741650f547957f818c0b2ee5b56a8eae4c3fe21be2bd0ac9c` | `9365f1dddc659d57` |
| MAside (SW3/SW6 control) | 01e301fc, `TopBarAside.update`: `aside = true` | `d2c2bd44edf2ce2d1f1474a146258525f9ce1dd3838065d30ace32dec963db78` | `d2c2bd44edf2ce2d` |
| M64 (SW4 control) | 01e301fc, `TypingSpace.decide`: the line height replaced by 64 dp | `89af83c39dc39e269374a09fa9b5ab3d4b99ddc60949bcd1f8e6661c85ab48a3` | `89af83c39dc39e26` |
| v1.3.0 (K2 attempt) | QA's 1.3.0 debug (2f7745c3) | `782cf3d3e80efc1a5b0cd86e34d65a5d99a3d9b65d48378c7b5c53f7a0bd0199` | `782cf3d3e80efc1a` |

- The mutations were built in `migraine-app-qa-245m`, each from a clean tree, and reverted after each build
  (`git status` clean; `logs/build.log`).
- **Each mutation is checked in the dex** (`dexdump`):
  - MAside's `TopBarAside.update` lost its `if-ge` comparison.
  - M64 has one more `roundToPx` call in `TypingSpace` (2 against 1).
  - F1's bytes equal the engineer's build.
- **Devices** (one at a time; every adb call with `-s`):
  - `emulator-5580` `Migrely_QA_API_36`: Android 16, Gboard, TalkBack 16, headless, cold-booted before the
    keyboard-up rows.
  - `emulator-5582` `Migrely_QA_API_26`: Android 8.0, AOSP keyboard, no TalkBack.
- Every row is a fresh install with the APK hash read back, then Maestro seeds (a past migraine; Ibuprofen where
  needed), launched with `am start`.
- **Method** (QA's re-measure, `qa-245/bin/m245.py`; harness `migrely-work/qa-245b/bin/q245.py`):
  - Before the tap, the page is brought to the field by slow drags. After the tap, only typing and DPAD keys act, so
    every later scroll is the app's own.
  - The caret is the rows that change between two screenshots 0.6 s apart. Compose draws it over the line box, so
    its height is the line: 112 px at 200%.
  - Visible part H = keyboard top − page top. The keyboard top comes from the IME window on API 36 and from the
    page's end while the keyboard is shown on API 26.
- **"Bar drawn over the page" is checked in pixels, not just in the dump** (the engineer's warning).
  - Before the tap, a mask is taken of the bar's glyph pixels over the full width, rows 70 to the page top. A cell
    reports the share of those pixels still unchanged.
  - A bar that is there reads 0.99 to 1.0. An aside bar reads 0.007 to 0.07.
  - Screenshots were looked at for SW1, SW7, the Save cell and the SW8 frames.
- **Host load:** `logd` held the 1-minute load at 9 to 15 with low CPU, and an iOS simulator spiked it to 60+.
  - The geometry rows passed a gate at 16 and record their own load.
  - Every TalkBack row ran at load 2.3 to 7.1.
  - Each row whose result counts and that ran at load ≥ 8 was repeated at low load, or is a layout result that the
    same cell reproduced at low load elsewhere.

## Rows (API 36, 200%, landscape unless said; H = the visible part under the bar with the keyboard up)

| Row | Window | Candidate 01e301fc | v1.3.1 control | Mutation control |
|---|---|---|---|---|
| SW1 Details note, end / start / middle | 360 dp (`wm size 945x2100`), H 61 | **112 of 112 px** each. Clearances 122/21, 21/122, 122/21. Back gone, bar pixels 0.04–0.05, shot checked (no title, date or Back over the page) | **40 of 112 px** each (278–318, 257–297), Back present | – |
| SW2 Log medication Name | 360 dp, H 87 | **112 of 112**, clearance 19/124, Back gone, bar 0.043 | **68 of 112** | – |
| SW2 Edit medication Name | 360 dp, H 87 | **112 of 112**, Back gone, bar 0.07 | **68 of 112** | – |
| SW2 Edit Usual dose | 360 dp, H 87 | **112 of 112**, Back gone, bar 0.02 | **68 of 112**: a fix cell | – |
| SW2 Delete all data word | 360 dp, H 87 | **112 of 112** (161–273), row behind, Back gone, bar 0.007 | **65 of 112**, row behind: a fix cell | – |
| SW3 Details | 411 dp, H 137 | Bar stays: Back at [171,129][234,192]. 112 of 112, margins 21/4 (end, middle), 4/21 (start), not evened | Identical | **MAside: Back gone (red)** |
| SW13 Details note | `wm 1060`, H 117 | **112 of 112**, margins 5/0 and 0/5, bar stays | **96 of 112** (cut 16 = 133 − H) | – |
| SW13 Details note | `wm 1070`, H 127 | **112 of 112**, margins 15/0 and 0/15, bar stays | **106 of 112** (cut 6) | – |
| SW13 Log Name | `wm 995`, H 113 | **111 of 112** (line box 230–342 against the page top at 231; #254), bar stays | 94 of 112 | **F1: 94 of 112 (red)** |
| SW13 Log Name | `wm 1007`, H 125 | **112 of 112** (242–354) | 106 of 112 | **F1: 106 of 112 (red)** |
| SW13 Edit Name | H 113 / H 125 | 111 of 112 (#254) / **112 of 112** | 94 / 106 of 112 | **F1: 94 / 106 (red)** |
| SW13 Edit dose | H 125 | **112 of 112** | – | – |
| SW13 + Save cell (#239's path): Name emptied and focused, keyboard up, Save tapped | H 113 / H 125 | Name keeps focus; **110 of 112** at both (#254), reproduced in a second run (c3) | v1.3.1 has no #239 path: Name not focused, not a control | **F1: 90 / 102 of 112 (red)** |
| (a) Edit Name | `wm 985`, H 103 | Bar aside, **112 of 112**, Back gone, bar 0.07 | 84 of 112 | – |
| (a) Delete word | `wm 985`, H 103 | Bar aside, **112 of 112**, row behind, bar 0.014 | 81 of 112 | – |
| (b) Delete row decision | `wm 1245 / 1250 / 1255`, H 328 / 333 / 338 | Row **stays behind** (button at 1035–1145 against keyboard 559–569); at 1250 the word's line is whole (407–519) | Row **rides** (button 412–527) | – |
| (b) bounds | `wm 1240` / `1260` | Behind / rides | Behind / rides | – |
| (e) Edit Save refused, bar aside | 360 dp | Name focused; input line whole (82–194). "Enter a name." shows above the keyboard with its lowest few px under it (U1: input line first) | – | – |
| SW6 portrait 100%: Details, Log, Edit, Delete; 200%: Details, Log, Edit, Delete | full size | Bar stays (Back at its pre-typing bounds), line whole, Back present after Back | – | **MAside, Details 100%: Back gone (red)** |
| (d) one-line follow in portrait | 130% and 200% | Edit Name: whole (81 / 112 px), already in view, 0 page moves. Edit dose: whole (81 / 112 px); the follow moves the page one way only (12 and 25 moving frames, 0 reversals). The check can fail: on Details it measures 10 moving frames | – | – |
| SW7 while aside | 360 dp | Cursor up 6 lines (line 21 px under the status bar), 10 more lines, Gboard ?123 and emoji panels (shots), hand scroll down and up: **Back and the bar never come back (6 of 6)**; keyboard top 318 throughout | – | – |
| SW8 animations on (recording) | 360 dp | 127 frames (14.3 fps). Opening: the bar is clipped from below in the keyboard's frames (17–21 of 15–27) and is gone before the keyboard settles. Closing: the bar moves in exactly the keyboard's frames (92–106). Glyph brightness in the kept rows 1.0: **no fade**. No frame shows bar text over page text (contact sheet) | – | – |
| SW8 Remove animations | 360 dp | **One step:** the bar 194 → 0 rows in the same frame the keyboard goes 0 → 627 rows (frame 9), and back in one step (frame 20); no intermediate height | – | – |
| SW11 / (c) dark room, Log medication from attack mode | 360 dp | Line whole (112). Back gone; bar pixels over the full width 0.007. The top band's most common colour is the dark-room background before and after, and in all 49 opening and 21 closing frames: **no lighter frame** | – | – |
| SW12 rotation with the keyboard up | 360 dp | Portrait: the keyboard closes (activity recreated), Back present. Back to landscape: Back present; a new opening decides again (bar aside, keyboard 318) | – | – |
| SW12 RTL, hardware keyboard | – | **Not run** | – | – |
| SW10 message | – | **Not run** on a device | – | – |
| SW9 TalkBack walk (40 swipes back) | 360 dp | **While aside: never Back or the title** (0 of 80 stops, 2 runs; the keyboard stayed up). **After closing: Back not reached in 40 swipes** (2 of 2): previous-item order cycles through 13 chip stops (#255, filed S3) | v1.3.1: the same broken order in the chip sections (`t7`) | Candidate at 411 dp, where the bar never moves: a loop too (`t8-sw9-411`). So it is pre-existing, not this diff |
| SW4 API 26 guard | 411 and 360 dp | See the API 26 section: bar stays, line whole with ≥ 8 dp | QA's a26dev rows (same numbers) | **M64: bar gone at 360 dp (red)** |

## K1 and K2 (#238 guards) and #239 in the short window

| Row | Candidate | Control |
|---|---|---|
| K1 KR1 (TalkBack, Back once; Details, Log, Edit at 100% portrait, Delete at 200% with the field in view; #238's `tbrow.py` unchanged) | **8 of 8**: 0 px page shift, 0 TYPE_VIEW_SCROLLED, speech "keyboard hidden" (Delete: the long-press hint), a double tap reopens the keyboard in the field, database unchanged | v1.3.0's jumps are in #238's record (KR1, KR3); not rerun |
| K1 KR4 (dump bounds, no TalkBack; four screens at 100% portrait and 200% landscape at 411 dp; #238's `run36.py` path) | **8 of 8**: 0 px field move, focus kept | As above |
| K2 / SW5 Details 360 dp, bar aside, TalkBack VERBOSE, close by Back | Bar back at its pre-typing bounds [155,129][218,192]; page top 63 → 257; field lower by **exactly 194 px (the bar's height)**; **0 TYPE_VIEW_SCROLLED** in 3 s; TalkBack silent | v1.3.0 row void (see Limits 4) |
| K2 / SW5, close by Gboard's hide key | The same: 194 px, 0 events, silent | – |
| K2 / SW5 after a drag to the page end while typing | Bar back, field lower by 194 px, 0 events; only TalkBack's long-press hint for the focused node | – |
| #239 V1 at 360 dp, bar aside, keyboard up, Name empty, Save by TalkBack (reached by one swipe, double tap) | **2 of 2: exactly 1 "Enter a name." within 3 s** (0.58 and 0.57 s). TalkBack then moves its own focus to Name and reads the field ("Error: Enter a name." at about 3.95 s; TalkBack-caused, as in the #239 record). "Invalid input" 0 times. Name's input line whole after (82–194). The app's scroll to Name logged 13 and 19 TYPE_VIEW_SCROLLED (the counter's positive control) | – |

**TalkBack log level:** VERBOSE was set and read back before each TalkBack block. ERROR was set and read back after
each one (`logs/tbll-*.log`).

**Console touches in landscape.** TalkBack on this image hears only emulator-console touches. A calibration with
TalkBack off tried all 8 axis transforms on Details' Back; only T110 (swap, flip) taps it (`cal-map`). With TalkBack on,
T110 swipes are heard, but touches near the top of the landscape screen are not (`diag-tb`). So V1 reaches Save by
swipes and presses it with TalkBack's double tap, which acts on the focused node wherever it lands.

## API 26 (emulator-5582, AOSP keyboard)

`show_ime_with_hard_keyboard` was 0 at both boots, so the AOSP keyboard drew no view. The rows set it to 1 and put
back what they found (read back as 0 after). The first pass, run before this setting was found, is void (`a1`, `a2`).

On API 26 the screenshots come at the panel's physical size under `wm size`, so caret pixels are scaled to the window's
logical px (factor 1.143 at 360 dp). Before that fix, the 360 dp cells (`a3`) were mis-measured and were rerun as `a5`.

| Row | Window | Candidate | Control |
|---|---|---|---|
| SW4 Details note, end / start / middle | 411 dp, H 239 | Bar stays (Back [43,129][106,192]); **112 of 112**, clearances 106/21, 21/106, 106/21 | M64: identical (H 239 > 168 px, so it cannot differ here) |
| SW4 Details note | 360 dp, H 156 | Bar stays; **line 280–392 (112 px) against the keyboard top at 413**, clearances 23/21 and 20/23: R1, exactly ux's numbers | **M64: the bar is gone** (error shot `a6-det-360-error.png`, `a4` the same): **red**. My measurement then crashed on a crop at the page top, so there are no line numbers for M64 |
| SW4 Log and Edit Name | 411 dp, H 265 | Bar stays; 112 of 112 (250–362) | – |
| SW4 Log and Edit Name | 360 dp, H 182 | Bar stays; whole (249–362), clearances 18/51 | M64 Log: bar stays (H 182 > 168, correct) |
| SW5 close by Back, no TalkBack | 360 and 411 dp | The bar never left; Back at the same bounds; field moves 0 px | – |
| SW8 on API 26 | 360 dp | The bar never leaves on API 26 (the guard), so this cell has nothing to animate; recorded (`a3-sw8-360`), not analysed | – |

## Limits and observations

1. **#254, S4.** See the verdict. Reproduced on 2 screens in 2 clean runs at low load. The F1 and v1.3.1 numbers show
   that the candidate is a large improvement there; the 2 px is what is left.
2. **SW10 and SW12 (RTL, hardware keyboard): not run on a device.** The engineer's Robolectric tests cover them, with
   mutations (3.4 `MessageBarAsideTest`; 2.1 "no inset"). The #239 record found the app stays left to right under
   per-app Hebrew and Arabic on this image, so RTL needs a system language. A console key collapses Gboard to the
   pill (no inset); I did not run it, because it would have spoiled the keyboard-up rows.
3. **SW8 frame rate.** The recordings hold the frames the compositor produced (14 to 21 per second on average), not a
   60 fps capture. "The same frames as the keyboard" is shown at that resolution.
4. **K2's control.** The scroll-on-close mutation was not built for the device; its control is the engineer's
   Robolectric one (4.1). My v1.3.0 row is void: TalkBack starting closed v1.3.0's keyboard and Back then left
   Details (#238's trap 1). My counter can fire: it logged the app's own scroll on Save (13 and 19 events).
5. **SW12 rotation:** see the verdict, item 5.
6. **(e) supporting line:** at 360 dp with the bar aside, after a refused Save, the error line's lowest few px are
   under the keyboard. The input line comes first and is whole, as U1 orders.
7. **#252** (one-step keyboard on an already focused one-line field in the band) is out of scope and was not re-run.
8. **Harness voids, kept as a record:**
   - `c1-del-360` (key clash);
   - `c2-sw8a0-360` (Home card; rerun as `c3`);
   - `ma1-p2.0-name` and `ma1-name-1007` (no medication seeded; MAside is red in two other cells);
   - `t1-v1`, `t1-sw9`, `t3-*` (console mapping; rerun as `t4`);
   - `t5` (my chain race turned 5580 off mid-step; rerun as `t6`);
   - `t2` (v1.3.0 K2: void, see 4);
   - `a1` and `a2` (API 26 booted with `show_ime_with_hard_keyboard` 0, so the AOSP keyboard drew no view and H was
     the whole page; rerun as `a3` and `a4` with it set to 1 and restored).

## What this does not prove

- **Devices and keyboards:** one emulator image per API level, with Gboard and AOSP only. There is no real phone,
  no Samsung Keyboard or SwiftKey, and no Bluetooth keyboard. The trigger uses measured H and L, so it should adapt;
  that is not shown here.
- **Font scales:** 200%, plus 130% for (d). Other scales were not measured.
- **Windows:** the band cells are single windows: H 113, 117, 125, 127; (a) 103; (b) 328–338.
- **"Whole" means the caret's line box**, judged on screenshots after the keyboard settles; SW8 is the only
  frame-by-frame row.
- **Light theme and Premium were not run** (R12). The dark room ran.

## Restore (task 7.2)

- **emulator-5580**, read back after the last row:
  - `settings_snapshot` equal to the first candidate run's start (`changed: {}`); every run's own snapshot also
    `changed: {}`.
  - TalkBack off, its log level read back as ERROR (`tbll-error*.log`, last `tbll-error6`). At the last boot it read
    ERROR before any change.
  - Font 1.0, rotation free, `wm size` physical, animator scales as found.
  - App data cleared (`pm clear` Success). Emulator off.
- **emulator-5582**: font 1.0, user rotation 0, auto-rotate 1, `wm size` physical 1080x2400, animator scale null,
  `show_ime_with_hard_keyboard` 0 (as found). App data cleared. Emulator off.
- One mid-run interruption: I stopped chain1 to change the geometry gate. The device was then restored by hand and
  matched the control run's start snapshot (`changed: {}`) before the next run.
- The candidate and mutation build worktrees were removed when clean. Nothing was pushed except this record.
- My chain race (`t5`) turned 5580 off while TalkBack's log level was being set. The next boot read it as ERROR, so
  nothing stayed changed.

## Evidence

`migrely-work/qa-245b/`:

- `rows.jsonl`, one JSON line per row with the device APK hash;
- `shots/` (screenshots), `raw/` (recordings, frame analyses, TalkBack logs), `logs/`;
- scripts in `bin/`: `q245.py`, `tb245.py`, `frames245.py`, `jump245.py`, `cal245.py`, `diag245.py`, `k1.py`, the
  chains;
- K1's rows are in `migrely-work/qa-238/walk/rows.jsonl`, prefixes `q245kr1` and `q245kr4`.

Row prefixes:

| Prefix | Build |
|---|---|
| `c1` to `c4`, `t4`, `t6`, `t8`, `a3`, `a5` | candidate |
| `k2r`, `k2s`, `k4`, `t7` | v1.3.1 control (`k2r` is a control-run prefix, not K2) |
| `f1r` | F1 |
| `ma1` | MAside |
| `a4`, `a6` | M64 |
| `t2` | v1.3.0 |
