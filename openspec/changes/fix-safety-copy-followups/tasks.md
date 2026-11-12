# Tasks

Base: `bugfix/200-safety-copy` from `origin/develop` (3d098049). Write `Refs #200` in every commit and in the PR into `develop`. Never
write `Fixes #200`, because the reporter retests and closes it. BRD D173.

Ownership:
- `medical-reviewer`, `ux-designer`, `product-manager`: 0;
- `android-engineer`: 1 to 6;
- `aso-seo-specialist`: 7;
- `code-reviewer`: 8;
- `qa-engineer`: 9;
- `product-manager`: 10.

## 0. Copy and layout (before any code)

- [x] 0.1 `medical-reviewer` checks every new string word by word against its source quote in design D1 and D2, and answers the
  three open questions there. Verify: a PASS or a list of exact changes in the packet. Nothing says "medically reviewed".
  Record: PASS 2026-11-11 on the 3 strings and the numbness decision, with fixes A–C to the plan's source wording (BRD D173 (7)).
- [x] 0.2 `ux-designer` checks the card (title 27 characters, sentence 89) and the longer rash item at 100% and 200% font, 320 dp
  and landscape, the dark room and TalkBack order. Verify: a packet that names any layout change, or says "none".
  Record: OK 2026-11-11, no copy or layout change (`migrely-work/ux-200/review.md`); its walk rows are in 6.1 and 6.2.
- [x] 0.3 `product-manager` folds any change from 0.1 or 0.2 into the spec, design D1/D2 and BRD D173 (8), and quotes the PASS in
  D173 (7). Verify: `openspec validate fix-safety-copy-followups --strict` exits 0, and the strings in the specs equal those in D173.

## 1. Domain, presentation, data

- [x] 1.1 None. Verify: `git diff --stat origin/develop -- '*/domain/*' '*/presentation/*' '*/data/*' '*.kts' '*.toml' app/src/main`
  is empty.
  Record (android-engineer, 2026-11-11): the diff is empty.

## 2. UI strings

- [x] 2.1 `features/attack/ui/src/main/res/values/strings.xml`: set `safety_title` and `safety_body` exactly as the spec's "Card
  text" scenario. `safety_dismiss` stays. Verify: `grep -nE 'worst|emergency services' features/attack/ui/src/main/res/values/strings.xml`
  prints nothing.
  Record: both strings set from D173 (8); the grep prints nothing (exit 1).
- [x] 2.2 `features/settings/ui/src/main/res/values/strings.xml`: item 8 of `settings_warning_signs_list` exactly as the spec's
  "Reading them" scenario, with the apostrophe escaped (`\'`). Add D173 to the comment above the list. No other item changes.
  Verify: `grep -c '<item>' features/settings/ui/src/main/res/values/strings.xml` is unchanged, and `grep -n 'brown or black skin'`
  prints one line.
  Record: item 8 set with `\'`, D173 added to the comment; `<item>` count 27 before and after; one `brown or black skin` line.
- [x] 2.3 In `AttackPainSection.kt`, update the comment on the title that says "the text is unchanged (BRD D165 (3))" so it cites
  D173. Change no code: no `LineBreak`, alignment, padding or `maxLines` change in `SafetyCard` (ux review: a lone "pain?" on
  320 dp at 100% is accepted). Verify: `git diff features/attack/ui/src/main/java` shows only comment lines.
  Record: one comment line, now "worded as Warning signs (BRD D173)"; no code line changed.

## 3. Tests (each must fail on 3d098049)

- [x] 3.1 Update the `TITLE` constant in `SafetyCardRevealTest`, `SafetyCardRestoreTest` and `SaveMessageTest`. Add an exact-text
  check of the title and sentence to one of them. Verify: `./gradlew :features:attack:ui:testDebugUnitTest` passes.
  Record: the three constants updated; `SaveMessageTest` "The card reads the spec's title, sentence and button word for
  word" added. Green: 40 tests in the three classes, 0 failures.
