# Proposal

## Why

#102 (S3, accessibility, found by the #101 walk): at 200% font the Doctor report's "Choose the dates" dialog shows no
calendar, in landscape and in portrait, so a custom range cannot be picked. Only Cancel and Done can be reached.
- **Cause:** Material's `DateRangePicker` sits in a 360 dp `DatePickerDialog`. Its header does not scroll:
  - the title keeps it at least 128 dp high;
  - the headline shows both dates in `headlineLarge`, as separate texts in one row, so at 200% the end date wraps
    into many short lines.
  The months are a `LazyColumn` that gets only the height left under the header, which at 200% is none.
- **At 100% in landscape** it already shows only about 2.5 weeks.
- **Why #101's fix does not apply:** a `LazyColumn` cannot sit in a vertical scroll.
- **Material's guidance:** on phones, a date range picker is a full-screen modal.

## What Changes

- **Design system:** a new `MigrelyDateRangePicker` (BRD D127).
  - It opens full screen, as Material advises for range pickers on phones.
  - The top row holds Cancel, the title and Done.
  - The picker has no title of its own, so its header has no minimum height.
  - Its headline is Material's, so the "Start date: …, End date: …" description and the live region stay, but it uses
    `titleLarge`.
  - Done is on once a start is chosen. A start alone gives a one-day range, as before.
- **Doctor report:** the custom range uses it. Its rules stay in the feature:
  - up to today;
  - this year and the two before (BRD D81, D84);
  - the range chosen before is preselected (walk B5).
- **Spec:** `doctor-report` gets a requirement that the custom range can be picked in landscape and at 200% font.

**Unchanged:**
- the periods of 1, 3, 6 and 12 months;
- the 24-month check;
- the report itself.

## Capabilities

### Modified Capabilities
- `doctor-report`: a custom range can be picked at any font size and in landscape.

## Impact

- `:designsystem`: new `picker/MigrelyDateRangePicker.kt` and its instrumented test.
- `:features:report:ui`: `ReportScreen.kt` (`RangeDialog`).
- `docs/BRD.md`: D127.
