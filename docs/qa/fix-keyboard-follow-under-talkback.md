# QA record: fix-keyboard-follow-under-talkback (#259, BRD D194), tasks 7.1 to 7.9

## Verdict

**QA (qa-259), 2026-11-22: GO with recorded limits** for merging `bugfix/259-talkback-focus-bar-aside` into `develop` as
the content of 1.3.3. Product code is at **d7aa097e**. The branch head fd7db694 adds only docs and openspec on top
(`git diff d7aa097e fd7db694` touches no `.kt` or `.kts` file).

- **#259 is fixed where it was found, and it reproduces on the controls.**
  - With TalkBack on, in a window 360 dp tall at 200% in landscape, the candidate kept TalkBack's focus on the note and
    wrote nothing in **37 of 37 judged openings** of the note on the debug build. They are: T1 ×12, T2 ×6, motion and
    Remove-animation openings ×6, page-end ×3, reopen U3 ×6, and K1/K2 ×4. The R8 build kept focus in 4 of 4 judged
    openings (T1 ×3, T2 ×1).
  - On v1.3.2 (= develop 4d141abe code) the same rows fail.
    - T1: focus jumped to an "Impact on your day" radio in 3 of 3 rows, and the next double tap wrote impact
      (NULL→2, 2→NULL, NULL→2).
    - The R8 v1.3.2 build wrote impact too (2 of 2, NULL→3, 3→2; run at load 21 to 52).
    - U3: the note was hidden, focus jumped to a radio, and impact was written: 3 of 3.
  - Both are **S1 data writes the candidate removes**.
- **Production v1.3.1 is affected on the reopen path (U3), without writes.** Focus jumped to "Back. Button" in 3 of 3
  rows, and the next double tap activated Back. On the original T1 path v1.3.1 kept focus (3 of 3), as the spec says.
  So 1.3.3 also improves the production behaviour.
- Every new check fails on a negative control (section "Controls"). Rows that could not run say so and are not counted
  as passes.
- Four findings, none a #259 regression. Team-lead decided all four on 2026-11-22:
  - **#262** (S4, open): on the reopen path, when the keyboard arrives in one step, the hold keeps 1 px less than
    showed (56 of 57, 62 of 63 ×3). design.md's own H2 cells record "59 to 60 kept for 60 shown". This is a known limit.
  - **#264** (S3, open, pre-existing in v1.3.1): rotating with the keyboard up and TalkBack on closes the keyboard and
    moves focus to Back. Task 7.8 (cr-259 F5) cannot pass on any build. This is a known limit, planned for 1.4.0.
  - **#263** (closed, not planned): it was a harness artifact. A 114 px "close shift" was TalkBack's own start
    scrolling the note into view, measured as if it belonged to the close. With the harness fixed, the close moves the
    page by exactly the bar's height in 6 of 6 rows.
  - The verdict rule for typed cells (K1/K2) was corrected (section "Re-judgement rules").

## Builds and devices

| Build | Source | sha256 (APK) | Read back on device |
|---|---|---|---|
| candidate debug | QA worktree `migraine-app-qa-259` detached at d7aa097e, clean before and after, `nice -n 19 ./gradlew --max-workers=2 :app:assembleDebug :app:assembleRelease :app:checkReleasePermissions` (exit 0, 2 m 4 s) | `2b3c151f26388147e5e0cb295fed5434efd19a0adea9936f7defc93242701cb6` | `2b3c151f26388147` |
| candidate R8 | same build | `16a1541d3759007733beb8b5ab16a228bfca938372dbcf110492ccd13d171e14` | `16a1541d…` |
| v1.3.2 debug (negative control) | QA 1.3.2 Go/No-Go build of c1c0afab (tree = tag v1.3.2; develop 4d141abe = it + one QA doc) | `9f280590339bdaf8…` | `9f280590…` |
| v1.3.2 R8 | same | `c7a027f6f81d8407…` | `c7a027f6…` |
| v1.3.1 debug (production control) | f626c2d0 (tree = tag v1.3.1) | `fde165dbc3a1c56b…` | `fde165db…` |

- Both candidate builds are vc14 "1.3.2". The version bump comes with the release branch, and the release/1.3.3
  Go/No-Go checks the bump diff.
- The engineer's `cand8.apk` (`787d7a3f…`, d7aa097e) was hashed and **not used**: every row ran QA's own build.
- Release permissions are identical to v1.3.2 R8: POST_NOTIFICATIONS, ACCESS_NETWORK_STATE, BILLING, WAKE_LOCK,
  RECEIVE_BOOT_COMPLETED and the dynamic receiver permission. There is **no INTERNET**
  (`aapt2 dump permissions`, diff empty).
- Devices:
  - emulator-5580, AVD `Migrely_QA_API_36`, Gboard, TalkBack 16 with log level VERBOSE during rows and ERROR after.
  - emulator-5582, AVD `Migrely_QA_API_26`, AOSP LatinIME. TalkBack is **not installed** (`pm list packages`).
  - Every row checked the serial and the AVD name, `df` (stop under 2 GB) and the 1-minute host load. From
    2026-11-22 10:47 every row waits for a load under 8 and logs `load_start`.
