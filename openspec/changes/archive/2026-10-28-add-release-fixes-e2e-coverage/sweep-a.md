# Batch A exploratory sweep: build 4 (versionCode 4, R8 release APK)

Issues filed from section 3, all with "launch blocker: no" sent to the dev session on 2026-10-27:
- D1 is #78 (S2);
- D2 is #80 (S3);
- D3 is #81 (S3);
- D4 is #79 (S2, accessibility);
- D5 is #82 (S4);
- D6 is #83 (S4);
- D7 is #84 (S4, accessibility).

#78 and #81 stay provisional until sweep 2 checks a backup round-trip and the Doctor report with them.

- Device: emulator-5580 (Migrely_QA_API_36, pixel_6 1080x2400, 420 dpi), en-US, 12-hour clock, Asia/Tashkent. Run on 2026-10-27, 11:42–14:38.
- APK: `migraine-app-qa-rc/app/build/outputs/apk/release/app-release.apk`. `dumpsys` showed versionCode=4 and no DEBUGGABLE flag.
- How I drove it: Maestro flows (in `sweep/flows/`) plus adb and uiautomator through `sweep/pick.py` (date and time pickers by label; exact minutes by dragging the dial). I measured from screenshots and `screenrecord`. Logcat crash and error capture ran the whole time (`sweep/logs/crash.log`, `sweep/logs/errors.log`).
- The clock and time zone were never changed.
- **Crashes: none in `com.migrely.app`.** All 9 `FATAL EXCEPTION` entries come from the `uiautomator` dump tool itself (`IllegalArgumentException: Illegal character (U+d83d)`), caused by defect D3 below.
- Evidence paths are relative to the QA session's sweep folder, which is not kept in the repo.

**Counts: 79 PASS (5 of them PASS\*, meaning the protective outcome was seen but the wording or place differs from the spec), 0 FAIL, 13 NOT RUN.**
The defects below break requirements in this batch (Note with a limit, Times never overlap, Accessible add screen) but in situations that no listed scenario describes, so they are reported as defects rather than scenario FAILs.

## 1. Batch A scenarios

