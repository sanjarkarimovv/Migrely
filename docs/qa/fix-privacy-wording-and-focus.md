# QA record: `fix-privacy-wording-and-focus` (T66, BRD D181, Refs #217), tasks 7.1 and 8.5

## Decision

**No verdict yet (2026-11-16 02:30). Not a GO.** 8.5 is open for head f846c899.

- Done: the 7.1 and review F1 flow fixes, each with a negative control (below). Part of an API 26 regression on head
  ran: 45 of 57 flows finished, 42 passed and 3 failed. None of the 3 is judged yet.
- Stopped: from about 22:00 the host ran on battery with the lid closed (`AppleClamshellState = Yes`, pmset "DarkWake
  from Deep Idle … Using BATT"). The Mac slept between short dark wakes, so flows took 10 to 37 min each. Any timeout
  from that run is void. I stopped the chain at 02:19 by exact PIDs, restored the device and powered it off.
- Not run yet (all phase 1 device rows): 4.1 History proof (head and develop), 4.3, 4.4–4.5 Home, 4.7 QuietLinks
  bounds, 5.5 three screens at 200% (with F2 round 2's second Save), 2.x privacy text on screen, 8.2 instrumented on
  API 26. Phase 2 (API 36, TalkBack, T63's deferred rows) is blocked: emulator-5560 was up and swap free was 1.2 GB
  against the 2 GB bar.

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

## API 26 regression on head f846c899 (partial, host asleep)

`QA_SERIAL=emulator-5582 qa/maestro.sh`, started 21:18 and stopped at 02:19. Pass 1 has 46 flows, pass 2 (a11y-timeout)
11, and 2 geometry flows are skipped below API 29. Report folder: `build/qa/maestro/20261115-211822`; log:
`migrely-work/qa-t66/out/reg-head.out`.

- Finished 45, passed 42. Among them: `list-opens-details` (smoke, changed), `track-periods-off-marks` (unchanged),
  and the users of `wait-message-gone` that ran (`cold-start-during-attack`, `move-start-rejected`,
  `several-attacks-storm` and the `log-attack-today` users in attack-history, day-markers, doctor-visit, month-sky and
  month-stats/summary).
- Failed 3, **not judged**:
  - `data-backup/open-and-export.yaml`: "Export for Excel (CSV)" not found by a plain `assertVisible`. Possible cause:
    the longer Your data note (D181 (1)) pushes the row below the fold on API 26's shorter screen. That would be a
    flow problem if the row can be scrolled to. Unproven.
  - `month-stats/medication-days.yaml` and `medication-overuse/history-medication-only.yaml`: "Days with attack
    medication: 0 / 1" not found by `scrollUntilVisible`. T66 changes no History code. The first one ran 10 min 55 s
    in a sleep window.
  - `open-and-export` and `medication-days` go through `log-attack-today`, so the changed wait is not ruled out yet.
- Not reached: the rest of pass 1, all of pass 2 (`undo-window-passes` included), and the two changed a11y-tour flows
  as part of the chain. `settings-privacy` passed by itself on head (N8 shows the purchase line; the flow's first step
  is red on develop), but it has not had a full green run on head.

## Next (to resume)

Run with the Mac awake and on power. Scripts are in `migrely-work/qa-t66/`.

1. Rerun the 3 failures twice on head and once on develop. If `open-and-export` fails only on head and the row is below
   the fold, change the flow to `scrollUntilVisible`.
2. Finish the regression: pass 1's remaining flows, pass 2, and both a11y-tour flows.
3. Run `hist.sh head` and `hist.sh ctl` (the 4.1 History verdict), `home_msg.sh` (4.4–4.5), the 4.7 bounds, and 5.5
   with the second Save (an empty name for the instant result, a duplicate name for the database path, touch Save).
   InvalidDose cannot be reached from the UI (the field stops at 20).
4. Run 8.2 on 5582 (the test APKs are already built).
5. Phase 2 when 5560 is off and swap free is above 2 GB.

## Device state at the end (emulator-5582)

- App: `device-start.apk` reinstalled. Read back `aff4e8b8…f18c150`, the same as at the start.
- App data: restored from `appdata26-before.tar`. The per-file SHA-256 lists before and after are equal (9 files).
  One known deviation: the `cache` folder was recreated, so its group is 10106 instead of 20106 and it lost setgid.
  It holds one 0-byte lock file. Fixing that needs root; a clean install recreates it.
- Settings: the 11 keys in `settings.sh` match the start. `zen_mode` stays 3, as found (it was already 3 before this
  run, a leftover from an earlier session).
- Powered off with `sync` and `reboot -p`; qemu exited. The QA lock folder is empty.
