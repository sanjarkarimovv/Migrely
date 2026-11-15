# Design

The decisions are BRD D182. product-manager read the code facts from `develop` 40536f49. Nothing was run.

**The UX source is `migrely-work/t66/ux-spec.md` §B** (ux-designer, 2026-11-15):
- B1: which timeout each message follows;
- B2: the pause and the Undo ceiling;
- B3: 64 dp in the dark room;
- §D: what UX could not judge without a device.

product-manager accepts B1–B3 as written. Where this file and §B differ, §B wins, and product-manager fixes this file.

## Context

- **Hosts.** 13 screens show messages through `MigrelySnackbarHost`. The 10 files that call it are:
  - `HomeMessages`, `HistoryMessages`, `AttackMessages` (Attack details, Add past migraine);
  - `MedicationMessages` (My medications, Edit medication, Log medication);
  - `RemindersScreen`, `ReportScreen`, `YourDataScreen`, `SettingsScreen`, `OnboardingScreen`, `PaywallScreen`.
- **Timeouts today.**
  - `messageTimeoutMillis()` (`designsystem/snackbar/MessageTimeout.kt`) passes `containsControls = true`.
  - Four hosts compute their own, with `containsControls` tied to Undo:
    - `HomeMessages.kt:85` (`undoTarget != null`);
    - `HistoryMessages.kt:69` (`undoesDeletion`);
    - `AttackMessages.kt:62` (`hasUndo`);
    - `MedicationMessages.kt:61` (`undoesArchive`).
  - So on those screens "Saved." and information messages follow the non-interactive timeout.
  - In `MedicationMessages`, any Undo other than an archive (the logged intake's Undo on Log medication) follows it too.
    The engineer confirms this in the red test of task 4.1.
- **Countdown today.**
  - Each host keeps `Shown<X>MessageUiModel(message, shownAtEpochMillis)` in `rememberSaveable`. It runs
    `LaunchedEffect(current, timeout) { delay(current.remainingMillis(System.currentTimeMillis(), timeout)); shown = null }`.
  - Nothing restarts that effect on resume or on a focus change.
  - The `delay` runs on the composition's dispatcher. Whether it counts deep sleep is not proven. Do not rely on it either
    way.
- **Time-left line.** `MigrelySnackbar(timeLeft = { fraction })` draws it. Home draws it only for an Undo, and only with
  reduce motion off.
- **Focus.** `MessageFocus` in `MessageHostParts.kt` already tracks `inside = hasFocus` on the message group, and clears
  focus when the focused message leaves or is replaced (D178, T63 F8).
- **Sizes.**
  - `MigrelySnackbar.kt:101` sets `MIN_HEIGHT = 52.dp`.
  - `SnackbarEndControl` gives Undo `heightIn(min = 48.dp)` and Dismiss a default `IconButton` (48 dp) with a 24 dp icon.
- **Dark room.**
  - `MigrelyDarkRoom(active)` (`designsystem/theme/DarkRoom.kt`) provides the dark theme and counts `LocalDarkScreens`.
    There is no local that says "inside an active dark room".
  - Home wraps its message host in `MigrelyDarkRoom(active = mode == HomeMode.Attack)` (`HomeScreen.kt:341`).
  - Log medication wraps the whole screen, host included, in `MigrelyDarkRoom(active = darkRoom)`
    (`LogMedicationScreen.kt:289`).
  - `DeleteAllDataScreen.kt:151` makes the screen a dark room while deleting.
- **Deletions.**
  - `DELETION_OFFER_MAX_AGE_MILLIS = 30_000` (`DeletionOffer.kt:4`). `isFreshDeletion` is used at `HomeMessages.kt:104` and
    `HistoryMessages.kt:84`.
  - `DELETED_RETENTION = Duration.ofMinutes(2)` is `internal` in `AttackDataSourceImpl.kt:33`.
  - `recentlyDeleted` purges first (lines 249–250). Home's and History's `onResume` call it (`HomeViewModel.kt:75`,
    `HistoryViewModel.kt:44`).
  - `AttackDao.restoreIfPossible` (lines 371–378) reads `deletedById` and does not check age. If the row was purged it
    returns false, and the host shows "This can no longer be undone.".

## Goals and non-goals

**Goals:**
- one timeout rule;
- one countdown mechanism with pause, floor and ceiling, shared by every host;
- an Undo that never outlives its data;
- 64 dp message controls in any active dark room.

**Non-goals:**
- data rules (D23, D31);
- base durations;
- text, placement and Tab order;
- the keyboard (D180) and T66 (D181) work.

## Decisions

### D1. One timeout function (D182 (1))

- Every host calls `messageTimeoutMillis(baseDurationMillis)`.
- The four local `timeoutMillis` helpers are deleted.
- A Konsist rule beside `MessageCallSitesTest` fails on any `calculateRecommendedTimeoutMillis` call outside
  `MessageTimeout.kt`, so the split cannot come back.
- `messageTimeoutMillis` keeps its signature. Its KDoc drops "without Undo".

### D2. A countdown holder in `designsystem/snackbar` (D182 (2), (3))

- **`MessageCountdown`** is a plain Kotlin state machine with no Compose types, so it can be unit-tested on the JVM.
  - **Inputs:**
    - `timeoutMillis`;
    - the clocks;
    - `ceiling: UndoCeiling?`, with `actionWallMillis` and `shownElapsedMillis` for an Undo, null otherwise;
    - focus events: `focused(true/false)`, `windowFocused(true/false)`, `screenReaderOn(true/false)`.
  - **Outputs:**
    - `remainingMillis(now)`;
    - `paused`;
    - `expired(now)`;
    - `timeLeftFraction(now)`.
- **Clocks.** `MessageClock { wallMillis(); elapsedRealtimeMillis() }` comes through a `LocalMessageClock`. The default
  is `System.currentTimeMillis()` and `SystemClock.elapsedRealtime()`. Tests provide a fake.
  - The countdown runs on `elapsedRealtime`, which counts sleep and cannot be set by the user.
  - The ceiling's first bound uses the wall clock, the same clock `deletedAt`, the purge and `isFreshDeletion` use.
- **What is saved.** The host's `rememberSaveable` state becomes:
  - the message;
  - `runMillis`: running time already shown;
  - `wasPaused`;
  - `shownElapsedMillis` and `shownWallMillis`;
  - `actionWallMillis`.
  - If, after a restore, `elapsedRealtime` reads below `shownElapsedMillis`, the phone rebooted. An Undo is then treated as
    past its ceiling. Any other message keeps its `runMillis`.
- **Pause.**
  - `paused = (keyboardFocus || screenReaderFocus) && windowFocused`.
  - When focus is lost, a 300 ms grace starts. Focus that returns within it cancels the resume. The grace does not count
    as shown time.
  - Window focus loss (`LocalWindowInfo.current.isWindowFocused == false`) ends a pause at once, with no grace.
- **Resume.** `remaining = max(remaining, 3_000)`, then capped by the ceiling.
- **Ceiling.**
  - `UNDO_CEILING_MILLIS = 115_000`, in `DeletionOffer.kt` next to `DELETION_OFFER_MAX_AGE_MILLIS`.
  - It ends at the earlier of `actionWallMillis + UNDO_CEILING_MILLIS` on the wall clock and
    `shownElapsedMillis + UNDO_CEILING_MILLIS` on `elapsedRealtime`.
  - It applies whether the message is paused or not, and the floor never passes it.
  - A wall clock set back past the action is caught by the second bound. One set forward ends the Undo early, which matches
    the purge.
- **Driver.** A composable helper, `rememberMessageCountdown(...)`, replaces each host's `LaunchedEffect(delay)`.
  - Its loop sleeps until `min(remaining, ceilingLeft)` or until a state change wakes it.
  - It re-evaluates on `LifecycleResumeEffect` and on `isWindowFocused` becoming true. This is the lock and unlock path,
    and it does not depend on what `delay` counts.
  - At expiry the host sets `shown = null`, exactly as on timeout today, so `MessageFocus` clears focus (D178).
- **Action time.**
  - The deletion messages (`HomeMessageUiModel.AttackDeleted`, `HistoryMessageUiModel.AttackDeleted`) already carry
    `deletedAtEpochMillis`. Their hosts pass it as `actionWallMillis`.
  - Every other Undo passes the moment it was shown.
- **Ties to D23.**
  - `DELETED_RETENTION` stays where it is. Its value moves to a public `DELETED_ATTACK_RETENTION_MILLIS` in
    `datasource/source`, and `AttackDataSourceImpl` derives from it.
  - A JVM test in `:app` (`app/src/test`), which sees both modules, asserts
    `UNDO_CEILING_MILLIS == DELETED_ATTACK_RETENTION_MILLIS - 5_000`.
  - The engineer may choose another place, as long as one test sees both values and fails when they drift.

### D3. Where the pause signals come from (D182 (2))

- **Keyboard.** `MessageFocus` already has `onFocusChanged { inside = it.hasFocus }` on the live message. It feeds
  `focused(...)` into the countdown. No new focus node is added, so the Tab order (D178, D181) is untouched.
- **Window.** `LocalWindowInfo.current.isWindowFocused`.
- **TalkBack.** No public Compose API is known. Spike first (task 3.1).
  - One candidate: a `ViewGroup.onRequestSendAccessibilityEvent` on the ComposeView's parent, catching
    `TYPE_VIEW_ACCESSIBILITY_FOCUSED` and `..._CLEARED` for the message's virtual node ids.
  - Another: a semantics-based probe.
  - "TalkBack off" comes from `AccessibilityManager` touch-exploration state.
  - **Pass bar:** on API 26 and API 36, ten focus moves onto and off the text and the control each give the right state,
    10 of 10.
  - **On failure:** the pre-agreed fallback is keyboard only.
    - The engineer reports the result to product-manager.
    - product-manager removes the TalkBack requirement from the delta and adds the known limit to D182 and EC-UX-54, before
      the code lands.
    - The ceiling and the interactive timeout still bound every message.
- **Switch Access.** It is not required. If its highlight shows up as accessibility focus, it pauses too.

### D4. Dark-room size (D182 (4))

- `MigrelyDarkRoom` provides `LocalInDarkRoom`, which is `active || LocalInDarkRoom.current`, so an inactive nested room
  does not reset it.
- `MigrelySnackbar` reads it:
  - `MIN_HEIGHT` becomes 64 dp in a dark room;
  - Undo gets `sizeIn(minWidth = 64.dp, minHeight = 64.dp)`;
  - Dismiss gets `Modifier.size(64.dp)` with the icon at 24 dp.
- The label style does not change. The inline-or-below measurement in `MessageLayout` uses the measured control width, so
  the rule holds without new constants.
- The tap guard already follows the measured height (D178 F2).
- D52's landscape column already moves by `messageBottom`.
- Delete all data's dark room gets the size too if a message ever shows there while deleting. That is consistent, not a
  goal.

### D5. Order with the open branches (D182 (6))

- `bugfix/141-keyboard-follows-cursor` (D180) adds "A message ignores the keyboard". It edits `MigrelySnackbar.kt`
  and `MessageLayout`'s insets.
- `bugfix/217-privacy-wording-and-focus` (D181) adds "Home Today's message order holds with nothing after Settings". It
  edits `SnackbarEndControl`'s semantics (the Undo's single spoken name).
