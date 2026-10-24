# Proposal

## Why

Change 3 lets people log what they take. Two questions from the BRD are still open.

- **Did it help? (MED-F4, ATK-F2):** the answer is the most useful thing to show a doctor about a medication. The BRD asks
  2 hours after an intake. Notifications come in change 6, so EC-MED-05 already decides that the question is asked
  inside the app, within 24 hours (D33).
- **Medication overuse (MED-F5):** taking painkillers or triptans too often can itself cause more headaches
  (medication-overuse headache, ICHD-3 8.2). It is one of the few things a tracker can catch early. The app counts the
  days and gives a calm, informative warning before and at the threshold. It never diagnoses.

## What Changes

- **"Did it help?" on home (D39, EC-MED-05, EC-MED-11):**
  - in the normal mode, one card under today's card asks "How much did Ibuprofen help?" about an acute intake taken
    2 to 24 hours ago;
  - the answers are "Not at all", "A little", "A lot" and "Completely". A close button skips the question for that
    intake;
  - each intake is asked at most once, the oldest first. Preventive medications are never asked about.
- **The answer in the attack's details (D39, EC-MED-12, ATK-F2):** each medication row shows its answer. Tapping the
  row sets or changes it, at any time.
- **Overuse warning (D45, EC-MED-02 to EC-MED-04, EC-MED-06):**
  - the app counts the days with an acute intake in the last 30 days, per category: simple painkillers 15, triptans
    10, combination or other (and "Not sure") 10;
  - with two or more categories, all acute days together count against 10 (ICHD-3 8.2.6);
  - a soft card appears 2 days before a threshold, and a "talk to your doctor" card at it;
  - "Got it" hides that category and level for 30 days. A higher level still shows.
- **One card at a time (D46):** the doctor card, then the soft card, then "Did it help?". There are no cards in attack
  mode. The cards belong to the medication feature, and `:app` places them in a home slot, as it does the calendar.
- **Storage:** database version 4 (`AutoMigration(3, 4)`):
  - `medication_intake` gains `effect` and `effect_closed`;
  - a new `overuse_notice` table keeps the last "Got it" per category and level.

**Out of scope:**
- the notification 2 hours after an intake (change 6, reminders);
- the overuse indicator in stats (change 5);
- answering about an intake logged outside an attack after 24 hours. Stats in change 5 will list those.

## Capabilities

### New Capabilities

- `medication-effect`: the "Did it help?" question on home and the answer in the attack's details.
- `medication-overuse`: how overuse days are counted and when the soft and doctor cards appear.

### Modified Capabilities

- `today-sky`: home shows at most one medication card under today's card, in the normal mode only.

## Impact

- **Datasource:**
  - `MedicationDataSource` gains:
    - the effect and the closed flag on intakes;
    - the pending question;
    - acute intakes in a window;
    - overuse notices;
  - DB v4 with `MigrationTest` 3→4.
- **Medication feature:**
  - domain: `GetMedicationPromptUseCase`, `AnswerEffectUseCase`, `SkipEffectQuestionUseCase` and
    `AcknowledgeOveruseUseCase`;
  - presentation: `MedicationPromptViewModel`;
  - UI: the `MedicationPrompt` card and its strings.
- **Attack feature:** intakes carry their effect; `SetIntakeEffectUseCase`; an effect dialog on the details row; Undo
  after removing an intake keeps its answer.
- **Home UI:** a `medicationPrompt` slot under today's card. Home knows nothing about medications.
- **`:app`:** DI, and the slot filled in `AppNavHost`.
- **Docs:** BRD 9.3 (EC-MED-11, EC-MED-12), 13 (row 3a), 17 (D39, D45, D46); ARCHITECTURE §3 and §6a.
- **BRD references:** MED-F4, MED-F5, EC-MED-02 to EC-MED-06, EC-MED-11, EC-MED-12, ATK-F2, EC-TIM-02, G4, D33,
  D39, D45, D46.
