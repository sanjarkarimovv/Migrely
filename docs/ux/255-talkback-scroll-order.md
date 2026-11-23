# #255: TalkBack scroll step, chip names, drawn (ux-designer decisions)

Change `fix-talkback-scroll-order`, tasks 1.1 to 1.3; BRD D193 (5), (2), (4); design D3, D4.1, D4.2, D5. Base
`bugfix/255-details-talkback-order` dec48ab4. Read-only work: code, eng-255's walks and logs
(`migrely-work/255/`), Compose 1.9.3 sources and TalkBack 16 sources kept there. No device was used.

The person: TalkBack on, one hand, dark room, eyes mostly closed, thinking slowly. A swipe must land on the next thing,
say what it is, and keep the place. Switch Access users use the same scroll action.

## 1. Scroll step (task 1.1)

**Decision: one accessibility scroll moves the page by half its visible part, `floor(V / 2)` px.** V is the visible
part of the scroll area: its height minus anything over it (top bar, message, keyboard, navigation bar, a dialog's
buttons). Where `scrollState.viewportSize` already ends at those edges (Attack details: insets, message clearance and
`TypingSpace` sit before `verticalScroll`), V = `viewportSize`. Where a page puts an inset inside the scroll content, the
step uses the visible part, not `viewportSize`. Less than one step left: Compose stops at the end, as now.

**Why half.**
- TalkBack 16 keeps its place only while the item it is on stays on screen. After a scroll it searches from that item
  and, if the next item is not yet in view, scrolls again in the same swipe, up to 100 times
  (`FPLN.java` 2733-2810, `MAX_MULTIPLE_AUTO_SCROLL_ATTEMPT`). If the item has left, it falls back to the first or last
  item in view. A step is safe when some multiple of it lands while both the focused item and the next one are in view.
  That holds for any step smaller than V − g, where g is the gap to the next stop. With half the visible part this means
  **g < V / 2**. The app's largest regular gap is the 28 dp section spacing (73.5 px at density 2.625).
- Half keeps half of what the person just saw on screen. A sighted Switch Access user, or a low-vision TalkBack user
  who glances at the screen, sees where they were. A whole-page jump replaces the whole screen at once, the kind of sudden
  full-frame change the migraine-safe rule avoids. The scroll stays eased: Compose animates it with its spring
  (`Scrollable.kt` 1049, `semanticsScrollBy`), over half the distance, and runs it instantly under Remove animations.
- It is the only value walked on a device: prototype 2 went from Delete to Back with all 55 stops once, and from Notes to
  Back with the keyboard up.

**Cost, accepted.** Switch Access needs about twice as many scroll actions on a long page (Attack details at 100%
portrait: about 4 instead of 2). Keeping the place is worth more to a slow, foggy mind than the extra actions. Rejected:
"scroll until the next item is fully in view", because the scroll action cannot know which item TalkBack will pick next,
and TalkBack already shows the item it lands on (ACTION_SHOW_ON_SCREEN). Also rejected: 2/3 or a whole page, because
neither was walked and both drop the place in short windows.

**Checked against the cases (density 2.625):**

