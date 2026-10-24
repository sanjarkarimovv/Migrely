## MODIFIED Requirements

### Requirement: Accessibility audit on real screens
The accessibility audit SHALL capture every listed screen state on the QA emulator at 100% font, at 200% font and in
landscape, and SHALL fail on any enabled text below WCAG 2.1 AA contrast (4.5:1, or 3:1 for large text), any control
smaller than 48 × 48 dp, any control without a label, and any control a keyboard cannot reach. Items of a group that is one Tab stop, such as
the month grid, SHALL still be focusable for accessibility services; their Tab order is checked by key events, not by
the audit (BRD §10, EC-UX-03, EC-UX-04, D42, D62).

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

## ADDED Requirements

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
