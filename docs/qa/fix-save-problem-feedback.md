# QA record: fix-save-problem-feedback (#239, T75, BRD D191, EC-UX-49, EC-UX-50, EC-UX-66 to EC-UX-71)

## Verdict

**QA (qa-239), 2026-11-18: GO with recorded limits** for merging `bugfix/239-save-error-feedback` @
**1ce3eab9c43988f6afd57965e5b2e9a6e78d3429** into `develop` as 1.3.2 content.

- **The issue is fixed, and the control shows it.** On Edit medication with TalkBack, an empty Name and Save now give
  exactly one "Enter a name." about 0.6 s after the press. v1.3.1 says nothing in 5 s (V1). With the keyboard closed,
  every one of 5 presses is heard on the candidate and none on v1.3.1 (V3T).
- Name and its error come into view after Save on every screen and size where they fit. v1.3.1 leaves Name off screen:
  V2, V3x, V9, V12 portrait, V13.
- With the cursor in Usual dose, the next letter goes to Name. On v1.3.1 it goes into the dose (V4).
- An older storage message leaves when the Name error comes (V10).
- The dark room stays dark (V11). Typing after the error is quiet (V7); its mutation fails, so the row can fail.
- Rows that could not be reached, or whose control cannot fail, say so below and are not counted as passes.
- **Recorded limits** (none is S1 or S2; details under "Limits and observations"):
  1. **V5 on Edit medication with TalkBack** is not proven. On the emulator, Gboard announces "Showing English (US)
     (QWERTY)" when focus moves to Name, and the field's reading is cut off. This happens on v1.3.1 too. With TalkBack
     on, hardware Tab from Usual dose went to Back and skipped Save, so the emulator cannot carry the row.
  2. **V6-timed:** a second TalkBack press 0.8 s after the first is heard in 3 of 4 runs. In the fourth, the error is
     heard once for two presses.
  3. **V12 in landscape at 200% with the keyboard up:** only the input line fits (163 px at 411 dp, 87 px at 360 dp).
     The error line is behind the keyboard, as ux U1 orders. This is #245's window capacity, not this change.
  4. **V16 right to left was not reachable.** The app stays left to right under Hebrew and Arabic per-app languages,
     on this image, whatever the build. The light-theme half passes at 6.32:1.
  5. V15 (Add past migraine) was not run.
- **Review asks:**
  - (a) **"Migrely" was said on 0 of 5 presses on v1.3.1 and on 0 of 5 on the candidate** (V3T, keyboard closed). The
    engineer's "Migrely" did not reproduce here.
  - (b) **The keyboard-closing race passes 8 of 8.** Save was touched 0.25 s and 3 s after the keyboard started closing,
    with the cursor in Usual dose (checked). Focus stays in Usual dose, Name comes into view, and the keyboard does not
    reopen.

## Builds and devices

| Build | Source | sha256 (APK) | Read back on device |
|---|---|---|---|
| candidate | detached worktree `migraine-app-qa-239` at 1ce3eab9, clean tree, `:app:assembleDebug` at load < 8 | `31439e322236b5f48956f1e7c9ee80cbf58b910e4f282638ef1d08b4d5e7a2f4` | `31439e322236b5f4` |
| control | v1.3.1 code: QA's debug APK of `develop` 76c38c38 (`git diff v1.3.1 76c38c38 -- features designsystem app` is empty) | `fde165dbc3a1c56b63cfd6b56955b9f631e4a6ca91da9220b790c3e003a0e012` | `fde165dbc3a1c56b` |
| M-V7a | candidate + `liveRegion = Polite` on the field while it has an error | `0b2be0891cd65f798ac82e4b4eb1e2f5b9afea139e7b4fda2ddee7225c889ffe` | `0b2be0891cd65f79` |
| M-V7b | candidate + `liveRegion = Polite` on the field always | `3fc17c502b0d2ddf70953958d110a5686f2b6cb6f3c9b973ea7ab35719121dc2` | `3fc17c502b0d2ddf` |

- Mutations were local, built in `migraine-app-qa-239m` and reverted after each build (`git status` clean). Diffs:
  `migrely-work/qa-239/logs/m-v7.diff`, `m-v7b.diff`.
