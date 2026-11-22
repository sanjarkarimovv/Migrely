# Design

Release 1.3.3 (team-lead 2026-11-19; 1.3.2 is QA-accepted and not widened), issue #255 (S3, accessibility, pre-existing on
v1.3.1 and `develop`). BRD D193, EC-UX-78 to EC-UX-82, EC-UX-89 and EC-UX-90; guards D178, D181, D189, D191, D192. Root cause and prototypes:
eng-255, `migrely-work/255/eng-255-report.md` (device walks, TalkBack order dumps, `prototype/proto2.diff`,
`prototype/TalkBackScrollStep2.kt`). This file binds what the person hears and what QA checks; the code shape is the
engineer's call within it.

## Context

The person: TalkBack on, one hand, in a dark room, swiping through a page to find Back. Swipes are their only map. A loop or
a jump is not an annoyance, it is a locked door.

### Cause B: the loop (main cause)

Compose keeps a clickable node in the accessibility tree while it lies up to 24 dp outside its scroll area's visible part.
Touch bounds are clipped against the scroll clip widened by half of `ViewConfiguration.minimumTouchTargetSize` (48 dp)
(`NodeCoordinator.rectInParent(clipToMinimumTouchTargetSize = true)`), while `boundsInWindow` uses the real clip and comes
out empty, `Rect.Zero` (`LayoutCoordinates.kt:193-206`). `SemanticsSort.sortByGeometryGroupings` then puts that node at the
top of the page, right after the top bar. The node is only reachable if the 24 dp band lies inside the window, which is true
whenever the visible part ends above the window's bottom: keyboard up; the navigation bar inset at 100% portrait with no
keyboard (visible part ends at 2337 px, window at 2400 px); a dialog's buttons; a message. TalkBack "previous" from the first
visible item goes to the hidden node, scrolls down to it and cycles. Its label is clipped, so it says only "Not selected".
Proof: TalkBack's own order dump on emulator-5570 (`tree-at-duration.log`): "Thursday, November 19" → "Add your own symptom"
(bounds 42,2349-330,2400, label invisible) → "Duration 0 min", scroll area 0,296-1080,2337.

### Cause A: skipped blocks

At the edge, TalkBack asks the scroll node to scroll forward or backward. Compose scrolls by the viewport length, a whole
visible height. The focused node leaves the screen and the tree; TalkBack falls back to the first or last item of the scroll
area. v1.3.1: "Stress → Symptoms", skipping "Possible triggers", "Add your own symptom" and every symptom chip.

Not the cause: `FlowRow`, `traversalIndex`, `MessageLayout`, chip semantics (each ruled out by eng-255).

### Device evidence (emulator-5570, TalkBack 16.0.0, console "previous" swipes, 411 dp portrait)

| Build | Keyboard | Result |
|---|---|---|
| `develop` de142a8a | no | skips, then loops after Back (`walk-b1`) |
| prototype 1 (half step + chip name on chip) | no | 50 stops straight, then a "Duration → Add your own symptom" loop: B remains |
| prototype 2 (half step + exact bounds) | no | Delete → Back, all 55 stops once; one chip said only "Not selected" |
| prototype 2 | yes | Notes → Back, every stop named |
| `develop` | yes | reached Back, 9 chips said only "Not selected" |

So both parts are needed, and exact bounds alone still leave one cut chip without its name: that is why "Every stop says its
name" is a separate requirement.

## Goals and non-goals

Goals: every scrolling page and dialog walks end to end, each stop once and named (EC-UX-78 to EC-UX-81); nothing drawn
changes and no touch target shrinks below 48 dp (EC-UX-82, EC-UX-89); one shared mechanism with a guard against new raw
scrolls (EC-UX-90); every check can fail.

