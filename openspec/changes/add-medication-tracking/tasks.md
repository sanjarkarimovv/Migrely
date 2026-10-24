# Tasks

## 1. Domain

- [x] 1.1 Scaffold `:features:medication:*` with `cag` and home's build files; add the medication domain models, repository interfaces, and `GetMedicationsUseCase`, `SaveMedicationUseCase`, `ArchiveMedicationUseCase` and `RestoreMedicationUseCase`, with tests:
  - name and dose limits and trimming;
  - duplicate, archived restore and the limit;
  - category cleared for preventive;
  - failures as values.

  Verify `./gradlew :features:medication:domain:test`
- [x] 1.2 Add `GetMedicationLogUseCase`, `LogIntakeUseCase` and `UndoIntakeUseCase`, with tests:
  - the default time (now, or the start of an ended attack);
  - the 5-minute tolerance;
  - linking to the given attack or to the containing attack;
  - the order;
  - failures.

  Verify the domain tests
- [x] 1.3 In `:features:attack:domain`, add intakes to `AttackDetailsDomainModel`, and `RemoveIntakeUseCase` and `RestoreIntakeUseCase` with tests; verify `./gradlew :features:attack:domain:test`

## 2. Presentation

- [x] 2.1 Add the medication presentation models, the `MedicationsViewModel`, `EditMedicationViewModel` and `LogMedicationViewModel`, their view states, notifications and navigation events, and the mappers, with `BaseViewModelTest` tests; verify `./gradlew :features:medication:presentation:test`
- [x] 2.2 Add the intakes, the remove/undo actions and `OnLogMedication(attackId)` to `AttackDetailsViewModel`, and `onMedicationTakenAction` and `onLogMedicationAction` to `HomeViewModel`, with tests; verify the attack and home presentation tests

## 3. Datasource

- [x] 3.1 Add `MedicationLocalModel`, `MedicationIntakeLocalModel`, `MedicationDao`, database version 3 with `AutoMigration(2, 3)` and the exported schema, and the intake move in `mergeInto`, with instrumented DAO tests and a `MigrationTest` from 2 to 3; verify `ANDROID_SERIAL=emulator-5570 ./gradlew :datasource:implementation:connectedDebugAndroidTest`
- [x] 3.2 Add `MedicationDataSource` with its implementation, data models and unit tests (mocked DAO, failure mapping); verify `./gradlew :datasource:implementation:test`

## 4. Data

- [x] 4.1 Add `MedicationRepository` (medication feature), and intake reads and writes in `AttackDetailsRepository`, with tests for every interface and failures as values; verify the data tests

## 5. UI

- [x] 5.1 Build Medication taken (list, time row and picker, logged state with Undo and Done, first-use form, dark-room styling), My medications and the medication editor, with UI models, mappers, previews and unit tests; verify `./gradlew :features:medication:ui:test`
- [x] 5.2 Add the details "Medications" section with remove and Undo, the attack-mode "+ Medication taken" button and the home "Log medication" link, checked at 200% and in landscape; verify the attack and home UI tests

## 6. App

- [x] 6.1 Add the routes, nav host entries, Hilt modules (`MedicationModule`, `MedicationDataModule`, `MedicationUiModule`, `MedicationDataSourceModule`) and navigation mappers with tests; verify `./gradlew :app:test`

## 7. Docs

- [x] 7.1 Update ARCHITECTURE §3 (the medication feature) and §6a (version 3, intakes in merge and purge); verify it matches the code

## 8. Verify

- [x] 8.1 Run the gate and the instrumented tests (datasource, designsystem) on emulator-5570; verify all pass
- [x] 8.2 Walk every scenario on emulator-5570: dark room, details, home, first use, duplicate and archived names, the limit, undo, merge and delete with intakes, 200% font, landscape, TalkBack and keyboard, rotation and process death. Restore the settings and record the results
- [ ] 8.3 Run an independent review, fix the real findings, and validate strictly

## Verification record (2026-10-24)

- **Gate** `./gradlew assembleDebug test ktlintCheck detekt`: BUILD SUCCESSFUL, 679 unit tests, 0 failures.
- **Instrumented** on emulator-5570: datasource 42/42 (with `MedicationDaoTest` and `MigrationTest` 2→3), designsystem
  16/16.
- **Upgrade:** installed over the version 2 database with 16 attacks; `user_version` became 3 and all 16 attacks stayed.
- **Emulator walk (emulator-5570, 1080×2400):**

| Scenario | Result |
|---|---|
| First use from home | Name, type, "Add and log"; an empty name shows "Enter a name." |
| First use in the dark room (EC-MED-08) | Dim form; zolmitriptan, Triptan, logged and linked to the attack in progress |
| Attack mode "+ Medication taken" | Dark room screen; active medications by last use, archived hidden, "preventive" shown |
| Log in the dark room | Linked to the attack, dose copied, zone recorded; "Done" returns to attack mode |
| Details | "Medications" lists the intake; "Add medication" opens the normal screen for the attack |
| Remove on details + Undo | "Medication removed · Undo"; Undo brings it back |
| Home, time inside an ended attack (EC-MED-09) | 12:25 inside 12:20–12:32: linked to that attack |
| Duplicate name | "You already have Ibu."; nothing added |
| Archived name (EC-MED-07) | Comes back as it was, Simple painkiller with 500 mg, same id |
| Archive + Undo | Hidden, then back |
| 50 active, add one more | "You can keep up to 50 medications. Remove one you no longer take."; still 50 |
| 65 characters typed | Stops at 60, "60/60" |
| Delete an attack with intakes (EC-MED-10) | Intakes hidden with it; after the undo window the purge removes them, medications stay |
| Merge with intakes | Covered by `MedicationDaoTest` (intakes move to the survivor); not walked |
| Landscape | The button fits the controls column; the screen keeps the dark room |
| 200% font | Attack mode, Medication taken, My medications fit; home links wrap side by side |
| Keyboard | Tab reaches the time and each row; Enter logs |
| TalkBack labels | "Log Ibuprofen · 400 mg", "Log Topiramate · 25 mg, preventive", "Remove Ibuprofen 400 mg", "Sumatriptan · 50 mg, Triptan. Opens it to edit." |
| Rotation | The logged state and Undo survive |
| Process death | `am kill` in the background: the logged state comes back and Undo works |

- **Found and fixed during the walk:**
  - An archived name took the form's values: the quick form's "Not sure" and empty dose erased the stored type and dose.
    `SaveMedicationUseCase` now only unarchives it (EC-MED-07; design and spec scenario made explicit, new tests).
  - In the dark room, "Add and log", "Done" and the selected type were bright primary. They now use the attack-mode
    colours ("Migraine ended", "+ Medication taken"), never white (18.3 B, EC-UX-01).
  - Medication rows were read twice by screen readers (description and inner text). The texts inside are now cleared,
    as on home (EC-UX-19).
  - "Taken at" did not look changeable: its time now uses the accent colour, as on the details screen.
  - The "Medications" heading on details was indented and not a heading; it now uses the shared section title.
- **Not app defects:** Gboard's stylus onboarding swallowed adb text (handwriting turned off for the walk and turned
  back on); adb drops some shifted letters under load.
- **Known, tracked elsewhere:** QA #20, the landscape undo message over the attack-mode controls, is fixed next in
  `fix-today-zone-and-landscape-message` with #19.
- **Settings restored:** font_scale 1.0, user_rotation 0, accelerometer_rotation 1, animator 1, auto_time_zone 1,
  stylus handwriting back to default, adbd unrooted; walk medications removed.