- Devices:
  - emulator-5580: API 36, TalkBack 16 with its log level VERBOSE during the rows and ERROR afterwards, Gboard, 1080x2400
    at 420 dpi, 100% portrait unless a row says otherwise.
  - emulator-5582: API 26, AOSP keyboard, no TalkBack installed.
- **Speech:** TalkBack's "Speaking fragment" lines after a device-time mark. Each speech row lists the counted
  utterances with their times.
  - "Enter a name." on its own is the app's announcement.
  - "Error: Enter a name." inside "Edit box. Name …" is the field's own reading.
  - A reading that follows a `TYPE_VIEW_ACCESSIBILITY_FOCUSED` move by TalkBack itself is TalkBack-caused (ux §5
    "Counting speech").
- **Presses with TalkBack** were a touch through the emulator's touch device (to focus), then a double tap. V17 pressed
  Save with `input tap`, the engineer's method, so that TalkBack's focus stays on Name.
- **Input:**
  - Name was cleared and typed by touching Gboard's keys, so there are no key events. In landscape, and on API 26,
    Name was cleared with key events; the race rows K and F show both ways behave the same.
  - The keyboard was closed with Gboard's hide key.
  - "Keyboard up" rows record the IME window's top before the press. Rows run after Gboard went compact (the engineer's
    warning) were discarded, and the emulator was cold-booted before every keyboard-up batch.
- **"In view" is judged two ways.**
  - Before the press, the only field in the dump is Usual dose (Name is off the top), so Name is out of view.
  - After the press, Name's node (which includes its supporting line) is whole inside the page view, from the top bar to
    the keyboard: full height, 236 px at 100% and 351 px at 200%. It is checked on a screenshot where the dump cannot see
    the error line, because its visible text has no semantics by design.

## Rows (API 36)