Non-goals: blanket naming changes on stops that keep their name when cut; reporting to Google (owner's send; note only), any visual, text or motion change, new accessibility features,
lazy lists and horizontal scrolls (none exist), #252, #254.

Layers: `designsystem`, then feature `ui` modules. No domain, presentation, data, DI, navigation, manifest or string change.

## Decisions

### D1. One entry point in `designsystem`

Both parts are applied by one `designsystem` API that every vertical scroll uses (shape is the engineer's: for example a
`Modifier` for the step plus a content wrapper for the bounds, or one scroll-column composable). No feature module calls
`Modifier.verticalScroll` directly. Reason: DRY, and a new screen cannot forget the fix.

A JVM test (or detekt rule) scans `src/main` of every module except `designsystem` and fails on `verticalScroll(`, naming
file and line. Negative control: add one raw call in a scratch copy, the check is red; remove it, green.

### D2. Exact accessibility bounds inside a scrolling page (fixes B)

Inside the scroll content, provide `LocalViewConfiguration` as a delegate of the current one with only
`minimumTouchTargetSize = DpSize.Zero`. Every other member delegates (`doubleTapTimeoutMillis`, `longPressTimeoutMillis`,
`touchSlop`, `doubleTapMinTimeMillis`, handwriting members), so the double-tap and lift rules (D161 family) and
`ScreenDestination`'s double-tap timeout read the same values. Material3's `minimumInteractiveComponentSize` reads
`LocalMinimumInteractiveComponentSize`, not this, so Material controls keep their 48 dp **layout** size. That holds for
layout, not for touch (correction from task 3.1): a Material3 `IconButton` reserves 48 dp but its clickable node is 40 dp,
and the 4 dp ring was touchable only through this widening. Under D2 a tap there misses.

Effect beyond accessibility: Compose's pointer hit-test also uses `minimumTouchTargetSize` to extend small pointer-input
nodes. Inside the page a clickable smaller than 48 dp would lose that extension. Hence D5 and its option-1 rule.

As built (task 2.3): `designsystem/layout/PageScroll.kt`, `PageScroll { Column(Modifier….pageScroll(state)) { … } }`. The
provider wraps the scrolling layout itself (Compose widens the clip in that layout node's own `rectInParent`), still never
the top bar, message host or dialog buttons (D4a).

### D3. TalkBack's scroll step (fixes A): floor(V/2) px (ux 1.1, lead decision 1)

On the scroll node, `semantics { getScrollViewportLength { step } }` (public Compose API) with `step = floor(V / 2)` px.
V is the part of the scroll area actually visible: its height minus anything over it (top bar, message, keyboard,
navigation bar, a dialog's buttons). Where `viewportSize` already ends at those edges (Attack details: insets, message
clearance and `TypingSpace` sit before `verticalScroll`), V = `viewportSize`; where a page puts an inset or the keyboard room
inside its scroll content, V is what shows, not `viewportSize`. Less than one step left: Compose stops at the end, as now.
A shown message is subtracted too (cr-255 F5): `MessageLayout` gives its message and its content a `MessageCover`
(`designsystem/snackbar/MessageCover.kt`), `MigrelySnackbarHost` fills in the shown message's bounds in root coordinates,
and `PageScroll` leaves that part out of V when TalkBack scrolls; null while no message shows. `PageScrollTest`: with the
cover ignored, red (433 px for 403).

**Known limit, kept (team-lead 2026-11-19):** a message drawn over the top of a page covers the items under it, and they
leave TalkBack's tree while it shows; swiping back at the page's top ends at the first item not covered. `develop` does the
same. The message is itself a stop with its own Dismiss, pauses under TalkBack focus (D182), and once it goes every item is
reachable again. Moving the host is out of #255's scope; a possible later ux item.

Compose's scroll-forward/backward actions then move by the step, eased by its spring (instant under Remove animations).
Switch Access uses the same action, so it needs about twice as many scrolls on a long page (ux accepted the cost: keeping
the place matters more). Finger scroll, fling, Tab-into-view and Page Up/Down do not use it.

**What the step can promise (ux 1.1).** TalkBack 16 keeps its place only while the focused item stays on screen; after a
scroll it searches from that item and scrolls again in the same swipe if the next one is not yet in view. A step of V/2
keeps both on screen whenever the gap g to the next stop is under V/2 (the largest regular gap is the 28 dp section spacing,
73.5 px at density 2.625). Where g ≥ V/2 (200% landscape, 411 dp, keyboard up on Attack details: V 137 px, step 68 px;
large non-focusable blocks on Today, Paywall and Onboarding at 200% landscape) the focused item can leave the screen, but
the next item is in view and TalkBack's fallback still lands on the next item in order. So the spec promises the order in
every case and "the item stays on screen" only while g < V/2; those scroll tests assert the order, not the pivot. A 61 px
visible part with a 73.5 px gap (the 360 dp window before #245) cannot be fixed by any step; D192 widens that window.

#### Interaction with #238's keyboard room

D189 says the room goes only with the person's scroll, TalkBack's scroll action included. A shorter step means the room may
shrink in two actions instead of one; it still shrinks only on the person's action. `KeyboardRoomTest`'s TalkBack case is a
guard and must pass unchanged.

### D4. Design inputs, decided (ux note `docs/ux/255-talkback-scroll-order.md`, 91ceda44; lead decisions 2, 3)

1. **Step:** D3.
2. **Chip names, option 3: yes, only together with D2.** On `ChoiceChip` (`AttackChoiceSections.kt:204-262`)
   `contentDescription = spokenLabel ?: label` on the chip's clickable node, inner `Text` semantics cleared (prototype 1's
   diff; the D181 (2) pattern already used by `PeriodToday` and attack mode's Details and move-start buttons). Compose puts
   the description on a fake child that always keeps the chip's own clipped bounds, so whenever the chip is a stop its name
   is too. Without D2 it fails (prototype 1: hidden chips in the 24 dp band had empty bounds); D2 without it leaves a cut
   chip whose text child is out of the tree (prototype 2, "Back of the head", `walk-q1.txt` line 36). Spoken form, TalkBack
   16 default verbosity, state first: "Not selected. Nausea", "Selected. Nausea", hint "Double-tap to toggle"; the add chip
   and a user's own tag are unchanged. This is what a whole chip says today, so nothing new is heard.
3. **Other stops: data-driven, not blanket (lead decision 3, KISS).** ux proposed that every stop whose name comes only
   from a child `Text` carry its words on its own node. Not applied across the app. The cut sweep (D7) finds the stops that
   lose their name when cut at the top or bottom edge; only those get their words on their own node (same string resource,
   inner `Text` cleared), and each passes the D181 one-name check. A stop that keeps its name is not touched.

### D4a. D2 wraps only the scroll content (ux 1.3, lead decision 5)

The `ViewConfiguration` delegate is provided inside the scroll content only. The top bar, the message host (its 64 dp
dark-room Undo and Dismiss, D182 (4)), bottom bars and dialog buttons stay outside, with the system's touch target and its
widening. The prototype wrapped the whole `ReadyContents` box; the real code does not. Checked by a test that reads the
minimum touch target outside (48 dp) and inside (0) on a changed page; wrapping too wide turns it red.

### D5. Touch-target audit before D2 lands on a page (team-lead condition; ux 1.3, lead decision 4)

Before D2 is applied to a page or dialog, a test measures every clickable node's touch bounds in it, width and height, at
360 and 411 dp wide, at 100% and 200% font. Bars: 48 dp; 64 dp for attack mode's intensity choices, "Migraine ended" and a
message's Undo and Dismiss (attack-mode spec). Results go in `tasks.md` (task 3) and the evidence file.

| # | Where | Rule |
|---|---|---|
| T1 | History month calendar day cells, `designsystem/.../sky/MonthSkyCalendar.kt:320-345` (`clickable` at 338, `CellSpacing` 6 dp), inside History's scroll | At 360 dp wide a cell is about 41.7 dp with 6 dp gaps; today Compose's widening covers the gaps, under D2 a tap in a gap would hit nothing. Measure at 360 dp wide and 200%. If under 48 dp: a touch area covering the whole cell pitch (cell plus half the gap on each side, about 47.7 dp; seven 48 dp targets cannot fit 328 dp, so the pitch is today's real target). The drawn cell, its clip and its focus ring stay; screenshots identical. History is not left out. |
| T2 | Date picker, `MigrelyDatePicker.kt:151`, `MigrelyDateTimePicker.kt:147` (Material3 `DatePicker`) | If any control measures under 48 dp under D2, that picker stays out of D2 (keeps D3) and D193 records it; result below: a residual with a follow-up issue. Touch wins: the picker has a text-entry mode for TalkBack users, a tremor has no other path. |
| T3 | Time picker, `MigrelyDateTimePicker.kt:189`, Reminders dialog `RemindersScreen.kt:589` (Material3 `TimePicker`) | As T2. |
| T4 | Attack mode, `AttackModeContents.kt:132, 170, 194` | No change: the 64 dp controls are a layout minimum D2 cannot shrink; Details, move-start and long-check buttons are 48 dp by design (D24) and pass at 48 dp. They are not enlarged and attack mode is not left out. |

**Option-1 rule (team-lead 2026-11-19).** Inside D2 every clickable, toggleable or selectable node is itself at least
48×48 dp (64 dp where attack mode's bar applies), from its own size, never from `ViewConfiguration` widening. Nothing drawn
changes.

- **Icon buttons:** `designsystem/button/MigrelyIconButton`: a 48 dp clickable node with Material's 40 dp circle,
  container, icon and ripple drawn inside it; presses are shifted by the 4 dp inset so the ripple starts under the finger.
  Used by the 9 in-page icon buttons: Attack details' intake and pain-entry Close, History's month chevrons, Today's
  Settings, a medication row's Close, the effect question's Skip, the visit card's and the trial banner's Close.
  `MigrelyIconButtonTest`: a tap 2 dp outside the 40 dp circle inside a page acts (Material's misses); at rest and disabled
  0 pixels differ from Material's. Pressed pixels: instrumented `MigrelyIconButtonPressedTest` (Robolectric draws no
  ripple), run in the device round (6.4).
- **Stay Material, outside D2:** top-bar Back and actions, the Paywall's close row above its scroll, the message host.
- **Checkboxes, switches, radio buttons:** each passes a null handler; the row is the toggleable or selectable node, 48 dp
  or more. No change.
- **Audit result (3.1):** 48 tests in 11 `ui` modules at 360 and 411 dp wide, 100% and 200%, plus 800×360 dp landscape for
  home and history; 31 screens and cards; every control at its bar (smallest 48.0 dp; attack mode's 64 dp controls 64;
  History's days 47–48 dp, T1). Table `migrely-work/255b/touch-targets.md` (127 rows).

**T1 as built (3.2).** Each History day's click, focus and semantics sit on a slot of the whole cell pitch (the 41.7 dp cell
plus 3 dp, half the gap, on each side: 47–48 dp at 360 dp); the drawn cell (clip, sky, rings, keyboard focus ring, ripple)
is inside it and the weeks are laid out 3 dp out, so each cell is drawn where it was. Pixels at 360 and 411 dp, 100% and
200%: byte-identical (control: without the outset all four differ). `MonthSkyCalendarPitchTest`: taps 1 dp into each gap,
red before (`[]`), green after (`[1, 2, 1, 8]`).

**T2 and T3 result (3.3): residual.** The Material date picker, the date-time picker and Reminders' time dialog have 40 dp
tall controls: the mode toggle and month arrows (40×40), the month/year button (193×40), AM/PM (52×40) and the time step's
Cancel and OK. They stay out of D2 and keep D3's half step (the entry point has a step-only form). D193 records it; at
close-out `qa-engineer` files a follow-up issue for these pickers and #255 closes on the rest. `PickerTouchTargetAuditTest`
turns red if Material's controls ever reach 48 dp, the cue to bring them into D2.

**TalkBack's focus outline (intended, team-lead).** It follows the node: 48 dp square for in-page icon buttons (was the 40
dp circle) and the whole pitch for History days (was the drawn cell). It now shows the true touch area, is drawn by
TalkBack, and shows only while focused. "Nothing drawn changes" (D193 (2)) means the resting and pressed UI. QA sees it in
8.2.

Pages left out of D2: `MigrelyDatePicker.kt:151`, `MigrelyDateTimePicker.kt:147, 189`, `RemindersScreen.kt:589`.

### D6. Guards (team-lead): must stay green unchanged

| Guard | What | Check |
|---|---|---|
| #238, D189 | closing the keyboard never scrolls; TalkBack focus stays on the field; room only with the person's scroll | `*KeyboardRoom*`, `AttackDetailsKeyboardRoomTest`, `DeleteKeyboardTest` |
| #245, D192 | `TopBarAside`, `TypingSpace`; aside bar's nodes out of the tree | `TopBarAsideTest`, `TypedLineMarginsTest`, `MessageBarAsideTest` |
| #239, D191 | one announcement per refused Save | `EditMedicationProblemFeedbackTest`, `FirstMedicationProblemFeedbackTest` |
| D181 | one spoken name, Tab stops visible and in view, Shift+Tab reverse | existing D181 tests |
| D178 | order: top bar, message, content | existing message-order tests |
| D161 family | double-tap and lift rules (pointer only) | `DialogWait*`, double-tap tests |

### D7. Tests and their controls

- **Kept:** eng-255's red tests (0ecb3bc6), `features/attack/ui/src/testDebug/.../view/`: `AttackDetailsTalkBackOrderTest`
  (3: portrait 100% keyboard 411 dp; landscape 360 dp 200% keyboard; portrait 100% no keyboard) and
  `AttackDetailsTalkBackScrollTest` (4: portrait and landscape, back and on), with `TalkBackTree`. On 05b37589: 7 tests, 6
  failed; the no-keyboard order test passes and is the positive control (the check can pass). Re-run on f70b6450 first.
- **Added:** the navigation-bar case. Robolectric has no navigation bar inset, which is why the no-keyboard order test is
  green while the device walk (`walk-b1`) loops. A variant with a bottom system-bar inset (visible part ending above the
  window) must be red on f70b6450.
- **Per screen group:** the same two checks (order, scroll) through a shared test fixture (move `TalkBackTree` to one place
  the `ui` modules can use; no copies), red on f70b6450 first, then green.
- **Cut sweep (lead decision 3):** for each stop on each page and dialog, scroll so that 1 px to 12 dp of it shows at the top
  edge and then at the bottom edge, and read its name from the **unmerged** tree as TalkBack builds it (the node's own text
  or description, then its visible non-focusable children). Today's `talkBackName()` (`TalkBackTree.kt:76`) reads merged
  semantics, which always contain the clipped text, so it can never fail this way; it is replaced. Negative control: a chip
  without option 3 under D2 is red.
- **Mutations:** remove D2 → order tests red; remove D3 → scroll tests red; raw `verticalScroll` → source check red; D2
  around the whole screen → scope test red; chip name back on the inner Text → cut sweep red.
- **Scroll tests and large gaps:** where the gap to the next stop is V/2 or more (D3), the scroll test asserts the order
  outcome, not that the focused item stayed on screen.
- **Fragility:** the tests read Compose's test-only `EXTRA_DATA_TEST_TRAVERSALBEFORE_VAL`. If a Compose upgrade changes it,
  they fail with "only N items"; they never pass silently.
- **Device:** QA walks TalkBack previous and next on the candidate with v1.3.1 and `develop` as the negative control (they
  must loop or skip on Attack details). Lesson from eng-255: a `uiautomator dump` taken while TalkBack is on resets its focus
  and voids that walk.

### D8. Every site (`develop` f70b6450, `src/main`)

| Group | Module | Site | Notes |
|---|---|---|---|
| G1 | `features/attack/ui` | `AttackDetailsScreen.kt:494`, `AddPastAttackScreen.kt:263` | the issue; red tests exist |
| G2 | `features/home/ui` | `TodayContents.kt:97, 116, 128`; `AttackModeContents.kt:132, 170, 194` | attack mode: 64 dp targets |
| G3 | `features/medication/ui` | `EditMedicationScreen.kt:237`, `LogMedicationScreen.kt:357`, `MedicationsScreen.kt:153` | #239, #245 guards |
| G4 | `features/settings/ui` | `SettingsScreen.kt:215`, `SettingsPages.kt:272`, `DeleteAllDataScreen.kt:342` | #238, #245 guards |
| G5 | `features/history/ui` | `HistoryScreen.kt:327, 335, 347` | |
| G5 | `features/insights/ui` | `InsightsScreen.kt:155` | Premium |
| G5 | `features/report/ui` | `ReportScreen.kt:288` | Premium |
| G5 | `features/backup/ui` | `YourDataScreen.kt:304`; dialogs `:390`, `:438` | dialogs |
| G5 | `features/reminders/ui` | `RemindersScreen.kt:357`; dialog `:589` | dialog `:589` (time picker): step only, out of D2 (D5) |
| G5 | `features/subscription/ui` | `PaywallScreen.kt:269` | |
| G5 | `features/onboarding/ui` | `OnboardingScreen.kt:180` | |
| G6 | `designsystem/picker` | `MigrelyDatePicker.kt:151`, `MigrelyDateTimePicker.kt:147, 189` | dialogs; step only, out of D2 (D5) |

23 page sites, 6 dialog sites, 29 in all. Line numbers are from f70b6450; the source check is the authority.

## Risks

- **Touch area inside pages (D2).** Mitigated by D5; a missed control is a real regression for tremor or one-hand use.
- **Step size (D3).** Half the visible part doubles Switch Access's scroll actions (accepted by ux). Where a gap is V/2 or
  more the focused item can leave the screen; the order still holds and is what the tests bind. V must be measured, not
  taken from `viewportSize`, on pages with an inset inside the scroll.
- **Naming changes beyond chips.** The cut sweep may find buttons or rows that say only "Button" or a state when cut; each
  fix widens the diff and the Maestro check.
- **Compose upgrade.** May fix B (D2 can go) or change the test data (tests fail loudly). Note for the owner: worth
  reporting upstream; the team does not send it.
- **Maestro selectors.** With option 3 the chip's name moves from a TextView's text to a content description on a fake
  child with the chip's bounds. Maestro's `text` selector also matches content-desc, so `tapOn: "Nausea"` should still work;
  `childOf`, `containsChild`, `index:` and `selected:`/`checked:` next to a text match may not (ux note, section 2).
- **Robolectric is not TalkBack.** The tests model TalkBack's order from Compose's traversal data and its scroll action; only
  device walks prove the experience. Real phones and real gestures stay unproven.

## Migration

None. No stored data, no schema, no setting.
