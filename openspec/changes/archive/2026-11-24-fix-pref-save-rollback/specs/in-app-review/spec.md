## ADDED Requirements

### Requirement: A rating request that is not saved still counts
When the mark of a rating request cannot be written to the phone, Migrely SHALL count that request, with its time, until
the process ends, so Home does not ask again on its next loads. The paywall's time that could not be saved SHALL keep the
rating away as a stored one does. After a restart only the stored marks SHALL count (BRD D130, D131, D197, EC-STO-13).

#### Scenario: Storage full when a rating is asked
- **WHEN** the phone's storage is full and the rating request opens
- **THEN** the next Home loads in this process do not ask again
