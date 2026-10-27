# Batch B exploratory sweep: build 4 (versionCode 4)

Issues filed from section 3:
- B1 is #87 (S4);
- B2 is #88 (S4).
Both have "launch blocker: no", sent to the dev session on 2026-10-27. Sweep A's leads are settled here:
- #78: a backup that holds an overlap imports whole;
- #81: the PDF with a lone surrogate builds;
- the double-tap delete: 14 runs, none lost.

- Device: emulator-5580 (Migrely_QA_API_36), en-US, 12-hour clock, Asia/Tashkent, run 2026-10-27 14:45–17:10.
- APK: the R8 release APK `migraine-app-qa-rc/app/build/outputs/apk/release/app-release.apk` (versionCode 4, not debuggable). The debug APK was used once, 15:53–16:03, for #81, which needs the simulated store to buy Premium; the release APK was installed again afterwards.
- Method:
  - Backup seeds written in `sweep/b/` (`medseed.py` and `bkseed.py`, built on `qa/seed_backup.py`'s Backup class), imported through Your data with the repo's `import-step.yaml`.
  - Clock moves with `cmd alarm set-time` for Got it and the 30 days.
  - uiautomator for the texts, and adb taps for timing-sensitive steps.
  - Logcat crash and error capture ran the whole session (`b/logs/`).
- **Scope cut by the coordinator at 16:00 and at 16:52:** everything after data-backup, the #78/#81 leads and the double-tap retries was moved to the 1.0.1 build.
- **Crashes: none in com.migrely.app.**
  - The only FATAL is the uiautomator dumper on the lone surrogate (#81), as in sweep A.
  - The other error lines are system noise and MediaProvider's "No space left on device" during the storage test.
- **Counts:** medication-overuse 11/11 PASS; data-backup 2 PASS + 1 NOT RUN; in-app-messages 2 PASS* (seen in the storage test) + 15 NOT RUN; everything else NOT RUN (moved to 1.0.1, or skipped by the brief: app-settings, app-shell, subscription).

## 1. Batch B scenarios
app-settings › Privacy and about › The full privacy policy | NOT RUN | skipped by the brief: other tracks cover it
app-settings › Privacy and about › No browser | NOT RUN | skipped by the brief: other tracks cover it
app-shell › App identity and supported devices › Install on the oldest supported Android version | NOT RUN | skipped by the brief: other tracks cover it
app-shell › App identity and supported devices › Device below the supported version | NOT RUN | skipped by the brief: other tracks cover it
app-shell › Launch opens the home screen › Rotation on the home screen | NOT RUN | skipped by the brief: other tracks cover it
app-shell › Dark low-brightness theme by default › System in light mode | NOT RUN | skipped by the brief: other tracks cover it
app-shell › Dark low-brightness theme by default › System in dark mode | NOT RUN | skipped by the brief: other tracks cover it
app-shell › No data leaves the device on launch › Launch with network traffic observed | NOT RUN | skipped by the brief: other tracks cover it
app-shell › Dates and times in English › A phone in Russian | NOT RUN | skipped by the brief: other tracks cover it
app-shell › Dates and times in English › A phone in English (United States) | NOT RUN | skipped by the brief: other tracks cover it
app-shell › Dates and times in English › The language changes while the app runs | NOT RUN | skipped by the brief: other tracks cover it
app-shell › Dates and times in English › A first day chosen in the phone's settings | NOT RUN | skipped by the brief: other tracks cover it
app-shell › Dates and times in English › The CSV on a phone in Russian | NOT RUN | skipped by the brief: other tracks cover it
custom-tags › Add your own symptom or trigger › Name too long | NOT RUN | moved to the 1.0.1 build
custom-tags › Add your own symptom or trigger › Empty name | NOT RUN | moved to the 1.0.1 build
custom-tags › No duplicate names › Archived match | NOT RUN | moved to the 1.0.1 build
custom-tags › At most 100 per kind › Limit reached | NOT RUN | moved to the 1.0.1 build
custom-tags › Remove from the list without losing history › Remove a used tag | NOT RUN | moved to the 1.0.1 build
data-backup › Replace or add › A migraine deleted after the backup | PASS | seed of 2 migraines, app export, one deleted past its undo, Add: "Added 1 migraine. 1 was already here."; points 4 and 7, Left/Nausea/Stress, impact, note, Ibuprofen (Helped a lot) and Sumatriptan back.
data-backup › Replace or add › Replace while a deletion can still be undone | PASS | delete, swipe the message, Replace 34 s later (inside the 2-min hidden window): only the backup's migraine; the automatic copy holds 1 (the deleted one left out); nothing came back after the purge and a relaunch; restoring the copy gave the other migraine only. See edge note: Settings is covered while the Undo message shows.
data-backup › Android backup › A new phone | NOT RUN | bmgr with LocalTransport and fake_encryption_flag=true: "Transport rejected backup of com.migrely.app, skipping" 3/3 (incl. after bmgr init, --non-incremental). Needs a real Google account backup.
day-markers › Mark a chosen day › Period tracking off | NOT RUN | moved to the 1.0.1 build
day-markers › Mark a chosen day › Removing a mark with tracking off | NOT RUN | moved to the 1.0.1 build
day-markers › Period today from home › Period tracking off | NOT RUN | moved to the 1.0.1 build
day-markers › Period today from home › Removing today's mark with tracking off | NOT RUN | moved to the 1.0.1 build
day-markers › Accessible marks › Disabled switch | NOT RUN | moved to the 1.0.1 build
doctor-report › Large periods › No note among the migraines shown | NOT RUN | moved to the 1.0.1 build
in-app-messages › Messages appear at the top and stay dim › Message during attack mode | NOT RUN | moved to the 1.0.1 build
in-app-messages › Messages appear at the top and stay dim › Landscape with a display cutout | NOT RUN | moved to the 1.0.1 build
in-app-messages › Messages appear at the top and stay dim › Landscape normal mode | NOT RUN | moved to the 1.0.1 build
in-app-messages › Landscape attack mode keeps messages beside the controls › Message during landscape attack mode | NOT RUN | moved to the 1.0.1 build
in-app-messages › Landscape attack mode keeps messages beside the controls › A tap while the guidance moves | NOT RUN | moved to the 1.0.1 build
in-app-messages › Message kinds are told apart by shape › Kinds of the home messages | NOT RUN | moved to the 1.0.1 build
in-app-messages › Undo window follows the accessibility timeout › Default undo window | NOT RUN | moved to the 1.0.1 build
in-app-messages › Undo window follows the accessibility timeout › Reduce motion | NOT RUN | moved to the 1.0.1 build
in-app-messages › Message durations by kind › Information message | NOT RUN | moved to the 1.0.1 build
in-app-messages › Message durations by kind › Storage full | PASS* | seen during the data-backup storage test only: "Your phone is full. Free up some space and try again." with Dismiss stayed 15 s until closed. The rest of in-app-messages moved to the 1.0.1 build.
in-app-messages › Messages survive rotation and short interruptions › Rotate during the undo window | NOT RUN | moved to the 1.0.1 build
in-app-messages › Messages survive rotation and short interruptions › Return after the time is up | NOT RUN | moved to the 1.0.1 build
in-app-messages › Messages can be dismissed › Close an error | PASS* | same run: tapping Dismiss closed the storage-full message.
in-app-messages › Undo outcomes are confirmed › Undo no longer possible | NOT RUN | moved to the 1.0.1 build
in-app-messages › Accessible messages › Largest font size | NOT RUN | moved to the 1.0.1 build
in-app-messages › Gentle motion and haptics for messages › Reduce motion | NOT RUN | moved to the 1.0.1 build
in-app-messages › Gentle motion and haptics for messages › Error haptic | NOT RUN | moved to the 1.0.1 build
medication-effect › Answer in the attack's details › Answer later | NOT RUN | moved to the 1.0.1 build
medication-effect › Accessible question › Screen reader | NOT RUN | moved to the 1.0.1 build
medication-effect › Accessible question › Largest font | NOT RUN | moved to the 1.0.1 build
medication-list › Edit a medication › Rename | NOT RUN | moved to the 1.0.1 build
medication-list › Order and limit › Recently used first | NOT RUN | moved to the 1.0.1 build
medication-list › Order and limit › Limit reached | NOT RUN | moved to the 1.0.1 build
medication-overuse › Count overuse days › Two categories in one day | PASS | Ibuprofen and Sumatriptan on the same 8 days: Simple 8, Triptans 8, All attack medication 8 (not 16); card "Triptans on 8 of the last 30 days", soft. A day with 3 intakes counted once (simple 13 seed).
medication-overuse › Count overuse days › Not sure | PASS | category null on 10 days: "Combination or other medications on 10 of the last 30 days", doctor text; History "Combination or other: 10 days (limit 10), at the limit". "Not sure" made in the UI: "Combination or other: 1 day (limit 10)".
medication-overuse › Count overuse days › Preventive | PASS | Topiramate (and a preventive tagged SIMPLE) every day for 41 days: no card, no "Last 30 days". With 7 triptan days: only "Triptans: 7 days", no All row.
medication-overuse › Count overuse days › Thirty days back | PASS | every seed had intakes 29, 30 and 31 days back: 29 counted, 30 and 31 not (triptans 7/8/10 exact). A Tokyo intake at 01:00 on Sep 28 Tokyo (Sep 27 21:00 Tashkent) counted on its Tokyo date.
medication-overuse › Soft and doctor warnings › Doctor warning | PASS | triptans 10: "Triptans on 10 of the last 30 days" + "Taking them this often can make headaches more frequent. It's worth talking to your doctor." Painkillers 15: "Painkillers on 15", doctor text.
medication-overuse › Soft and doctor warnings › Below the soft level | PASS | triptans 7: no card. Painkillers 12: no card. Soft at triptans 8 and painkillers 13 ("Taking them on 10/15 days or more a month can lead to more headaches. Worth keeping an eye on.").
medication-overuse › Soft and doctor warnings › Several categories together | PASS | 6 painkiller + 4 other triptan days: "Acute medications on 10 of the last 30 days", doctor. 5+3: "Acute medications on 8", soft.
medication-overuse › Soft and doctor warnings › A single category already warned | PASS | 15 painkiller + 2 triptan days: "Painkillers on 15", not 17 together (History All 17). 13+2: "Acute medications on 15", doctor, as the rule says (higher than the single soft level).
medication-overuse › A warning once in 30 days › Got it | PASS | Got it on the triptan doctor card: hidden after relaunch and after Add of 2 more triptan days (12). Got it is per category: Got it on "Acute medications" did not hide "Triptans on 10".
medication-overuse › A warning once in 30 days › Another category's Got it | PASS | clock +3 days, painkillers on 13 days added: "Painkillers on 13", soft, while triptans (11) stay hidden.
medication-overuse › A warning once in 30 days › After 30 days | PASS | Got it at Oct 27 15:15:20. Hidden at Nov 25 15:20, Nov 26 13:00 and 15:14; shown at Nov 26 15:17 "Triptans on 10" (shots-b/after30-triptan-card.png). Counted by the instant, not the date.
month-sky › One sky per day from data › Medication counts in its own zone, preventives never | NOT RUN | moved to the 1.0.1 build
month-sky › One sky per day from data › Worst attack decides | NOT RUN | moved to the 1.0.1 build
month-sky › One sky per day from data › Pain level never recorded | NOT RUN | moved to the 1.0.1 build
month-sky › One sky per day from data › Unrecorded and mild | NOT RUN | moved to the 1.0.1 build
month-sky › One sky per day from data › Nothing logged | NOT RUN | moved to the 1.0.1 build
month-sky › One sky per day from data › Future day | NOT RUN | moved to the 1.0.1 build
month-sky › One sky per day from data › Attack in progress | NOT RUN | moved to the 1.0.1 build
month-sky › One sky per day from data › Pain-free day | NOT RUN | moved to the 1.0.1 build
month-sky › One sky per day from data › Period day | NOT RUN | moved to the 1.0.1 build
month-sky › One sky per day from data › A day with medication only | NOT RUN | moved to the 1.0.1 build
month-sky › One sky per day from data › Medication on a migraine day | NOT RUN | moved to the 1.0.1 build
month-sky › One sky per day from data › Medication on a pain-free day | NOT RUN | moved to the 1.0.1 build
month-sky › One sky per day from data › Preventive medication only | NOT RUN | moved to the 1.0.1 build
month-sky › Days follow where the attack was logged › Past midnight | NOT RUN | moved to the 1.0.1 build
month-sky › Days follow where the attack was logged › Ends at midnight | NOT RUN | moved to the 1.0.1 build
month-sky › Calendar follows the locale › Week starts on Monday | NOT RUN | moved to the 1.0.1 build
month-sky › Calendar follows the locale › Week starts on Sunday | NOT RUN | moved to the 1.0.1 build
month-sky › Calendar follows the locale › A phone in Russian | NOT RUN | moved to the 1.0.1 build
month-sky › Calendar follows the locale › Today outlined | NOT RUN | moved to the 1.0.1 build
month-sky › Accessible days › Screen reader on a storm day | NOT RUN | moved to the 1.0.1 build
month-sky › Accessible days › Screen reader on today during an attack | NOT RUN | moved to the 1.0.1 build
month-sky › Accessible days › Large font | NOT RUN | moved to the 1.0.1 build
month-sky › Only the shown month is loaded › Ten years of history | NOT RUN | moved to the 1.0.1 build
month-sky › Days reachable with a keyboard › Accessibility services | NOT RUN | moved to the 1.0.1 build
month-stats › A month summary › The current month | NOT RUN | moved to the 1.0.1 build
month-stats › The progress message › Not before any attack | NOT RUN | moved to the 1.0.1 build
month-stats › The progress message › No migraine logged | NOT RUN | moved to the 1.0.1 build
reminders › Offered during onboarding › Permission refused | NOT RUN | moved to the 1.0.1 build
subscription › Payment problems › Grace period | NOT RUN | skipped by the brief: other tracks cover it
subscription › When the paywall shows itself › Google Play offers no products | NOT RUN | skipped by the brief: other tracks cover it
today-sky › Today's sky reflects today's data only › Attack in progress | NOT RUN | moved to the 1.0.1 build
today-sky › Today's sky reflects today's data only › Attack from last night | NOT RUN | moved to the 1.0.1 build
today-sky › Sky readable without colour › Screen reader on the card | NOT RUN | moved to the 1.0.1 build
today-sky › Today's attacks open their details › Two attacks today | NOT RUN | moved to the 1.0.1 build
today-sky › Today's attacks open their details › Attack from last night | NOT RUN | moved to the 1.0.1 build
today-sky › Deleted attack can be undone from home › Rotation keeps the undo | NOT RUN | moved to the 1.0.1 build
today-sky › Add a past migraine from home › Landscape | NOT RUN | moved to the 1.0.1 build
today-sky › This month under today's sky › Calendar on home | NOT RUN | moved to the 1.0.1 build
today-sky › This month under today's sky › Undo on home updates the calendar | NOT RUN | moved to the 1.0.1 build
today-sky › This month under today's sky › Landscape | NOT RUN | moved to the 1.0.1 build
today-sky › Today's attacks reachable with a keyboard › Tab to a row | NOT RUN | moved to the 1.0.1 build
today-sky › Today's attacks reachable with a keyboard › Accessibility services | NOT RUN | moved to the 1.0.1 build
today-sky › Log a medication from home › Log from home | NOT RUN | moved to the 1.0.1 build
today-sky › One medication card under today's sky › Warning and question together | NOT RUN | moved to the 1.0.1 build
today-sky › One medication card under today's sky › Nothing due | NOT RUN | moved to the 1.0.1 build
today-sky › One medication card under today's sky › Attack mode | NOT RUN | moved to the 1.0.1 build

## 2. Edge cases and leads

### medication-overuse: boundaries and counting
- **Thresholds:**
  - Triptans: 7 none / 8 soft / 10 doctor.
  - Painkillers: 12 none / 13 soft / 15 doctor.
  - Combination or other: 10 doctor.
  - All acute together: 7 none / 8 soft / 10 doctor.
- **Wording:** every card text matches the spec exactly. History "Last 30 days" agreed with the card in every case: "at the limit" / "over the limit" and the doctor line.
- **Day 29 versus day 30 back:** 29 counts, 30 and 31 do not.
- **Time zones:** an intake counts on its date in the zone it was taken in (Tokyo).
- **Got it:**
  - It survives a relaunch and more days being added.
  - It is per category, the all-together card included.
  - It expires 30×24 h after the tap (15:14 hidden, 15:17 shown on day 30).
- **Overlapping attacks (D1/#78):** each with a Sumatriptan intake on the same day, they count as 1 day ("Triptans: 1 day (limit 10)"). History: "2 migraines", 1 migraine day.

### data-backup
- **#78, a backup holding two overlapping migraines:**
  - Setup: D1 reproduced on release (1:41–3:45 PM plus a live 3:42–3:42 PM). Both got a pain point, a note and an intake, and both open and edit.
  - JSON export: both attacks are in it.
  - Replace: "Backup imported: 2 migraines."
  - Import on a clean install: the same message, with every field intact.
  - Add: "Nothing new".
  - **Not a blocker.**
- **#81, a lone surrogate (debug APK):**
  - The database note is 999 ASCII characters plus EDA0BD, a lone U+D83D.
  - The 1-month Doctor report with notes on builds: 2 pages, the note in full ending "�", no app crash (`b/report-surrogate.pdf`, `shots-b/report-surrogate-p2-2.png`).
  - The JSON export is valid UTF-8 and writes the half character as "?".
  - Replace then a second export: identical attacks, medications, intakes and day marks.
  - **Not a blocker.**
- **Import, delete, Undo:** "Undone." brings the migraine back whole: points, both intakes with the answer, tags, impact and note.
- **Delete, swipe the message, Add the backup within the 2-min hidden window:** "Added 1 migraine. 1 was already here."
  - The copy is whole.
  - It survives the purge: still there after +161 s and a relaunch.
- **Refused files:** none changed anything, and the app stayed up.
  - The empty file (0 B), a truncated file, random bytes, another format, a wrong field type, a bare `[]`, and an attack whose end is before its start: "This file isn't a Migrely backup. Nothing was changed."
  - schemaVersion 7: "Update Migrely to open this file."
  - A UTF-8 BOM file is accepted.
- **Kill during an import** (900-migraine seed, `am force-stop` after tapping Replace):
  - At 0.05 s: the old 2 migraines stay intact.
  - At 0.15 s and 0.3 s: all 900 (export counts 900/1/128).
  - Restore the copy, killed at 0.1 s: exactly 2.
  - Never partial.
- **Storage nearly full** (`fallocate` in /data/local/tmp):
  - With about 300 KB free, a 746 KB export succeeded and the file is complete. Android freed cache.
  - With 0 bytes free, the system picker's SAVE does nothing, and MediaProvider logs "No space left on device". No file, no app message, no crash.
  - Replace with 0 bytes free: "Your phone is full. Free up some space and try again." with Dismiss. It stayed until dismissed, and the data (900) was intact.
  - The fill files are deleted.
- **Android backup:** see "A new phone" above. The bmgr state was restored: disabled, the GMS transport, the parameters deleted.

### Double-tap "Delete migraine" (sweep A lead) — 14 runs, none lost
- The runs, with the accessibility timeout at 20 s:
  - Home: x2 (0.12 s apart), x3, x2 at 0.04 s, x2 at 0.35 s, plus 3 more x2.
  - History: x2, x3, x2 at 0.05 s, x3 at 0.3 s.
  - Right after rotating: x2 in landscape, x2 after landscape→portrait.
  - "Don't keep activities" on (state=DESTROYED checked): x2 with Home and Recents during the window, x3.
- **Every run:** one deletion, "Migraine deleted" with "Undo the deletion" shown, Undo gave "Undone.", and the attack came back whole.
  - Whole means 2 pain points, Ibuprofen "Helped a lot", Sumatriptan, Left/Nausea/Stress, impact and note: the same fingerprint every time.
- **Explanation for sweep A:** one early run of mine missed the Undo at the default 5 s window. The uiautomator dump waits for idle while the shrinking line animates, so the tap landed after the message had gone. Sweep A's "no message" is most likely that tool delay, or Maestro's settle wait.

### Other
- **Long session:** TOTAL PSS was 40,363 KB at 14:48 (a fresh process) and 44,180 KB at 16:55. Not one 30-minute process: clean installs and kills restarted it often.
- **Not run (cut):** rotation with dialogs, font 2.0, and the DKA view-state walk on the batch-B screens.

## 3. Defects (each reproduced at least twice)

### B1: Add of a backup with no migraines says "Every migraine in it was already here." (S4, copy)
- **Steps:**
  1. A phone with entries.
  2. Your data → Import a backup → a file holding only medications and intakes (0 migraines).
  3. Add.
- **Expected:** a result that does not claim migraines. The spec leaves the wording open: fix-import-deleted lists "how the import's outcome is worded" as out of scope.
- **Actual:** "Added the other entries. Every migraine in it was already here."
- **Evidence:** uiautomator text, 4 runs (15:15, 15:16, 15:17, 15:20; `b/logs/imp-1515*.log` and following). No screenshot: the device was offline when I wrote this.
- **Launch blocker: no.** The data is right, only the sentence is odd.

### B2: While an Undo message shows, it covers home's Settings button, so Your data cannot be reached (S4, UX)
- **Steps:** delete a migraine, then Back to home. Seen in portrait and in landscape.
- **Expected:** the spec says only that messages never cover the large controls. Settings is not one of them.
- **Actual:**
  - The message replaces the top bar, and Settings is absent from the tree for the whole window, up to the accessibility timeout (120 s tried).
  - A tap where Settings was lands on "Undo".
  - "Replace while a deletion can still be undone" is reachable only by swiping the message away first.
- **Evidence:** `shots-b/land-undo-home-small.png`; uiautomator at 15:30 (portrait) and 15:33 (landscape).
- **Launch blocker: no.**

## 4. Device restore (and an incident)
- **16:56 incident:** `settings delete global development_settings_enabled` turned Developer options off, and with them USB debugging, so adb went offline.
  - **Fix:** through the emulator console's input events (`event mouse`, `event text`). Settings → About → Build number ×7 ("You are now a developer!"), then System → Developer options → USB debugging on → OK.
  - adb came back at 17:08.
  - **Left on deliberately:** development_settings_enabled=1 (it was null at the start), because deleting it turns adb off again. adb_enabled=1.
- **What I changed and put back:**

| Setting | How | Final value |
|---|---|---|
| always_finish_activities | `settings put` + the DKA switch flipped off in Developer options | 0 |
| accessibility_interactive_ui_timeout_ms | was 20000 / 120000 | deleted (null) |
| auto_time | was 0 during clock moves | 1 (clock 17:09 = host) |
| time zone | never changed | Asia/Tashkent |
| user_rotation / accelerometer_rotation | changed for the landscape steps | 0 / 1 |
| font_scale | never changed | 1.0 |
| animator_duration_scale | read 1.0 after the Developer options visit | deleted |
| bmgr | | disabled, GMS transport, `backup_local_transport_parameters` deleted |
| Download and /data/local/tmp | my files | deleted |

- **Packages:** none disabled by me. The 4 disabled packages (nfc, supervision, devicelockcontroller) were disabled at the start.
- **App:** the R8 release APK, versionCode 4, is installed. Its data is a 2-migraine seed.
- **Last of all:** `QA_SERIAL=emulator-5580 qa/reset_device.sh` ran: "back to its baseline".
