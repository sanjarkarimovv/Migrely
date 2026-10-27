## MODIFIED Requirements

### Requirement: Replace or add
With records already on the phone, the import SHALL offer "Replace", which first saves an automatic backup inside the
app and keeps the last 3, and "Add", which skips records already present and links a medication whose name already
exists to that one. A migraine being deleted, and the intakes hidden with it, SHALL count as absent: "Add" SHALL put
the backup's copy in its place and count it as added, and "Replace" SHALL remove it with the rest (BRD BAK-F3,
EC-BAK-04, EC-MED-07, D73, D118).

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

#### Scenario: A migraine deleted after the backup
- **WHEN** the user exports a backup of 2 migraines, deletes one without undoing it, and later adds the backup
- **THEN** the deleted migraine is back as the file has it, with its pain points, tags and intakes
- **AND** Migrely says "Added 1 migraine. 1 was already here."

#### Scenario: Replace while a deletion can still be undone
- **WHEN** the user deletes a migraine and chooses "Replace" with a backup before the undo has run out
- **THEN** the phone holds only the backup's records, and the deleted migraine can no longer be restored

#### Scenario: A file with no migraines
- **WHEN** the user adds a backup that holds medications and intakes but no migraine, and some of them are new
- **THEN** Migrely says "Added the new entries." and says nothing about migraines
