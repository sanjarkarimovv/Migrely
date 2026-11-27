# Design

## Context

Base `origin/develop` 476f7a27. The design of record is ux-designer's ruling,
`migrely-work/patch141b/ux-rulings.md` section 1 (2026-11-27). Code read:

- `designsystem/.../sky/MonthSkyCalendar.kt`: the day number is `labelSmall` with `tnum` (line ~397), inset 5 dp start and
  3 dp top, under `clearAndSetSemantics {}`; the cell clips with `CellShape`. Only `HistoryCalendar.kt` calls it.
- `features/history/ui/.../HistoryScreen.kt:324`: `if (isCompactHeight())` (height < 480 dp) gives two equal panes with
  24 dp between; else one column capped at `CONTENT_MAX_WIDTH`. `AttackRow` (line ~624) is a `Row` with the date
  `Text(weight(1f))` and the peak `Text`.

The person's moment: they open History with large text, turned sideways, to find which day an attack was. The day number
must be the true day.

## Goals and non-goals

Goals: no day number or weekday initial ever wraps or is cut, in any window; list rows never break inside a word; portrait
at 360 and 411 dp unchanged to the pixel.

Non-goals: #283 touch pitch; any change to cells, gaps, colours, labels; the Today screen.

## Decisions

### D1. Two changes, not one with #179

#275 lives in `designsystem` sky and `features/history`; #179 in `designsystem` menus and `features/attack`. Different
decisions (D198, D199), different reviewers' focus; each can slip or be reverted alone, and two engineers can build them
in parallel. The cost is two BRD merges touching neighbouring rows (D198/D199, §13 rows 63/64): the second to merge
resolves the textual conflict, keeping numeric order.

### D2. Fitted size, measured, one per grid (D198 (2))

| Approach | Result | Choice |
|---|---|---|
| `TextMeasurer` over "28".."31" in Bold at sizes from the person's down to 12 sp | Works with Android 14's non-linear scale; one size | **Yes** |
| `fontScale` arithmetic | Wrong on API 34+ (non-linear) | No |
| Fixed cap (150%) | Still cuts at the 251 dp landscape pane and in split screen; shrinks portrait needlessly | No |
| Smaller gap | Moves every drawn pixel (goldens), merges dark cells | No |
| `autoSize` per Text | One size per day, so days differ; Compose version risk | No |

Measure the widest two-digit string with `tnum` in Bold (632/1000 em) against `cellWidth − 5 dp − 3 dp`. Step in whole
sp from the person's `labelSmall` size down to 12 sp; a binary search is fine. Key the result on `(cellWidthPx,
fontScale, density)` with `remember`, never on the month. The weekday row does the same with its widest initial.

### D3. Reaching the number in a test

The number is under `clearAndSetSemantics {}` today. Expose its layout for tests (for example a test tag with
`hideFromAccessibility()` in place of the clear) only if TalkBack's merged label stays identical; the existing TalkBack
order test plus a label assertion prove that. If merging changes, use a `TextLayoutResult` callback captured in a test
hook instead. Engineer's choice, stated in tasks.

### D4. Pane guard (D198 (4))

Two panes iff `isCompactHeight()` and `(windowWidthDp − 2 × 16 dp page padding − 24 dp) / 2 ≥ 200 dp`, that is a window at
least 456 dp wide. Use the same width source the layout already uses (`LocalConfiguration.screenWidthDp` or the
`BoxWithConstraints` width the content gets); state which in the code and test both sides of 456 dp. 640 and 800 dp keep
two panes.

### D5. List row stacking (D181 (5))

Measure the peak's single-line width and the date's `minIntrinsicWidth` (its widest unbreakable piece). Side by side iff
`inner − 12 dp − peak ≥ dateMinIntrinsic`; else a `Column`: date at full width, peak 4 dp below, start aligned. Use the
`PickerTopRowTest` line-break assertion pattern. The row keeps its merged label and `Role.Button`.

## Risks

| # | Risk | Mitigation |
|---|---|---|
| R1 | Goldens shift by a pixel from a changed modifier chain | Goldens are never regenerated; any diff is a defect to fix in code |
| R2 | Exposing the number to tests changes TalkBack's label | D3 rule; label and order tests |
| R3 | The 24 dp-at-200% and 152 dp-pane numbers are worked out, not measured | Device walk measures R25 again plus split screen and side navigation |
| R4 | One column in split screen is a new look for a few users | Existing portrait layout; no new code path for content |
| R5 | API 26 (linear scale) and API 36 (non-linear) differ | Both in QA rows |
