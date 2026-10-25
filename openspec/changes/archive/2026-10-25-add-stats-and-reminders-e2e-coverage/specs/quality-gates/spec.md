## ADDED Requirements

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