### attack-details
| Scenario | Result | Note |
|---|---|---|
| Open an attack's details › Attack no longer exists | NOT RUN | No user path to a deleted attack's details: there is no undo on the details screen, no deep link, and the back stack never keeps a deleted attack's screen. |
| Exact start and end times › Correct the start | PASS | 11:20 → 11:05. Duration went to 52 min; after leaving, home shows "11:05 AM to 11:57 AM". |
| Exact start and end times › End in the future | PASS\* | End at now+20: the picker refuses OK with "Pick a time between the start and now." and the end is unchanged. The spec's inline error under the time ("This time is in the future.") can only be reached with a clock change. |
| Exact start and end times › Clock tolerance | PASS | At 11:53:18 I picked 11:57 (+3 min 42 s) and it was stored. +6 min (5 min 13 s ahead) was refused. |
| Exact start and end times › End before start | PASS\* | End 11:10 before start 11:20 is refused in the picker ("Pick a time between the start and now."); nothing stored. A start after the end gives "Pick a time before the end." |
| Exact start and end times › Attack in progress | PASS | Shows "In progress" with no end picker node; the start moved 12:33 → 11:30. |
| Times never overlap › Overlapping start | PASS | "This would overlap the migraine from Mon, Oct 26, 12:00 PM to 1:30 PM." plus "Merge them"; the start stays 2:05 PM. |
| Times never overlap › Two overlapping attacks | PASS | "This would overlap 2 migraines: Mon, Oct 26, 9:00 AM to 10:00 AM; Mon, Oct 26, 12:00 PM to 1:30 PM." |
| Times never overlap › Merge from the details screen | PASS | Now 12:00 PM–4:30 PM with the other attack's 12:30 entry (5/10), Nausea and Ibuprofen 12:15. The other attack is gone (count 4→3); "Migraines merged." shown. |
| Times never overlap › Note typed before merging | PASS | Stored note `'Bright office\n\nTook ibuprofen'` (29/1000) after leaving. |
| Times never overlap › Touching attacks | PASS | Start 1:30 PM stored next to an attack that ended at 1:30 PM. |
| Times never overlap › Seconds do not overlap | PASS | The other attack ended 12:45:37; start set to 12:45 is accepted with no error. Duration shows "2 min" (12:45:37→12:48), not 3 min. |
| Pain entries over time › Add a later, stronger entry | PASS | "2:10 PM · 6/10" then "3:00 PM · 8/10", "Peak 8/10". |
| Pain entries over time › Entry outside the attack | PASS\* | 5:00 PM after a 4:30 PM end is refused in the picker ("Pick a time inside this migraine."); nothing stored. |
| Pain entries over time › Remove and undo | PASS | "Pain level removed" → Undo → 3:00 PM 8/10 back. |
| Pain entries outside new times › Shorten the attack | PASS | End 6:00 → 4:00 PM: "1 pain level outside the new time was removed"; the 5:00 PM entry is gone. |
| Pain entries outside new times › Undo the shortening | PASS | Undo → end 6:00 PM and 5:00 PM 7/10 back. |
| First maximum pain safety card › First 10 | PASS | Card "Sudden, worst-ever pain? If this pain came on suddenly…" with "Got it" above the details; the rest scrolls and works (`shots/first-10-card.png`). |
| First maximum pain safety card › Later 10 | PASS | With a 10 on the Oct 21 attack, adding a 10 to Oct 23 shows no card. |
| Where it hurts… › Built-in choices | PASS | 7 locations, 8 symptoms and 13 triggers, exactly the spec lists. |
| Where it hurts… › Quick double tap | PASS | `doubleTapOn "Forehead"`, twice: not selected, nothing else selected. A single tap selects it (control). |
| Functional impact › Change the impact | PASS | Had to stop, then Bed rest → only "3 · Bed rest" checked. |
| Functional impact › Clear the impact | PASS | Tapping Bed rest again leaves nothing checked. |
| Note with a limit › Reaching the limit | PASS | 1000 characters with the cursor at the end: "Z" and "👍" are both rejected, note unchanged, counter "1000/1000". **See D2** (typing mid-note at the limit cuts the end) and **D3** (emoji crossing the limit leaves half a character). |
| Delete with undo › Window passes | PASS | Default 5 s window: the message goes, the attack is never listed again (also after force-stop + relaunch), "Days with attack medication" 1→0. |
| Delete with undo › Delete the attack in progress | PASS | Home leaves attack mode and shows "Migraine deleted" with Undo. |
| Accessible details screen › Screen reader on a symptom | NOT RUN | TalkBack not run (lowest priority). The chip reports checkable=true and checked=true; whether TalkBack says "selected" or "checked" is unverified. |
| Accessible details screen › Largest font size | PASS | font_scale 2.0: every section (Time … Notes, counter, Delete migraine) was reached by scrolling. |
| Medications taken › Intakes listed | PASS | "Ibuprofen 400 mg, 2:20 PM" then "Sumatriptan 50 mg, 4:00 PM", oldest first. |
| Medications taken › Remove with undo | PASS | "Medication removed" → Undo → listed again. |
| Medications taken › Add from details | PASS | "Taken at Mon, Oct 26, 2:05 PM", the attack's start. |

