# Tasks

## 1. Domain (`:features:attack:domain`)

- [x] 1.1 Add the models from design D2, replace `FindOverlappingAttackRepository` with `FindOverlappingAttacksRepository`, and add `StorePastAttackRepository` and `MergeAttacksRepository`; verify `./gradlew :features:attack:domain:build`
- [x] 1.2 Extract `snapToNeighbours` (design D3) and move `UpdateAttackTimesUseCase` and `UndoTimesChangeUseCase` to the list lookup, keeping their tests green and adding:
  - two overlapping attacks named oldest first;
  - snapping with one neighbour that snaps and one that does not.

  Verify `./gradlew :features:attack:domain:test`
- [x] 1.3 Implement `GetPastAttackDraftUseCase` and `CreatePastAttackUseCase` with tests for:
  - the rounded defaults;
  - the start and the end in the future, inside and beyond the tolerance;
  - an end not after the start;
  - one and two overlaps, touching, and seconds that snap;
  - the store's `Overlapping` race, storage full and failure.

  Verify domain tests pass
- [x] 1.4 Implement `MergeAttacksUseCase` (design D4) with tests for:
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

- [x] 2.1 Add the add-screen view state, notifications, navigation events and presentation models from design D6, the range and draft mappers with tests, and the changed `TimeRejectionPresentationModel.Overlaps`; verify `./gradlew :features:attack:presentation:test`
- [x] 2.2 Implement `AddPastAttackViewModel` with `BaseViewModelTest` tests for every result, and add `onMergeAction`, `AttacksMerged` and `onEnter(attackId, attacksMerged)` to `AttackDetailsViewModel` with tests; verify presentation tests and Konsist `ArchitecturePresentationTest` pass

## 3. Datasources (`:datasource:source`, `:datasource:implementation`)

- [x] 3.1 Add `overlappingAll`, `insertEndedIfNoOverlap` and `mergeInto` to `AttackDao` (design D5), with instrumented tests for:
  - excluding and not excluding an attack, several overlaps oldest first, and hidden deleted rows;
  - refusing an overlapping insert;
  - a merge moving points, copying tags once, merging the long-attack check, updating the survivor and deleting the others.

  Verify `ANDROID_SERIAL=emulator-5570 ./gradlew :datasource:implementation:connectedDebugAndroidTest`
- [x] 3.2 Change `AttackDataSource` and `AttackDataSourceImpl` (`overlapping` list, `createEnded`, `merge` with validation), with unit tests for mapping, validation and the storage exceptions; verify `./gradlew :datasource:implementation:testDebugUnitTest`

## 4. Data (`:features:attack:data`)

- [x] 4.1 Implement `FindOverlappingAttacksRepository`, `StorePastAttackRepository` and `MergeAttacksRepository` in `AttackDetailsRepository`, with a success and a failure-as-value test for each; verify `./gradlew :features:attack:data:test`

## 5. Home (`:features:home:*`)

- [x] 5.1 Add `OnAddPastAttack`, `onAddPastAttackAction()` with a test, and the "Add a past migraine" link under "Migraine started" in both layouts, with previews; verify `./gradlew :features:home:presentation:test :features:home:ui:build`

## 6. Attack UI (`:features:attack:ui`)

- [x] 6.1 Add the UI models (`PastAttackDraftUiModel` with `withStart`, `AttackRangeUiModel`, the changed overlap reason), the add-screen event presenter and notification mapper, `AttacksMerged` in the messages and behaviour mapper, with tests; verify `./gradlew :features:attack:ui:testDebugUnitTest`
- [x] 6.2 Build `AddPastAttack()` and `AddPastAttackContents()` (design D7): the time rows with pickers, inline errors, the overlap card, the tap guard and messages, with previews for loading, ready, overlap and 200% font; verify `./gradlew :features:attack:ui:build`
- [x] 6.3 On the details screen, list every overlapping attack in the inline error with "Merge them", and show "Migraines merged." after a merge and with `announceMerge`; verify `./gradlew :features:attack:ui:build`

## 7. App wiring

- [x] 7.1 Add the `AddPastAttack` route, `announceMerge` on `AttackDetails`, the nav host entry, DI for the new use cases, repositories, ViewModel and `AddPastAttackDependencies`, and the navigation mappers with parameterized tests; verify `./gradlew :app:assembleDebug :app:testDebugUnitTest`

## 8. Docs and verify

- [x] 8.1 Update `docs/ARCHITECTURE.md` §3 (two attack screens) and §6a (store-if-no-overlap, merge transaction); verify each item is documented
- [x] 8.2 Run `./gradlew assembleDebug test ktlintCheck detekt`; verify BUILD SUCCESSFUL and record the test count
- [x] 8.3 Run the instrumented tests per module (`:datasource:implementation`, `:designsystem`) on `emulator-5570`; verify all pass
- [x] 8.4 Walk the `past-attacks`, changed `attack-details` and `today-sky` scenarios on `Migrely_API_36` and record each result:
  - the link in portrait, landscape and attack mode;
  - defaults, moving the start, the end bound;
  - add and open details, back to home, leave without adding;
  - one and two overlaps, "Change the times", touching;
  - merge from the add screen and from details, including into the attack in progress;
  - double tap;
  - rotation and process death with picked times;
  - 200% font, landscape and TalkBack labels in `uiautomator dump`.
