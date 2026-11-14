# Design

The decisions are BRD D178. The code facts were read from `develop` 52c12ce5 and not run. The pattern comes from D165 and the
archived change `2026-11-09-fix-save-message` (its design D4 and D5).

**The UX source is `migrely-work/t63/ux-spec.md`** (ux-designer, 2026-11-14). Its §2 holds the engineer's rules and §3 the state
tables O1–O20, F-a–F-h, M1–M12, P1–P7, S1–S4 and R1–R5. `team-lead` accepted findings F1–F8 as written. F9 is out of scope. Where
this file and the ux spec differ, the ux spec's §2 and §3 win, and `product-manager` fixes this file.

## Context

- **Hosts.** 13 screens show messages through `MigrelySnackbarHost`.
  - **Top-bar screens, through a `messages()` slot:** Settings, Paywall, Doctor report, Your data, Reminders, History, My
    medications, Edit medication and Log medication.
  - **Onboarding:** a `messages()` slot and no top bar.
  - **Home:** `Box(messageArea) { messages() }`, with no top bar.
  - **Attack details and Add past migraine:** `AttackMessages` after the content.
  - **Order:** in every one the host is placed after the content. Top-bar screens are `Box { Column { TopAppBar; content };
    messages() }`.
- **Tab order.** Compose's one-dimensional focus search (Tab, Shift+Tab) follows the placement order of the children of the
  lowest common ancestor. The message is a sibling of the whole column that holds the top bar and the content, so it comes after
  both. This explains #185. Verify it with the red test in task 5.2, not from this note.
- **Fade-in.** `MigrelySnackbarHost` enters with `slideInVertically + fadeIn(tween(250))` from alpha 0 (`MigrelySnackbar.kt:353-357`).
  A replacement cross-fades unless `replaceInPlace` is set, and only Attack details sets it, in card mode (D165 (3)). Compose
  reports a node at alpha 0 as not visible to the user, so a polite live region added in that frame is announced with no text
  (QA TB12).
- **Messages with no duration.** "Storage full" has `baseDurationMillis = null` in the behaviour mappers of home, history,
  medication, reminders, report, backup and attack. Settings, onboarding and the paywall have only timed (8 s) errors.
- **Silent successes.** Writes that succeed and send nothing today:

  | Screen | Writes that succeed with no message | Writes with their own message |
  |---|---|---|
  | Home | `RecordIntensity.Recorded`, `MoveStart.Moved`, period today `Written`, `AnswerLongAttack.Saved` | start, end, undo start, end and delete |
  | History | `SetDayMark.Done` (period, pain-free) | `AttackLogged`, undo delete |
  | Reminders | daily reminder and time, effect reminder (`ReminderWriteResult.Done -> Unit`), visit date `Saved`, with or without a permission ask (F4) | visit removed (Undo), undo |
  | Doctor report | `Share` (the share sheet opens) | `NothingLogged` (information) |
  | Your data | none | backup, CSV, replace, add, restore |
  | My medications | none | archive (Undo), undo |
  | Log medication | none | logged (Undo), intake undone |
  | Edit medication | save navigates back (`OnLeave`) | |
  | Add past migraine | create and merge navigate away | |

  A result that writes nothing (`NotInProgress`, `Unchanged`, `NothingToRemove`, `AlreadyInProgress`) is not a success.
- **State.** Every screen keeps the shown message in `rememberSaveable`. `ScreenLeaveObserver` (`architecture/ui`) already gives
  the navigation-based leave that D165 (5) needs.
- **Review slot.** Home composes `paywallTrigger()` and `reviewPrompt()` when `mode == HomeMode.Today` (`HomeScreen.kt:300-303`).
  `ReviewPrompt` waits 2 s in a `LaunchedEffect` and calls `onLeave()` when it leaves the composition.
