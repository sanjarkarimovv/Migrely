## ADDED Requirements

### Requirement: Restore the automatic copy
When an automatic copy exists, Your data SHALL offer "Restore the copy from <date>". It SHALL check the newest copy and
show what it holds. On confirming, it SHALL first save what the phone holds as a new automatic copy, keeping the newest
three, and then replace the records in one transaction (BRD D75, D98).

#### Scenario: Undoing a Replace
- **WHEN** the user replaced 16 migraines with a backup of 3, then restores the copy from today
- **THEN** the phone holds the 16 again, and a copy of the 3 is kept