| Row | Candidate 1ce3eab9 | v1.3.1 control |
|---|---|---|
| V1 Silent Save | Keyboard up (IME top 1517). Within 3 s exactly **one** utterance with the error: "Enter a name." at 0.64 s. TalkBack then moved its own focus to "Kind" (TalkBack-caused, no error). Record unchanged | **0** utterances in 5 s; Name stays off screen |
| V2 In view (Edit, 100%) | Before: Name off the top. 1 s after: Name and "Enter a name." whole at y 296–532, keyboard still open, input focus in Name | (see V12 portrait: Name stays off screen) |
| V2 In view (first use, 100%) | Name already in view before the press, so this cell **cannot fail**. Not counted | – |
| V3 Keyboard closed | 100%: the form fits, Name in view before the press, so it **cannot fail**. V3x at 200%: Name clipped at the top (36 px shown) before; after, whole (296–647), keyboard stays closed, focus unchanged | V3x: Name stays clipped (233–269) |
| V3T Every press heard (keyboard closed, 5 presses) | **5 of 5 heard.** Press 1: the field's reading "Error: Enter a name." at 0.34 s, then the announcement at 2.14 s. Presses 2 to 5: "Enter a name." at 0.37–0.38 s. "Migrely" on 0 of 5 | **0 of 5 heard**; "Migrely" on 0 of 5 |
| V4 Cursor in Dose (cursor checked in Usual dose) | After Save: input focus in Name, keyboard open throughout (1517), "a" lands in Name, dose stays empty | "a" lands in **Usual dose**, focus stays there |
| V5 Hardware keyboard, first use | Gboard collapsed after the first hardware key: `mInputShown=false`, IME at the screen bottom. Tabs (hardware, logged by TalkBack with scan code 15) to "Add and log. Button", then Enter → focus on Name, in view. TalkBack: "Error: Enter a name." at 0.19 s (the field read when the app moved focus), then TalkBack's own re-reading; never "Invalid input" | Enter → focus stays parked, silent ("Press Alt + ENTER to activate" only) |
| V5 Hardware keyboard, Edit | Without TalkBack: 8 hardware Tabs to Save, Enter → focus on Name, in view. With TalkBack: **not proven** (see Limits 1). The focus description is "Name. Error: Enter a name." | Same flush by Gboard; the focus description is "Name. Enter a name.. **Error: Invalid input**" |
| V6 Twice | Covered by V3T presses 2 to 5 (keyboard closed): each press gives a new announcement. With the keyboard up, Save is behind the keyboard after the first scroll, so a second press needs the keyboard closed first | Silent (V3T) |
| V6-timed (inside 1 s) | TalkBack double taps 0.8 s apart: 2 announcements in 3 runs (0.44 + 1.51 s); 1 in 1 run. At 1.5 s: 2. With `input tap` presses 0.8 or 1.5 s apart: 1 (the second arrival is merged); 3 s apart: 2 | – |
| V7 Typing quiet | After V1, "ib" by Gboard keys: only "I", "b" and a Gboard suggestion word. No "1/60", no field re-reading. The error leaves silently | M-V7a (live region only while in error): same as the candidate, **cannot fail**, not counted. M-V7b (live region always): "Editing.", "Edit box.", "Name" re-read while typing: **fails, as it must** |
| V8 Swipe walk | The walk back from Save reaches Name as **one** stop: "Editing. Edit box. Name. Error: Enter a name.". No extra "Enter a name." stop (the walk began more than 1 s after the press); never "Invalid input" | The walk went into Gboard's keys and did not reach Name, so not recorded (see V5 control for v1.3.1's reading) |
| V9 Duplicate | Add medication, "ibuprofen": Name brought into view with "You already have Ibuprofen." (shot), keyboard open, record unchanged | 200%: no scroll, Name off screen |
| V10 Message first | Storage full, Save: "Couldn't save…" shows (valid, 0 KB free). Free, clear Name, Save: storage message gone, Name in view and focused, no message over it. Recording: the message's last frame is 1.13 s; Name's error enters view at 1.29 s, as the scroll brings it in. No frame has both | "Couldn't save…" **stays**, Name not brought into view |
| V11 Dark room (first use from attack mode) | One "Enter a name." at 0.43 s. Error colour (230,164,164), luminance 0.461, below `#BCC8D3`'s 0.567. The brightest pixel outside the field is `#BCC8D3` itself, on a background of (8,9,12). One frame after the result, not a recording | **0** utterances |
| V12 200% | Portrait Edit and first use: Name off screen before, whole (296–647) after, keyboard open. Landscape 411 dp (Edit and first use) and 360 dp (first use): the input line is in view (163 px, or 87 px), and the error line is behind the keyboard (Limits 3). Landscape 360 dp Edit: Save could not be brought into the 87 px space by drags, **not run** | Portrait Edit and first use: Name stays off screen |
| V13 Remove animations | Animator scales 0: as V2, Name whole at 296–532, keyboard open. "Scroll in one step" not measured frame by frame | – |
| V14 Pending | Two double taps 0.46 s apart, the second before the first result: one announcement (0.70 s), so one result | – |
| V15 Add past migraine | **Not run** | – |
| V16 RTL, light theme | Light theme: error line under the field, (158,52,52) on (245,243,238) = **6.32:1**. RTL: **not reachable** (Limits 4) | – |
| V17 Swipe within 1 s | Swipe 0.05 s after Save, 3 of 3: TalkBack lands on the announcement node and says "Enter a name." twice (the accepted D191 (8) residual), then returns to Name. At 1.58 s, 3 of 3: the swipe goes past to "Edit medication". Never "Invalid input" | 3 of 3: no announcement node; the swipe goes to "Edit medication" |
| (b) Closing race | Cursor in Usual dose (checked), keyboard closed by the hide key, Save touched 0.25 s and 3 s later, twice each on Add and twice each on Edit: focus stays in Usual dose, Name brought into view (296), keyboard not reopened, 8 of 8 | – |

Earlier race rows that seemed to move focus to Name (`cA-R*`, `cA2-*`, `cA4-*`) never had the cursor in Usual dose: the
dose field was behind the keyboard and my tap hit Name. They are void and kept only as a record of that harness error.

## API 26 (emulator-5582, AOSP keyboard, no TalkBack)

This boot came up with `show_ime_with_hard_keyboard=0` (the first API 26 boot today had 1). So the AOSP keyboard drew
no view in these rows: V2 here is the hardware-keyboard state, not a soft keyboard taking space.

