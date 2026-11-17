# QA record: fix-message-timing-and-size (T64, BRD D182, EC-UX-52…60, EC-ATK-24)

## Verdict

**QA (qa-t64), 2026-11-17 12:10: GO with risks for the merge of `feature/fix-message-timing-and-size` @
58e620729a618b88cfb32fa1e787828501f5c641 into `develop`.**

- Design "Acceptance" A1 to A6 pass on the branch, and each new check fails or differs on the negative control `v1.3.0`
  (c9776b5c; QA's 1.3.0 debug APK from 2f7745c3, whose tree differs from the tag only in `docs/release/whats-new.md`).
- No defect was found, so no issue was filed.
- Two rows need a person with TalkBack on a real phone. They are written below as **PENDING-PERSON** with steps.
  The emulator already answered most of both questions; the person row confirms them on real hardware.
- Risks that stay open are listed at the end. None is S1 or S2.

## Builds and devices

| Build | Source | sha256 (APK) | On device |
|---|---|---|---|
| branch | `migraine-app-qa-t64` detached at 58e62072, clean tree, `:app:assembleDebug` | `3888b050…3a68f` | `3888b05071163891` in every branch row |
| control | `v1.3.0` code (2f7745c3 debug, QA 1.3.0 build) | `782cf3d3…0bd0199` | `782cf3d3e80efc1a` in every control row |

- `emulator-5580` `Migrely_QA_API_36` (Android 16, density 2.625, TalkBack 16.0.0), then `emulator-5582`
  `Migrely_QA_API_26` (Android 8.0). One at a time; both are off at the end.
- API 36 rows set "Time to take action" (`accessibility_interactive_ui_timeout_ms`) as stated per row. API 26 has no such
  setting, so there an Undo lasts its base 5 s and "Saved." 8 s.
- Every row is a fresh install (`pm uninstall`, install, hash checked), onboarding skipped, launched with `am start`.
- Evidence: `migrely-work/qa-t64/` (`rows.jsonl` one line per row with the device APK hash, `logs/`, `shots/`, `raw/`,
  scripts in `bin/`). Harness voids are in `logs/void/`.

## Results

### A1, "Saved." follows "Time to take action" (B1, EC-UX-52), API 36, setting 30 s

"Saved." only replaces an error (D178 (1)), so each row fills `/data`, makes a write fail with the storage error, frees
the disk and repeats the write. Time is from the tap that saved to the first frame without the message.

| Screen | Branch | Control `v1.3.0` |
|---|---|---|
| Home (Period today) | **30.8 s** | 8.9 s |
| History (day mark Period) | **31.4 s** | 8.9 s |
| Reminders (Ask if a medication helped) | **30.8 s** | 30.9 s (it already used the interactive timeout) |

Pass: 3/3. The control shows the old split on Home and History, and that Reminders was already right.

### A2, keyboard pause (B2 P1, P2, P5, P6; EC-UX-53, 55, 56)

Hardware Tab and Enter by `input keyevent`. Focus is read with `uiautomator`. While the message runs, presence is read
from raw frames: the control's label colour inside the control's box. A point probe gave false "present" on the
attack-mode rain card, so its 3 rows are void (`logs/void/`).

API 36, setting 20 s; Home in attack mode at 200%, History and Reminders at 100%:

| Row | Branch | Control `v1.3.0` |
|---|---|---|
| P1 Home, Undo the start focused at 1.1 s, held | still shown and focused at 33.5 s and 32.8 s; gone 20.7 s and 20.1 s after Tab away (19 s were left) — **2/2** | gone by 20 s, not shown at 30.8 s |
| P1 History, Undo the deletion (4 Tabs) | shown at 32.5 s, gone 20.3 s after leaving — **1/1** | gone by 20 s, not shown at 30.5 s |
| P1 Reminders, "Saved." Dismiss | shown at 32.6 s, gone 20.1 s after leaving — **1/1** | not run |
| P2 Home, focused with 0.4 s left, held 9 s | last frame with it 4.1 s, first without it 4.9 s after leaving | gone before focus could land (Undo not in the tree 2.5 s later) |
| P2 History, focused with 0.2 s left | last 4.1 s, gone 4.6 s after leaving | not run |
| P5 Home and Reminders, focus held, shade open 23 s | gone when the shade closed (not in the tree) — **2/2** | not run |
| P6 Home, Enter on the focused Undo | "Undone." replaces it with no focus, and lasts 21.0 s (its full 20 s) — **1/1** | not run |
| P6 Home, a touch on "Migraine ended" while Undo is focused | the new "Migraine ended" lasts 20.8 s (full time) — **1/1** | not run |

API 26, base timeouts (Undo 5 s, "Saved." 8 s):

| Row | Branch | Control `v1.3.0` |
|---|---|---|
| P1 Home attack 200%, Undo the start | held to 17.2 s, gone 5.8 s after leaving — **1/1** | not shown at 16.3 s |
| P1 History, Undo the deletion | held to 16.8 s, gone 5.6 s after leaving — **1/1** | not shown at 15.3 s |
| P1 Reminders, "Saved." Dismiss (storage error first) | held to 19.3 s, gone 7.8 s after leaving — **1/1** | not run |
| P1 Reminders, "Visit removed", Undo removing the visit | held to 15.4 s, gone 5.4 s after leaving — **1/1** | not shown at 16.0 s |
| P2 Home, focused with 0.4 s left | last 3.9 s, gone 4.9 s after leaving | not run |
| P2 History, focused with 0.2 s left | last 3.7 s, gone 4.5 s after leaving | not run |
| P5 Home, shade 8 s | gone when the shade closed — **1/1** | not run |
| P6 Home, Enter on Undo | "Undone." 9.3 s (its 8 s), unfocused — **1/1** | not run |

**P2 judgement, stated openly:** the bound fixed before the run was "gone 3.0 to 4.5 s after leaving". Three of four P2
rows ended 4.6 to 4.9 s. That is the harness, not the app: the P1 rows on the same harness end 0.9 to 1.5 s after the
time the app should leave (frame capture interval plus the exit fade). With that offset, 3.3 s (floor plus grace) reads as
4.2 to 4.8 s. The message was still on screen 3.7 to 4.1 s after focus left with only 0.2 to 0.4 s of its own time left,
which only the 3 s floor explains. P2 passes 4/4 on that reading; the raw numbers are in `rows.jsonl`.

### A3, TalkBack pause (B2 P4, EC-UX-54), API 36

- **7.2's instrumented test rerun by QA**, `MessageTalkBackPauseTest`, TalkBack on and touch exploration on:
  - run 1: exit 0, **10/10** rounds (paused on text, paused through the move to Undo, resumed after the grace with
    2994–3802 ms left); TalkBack had already focused the new message itself in 5 of 10 rounds;
  - **negative control:** `onChanged = { currentCountdown?.screenReaderFocus = it }` → `onChanged = { }` in
    `MigrelySnackbarHost`: exit 1, **0/10** (no round paused; every message ran out). Reverted; `git diff` empty;
  - run 2 after the revert: exit 0, **10/10** (5 of 10 self-focused).
- **Real TalkBack in the real app** (Home attack mode, setting 20 s; touches through the emulator console, which TalkBack
  hears; `input tap` does not reach it):
  - focus on the message text at 2.3 s: still shown at 26 s; a right swipe moved to "Undo the start. Button": still
    shown 3 s later; a touch on "Mild" (focus leaves): gone 19.6 s later (its time left, above the floor) — 1/1;
  - control: gone before 26 s, with the same touches.
- **TalkBack menu (the manual question), answered on the emulator:** the menu gesture (swipe down then right) opened
  "TalkBack menu", and TalkBack's window took window focus (`mCurrentFocus` = TalkBack) in 3/3 runs. With the menu open
  25 s, the message was gone when the menu closed: **the countdown runs while the menu is open**, as design D3 says for
  window focus loss. With the menu open 3 s, the message was still there after it closed, and TalkBack's focus held it
  again (still shown at 60 s).

### A4, Undo ceiling (EC-ATK-24, D182 (3)), API 36, setting 120 s

Lock at about 30 s, unlock at 121.6 s after the deletion, tap where Undo was. The offer must come late (about 25 s) for
the old build to show the problem, because Details closes at once after a delete:
- Home: delete on Details opened from History, Back to Home at 20 s; Home offered the Undo at 24.5–25.3 s.
- History: delete on Details, the app to the background at once, back at 20 s; History offered it at 24.7–24.8 s.

| Row | Branch | Control `v1.3.0` |
|---|---|---|
| Home | no Undo at unlock (frame and tree), the tap does nothing, the migraine stays deleted — **PASS 1/1** | Undo shown at unlock; the tap gives "This can no longer be undone."; the row is already purged — **reproduces** |
| History, late offer | no Undo at unlock, nothing after the tap — **PASS 1/1** | Undo shown; "This can no longer be undone." — **reproduces** |
| History, offer at 2 s, unlock at 117.7 s | no Undo (the ceiling at 115 s ended it) — **PASS 1/1** | Undo still shown at 117.7 s (frame) |

- Void: one History run (no late offer, because History never offers a deletion made from Home; by design D31 and
  EC-HIS-04). Two runs overlapped with a second chain (QA harness error) and are void.
- The ceiling also showed under TalkBack (A3/EC-UX-60 row below): a self-focused Undo left at 116.9 s.

### A5, 64 dp in the dark room (B3, EC-UX-59), API 36, `uiautomator` bounds at 2.625

| Cell | Branch | Control `v1.3.0` |
|---|---|---|
| Home attack, portrait 100% | Undo 168×168 | 152×126 |
| Home attack, portrait 200% | Undo 230×168 | 230×126 |
| Home attack, landscape 100% (`cur=2400x1080`) | Undo 168×168 | not run |
| Home attack, landscape 200% | Undo 230×168 | not run |
| Log medication from attack mode, storage full | Dismiss button 168×168, its icon 53×53 (20 dp, unchanged) | 126×126 |
| Home Today (light), "Migraine deleted" | Undo 152×126 (48 dp) | not run |
| History, "Migraine deleted" | Undo 152×126 | not run |

- Pass: 7/7 on the branch. The control fails the 168 px check in all 3 cells measured.
- Screenshot at 200% portrait attack (`shots/a5-branch-attack-2.0-P.png`): the message is whole; the rain card's text
  and "Details" are uncovered; "Migraine ended" is untouched.

### A6, nothing new is spoken (EC-UX-59)

- TalkBack speech (VERBOSE log) for the same touches on the branch and on `v1.3.0` is the same for the message:
  "Migraine started. Undo the start. Button", "Migraine started", "Undo the start. Button". No other word about the
  message, and no "paused", on either build.
- Not done: a full accessibility-tree diff. The labels on the message's nodes are the same on both builds in the A5
  dumps.

### EC-UX-60, TalkBack focuses a new message by itself (emulator observation)

- On Home, "Migraine started" activated by a TalkBack double tap: the button leaves, and TalkBack moved its focus to the
  message's Undo by itself ("Migraine started. Undo the start. Button" spoken, no touch).
- Then nothing was touched. Branch: the message stayed 75 s and more in run 1, and left at **116.9 s** (the 115 s
  ceiling) in run 2. Control: gone at 22 s.
- What this proves: on this emulator, on Home in attack mode, TalkBack's own focus holds a new Undo until the ceiling,
  as EC-UX-60 accepts. It does not prove the same on History, dark-room Log medication, for "Saved." or on a real phone.

### Static checks

- All 10 host files call `MigrelySnackbarHost(..., countdown = ...)`; `calculateRecommendedTimeoutMillis` is called only
  in `MessageTimeout.kt` in main code (`grep`, 58e62072).
- The gate (8.1) was not rerun by QA. The engineer's run is on 0be03fda, which already holds the review fixes
  (c6a3de7a, ac573e81); 58e62072 changes only `tasks.md`. QA's own build of 58e62072 (`:app:assembleDebug
  :designsystem:assembleDebugAndroidTest`) exited 0.

## PENDING-PERSON rows (real phone with TalkBack, about 10 minutes)

The person needs an Android phone with Migrely from this branch installed and TalkBack on. Before starting, open the
phone's Settings, Accessibility, "Time to take action" (or "Timing controls"), and set **1 minute**. Restore it after.

**Row 1, the TalkBack menu (2 minutes).**
1. Open Migrely. On Home, touch "Migraine started" once, then double tap. A message "Migraine started" appears at the top.
2. Touch the words "Migraine started" in that message once. TalkBack reads it.
3. Open the TalkBack menu: swipe down then right in one stroke (or tap with three fingers).
4. Leave the menu open and count slowly to 70. Then close the menu (Back).
5. Write down: is the message still there? Expected: **gone**, because the menu takes the screen and the time runs.
6. Undo the migraine (end it, or delete it from Details) so the next row starts clean.

**Row 2, does a new message take TalkBack focus by itself (EC-UX-60), 6 minutes.**
For each screen below, do not touch the screen after the step that makes the message. Watch a clock.
1. **Home:** touch "Migraine started", double tap. Write down what TalkBack reads next, without your touch.
   Wait. Write down when the message leaves. Expected: if TalkBack read "Undo the start" by itself, it stays up to about
   2 minutes (never longer than 2 min after the start); if not, it leaves after about 1 minute.
2. **History:** open a migraine from History, choose Delete, double tap. Back on History, a message "Migraine deleted"
   appears. Same notes: what was read without your touch, and when the message left.
3. **Log medication in the dark:** start a migraine, then "Medication taken", choose or add a medication and log it.
   A message appears. Same notes.
4. On any screen where the message stayed, swipe right twice to move TalkBack away from it. Expected: it leaves within
   a few seconds, at least 3 seconds after you moved away.

Report: for each screen, "read by itself: yes/no", "left after: N seconds", and "after moving away: N seconds".

## What these checks do not prove

- **Real phones and other TalkBack versions:** every TalkBack row ran on one emulator (TalkBack 16). Self-focus and the
  menu taking window focus may differ on an OEM build.
- **API 26 TalkBack:** not checked (no TalkBack on the image, D182 known limit). Keyboard rows did run on API 26.
- **EC-UX-60 beyond Home:** History, dark-room Log medication and "Saved." under self-focus were not observed.
- **Not run in this check, within the diff's reach, covered only by the engineer's unit and Robolectric tests:**
  rotation and process death while paused (EC-UX-57), the clock set back or forward (EC-UX-58), the setting changed
  while a message shows, reduce motion, right-to-left, landscape keyboard pause, TalkBack turned off while paused,
  Switch Access, and the 48 to 64 dp switch within one frame.
- **Timing precision:** frames are read every 0.1 to 0.5 s and the exit fade adds time, so leave times carry about
  +1 s; the 300 ms grace itself is not resolved on device (only by the instrumented test, 13–33 ms after leaving).
- **Deep sleep:** the emulator does not deep-sleep. The lock rows show `onResume` and window focus re-evaluation, not
  what `delay` counts on a sleeping phone.
- **Feel:** whether 3 s and 300 ms feel right and whether 64 dp feels heavy needs a person (ux §D).
- Sizes are proved by bounds at density 2.625 only.

## Residual risks

- **TalkBack users may see a message stay longer than they expect.** When TalkBack puts its focus on a new Undo, it stays
  up to 115 s; a "Saved." or information message so focused stays until focus moves. Accepted in EC-UX-60; the ceiling
  bounds every Undo (seen: 116.9 s).
- **The TalkBack menu lets the countdown run.** A person who opens the menu on a message to have it read again can lose
  the message after its time left (at least 3 s). By design (window focus loss); the person row confirms it on a phone.
- **Possible false hold (code reading, not tested):** the TalkBack probe counts any focused node whose bounds lie inside
  the message's box. A content node fully covered by the message (for example a short title under it) would pause the
  countdown while TalkBack is on it. Effect: a message stays while TalkBack sits on a hidden node; the Undo ceiling still
  applies. Not seen on the screens walked.
- Settings, Onboarding and Paywall still drop their message on rotation (#236, pre-existing, out of scope).

## Harness notes for the next run

- Leaving the app in Home attack mode keeps the rain animation running: the API 36 emulator used about 6.5 host cores
  until the app was stopped. Stop the app at the end of each row.
- Gboard's first-run stylus tutorial on the API 36 image swallows `input text`; dismiss it ("Cancel") or restore with
  `pm clear com.google.android.inputmethod.latin` (done at the end of this run).
- The API 26 image booted with `font_scale` 2.0 and `show_ime_with_hard_keyboard` 1 from an earlier session; QA left
  them as found.

Settings: API 36 and API 26 compared with the boot snapshots, identical at the end (`raw/settings-*.json`); TalkBack
off, TalkBack log level back to ERROR, both emulators off.
