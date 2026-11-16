# QA record: `fix-privacy-wording-and-focus` (T66, BRD D181, Refs #217), tasks 7.1 and 8.5

## Decision

**QA's verdict, 2026-11-16 12:45: GO for API 26, API 36 pending**, for the merge of
`bugfix/217-privacy-wording-and-focus` at **f846c899** into `develop`. This is a change verdict, not a release verdict.

- **Every phase 1 row the diff reaches passes on API 26, each against develop as a control.**
  - Privacy page: 7 stops, each equal to `strings.xml`, reachable at 200% portrait and landscape.
  - One spoken name for the Ended row, the time rows and the message Undo (Maestro and uiautomator).
  - Quick links stack at 360 dp 200%, where develop breaks "medicatio / n"; within 1 px of develop at 100%.
  - Home order with and without a message.
  - The three screens' order at 200%, including F2 round 2: the instant second Save keeps focus on Name on f846c899
    and loses it on 24066a1e.
  - Log by Enter clears focus; "Change the times" moves focus to Started.
  - Instrumented classes the diff reaches pass; the regression passes except 2 failures that predate T66.
- **History (review F3): the invisible stop does not reproduce on API 26**, on head or develop, in 11 cells each. On
  API 26, 4.3 has nothing to fix.
- **No open S1 or S2.** Two defects that predate T66 were found and filed: #229 (S4: on API 26 a short tap during a
  dialog's wait focuses its text field; an instrumented test fails on develop) and #230 (S3, accessibility: at 200% on
  API 26, Tab to "Change the times" leaves it behind the navigation bar, also on develop). Neither blocks T66.
- **API 36 is pending and is a condition of a full GO.** It covers TalkBack rows 2.5 and 3.6 (each of the 10 sites
  read once, label in name, "At …, change the time"), 8.3, the History and Home order with Gboard, the TalkBack pass on
  the Name error (5.5), and History on API 36. They did not run: emulator-5560 (another project) was still up and swap
  free was 1.2–1.6 GB against the 2 GB bar. Until they run, the verdict for API 36 is open, not GO.
- The merge also waits for 0.2, the owner's approval of the strings (including the phone-transfer words). That is not
  QA's.

History of this record: a first run on 2026-11-15/16 stopped at 02:19. The host slept on battery with the lid closed,
and 45 of 57 regression flows ran then, with timeouts that are void. Phase 1 resumed on 2026-11-16 at 10:13 with
`caffeinate -dimsu` and a sleep guard (`guard.sh`) before and after every flow or cell. The guard compares the last
"DarkWake from Deep Idle" or "Entering Sleep" line in `pmset -g log` with the 10:12:12 baseline and checks the lid. It
did not trip during any row below. The charger was attached from about 11:15.

## Candidate, builds and device

- Head: `bugfix/217-privacy-wording-and-focus` at **f846c899** (F2 round 2). 24066a1e was the head when this run was
  briefed; f846c899 changes only `EditMedicationScreen.kt`, its test and `tasks.md` (`git diff --stat 24066a1e f846c899`),
  and every device row below ran on f846c899.
- Control: `develop` 9861a4c2. Its product tree equals d3d98673 (`git diff --stat d3d98673 9861a4c2` lists only
  `docs/qa/fix-keyboard-follows-cursor.md` and `tasks.md`), so the d3d98673 debug APK built for T63's 8.2 is the control.
- QA branch: `qa/217-acceptance`, fast-forwarded from 24066a1e to f846c899; flow edits only.
- APKs (SHA-256, read back from the device with `sha256sum $(pm path com.migrely.app)` after each install):

| Build | File | SHA-256 |
|---|---|---|
| head f846c899 debug | `migrely-work/qa-t66/apk/head-f846c899-debug.apk` | `01155f2f1d14efb6bd221675ebcc505a1b98ef8ba4ec19cf513232882d26d670` |
| control d3d98673 (= 9861a4c2) debug | `migrely-work/qa-t66/apk/ctl-d3d98673-debug.apk` | `1af5b0cfbcf6d6bbf6099a76106b58d9f4054d0ea69051023fd79b40ecff8067` |
| 24066a1e debug (F2 control) | `migrely-work/qa-t66/apk/head-24066a1e-debug.apk` | `2cc4df562e91cff8c9771c146c93e4d36bde5387b38f35f22852fd09eafbca6f` |
| on the device at the start | `migrely-work/qa-t66/apk/device-start.apk` | `aff4e8b8485fa39f59cd190f62dc6e1cd0cb9a56d4edde9bda81c5b64f18c150` |