### attack-history
| Scenario | Result | Note |
|---|---|---|
| The month's attacks › Newest first | PASS | "4 migraines in October": 27 Oct, 26 Oct 2:05, 12:00, 9:00 in that order. |
| The month's attacks › Attack across two months | PASS | Sep 30 11:00 PM – Thu 2:00 AM is listed under "1 migraine in September" and "3 migraines in October"; the Oct 1 cell is cloudy. |
| Choose a day › Choose a day | PASS | "Tuesday, October 27" lists 11:05, 12:41, 12:44 and 12:45, oldest first, plus "Show all of October". |
| No attacks yet › A day with medication only | PASS | Clean install, Ibuprofen logged for Oct 20: "Tuesday, October 20: attack medication taken, no migraine logged." Solid outline, no sky (`shots/crop-oct19-21.png`). |
| Undo a deletion on History › Offered once | PASS | Deleted from History, swiped the message away, rotated with landscape confirmed (mRotation=1) and back: no "Migraine deleted" or Undo. |
| History stays current › Time moved to another day | PASS | Oct 14 attack moved to Oct 15. Back on History: "October 14: nothing logged.", "October 15: cloudy. 1 migraine". |
| Accessible History › Screen reader on a listed attack | NOT RUN | TalkBack not run. Row label: "Monday, October 26, 2:05 PM to 4:30 PM, peak 10 of 10. Opens its details." |
| Accessible History › Landscape at 200% font | PASS | Calendar, summary and list scroll to the end; no clipping seen (`shots/history-200-land-end-small.png`). |
| Listed attacks reachable… › Accessibility services on a listed attack | PASS | Row node is focusable=true and clickable=true; the description sits on its merged child. |
| Doctor report from History › Opening it | PASS\* | Opens "A report for your doctor" behind the Premium gate: "Premium isn't available in your region yet" (no Play billing; subscription spec). |
| Insights from History › Opening it | PASS\* | Opens "See what your migraines have in common" behind the same Premium gate. |

### attack-logging
| Scenario | Result | Note |
|---|---|---|
| Start an attack in one tap › Start from the launcher shortcut | PASS | Long-press the launcher icon → "Migraine started" → attack mode "Started 1:24 PM · 0 min" with "Undo the start". |
| One attack in progress › Shortcut used during an attack | PASS | Shows the attack in progress ("Started 1:20 PM · 1 min"); no new start, no undo message; History lists 1 attack. |
| Record pain intensity › First intensity | PASS | Moderate at 12:34:53 → "12:34 PM · 6/10". |
| Record pain intensity › Pain changes later | PASS | 6 at 12:37, then 9 at 12:39 (2 min 18 s later), "Peak 9/10". A Severe tap only 1 min 58 s after Moderate replaced it instead (the correction rule). |
| Check attacks open for 72 hours › It has ended | PASS | Start moved to Fri 1:00 PM, relaunched: "Still going? Attacks longer than 72 hours should be checked by a doctor." with the 3 answers. "It has ended": only Oct 23–27 pickable, 12:00 on Oct 23 refused, 3:00 PM accepted → "1:00 PM to 3:00 PM". |
| Time storage › Attack across midnight | PASS | Ended past attack Oct 25 11:30 PM → Mon 12:30 AM: one row, both days' cells "1 migraine", Migraine days 4. The variant still in progress across midnight needs the clock and was not run. |
| Time storage › Daylight-saving change | NOT RUN | Needs a time-zone change (Tashkent has no DST); the zone belongs to other QA tooling. |
| Durable logging › Storage full | NOT RUN | Filling the shared emulator's storage was judged unsafe. |
| Double taps › Quick undo does not weaken the guard | PASS | End, Undo 0.4 s later ("Undone."), then a double tap at 540,2120, inside the normal-mode "Migraine started" bounds [42,1979][1038,2168]. Result: ended, normal home, no new attack. |

