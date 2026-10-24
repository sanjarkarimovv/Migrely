# Design

## Context

The QA tooling from `add-quality-automation` (`qa/maestro.sh`, `qa/device_scenarios.sh`, the audit) is in develop. See
proposal.md for why this change exists. Changes 3, 3a, 4 and PR #24 are merged.

## Goals / Non-Goals

**Goals:** check the time-dependent medication rules and the zone fixes black-box, and close PR #24's retest.

**Non-Goals:** overuse with several categories, the 30-day edge and per-zone dose display. They are listed in tasks.md with a
reason and a follow-up (D61).

## Decisions

### D1. Moving the clock instead of seeding the database (D60)

| Option | Verdict |
|---|---|
| `run-as … sqlite3` inserts into `medication_intake` | Fast, but tied to the schema version and skips the app's own rules (zone of the intake, linking). Kept only as a fallback |
| **Move the device clock, then log through the UI** | Black-box. It exercises the real linking and day attribution. Each logged day costs about 30 s, so the 15 days for overuse take about 9 min |

- **Base time.** The base is 10:00 on the 15th of the current month in the device's zone (`day_millis N`), so the
  days stay inside one month and away from midnight.
- **Restore.** Every scenario restores the real time afterwards (the existing loop in `device_scenarios.sh`).

### D2. Absence checks wait first

"No question" is checked only after an optional 4 s wait for the card. The card loads after home, and checking at once
could pass before the card appears.

### D3. Attack mode marker

`start-attack` and `end-attack` wait on "Details of this migraine". In landscape the message now covers "Rest now…"
(#20 fix, #26), which made `end-attack`'s "not visible" wait pass early.

## Risks / Trade-offs

- [A clock move during a running process can arrive while the app is open] → Each step closes the app (`stopApp`)
  before the clock moves, and the next flow starts with `launchApp`.
- [`med_overuse` is long] → It runs last in the default list; it can be run alone.
