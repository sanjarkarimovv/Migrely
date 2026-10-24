## MODIFIED Requirements

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
- **WHEN** an intake is logged and the clock moves forward 1 hour, then 3 hours, then 25 hours
- **THEN** the scenario checks that "Did it help?" is absent, then shown, then absent again

#### Scenario: Overuse days
- **WHEN** painkillers are logged on 13 distinct days by moving the clock back a day at a time
- **THEN** the scenario checks that the soft warning names 13 days, and after 2 more days the doctor warning names 15

## ADDED Requirements

### Requirement: New capabilities get end-to-end coverage
Before the QA of a change that adds a capability is closed, every user-visible scenario of that capability SHALL have
an end-to-end flow or a device scenario, or SHALL be listed by the coverage report with the reason it is left to unit
tests (BRD D61).

#### Scenario: Coverage of a new capability
- **WHEN** the QA of a change that adds a capability is about to be closed
- **THEN** the coverage report lists that capability, and each of its scenarios without a flow has a reason in the
  change's tasks