### attack-mode
| Scenario | Result | Note |
|---|---|---|
| Dark room › Elapsed time keeps up | PASS | Started 12:33:49. "3 min" at 12:37:43, "4 min" at 12:38:05, "5 min" at 12:39:12. |
| Dark room › Medication taken | PASS | "+ Medication taken" opens "Medication taken" (Taken at = now); the intake is linked to the attack in progress. |
| Large controls › One-handed use | PASS | Mild, Moderate and Severe are 118×64 dp with top y=1675; Migraine ended is 379×72 dp with top y=2085 (screen is 2400). |
| Gentle transitions › Entering attack mode | PASS | `screenrecord`: brightness changes from 1.85 s, darkens steadily and settles by 2.15–2.65 s. One dip and rise, no flashing (`logs/enter.mp4`). |
| Gentle transitions › Animations removed | PASS | With animator_duration_scale 0, start and end each switch in one frame and the screen is static afterwards. Only the animator scale was set, not transition or window. |
| Haptic confirmation › Confirm without looking | PASS | `dumpsys vibrator_manager`: com.migrely.app effect of 111 ms at 12:33:51.06, once per start and once per end. |
| Accessible attack mode › Screen reader | NOT RUN | TalkBack not run. |
| Accessible attack mode › Largest font size | PASS | At 200% every control is fully visible; targets are 64 dp or more, Details and the "earlier" chips 48 dp (`shots/attack-mode-200-small.png`). |
| Readable dim text › Start time line | PASS | Text (138,144,154) on card (17,21,31) = 5.68:1, not pure white. |

### medication-intakes
| Scenario | Result | Note |
|---|---|---|
| One tap logs › Log during an attack | PASS | "Ibuprofen 400 mg logged at 12:35 PM" with Done and Undo; listed in the attack's details. |
| One tap logs › Time in the future | PASS | now+10 is refused: "Pick a time up to now." |
| Intakes link › Logged from home in an attack's first minute | PASS | Attack 12:44:28–12:45:37; Ibuprofen logged from home at 12:44 is listed in its details. |
| Intakes link › Undo, then a new time from home | PASS | Logged 11:30 inside the 11:05 attack, Undo, then 10:30 and logged again. The 11:05 attack lists only its own 12:35 intake. |
| Intakes link › Later, for an attack recorded in another zone | NOT RUN | Needs a time-zone change. |
| Intakes follow › Delete and undo | PASS | Deleted an attack with 3 intakes, Undo on History: all 3 intakes, the entries and the note are back. |
| Intakes follow › Merge | PASS | The remaining attack lists Ibuprofen 12:15 (from the merged-away attack), Ibuprofen 2:20 and Sumatriptan 4:00. |
| Intakes follow › Undo a start after logging a medication | PASS | Clean install: start, log Ibuprofen, Done, Undo 7 s after the start → "Undone.", "No migraines logged in October", today "attack medication taken, no migraine logged". An earlier try at 19 s missed the window and is not counted. |
| Usable in the dark room › Large choices | PASS | Each "Log …" button is 379×64 dp on the attack-mode background (18,21,26); text is not pure white. |
| Accessible logging › Rotation keeps the time | PASS | Run again with landscape confirmed (mRotation=1): "1:40 PM" kept through landscape and back. |

