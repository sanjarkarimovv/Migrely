# Design

## Context

After `add-attack-logging` and `refine-messages-and-type`:
- **Storage:** attacks and intensity points live in Room v1. `AttackDataSource` serves only the home feature's quick
  actions, and `delete` removes a row for good (used by "undo start").
- **Messages:** the home screen shows them with `MigrelySnackbarHost` through `HomeMessages`.
- **Pickers:** the date-then-time picker is private to the home 72-hour card.
- **Navigation:** it has one route, `Home(startAttack)`, and `HomePresentationNavigationEvent` has no subtypes.

See proposal.md for motivation and the specs for the required behaviour. Modules, in checklist order:
1. new `:features:attack:{domain,presentation,data,ui}`;
2. `:datasource:{source,implementation}`;
3. `:features:home:*`;
4. `:designsystem`;
5. `:app`.

## Goals / Non-Goals

**Goals:**
- One details screen that edits every stored field of an attack, with each change written at once.
- A storage shape that change 1c (merge), 2 (history), 10 (report) and 11 (insights) can read without another schema
  change for tags.
- Undo everywhere data is removed, with no dialogs.

**Non-Goals:**
- No past-attack creation or merging (change 1c).
- No medication rows (change 3).
- No E2E test suite. Room migrations and DAO SQL get instrumented tests; everything else gets unit tests.

## Decisions

### D1. A separate `attack` feature

The details screen is its own feature, generated with
`cag --new-feature --name=Attack --package=com.migrely.app.attack --ktlint --detekt`. It has its own models and
single-function repositories over the shared `AttackDataSource` and the new `TagDataSource`. Home keeps its quick actions
and only gains navigation events. Features never depend on each other (skill rule 6, profile §3).

### D2. Domain (`:features:attack:domain`)

**Models:**

| Model | Fields / cases |
|---|---|
| `AttackDetailsDomainModel` | `id`, `start`, `startZone`, `end?`, `intensityPoints`, `locations: Set<HeadLocationDomainModel>`, `symptoms: Set<SymptomDomainModel>`, `triggers: Set<TriggerDomainModel>`, `functionalImpact: FunctionalImpactDomainModel?`, `note` |
| `HeadLocationDomainModel` | `Left`, `Right`, `BothSides`, `Forehead`, `BackOfHead`, `AroundEyes`, `WholeHead` |
| `SymptomDomainModel` (sealed) | `BuiltIn(BuiltInSymptomDomainModel)` · `Custom(CustomTagDomainModel)` |
| `BuiltInSymptomDomainModel` | `Aura`, `Nausea`, `Vomiting`, `LightSensitivity`, `SoundSensitivity`, `SmellSensitivity`, `Dizziness`, `NeckPain` |
| `TriggerDomainModel` (sealed) | `BuiltIn(BuiltInTriggerDomainModel)` · `Custom(CustomTagDomainModel)` |
| `BuiltInTriggerDomainModel` | `Stress`, `TooLittleSleep`, `TooMuchSleep`, `SkippedMeal`, `Dehydration`, `Alcohol`, `Caffeine`, `Period`, `Weather`, `BrightLightOrScreens`, `Smell`, `PhysicalEffort`, `Travel` |
| `CustomTagDomainModel` | `id`, `kind: TagKindDomainModel` (`Symptom`, `Trigger`), `name`, `archived` |
| `FunctionalImpactDomainModel` | `None` 0, `LessActive` 1, `HadToStop` 2, `BedRest` 3 |
| `AttackTagDomainModel` (sealed) | `Location(HeadLocationDomainModel)` · `Symptom(SymptomDomainModel)` · `Trigger(TriggerDomainModel)` |
| `AttackDetailsScreenDomainModel` | `details`, `customSymptoms`, `customTriggers` (active only), `now` |

**Use cases.** Each one is a `BackgroundExecutingUseCase` and returns a sealed result. Every result also has
`NotFound`, `StorageFull` and `Failed` unless the table says otherwise.

