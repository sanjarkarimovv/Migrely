# Tasks

Base: `bugfix/192-onboarding-goal` from `origin/develop` (5bbf8f94). Write `Refs #192` in every commit and in the PR into
`develop`. Never write `Fixes #192`: the reporter retests and closes it. BRD D177, release 1.3.0.

Ownership:
- `medical-reviewer`, `product-manager`: 0;
- `ux-designer`: 1;
- `android-engineer`: 2 to 5;
- `code-reviewer`: 6.2;
- `medical-reviewer`: 6.1;
- `qa-engineer`: 7;
- `product-manager`: 8.

## 0. Copy and plan (before any code)

- [x] 0.1 `medical-reviewer` gives the words (med-192, 2026-11-13):
  - "Take fewer painkillers": FAIL;
  - "Keep track of my painkiller days": FIX ("painkiller" leaves out triptans);
  - final text: "Keep track of my medication days";
  - title, the other two goals and the disclaimer: PASS;
  - storage key kept.
- [x] 0.2 `product-manager`: BRD ONB-F1, D77, D177, EC-ONB-01…06, §13 row 45; this change. Verify:
  `openspec validate fix-onboarding-goal-wording --strict` exits 0.

## 1. Fit and accessibility (`ux-designer`, short check)

- [ ] 1.1 Check "Keep track of my medication days" on step 1 at 100% and 200% font on a 360 dp screen, portrait and
  landscape. Checks: wrapping (no clip, no "…", no break inside a word), touch target ≥ 48 dp, the disclaimer and "Restore" still
  reachable, and the TalkBack and keyboard order unchanged (design D3, D4). No new words: the wording is medical and exact.
  Verify: a packet that says PASS, or a finding sent to `product-manager`.

## 2. Domain, presentation and data (`android-engineer`)

- [x] 2.1 None. The stored value `fewer_painkillers`, `GoalDataModel`, `OnboardingGoalDomainModel`,
  `OnboardingGoalPresentationModel` and their mappers do not change (design D2). Verify:
  `git diff --stat origin/develop -- '*/domain/*' '*/presentation/*' '*/data/*' datasource '*.kts' '*.toml' app/src/main`
  prints nothing.
  Record (eng-192, 2026-11-13): the command printed nothing. No test, preview or Maestro flow asserts the old label
  (`git grep -n -i 'take fewer\|fewer painkillers'` outside docs and openspec: only the string itself and two unrelated
  website sentences), so no test changed.

## 3. UI string (`android-engineer`)

- [x] 3.1 `features/onboarding/ui/src/main/res/values/strings.xml`: `onboarding_goal_painkillers` = "Keep track of my medication
  days". The resource id stays. Verify:
  - `grep -c '>Keep track of my medication days<' features/onboarding/ui/src/main/res/values/strings.xml` is 1;
  - `git diff origin/develop -- features/onboarding/ui/src/main/res/values/strings.xml | grep -c '^[-+] '` is 2.
  Record (eng-192, 2026-11-13): 1 and 2. Only `values/` exists, so there are no translations to update.

## 4. Gate (`android-engineer`)

- [ ] 4.1 `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`. Verify: exit 0, with the task count
  and the test count.
- [ ] 4.2 Negative control: `git grep -n -i 'fewer painkillers' -- features app core designsystem` exits 1 here and prints 1 line
  on `origin/develop` 5bbf8f94.

## 5. Walk (`emulator-5570`, `android-engineer`)

- [ ] 5.1 Fresh install of the debug build, step 1 at 100% and 200% font, portrait and landscape:
  - the label wraps and is never clipped;
  - tapping it opens step 2;
  - "Skip" opens step 2.

  Then restart, Back on step 1 leaves the app, and onboarding shows again. Screenshots go in `migrely-work/walks/192/`.
  Restore the font scale and rotation. Verify: the screenshot paths, and the stored value read with
  `adb shell run-as com.migrely.app cat shared_prefs/migrely_preferences.xml | grep goal` showing `fewer_painkillers`.

## 6. Review

- [ ] 6.1 `medical-reviewer` re-reviews the final diff: the string as shipped, and that no other text changed. Verify: a PASS
  quoted into BRD D177 by `product-manager`.
- [ ] 6.2 `code-reviewer` on the diff. Verify: a packet with findings or "none", and every finding closed.

## 7. Acceptance (`qa-engineer`, scope: this diff only)

- [ ] 7.1 Strings: `git grep -n -i 'fewer painkillers' -- features app core designsystem` exits 1 on the candidate. Negative
  control: 1 line on 5bbf8f94. Also scan the APK resources for "Take fewer painkillers": 0 on the candidate, 1 on the base.
- [ ] 7.2 Step 1 on the device at 100% and 200% font, 360 dp, portrait and landscape: the label is not clipped, the disclaimer and
  "Restore" are reachable, and TalkBack reads "Keep track of my medication days" as actionable, in the unchanged order. This
  covers "The label", "Large font, narrow screen, landscape" and "TalkBack and keyboard".
- [ ] 7.3 Choosing it stores `fewer_painkillers`, "Skip" stores no goal, and Back on step 1 leaves the app. This covers
  "Choosing it" and "Skip and Back on step 1".
- [ ] 7.4 Upgrade: install 5bbf8f94, choose "Take fewer painkillers", finish onboarding, then update to the candidate without
  clearing data. Check:
  - home opens and onboarding does not show;
  - the prefs still hold `fewer_painkillers`;
  - no crash in logcat.

  This covers "An upgrade with the old goal stored". Control: the same run on 5bbf8f94 over itself.
- [ ] 7.5 Backup: export JSON on the candidate, with no `goal` key in the file, and import it with Add on 5bbf8f94. This covers
  "Backup and restore". The Auto Backup restore is accepted by reading the code, because D78 is unchanged; say so in the
  record.
- [ ] 7.6 Go/No-Go packet in `docs/qa/fix-onboarding-goal-wording.md`, saying what these checks do not prove. "Offline, Free or
  Premium, dark theme" is covered by 7.2 run in airplane mode with the dark theme.

## 8. Close (`product-manager`)

- [ ] 8.1 Record the PASS from 6.1 in D177, sync the spec, and archive. Verify:
  - `openspec validate --strict --all` exits 0;
  - `openspec/specs/onboarding/spec.md` holds "Keep track of my medication days".
