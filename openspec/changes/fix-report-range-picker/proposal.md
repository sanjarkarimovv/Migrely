# Proposal

## Why

#102 (S3, accessibility, found by the #101 walk): at 200% font the Doctor report's "Choose the dates" dialog shows no
calendar, in landscape and in portrait, so a custom range cannot be picked. Only Cancel and Done can be reached. At 100%
in landscape it shows only about 2.5 weeks.
- **Cause:** Material's `DateRangePicker` sits in a `DatePickerDialog`, which is 360 dp wide and at most 568 dp high
  with its buttons inside. The picker's header does not scroll, and the months are a `LazyColumn` that gets only the
  height left under it.
  - The title is one more line in the header.
  - The headline starts 64 dp in and shows the two dates as separate texts in one row, so in 360 dp the end date wraps
    into many short lines.
  - At 200% that leaves the months no height.
- **Why #101's fix does not apply:** a `LazyColumn` cannot sit in a vertical scroll.
- **Material's guidance:** on phones, a date range picker is a full-screen modal.

## What Changes

- **Design system:** a new `MigrelyDateRangePicker` (BRD D127).
  - It opens full screen, so the picker gets the whole height and width.
  - The top row holds Cancel, the title and Done. The picker has no title of its own, so that line leaves the header.
  - The headline is Material's, so its "Start date: …, End date: …" description and live region stay. It starts 24 dp
    in instead of 64 dp, so the dates wrap less.
  - The window's pane title is the title, so TalkBack names it when it opens.
  - Done is on only once the range has an end, as in Material's own full-screen picker. Picking the same day twice
    gives one day. The old dialog took a start alone. The walk showed this lets text input mode give a one-day range
    silently, when the changed start dropped the end, or when the end was not allowed.
  - It pads for the system bars but not the keyboard, so in text input mode the fields at the top keep their size.
- **Doctor report:** the custom range uses it. Its rules stay in the feature:
  - up to today;
  - this year and the two before (BRD D81, D84);
  - the range chosen before is preselected (walk B5).
- **Spec:** `doctor-report` gets a requirement for landscape and 200% font, and for Done.

**Unchanged:**
- the periods of 1, 3, 6 and 12 months;
- the 24-month check;
- the report itself.

## Capabilities

### Modified Capabilities
- `doctor-report`: a custom range can be picked at any font size and in landscape, and Done needs an end.

## Impact

- `:designsystem`: new `picker/MigrelyDateRangePicker.kt` and its instrumented test.
- `:features:report:ui`: `ReportScreen.kt` (`RangeDialog`).
- `docs/BRD.md`: D127.
