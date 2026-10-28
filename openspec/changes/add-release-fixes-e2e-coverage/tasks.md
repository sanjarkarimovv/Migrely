# Tasks

## 1. Requirements and the Go/No-Go record

- [x] 1.1 BRD §13 row 13c, §17 D203 and D204. Verify: `grep -n "| 13c |\|| D203 \|| D204 " docs/BRD.md`
- [x] 1.2 `docs/qa/go-no-go-1.0.0.md` for build 4's Play state:
  - the closed testing release and managed publishing, with Publish held for QA's verdict;
  - line 11 is the closed track's pre-launch report, read when it is out (build 4 went to closed testing first);
  - line 12 is waived by the user on 2026-10-27.
  Verify: `grep -n "waived\|managed publishing\|pre-launch" docs/qa/go-no-go-1.0.0.md`

## 2. Release-fix scenarios

- [x] 2.1 `qa/device_scenarios.sh privacy_link` (design D2): the link to Chrome, and "No browser" with Chrome disabled.
  `restore()` and `qa/reset_device.sh` enable Chrome again. Verify: passes twice; after a run stopped half-way
  (Ctrl-C while Chrome is disabled), `pm list packages -d` does not list Chrome
- [x] 2.2 `.maestro/day-markers/track-periods-off-marks.yaml` (design D3). Verify: passes 3 times; it fails when step 3's
  check is pointed at a day that keeps its mark (mutation of the flow, reverted)
- [x] 2.3 Seed `report-heavy-old-notes` in `qa/seed_backup.py`, with its self-test, and its check in `report_heavy`
  (design D4). Verify: the self-test recounts it; `report_heavy` passes twice
- [x] 2.4 "Google Play offers no products" in a new scenario `premium_no_products`, as D122 is on develop (PR #85,
  d156579; design D5). Verify: passes twice on a develop debug build

## 3. Accessibility audit on every screen (D203)

- [x] 3.1 Tour states `onboarding-first`, `settings-main`, `settings-warning-signs`, `settings-privacy`,
  `settings-privacy-link`, `settings-theme`, `medications-list`, `reminders` and `your-data` (design D1). Verify:
  `qa/a11y_audit.sh <state>` captures each at 100%, 200% and landscape
- [x] 3.2 Tour states `paywall`, `report-period` and `insights` through the simulated store. Verify: as 3.1
- [x] 3.3 The full audit on the 1.0.1 candidate's debug APK (line 4 of its record, task 7.2). Every failure is filed as an `accessibility` issue with its capture,
  and every "not judged" line is looked at. Verify: the audit's summary in the verification record

## 4. Release-candidate checks and residual risk (D204)

- [x] 4.1 Lint and the instrumented tests on build 4 (design D7), with the results and the #74 exception in the
  Go/No-Go record. Verify: the record's lines name the counts and #74
- [x] 4.2 `qa/split_apks.sh` and the device specs in `qa/fixtures/device-specs/` (design D8), on the 1.0.1 bundle (build 4
  went to closed testing before the script ran). Verify:
  - it refuses emulator-5570;
  - `pm path` lists 4 APKs for each profile;
  - the smoke suite and the D117 checks pass on ru-26 (emulator-5582) and on he-36, uz-36 and en-36 (emulator-5580);
  - the system language is back afterwards
- [x] 4.3 The residual-risk section in `docs/qa/go-no-go-1.0.0.md` (design D9), and line 9's new pass condition in
  `docs/QA.md` §5. Verify: the section lists every capability under 50% from `qa/e2e_coverage.py`, D42, C3–C5 and the
  checks that could not run
- [x] 4.4 `docs/qa/test-strategy.md` (design D10). Verify: every capability of `openspec/specs/` has a row (a script
  compares the two lists); every level has its tool, owner and when

## 5. Exploratory testing on build 4

- [x] 5.1 The core-flow sweep (design D6): every one of the 92 scenarios is PASS, FAIL or NOT RUN with a reason, and every
  edge case has notes. Verify: the results in the verification record; an issue for every defect, with its blocker
  verdict sent to the dev session
- [ ] 5.2 Charters 1–4 of design D6, each with notes: what was tried, and what was found. Verify: the notes in the
  verification record; an issue for every defect

## 7. The 1.0.1 hotfix candidate (design D11)

