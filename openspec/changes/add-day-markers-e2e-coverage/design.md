# Design

## Context

See proposal.md (Why). The QA tooling from `add-quality-automation` and `add-medication-e2e-coverage` is in develop:
- `qa/maestro.sh`, with three passes;
- `qa/device_scenarios.sh`, which moves the clock and zone (D60);
- `qa/a11y_audit.py`, which reads real pixels and the uiautomator hierarchy.

develop c8b4e7c has changes 4, 4a and 5. QA touches only `.maestro/`, `qa/` and `docs/`; no product module or layer
changes.

## Goals / Non-Goals

**Goals:**
- check day marks and the month sky black-box;
- prove D38 (one Tab stop, arrows) and D52 (the message band) with real input;
- keep the audit's keyboard rule true to D38.

**Non-Goals:**
- `month-stats` (change 5), which gets its own QA change;
- product fixes;
- release-only checks (the API 26 smoke run, monkey).

## Decisions

### D1. Real key events (D62)

| Option | Verdict |
|---|---|
| Read `focusable` in the hierarchy only (today's audit) | Rejected: D38 makes day cells reachable only by the arrows, which a hierarchy cannot show |
| Maestro `pressKey` (Tab, the D-pad keys, Enter) with `focused: true` assertions | **Chosen** for Tab, the arrows and Enter |
| adb `input keycombination` for Shift+Tab, with the focused node read from uiautomator | **Chosen** for Shift+Tab, which Maestro cannot send. It runs as a device scenario |

If Maestro cannot check the focus, every key goes through adb in a device scenario. Task 2.1 found exactly that: the
`focused` selector does not see a label that Compose keeps on a child of the focused node, so adb is used throughout.

### D2. Reduce motion is the animator scale

The app reads `Settings.Global.ANIMATOR_DURATION_SCALE == 0` ("Remove animations"). The audit and the scenarios set
`global animator_duration_scale 0`. They save the value they found and restore it on exit, like the font scale and
rotation.

### D3. The audit's keyboard rule stays

The plan was to accept day cells that are not Tab-focusable. A measurement on the device (task 5.1) showed that each
day cell is still focusable and clickable for accessibility services, as `month-sky` › "Accessibility services"
requires. D38 changes only the Tab order, and uiautomator cannot see the Tab order. So the audit's rule ("every
clickable is focusable") stays as it is and still judges the cells. The Tab order is proven by the key events (D1).

### D4. Data through the UI

- **Marks.** They are set with History's switches and the home chip.
- **Past attacks.** They are added with History's "Add for this day".
- **Clock.** Only the keyboard checks (pinned to the 24th), the earliest-month check (D51) and the travel check
  (EC-DAY-04) move it, through `qa/device_scenarios.sh`,
  with the base at the 15th at 10:00 (D60).
- **No sqlite.** No database writes are needed.

## Risks / Trade-offs

- **Key events depend on focus.** A flow that starts without focus may need one Tab first. Every key flow starts from a
  clean launch, with no focus.
- **Change 5 adds content to History and home.** Flows scroll by text, never by position. The baseline run (task 1.2)
  lists what broke.
