# QA record: fix-talkback-scroll-order (#255, BRD D193), tasks 8.1 to 8.4 and 9.1

## Verdict

**QA (qa-255), 2026-11-23: GO with recorded limits** for merging `bugfix/255-details-talkback-order` into `develop` for
1.4.0. The verdict applies to the final head **5e39100d**.

- **Product code: b86cadd2.** QA's APKs were built from b86cadd2. The final head adds only docs, openspec and
  `src/testDebug` files. QA's own check on 2026-11-23:

  ```
  $ git diff --name-only b86cadd2 5e39100d
  docs/BRD.md
  features/attack/ui/src/testDebug/java/com/migrely/app/attack/ui/view/AttackDetailsKeyboardFramesTest.kt
  features/history/ui/src/testDebug/java/com/migrely/app/history/ui/view/HistoryTouchTargetAuditTest.kt
  features/home/ui/src/testDebug/java/com/migrely/app/home/ui/view/HomeTouchTargetAuditTest.kt
  openspec/changes/fix-talkback-scroll-order/design.md
  openspec/changes/fix-talkback-scroll-order/proposal.md
  openspec/changes/fix-talkback-scroll-order/specs/app-shell/spec.md
  openspec/changes/fix-talkback-scroll-order/tasks.md
  # paths outside docs/, openspec/ and */src/test*/: 0
  ```

- **#255 is fixed where it was found.** The negative controls reproduce it on production v1.3.1 and on develop 224dad3d.
  - **Issue steps: Attack details, 200% font, landscape, 360 dp window.** Type in the note, swipe back 40 times with the
    keyboard up, close the keyboard with Back, then swipe back to Back.
    - **Candidate:** Back was reached with every stop once and none bare in **3 of 3 valid debug rows**: 2 at 360 dp and 1 at 411 dp. The walk took 15
      to 17 stops. Going forward to "Delete migraine" took 56 stops, every one once. The **R8 build** passed 1 of 1.
    - **Develop 224dad3d:** a 13-stop loop, 3 times round, and Back was never reached. It had 18 bare "Not selected" /
      "Selected" stops. Even with the keyboard up, the walk skipped Caffeine, Period, Weather and Smell.
    - **v1.3.1:** a 15-stop loop over the impact radios and triggers, and Back was never reached.
  - **Attack details at 100% portrait, no keyboard, from the end back to Back and on to the end.**
    - **Candidate:** 56 or 57 stops back and 55 or 56 forward. No repeat and no bare stop in **3 of 3** debug rows, with
      Remove animations (1 of 1), at 200% landscape (1 of 1), and on R8 (1 of 1).
    - **Develop:** 49 stops back. The whole Symptoms section and the "Possible triggers" heading were skipped, and 3 bare
      stops appeared at the top.
    - **v1.3.1:** 46 stops back (10 skipped) and repeats going forward.
- **No regression in the guards:**
  - **#259:** focus kept and 0 database writes in 6 of 6 debug rows and 2 of 2 R8 rows.
  - **#238 KR1/KR3:** 0 px shift and 0 scroll events, 3 of 3.
  - **#245 SW5:** the field moved by exactly the bar's 194 px.
  - **#239 V1:** "Enter a name." was said at 0.73 s.
  - **Maestro:** every flow 8.4 names passes on both builds (32 flows each, plus the process-death pair).
- **Today's first TalkBack focus changed, and the change is a fix.** At 100% portrait on 2026-11-22, develop's first
  focus was **"Monday, November 23"**, a day still to come, in the middle of the calendar. The candidate's first focus
  was the page title **"Sunday, November 22"**, the top of the page. In landscape both builds start on the title.
