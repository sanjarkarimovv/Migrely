# QA record: clear-message-when-intake-logged (#237, T75, BRD D190, EC-MED-16, EC-MED-17)

## Verdict

**QA (qa-237), 2026-11-18: GO with recorded observations** for merging `bugfix/237-error-stays-after-success` into
`develop` as 1.3.2 content. Tested at **8b96026b51257dc0667595f2451eacff3e3075a2**. The branch head is now
**d2fa83b4aa5bea4793305ed3db662b7c736ec7fb**, and the verdict carries over to it by diff:
`git diff --name-only 8b96026b d2fa83b4` lists only files under `src/testDebug/` (three test files and
`LogMedicationFakes.kt`, a test fake that is not built into the APK) and the change's `tasks.md`. No product path
changed.

- Design "Acceptance" A1 to A9, this diff only, with A6 and A7 because rule 2 ships. **Every row passes on the
  candidate.** Every row that needs a control **shows the defect on v1.3.1's code**: A1, A2, A3, A4, A5, A6 and A9.
  A7 and A8 have no control by design.
- **EC-MED-17 "Done before the outcome" was reached on the device without changing the app.** Another process held a
  write lock on the app's database (`BEGIN EXCLUSIVE` through `run-as … sqlite3`), so the removal had to wait.
  - Undo, then Done 0.36 s later: the screen closes to Home at once. No message shows on Home over 13 s. The removal
    is stored when the lock ends (intakes 3 → 2).
  - The same lock held past Android's busy timeout also exercises the design's third Undo row. The result page stays
    while the removal waits, then "Still logged. Something went wrong. Please try again." shows with Done, and the
    intake is still stored. On v1.3.1 the list comes back at once and "Something went wrong" shows over it, while the
    intake is still stored.
- **The 800 ms guard and Done, at 200%:**
  - **Portrait:** the cleared message's place ends above Done. Finger taps on Done 0.3 s and 0.6 s after the clear
    close the screen (4 of 4 tries).
  - **Landscape:** the place overlaps the **top 21 px** of Done's 147 px touch target. A tap there 0.3 s after the
    clear is swallowed. The same tap at 1.6 s works, and a tap on Done's centre at 0.3 s works.
  - **TalkBack:** it activates Done with a click action (`action=16`), which the guard cannot block: it is a touch-only
    layer with no semantics (`TapGuard`). Under host load TalkBack reached Done 1.07 s after the log, so no TalkBack
    tap inside the 800 ms window was produced.
  - This is the host's designed tap protection, which design D2 keeps. It is recorded as an observation, not filed.
- **No new defect filed.** Three observations are listed under "Observations". None is S1 or S2.

## Builds and devices

| Build | Source | sha256 (APK) | Read back on device |
|---|---|---|---|
| candidate | detached worktree `migraine-app-qa-237` at 8b96026b, clean tree, `nice -n 19 ./gradlew --max-workers=2 :app:assembleDebug` (40 s) | `8fe196fa369d39a342b786434556e2921e235ed113c46eb3a962f299703d6174` | `8fe196fa369d39a3` |
| control (v1.3.1 code) | QA's debug APK of `develop` 76c38c38. `git diff v1.3.1 76c38c38 -- features designsystem app` is empty | `fde165dbc3a1c56b63cfd6b56955b9f631e4a6ca91da9220b790c3e003a0e012` | `fde165dbc3a1c56b` |

- Device: emulator-5580 (`Migrely_QA_API_36`, API 36, Gboard, 1080x2400 at 420 dpi), 100% portrait unless a row
  says otherwise. Every row logs the APK hash read back from the device. Host load was under 8 before the build and
  before every timing row; G200tb2 ran at a load of 20 to 32 and is judged only on what timing cannot change.
