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

- [ ] 2.1 Models, repositories and `GetMedicationPromptUseCase`, `AnswerEffectUseCase`, `SkipEffectQuestionUseCase`,
  `AcknowledgeOveruseUseCase`, with tests for every spec scenario of `medication-effect`, `medication-overuse` and the
  card order.
  Verify: `./gradlew :features:medication:domain:test`

## 3. Medication presentation and data

- [ ] 3.1 `MedicationPromptViewModel` and its presentation models and mappers, with tests.
  Verify: `./gradlew :features:medication:presentation:test`
- [ ] 3.2 `MedicationRepository` implements the new repositories, with tests.
  Verify: `./gradlew :features:medication:data:test`

## 4. Attack feature (design D4)

- [ ] 4.1 The effect on attack intakes through domain, data, presentation and UI models; `SetIntakeEffectUseCase`;
  `onIntakeEffectAction`; tests.
  Verify: `./gradlew :features:attack:domain:test :features:attack:data:test :features:attack:presentation:test`

## 5. UI

- [ ] 5.1 `MedicationPrompt` card (question, soft and doctor), its strings and its UI mappers, with tests.
  Verify: `./gradlew :features:medication:ui:test`
- [ ] 5.2 Details: the answer on each row and the answer dialog.
  Verify: `./gradlew :features:attack:ui:test`
- [ ] 5.3 Home: the `medicationPrompt` slot under today's card.
  Verify: `./gradlew :features:home:ui:test`

## 6. App wiring

- [ ] 6.1 DI for the new use cases, ViewModel and dependencies; the slot filled in `AppNavHost`.
  Verify: `./gradlew :app:assembleDebug`

## 7. Docs

- [ ] 7.1 ARCHITECTURE §3 (the prompt slot) and §6a (DB v4).

## 8. Verify

- [ ] 8.1 Gate `./gradlew assembleDebug test ktlintCheck detekt`; instrumented datasource and designsystem.
- [ ] 8.2 Upgrade from v3 with data; emulator walk of every scenario on emulator-5570 (intakes seeded with past times
  through the app's own screens or the database). Cover the question, skip, order, attack mode, details answer,
  Undo, soft, doctor, multiple, "Got it" and 30 days. Also 200% font, landscape and TalkBack labels. Record below.
- [ ] 8.3 Independent review; fix and record.
