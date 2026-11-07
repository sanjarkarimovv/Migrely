# Proposal

## Why

A quick double tap opens a screen with the first tap, and the second tap lands on whatever control of the new screen sits
under the finger (#156, S2). Where that control writes on one tap, it writes:

- a false medication intake, which feeds the overuse count, from details, Home and attack mode;
- Remove pain level;
- a location chip, with no message;
- the "Ask if a medication helped" switch on Reminders, with no message.

QA measured this on emulators with injected taps, on `develop` and on PR #147's head, in debug and R8. How often it happens
to a person is unknown. `docs/QA.md` blocks a release on an open S2, so this ships in 1.1.0.

The fix must not bring back what Google rejected 1.0.1 for: a control that does not answer (#146).

## What Changes

- **The second tap of a double tap does not reach a new screen.** A press on a screen just entered is ignored, as a whole
  gesture, when it comes less than Android's double-tap timeout after a touch lifted on the screen before, and within
  Android's double-tap distance of that lift. Today those values are 300 ms and 100 dp. They are read from the platform.
- **Every other press answers.** A tap from the timeout up, a tap farther away, and any tap on a screen opened with no tap
  (notification, shortcut) work as before.
- **Back and other returns do not change.** A return to a screen the user has already touched is not covered, so #148 cannot
  get worse.
- **Touch only.** Semantics are not touched, so TalkBack, switch access's click action and keyboards keep working.
- **The check can fail.** With the guard removed, QA's table must write again, on debug and on R8. A person checks it by hand
  on the internal track.

No new visible text, layout, string, permission or schema.

## Out of scope

- **A double tap slower than the timeout.** From 300 ms up the second tap is a plain tap on a visible control and it still
  writes, by decision. Intake and pain level offer Undo. A slower hand is not helped by this change.
- **A second press before the navigation has happened** (D155 point 5). The first event has not navigated, so the old screen
  still takes it.
- **Dialogs, popups and other windows.** The first tap may open one and the second may land on it. This rule does not reach
  them. That path has no issue and has not been measured, so it is not assumed safe. QA measures it now (task 6.8), and the
  result could change this scope. #149 is a different path.
- **A change inside one screen,** such as Today to attack mode. D32 and the existing `TapGuard`s stay as they are.
- **Kind and Category's 700 ms** (D157) stay.
- **Fixing #148,** which stays open. Taps after Back are not changed here.
- **The one-tap write controls themselves.** Nothing asks for a confirmation or changes how a write works.
- **Measuring how often people double tap.**

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `app-shell`: which press a screen just entered takes.

## Impact

- `docs/BRD.md`: D159, EC-UX-24 to 26, a note on D155, §13 row 30.
- Code in `:app` only, beside `touchesOnlyWhile` and `screen()`. Every destination already goes through `screen()`.
- Rests on PR #143 and PR #147, which merge first. This branch then takes `develop`.
- `docs/ARCHITECTURE.md` section 8.
- No health claim changes, so `medical-reviewer` is not needed. No visible text changes, so `ux-designer` only needs to know.
- BRD ids: D159, EC-UX-24, EC-UX-25, EC-UX-26. Issue #156.