- `aapt2 dump resources`: `settings_privacy_purchase` is in the head APK (1) and not in the control (0).
- Device: emulator-5582, `Migrely_QA_API_26` (Android 8.0), hardware keyboard on, no TalkBack package. Shift+Tab on
  API 26 has no `input keycombination`; the emulator console sends it (`adb emu event send EV_KEY:KEY_LEFTSHIFT:1
  EV_KEY:KEY_TAB:1 EV_KEY:KEY_TAB:0 EV_KEY:KEY_LEFTSHIFT:0`). Proof: from "Period today" it went back to Settings,
  where Tab goes forward.
- Tools (not in the repo): `migrely-work/qa-t66/` (`kb66.py` focus walker, `hist.sh`, `home_msg.sh`, `neg.sh`,
  `tl.sh` wall-clock limit, `negctl/` and `setup/` flows, `out/` logs).

## 7.1 and review F1: Maestro flows

Maestro matches text and content-desc as a whole-string regex. Clearing the shown words from semantics (D181 (2)) left
some assertions empty or broken. Changes on `qa/217-acceptance`:

| Flow | Before | After | Why |
|---|---|---|---|
| `today-sky/list-opens-details.yaml` (smoke) | `visible: "Ended"` | `visible: "Change when it ended, now .*"` | the Ended row's "Ended" is no longer in the tree |
| `attack-logging/undo-window-passes.yaml` | `notVisible: "Undo"` | `notVisible: "Undo the start"` | a bare "Undo" matches no node and passes at once |
| `a11y-tour/home-with-attacks.yaml` | `notVisible: "Undo"` | `notVisible: "Undo the end"` | same |
| `subflows/wait-message-gone.yaml` (19 flows) | `notVisible: "Undo"` | `notVisible: "Undo( .+)?"` | any message Undo by its spoken name; still matches Log medication's own "Undo" button as before; "Undone." does not match |
| `a11y-tour/settings-privacy.yaml` (7.1) | old first paragraph | new first paragraph, plus the purchase sentence | D181 (1) |
| `day-markers/track-periods-off-marks.yaml` | `containsChild: "Period today"` + checked | **unchanged** | review F1 assumed the name moved onto the chip; on the device it did not (below) |

**`track-periods-off-marks` (F1 disproved for this flow).** On head after a tap, uiautomator shows the clickable chip
`[42,772][312,898]` with `checkable=true checked=true` and, inside it, a child `View` with content-desc "Period today"
(`migrely-work/qa-t66/5582/period-post.xml`). That is the same shape as on develop, so `containsChild` still matches.
`text: "Period today", checked: true` matches nothing on head (P3, red after a tap), which is why the first edit was
reverted.

Negative controls, each flow run alone (`migrely-work/qa-t66/out/neg-results.txt`):

| Id | Build | Check | Expected | Result |
|---|---|---|---|---|
| P0 | head | absolute `runFlow` paths work | pass | pass |
| N1 | head | old `"Ended"` after opening an ended migraine | red (F1 real) | red at that step |
| N1 | control | the same | pass | pass |
| N2 | head | new `"Change when it ended, now .*"` on an **in-progress** migraine (wrong state) | red | red at that step |
| N3c | head | `containsChild "Period today"` checked, chip off | red | red |
| N4c | head | `assertNotVisible containsChild … checked`, chip on | red | red |
| P3c | head | on, then off | pass | pass |
| E5 | head | old `notVisible: "Undo"` 1 s, message on screen | passes at once (empty wait) | **pass**: the wait was empty |
| E5 | control | the same | red (the old wait worked) | red |
| N5 | head | `notVisible: "Undo the start"` 1 s, message on screen | red | red |
| N6 | head | `notVisible: "Undo( .+)?"` 1 s, message on screen | red | red |
| N7 | head | `notVisible: "Undo the end"` 1 s, message on screen | red | red |
| 7.1 | control | new `settings-privacy.yaml` | red | red at the first paragraph |
| N8 | control | purchase sentence, after "Your entries stay on this phone" is seen | red | red at the purchase step |
| N8 | head | the same | pass | pass |

