# Tasks

## 1. Feature scaffold

- [ ] 1.1 Run `cag --new-feature --name=Attack --package=com.migrely.app.attack --ktlint --detekt`. Remove the generated stubs, align the build files with `:features:home:*`, and register the modules in `settings.gradle.kts` and `:app`; verify `./gradlew :features:attack:domain:build :features:attack:presentation:build :features:attack:data:build :features:attack:ui:build`

## 2. Domain (`:features:attack:domain`)

- [ ] 2.1 Add the models from design D2 and one single-function repository interface per use case, plus `GetCurrentTimeRepository`; verify the module builds and Konsist `ArchitectureDomainTest` passes
- [ ] 2.2 Implement `GetAttackDetailsUseCase` and `UpdateAttackTimesUseCase` with tests for:
  - a valid change;
  - the start and the end in the future, inside and beyond the 5-minute tolerance;
  - an end before the start;
  - an attempt to set the end of an attack in progress;
  - an overlap, and touching at one instant;
  - points left outside the new range.

  Verify `./gradlew :features:attack:domain:test`
- [ ] 2.3 Implement `UndoTimesChangeUseCase`, `AddIntensityPointUseCase` (range, future, first maximum) and `RemoveIntensityPointUseCase` with tests; verify domain tests pass
- [ ] 2.4 Implement `SelectAttackTagUseCase`, `SetFunctionalImpactUseCase`, `UpdateAttackNoteUseCase` (1,000 limit), `AddCustomTagUseCase` (trim, 1–40, case-insensitive reuse, unarchive, 100 limit, select) and `ArchiveCustomTagUseCase`, and `DeleteAttackUseCase`, with tests; verify domain tests pass

## 3. Presentation (`:features:attack:presentation`)

- [ ] 3.1 Add `AttackDetailsViewState`, the presentation models, notifications and navigation events from design D5, and the domain-to-presentation and presentation-to-domain mappers, with parameterized tests; verify `./gradlew :features:attack:presentation:test`
- [ ] 3.2 Implement `AttackDetailsViewModel` with every action from design D5 and `BaseViewModelTest` tests for each view state, notification and navigation event, and a reload after each successful write; verify presentation tests and Konsist `ArchitecturePresentationTest` pass

## 4. Datasources (`:datasource:source`, `:datasource:implementation`)

- [ ] 4.1 Add the version 2 entities (the new `attack` columns, `AttackTagLocalModel`, `CustomTagLocalModel`) and `@AutoMigration(from = 1, to = 2)`, and commit `schemas/.../2.json`. Add an instrumented `MigrationTest` that opens a version 1 database with an attack and points, migrates it and reads it back; verify `ANDROID_SERIAL=emulator-5570 ./gradlew :datasource:implementation:connectedDebugAndroidTest`
- [ ] 4.2 Add `deleted_epoch_millis IS NULL` to every existing query, and add the new DAO queries and transactions from design D4 (by id, overlapping, update times with point removal, restore times, remove point, maximum before, set tag, impact, note, soft delete, restore, recently deleted with purge), with instrumented DAO tests for the overlap boundaries, the point removal transaction, soft delete hiding rows and the purge; verify the connected tests pass
- [ ] 4.3 Extend `AttackDataSource` and `AttackDataSourceImpl`, and add `TagDataSource`, `TagDataSourceImpl` and their data models and mappers, with unit tests for mapping, validation (impact 0–3, note ≤ 1,000, tag kinds) and the storage exceptions; verify `./gradlew :datasource:implementation:testDebugUnitTest`

## 5. Data (`:features:attack:data`)

- [ ] 5.1 Add the data-to-domain mappers (tags from keys, built-in key tables, custom tags) and the domain-to-data mappers with parameterized tests; verify `./gradlew :features:attack:data:test`
- [ ] 5.2 Implement `AttackDetailsRepository` for every attack repository interface, returning every failure as a value, with a success and a failure test per function; verify data tests pass

## 6. Design system and home

- [ ] 6.1 Move the date-then-time picker to `:designsystem` as `MigrelyDateTimePicker` (design D7) and use it in the home 72-hour card; verify `./gradlew :designsystem:build :features:home:ui:build`
- [ ] 6.2 Home domain, data and presentation (design D3):
  - attack summaries in `AttacksEnded`;
  - `recentlyDeleted` in the snapshot;
  - `RestoreDeletedAttackUseCase`;
  - the `AttackDeleted` notification;
  - `onUndoDeleteAction` and `onAttackDetailsAction`;
  - `OnViewAttackDetails`.

  Add tests; verify `./gradlew :features:home:domain:test :features:home:data:test :features:home:presentation:test`
- [ ] 6.3 Home UI:
  - today's attack list under the card, with times, the earlier day and the peak;
  - the dim "Details" link in attack mode;
  - `AttackDeleted` as a confirmation with undo that is timed from the deletion.

  Add tests and previews; verify `./gradlew :features:home:ui:testDebugUnitTest`

## 7. Attack UI (`:features:attack:ui`)

- [ ] 7.1 Add the UI models, `AttackDetailsViewStateUiMapper`, `AttackTimeUiFormatter`, the label mappers for built-in tags, locations and impact, and the message models and behaviour mapper, with tests; verify `./gradlew :features:attack:ui:testDebugUnitTest`
- [ ] 7.2 Build `AttackDetails()` and `AttackDetailsContents()`:
  - the time section with pickers and inline errors;
  - the pain section with its dialog and slider;
  - the safety card;
  - chips for location, symptoms and triggers, with custom tag add and remove;
  - impact;
  - the note with its counter and autosave on pause and dispose;
  - delete;
  - messages.

  Add previews for each view state and 200% font; verify `./gradlew :features:attack:ui:build`

## 8. App wiring

- [ ] 8.1 Add `AttackModule`, `AttackDataModule`, `AttackUiModule` and the `TagDataSource` binding, the `AttackDetails(attackId)` route and nav host entry, and both navigation mappers with parameterized tests; verify `./gradlew :app:assembleDebug :app:testDebugUnitTest`

## 9. Docs and verify

- [ ] 9.1 Update `docs/ARCHITECTURE.md` §3 (attack feature, tag package, picker), §6a (soft delete, purge, auto-migrations with tests); verify each item is documented
- [ ] 9.2 Run `./gradlew assembleDebug test ktlintCheck detekt`; verify BUILD SUCCESSFUL and record the test count
- [ ] 9.3 Run `ANDROID_SERIAL=emulator-5570 ./gradlew connectedDebugAndroidTest`; verify all instrumented tests pass
- [ ] 9.4 Walk the `attack-details`, `custom-tags`, `attack-mode` and `today-sky` scenarios on the `Migrely_API_36` emulator and record each result:
  - open from the list and from attack mode;
  - edit times, with each inline error;
  - add and remove points with undo, and the shortening undo;
  - the first-10 card;
  - chips, custom tags with duplicates, archive and limit;
  - impact, and the note with back and process death;
  - delete and undo from home;
  - 200% font, landscape and TalkBack labels in `uiautomator dump`.
- [ ] 9.5 Run `openspec validate add-attack-details --strict`; verify it is valid

## Workflow follow-up

- Commit per task group with the session's attribution, push `feat/add-attack-details` and open a PR stacked on PR #3.
- Archive after review.
