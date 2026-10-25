# Design

## Decisions

### D1. The earliest intake

- **The datasource:** `MedicationDataSource.earliestAcuteIntake()` returns the first attack-medication intake's
  instant and zone, or null. Preventive medications are left out, as `acuteIntakesTakenBetween` does.
- **History's domain:**
  - a `GetEarliestAttackMedicationIntakeRepository` returns Found, None or Failed, as the earliest attack and day-mark
    repositories do;
  - an unknown zone reads as UTC.

### D2. Anything logged

- **The model:** `HistoryMonthDomainModel.anyAttackLogged` becomes `anythingLogged`: an attack, a day mark or an
  attack-medication intake.
- **The view state:** `HistoryViewModel` chooses `Ready` when anything is logged, and `NoAttacks` only when nothing is.
- **The empty state:** the copy stays "No migraines logged yet" with "Log your first migraine", since it now shows only
  when nothing at all is logged.
- **The progress message:** it still needs a logged attack. It compares migraine days, and without a migraine ever
  logged that comparison means nothing (`month-stats`).
