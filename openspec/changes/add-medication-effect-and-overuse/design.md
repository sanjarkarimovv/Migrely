# Design

## Context

- Change 3 stores medications and intakes (DB v3). An intake has a medication, an optional attack, the time and zone
  it was taken, and a dose. It has no effect yet.
- Home's month calendar is a slot that `:app` fills with History's `MonthSky` (change 2, design D1). It reloads
  whenever home loads, through `refreshKey`. The medication cards use the same pattern, so home never depends on the
  medication feature.
- The attack feature reads its intakes through its own `AttackDetailsRepository` and models (change 3). The details
  section lists them and removes them with Undo.

## Goals and non-goals

- **Goals:** "Did it help?" in the app, the answer in details, an overuse count that follows ICHD-3, and calm warnings.
  Every rule lives in a use case and is unit tested.
- **Non-goals:** notifications (change 6); the stats indicator (change 5); a list of standalone intakes (change 5).

## Decisions

### D1. Storage, DB v4

| Change | Column or table | Notes |
|---|---|---|
| `medication_intake.effect` | `TEXT NULL` | `NOT_AT_ALL`, `A_LITTLE`, `A_LOT`, `COMPLETELY` |
| `medication_intake.effect_closed` | `INTEGER NOT NULL DEFAULT 0` | set by an answer or a skip; the question is never asked again |
| `overuse_notice` | `category TEXT`, `level TEXT`, `acknowledged_epoch_millis INTEGER`, primary key `(category, level)` | the last "Got it"; `category` is `SIMPLE`, `TRIPTAN`, `COMBINATION_OTHER` or `MULTIPLE`, `level` is `SOFT` or `DOCTOR` |

- Additive, so `AutoMigration(3, 4)`, with `MigrationTest` 3→4.
- `MedicationDataSource` gains six functions:
  - `pendingEffectQuestion(takenFrom, takenTo)`: the oldest intake of an acute medication with no effect, not closed,
    taken in the window, and not in an attack being deleted;
  - `setIntakeEffect(intakeId, effect)`: stores the effect and closes the question;
  - `closeEffectQuestion(intakeId)`: closes it without an answer;
  - `acuteIntakesTakenBetween(from, to)`: the taken time, the zone and the category of each acute intake, outside
    attacks being deleted;
  - `overuseNotices()` and `acknowledgeOveruse(category, level, at)`: read the notices and upsert one.
- `IntakeDataModel` gains `effect` and `effectClosed`. `restoreIntake` writes them back, so Undo keeps an answer.

### D2. One use case decides the card

`GetMedicationPromptUseCase` returns `MedicationPromptDomainModel`, one of:
- `None`;
- `Overuse(category, level, days, threshold)`;
- `EffectQuestion(intakeId, medicationName, dose, takenAt, zone)`.

**Overuse:**
1. Read the acute intakes from 32 days before today's start to the end of today, in the device zone. That covers any
   zone an intake was taken in.
2. Keep the intakes whose date in their own zone is within `today - 29 .. today` (D45).
3. Count distinct dates per category: SIMPLE (threshold 15), TRIPTAN (10), COMBINATION_OTHER (10, with null).
4. MULTIPLE (threshold 10) counts all distinct acute dates when two or more categories have a day.
5. Each category's level is DOCTOR when `days ≥ threshold`, and SOFT when `days ≥ threshold - 2`. MULTIPLE is kept
   only when its level is higher than every single category's level, before any notice is considered.
6. A candidate is hidden when a notice for its category at the same or a higher level is younger than 30 days. A "Got
   it" is per category (EC-MED-06), so it never hides another category's warning.
7. Of the candidates left, the top level wins.
8. Among those, the one with the most days wins. Ties go in the order SIMPLE, TRIPTAN, COMBINATION_OTHER, MULTIPLE.

**Question:** the pending intake taken in `[now - 24 h, now - 2 h)`, that is more than 2 and at most 24 hours ago.

**Order:** a DOCTOR overuse card, then a SOFT one, then the question (D46).

Other use cases:
- `AnswerEffectUseCase(intakeId, effect)` → `setIntakeEffect`;
- `SkipEffectQuestionUseCase(intakeId)` → `closeEffectQuestion`;
- `AcknowledgeOveruseUseCase(category, level)` → `acknowledgeOveruse` at the current time.

Failures are values (`WriteResultDomainModel`). A failed read gives `None`, because home reports its own failures.

### D3. The prompt slot

| Piece | Where |
|---|---|
| `MedicationPromptViewModel` (`onEnter`, `onEffectAction`, `onSkipEffectAction`, `onOveruseSeenAction`) | `:features:medication:presentation` |
| `MedicationPromptDependencies.MedicationPrompt(refreshKey)` and `MedicationPromptContents` | `:features:medication:ui` |
| `medicationPrompt: @Composable (refreshKey: Int) -> Unit = {}` on `Home`, drawn by `TodayContents` under today's card | `:features:home:ui` |
| the slot filled in `AppNavHost`; DI in `MedicationUiModule` | `:app` |

- After an action the ViewModel loads again, so the next card appears at once.
- A failed write shows nothing new: the same card stays, so the user can try again.
- The card is calm, on the theme's surface container. The doctor card uses the information tone, never an alarm
  colour (BRD 18.4).

### D4. The answer in details

- `AttackIntakeDomainModel` and the attack's presentation and UI models gain `effect`.
- `SetIntakeEffectRepository` and `SetIntakeEffectUseCase` live in the attack domain. `AttackDetailsRepository`
  implements the repository with `MedicationDataSource.setIntakeEffect`.
- `AttackDetailsViewModel.onIntakeEffectAction(attackId, intakeId, effect)` writes the answer, then reloads.
- Tapping a row opens a dialog with the four answers as radio rows and "Cancel". The row's second line shows "Helped a
  lot", or nothing when there is no answer.
- The models also carry `effectQuestionClosed`, so "Undo" after removing a skipped intake keeps its question closed.
- Any intake can be answered in details, a preventive one included. The user chose to open it, and home never asks
  about preventives (EC-MED-11).

### D5. Copy

| Case | Title | Body |
|---|---|---|
| question | How much did Ibuprofen help? | Taken at 2:00 PM · 400 mg |
| soft, simple | Painkillers on 13 of the last 30 days | Taking them on 15 days or more a month can lead to more headaches. Worth keeping an eye on. |
| doctor, simple | Painkillers on 15 of the last 30 days | Taking them this often can make headaches more frequent. It's worth talking to your doctor. |
| triptans, combination/other | "Triptans", "Combination or other medications" | same bodies, with 10 |
| multiple | Acute medications on 10 of the last 30 days | same bodies, with 10 |

## Risks

| Risk | Mitigation |
|---|---|
| The count reads up to 32 days of intakes on every home load | indexed by `taken_epoch_millis`, a few hundred rows at most |
| An effect question appears while home stays open past 2 hours | it appears on the next home load (resume, any action); notifications come in change 6 |
| Clinical wording | informative only, a doctor suggested at the threshold, never "you have MOH" (BRD 11) |