| Use case | Request | Result cases | Rules |
|---|---|---|---|
| `GetAttackDetailsUseCase` | `attackId` | `Loaded(screen)` · `NotFound` · `Failed` | — |
| `UpdateAttackTimesUseCase` | `attackId`, `start`, `end?` | `Updated(previousStart, previousEnd, removedPoints)` · `InFuture(field)` · `EndBeforeStart` · `Overlaps(otherStart, otherEnd?)` | (a) A time more than 5 minutes after now is rejected (EC-ATK-03). (b) The end of an attack in progress cannot be set. (c) The new range must not overlap another non-deleted attack; touching at one instant is allowed. (d) Points outside the new range are removed in the same transaction (EC-ATK-09). |
| `UndoTimesChangeUseCase` | `attackId`, `start`, `end?`, `points` | `Restored` · `NotPossible` | Restores times and points. `NotPossible` when the old range now overlaps another attack. |
| `AddIntensityPointUseCase` | `attackId`, `at`, `value` 1–10 | `Added(firstMaximum)` · `OutsideAttack` · `InFuture` | `firstMaximum` is true when `value == 10` and no non-deleted attack had a 10 before (SAF-F2). |
| `RemoveIntensityPointUseCase` | `attackId`, `point` | `Removed(point)` | Removes one entry matching `at` and `value`. |
| `SelectAttackTagUseCase` | `attackId`, `tag`, `selected` | `WriteResultDomainModel` | Selects or clears one tag. This is idempotent, so quick double taps never lose a change. |
| `SetFunctionalImpactUseCase` | `attackId`, `impact?` | `WriteResultDomainModel` | — |
| `UpdateAttackNoteUseCase` | `attackId`, `note` | `Saved` · `TooLong` | At most 1,000 characters. |
| `AddCustomTagUseCase` | `attackId`, `kind`, `name` | `Added(tag)` · `Invalid` · `LimitReached` | (a) Trim the name; 1–40 characters. (b) A case-insensitive match is reused, and unarchived if needed. (c) Otherwise it is created when fewer than 100 active tags of that kind exist. (d) The tag is selected on the attack. |
| `ArchiveCustomTagUseCase` | `tagId` | `WriteResultDomainModel` | EC-ATK-11 |
| `DeleteAttackUseCase` | `attackId` | `Deleted` | Soft delete (D4). |

"Now" comes from `GetCurrentTimeRepository`. The 5-minute tolerance is `CLOCK_TOLERANCE`.

### D3. Home additions (`:features:home`)

- `TodayDomainModel.AttacksEnded` gains `attacks: List<AttackSummaryDomainModel(id, start, startZone, end, peak?)>`,
  oldest first.
- `TodaySnapshotDomainModel` gains `recentlyDeleted: DeletedAttackDomainModel(id, deletedAt)?`. This is the newest
  deletion of the last 2 minutes.
- New `RestoreDeletedAttackUseCase(attackId)` returns `Restored`, `NotPossible` or `Failed`. `NotPossible` means
  restoring would make two attacks in progress or an overlap.
- `HomeViewModel` changes:
  - after loading today it notifies `AttackDeleted(attackId, deletedAt)` when `recentlyDeleted` is set;
  - `onUndoDeleteAction(attackId)` restores and then notifies `Undone` or `UndoNotPossible`;
  - `onAttackDetailsAction(attackId)` emits `HomePresentationNavigationEvent.OnViewAttackDetails(attackId)`.
- `HomeMessageUiModel.AttackDeleted(attackId, deletedAtEpochMillis)` is a confirmation with undo, 5 s base.
  - Home offers each deletion once: `HomeMessages` remembers the deletion it offered in `rememberSaveable`, so a reload
    or a rotation does not offer it again after a dismissal.
  - The window starts when home shows the message, so the trip back from the details screen does not shorten it.
  - The emulator walk showed the alternative, timing from the deletion itself, left only about 3.5 seconds on a slow
    device.

### D4. Storage: database v2, soft delete, tags

**`attack` table.** It gains three columns: `functional_impact INTEGER NULL`, `note TEXT NULL` and
`deleted_epoch_millis INTEGER NULL`.

**`attack_tag` table.**
- Columns: `attack_id`, `kind` (`LOCATION`, `SYMPTOM` or `TRIGGER`) and `tag_key`.
- The primary key is all three columns. `attack_id` cascades on delete. There is an index on `(kind, tag_key)`.
- Built-in keys are stable snake_case names (`aura`, `too_little_sleep`). Custom keys are `custom:<uuid>`.

**`custom_tag` table.**
- Columns: `id`, `kind` (`SYMPTOM` or `TRIGGER`), `name`, `name_key` (trimmed and lower-cased), `archived` and
  `created_epoch_millis`.
- There is a unique index on `(kind, name_key)`.
- Custom tags are never deleted, so their keys in `attack_tag` always resolve.

**Migration and queries.**
- `@AutoMigration(from = 1, to = 2)` writes the migration, because version 2 only adds things.
- `MigrationTestHelper` checks the upgrade on a version 1 database with data (EC-STO-01).
- Every existing query gains `deleted_epoch_millis IS NULL`. That includes "in progress", "touching", "previous end"
  and "start if none in progress".

**Soft delete and purge.**
- `softDelete(id, at)` sets `deleted_epoch_millis`, and `restore(id)` clears it.
- Rows deleted more than 2 minutes ago (D23) are purged inside the datasource whenever `softDelete` or
  `recentlyDeleted` runs. That is housekeeping only the datasource knows about.
- The existing hard `delete` stays for "undo start", where the attack should never have existed.

**Datasource interfaces.**
- New `TagDataSource` in `com.migrely.app.datasource.tag`: `customTags(kind)`, `findByNameKey(kind, nameKey)`,
  `insert`, `setArchived` and `countActive(kind)`.
