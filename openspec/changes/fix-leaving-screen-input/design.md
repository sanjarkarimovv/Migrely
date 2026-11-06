# Design

This records code that already exists on `bugfix/142-leaving-screen-input` at 10261340. Measurements come from the texts of
PR #143 and PR #147, and from the QA issues. Nothing was rerun for this document.

## Context

`NavHost` keeps a leaving screen composed through its exit fade. Before this work:

- that fade was the library's default of 700 ms, which nobody chose;
- a finger already down on the leaving screen still clicked when it lifted;
- a late result from the leaving screen still navigated;
- `launchSingleTop` on the editor route kept one entry with new arguments, while its form stayed on the old medication.

The person's moment is a shaky hand, or a thumb resting on the list while another finger taps. Nothing on screen tells them
that a second touch landed.

## Goals and non-goals

**Goals:** a touch or late result of a screen that has been left changes nothing. The form on screen and the row Save writes
are always the same medication. A control that answers today still answers.

**Non-goals:** see the proposal's out of scope.

## Decisions

### D1. Only the current screen navigates (BRD D154)

`BaseComposeHolder.ViewModelObserver` drops a navigation event unless its screen is the current back-stack entry. All
observers pass through this one place.

- **Compared by `NavBackStackEntry.id`, not by object.** `launchSingleTop` replaces the top entry's object and keeps its id.
- **A screen fading in is already current,** so its first tap and any event it sends on entering count.
- **An observer outside a `NavHost` has no back stack** and is not limited.

**What the check does not know.** It reads the observer's screen, not the sender's. One ViewModel serves every entry of a
destination. So an event sent by a leaving entry passes if another entry of the same destination is current. D2 closes the
touch path; a late async result in that shape stays possible.

| Alternative | Why it lost | Evidence |
|---|---|---|
| Time-based tap guard | A resting finger's click arrives up to about 700 ms later | QA's table in #138 |
| `Lifecycle.RESUMED` check | A screen fading in is only STARTED, so its first tap and events sent on entering would be refused | Mutant: 4 of 13 tests fail |
| `launchSingleTop` route by route | Keeps the stale entry with new arguments, which is #138, and misses the same-row and two-row stacks | QA |
| In the `:app` mappers | A mapper does not know which screen is on top when the event arrives. The observer knows its own screen | Code |

### D2. A touch on a screen that is not current does nothing (BRD D155)

D1 does not stop actions that do not navigate, such as Remove and Delete. So every destination in `AppNavHost` goes through
`screen()`, and `touchesOnlyWhile` consumes the pointer input while the entry is not current.

- **Read at each event,** in the initial pass. A finger that went down while the screen was current cancels instead of
  clicking.
- **Semantics are not touched, deliberately.** Screen readers and switch access keep working: a test performs the click
  action on a leaving screen and it still runs. If that click navigates, D1 drops it.
