# Tasks

## 1. Calendar keyboard (design D1)

- [x] 1.1 `MonthSkyCalendar`: Tab leaves the grid, entering focuses the anchor day, arrows by day and week, a visible
  focus ring; Compose instrumented tests (`MonthSkyCalendarKeyboardTest`, 10 tests; the first 6 were checked to fail
  without the grid's handling).
  Verify: `ANDROID_SERIAL=emulator-5570 ./gradlew :designsystem:connectedDebugAndroidTest`
- [x] 1.2 History and the month card pass the anchor day. Not needed: the anchor comes from `CalendarDay`'s own
  flags (design D1).

## 2. Landscape message (design D2)

- [x] 2.1 `MigrelySnackbarHost` reports the message's bottom; `SideBySideAttackLayout` keeps the guidance column below
  it (`MigrelySnackbarHostTest`, 2 tests; `GuidanceTopPaddingTest`, 4 cases).
  Verify: `./gradlew :features:home:ui:test` and the designsystem instrumented tests

## 3. Verify

- [x] 3.1 Gate and designsystem instrumented tests.
- [x] 3.2 Emulator check on emulator-5570:
  - a hardware keyboard on home and History: Tab in and out, Shift+Tab, the anchor, arrows, Enter;
  - landscape attack mode with "Migraine started" at 100% and 200%, and with "Reduce motion";
  - `uiautomator dump` still reports the days as focusable.
- [x] 3.3 Independent review; fix and record.

## Verification record

**Gate** (`./gradlew assembleDebug test ktlintCheck detekt`): BUILD SUCCESSFUL, 848 unit tests, 0 failures.

**Designsystem instrumented tests** on emulator-5570: 28/28 (16 before, plus `MonthSkyCalendarKeyboardTest` 10 and
`MigrelySnackbarHostTest` 2).

**Emulator walk** (first build, by a walk agent): all scenarios passed.

| Scenario | Result |
|---|---|
| Home Tab order | "Period" → "October. Opens History." → today → "Migraine started": the calendar is one stop, and "Migraine started" is 4 Tabs from launch (about 30 before) |
| Shift+Tab | lands on today, the next one leaves backwards |
| Arrows, Enter, ring | 14 → 15 → 22; Enter on 23 opens History with 23 chosen, as a tap does; the ring is clearly visible and differs from today's outline |
| History | the chosen day, or 1 September in a past month with no chosen day |
| Accessibility services | all 24 past days are focusable and clickable, as before |
| Landscape attack, 100% and 200% | the message over the start column; "Rest now" starts below it with no line cut; back up once it goes; the controls never move |
| Reduce motion | no in-between positions, no overlap |

**Found in the walk:** one frame with the guidance 24 dp too low on entering attack mode, the same as review finding
4. Fixed.
**Noted, not from this change:**
- in landscape, "Migraine ended" needs a scroll at 200%;
- the message is centred for one frame before attack mode takes over.

## Review (3.3)

An independent `superpowers:code-reviewer` pass. It found nothing critical, and confirmed:
- the onEnter mechanism against the Compose 1.9.3 sources, and Tab handling only while focus is in the grid;
- RTL order, wrap-around, an empty month, accessibility, touch mode, the coordinate spaces, and that there is no
  feedback loop.

| Finding | Outcome |
|---|---|
| **I1** the guidance column's buttons move with it: a quick second tap could hit another button | fixed: a `TapGuard` over the column while it moves and for 500 ms after; D52 and an in-app-messages scenario say so |
| **I2** the whole home screen recomposed on every frame of the message's animation | fixed: a `() -> Dp` reader, read only in a layout modifier |
| M3 the guidance slid up under the fading message | fixed: the host shrinks only after the fade |
| M4 a first-frame jump (also seen in the walk) | fixed: no padding until the column is placed |
| M5 the arrows used 2D search, which jumped weeks next to the last selectable day | fixed: day and week steps, mirrored in RTL, doing nothing off the selectable days; a spec scenario |
| M6 test gaps | added `GuidanceTopPaddingTest`, and keyboard tests for the edge, RTL, a chosen day that cannot be chosen, and an empty month |
| M7 docs and naming | design uses the right test name and "root coordinates"; the spec says "start column"; `ChosenRingWidth` named; the padding formula lost its 8 dp gap, because the viewport now starts at the message's bottom (a scrolled line could hide under those 8 dp) |
