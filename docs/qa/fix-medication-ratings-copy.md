# QA record: `fix-medication-ratings-copy` (#205, S3, D174), tasks 7 and 8

## Decision

**QA's verdict, 2026-11-12 at 15:55: PASS for `bugfix/205-medication-ratings`.** App code at 8dd0c04c (base e788d68e); the
commits after it change only `.maestro/`, `qa/`, `docs/qa/` and the task ticks. Nothing was filed against this change.

- Insights heads the medication section "How you rated your medications". It is whole, with no "…", at 100% and 200% font,
  360 dp, in portrait and landscape. TalkBack reads it as "How you rated your medications. Heading", and reads each
  medication as one stop with its answers line.
- The Ibuprofen line still reads "Helped a lot or completely: 7 of 9 answers (78%)", and Sumatriptan "Needs 3 answers (has 2)".
- The Insights paywall shows the spec's lead word for word, and the benefit row recorded in task 1.3.
- An update from e788d68e keeps every count and percentage, and only the heading's words change.
- No text in the candidate APKs says "what helped" or "shows your triggers", in any letter case. The base APK has 3 such
  strings.
- **Negative controls:** the updated Maestro flow and the device scenarios fail on e788d68e, at the changed texts only.
  The old flow fails on the candidate.

Scope follows the owner's rule of 2026-11-09: only what the diff reaches. The product diff is 3 strings in 2 `strings.xml`
files, 2 KDoc comments and 1 test name. No logic, layout, data, permission or Gradle change.

## Builds

Built at 8dd0c04c and e788d68e with `--no-daemon` and no `keystore.properties`, so R8 is signed with the debug key (D102).
`aapt2 dump resources` read the three strings in each APK. The base debug APK is byte-identical to the #200 record's
candidate (`fix-safety-copy-followups.md`), because e788d68e merges that tree.

| APK | Commit | sha256 | The three strings |
|---|---|---|---|
| `new-debug.apk` | 8dd0c04c | 43590efb5bbcea49147d66a8d795594a216d471f288e71c87d42e0cb4ddfaa78 | new |
| `new-r8.apk` (not debuggable) | 8dd0c04c | aa3d89a1ca0ff1ed0e5b89d1c1ef1d0c0eaa1a87d1dd762c4bd35efa98f64cf4 | new |
| `base-debug.apk` (control) | e788d68e | 96b87815cdd152d61c4791640980e65814fb7b633f23c80df94e98c0c2bcc15b | "What helped", old lead, old row |

Whole-APK scan (resources table and every `.dex`, regex `(?i)what\s+helped|shows\s+your\s+triggers`): new-debug 0 and 0,
new-r8 0 and 0, base 3 and 0.

## Static checks (8.1, 8.2)

| Check | Candidate | e788d68e (control) |
|---|---|---|
| `git grep -n -i 'what helped' -- features app core` | exit 1, 0 lines | exit 0, 5 lines |
| `git grep -n -i 'shows your triggers' -- features app core` | exit 1, 0 lines | exit 0, 1 line |
| `git diff e788d68e 8dd0c04c -- features \| grep -E '^[-+].*(insights_helped\|insights_needs_answers\|Did it help)'` | exit 1, no output | |
| `git grep -n -i 'what helped' -- .maestro qa` (7.2) | exit 1 | 4 hits before task 7 |

The `.kt` changes are KDoc on `InsightsCalculator.medications` and `MedicationsSection`, plus the test's name.
`git grep` misses a phrase that a line break splits. A multi-line scan of `.maestro`, `qa`, `docs/qa`, `features`, `app` and
`core` found one: `qa/seed_expect.py:11-12`, a docstring that task 7 fixed.

## QA files (task 7)

- `.maestro/subscription/paywall-and-purchase.yaml`: asserts the new lead, then scrolls to the benefit row and asserts it.
  After each of the two, it asserts that `(?i).*(what\s+helped|shows\s+your\s+triggers).*` is not visible. The flow now
  covers "The Insights lead" and "The benefit row".
- `qa/device_scenarios.sh` › `insights`: `# Covers:` lines for the renamed requirement (Ibuprofen, The heading, Fewer than three
  answers). The expected texts now include the heading. A grep in any letter case fails on the old heading.
- `qa/device_scenarios.sh` › new `insights_upgrade`. It runs on demand and in a full run when `QA_OLD_APK` is set. The older
  build gets the insights-patterns seed and Premium, and its Insights texts are read. The newer build is then installed over
  it with `install -r`. Insights must show the same texts under the new heading, and no old heading. It covers "Upgrade or
  restore". The first launch is by the launcher, so the Android 8.0 clearState trap cannot empty the database.