- **One open limit on the candidate (not a #255 regression as far as QA could prove; see "Findings" F1):**
  - On Today at 200% landscape, swiping back past the top wraps into the right pane. From there TalkBack returns to
    the left pane's last visible item and loops among 7 stops. The calendar, the migraine row and "Logged today" are
    not reached again on that pass.
  - The first pass up and the first pass down are complete and in order on the candidate.
  - On develop, the same page loops inside the calendar itself and never reaches the top (2026-11-20 row).
- **9.1:** follow-up issue **#269** (S3, pre-existing) is filed for the pickers and Reminders' time dialog, with sizes
  and a walk measured by QA. #255 stays open; QA closes it after the merge retest.

What this does **not** prove is listed at the end. Rows QA could not run are marked NOT CHECKED, never as passes.

## Builds

| Build | Source | sha256 | On the device |
|---|---|---|---|
| candidate debug `c255` | QA worktree `migraine-app-qa-255`, detached at b86cadd2, `nice -n 19 ./gradlew --max-workers=2 :app:assembleDebug :app:assembleRelease :app:checkReleasePermissions`, BUILD SUCCESSFUL in 3 m 7 s, tree clean after | `91898ac8112878058e5d8cc00b8bdc09b95ddeed3e6242c71f5b43ac92f65931` (vc15 "1.3.3", debuggable) | `91898ac8…` read back in every row |
| candidate R8 `r8c255` | same build | `7567b7c411be154501be80edaee06cfc97cce3c2b461f0db13df6c1cded708d1` (vc15, not debuggable) | `7567b7c4…` |
| develop `dev224` | same worktree at 224dad3d (develop = v1.3.3 + its QA doc; `git diff --stat v1.3.3 224dad3d`: only `docs/qa/go-no-go-1.3.3.md`) `:app:assembleDebug` | `0caededa2ca2e1b481b3418f21cce08e0096a915ca78122fff4dda6f6a7c0467` | `0caededa…` |
| v1.3.1 `v131` (production) | `migrely-work/qa-rel132/apk/debug-f626c2d0.apk`, tree f626c2d0 = tag v1.3.1 (`git diff` empty) | `fde165dbc3a1c56b63cfd6b56955b9f631e4a6ca91da9220b790c3e003a0e012` (vc13) | `fde165db…` |

Task 8.1 names develop f70b6450 as the control. QA used **224dad3d** instead, as team-lead asked: it is newer and carries
#259 and 1.3.3, which the candidate merged in 2e6f5ad8.

**Permissions and manifest:**
- `checkReleasePermissions` passed in QA's build.
- `aapt2 dump permissions` on the candidate R8 is identical to the 1.3.3 R8 (`qa-rel133/apk/r8-d023ba76.apk`).
- INTERNET: 0.
- `git diff --stat 224dad3d b86cadd2` over manifests, `strings*.xml`, Room schemas and proguard files: empty.

## Device and method

- **Device:** emulator-5580 `Migrely_QA_API_36` (AVD name read back), API 36, TalkBack 16.0.0. TalkBack's log level was
  set to VERBOSE before the rows and read back.
- **Gestures:** one-finger swipes and touches sent through the emulator console. TalkBack does not hear `input`. The
  console mapping was calibrated per walk: one right swipe must be recognised as `GESTURE_SWIPE_RIGHT`.
- **Data:** a seeded past migraine (Severe, Nausea) and Ibuprofen. The data was re-seeded on each install.
- **Driver:** `migrely-work/qa-255/bin/walk255.py` and `pages255.py`. It is QA's own harness on qa-245b/qa-238. It does
  not use the engineer's `dev.py`.
- **Guard drivers:** `g255.py` runs QA's earlier drivers unchanged: `tbrow` (#238), `tb245` (#245 SW5, #239 V1) and
  `x259q` (#259).
- **Results:** `migrely-work/qa-255/rows.jsonl`, `acc/*/rows.jsonl` and `raw/` (TalkBack logs per walk).
- **Judging:**
  - A walk's stops are TalkBack's `TYPE_VIEW_ACCESSIBILITY_FOCUSED` events in order.
  - "Reached" means the end stop came up.
  - "Repeat" means a stop came back before the end.
  - "Bare" means a stop with only a state ("Not selected", "Selected").
  - A walk with no speech, or with the app out of the foreground, is VOID.
- **Load rule:** a row counts only if the 1-minute load was under 8 at its start and at its end. Rows outside it are
  listed as LOAD and are not counted.
- **Calendar dates:** the clock passed midnight during the run (2026-11-22 → 23). Calendar rows on 2026-11-20 used a
  root clock change. The clock was set back to the host time with `auto_time` 1, then `adb unroot` (`uid=2000(shell)`
  read back).

## 8.1 Negative controls and 8.2 the same walks on the candidate (Attack details)

| Row | Build | Load start → end | Keyboard-up walk (40 back) | After Back: back to Back | Then forward to "Delete migraine" |
|---|---|---|---|---|---|
| `v1-kb-360` | v1.3.1 | 4.8 → 2.3 | 38 stops, no repeat | **not reached**: 36 stops, 15 unique, 5 bare, a loop over impact radios and triggers | reached (14) |
| `d1-kb-360` | develop | 6.5 → 3.5 | **31 unique of 39, 5 bare**; Caffeine, Period, Weather, Smell skipped | **not reached**: a 13-stop loop ×3 ("Forehead, Not selected ×4, Left side, Not selected, Selected, Aura, Symptoms, Whole head, Around the eyes, Back of the head"), 18 bare | reached (36) |
| `c5-kb-360` | candidate | 4.5 → 3.1 | 39 stops, no repeat, none bare | **reached, 16 stops, each once** | **reached, 56 each once** |
| `c8-kb-360` | candidate | 5.8 → 7.3 | 39, none bare | reached, 17 each once | reached, 56 each once |
| `c1-kb-411` | candidate | 2.8 → 3.9 | 40, none bare | reached, 14 each once | reached, 55 each once |
| `r1-kb-360` | candidate R8 | 5.8 → 3.6 | 40, none bare | reached, 15 each once | reached, 56 each once |
| `c6-kb-360` | candidate | 7.8 → **8.6** | (LOAD, not counted) | reached, 15 | reached, 56 |
| `c1-kb-360` | candidate | 2.1 → 2.8 | 40 | **VOID for this table** (see F2) | |

| Row | Build | Load | Back from the end | Forward from Back |
|---|---|---|---|---|
| `v1-p100` | v1.3.1 | 2.3 → 1.8 | reached, **46 stops: 10 skipped** | reached, 59 stops, **3 repeats** |
| `d1-p100` | develop | 3.3 → 3.3 | reached, **49 stops: Symptoms section and "Possible triggers" skipped; 3 bare** at the top (pain chips under the bar) | reached, 58, 3 bare |
| `a2-p100`, `c1-p100`, `c5-p100` | candidate | 2.5–4.7 | reached, 56/56/57, each once, 0 bare | reached, 55/55/56, each once, 0 bare |
| `c2-pg-anim0` (Remove animations: 3 scales 0) | candidate | 2.8 → 4.1 | reached, 56, each once | reached, 55, each once |
| `r1-p100` | candidate R8 | 3.7 → 2.7 | reached, 57, each once | reached, 56, each once |
| `c1-l200-360` (200% landscape 360 dp, no keyboard) | candidate | 3.9 → 3.0 | reached, 56, each once | reached, 55, each once |

**Rotation and process death mid walk (candidate, 100% portrait):**
- **`c2-rot`:** 12 swipes back, then a turn to landscape. TalkBack started again on the first stop in view. The walk
  then reached Back with 53 stops, each once, and the end going forward (55).
- **`c2-death`:** 12 swipes back, HOME, then `run-as kill -9` (pid gone, read back), then the launcher intent. The app
  reopened on Attack details with focus on Back. It walked back to Back with 56 stops and forward to the end with 55,
  each once.

**Hardware Tab (TalkBack off, focused node from uiautomator) on the candidate:**
- **`c2-tab-p`:** 100% portrait. 45 presses from Back to the note, and every focused node was inside the screen (0 out
  of view).
- **`c2-tab-l`:** 200% landscape. 0 out of view, but the end load was 65.8 (another session's Gradle), so the row is
  not counted.
- Tab inside the note types a tab character, as on develop. It is not a defect.

**Chip cut at an edge, 100% portrait, TalkBack touch on the visible part:**
- **Bottom edge:** "Travel" with 21 and 15 px of 126 showing said **"Not selected. Travel"** (`c6-cut-bot`,
  `c9-cut-bot`).
- **Top edge:** "Nausea" with 12 and 24 px showing gave **no focus and no speech** (`c6-cut-top`, `c9-cut-top`). The
  touch lands 6 to 12 px under the top bar's edge, so QA cannot tell a name problem from a touch that missed.
- **Develop control (`d9-cut-*`):** the same at both edges (top, 26 px: silent; bottom, 14 px: "Not selected. Travel").
  So the device check **does not separate the builds**. On develop, too, a chip cut at the bottom edge said its name in
  this geometry.
- The "says only its state on develop" half of 8.2 is therefore covered only by the walks above. Develop's bare stops
  are the cut chips under the bar (3 in `d1-p100`, 18 in `d1-kb-360`), against 0 in every candidate walk.

**Known, intended change (not a defect):** TalkBack's outline is the 48 dp node on in-page icon buttons and the whole
pitch on History days. QA saw it in the screenshots and did not count it.

## 8.2 Other pages and dialogs (rest of 6.2), candidate

| Row | Page | Load | Result |
|---|---|---|---|
| `c3-pg-editkb-p` | Edit medication, name field tapped, keyboard up, 100% portrait | 7.6 → 5.7 | back: Edit medication → Back; forward: title, "Editing. Ibuprofen. Edit box. Name. 9/60", Kind, 2 radios, Type, 4 radios, dose "0/20", Save, then Gboard. 12 stops, each once, named |
| `c6-pg-editkb-l`, `c3-pg-editkb-l` | Edit medication, keyboard up, 200% landscape 360 dp | end 8.1 and 8.5 (LOAD) | same order, 12 each once. The counters "9/60" and "0/20" are not said in landscape. Develop says the same (`d2-pg-editkb-l`, load 3.8 → 6.1: same 12 stops, no counters). Pre-existing, not #255 |
| `c7-pg-am-l`, `c8-pg-am-l` | Attack mode (dark room), 200% landscape | 7.0 → 5.7, 7.2 → 5.5 | 10 and 13 stops each way, each once, chips named ("Not selected. Severe"). The 2 portrait rows ended at load 13.3 and 11.8 (LOAD) with the same order |
| `c7-pg-amlog-l` | Log medication from attack mode (dark room, medication saved), 200% landscape | 5.7 → 3.8 | 5 stops (Back, "Medication taken", "Taken at", "Log Ibuprofen", "Manage medications"), in order both ways. The forward walk then wraps to the top: TalkBack's wrap on a short page, not a loop |
| `c8-pg-msg` | Attack details with the Undo message "Pain level removed" showing, 100% portrait | 5.5 → 2.7 | back to Back: 26 stops, each once; forward: 50 stops, each once (stopped at the 50-swipe cap at "Neck pain", before the end) |
| `c8-pg-imp-p`, `c8-pg-imp-l` | Your data → Import → the "Import this backup?" dialog (a 5-migraine seed file), portrait and 200% landscape | 2.8 → 2.4, 2.4 → 5.4 | 5 stops (title, "It holds 5 migraines…", Cancel, Add, Replace), each once per round both ways. The dialog wraps |
| `c8-pg-onb-l` | Onboarding step 1, 200% landscape | 8.0 → 7.2 | back: 4 stops to "Step 1 of 4"; forward: "Skip this step". Portrait (`c8-pg-onb-p`) ended at 8.3 (LOAD) with 8 stops back, each once |
| `c9-pg-timedlg-l` | Reminders' time dialog, 200% landscape (residual, design D5) | 7.9 → 2.4 | as the engineer saw: back loops and never reaches Cancel or OK; forward skips 4–8 o'clock once and has a bare "Not selected". In #269 |
| `c5-pg-pick-p`, `c5-pg-pick-l` | Date-time picker and Reminders' time dialog sizes | 2.4 → 4.4, 4.4 → 3.5 | in #269. In landscape, nodes clipped by the dialog's own scroll show small sizes; those are not counted |

**Today (the page ac0a7695 changed):**
- **`c6-today-p`:** 100% portrait, candidate, 2026-11-22. Load 3.1 → 1.8. Forward from the title, the walk passed
  Settings, the sky card, Period, the migraine row, November and days 1–22, then **23–30 right after today**, then the
  legend and the three buttons. That is 46 stops, each once and in calendar order. After that, TalkBack wraps.
- **`c8-today-p`:** 2026-11-23. Load 2.5 → 2.3. Forward: the same first pass in order.
- **`t1-today-l`:** 200% landscape, candidate, **2026-11-20** (the case ac0a7695 fixed). Load 7.5 → 2.4.
  - Forward: Settings … 1–20, **21–30**, legend, right pane, each once.
  - Back from Nov 14: up to Settings and the title, each once.
- **`t2-today-l`:** develop, 2026-11-20. Load 4.7 → 2.2.
  - Forward: Settings, sky card, Period, then **straight to Nov 8**. "Logged today", the migraine row, November and
    days 1–7 were skipped. Days 21–30 come **after the action buttons**.
  - Back: the walk **loops inside the calendar** (15 → 1 → November → migraine row → 15 …) and never reaches Settings.
  - This is the fault ac0a7695 removes. The candidate is clean on the same rows.
- **`c8-today-l`:** 2026-11-23. Load 6.2 → 2.1. The same as `t1-today-l`.
- **`c6-today-l`**, **`d1-today-l`**, **`t*-today-p`**, **`t*-hist-*`:** VOID by harness. The console mapping was not
  calibrated in those rows, or History could not be reached in landscape. They are not counted.

**Today, first TalkBack focus (cr-255's question).** TalkBack was on, then the app was opened with `am start`, and the
first focus event was taken.
- **Candidate:** the page title in all 4 rows. Portrait and landscape: "Sunday, November 22" on 2026-11-22
  (`c6-first-*`) and "Monday, November 23" on 2026-11-23 (`c8-first-*`).
- **Develop on 2026-11-22:**
  - Landscape: the title (`d1-first-l`).
  - **Portrait: "Monday, November 23", a day still to come in the calendar** (`d1-first-p`). It is not the title.
- **So the first focus changed in portrait, from a calendar cell to the top of the page.** That is the intended order.
  In landscape it is unchanged.
- tasks.md says the 200% landscape *previous* walk "starts on Sunday 15 now". That is TalkBack's first stop when the
  walk begins with the page scrolled to the calendar. It is not the window's first focus.
- **Develop on 2026-11-23:** portrait **"Tuesday, November 24"** (a day still to come), landscape "Monday,
  November 23" (the title) (`d9-first-*`). Develop's portrait first focus was off the title in 2 of 2 days; the
  candidate's was on it in 2 of 2.

**Touch, no TalkBack (the D2 touch-area change):**
- **History day gap.** A tap exactly on the border between Nov 9 and Nov 10 opened or selected Nov 10 on the candidate
  at 411 dp (`c6-pg-ring`, new labels "Tuesday, November 10 / No migraine logged") and at 360 dp (`c9-pg-ring360`,
  2,632 px of the two cells changed).
- **Develop:** the same tap did nothing at 411 dp (`d1-pg-ring`, 0 px changed) and acted at 360 dp.
- **The ring around an in-page icon button:** VOID. "Previous month" was inert in this data: a tap at its centre did
  not change the month either (`c9-pg-ring`, control row). So the 4 dp ring tap could not be judged on the device.
  It rests on `MigrelyIconButtonTest` and the engineer's 6.4 pressed-pixel rows.
- **Touch targets on the device (`c9-pg-aud-p`, `c9-pg-aud-l`, candidate):** every clickable node on Attack details
  was dragged through from top to end, skipping nodes clipped at the scroll edge. **0 nodes under 47.5 dp** at 100%
  portrait and at 200% landscape 360 dp. This agrees with the engineer's audit (smallest 48 dp).
  The earlier rows `c6-pg-aud-*` counted clipped nodes and are not used.

## 8.3 Guards on the candidate

| Guard | Driver and row | Load | Result | Rule |
|---|---|---|---|---|
| #238 KR1/KR3: Attack details, TalkBack on, keyboard closed near the page end | `tbrow` `g1-details-0..2` | 4.1, 3.2, 3.6 | **3/3 valid: 0 px page shift (153/153/140 votes), 0 `TYPE_VIEW_SCROLLED`, only "keyboard hidden" said, the double tap reopens the keyboard in the note, database unchanged** | as the 1.3.1 record |
| #238 KR on Log medication | `g1-log-0/1` | | VOID: the harness needs a first-use Log medication (a name field); the data had a saved medication | NOT CHECKED here; #259 rows below cover the field on Edit medication |
| #238 KR on Delete all data | `g1-del2-0..` | 3.0 | **invalid by the driver's own rule**: TalkBack's start focus was Back, not the field ("Editing" not said), so the row's double tap pressed Back.See F0. | |
| #245 SW5/K2: Attack details 360 dp, bar aside, close by Back | `tb245` `g2-k2back-360` | 3.1 | **the field moved by exactly the bar's 194 px, 0 scroll events, nothing said** | as the #245 record |
| #245 SW9: walk back after typing | `g2-sw9-360` (8 swipes) and the `*-kb-360` rows above (40) | | while aside, the bar is out of the tree (none of the 8 stops is Back or the title); after closing, Back reached in every valid candidate row | |
| #239 V1: Edit medication, Name empty, Save by TalkBack double tap | `g2-v1-360` | 3.9 | **"Enter a name." said at 0.73 s; no "Invalid input"** | within 3 s |
| #259 core: TalkBack double tap opens the field, 200% landscape 360 dp | `x259q` `g3-tb-det-360` ×3, `tb-name`, `tb-dose`, `tb-del` | 2.6–4.8 | **6/6 PASS: 0 focus moves after the double tap, 0 database writes, line 112/112** | the #259 record's rules |
| #259 on R8 | `r2-tb-det-360` ×2 (root reads) | 2.1–2.7 | **2/2 PASS** | |
| #259 on Log medication (normal and dark room) | `g3-tb-log-360` VOID (medication saved); rerun with no medication `cl-tb-log-360`, `cl-tb-dark-360` | 5.2→4.0, 4.0→3.8 | **2/2 PASS**: 0 focus moves, 0 writes, line 112/112 | |

## 8.4 Maestro (flows that select chips or stops by text)

`qa/maestro.sh` (QA's lock, baseline and restore) with `QA_APK`, one target per call, 40-minute limit. The same targets
were run on the candidate and on develop. **Every flow passed on both builds.**

| Target | Candidate | Develop |
|---|---|---|
| `attack-details/` | 2/2 + 1/1 (a11y-timeout pass) | same |
| `custom-tags/` | 1/1 | 1/1 |
| `attack-logging/` | 7/7 + 3/3 | same |
| `past-attacks/` | 4/4 | 4/4 |
| `a11y-tour/custom-tag-dialog.yaml` | 1/1 | 1/1 |
| `day-markers/` (`selected:`/`checked:` next to text) | 5/5 | 5/5 |
| `attack-mode/` | 2/2 | 2/2 |
| `attack-history/add-for-empty-day.yaml` | 1/1 | 1/1 |
| `in-app-messages/` | 4/4 | 4/4 |
| `subscription/paywall-and-purchase.yaml` | 1/1 | 1/1 |
| `device/details-setup.yaml` → HOME, `run-as kill -9` (pid gone) → launcher → `device/details-restored.yaml` (`containsChild: "Nausea"`, `checked: true`) | pass | pass |

No flow needed a change. Logs: `migrely-work/qa-255/logs/m/`.

## 9.1 Follow-up issue

**#269** (S3, `qa`, `accessibility`, `pre-existing`) covers:
- the Material date picker, the date-time picker and Reminders' time dialog;
- their measured sizes: AM/PM 48 × 40 dp as exposed; the toggle, arrows and year button 40 dp own size per the audit;
- QA's own walk of the time dialog, where the previous walk loops, Cancel and OK are never reached, and a bare stop
  appears;
- `Refs #255`.

#255 stays open until QA's retest after the merge.

## Findings

- **F0. Delete all data: when TalkBack is turned on while typing, TalkBack starts on Back, not on the field.**
  - Rows `g1-del2-0/1` and `cg-del2-0..2` (tbrow, 200% portrait), 5 of 5. The driver marks such a row invalid. Its
    double tap then pressed Back, and the page closed with nothing written.
  - #259's own rows on the same screen pass: the field is opened by a TalkBack double tap with TalkBack already on
    (`g3-tb-del-360`).
  - **Develop does the same**: TalkBack starts on Back in 3 of 3 rows (`dg-del2-0..2`). Those rows ran at load 26 to 53,
    so they are not counted, but the start focus does not depend on timing. This is pre-existing, not a #255 change,
    and it is not filed.
- **F1. Today at 200% landscape: swiping back past the top loops among 7 stops on the candidate. Not filed.**
  - **What happens.** Rows `t1-today-l` (2026-11-20) and `c8-today-l` (2026-11-23).
    - The first pass up is complete and in order.
    - Past the title, TalkBack wraps to the right pane: Log medication → Add a past migraine → Migraine started.
    - From "Migraine started" it goes to the left pane's last *visible* item, "Period today".
    - Then it goes up again: Today card → Settings → title → right pane … The calendar, "Logged today" and the migraine
      row are not visited on later rounds.
    - Going forward, the wrap returns to the first visible day (Nov 15), not to the title.
  - **Why it is not filed.** It needs a pass over the page's first item or a start in the right pane. Develop on the
    same date loops earlier and worse: inside the calendar, never reaching the top (`t2-today-l`). The cause looks like
    TalkBack's handling of two separate scrolling panes when it wraps. On 2026-11-23, develop again looped inside the calendar going back (`d9-today-l`) and never reached the top, so whether develop has the same loop after the wrap cannot be shown.
  - **Who decides.** team-lead decides whether ux wants a follow-up. It is not a release blocker under the S1/S2 rule.
- **F2. `c1-kb-360` (first row of the night): after the 40-swipe keyboard walk, Back left Attack details for Today.**
  - The driver did not yet record whether the keyboard was still up before Back. If it had closed during the walk,
    Back navigated. The walks recorded after that are Today's, which is how F1 was first seen.
  - From `c5-kb-360` on, the driver checks the keyboard before Back. In 5 later rows it was still up every time
    (`ime_before_back` true), and Back closed it.
  - This is not counted as a pass or a fail.
- **F3. Edit medication at 200% landscape with the keyboard up does not say the counters.** "9/60" and "0/20" are
  missing. It is the same on develop, so it is pre-existing and out of the diff. Noted only.
- **Known limits seen:**
  - #264: rotation with the keyboard up was not run.
  - #262: not touched.

## Not checked, and what this record does not prove

- **API 26 (emulator-5582): NOT CHECKED.** Task 8.2 asks for API 26 at 360 and 411 dp. The API 26 image has no TalkBack.
  The Tab, audit and Maestro chain for it (`bin/chain26.sh`) was written but not run, inside the time QA had. The
  verdict rests on API 36 only.
- **RTL: NOT CHECKED.** Migrely ships English only. A per-app `ar`/`iw` locale does not mirror it (QA note from T53),
  and `debug.force_rtl` needs root plus an app restart. QA did not try it.
- **Switch Access scan and scroll: NOT CHECKED.** It uses the same scroll action as TalkBack (design D3), so its half
  step is covered only through the TalkBack walks and the engineer's `PageScrollTest`.
- **Not reached or VOID:**
  - History walks at 100% and 200% (harness).
  - The Your data restore dialog: Replace imported directly in this data and showed no second dialog.
  - Attack mode's History.
  - The top-edge chip cut (touch missed or nothing focusable; see above).
  - The in-page icon-button ring tap (the target was inert).
  - #238 KR on Log medication.
  - The landscape Tab row (load).
- **Emulator only.** No real phone, no real finger gestures (console swipes), TalkBack 16.0 only, Gboard only.
- **Counts are small.**
  - Issue-step walks: 3 valid debug rows and 1 R8 row.
  - Walks with no keyboard: 3 at 100% portrait, 1 with Remove animations, 1 at 200% landscape and 1 on R8. Add the
    rotation and process-death rows.
  - 0 failures in 4 bounds a failure rate only below about 53% (95%). Across all 12 counted Attack-details walks on the
    candidate (0 failures), it is below about 22%.
  - The controls failed in 4 of 4 rows.
- **QA did not re-run** the Gradle gate (team-lead reports 5e39100d: exit 0, 3,660 tests, 0 failed), Android Lint,
  instrumented tests, monkey, or the upgrade rows. The owner rule of 2026-11-09 limits this record to what the diff
  reaches. The nightly develop regression carries the rest.
- **Engineer rows not re-run by QA** are evidence only, not QA proof:
  - Settings, Warning signs, About, Privacy, Licences, Your data, Reminders, Delete all data, History, Paywall,
    Medications and Add past attack walks (6.2);
  - the pressed-pixel test (6.4);
  - the date-picker jump.

## Device restore

Read back at the end (2026-11-23 10:47):
- font 1.0; `user_rotation` 0 and auto-rotate 1; `wm size` 1080×2400 and density 420;
- TalkBack off (`enabled_accessibility_services` null, `accessibility_enabled` 0); TalkBack log level ERROR, read back;
- accessibility timeouts null; animator scales as before each run (`settings-after` rows: `changed: {}`);
- `auto_time` 1 and the clock on the host time; adbd not root (`id -u` 2000).

emulator-5580 was turned off with `emu kill`. emulator-5582 was not started.

**Stopped early:** the develop tail of `d9` (picker sizes, audit and Edit medication on develop). The host load from
other sessions was 18 to 53, so those rows would not have counted. Develop's Edit medication rows exist from `d2`. The
picker sizes are the same Material code, measured on the candidate.

## Housekeeping (qa-255)

- **Removed after checks:** worktrees `migraine-app-qa-259` (qa/259-acceptance 9967de02), `-qa-rel132` (c1c0afab),
  `-qa-rel132-doc` (qa/go-no-go-1.3.2 99896fff) and `-qa-rel133` (qa/go-no-go-1.3.3 b3ccf6f9). Each commit was in
  origin/develop (three also in origin/main), and each had 0 uncommitted files.
- **Not folded:** `migrely-work/phone-131/result.md` was **not** folded into the 1.3.1 record. It is incomplete:
  - P1, P2b and P2c are NOT RUN;
  - "Time to take action" was still 60000 on the owner's phone at 10:29 and is not read back as restored.
