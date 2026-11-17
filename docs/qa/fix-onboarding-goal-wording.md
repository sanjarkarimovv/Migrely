# QA record: `fix-onboarding-goal-wording` (#192, S4, D177), task 7

## Decision

**QA's verdict, 2026-11-14 at 11:40: GO for `bugfix/192-onboarding-goal` at f7530040** (base 5bbf8f94). The app code at
that SHA differs from the base by one line, `onboarding_goal_painkillers` in
`features/onboarding/ui/src/main/res/values/strings.xml`. The commit that adds this record changes only `docs/qa/` and the
task ticks. Nothing was filed against this change. One issue was filed for something older that this change did not cause:
#217 (S4, accessibility). On step 1, TalkBack reads "Skip this step. Skip. Button". 5bbf8f94 does the same.

- Step 1 reads "Keep track of my medication days". It is whole, inside its outline, with no "…" and no word broken across
  lines, at 100% and 200% font, 360 dp, in portrait and landscape. The disclaimer and "Restore from a backup file" can be
  reached by scrolling. All touch targets are 48 dp or more.
- TalkBack reads "Keep track of my medication days. Button", "Double-tap to activate". The order is the same as on the base,
  stop for stop. Only this one label differs.
- Choosing it stores `fewer_painkillers`. "Skip" stores no goal. Back on step 1 leaves the app.
- An update from 5bbf8f94 with "Take fewer painkillers" already stored opens home. The stored value stays
  `fewer_painkillers`, and nothing crashes.
- A JSON backup from the candidate holds no goal. 5bbf8f94 imports it with Add.
- **Negative controls:** each check below fails where it should. The grep and the APK scan find the old label on the base.
  The upgrade check sees onboarding and no goal after `pm clear`. The crash check sees `am crash`. The backup check flags a
  planted `goal` key. The import expectation fails on a second import of the same file.

Scope follows the owner's rule of 2026-11-09: only what the diff reaches. There is no logic, layout, data, permission or
Gradle change. The two debug APKs differ only in `resources.arsc` (`diff -rq` of the unzipped APKs). Every `classes*.dex` is
byte-identical.

## Builds

Built in a detached QA worktree with `ANDROID_HOME=$HOME/Library/Android/sdk nice -n 19 ./gradlew --max-workers=2 -q`.
There was no `keystore.properties`, so the R8 build is signed with the debug key (D102). All three APKs are versionCode 11,
1.2.1, and have the same signer (SHA-256 `7eae4e48…bfbabd`). Each install was checked on the device by `sha256sum` of
`base.apk` against the local file. That was 31 installs, and every one matched.

| APK | Commit | sha256 | `onboarding_goal_painkillers` |
|---|---|---|---|
| `cand-debug.apk` | f7530040 | 5aa6544e9426ccdeea8b0dacdb77c61f11bf6714bb1d345e74cc424b75121e11 | "Keep track of my medication days" |
| `cand-release.apk` (R8, not debuggable) | f7530040 | fa49b1eec5af6033e48a92ae378da00511bf18a82577499e1fb2da6278a2eccc | "Keep track of my medication days" |
| `base-debug.apk` (control) | 5bbf8f94 | 394abd0e4b4f2e7c2529e31182b623d12153e7a782e04b3ef7a7f3d819fc5e6c | "Take fewer painkillers" |

## Results

Device: `emulator-5580`, `Migrely_QA_API_36` (1080x2400), cold boot, `qa/reset_device.sh` first. The chain
`migrely-work/qa-192/chain.sh` held the `qa/lib.sh` device lock and put a time limit on every step. The host load was 3 to
50 while the upgrade steps ran (they passed), and 3 to 7 for everything else.

