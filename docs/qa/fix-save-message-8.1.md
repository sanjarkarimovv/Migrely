# QA record: `fix-save-message` (PR #180, D165), task 8.1

## Decision

**QA's verdict, 2026-11-09 at 20:30: GO for PR #180 at febbfad3, with the risks below open.**

- **No S1 or S2 is open on febbfad3.**
  - #166 and #167 passed their retests and were closed by QA. The fix reaches `develop` only with PR #180.
  - One S3 was found here and is older than this change: #185, a message's Dismiss comes last in the Tab order.
- **#166:** in 17 of 17 valid placement rows, on debug and R8, a failed first 10 leaves the card's title, sentence and "Got it" in the tree, not
  covered and not clipped. On the base the title is missing (2 of 2).
- **TalkBack:** the card is spoken first and in full, then the message, in 15 of 15 runs. A failed retry is spoken again in 6 of 6
  runs (100%, 200%, scrolled).
- **#167:** a later save replaces "Couldn't save" with "Saved." in 9 of 9 runs. With TalkBack, the card is spoken before "Saved." in 4 of 4 valid runs.
- **Leaving the screen:** Back and a forward trip clear the error. Home, Android Settings, rotation, process death, and rotation or Back inside the
  hold behave as designed.
- **The review rounds found two defects; both are fixed and retested.**
  - 970de3a6: the hold flashed the message off and on. Fixed in 524709a1; seen on 524709a1 as a cross-fade, used here as the frame control.
  - 3f2ca3fb to 524709a1: a failed retry was silent under TalkBack in card mode, found by QA as TB12. Fixed in febbfad3.
- **Not proved here:**
  - RTL: neither `ar-XB` nor `iw` mirrors this English-only app;
  - live Switch Access;
  - a real phone;
  - the 9 other screens, which are split to the follow-up (D165 (7)).

## Candidate, scope and builds

- **Candidate:** `bugfix/166-save-message` **febbfad3** (febbfad3b8bde22e9c48e94a7ae5efa8dfda6b0f), merge base `develop` 6cc48123.
- **The review rounds, and the product code each one changed:**

| SHA | Product change | Rows run on it |
|---|---|---|
| 3f2ca3fb | first QA SHA: card mode, Saved, `ScreenLeaveObserver`, `clearOfMessage` moved into `:designsystem` | layout of a saved first 10 (no message), Home's moved modifier, TB12 control |
| 970de3a6 | message stored at once, shown after the card's announcement | none: a superseded SHA |
| 524709a1 | the old message stays on screen during the hold; the hold cannot stick | priority message rows, then superseded. Its recordings are the frame control |
| febbfad3 | `MigrelySnackbarHost(replaceInPlace)` in card mode, and `key(shown)` per message in the shared host | every message row below |

- `git diff 3f2ca3fb febbfad3 --stat` on product code touches only `AttackDetailsScreen.kt`, `AttackMessages.kt` and `MigrelySnackbar.kt`. The
  layout rows of a saved first 10 (no message) and Home's band row carry over from 3f2ca3fb. Home was rerun on febbfad3 anyway, because
  `key(shown)` is in the host that every screen shares.
- **Scope (owner rule, 2026-11-09):** only what the diff reaches: the Attack details message and card, Home's moved `clearOfMessage`, and the new
  `ScreenLeaveObserver`. No whole-app regression.
