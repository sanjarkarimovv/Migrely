# Design

Release 1.3.2, tracker T75, issue #245 (S3, accessibility, pre-existing). BRD D192, EC-UX-39, EC-UX-40, EC-UX-43, EC-UX-65,
EC-UX-72 to EC-UX-77; D180 (2), (4), (7), (8), ux U1 to U3; D189 (#238). The behaviour is `ux-designer`'s
`docs/ux/245-short-window-cursor.md` (R1 to R12, SW1 to SW13; 11f26d76), measured against QA's re-measure (#245 comment
6970307531). This file binds what the person sees and what QA checks; the code shape is the engineer's call within it.

## Context

Terms: *L* = height of the line being typed (the note's caret line box, `CursorGeometry.lineOf` without its margin; a
single-line field's laid-out input line). *M* = 8 dp. *H* = the visible part from under the top bar to the keyboard's top.

| Where (200% landscape) | H | L | Today | Rule |
|---|---|---|---|---|
| API 36 Gboard 360 dp, Attack details | 61 px | 112 px | 40 px of the line | R3 (61 + 194 = 255 px) |
| API 36 Gboard 360 dp, Edit / Log name | 87 px | 112 px | 68 px of the line | R3 (87 + 168 = 255 px) |
| API 36 Gboard 411 dp, Attack details | 137 px | 112 px | whole, 4 / 21 px | R2 band, already whole |
| API 36 Gboard about 395–409 dp (estimate) | 112–132 px | 112 px | cut by up to 21 px (predicted) | reduced R2 (conditional) |
| API 26 AOSP 360 and 411 dp | 156 px and more | 112 px | whole, 8 dp | R1, guard |

Existing parts: `CursorFollowingTextField` (note: brings `lineOf(layout, cursor, margin)` into view), `SingleLineFieldInView`
(U1 order), `KeyboardFollow` (follows only while in view), `KeyboardRoom` (#238: the room after the last item on close),
`MessageLayout` (top bar slot, then content, message over content; used by Attack details, Edit and Log medication), and
Delete all data's own `TopAppBar` with `rememberRowRidesAboveKeyboard` (D180 (8), decided once from `imeAnimationTarget`).

## Goals and non-goals

Goals: R1, R3, R5 to R12 on the four screens, reduced R2 if task 2.0 is red; one shared mechanism (DRY); #238 untouched; a
check for each rule that can fail.

Layers: `designsystem`, then the four screens' `ui` modules. No domain, presentation, data, DI or navigation change.

Non-goals: R4, equal margins, a pull on close, screens without a text field, strings.

## Decisions

### D1. One decision per keyboard opening (R5)

At the keyboard's final height (API 30+ `WindowInsets.imeAnimationTarget`, API 26–29 the inset after its single step), the
screen compares H (measured with the bar present) with L (from the focused field's layout) and decides `barAside = H < L`.
The decision is held until the keyboard closes; cursor moves, more lines, scrolling, messages and a keyboard height change
(symbols, emoji) do not re-decide. A new opening, a rotation or a font change decides again. No bottom IME inset: never aside
(R9). The same "decide once from the final height" shape as Delete all data's row; Delete all data decides its row first,
then the bar (R3 after D7).

Why not a dp constant: nonlinear font scaling makes 200% text 42.7 dp; a 64 dp constant would wrongly hide the bar on API 26
at 360 dp (156 < 168 px). SW4's mutation proves the guard can fail.

### D2. The bar's height follows the keyboard's inset fraction (R3, R6)

While stepping aside, the bar's laid-out height goes from its full height to 0 in proportion to the keyboard's inset
animation fraction (no own curve). On API 26–29 and with Remove animations it is one step. The bar's content is clipped, not
drawn over the page; no alpha fade through another colour. The area it leaves is the page's background (dark room in attack
mode). The status bar inset stays.

Place: a shared modifier or wrapper in `designsystem` used for the top bar slot of `MessageLayout` and for Delete all data's
`TopAppBar`, so one implementation serves four screens.

### D3. Semantics while aside (R11)

While aside (height 0), the bar's subtree is removed from the semantics tree (for example `clearAndSetSemantics {}` with
`invisibleToUser` or by not composing its content), so TalkBack, Switch Access and Tab skip it. Nothing is announced on leave
or return.

### D4. Reduced R2: the line before its margins (conditional)

When L ≤ H < L + 2M, the follow brings the line itself into view first, then as much margin as fits; it never evens the
margins out. One way: request the bare line rect (`lineOf` with margin 0) when the margin rect does not fit H, then the margin
rect clipped to H. Only after task 2.0 is red.

### D5. Closing keeps #238 (R7, R8)

On close the bar returns over the same frames; the scroll position does not change, so the page moves down by the bar's
height and `KeyboardRoom` adds room for the rest of the visible part's growth (keyboard height minus bar height). No
`scrollTo`, no `bringIntoView` on close. The first system Back closes the keyboard; the second leaves.

### D6. Messages (R10)

Messages keep the coordinates D180 (4) gives them with the bar present; the bar stepping aside does not move them, and a message
does not re-decide the bar.

## Risks

- **The R2 band is an estimate.** If task 2.0 cannot show the cut on v1.3.1 code, R2 and SW13 drop.
- **Robolectric and IME animation.** Tests drive the IME inset by hand (as `KeyboardRoomTest` does); the frame-level "same
  frames as the keyboard" claim needs QA's 60 fps recording (SW8).
- **#238 guards may need a bar-aside variant.** Existing keyboard-room tests run with the bar present and must pass unchanged;
  new cases cover the bar-aside close.
- **Shared files with #239** (`EditMedicationScreen`, `LogMedicationScreen`): different regions; whichever merges second
  merges `develop` first.
- **Other keyboards and real phones** are not measured; the trigger uses measured H and L, so it adapts, but QA cannot prove it.

## Acceptance (for `qa-engineer`, this diff only)

Rows SW1 to SW13 of `docs/ux/245-short-window-cursor.md` section 7, plus rows K1 and K2 below for #238. Build: the PR head.
Negative control: v1.3.1 (`v1.3.1` tag). SW1 must show 40 of 112 px on v1.3.1; SW2's dose and Delete all data cells are
measured on v1.3.1 first, and a cell whole there becomes a guard with SW3's mutation as its control. SW4 is an API 26 guard
whose control is the constant-64-dp mutation. SW13 runs only if task 2.0 was red, and is itself R2's disproving control.

| Row | Steps | Candidate | Control |
|---|---|---|---|
| K1 | #238's KR1 and KR4 rows (four screens, near the end, Back, TalkBack on) on the candidate at 100% portrait and 200% landscape 411 dp | unchanged from 1.3.1's GO: 0 px move, no scroll event, TalkBack on the field | v1.3.0 (jumps), as in #238's record |
| K2 | SW5: the 360 dp API 36 set-up with the bar aside, close by Back and by the hide key, TalkBack VERBOSE | bar back, scroll position unchanged, field lower by exactly the bar's height, 0 TYPE_VIEW_SCROLLED, TalkBack on the field | mutation that keeps the field's screen position by scrolling: a scroll event, red |
