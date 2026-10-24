# Tasks

## 1. Datasources

- [x] 1.1 DAO additions (read all, delete all, insert-ignore) and `BackupDataSource` with one-transaction replace and
  add; instrumented tests.
- [x] 1.2 `BackupFileDataSource` (kotlinx.serialization JSON, decode results, automatic backups keeping 3) with unit
  tests.
- [x] 1.3 `DocumentDataSource` (ContentResolver).

## 2. Backup feature

- [x] 2.1 Domain: models, single-function repositories, use cases, tests.
- [x] 2.2 Data: `BackupRepository`, CSV mapper (RFC 4180, BOM), tests.
- [x] 2.3 Presentation: `YourDataViewModel`, tests.
- [x] 2.4 UI: the Your data screen, SAF launchers, the import dialog.

## 3. Settings

- [x] 3.1 `:features:settings:{presentation,ui}`; home's header shows "Settings" in place of the bell; routes and DI.

## 4. Auto Backup

- [x] 4.1 `backup_rules.xml` and `data_extraction_rules.xml`.

## 5. Verify

- [x] 5.1 Gate (1016 unit tests), and instrumented tests (datasource 82/82, designsystem 28/28).
- [x] 5.2 Emulator walk:
  - export JSON and CSV to Downloads, and read them back;
  - import Replace and Add, a broken file, a newer file and an older file;
  - the automatic backup;
  - Settings and Reminders;
  - accessibility.
- [x] 5.3 Independent review; fix and record.

## Verification record

**Emulator walk** on emulator-5570 (host GPU), by a walk agent on f579917.
- The baseline held only attacks, so the agent first added 3 medications, 5 intakes, 3 day marks, a custom tag, more
  tags, and an attack being deleted.
- At the end the database was restored byte-identical to the baseline (md5 8f7d7815…), with every file removed and
  the settings as before.
- All 10 scenarios passed:

| Scenario | Result |
|---|---|
| Settings | the gear on home and not in attack mode; each row is one clickable node; Reminders, Your data and Back navigate |
| JSON export | `migrely-2026-10-25.json`, "Backup saved.", counts match the database, the attack being deleted and its intake are left out |
| CSV export | BOM, CRLF, the exact header, 16 rows newest first; spot checks of peak, level, tags and medications match; a German locale gives ";" |
| Add of the same file | the dialog shows the numbers and three choices; nothing changes and no copy is made |
| Cancel and rotation | the dialog survives landscape; Cancel changes nothing |
| Replace | "Backup imported: 3 migraines.", exactly one copy, identical to the export; home and History show the new state |
| Add after Replace | "Added 13 migraines. 3 were already here."; counts back, no duplicate intakes, and a new export equals the first |
| Files that cannot be imported | a text file, another format and an edited file give "isn't a Migrely backup"; a newer one gives "Update Migrely"; nothing changes |
| An empty phone | an empty backup empties it; the next import shows only Import and Cancel |
| Accessibility | rows are single nodes with role Button; nothing is clipped at 200% font in portrait or landscape; the dialog scrolls |

**Noted, accepted:**
- At 200% font in landscape, the dialog's text area is small but scrolls.
- The open picker lists CSV files too, and picking one gives the clear message.
- An empty backup still offers Add, which does nothing.

**Re-walk after the review fixes** (9438bd7): every check passed, and the database was restored byte-identical.
- Add with only a new marked day reads "Added the other entries. Every migraine in it was already here."; the same file
  again reads "Nothing new: everything in it is already here."
- The same message twice in a row shows twice.
- Replace is drawn in the theme's error color.
- A file that is not a backup makes no copy; a Replace makes exactly one.
- An export finished across a rotation still says "Backup saved." with the rows enabled; the dialog survives a
  rotation.
- Seen, outside Migrely: DocumentsUI can leave an empty file when rotated within half a second of SAVE; that file is
  refused as not a backup.

## Review (5.3)

An independent `superpowers:code-reviewer` pass found nothing critical. It confirmed:
- Replace and Add stay inside one transaction; the delete order fits the foreign keys; device state is kept; the
  name-key rule matches the app;
- app-written backups pass the validator; the BOM, the header-first decode and the 16 MB limit;
- the CSV's quoting, formula guard, separator and zones;
- the Auto Backup rules, the layer rules, and that no string name repeats across modules.

| Finding | Outcome |
|---|---|
| **I1** Replace saved its copy before reading the file, so failed attempts pushed older copies out, and a clock moved back deleted the new copy | fixed: the copy is saved only for a file that reads as a backup, and the copy just written always stays |
| **I2** a rotation during work rebuilt the view model: the outcome was lost and the rows came back enabled | fixed: `YourDataViewModel` is app-wide, counts the work and keeps the outcome until the screen takes it (ARCHITECTURE §13, design D9) |
| **I3** a provider's runtime exception, or a zone this phone lacks, would crash inside the background use case | fixed: `file {}` maps every provider failure, the CSV falls back to UTC, and a missing medication rolls back through the foreign key |
| **I4** Add said "Nothing new" when it added marks, medications or intakes | fixed: those are counted, with "Added the other entries." |
| Minor | fixed: no link to an attack being deleted; one tag row when two backup tags stand for one; a failed export deletes its file; cloud backup only with encryption (D74); the same message shows again; Replace in the error color; stale KDoc and test names; design D1, D5, D6, D8–D10. Accepted with reasons in design: duplicate intakes from two phones, Add past the active limits, and the unchecked tag kind and lengths |
| Tests | added: the Settings, Your data and home navigation mappers; five DAO cases (an attack in progress in the backup, overlaps inside it, the same ID being deleted, Replace over it, Add rolling back); a round trip through the app's own DAOs; the document datasource |