- Neither touches durations, pause or size. Their requirements and this delta do not contradict each other.
- The code will conflict in `MigrelySnackbar.kt` (`SnackbarEndControl`). Task 0.1 merges `develop` after both land and keeps:
  - D181's semantics;
  - D180's no-IME rule;
  - this change's size.
- BRD rows will conflict textually next to EC-UX-38 and §13 row 47. Keep every row, in numeric order.
- If two branches take one number, the second to merge renumbers.

## Questions for ux-designer

None block the plan. The §D items become device-walk checks (task 7):
- the feel of the 3 s floor and the 300 ms grace;
- whether 64 dp feels heavy over the rain card at 200%;
- whether stock Android writes only the interactive timeout.

If the walk says a number is wrong, ux-designer proposes a new one, and product-manager updates D182 and the delta.

## Edge cases

| Area | Case | Rule | Scenario |
|---|---|---|---|
| EC-UX-52 | "Saved." on Home with a 20 s interactive and an 8 s non-interactive timeout | 20 s | Message durations by kind: The same rule on every screen |
| EC-UX-52 | Setting changed while shown | new value from the moment it appeared | The setting changes while a message shows |
| EC-UX-52 | Attack mode "Saved." | about 30 s, dim, one tap closes it | Saved on Home in attack mode |
| EC-UX-53 | Keyboard on Undo | pause, then the time left | Keyboard holds Undo |
| EC-UX-53 | 0.5 s left on leave | 3 s floor | Focus leaves with little time left |
| EC-UX-53 | Reduce motion | no line, the pause still works | Undo window: Reduce motion; The line stops while paused |
| EC-UX-53 | 200%, landscape, RTL | the same pause | Large font, landscape and right-to-left |
| EC-UX-54 | TalkBack text, then control | no resume between them | From the text to the control |
| EC-UX-54 | API 26 and 36 | both | Both Android versions |
| EC-UX-55 | Shade, other app, dialog, screen off | runs at once | The window loses focus |
| EC-UX-55 | Touch, Back | focus left | A touch ends keyboard mode |
| EC-UX-55 | TalkBack turned off | runs | TalkBack is turned off |
| EC-UX-56 | Replacement while paused | full, unpaused time | A new message replaces a paused one |
| EC-UX-56 | Undo while paused | "Undone." full time | Undo pressed while paused |
| EC-UX-56 | Storage full | unchanged | Storage full has nothing to pause |
| EC-UX-57 | Rotation while paused | no focus, at least 3 s | Rotate while paused |
| EC-UX-57 | Process death while paused | no focus, at least 3 s | Process death while paused |
| EC-UX-57 | Restore past the ceiling | no Undo | Restored after the ceiling |
| EC-UX-58 | Held Undo | 115 s after the action | A held deletion Undo; TalkBack holds an Undo; Other Undo messages |
| EC-UX-58 | Clock back or forward | elapsed bound or early end | The clock is set back during a pause; The clock is set forward |
| EC-UX-58 | D23 changes | test fails | The ceiling follows D23 |
| EC-ATK-24 | Lock and unlock at 122 s, Home and History | no Undo, no "can no longer" | Lock and unlock at 122 seconds on Home, on History |
| EC-ATK-24 | Late offer | 115 s, not 145 s | A deletion offered late |
| EC-ATK-24 | Positive control | restores at 114 s | Undo before the ceiling still restores |
| EC-UX-59 | Portrait and landscape attack mode, 100% and 200% | 64 dp | Portrait attack mode; Landscape attack mode; attack-mode: Undo the start in the dark |
| EC-UX-59 | Log medication dark room | Dismiss 64 dp | Log medication in the dark room |
| EC-UX-59 | Outside the dark room | 48 dp | Outside the dark room |
| EC-UX-59 | Room changes under a message | the new room's size | The room changes under a message |
| EC-UX-59 | TalkBack | nothing new spoken | Nothing new is spoken |

