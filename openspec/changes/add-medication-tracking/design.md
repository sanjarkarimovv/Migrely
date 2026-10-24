# Design

## Context

- **Attacks:** they live in the `attack` table. Home (attack mode) and the `attack` feature (details) work with them, and
  features never depend on each other.
- **Database:** Room at version 2. Additive steps use `AutoMigration` with a `MigrationTest` (ARCHITECTURE §6a).
- **Close pattern:** custom tags (change 1b) already show how a user-owned list is archived instead of deleted.

## Goals and non-goals

**Goals**
- Log a pill in two taps during an attack.
- One medication list that the effect question and the overuse count of change 3a can build on.
- Intakes never orphaned or lost when attacks are deleted or merged.

**Non-goals**
- The effect question, overuse warnings and notifications (changes 3a and 6).
- A drug database.

## Decisions

### D1. A `medication` feature with three screens

| Screen | Route | Opened from |
|---|---|---|
| Medication taken | `LogMedication(attackId: String?, darkRoom: Boolean)` | attack mode (`darkRoom = true`), details, the home link |
| My medications | `Medications` | "Manage medications" on Medication taken |
| Medication editor | `EditMedication(medicationId: String?)` | My medications ("Add", or a row) |

Home and `attack` only emit navigation events, `OnLogMedication(attackId)`, which `:app` maps to the route. The
details screen reads its intakes from the shared datasource through its own models (ARCHITECTURE §3).

### D2. Schema version 3 (additive `AutoMigration(2, 3)`)

| Table | Columns |
|---|---|
| `medication` | `id` PK, `name`, `name_key` (unique; trimmed, lower-case), `kind` (`ACUTE`/`PREVENTIVE`), `category` (nullable: `SIMPLE`, `TRIPTAN`, `COMBINATION_OTHER`; null means "Not sure"), `default_dose` (nullable), `archived`, `created_epoch_millis`, `updated_epoch_millis` |
| `medication_intake` | `id` PK, `medication_id` (FK `medication`, RESTRICT), `attack_id` (nullable FK `attack`, CASCADE), `taken_epoch_millis`, `taken_zone_id`, `dose` (nullable), `created_epoch_millis`; indices on `attack_id`, `medication_id` and `taken_epoch_millis` |

- **Medications:** never deleted, only archived (EC-MED-01), so RESTRICT can never fire.
- **Attack purge:** the purge after a deletion's undo window cascades to the intakes (EC-MED-10). A soft-deleted attack
  hides them, because every read of intakes goes through a visible attack or is standalone.
- **Merge:** `mergeInto` also runs `UPDATE medication_intake SET attack_id = :survivor WHERE attack_id IN (:absorbed)`
  inside the same transaction (D27).
- **YAGNI:** the effect columns come with change 3a in their own additive migration.

### D3. Domain rules (medication feature)

| Rule | Where |
|---|---|
| Name 1–60 characters after trimming, dose at most 20 characters; the key is the trimmed lower-case name | `SaveMedicationUseCase` |
| A duplicate active name is refused (`Duplicate(name)`); an archived name is restored and updated (`Restored`); more than 50 active is refused (`LimitReached`) | `SaveMedicationUseCase` |
| Category only for acute; it is cleared for preventive | `SaveMedicationUseCase` |
| Default time: now, or the start of the given attack when it has ended; never more than 5 minutes ahead | `GetMedicationLogUseCase`, `LogIntakeUseCase` |
| Link: the given attack, or else the attack containing the time (in progress included) | `LogIntakeUseCase` |
| Order: acute first, then most recently used, then name | the datasource query plus the use case |

### D4. Logging flow and state

- `LogMedicationViewModel` is stateless. The screen keeps the picked time, the first-use form, and the last logged intake
  for Undo in `rememberSaveable`.
- A successful log shows the logged state: "Ibuprofen 400 mg logged at 14:20", "Undo" and "Done". Undo deletes the intake
  and returns to the list.
- First use is a "No medications" state with the name and category form and "Add and log". The ViewModel chains save,
  then log.
- With `darkRoom`, the screen uses `MigrelyColors.AttackMode*` and 64 dp buttons.

### D5. Details "Medications" section

- `AttackDetailsDomainModel.intakes` holds `AttackIntakeDomainModel(id, medicationName, dose, takenAt, zone)`, oldest
  first.
- Removing an intake deletes it and returns it for Undo, which re-inserts it with the same id. This is the same message
  pattern as pain entries (`AttackMessages`).
- The section reloads on resume, which picks up intakes logged from the Medication taken screen.

## Risks

- **Intake times are not bound to the attack's range** (D34). Pills taken just before an attack still belong to it. The
  details list shows any time with its day.
- **The home link adds a second quiet link** under the main button. Both stay text buttons at least 48 dp tall; layouts
  are checked at 200% and in landscape.
