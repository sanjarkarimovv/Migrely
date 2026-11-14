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
| Delete all data | confirmation (`DeleteAllDataScreen.kt:265`) | the Delete row is a fixed bottom row with `safeDrawing` bottom (`:214-222`); the scroll above it has none | the row rises with the keyboard; the field's scroll shrinks with it, likely fine |
| Home Today, attack mode | none | `safeDrawingPadding()` (`TodayContents.kt:88`, `AttackModeContents.kt:105`) | pads for a keyboard it never asks for |
| History, Insights, Settings, Settings pages, paywall, Doctor report, My medications, Your data, Reminders | none | `safeDrawing` bottom | the same |
| Dialogs: custom tag, date pickers in input mode, time picker | yes | their own window | unchanged |

The message host is laid out by `MessageLayout` with the top bar and the content. Its fit rule reads the bars and the cutout,
never the keyboard (`MigrelySnackbar.kt:360`, `MessageFit.kt`, D178 (6)). `MessageFitInsetsTest` proves that rule against a
dispatched keyboard inset.

## Goals and non-goals

**Goals:** the line being typed is always visible (#141); the top bar and Back stay put (#83); no screen without a field reserves
keyboard space (#145's likely cause); messages stay where D158, D165 and D178 put them.

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
then works against the right viewport. The padding must not be counted twice: the bottom is the larger of the keyboard and the
navigation bar, not their sum. `union` gives that.

Compose's legacy `OutlinedTextField` brings the field into view on focus. Whether it keeps the **cursor line** in view on every
new line inside a multi-line field is to be proved by the red test (tasks 3.1). If it does not, a small helper in
`designsystem` (`Modifier.keepCursorInView`) takes the cursor's rectangle from `onTextLayout`
(`TextLayoutResult.getCursorRect(selection.end)`) and calls `BringIntoViewRequester.bringIntoView(rect)` when the selection or
the layout changes while focused. Only the note needs it; single-line fields keep their own behaviour.

Delete all data already lifts its fixed Delete row with the keyboard. It changes only if the walk shows the field hidden.

*Rejected:* computing scroll offsets per screen by hand (not KISS); a padding spacer at the end of the scroll (the viewport
does not shrink, so bring-into-view thinks the line is visible, which is #141 today).

### D3. A shared "bars" inset for screens without a field (D180 (3))

`designsystem` gets one public value, for example `MigrelyInsets.bars = WindowInsets.systemBars.union(WindowInsets.displayCutout)`.
The 10 screens without a field use it where they now use `safeDrawing` (the same sides as today). `MigrelySnackbarHost` uses
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
by `rememberScrollState` (already), so closing does not jump. A floating keyboard and split screen give no bottom `ime` inset,
so nothing is reserved. "Remove animations": the inset change and any bring-into-view jump; `rememberReduceMotion()` decides
whether the helper of D2 animates.

### D6. #145 is measured before code (D180 (6))

`qa-engineer` on emulator-5582 (API 26), develop 046efe95: log `WindowInsets.ime` bottom and `safeDrawing` bottom (a debug-only
log line or Layout Inspector) on Home before the note, with the keyboard open, after COPY and Back, and after a restart. If the
stale value is `ime`, D3 removes it from every screen without a field, and #145's rows go into QA's plan. If not, `team-lead`
gets the finding, #145 stays open, and PM drops the "Android 8 after a copy" scenario before code.

## Risks and trade-offs

| Risk | Mitigation |
|---|---|
| A screen with a field that relied on the pan now hides a control | D2 covers all three; the walk and QA run each field at 200% in landscape |
| Double bottom padding (keyboard + navigation bar) | `union`, not `add`; a test asserts the note's bottom at the keyboard's top |
| On API 26 to 29, `systemBars` includes the keyboard by mistake, and the message or the bars inset moves | D4's `sdk = [26]` test; QA's API 26 rows |
| Compose's text field moves focus or scroll on its own and fights the helper | the helper only acts while focused and only when the cursor rect is outside the viewport |
| The safety card's reveal (D167) measures the viewport, which now shrinks with the keyboard | the card appears only after a pain dialog, with the keyboard closed; the D167 tests run unchanged |
| A tall message in a short landscape window lies partly over the keyboard | accepted by D178 (6) and the review: a message never moves for the keyboard; ux question U5 |

## Questions for ux-designer (answer before code)

| # | Question | PM's proposal |
|---|---|---|
| U1 | How much room under the cursor line: only the line, or the line plus the counter ("N/1000") under the field? | The line plus 8 dp; the counter may stay under the keyboard while typing |
| U2 | Top bar in landscape at 200%, where the bar and the keyboard leave about two lines: keep it fixed with Back, or let it scroll away? | Keep it fixed: Back must stay reachable (#83) |
| U3 | Bring-into-view motion: follow the keyboard's own animation, or jump? | Follow the keyboard; jump with "Remove animations" |
| U4 | A tap in the first lines of a long note (QA's row 3): show the tapped line where it is, scroll the minimum, never jump to the note's end? | Yes |
| U5 | A message in a short landscape window with the keyboard open may lie partly over the keyboard, since it never moves for the keyboard. Accept, or ask for a change to D178 (6)? | Accept; the message stays dismissible and its text is spoken |
| U6 | When the keyboard closes, the page stays where it is, or goes back to where it was before the field was focused? | Stays where it is |
| U7 | Delete all data in landscape at 200%: the field and the fixed Delete row above the keyboard, with the warning scrolled away. Enough, or must part of the warning show? | Enough; the warning was read before typing |
| U8 | TalkBack: any announcement when the page scrolls for the cursor? | None; TalkBack already speaks the typed text |
| U9 | Walk matrix: which fields at which font and orientation, on API 36 and API 26, must the engineer's walk and QA's run cover? | Every field, 100% and 200%, portrait and landscape, API 36; on API 26 the note and #145 |

## Acceptance (QA, this diff only)

| # | Build | Steps | Pass | Negative control |
|---|---|---|---|---|
| K1 | Debug and R8 | Note: type past the keyboard at 100% and 200%, portrait and landscape; tap the first lines of a 996-unit note; Tab into it; paste | cursor line visible after each step | same commit without D2 on details: line under the keyboard |
| K2 | Debug | Note with the keyboard open: Back's bounds and a first tap; status bar clear; TalkBack reaches Back | bounds unchanged, leaves the screen | same commit without `adjustResize`: bounds `[0,0][0,0]` |
| K3 | Debug | Edit medication name and dose, Log medication name, Delete all data, 200% landscape | field, cursor and Save or Delete reachable | without D2 on Edit medication |
| K4 | Debug | A message on details, then the keyboard; the keyboard, then "storage full"; the keyboard closes under a message | the message does not move or cover Back | host padded for `ime` (unit test mutation) |
| K5 | Debug, API 26 | #145 steps, then Home and History | full content, no restart (only if D6 found a stale `ime`) | 1.2.1's build: half blank |
| K6 | Debug | Back from the note with the keyboard open, rotation, background and return, process death, "Remove animations", RTL (pseudo-locale), hardware keyboard on the emulator | no blank space, no jump, note kept | none needed |
| K7 | Debug | Custom tag dialog and the date picker's input mode | still usable | none needed |
| K8 | Release | `:app:checkReleasePermissions` and the merged manifest | 1.2.1's permissions, `adjustResize` on `MainActivity` | none needed |

**What these do not prove:** a real phone, keyboards other than the emulator's (Gboard on API 36, the AOSP keyboard on API 26),
API 27 to 35, foldables and tablets.

## Migration

None. No data, string or permission changes.