## API 26 regression on head f846c899

Pass 1 ran overnight. `QA_SERIAL=emulator-5582 qa/maestro.sh`, started 21:18 and stopped at 02:19 while the host slept.
Its failures were triaged and pass 2 rerun the next day, below. Pass 1 has 46 flows, pass 2 (a11y-timeout)
11, and 2 geometry flows are skipped below API 29. Report folder: `build/qa/maestro/20261115-211822`; log:
`migrely-work/qa-t66/out/reg-head.out`.

- Finished 45, passed 42. Among them: `list-opens-details` (smoke, changed), `track-periods-off-marks` (unchanged),
  and the users of `wait-message-gone` that ran (`cold-start-during-attack`, `move-start-rejected`,
  `several-attacks-storm` and the `log-attack-today` users in attack-history, day-markers, doctor-visit, month-sky and
  month-stats/summary).
- Failed 3. Triaged on 2026-11-16 with the host awake (`out/tri-results.txt`). Each flow was run alone, on head and on
  the d3d98673 control, and with the new and the old `wait-message-gone`. The old one is in `migrely-work/qa-t66/maestro-old/`:

| Flow | head, new subflow | head, old subflow | control, new | control, old | Verdict |
|---|---|---|---|---|---|
| `month-stats/medication-days` | pass 99 s | pass 95 s | pass 98 s | pass 97 s | the overnight failure was the sleep (10 min 55 s); void |
| `medication-overuse/history-medication-only` (no subflow change) | pass 86 s, pass 86 s | n/a | pass 86 s | n/a | void, sleep window |
| `data-backup/open-and-export` | fail, fail | fail | fail | fail | **flow flake that predates T66**; not the subflow, not the product |

  `open-and-export` fails in every cell at the same step. The tap on SAVE in Android's own save dialog completes, but
  the dialog stays open, and the next tap ("Export for Excel (CSV)") finds no row. The overnight "Element not found"
  was the same step. It fails with `show_ime_with_hard_keyboard` 1 and 0 alike. By hand on head, export works:
  "Backup saved." shows and `Download/migrely-2026-11-16.json` (515 bytes) appears. So the product's export works.
  The flow's way through the dialog (`Show roots` → `Downloads` → SAVE with `waitToSettleTimeoutMs: 0`) does not take
  on this image. QA follow-up: fix the flow (my file). It is not filed against the product.
- Pass 2 (11 flows, `--tags a11y-timeout`), rerun with the host awake (`out/pass2.out`): 10 passed, including
  `undo-window-passes` (changed). `in-app-messages/longer-timeout` failed ("Undo the start" not visible), as expected on
  API 26: that image has no accessibility-timeout setting (the runner prints this), so a flow that checks the longer
  timeout cannot pass there. The control fails it the same way (12:36:41).
- The two changed a11y-tour flows on head: `home-with-attacks` pass, `settings-privacy` pass.

**Regression total on head f846c899, API 26:** 57 flows ran, 2 geometry flows skipped. 54 passed; 2 failures predate
T66 and are explained above (`open-and-export`, `longer-timeout`). One failure was void (sleep) and passes on rerun.
Both tours pass.

## Phase 1 device rows on API 26 (head f846c899 against control d3d98673)

All rows use the hardware keyboard on emulator-5582 with uiautomator dumps after every key (`kb66.py walk`). A line
reads: focused node class, bounds, IN (inside the window) or OUT, its label. Shift+Tab goes through the emulator
console. Evidence: `migrely-work/qa-t66/out/hist-*.out`, `out/chain3.out`, `out/chain4.out`, `out/chain6.out`, the
dumps under `5582/`.

### 4.1 History proof (review F3): the invisible stop does not reproduce on API 26

| Cell (variant-font-orientation) | head | control |
|---|---|---|
| migraine row, 100% P | S lands on the chosen day, IN; T back to the row | same |
| Period, 100% P | same, from Period | same |
| Pain-free (Period hidden), 100% P | same, from Pain-free | same |
| migraine row, 200% P | same; the page scrolls the day into view (y 766 → 231) | same |
| Period, 200% P | same | same |
| Pain-free, 200% P | same | same |
| Period, 100% L (two panes) | same | same |
| Period, 200% L | same | same |
| day chosen by arrows + Enter, Period, 100% P | same | same |
| day chosen by arrows + Enter, Period, 200% P | same | same |
| day chosen by arrows + Enter, migraine row, 200% P | same | same |