- **This window only.** Dialogs and popups are windows of their own and are not reached (#149).
- **A screen fading in stays tappable.** A plain `pointerInput` around a screen makes the whole screen a hit, and during a
  pop, where the leaving screen is on top, the screen below would get nothing. So the node intercepts out-of-bounds child
  events and its own bounds are moved one width aside, with the content moved back. This rests on
  `NodeCoordinator.speculativeHit` in Compose 1.9.3. `LeavingScreenInputTest` fails if an upgrade changes it.
- **No layout change.** `Box(propagateMinConstraints = true)` passes on the host's constraints.

The `ViewModelObserver` check stays. It covers what touch cannot: late async results, semantics clicks, and a click just
before the screen left.

**What this does not do:**

- During a pop, a tap where the leaving screen has a control or a scroll column is consumed and does not reach the screen
  below. "A screen fading in stays tappable" holds fully on a push, and on a pop only where the leaving screen takes no touch.
- A finger held through a navigation and a return inside the fade still clicks, if it lifts after the screen is current
  again.
- Two clicks before the first event navigates: the second still fires. If it navigates, D1 drops it. If it writes, it writes.
  Not reached in the walks.

### D3. The editor belongs to one id (BRD D157)

`EditMedication` runs under `key(medicationId)`, so another id starts everything it holds afresh. Save writes
`medicationId`, so the form takes only that row.

- **A read of another medication is ignored.** A `Ready` read fills the form only if its id matches and the form is empty.
- **A loaded form wins over NotFound,** because NotFound carries no id and may be another medication's.
- **Reads are collected directly, not as conflated state.** The screen asks once, so a conflated state could hide the right
  read behind a late one for good.

Limits: NotFound carrying no id means a late NotFound of another medication shows "no longer exists" until the right read
arrives. If the right read then fails, it stays, since a failed read gives only a message. A rotation during the first read
leaves the editor blank until Back (#150, open).

### D4. The restart after Delete all data (BRD D154)

`DeleteAllDataObserver` sits beside the `NavHost`, so it belongs to no back-stack entry and D1 does not limit it. It asks
where the deletion stands when composed, so a recreation or a late composition still restarts. The old place, on the delete
screen, fails 2 of 5 tests: deletion ending during the fade and after it.

### D5. One 200 ms linear fade (BRD D156)

`ScreenNavHost` sets all four transitions once. Linear, because at this length the default easing looks close to a cut.

The fade sets how long taps on a pop go dead, so this shortens #148 from 700 ms to 200 ms. It does not remove it.

The shortcut into attack mode from a light screen now changes brightness in 200 ms instead of 700 ms. That is one change,
within EC-UX-07, but it has not been judged on a device.

"Remove animations" is not handled by this code: `ScreenNavHost` does not read `rememberReduceMotion`. That no fade plays is
expected from Compose's own duration scale, and QA confirms it (task 5.3).

### D6. Kind and Category while the form opens (BRD D157)

The existing `TapGuard` covers Kind and Category only, for 700 ms from when the form appears, on Add and Edit.

- **Why these two.** A stray second tap could flip Kind to Preventive, which removes the medication from the overuse count.
  Safety ranks first.
- **Why 700 ms with a 200 ms fade.** The length follows the measured gaps of a stray tap, up to 649 ms (`TapGuard` at 500 ms
  fails at that gap), not the fade.
- **The accepted cost.** A deliberate first tap on Kind or Category within 700 ms is eaten with no feedback.
- **It starts when the form appears,** after its read. A slow read shifts it later, which only adds protection.
- **Rotation arms it again,** because its state is not saved.
- **Touch only.** It has no semantics, so screen readers, switch access and keyboards are not affected.

| Alternative | Why it lost |
|---|---|
| Guard over the whole form | Swallows the first tap on Name, Dose and Save. 3 of 8 tests fail |
| No guard, relying on the shorter fade | A stray tap at the measured gaps lands on a visible form. 3 of 8 tests fail |

With Name, Dose and Save free, a stray second tap can land there: focus, or a save with the shown values. The earlier measure
on Name was harmless, 8/8. Save was not measured.

## Edge-case checklist

| Area | Result |
|---|---|
| Empty, first use | Add form guarded the same way; a late read cannot fill it |
| Loading, failure | A slow read delays the guard. A failed read: see D3's limits |
| Full storage, offline, Premium and Free | Not touched |
| During an attack | The rules hold on every screen; the shortcut path is walked |
| Time, data, backup, upgrade | Not touched. No data or schema change |
| Undo | Delete lands on Home with Undo after two touches (#139) |
| Rotation, process death | Draft kept, Save writes the right row. Guard arms again. #150 stays open |
| Double tap | One editor, one details screen, one attack, one merge |
| Unordered async reads | D3 |
| 200% font, landscape | Walked for #138 before the fade change; re-walk owed |
| TalkBack, switch access, keyboard | Not touched by design; a test proves the click action. Not run on a device |
| Right-to-left | The shift uses absolute placement and its two moves cancel, and the app is English only (D148). Not run |
| Reduced motion | D5, QA confirms |
| Purchase | A paywall due on Home's load does not open if the user has just left Home, and nothing is marked, so the next load asks again |

## Risks

- Compose upgrade: see D2. A test fails if the hit-testing assumption changes.
- The walks in PR #147 are from before the 200 ms fade, so every gap shifts. They are history until the re-walk (task 5.2).
- Not run: R8, a real phone, a human hand, TalkBack on a device.
- Other entering screens have one-shot writes under where the previous screen's button sits. This was reported to `team-lead`
  and is not changed here.

## Migration

None.