- [x] 8.5 Run `openspec validate add-past-attacks --strict` and `openspec validate --specs --strict`; verify both are valid

## Verification record (2026-10-23)

- 8.2 Gate `./gradlew assembleDebug test ktlintCheck detekt`: BUILD SUCCESSFUL. 450 unit tests (debug variants), 0
  failures:

  | Module | Tests |
  |---|---|
  | attack domain | 85 |
  | attack data | 43 |
  | attack presentation | 49 |
  | attack UI | 40 |
  | home domain | 51 |
  | home data | 26 |
  | home presentation | 32 |
  | home UI | 39 |
  | datasource | 58 |
  | app (Konsist and navigation mappers) | 23 |
  | designsystem | 4 |

- 8.3 Instrumented tests on `Migrely_API_36`, per module:
  - datasource 30/30, including the 7 new `AttackDaoPastAttacksTest` cases for listing overlaps, refusing an
    overlapping insert and the merge transaction;
  - designsystem 7/7.
- 8.4 Emulator walk (`emulator-5570`, 12-hour clock, Asia/Tashkent), as specified unless noted:
  - **Home link:** "Add a past migraine" sits under "Migraine started" in portrait and landscape, also at 200%. It is
    not shown in attack mode.
  - **Defaults:** opened at 23:25, the screen showed 21:25 to 23:25.
  - **Moving the start:**
    - yesterday 21:00 moved the end to yesterday 23:00 and kept "2 h 0 min";
    - today 23:00 capped the end at the current minute, 23:37.
  - **End bound:** the end picker enabled only the start's day up to today.
  - **Add and back:**
    - "Add migraine" opened the new attack's details with "Not recorded", and back returned home;
    - back from the add screen stored nothing.
  - **Overlaps:**
    - one overlap named the 19:43–19:44 migraine;
    - an attack from yesterday 22:00 to today 20:00 named both overlapping migraines, oldest first;
    - three overlaps were named at 200% in landscape, where everything was reachable by scrolling;
    - "Change the times" closed the card and kept the times.
  - **Touching and seconds:**
    - an attack starting at the minute another ended was stored;
    - an attack picked from 23:40, after one that ended at 23:40:4x, was stored with its start snapped to that end
      (duration "0 min"), with no card.
  - **Merging:**
    - from the add screen, the earliest attack survived with the union of the times, and "Migraines merged." showed;
    - from the details screen, the edited attack kept "Nausea", gained the other attack's pain entry and its start;
    - three attacks were merged into one;
    - moving the start of the attack in progress into an ended one merged them, and home stayed in attack mode,
      "Started 11:00 PM".
  - **Double tap:** a double tap on "Add migraine" stored one attack and opened one details screen.
  - **Interruptions:**
    - picked times survived rotation, and a process killed in the background;
    - after a merge, neither rotation nor process death showed "Migraines merged." again.
  - **TalkBack labels** (`uiautomator dump`): the rows read "Change when it started, now Thu, Oct 22, 9:00 PM", and
    each row's focusable node is the clickable parent.
  - **Settings restored:** font 1.0, rotation 0, auto-rotate on, animator 1, and adbd back to non-root.
- Found and fixed during the walk:
  - With the overlap card shown, "Add migraine" competed with the card's two choices and could only bring the card
    back. The card now takes its place (spec scenario updated).
  - The spec's "Merge with the attack in progress" scenario described a path the app does not offer: an ended
    attack's details cannot be opened while another attack is in progress. It now describes the reachable path, from
    the attack in progress. The domain test covers both directions.
- **One ANR during the walk, environmental.** While another session started an emulator, the host load average was
  about 20 and that emulator used 190% CPU. A swipe on the details screen then waited 5 s for input dispatch.
  - The ANR trace shows Migrely's main thread idle in `Looper.pollOnce`. A concurrent GC took 10.3 s of wall time
    instead of the usual 0.4 to 1.4 s.
  - Afterwards the app's input channel was `responsive=true`, the app used 0% CPU at rest, and the ANR did not happen
    again.
- Not walked on the emulator, covered by tests:
  - times in the future and the clock tolerance, which needs a clock change while the screen is open (domain and
    ViewModel tests);
  - joined notes, the highest impact and the 1,000-character cut (domain tests), and points and tags moving in one
    transaction (DAO test);
  - the overlap card's spoken announcement: the card text is a polite live region, but the emulator has no audio.
- 8.5 `openspec validate add-past-attacks --strict` and `openspec validate --specs --strict`: valid.