### past-attacks
| Scenario | Result | Note |
|---|---|---|
| Add an attack that has ended › Defaults | PASS | Opened at 11:44:1x: Started 9:44 AM, Ended 11:44 AM. |
| Moving the start keeps the duration › Last night | PASS | Start yesterday 9:00 PM → Ended Mon 11:00 PM, 2 h 0 min. |
| Moving the start keeps the duration › Never past now | PASS | At 11:50, start 11:20 today → Ended 11:50 AM, 30 min. |
| Times in the past only › End in the future | NOT RUN | Needs the clock moved back while the screen is open. Variant: an end 6 min ahead is refused in the picker. |
| Times in the past only › Clock tolerance | PASS | End 1:38 PM picked at 13:34:06; "Add migraine" tapped at 13:34:36 (+3 min 24 s) → stored. |
| Times in the past only › End not after the start | NOT RUN | Not reachable without a clock change: the end picker refuses an end before the start, and the start picker refuses the current minute ("Pick a time before now."). |
| Overlapping attacks are named › Two overlaps | PASS | The card names both, 9:00–10:00 first, with "Merge them" and "Change the times". |
| Overlapping attacks are named › Touching attacks | PASS | 2:00–3:00 PM stored right after an attack ending at 2:00 PM. |
| Overlapping attacks are named › Seconds do not overlap | PASS | Previous attack ended 12:41:21; 12:41–12:43 is stored with Duration "1 min" (1 min 39 s), so the start snapped to 12:41:21. |
| Merge overlapping attacks › Merge three into one | PASS | 8:00 AM–2:00 PM, "3 · Bed rest", note `'Bright office\n\nSkipped lunch'`. "Merge them" was double-tapped: one merge, one attack. |
| Merge overlapping attacks › Merge with the attack in progress | PASS | In-progress start moved to 11:30 over 11:05–11:57, Merge → in progress since 11:05 AM; home in attack mode, "Started 11:05 AM · 1 h 35 min". |
| Merge overlapping attacks › Nothing overlaps any more | NOT RUN | No user path to delete the overlapping attack while the add screen stays open. |
| Merged notes stay within the limit › Long notes | PASS | 700+700 → exactly first + "\n\n" + first 298 of the second, 1000 characters. |
| Merged notes stay within the limit › Three notes | PASS | 600/600/10 → exactly first + "\n\n" + first 398 of the second. Extra: when the cut falls inside "👍", the whole emoji is dropped (999 units, no half character). |
| One attack per tap › Double tap | PASS | `doubleTapOn "Add migraine"`: one attack listed, and one back press returns home. |
| Picked times survive interruptions › Rotation | PASS | Yesterday 9:03 PM / 11:03 PM kept in landscape (bounds changed) and back. |
| Accessible add screen › Screen reader on a time | NOT RUN | TalkBack not run. Label: "Change when it started, now Tue, Oct 27, 9:44 AM". |
| Accessible add screen › Card announced | NOT RUN | TalkBack not run. |
| Accessible add screen › Largest font size in landscape | PASS | At 200% in landscape the times, the overlap card, "Merge them" and "Change the times" are all reachable by scrolling. **See D4**: the time picker dialog itself is clipped in landscape. |
| Add for a chosen day › A past day | PASS | At 13:28, History Oct 14 → Started Wed Oct 14 11:28 AM, Ended 1:28 PM. |
| Add for a chosen day › Yesterday, late at night | NOT RUN | Needs the clock at 01:10. |
| Add for a chosen day › Today | PASS | At 13:29:5x, History today → 11:29 AM to 1:29 PM, both Oct 27. |

## 2. Veteran edge cases

### Note field
- **Emoji and the counter:** "Hi 👨‍👩‍👧 🇺🇿 👍🏽 é" (10 visible characters, 16 code points) shows "23/1000". The counter counts UTF-16 units, so the family emoji counts 8, the flag 4 and the toned thumb 4. Seen on two clean installs. → D5.
- **Can the user exceed the limit:**
  - At the end, no: with 1000 characters, both "Z" and "👍" are rejected.
  - In the middle, the text changes: each typed character is inserted and the note's last character is silently dropped. → D2.
- **Can a cut split an emoji:** yes, when typing or pasting crosses the limit.
  - 996 ASCII + 👨‍👩‍👧 → `👨 ZWJ` + lone U+D83D, shown "👨�".
  - 999 + 👍 → lone U+D83D, shown "�".
  - Pasting 590 ASCII + 👍×5 after 401 characters → "👍👍👍👍�".
  - Stored and still there after reopening. → D3.
  - Merges do not split (the whole emoji is dropped).
- **Arabic, Hebrew and English with many newlines:** `'Pain صداع شدید after שלום עולם work 12\n\n\nline2\n\n\n\n\nend\n\n'` (56/1000) was identical after leaving and reopening, and after force-stop and relaunch. It renders correctly with bidi and the blank lines (`shots/rtl-note-reopened-small.png`). History and home never show notes, so there was nothing to check there.
- **Paste far beyond the limit:** I copied a 1000-character note with Ctrl+A / Ctrl+C and pasted it after "Start-". The result is exactly "Start-" + the first 994 characters, cut silently with no feedback.
- **Typing tool note:** `adb shell input text` silently drops the tail of long strings (it stopped at 617 and 468 of 700). That is the tool, not the app; long notes were typed in verified 100-character chunks (`typelong.py`). One merge where the first note was only 468 characters showed the cut-from-the-end rule working (468 + "\n\n" + 530).

