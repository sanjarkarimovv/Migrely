# Tasks

## 1. Requirements

- [x] 1.1 BRD §13 row 3b and §17 D60, D61. Verify: `grep -n "| 3b |\|| D60 \|| D61 " docs/BRD.md`

## 2. Retest of PR #24 and zone scenarios

- [x] 2.1 `landscape-message-clear` without `known-bug`, asserting "+ Medication taken"; `travel-after` with the new
  Covers lines and the History city check. Verify: both pass (done 2026-10-24 17:55, commented on PR #24)
- [x] 2.2 `travel_west`, `same_date`, `zone_attack` and `zone_history`. Verify: `qa/device_scenarios.sh travel_west
  same_date zone_attack zone_history` passes
- [x] 2.3 `start-attack` and `end-attack` wait on "Details of this migraine". Verify: the three landscape audit states
  are captured (`qa/a11y_audit.sh attack-mode-message details-bottom home-with-attacks`)

## 3. Medication list flows

- [ ] 3.1 Subflows `add-first-medication`, `add-medication`, `open-my-medications`, `log-from-home`; flows
  `medication-list/add-and-limits` and `unique-names`. Verify: `qa/maestro.sh .maestro/medication-list` passes 3 times

## 4. Medication device scenarios

- [ ] 4.1 `med_effect`, `med_effect_attack`, `med_effect_skip`, `med_effect_late`, `med_preventive`,
  `med_two_waiting`, `med_link_home`. Verify: each passes, run twice
- [ ] 4.2 `med_overuse`. Verify: passes once, which is about 9 min

## 5. Documents

- [ ] 5.1 `docs/QA.md`: the clock-moving approach (D60) and the coverage rule (D61). Verify:
  `grep -n "D60\|D61" docs/QA.md`

## 6. Verify

- [ ] 6.1 Full regression (`qa/maestro.sh`), all device scenarios, `python3 -I qa/e2e_coverage.py` exit 0. Record the
  results, the coverage and the remaining medication gaps with reasons in a "Verification record" below
- [ ] 6.2 Gate `./gradlew assembleDebug test ktlintCheck detekt` and `openspec validate add-medication-e2e-coverage
  --strict`
