# Tasks

## 1. Requirements and baseline

- [x] 1.1 BRD §13 row 11b and §17 D201, D202. Verify: `grep -n "| 11b |\|| D201 \|| D202 " docs/BRD.md`
- [x] 1.2 `QA_APK` in `qa/maestro.sh` and `qa/device_scenarios.sh`; the release branch's debug APK built in
  `../migraine-app-qa-rc` (design D1). Verify: `bash -n`; the APK installs
- [x] 1.3 Baseline on `release/1.0.0`: full regression and all device scenarios. Adapt the flows to the self-showing
  paywalls (design D2), and triage every other failure. Verify: stale checks fixed, defects filed. The baseline moved
  with the RCs (cedc4c4 → 195fff2 → 0914bcb) and became the 1.0.0 Go/No-Go (`docs/qa/go-no-go-1.0.0.md`)

## 2. Tools

- [x] 2.1 `qa_store` in `qa/device_scenarios.sh`, `subflows/buy.yaml` and `subflows/close-onboarding-paywall.yaml`
  (design D3). Verify: a purchase completes, and `reset` empties the store
- [x] 2.2 `qa/seed_backup.py` with the named seeds of design D4, and `qa/seed_expect.py`. Verify: its output passes
  `backup_files.py json`, its self-test recounts every seed, and an imported seed shows the expected texts
- [x] 2.3 `qa/report_pdf.py` (design D5). Verify: it reads a real report, and fails on a missing text and a wrong day

## 3. Subscription (SUB-F*, EC-BIL-*)

- [x] 3.1 The paywall: From the doctor report, Insights' and Settings' headlines, the products with the yearly plan
  first, the button names, the terms, Restore and Close. Verify: passes 3 times
- [x] 3.2 Prices shown (de-DE) and No trial left. Verify: passes twice
- [x] 3.3 A purchase, Pending (completed and declined from adb), and a cancelled sheet. Verify: passes twice
- [x] 3.4 The entitlement: Offline (2 and 4 days), Refunded, New phone, and Reinstall or backup on another account.
  Verify: passes twice
- [x] 3.5 Lifetime over a subscription. Verify: passes twice
- [x] 3.6 No billing, and the debug half of no billing service. Verify: passes twice
- [x] 3.7 When the paywall shows itself: Finishing onboarding, The 5th migraine, Patterns not ready yet, Within 7 days,
  and never in attack mode. Verify: passes twice
- [x] 3.8 Trial ending reminder, and Premium in Settings (A subscriber). Verify: passes twice

## 4. Doctor report (REP-F*, EC-REP-*)

- [x] 4.1 Opening the report, An empty month, and Free user. Verify: passes 3 times
- [x] 4.2 Three months, A month only partly in the period, Notes left out, and Logged in Tokyo, checked in the PDF.
  Verify: passes twice
- [x] 4.3 Twelve heavy months, and Closed while making it. Verify: passes twice

## 5. Insights (INS-F*, EC-INS-*)

- [x] 5.1 Opening Insights, Three migraines so far, and Free user. Verify: passes 3 times
- [x] 5.2 Stress, Mondays, Tracking off, Ibuprofen, and A screen reader. Verify: passes twice

## 6. Documentation and verification

- [x] 6.1 `docs/QA.md` (D201, D202, `QA_APK`, the seeds and the PDF check) and the README of `.maestro/`. Verify:
  `grep -n "D201\|D202\|QA_APK" docs/QA.md`
- [x] 6.2 Coverage, `qa/resource_names.py`, `openspec validate --strict`, smoke, and every new flow and scenario as its
  task says. Verify: the verification record below
- [x] 6.3 Review (`superpowers:code-reviewer`), every finding fixed or answered. Verify: re-run of what each fix touched

## Scenarios without a check, or partly checked (D61)

After the merge of develop (d04a412), the specs hold the release fixes D110, D117, D119, D120, #66 and #67. Their new
scenarios in this change's capabilities are listed here too.

