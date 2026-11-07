# Design

Plan only. Nothing here has been run yet. The measures come from issue #156 and QA's log `t5_write.jsonl`, on emulators with
injected taps.

## Context

`touchesOnlyWhile` (D155) consumes pointer input on a screen that is no longer current. It deliberately lets a screen fading in
take touches, because a first tap there must work. So the second tap of a double tap reaches the new screen.

Tally of `t5_write.jsonl` on 2f800c71, all routes in the log, by the gap from the lift of the first tap to the press of the
second. These counts mix layouts where a write is possible with ones where it is not, so they show a shape, not a rate:

| Gap | Wrote / trials |
|---|---|
| Under 60 ms | 1 / 20 |
| 60 to 100 ms | 17 / 29 |
| 100 to 200 ms | 40 / 51 |
| 200 to 300 ms | 42 / 51 |
| 300 to 350 ms | 0 / 1 |
| 350 to 900 ms | 58 / 65 |

Two things follow. Writes start below QA's "about 100 ms", so the rule has no lower limit. And the log has one trial between
300 and 350 ms, because an ask of 300 mostly came out under 300. The boundary has not been measured, and task 6.2 measures it.

## Goals and non-goals

**Goals:** the second tap of a double tap writes nothing on the routes in #156. A deliberate first tap always answers. #148 gets
no worse. TalkBack and switch access are not affected.

**Non-goals:** see the proposal's out of scope. Above all, a double tap slower than the timeout still writes.

## Decisions

### D1. Guard the touch, not the controls (BRD D159)

| | One by one on the write controls | The touch itself, in `screen()` |
|---|---|---|
| **Missing a route** | High. Four kinds of write control reached from six first taps today, and each new one-tap write must remember the guard. #156's table is what QA reached, and it says one route is not proved | Low. All 20 destinations go through `screen()`, and a new one is covered with no extra code |
| **Swallowing a real tap** | Higher. `TapGuard` swallows every touch on that control for its length, wherever the last tap was, and from a screen opened with no tap too. Its time starts after composition, so a slow frame moves it | Lower. It acts only if a touch has just lifted within the distance on the screen before. A tap elsewhere, a later tap and a screen opened with no tap all pass |
| **Proving it** | One test per control, and no test can notice a control that was missed | The rule is a pure function, tested at each gap. One Compose test proves it through `screen()`, and QA's table proves the routes. The negative control is one change |
| **Cost** | Small, local code, many places | One small class and one node in one place. A press near the first tap under the timeout is ignored on every screen, not only where it would write |

The cost in the last row is accepted. The second press of a double tap has no deliberate reading on any screen, and it is the
same press on a Back arrow, a text field or a buy button. Treating it as one rule is simpler than deciding screen by screen.

The existing `TapGuard` is not reused here. It needs a time that starts from the screen appearing. This rule needs a time that
starts from the finger lifting. It builds on `touchesOnlyWhile`'s node instead, and so has the same hit testing and the same
untouched semantics.

### D2. The rule

A press on the current entry is ignored, until all of its fingers lift, when all of these hold:

1. **It is close in time.** Its time minus the time of the last lift is under the timeout.
2. **It is close in place.** Its distance from the last lift is under the double-tap distance.
3. **This screen is new to the finger.** No touch has lifted on this entry yet.

| What | Value | From | Why |
|---|---|---|---|
| Timeout | 300 ms today | `LocalViewConfiguration.current.doubleTapTimeoutMillis`, which reads `android.view.ViewConfiguration.getDoubleTapTimeout()` | This is Android's own definition of a double tap. `GestureDetector` measures it from the first lift to the second press, the same gap QA measured. It agrees with QA's "about 100 to 300 ms" |
| Distance | 100 dp today | `android.view.ViewConfiguration.get(context).scaledDoubleTapSlop` | Compose's `ViewConfiguration` does not expose it, and `detectTapGestures` checks no distance for a second press. So this one comes from the platform directly |
| Lower limit | None | | Android and Compose ignore a second press under 40 ms. That is for telling a bounce from a double tap. Here a bounce must not write either, and the log shows writes at 60 to 100 ms |
| Times | `PointerInputChange.uptimeMillis` | The touch event | Not a timer and not frames: slow frames and a moved clock do not change it. A rule on the event's own time cannot be stretched by load, which `TapGuard`'s `delay` can |

**Boundary.** Exactly at the timeout the press answers. Android counts it as a double tap, but the brief says a tap from about
300 ms up must work, and one millisecond means nothing to a person.

**Why not another number.**

| Number | Why it lost |
|---|---|
| The fade, 200 ms | Writes stay at 200 to 300 ms: 42 of 51 in the log |
| 700 ms, as Kind and Category | It would swallow a deliberate tap at the same point on a visible control for most of a second. This is the shape Google rejected. Kind and Category accept that only because they take a medication out of the overuse count, and they are two controls |
| A number of our own | Nothing supports one over the platform's definition |

