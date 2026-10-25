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
- **The view state:** `HistoryViewModel` chooses `Ready` when anything is logged, and `NothingLogged` (formerly
  `NoAttacks`, renamed for what it now means) only when nothing is.
- **The empty state:** the copy stays "No migraines logged yet" with "Log your first migraine", since it now shows only
  when nothing at all is logged.
- **The progress message:** it still needs a logged attack. It compares migraine days, and without a migraine ever
  logged that comparison means nothing (`month-stats`).

### D3. A day with attack medication only (BRD D100)

- **The domain:** `HistoryDayDomainModel.attackMedication` is true when one of the month's attack-medication intakes
  falls on the day, in the zone it was taken in. The intakes are the ones `GetHistoryMonthUseCase` already reads for
  the month's numbers, so preventive ones never count and no new read is needed.
- **The sky:** the presentation mapper keeps its order: rain, then the worst attack, then pain-free, then future. A
  past day with attack medication comes next, as `MedicationOnly`, before "nothing logged". A day with an attack keeps
  its sky, and a pain-free mark keeps the clear sky, since the mark is the user's word.
- **The drawing:** `DaySkyAppearance.MedicationOnly` is a solid outline in `colorScheme.outline`, the same width as
  the dashed one, with no sky. Since change 12 the nothing-logged dash uses `outline` too: 3.8:1 on the light theme,
  and dim by design on the dark one. So the two differ by shape (solid or dashed) and by TalkBack text rather than by
  colour.
- **Spoken text:** "<day>: attack medication taken, no migraine logged." A pain-free day with an intake adds "Attack
  medication taken.", before "Period." on a period day.
- **Home:** the month card draws the same `HistoryCalendar` from the same view model output, so it follows without
  changes.
- **The legend:** "Attack medication only", with its solid outline, before "Not logged".
