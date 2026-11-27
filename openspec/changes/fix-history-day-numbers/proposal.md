# Proposal

## Why

**#275 (S3, accessibility, pre-existing).** History at 200% font in a 640 × 360 dp landscape window, English and German
(QA R25, build 8d56c42b):

- The calendar's drawn cell is 34.3 dp. A two-digit day in Bold needs 1.264 em, about 30.3 dp at 200%, plus the 5 dp
  inset: 35.3 dp. Every day from 10 to 31 wraps, and the cell's clip cuts the second line, so "16" shows as "1". A cut day
  number is misread health data: the person ties an attack to the wrong day.
- The list rows squeeze the date beside the peak: English breaks "Mon / , / Nov", German cuts the time to "12:2" beside
  "Nicht erfasst".
- Worked out, not yet captured: split screen in portrait gives two panes of about 152 dp, where two-digit days are cut even
  at 100%.

D181 and D195 (10) say the font never shrinks and the layout stacks. A seven-column calendar cannot stack, so it needs its
own rule (D198). The list rows need no new rule: D181 (5) stacking, as in EC-UX-48.

## What Changes

- Calendar day numbers and weekday initials: one line, one fitted size per grid, between the 100% size and the person's
  size (D198 (1)–(3), (5)). `MonthSkyCalendar` is used only by History.
- History shows two panes in a short window only when each pane is at least 200 dp wide; otherwise the existing
  single-column layout (D198 (4)).
- History's list rows put the peak under the date when the date's widest piece would not fit beside it (D181 (5)).

## Out of scope

- #283 (edge-column touch pitch of 45 dp at 360 dp; 40.3/37 dp in landscape 640): a different cause, its own ux pass in
  the next minor; QA re-grades it. This change alters no cell, gap or pitch, so it neither fixes nor worsens #283.
- Any change to cell size, gaps, drawings, colours, TalkBack labels, order or touch targets.
- Other screens' text, the Today screen and its links (EC-UX-48 already done), and #272.
- New copy: none, so nothing goes to medical-reviewer.

## Capabilities

### Modified capabilities

- `month-sky`: ADDED "Day numbers fit their cell on one line".
- `attack-history`: ADDED "Two panes only when each pane is wide enough" and "A list row puts the peak under the date
  when the date would break".

## Impact

- Code: `designsystem` (`MonthSkyCalendar.kt`), `features/history/ui` (`HistoryScreen.kt`: the pane guard and
  `AttackRow`), tests in both modules' `src/testDebug`.
- No string, permission, manifest, data, migration or backup change. Goldens in
  `designsystem/src/testDebug/resources/calendar/` stay byte-identical and are never regenerated.
- BRD: D198 (new), EC-HIS-05, EC-HIS-06, EC-HIS-07, §13 row 63.
- 1.5.0 (T78 German doctor report, T70 "Did it help?"): no shared file.
