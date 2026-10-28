# quality-gates Specification

## Purpose
Defines how Migrely's quality is checked before anything reaches Play: an end-to-end suite traced to the specs, an
accessibility audit and device scenarios on a real emulator, robustness and privacy checks, how defects are reported
and retested, and the Go/No-Go gate every release candidate must pass (BRD §10, §14, D40–D44).

## Requirements

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
smaller than 48 × 48 dp, any control without a label, and any control that accessibility services cannot focus. The
Tab order is checked by key events instead (BRD §10, EC-UX-03, EC-UX-04, D42, D62).

#### Scenario: Low contrast
- **WHEN** a 15 sp line is drawn at 4.05:1 against its background
- **THEN** the audit fails and names the text, both colours and the ratio

#### Scenario: Not reachable by keyboard
- **WHEN** a clickable row is not keyboard-focusable
- **THEN** the audit fails and names the row by its spoken label

#### Scenario: Large font
- **WHEN** the audit runs at 200% font
- **THEN** every state is captured again at that size and checked by the same rules

#### Scenario: Month grid as one stop
- **WHEN** the audit captures home or History with the month grid
- **THEN** it checks each day cell like any other control, so a day cell that is not focusable for accessibility
  services fails the audit

### Requirement: Device settings are always restored
Every QA script that changes a device setting (font size, rotation, accessibility timeout, animator scale, app language,
clock, automatic time or time zone) SHALL restore the value it found when it exits, including when a check fails or the run is interrupted
(D42, D43).

#### Scenario: Failure in the middle
- **WHEN** a device scenario fails after moving the clock forward 73 hours
- **THEN** the clock, automatic time and time zone are back to their original values when the script exits

#### Scenario: Audit stopped
- **WHEN** the accessibility audit is stopped while the font is at 200%
- **THEN** the font scale, rotation and accessibility timeout are back to their original values

#### Scenario: Reduce motion stopped
- **WHEN** a check is stopped while the animator scale is 0
- **THEN** the animator scale is back to its original value

#### Scenario: A restore that does not take
- **WHEN** a script restores a setting but the device keeps another value, for example because the emulator stopped
  answering
- **THEN** the script reads the value back and reports the setting, the value it wanted and the value it found

#### Scenario: Leftover setting at the start
- **WHEN** a script starts while a setting differs from the QA emulator's baseline, for example auto-rotate off after a
  crashed run
- **THEN** it warns about that setting, and `qa/reset_device.sh` puts the baseline back

### Requirement: Device scenarios without test code
Behaviour that depends on the clock, the time zone, exact tap timing, the system ending the app or an app update
SHALL be checked by changing the QA emulator itself between end-to-end flows, with no test-only code in the app. This
covers the 72-hour check, process death, midnight, travel and zone changes while the app runs, double taps, an update
over an older build, and medication timing (BRD EC-ATK-02, EC-ATK-10, EC-TIM-02, EC-STO-01, MED-F4, MED-F5, D43, D60).

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

#### Scenario: Double tap with an exact gap
- **WHEN** "Migraine started" is tapped twice 0.15, 0.4 and 0.7 seconds apart, in its lower and in its upper part
- **THEN** the scenario checks that exactly one attack is in progress and attack mode is shown each time

#### Scenario: Update over an older build
- **WHEN** attacks with pain levels and symptoms are logged on an older build and the newer build is installed over it
- **THEN** the scenario checks that every attack, pain level and symptom is still there

#### Scenario: Medication timing
- **WHEN** one intake is checked 1 hour and 3 hours after it is logged, and another one 23 hours and 25 hours after it
  is logged
- **THEN** the scenario checks that "Did it help?" is absent at 1 hour, shown at 3 and 23 hours, and absent at 25 hours

#### Scenario: Overuse days
- **WHEN** painkillers are logged through the UI on 13 distinct days, with the clock set to each day in turn
- **THEN** the scenario checks that the soft warning names 13 days, and after 2 more days the doctor warning names 15

#### Scenario: Day mark after travel
- **WHEN** a period day is marked in Tashkent and the phone then moves to Berlin and to Pago Pago
- **THEN** the scenario checks that the same calendar date is still marked as a period day in each zone (EC-DAY-04)

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
- the build gate passes with no Android Lint error;
- the instrumented tests pass on the QA emulator;
- smoke passes on Android 8.0, the target version, and the split APKs Play builds for `docs/QA.md`'s device profiles;
- the full regression passes on the target version;
- the accessibility audit, device scenarios, random-input run and permission audit pass;
- no S1 or S2 qa issue is open.