- 11 of 11 cells per build: no key left the window without a focused node.
  - Every Shift+Tab from the first control of the day section landed on the chosen day, fully inside the viewport.
  - Every Tab from there returned to the same control.
  - The screenshot after the last Shift+Tab shows the ring on the day (`5582/hist-head/head-per-2.0-P-end.png`).
- Order before the day, both builds: Back → Insights → Doctor report → the chosen day. Previous month was disabled
  (no older migraine).
- **So on API 26 there is nothing for 4.3 to fix, and nothing differs between head and develop.** The History code is
  the same on both; only a test was added.
  - The original sighting is not explained by these rows.
  - API 36 and TalkBack are not checked; see "Not proven".

### 4.4–4.5 Home Today order

| State | head | control |
|---|---|---|
| no message, 100% P | Settings → Period today → month card → today's cell → Migraine started → Add a past migraine → Log medication → Settings (wrap); Shift+Tab the exact reverse | same |
| no message, 200% L | same order (two columns); reverse exact | same |
| no message, 200% L in a 360 dp window (`wm size 945x2100`) | same | same |
| "Migraine ended" message, 100% P (filmed) | Settings → Undo → Period today; Shift+Tab → Undo → Settings | same |
| message, 200% L (filmed) | same | not run (see below) |
| message, 200% L in a 360 dp window (filmed) | same | not run (see below) |

- The message lasts 5 s on API 26, and a uiautomator dump blocks for about 5 s while it counts down. So these walks
  were filmed (`screenrecord`, keys about 0.5 s apart), and one frame was cut 0.3 s after each key
  (`out/head-msg-P100-g-sheet.png`, `out/head-msg-L-both.png`, `out/ctl2-msg-P100-g-sheet.png`). The first Tab's ring
  on Settings was not yet drawn 0.3 s later in portrait. It is inferred: the next Tab reaches Undo, which only Settings
  leads to. In both landscape films it is visible.
- **What the device cannot show:** on this device, Period today and the month card always follow Settings in its
  column, so the empty group of 4.4 (a)/(b) never happens. That is the case D181 (4) fixes. The device rows are
  case (c), the regression check, and they pass on both builds. (a)/(b) rest on the unit tests (`HomeMessageOrderTest`,
  red on the pre-fix base per tasks 4.4).
- The control's landscape message films did not run. System UI crashed on the emulator ("System UI has stopped",
  11:55) right after 200% landscape was set. That voided the control's landscape and message cells. A rerun at
  12:00–12:05 gave the control's order rows in all three states and the 100% portrait film; the two control landscape
  films were not rerun. The crash happened between rows, not in the app. Its log had rotated away.

### 4.7 Quiet links

| State | head | control |
|---|---|---|
| 411 dp window (the device), 100% P | side by side, `[77,2151][506,2208]`, `[622,2151][956,2208]` | identical bounds |
| 411 dp, 200% P | side by side, "Log / medication" breaks at the space; whole words | identical |
| 411 dp, 100% L and 200% L | identical to control | identical |
| **360 dp window, 100% P** | `[74,1801][440,1921]`, `[520,1815][854,1872]` | `[74,1801][440,1921]`, `[521,1815][855,1872]`: within 1 px |
| **360 dp, 200% P** | **stacked**, each full width (`[42,1668][903,1805]`, `[42,1805][903,1942]`, 137 px = 52 dp) | side by side, **"medicatio / n"** (305 px tall, three lines) |
| **360 dp, 200% L (short window)** | **stacked** in the end column | side by side, broken |
| Tab order, 360 dp 200% P and L | Add a past migraine → Log medication; Shift+Tab back | same order |

Screenshot: `out/ql360-ctl-over-head.png` (control above, head below).

### 5.5 Three screens, 200% portrait, hardware keyboard

