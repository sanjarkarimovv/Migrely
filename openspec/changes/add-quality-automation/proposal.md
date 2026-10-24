# Proposal

## Why

Until now every change was checked by walking its scenarios by hand on the dev emulator (200% font, landscape,
TalkBack labels, rotation, process death). That walk is slow, it cannot be repeated for every later change, and it
missed defects that only show up with real timing, real pixels or a real clock:
- **#12 (S2):** a double tap on "Migraine started" started and ended an attack.
- **#13 (S3):** a 4.05:1 contrast line in attack mode.
- **#14 and #17 (S3):** rows and calendar days that a keyboard could not reach.
- **#19 (S3):** a day counted twice after crossing time zones.

Play needs a release that holds up on real devices (BRD §10, §14). The project has no CI because GitHub Actions are
blocked for billing, so the checks have to run locally and be cheap enough to repeat on every change.

## What Changes

- **End-to-end suite (D41):**
  - Maestro flows in `.maestro/`, one folder per capability.
  - Every flow starts from a clean install and finds controls by visible text or spoken label, the way a person or
    TalkBack would.
  - Flows carry the tags `smoke`, `regression` and `known-bug`.
  - `qa/maestro.sh` runs them only on the QA emulator `emulator-5580` and writes JUnit reports to `build/qa/`.
- **Traceability (D41):**
  - every flow names the spec scenarios it covers in `# Covers:` lines;
  - `qa/e2e_coverage.py` reports coverage per capability and fails when a line names a scenario that no longer
    exists.
- **Accessibility audit on real pixels (D42):** `qa/a11y_audit.sh` drives every state in `.maestro/a11y-tour/` at
  100% font, 200% font and landscape. `qa/a11y_audit.py` then checks the screenshot and the accessibility tree:
  - WCAG 2.1 AA text contrast;
  - 48 dp touch targets;
  - a label on every control;
  - keyboard reach.
- **Device scenarios (D43):** `qa/device_scenarios.sh` moves the clock and the time zone, and ends the app the way
  the system does, between Maestro flows. It covers:
  - the 72-hour check (EC-ATK-02);
  - process death on History, the add screen and the details screen (EC-ATK-10);
  - home across midnight;
  - travel between zones (EC-TIM-02).

  It adds no test code to the app, and everything it changes is restored on exit.
- **Robustness and privacy checks (D44):**
  - `qa/monkey.sh` runs a seeded `monkey` stress run and fails on any crash or ANR in the app's process;
  - `qa/permission_audit.sh` fails if the built APK asks for any permission outside an allowlist, `INTERNET` first
    (BRD principle 4).
- **Release gate and bug workflow (D40, D44):** `docs/QA.md` holds:
  - the test strategy;
  - the S1–S4 severity scale (GitHub labels `qa` and `severity: …`);
  - how QA files bugs and retests fix PRs;
  - the Go/No-Go list that every release candidate must pass before a Play track (BRD §14.3).
- **Minimum Android version (app-shell):** the smoke suite also runs on an Android 8.0 (API 26) emulator,
  `emulator-5582`, the oldest version the app supports.
- **BRD:**
  - §13 row 2b;
  - §17 D40–D44.

**Out of scope:**
- **Product code.** QA files issues and the dev session fixes them (D40). Open: #19, fixed on its own branch.
- **Visual regression** (Roborazzi screenshots of the `@Preview`s, pseudo-locales `en-XA`/`ar-XB`). It needs build
  changes in `:app` and committed golden images, so it comes in its own QA change after change 3 (D42).
- **R8 release smoke and Baseline Profile/Macrobenchmark.** R8 is off until change 13 `prepare-release`. The gate in
  `docs/QA.md` lists it as a required release step.
- **Play pre-launch report and internal testing with people (BRD §14.1).** They need an uploaded AAB, which happens
  at change 13.
- **Maestro Cloud and Firebase Test Lab.** Everything runs locally and free.

## Capabilities

### New Capabilities

- `quality-gates`: how Migrely's quality is checked and enforced:
  - the end-to-end suite and its device rules;
  - traceability to specs;
  - the device accessibility audit;
  - device scenarios;
  - robustness and permission checks;
  - bug reports and fix retests;
  - the release Go/No-Go gate.

### Modified Capabilities

(none; the suite checks existing requirements and does not change them)

## Impact

- **New files:**
  - `.maestro/` (flows, subflows, accessibility tour, device steps, README);
  - `qa/` (`maestro.sh`, `e2e_coverage.py`, `a11y_audit.sh`, `a11y_audit.py`, `device_scenarios.sh`, `monkey.sh`,
    `permission_audit.sh`);
  - `docs/QA.md`.
- **Changed docs:**
  - BRD §13 (row 2b) and §17 (D40–D44);
  - `docs/ARCHITECTURE.md` §11 Testing, which points to `docs/QA.md`;
  - README, with a short "Quality checks" section.
- **Product code, build files and the gate (`./gradlew assembleDebug test ktlintCheck detekt`):** unchanged.
- **Local tools:**
  - Maestro CLI 2.11 (Homebrew) with analytics off;
  - AVDs `Migrely_QA_API_36` (port 5580) and `Migrely_QA_API_26` (port 5582).
- **BRD references:** §10 NFR (Test, Accessibility, Barqarorlik, Maxfiylik), §14.1, §14.3, EC-ATK-02, EC-ATK-10,
  EC-TIM-02, EC-UX-03, EC-UX-04, EC-UX-16, D40–D44.