| Task | Check | Candidate | Control (5bbf8f94 or a planted fault) |
|---|---|---|---|
| 7.1 | `git grep -n -i 'fewer painkillers' -- features app core designsystem` | exit 1, 0 lines | exit 0, 1 line (`strings.xml:9`) |
| 7.1 | APK string pool (`aapt2 dump strings`), `(?i)fewer\s+painkillers` | debug 0, R8 0 | 1 |
| 7.1 | The new label in the string pool | debug 1, R8 1 | 0 |
| 7.1 | Every `classes*.dex`, the old phrase / the stored key `fewer_painkillers` | 0 / 1 (debug and R8) | 0 / 1 |
| 7.2 | Geometry, 4 conditions × 2 passes (dark theme with airplane mode on, and again with them off) | 8 of 8 pass | base, dark and offline: 4 of 4 pass (the old label) |
| 7.2 | TalkBack, the step from the counter onward, 9 right swipes, 100% and 200% | 2 of 2: same stops as the base | base 100%: 1 run |
| 7.2 | TalkBack: touch the third choice, then double-tap | "Keep track of my medication days. Button", "Double-tap to activate". Step 2 opens and `fewer_painkillers` is stored (2 of 2) | "Take fewer painkillers. Button" (1 of 1) |
| 7.2 | Keyboard Tab cycle (10 Tabs, 2 Shift+Tabs) | 3 of 3: Skip, the three choices, Restore, and back to Skip | 3 of 3: the same cycle |
| 7.3 | Choose / Skip / Back / process death | 1 of 1 pass | base 1 of 1, the same values |
| 7.4 | Update 5bbf8f94 → f7530040 with the old goal stored | 2 of 2 pass | 5bbf8f94 over itself: pass. After `pm clear`, the checks see onboarding and no goal |
| 7.4 | Crash check (`logcat -b crash`, `AndroidRuntime`) | 0 lines in every run | `am crash` gives 1 `FATAL EXCEPTION` line |
| 7.5 | JSON export on the candidate, keys or values matching `(?i)goal\|painkiller\|medication\s+days` | 0 hits, exit 0 | a copy with `"goal": "fewer_painkillers"` planted: 2 hits, exit 1 |
| 7.5 | Import with Add on 5bbf8f94 | "It holds 1 migraine, 0 medications and 0 marked days.", then "Added 1 migraine." | the same file again: the "Added 1 migraine." assertion fails |

### 7.2 Geometry

Settings were `wm density 480` (a 360 dp short side), `font_scale` 1.0 and 2.0, and the rotation locked at 0 or 1. The
app was opened fresh with `pm clear` and the launcher intent. uiautomator reports only the visible part of a clipped node.
So each element was measured at the scroll position where its whole button was inside the scroll viewport. The first pass
measured at the top of the screen. It cut the third row and Restore at 200% landscape, and was replaced; its rows are not
counted. 48 dp is 144 px at 480 dpi.

| Font, orientation | Label text height (px) | Lines | Its button (px) | Restore button (px) | Swipes to Restore |
|---|---|---|---|---|---|
| 100%, portrait | 61 | 1 | 168 | 144 | 0 |
| 100%, landscape | 61 | 1 | 168 | 144 | 2 |
| 200%, portrait | 233 | 2 ("Keep track of my" / "medication days") | 281 | 288 | 1 |
| 200%, landscape | 105 | 1 | 168 | 168 | 3 |

The candidate's numbers are the same in the dark-and-offline pass and the system-light pass. The base gives the same
numbers for "Take fewer painkillers", which also takes 2 lines at 200% portrait. So the new label adds no height. The text
lies inside its button in every condition. The screenshots were reviewed (`shots/g2-*.png`): no "…", and no word split.

**Theme and network.** On step 1 the app is always dark: the stored theme defaults to Dark (`PreferencesDataSourceImpl`
line 79), and a fresh install has no other theme yet. It stayed dark with the system set to light. With `cmd uimode night
yes` and airplane mode on, the label's glyph colour is rgb(138, 143, 152) on rgb(15, 17, 21), about 5.8:1. The base's old
label gives the same values, so the colours and contrast are unchanged. Step 1 has no network or Premium code path:
`git grep -i -E "premium|entitlement|billing|connectivity|network" f7530040 -- features/onboarding` exits 1.