- **Short window.** On Home Today, `messageTopOffset` returns 0 in a short window, and `messagesBesideControls` is true only in
  attack mode (`HomeScreen.kt:191-200`, `314-321`). Today's short layout is a `Row` of two equal columns: the date and Settings
  row starts the start column, and "Migraine started" sits in the end column (`TodayContents.kt:84-111`).

## Goals and non-goals

**Goals:** the four issues are gone on every screen they can reach, through one mechanism per concern in `designsystem` and the
D165 pattern per screen.

**Non-goals:** keyboard and cursor behaviour (next change), timed errors, message text, D158's other overlaps, any data change.

## Decisions

### D1. An error goes on a later success, on 9 screens (D178 (1))

| Part | Decision |
|---|---|
| Signal | Home, History and Reminders ViewModels emit one "write saved" event (not a message) on each success in the table's middle column. On Reminders this includes a visit date `Saved` with `askForPermission = true`, emitted alongside the permission request (F4). Nothing on a result that writes nothing |
| Who decides | The screen. If an error shows (storage full or something went wrong), it replaces it with `Saved`. Otherwise it ignores the event. This mirrors `AttackDetailsScreen.kt:209` |
| "Saved." | A new `Saved` in `HomeMessageUiModel`, `HistoryMessageUiModel` and the reminders message model. Confirmation tone, polite, `READING_MILLIS` stretched by the accessibility timeout, body tap closes it, no haptic. The string is "Saved." in each module, the same as `attack_message_saved` |
| Doctor report | On `Share`, if an error shows, clear it with no message. The share sheet is the outcome, and the app saved nothing, so "Saved." would be false |
| Own-message screens | Your data, My medications and Log medication need no new signal, because "Newest message wins" already replaces the error. Tests prove each one |
| Screens that close | Edit medication and Add past migraine close on success. Their error is popped with the entry |
| Two quick successes | The first replaces the error. The second finds "Saved.", not an error, and stays silent |

**Why the screen decides:** the ViewModel is stateless (`docs/ARCHITECTURE.md`). D165 (4) set this pattern and it has shipped.

### D2. An error belongs to the visit (D178 (2))

Each of the 9 screens adds `ScreenLeaveObserver(navController) { if (errorShown()) shown = null }`, as Attack details does. It
reads the navigation state, not the lifecycle. The system file picker, the share sheet, Android Settings and Home are other
activities or the background, so nothing clears. Undo and information messages are untouched. The tests cover both sides, and
one of them fails with a lifecycle `ON_STOP` clear in its place (negative control N4).

### D3. One order mechanism for keyboard, Switch Access and TalkBack (D178 (4))

| Question | Decision |
|---|---|
| Order | The top bar's controls, then the message's text and its control, then the content. On Home (both modes, every orientation) and onboarding the message comes first (ux Q2). Text and control stay two nodes, never merged |
| Where | One composable or modifier in `designsystem/snackbar`, used by all 13 screens. No per-screen focus requesters |
| Recommended shape | A layout with three slots, `topBar`, `message` and `content`. It places them in that order, so Tab follows it. It draws `message` above `content` with `zIndex` and measures it over the whole screen as today. The layout is a traversal group and gives the slots `traversalIndex` in the same order for TalkBack. Home and onboarding leave `topBar` empty. Attack details lifts its top bar out of `AttackDetailsContents`, and the paywall's Close row goes into `topBar` (ux §2.1). The engineer may choose another mechanism if tasks 5.2 and 5.3 pass |
| Geometry | The message's bounds stay unchanged (±1 px at 100% and 200%), and so do `onMessageBottomChanged`, `topOffset` and D165's card mode. Exceptions: Home Today's measured offset and the fit rule (D6, ux P3–P6) |
| Guard after a replacement | The 0.8 s `TapGuard` covers the taller of the old and the new message, not only the new one. Today `guardHeight` follows the new message at once, so at 200% a shorter "Saved." would expose the strip where the error's Dismiss sat (F2) |
| Focus on arrival | None. TalkBack hears the message through its live region, as today |
| Focus on leave | Focus clears when the focused message leaves **and** when it is replaced; it is never carried to the new message's control and never moved into the content. The next Tab goes to the screen's first stop, Shift+Tab to the last. From the first frame of its exit fade a leaving message is neither a Tab stop nor in the semantics tree (Q3, F8) |