- [x] 7.1 Retests of #77, #78, #79, #80 and #81 on the candidate's debug and R8 APKs, twice each. Also the dev session's
  regression case: a paste whose first letter matches the text after the cursor, then one DEL beside an emoji. Verify:
  the retest table in `docs/qa/go-no-go-1.0.1.md`
- [x] 7.2 Lines 1–10 of `docs/QA.md` §5 on the candidate:
  - line 2a and line 10 use the upload-signed bundle that goes to Play;
  - line 6a upgrades from build 4.
  Verify: each line's result in the record, and its section "What these checks do not prove"
- [ ] 7.3 Lines 11 and 12:
  - line 11 is the pre-launch report of 1.0.1's closed track, read before the build is promoted to production;
  - line 12 records the user's decision.
  Verify: both lines in the record
- [x] 7.4 The verdict, "Go, with these risks open" or No-Go with the failing line and its issue, sent to the dev session
  before the hotfix is finished. After the tag, `git diff <candidate> v1.0.1` is empty (D125). Verify: the record's
  decision, and the diff

## 6. Documentation and verification

- [x] 6.1 `docs/QA.md`:
  - the audit's screen list (D203);
  - `privacy_link` and the new seed;
  - §5's new lines (lint, instrumented, split APKs) and the residual-risk rule (D204).
  Verify: `grep -n "D203\|D204\|privacy_link\|report-heavy-old-notes\|split_apks" docs/QA.md`
- [x] 6.2 Coverage, `qa/resource_names.py`, `openspec validate --strict`, smoke, and every new flow and scenario as its
  task says. Verify: the verification record below
- [x] 6.3 Review (`superpowers:code-reviewer`), with every finding fixed or answered. Verify: a re-run of what each fix
  touched
- [x] 6.4 Gate `./gradlew assembleDebug assembleRelease test ktlintCheck detekt --no-daemon` after merging develop.
  Verify: exit 0

## Verification record

**5.1, the core-flow sweep on build 4's R8 APK** (emulator-5580, 2026-10-27, 11:42–14:38): `sweep-a.md`.
- **The 92 scenarios:** 79 PASS (5 of them PASS*), 0 FAIL, 13 NOT RUN, each with its reason.
- **App crashes or ANRs:** none.
- **Issues:** #78, #79, #80, #81, #82, #83, #84. Every one has "launch blocker: no", sent to the dev session.

**Sweep 2 on build 4** (emulator-5580, 2026-10-27, until 16:52): `sweep-b.md`.
- **The scope:** cut twice for the 1.0.1 candidate, after data-backup, sweep A's leads and the delete retries. The rest of
  batch B is NOT RUN, moved to the 1.0.1 build and to QA 13d.
- **medication-overuse:** 11/11 PASS, every threshold and text exact.
- **data-backup:** 2 PASS; "A new phone" is NOT RUN, since it needs a real Google backup.
- **App crashes:** none.
- **Issues:** #87 and #88 (S4). No blocker.

**2.1, `privacy_link` on build 4's debug APK:** it fails 2 of 2 runs (14:42 and 14:43) on a real defect, #77 (S2,
launch blocker: no).
- The app crashes with `IllegalArgumentException: Can't open https://migrely.health/privacy/.` from
  `AndroidUriHandler.openUri`.
- Chrome was enabled again after both runs. Before them, `pm list packages -d` did not list Chrome.
- The fix is on `hotfix/1.0.1` (590e5d7). The scenario's pass is its retest on that build.

**2.1 on 1.0.1** (ed695424 = `v1.0.1`, debug APK, emulator-5580, 2026-10-27):
- **The flow:** `privacy-policy-link.yaml` first failed 2 of 2 in the retest. It tapped "Settings" during home's 1 s mode
  guard, so Settings never opened. It now waits for home and the guard.
- **After that fix:** `privacy_link` passed 2 of 2 (22:26 and 22:28): Chrome opened the link, and with no browser
  Migrely stayed in front.
- **Ctrl-C:** sent to the script's process group 56 s in, while Chrome was disabled. The script stopped with exit 1, and
  `pm list packages -d` then did not list Chrome.

**2.2 on 1.0.1** (same build and emulator):
- **The first run:** the flow failed in line 3's regression. Its `text: "Period today", checked: true` matched the label,
  and the state sits on its clickable parent.
