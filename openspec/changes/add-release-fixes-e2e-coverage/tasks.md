# Tasks

## 1. Requirements and the Go/No-Go record

- [ ] 1.1 BRD §13 row 13c and §17 D203. Verify: `grep -n "| 13c |\|| D203 " docs/BRD.md`
- [ ] 1.2 `docs/qa/go-no-go-1.0.0.md`:
  - line 12 waived by the user on 2026-10-27;
  - line 11 not available before launch (no closed track), the user's decision of 2026-10-27;
  - the decision says production at 100%.
  Verify: `grep -n "waived\|not available before launch" docs/qa/go-no-go-1.0.0.md`

## 2. Release-fix scenarios

- [ ] 2.1 `qa/device_scenarios.sh privacy_link` (design D2): the link to Chrome, and "No browser" with Chrome disabled.
  `restore()` and `qa/reset_device.sh` enable Chrome again. Verify: passes twice; after a run stopped half-way
  (Ctrl-C while Chrome is disabled), `pm list packages -d` does not list Chrome
- [ ] 2.2 `.maestro/day-markers/track-periods-off-marks.yaml` (design D3). Verify: passes 3 times; it fails when step 3's
  check is pointed at a day that keeps its mark (mutation of the flow, reverted)
- [ ] 2.3 Seed `report-heavy-old-notes` in `qa/seed_backup.py`, with its self-test, and its check in `report_heavy`
  (design D4). Verify: the self-test recounts it; `report_heavy` passes twice
- [ ] 2.4 "Google Play offers no products" in `premium_store_states` once D122 is on develop (design D5). Otherwise it
  goes to the D61 table with its follow-up. Verify: passes twice, or the D61 row

## 3. Accessibility audit on every screen (D203)

- [ ] 3.1 Tour states `onboarding-first`, `settings-main`, `settings-warning-signs`, `settings-privacy`,
  `settings-theme`, `medications-list`, `reminders` and `your-data` (design D1). Verify: `qa/a11y_audit.sh <state>`
  captures each at 100%, 200% and landscape
- [ ] 3.2 Tour states `paywall`, `report-period` and `insights` through the simulated store. Verify: as 3.1
- [ ] 3.3 The full audit on build 4's debug APK. Every failure is filed as an `accessibility` issue with its capture,
  and every "not judged" line is looked at. Verify: the audit's summary in the verification record

## 4. Exploratory testing on build 4

- [ ] 4.1 Charters 1–4 of design D6, each with notes: what was tried, and what was found. Verify: the notes in the
  verification record; an issue for every defect

## 5. Documentation and verification

- [ ] 5.1 `docs/QA.md`: the audit's screen list (D203), `privacy_link`, and the new seed. Verify: `grep -n "D203\|privacy_link\|report-heavy-old-notes" docs/QA.md`
- [ ] 5.2 Coverage, `qa/resource_names.py`, `openspec validate --strict`, smoke, and every new flow and scenario as its
  task says. Verify: the verification record below
- [ ] 5.3 Review (`superpowers:code-reviewer`), with every finding fixed or answered. Verify: a re-run of what each fix
  touched
- [ ] 5.4 Gate `./gradlew assembleDebug assembleRelease test ktlintCheck detekt --no-daemon` after merging develop.
  Verify: exit 0