- **Full disk:**
  - `fallocate` as shell, then as the app (`run-as … fallocate` in its cache), down to **0 KB free** in every row.
  - The engineer's trap (a write-ahead log with room left lets a write pass on a full disk) was avoided by running
    `pragma wal_checkpoint(TRUNCATE)` on the stopped app before each fill. It returned `0|0|0` every time.
  - After the fill, buttons are tapped at positions learned before it, because a `uiautomator dump` deletes and
    rewrites its reserved file and would free room for the app's write.
  - A full-disk step counts only if the app showed its own failure message **and** the database did not change.
    That held in every row.
- **Counts:** `select count(*)` on `medication_intake` and `medication` through `run-as … sqlite3`.
- **Screen states:** `uiautomator dump`. Each dump takes about 2 s, and the rows record when each dump started and
  ended, relative to the tap.
- **TalkBack 16 speech:** its own log level set to VERBOSE through its settings, restored to ERROR afterwards.
  Touches went through the emulator's touch device, because TalkBack does not see `input tap`.

## Rows

"Before, after the failed tap, after success" counts are `medication_intake` rows.

| Row | What | Candidate 8b96026b | v1.3.1 control |
|---|---|---|---|
| A1 | Full disk, tap Ibuprofen; free space, tap again | Error shown, intakes 1 → 1 (valid). After the second tap: **no message** in the dumps taken 0–2 s, 2–4 s and 8–10 s after it; "Ibuprofen logged at …" in the tree; intakes → 2. Run twice (`c1-A1`, and as the base of A8p) | "Couldn't save. Your phone's storage is full." **still there** at 0–2, 2–4 and 8–10 s; the logged line **not in the tree**; intakes 1 → 1 → 2 |
| A2 | Empty list, "Add and log" on a full disk; free space, again | Error, medications 0 → 0, intakes 0 → 0 (valid). Then **no message** at 0–2, 2–4 and 8–10 s, "Naproxen logged at …" present; medications → 1, intakes → 1 | Error **stays** at all three dumps; medications and intakes 0 → 0 → 1 |
| A3 | Undo; tap Ibuprofen while "Removed. Nothing was logged." shows | "Removed…" seen within 2 s of Undo; logged 4 s after Undo. **No message** in the dump at 0–2 s or 2–4 s after, logged line present | "Removed…" **over the result** at 0–2 and 2–4 s; logged line not in the tree |
| A4 | A1 from attack mode (dark room) | As A1: error (valid), then no message at all three dumps; intakes 3 → 3 → 4. Dark room confirmed: title peak 143 against 203 in A1, page background mean 9 against 17 | Error **stays** at all three dumps; intakes 7 → 7 → 8 |
| A5 | A1 with TalkBack (double tap on the list button) | Focus speech "Log Ibuprofen" (valid). After the double tap TalkBack says **"Ibuprofen logged at 11:43 AM"**. No "Saved.", nothing about storage. Intakes 5 → 6 | The logged line is **never spoken**; the error stays on screen. Intakes 3 → 4 |
| A6 | Log, fill the disk, Undo | Result stays with **"Still logged. Your phone's storage is full."**. Done [484,535][597,592] and Undo [483,714][598,771] shown; the message ends at y 388, so Done is not covered. Intakes 2 → 2 (still stored) | **List back** with "Couldn't save. Your phone's storage is full."; intakes 5 → 5, so the dose is still stored while the page says otherwise |
| A7 | A6, free space, Undo again | List and "Removed. Nothing was logged."; intakes 2 → 1 | n/a |
| A8 | A1 at 200% font | Portrait: logged line [42,359][1038,564], Done [447,664][633,759], Undo [445,843][635,938], no message; intakes 3 → 3 → 4. Landscape (screen turned after opening): logged line [583,294][1628,389], Done [1172,489][1358,584], Undo [1170,668][1360,763] in 2400x1080, no message; intakes 4 → 4 → 5. All whole on the shots | n/a |
| A9 | `screenrecord` across the second tap of A1 | 4 frames (the recorder stores a frame when the screen changes). Frame 0: list with the message. Frame 1 (1.43 s): logged page, message box at the page background. **0 frames with the logged page and the message together; 0 in-between colours** | 2 frames; frame 1 is the logged page **with the message**: 1 frame together. The check fails here, as it must |
| A9 fade control | Can this recorder show a fade at all? "Removed…" arrives after Undo, then is closed by a tap on its body | The close is recorded as **10 graded frames over about 170 ms** (container colour 131 → 123 → 108 → 81 → 61 → 47 → 37 → 28 → 25 → 23 → 20), and the arrival as a graded cross-fade. So a 200 ms fade on the clear would have shown up in A9 | – |

