# Tasks

## 1. Requirements and baseline

- [x] 1.1 BRD §13 row 11b and §17 D201, D202. Verify: `grep -n "| 11b |\|| D201 \|| D202 " docs/BRD.md`
- [ ] 1.2 `QA_APK` in `qa/maestro.sh` and `qa/device_scenarios.sh`; the release branch's debug APK built in
  `../migraine-app-qa-rc` (design D1). Verify: `bash -n`; the APK installs
- [ ] 1.3 Baseline on `release/1.0.0` cedc4c4: full regression and all device scenarios. Adapt the flows to the
  self-showing paywalls (design D2), and triage every other failure. Verify: stale checks fixed, defects filed

## 2. Tools

- [ ] 2.1 `qa_store` in `qa/device_scenarios.sh`, `subflows/buy.yaml` and `subflows/close-paywall.yaml` (design D3).
  Verify: a purchase completes, and `reset` empties the store
- [ ] 2.2 `qa/seed_backup.py` with the named seeds of design D4. Verify: its output passes `backup_files.py json`, and
  an imported seed shows the expected count in History
- [ ] 2.3 `qa/report_pdf.py` (design D5). Verify: it reads a real report, and fails on a missing text and on too many
  pages

## 3. Subscription (SUB-F*, EC-BIL-*)

- [ ] 3.1 The paywall: From the doctor report, Insights' and Settings' headlines, the products with the yearly plan
  first, the button names, the terms, Restore and Close. Verify: passes 3 times
- [ ] 3.2 Prices shown (de-DE) and No trial left. Verify: passes twice
- [ ] 3.3 A purchase, Pending (completed and declined from adb), and a cancelled sheet. Verify: passes twice
- [ ] 3.4 The entitlement: Offline (2 and 4 days), Refunded, New phone, and Reinstall or backup on another account.
  Verify: passes twice
- [ ] 3.5 Lifetime over a subscription. Verify: passes twice
- [ ] 3.6 No billing, and the debug half of no billing service. Verify: passes twice
- [ ] 3.7 When the paywall shows itself: Finishing onboarding, The 5th migraine, Patterns not ready yet, Within 7 days,
  and never in attack mode. Verify: passes twice
- [ ] 3.8 Trial ending reminder, and Premium in Settings (A subscriber). Verify: passes twice

## 4. Doctor report (REP-F*, EC-REP-*)

- [ ] 4.1 Opening the report, An empty month, and Free user. Verify: passes 3 times
- [ ] 4.2 Three months, A month only partly in the period, Notes left out, and Logged in Tokyo, checked in the PDF.
  Verify: passes twice
- [ ] 4.3 Twelve heavy months, and Closed while making it. Verify: passes twice

## 5. Insights (INS-F*, EC-INS-*)

- [ ] 5.1 Opening Insights, Three migraines so far, and Free user. Verify: passes 3 times
- [ ] 5.2 Stress, Mondays, Tracking off, Ibuprofen, and A screen reader. Verify: passes twice

## 6. Documentation and verification

- [ ] 6.1 `docs/QA.md` (D201, D202, `QA_APK`, the seeds and the PDF check) and the README of `.maestro/`. Verify:
  `grep -n "D201\|D202\|QA_APK" docs/QA.md`
- [ ] 6.2 Coverage, `qa/resource_names.py`, `openspec validate --strict`, smoke, and every new flow and scenario as its
  task says. Verify: the verification record below
- [ ] 6.3 Review (`superpowers:code-reviewer`), every finding fixed or answered. Verify: re-run of what each fix touched