- `qa/seed_backup.py` prints the section as "How you rated your medications:". `qa/seed_expect.py` turns that line into an
  expected screen text. `--self-test`: ok, 15 seeds on 5 dates.
- The guards use `\s+` and `[[:space:]]+`. A grep of `.maestro` and `qa` for the old copy therefore finds only stale
  expectations, not the guards.
- `python3 -I qa/e2e_coverage.py`: exit 0, no stale Covers line. subscription 24/78 (was 22), trigger-insights 11/15 (was 8).
  While the change is open, the 15 still counts the old name "What helped".

## Device runs (8.2 to 8.5), emulator-5580, API 36, candidate debug unless stated

Chain `migrely-work/qa-205/chain.sh`. Each step has a time limit and expects pass or fail. The host load stayed at 4 to 10.

| Step | Build | Want | Got | What it shows |
|---|---|---|---|---|
| Paywall flow, run 1 and 2 | new | pass | 2 of 2 pass (1 m 5 s each, `tests="1" failures="0"`) | 8.4: the lead, the benefit row and both guards pass. Close works after the scroll |
| Paywall flow as of 8dd0c04c (old lead) | new | fail | fails at the old lead | the merge blocker was real |
| Probe `assertVisible "(?i).*WHAT\s+HELPED.*"` | new | fail | fails | nothing on the Insights paywall says it in any case |
| Lead guard alone | new | pass | pass | |
| Row guard alone (`below: "What Premium adds"`) | new | pass | pass | |
| Paywall flow (new) | e788d68e | fail | **fails** at the new lead | the flow can fail |
| Paywall flow (old) | e788d68e | pass | pass | the control is sound |
| Probe `(?i).*WHAT\s+HELPED.*` | e788d68e | pass | pass | Maestro's `(?i)` ignores letter case |
| Lead guard alone | e788d68e | fail | **fails** on the guard | the first guard can fail |
| Row guard alone | e788d68e | fail | **fails** on the guard, below "What Premium adds" | the second guard catches the old row, not just the lead |
| `device_scenarios.sh insights` | new | pass | pass: insights-patterns 24 texts seen (heading, Ibuprofen 7 of 9 (78%), Sumatriptan Needs 3 (has 2)); insights-three 1 | 8.2 on device |
| `device_scenarios.sh insights` | e788d68e | fail | **fails**: the only text missing is "How you rated your medications" | the heading check can fail. The case-insensitive grep matches "What helped" in that run's screen texts (exit 0) and not the candidate's (exit 1) |
| `insights_upgrade`, run 1 and 2 (old e788d68e, new candidate) | both | pass | 2 of 2 pass. Before: 23 texts, heading "What helped". After: the same 23 texts and "How you rated your medications", with Premium and data kept | 8.5 |
| `insights_upgrade` control (e788d68e over itself) | base | fail | **fails** after the update: the new heading is not on screen | the "after" check can fail |

### Heading geometry (8.3)

Harness `migrely-work/qa-205/h83.py`. Settings were `wm density 480` (360 dp short side), `font_scale` 1.0 and 2.0, and
`wm user-rotation lock 0` or `1`. The app was opened with `am start`, and the screen swiped until the heading and the
Ibuprofen line were whole on screen. Screenshots were reviewed: `migrely-work/qa-205/shots/insights-*.png`, and the crops in
`heading-crops.png`.

| Condition | Screen (px) | Heading bounds | Height | Lines | Clipped or "…" |
|---|---|---|---|---|---|
| 100%, portrait | 1080x2400 | [96,1785][896,1850] | 65 | 1 | no |
| 100%, landscape | 2400x1080 | [524,550][1324,615] | 65 | 1 | no |
| 200%, portrait | 1080x2400 | [96,1019][984,1252] | 233 | 2 | no |
| 200%, landscape | 2400x1080 | [524,439][1861,547] | 108 | 1 | no |

The Ibuprofen line read 7 of 9 (78%) in all four, and no text matched "what helped" in any case.

### TalkBack (8.3)

TalkBack 16, speech read from its VERBOSE log (`Actors: act() … action=SPEAK text=…`). Taps and swipes went in through the
emulator's touch hardware (`adb emu event mouse`). The emulators run without audio. Same results at 100% and 200% font,
portrait, 360 dp:

| Action | Spoken |
|---|---|
| Touch the heading (twice) | "How you rated your medications. Heading" |
| Touch Ibuprofen | "Ibuprofen. Helped a lot or completely: 7 of 9 answers (78%)" (one stop, no "Heading") |
| 3 right swipes from the heading | "Ibuprofen. Helped a lot or completely: 7 of 9 answers (78%)", "Sumatriptan. Needs 3 answers (has 2)", "The last 12 months. Heading" |
| 3 left swipes back | Sumatriptan, Ibuprofen, "How you rated your medications. Heading" |
| Control: touch "Often logged with your migraines" | "Often logged with your migraines. Heading" |

## What this does not prove

- **Clinical truth and comprehension.** The words follow the medical-reviewer's PASS (D174), not a clinician (T31 is open;
  nothing may say "medically reviewed"). Whether a person reads "How you rated your medications" as their own ratings and
  not as advice was not measured.
- **Other surfaces still say "what helped"** until the D174 follow-ups land: the Play listing and screenshot 05
  ("Learn what really helps"), `docs/release/listing.md:46`, `docs/release/play-products.md:16`, the promo video and its
  transcript on the site, and social post 05. None was checked here. They belong to `aso-seo-specialist` and
  `content-creator`, and the Play part needs the owner.
- **API 26:** not run. The diff changes only text, and the same Compose `Text` with no `maxLines` wraps the same way. But
  API 26 scales 200% font linearly, unlike API 34+, so its 200% heading is larger than measured here.
- **R8 build on a device:** not run. It has no simulated store, so Insights and its paywall cannot open without Google Play.
  The R8 APK was checked only by `aapt2` and the APK scan.
- **The paywall at 200% font** and the benefit-row fallback (EC-INS-04) were not re-measured by QA. The full row shipped on
  the engineer's evidence (task 1.3 screenshots). The flow runs at 100% only.
- **"No claim on any paywall"** was checked on screen for the Insights paywall only (the lead area and the benefit list). For
  the onboarding and report paywalls, offline or online, the proof is the APK scan: no resource or dex string holds either
  phrase. A string built from separate parts at run time would escape that scan. None of the diff's strings is built that
  way.
- **"No attack medication"** (the heading with no medication) was not run. It is the same `Section` call and an unchanged
  string.
- **Restore half of "Upgrade or restore":** the seed comes in through the app's backup import on both builds. A backup
  written by the old build and imported into the new one was not run. No backup, schema or data code is in the diff.
- **Upgrade path:** from an e788d68e debug build to the candidate debug build, both versionCode 11 with the debug key. Not
  from the shipped 1.1.1 or 1.2.1 release builds; that is the release Go/No-Go's upgrade row.
- **The 1-answer plural** of `insights_helped` cannot show. A medication is "Rated" only from 3 answers
  (`ANSWERS_NEEDED = 3`), so English always uses "other".
- **Speech:** TalkBack's log shows what goes to speech, not what a voice says. Other screen readers and TalkBack on API 26
  were not run.
- **Full regression, monkey, a11y audit, permissions:** not run (owner rule of 2026-11-09). The nightly develop run covers
  them.

## Notes

- **Register:** `python3 -I qa/e2e_coverage.py --register docs/qa/test-strategy.md` already exits 1 on e788d68e, with 6
  stale capability rows and the overall total. Those rows come from changes merged before this one. This change moves
  subscription and trigger-insights again, and the archive (task 9) changes the totals once more. The register is due at the
  next release Go/No-Go, after the archive.
- **Voided runs (not counted above):**
  - The first chain ran shell functions through `perl -e 'exec'`. The exec failed silently and the steps "passed" without
    running. Two chains also overlapped on the device. Both chains were killed by exact PID and every step rerun.
  - The first row probe assumed the lead scrolls away; at 100% font it never does. It was replaced by the `below:` probe.
  - The first `insights_upgrade` run lacked the onboarding skip.
  - Three TalkBack attempts used `input tap`, which TalkBack 16 does not receive while "Handle gestures in TalkBack" is on.
    No speech was logged, so those attempts were not counted. `adb emu event mouse` works.
- **Device state:** emulator-5580 was started for this run and turned off at the end. Font scale, density, rotation,
  TalkBack (off, log level back to ERROR), animator scales, zen mode, a11y timeouts and auto time match the values recorded at
  the start (`state-5580-before.txt` = `state-5580-after.txt`). The candidate debug build stays installed. emulator-5582 was
  not used.
- **Evidence:** `migrely-work/qa-205/` (`results.txt`, `logs/`, `shots/`, `h83-geometry.json`, `talkback-100.json`,
  `talkback-200.json`, probe flows in `flows/`). Scenario texts (copied from `build/qa/device/`) in `migrely-work/qa-205/device/`: `20261112-150152`
  (candidate), `-151259` (base), `-151736` and `-152547` (upgrade), `-152153` (upgrade control).
