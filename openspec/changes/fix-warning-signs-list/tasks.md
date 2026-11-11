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

- [x] 1.1 None. This change touches no use case, ViewModel, repository, database or backup. Verify: `git diff --stat origin/release/1.2.1 -- '*/domain/*' '*/presentation/*' '*/data/*' app/src/main/AndroidManifest.xml` is empty.

## 2. UI

- [x] 2.1 Copy each string exactly as the spec gives it. Edit `features/settings/ui/src/main/res/values/strings.xml`:
  - `settings_warning_signs_call`: the H1 text, which is unchanged in words;
  - `settings_warning_signs_list`: the 9 items, in the spec's order;
  - add `settings_warning_signs_no_drive`, `settings_warning_signs_urgent`, `settings_warning_signs_urgent_list` (3 items) and `settings_warning_signs_other`;
  - `settings_about_urgent`: the new text;
  - the comment cites BRD SAF-F1, D171, and every apostrophe is escaped (`\'`).

  Verify: `grep -c '<item>' features/settings/ui/src/main/res/values/strings.xml` rises by 7 (5 old items become 12). `grep -n "doesn\\'t pass\|age of 50\|recent head" features/settings/ui/src/main/res/values/strings.xml` prints nothing.
- [x] 2.2 In `features/home/ui/src/main/res/values/strings.xml`, set `long_check_body` = "If it's still going after 72 hours, get urgent medical advice." Verify: `grep -n long_check_body features/home/ui/src/main/res/values/strings.xml`.
- [x] 2.3 Lay out `WarningSignsContents` (`SettingsPages.kt`) as in design D5:
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

- [x] 3.1 None: the route and the Settings entry do not change. Verify: `git diff origin/release/1.2.1 -- app/src/main/java` is empty.

## 4. Gate and walk

- [x] 4.1 Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0, and `:app:checkReleasePermissions` is unchanged.
- [x] 4.2 Walk on `emulator-5570`:
  - Settings › Warning signs at 100% and 200%, portrait and landscape, scrolling to the last line;
  - About Migrely;
  - the 72-hour "Still going?" card, using an attack started 73 hours earlier.

  Restore font scale and rotation afterwards. Verify: screenshots in `migrely-work/walks/194/`, every line whole.

### Record of 1 to 4 (`android-engineer`, 2026-11-11)

- 1.1 and 3.1: both diffs against `origin/release/1.2.1` (e7e6a002) are empty; so is the one for `*.kts`, `*.toml`
  and every `AndroidManifest.xml`.
- 2.1: `<item>` count 20 -> 27. The grep for the old text prints nothing. `medical-reviewer` compared the committed XML
  at 22f8462a with D171 (8) by script: 10/10 match, PASS. A later commit changes one XML comment only.
- 2.2: `long_check_body` = "If it\'s still going after 72 hours, get urgent medical advice."
- 2.3: `WarningSignsContentsTest` (Robolectric, 4 tests) checks the 16 texts in the spec's order, the 3 headings
  (title, H1, H2), no exposed bullet glyph, and About's last line.
  - On e7e6a002's code, 3 of 4 fail on the old text.
  - The glyph check fails when the glyph's semantics are not cleared (control reverted).
  - `:features:settings:ui:testDebugUnitTest`: 22 tests, 0 failures.
  - The spec's own list gives 16 text nodes and 18 TalkBack stops, not 14 and 17; the lead has confirmed this.
- 4.1: the gate exits 0 at 276dfe06: 3013 unit tests, 0 failures. `:app:checkReleasePermissions` exits 0. The merged
  release manifest has the same six permissions, and there is no INTERNET.
- 4.2: walked on `emulator-5570` (API 36, 411 dp, 1.2.1 vc11 debug), screenshots in `migrely-work/walks/194/`.

  | Scenario | Result |
  |---|---|
  | Warning signs, 100%, portrait: top and end | 16 texts in the spec's order; 24 dp before H2 and before the last line (63 px against 42 px); last line whole |
  | 100%, landscape | Whole to the last line |
  | 200%, landscape | App bar title not cut; whole to the last line |
  | 200%, portrait | H1 whole on the first screen; whole to the last line |
  | 200%, 320 dp (`wm size 840x1866`) | H1 whole on the first screen; title not cut; last line whole |
  | About Migrely | Last paragraph equals the spec |
  | 72-hour card, attack moved back 73 h (3 d 1 h) | "Still going?" with the new text, at 100% and at 200% |

  Restored: font 1.0, rotation 0, auto-rotate on, `wm size` reset, adbd unrooted.

  Not checked, and why:
  - TalkBack (QA 6.3): headings and the absent glyph are proven in Robolectric only.
  - API 26, ar-XB and dark/light switching (QA 6.4).
  - Process death: the page has no state.
  - Warning signs during an attack: attack mode has no way into Settings.
  - At 200% the 72-hour card sits below the rain card, in the short top area that scrolls. It shows only after a scroll
    there. That layout is unchanged by this diff and is noted for `ux-designer` and QA.

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