- [x] 3.2 Update item 8 in `WarningSignsContentsTest`. Keep its order, its 16 text nodes after the title, its two headings and its
  no-glyph checks. Add a check that no node contains "numb". Verify: `./gradlew :features:settings:ui:testDebugUnitTest` passes.
  Record: item 8 updated; "no text names numbness" added. It matches `numb`/`numbness` as a word: a first version
  matched "number" and failed on the headings, so it was fixed before use. Green: 5 tests, 0 failures. Its own negative
  control: with a temporary "Numbness in your face or body" item it fails with exactly that text (restored after).
- [x] 3.3 Add a Robolectric test in `app/src/testDebug` (design D5). It reads `safety_body` and `settings_warning_signs_list[0]`.
  It asserts that the sentence contains the item in lower case, and that neither card string contains "worst" or "emergency
  services". Verify: `./gradlew :app:testDebugUnitTest` passes.
  Record: `app/src/testDebug/.../ui/safety/SafetyCopyParityTest.kt`, 2 tests, 0 failures.
- [x] 3.4 Negative control. Put back only the two old string files (`git checkout 3d098049 -- features/attack/ui/src/main/res/values/strings.xml
  features/settings/ui/src/main/res/values/strings.xml`), run 3.1 to 3.3, then restore them (`git checkout HEAD -- …`). Verify: each
  new or changed assertion fails on the old files. Paste the failing test names into the packet, and `git status` is clean after.
  Record: on the 3d098049 files 23 tests fail: the 20 attack tests that find the card by its title (the new exact-text
  test among them), `WarningSignsContentsTest` "every text reads in the spec's order", and both `SafetyCopyParityTest`
  tests ("…should contain \"extremely bad pain that came on suddenly\"" and the three old-word hits). The numbness test
  passes there, as it should: the old page has no numbness sign; its control is in 3.2. `git status` showed only this
  change's files after the restore. Log: `migrely-work/walks/200/neg-control-3.4.log`.

## 4. Wiring

- [x] 4.1 None: no route, entry, ViewModel or DI changes. Verify: the 1.1 diff is empty.
  Record: none; the 1.1 diff is empty.

## 5. Gate

- [x] 5.1 Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0, and
  `:app:checkReleasePermissions` is unchanged.
  Record: first run exit 1 on `:app:ktlintTestDebugSourceSetCheck` (import order and one long line in the new parity
  test); fixed, the parity test's negative control rerun (2 of 2 fail on 3d098049), then the gate exits 0: 3017 tests,
  0 failures, 0 skipped, 53 modules. `:app:checkReleasePermissions` exits 0; the merged release manifest has
  ACCESS_NETWORK_STATE, BIND_JOB_SERVICE, DUMP, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, WAKE_LOCK and no INTERNET;
  this diff touches no manifest or Gradle file. Logs: `migrely-work/walks/200/gate2.log`, `perms.log`.

## 6. Walk (`emulator-5570`)

Setup: AVD `Migrely_API_36`, 1080 × 2400, dark theme, Free user. For 320 dp wide: `adb -s emulator-5570 shell wm density 540`
(1080 px ÷ 3.375 = 320 dp, 711 dp tall). Landscape runs at the default density (411 dp tall, like a real phone). Font:
`settings put system font_scale 1.0` or `2.0`. For each card row: start an attack, scroll down to Pain, add a first 10 with no
saved 10. Pass means the spec's "The card stays whole at 200% font": card top just below the message, nothing under it, nothing
clipped, the rest reached by scrolling. Whether "Got it" is in view without scrolling is data, not pass or fail.