(BRD §14.3, D44, D204)

#### Scenario: Open S2
- **WHEN** an S2 qa issue is open for a release candidate
- **THEN** the candidate is No-Go

#### Scenario: All checks pass
- **WHEN** every check in the gate passes for a release candidate
- **THEN** the candidate is Go for the internal testing track

#### Scenario: A lint error
- **WHEN** Android Lint reports an error on a release candidate
- **THEN** the candidate is No-Go until the error is fixed. A lint baseline never holds an error. A candidate checked
  before this rule existed records each error as a stated exception: why it cannot reach users, and the issue that
  fixes it

#### Scenario: The APKs users install
- **WHEN** the candidate's bundle is turned into split APKs for a device profile and installed
- **THEN** the smoke suite passes on them. In the profile's language, read back from the device, the dates and
  pickers stay in English with the region's date order and first weekday (D117)

### Requirement: New capabilities get end-to-end coverage
Before the QA of a change that adds a capability is closed, every user-visible scenario of that capability SHALL have
an end-to-end flow or a device scenario, or SHALL be listed in that QA change's tasks with the reason it has none and
the follow-up (BRD D61).

#### Scenario: Coverage of a new capability
- **WHEN** the QA of a change that adds a capability is about to be closed
- **THEN** the coverage report lists that capability, and each of its scenarios without a flow is listed in the
  change's tasks with a reason and a follow-up

### Requirement: Keyboard paths are checked with key events
Keyboard behaviour SHALL be checked end to end with real key events (Tab, Shift+Tab, the arrow keys and Enter) sent to
the QA emulator, not only by reading which nodes are focusable (BRD D62).

#### Scenario: One Tab stop for the month
- **WHEN** home is open with no attack in progress and Tab is pressed from launch
- **THEN** the check confirms that focus goes "Period", the month title, today's cell, then "Migraine started", and
  that Shift+Tab goes back the same way

#### Scenario: Arrows inside the month
- **WHEN** a day of the month has focus and the arrow keys are pressed
- **THEN** the check confirms that focus moves by one day and by one week, and stays on today when the next day is in
  the future

#### Scenario: Enter on a day
- **WHEN** a past day has keyboard focus on home and Enter is pressed
- **THEN** the check confirms that History opens with that day chosen, as a tap does

### Requirement: Verification depth per change and per cycle
Each QA change SHALL run its new and changed flows three times, the device scenarios it affects twice, and the smoke
flows. The full regression, every device scenario, the accessibility audit and the random-input run SHALL run at the
end of each QA cycle and before every release candidate. Every BRD edge case of the dev changes under test SHALL have a
flow, a device scenario, or a reason in the QA change's tasks (BRD D63, D61).

#### Scenario: A QA change in the middle of a cycle
- **WHEN** a QA change adds flows for one capability
- **THEN** those flows pass three times, the affected device scenarios pass twice and the smoke flows pass, before the
  change is merged

#### Scenario: The end of a QA cycle
- **WHEN** the last QA change of a cycle is about to be merged
- **THEN** the full regression, every device scenario, the audit and the random-input run have passed on its base

### Requirement: Notifications are checked black-box
Notifications SHALL be checked on the QA emulator without test code in the app. The device clock is set just before the
planned time, the app re-plans when it starts, the planned work is run on demand, the notification is read from the
notification service, and its actions are tapped in the open shade. The notification permission SHALL be granted and
reset through the package manager and refused with a real tap in the system dialog, and a reboot SHALL be a real
restart of the emulator (BRD D64).

#### Scenario: Daily reminder on demand
- **WHEN** the daily reminder is on and the clock is set two minutes before its time, the app is opened and the planned
  work is run on demand
- **THEN** the check finds "How was today?" in the shade with "Pain-free day" and "Log a migraine"

#### Scenario: Refused permission
- **WHEN** the notification permission is reset and the user turns a reminder on and taps "Don’t allow"
- **THEN** the check finds that the switch stays off and "Open settings" is shown

#### Scenario: After a reboot
- **WHEN** the daily reminder is on and the emulator restarts
- **THEN** the check finds the daily work planned again for the next reminder time, before the app is opened