| Scenario | Reason | Follow-up |
|---|---|---|
| subscription › Payment problems › Grace period | Google Play's own in-app messages, which the simulated store does not show | A failed renewal with a license tester on the user's phone (Go/No-Go "Still open") |
| subscription › When the paywall shows itself › Google Play offers no products | The simulated store has no "no products" state. The R8 APK, which finds no products, was checked by hand: no paywall after onboarding in 3 runs, and a tap on Doctor report says Premium isn't available (Go/No-Go, D110). "At most once an hour" is not checked | Ask the dev session for a `no-products` command in the simulated store, then a scenario |
| subscription › Billing unavailable › No Google Play billing service on the phone | Partly: `premium_store_states` checks the line and that no paywall shows itself. "At once" is not timed, and the release half is the Go/No-Go's D110 check | — |
| subscription › Changing plans › Monthly to yearly | Partly: the simulated store's switch and Settings' "Premium · yearly" (`paywall-and-purchase.yaml`). Google Play accepting it was checked on the user's phone (Go/No-Go, real Play 9). The charge on the renewal date is Play's | — |
| doctor-report › The summary page › Three months | Partly: each migraines row is matched by its start. Its other columns are not asserted | Add the duration and pain columns to `qa/seed_expect.py` |
| doctor-report › Large periods › No note among the migraines shown | Needs a heavy seed whose newest migraines have no note. The dev's unit tests pin it (PR #69) | A `report-heavy-old-notes` seed in the next QA change |
| app-settings › Privacy and about › The full privacy policy, No browser | Checked by hand on 195fff2 and 0914bcb (D119: the VIEW intent opens Chrome). "No browser" needs the browser turned off | A scenario in the next QA change: the intent read from `dumpsys activity`, then Chrome disabled and enabled again |
| day-markers › Mark a chosen day › Period tracking off, Removing a mark with tracking off; Period today from home › Period tracking off, Removing today's mark with tracking off | New with #66 (PR #68). Only "Turning tracking off from home" has a flow (`track-periods-off-live.yaml`) | Flows in the next QA change |

## Verification record (2026-10-26, emulator-5580 `Migrely_QA_API_36` and emulator-5582 `Migrely_QA_API_26`)

- **Base:** develop d04a412 merged into the branch (fa5bdf0).
- **Build under test:** `release/1.0.0` @ 29aadb3 (versionCode 4), built in `../migraine-app-qa-rc` (design D1). Its
  product code is the same as develop's.
- **Earlier builds:** the flows and scenarios were first run on cedc4c4, 195fff2 and 0914bcb. Those runs found #66 and
  #67 and the stale checks of task 1.3.

| Check | Result |
|---|---|
| Gate on the merge, `./gradlew assembleDebug assembleRelease test ktlintCheck detekt --no-daemon` | ✅ exit 0 |
| Full regression on API 36, debug | ✅ 43/43 + 9/9; `track-periods-off-live` (#66) now passes, and loses `known-bug` |
| Full regression on API 36, R8 release APK | ✅ 41/41 + 9/9; the 2 `simulated-store` flows are skipped, since the build is not debuggable |
| Smoke on API 26, debug and R8 | ✅ 12/12 + 3/3 each |
| New flows (`subscription/paywall-and-purchase`, `onboarding/paywall-after-onboarding`) | ✅ 4 of 4 each: the regression, then 3 in a row |
| The 13 new device scenarios | ✅ 2 of 2 full runs (13:11 and 15:24). `insights`, `report_three_months` and `report_heavy` passed a third run (16:35), after the notes checks were added |
| All 53 device scenarios | ✅ See the Go/No-Go, line 5 |
| `qa/release_apk.py` on the R8 APK | ✅ 0 hits in code, resources and manifest; the debug control has 57, 2 and 4 |
| `qa/backup_rules.py` on the R8 APK | ✅ `migrely_entitlement.xml` excluded in full-backup-content, cloud-backup and device-transfer |
| `qa/permission_audit.sh`, debug and R8 | ✅ passes |
| `qa/message_timing.py` (#56) | ✅ 8.0 s at the default, 20.0 s with both timeouts at 20 s (±0.2 s) |
| `python3 -I qa/e2e_coverage.py` | ✅ exit 0 |
| `python3 -I qa/resource_names.py` | ✅ 704 names, passed |
| `openspec validate add-premium-e2e-coverage --strict` | ✅ valid |
| Review (`superpowers:code-reviewer`) | ✅ C1, I1–I10 and M1–M10 fixed in faebc9e and after |

**Coverage, with the develop specs:**
- subscription 18/20;
- doctor-report 11/12;
- trigger-insights 8/8;
- onboarding 9/9;
- app-settings 8/10.

The gaps are in the D61 table above.

**Issues:**
- **Filed and closed:**
  - #66 (S3): Track periods off kept home's Period toggle. Fixed in PR #68;
  - #67 (S3): a large report's notes were not capped. Fixed in PR #69.
- **Retested and closed:** #45, #56 and #58.