- [x] 6.1 Card rows, screenshots in `migrely-work/walks/200/`:
  - C1: 100%, 411 dp portrait, saved. Card scrolls into view; title and sentence exact; "Got it" visible; no old words.
  - C2: 200%, 411 dp portrait, storage full. Message under the top bar; the whole card below it; nothing under the message.
  - C3: 200%, 320 dp portrait, storage full. Card top right below the message. Record the title's line count (3 expected) and
    "Got it in view y/n". If n, one scroll reaches it and nothing is clipped.
  - C4: 100%, 320 dp portrait, saved. Record title lines (1 or 2; a lone "pain?" is accepted). Nothing clipped.
  - C5: 200%, landscape, storage full. Card top below the message; the visible part shows the title and the start of "Call your
    local emergency number now"; the rest scrolls, nothing clipped. Record "Got it in view y/n".
  - C6: 200%, landscape, saved. As C5 without the message.
  - C9: 200%, rotate portrait → landscape with the card and message showing (storage full). Both stay; the message does not cover
    the card; no new scroll. A screenshot pair.
  - C10: 100%, 411 dp, "Remove animations" on. The reveal jumps with no ease; text as C1.
  Record (android-engineer, 2026-11-11/12, debug APK of 392a349b, sha256 722d0518…, dark theme, Free; evidence in
  `migrely-work/walks/200/`: `walk-card.jsonl`, `walk-w.jsonl`, `shots/`). Card: every row shows the exact title and sentence
  and no old words; the card never sits under the message. "Got it in view" is data: y = whole button inside the viewport.
  - C1 pass: card scrolled into view from below Pain; title 1 line; Got it in view y.
  - C2 pass: message y 328-517 under the top bar, card title from y 623; title 2 lines; Got it in view y.
  - C3 pass: message y 385-763, content from 966, card title from 1061; title 3 lines; Got it in view n (45 of 135 px at
    the screen edge). One scroll: Got it whole at y 1843-1978, sentence whole, message unchanged.
  - C4 pass: title 2 lines with a lone "pain?" (accepted); Got it in view y; nothing clipped.
  - C5 pass: message y 263-452, card title from 558; title 1 line; whole sentence visible; Got it in view n (70 of 105 px).
  - C6 pass: card at the content top (title y 331); Got it in view y.
  - C9 pass: after portrait to landscape both stay, no overlap; the card keeps the same 74 px below the content top (no
    new scroll). Pair: `C9-portrait.png`, `C9-landscape.png`.
  - C10 pass: end state as C1. The jump itself is not visible in a screenshot; the Robolectric test "Given Remove
    animations then the content jumps to the card" covers it.
  - W1 pass: 16 texts in the spec's order, item 8 exact with the full bracket (3 lines), items 1-7 and 9 unchanged.
  - W2 pass: item 8 wraps to 7 lines, none clipped, ")" visible; the last line is reached by scrolling (run W2b).
  - W3 pass: landscape, item 8 3 lines, ")" visible, last line reached.
  - W5 not run (optional): the app is English only and the ux review reads it from the code.
  Harness notes: the Mac slept repeatedly during the walk (`caffeinate -i` does not stop it), so rows took 15-50 min of
  wall time; first runs of C3, C5, C6, W1 and W2 failed in the scripts (Home dump hang at 200% on 320 dp, Back-gesture
  edge, wrong landscape touch mapping, clipped-bounds check) and were rerun; no app defect behind any of them.

  Warning signs rows: W1 100% 411 dp portrait, item 8 reads with the full bracket exactly as the spec, items 1–7 and 9 unchanged.
  W2 200% 320 dp portrait, item 8 wraps (about 6–7 lines), no line clipped, the closing ")" visible, scroll to the last line.
  W3 200% landscape, the same. W5 (optional) forced RTL layout: ")" at the end of the item, the bullet mirrored.
  Verify: one screenshot per row, the C3/C4 line counts and the C3/C5 "Got it in view y/n" in the packet.
- [x] 6.2 TalkBack rows, log lines in the packet:
  - C7: 100%, 411 dp portrait, saved. Spoken once in view: the title (heading), then the sentence in full. Focus not moved.
  - C8: 200%, 320 dp portrait, storage full. As C7, then the message after the card (D165). The card is partly out of view here;
    confirm it is still spoken, and record "Got it in view y/n".
  - W4: 100%, 411 dp, Settings › Warning signs. Swipe the whole page and count the stops: 18, in D171's order. Item 8 is one stop,
    spoken in full with its note. Record how the brackets are spoken at default punctuation.
  Record (TalkBack log VERBOSE for the rows, back to ERROR after):
  - C7 pass: after "Add, Button", TalkBack says "Sudden, extremely bad pain?", then "Call your local emergency number now
    if this is extremely bad pain that came on suddenly.", then "Got it, Button"; then "Migrely", "Back", the
    usual focus return when the sheet closes. The word "Heading" is not spoken at default verbosity; the heading role is
    covered by `SaveMessageTest` "The card's title is a heading" and by W4 ("Warning signs, Heading").
  - C8 pass: card first (title, sentence, "Got it, Button"), then "Couldn't save. Your phone's storage is full.",
    "Dismiss, Button" (D165). The card is partly out of view; Got it in view n (45 px).
  - W4 pass: 18 stops. Right swipes from TalkBack's start speak the title ("Warning signs, Heading") and the 16 texts in
    D171's order; one left swipe from the title speaks "Back, Button" (stop 1). Item 8 is one stop, spoken in full with
    its note. Brackets: TalkBack's spoken text keeps "(" and ")" as written, so at default punctuation it does not turn
    them into words; the emulator runs with no audio, so how the voice renders them was not heard.

  Verify: the log lines, the stop count of 18, and the bracket speech in the packet.
