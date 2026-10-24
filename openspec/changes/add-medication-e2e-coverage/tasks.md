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

- [x] 3.1 Subflows `add-first-medication`, `add-medication`, `open-my-medications`, `log-from-home`; flows
  `medication-list/add-and-limits` and `unique-names`. Verify: `qa/maestro.sh .maestro/medication-list` passes 3 times
- [x] 3.2 Review fixes: `medication-list/*` and `medication-intakes/*` in `.maestro/config.yaml` (they never ran in the
  regression or smoke before); `add-and-limits` archives the logged Paracetamol and brings it back; `unique-names`
  claims only what it asserts; `medication-intakes/undo-logged`. Verify: both folders and `add-for-empty-day` pass 3
  times (done 2026-10-24 19:30)

## 4. Medication device scenarios

- [x] 4.1 `med_effect`, `med_effect_attack`, `med_effect_skip`, `med_effect_late`, `med_preventive`,
  `med_two_waiting`, `med_link_home`. Verify: each passes, run twice
- [x] 4.2 `med_overuse`. Verify: passes once, which is about 9 min
- [x] 4.3 Review fixes in `qa/device_scenarios.sh`: clock and zone moves are read back; the accessibility timeout is
  saved and restored; the base time is computed once; one log per flow run. `med_two_waiting` answers the first
  question and expects the second; `med_effect_late` checks 23 h before 25 h; `med_effect_attack` ends the attack and
  expects the question; the soft card stays hidden after a relaunch; `travel_west` checks the D50 future day (#27).
  Verify: all `med_*` scenarios pass (done 2026-10-24 19:34)

## 5. Documents

- [x] 5.1 `docs/QA.md`: the clock-moving approach (D60) and the coverage rule (D61). Verify:
  `grep -n "D60\|D61" docs/QA.md`

## 6. Verify

- [ ] 6.1 Full regression (`qa/maestro.sh`), all device scenarios, `python3 -I qa/e2e_coverage.py` exit 0. Record the
  results, the coverage and the remaining medication gaps with reasons in a "Verification record" below
- [ ] 6.2 Gate `./gradlew assembleDebug test ktlintCheck detekt` and `openspec validate add-medication-e2e-coverage
  --strict`

## Medication scenarios without a flow (D61)

Each line gives the reason and the follow-up. "Unit" means the rule is covered by the dev change's unit or
instrumented tests (change 3: medication domain and `MedicationDataSource`; 3a: `GetMedicationPromptUseCase` and the
overuse counting).

| Scenario | Reason | Follow-up |
|---|---|---|
| medication-effect › Answer in the attack's details › Answer later | Cheap flow, not written yet | Next QA change |
| medication-effect › Accessible question › Screen reader, Largest font | Belongs to the audit, not a flow | Add the card to `qa/a11y_audit.sh` states |
| medication-intakes › One tap logs a medication › Log during an attack | Partly covered: `add-and-limits` logs during an attack with "Add and log" | Next QA change: a one-tap log |
| medication-intakes › One tap logs a medication › Time in the future | Unit (`MedicationViewModelsTest`: a future time is refused) | Next QA change |
| medication-intakes › Intakes link to their attack › Logged from home in an attack's first minute | Needs the minute edge; unit (`FindAttackAtRepository.attackDuring`) | Device scenario with a pinned clock |
| medication-intakes › Intakes link to their attack › Undo, then a new time from home | Not written yet | Next QA change |
| medication-intakes › Intakes link to their attack › Later, for an attack recorded in another zone | Not written yet | Zone device scenario |
| medication-intakes › Intakes follow their attack › Delete and undo, Merge, Undo a start after logging a medication | Instrumented (`AttackDao` cascade, merge moves intakes, discard keeps intakes) | Next QA change |
| medication-intakes › Usable in the dark room › Large choices | Belongs to the audit (48 dp and 64 dp targets) | Add the log screen in attack mode to the audit |
| medication-intakes › Accessible logging › Rotation keeps the time | Not written yet | Next QA change |
| medication-list › Edit a medication › Rename | Not written yet; renaming to an archived name is covered | Next QA change |
| medication-list › Order and limit › Recently used first | Not written yet | Next QA change |
| medication-list › Order and limit › Limit reached | 50 medications through the UI take about 25 min; unit (restore checks the limit) | Device scenario, release runs only |
| medication-list › Accessible medication screens › Screen reader on a medication | Open issue #29 (S4): the label says " · " instead of ", " | The flow's regex follows the fix |
| medication-overuse › 11 scenarios (two categories in one day, Not sure, Preventive, Thirty days back, Doctor warning for triptans, Below the soft level, Several categories together, A single category already warned, Got it, Another category's Got it, After 30 days) | Unit (3a counting rules). Each needs 10–15 logged days through the UI, about 9 min each; the simple-painkiller path is covered end to end by `med_overuse` | One multi-category device scenario in the next QA change |
