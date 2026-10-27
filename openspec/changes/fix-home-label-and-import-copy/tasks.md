# Tasks

## 1. Fix

- [ ] 1.1 Home: `attack_summary_peak_not_recorded_spoken` ("not recorded") for the row's label
- [ ] 1.2 Your data: "Added the new entries." when an Add brought only other entries and the file had no migraine

## 2. Verify

- [ ] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [ ] 2.2 `openspec validate fix-home-label-and-import-copy --strict`
- [ ] 2.3 On emulator-5570: home's row label with no pain recorded reads "…peak not recorded…"; an Add of a file with
  only medications and intakes says "Added the new entries."
