# Spec Delta

## Purpose
Defines how Migrely's quality is checked before anything reaches Play: an end-to-end suite traced to the specs, an
accessibility audit and device scenarios on a real emulator, robustness and privacy checks, how defects are reported
and retested, and the Go/No-Go gate every release candidate must pass (BRD §10, §14, D40–D44).

## ADDED Requirements

### Requirement: End-to-end suite on the QA device only
The end-to-end suite SHALL run only on the dedicated QA emulator and SHALL refuse any other device, because every flow
clears the app's data. Every flow SHALL start from a clean install, find controls by visible text or spoken label, and
carry at least one of the tags smoke, regression or known-bug. Each run SHALL leave a JUnit report (D40, D41).

#### Scenario: Another device is selected
- **WHEN** the suite is started with ANDROID_SERIAL set to the dev emulator emulator-5570
- **THEN** it stops before running any flow and names the QA emulator it requires

#### Scenario: Smoke run
- **WHEN** the suite is started with the smoke tag on the booted QA emulator
- **THEN** only flows tagged smoke run and a JUnit report is written for the run

#### Scenario: Control without a label
- **WHEN** a flow cannot find a control by its visible text or spoken label
- **THEN** the missing label is filed as an accessibility issue instead of adding a test-only identifier

### Requirement: Known bugs stay visible
A flow that reproduces an open defect SHALL be tagged known-bug and SHALL name the issue it reproduces. It SHALL keep
running in the regression suite, where its failure is expected, and its tag SHALL be removed together with the fix
(D41).

#### Scenario: Open defect
- **WHEN** a defect such as a double tap that starts and ends an attack is reproduced by a flow
- **THEN** the flow is tagged known-bug, names the issue, and fails in every regression run until the fix

#### Scenario: Fix merged
- **WHEN** the fix for that issue is merged and the flow passes
- **THEN** the known-bug tag is removed and the flow becomes a regression guard

### Requirement: Flows trace to spec scenarios
Every end-to-end flow SHALL name each spec scenario it checks as capability, requirement and scenario. A coverage
report SHALL count, per capability, the scenarios that have a flow, and SHALL fail when a flow names a scenario that no
longer exists in the specs (D41).

#### Scenario: Coverage report
- **WHEN** the coverage report is run
- **THEN** it lists every capability with its covered and total scenarios and the overall share

#### Scenario: Scenario renamed
- **WHEN** a scenario named by a flow is renamed or removed from the specs
- **THEN** the coverage report fails and lists the flow and the stale name

### Requirement: Accessibility audit on real screens
The accessibility audit SHALL capture every listed screen state on the QA emulator at 100% font, at 200% font and in
landscape, and SHALL fail on any enabled text below WCAG 2.1 AA contrast (4.5:1, or 3:1 for large text), any control
smaller than 48 × 48 dp, any control without a label, and any control a keyboard cannot reach (BRD §10, EC-UX-03,
EC-UX-04, D42).

#### Scenario: Low contrast
- **WHEN** a 15 sp line is drawn at 4.05:1 against its background
- **THEN** the audit fails and names the text, both colours and the ratio

#### Scenario: Not reachable by keyboard
- **WHEN** a clickable row is not keyboard-focusable
- **THEN** the audit fails and names the row by its spoken label

#### Scenario: Large font
- **WHEN** the audit runs at 200% font
- **THEN** every state is captured again at that size and checked by the same rules

### Requirement: Device settings are always restored
Every QA script that changes a device setting (font size, rotation, accessibility timeout, clock, automatic time or
time zone) SHALL restore the value it found when it exits, including when a check fails or the run is interrupted
(D42, D43).

#### Scenario: Failure in the middle
- **WHEN** a device scenario fails after moving the clock forward 73 hours
- **THEN** the clock, automatic time and time zone are back to their original values when the script exits

#### Scenario: Audit stopped
- **WHEN** the accessibility audit is stopped while the font is at 200%
- **THEN** the font scale, rotation and accessibility timeout are back to their original values

### Requirement: Device scenarios without test code
Behaviour that depends on the clock, the time zone or the system ending the app SHALL be checked by changing the QA
emulator itself between end-to-end flows, with no test-only code in the app. The scenarios SHALL cover the 72-hour
check, process death on History, the add screen and the details screen, home across midnight and travel between time
zones (BRD EC-ATK-02, EC-ATK-10, EC-TIM-02, D43).

#### Scenario: Attack left open for days
- **WHEN** an attack is started, the clock moves forward 73 hours and the app is opened
- **THEN** the app asks whether it is still going, and the answers "Still going" and "Don't ask again for this
  attack" behave as the attack-logging spec says

#### Scenario: Process death
- **WHEN** the app is ended by the system in the background while History shows last month with a day chosen, and is
  opened again from the launcher
- **THEN** the scenario checks that History shows the same month with the same day chosen

#### Scenario: Travel
- **WHEN** an attack is logged at 23:15 in Berlin and the app is opened at 02:15 the next day in Tashkent
- **THEN** the scenario checks that every part of home attributes the attack to the Berlin day

### Requirement: Robustness and privacy checks
A seeded random-input run of at least 5,000 events SHALL finish with no crash and no "app not responding" in the app's
process. A permission audit SHALL fail when the built app requests any permission outside its allowlist, and SHALL
always fail on network access (BRD §10, principle 4, D44).

#### Scenario: Random input
- **WHEN** the random-input run with a fixed seed finishes
- **THEN** it reports the seed and the number of crashes and ANRs, and fails if either is above zero

#### Scenario: Network permission
- **WHEN** a build requests android.permission.INTERNET
- **THEN** the permission audit fails and names the permission

### Requirement: Defects are reported and retested
Every defect found SHALL be filed as an issue labelled qa and with one severity from S1 critical to S4 trivial. The
issue SHALL include the steps, expected and actual results, the build, the device and evidence. QA SHALL retest each
fix on the QA emulator and record the result on the fix's pull request (D40).

#### Scenario: Defect found
- **WHEN** QA confirms a defect twice or more
- **THEN** an issue with the qa label, a severity, repro steps and measurements is filed

#### Scenario: Fix retest
- **WHEN** a pull request fixing QA issues is merged
- **THEN** QA reruns the reproducing flows and the related checks and comments the results on that pull request

### Requirement: Release Go/No-Go gate
A release candidate SHALL go to a Play track only when all of these hold:
- the build gate passes;
- the smoke suite passes on Android 8.0 and the target version;
- the full regression passes on the target version;
- the accessibility audit, the device scenarios, the random-input run and the permission audit pass;
- no S1 or S2 qa issue is open.

(BRD §14.3, D44)

#### Scenario: Open S2
- **WHEN** an S2 qa issue is open for a release candidate
- **THEN** the candidate is No-Go

#### Scenario: All checks pass
- **WHEN** every check in the gate passes for a release candidate
- **THEN** the candidate is Go for the internal testing track
