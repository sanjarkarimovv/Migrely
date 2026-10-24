# Tasks

## 1. Domain

- [ ] 1.1 Scaffold `:features:medication:*` with `cag` and home's build files; add the medication domain models, repository interfaces, and `GetMedicationsUseCase`, `SaveMedicationUseCase`, `ArchiveMedicationUseCase` and `RestoreMedicationUseCase`, with tests:
  - name and dose limits and trimming;
  - duplicate, archived restore and the limit;
  - category cleared for preventive;
  - failures as values.

  Verify `./gradlew :features:medication:domain:test`
- [ ] 1.2 Add `GetMedicationLogUseCase`, `LogIntakeUseCase` and `UndoIntakeUseCase`, with tests:
  - the default time (now, or the start of an ended attack);
  - the 5-minute tolerance;
  - linking to the given attack or to the containing attack;
  - the order;
  - failures.

  Verify the domain tests
- [ ] 1.3 In `:features:attack:domain`, add intakes to `AttackDetailsDomainModel`, and `RemoveIntakeUseCase` and `RestoreIntakeUseCase` with tests; verify `./gradlew :features:attack:domain:test`

## 2. Presentation

- [ ] 2.1 Add the medication presentation models, the `MedicationsViewModel`, `EditMedicationViewModel` and `LogMedicationViewModel`, their view states, notifications and navigation events, and the mappers, with `BaseViewModelTest` tests; verify `./gradlew :features:medication:presentation:test`
- [ ] 2.2 Add the intakes, the remove/undo actions and `OnLogMedication(attackId)` to `AttackDetailsViewModel`, and `onMedicationTakenAction` and `onLogMedicationAction` to `HomeViewModel`, with tests; verify the attack and home presentation tests

## 3. Datasource

- [ ] 3.1 Add `MedicationLocalModel`, `MedicationIntakeLocalModel`, `MedicationDao`, database version 3 with `AutoMigration(2, 3)` and the exported schema, and the intake move in `mergeInto`, with instrumented DAO tests and a `MigrationTest` from 2 to 3; verify `ANDROID_SERIAL=emulator-5570 ./gradlew :datasource:implementation:connectedDebugAndroidTest`
- [ ] 3.2 Add `MedicationDataSource` with its implementation, data models and unit tests (mocked DAO, failure mapping); verify `./gradlew :datasource:implementation:test`

## 4. Data

- [ ] 4.1 Add `MedicationRepository` (medication feature), and intake reads and writes in `AttackDetailsRepository`, with tests for every interface and failures as values; verify the data tests

## 5. UI

- [ ] 5.1 Build Medication taken (list, time row and picker, logged state with Undo and Done, first-use form, dark-room styling), My medications and the medication editor, with UI models, mappers, previews and unit tests; verify `./gradlew :features:medication:ui:test`
- [ ] 5.2 Add the details "Medications" section with remove and Undo, the attack-mode "+ Medication taken" button and the home "Log medication" link, checked at 200% and in landscape; verify the attack and home UI tests

## 6. App

- [ ] 6.1 Add the routes, nav host entries, Hilt modules (`MedicationModule`, `MedicationDataModule`, `MedicationUiModule`, `MedicationDataSourceModule`) and navigation mappers with tests; verify `./gradlew :app:test`

## 7. Docs

- [ ] 7.1 Update ARCHITECTURE §3 (the medication feature) and §6a (version 3, intakes in merge and purge); verify it matches the code

## 8. Verify

- [ ] 8.1 Run the gate and the instrumented tests (datasource, designsystem) on emulator-5570; verify all pass
- [ ] 8.2 Walk every scenario on emulator-5570: dark room, details, home, first use, duplicate and archived names, the limit, undo, merge and delete with intakes, 200% font, landscape, TalkBack and keyboard, rotation and process death. Restore the settings and record the results
- [ ] 8.3 Run an independent review, fix the real findings, and validate strictly