### 7.2 TalkBack and keyboard

TalkBack 16. Its log level was set to VERBOSE through its developer settings and put back to ERROR afterwards. Speech was
read from the `Actors: act() … action=SPEAK` lines. Touches went in through `adb emu event mouse`. The candidate gave the
same stops at 100% and at 200%. The base gave the same stops except for its own label:

| Action | Spoken |
|---|---|
| Touch the step counter | "Step 1 of 4" |
| Right swipes 1 to 7 | "Skip this step. Skip. Button" (#217), "What brings you to Migrely?. Heading", "Keep a record for my doctor. Button", "Find what triggers my migraines. Button", **"Keep track of my medication days. Button"**, "Migrely is a headache diary. It doesn't diagnose or treat.", "Restore from a backup file. Button". Each button also gets "Double-tap to activate" |
| Swipes 8 and 9 | nothing (the end), then "Step 1 of 4" |

The keyboard was driven with `input keyevent 61` and `keycombination 59 61`. The focused node's labels were read with
uiautomator. The Tab cycle on both builds is Skip, doctor, triggers, the third choice, Restore, and back to Skip. Shift+Tab
moves back. The stop where the *first* Tab lands depends on timing on both builds: Skip or the first choice, varying with
the wait before the first key (base: 2 of 4 runs Skip, 2 of 4 doctor; candidate: Skip at 0 s, doctor at 4 s). Since the
code is byte-identical, this is not caused by the change. It is noted here; no issue was filed for it.

### 7.3 Choosing, Skip, Back (candidate; the base gave the same values)

- The goal is unset at step 1. Tapping "Keep track of my medication days" opens step 2 and stores
  `<string name="goal">fewer_painkillers</string>`. Control: "Keep a record for my doctor" stores `record_for_doctor`.
- "Skip" opens step 2. The preferences file does not exist yet. After all four steps are skipped and the paywall is closed,
  the keys are `onboarding_finished`, `paywall_last_shown` and `paywall_shown_onboarding`. There is no `goal`.
- Back on step 1 leaves the app: the launcher is the resumed activity. The next start shows "Step 1 of 4", with no goal.
- HOME, then `run-as com.migrely.app kill -9 <pid>`, then the launcher: onboarding shows again. It is at "Step 2 of 4",
  with the chosen goal kept.

### 7.4 Upgrade

The old build was installed fresh. "Take fewer painkillers" was chosen, the other steps were skipped, the paywall was closed,
and home opened. Then `adb install -r` of the new build. The keys before and after were `onboarding_finished`, `goal`
(`fewer_painkillers`), `paywall_last_shown` and `paywall_shown_onboarding`. The first start after the update opened home
("Today: …"), with no "Step N of 4". So did a second cold start. Crash lines: 0. Runs: up1 and up2 (base → candidate)
passed. The control (base → base) passed in the same way.

### 7.5 Backup

`75-cand-export.yaml` covered: a fresh candidate, the new goal chosen, one attack logged today, and Your data, then "Export
a backup (JSON)" to Download. The file `migrely-2026-11-14.json` is 518 bytes. It has `format` `migrely-backup` and
`schemaVersion` 7, with 1 attack and empty `customTags`, `medications`, `intakes`, `dayMarks` and `doctorVisits`. It has 21
distinct keys and no goal. The preferences held `fewer_painkillers` at the time of the export. On a fresh 5bbf8f94 install
with one past migraine, importing that file showed the summary and "Added 1 migraine." after Add. Crash lines: 0.

**Auto Backup restore** was accepted by reading the code, as task 7.5 allows. D78 is unchanged: `backup_rules.xml` and
`data_extraction_rules.xml` include `sharedpref`, and the diff touches neither. The stored key is the same string in the
base and candidate dex (`fewer_painkillers`, 1 occurrence each), so a restored value maps to the same `GoalDataModel`. No
Android Backup transport was run.

## What this does not prove

- **Clinical truth and comprehension.** The wording rests on medical-reviewer's PASS (6.1, D177), not on a clinician
  (T31 is open). Whether people read "Keep track of my medication days" as a diary goal and not as advice was not measured.
- **API 26:** not run. The diff is text only, and the same `Text`, with no `maxLines`, wraps in the same way. But API 26
  scales a 200% font linearly, so its 200% layout is larger than the one measured here.
- **A real phone:** not run, and neither was any screen narrower than 360 dp or any display size above the default.
- **The R8 build on a device:** not run. It was checked only with `aapt2` and the dex scan. The release path (Play Billing)
  cannot run on the QA emulator.
- **Upgrade path:** from a 5bbf8f94 debug build to the candidate debug build, both versionCode 11 with the debug key. The
  update from the shipped 1.1.1 or 1.2.1 release builds was not run. That is the release Go/No-Go's upgrade row (1.3.0).
- **Auto Backup restore** to a new phone: read in the code, not run (see 7.5).
- **Import with Replace** and older versions than 5bbf8f94 reading the candidate's file: not run. The file format does not
  change in this diff.
- **Light theme on step 1:** not run. It can show only when an Auto Backup restore brings back a stored Light theme. Then
  the same colour tokens apply as on every other light screen.
- **Speech:** TalkBack's log shows what is sent to speech, not what a voice says. Switch Access, other screen readers and
  TalkBack on API 26 were not run.
- **Other surfaces** that may still say "fewer painkillers", such as video and Reels frames, are out of this change's scope
  (proposal, Out of scope). Only the app's sources and APKs were scanned.
- **Full regression, monkey, a11y audit, permissions:** not run, per the owner's rule of 2026-11-09. The nightly `develop`
  run covers them. The engineer's gate (task 4.1) is the record for unit tests, lint and permissions.

## Notes

- **Merge into develop:** `git merge-tree --write-tree d8194c06 f7530040` reports a conflict in `docs/BRD.md` only, in two
  places. One is §13 row 44 (develop) beside row 45 (this branch). The other is D175 and D176 (develop) beside D177 (this
  branch). These rows sit next to each other and their numbers do not collide, so keeping both sides resolves it. The app
  code merges cleanly, because develop has not changed `features/onboarding` since 5bbf8f94.
- **Voided runs (not counted above):**
  - The first geometry pass measured from the top of the screen and cut the third row at 200% landscape (see 7.2).
  - The first TalkBack pass logged no speech, because TalkBack's log level was still ERROR on the fresh boot. Its
    double-tap rows were kept: step 2 opened and the goal was stored.
  - In the first chain, the backup checker's path was wrong. It was rerun on the same exported file.
- **Environment:** the host disk had 0.6 GB free at the start, so the emulator refused to boot. The run waited until
  about 4.8 GB was free. QA's own worktree build outputs were deleted (about 440 MB). Nothing else was deleted.
- **Device state:** emulator-5580 was cold booted for this run and shut down at the end. The font scale, density, rotation,
  TalkBack (off, log level back to ERROR), night mode, airplane mode, animator scales, zen mode, a11y timeouts, auto time and
  locale match the values recorded at the start (`state-5580-before.txt` = `state-5580-final.txt`). emulator-5582 was not
  used.
- **Evidence:** in `migrely-work/qa-192/`:
  - `results.jsonl`, with every record;
  - `chain-run1.log` and `chain-run2.log`;
  - `71-source.txt`, `71-apkscan.txt`, `75-check.txt`, `merge-check.txt` and `static-code.txt`;
  - `75-export.json`;
  - `shots/`;
  - `logs/` (Maestro);
  - the harness: `q192.py`, `chain.sh`, `run.sh`, `state.sh`, `apkscan.py`, `backup_check.py` and `flows/`.