- Rows: `migrely-work/qa-259/rows.jsonl` (409 lines). Harness: `qa-259/bin/`. Raw TalkBack logs: `raw/`. Frame
  tables: `frames/`. These are private, outside this repo.

## Rows (candidate; "judged" = load under 8 at start and end)

| Task | Row | Result | Control |
|---|---|---|---|
| 7.1 | T1 note 360 dp, TalkBack double tap, 3 s and on to a 2nd double tap; DB read before and after each tap | **12/12 PASS**: focus kept, 0 writes, line 112/112 | v1.3.2 3/3 FAIL (focus to an impact radio at +0.7 to 1.1 s, impact written); v1.3.1 3/3 kept |
| 7.1 | T2 impact 2 recorded first | **6/6 PASS**, 0 writes; in the 3 later rows (t2b) impact read back as 2 at every read (the first 3 ran before the impact read was logged) | v1.3.2 2/2 focus FAIL; a recorded 2 was cleared in dt1 n1 |
| 7.1 | T3 Log medication (usual ×2, dark room ×2), Edit name ×2, dose ×2, Delete all data ×2, at 360 and 411 dp | **10/10 PASS** | v1.3.2 Log 360 and Delete 360 kept focus (latent, as the design says) |
| 7.1 | T4 note 411 dp | **3/3 PASS** | |
| 7.1 / 7.4 | T5 TalkBack off, 360 dp, bar aside | line 112/112 on note, Log name, Edit name, Delete word (4/4) | |
| 7.1 | T6 TalkBack on: Back, hide key, Done (Gboard's action key) | **10/10 PASS judged on the close itself** (note ×4 after the #263 fix, name and Delete ×6): the field moved by exactly the bar's height (194 px note, 168 px others), 0 TYPE_VIEW_SCROLLED after the close, focus on the field | the opening's own TYPE_VIEW_SCROLLED (1 to 6 per opening) show the counter counts |
| 7.4 | T6 TalkBack off, four screens × Back, hide, Done | **11/11 PASS** (bar height, field focused) | |
| 7.4 | same closes with an accessibility client attached ("client on", `uiautomator events`) | **11/11 PASS** on px | see "does not prove": that client saw no scroll events at all |
| 7.1 | T7 Remove animations (3 scales 0, restored) | **3/3 PASS**: focus kept, one-step move, no hidden frame | |
| 7.1 | M2 rest position | candidate = v1.3.2 to the pixel: note 185..297 (360), 278..390 (411); name 82..194 | |
| 7.1 | M3 note at page end, Delete all data (Dark) | PASS: no stretch or glow in the frames | |
| 7.1 | M4 Log medication from attack mode, dark room | no lighter frame (frame mean ≤ 18.4 at start, 11.8 settled) | |
| 7.2 | R8: T1 ×3, T2 ×1 judged (+1 at load 42), U3 ×3 (load 13 to 72), T6 Back ×1 | focus kept and 0 writes in all; T6 Back 194 px | R8 v1.3.2 T1 2/2 FAIL with writes (load 21 to 52) |
| 7.3 | API 26: T1 and U3 as TalkBack rows | **not reachable** (no TalkBack on the AOSP image) | |
| 7.3 | API 26 stand-in: note opened by a tap with an accessibility client attached, 360 and 411 | 4/4: 0 writes, line whole | |
| 7.3 | API 26 T6 Back on note and Edit name, 360 and 411 | **4/4 PASS** at 0 px (the bar does not step aside there) | Enter as a key event does not close single-line fields: 2 void; AOSP has no hide key |
| 7.3 | API 26 U3 reopen by a tap at the caret (no TalkBack) | 1 run showed the hold in one step (65 px shown, 65 kept, caret line cut = H5); 1 run took the follow path; 0 writes | |
| 7.3 | API 26 M5 | **not proven**: `screenrecord` on API 26 missed the opening even with a 3.5 s pre-roll; every captured frame shows the line | |
| 7.5 | Motion, note, 360 dp, TalkBack on, cut start | **2/2 PASS** (C1, C2, +500 ms = keyboard stop), TalkBack off 1/1 PASS; a third run recorded only with the console recorder (not used) | v1.3.2: line hidden at the keyboard's stop, then the spring moves it, 2/2 FAIL C3 (TalkBack on and off) |
| 7.5 | page-end start | 2/2 within 1 px; m3 78 → 80 px (2 px) on a low-texture anchor | v1.3.2 page-end 1/1 PASS (the issue never was the page-end start) |
| 7.6 | U3 reopen: 60 px caret line 1 ×2, 60 px caret end ×2, 150 px, 350 px | **6/6 PASS**: focus kept, 0 writes; H1 and H2 hold in every captured frame (kept 62 to 71, 152, 154 px; median capture interval 18 to 19 ms during the motion, max 34 ms) | v1.3.2 3/3 FAIL (note hidden, focus to a radio, impact written); v1.3.1 3/3 FAIL (focus to Back, Back activated, 0 writes) |
| 7.7 | K1: U3 at 60 px, caret at the end, one typed "k", double tap | **3/3 PASS** + K2 (Remove animations) 1/1: focus kept; the double tap wrote 0; the only change is the typed "k" (note-only); caret line 185..297 whole | |
| 7.8 | Rotation to portrait and back with the keyboard up, TalkBack on: note ×2, Edit name ×2 | **FAIL 4/4, not counted as a #259 fail (lead decision): fails the same way on every build, see #264** | v1.3.2 1/1 and v1.3.1 1/1 the same |

**Typing bring (K1, logged, not judged):** one direction, settled in 9 to 10 captured frames (3 frames unmeasurable),
peak at least 46 to 99 px per frame. With Remove animations (K2) it is one step of 177 px.

## Controls: every check can fail

- **Focus parser** (`tbfocus.py`), offline: it gives FAIL for the engineer's develop logs (d4 n0, a radio activated;
  d4 n1, no focus, so the simulated click cleared impact) and for 9eb83586 m3b (focus to "Notes" at +3.1 s). A log
  without speech is VOID.
