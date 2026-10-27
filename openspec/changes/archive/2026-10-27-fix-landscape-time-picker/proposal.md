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
- **Reminders:** the time dialog does the same. It becomes a dialog that scrolls as a whole, like the design system's
  picker, in place of an `AlertDialog`. Inside an `AlertDialog`, only the text slot scrolled, and in landscape the
  256 dp dial filled it, so drags turned the hand instead of scrolling (review finding).

**Unchanged:**
- portrait, which already used the vertical layout;
- the date step;
- the picked values and their checks.

**Out of scope:** nothing.

## Capabilities

None changed. The fix makes the code meet the existing landscape and accessibility requirements.

## Impact

- `:designsystem`: `MigrelyDateTimePicker.kt`. It is used at seven places: add a past migraine (start and end), attack
  details (start and end), a pain entry's time, attack mode and logging a medication.
- `:features:reminders:ui`: `RemindersScreen.kt` (`TimeDialog`).