### "Don't keep activities"
- `settings put global always_finish_activities 1` alone did **not** take effect: the activity stayed STOPPED.
- I enabled Developer options (development_settings_enabled was unset) and flipped the "Don't keep activities" switch off and on. After that, `dumpsys` showed state=DESTROYED after Home.
- Each case below was Home, then reopen from Recents:
  - Add screen with picked times (Mon 9:00 PM / 11:00 PM): kept.
  - Add screen with the date picker open on Oct 27: dialog and selection restored.
  - Add screen with the time picker open (11:40 PM): restored.
  - Details screen with a typed note ("Unsaved-DKA-note", not left): kept.
  - Details screen with the "Add pain level" dialog at 8/10: restored.
  - Details screen with the pain dialog's own time picker open (9:35 PM): restored, and the entry was saved.
  - "Medication taken" with Taken at changed to 10:10 PM: kept.
- Rotating with the date picker open (September 15 selected) kept the selection and month. Rotating with the time picker open (2:25 PM) kept the time. Rotating with the pain dialog open (8/10 at 9:35 PM) kept both.
- Restored afterwards: switch off (activity back to STOPPED), the setting set to 0, development_settings_enabled deleted.
- No state loss found.

### Back during unsaved edits
- Details note followed by two quick system backs: the note is stored.
- **Top-bar Back while the keyboard is open is impossible.** The window pans, the top bar (with Back) leaves the screen with bounds [0,0][0,0], and the chips draw under the status bar. → D6. With the keyboard hidden, top-bar Back works.
- Add screen with a picked start, then top-bar Back: nothing stored, History shows "No migraine logged on this day."
- "Medication taken" with a typed name ("Paracetamol"), then top-bar Back: nothing logged and nothing added ("None logged yet.").

### Rapid repeated taps
- Double tap on "Merge them": one merge.
- Double tap on "Add migraine": one attack.
- Double tap on "Delete migraine": one deletion. In one of two runs no Undo message was visible afterwards (see Unconfirmed).
- adb double tap on "Undo" in History: restored once ("Undone."), and the second tap did not open "Doctor report" underneath.
- Double tap on "Add and log": one "Ibuprofen logged".
- Double tap on "Save" in the medication editor: one Naproxen.
- End: double tap in the upper part of "Migraine ended" after a quick Undo: ended, no new attack.
- Forehead double tap: no change.

### Midnight-adjacent times
- 23:30 → 00:30 (Oct 25 → Oct 26): stored as one attack; History shows it on both days and the list shows "11:30 PM to Mon 12:30 AM". Duration 1 h 0 min.
- Sep 30 23:00 → Oct 1 02:00 is listed in both months.

### Logcat
- No `com.migrely.app` FATAL or AndroidRuntime crash all session.
- The 9 FATAL entries are the `uiautomator` dumper crashing on the lone surrogate from D3 (KXmlSerializer "Illegal character (U+d83d)"). Maestro hierarchy reads on that screen fail the same way.

## 3. Confirmed defects

### D1: A live attack can be started inside a stored attack whose end is a few minutes in the future, so two overlapping attacks get stored (S2, wrong data; narrow trigger)
- **Steps** (from a clean install):
  1. Home → "Add a past migraine".
  2. Change "Ended" to today at now + 4 min, which the clock tolerance allows.
  3. "Add migraine", then back to home.
  4. Tap "Migraine started" straight away.
  5. A few seconds later, tap "Migraine ended".
- **Expected:** "Times never overlap another attack" (attack-details and past-attacks), and attack-logging: "The new start SHALL NOT be earlier than the end of the previous attack". No two attacks should cover the same time. The start should be refused, explained, or moved to the stored attack's end.
- **Actual:** home shows both attacks and counts both.
  - Run 1, 13:54: "Migraine from 11:54 AM to 1:58 PM…" and "Migraine from 1:54 PM to 1:55 PM…", "2 migraines today".
  - Run 2, 13:56: "11:55 AM to 1:59 PM" and "1:56 PM to 1:56 PM".
  - The first time it happened (11:34–1:38 PM with 1:35–1:35 and 1:35–1:36) was during the sweep.
