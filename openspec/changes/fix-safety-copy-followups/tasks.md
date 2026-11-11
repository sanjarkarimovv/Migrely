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

- [ ] 0.1 `medical-reviewer` checks every new string word by word against its source quote in design D1 and D2, and answers the
  three open questions there. Verify: a PASS or a list of exact changes in the packet. Nothing says "medically reviewed".
- [ ] 0.2 `ux-designer` checks the card (title 27 characters, sentence 89) and the longer rash item at 100% and 200% font, 320 dp
  and landscape, the dark room and TalkBack order. Verify: a packet that names any layout change, or says "none".
- [ ] 0.3 `product-manager` folds any change from 0.1 or 0.2 into the spec, design D1/D2 and BRD D173 (8), and quotes the PASS in
  D173 (7). Verify: `openspec validate fix-safety-copy-followups --strict` exits 0, and the strings in the specs equal those in D173.

## 1. Domain, presentation, data

- [ ] 1.1 None. Verify: `git diff --stat origin/develop -- '*/domain/*' '*/presentation/*' '*/data/*' '*.kts' '*.toml' app/src/main`
  is empty.

## 2. UI strings

- [ ] 2.1 `features/attack/ui/src/main/res/values/strings.xml`: set `safety_title` and `safety_body` exactly as the spec's "Card
  text" scenario. `safety_dismiss` stays. Verify: `grep -nE 'worst|emergency services' features/attack/ui/src/main/res/values/strings.xml`
  prints nothing.
- [ ] 2.2 `features/settings/ui/src/main/res/values/strings.xml`: item 8 of `settings_warning_signs_list` exactly as the spec's
  "Reading them" scenario, with the apostrophe escaped (`\'`). Add D173 to the comment above the list. No other item changes.
  Verify: `grep -c '<item>' features/settings/ui/src/main/res/values/strings.xml` is unchanged, and `grep -n 'brown or black skin'`
  prints one line.
- [ ] 2.3 In `AttackPainSection.kt`, update the comment on the title that says "the text is unchanged (BRD D165 (3))" so it cites
  D173. Change no code. Verify: `git diff features/attack/ui/src/main/java` shows only comment lines.

## 3. Tests (each must fail on 3d098049)

- [ ] 3.1 Update the `TITLE` constant in `SafetyCardRevealTest`, `SafetyCardRestoreTest` and `SaveMessageTest`. Add an exact-text
  check of the title and sentence to one of them. Verify: `./gradlew :features:attack:ui:testDebugUnitTest` passes.
- [ ] 3.2 Update item 8 in `WarningSignsContentsTest`. Keep its order, its 16 text nodes after the title, its two headings and its
  no-glyph checks. Add a check that no node contains "numb". Verify: `./gradlew :features:settings:ui:testDebugUnitTest` passes.
- [ ] 3.3 Add a Robolectric test in `app/src/testDebug` (design D5). It reads `safety_body` and `settings_warning_signs_list[0]`.
  It asserts that the sentence contains the item in lower case, and that neither card string contains "worst" or "emergency
  services". Verify: `./gradlew :app:testDebugUnitTest` passes.
- [ ] 3.4 Negative control. Put back only the two old string files (`git checkout 3d098049 -- features/attack/ui/src/main/res/values/strings.xml
  features/settings/ui/src/main/res/values/strings.xml`), run 3.1 to 3.3, then restore them (`git checkout HEAD -- …`). Verify: each
  new or changed assertion fails on the old files. Paste the failing test names into the packet, and `git status` is clean after.

## 4. Wiring

- [ ] 4.1 None: no route, entry, ViewModel or DI changes. Verify: the 1.1 diff is empty.

## 5. Gate

- [ ] 5.1 Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0, and
  `:app:checkReleasePermissions` is unchanged.

## 6. Walk (`emulator-5570`)

- [ ] 6.1 Start an attack and scroll down to Pain. Add a first 10 at 100% and 200% font, in portrait and landscape and on 320 dp,
  once saved and once with storage full. Check the card comes into view whole (title, sentence, "Got it") below any message.
  Verify: screenshots in `migrely-work/walks/200/`.
- [ ] 6.2 With TalkBack on: the card is spoken once it is in view, the title as a heading and then the sentence, with focus not moved.
  On Settings › Warning signs the rash item is one stop read with its bracket, out of 18 stops. Verify: the TalkBack log lines in
  the packet.
- [ ] 6.3 Restore font scale, rotation, TalkBack, storage and animator scale. Verify: `adb shell settings get system font_scale`
  shows `1.0`, and TalkBack is off.

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

  Negative control: `3d098049` shows the old words. Verify: a Go/No-Go note in `docs/qa/` that states what it does not prove:
  clinical truth (T31), and comprehension by a person in pain.

## 10. Close

- [ ] 10.1 `product-manager` checks acceptance, and the change is archived. The pending `fix-safety-card-into-view` may be archived
  before or after this one: its scenarios no longer quote the card's words (design D4). Verify:
  `grep -n 'worst' openspec/specs/attack-details/spec.md` prints nothing after archiving.
