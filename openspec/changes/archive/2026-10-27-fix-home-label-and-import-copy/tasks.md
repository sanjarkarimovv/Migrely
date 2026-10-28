# Tasks

## 1. Fix

- [x] 1.1 Home: `attack_summary_peak_not_recorded_spoken` ("not recorded") for the row's label
- [x] 1.2 Your data: "Added the new entries." when an Add brought only other entries and the file had no migraine

## 2. Verify

- [x] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [x] 2.2 `openspec validate fix-home-label-and-import-copy --strict`
- [x] 2.3 On emulator-5570: home's row label with no pain recorded reads "…peak not recorded…"; an Add of a file with
  only medications and intakes says "Added the new entries."

## 3. Review

- [x] 3.1 Independent review (`superpowers:code-reviewer`)

## Verification record

- **Gate:** `assembleDebug test ktlintCheck detekt` passes.
- **Emulator (emulator-5570, API 36, debug), 0 FATAL EXCEPTION:**
  - **#84:** a migraine started and ended with no pain gives the row label "Migraine from 6:13 PM to 6:13 PM, peak not
    recorded. Opens its details."
  - **#87:** a backup of 1 medication and 1 intake with 0 migraines, added over a diary with one past migraine, says
    "Added the new entries.". The medication is there afterwards. The same file added again says "Nothing new:
    everything in it is already here."
- **Review:**
  - The `when` gives the right sentence for every combination of migraines added, migraines already here and other
    entries added. A file with no migraines is the only case of no migraines added and none already here.
  - The spec delta repeats the requirement word for word.
  - No other sentence reuses the visible "Not recorded".
- **For QA, whose files these are:**
  - `.maestro/attack-logging/start-and-end.yaml:16` still asserts "peak Not recorded". Maestro likely matches without
    regard to case, so it passes but would not catch #84 coming back.
  - `qa/device_scenarios.sh:1763`'s list of other result messages lacks "Added the new entries.".