- **Evidence:** `shots/overlap-repro1.png`, `shots/overlap-repro2.png`; uiautomator labels quoted above. Reproduced 2/2 from clean installs.

### D2: In a full note, typing in the middle inserts the character and silently deletes the note's last character (S3; strictly it's a small silent data loss)
- **Steps:**
  1. Clean install → add a past attack.
  2. In Notes, type 1000 characters.
  3. Tap somewhere in the middle of the text.
  4. Type "Q".
- **Expected:** "Input SHALL stop at 1,000 characters" (attack-details › Note with a limit). The extra character is refused and the existing text is unchanged, as happens at the end of the note.
- **Actual:** "Q" is inserted at the cursor and the last character of the note disappears. The counter stays "1000/1000" with no feedback. Each further keystroke eats one more character from the end.
- **Evidence:** text diffs from the uiautomator EditText.
  - Clean run: `('insert', 53, 53, 53, 54) '.F000000' -> '.F00Q0000'` and `('delete', 999, 1000, …) '0990.' -> '0990'`.
  - Earlier run on a merged 1000-character note: insert at 225, delete at 999, repeated for 4 keystrokes.
- 2 runs, one from a clean install. If the end of the note holds an emoji, this cut can also split it (see D3).

### D3: When typing or pasting crosses the 1000 limit, an emoji is split and half a character is stored, shown as "�" (S3)
- **Steps:**
  1. Clean install → add a past attack.
  2. In Notes, type 999 ASCII characters.
  3. Type "👍" (or type 996 characters and then "👨‍👩‍👧"; or paste text ending in 👍👍👍👍👍 so it crosses 1000).
  4. Leave and reopen the attack.
- **Expected:** the whole character is refused, as the merge does ("no character SHALL be split in half", past-attacks). The obvious user expectation is that an emoji is never broken.
- **Actual:** the note keeps the high surrogate U+D83D without its low half: "G00000990�" (999+👍), "E00000👨�" (996+family; the ZWJ sequence is cut and half of 👩 is left), "…👍👍👍👍�" (paste). Counter "1000/1000". The broken character is stored and shows after reopening. JSON backup and CSV export write it as "?" (`pulled/migrely-2026-10-27.json`: `E00000\xf0\x9f\x91\xa8\xe2\x80\x8d?`). The text also crashes `uiautomator dump` (`IllegalArgumentException: Illegal character (U+d83d)`), and Maestro can no longer read that screen.
- **Evidence:** `shots/emoji-cut-996-end-small.png`, `shots/emoji-cut-repro2-small.png`, `shots/emoji-thumb-999-small.png`, `shots/emoji-paste-split-small.png`; `logs/crash.log` (9 uiautomator FATALs). Reproduced 4 times, 2 of them from clean installs.

### D4: In landscape, the time picker dialog is too narrow: hours 1–5, the minutes and PM can't be seen or picked (S2 in landscape: you can't pick an afternoon time; portrait works)
- **Steps** (from a clean install):
  1. Turn the phone to landscape (auto-rotate off, user_rotation 1; mRotation=1 confirmed).
  2. Open "Add a past migraine" → "Ended" → Next. The same happens with "Log medication" → Taken at → Next, and when rotating with the picker already open.
- **Expected:** "The screen SHALL stay usable at a 200% font size and in landscape" (past-attacks and attack-details › accessible). Every hour, the minutes and AM/PM can be picked.
- **Actual:** the dialog is 840 px wide ([780,63][1620,1017]). The horizontal time picker's dial runs past its right edge and covers the minutes field and the PM half of the AM/PM switch. The accessibility tree has only "12" and "6" to "11 o'clock", "AM" and an empty "Select minutes". "1" to "5 o'clock", "PM" and the minute digits are missing.
- **Evidence:** `shots/timepicker-landscape-small.png` (add screen), `shots/med-timepicker-landscape-crop.png` (medication screen). Reproduced on 2 clean installs and 2 screens.