### EC-MED-17 and the guard (added rows)

The removal was slowed by a write lock that another process held on the app's database:
`run-as com.migrely.app sh -c '(echo "BEGIN EXCLUSIVE;"; sleep N; echo "COMMIT;") | sqlite3 databases/migrely.db'`,
started 0.8 s before the tap. Nothing in the app was changed. A debug-only delay was not used.

| Row | What | Candidate | v1.3.1 control |
|---|---|---|---|
| E17 | Undo, then Done 0.36 s later, while a 3 s lock holds the removal | The screen **closes to Home** at once (dump at 0.4–2.4 s). **No message on Home** in 4 dumps up to 13.5 s. The removal is stored when the lock ends: intakes 3 → 2 | not applicable: on v1.3.1 the result page has already gone when Undo is tapped |
| E17dd | Undo twice, 0.3 s apart, under a 2 s lock | One "Removed. Nothing was logged."; intakes 3 → 2 | not run |
| E17f | Undo under a 6 s lock (longer than Android's busy timeout) | Result page **kept while the removal waits** (dump at 2.0–4.0 s: "Ibuprofen logged at 11:41 AM", no message). Then **"Still logged. Something went wrong. Please try again."** with Done (4.5–8.9 s). Intakes 3 → 3 | **List at once** (2.0–4.0 s), then "Something went wrong. Please try again." over the list; intakes 7 → 7, so the dose is still stored |
| E17u | Undo under a 2 s lock, page read during the lock | **Proves nothing**: the first dump started 2 s after the tap, after the lock had ended. E17f is the "during" evidence | – |
| G200 | 200% portrait: storage error; free space; finger tap on Ibuprofen, then on Done's centre D s later (one `adb shell` line) | The error text ends at y 517 (container about 549); Done's text starts at 664. D = 0.3 s, 0.6 s, 1.6 s and 0.3 s again: **each closed the screen** | – |
| G200L | 200% landscape, the same | Error container y 231–484; Done's touch target [583,463][1946,610]. Top edge (y 467) at 0.3 s: **swallowed**, the result stays. Top edge at 1.6 s: closes. Centre (y 536) at 0.3 s: closes. Load 5.4 | – |
| G200tb2 | 200% portrait, TalkBack: double tap on Ibuprofen, touch Done, double tap | Focus reached Done ("Done. Button") and the double tap clicked it (`TYPE_VIEW_CLICKED`, `action=16`): the screen closed. The touch on Done came **1.07 s** after the log, at a host load of 20 to 32, so it is outside the 800 ms | – |
| G200tb | The same, first try | **Void**: TalkBack's focus was on Back (see Observations) and the touch on Done did not focus it, so the double tap pressed Back. Not counted | – |

## Observations (not filed)

1. **The guard covers the top edge of Done in landscape at 200%** (G200L). For 800 ms after the clear, a finger tap
   on Done's top 21 px (of 147) does nothing; the centre works. This is the host's designed protection for a place
   where a message just was (D2 keeps it), nothing is written, and a second tap works. TalkBack's click action is not
   affected (G200tb2). At most S4. Not filed, because it is the design: `ux-designer` may want to see it.
2. **TalkBack's focus goes to Back after a log.** About 0.27 s after the double tap on a list button, focus moves to
   "Back. Button" (`TYPE_VIEW_ACCESSIBILITY_FOCUSED`). The control does the same, so this is pre-existing, not from this
   diff. Back calls the same action as Done (`onBack = onDoneAction`), so a double tap there closes the screen with the
   dose kept. On the candidate the logged line is spoken first (A5).
3. **In A6, "Still logged. Your phone's storage is full." lies over the logged line.** "Ibuprofen logged at …" is not
   in the tree while that message shows; Done and Undo are clear of it. The message has no time limit and can be
   closed. This is the first-row rule (D158), which this change does not touch; the engineer saw the same.

## What this does not prove

- **API 26 was not run**, so neither the AOSP keyboard nor API 26's `pidof` was exercised here. The diff is
  screen-state logic, and API 26 runs the same Compose code.
- Clearing by a log was shown for the storage error (A1, A2, A4, A5, A9) and for "Removed. Nothing was logged." (A3).
  Not shown on the device: "Something went wrong.", "Pick a time up to now." and the 50-medication message being cleared
  by a log. The Robolectric tests on the branch cover these; the device rows do not.
- **Rotation or process death while a removal runs** (spec scenario) was not run.
- A6 did not check that TalkBack speaks "Still logged…".
- **The candidate's A9 ran at a host load of 9.0**, above the gate of 8; the control's ran at 5.6 and the fade control at 7.5. Under load the recorder could skip frames, which would hide a fade, so the candidate's "0 in-between frames" is a little weaker than it looks. The code makes that exit instant (`reduceMotion || leavesAtOnce`, design D2), and the branch has a unit mutation for the fade. Not rerun.
- A9 relies on `screenrecord`, which stores a frame only when the screen changes. The fade control shows that it does
  store a 200 ms fade as about 10 frames. A change faster than one frame is not visible to it.
- **"Error gone ≤ 1 s" is bounded by dump time:** the first dump after the tap covers 0–2 s. A9's frame times are
  the sub-second evidence (logged page without the message in the first changed frame).
- E17 slowed the removal with an external database lock, not a slow disk. A slow disk could behave differently, for
  example by timing out elsewhere.
- One emulator and one keyboard (Gboard). No real phone.

## Restore (task 7.2)

Read back on emulator-5580 after the last row (`migrely-work/qa-237/raw/restore-readback.txt`):
- `font_scale` 1.0, `accelerometer_rotation` 1, `user_rotation` 0.
- `enabled_accessibility_services` null, `accessibility_enabled` 0, `touch_exploration_enabled` 0, and TalkBack's own
  log level back to ERROR (read back on a fresh open).
- `animator_duration_scale` null, `zen_mode` 0, `airplane_mode_on` 0, `show_ime_with_hard_keyboard` 0.
- `wm size` 1080x2400, density 420.
- Storage: no filler files left in `/data/local/tmp` or the app's cache; `df -k /data` shows 4283580 KB free.
- Every batch's settings snapshot before and after: no change. The app's data was cleared, and the emulator was turned
  off.

## Evidence

All in `migrely-work/qa-237/`:
- `rows.jsonl`: one JSON line per row, with the APK hash read back. In the first candidate batch (`cand`), every row after A4 failed in the harness, because A4's migraine replaced Home with attack mode. Those rows were rerun in `cand2`, with A4 moved to the end; the failed attempts are not counted. Prefixes `cand`, `cand2` to `cand6` are the
  candidate, `ctl` the control, and `c1` the first candidate trial.
- `shots/` (43 PNGs).
- `raw/*.mp4`: A9 for both builds, plus the fade controls `cand4-A9X` (void: the dismiss was not captured) and
  `cand6-A9X2` (counted).
- `raw/*-A5.log` and `raw/*-G200tb*.log`: TalkBack's logs.
- `bin/h237.py`: the harness. `bin/a9frames.py`: the frame check.
- `logs/build.log`: the build.
