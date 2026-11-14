# QA record: `fix-messages-everywhere` (T63, BRD D178), task 10.1

## Decision

**QA's verdict, 2026-11-14: GO for the merge of `bugfix/181-messages-everywhere` at 1000c181 into `develop`, with the open risks below.** This is a change verdict, not a release verdict**

- **The candidate.** Branch `bugfix/181-messages-everywhere` at **1000c181** (code head 3212406d; 1000c181 changes `tasks.md` only).
  The control is `develop` **52c12ce5**.
- **No S1 or S2 was found.** No issue was filed. Two first suspicions died on a rerun: Undo missing from the Tab order on API 26 (the message had timed out before the walk) and "Saved." missing in landscape (the driver's tap landed on the message).
- **Safety and data paths hold.**
  - "Saved." replaces the storage error after a later save. The mark is stored, and a newer failure replaces "Saved." and
    stays: Home 5 of 5 on debug, 2 of 2 with "Remove animations", 1 of 1 on R8. On `develop` the error stays after the save,
    2 of 2 (N1, #181).
  - A tap in the strip that a shorter replacement uncovers reaches nothing within 0.8 s: 8 of 8 on debug and R8 at 341–679 ms.
    The same tap opens the picker under it at about 2.56 s (3 of 3), and on the N8 mutant at 300–679 ms (4 of 4).
  - A failed retry keeps the error and is spoken again with its text: Home, History and Reminders, 18 of 18 valid runs.
    No empty live-region announcement occurred in 66 head events (60 debug, 6 R8). On `develop`, History's same-text retry gave an empty
    live-region announcement 2 of 2 times, and once nothing at all was spoken (N6, #183 reproduced).
- **What was not proven** is listed at the end. In short: 8 of the 13 hosts were not run on the device (Paywall, Doctor report,
  Your data, My medications, Edit medication, Log medication, Add past migraine, Onboarding). Also not run: Switch Access,
  TalkBack swipe order, 60 fps frame captures, and right to left, which this English-only app does not mirror.

## Candidate, builds and device

| APK | Source | sha256 (first 16) | On device |
|---|---|---|---|
| `debug-1000c181` | head | e702b723a620d572 | read back with `sha256sum` of `pm path`: equal |
| `r8-1000c181` | head, `assembleRelease` (R8, signed with the debug key: no `keystore.properties` in the QA worktree, D102) | caf86e756ef443a5 | equal |
| `debug-52c12ce5` | `develop`, the control | 5dae79a35ab0b9c4 | equal |
| `debug-n4` | head with one line changed: History's `ScreenLeaveObserver` clear replaced with `LifecycleEventEffect(ON_STOP)` | 02e63cf077c04845 | equal |
| `debug-n8` | head with one line changed: `ReplacementGuard.target` sets `replacedHeight = 0.dp` (the guard follows only the new message) | 9c74bd688dab3486 | equal |

- **Builds:** `migrely-work/qa-msgs/build.sh` in the detached worktree `migraine-app-qa-msgs`,
  `nice -n 19 ./gradlew --max-workers=2 -q :app:assembleDebug :app:assembleRelease`, exit 0 for each build. Each mutant was
  applied, built and reverted. The tree was clean afterwards (0 lines in `git status --porcelain`). The mutant diffs are in
  `logs/n4.diff` and `logs/n8.diff`. Each mutant changed exactly one dex file compared with the head APK.
- **Gate:** `android-engineer` ran it on 3212406d (exit 0, 3176 tests). QA did not rerun it.
- **Device:** `Migrely_QA_API_36` (emulator-5580, 1080x2400, 420 dpi, TalkBack with VERBOSE log level, hardware keyboard through
  `adb input`). It was cold-booted at 16:08. `Migrely_QA_API_26` (emulator-5582, Android 8.0) ran O20 after 5580 was shut down. On API 26 the shell user cannot fill the last ~475 MB of `/data`, so the rest was filled as the app through `run-as` (debug only) and removed afterwards.
- **Full disk** means `/data` filled with `fallocate` to under 8 KB free (64 KB in the first runs). It was filled again before each
  retry, because a failed write can hand back a few pages (see "Void rows").
- **Evidence:** `migrely-work/qa-msgs/` beside the repo. It holds the drivers (`m.py` and one script per row), the chains
  (`chain-*.sh`), raw rows (`logs/*.jsonl`), TalkBack logs (`logs/tb-*.txt`) and screenshots (`shots/`).
- **Host load:** the host's 1-minute load reached 107 from 16:40 to 18:00 (`mediaanalysisd`). Timing rows from that period
  (the first guard run) were repeated at loads from 4 to 23, with the same results. Each row records the load when it ran.

## Rows

"Valid" leaves out the void rows explained below. ux §3 row ids are in brackets.

### Safety and data paths

| Check | Head debug | R8 | Control |
|---|---|---|---|
| A1 Home Today: fail a period mark, free space, mark again. Then a newer failure [S1] | ✅ 5/5: the error shows, the chip stays unmarked, "Saved." at the first read (≈2.1 s, one uiautomator dump), the DB has the mark, then a newer failure shows the error and it stays after 3 s | ✅ 1/1 (no DB read on R8) | N1 `develop`: the error stays after the successful save, 2/2 ✅ (the check can fail) |
| A10 / S4 / N5: a normal save with no error | ✅ no message, 5/5 (+2 with "Remove animations", +1 R8) | | |
| A19 "Remove animations": A1 again | ✅ 2/2 | | Only the outcome was checked, not "at once" |
| A7 / A16 / M1 / M7 TalkBack: fail after quiet → the same failure again → space freed and saved, by double tap on the control | ✅ Home 10/10, Reminders (daily reminder switch) 5/5, History (pain-free switch) 3/3 valid. Each run: the error spoken with its text, then spoken again, then "Saved." spoken. 0 empty live-region events in 60 events | ✅ Home 2/2 | N6 `develop` History: the same-text retry gave `ttsOutput= {}` with `nodeLiveRegion=2` 2/2 times, and in 1 of 2 the retry spoke nothing (#183 and A7 reproduced) |
| M3 / A17 guard / F2: Attack details at 200%, no card: error → "Saved." (shorter); tap at (540, 527) between the two bottoms, over "Started" | ✅ swallowed 6/6 at 341–679 ms (2 of them at host load 37–49, 4 at 11–23); opens the picker at 2564–2566 ms 2/2 | ✅ swallowed 2/2 (363, 641 ms); opens at 2.5 s 1/1 | N8: opens the picker 4/4 at 348–679 ms ✅ (the check can fail) |
| A8 History: error, then HOME → Android Settings → launcher | ✅ the error stays 2/2 | | N4 (lifecycle ON_STOP clear): the error is gone 2/2 ✅ (A8 tells them apart) |
| A8 History: error, Insights, Back | ✅ the error is gone 2/2 | | N4: gone 2/2 (unchanged by the mutant, as expected) |
| A9 History: error, rotate to landscape and back | ✅ stays in landscape and after 2/2 | | |

### Review request (A11, R1–R5), debug through `review_request_count`

| Check | Head | Control |
|---|---|---|
| R5 due, no message | ✅ count 1 at 6 s, 3/3 (1 void: prefs write race in the first driver) | `develop` 1/1 |
| R1 error on Home Today before the 2 s settle | ✅ count 0 at 10 s with the error still shown, 2/2 | N7 `develop`: count 1 at 10 s with the error shown, 2/2 ✅ (the check can fail). Head with the same driver: count 0, 2/2 (4/4 in total) |
| R2 error dismissed, stay 10 s | ✅ count 0, 2/2 | |
| R3 Settings and back, 6 s | ✅ count 1, 2/2 | |
| R4 "Migraine ended [Undo]" | ✅ 2/2 by the count: 0 at 10.4 s and 10.8 s after the end (the message lasts 8 s, so without the gate the count would be 1 by 2 s), then 1 by 12.4–12.9 s. The message was read on screen in 1 of the 2 runs; in the other the first dump came after it left (load 20) | not run (see void rows) |

### Keyboard order (A12, O-rows), from nothing focused, 25 Tab then 8 Shift+Tab (or the stated counts)

| Screen | Message | Head result | Control |
|---|---|---|---|
| Attack details 100% and 200% P [O16] | storage full | ✅ Back → Dismiss → Started → Ended → …; Shift+Tab: Started → Dismiss → Back → wraps to Delete migraine. R8: same | N2 `develop`: Back → Started → … with no Dismiss in the first 12 stops ✅ |
| History 100% and 200% P [O6] | storage full | ✅ Back → Insights → Doctor report → Dismiss → the chosen day → Period → … ; Shift+Tab reaches Dismiss → Doctor report → Insights → Back | `develop`: Dismiss after "Show all of November" (last) |
| Settings 100% P [O1] | timed error | ✅ Back → Dismiss → Premium row → Restore purchases → Reminders … | |
| Reminders 100% P [O5] | storage full | ✅ Back → Dismiss → Next visit → Daily reminder → Reminder time → …; Shift+Tab reverses | |
| Home Today 200% P [O11 revised] | storage full | ✅ Settings → Dismiss → Period today → …; Shift+Tab from Period today → Dismiss → Settings → wraps to Log medication. R8 100%: same | |
| Home Today 100% P [O11 revised] | "Migraine ended" | ✅ Settings → Undo → Period today → … | |
| Home attack mode 100% and 200% P [O14] | "Migraine started" | ✅ Undo → Details → Move the start … → Mild → Moderate → Severe → Medication taken → Migraine ended | |
| Home Today, no message [O19] | none | ✅ identical to `develop` (Settings → Period today → … → Log medication) | `develop` same |

The message stays on screen to the end of every walk (accessibility timeout 120 s during walks, restored). 25 Tab presses were
recorded on Attack details 200%, History 100% and 200%, Home Today 200%, Settings, Reminders, attack mode 100% and Home with no
message; the other walks were shorter (4 to 12 presses), aimed at the message and the reverse order.

### Home placement (A18, P1–P5), landscape

| Window, font | Message | Head result |
|---|---|---|
| 411 dp, 100% [P1] | error, "Saved.", "Migraine ended" | ✅ start column (x 297–1147 of 2400), 75–112 px below the Settings icon, overlap with Settings 0 and with "Migraine started" 0; Dismiss works by touch; the first tap on Settings while Undo shows opens Settings. R8: same |
| 411 dp, 200% [P1] | same | ✅ the same, overlap 0 and 0; a first tap on Settings opens it (debug and R8) |
| 411 dp, 100%, `develop` [N3] | "Migraine ended" | ✅ control: the message spans x 656–1988 and Settings is not in the tree; a tap at Settings' place does not open Settings |
| 274 dp (`wm density 631`), 100% [P2] | "Migraine ended" | ✅ start column, overlap with Settings 0 and with "Migraine started" 0, a first tap on Settings opens it |
| 274 dp, 100% [P2] | storage full (from a failed start) | ✅ whole on screen (y 395–735 of 1080), Dismiss works by touch, overlap with "Migraine started" 0 |
| 274 dp, 200% [P3] | "Migraine ended" | ✅ the date on 2 lines, the message at y 593–878 below the Settings row, overlap 0 and 0, a first tap on Settings opens it |
| 274 dp, 200% [P4] | storage full | ✅ the fit rule raised it (y 275–899), whole on screen, Dismiss works by touch, "Migraine started" not covered. Settings is mostly covered in this cell, which ux P4 accepts (screenshot `p274-debug-1000c181-2.0-error.png`) |
| 274 dp, scrolled column [P5] | storage full | ✅ the message stays at its top while the column scrolls (screenshot `place-debug-1000c181-274-1.0-ltr-error.png`) |
| 411 dp, 100%, `iw` locale | all three | bounds identical to left to right: the app does not mirror, so right to left is not proven |

### Bounds against `develop` (A15, P7, spot check)

The storage error's text and Dismiss bounds, portrait, debug (`bounds.py`):

| Screen | 100%: head vs `develop` | 200%: head vs `develop` |
|---|---|---|
| History, Attack details, Reminders | equal: text 169,328–901,433, Dismiss 938,354–991,407 | equal: text 169,328–901,517, Dismiss 938,396–991,449 |
| Settings | equal: text 169,341–686,388 | equal |
| Home Today | head text 169,328–901,433; `develop` not read (dump failed) | head 27 px lower (y 355 vs 328): the measured Settings-row offset in portrait (ux F6), the accepted exception |

The other 8 hosts were not compared.

## Void rows, not counted

- **Driver faults, fixed and rerun:** the first A1 run (the message pattern matched "nothing logged"), the first TalkBack run (a logcat
  tag filter that returns nothing), the first two review drivers (the error tap came before Home was ready; a prefs write raced the
  app's own), and the first landscape placement runs (Home's "Migraine started" button was taken for the message).
- **History TalkBack, 2 runs:** the disk had 20 KB free at the retry, so the retry saved and "Saved." was spoken, which is correct.
  Fill was then tightened to under 8 KB.
- **Review on `develop`, the first N7 runs:** the request fired while the disk was still full, so the count write failed, and
  right after it `migrely_preferences.xml` was missing (`No such file or directory`). Android keeps a `.bak` copy after a failed
  write and restores it on the next load; that was not checked further, and it is not this change. The driver now frees the disk
  0.5 s after the failed mark, before the 2 s settle, and the control then works (N7 above).
- **R4, one run per driver version:** the first driver read the count while its prefs write raced the app; in one later run the
  first uiautomator read came after the 8 s message had left (load 20). Both are counted by the count timing in the table.
- **API 26, first walks:** the disk was not full (see Device), so no error showed, and a walk with Undo started after the 8 s
  message had left. Both were rerun as described.

## Observations, not filed

- **"Saved." on Home is stretched by the non-interactive accessibility timeout, not the interactive one.** `HomeMessages` passes
  `containsControls = undoTarget != null`, so "Saved." (which has only Dismiss) leaves after about 8 s even with "Time to take
  action" at 120 s. This matches the design ("READING_MILLIS stretched by the accessibility timeout") and older Home messages.
- **History, Shift+Tab from "Period" in the chosen day's section** goes to a stop that uiautomator reports with no focused node,
  then to Dismiss. Forward Tab reports the chosen day cell there. The calendar is not in this diff, and it was not compared on `develop`.
- **Right to left:** `cmd locale set-app-locales com.migrely.app --locales iw` gives bounds identical to left to right, so the app
  does not mirror (as in the D165 record). EC 17 is not proven.

## What these checks do not prove

- **8 of 13 hosts were not run on the device:** Paywall [O2], Doctor report [A4, O3, S3], Your data [A5, O4], My medications
  [A5, O7], Edit medication [A6, O8], Log medication [A5, O9], Add past migraine [A6, O18] and Onboarding [O10]. For these,
  only the engineer's unit tests and the reviewers' reading stand. The order layout and the host are shared, which lowers the risk
  but does not remove it.
- **Not run:** S2 (Reminders visit date with a permission request), A13 (TalkBack swipe order), A14 / F-h (Switch Access),
  F-a to F-g focus states (only what the walks show), M2, M4 to M6, M8 to M12 (no 60 fps capture, so no "no two messages in a
  frame" or brightness proof), O15 and O17 (landscape attack mode, card mode), P6 (360 dp portrait with a wrapped date: the
  engineer walked it).
- **"Saved." within 1 s** is not measured: a uiautomator read takes about 2 s. The frame-level timing is not proven.
- **Alpha never 0** is proven only through speech (no empty announcement), not by frames.
- **TalkBack** ran on API 36 only, with injected double taps, on an emulator. TalkBack's queueing on other versions, and a person
  hearing it, are not proven.
- **Emulator, not a phone:** no 3-button navigation bar in landscape, no real touch.
- **Right to left** is not proven (see above).
- **API 26** ran O20 only, at 130%: Tab from nothing focused on History (Back → Insights → Doctor report → Dismiss → the chosen day …) and Home Today (Settings → Dismiss → Period today …; with "Migraine ended": Settings → Undo → Period today, 2/2), and one save after an error ("Saved." and the mark stored). Shift+Tab cannot be sent on API 26 (`input keycombination` does not exist there), and TalkBack was not run on API 26.

## Retests on the issues

Comments with these results go on #181, #185, #183, #88 and #153. The issues stay open until the PR is merged into `develop`;
QA closes them after that.

| Issue | Result on 1000c181 | Control on `develop` 52c12ce5 |
|---|---|---|
| #181 error after a later save; review while it shows | ✅ A1 5/5 (+3), History and Reminders through TalkBack 8/8; R1–R3 4/4 | error stays 2/2; review fires 2/2 |
| #185 Dismiss last in the Tab order | ✅ Attack details, History, Settings, Reminders, Home: the message's control right after the bar (or after Settings on Home) | Attack details: no Dismiss in the first 12 stops; History: Dismiss last |
| #183 empty announcement | ✅ 0 in 66 events on Home, History and Reminders | History: 3 empty in 6 events, one silent retry |
| #88 Undo covers Settings | ✅ 411 dp at 100% and 200%, debug and R8: overlap 0, a first tap opens Settings | Settings hidden under the message, the tap does not open it |
| #153 (message part) Undo over "Migraine started" at 274 dp, 200% | ✅ overlap 0, first tap on Settings works; the button is not covered by Undo or by the error | not run on `develop` here (the issue's own numbers stand) |