| Case | V | Step | g < V/2? | What happens to the focused item |
|---|---|---|---|---|
| 100% portrait, 411 dp, nav bar, no keyboard (scroll area 296-2337 px) | 2041 px (777 dp) | 1020 px | yes, 73.5 ≪ 1020 | The item at the bottom edge moves to mid-screen, still whole and in the tree. The next item is in view. TalkBack goes on from it. |
| 200% landscape, 360 dp, keyboard up, Attack details on `develop` (top bar aside, D192: 61 + 194 px) | 255 px (97 dp) | 127 px | yes, 73.5 < 127 | A 64 dp chip (168 px) at the bottom: after one step its bottom is at 128 px and 40 px of its top are hidden. It is partly on screen, so it stays in the tree. The next chip (gap 21 px) or the next section title (gap 73.5 px) is in view, and TalkBack focuses it and shows it whole. |
| The 61 px visible part from the brief (the same window **before** #245; not reachable on Attack details now that the bar steps aside) | 61 px (23 dp) | 30 px | **no**, 73.5 > 61 | Between chips (gap 21 px) the place holds: the focused chip is taller than the window and stays in it while the next one enters. At a section gap no step of any size can keep both on screen, because the gap is taller than the window. The focused item leaves first. In about 58% of cases the next title is already in view and TalkBack's fallback lands on it correctly. Otherwise nothing is in view and that swipe is silent. This is why D192 widens the window; the step cannot fix it. |
| 200% landscape, 411 dp, keyboard up, Attack details (bar not aside, H 137 px) | 137 px (52 dp) | 68 px | **no, narrowly** (73.5 > 68.5) | At a section gap the focused chip can leave the screen (about 7% of crossings). The next title is always in view then, so TalkBack's fallback still lands on the next item in order. No skip and no loop, but the item does leave the screen. |

**Finding for product-manager (not a blocker):** the requirement "TalkBack's scroll keeps the focused item on screen"
cannot hold literally when the gap to the next stop is half the visible part or more. That happens in the 411 dp
landscape keyboard case above, and wherever a page has a large non-focusable block between stops (for example
illustrations on Today, Paywall and Onboarding at 200% landscape). Suggested wording: "the focused item stays on screen
while the gap to the next stop is under half the visible part; otherwise the next stop is still the next item in order".
The order walk is the binding check either way. Engineer: the scroll tests for those pages and the 411 dp landscape
keyboard case should assert the order outcome, not "pivot stayed", or PM must accept the narrower sentence first.

**Unsure: Robolectric does not measure V on devices. The engineer measures it in task 2.3 (b) and the task 6.1 walks:
one scroll moves by V/2 within 1 px on every site, with V read from the device and not assumed.**

## 2. Chip names (task 1.2)

**Decision: option 3, yes.** On `ChoiceChip` (`AttackChoiceSections.kt:204-262`) set
`contentDescription = spokenLabel ?: label` on the chip's clickable node and clear the inner `Text`'s semantics. This is
exactly prototype 1's diff (`migrely-work/255/prototype/full-prototype.diff`). The app already uses the same pattern for
one spoken name (D181 (2)): `PeriodToday` (`TodayContents.kt:347-358`) and the attack-mode Details and move-start buttons
(`AttackModeContents.kt:291-300, 340-350`).

**Why it works, and why it failed alone in prototype 1.** Today the chip's name lives on a child TextView. TalkBack
appends it to the chip's state only if that child is in the tree (`walk-q1.log`: `getAppendedTreeDescription ...
{Nausea}`). The child sits 12-14 dp inside the chip's touch bounds. With exact bounds (D2), a chip with 1 px in view is a
stop, but its text child can be out of the tree. That is the one "Not selected" in prototype 2's walk ("Back of the head",
`walk-q1.txt` line 36). Under option 3, Compose moves the description to a fake child (`SemanticsNode.kt:461-474`).
That fake child is always kept with its parent and gets the parent's own clipped bounds (`SemanticsOwner.kt`, the
`isFake` branch). So whenever the chip is a stop, its name is in the tree and visible.
Prototype 1 still failed because it had no D2: its nameless chips were wholly hidden, in the 24 dp band, with empty
bounds, so their fake child was invisible too (`walk-p1.log`: `isContentDescriptionEmpty=true` on the parent, with
`content="Nausea"` on a child). **Option 3 needs D2. Neither works alone.**

**Spoken form (TalkBack 16.0.0, default verbosity, as eng-255's walks log it).** The device puts the **state first**:
`descriptionOrder=1` in the logs means state, name, role. The brief said "name, state, role", but the logs show state
first, so this note follows the logs:

| Chip | Heard | Hint after a pause |
|---|---|---|
| Unselected | "Not selected. Nausea" | "Double-tap to toggle" |
| Selected | "Selected. Nausea" | "Double-tap to toggle" |
| Add chip (unchanged) | "Add your own symptom. Button" | "Double-tap to activate" |
| User's own tag | "Not selected. <tag>" plus TalkBack's actions notice for "Remove from my list" (unchanged) | |

This is word for word what is heard today for a chip in full view (`walk-p1.txt` against `walk-q1.txt`), so nothing
changes in what people hear. If a person changes TalkBack's element order setting, TalkBack reorders the same parts. A
cut chip now says the same as a whole chip.

**The same rule for every stop (EC-UX-80 says "every item").** Any stop in a scrolling page or dialog whose name comes
only from a child `Text` inside padding loses its name the same way when cut. A cut M3 button says only "Button"; a
cut radio or switch row says only its state. The rule: **every such stop carries its visible words, in visual order, on
its own node** (`contentDescription` = the same string resource, inner `Text` semantics cleared). Stops that are their
own text (headings, plain text, text fields) need nothing. Rather than editing every button blindly, the engineer adds a
**cut sweep** to the shared fixture. For each stop on each page, scroll so that 1 px to 12 dp of the stop shows at the
top edge and at the bottom edge. Then read its name from the `AccessibilityNodeInfo` tree as TalkBack builds it: the
node's own text or description, then visible non-focusable children. Fix what comes back empty or partial.
**The current `talkBackName()` (`TalkBackTree.kt:76`) reads merged semantics, which always contain the clipped text, so
it cannot fail this way.** Negative control: a chip without option 3 under D2 must be red. This widens task 4's diff
beyond chips. Product-manager may want it named in tasks 4.1 to 4.6.

**Maestro (task 8.4, qa-engineer).** On Android, Maestro's `text` selector also matches content-desc, so
`tapOn: "Nausea"` still finds the chip. It now matches the fake child: a View with content-desc instead of a TextView
with text, and the same bounds as the chip, so the tap toggles. Check selectors that need a TextView, `childOf` or
`containsChild` chains, `index:`, and `selected:` or `checked:` traits next to a text match. Likely flows:
`.maestro/attack-details/`, `custom-tags/`, `attack-logging/`, `past-attacks/`, `a11y-tour/custom-tag-dialog.yaml`,
`device/details-*.yaml`. If the rule reaches buttons and rows, every page's flows are in scope.

**Unsure: option 3 together with D2 has not been walked on a device. The engineer proves it in task 6.1 with a walk where
a chip is cut at an edge, and QA again in 8.2. Voice Access ("Tap Nausea") is not checked; QA covers the spec's
Voice Access scenario.**

## 3. Drawn (task 1.3)

**Decision: nothing drawn changes (D193 (2)). Confirmed by design:**
- D2 changes one `ViewConfiguration` value. Compose reads it for accessibility bounds and for widening small pointer
  targets. Neither draws. Material3's `minimumInteractiveComponentSize` reads `LocalMinimumInteractiveComponentSize`, so
  Material controls keep their 48 dp layout.
- D3 changes only how far an accessibility scroll moves. The scroll is still eased, over a shorter distance.
- The naming rule is semantics only: `contentDescription` plus `clearAndSetSemantics {}` change no layout or pixels.
- Scope: D2 wraps only the scroll content. The top bar, the message host (its 64 dp dark-room Undo and Dismiss, D182 (4)),
  bottom bars and dialog buttons stay outside it. The prototype wrapped the whole `ReadyContents` Box; keep the wrapper at
  the scroll content.
- TalkBack's green focus box is drawn by TalkBack, not the app. Where a touch area grows (below), that box grows with it.
  That is acceptable and not part of the screenshots.

**The 64 dp bar in attack mode** is what the attack-mode spec names: the intensity choices, "Migraine ended", and a
message's Undo and Dismiss. The dim secondary buttons (Details, move-start options, long-check buttons;
`AttackModeContents.kt:291, 340, 493`) are 48 dp by design (D24). They pass at 48 dp and must **not** be enlarged, which
would be a drawn change, and attack mode must not be left out because of them.

**Pages where the touch-target audit (task 3) may force a touch-area change ux must see:**

| # | Where | Why it may fail | ux decision if it fails |
|---|---|---|---|
| T1 | History month calendar day cells: `designsystem/.../sky/MonthSkyCalendar.kt:320-345` (`clickable` at 338, `CellSpacing` 6 dp at 73), inside History's scroll via `HistoryCalendar.kt:58` and `MonthSkyCard.kt:109` | At **360 dp wide** each cell is about 41.7 dp with 6 dp gaps. Today Compose's widening covers the gaps. Under D2 a tap in a gap hits nothing. At 411 dp wide cells are about 49 dp, **so an audit at 411 dp alone misses this. Measure at 360 dp wide.** | Give each cell a touch area of its whole pitch (cell plus half the gap on each side, about 47.7 dp at 360 dp). Keep the drawn cell, its clip and the keyboard focus ring on the inner cell. Seven 48 dp targets cannot fit in 328 dp, so the pitch is also today's real target. Screenshots stay identical. Do not leave History out of D2: its 3 scroll sites would keep the loop. |
| T2 | Date picker: `designsystem/.../picker/MigrelyDatePicker.kt:151`, `MigrelyDateTimePicker.kt:147` (Material3 `DatePicker`: day cells, year grid, month arrows) | The D2 local reaches into Material's internals. A Material control that depends on Compose's widening rather than a layout minimum would shrink. Not determined from code. | If any Material control there measures under 48 dp, leave that picker out of D2 (D5) and record it. Touch wins over the edge band here: the date picker has a text-entry mode TalkBack users can use, while a tremor has no other path. D3 still applies. |
| T3 | Time picker: `MigrelyDateTimePicker.kt:189` and the Reminders dialog `RemindersScreen.kt:589` (Material3 `TimePicker`, vertical) | Same as T2: clock numbers, hour and minute boxes, AM/PM. The dial is one large pointer area, so this is probably fine. | Same as T2. |
| T4 | Attack mode, `AttackModeContents.kt:132, 170, 194` | The 64 dp controls are `heightIn(min = 64.dp)` or larger (`:214, :233, :417`), a layout size, so D2 cannot shrink them. Listed only so the audit applies the right bar (see above). | No change expected. |

All other raw `clickable`, `selectable` and `toggleable` sites in the 29 scroll sites already have a layout minimum of 48
dp or more (rows `heightIn` 48/56/64 dp, chips 48 dp). Material buttons, chips and icon buttons keep 48 dp through
`minimumInteractiveComponentSize`. **Unsure: widths were not measured. Task 3 measures width and height, at 360 and 411
dp wide, and at 200% font.**

## Verify

- 1.1: section 1. 1.2: section 2 ("Chip names", spoken forms). 1.3: section 3 ("Drawn").
- `git log --oneline -1 -- docs/ux/255-talkback-scroll-order.md`