| Row | Candidate | v1.3.1 control |
|---|---|---|
| V1 | Not reachable: the API 26 image has no TalkBack | – |
| V2 (200% portrait) | Name clipped at the top before; after Save, whole at 231–582 and focused | Name stays clipped (168–206) |
| V3x (keyboard closed, 200%) | The same: Name brought into view, keyboard stays closed | Name stays clipped |
| V5 keyboard half, Edit | Literal `mInputShown=false` (AOSP, no view). 8 hardware Tabs to Save, Enter → focus on Name, in view | The same (focus already moves in keyboard mode on v1.3.1, D181 (6)), so this cell **cannot fail** |
| V5 keyboard half, first use | `mInputShown=false`; 5 Tabs to "Add and log", Enter → focus on Name, in view | Focus stays parked, not on Name |
| V3T non-TalkBack part | = V3x above | = V3x above |

## Limits and observations

1. **V5 on Edit with TalkBack.** When the app moves focus to Name, Gboard announces "Showing English (US) (QWERTY)" in
   the same instant, and TalkBack's reading of Name is cut off within 5 s, on both builds (2 of 2 candidate, 1 of 1
   control). With TalkBack on, the hardware Tab after Usual dose went to Back, not Save, on both builds. The console has
   no working Enter, so Enter was `input keyevent --source keyboard` (a virtual key). A real Bluetooth keyboard and
   another keyboard app may behave differently. V5 on the first-use form passes with TalkBack.
2. **V6-timed.** One of four TalkBack runs (`c8-V6tr`) heard the error once for two presses 0.81 s apart, the first
   announcement still being spoken. The other three heard it twice. Not filed: the person hears the error after their
   second press as well. It is recorded for the D191 (8) residual.
3. **Landscape 200% with the keyboard up.** Only the input line fits; the error text is behind the keyboard until it
   closes. This is the window capacity of #245 (open), not this change.
4. **RTL.** `cmd locale set-app-locales com.migrely.app --locales he` (and `ar`) leaves the app left to right: Back at
   x 43–106. Not this diff; the RTL half of V16 needs another route, such as a system language.
5. **TalkBack's own focus moves** after a scroll ("Kind" in V1, Name in V6) are TalkBack-caused and land on no control
   that writes data.

## What this does not prove

- One emulator image per API level; Gboard and AOSP only; no real phone and no Bluetooth keyboard.
- Not run: V15; V12 Edit at 360 dp landscape; V5 Edit with TalkBack (Limits 1); V16 RTL.
- V11 and V13 judge single frames, not every frame.
- **Void and rerun:**
  - Runs `c1` to `c3` raced with orphaned harness processes of mine (uiautomator "Killed" 8 of 10), and `c6` ran with
    Gboard compact. All are void; the counted rows come from `c4`, `c5` (V8), `c7` to `c13`, `cA3` and `cA5`.
  - The control rows are `k1` and `k2`; the mutants are `m1` and `m2`.

## Restore (task 7.2)

- emulator-5580, after the last row: settings snapshot unchanged against the start (`changed: {}`).
  - TalkBack off, with its log level back to ERROR.
  - Font 1.0, auto-rotate 1, rotation 0, `wm size` 1080x2400.
  - The app's language reset to none, the app theme back to Dark, app data cleared, /data 4,250,612 KB free, emulator off.
  - The mutation worktree was reverted, and the two build worktrees were removed when clean.
- emulator-5582, read back: `show_ime_with_hard_keyboard` 0 (as found at this boot; the row restores what it found),
  font 1.0, auto-rotate 1, rotation 0, `wm size` 1080x2400, /data 5,940,556 KB free, app data cleared, emulator off.
- During the work, a harness run I stopped had left the font at 2.0, auto-rotate off, the app language Hebrew and the
  theme Light. Each was restored and read back before the next row.

## Evidence

`migrely-work/qa-239/`:
- `rows.jsonl`: one JSON line per row with the APK hash read back.
- `bin/h239.py` (harness) and `bin/s239.py` (summary).
- `shots/`, `raw/*.mp4`, `raw/taborder-edit.json`, `probe.jsonl` (V5 method), `logs/`.
