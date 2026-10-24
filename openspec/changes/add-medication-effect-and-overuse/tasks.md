# Tasks

## 1. Datasource (design D1)

- [x] 1.1 DB v4: `effect`, `effect_closed` on `medication_intake`, the `overuse_notice` table, `AutoMigration(3, 4)`,
  schema `4.json`.
- [x] 1.2 `MedicationDataSource`: `pendingEffectQuestion`, `setIntakeEffect`, `closeEffectQuestion`,
  `acuteIntakesTakenBetween`, `overuseNotices`, `acknowledgeOveruse`; `IntakeDataModel` with `effect` and
  `effectClosed`, restored by `restoreIntake`. Unit tests for the impl, and DAO and `MigrationTest` 3→4 instrumented tests.
  Verify: `./gradlew :datasource:implementation:test` and
  `ANDROID_SERIAL=emulator-5570 ./gradlew :datasource:implementation:connectedDebugAndroidTest`

## 2. Medication domain (design D2)

- [x] 2.1 Models, repositories and `GetMedicationPromptUseCase`, `AnswerEffectUseCase`, `SkipEffectQuestionUseCase`,
  `AcknowledgeOveruseUseCase`, with tests for every spec scenario of `medication-effect`, `medication-overuse` and the
  card order.
  Verify: `./gradlew :features:medication:domain:test`

## 3. Medication presentation and data

- [x] 3.1 `MedicationPromptViewModel` and its presentation models and mappers, with tests.
  Verify: `./gradlew :features:medication:presentation:test`
- [x] 3.2 `MedicationRepository` implements the new repositories, with tests.
  Verify: `./gradlew :features:medication:data:test`

## 4. Attack feature (design D4)

- [x] 4.1 The effect on attack intakes through domain, data, presentation and UI models; `SetIntakeEffectUseCase`;
  `onIntakeEffectAction`; tests.
  Verify: `./gradlew :features:attack:domain:test :features:attack:data:test :features:attack:presentation:test`

## 5. UI

- [x] 5.1 `MedicationPrompt` card (question, soft and doctor), its strings and its UI mappers, with tests.
  Verify: `./gradlew :features:medication:ui:test`
- [x] 5.2 Details: the answer on each row and the answer dialog.
  Verify: `./gradlew :features:attack:ui:test`
- [x] 5.3 Home: the `medicationPrompt` slot under today's card.
  Verify: `./gradlew :features:home:ui:test`

## 6. App wiring

- [x] 6.1 DI for the new use cases, ViewModel and dependencies; the slot filled in `AppNavHost`.
  Verify: `./gradlew :app:assembleDebug`

## 7. Docs

- [x] 7.1 ARCHITECTURE §3 (the prompt slot) and §6a (DB v4).

## 8. Verify

- [x] 8.1 Gate `./gradlew assembleDebug test ktlintCheck detekt`; instrumented datasource and designsystem.
- [x] 8.2 Upgrade from v3 with data; emulator walk of every scenario on emulator-5570 (intakes seeded with past times
  through the app's own screens or the database). Cover the question, skip, order, attack mode, details answer,
  Undo, soft, doctor, multiple, "Got it" and 30 days. Also 200% font, landscape and TalkBack labels. Record below.
- [ ] 8.3 Independent review; fix and record.

## Verification record

**Gate** `./gradlew assembleDebug test ktlintCheck detekt`: BUILD SUCCESSFUL, 779 unit tests (711 before), 0 failures.
New tests:
- `GetMedicationPromptUseCaseTest` 27;
- the write use cases 3;
- `MedicationPromptRepositoryTest` 11;
- `MedicationPromptViewModelTest` 5, `MedicationPromptMappersTest` 4, `MedicationPromptUiMappersTest` 4;
- the datasource impl 7;
- attack data 3, ViewModel 3 and UI mapper 1.

**Instrumented tests** on emulator-5570:
- datasource 54/54. New: 5 `MedicationDaoTest` cases (the pending question, answer and clear, an attack's answers, acute
  intakes in a window, notice upsert) and `MigrationTest` 3→4 and 1→4;
- designsystem 16/16, after `MigrelyIcons.Information`.

**Upgrade:** a version 3 database with 29 attacks, a medication and an intake was upgraded in place. It became version
4 with all 29 attacks, the intake kept (`effect` null, `effect_closed` 0) and an empty `overuse_notice`. Home asked
"How much did Ibuprofen help?" at once.

**Emulator walk** on emulator-5570. Intakes were seeded with past times and zones through the database (adb root on the
userdebug image, unrooted after):

| Scenario | Result |
|---|---|
| Two hours later | "How much did Ibuprofen help?", "Taken at 1:44 PM · 400 mg", four answers; TalkBack reads "Taken at 1:44 PM, 400 mg" and "Don't ask about this one" |
| Answer | "A lot" stored as `A_LOT`, question closed, card gone |
| One at a time, oldest first, skip | Sumatriptan (7 h ago) asked first; × closes it with no answer; Ibuprofen (6 h ago) asked next; "Completely" stored |
| Too early (1 h), too late (25 h), preventive (3 h) | none asked |
| Attack in progress | attack mode shows no card |
| Answer in details | the row "Sumatriptan 50 mg, 1:00 PM. Opens how much it helped." opens a radio dialog; "Completely" makes the row "Helped completely"; home stops asking about it |
| Undo keeps the answer | removed, then Undo: the row comes back "Helped completely", stored `COMPLETELY`, closed |
| Soft warning | 13 painkiller days: "Painkillers on 13 of the last 30 days" with the 15-day note; "Got it" stores SIMPLE/SOFT and the question card follows |
| From soft to doctor | 15 days: the doctor card shows despite the soft "Got it" |
| Several categories | 6 painkiller and 4 triptan days: "Acute medications on 10 of the last 30 days" and the doctor text |
| Once in 30 days | MULTIPLE/DOCTOR acknowledged 29 days ago: hidden; 30 days ago: shown again |
| Double tap | three taps on "A lot" 250 ms apart: only the first question answered, the next one shown and still open |
| 200% font | answers one per row with full labels; the warning wraps; landscape keeps the card in the left column |

**Found and fixed in the walk:**
- the four answers sat three and one; they are now a 2×2 grid, one per row at large font sizes;
- "50 mg" broke between the number and the unit at 200%; the dose now uses no-break spaces.

**Not checked:**
- the heading flag, which `uiautomator dump` does not show. It is set in code with `semantics { heading() }`;
- a real 2-hour wait. The window is covered by `GetMedicationPromptUseCaseTest` and the seeded times.

**Settings restored:**
- font 1.0, rotation 0, accelerometer 1, the accessibility timeout deleted, adbd unrooted;
- the walk medications, intakes and notices deleted;
- one attack started by the walk removed.
