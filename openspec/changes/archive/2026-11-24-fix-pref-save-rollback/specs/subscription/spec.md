## ADDED Requirements

### Requirement: A paywall mark that is not saved still counts
When the mark that a paywall showed by itself cannot be written to the phone, Migrely SHALL count that paywall as shown,
with its time, until the process ends, so it does not open again on the next Home load. After a restart only the stored
marks SHALL count (BRD D110, D197, EC-STO-13).

#### Scenario: Storage full when a paywall shows by itself
- **WHEN** the phone's storage is full and the fifth-migraine paywall shows and is closed
- **THEN** the next Home loads do not open it again in this process

#### Scenario: After a restart with space freed
- **WHEN** the app starts again after the storage was freed
- **THEN** the paywall follows its usual rules, as the mark was never stored
