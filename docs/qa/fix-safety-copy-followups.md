# QA record: `fix-safety-copy-followups` (#200, S3, D173), task 9

## Decision

**QA's verdict, 2026-11-12 at 11:05: PASS for `bugfix/200-safety-copy` at b10c8781** (app code 623df09b; 43825a59 and b10c8781
change only `website/`). Nothing new was filed against this change.

- The 10/10 card shows the new title and sentence word for word, and no "worst" or "emergency services", on API 36 and API 26,
  at 100% and 200%, in portrait and landscape, when the 10 saves and when the disk is full.
- Settings › Warning signs shows item 8 with its bracket, keeps its 16 texts in D171's order, and keeps 18 TalkBack stops.
- The Maestro flow that blocked the merge is fixed and can fail: it fails on develop and the old flow fails on this branch.
- **Negative control:** develop 3d098049 shows the old card words and the old item 8 on the same screens.

Scope follows the owner's rule of 2026-11-09: only what the diff reaches. The product diff is 2 string files and 1 comment line
in `AttackPainSection.kt`; no logic, layout, data, permission or Gradle change (tasks 1.1, 2.3, 4.1, 5.1).

## Builds

Built in detached QA worktrees with `--no-daemon`, no `keystore.properties`, so R8 is signed with the debug key (D102).
`aapt2 dump resources` confirms the strings in each APK before use.

| APK | Commit | sha256 | Strings |
|---|---|---|---|
| `new-debug.apk` | b10c8781 | 96b87815cdd152d61c4791640980e65814fb7b633f23c80df94e98c0c2bcc15b | new title, sentence and item 8 |
| `new-r8.apk` (not debuggable) | b10c8781 | ce355538618d5751287824fee10b2ab3e2fe960096922a1872f61f32afd6ab3d | new |
| `base-debug.apk` (control) | 3d098049 | 1455aad531ac932c6fc75e3eea0b6e0716bc11c00c5ff30aa9e4d69cce27f448 | "Sudden, worst-ever pain?", no "brown or black" |

## Maestro (task 9.1)

`.maestro/app-settings/sections-and-pages.yaml` now scrolls to item 8 as
`A rash that doesn.t fade when you press a glass against it \\(this may be harder to see on brown or black skin\\)` and then
asserts that the item without its note is not visible. `grep -rnE 'worst|emergency services|press a glass|brown or black'
.maestro qa` finds only these lines and the older D171 check for "Sudden pain that is the worst of your life". The a11y tour state
`settings-warning-signs.yaml` only opens the page, so it needs no change.

| Run | Build | Result | What it shows |
|---|---|---|---|
| New flow, 3 times | candidate debug, API 36 | 3 of 3 pass | |
| New flow | candidate R8, API 36 | pass | |
| Old flow (b10c8781's version) | candidate debug | **fails** at the rash item | the merge blocker was real; Maestro matches the whole text |
| Item 8 with unescaped brackets | candidate debug | **fails** | the escape is needed |
| New flow | develop 3d098049 | **fails** at the rash item | the flow can fail |
| "Old item 8 not visible" alone | develop 3d098049 | **fails** ("is not visible" is false) | the new `assertNotVisible` can fail |
| Old flow | develop 3d098049 | passes | the control itself is sound |

## Rows (tasks 6.1 and 6.2, starred)

Harness `migrely-work/qa-200/q200.py`. Each row starts from `pm clear`, skips onboarding, starts a migraine, opens Details, scrolls
until Pain's "Add pain level" is near the top (the card's place is then above the viewport, D167), and adds a 10. Dark theme. A
full disk is under 64 KB free as shell (API 36) or at the 16 MB root floor (API 26), and the row counts only if "Couldn't save.
Your phone's storage is full." shows. "Got it in view" means the whole button is inside the viewport.

