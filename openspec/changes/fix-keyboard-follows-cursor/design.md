# Design

## Context

Migrely draws edge to edge (`MainActivity.kt:295`, `enableEdgeToEdge`) and its manifest sets no `windowSoftInputMode`. Android
then pans the window for the keyboard. Compose screens learn about the keyboard only through `WindowInsets.ime`, which is part
of `WindowInsets.safeDrawing`. Read at `develop` 046efe95:

| Screen | Text field | Bottom inset today | With the pan turned off |
|---|---|---|---|
| Attack details | Note (`AttackImpactNoteSections.kt:112`, `minLines = 3`) | none on the scroll, a `navigationBars` spacer at the end (`AttackDetailsScreen.kt:511`) | the note runs under the keyboard (#141) |
| Edit medication | name, dose (`EditMedicationScreen.kt:254`, `316`) | `safeDrawing` bottom **inside** `verticalScroll` (`:214-217`) | the viewport does not shrink, so a field near the bottom or Save can sit under the keyboard |
| Log medication | name (`LogMedicationScreen.kt:544`) | the same, inside the scroll (`:329-332`) | the same |
| Delete all data | confirmation (`DeleteAllDataScreen.kt:265`) | the Delete row is a fixed bottom row with `safeDrawing` bottom (`:214-222`); the scroll above it has none | on API 36 the row rises with the keyboard, and in a short landscape window it then leaves no room for the input line; on API 26 with no toolbar shown Compose never sees the keyboard today, so the row stays under it (QA 1.3, y 2159). Gets D7 |
| Home Today, attack mode | none | `safeDrawingPadding()` (`TodayContents.kt:88`, `AttackModeContents.kt:105`) | pads for a keyboard it never asks for |
| History, Insights, Settings, Settings pages, paywall, Doctor report, My medications, Your data, Reminders | none | `safeDrawing` bottom | the same |
| Onboarding | none | `safeDrawing` on all sides (`OnboardingScreen.kt:178`) | the same (ux F2) |
| Dialogs: custom tag, date pickers in input mode, time picker | yes | their own window | unchanged |

The message host is laid out by `MessageLayout` with the top bar and the content. Its fit rule reads the bars and the cutout,
never the keyboard (`MigrelySnackbar.kt:360`, `MessageFit.kt`, D178 (6)). `MessageFitInsetsTest` proves that rule against a
dispatched keyboard inset.

## Goals and non-goals

**Goals:** the line being typed is always visible, 8 dp clear of the keyboard (#141); the top bar and Back stay put (#83); no
screen without a field reserves keyboard space (#145, confirmed as a stale `ime` on API 26); messages stay where D158, D165 and
D178 put them.

The UX answers are `migrely-work/t63/ux-keyboard.md` (ux-designer, 2026-11-14), accepted by `team-lead` as written, with
findings F1 to F4 folded in here and F5 to F7 noted below. QA's #145 measurement is task 1.3's record.

**Non-goals:** the list in the proposal's "Out of scope".

## Decisions

### D1. `adjustResize` on `MainActivity` (D180 (1))

One manifest attribute. Under edge to edge, Android then neither pans nor shrinks the window on API 30 and later. It reports
the keyboard as an inset, and each screen decides. On API 26 to 29 the decor's layout flags keep the window full size as well,
and the keyboard arrives as system-window insets, which `WindowInsetsCompat` splits into `ime`.

*Rejected:* keeping `adjustPan`. It is #83. `adjustNothing` gives no inset on older APIs, so the cursor cannot follow.

### D2. Keyboard padding outside the scroll on screens with a field (D180 (2))

On Attack details, Edit medication and Log medication, the bottom keyboard inset goes **before** `verticalScroll` in the
modifier chain, as `windowInsetsPadding(<bars ∪ ime>.only(Bottom))` or `imePadding()` followed by the navigation-bar inset
where needed. The scroll's viewport then ends at the keyboard's top. Compose's `BringIntoView` for a focused field and its cursor
then works against the right viewport. The margin is 8 dp between the cursor line and the keyboard's top, and 8 dp below the top
bar when the cursor moves up (U1); the note's counter may stay under the keyboard. A focused single-line field shows its box
and its supporting line when they fit, otherwise its input line, then the supporting line, then the label (U1). Motion is the
keyboard's own inset animation and Compose's default bring-into-view; no custom curve or timing; "Remove animations" jumps; on
API 26 to 29 the inset arrives in one step (U3). A tap on a visible line moves nothing, and a tap's place wins over
`opensAtEnd` (U4). The padding must not be counted twice: the bottom is the larger of the keyboard and the
navigation bar, not their sum. `union` gives that.

Compose's legacy `OutlinedTextField` brings the field into view on focus. Whether it keeps the **cursor line** in view on every
new line inside a multi-line field is to be proved by the red test (tasks 3.1). If it does not, a small helper in
`designsystem` (`Modifier.keepCursorInView`) takes the cursor's rectangle from `onTextLayout`
(`TextLayoutResult.getCursorRect(selection.end)`) and calls `BringIntoViewRequester.bringIntoView(rect)` when the selection or
the layout changes while focused. Only the note needs it; single-line fields keep their own behaviour.

Delete all data gets its own rule, D7.

*Rejected:* computing scroll offsets per screen by hand (not KISS); a padding spacer at the end of the scroll (the viewport
does not shrink, so bring-into-view thinks the line is visible, which is #141 today).

### D3. A shared "bars" inset for screens without a field (D180 (3))

`designsystem` gets one public value, for example `MigrelyInsets.bars = WindowInsets.systemBars.union(WindowInsets.displayCutout)`.
The 11 screens without a field (12 files, onboarding included after ux F2) use it where they now use `safeDrawing` (the same sides as today). `MigrelySnackbarHost` uses
the same value in its fit rule, so the rule and the screens cannot drift (DRY). Screens with a field use
`MigrelyInsets.bars.union(WindowInsets.ime)`, which equals `safeDrawing` today and is named so the intent is clear.

### D4. Messages stay keyboard-independent (D180 (4))

The keyboard padding is applied inside the `content` slot of `MessageLayout`, on the scroll column only. Nothing above it
(`MessageLayout`'s modifier, the screen's root `Box`, `MigrelySnackbarHost`) gets a keyboard inset. A Robolectric test on
Attack details shows a message, dispatches a 250 dp `ime` inset (the same way `MessageFitInsetsTest` does) and asserts that the
message's top and Back's bounds did not change. A local mutation that adds `imePadding()` to the host's modifier must make it
fail. On API 26 to 29 the test also runs with `@Config(sdk = [26])` to show that `systemBars` does not pick up the keyboard there.

### D5. Closing, rotation, other keyboards (D180 (5))

Nothing is added: the insets go to zero when the keyboard closes, and Compose re-lays the scroll. The scroll position is kept
by `rememberScrollState` (already), so closing does not jump: the page stays where it is, and settles down by at most the
keyboard's height if it was scrolled to its end (U6). The first Back closes only the keyboard. Nothing is announced (U8). The
strip the keyboard uncovers is the page's own colour (ux S13). A floating keyboard and split screen give no bottom `ime` inset,
so nothing is reserved. "Remove animations": the inset change and any bring-into-view jump; `rememberReduceMotion()` decides
whether the helper of D2 animates.

### D6. #145: a stale `ime` on Android 8 (D180 (6))

Measured by `qa-engineer` on emulator-5582 (task 1.3, develop ea41869a). Under the resolved `adjustPan`, `WindowInsetsCompat` on
API < 30 takes `ime` from the root view's visible insets and caches it only on an insets dispatch. Showing or hiding the text
toolbar dispatches; closing the keyboard under pan does not. After COPY, `ime` stays 901 px: Home loses 775 px, History ends at
y 1499, and only a new activity clears it. It reproduced from the note, Edit medication, Delete all data and History; without
the toolbar 6/6 clean; API 36 clean.

D3 removes the value from every screen without a field (`navigationBars` on API 26 is capped at the root's stable bottom). The
screens with a field still read `ime` (D2). **Not proven: whether `adjustResize` alone makes Android dispatch on the keyboard's
close and so clear the value.** Task 3.6 proves it red to green, by a test if Robolectric can model it, else by a device check
on API 26 that QA runs for the engineer. If it does not clear, the engineer adds the smallest fix that does (for example a
re-request of insets when the keyboard's visibility changes) and reports it before going on.

### D7. Delete all data with the keyboard open (D180 (8), ux U7)

The Delete row rides above the keyboard only while the input line plus 8 dp still shows above it. Otherwise it stays at the
window's bottom, behind the keyboard; Done or Back closes the keyboard and the field and "Delete everything" show together.
The choice is made once per keyboard opening, from the keyboard's final height (`WindowInsets.imeAnimationTarget` on API 30 and
later, the one-step inset on API 26 to 29), so the row never jumps midway. On API 26 the same rule now applies because the
screen sees the keyboard through `adjustResize`. Fallback if the rule proves fragile in the walk: the row never rides above the
keyboard, and the flow is always type, Done, Delete.

### D8. An autosave does not move the cursor (D180 (9), ux F4)

`LimitedField.kt:32` (`rememberLimitedField`) shows `atTheEnd(text)` whenever the stored `text` differs from the field's text.
If the note's autosave (600 ms) leaves a frame where the stored text lags the field, the cursor jumps to the end, and with D2
the page follows it. The red test W-AUTOSAVE (task 3.1) types, waits past the autosave and types again, and asserts that neither
the cursor nor the page moved. If it fails, the fix belongs where the draft and the saved note meet, so that a save of the same
text never counts as an outside change; a real outside change still puts the cursor at the end ("Note with a limit").

### Not in this change (ux F5 to F7)

- **F5:** on Edit medication a name or dose error shows only in the supporting line, which is not a live region, so after Save
  with the keyboard open it may be off screen and silent for TalkBack. Pre-existing, not reproduced. QA confirms it on a device
  (task 8.3) before anyone files an issue.
- **F6:** Compose text fields may turn off a keyboard's full-screen editor; if QA cannot reach it, the row is "not reachable",
  not a pass.
- **F7:** the system keyboard follows the phone's theme, so a light keyboard can open over the dark attack screens. A later note.

## Risks and trade-offs

| Risk | Mitigation |
|---|---|
| A screen with a field that relied on the pan now hides a control | D2 covers all three; the walk and QA run each field at 200% in landscape |
| Double bottom padding (keyboard + navigation bar) | `union`, not `add`; a test asserts the note's bottom at the keyboard's top |
| On API 26 to 29, `systemBars` includes the keyboard by mistake, and the message or the bars inset moves | D4's `sdk = [26]` test; QA's API 26 rows |
| Compose's text field moves focus or scroll on its own and fights the helper | the helper only acts while focused and only when the cursor rect is outside the viewport |
| The safety card's reveal (D167) measures the viewport, which now shrinks with the keyboard | the card appears only after a pain dialog, with the keyboard closed; the D167 tests run unchanged |
| In a short landscape window the keyboard covers a message's lower part, its Dismiss or Undo included; a message over the content can cover the cursor line | accepted (ux U5, F1): the keyboard is a window above the app, a message never moves for it (D178 (6)); it stays dismissible by a sideways swipe, TalkBack's action or closing the keyboard, and the untimed storage-full error may cover the line |
| `adjustResize` alone may not clear the stale `ime` on API 26 for screens with a field | task 3.6, D6 |

## UX answers (task 1.1, accepted by `team-lead`)

U1 to U9 are answered in `migrely-work/t63/ux-keyboard.md` and folded into D2, D5 and D7 above and into the deltas. Departures
from PM's proposals, all accepted: U1 adds the single-line field rule; U5 is accepted with the facts corrected (the keyboard
covers the message, never the reverse) plus the cursor-line exception (F1); U7 adds the short-window rule (D7); U9 widens the
matrix. The walk matrix (U9) in that file is the matrix for tasks 6.2 and 8.1: fields F1 to F7, 20 cells on API 36, 2 cells on a
360 dp landscape window, 6 cells on API 26, and the single rows (TalkBack, messages, Remove animations, RTL, hardware keyboard,
rotation, background and process death, W-AUTOSAVE, light theme, split screen, floating keyboard, screens without a field).

## Acceptance (QA, this diff only)

| # | Build | Steps | Pass | Negative control |
|---|---|---|---|---|
| K1 | Debug and R8 | Note: type past the keyboard at 100% and 200%, portrait and landscape, 411 and 360 dp tall; tap the first lines of a 996-unit note; Tab into it; paste; W-AUTOSAVE (tap the middle, type, wait 1 s, type) | cursor line 8 dp above the keyboard after each step; W-AUTOSAVE: neither cursor nor page moves | same commit without D2 on details: line under the keyboard |
| K2 | Debug | Note with the keyboard open: Back's bounds and a first tap; status bar clear; TalkBack reaches Back | bounds unchanged, leaves the screen | same commit without `adjustResize`: bounds `[0,0][0,0]` |
| K3 | Debug | Edit medication name and dose, Log medication name, Delete all data, 200% landscape (411 and 360 dp), and on API 26 Log medication and Delete all data | input line above the keyboard, supporting line when it fits, Save reachable; Delete all data per D7: the row above the keyboard only with room, else behind it and shown after Done; on API 26 the row never left under the keyboard with the field hidden | without D2 on Edit medication; develop ea41869a on API 26 (row under the keyboard) |
| K4 | Debug | A message on details, then the keyboard; the keyboard, then "storage full"; the keyboard closes under a message; at 200% landscape the keyboard covers the message's button, then a swipe and a TalkBack action dismiss it; a message over the cursor line | the message does not move or cover Back, and stays dismissible | host padded for `ime` (unit test mutation) |
| K5 | Debug, API 26 | #145 COPY steps from the note, an Edit medication field and the Delete all data field, then Home and History; and back on a field screen with the keyboard closed | full content, no restart, no space kept for a closed keyboard | develop ea41869a: Home loses 775 px, History ends at y 1499 |
| K6 | Debug | Back from the note with the keyboard open, rotation, background and return, process death, "Remove animations", RTL (pseudo-locale), hardware keyboard on the emulator, light theme on API 26 and 36, split screen, floating keyboard and full-screen editor if reachable | no blank space, no jump, no band, note kept; an unreachable row is "not reachable", not a pass | none needed |
| K7 | Debug | Custom tag dialog and the date picker's input mode | still usable | none needed |
| K8 | Release | `:app:checkReleasePermissions` and the merged manifest | 1.2.1's permissions, `adjustResize` on `MainActivity` | none needed |

**What these do not prove:** a real phone, keyboards other than the emulator's (Gboard on API 36, the AOSP keyboard on API 26),
API 27 to 35, foldables and tablets.

## Migration

None. No data, string or permission changes.