- **Database diff** (`dbsnap.py`), self-test:
  - impact 2→NULL = a write;
  - a new intake = a write;
  - a typed note = note-only.
  - Note text with line breaks is escaped (one row per line).
- **Row rule** (`rules.py`): in a typed cell a note-only change passes. A write to another table, a note change with
  no typing, any write by the second double tap, or a focus move each fail it.
- **Frame tables** (`frames.py`), on synthetic 60 fps videos: a 40 px leap FAILs C1, a tail FAILs C3, a 10 px dip
  FAILs C2, a hidden line FAILs H2; the good case PASSes. On device, v1.3.2's spring FAILs C3 2/2.
- `bin/selftest.sh`: 22/22 on 2026-11-22.

## Re-judgement rules (stored `verdict` fields differ)

- **K1/K2 (typed cells).** The stored verdict was FAIL because the opening read included the person's typed "k".
  The rule now excludes a change of only the note and its updated-at in typed cells, reports it as `note_only`, and
  still fails any other write. All 4 rows: note_only 1, other 0, second tap 0 → PASS. No other row's verdict changed.
- **T6 (#263).** Rows before the harness fix measured TalkBack's start together with the close. T6 is judged on
  `tb_split.close_shift` (log cleared before TalkBack starts; start shift measured from screenshots). Pre-fix rows with
  a non-bar shift are "unsplit" and not counted. A close that did not close (Enter key event) or a Back that left the
  screen (TalkBack's start had already hidden the keyboard) is VOID.

## Known limits (does not prove, 7.9)

- H5: "On a reopen with focus kept, the caret line may stay cut until the first keystroke."
- K3: "The typing bring (D180 (2), unchanged) can move up to about 100 px per frame at its peak, eased, after a rescue or a drag."
- K2: with Remove animations that bring is one step of up to about 430 px (QA measured 177 px).
- Follow-up **#260** (S4): the typing bring's speed.
- **#262** (S4): the hold keeps 1 px less than showed at a one-step inset (56/57, 62/63 ×3).
- **#264** (S3): rotation with the keyboard up and TalkBack on closes the keyboard and moves focus to Back. It is the
  same on v1.3.1, v1.3.2 and the candidate. F5 cannot pass on any build.
- Real phones, and keyboards other than Gboard and AOSP (Samsung Keyboard, SwiftKey stepwise insets), are not tested.

## What these checks do not prove

- **Rates.** 0 focus moves in 37 judged debug openings at 360 dp bounds the failure rate only below about 8% (95%). The v1.3.2
  jump itself was not deterministic: 3/3 without the recorder, 0/2 with it.
- **Per-frame motion.** Captures were 16 to 23 ms apart during the motion, but in TalkBack openings Gboard's keyboard
  arrived in one captured frame. So C1 there is per captured interval, not a proof of every 60 Hz frame. The page-end
  2 px drift (1 of 3 runs) is within the measuring noise of a low-texture anchor, so C3 at the page end is not proven
  to the pixel.
- **"Client on" scroll counts.** That client received only focus and window events and has no positive control. Only
  the px results stand.
- **API 26.** There is no TalkBack there; M5's transition frames were not captured; Done and the hide key were not
  reachable on AOSP.
- **R8.** U3 R8 rows ran at load 13 to 72. Their focus and write results stand, but the one-step inset there is why
  #262 showed. The R8 build is locally signed, not Play-signed.
- **Not reachable** (EC-UX-86): hardware keyboard, floating keyboard, split screen. Also not run: other screens'
  rotation, rotation with TalkBack off, light theme, RTL.
- **Harness artifacts handled**, not product: TalkBack's start scrolling the half-hidden note into view (#263), and
  TalkBack's start sometimes hiding the keyboard.

## Device state

- 5580 and 5582 were restored, read back and shut down.
- TalkBack is off, and its log level is back to ERROR.
- Font 1.0, rotation free, animator scales as found (null / 1.0 / 1.0), a11y timeouts null, wm size reset, adbd not
  root.
- show_ime_with_hard_keyboard on 5582 is back to 0.
- Gboard's stylus setting was never changed.
