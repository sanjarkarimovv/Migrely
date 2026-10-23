# Design

## Context

After `setup-project-foundation` the project has:

- `:features:home:*` with a single `Ready` view state;
- empty `:datasource:source` and `:datasource:implementation` JVM modules;
- the theme in `:app/ui/theme`;
- Nav2 type-safe routes with one `Home` object.

There is no persistence and no time handling. The project profile (`docs/ARCHITECTURE.md`) fixes the layering, naming,
failure routing (§7: background use cases never throw; failures return as sealed values) and the quality gate.
See proposal.md for motivation and the specs for the required behaviour.

## Goals / Non-Goals

**Goals:**
- One-tap start and end that cannot create two attacks in progress, even with double taps or the shortcut.
- A storage model that already serves the detail screen (change 1b) and the history calendar (change 2), so neither
  needs a schema change for attacks and intensity points.
- Sky drawing in one shared place, reused by today's card now and the month calendar later.

**Non-Goals:**
- No detail editing, tags, past attacks or merging (change 1b).
- No DataStore, reminders or WorkManager (the 72-hour check runs when the home screen opens).
- No end-to-end UI test suite. Room SQL is covered by one instrumented DAO test run on the emulator.

## Decisions

### D1. The home feature owns today, attack mode and quick actions

Start, intensity, move-start, end, undo and the 72-hour check all happen on the home screen, so they live in
`:features:home:{domain,presentation,data,ui}`. The detail screen in change 1b becomes a separate `attack` feature with
its own models and repositories over the same datasource.

- **Alternative:** a shared `attack` feature used by home. Rejected because features must not depend on each other
  (skill rule 6, profile §3).

### D2. Domain model and use cases (`:features:home:domain`)

**Models:**

| Model | Fields / cases |
|---|---|
| `AttackDomainModel` | `id`, `start: Instant`, `startZone: ZoneId`, `end: Instant?`, `intensityPoints: List<IntensityPointDomainModel>`, `longAttackCheck: LongAttackCheckDomainModel` |
| `IntensityPointDomainModel` | `at: Instant`, `value: Int` (1–10) |
| `LongAttackCheckDomainModel` | `confirmedAt: Instant?`, `disabled: Boolean` |
| `TodayDomainModel` (sealed) | `Calm` · `AttackInProgress(attack, longAttackCheckDue)` · `AttacksEnded(count, peakIntensity: Int?)` |

**Result models** carry expected failures as values (profile §7):

| Result model | Cases |
|---|---|
| `StartAttackResultDomainModel` | `Started(attack)` · `AlreadyInProgress(attack)` · `StorageFull` · `Failed` |
| `EndAttackResultDomainModel` | `Ended` · `NotInProgress` · `StorageFull` · `Failed` |
| `MoveStartResultDomainModel` | `Moved` · `OverlapsPrevious(previousEnd)` · `NotInProgress` · `StorageFull` · `Failed` |
| `RecordIntensityResultDomainModel` | `Recorded` · `NotInProgress` · `StorageFull` · `Failed` |
| `UndoResultDomainModel` | `Undone` · `NotPossible` · `Failed` |

Each use case has one single-function repository interface (skill rule 3):

| Use case | Kind | Rule it owns |
|---|---|---|
| `ObserveTodayUseCase` | continuous | Classifies today. `longAttackCheckDue` = not disabled and `now − (confirmedAt ?: start) ≥ 72 h` |
| `StartAttackUseCase` | background | Delegates to an atomic start-if-none in the repository (D5) |
| `RecordIntensityUseCase(level)` | background | Mild 3 / Moderate 6 / Severe 9. Replaces the last point if it is less than 2 minutes old |
| `MoveAttackStartEarlierUseCase(minutes)` | background | Rejects a new start earlier than the previous attack's end |
| `EndAttackUseCase(at: Instant?)` | background | `null` means now. The 72-hour "It has ended" path passes the picked time, validated within `[start, now]` |
| `UndoAttackStartUseCase(id)` | background | Deletes only an attack that is still in progress |
| `UndoAttackEndUseCase(id)` | background | Reopens only if no other attack is in progress |
| `AnswerLongAttackCheckUseCase(id, answer)` | background | `StillGoing` sets `confirmedAt = now`; `DontAskAgain` sets `disabled` |

"Now" comes from a `CurrentTimeRepository`, so rules are testable with fixed instants.

**Day logic.** An attack touches day `D` when `[start, end ?: now)` overlaps the start of `D` until the start of
`D + 1`. Both bounds are taken in the attack's start zone for its own day, and in the device zone for "today"
(EC-TIM-02, EC-ATK-06). Durations always use `Instant`s (EC-TIM-03).

### D3. Undo is driven by the UI, as skill rule 4 requires

The ViewModel stays stateless. On success it emits `AttackStarted(attackId)` or `AttackEnded(attackId)` notifications.
The UI shows a 5-second snackbar and, on "Undo", passes the id back through `onUndoStartAction(id)` or
`onUndoEndAction(id)`. The domain guards in D2 make a late or duplicated undo harmless.