- `AttackDataSource` gains:
  - `byId`;
  - `overlapping(start, end?, excludingId)`;
  - `updateTimes(id, start, end?, removePointsOutside)`, as one transaction that returns the removed points;
  - `restoreTimes(id, start, end?, points)`;
  - `removeIntensityPoint(id, at, value)`;
  - `hasAnyMaximumBefore(excludingId)`;
  - `setTag(id, kind, key, selected)`;
  - `setFunctionalImpact` and `setNote`;
  - `softDelete`, `restore` and `recentlyDeleted(since)`.

### D5. Presentation (`:features:attack:presentation`)

- **View state:** `AttackDetailsViewState` is `Loading`, `Ready(details: AttackDetailsPresentationModel)` or
  `NotFound`.
- **Notifications:**
  - `TimeRejected(field, reason)`, where `reason` is `InFuture`, `EndBeforeStart` or `Overlaps(start, end?)`;
  - `PointRejected(reason)`, where `reason` is `OutsideAttack` or `InFuture`;
  - `PointsRemovedByTimeChange(count, undo: TimesUndoPresentationModel)`;
  - `PointRemoved(point)`, `FirstMaximumRecorded`, `TagLimitReached`, `Undone`, `UndoNotPossible`, `StorageFull`
    and `SomethingWentWrong`.
- **Navigation:** `AttackDetailsPresentationNavigationEvent` is `OnLeave` or `OnAttackDeleted`.
- **Actions:**
  - `onEnter(attackId)` and `onResume(attackId)`;
  - `onStartAction`, `onEndAction`, `onAddPointAction`, `onRemovePointAction`, `onUndoPointRemovalAction` and
    `onUndoTimesChangeAction`;
  - `onTagAction(tag, selected)`, `onImpactAction`, `onNoteAction` and `onAddCustomTagAction`;
  - `onArchiveCustomTagAction`, `onDeleteAction` and `onLeaveAction`.

  Each takes the attack id and everything it needs as parameters; the ViewModel holds no state (skill rule 4). After
  every successful write it reloads the details.

### D6. UI (`:features:attack:ui`)

**Layout.** One scrolling column with sections in this order:
1. the safety card, when shown;
2. time;
3. pain;
4. where it hurts;
5. symptoms;
6. triggers;
7. impact on your day;
8. notes;
9. "Delete migraine".

The top app bar has a back arrow. Sections use `titleMedium`, and the screen title uses `titleLarge` (Fraunces).

**Controls.**
- Choices use `FilterChip`s in `FlowRow`s. Impact is a single-choice list with `selectable` semantics; tapping the
  selected one clears it.
- The pain dialog uses a Material `Slider` from 1 to 10 with steps, so accessibility actions come built in. Its time
  row opens the date-time picker, bounded to the attack.
- A custom tag is added through a dialog with a 40-character text field. A custom tag is removed from the list with a
  long press or the accessibility action "Remove from my list".

**State and saving.**
- Inline time and point errors are kept in `rememberSaveable` and cleared on the next change.
- The note is local `rememberSaveable` text once edited. It is saved 600 ms after typing stops, on `ON_PAUSE` and on
  dispose. That covers back press and process death (EC-ATK-10).

**Messages and formatting.**
- Messages follow the home pattern: an `AttackMessageUiModel` that is parcelable, a behaviour mapper, a saveable
  shown message and `MigrelySnackbarHost`.
- Undo payloads, the previous times and the removed points, travel inside the message model, so the ViewModel stays
  stateless.
- Times use the attack's start zone and the device's 12- or 24-hour setting. That is done by `AttackTimeUiFormatter`,
  the attack feature's own formatter (features share no UI code).

### D7. Date-time picker in `:designsystem`

`MigrelyDateTimePicker(initialEpochMillis, minEpochMillis, maxEpochMillis, zoneId, title, outOfRangeText, onConfirm,
onDismiss)` shows the Material date picker and then the time picker.
- Dates are limited to the range, and confirming is disabled outside it.
- Home's 72-hour "It has ended" flow and every picker on the details screen use it.
- The upper bound passed by the details screen is now + 5 minutes, and the domain checks again.

### D8. Navigation (`:app`)

- **Route:** `@Serializable data class AttackDetails(val attackId: String)` is added to `Routes.kt`, which removes the
  single-route ktlint suppression.
- **Home mapper:** `HomeNavigationEventDestinationMapper` maps `OnViewAttackDetails(id)` to
  `navController.navigate(AttackDetails(id))`.
- **Details mapper:** `AttackDetailsNavigationEventDestinationMapper` maps both `OnLeave` and `OnAttackDeleted` to
  `popBackStack()`. Home reloads on resume and shows the deleted message.

Both mappers have parameterized tests.

## Risks / Trade-offs

- **Undo after a purge.** A deletion undone after the 2-minute purge would find no row. The home message cannot outlive
  the window, and the longest accessibility timeout is 2 minutes, so `NotPossible` covers the race.
- **Autosave reloads on every write.** The note field ignores incoming state once the user has edited it, so reloads
  never overwrite typing.
- **The schema grows by two tables now.** The alternative, columns per tag, would need a migration for every new
  built-in tag.
- **No merge yet.** Overlapping edits are refused until 1c. The message names the other attack, so the user can fix the
  times.