**Point 3 is what keeps Back out.** A forward navigation lands on a new entry, where no touch has lifted. A return lands on an
entry where one has, because the user tapped there to leave it. So no back-stack API is needed, and `popUpTo` forward routes,
such as Add past into details, are covered too. `NavController.currentBackStack` is restricted, so a comparison of stacks is
out, and comparing `previousBackStackEntry` would miss those `popUpTo` routes.

**Three taps.** The lift of an ignored press updates the time and place of the last lift, and does not mark the entry. So a
quick burst stays ignored until a pause of the timeout. Otherwise the third tap of a shaky triple tap would write.

**The whole gesture.** Once ignored, a press stays ignored until its finger lifts, tracked by pointer id, as `touchesOnlyWhile`
does. Another finger is judged by itself.

### D3. Where it lives

- A small pure class in `:app`, `ui/main`, with no Android types apart from values passed in: told about lifts, asked about
  presses. One instance for the nav host, made in `ScreenNavHost`.
- `touchesOnlyWhile`'s node asks it in the same initial pass. So the hit-testing trick and D155's rule stay in one place.
- `screen()` passes the entry id, the timeout and the distance, read in composition. The engineer picks how the instance reaches
  `screen()`, with no edit at each of the 20 destinations if possible.
- Positions are in the node's own coordinates. Both screens' nodes have the same shift and size, so they compare directly.

**What the node sees.** The node takes part only in touches that hit something inside the content. So a press on empty space on
the new screen reaches no node, and there is nothing to guard. A first tap that hit no control opened no screen.

### D4. Forward, Back, and no first tap

| Case | What happens | Why |
|---|---|---|
| Forward, second tap | Ignored under the timeout and within the distance | The bug |
| Forward, tap elsewhere or later | Answers | Must answer |
| Forward by `popUpTo`, such as Add past Save into details | As forward | The entry is new |
| Back arrow, Done, Save, `popUpTo` back to a touched screen | **This rule does nothing** | Point 3. D155 already has the leaving screen take a second tap at that point, up to the fade |
| System Back | This rule does nothing | No pointer in this window at all |
| Notification, launcher shortcut, first launch | This rule does not act | No lift in this window just before it. `ACTION_START_ATTACK` clears the stack with no touch. Attack mode's own wait (D32), if it applies there, is a separate rule and stays |
| Rotation | The instance may not survive it. If it does not, the press answers | Nothing is worth saving for 300 ms |
| Process death | Answers | Not saved |

**Back and #148.** #148's window comes from the fade, and this rule adds nothing to it, so it cannot get worse. The same point
also means Back's own residue stays: a second tap on Back after the fade lands below, which can pop twice, and a second tap on
Done or Save after the fade lands on whatever is below. Nobody has measured whether that writes. Task 6.4 measures it. A write
there gets its own issue, and it is not assumed to be safe.

### D5. TalkBack, switch access and other input

Checked against the code and the platform source, not on a device:

- **The click action of a screen reader, switch access or voice control** goes through semantics and reaches `onClick` with no
  pointer event, so this rule never sees it. PR #147's test does this on a leaving screen, and this change adds the same for a
  screen entering.
- **Hover, which TalkBack uses to explore by touch,** is not a press, and the rule acts only on presses.
- **Keyboards** send key events.
- **The node adds no semantics.** It is the same `PointerInputModifierNode`, so a test can compare the tree.
- **Gestures a service injects,** such as switch access's point scan, are real touches, and the rule applies to them in the same
  way. They would need to land under 300 ms after a lift, within the distance. Android treats them more strictly than this: for
  a generated gesture `GestureDetector` allows no distance at all. Scanning takes far longer than 300 ms, so it should not be
  reached, but this is not measured.

Not run on a device: TalkBack, switch access or a keyboard against this rule.

## Edge-case checklist

| Area | Result |
|---|---|
| Empty, first use, loading, failure | A press on a loading screen follows the same rule. Nothing is written by the rule |
| Full storage, offline | Not touched. No network and no database |
| Premium and Free | Not touched. A second tap on a paywall's buy button under the timeout does nothing |
| During an attack | Attack mode's route is in the table. Safety controls are not delayed: "Medication taken" opens on the first tap. A deliberate tap from 300 ms up answers |
| Zone, clock, midnight, DST | Event uptime, so they do not apply |
| Limits, unicode, duplicates, backup, upgrade | Not touched. No data or schema change. Nothing is kept on disk |
| Undo | Not touched. Undo under the second tap on a new screen is ignored like any other control, under the timeout |
| Deletion | Not touched. A first tap that opens a dialog, with a second tap landing on it, is not covered by this rule and is not assumed safe. It has no issue and has not been measured: `t5_write.py` has no dialog route. Task 6.8 measures it |
| Rotation, process death | D4 |
| Double tap | Between two taps with no new screen: both count. Same-screen rules D32 and D157 stay |
| Unordered async | A navigation that comes after a second press begins: D155 cancels that press |
| 200% font | Table route. The distance follows density, not font size |
| Landscape | Table route |
| Right-to-left | The distance has no direction. The app is English only (D148) |
| Reduced motion | The rule does not depend on the fade. QA runs it with animator scale 0 |
| Purchase | No purchase state touched. The buy button is covered above |