| Row | head | control (develop) | 24066a1e |
|---|---|---|---|
| Edit medication (Add), Tab order | Name → For an attack → Preventive → Simple painkiller → Triptan → Combination or other → Not sure → Usual dose → Save → Back → Name; Shift+Tab exact reverse; all IN | not run | not run |
| Down arrow on "For an attack" | focus moves to Preventive; "For an attack" stays checked, Preventive is not checked | not run | not run |
| Tab in Name | leaves the field; Name keeps "Paracetamol" (11/60), no tab typed | not run | not run |
| **Instant second Save (empty name, no database)** | Save → focus on Name ("Enter a name."); again → Name | Save keeps focus both times (never moves) | first → Name; **second → no focused node** (F2 round 2 reproduced) |
| Duplicate name (database), keyboard | Save → Name ("You already have Paracetamol."); again → Name | Save keeps focus | not run (round 1 fixed this path) |
| Duplicate name, touch Save | error shown; no soft keyboard (`mInputShown=false`, with `show_ime_with_hard_keyboard=1`); the page does not jump; focus stays where the touch left it | not run | not run |
| Log medication list order | Back → Taken at → Log Paracetamol → Manage medications → Back; reverse exact | not run | not run |
| Log by Enter | nothing focused; then Tab → Back → Done → Undo | focus jumps to Back | not run |
| Add past migraine, draft | Back → Started → Ended → Add migraine; reverse exact; one spoken name per row ("Change when it started, now …") | rows also expose "Started"/"Ended" and the date again (read twice) | not run |
| Overlap, order | Back → Started → Ended → Merge them → Change the times; reverse exact | same order | not run |
| "Change the times" by Enter | focus moves to Started | focus jumps to Back | not run |
| TalkBack on the Name error | not possible on API 26 (no TalkBack) | | |

InvalidDose cannot be reached from the UI: the dose field stops at 20 characters, the limit.

### 2.x Privacy page on screen

- 100% P, head: 6 paragraphs and the policy link with its address, in the order of D181 (1). Each paragraph equals
  its `strings.xml` value exactly (`settings_privacy_offline`, `_purchase`, `_phone`, `_files`, `_delete`,
  `settings_android_backup`). The link "Read the full privacy policy" and "migrely.health/privacy" come last.
- 200% P: all 9 texts reached by scrolling. 200% L: all reached by scrolling, checked by hand (my collector used
  portrait swipe coordinates there, a harness slip).
