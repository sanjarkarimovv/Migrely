# Tasks

Base: `bugfix/194-warning-signs` from `origin/release/1.2.1` (e7e6a002). Write `Refs #194` in every commit and in the PR into `release/1.2.1`.
Never write `Fixes #194`: the reporter retests it and closes it. BRD D171.

Ownership:
- `product-manager`, `medical-reviewer`, `ux-designer`: 0;
- `android-engineer`: 1 to 4;
- `code-reviewer`: 5;
- `qa-engineer`: 6.

## 0. Copy and layout

- [x] 0.1 `medical-reviewer` passes every line word by word, and `ux-designer` gives the layout and a11y spec (design D5). Verify: the verdict is quoted in BRD D171, and the exact strings are in `specs/app-settings/spec.md` and `specs/attack-logging/spec.md`. `openspec validate fix-warning-signs-list --strict` exits 0.

## 1. Domain, presentation, data

- [ ] 1.1 None. This change touches no use case, ViewModel, repository, database or backup. Verify: `git diff --stat origin/release/1.2.1 -- '*/domain/*' '*/presentation/*' '*/data/*' app/src/main/AndroidManifest.xml` is empty.

## 2. UI

- [ ] 2.1 Copy each string exactly as the spec gives it. Edit `features/settings/ui/src/main/res/values/strings.xml`:
  - `settings_warning_signs_call`: the H1 text, which is unchanged in words;
  - `settings_warning_signs_list`: the 9 items, in the spec's order;
  - add `settings_warning_signs_no_drive`, `settings_warning_signs_urgent`, `settings_warning_signs_urgent_list` (3 items) and `settings_warning_signs_other`;
  - `settings_about_urgent`: the new text;
  - the comment cites BRD SAF-F1, D171, and every apostrophe is escaped (`\'`).

  Verify: `grep -c '<item>' features/settings/ui/src/main/res/values/strings.xml` rises by 7 (5 old items become 12). `grep -n "doesn\\'t pass\|age of 50\|recent head" features/settings/ui/src/main/res/values/strings.xml` prints nothing.
- [ ] 2.2 In `features/home/ui/src/main/res/values/strings.xml`, set `long_check_body` = "If it's still going after 72 hours, get urgent medical advice." Verify: `grep -n long_check_body features/home/ui/src/main/res/values/strings.xml`.
- [ ] 2.3 Lay out `WarningSignsContents` (`SettingsPages.kt`) as in design D5:
  - H1 and its 9 bullets, the "Don't drive" `Paragraph`, a 24 dp gap, H2 and its 3 bullets, a 24 dp gap, and the last `Paragraph`;
  - H1 and H2 get `semantics { heading() }`;
  - keep `Bullet` and the scrolling `SettingsPage`, with no dial action and no `maxLines`;
  - add the light preview and the 200%, 320 dp preview.

  Verify: a Compose UI or Robolectric test asserts:
  - two heading nodes;
  - 14 text nodes after the title, in the spec's order;
  - no node with the bullet glyph.

  Then run `./gradlew :features:settings:ui:testDebugUnitTest`. Negative control: the test fails on e7e6a002.

## 3. Wiring

- [ ] 3.1 None: the route and the Settings entry do not change. Verify: `git diff origin/release/1.2.1 -- app/src/main/java` is empty.

## 4. Gate and walk

- [ ] 4.1 Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0, and `:app:checkReleasePermissions` is unchanged.
- [ ] 4.2 Walk on `emulator-5570`:
  - Settings › Warning signs at 100% and 200%, portrait and landscape, scrolling to the last line;
  - About Migrely;
  - the 72-hour "Still going?" card, using an attack started 73 hours earlier.

  Restore font scale and rotation afterwards. Verify: screenshots in `migrely-work/walks/194/`, every line whole.

## 5. Review

- [ ] 5.1 `code-reviewer` reviews the diff against this spec. Verify: written review with no open blocking finding.

## 6. QA (`qa-engineer`, diff scope only, owner rule 2026-11-09)

- [ ] 6.1 Update the flows that assert the old text: `.maestro/app-settings/sections-and-pages.yaml` (lines 32-38) and `.maestro/a11y-tour/settings-warning-signs.yaml` (line 12). Verify: both pass on `Migrely_QA_API_36` on the candidate SHA.
- [ ] 6.2 Text and order: every line on the page, About's last paragraph and the 72-hour card text equal the spec, character for character. Verify: a dump of the UI hierarchy compared with the spec. Negative control: e7e6a002 shows the old five items and the old 72-hour text, and fails.
- [ ] 6.3 TalkBack on API 36:
  - 17 stops in the spec's order;
  - both part headings announced as headings;
  - heading navigation jumps from H1 to H2;
  - no bullet glyph spoken.

  Verify: a TalkBack log.
- [ ] 6.4 Layout on API 36 and API 26, in this order:
  - (a) at 320 dp, 200% font and dark, H1 is whole on the first screen;
  - (b) in landscape at 200%, the app bar title is not cut;
  - scroll to the last line: nothing is cut;
  - (d) the gap before H2 reads as a break between the parts;
  - (e) in the ar-XB pseudo-locale, the glyph is on the start side.

  Verify: screenshots, then restore the device settings.
- [ ] 6.5 The Go/No-Go says what QA cannot prove:
  - whether the wording is clinically right (T31);
  - whether a person in pain understands it;
  - behaviour on a real phone.
