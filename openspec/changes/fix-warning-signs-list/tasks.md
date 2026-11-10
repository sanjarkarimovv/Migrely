# Tasks

Base: `bugfix/194-warning-signs` from `origin/release/1.2.1` (e7e6a002). Write `Refs #194` in every commit and in the PR into `release/1.2.1`.
Never write `Fixes #194`: the reporter retests it and closes it. BRD D171.

Ownership:
- `product-manager`, `medical-reviewer`, `ux-designer`: 0;
- `android-engineer`: 1 to 4;
- `code-reviewer`: 5;
- `qa-engineer`: 6.

## 0. Copy and layout

- [ ] 0.1 `medical-reviewer` passes every line word by word, and `ux-designer` gives the layout and a11y spec (design D5). Verify: the verdict is quoted in BRD D171 and the exact strings are in `specs/app-settings/spec.md`. `openspec validate fix-warning-signs-list --strict` exits 0.

## 1. Domain, presentation, data

- [ ] 1.1 None. This change touches no use case, ViewModel, repository, database or backup. Verify: `git diff --stat origin/release/1.2.1 -- '*/domain/*' '*/presentation/*' '*/data/*' app/src/main/AndroidManifest.xml` is empty.

## 2. UI

- [ ] 2.1 In `features/settings/ui/src/main/res/values/strings.xml`:
  - `settings_warning_signs_call` and `settings_warning_signs_list` take the spec's emergency heading and its 8 items, in order;
  - add a heading string and a string array for the second part, with its 3 items;
  - `settings_about_urgent` takes the spec's About line;
  - the comment cites BRD SAF-F1, D171;
  - apostrophes escaped (`\'`).

  Verify: `grep -c '<item>' features/settings/ui/src/main/res/values/strings.xml` rises by 6 (5 old items, 11 new), and `grep -n "doesn\\\\'t pass\|age of 50" features/settings/ui/src/main/res/values/strings.xml` prints nothing.
- [ ] 2.2 In `WarningSignsContents` (`SettingsPages.kt`), lay out H1 + 8 bullets, then H2 + 3 bullets, as in design D5. H1 and H2 get `semantics { heading() }`. Keep `Bullet` (the glyph stays silent) and the scrolling `SettingsPage`. Add no dial action. Verify: a Compose UI or Robolectric test asserts the two headings, 11 sign nodes in order, and no node with the bullet glyph. Then `./gradlew :features:settings:ui:testDebugUnitTest`. Negative control: the test fails on e7e6a002.

## 3. Wiring

- [ ] 3.1 None: the route and the Settings entry do not change. Verify: `git diff origin/release/1.2.1 -- app/src/main/java` is empty.

## 4. Gate and walk

- [ ] 4.1 Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0, and `:app:checkReleasePermissions` is unchanged.
- [ ] 4.2 Walk on `emulator-5570`. Settings › Warning signs at 100% and 200%, portrait and landscape: scroll to the last item. Then About Migrely. Restore font scale and rotation. Verify: screenshots in `migrely-work/walks/194/`, every item whole.

## 5. Review

- [ ] 5.1 `code-reviewer` reviews the diff against this spec. Verify: written review with no open blocking finding.

## 6. QA (`qa-engineer`, diff scope only, owner rule 2026-11-09)

- [ ] 6.1 Update the flows that assert the old text: `.maestro/app-settings/sections-and-pages.yaml` (lines 32-38) and `.maestro/a11y-tour/settings-warning-signs.yaml` (line 12). Verify: both pass on `Migrely_QA_API_36` on the candidate SHA.
- [ ] 6.2 Text and order. Every string on the page and About's last paragraph equal the spec, character for character. Verify: a dump of the UI hierarchy compared to the spec. Negative control: e7e6a002 shows the old five items and fails.
- [ ] 6.3 TalkBack. Read order: title, H1, 8 items, H2, 3 items. Both are announced as headings, heading navigation jumps H1 → H2, and no bullet glyph is spoken. Verify: TalkBack log on API 36.
- [ ] 6.4 200% font and landscape on API 36 and API 26: scroll to "You're pregnant or have just had a baby", nothing cut. Verify: screenshots. Restore device settings.
- [ ] 6.5 Not proven by QA, so the Go/No-Go must say so: whether the wording is clinically right (T31), whether a person in pain understands it, and behaviour on a real phone.