### D5: The note counter counts UTF-16 units, not the characters the user sees (S4)
- **Steps:** clean install → add a past attack → type "Hi 👨‍👩‍👧 🇺🇿 👍🏽 é" in Notes.
- **Expected:** the counter "SHALL show the characters used". A user sees 10 characters.
- **Actual:** "23/1000". One family emoji costs 8 of the 1000.
- **Evidence:** uiautomator: codepoints 16, utf16 23, COUNTER 23/1000. Two clean installs.

### D6: With the keyboard open on the details screen, the whole window pans: the top bar (with Back) goes off screen and content draws under the status bar (S4)
- **Steps:** clean install → open an attack's details → tap Notes so the keyboard opens.
- **Expected:** the top bar with Back stays in place and content doesn't overlap the status bar.
- **Actual:** the top bar is off screen (Back bounds [0,0][0,0]) and the trigger chips are drawn under the status-bar clock. Top-bar Back can't be reached until the keyboard is closed.
- **Evidence:** `shots/note-keyboard-topbar-small.png` (clean install at 14:01), `shots/emoji-cut-996-small.png` (clean install at 13:25).

### D7: Home's attack-row label capitalises "Not" mid-sentence (S4, screen reader)
- **Steps:** clean install → add any attack without pain → read the label of home's "Logged today" row.
- **Expected:** "…peak not recorded…", as History says.
- **Actual:** "Migraine from 1:54 PM to 1:55 PM, peak Not recorded. Opens its details." The same row with a value reads "peak 9 out of 10".
- **Evidence:** uiautomator labels from the D1 runs (13:54 and 13:56 clean installs) and others.

## 4. Unconfirmed, observations and spec mismatches
- **Unconfirmed:** a double tap on "Delete migraine" (details opened from History, 20 s undo window) once left History with the attack gone and no "Migraine deleted" or Undo message at all (14:18). On a clean repeat the message showed. Not reproduced; it would be data loss if real.
- **Spec mismatch, wording/place:** time rejections are shown inside the picker dialog ("Pick a time between the start and now.", "Pick a time before the end.", "Pick a time inside this migraine.", "Pick a time up to now.", "Pick a time before now."). The specs describe inline errors under the time ("This time is in the future.", "The end must be after the start."). Those strings exist but can't be reached without a clock change. The protective behaviour is correct.
- **Observation:** the first-10 safety card appeared again after the only earlier attack with a 10 had been deleted for good. That fits "no attack has ever had a 10" but not "the first time the user ever records a 10". The spec is ambiguous.
- **Observation:** "Taken at" defaults to the minute the screen was opened and doesn't follow the clock: opened at 12:35, tapped at 12:36:17, logged at 12:35. This matches "log it at the shown time".
- **Observation:** symptom chips expose checkable/checked rather than selected. Whether TalkBack reads "Aura, selected" was not checked.
- **Test-harness note:** setting `always_finish_activities` with `settings put` doesn't apply until the Developer-options switch is flipped. A plain `settings get global always_finish_activities` therefore tells you nothing.

## 5. Device restore
- Done:
  - "Don't keep activities" turned off with the switch, and `always_finish_activities=0`.
  - `development_settings_enabled` deleted (it was unset).
  - `accelerometer_rotation 0` / `user_rotation 0`, then the baseline.
  - `font_scale 1.0`, `cmd uimode night no` (night mode was on before I started), `animator_duration_scale` deleted.
  - Accessibility timeout back to null.
  - `qa/reset_device.sh` run, last of all.
- Verified: always_finish_activities=0, development_settings_enabled=null, font_scale=1.0, user_rotation=0, mRotation=0, night mode no, timeout null.
- Build 4 release APK (versionCode=4) is still installed. My exported files in Download and the temporary files on /sdcard were deleted, and the logcat captures were stopped. App data from the last clean-install test is left in place.
