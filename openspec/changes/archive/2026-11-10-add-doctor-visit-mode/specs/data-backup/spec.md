## MODIFIED Requirements

### Requirement: JSON backup
"Export a backup (JSON)" SHALL write every health record to a file named `migrely-YYYY-MM-DD.json`: attacks with their
pain points, tags, functional impact and note, custom tags, medications (archived ones too), intakes, day marks and
doctor visits. The file SHALL carry `format: "migrely-backup"` and its `schemaVersion`, 7 from 1.2.0. It SHALL NOT
include reminder settings, a reminder's shown state, overuse acknowledgements, or an attack being deleted (BRD BAK-F1,
D71, D168).

#### Scenario: Exporting
- **WHEN** the user exports with 16 attacks, 2 medications and 5 day marks
- **THEN** the file holds 16 attacks, 2 medications and 5 day marks, and Migrely says "Backup saved."

#### Scenario: Exporting visits
- **WHEN** the user exports with a last visit on August 20 and a next visit on November 20
- **THEN** the file has `schemaVersion` 7 and `doctorVisits` with both dates, and no reminder state

## ADDED Requirements

### Requirement: Doctor visits on import
A backup without `doctorVisits`, from schema 6 or older, SHALL import as before with no visit. "Replace" SHALL keep
the file's visits only. "Add" SHALL skip a visit with the same id or date, and SHALL add the file's upcoming visit
only when the phone has none after today. After an import the reminder SHALL be planned again, an imported visit
already too close counting as reminded (BRD EC-BAK-03, EC-BAK-06, EC-REM-08, D168).

#### Scenario: An older file
- **WHEN** the user imports a schema 6 backup of 10 attacks
- **THEN** the 10 attacks are imported, no error shows, and the phone's visits are as before with "Add"

#### Scenario: Add keeps one next visit
- **WHEN** the phone's next visit is November 20 and the user adds a backup whose next visit is December 3
- **THEN** the next visit stays November 20, and the backup's past visits are added

#### Scenario: Replace
- **WHEN** the phone's next visit is November 20 and the user replaces with a backup whose next visit is December 3
- **THEN** the next visit is December 3, and its reminder is planned for about 10:00 on December 1