**Rejected options:**
- Per-screen `focusProperties` chains: 13 copies.
- Moving the host into the column: it would push the content, and the content would lose D158's overlay rules.
- Esc to dismiss: a new interaction, out of scope.

### D4. Never announced empty (D178 (5))

| Question | Decision |
|---|---|
| Replacement | In place on every host. The new message takes the old one's slot within one frame, with no cross-fade. This is D165's `replaceInPlace` path, so it becomes the only behaviour. The parameter can go once Attack details is green |
| Arrival when none shows | It still slides in over 250 ms, but no frame holds a fully transparent node in the accessibility tree: `fadeIn(initialAlpha = 0.02f)` (allowed 0.01–0.05, never 0), slide and easing unchanged (ux Q4). Setting the live region when the enter transition settles is the fallback, used only if A16 still hears an empty announcement |
| Size | The message itself swaps in one frame. When the new message is **shorter**, the bottom the host reports eases up over 250 ms (the arrival tween), so content tied to it (D52 landscape attack mode, D165 card mode) moves up smoothly. When it is **taller**, the bottom moves at once, so the message never covers the safety card's title or the guidance for a frame. Only non-null to non-null changes ease. Reduce motion: at once (F1) |
| Same-text retries | Every posted message has its own identity (a posting time, as `ShownAttackMessageUiModel` has), so a swap to an equal-looking message is a new node and is spoken again (ux §2.8) |
| Proof without a field repro | The red test shows the transparent frame on `develop`. QA tries to reproduce #183 on one more screen as a control and records the result either way |

`ReplaceInPlaceTest`'s case "By default the new message fades in over the old one" inverts, because the spec changes. That is
a deliberate change to the expected behaviour, not a test bent to pass. Record it in the PR.

### D5. Review request (D178 (3))

Ux F3, accepted by `team-lead`. Two gates on Home's review slot:

1. **Any message.** `HomeMessages` reports whether any message shows (error, `Saved`, Undo or information), through a callback
   like `onMessageBottomChanged`. While one shows, Home does not compose `reviewPrompt()`.
2. **Error this visit.** Home keeps a `rememberSaveable` flag, set when an error is posted and cleared by the same
   `ScreenLeaveObserver` that ends the visit (D2). While it is set, Home does not compose `reviewPrompt()`.

Home composes the slot only when `mode == Today` and both gates are open. When the slot leaves the composition, the existing
`ReviewPrompt` cancels its wait and calls `onLeave()`, so a flow being prepared is not launched and nothing counts. When it
returns, the 2 s start again. The next visit or app start checks D130 as usual. The review module itself does not change.

### D6. Home Today in a short window (D178 (6))

| Question | Decision |
|---|---|
| Width | The start column, through the rule landscape attack mode uses (`GUIDANCE_COLUMN_WIDTH_FRACTION`, `messagesBesideControls` true for Today in a short window) |
| Top | `topOffset = max(MESSAGE_OFFSET_BELOW_TOP_BAR, settingsRowHeight + 2.dp)`: 10 dp below the row, because the host adds 8 dp. Measure the row's **height** (`onSizeChanged`), never its position, so the message never follows scrolling. Until the row is measured, use the fixed offset (ux Q6, F5) |
| Portrait | The same measured offset in portrait Home Today. It changes nothing while the date fits on one line, and stops a wrapped 200% date on a 360 dp phone from being covered (F6) |
| Fit rule | In the host, for every screen: the message is always whole inside the safe area. If its bottom would pass the bottom safe edge, it rises just enough, never above where it sits today (safe top + 8 dp). It does nothing where the message already fits. At 274 dp and 200% a no-duration error then may cover Settings, in that cell only, which is better than an unreachable Dismiss. "Migraine started" is never covered (F5) |
| Covered | The first row under Settings, the trial banner or today's card, may lie under it (D158's first-row rule) |
| Unchanged | Attack mode in either orientation, and every other screen's placement (the fit rule is a no-op there) |