### D4. Data layer (`:features:home:data`)

`HomeRepository` implements every home repository interface. It maps `AttackDataModel` to `AttackDomainModel` with
mapper classes and translates datasource exceptions into the result values:
- `StorageFullDataException` becomes `StorageFull`;
- any other `DataException` becomes `Failed`.

A unit test for each repository function proves that failures come back as values.

`ObserveTodayUseCase` streams from `HomeRepository.observeToday()`. That flow combines three sources:
- the attack in progress;
- the attacks touching today;
- a date ticker from the clock datasource, so the card resets at midnight.

### D5. Datasources: Room for attacks, a clock resource

**`:datasource:source`** (JVM). Package `com.migrely.app.datasource.<resource>.{datasource,model,exception}`:

- `AttackDataSource`:
  - `observeInProgress()`
  - `observeTouching(from, to)`
  - `startIfNoneInProgress(start, zone): AttackStartDataModel`
  - `addIntensityPoint`
  - `replaceLatestIntensityPoint`
  - `updateStart`
  - `previousEndBefore(start)`
  - `end`
  - `reopen`
  - `delete`
  - `updateLongAttackCheck`
- `ClockDataSource`: `now()`, `zone()`, `observeDate(): Flow<LocalDate>`. The flow emits now and again at each local
  midnight.
- `AttackDataModel`, `IntensityPointDataModel`, `StorageFullDataException`, `DatabaseDataException`.

**`:datasource:implementation`** becomes an Android library with Room 2.8.5 via KSP and the Room Gradle plugin:

| Element | Contents |
|---|---|
| Database | `MigrelyDatabase`, version 1 |
| Tables | `AttackLocalModel` (`id` UUID, `startEpochMillis`, `startZoneId`, `endEpochMillis?`, `longCheckConfirmedEpochMillis?`, `longCheckDisabled`, `createdEpochMillis`, `updatedEpochMillis`; index on start and end) and `IntensityPointLocalModel` (`attackId` FK with cascade delete, `atEpochMillis`, `value` with a CHECK of 1–10) |
| DAO | `AttackDao` |
| Mappers | `AttackLocalMapper` |
| Implementations | `AttackDataSourceImpl`, `ClockDataSourceImpl` |

**Rules:**
- `startIfNoneInProgress` and the move-start overlap check run inside one Room `@Transaction`. This is what guarantees
  a single attack in progress (EC-ATK-01).
- `SQLiteFullException` becomes `StorageFullDataException`. Other `SQLiteException`s become `DatabaseDataException`.
- `exportSchema = true` with schemas in `datasource/implementation/schemas`. No `fallbackToDestructiveMigration`, ever
  (EC-STO-01).

**Alternative: one datasource module per resource.** Rejected per profile §3.

### D6. `:designsystem` module for the theme and the sky

A new Android library (Compose only, with no dependency on any feature or presentation module) holds:

- `MigrelyTheme` and its palette, moved from `:app/ui/theme`;
- `SkyCanvas(appearance: SkyAppearance, animate: Boolean)`, drawn with Compose `Canvas`. `SkyAppearance` covers
  `Calm`, `Hazy`, `Cloudy`, `Storm` and `Rain`. Shapes follow the style frames: stars, waves, clouds made of circles,
  a still amber lightning line and rain strokes. Outlines get a small deterministic jitter for the hand-drawn feel;
  there is no runtime noise filter;
- `rememberReduceMotion()`, which reads `Settings.Global.ANIMATOR_DURATION_SCALE == 0f` and refreshes on resume.

Feature UI mappers convert their own UI models to `SkyAppearance`, so no feature model crosses into the design system
(rule 6). Feature previews use the real theme.

- **Alternative: keep the theme in `:app`, as the sample does.** Rejected because two features (home now, history next)
  need the same sky drawing, and profile §3 allows a dedicated shared module with its own reason to exist. The profile
  is updated.

### D7. Presentation (`:features:home:presentation`)

**`HomeViewState`:**
- `Loading`
- `Today(TodaySkyPresentationModel)`, where the sky is `Calm` or `Ended(count, peak: IntensityLevelPresentationModel)`
  and the level is `Mild`, `Moderate`, `Severe` or `NotRecorded`
- `AttackInProgress(AttackInProgressPresentationModel)`, carrying `id`, `start`, `startZone`, `currentLevel` and
  `longAttackCheckDue`

**`HomePresentationNotification`:**
- `AttackStarted(id)`
- `AttackEnded(id)`
- `StorageFull`
- `StartMoveRejected(previousEnd)`
- `SomethingWentWrong`

There are no navigation events yet.

**`HomeViewModel` functions** (all public functions are events, rule 5):