**Checklist items with no change:**
- *Data:* nothing stored changes, so there is no effect on backup, restore or upgrade from 1.0.x to 1.2.1.
- *Purchase, Premium, Free:* the same for all.
- *Offline:* always works.
- *Full storage:* covered by "storage full" being unchanged.
- *Zone change and DST:* no effect, because every stamp is epoch milliseconds.
- *Midnight:* no effect.
- *Double tap on Undo:* the existing `shown == visible` guard stays.
- *Unordered async reads:* the countdown is UI state only.
- *Font, landscape, RTL, TalkBack, keyboard, reduced motion:* scenarios above.

## Acceptance (QA, this diff only)

- **A1 (B1):**
  - device with "Time to take action" at 30 s;
  - "Saved." on Home stays about 30 s, and so do History's and Reminders';
  - negative control: Home's leaves at about 8 s on `develop` 40536f49.
- **A2 (B2, keyboard):** P1, P2, P5 and P6 from ux §B2, on API 26 and API 36, on Home attack mode portrait 200% (Undo the
  start), History (Undo the deletion) and Reminders ("Saved.").
- **A3 (B2, TalkBack):** P4 on API 26 and 36, if the spike passed. Otherwise QA confirms there is no pause under TalkBack
  and that the timeout and the ceiling still end the message.