### D7. Merge order

The next change, `fix-keyboard-follows-cursor`, also edits `AttackDetailsScreen.kt`. This change merges first, and that one
branches from the `develop` that contains it.

## Questions for ux-designer (answered 2026-11-14)

The ux answers are in bold. They are in `migrely-work/t63/ux-spec.md` §1, and `team-lead` accepted F1–F8.

| # | Question | PM default |
|---|---|---|
| Q1 | Order on top-bar screens: are the bar's actions before the message (History: Back, Insights, Doctor report, message, content)? | Yes, the whole bar first. **Ux: yes** (F7 fixed the History example) |
| Q2 | Home and onboarding have no top bar. Is the message the first stop even in portrait Home Today, where it sits below the Settings row? | Yes. **Ux: yes**, in every Home mode and orientation |
| Q3 | Where does focus go when the focused message leaves? | Focus cleared. **Ux: yes**, plus F8: a replacement also clears focus, and a leaving message is not a stop |
| Q4 | Arrival fade: what start alpha keeps the look the same while never being 0? Or defer the live region instead? | **Ux: 0.02** (range 0.01–0.05). Deferring the live region is the fallback only |
| Q5 | A replacement switches at once on every screen, with no cross-fade, and content tied to the message's bottom moves at once. Acceptable? | **Ux: partly** (F1, F2). In place, yes; a shorter replacement eases the bottom over 250 ms, a taller one moves at once; the guard covers the taller message |
| Q6 | Short-window Home Today: the start-column width, and is the fixed offset enough at 200% when the date wraps in half the width? | **Ux: not enough.** The start column, the measured row height with 56 dp as the floor, a fit rule, and the same offset in portrait (F5, F6) |
| Q7 | "Saved." per screen: the same text, icon, tone, 8 s and no haptic as Attack details, on Home (both modes), History and Reminders. A silent clear on Doctor report? | **Ux: yes**, plus F4: Reminders' visit date with a permission request also says "Saved." |
| Q8 | Review: after an error or "Saved." leaves, start D130's 2 s again in the same visit, rather than waiting for the next visit? | **Ux: no** (F3). Wait while any message shows; after an error, no request for the rest of the Home visit |

## Edge cases

