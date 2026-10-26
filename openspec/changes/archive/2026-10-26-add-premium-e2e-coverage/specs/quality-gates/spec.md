## ADDED Requirements

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