## What the check will not prove

- **A human hand.** Everything measured is one injected finger at exactly the same point, on emulators. A real double tap drifts
  and has its own rhythm. Whether real gaps fall under 300 ms is unknown. D157's stray gaps reached 649 ms in a different test,
  so a slower hand may still write.
- **How often it happens.** Nothing here measures that, and nothing here should claim a rate.
- **A real phone.** Frame timing, touch sampling and vendor gesture layers are not measured.
- **All screens.** The rule is on all 20 destinations, but QA's table covers the routes in #156. A second tap elsewhere is covered by
  the rule, not by a measure.
- **That no deliberate tap is ever lost.** A tap under 300 ms, within 100 dp of the last one, on a new screen, is ignored. It is
  believed to be rare and that is not measured. The internal-track check looks for it by feel only.
- **Rest on Compose.** The node keeps resting on Compose 1.9.3's hit testing (D155), and `LeavingScreenInputTest` guards that.

## Test plan

| Level | What | Who |
|---|---|---|
| JVM | The pure class: gaps 0, 39, 40, 60, 100, 150, 299, 300, 301 and 700 ms. Under and over the distance. A touched entry and a new one. A burst. No lift yet. Two fingers | `android-engineer` |
| Compose, Robolectric | Through real `screen()` and a real nav host, with a counter on a control under the finger. Forward, Back, elsewhere, later, a held press, a drag, a semantics click. The tree compared | `android-engineer` |
| Black box, debug | `t5_write.py`: every row of #156, with the database read. Asks that land on both sides of 300 ms | `qa-engineer` |
| Black box, R8 | `t5r8.py` extended to every kind of write in the table, read from the screen, because R8 has no database read | `qa-engineer` |
| Negative control | A build with only the guard removed, debug and R8, never pushed. The same runs must write. A JVM or Compose test must fail too | `qa-engineer`, `android-engineer` |
| #148 | `t8_148.py` with the same asks: no worse than 2f800c71 | `qa-engineer` |
| Dialogs | A double tap on each control that opens a dialog: what lands, and which have a one-tap write. A write is a new issue, and the result could change the scope | `qa-engineer` |
| Human hand | Real phone, internal track, release guide's step 2 | `team-lead`, the owner |

**Existing tests.** Some may tap the same point twice on a push under the timeout, such as tests of a first tap on a screen fading
in, in `LeavingScreenInputTest`. Such a test now describes a second tap of a double tap. Where one fails, the engineer lists it
in the packet and moves it past the distance or the timeout only if it still tests its own point. Never adjust one only to pass.

**Reading the retest.** Run as before, #156's table will still write at gaps from 300 ms up. That is the decision (`team-lead`,
2026-11-07) and not a failed fix: past the fade it is a plain tap on a visible control, and no app can tell the two apart. S2 closes
when no route writes under the timeout and the tap still answers from about 300 ms up. QA reads each trial by its measured gap, not
the ask, and cells close to 300 ms are reported and not judged. QA still judges the closure, and if it disagrees it brings evidence
to `team-lead`.

**What stays after it closes.** The remaining writes go into the 1.1.0 Go/No-Go as residual risk. If the hand check on the internal
track shows a false intake from a natural double tap at or above the timeout, it goes back to `team-lead` before Production.

## Risks

- **A deliberate tap is lost with no feedback.** Only under 300 ms and within 100 dp of the last lift, on a new screen. Google's
  rejection was about controls that do not answer, so the internal-track step looks for this. If a person hits it, it goes back
  to `team-lead`.
- **Writes remain from 300 ms up.** S2's wrong data is reduced and not removed. Intake and pain level are visible with Undo. A
  chip or a switch has no message, only the changed control. See "What stays after it closes".
- **A first tap that opens a dialog.** The dialog is its own window, so this rule cannot reach it, and the second tap may land on
  a button there. Not measured, no issue yet, and #149 is a different path: a long press through a navigation, and dialogs of a
  screen that is leaving. Task 6.8 could change the scope.
- **This branch stacks on PR #143 and PR #147.** It takes `develop` once they merge. `app-shell`'s "Touches only on the current
  screen" lives in an unarchived change, so this delta adds a requirement that says it comes first, rather than modifying that
  one. If that change is archived first, re-read its "A push" scenario and add a MODIFIED delta if it reads false.
- **A Compose upgrade** could change the hit testing. D155's test covers it, and this change's Compose test does too.
- **A second press before the entry changes** is taken by the old screen. D155 cancels it if the navigation happens before it lifts.
  A full second click before that still runs (D155 point 5).

## Migration

None. No data, no setting. To back out, remove the guard's call from the node.