### Requirement: Files through the system picker are checked by their bytes
Files the app writes or reads through the system file picker SHALL be checked on the QA emulator without test code in
the app. Exported files SHALL be pulled from the device and checked byte by byte against the format the specs
describe, and files to import SHALL be prepared by the QA tools and pushed to the device's Download folder, including
broken files and files from a newer version (BRD D200).

#### Scenario: An exported CSV
- **WHEN** a CSV export is saved to Download and pulled from the device
- **THEN** the check confirms the byte order mark, CRLF line ends, the header, the separator for the app's language, and
  a `'` before a note that starts with `=`, `+`, `-` or `@`

#### Scenario: A prepared broken file
- **WHEN** QA pushes a file that is not a Migrely backup and imports it
- **THEN** the check confirms "This file isn't a Migrely backup. Nothing was changed." and that every entry is still
  there

### Requirement: Premium is checked through the simulated store
Paid features SHALL be checked on the QA emulator with the debug build's simulated store, without test code in the
app. QA SHALL set the store's answer from adb and buy through the app's own paywall and the simulated purchase sheet.
Rules that depend on time SHALL be checked with the emulator's clock. What only Google Play can show SHALL be checked on
a real phone through Play's internal track (BRD D201).

#### Scenario: A purchase from the paywall
- **WHEN** a free user opens the doctor report, chooses the monthly plan and completes the simulated purchase
- **THEN** the check confirms the report screen opens and Settings shows "Premium · monthly"

#### Scenario: Offline for too long
- **WHEN** the subscription was last confirmed 4 days ago and the store is unreachable
- **THEN** the check confirms the premium features are locked again, and every entry is still there

### Requirement: Generated reports are checked by their text
A PDF the app generates SHALL be pulled from the app's cache on the QA emulator and checked by its text and page
count against data with known numbers. The data SHALL be prepared by a QA tool as a backup file and imported through
the app (BRD D202).

#### Scenario: A three-month report
- **WHEN** a backup with 18 migraine days in 92 days, 4 of them at "Bed rest", is imported and a 3-month report is made
- **THEN** the check finds "5.9" migraine days a month and 4 "Bed rest" attacks in the PDF's text, and "Patient-entered
  data. Not a diagnosis." on every page

### Requirement: Every screen is in the accessibility audit
The accessibility audit's tour SHALL hold at least one state for every screen the app shows, and a change that adds a
screen SHALL add its state. A screen behind Premium SHALL be reached through the simulated store (D201). A failure
SHALL be filed as an `accessibility` issue, never worked around in the audit (BRD §10, EC-UX-03, EC-UX-04, D42, D203).

#### Scenario: The screens of 1.0.0
- **WHEN** the audit runs on 1.0.0
- **THEN** it captures home, attack mode, History, attack details, the past-migraine screen, onboarding, Settings and
  its pages, My medications, Reminders, Your data, the paywall, the doctor report and Insights

#### Scenario: The premium screens
- **WHEN** the audit runs
- **THEN** the paywall, the doctor report's period screen and Insights are each captured at 100% font, at 200% font and
  in landscape, and checked by the same rules as the other states

#### Scenario: A screen without a state
- **WHEN** a change adds a screen and the tour has no state for it
- **THEN** QA's review of the change finds the missing state, and the change does not pass verification until the state
  is added

### Requirement: A test strategy names the risk left
QA SHALL keep a test strategy that names each test level with its tool, its owner and when it runs. Its risk register
SHALL hold every capability of the specs, with:
- its checks at each level;
- the scenarios that have no check at any level;
- the follow-up that closes them.
A QA change that is deferred SHALL stay in the register as open until it is done (D204).

#### Scenario: A capability with few checks
- **WHEN** under half of a capability's scenarios have an end-to-end flow or a device scenario
- **THEN** the register names the capability as a risk, with the levels that do check it and the follow-up

#### Scenario: A deferred QA change
- **WHEN** a QA change named by a BRD decision is not done, such as D42's visual regression
- **THEN** the register lists it as open, and every Go/No-Go record lists it among the residual risks

### Requirement: The Go/No-Go record names the risk left
The Go/No-Go record SHALL state what each check does not prove. It SHALL also list the residual risks:
- the capabilities with under half of their scenarios checked;
- the deferred QA changes;
- the checks that could not run, with the reason.

(D204)

#### Scenario: Residual risk in the record
- **WHEN** QA writes the Go/No-Go record of a release candidate
- **THEN** the record names, next to its verdict, each capability with under half of its scenarios checked, each
  deferred QA change and each check that could not run
