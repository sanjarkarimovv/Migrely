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

Goals: no day number or weekday initial ever wraps or is cut, in any window; list rows never break inside a word; the
calendar in portrait at 360 and 411 dp unchanged to the pixel; list rows unchanged wherever their date already fits beside
the peak (a row whose date broke inside a word on `develop` stacks, in any orientation; D6).

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

The number is under `clearAndSetSemantics {}` today. As built (accepted 2026-11-27): `hideFromAccessibility()` was
rejected because Compose 1.9.3 merges hidden children into the parent's merged node, which would change the day's TalkBack
label. Each hidden Text keeps `clearAndSetSemantics` and sets only a custom semantics key (`ShownTextLayoutsKey`,
designsystem `layout/ShownTextLayouts.kt`) that is not important for accessibility and whose merge policy keeps the
parent's value, so it never reaches the clickable node. For the same reason the list row's date and peak clear their
semantics on each Text instead of on the Row. Proof: the label assertions (calendar 2.5, row "A stacked row says the same
and stays one button") equal `develop`'s label; code review checks that an unstacked row's label is unchanged too.

### D4. Pane guard (D198 (4))

Two panes iff `isCompactHeight()` (height < 480 dp) and `(W − 2 × 16 dp page padding − 24 dp) / 2 ≥ 200 dp`, where W is the
`BoxWithConstraints` width History's content gets less the horizontal system-bar and cutout insets
(`LocalConfiguration.screenWidthDp` includes the bars on API 35+, so it is not used). Equivalently: two panes when the
width left after the side bars and cutout is at least 456 dp. Confirmed as built; the tests check 455 dp (one column) and
456 dp (two panes). A tester checks it with `wm size` and no side bars: 455 dp wide gives one column, 456 dp two panes.
640 and 800 dp keep two panes; a 640 dp phone with a 48 dp side navigation bar leaves 592 dp, still two panes.

What 200 dp buys (corrected after code review ee02ad70, N1): a 200 dp pane gives a drawn cell of (200 + 6) / 7 − 6 =
23.4 dp and a number box of cell − 5 dp − 3 dp = 15.4 dp. The 100% number needs 15.2 dp (12 sp × 1.264 em), a 23.2 dp
cell: 0.26 dp to spare, not the 3 dp the plan claimed (its "20.2 dp" counted only the start inset). At mdpi, hdpi and
xhdpi pixel rounding uses that up: the number stays at the 100% floor and runs up to about 1 dp into the 3 dp end inset.
The Text's own box is cell − 5 dp, so nothing wraps or is cut; at hdpi, in windows 456 to about 460 dp wide, its end
touches the 2.5 dp chosen ring by about 0.5 px. Decision: the guard stays at 200 dp and the margin is accepted. Such a
window (under 480 dp tall, 456–460 dp wide) is a rare split-screen size, nothing is cut, and raising the guard would move
the threshold a tester checks. So D198 (3) is reachable up to pixel rounding, not "always".

### D5. List row stacking (D181 (5))

Measure the peak's single-line width and the date's widest unbreakable piece. As built (accepted 2026-11-27): Compose's
`minIntrinsicWidth` let the side-by-side branch through while the real layout broke inside "12:27" (78 px room in
Robolectric), so the row measures its widest space-separated word itself with a `TextMeasurer` and the same style
(`text/WidestWidth.kt`, shared with the calendar fit); a no-break space keeps its pieces together. A word is never
narrower than any piece the layout could break it into, so the error can only be an extra stack, never a broken word.
Side by side iff `inner − 12 dp − peak ≥ widest word`; else a `Column`: date at full width, peak 4 dp below, start
aligned. Use the `PickerTopRowTest` line-break assertion pattern. The row keeps its merged label and `Role.Button`.

Each row decides alone (ux L1). One list may therefore mix rows: in R25's geometry a "7/10" row stays beside its date
while a "Not recorded" row stacks. Accepted: every row is whole and readable, and stacking every row would make short-peak
rows taller in a 360 dp tall window, where height is scarce. A list-wide rule would be a new ux decision, not needed for
this fix.

### D6. A row that already broke on `develop` stacks, in any orientation

The rule depends on measured widths, so it can also fire where the plan expected no change. In Robolectric at mdpi, 360 dp
portrait at 200%, `develop` already broke "In progres|s"; this branch stacks that row. That is D181 (5)'s rule doing its
job (a word broke, so the layout stacks), recorded as EC-HIS-07's covered case, not a regression. At xxhdpi (420–480 dpi
phones) portrait 360 and 411 dp at 200% stay side by side, as the negative controls show. Rows whose date fits are
unchanged at every density.

## Risks

| # | Risk | Mitigation |
|---|---|---|
| R1 | Goldens shift by a pixel from a changed modifier chain | Goldens are never regenerated; any diff is a defect to fix in code |
| R2 | Exposing the number to tests changes TalkBack's label | D3 rule; label and order tests |
| R3 | The 24 dp-at-200% and 152 dp-pane numbers are worked out, not measured | Device walk measures R25 again plus split screen and side navigation |
| R4 | One column in split screen is a new look for a few users | Existing portrait layout; no new code path for content |
| R5 | API 26 (linear scale) and API 36 (non-linear) differ | Both in QA rows |
| R6 | Robolectric draws no insets, so its 640 dp window gives 292 dp panes, wider than R25's 275 dp | Stacking tests use `w606dp-h360dp-land-xxhdpi` (R25's pane); a 640 dp case checks only that no word breaks; the device walk re-takes R25 |
