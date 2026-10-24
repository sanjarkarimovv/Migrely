# Proposal

## Why

Changes 3 and 3a added 52 medication scenarios. PR #24 added 9 time-zone and landscape scenarios. None of them had an
end-to-end check, and the medication rules depend on time:
- the "Did it help?" window runs from 2 to 24 h;
- overuse counts distinct days in the last 30;
- an intake logged from home joins the attack whose time it falls in.

These can only be seen on a device by letting time pass.

## What Changes

- **Retest of PR #24**, recorded on the PR:
  - the #20 flow loses `known-bug` and asserts "+ Medication taken";
  - the travel scenario names its new spec scenarios and checks that History shows the city once.
- **Zone scenarios** (`qa/device_scenarios.sh`): `travel_west`, `same_date`, `zone_attack`, `zone_history`.
- **Medication flows:**
  - `.maestro/medication-list/`: add with type and dose, preventive without a category, the 60-character limit,
    unique names, archive and Undo, renaming to an archived name, the archived name coming back;
  - the first medication from attack mode.
- **Medication device scenarios (D60):**
  - `med_effect`: too early, the card, the answer, changing it in details, Undo keeps it;
  - `med_effect_attack`, `med_effect_skip`, `med_effect_late`, `med_preventive`, `med_two_waiting`;
  - `med_link_home`: an intake from home joins a past attack;
  - `med_overuse`: soft at 13 days, "Got it", doctor at 15.
- **Subflows:** `start-attack` and `end-attack` mark attack mode by "Details of this migraine", which stays visible in
  both orientations.
- **BRD:** row 3b, D60, D61.

**Out of scope:**
- Product code. Issues found are filed for the dev session.
- Overuse across several categories, "Thirty days back", and dose and zone display on later entries. They are listed
  in tasks.md with a reason and a follow-up QA change (D61).
- Change 4 (day markers, PR #28): its flows come in their own QA change; here only its changed copy is followed.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `quality-gates`: device scenarios also cover medication timing and time-zone changes while the app runs, and new
  capabilities get end-to-end coverage before their QA is closed (D60, D61).

## Impact

- **New:**
  - `.maestro/medication-list/`;
  - `.maestro/device/` medication and zone steps;
  - the subflows `add-first-medication`, `add-medication`, `open-my-medications`, `log-from-home`.
- **Changed:** `qa/device_scenarios.sh` (12 new scenarios), `start-attack` and `end-attack`, the travel and #20
  flows, `docs/QA.md`.
- **Unchanged:** product code, build files and the gate.
- **BRD:** MED-F1 to F5, EC-MED-07 to 12, EC-TIM-02, D36, D50, D60, D61.
