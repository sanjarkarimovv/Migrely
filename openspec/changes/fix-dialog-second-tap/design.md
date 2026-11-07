# Design

D1 to D5 are built on this branch, and `tasks.md` holds the engineer's evidence. QA's independent run is still to come. The measures come from issues
#157 and #159, `recount_dialog.py`, `recount_t10c.py` and QA's `t10b.jsonl`, on an emulator with injected taps. The Compose and
Material facts are read from the sources of Compose UI 1.9.3 and Material 3 1.4.0.

## Context

Dialogs in this app are Compose dialogs, each a window of its own, so `touchesOnlyWhile` and D159's rule cannot reach them. These are
every one, from `git grep -nP '\b(AlertDialog|BasicAlertDialog|Dialog|DatePickerDialog|TimePickerDialog|ModalBottomSheet)\s*\('`:

| Dialog | File | Calls | One-tap write inside |
|---|---|---|---|
| `EffectDialog` | `AttackMedicationsSection.kt` | `AlertDialog` | Yes, each row |
| `AddPainDialog` | `AttackPainSection.kt` | `AlertDialog` | Yes, Add |
| `CustomTagDialog` | `AttackChoiceSections.kt` | `AlertDialog` | No: Add is off until a name is typed |
| `ThemeDialog` | `SettingsDialogs.kt` | `AlertDialog` | Yes, each row |
| `ImportDialog` | `YourDataScreen.kt` | `AlertDialog` | Yes, its import choices |
| `RestoreDialog` | `YourDataScreen.kt` | `AlertDialog` | Yes, Restore |
| Reminder time, `TimeDialog` | `RemindersScreen.kt` | `Dialog` | Yes, OK |
| Date step | `MigrelyDateTimePicker.kt` | `DatePickerDialog` | No: Next goes to the time step |
| Time step | `MigrelyDateTimePicker.kt` | `Dialog` | Yes, its confirm button |
| Date range, used by Report's `RangeDialog` | `MigrelyDateRangePicker.kt` | `Dialog`, full screen | Yes, Done |

