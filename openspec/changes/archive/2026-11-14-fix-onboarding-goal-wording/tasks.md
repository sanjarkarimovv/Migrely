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

- [x] 1.1 Check "Keep track of my medication days" on step 1 at 100% and 200% font on a 360 dp screen, portrait and
  landscape. Checks: wrapping (no clip, no "…", no break inside a word), touch target ≥ 48 dp, the disclaimer and "Restore" still
  reachable, and the TalkBack and keyboard order unchanged (design D3, D4). No new words: the wording is medical and exact.
  Verify: a packet that says PASS, or a finding sent to `product-manager`.
  Record (ticked by `product-manager`, 2026-11-14): `ux-designer` reported CONFIRM on 2026-11-13: the label wraps to 2 lines
  with no clip and TalkBack reads it as "…, button". The box was not ticked at the time. QA 7.2 (qa-192, 2026-11-14) measured
  the same independently at 100% and 200% font, 360 dp, portrait and landscape: 8 of 8 rows pass, buttons ≥ 48 dp, Restore
  reachable, TalkBack and Tab order as on the base. Not covered by either: a real phone.

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

- [x] 4.1 `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`. Verify: exit 0, with the task count
  and the test count.
  Record (eng-192, 2026-11-13, on 6c0f4548, load 1.9): `ANDROID_HOME=… nice -n 19 ./gradlew --max-workers=2 assembleDebug test
  ktlintCheck detekt lint` exit 0, BUILD SUCCESSFUL in 5m 5s, 2058 actionable tasks, 2058 executed. Test results: 468 files,
  3017 tests (debug and release variants together), 0 failures, 0 errors, 0 skipped. Then `:app:checkReleasePermissions
  :app:checkReleaseDebugComponents` exit 0; the merged release manifest has no `android.permission.INTERNET` (count 0).
- [x] 4.2 Negative control: `git grep -n -i 'fewer painkillers' -- features app core designsystem` exits 1 here and prints 1 line
  on `origin/develop` 5bbf8f94.
  Record (eng-192, 2026-11-13): at the head, exit 1 and no output. `git grep -n -i 'fewer painkillers' 5bbf8f94 -- features
  app core designsystem` exit 0, 1 line: `features/onboarding/ui/src/main/res/values/strings.xml:9`. The built APK's
  `string/onboarding_goal_painkillers` reads "Keep track of my medication days" (`aapt2 dump resources`).

## 5. Walk (`emulator-5570`, `android-engineer`)

- [x] 5.1 Fresh install of the debug build, step 1 at 100% and 200% font, portrait and landscape:
  - the label wraps and is never clipped;
  - tapping it opens step 2;
  - "Skip" opens step 2.

  Then restart, Back on step 1 leaves the app, and onboarding shows again. Screenshots go in `migrely-work/walks/192/`.
  Restore the font scale and rotation. Verify: the screenshot paths, and the stored value read with
  `adb shell run-as com.migrely.app cat shared_prefs/migrely_preferences.xml | grep goal` showing `fewer_painkillers`.
  Record (eng-192, 2026-11-13, emulator-5570, API 36, 1080 px at `wm density 480` = 360 dp, dark theme, uninstall then install
  of the debug build from 6c0f4548). Screenshots in `migrely-work/walks/192/`:

  | # | File | Font, orientation, scroll | Result |
  |---|---|---|---|
  | 1 | `01-100-portrait-top.png` | 100%, portrait, top | 1 line, inside the outline |
  | 2 | `02-200-portrait-top.png` | 200%, portrait, top | 2 lines ("Keep track of my" / "medication days"), inside the outline, the same height as the other two choices |
  | 3 | `03-200-portrait-bottom.png` | 200%, portrait, bottom | disclaimer and "Restore from a backup file" fully visible |
  | 4 | `04-100-landscape-top.png` | 100%, landscape, top | 1 line; the third row continues below the fold |
  | 5 | `05-200-landscape-top.png` | 200%, landscape, top | the third row starts at the fold; the scroll reaches it (6) |
  | 6 | `06-200-landscape-bottom.png` | 200%, landscape, bottom | label on 1 line; disclaimer and Restore visible |

  In all six: no "…", no word broken across lines, the outline encloses all the text, and the 2-line label (2, 3) keeps the
  same gap to the disclaimer as the other rows keep to each other. A full-size crop of the third row at 200% portrait
  (`crop-02-third.png`) shows the descender of "days" inside the outline.
  Behaviour (portrait, 100%):
  - Back on step 1 leaves the app (the launcher becomes the resumed activity); relaunch shows "Step 1 of 4";
  - "Skip this step" opens "Step 2 of 4"; `migrely_preferences.xml` does not exist afterwards, so no goal is stored;
  - Back to step 1, tap "Keep track of my medication days": "Step 2 of 4", and the prefs hold
    `<string name="goal">fewer_painkillers</string>`;
  - `run-as com.migrely.app kill -9 <pid>`, cold start: onboarding shows again at "Step 1 of 4".

  Restored afterwards: `wm density reset` (420), font scale 1.0, `user_rotation` 0, `accelerometer_rotation` 1, night mode
  no. The app data taken before the walk was put back and a fresh tar of it diffs empty against the backup; the installed
  build stays this branch's debug build (1.2.1, vc 11).
  Not checked: TalkBack wording and focus order (QA 7.2, as agreed); keyboard order; the label on a real phone; tapping the
  label at 200% or in landscape (the tap target is the same row, checked at 100% portrait).

## 6. Review

