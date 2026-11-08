# Release Go/No-Go: Migrely 1.1.0 (versionCode 8)

- **Candidate:** `release/1.1.0` @ 8f2750348811eecddcdea47052e261ba4d3d317d, versionName 1.1.0, versionCode 8.
  - It was cut from develop 5ca5a614 and adds the version, `docs/release/whats-new.md` and BRD D162 (94730827).
  - Then PR #165, the #155 fix (D163): `git diff 94730827 8f275034` is 11 files, the BRD, 5 openspec files and 5 attack
    files (`AddIntensityPointUseCase`, its result model, `AttackDetailsViewModel` and their two tests). No shared UI,
    no message host, no dialog and no design-system file.
- **Why 1.1.0:** Google rejected 1.0.1 ("Unresponsive UI", the paywall's purchase buttons with Play's own "No internet
  connection" sheet). 1.0.2 was tagged and never shipped, so this is 1.1.0 (D162). The fix for the rejection is #154
  (#146, #152, D153, D158).
- **Builds:** QA's builds in `../migraine-app-qa-rel2`, detached at 8f275034, clean before and after:
  - `debug-8f27.apk` (sha256 637d1caa00…);
  - `r8-8f27.apk` (c5d4c9161f…), minified, **signed with the debug key** (QA has no upload key, D102);
  - `r8-8f27.aab` (fe95c2a7c0…), from `bundleRelease` without the upload key; line 2a signs its splits with the debug
    key. **The bundle that goes to Play is built by the dev session after this verdict;** its tree must equal the tag.
- **Emulators:** API 36 (`Migrely_QA_API_36`, emulator-5580) and API 26 (`Migrely_QA_API_26`, emulator-5582).
- **Rules:** `docs/QA.md` §5, BRD D44, D125, D204.
- **Evidence root:** the QA work folder `migrely-work/qa-110/` beside the repo (not in the repo). Step logs are
  `logs/step-<tag>.out`; segment logs `logs/sN.out`.
- **Which tree each QA script came from:** `qa/` and `.maestro/` do not ship, and this record and QA's flow fixes are
  not in the release branch, so the tag tree stays 8f275034.
  - Lines 1–8 and 10 ran the release tree's own `qa/` and `.maestro/` (8f275034).
  - Lines 3 (one flow), 5 (`message_tap_guard`) and 9 used PR #164's QA files (branch `feature/qa-paywall-line`,
    head cfd1d3a6), as each row says. #164 is not in the release branch; the lead merges it into develop.

## Decision

**QA's verdict, 2026-11-09 at 05:00: GO for `release/1.1.0` @ 8f275034, with the risks below open.**
- **Lines 1–10 all pass on 8f275034** (versionCode 8), and no S1 or S2 is open. The two S2s that gated it, #146 and
  #152 (the fix for Google's rejection), were retested on this SHA's own build and closed.
- **Lines 3, 5, 9 and 10 pass with PR #164's QA files** (head cfd1d3a6): two flows (`unique-names`, the paywall) and
  `message_tap_guard` were made stale by D158, and the register by #154–#159. Each old version failed and each new one
  passed 2/2 where the rows say so. These files do not ship, so the release tree stays 8f275034.
- **No app defect was found on the candidate.** Every failure on the way came from QA's own drivers or flows, each
  disproved by a rerun:
  - `message_tap_guard` tapped a point D158 moved (old point failed 2/2, new passed 2/2, a guard-0 build failed 2/2);
  - the #152 945x1680 rows first ran without `input tap` under the size override (void, rerun 12/12);
  - #119's first "Home left within 2 s" row tapped about 3 s late (timed rerun: no request 2/2, control 2/2).
- **Carried over from 94730827:** line 4 only, for the reason stated under the table.
- **Lines 11 and 12** are not run, under the owner's standing order: no Play Console uploads while the app is in review.
- **Open risks:** the residual risks and the "not proved" list below. In particular, on 8f275034:
  - TalkBack's speech of the new purchase line;
  - the tap guards on Android 8 (7.16);
  - real Google Play.
- **Release steps (D125):**
  - team-lead merges #164 into develop;
  - the dev session builds the upload-signed bundle from 8f275034 and tags `v1.1.0` on main;
  - QA then checks that `git diff 8f275034 v1.1.0` is empty;
  - no Play upload while the app is in review (the owner's order).

## The lines (on 8f275034)

Paths are under `migrely-work/qa-110/`. "From #164" means PR #164's QA files (head cfd1d3a6) against the 8f275034 APK.

| # | Check | Result | Evidence |
|---|---|---|---|
| 1 | Build gate (`assembleDebug test ktlintCheck detekt lint`) | ✅ exit 0, 17:31–17:35, 1992 tasks, tree clean before and after; `assembleRelease` and `bundleRelease` exit 0 | `logs/line1-8f27.out`, `logs/line1-gate-8f27.log` |
| 1a | Lint's warnings (D204) | ✅ 0 errors, 57 warnings, the same set as 94730827. Against v1.0.2 (57): 1 new, `ReturnFromAwaitPointerEventScope` in the test file `DoubleTapRestorationTest.kt` (`src/testDebug`, does not ship); 1 gone, the plural warning in the medication strings | `logs/lint8f27.txt`, `lint102.txt` |
| 1b | Instrumented tests | ✅ 200/200, 0 failed, errors or skipped (recounted from the 6 result XMLs, 18:29–18:31): app 3, architecture/ui 13, datasource 112, designsystem 53, attack/ui 5, medication/ui 14. 1.0.2 had 161 | `logs/step-F-L1b.out`; XML in `migraine-app-qa-rel2/**/androidTest-results/connected/debug/` |
| 2 | Smoke on API 26, debug and R8 | ✅ emulator-5582, 2026-11-09 02:36–02:55. Debug APK: 12/12, then 3/3. R8 APK (not debuggable): 12/12, then 3/3 | `logs/s8.out`, `step-L2-debug.out`, `step-L2-r8.out` |
| 2a | Split APKs ru-26, he-36, uz-36, en-36 | ✅ "Split APKs passed" for each profile (ru-26 on emulator-5582; he, uz and en on 5580), from `r8-8f27.aab`, splits signed with the debug key | `logs/s8.out`, `logs/s9.out`, `step-L2a-*.out` |
| 3 | Full regression, debug | ✅ with one flow from #164. Pass 1: 44/44. Pass 2: 9/10: `medication-list/unique-names.yaml` failed at "Ibuprofen" because the flow was out of date (since #154 the Undo message lies over row 1 of My medications, which D158 allows). #164's flow passes on 8f275034 (1/1) and on 94730827 (2/2); the old flow failed 2/2 on 94730827. #164's paywall flow (the purchase line, Restore's place) passes on 8f275034 | `logs/step-F-L3.out`, `step-F-L3-164-un.out`, `step-F-L3-164-paywall.out` |
| 4 | Accessibility audit (22 states × 3) | ✅ carried over from 94730827 (see below), plus `report-period` rerun on 8f275034: 3/3 `[ok]`. 0 failures; 65 captures `[ok]` on 94730827, `report-period.font100` lost to a brief adb drop and captured on the rerun | `logs/step-L4.out`, `logs/step-L4-report-period.out`, `l4/` |
| 5 | Device scenarios | ✅ 55 of 55, with `message_tap_guard` from #164; plus `upgrade` ×2 (line 6a) and `release_apk` (no simulated store, not debuggable).<ul><li>First run (release-tree `qa/`): 45 passed, `message_tap_guard` failed, and the chain stopped for the handover inside `premium_plans`.</li><li>Resume: the 9 not finished, 9/9 (`premium_plans` run again in full).</li><li>`message_tap_guard` failed on an expectation D158 made stale. It tapped the centre of Doctor report, which Undo covered until #154; since D158 the message sits below the bar and its guard covers only its own place, so Doctor report opening at 5.2 s is correct. #164 now deletes last month's migraine with an older one kept, so History's Next month answers under Undo, measures Undo's place from the screen, and times taps at 5.2, 5.5, 5.8 and 7.5 s. Old point: failed 2/2. New: passed 2/2 (guarded at 5.5 and 5.8 s, Next month at 7.5 s). A build of 8f275034 with the guard set to 0 ms failed 2/2 (Next month at 5.8 s), so the check can fail. Three earlier runs of the new scenario failed in its measuring step because `uiautomator dump` returns only at about +5.1 s while the message's time-left line moves; a tap at a fixed 2.0 s undoes 2/2, so this was the test's timing, not the app</li></ul> | `logs/step-L5.out`, `logs/step-L5-rest.out`, `logs/step-L5-release-apk.out`, `mtg/` |
| 6 | Random input (`qa/monkey.sh 20261024 5000`) | ✅ 5,000 events, 0 crashes, 0 ANRs. Monkey turned airplane mode and Do Not Disturb on; both restored | `logs/step-F-L6-monkey.out` |
| 6a | Upgrade from each shipped version, data kept | ✅ from 1.0.1 (vc6, tag v1.0.1) and from 1.0.2 (vc7, tag v1.0.2, never shipped): "All scenarios passed", ends on vc8 1.1.0 | `logs/step-F-L6a-from-101.out`, `-102.out` |
| 7 | Permissions (debug, R8, no simulated store in R8) | ✅ the 5 permissions of 1.0.2, no INTERNET. R8: simulated-store code 0, resources 0, manifest 0, not debuggable; the debug control finds 57, 2 and 4, so the check can fail | `logs/step-F-L7-debug.out`, `-r8.out`, `-r8-nostore.out` |
| 8 | Open defects | ✅ no S1 or S2 open (checked 2026-11-09 after closing #146 and #152). Open, accepted for 1.1.0: #161, #166, #167, #148, #145, #141 (S3); #153, #150, #149, #144, #118, #109, #88, #83, #82 (S4) | `gh issue list --state open`; #146 and #152 closing comments |
| 9 | Coverage report | ✅ from #164: exit 0, 278 of 855 scenarios (33%). On the release tree's own register it exits 1: the register went stale with #154, #156, #157 and #159 (app-shell 16→143 scenarios, subscription 24→55), and #164 brings it up to date | `logs/step-L9-164-new.out` (cfd1d3a6's files), `logs/step-L9-on-release.out` |
| 10 | R8: lines 3 and 7 on the minified APK | ✅ with one flow from #164. Pass 1: 42/42 (the 2 simulated-store flows need a debug build). Pass 2: 9/10, the same stale `unique-names` flow as line 3; #164's flow passes on the R8 APK 2/2. Line 7 on this APK ✅ above | `logs/step-L10.out`, `step-L10-164-un-1.out`, `-2.out` |
| 11 | Play pre-launch report | Not run: the owner's standing order, no Play Console uploads while the app is in review | |
| 12 | Internal testing with people, a week | Not run: the owner's decision, for the same reason | |

**Other rows of this Go/No-Go (all on 8f275034):**

| Row | Result | Evidence |
|---|---|---|
| #146 (S2) retest | ✅ closed. Try again 2/2 (1 message per tap; a double tap 114–115 ms apart gives 1); failed purchase: message plus the connection notice 2/2; Play unavailable and no products: the region line, no message, 2/2 each; the purchase line before any tap 6/6 | `qa-154/logs/t154.jsonl` (build `debug-8f27`), `qa-110/recount_152_8f27.py`, #146's closing comment |
| #152 (S2) retest | ✅ closed. Doctor report from History, My medications' Back, and Back on details, Your data and History answer a first tap with a message showing, 2/2 each. The paywall's Close, Try again and Restore are not under its message and answer a first tap: 1080x2400 at 100% and 200% portrait and 200% landscape, 6/6 each; 945x1680 through Settings and through History, 12/12. A first 945x1680 run (6 rows) is void: QA ran it without `input tap` under the size override | the same; #152's closing comment |
| 7.14: #138, #139, #140, #142 tables, debug and R8 | ✅ the same pattern as the fixed reference 2f800c71 on both builds. #138 15/15 right row (R8: 15/15 saved); #139 13/13, nothing stacked; #142: Remove, Delete with Back, Delete with Add and Delete all ran nothing 2/2 each, the controls ran; #140: an edit or add within 700 ms kept 5/5. One R8 row differs: a tap on the name field 49 ms after Add focused it (the R8 editor opened that fast). Nothing was saved, and from 100 ms every build focuses it | `qa-110/logs/s5.out`, `qa-t43/recount_8f27.py` over `qa-t43/logs/` |
| 7.15: #148 after Back | ✅ taps from 280 ms after Back landed 36/36, none on a wrong row, debug and R8 | the same |
| #142 Delete-all control | ✅ a late tap wipes (onboarding again, a new process), so the Delete-all row of 7.14 can fail | `qa-110/logs/s7.out` |
| #119 rating request (D129–D132) | ✅ debug, through `review_request_count` (D131), 2/2 each: no request with the paywall shown 1 h ago; a request when it was 4 days ago; none at a second launch (120-day gap); none with a migraine in progress; none when Home is left 0.8 s after it opens, and one when it stays (exact timing, `t119b.py`). The first driver's "left within 2 s" row failed 2/2 because its tap came about 3 s after Home opened (uiautomator dumps); the timed rerun is the result. R8: Google's review service cannot bind (no Play Store), 0 crashes, Home stays, 2/2 | `qa-110/logs/s7.out`, `step-t119.out`, `step-t119b.out`, `qa-154/logs/t119.jsonl` |
| 7.16: the #156 and #159 tap guards on API 26 | **Not proved.** The exact-touch drivers (`t5_write.py`, `t10_under.py`) measured nothing on the API 26 image (no gap in any row), and `two_touch` could not parse API 26's getevent and stopped before `double_tap` (run once by team-lead's decision, with a copy of `qa/device_scenarios.sh` that differs only in its serial line). On Android 8 the guards rest on the shared code path and the API 36 measurements (7.14, 7.15, 6.6) | `qa-110/logs/s8.out`, `s716.out`, `step-716-26-scen.out` |
| 7.4 dialogs at 200% and landscape (`date_picker`, `reminder_time`) | ✅ for stray writes only: 33 rows, 0 errors; a second tap 100–500 ms later wrote nothing in any row. At these layouts the dialog's button does not lie over the control, so the dialog guard itself is not exercised here | `qa-110/logs/s9.out`, `qa-s7/logs/t9_dialog.jsonl` |
| 6.9 bursts (10 taps, 100 and 250 ms apart) | ✅ portrait (Medication taken): no intake in any burst, and one after a pause, 12/12. The landscape row (attack mode) proves nothing: its tap after the pause wrote nothing either, so the point is not on a write control | `qa-110/logs/s9.out`, `qa-t49/logs/t69_burst.jsonl` |
| 6.6 / 7.12 animator scale 0, debug and R8 | ✅ with animations off, a tap after Back lands from 100 ms and not at 0–30 ms, 3/3 each, the same as the fixed reference; the scale is restored | `qa-110/logs/s9.out`, `qa-t43/logs/t7_anim0.jsonl` |
| #154: the purchase line as its own stop | Structure ✅: the line is one text node of its own, not inside the plan's terms, for Monthly, Yearly and Lifetime. **Speech not proved:** the TalkBack log captured no speech on 8f275034, not even for the terms node used as the control | `qa-110/logs/s9.out`, `qa-154/logs/t154.jsonl` |

**Line 4 carried over from 94730827, and why the #155 diff cannot reach it.** The diff 94730827..8f275034 changes only
`AddIntensityPointUseCase`, its result model and `AttackDetailsViewModel` (adding a pain entry, and the safety card on a
first 10), plus docs. `AddIntensityPointUseCase` is wired only into `AttackDetailsViewModel` (`AttackModule`), and no
tour state adds a pain entry on a migraine's details. The one tour state that sets a pain level, `home-with-attacks`,
does it in attack mode (Mild, Severe), a different path. No layout, string, theme or design-system file changed.

**Line 4's warnings and "not judged" lines, looked at one by one** (`l4/clipped_cross.py`, `l4/merged_screen.py`,
`l4/visible_contrast.py`, outputs beside them):
- 44 nodes cut off at a scroll edge (20 lines). 32 are judged whole in another state of the same variant, and 7 at
  another variant of the same state (100% or landscape; same colours).
- **Never whole in any capture:**
  - the paywall's "Logging, history, reminders, export and every safety warning stay free." Its visible glyphs measure
    7.18:1 (#9AA0A9 on #0F1115), so it passes AA;
  - the first row of trigger chips on a migraine's details, cut under the top bar in `details-bottom`. It is the same
    chip component as the rows judged in full there. Not measured itself.
- 449 merged nodes (spoken label only) screened: the highest-contrast pixel of each against its most common colour.
  - Only 10 fall under 4.5:1, all History's month arrows at 2.65:1, and in every one of those captures the arrows are
    disabled (exempt, WCAG 1.4.3 and 1.4.11).
  - The enabled arrows, which the tour never captures, measure 11.72:1 on 8f275034 (`mtg/p-after.png`).
  - A high ratio for the other 439 proves nothing about their text: an icon or a border may be the brightest pixel.

## What these checks do not prove

| Line | Passing it does not show |
|---|---|
| 1 Build gate | that the logic is tested: code coverage is not measured |
| 1b Instrumented | the screens: DAO SQL, migrations, design-system components and a few screen tests only (200 tests) |
| 2, 3, 10 Smoke and regression | the scenarios without a flow: 577 of 855 (67%, line 9) |
| 4 Accessibility audit | what a screen reader says (TalkBack is not driven by the tour); open dialogs; text contrast inside merged nodes (cards, calendar days, rows), only screened; the first row of trigger chips on a migraine's details, never whole in a capture (same chip component as the rows judged in full); text at 200% for 10 nodes judged only at 100% or landscape (same colours) |
| 5 Device scenarios | anything beyond its named scenarios; `message_tap_guard` checks History only |
| 6 Random input | anything beyond 5,000 events from one seed |
| 6a Upgrade | an upgrade through Android's backup on a new phone; an upgrade from 1.0.0 |
| 7 Permissions | what the Play app does with billing data, which Google Play's own terms govern |
| R8 APK | the upload signature: QA's R8 APK and bundle are signed with the debug key. The bundle for Play is built after this verdict |
| #146 / #152 retests | real Google Play's own error sheet, USER_CANCELED after it (unit tests only), "Getting prices" with a slow real Play (the simulator answers in the same frame), TalkBack on the new notice, a real phone and network |

**Capabilities under 50% (line 9, `qa/e2e_coverage.py --below 50` on PR #164's register), with the register's
follow-up:**

| Capability | Covered | Follow-up (`docs/qa/test-strategy.md`) |
|---|---|---|
| app-identity | 0 of 3 | a release-checklist line in QA 13d |
| app-shell | 6 of 143 | release_apk and the permission audit hold the network promise; split APKs hold the language rules; QA 13d |
| attack-details | 6 of 46 | build 4 sweep; QA 13d |
| attack-mode | 5 of 14 | build 4 sweep; QA 13d |
| brand-typography | 0 of 5 | audit C4 (visual) |
| custom-tags | 2 of 11 | sweep 2; QA 13d |
| in-app-messages | 7 of 45 | sweep 2; QA 13d |
| in-app-review | 0 of 18 | device rows (#119, this record); the real card only on an internal-testing install |
| medication-intakes | 4 of 14 | build 4 sweep; QA 13d |
| medication-list | 9 of 38 | QA 13d |
| month-sky | 12 of 36 | unit tests hold the rules; sweep 2 samples them; screenshots in C4 |
| past-attacks | 7 of 30 | build 4 sweep; QA 13d |
| quality-gates | 13 of 52 | none named |
| release-build | 0 of 3 | Covers lines for those checks in QA 13d |
| subscription | 22 of 55 | under the 80% target: sheet codes and the grace period need a license tester on a real phone |
| website | 0 of 84 | the website's own pipeline; not an Android flow |

**Not run, and why:**
- **Line 11, Play pre-launch report:** the owner's standing order, no Play Console uploads while the app is in review.
- **Line 12, internal testing with people for a week:** the owner's decision; nothing can be uploaded now.
- **6.4, double taps on Done (Log medication) and Save (the medication editor):** no driver exists. Back is covered by
  7.15.
- **6.5, 7.3, 7.8: switch access, a hardware keyboard on the new screens, rotation and process death with a dialog
  open:** not run on 8f275034. 6.5's recreation was measured on #156's head only.
- **7.2, the hand check on the owner's phone:** pending on the owner (team-lead's internal-track step).
- **Landscape for #166; D163's other failure kinds** (another error, a rejected time, a deleted migraine): unit tests
  only.
- **The clinical view of the safety card's timing:** medical-reviewer is not a clinician (T31).
- **On 8f275034, measured by no driver:**
  - TalkBack's speech of the purchase line: the speech log stayed empty;
  - the tap guards on Android 8 (7.16): no exact-touch measurement works on the API 26 image;
  - the dialog guard where a dialog's button lies over the control (7.4: no such overlap at the layouts tried);
  - a burst in landscape attack mode (6.9's landscape row has no working control).

## Residual risks

- **Double taps (D159, D160, D161):** a second tap from about 300 ms on a new screen, or about 500 ms around a dialog,
  still writes: a false intake (with Undo), a chip, the Track periods switch, or Restore in landscape (it makes a
  backup first). A deliberate tap under those limits and within 100 dp is lost once with no feedback. The cost of lost
  taps is not measured with real hands.
- **Known open issues, accepted for 1.1.0:** #161, #166, #167, #148 (S3); #88 (S4).
- **Restore on the paywall is disabled when Play billing is missing,** and Settings then says "Premium isn't available
  in your region yet.", which names the wrong cause. Already on develop (`PaywallScreen.kt`), not new.
- **The paywall message covers the paywall's heading and first line for its duration** (no control is covered). A
  layout choice for ux-designer; not filed.
- **Real Google Play:** the review device, its network and Play's own sheets. Review may still fail on what only
  Google sees. The real In-App Review card (#119) cannot show on emulators with no Play Store.
- **Coverage:** subscription 22 of 55 scenarios (40%) end to end, under the 80% high-risk target. The sheet's codes
  need a real Play license tester.
- **QA's own spec finding (not a defect):** `in-app-messages`, "Undo just as the message goes", still says neither
  Doctor report nor Insights opens. Since D158 Undo lies over History's Next month, so that sentence can no longer
  fail. Goes to product-manager after the release (team-lead).