The brief's `RangeDialog` is a wrapper of the date range picker, not a dialog of its own. `DropdownMenu` on a custom tag is a popup that
opens on a long press, and it is not covered (#149). No `DialogFragment` or bottom sheet is in the app. The only `MaterialAlertDialogBuilder` is
`app/src/debug/.../DialogSimulatedSheet.kt`, the debug build's stand-in for Play's sheet, which is not in a release.

## Goals and non-goals

**Goals:** no control in a dialog can run until a person has had a moment to see it. A deliberate tap after that answers first time.
No visual change. TalkBack, switch access and a keyboard work at once.

**Non-goals:** see the proposal's out of scope. Above all, a second tap after the wait still runs.

## Decisions

### D1. Why not `TapGuard` as it is (BRD D160)

`TapGuard` is the right idea: touch only, no semantics, no visual. Used as it is, it does not fit, for two reasons.

- **`AlertDialog` has no place for its layer.** `TapGuard` must be a later sibling over the content. Material's `AlertDialog` takes
  separate slots for the title, the text and the buttons, and its layout is internal. Wrapping the content would mean `BasicAlertDialog`
  and rewriting Material's layout, which is a visual change. What `AlertDialog` and `DatePickerDialog` do offer is a `modifier`, which
  `BasicAlertDialog` puts on the box around the whole card, inside the dialog's window. So this must be a modifier.
- **A timer can let a press through under a stall.** `TapGuard` turns off when a `delay` ends. If the main thread stalls, the end of the
  `delay` and a press made during the wait are both waiting, and the delay can run first, so the press goes through. D159 hit the same
  point and judges by the touch event's own time. Here a miss replaces data on the phone.

So the helper keeps `TapGuard`'s method, consuming in the initial pass with no semantics, in a modifier that judges by event time.
`TapGuard` itself and its users stay unchanged. Moving them is a risk with no gain to 1.1.0.

### D2. The helper

In `designsystem`, one file:

| Part | What |
|---|---|
| `DIALOG_WAIT_MILLIS = 500L` | The one constant |
| A pure rule | Asked about a press: its event time, when the dialog appeared, the pointer id. Keeps what it ignored until that pointer lifts |
| A modifier node | `PointerInputModifierNode`, in the initial pass, as `touchesOnlyWhile`. Notes when it appears when it is attached in the dialog's own window, not when the caller composed |
| `MigrelyAlertDialog`, `MigrelyDatePickerDialog`, `MigrelyDialog` | Thin wrappers with the parameters this app uses. Each applies the modifier and the outside-tap rule |

A wrapper rather than something each dialog must remember to add, so a new dialog has the wait by using the only dialog available.

**A check that a dialog uses it.** Konsist is already in the project and runs in the gate. A new test in
`app/src/test/.../architecture`, with `Konsist.scopeFromProduction()`: outside the helper's file, no file's text names
`androidx.compose.ui.window.Dialog`, or `AlertDialog`, `BasicAlertDialog`, `DatePickerDialog`, `TimePickerDialog` or `ModalBottomSheet` from
`androidx.compose.material3`. So an import and a fully qualified call both fail it, and it names the file. A wildcard import would get past it,
and ktlint refuses one.

### D3. The length: 500 ms for all

From when the dialog appears. One number for all 10 dialogs.

**Where 500 comes from, plainly.** It is where QA's asks stopped, not where writes were seen to stop. Nothing above about 505 ms was
tested for dialogs. On the main window, writes went on to about 727 ms (#156), and D157 records stray gaps up to 649 ms. The dialog
appears after the first lift, and at about 90 to 100 ms it is usually not up yet. So a wait of 500 ms from appearing should cover gaps
up to roughly 600 ms from the first lift. That is an estimate. Task 7.2 measures where writes come back.

**Why not longer.** A longer wait eats more deliberate taps with no feedback, on dialogs people use often, such as the effect question
during an attack. That is the shape Google rejected. It is also why D157's 700 ms is not reused: that one covers two controls on a form,
and the form's other controls stay free. Here every control in the dialog waits.

**Why not a longer one for Restore.** Restore is the one candidate, and it was considered: data loss ranks first, and a person uses it
rarely. It is not taken. Reach is already narrow (landscape, and an automatic copy exists), the app saves a copy before it replaces, and
a second number needs a parameter that invites more of them. If task 7.2 or the hand check shows Restore writing from a natural double
tap past the wait, that goes back to `team-lead` before Production, and a longer wait for Restore is the first thing to look at.

### D4. Edge cases

| # | Case | Decision | Why |
|---|---|---|---|
| 1 | A second tap outside the card | **Held off for the wait.** The dialog is opened with `dismissOnClickOutside = false`, restored when the wait ends | A dialog that opens and shuts looks like a control that did nothing, the class Google rejected. Measured: the date picker closed in 2 of 4 |
| 1a | A press outside that lifts after the wait | Does not close | `DialogWrapper.onTouchEvent` only closes if it saw the press go down while `dismissOnClickOutside` was on (`isPressOutside`). This needs no code of ours, only a test |
| 2 | Back inside the wait | **Closes at once** | It is not a finger on the glass, writes nothing, and a way out must never wait. `dismissOnBackPress` and `onDismissRequest` are not touched |
| 3 | Rotation with a dialog open | **The wait starts again** | The activity is recreated (no `configChanges`) and the window is new. Most open flags are `rememberSaveable`. Cost: one tap in 500 ms after a rotation, and a person is unlikely to be tapping then. D157 arms again the same way |
| 4 | Process death and restore | As rotation | No state of the wait is saved. Nothing to save |
| 5 | A dialog with no first tap | **The same wait, no exception** | The rule gives a person a moment to see the dialog, so it does not ask how the dialog came. It also covers a real path: a double tap on a file in the system picker lands on `ImportDialog`, whose second tap could run Replace (not measured, task 7.4) |
| 6 | A really fast person | **The press does nothing and the next works.** No ripple, text or sound | Any feedback in 500 ms would be a flash on the screen for someone in pain, and it adds a string. The cost is real: a known dialog tapped under 500 ms. Hand check, task 8.1 |
| 7 | A press that begins in the wait and lifts after | Ignored until it lifts | Otherwise holding a finger down would get past the wait |
| 8 | Text field | A short tap in the wait does not focus. No dialog requests focus by itself (`FocusRequester` is not used in any dialog), so no keyboard opens by itself. After the wait a tap focuses as before | Consumption reaches the tap. The wait reaches only the card, and a keyboard is a window of its own. Nothing requests focus in `CustomTagDialog`, so whether a keyboard is still open after a recreation is not known. Task 7.8 records it |
| 9 | A press held on a text field | Long-press selection can still start, with a haptic. The text does not change | The same residue as D159: `SelectionGestures.kt` reads `changedToDownIgnoreConsumed`. Accepted |
| 10 | Slider, clock dial, calendar days | Must write nothing. Task 3.2 records for each whether a consumed press still moves it | Some of Material's controls can read a press with `requireUnconsumed = false`. I have not checked each. A slider or dial moving writes nothing until a confirm button, so it is a look, not a loss |
| 11 | The main window after a dialog closed with no lift on its card | The press answers, because a touch lifted on that screen when it opened the dialog | D159's third condition. So the wait neither adds to nor takes from D159 |
| 12 | A double tap on a dialog's own button | **Measured, and it writes.** The first closes the dialog, and the second lands beneath | D5 |
| 13 | A return from Home or Recents to a dialog still open | **The wait does not start again.** The window was never gone, so nothing is re-attached, and no code handles this | The person has seen the dialog. A double tap on a Recents card landing on a dialog's button is residual risk. Task 7.8 measures it. A write would bring new code back to `team-lead` |

**Why the full-screen dialog keeps its properties.** It has no outside to hold off, so there is nothing to change. An earlier version of this
document said a change would undo the date range picker's `ADJUST_RESIZE`. That was wrong: with `decorFitsSystemWindows` true, `updateParameters` passes
mode 0, and `Window.setSoftInputMode` then leaves the attribute alone. A property change is not expected to redraw anything, and task 7.3 looks for that.

**TalkBack, switch access, keyboard.** A click action goes through semantics and reaches `onClick` with no pointer event, keyboards send key
events, and hover is not a press. The modifier adds no semantics. These are from reading the code, and are not run on a device until task
7.3. A service that injects gestures, such as switch access's point scan, is a real touch and the wait applies to it, which at 500 ms from
appearing should not be reached.

### D5. A lift in a dialog counts for what is beneath it (BRD D161, #159)

**The path.** A double tap on a dialog's own option. The first tap closes the dialog, and the second lands on the window beneath. D160 does not stop it,
because that is not the dialog's wait. D159 does not, because a dialog is another window, so it never hears the lift, and opening the dialog already left a
lift on the entry.

**The worst case in #159** is the OK of Log medication's "Taken at" picker over a Log button: a false intake, 6 of 6 in landscape with 4 medications, at 142 to 522 ms (`recount_t10c.py`). Beside it, on a build whose app code equals `develop`: the OK of Add past's picker over "Add migraine" in landscape creates a migraine, 4 of 4 at 138 to 516 ms ("Change when it started" 2 of 2, "Change when it ended" 2 of 2), and details opens on it. Single taps wrote nothing. #159 stays S2, because both are visible, and the one has Undo and the other can be deleted.

QA's run on 4bb7127a, debug, `t10b.jsonl`, `effect_row`, "Not at all" over "Right side", 48 double taps at one point, 100% and 200% portrait. By measured gap:

| Gap, first lift to second press | Wrote a false location |
|---|---|
| Under 100 ms | 0 / 5 |
| 100 to 300 ms | 22 / 24 |
| 300 to 500 ms | 8 / 8 |
| 500 ms and up | 11 / 11 |

The same file has Theme over "Track periods" at 100%: `period_tracking` flipped on all 3 double taps, at 146, 248 and 523 ms, and not on the single tap. That is
a setting changed with no message. Other runs, one repeat each: Theme in landscape opened Your data, and Restore in landscape reopened its dialog from about 250 ms.

**The rule, in plain words.** A press is the second tap of a double tap if a finger just lifted on a dialog's card in another window. Under 500 ms and within
the distance of that lift, it is ignored until it lifts. Everything else answers.

**Measured at 167b2e95,** `w161_final.jsonl`, debug, one injected finger:

| Row | Gap | Result |
|---|---|---|
| `effect_row` | 157-162 ms, 391-399 ms | tag 0 of 3 each |
| `effect_row` | 595-617 ms | the chip answers, tag 3 of 3 |
| Theme over "Track periods" | 238-248 ms, 389-398 ms | flip 0 of 3 each |
| "Taken at" OK over Log Deltacet, landscape, 4 medications | 239-244 ms, 395-398 ms | intake 0 of 3 each |
| The same | 692-709 ms | intake 3 of 3, "Deltacet logged at…" |

| Question | Decision | Why |
|---|---|---|
| The limit | **`DIALOG_WAIT_MILLIS`, 500 ms, in both nodes, read inside `DialogLiftRule`** (`team-lead`). D159's 300 ms stays | This plan first said 300, before the worst case was known. #159's worst case is a false intake, and the handbook's order puts wrong data feeding the overuse count above a lost gesture that costs one retry. D159 stays at 300 because a new screen's first tap is common and must answer, and that reason does not apply to a screen the person has just come back to. On `effect_row`, 30 of the 41 measured writes are under 500 ms, and 11 stay. The cost is under "What a person loses" |
| Lower limit | None | As D159 |
| Distance | **Yes, in screen coordinates,** with `scaledDoubleTapSlop`, 100 dp today | It is what makes this Android's own double tap, and a real double tap does not drift that far. Without it, every press in the window for 500 ms is lost. The two windows have different origins, so both nodes use `localToScreen`. A wrong conversion fails silently toward writing, so a test on `emulator-5570` proves it with a real offset (task 6a.5) and QA's table proves it again |
| D159's "no touch has lifted on this entry" | **Not asked on this path** | Opening the dialog left that lift, so asking would make the rule never act. D159's own path keeps the condition |
| Which lift counts | **Any lift on the card that the wait let through.** An option, OK, Cancel, or one that closes nothing | The rule cannot know what a lift will close, since some close after async work. It need not: while the dialog stays, the window beneath takes no touch, so a lift that closes nothing costs nothing. It only asks about another window, so two days in one calendar both count |
| A press the wait ignored | Does not count | Nothing ran. Otherwise a stray tap in the wait, then Back, would swallow the next tap |
| Cancel | Counts | The second tap lands beneath the same way |
| Back | Does not count | No finger lifted on the glass in the app's windows. `aControlUnderADialogClosedInItsWaitAnswers` stays true |
| A tap outside the card | **Does not count, and is residual** | It never reaches the card's node. `DialogWrapper` handles it, and calls the same `onDismissRequest` as Back, so a wrapper cannot tell them apart without taking Back over itself. Task 7.10 measures a double tap outside. A write comes back to `team-lead` |
| A dialog beneath a dialog | **The same rule** | The older dialog's node asks the same question, as well as its own wait. So OK or Cancel on the picker, and a press in the gap before the time step's window is up, are both covered. This costs one call |
| A burst | The lift of an ignored press moves the lift, and it stays a dialog's lift | Otherwise the third tap of a shaky triple tap writes, as in D159. No upper limit, as in D159 |
| A new screen from a dialog's button | Ignored the same way | The entry is new. D159 cannot see it either |
| A return from a dialog's button | Ignored the same way | The one exception to D159's "SHALL NOT cover a return", and it is narrow. Back and a control of the main window stay as they were, so #148 is untouched. From reading the code, no dialog's button pops today |
| Recreation | The lift is not kept | 500 ms does not outlast a recreation |
| A press's pointer | Ignored pointers are kept per window, and a dialog's key is an `Any()` for its node | A touch id means something only in its own window |

**How `designsystem` tells `app`.** `app` depends on `designsystem`, not the other way, and the older-dialog case needs the question asked on both sides. So one small
pure class goes in `designsystem`, in the dialog package, with a `staticCompositionLocalOf` whose default never ignores anything, so previews and feature tests
need nothing. `ScreenNavHost` provides the one instance. Composition locals reach a dialog's content, because a dialog's composition has its caller as parent. `SecondTapRule`
stays in `app` and gets one more input, and no class moves.

**Why not one rule for both.** Moving `SecondTapRule` into `designsystem` would widen #156's diff for no gain, and the entry mark belongs to Navigation.

**What was dropped.**

| Option | Why |
|---|---|
| A wait on the main window when a dialog closes | It would ignore every press, at any point, and it needs a dialog-closed signal that Back also raises |
| 300 ms, D159's limit | This plan's first choice, and built first. It left a false intake at 300 to 500 ms. See the limit above |
| No distance test | Simpler and it cannot fail silently, but it swallows a tap anywhere. Kept as the fallback if the coordinates prove unreliable: a change of one condition |
| Wrapping the window's `Window.Callback` to see every touch, outside included | It would see outside taps and raw coordinates, but it is a second, non-Compose way into touch |
| `dismissOnBackPress = false` with a `BackHandler`, to tell Back from outside | It takes over Back, which D160 promises is untouched, for a rare residue |
| A debounce on each write control | Same reasons as D159's D1: missed routes |

**The menu.** `DropdownMenu` on a custom tag is a popup that opens on a long press, not through the helper. Its Remove closes it and archives the tag, and the second tap of a double tap
lands beneath, where other chips lie. So far from reading the code only. Task 7.10 measures it, and the scope widens only if it writes.

**What a person loses.** All of it:

- A tap under 500 ms and within 100 dp of the option, beneath it, does nothing.
- A whole drag, hold or fling that starts there is dead until the finger lifts, with no time limit.
- The centre walks. Each ignored lift moves the time and the place, so tapping along a row of chips at under 500 ms per tap stays dead until a 500 ms pause. It can only start with a press inside the first 500 ms.
- Writes from 500 ms up stay, including a false intake with its Undo: 700 ms wrote on the engineer's walk.
- On a dialog opened from a dialog, a tap just after the wait can be ignored too, if a second tap was ignored and lifted just before it. Trace: a lift on Next at 1000, the time step attached at 1060, a second tap at 1280 lifted at 1320, and a deliberate tap at 1565 is ignored, 5 ms after the wait. A tap after a 500 ms pause answers.

None of the lost gestures writes. Whether they feel dead to a person is unproved until the hand check.

**What stays unproved.** A human hand, a real phone, and how often this happens. Whether a chip or switch lies under an option depends on scrolling, and QA's rows are one geometry for each, moved there on purpose.
Writes from 500 ms up are not fixed. Nothing here proves that a person's natural double tap on an option is under 500 ms.

**Back.** Back lifts nothing on a card, so this rule does not act. A tap straight after Back can still be lost to the dialog's window closing: 0 of 5 landed at 0 ms after Back's command, 2 of 5 at 100 ms, and 15 of 15 from 200 ms, with and without the rule alike.

## Edge-case checklist

| Area | Result |
|---|---|
| Empty, first use | The same wait |
| Loading, failure, full storage, offline | Not touched. A failure message is on the main window |
| Premium and Free | Not touched. No paywall is a dialog |
| During an attack | The effect question and the pain dialog wait 500 ms. Nothing in them is a safety control. Back is free. Hand check looks for a dead feel |
| Zone, clock, midnight, DST | Event uptime, so they do not apply. `AddPainDialog`'s own "now" is unchanged |
| Limits, unicode, duplicates, undo, deletion | Not touched |
| Backup and restore | The reason for the change. What Restore and Import do is not touched |
| Upgrade | No data, no schema. Nothing is saved |
| Rotation, process death | D4 rows 3 and 4 |
| Double tap | The whole point. One after the wait still runs (residual) |
| Unordered async | A dialog that opens late still waits from when it appears |
| 200% font, landscape | Table rows, and the Konsist test means every layout gets it |
| Right-to-left | No direction in the rule. English only (D148) |
| Reduced motion | No animation is involved |
| Purchase | Not touched |

## What the check will not prove

- **A human hand.** One injected finger at exactly the same point, on an emulator. A real double tap drifts and has its own rhythm.
- **How often it happens,** or how often a deliberate tap comes under 500 ms. Neither is measured, and nothing here should claim a rate.
- **A real phone.** A slow phone's window can take longer to show. The wait counts from attachment in that window, close to but not exactly when
  the person can see it.
- **That 500 ms is enough.** It is one choice. Task 7.2 shows where writes come back on an emulator, not what people do.
- **Dialogs and layouts not run.** The wait is on all 10 by construction, but only the rows in the test plan are measured.

## Test plan

| Level | What | Who |
|---|---|---|
| JVM | The pure rule at 0, 100, 499, 500, 501 and 800 ms. A press held across the end. Two pointers. A dialog not yet appeared | `android-engineer` |
| Compose, Robolectric if dialog windows work there, otherwise `androidTest` on `emulator-5570`; say which | Through each wrapper: a press inside the wait does nothing, a press after answers, one held across the end does nothing, a click action inside the wait runs, the tree and bounds match a raw dialog. One per kind of control: button, radio row, slider, dial, day, text field | `android-engineer` |
| Negative control | The modifier and the outside-tap rule removed in turn, locally: these tests must fail | `android-engineer` |
| Konsist | No raw dialog. A raw dialog added locally must fail it | `android-engineer` |
| Black box, debug and R8 | `t9_dialog.py`'s rows with and without the guard, at gaps both sides, a tap just after the wait, 100% and 200%, landscape | `qa-engineer` |
| A lift in a dialog | JVM and Compose tests of D5 with a negative control on each part, and a walk on 5570 | `android-engineer` |
| The paths not measured | Import after the picker, the rows #157 lists as not proved, the stacked picker, a double tap outside the card, the menu | `qa-engineer` |
| A double tap on a dialog's option | `t10b.py`'s rows with the rule and without it, debug and R8, read by measured gap | `qa-engineer` |
| Human hand | Real phone, internal track | `team-lead`, the owner |

**Reading the retest.** Run as before, `t9_dialog.py` used asks up to 500 ms, and a wait from appearing should cover those. So asks above that
are needed, and writes are expected to come back somewhere above 500 ms. That is the decision, not a failed fix. S2 closes when no row writes
inside the wait and a tap just after it answers. QA reads each trial by its measured gap and still judges the closure. The writes after the wait
go into the 1.1.0 Go/No-Go as residual risk.

## Risks

- **A deliberate tap is lost with no feedback,** on a dialog a person knows well. Google's rejection was about controls that do not answer. The
  hand check looks for this, and it goes back to `team-lead` if a person feels it.
- **#156 merges first and its spec says something this overtakes.** Its scenario "A dialog" says the second tap reaches the dialog and the path has
  not been measured. Once this merges, it needs one line. Task 6.3.
- **The wait in the helper rests on Material's `modifier` staying on the card's box.** A Material upgrade could move it. The Compose tests fail if a
  press stops being ignored.
- **Screen coordinates across windows** fail silently toward writing if they are wrong. D5.
- **This branch now builds on `feature/fix-double-tap-writes`,** which must merge to `develop` first. If #156 changes, this rule moves with it.
- **A tap outside the card, and the menu,** are not covered by D161. If QA measures a write, the scope goes back to `team-lead`.

## Migration

None. No data and no setting. To back out, make the wrappers call the raw dialogs.
