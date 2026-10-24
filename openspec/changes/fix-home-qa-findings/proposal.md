# Proposal

## Why

The QA session tested `develop` with Maestro and found four defects on the home screen (GitHub issues #12 to #15). One
of them stores wrong data in the core flow.
- **#12 (S2):** "Migraine ended" in attack mode sits under the lower 32 dp of "Migraine started". A double tap there,
  common for a person in pain, starts an attack and ends it at once (a 0-minute attack). The mirror case ends an attack
  and starts a phantom one.
- **#13 (S3):** the attack-mode line "Started … · N min" has 4.05:1 contrast. BRD §10 requires WCAG 2.1 AA (4.5:1).
- **#14 (S3):** today's attack rows are not focusable for accessibility services. `clearAndSetSemantics` drops the
  clickable's focus semantics. Keyboard focus itself still reaches them, which was checked on the emulator. The
  calendar days and the History rows of change 2 use the same pattern.
- **#15 (S4):** after "Started earlier? → 1 h", attack mode shows "59 min" for up to 30 s, because the clock behind the
  elapsed time ticks every 30 s and does not refresh when the start moves.

## What Changes

- **Mode switch tap guard (#12, EC-UX-18, D32):** after home switches between normal mode and attack mode, every
  control of the new mode ignores taps for 1 s. There is no guard after launch or rotation.
- **Contrast (#13):** the start-time line uses the full attack-mode text colour (about 5.7:1). It stays dim and never
  pure white (EC-UX-01).
- **Focusable for accessibility services (#14, EC-UX-19):** rows and calendar days merge their description into the
  clickable instead of clearing its semantics. This applies to today's attack rows on home, the History rows, the
  calendar days and the month card's title.
- **Elapsed time (#15):** the clock refreshes as soon as the start of the attack changes.

**Out of scope:**
- The order of keyboard focus and its visible indicator, which stay the platform defaults.
- Other screens, which QA found clean.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `attack-logging`: a double tap never crosses a mode switch.
- `attack-mode`: readable dim text; the elapsed time follows a moved start.
- `today-sky`, `month-sky`, `attack-history`: rows and days are focusable for keyboards and switch access.

## Impact

- **Changed modules:**
  - `:features:home:ui`: `HomeScreen.kt`, `TodayContents.kt`, `AttackModeContents.kt`;
  - `:features:history:ui`: the History rows and the month card title;
  - `:designsystem`: `MonthSkyCalendar`.
- **Unchanged:** the domain, presentation, data and datasource layers.
- **Docs:** BRD 9 (EC-UX-18, EC-UX-19), 13 (row 2a) and 17 (D32).
- **QA retest** on emulator-5580:
  - the Maestro flows `double-tap-start.yaml` and `double-tap-end.yaml`, currently tagged known-bug;
  - the full regression;
  - the #13 contrast and the #14 focusable flag.
- **BRD references:** EC-ATK-01, EC-UX-01, EC-UX-04, EC-UX-18, EC-UX-19, D32, §10 (WCAG 2.1 AA).