| Function | Purpose |
|---|---|
| `onEnter(startAttackRequested)` | starts observing today and, if requested by the shortcut, starts an attack |
| `onStartAttackAction()` | start |
| `onIntensityAction(level)` | record intensity |
| `onMoveStartEarlierAction(minutes)` | move the start earlier |
| `onEndAttackAction()` | end now |
| `onUndoStartAction(id)` | undo a start |
| `onUndoEndAction(id)` | undo an end |
| `onStillGoingAction(id)` | 72-hour check: still going |
| `onDontAskAgainAction(id)` | 72-hour check: don't ask again |
| `onEndedAtAction(id, end)` | 72-hour check: it has ended at the picked time |

### D8. UI (`:features:home:ui`)

- **Screen.** `Home()` collects the view state, maps it to `HomeViewStateUiModel` and renders `HomeContents`. That
  switches between `TodayContents` (sky card, "Migraine started") and `AttackModeContents` (rain card, one guidance
  line, "Started HH:MM · N min", intensity chips at least 64 dp, "Started earlier" options, "Migraine ended").
- **Transitions.** A cross-fade and background colour animation of 1,200 ms. It snaps when reduce-motion is on, and
  the first frame after entering the screen never animates (EC-UX-07, EC-UX-08, EC-UX-12).
- **Elapsed time.** It ticks every 30 seconds through `produceState`, so the UI keeps the timer and the ViewModel stays
  stateless.
- **Undo snackbar.** Shown for 5 seconds by a `LaunchedEffect` that dismisses an indefinite snackbar.
- **72-hour check.** An inline card in attack mode, not a dialog, so the screen stays calm. "It has ended" opens the
  Material 3 date and time pickers, limited to `[start, now]`.
- **Haptics.** `LocalHapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)` after the start or end
  notification. The system's touch-feedback setting is honoured automatically.
- **Time format.** A small `TimeUiFormatter` class follows the locale and `DateFormat.is24HourFormat`, and appends the
  start zone's city when it differs from the device zone (EC-TIM-02).
- **Strings.** All strings live in `:features:home:ui` resources. Every control has a `contentDescription` or semantics
  role. A selected chip uses `selected = true` semantics (EC-UX-04).

### D9. App wiring, route and launcher shortcut

- **Route.** `Home` becomes `@Serializable data class Home(val startAttack: Boolean = false)`.
- **Shortcut.** `res/xml/shortcuts.xml` declares a static shortcut "Migraine started" with the action
  `com.migrely.app.action.START_ATTACK`, targeting `MainActivity`.
- **Intents.** `MainActivity` is `singleTop`.
  - A launch intent with that action sets the start destination to `Home(startAttack = true)`.
  - `onNewIntent` with the action navigates to `Home(startAttack = true)`, popping the old home entry.
  - `ScreenEnterObserver` ensures the request runs once per entry, and EC-ATK-01 makes a repeat harmless anyway.
- **DI.** `DatabaseModule` provides the Room database (singleton), the DAO and the datasources. `HomeModule`,
  `HomeDataModule` and `HomeUiModule` provide the feature classes.

### D10. Testing

| Level | What |
|---|---|
| Domain | Every use-case rule: single start, 2-minute replacement, overlap rejection, 72-hour due computation including `confirmedAt`, end-time bounds, undo guards |
| Data | `HomeRepository` success and failure-as-value paths, plus the mappers |
| Datasource | `AttackDataSourceImpl` with a mocked DAO (exception mapping); mappers; `ClockDataSourceImpl` midnight computation with an injected `java.time.Clock` |
| Instrumented | `AttackDaoTest` on the emulator: the transaction rules, cascade delete, the touching-range query |
| Presentation | `HomeViewModel` for every action and notification |
| UI | Mappers, `TimeUiFormatter`, and the navigation mapper with the new route |

Konsist runs unchanged. The app is then walked through the spec scenarios on the API 36 emulator.

## Risks / Trade-offs

- **[Room KSP and the Room Gradle plugin on AGP 8.13 / Kotlin 2.2.20]** → Room 2.8.5 is built against Kotlin 2.1.20 and
  works with KSP 2.2.20-2.0.4. Verify with the first module build before writing DAO code.
- **[Converting `:datasource:implementation` to Android]** → Copy the build shape of `:architecture:ui`, the
  generator's Android library template. Konsist datasource rules already expect this package layout.
- **[Midnight ticker and time-zone changes while the app is open]** → `observeDate()` recomputes the zone at every
  emission, and the home screen re-subscribes on resume. A zone change mid-session updates on the next resume, which is
  acceptable.
- **[Snackbar timing is not exactly 5 seconds on every device]** → The undo guards in D2 keep a late undo safe.
- **[The hand-drawn look on Canvas may read as flat]** → Deterministic jitter, plus comparing previews against the
  style frames before the change is closed.

## Migration Plan

This is the first database version (1). There is nothing to migrate. Rollback is a `git revert` of this change's
commits before release.

## Open Questions

None that change the approach. Exact copy for the 72-hour card is finalised during UI work against the spec wording.