- [x] 6.1 `medical-reviewer` re-reviews the final diff: the string as shipped, and that no other text changed. Verify: a PASS
  quoted into BRD D177 by `product-manager`.
  Record (med-192b, 2026-11-13, diff 5bbf8f94..3fef0fdc): the string PASS; other health text PASS. One FIX: the NICE
  description overstated what NICE says. Fixed word for word by `product-manager` in BRD D177 ("dorini toʻxtatishni shifokor
  maslahat beradi va kuzatib boradi") and in the proposal's Why ("withdrawal is advised and followed up by a clinician"). No
  method of withdrawal is named anywhere. med-192b: no further review needed. The PASS is quoted in D177.
- [x] 6.2 `code-reviewer` on the diff. Verify: a packet with findings or "none", and every finding closed.
  Record (cr-192, 2026-11-13, diff 5bbf8f94..3fef0fdc): no blocking finding; one Minor, closed (relayed by `team-lead` as
  fixed). In the same commit `product-manager` corrected the proposal's QA line: the Maestro flow also checks the disclaimer and
  taps Restore, and it does not assert the title only.

## 7. Acceptance (`qa-engineer`, scope: this diff only)

- [x] 7.1 Strings: `git grep -n -i 'fewer painkillers' -- features app core designsystem` exits 1 on the candidate. Negative
  control: 1 line on 5bbf8f94. Also scan the APK resources for "Take fewer painkillers": 0 on the candidate, 1 on the base.
  Record (qa-192, 2026-11-14, f7530040): exit 1 and 0 lines; on 5bbf8f94 exit 0, 1 line. String pool `(?i)fewer\s+painkillers`:
  candidate debug 0, R8 0, base 1; the new label 1, 1, 0. The two debug APKs differ only in `resources.arsc`.
- [x] 7.2 Step 1 on the device at 100% and 200% font, 360 dp, portrait and landscape: the label is not clipped, the disclaimer and
  "Restore" are reachable, and TalkBack reads "Keep track of my medication days" as actionable, in the unchanged order. This
  covers "The label", "Large font, narrow screen, landscape" and "TalkBack and keyboard".
  Record (qa-192, 2026-11-14, emulator-5580, API 36, 360 dp): 8 of 8 geometry rows pass (4 conditions, dark theme with
  airplane mode on and off). The label is 2 lines at 200% portrait and 1 line otherwise, inside its button; buttons are
  ≥ 144 px (48 dp); Restore is reached after 0 to 3 swipes. TalkBack: "Keep track of my medication days. Button", the same
  stops as the base (100% and 200%); a double-tap opens step 2. Tab cycle as on the base. Pre-existing, filed as #217 (S4):
  Skip reads "Skip this step. Skip. Button".
- [x] 7.3 Choosing it stores `fewer_painkillers`, "Skip" stores no goal, and Back on step 1 leaves the app. This covers
  "Choosing it" and "Skip and Back on step 1".
  Record (qa-192, 2026-11-14): choose → `fewer_painkillers` (control: doctor → `record_for_doctor`); Skip → no `goal` key,
  also after all four steps; Back → the launcher resumes and the next start shows step 1. The base gives the same values.
- [x] 7.4 Upgrade: install 5bbf8f94, choose "Take fewer painkillers", finish onboarding, then update to the candidate without
  clearing data. Check:
  - home opens and onboarding does not show;
  - the prefs still hold `fewer_painkillers`;
  - no crash in logcat.

  This covers "An upgrade with the old goal stored". Control: the same run on 5bbf8f94 over itself.
  Record (qa-192, 2026-11-14): 2 of 2 pass (home, no onboarding, `fewer_painkillers` kept, 0 crash lines, a second cold
  start also home); the control passes too. Negative controls: after `pm clear` the checks see onboarding and no goal;
  `am crash` gives 1 FATAL line.
- [x] 7.5 Backup: export JSON on the candidate, with no `goal` key in the file, and import it with Add on 5bbf8f94. This covers
  "Backup and restore". The Auto Backup restore is accepted by reading the code, because D78 is unchanged; say so in the
  record.
  Record (qa-192, 2026-11-14): the export (518 bytes, schemaVersion 7, 1 attack) has 0 goal, painkiller or "medication days"
  keys or values; a planted `goal` gives 2 hits (exit 1). 5bbf8f94 with one past migraine: "It holds 1 migraine, 0
  medications and 0 marked days.", Add → "Added 1 migraine."; a second import of the file fails that assertion. Auto
  Backup: accepted by reading the code (D78 rules unchanged, the same stored key in both dex files).
- [x] 7.6 Go/No-Go packet in `docs/qa/fix-onboarding-goal-wording.md`, saying what these checks do not prove. "Offline, Free or
  Premium, dark theme" is covered by 7.2 run in airplane mode with the dark theme.
  Record (qa-192, 2026-11-14): `docs/qa/fix-onboarding-goal-wording.md`, GO at f7530040.

## 8. Close (`product-manager`)

- [x] 8.1 Record the PASS from 6.1 in D177, sync the spec, and archive. Verify:
  - `openspec validate --strict --all` exits 0;
  - `openspec/specs/onboarding/spec.md` holds "Keep track of my medication days".
  Record (`product-manager`, 2026-11-14): the 6.1 PASS (med-192b) was already quoted in D177; D177 now also records
  `ux-designer`'s CONFIRM, QA's GO at f7530040 per EC-ONB-01…06, and #217 (S4, pre-existing) as a follow-up out of scope.
  Before the archive, `origin/develop` d8194c06 (PR #216) was merged into the branch: `docs/BRD.md` only conflicted, both sides
  kept in numeric order (§13 rows 44, 45; D175, D176, D177). The change then validated and was archived; the commands and
  counts are in the commit and the handoff packet.
