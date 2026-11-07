# Design

Plan only. Nothing here has been built or run. The measures come from issue #157 and `recount_dialog.py`, on an emulator with
injected taps. The Compose and Material facts are read from the sources of Compose UI 1.9.3 and Material 3 1.4.0.

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
opens on a long press, and it is not covered (#149). No `AlertDialog.Builder`, `DialogFragment` or bottom sheet is in the app.

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
`app/src/test/.../architecture`, with `Konsist.scopeFromProduction()`: outside the helper's file, no file imports
`androidx.compose.ui.window.Dialog`, or `AlertDialog`, `BasicAlertDialog`, `DatePickerDialog`, `TimePickerDialog` or `ModalBottomSheet` from
`androidx.compose.material3`. It is a real test: a raw dialog must fail it, and it names the file. It reads imports, so a fully
qualified call would get past it, and the gate's review has to catch that.

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
| 8 | Text field | A short tap in the wait does not focus. No dialog requests focus by itself (`FocusRequester` is not used in any dialog), so no keyboard opens by itself. After the wait a tap focuses as before | Consumption reaches the tap. An open keyboard is a window of its own, so typing goes on during a new wait after a rotation, and the wait reaches only the card |
| 9 | A press held on a text field | Long-press selection can still start, with a haptic. The text does not change | The same residue as D159: `SelectionGestures.kt` reads `changedToDownIgnoreConsumed`. Accepted |
| 10 | Slider, clock dial, calendar days | Must write nothing. Task 3.2 records for each whether a consumed press still moves it | Some of Material's controls can read a press with `requireUnconsumed = false`. I have not checked each. A slider or dial moving writes nothing until a confirm button, so it is a look, not a loss |
| 11 | The main window after a dialog | **The two rules stay separate.** Touches in a dialog's window never reach `SecondTapRule`. After a dialog closes, the press on the main window answers, because a touch lifted on that screen when it opened the dialog | D159's third condition. So the wait neither adds to nor takes from D159 |
| 12 | A double tap on a dialog's own button | **Neither rule reaches the second tap.** The first closes the dialog, and the second lands on the screen under it, where it could toggle a chip or open a dialog | Not measured. It is not assumed safe. Task 7.5 measures it, and a write is a new issue that could change the scope |

**How the outside-tap change can bite.** Changing `DialogProperties` makes the dialog call `updateParameters` once more, and that sets the
window's soft input mode. `MigrelyDateRangePicker` sets its own mode, `ADJUST_RESIZE`, in a `SideEffect`, because a pan once hid Done under
the status bar. So a change there would undo that. The date range picker is full screen and has no outside, so the helper leaves its
properties alone. Task 3.3 tests the rest. A property change is not expected to redraw anything, and task 7.3 looks for that.

**TalkBack, switch access, keyboard.** A click action goes through semantics and reaches `onClick` with no pointer event, keyboards send key
events, and hover is not a press. The modifier adds no semantics. These are from reading the code, and are not run on a device until task
7.3. A service that injects gestures, such as switch access's point scan, is a real touch and the wait applies to it, which at 500 ms from
appearing should not be reached.

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
| The paths not measured | Import after the picker, a double tap on a dialog's own button, the rows #157 lists as not proved | `qa-engineer` |
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
- **A fully qualified dialog call** gets past the Konsist test.

## Migration

None. No data and no setting. To back out, make the wrappers call the raw dialogs.
