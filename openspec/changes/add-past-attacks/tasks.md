# Tasks

## 1. Domain (`:features:attack:domain`)

- [ ] 1.1 Add the models from design D2, replace `FindOverlappingAttackRepository` with `FindOverlappingAttacksRepository`, and add `StorePastAttackRepository` and `MergeAttacksRepository`; verify `./gradlew :features:attack:domain:build`
- [ ] 1.2 Extract `snapToNeighbours` (design D3) and move `UpdateAttackTimesUseCase` and `UndoTimesChangeUseCase` to the list lookup, keeping their tests green and adding:
  - two overlapping attacks named oldest first;
  - snapping with one neighbour that snaps and one that does not.

  Verify `./gradlew :features:attack:domain:test`
- [ ] 1.3 Implement `GetPastAttackDraftUseCase` and `CreatePastAttackUseCase` with tests for:
  - the rounded defaults;
  - the start and the end in the future, inside and beyond the tolerance;
  - an end not after the start;
  - one and two overlaps, touching, and seconds that snap;
  - the store's `Overlapping` race, storage full and failure.

  Verify domain tests pass
- [ ] 1.4 Implement `MergeAttacksUseCase` (design D4) with tests for:
  - the survivor for a new and an edited attack;
  - the range never shrinking and including pain entries;
  - growing until stable;
  - in progress;
  - the start zone;
  - the highest impact;
  - notes joined and cut at 1,000 without splitting a surrogate pair;
  - nothing overlapping any more, for a new and an edited attack;
  - not found, validation, storage full and failure.

  Verify domain tests pass

## 2. Presentation (`:features:attack:presentation`)

- [ ] 2.1 Add the add-screen view state, notifications, navigation events and presentation models from design D6, the range and draft mappers with tests, and the changed `TimeRejectionPresentationModel.Overlaps`; verify `./gradlew :features:attack:presentation:test`
- [ ] 2.2 Implement `AddPastAttackViewModel` with `BaseViewModelTest` tests for every result, and add `onMergeAction`, `AttacksMerged` and `onEnter(attackId, attacksMerged)` to `AttackDetailsViewModel` with tests; verify presentation tests and Konsist `ArchitecturePresentationTest` pass

## 3. Datasources (`:datasource:source`, `:datasource:implementation`)

- [ ] 3.1 Add `overlappingAll`, `insertEndedIfNoOverlap` and `mergeInto` to `AttackDao` (design D5), with instrumented tests for:
  - excluding and not excluding an attack, several overlaps oldest first, and hidden deleted rows;
  - refusing an overlapping insert;
  - a merge moving points, copying tags once, merging the long-attack check, updating the survivor and deleting the others.

  Verify `ANDROID_SERIAL=emulator-5570 ./gradlew :datasource:implementation:connectedDebugAndroidTest`
- [ ] 3.2 Change `AttackDataSource` and `AttackDataSourceImpl` (`overlapping` list, `createEnded`, `merge` with validation), with unit tests for mapping, validation and the storage exceptions; verify `./gradlew :datasource:implementation:testDebugUnitTest`

## 4. Data (`:features:attack:data`)

- [ ] 4.1 Implement `FindOverlappingAttacksRepository`, `StorePastAttackRepository` and `MergeAttacksRepository` in `AttackDetailsRepository`, with a success and a failure-as-value test for each; verify `./gradlew :features:attack:data:test`

## 5. Home (`:features:home:*`)

- [ ] 5.1 Add `OnAddPastAttack`, `onAddPastAttackAction()` with a test, and the "Add a past migraine" link under "Migraine started" in both layouts, with previews; verify `./gradlew :features:home:presentation:test :features:home:ui:build`

## 6. Attack UI (`:features:attack:ui`)

- [ ] 6.1 Add the UI models (`PastAttackDraftUiModel` with `withStart`, `AttackRangeUiModel`, the changed overlap reason), the add-screen event presenter and notification mapper, `AttacksMerged` in the messages and behaviour mapper, with tests; verify `./gradlew :features:attack:ui:testDebugUnitTest`
- [ ] 6.2 Build `AddPastAttack()` and `AddPastAttackContents()` (design D7): the time rows with pickers, inline errors, the overlap card, the tap guard and messages, with previews for loading, ready, overlap and 200% font; verify `./gradlew :features:attack:ui:build`
- [ ] 6.3 On the details screen, list every overlapping attack in the inline error with "Merge them", and show "Migraines merged." after a merge and with `announceMerge`; verify `./gradlew :features:attack:ui:build`

## 7. App wiring

- [ ] 7.1 Add the `AddPastAttack` route, `announceMerge` on `AttackDetails`, the nav host entry, DI for the new use cases, repositories, ViewModel and `AddPastAttackDependencies`, and the navigation mappers with parameterized tests; verify `./gradlew :app:assembleDebug :app:testDebugUnitTest`

## 8. Docs and verify

- [ ] 8.1 Update `docs/ARCHITECTURE.md` §3 (two attack screens) and §6a (store-if-no-overlap, merge transaction); verify each item is documented
- [ ] 8.2 Run `./gradlew assembleDebug test ktlintCheck detekt`; verify BUILD SUCCESSFUL and record the test count
- [ ] 8.3 Run the instrumented tests per module (`:datasource:implementation`, `:designsystem`) on `emulator-5570`; verify all pass
- [ ] 8.4 Walk the `past-attacks`, changed `attack-details` and `today-sky` scenarios on `Migrely_API_36` and record each result:
  - the link in portrait, landscape and attack mode;
  - defaults, moving the start, the end bound;
  - add and open details, back to home, leave without adding;
  - one and two overlaps, "Change the times", touching;
  - merge from the add screen and from details, including into the attack in progress;
  - double tap;
  - rotation and process death with picked times;
  - 200% font, landscape and TalkBack labels in `uiautomator dump`.
- [ ] 8.5 Run `openspec validate add-past-attacks --strict` and `openspec validate --specs --strict`; verify both are valid