- **The fix:** the checks now use `containsChild`, as `.maestro/README.md` says.
- **After that fix:** passed 3 of 3 (22:19, 22:21, 22:22).
- **The mutation:** step 3's check pointed at the past day that keeps its mark. The flow failed at that step ("Thursday,
  October 1: nothing logged. Period." is visible). The mutation lives in a scratch copy, so nothing needs reverting.

**7.1, the 1.0.1 retests:** every issue passed twice on each APK. See the retest table in `docs/qa/go-no-go-1.0.1.md`.

**3.1–3.3, the accessibility audit on 1.0.1's debug APK** (ed695424, emulator-5580, 2026-10-28):
- **The full audit, 00:21–01:06:** 22 states at 100% and 200% font and in landscape. Of the 66 captures, 59 were
  taken, with `0 failure(s)`. The other 7, of `insights`, `medications-list` and `settings-theme`, failed in their
  tour flows.
- **The tour fixes:**
  - `add-past-attack-on`: Maestro read `${DAY}, [0-9]{4}` as one script up to the last brace, so the regex is
    `[0-9]+`. In landscape, OK is below the time dial, so the dialog is scrolled to it, right of the dial;
  - `insights`: picks days 15–21, which are always in the third or fourth row. The landscape date picker hides the
    first row (#101);
  - `medications-list`: taps Done, since "Medication taken" does not time out. `add-first-medication` scrolls to "Add
    and log";
  - `settings-theme`: scrolls to Theme;
  - `buy.yaml`: scrolls to the buy button.
- **After the fixes:** each of the 3 states passed twice in all 3 configurations, with 0 failures. The flows that share
  the changed subflows passed: the 6 medication flows (05:23–05:32) and `paywall-and-purchase` (04:53).
- **Not judged:**
  - 27 labels are cut off in every capture of their screen. Each belongs to a component that is judged in full
    elsewhere;
  - 459 merged nodes were measured with the audit's own contrast method. 449 are at least 4.5:1. The other 10 are the
    disabled month arrows, which are exempt.
  - The detail is in line 4 of `docs/qa/go-no-go-1.0.1.md`.
- **Issues:** no audit failure, so none was filed from the audit. The tours found #101 (S3, accessibility), with 3
  reproductions.

**4.1:** in `docs/qa/go-no-go-1.0.0.md`:
- line 1a: 3 errors, #74;
- line 1b: datasource 100/100, designsystem 37/37, app 1/1.

On 1.0.1, line 1b gave datasource 104/104, designsystem 37/37 and app 1/1.

**4.2, `qa/split_apks.sh` on the upload-signed `Migrely-1.0.1-6.aab`:**
- **ru-26** (emulator-5582, 00:03–00:15): ru-RU was read back as the system language; smoke 15 of 15. At the end the
  system language read back as en-US.
- **he-36, uz-36 and en-36** (emulator-5580, 01:06–01:45): smoke 14 of 14, then 3 of 3 with the 20 s timeout. The D117
  orders hold:
  - he: day first, the week starts on Sunday;
  - uz: month first, Monday;
  - en: month first, Sunday.
  The app language was set and then cleared.
- **`pm path`:** 4 APKs for each profile: base, ABI, density and language. He's split is `base-iw.apk`.
- **The serial guard:** it is an allowlist of emulator-5580 and emulator-5582, checked before any adb call.
  `QA_SERIAL=emulator-0000` printed "Refusing to run on emulator-0000" and exited 2. emulator-5570 takes the same branch.
  It was not run on 5570 itself, which is the dev session's.

**7.2:** lines 1–10 of `docs/qa/go-no-go-1.0.1.md` all pass. 2a and 10 used the upload-signed bundle, and 6a upgraded
from build 4. The record has "What these checks do not prove", and QA's verdict on lines 1–10 is "Go, with these risks
open".
- **The monkey:** `qa/monkey.sh` now turns Do Not Disturb off through `cmd notification set_dnd off`. A plain
  `settings put` was written back. Its second run left `zen_mode` at 0.

**2.3 and 2.4 on develop** (the QA branch with develop merged up to #100, 396e602d; its debug APK; emulator-5580,
2026-10-28, host load average 9–13):
- **2.3:** `python3 -I qa/seed_backup.py --self-test` recounts `report-heavy-old-notes` on 5 dates, with 4 spec values
  each. `report_heavy` passed 3 times:
  - once in 1.0.1's line 5;
  - twice on develop (06:01–06:08 and 06:08–06:16).
  Each time: "the latest 382 of 900 migraines", "Notes from the latest 382 migraines." and no HEAVY- note.
- **2.4:** `premium_no_products` passed in the same 2 runs. Every point of design D5 held:
  - no paywall after onboarding, nor at the 5th migraine 8 days on;
  - with products, the patterns paywall shows in that same state;
  - Doctor report shows the region line, with no buy button and no price;
  - Premium bought before is still "Premium · monthly" 20 minutes on.
  On 1.0.1 it fails, as expected: D122 is on develop only.

**6.2:**
- **Coverage:** `qa/e2e_coverage.py --below 50 --register docs/qa/test-strategy.md` exits 0. Develop brought the total
  to 520 scenarios, so the register now says 267 of 520 (51%). Its attack-logging, attack-details, past-attacks and
  data-backup rows follow.
- **Resources:** `qa/resource_names.py` passed with 706 names.
- **Specs:** `openspec validate --all --strict` gave 28 of 28.
- **Smoke on develop:** 14 of 14, then 3 of 3 (06:16–06:27).
- **The new flows and scenarios:** each passed as its task says. See 2.1–2.4, 3.1–3.3 and 7.2 above.

**6.4:** after merging develop, `./gradlew assembleDebug assembleRelease test ktlintCheck detekt lint --no-daemon`
exited 0 (05:58–06:01). Lint has been in the gate since D123.

**7.4:**
- **The verdict:** "Go, with these risks open", on lines 1–10. It was sent to the dev session at 05:55 and recorded
  under Decision in `docs/qa/go-no-go-1.0.1.md`.
- **The timing:** the hotfix had been finished before it, on the user's decision of 2026-10-27 at 20:30. That
  decision is recorded there too.
- **The diff:** `git diff ed695424 v1.0.1` is empty. The two trees are the same, 2e546e95.

**6.3:** the review (`superpowers:code-reviewer`) covered `git diff 152ad9ed HEAD`, read-only. It confirmed:
- the `${DAY}` cause, read from Maestro 2.11's `Env` regex;
- days 15–21, which are in row 3 or 4 for any first weekday;
- the new scrolls, which do nothing when the element is already visible, for all callers.

Its findings, and what was done:
1. **Line 1a read only the app's lint report;** each module writes its own. `docs/QA.md` now reads every module's report.
   1.0.1's "56 warnings" was already all of them: the app's 47 and 9 from 5 UI modules.
2. **`cmd notification set_dnd` exists only from API 30.** A plain put of `airplane_mode_on` also leaves the radios off.
   - `qa/monkey.sh` uses the API 30 commands where they exist.
   - It reads both values again 1 s later and warns if they reverted.
   - `QA_BASELINE` now has `zen_mode=0` and `airplane_mode_on=0`, and `qa/reset_device.sh` turns both off.
   - **Re-run:** reset on emulator-5580, then the monkey on develop (06:27): 0 crashes, 0 ANRs. Do Not Disturb went
     back to 0 with no warning, and airplane mode stayed disabled.
3. **`.maestro/README.md` still said a flow times the 5 s window.** It now says no flow does: the length was measured by
   hand, `message_duration` times a message, and on Android 8.0 `undo-window-passes` can still fail under load.
4. **The landscape swipe at 65% had about 9 dp to the dial.** It runs at 66%, the midpoint measured on
   Migrely_QA_API_36 (dial 1536 px, dialog 1620 px). **Re-run:** `insights` passed twice in all 3 configurations on
   develop (06:30–06:50).
5. **The subscription row counted 20/21 before 2.4 had run.** Answered: 2.4 has now passed twice on develop, so the row
   holds.
6. **A tag with a script turns into an ab_CD form that Custom Locale rejects.** `qa_set_system_locale` now refuses
   anything but language-COUNTRY: `uz-Latn-UZ` gets "is not a language-COUNTRY tag" and exit 1, while ru-RU, en-US,
   he-IL and uz-UZ pass.

