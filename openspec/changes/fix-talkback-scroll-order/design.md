# Design

Release 1.3.3 (team-lead 2026-11-19; 1.3.2 is QA-accepted and not widened), issue #255 (S3, accessibility, pre-existing on
v1.3.1 and `develop`). BRD D193, EC-UX-78 to EC-UX-84; guards D178, D181, D189, D191, D192. Root cause and prototypes:
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
changes and no touch target shrinks below 48 dp (EC-UX-82, EC-UX-83); one shared mechanism with a guard against new raw
scrolls (EC-UX-84); every check can fail.

Non-goals: reporting to Google (owner's send; note only), any visual, text or motion change, new accessibility features,
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
`LocalMinimumInteractiveComponentSize`, not this, so Material controls keep their 48 dp layout size.

Effect beyond accessibility: Compose's pointer hit-test also uses `minimumTouchTargetSize` to extend small pointer-input
nodes. Inside the page a clickable smaller than 48 dp would lose that extension. Hence D5.

### D3. TalkBack's scroll step (fixes A)

On the scroll node, `semantics { getScrollViewportLength { step } }` (public Compose API), where `step` is `ux-designer`'s
value (D4). Compose's scroll-forward/backward accessibility actions then move by `step`. This is the same action Switch
Access uses, so its scroll step changes too. Finger scroll, fling, keyboard Tab-into-view and Page Up/Down do not use it.

#### Interaction with #238's keyboard room

D189 says the room goes only with the person's scroll, TalkBack's scroll action included. A shorter step means the room may
shrink in two actions instead of one; it still shrinks only on the person's action. `KeyboardRoomTest`'s TalkBack case is a
guard and must pass unchanged.

### D4. Open design inputs: `ux-designer` decides before code (tasks 1.1, 1.2)

1. **Step size.** Proposed: half the visible part (prototype; walks clean). Alternatives: another fraction, or "scroll so
   the next item is fully in view". Consider Switch Access users (more actions per page) and 200% landscape (visible part
   61 px in the 360 dp window with the keyboard up).
2. **Chip name on the chip node (option 3).** The chip's `contentDescription` = its label and the inner Text's semantics
   cleared, so a chip cut by the edge still says its name. It matches D181 (visible words in order). It changes what Maestro
   sees (text → content description), so `qa-engineer` checks `.maestro/` selectors. If ux rejects it, ux names another way
   to meet "Every stop says its name"; if there is none, `product-manager` changes that requirement before code. No other
   requirement depends on this choice.

### D5. Touch-target audit before D2 lands on a page (team-lead condition)

Before D2 is applied to a page or dialog, a test measures every clickable node's touch bounds in it (at 100% portrait, the
smallest default case): each ≥ 48 dp, ≥ 64 dp in attack mode. Results go in `tasks.md` (task 3) and the evidence file.

- All pass: D2 lands.
- One fails: give it a 48 dp (64 dp) touch area without changing what is drawn (for example
  `minimumInteractiveComponentSize()` or padding inside its clickable bounds), measure again, and prove the screenshot
  unchanged; or leave that page out of D2, list it below, and `product-manager` records it in D193. #255 stays open there.

Pages left out of D2: none yet.

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
- **Mutations:** remove D2 → order tests red; remove D3 → scroll tests red; raw `verticalScroll` → source check red.
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
| G5 | `features/reminders/ui` | `RemindersScreen.kt:357`; dialog `:589` | dialog |
| G5 | `features/subscription/ui` | `PaywallScreen.kt:269` | |
| G5 | `features/onboarding/ui` | `OnboardingScreen.kt:180` | |
| G6 | `designsystem/picker` | `MigrelyDatePicker.kt:151`, `MigrelyDateTimePicker.kt:147, 189` | dialogs |

23 page sites, 6 dialog sites, 29 in all. Line numbers are from f70b6450; the source check is the authority.

## Risks

- **Touch area inside pages (D2).** Mitigated by D5; a missed control is a real regression for tremor or one-hand use.
- **Step size (D3).** Too small makes Switch Access and TalkBack scroll slower; too large brings A back. ux decides; tests
  pin the value.
- **Compose upgrade.** May fix B (D2 can go) or change the test data (tests fail loudly). Note for the owner: worth
  reporting upstream; the team does not send it.
- **Maestro selectors** if option 3 is chosen.
- **Robolectric is not TalkBack.** The tests model TalkBack's order from Compose's traversal data and its scroll action; only
  device walks prove the experience. Real phones and real gestures stay unproven.

## Migration

None. No stored data, no schema, no setting.