| # | Case | Decision |
|---|---|---|
| 1 | Error, then a later silent success (Home, History, Reminders) | "Saved." replaces it |
| 2 | Error, then a success with its own message | That message replaces it |
| 3 | Error, then a report created | The error clears with no message, and the share sheet opens |
| 4 | Error, then a result that wrote nothing | Nothing changes |
| 5 | Error, then a failed retry | The error again, in place, spoken again |
| 6 | Two quick successes | One "Saved." |
| 7 | A late success event after leaving | The screen is not current, so nothing shows. On return the error is already cleared |
| 8 | Leaving by Back or forward | The error clears. Undo and information stay |
| 9 | File picker, share sheet, Android Settings, Home, Recents, screen off | The error stays |
| 10 | Rotation, process death | The error stays with no duration. Saved instance state brings it back |
| 11 | Error on Home Today when the review is due | No request for the rest of that Home visit. Any other message: no request while it shows, 2 s after it leaves |
| 12 | Attack mode | "Saved." is dim (Dark room), and nothing new is bright. Attack-mode controls stay uncovered (D52) |
| 13 | Free and Premium | Identical |
| 14 | Offline | Nothing here uses the network |
| 15 | Full storage | "Saved." needs no write of its own |
| 16 | 200% font, landscape | "Saved." is visible whole. The order is unchanged. The action-below-text layout keeps text before the control |
| 17 | Right to left | Order is logical, not visual. The short-window start column is on the right |
| 18 | Reduce motion | Arrival, replacement and leave happen at once |
| 19 | TalkBack | The message is read after the bar, never empty, and does not take focus |
| 20 | Hardware keyboard, Switch Access | The message's control is reachable forward and backward, with a separate highlight |
| 21 | A message that leaves or is replaced while focused | Focus clears, never moves into the content, the next Tab goes to the first stop, no crash. A leaving message is never a stop |
| 22 | Clock or zone change, DST, midnight | Not touched. `shownAt` uses the wall clock as today |
| 23 | Upgrade from 1.2.x | No stored data. Saved instance state does not survive an update |
| 24 | Double tap on a write | Each write is its own result: rule 6 |
| 25 | Short window, 274 dp, 200% | An Undo covers part of the start column only. A no-duration error rises to stay whole and may cover Settings in that cell only. "Migraine started" is never covered |
| 26 | A shorter replacement at 200% (error with Dismiss below → "Saved.") | A tap where the Dismiss was, within 0.8 s, reaches nothing |
| 27 | Reminders visit date that asks for permission after an error | The error goes; "Saved." may sit behind the system dialog or time out there |
| 28 | Portrait Home Today, 360 dp, 200%, a date that wraps | The message sits below the second date line, and Settings is fully uncovered |

## Acceptance (QA, this diff only)

Setup: API 36, 1080x2400, a debug build of this branch's head; `/data` filled under 64 KB with `fallocate` where an error is
needed; a hardware keyboard; TalkBack with verbose `action=SPEAK` logging; uiautomator bounds. Run at 100% and 200% in portrait
unless a check says otherwise. Restore font scale, rotation, animator scale, the accessibility timeout, storage and locale
afterwards.

| # | Check |
|---|---|
| A1 | Home Today: fail a period mark, free space, mark again. "Saved." within 1 s, spoken, the mark stored. 5 of 5 |
| A2 | Home attack mode: fail a pain level, free space, record another. "Saved." is dim and the controls are uncovered |
| A3 | History day mark, Reminders daily toggle and visit date: the same as A1 |
| A4 | Doctor report: fail, free space, create. The share sheet opens, the error is gone, and no "Saved." |
| A5 | Your data export, My medications archive and Log medication log, each after an error: the own message replaces it |
| A6 | Edit medication and Add past migraine: fail, free space, save. The screen closes, and no error shows where it returns |
| A7 | Fail, keep the disk full, retry on Home, History and Reminders: the error stays and is spoken again with text |
| A8 | Fail, go forward and back (Home to Settings, History to a migraine): no error. Fail, Home, Android Settings, return: the error stays. Your data file picker and Report share sheet: the error stays |
| A9 | Fail, rotate. Fail, `am kill` in the background, reopen: the error stays |
| A10 | A normal save with no error: no "Saved." |
| A11 | Review, ux R1–R5: a debug setup where the request is due (D130 met). With an error on Home Today, no request in 10 s; dismiss it and stay, no request for the rest of the visit; go to Settings and back, the request may run 2 s after Today shows. With "Migraine ended [Undo]", no request until it leaves, then after 2 s |
| A12 | Tab order, ux O1–O20 and F-a–F-h: bar, message, content on every screen (History: Back, Insights, Doctor report, message control, month). Shift+Tab reverses. 25 presses recorded per screen. Focus clears on leave and on replacement, and Tab never lands on a leaving message |
| A13 | TalkBack swipe order on the same screens: bar, message text, its control, content |
| A14 | Switch Access on Attack details, History and Home: separate highlights in the same order, and selecting each works |
| A15 | Message bounds on every screen at 100% and 200%: equal to `develop` 52c12ce5 within 1 px, except Home Today's measured offset and the fit rule (ux P7). The 0.8 s guard holds |
| A16 | #183: on History, Reminders and Home, a message after more than 100 ms of quiet, and a same-text retry. No `ttsOutput={}` with a live region, 10 of 10 per screen |
| A17 | Replacements, ux M2–M9 and M12: no frame shows two messages (60 fps capture), no frame is visibly brighter than its neighbours; a shorter replacement eases the tied content over 250 ms, a taller one moves it at once; the guard covers the taller message |
| A18 | Home Today placement, ux P1–P6: 411 and 274 dp, 100%, 130% and 200%, LTR and `ar-XB`; Settings and "Migraine started" uncovered, answer the first tap, stay in the tree while Undo shows; at 274 dp 200% a storage-full error is whole on screen with Dismiss reachable; portrait 360 dp 200% with a wrapped date |
| A20 | "Saved." states, ux S1–S4, including the Reminders visit date with a permission request (F4) |
| A19 | "Remove animations": everything happens at once |

