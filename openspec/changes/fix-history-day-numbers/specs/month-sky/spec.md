## ADDED Requirements

### Requirement: Day numbers fit their cell on one line
A day number SHALL be one line that never wraps, is never cut and never shows "…". All day numbers of a grid SHALL share
one size: the person's `labelSmall` when the widest two-digit day, in Bold, fits the cell less 5 dp at the start and 3 dp
at the end; else the largest size that fits, never below the lesser of the 100% size and the person's. Weekday initials
SHALL follow the same rule. Cells, gaps, pitch, drawings and colours SHALL not change (BRD D198, EC-HIS-05).

#### Scenario: Landscape at 200%
- **WHEN** History shows a month at 200% font in a 640 × 360 dp landscape window, in English and in German
- **THEN** every day number from 1 to 31 lays out on 1 line, its width is at most the cell's width less 8 dp, and "16"
  reads "16", not "1"

#### Scenario: All days share one size
- **WHEN** the grid had to step its size down
- **THEN** one-digit days, two-digit days and today (Bold) all use that same size, so no day looks more important

#### Scenario: The largest size that fits
- **WHEN** the grid steps its size down
- **THEN** the chosen size fits, and one px more would not fit

#### Scenario: Never below the 100% size
- **WHEN** the cell is too narrow even for the 100% size
- **THEN** the number keeps the 100% size on one line, inside its own box (the cell less 5 dp), and may run up to about
  1 dp into the 3 dp end inset; the History pane guard keeps cells narrower than about 23 dp from being shown, and at a
  200 dp pane pixel rounding can still leave the number at this floor (D198 (4))

#### Scenario: A font smaller than 100%
- **WHEN** the person's font scale is below 1.0 and their size does not fit
- **THEN** the floor is the person's own size, so the fit never makes the number larger than the person chose

#### Scenario: Portrait stays as it is (negative control)
- **WHEN** the calendar renders at 360 dp and at 411 dp in portrait, at 100% and at 200%
- **THEN** the size is the person's own, and the four goldens of `MonthSkyCalendarPitchTest` match byte for byte without
  being regenerated

#### Scenario: The check can fail
- **WHEN** the landscape test runs against `develop`'s calendar
- **THEN** days 10 to 31 lay out on 2 lines and the test fails

#### Scenario: Paging months and rotating
- **WHEN** the user pages between months, or rotates the phone
- **THEN** paging never resizes the numbers; after a rotation the size is worked out again for the new cell, with no
  animation

#### Scenario: Today, chosen and focus rings, period drops and future days
- **WHEN** a day is today, chosen, keyboard-focused, carries a period drop or lies in the future
- **THEN** its number has the grid's one size and stays clear of the ring, with the same 5 dp inset as before

#### Scenario: First use and loading
- **WHEN** nothing is logged yet, or the month is still loading
- **THEN** the dashed cells use the same rule, and while loading nothing is drawn

#### Scenario: TalkBack, keyboard and right to left
- **WHEN** TalkBack or a keyboard moves through the days, or "Force RTL layout direction" is on
- **THEN** each day says the same name and chosen state as before, in the same order, with one Tab stop and arrows inside,
  and the number stays at the top start; the result depends only on width

#### Scenario: Dark, light, Premium and during an attack
- **WHEN** the theme is dark or light, the user is Free or Premium, or an attack is in progress
- **THEN** the rule and the colours are the same; nothing is Premium
