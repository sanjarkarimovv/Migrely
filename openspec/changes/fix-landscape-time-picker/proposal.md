# Proposal

## Why

QA #79 (S2, accessibility) shows that no afternoon time can be picked in landscape in 1.0.0.
- **Where:** "Add a past migraine" › Ended › Next, and "Log medication" › Taken at.
- **Cause:** Material's `TimePicker` switches to its horizontal layout in landscape. That layout is wider than the dialog
  (840 px at 420 dpi), so the dial runs past the edge and hides the minutes, the PM half of the AM/PM switch and the
  hours 1 to 5.
- **The spec it breaks:** the specs already require these screens to stay usable in landscape and at 200% font.

## What Changes

- **Design system:** `MigrelyDateTimePicker` uses the vertical time picker in every orientation. Its column already
  scrolls, so in landscape the user scrolls to the buttons.
- **Reminders:** the time dialog does the same, inside a scrolling column.

**Unchanged:**
- portrait, which already used the vertical layout;
- the date step;
- the picked values and their checks.

**Out of scope:** nothing.

## Capabilities

None changed. The fix makes the code meet the existing landscape and accessibility requirements.

## Impact

- `:designsystem`: `MigrelyDateTimePicker.kt`.
- `:features:reminders:ui`: `RemindersScreen.kt` (`TimeDialog`).