**Negative controls.** Each one shows that its check can fail.

| # | Build | Run | Must show |
|---|---|---|---|
| N1 | `develop` 52c12ce5 | A1 on Home | "Couldn't save" stays after the successful save (#181) |
| N2 | `develop` | A12 on Attack details | Dismiss is not among the forward stops (#185) |
| N3 | `develop` | A18 at 411 dp, 100% | Settings is under the message and does not open at the first tap (#88) |
| N4 | This branch with the leave clear swapped for a lifecycle `ON_STOP` clear, local | A8's Android Settings path | The error is gone after returning, so A8 can tell the two apart |
| N5 | This branch | A10 | No "Saved.", the control for D1 |
| N6 | `develop` | A16 on one screen other than Attack details | Record the result whether or not an empty announcement appears. It is a control attempt, not a pass condition |
| N7 | `develop` | A11 | The request starts while the error shows. This shows A11 can fail |
| N8 | This branch with the guard following only the new message, local | Ux M3 | The tap at the old Dismiss spot reaches the row underneath, so A17's guard check can fail |

## What the checks will not prove

- A human hand, a real phone, and TalkBack's queueing on other Android versions. These are emulator runs with injected input.
- That #183 happens in the field on other screens. The red test proves the transparent frame, not a user hearing silence.
- Window heights other than 411 and 274 dp, and fonts other than 100%, 130% and 200%.
- How a replacement with no cross-fade feels to a person in pain. It needs eyes on a screen.

## Risks

- **Restructuring 13 layouts for the order** could move a message, break insets, or change D165's card mode. Mitigations: A15
  compares bounds with `develop`, the existing `MessagePlaceAndTapTest` and `MessageClearanceLayoutTest` stay green, and D165's
  tests stay green.
- **The placement-order claim** about Compose's Tab search could be wrong. Task 5.2's red test shows it before any screen moves.
  If it is wrong, the engineer uses the fallback in D3 and records why.
- **An alpha above 0 might not be enough** if TalkBack checks other visibility signals. The live-region fallback is in D4, and
  A16 measures it.
- **A leave clear that fires on Home or the file picker** would break #167's main path. N4 and A8 test both sides.
- **"Saved." on Home in attack mode** is new text during an attack. It is dim, polite, gives no haptic and replaces an error the
  person already saw. Ux Q7 confirmed it.
- **Ux heights are estimates** (row heights, the wrap, the 274 dp overflow). P1–P6 decide them on a device.
- **The fit rule is new host behaviour on every screen.** It must be a no-op wherever the message already fits; A15 shows that.
- **Scope L.** If it does not fit one reviewable PR, split it along D1+D2+D5 (#181) and D3+D4+D6 (#185, #183, #88). Keep D178 and
  renumber nothing.

## Migration

None. No data, no setting, no permission. To back out, revert the branch.
