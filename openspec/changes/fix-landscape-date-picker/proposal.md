# Proposal

## Why

QA #101 (S3, accessibility) shows that, in landscape, the date step of every date-and-time picker hides days in 1.0.1.
- **Where:** "Add a past migraine" › Started (and every other use of `MigrelyDateTimePicker`).
- **What QA saw:**
  - days 1–5 of September 2026 are not in the accessibility tree;
  - the second week is 18 dp high and drawn over the weekday letters;
  - the last week is under Cancel and Next;
  - nothing scrolls.
- **Cause:** Material's `DatePicker` draws the month as a grid with a fixed height, `requiredHeight(48 dp × 6)`.
  `DatePickerDialog` gives its content only the height left above the buttons. In landscape that is less than the
  grid needs, so the grid is centred on the space it gets and spills above and below it. The picker itself never
  scrolls.
- **The spec it breaks:** the specs already require these screens to stay usable in landscape and at 200% font
  (`past-attacks`, `attack-details`, `medication-intakes`). The workarounds are text input mode and portrait.

## What Changes

- **Design system:** the date step's `DatePicker` scrolls vertically. Measured at its own height, the month grid
  keeps its 48 dp rows. When the dialog is shorter, the user scrolls to the weeks that do not fit. Like the time step
  since QA #79 it scrolls, but here Cancel and Next stay in place, because the dialog keeps them below its content.

**Unchanged:**
- portrait at the default font size, where the picker fits;
- text input mode;
- the time step;
- the picked values and their checks;
- the Doctor report's `DateRangePicker`, whose months already scroll in a list. Its header does not scroll, so the
  walk also checks it in landscape at 200% font.

**Out of scope:** the AM/PM switch's missing selected state (#92).

## Capabilities

None changed. The fix makes the code meet the existing landscape and accessibility requirements.

## Impact

- `:designsystem`: `MigrelyDateTimePicker.kt`, used at seven places: add a past migraine (start and end), attack
  details (start and end), a pain entry's time, attack mode and logging a medication.
- `:designsystem` instrumented tests: a new `DateStepCalendarTest`.