- Control: the old 6-stop text, as expected.
- The head strings include the phone-transfer words ("and moving to a new phone"; Your data: "in a transfer to a new
  phone"). Their approval is 0.2, the owner's.

### 8.2 Instrumented tests on API 26

The full run of the three modules stopped at 10 of 58 with no progress for 12 minutes. I stopped it by PID. The
packages were force-stopped before the next suite. Then each class was run alone (`migrely-work/qa-t66/82.sh`,
`out/82-results.txt`):

| Class (module) | head f846c899 | develop (worktree at 9861a4c2) |
|---|---|---|
| `snackbar.MessageTimeoutTest`, `MigrelySnackbarHostTest`, `MigrelySnackbarTest` (designsystem; T66 changed this code) | pass 3/3, 7/7, 7/7 | not run |
| `input.TapGuardTest`, `sky.MonthSkyCalendarKeyboardTest`, `sky.MonthSkyCalendarTest`, `theme.MigrelyDarkRoomTest` | pass 3/3, 10/10, 6/6, 5/5 | not run |
| `picker.DatePickerScreenTest` | pass 5/5 | not run |
| `picker.DateStepCalendarTest` | 1 failure on the first run, then pass 3/3 twice | pass 3/3 |
| `picker.DateRangePickerScreenTest` | **hangs** (1 of 8 in 300 s, stopped) | not run: known #144 |
| `dialog.DialogWaitTextFieldTest` | **fails** 2/2 (`aShortTapOnATextFieldDoesNotFocusAndTheNextDoes`: Focused = 'false') | **fails** 2/2, the same |
| `CustomTagDialogLimitTest` (attack/ui) | pass 5/5 | not run |
| `MedicationFieldsLimitTest` (medication/ui) | pass 14/14 | not run |

- The hang that stopped the full run is #144 (open): the range picker class never finishes on API 26.
- `DateStepCalendarTest` failed once, on the run right after the hung class was killed. It passed 2 of 2 on head
  afterwards and 1 of 1 on develop. It is not reproduced, so it is not filed.
- `DialogWaitTextFieldTest` fails the same on develop, so T66 did not cause it. T66 changes no dialog code. Filed as
  #229 (S4).
- **8.2 verdict on API 26:** every class that T66's diff reaches passes: the three snackbar classes, the date-step and
  tap-guard classes, and the two module classes. The 2 failing classes fail the same on develop.

## Findings filed (both predate T66)

- **#230 (S3, accessibility).** At 200% portrait on API 26, Tab to "Change the times" on the overlap card leaves the
  button behind the navigation bar. Only its top 50 px show: focused bounds `[84,2224][996,2274]`, container to 2274.
  The page can scroll there by hand (`[227,2074][854,2169]` after swipes). Seen in 3 of 3 head walks and 2 of 2 develop
  walks.
- **#229 (S4).** On API 26, `DialogWaitTextFieldTest.aShortTapOnATextFieldDoesNotFocusAndTheNextDoes` fails on develop
  (2/2) and head (2/2). A real-finger repro was not done.

## Observations (not filed)

- `data-backup/open-and-export` cannot finish Android's save dialog on this API 26 image (it fails the same on develop).
  The export works by hand. QA follow-up: fix the flow.
- On the control, System UI crashed once on the emulator ("System UI has stopped", 11:55) right after 200% landscape was
  set. It did not recur on head. Platform, not the app.
- Harness slips, corrected and rerun:
  - I edited `hist.sh` while it ran, so its trailing restore did not run after the control's 8 cells. The cells
    themselves were whole, and I restored font, rotation and auto-rotate by hand.
  - A comma inside a walk pattern split a key list, so those cells were rerun.
  - Two manual adb reads (logcat, a dump) ran during the control's Home chain, after its setup had already failed on
    the System UI dialog. Those control cells were all rerun.

## What these rows do not prove

- Nothing on API 36, nothing with TalkBack, nothing with Gboard. The API 26 image has no TalkBack, and its keyboard is
  AOSP LatinIME with a hardware keyboard.
- The empty after-Settings group of 4.4 (a)/(b) never occurs on this device, so the D181 (4) fix itself was not
  exercised on a device. The device rows only show that case (c) did not regress. (a)/(b) rest on unit tests.
- The message walks were filmed with keys about 0.5 s apart, and a frame was read 0.3 s after each key. A ring that
  is drawn later than that was inferred, not seen (the first Tab to Settings in portrait).
- The dialog's "focus never selects" was checked only for the Down and Up arrows on "Kind". Space and Enter selecting
  were not walked.
- Dark theme only (the device's `ui_night_mode=1`); light theme not walked.
- History was checked in November 2026 with no older migraine. Previous month was disabled, so it was never a stop.
- The quick-link bounds at 360 dp come from `wm size 945x2100` on a 1080 px screen, not from a 360 dp device.

## Device state at the end (emulator-5582, 2026-11-16 12:41)

- App: `device-start.apk` reinstalled. Read back `aff4e8b8…f18c150`, the same as at the start of both runs.
- App data: restored from `appdata26-before.tar`; per-file SHA-256 equal to the start (9 files). The `cache` folder's
  group is again 10106 instead of 20106 (no setgid), as after the first run; it holds one 0-byte lock file.
- Download: the one file this run created (`migrely-2026-11-16.json`) was removed. The folder lists what it held at
  the start.
- Settings: the 11 keys match the start, `wm size` is reset (1080x2400), and `zen_mode` stays 3, as found.
- Powered off with `sync` and `reboot -p`; qemu exited. The QA lock folder is empty, `caffeinate` was stopped by PID,
  and the temporary develop worktree `migraine-app-qa-ctl` was removed.

## Device state after the first run (emulator-5582, 2026-11-16 02:30)

- App: `device-start.apk` reinstalled. Read back `aff4e8b8…f18c150`, the same as at the start.
- App data: restored from `appdata26-before.tar`. The per-file SHA-256 lists before and after are equal (9 files).
  One known deviation: the `cache` folder was recreated, so its group is 10106 instead of 20106 and it lost setgid.
  It holds one 0-byte lock file. Fixing that needs root; a clean install recreates it.
- Settings: the 11 keys in `settings.sh` match the start. `zen_mode` stays 3, as found (it was already 3 before this
  run, a leftover from an earlier session).
- Powered off with `sync` and `reboot -p`; qemu exited. The QA lock folder is empty.