| Row | Setup | API, build | Result |
|---|---|---|---|
| C1 | 100%, 411 dp, saved | 36 debug; 26 R8 | pass: card scrolled into view, title 1 line, exact sentence, Got it in view y |
| C2 | 200%, 411 dp, full | 36 debug; 26 R8 | pass: message 328-517 (API 26: 263-452), card title from 623 (558), nothing above the message's bottom, Got it in view y |
| C3 | 200%, 320 dp (`wm density 540`), full | 36 debug | pass: message 385-763, content from 966, title from 1061 on 3 lines; Got it in view n (45 of 135 px); one scroll: Got it whole at 1891-2026, message unchanged |
| C5 | 200%, landscape, full | 36 debug | pass: message 263-452, title from 558 on 1 line, whole sentence visible; Got it in view n (70 of 105 px); one scroll: whole |
| C7 | TalkBack, 100%, 411 dp, saved | 36 debug | pass: the first utterance after Add is "Sudden, extremely bad pain?. Call your local emergency number now if this is extremely bad pain that came on suddenly.. Got it. Button"; then "Migrely", "Back. Button" as the sheet closes |
| W1 | 100%, 411 dp | 36 debug; 26 R8 | pass: 16 texts in order, item 8 exact with its bracket, no "numb" word |
| W2 | 200%, 320 dp | 36 debug | pass: order as W1; item 8 wraps to 7 lines (981 px), whole in the viewport, ")" visible |
| W4 | TalkBack, 100%, 411 dp | 36 debug | pass: right swipes speak "Warning signs. Heading" and the 16 texts in D171's order; after the end the wrap reaches "Back. Button": 18 stops. Item 8 is one stop, spoken in full; the logged text keeps "(" and ")" |
| NC1, NC2, NW1 | C1, C2, W1 | develop debug, 36 (NC1, NW1 also on 26) | old title and sentence, old item 8, no new words |

A11y audit `qa/a11y_audit.sh settings-warning-signs` (candidate debug, API 36): 0 failures in its 3 states.
Smoke `qa/maestro.sh --tags smoke` on the R8 APK (API 36): 17 of 17 (14 + 3).

## What this does not prove

- **Clinical truth.** The words are checked against the medical-reviewer's PASS and the spec, not by a clinician (T31 is open;
  nothing may say "medically reviewed").
- **Comprehension.** Nobody in pain read the card. Whether "Sudden, extremely bad pain?" reads as a condition and not as every 10
  is a design judgement (D173 (1)), not measured.
- **Speech.** The emulators run without audio. W4 shows what TalkBack sends to speech, with the brackets kept; how a voice reads
  them was not heard. "Heading" on the card's title is not spoken at default verbosity; the role is covered by
  `SaveMessageTest`, not here.
- **Not run:** C4, C6, C9, C10, W3 and W5 (not starred; the engineer ran C4 to C10 and W3); TalkBack on API 26; landscape and
  320 dp on API 26; the full regression and monkey (no logic or layout changed; the nightly develop run covers them).
- **Upgrade:** not run. Only resource text changed, with no data, migration or backup change (EC-SAF-16).
- **Website and PDFs** (`website/`, tasks 7.x) are outside this record.

## Notes

- **Seen, not in this diff:** at 200% font, while "Couldn't save" shows, the message's surface covers about the bottom 26 px of
  the top bar's date line ("Thursday, November 12"). Without the message the date line is whole (244-322 px). Develop 3d098049
  shows the same, and the date is also under "Started". Reported to the lead as a possible S4; not filed here.
- **Harness traps found today:** `monkey -p … 1` thaws a `wm user-rotation lock`, so landscape needs `am start`; on API 36
  `user_rotation` alone does not rotate; on API 26 the shell user hits a disk quota at about 528 MB free, so the fill runs as root
  (`adb root`, then `adb unroot`). On API 26 the system Phone app then crashes on the full disk (`SQLiteFullException` in
  voicemail). That is not Migrely: it was disabled for that one row and enabled again.
- **Device state:** both QA emulators were back to their starting settings and then turned off. API 26 started and ended with
  `zen_mode=3`, a leftover that `qa/reset_device.sh` cannot clear there.