- [x] 6.3 Restore: `wm density reset`, `wm size reset`, font scale `1.0`, rotation back to auto and portrait, TalkBack off, forced
  RTL off, animator scales back, storage freed. Verify: `adb shell wm density` and `adb shell wm size` show no override,
  `adb shell settings get system font_scale` shows `1.0`, and TalkBack is off.
  Record: font 1.0, density 420 and size 1080x2400 with no override, accelerometer_rotation 1 and user_rotation 0,
  animator scales 1/1/1, accessibility off (no service, touch exploration 0), TalkBack log ERROR, night mode no (as
  found), no filler files, app data restored from the pre-walk backup (`diff -rq` empty). 5570 turned off.

## 7. Site (`aso-seo-specialist`, after 0.1 passes)

- [ ] 7.1 Add the bracket to the rash item in `website/build.py` `urgent_help()` and in `website/make_diary_pdf.py`. Rebuild the four
  pages (`headache-diary`, `medication-overuse-headache`, `menstrual-migraine`, `va-migraine-log`) and both PDFs, keeping each
  under 300 KB. Add [Men] to those pages' sources if it is missing. Do not move any `*_REVIEWED` date unless every fact on that
  page has been re-checked against its sources that day. The PDF footer's "Updated <date>" may move. Verify: `grep -rl 'brown
  or black skin' website/public` lists all four pages, the PDFs' text has it too, and `git diff -- website/build.py | grep
  _REVIEWED` prints nothing unless the packet names the full re-check.
- [ ] 7.2 `medical-reviewer` checks the site diff. Deploying is the owner's step (handbook section 5). Verify: the PASS in the packet
  and a deploy request to `team-lead`.

## 8. Review

- [ ] 8.1 `code-reviewer` reviews the diff against this change. Verify: findings resolved or answered in the PR.

## 9. QA (scope: this diff only)

- [ ] 9.1 `qa-engineer` updates `.maestro/app-settings/sections-and-pages.yaml` (line 45 matches the old rash text) and
  `.maestro/a11y-tour/settings-warning-signs.yaml` if it reads item 8. Then they run:
  - the card text at 100% and 200%, portrait and landscape, saved and storage full, scrolled down;
  - TalkBack on the card and on the rash item;
  - the Warning signs page to its last line.

  In `sections-and-pages.yaml` the item text is a regex: escape the new brackets as `\\(this may be harder to see on brown or
  black skin\\)`, as the file already does with `\\.`. Unescaped, `(…)` is a group and the literal brackets do not match. Add an
  `assertNotVisible` for the old item 8 only if Maestro matches the whole text there. No flow reads the card text today, so the
  card's negative control is task 3.4 plus a manual C1 on 3d098049. Run the starred rows of the ux review: C1, C2, C3, C5, C7, W1,
  W2, W4.

  Negative control: `3d098049` shows the old words. Verify: a Go/No-Go note in `docs/qa/` that states what it does not prove:
  clinical truth (T31), and comprehension by a person in pain.

## 10. Close

- [ ] 10.1 `product-manager` checks acceptance, and the change is archived. The pending `fix-safety-card-into-view` may be archived
  before or after this one: its scenarios no longer quote the card's words (design D4). Verify:
  `grep -n 'worst' openspec/specs/attack-details/spec.md` prints nothing after archiving.