- **A4 (ceiling, device):**
  - Set `adb shell settings put secure accessibility_interactive_ui_timeout_ms 120000`.
  - Delete a migraine on Details, wait about 20 s, go Back to Home, lock, unlock when about 122 s have passed since the
    delete, tap where Undo was.
    - Branch: no Undo on screen.
    - `develop` 40536f49: Undo shown, and the tap gives "This can no longer be undone.".
  - Repeat on History.
  - Restore the setting afterwards.
- **A5 (B3):**
  - uiautomator bounds at density 2.625: Undo and Dismiss are at least 168 px in both dimensions in Home attack mode
    (portrait and landscape, 100% and 200%) and on dark-room Log medication with "storage full";
  - Home Today light and History stay at 126 px (48 dp);
  - one screenshot at 200% portrait attack mode.
- **A6:** the speech check finds nothing new spoken.

## What the checks will not prove

- Unit and Robolectric tests use fake clocks and a fake `AccessibilityManager`. They do not prove what stock Android
  writes to the two timeout settings, or how `delay` behaves in deep sleep.
- The TalkBack spike shows observability on two API levels on emulators, not on every OEM build.
- The bounds checks prove size, not comfort. Whether 64 dp feels heavy over the rain card, and whether 3 s and 300 ms feel
  right, needs a person (ux §D).

## Risks

- **TalkBack focus is not observable reliably.** The fallback is pre-agreed, and the spec changes before code.
- **A longer "Saved." keeps Home's review request waiting longer.** Accepted in ux B1: it is harmless.
- **Code conflict with D180 and D181 in `MigrelySnackbar.kt`.** Task 0.1 handles it. The review checks all three rules
  after the merge.
- **A countdown loop that wakes too often.** It wakes only on state change or expiry. There is no per-frame tick, except the
  existing line drawing.

## Migration

None. No stored value, schema, permission or manifest change.