- **Gate:** `android-engineer` ran it on febbfad3 (exit 0), and `team-lead` checked it. QA did not rerun it.
- **Builds** (QA's, from the detached worktree `migraine-app-qa-t53b`; tree clean before and after each mutant):

| APK | SHA | md5 (first 12) | Note |
|---|---|---|---|
| `debug-febbfad3` | febbfad3 | 62eeceb2d92c | |
| `r8-febbfad3` | febbfad3 | 38307555c754 | minified, signed with the debug key (QA has no upload key, D102) |
| `debug-n5-febbfad3` | febbfad3 − one line | 0415602393b3 | N5: `.clearOfMessage(localState.messageClearance)` removed |
| `debug-n6-febbfad3` | febbfad3 − one line | ec27182c4807 | N6: `announcePolitely = cardMode` set to `false` |
| `debug-c2761cba` | develop c2761cba | 84610bddb02e | base for N1–N3 |
| `debug-nhome` | 3f2ca3fb − one line | 20dce65a3437 | Home's `.clearOfMessage(messageBottom)` removed (band control) |

- **Device:** `Migrely_QA_API_36` (emulator-5580, 1080x2400, 420 dpi, TalkBack 16.0, hardware keyboard). It was cold-booted before the febbfad3 run.
  Full disk means `/data` filled with `fallocate` until less than 64 KB was free. Speech comes from TalkBack's VERBOSE log, and geometry from uiautomator bounds.
- **Evidence root:** `migrely-work/qa-t53b/` beside the repo.
  - Raw rows: `logs/rows.jsonl`, one JSON line per row, counted by `table.py <build>`.
  - Drivers: `run53.py` and `lib53.py`; chains: `chain-febb*.sh`.
  - TalkBack logs: `logs/*-logcat.txt`; frames and recordings: `shots/`.

## The rows (febbfad3 unless stated)

"Valid" leaves out rows that are invalid for a stated reason (see "Invalid rows"). ux §6 numbers are in brackets.

### Placement, #166

| Row | Result | Valid rows |
|---|---|---|
| [1, 5, 6] Failed first 10, unscrolled and scrolled; then "Got it" | ✅ title, sentence and Got it in the tree; message and card overlap 0 px²; content starts below the message; title height equals the no-message frame (57 px at 100%, 205 px at 200%); after Got it the message moves 0 px and the content stays below it | 100% 4/4, 130% 2/2, 200% 2/2 |
| [8] Landscape | ✅ as above. At 200% the content area left under the message is 596 px (227 dp) | 100% 2/2, 200% 1/1 |
| [1] R8 | ✅ | 4/4 (100% ×2, 200%, landscape) |
| [23] Light theme | ✅ | 2/2 (100%, 200%) |
| Geometry at 100% | message y 328–433, content from 465, title from 539; unscrolled and scrolled identical (the #174 lesson) | |
| [3] Saved first 10 while "Pain level removed / Undo" shows | ✅ card not covered, Undo gives "Undone." | 1/1 |
| [4] Card scrolled away, remove a point | ✅ Undo visible and works; back at the top the card is not covered | 1/1 |
| [7] Got it, then Dismiss, then a tap at the message's place | ✅ taps at 289 ms and 598 ms open nothing; at 1318 ms the "Started" picker opens | 1/1 |
| [9] No card (a failed 5) | ✅ identical to the base: the message (328–433) lies over the first row (content from 296) | 1/1 and base 1/1 |
| [22] Remove animations | ✅ the longest run of moving frames is 2–3, against 20 with animations | 2/2 |
| Saved first 10, no message, on 3f2ca3fb (carried over) | ✅ card whole; scrolled: title 74 px under the viewport's top | 34/34 valid: portrait at 100%, 130% and 200%; landscape at 100% and 130%; Light at 100%; Remove animations at 100% and 200%. Landscape 200% is on febbfad3 (S 1/1) |

### TalkBack

| Row | Result | Valid rows |
|---|---|---|
| [2] Failed first 10 | ✅ title, sentence, Got it, then the full message, message once | 15/15 (TB:top 100% ×3, 130%, 200%, landscape 100% and 200%, R8 ×2, plus the first attempt of every TB12) |
| [N8] Saved first 10 | ✅ title spoken, no "Saved." | 1/1 |
| [12] Retry on a full disk (TB12) | ✅ card again, then "Couldn't save" again; the DB has no 10 | 100% 2/2, 200% 1/1, scrolled 100% 2/2, scrolled 200% 1/1 |
| Retry outside card mode (a 5, no card) | ✅ spoken again | 2/2 (base c2761cba: silent, 0/1) |
| [10] Fail, free the space, add the 10 itself again, card left showing (TB10) | ✅ card first, then "Saved." once | 1/1 |
| [10] The same after "Got it" (TB10g) | ✅ card first, then "Saved." once | 100% 2/2, 200% 1/1 (524709a1: 3/3) |
| Home "Migraine started" twice on a full disk | ✅ the second "Couldn't save" is spoken again | 2/2 (base 1/1) |

### Clearing, #167, and the leave rules

| Row | Result | Valid rows |
|---|---|---|
| [10] Fail, free, add the 10 again | ✅ "Saved." replaces the error, the DB has the 10, card not covered | 100% 4/4 (1 Light), 130% 1/1, 200% 1/1, landscape 2/2, R8 1/1 (UI only) |
| "Saved." leaves | 7.8–10.4 s after the tap (8 s plus the hold) | 524709a1 5/5 |
| [11] A note, a tag, an effect after a failure | ✅ "Saved.", the value in the DB, card still there | 1/1 each |
| [12] Retry on a full disk, on screen | ✅ the error stays, no "Saved.", no 10 | debug 2/2 (+1 Light), R8 1/1 |
| [12] Retry scrolled, frames | ✅ the message's box does not change in any frame after the dialog closes; the content does not move | 100% 1/1, 200% 1/1 |
| [13] Fail, then remove a point | ✅ "Pain level removed / Undo", no "Saved." | 1/1 |
| [14] Fail, Add medication, Back | ✅ no message, card still there | debug 1/1, R8 1/1 |
| [14] Fail, Back, reopen | ✅ no message | debug 1/1, R8 1/1 |
| [15] Fail, Home, Android Settings (storage), launcher | ✅ the error stays | 1/1 |
| [16] Fail, rotate and back | ✅ the error stays, card not covered | debug 2/2 (incl. landscape), R8 1/1 |
| [16] Fail, Home, process death, launcher | ✅ the same, new pid | debug 1/1, R8 1/1 |
| Rotation during the hold (tap and rotation in one shell command) | ✅ one error, card not covered, in both orientations | 2/2 |
| Back during the hold, then reopen | ✅ no error | 2/2 |
| [17] Normal save, no error (N7) | ✅ no "Saved." | 1/1 |
| Undo across a forward trip; Undo when a later write saves | ✅ Undo stays and works, no "Saved." | 1/1 each |
| Second failed 10 soon after the first, then a removal (no stuck hold) | ✅ one error, then the Undo shows | 524709a1 3/3 (13.5 s apart; a burst within the hold was not achieved, see below) |

### Accessibility, theme and Home

| Row | Result |
|---|---|
| [19] Switch Access | ⚠️ proxy only: actionable nodes in tree order, no overlapping bounds. Not run live |
| [20] Tab order | ❌ against ux §6: Back, Got it, then content. Dismiss is not among 25 Tab stops; Shift+Tab reaches it first. The same on the base: **#185**, S3, older than this change |
| [21] RTL | not proved: `ar-XB` (no pseudo-locales) and `iw` (only the clock turns to 24 h) do not mirror the English-only app |
| [23] Flash | ✅ no frame brighter than its neighbours by more than 8 grey levels in the C10, C12 and P recordings, Dark and Light |
| Home, landscape attack mode (`qa/device_scenarios.sh landscape_band`) | ✅ 3f2ca3fb (2 runs) and febbfad3: the guidance starts below the message and comes back, animated and reduced |
| Home, Maestro on febbfad3 | ✅ `in-app-messages` 4/4, `attack-mode` 2/2, `attack-details` 2/2 |
| Home portrait | no `clearOfMessage` call site (grep), so not reachable |
| Crashes | 0 Migrely crashes and 0 ANRs in `dumpsys dropbox` since the febbfad3 cold boot |

### Negative controls

| # | Build | Result |
|---|---|---|
| N1 | c2761cba, check 1 at 100% | ✅ fails as it should: the title is not in the tree, 2/2 |
| N2 | c2761cba, check 10 | ✅ fails: "Couldn't save" stays after the 10 is saved, 1/1 |
| N3 | c2761cba, check 14 | ✅ fails: the error is back after the forward trip, 1/1 |
| N5 | febbfad3 without the card-mode padding | ✅ fails: the title leaves the tree, 2/2 |
| N6 | febbfad3 without the polite override | recorded: the card is still spoken first, 3/3. On TalkBack 16 the 200 ms hold sets the order; the polite override adds nothing measurable |
| N7, N8 | febbfad3 | in the tables above |
| Check 2 | c2761cba with TalkBack | ✅ fails: the title is not spoken, or the message comes first, 2/2 |
| TB12 | 3f2ca3fb | ✅ fails: the retry is silent, 2/2 (the defect fixed in febbfad3) |
| Frames | 524709a1 recordings | ✅ the same analysis flags the cross-fade, 4 frames in each of 3 |
| Home band | `debug-nhome` | ✅ `landscape_band` fails |

### Invalid rows (not counted)

- **The write saved on a "full" disk** (SQLite reused WAL pages): 4 rows, C12 scrolled ×2, TB12 scrolled ×1 and TB10 ×1, all on febbfad3.
- **Driver errors:** TB10/TB10g ×4 (a parameter bug, rerun); C12fast ×2 (the burst's taps landed on the details screen). Also landscape 200% S rows on 3f2ca3fb (the row was not opened), rerun on febbfad3.
- **Screen covered:** TB:top at 130%. Its speech passed in full, but Android's "storage space running out" notice covered the screen when the geometry was read.
- **Superseded control:** PRESCTL (a Dismiss during a recording). After the frame rule changed to the settled reference it no longer serves; the 524709a1
  recordings are the control.
- **Superseded SHAs:** rows on 970de3a6 and 524709a1 are history, except where a table names them.

## What these checks do not prove

| Row | Passing it does not show |
|---|---|
| All | a real phone, a human hand, other screen sizes, TalkBack versions other than 16.0, or a slower device. The order rests on a 200 ms hold against Compose's 100 ms event batches |
| Placement | layouts other than 1080x2400; RTL |
| Frames | a flash shorter than about 150 ms: the recordings run at 4–7 fps |
| R8 | the DB: R8 has no `run-as`, so the 10 was checked on the UI |
| Hold | a second failed save inside the hold itself: the fastest UI path was 13.5 s apart |
| Host change (`key(shown)`) | screens other than Home and Attack details |
| Counts | each cell is 1–5 runs |

## Residual risks and follow-ups

- **#183 (S3):** TalkBack may announce a fading-in message as empty on other screens. febbfad3 fixes card mode on Attack details, and Home's
  identical retry was spoken. The other screens were not measured.
- **#185 (S3, older than this change):** a message's Dismiss comes last in the Tab order on Attack details.
- **The 9 other screens with a "Couldn't save" that never times out** still have #167's behaviour until the D165 (7) follow-up.
- **Full-disk test method:** with WAL reuse, a "full" disk sometimes lets a write through, so each row is checked against the DB.
