# data-backup Specification

## Purpose
Defines how the user keeps and moves their Migrely diary: a JSON backup to restore from, a CSV to read in a
spreadsheet, a safe import, and Android's own backup, all without a network or storage permission (BRD BAK-F1–F5,
EC-BAK-01–04, D71–D74).

## Requirements

### Requirement: Your data
Settings SHALL list "Your data". It SHALL open a screen with "Export a backup (JSON)", "Export for Excel (CSV)" and
"Import a backup". Files SHALL be chosen through the system's file picker, and no storage permission SHALL be asked for
(BRD BAK-F5, D70).

#### Scenario: Opening Your data
- **WHEN** the user opens Settings and taps "Your data"
- **THEN** the three actions show

### Requirement: JSON backup
"Export a backup (JSON)" SHALL write every health record to a file named `migrely-YYYY-MM-DD.json`: attacks with their
pain points, tags, functional impact and note, custom tags, medications (archived ones too), intakes and day marks.
The file SHALL carry `format: "migrely-backup"` and its `schemaVersion`. It SHALL NOT include reminder settings,
overuse acknowledgements, or an attack being deleted (BRD BAK-F1, D71).

#### Scenario: Exporting
- **WHEN** the user exports with 16 attacks, 2 medications and 5 day marks
- **THEN** the file holds 16 attacks, 2 medications and 5 day marks, and Migrely says "Backup saved."

### Requirement: CSV export
"Export for Excel (CSV)" SHALL write one row per attack, with its start and end in the attack's local time, duration
in minutes, zone, peak pain and level, medications, triggers, symptoms, functional impact and note. The file SHALL be
UTF-8 with a byte-order mark, named `migrely-attacks-YYYY-MM-DD.csv` (BRD BAK-F2, D72).

#### Scenario: Opening in Excel
- **WHEN** the CSV is opened in a spreadsheet
- **THEN** each attack is one row, and names with non-Latin letters read correctly

#### Scenario: A phone in Uzbek or Russian
- **WHEN** the phone's language writes decimals with a comma
- **THEN** the columns are separated by semicolons, as a spreadsheet there expects

#### Scenario: A note that looks like a formula
- **WHEN** a note starts with "="
- **THEN** the cell shows the note as text and runs nothing

### Requirement: Import shows what it brings
"Import a backup" SHALL first read the file and show how many attacks, medications and day marks it holds, before
anything on the phone changes. The import SHALL then be applied in one transaction, so either all of it is saved or
none (BRD BAK-F3, D73).

#### Scenario: Before importing
- **WHEN** the user picks a backup of 10 attacks, 2 medications and 5 day marks
- **THEN** Migrely shows those numbers and asks how to import, and nothing has changed yet

### Requirement: Replace or add
With records already on the phone, the import SHALL offer "Replace", which first saves an automatic backup inside the
app and keeps the last 3, and "Add", which skips records already present and links a medication whose name already
exists to that one (BRD BAK-F3, EC-BAK-04, EC-MED-07, D73).

#### Scenario: Replace
- **WHEN** the user imports a backup of 10 attacks over 16 and chooses "Replace"
- **THEN** an automatic backup of the 16 is saved first, and the phone then holds the 10

#### Scenario: Add
- **WHEN** the user adds a backup that shares 4 attacks with the phone
- **THEN** those 4 are skipped and the others are added

#### Scenario: The same attack logged on two phones
- **WHEN** the user adds a backup holding an attack that overlaps one on the phone, under another ID
- **THEN** it is skipped, so attacks never overlap

#### Scenario: A medication by the same name
- **WHEN** the user adds a backup whose "ibuprofen" has another ID than the phone's "Ibuprofen"
- **THEN** no second medication is added, and the backup's intakes count as the phone's Ibuprofen

### Requirement: Files that cannot be imported
A broken file, or a file of another format, SHALL change nothing and say "This file isn't a Migrely backup." A file
from a newer version of Migrely SHALL be refused with "Update Migrely to open this file." A file from an older version
SHALL be imported, with missing fields taking their defaults (BRD EC-BAK-01, EC-BAK-02, EC-BAK-03, D73).

#### Scenario: A broken file
- **WHEN** the user picks a file that is not a Migrely backup
- **THEN** nothing on the phone changes, and the message says so

#### Scenario: A newer file
- **WHEN** the file's schemaVersion is higher than the app's
- **THEN** it is refused with "Update Migrely to open this file."

### Requirement: Android backup
Android's Auto Backup and device-to-device transfer SHALL include the database, shared preferences and the automatic
backups, and SHALL exclude the cache (BRD BAK-F4, D74).

#### Scenario: A new phone
- **WHEN** the user restores their Google backup on a new phone
- **THEN** their diary is back, and the daily reminder is planned again when the app first starts

### Requirement: Restore the automatic copy
When an automatic copy exists, Your data SHALL offer "Restore the copy from <date>". It SHALL check the newest copy and
show what it holds. On confirming, it SHALL first save what the phone holds as a new automatic copy, keeping the newest
three, and then replace the records in one transaction (BRD D75, D98).

#### Scenario: Undoing a Replace
- **WHEN** the user replaced 16 migraines with a backup of 3, then restores the copy from today
- **THEN** the phone holds the 16 again, and a copy of the 3 is kept
