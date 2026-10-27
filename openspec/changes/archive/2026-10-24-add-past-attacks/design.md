# Design

## Context

After `add-attack-details`:
- **Storage:** Room v2 holds attacks, intensity points, `attack_tag` and `custom_tag`. `AttackDataSource.overlapping`
  returns at most one other attack, and attacks never overlap (every write checks it).
- **Details screen:** `UpdateAttackTimesUseCase` refuses an overlap and names the first overlapping attack. An overlap of
  less than a minute snaps to the neighbour's boundary, because pickers work in whole minutes while stored times keep
  their seconds (1b review fix 2).
- **Home:** the normal mode shows today's sky, today's attacks and "Migraine started". There is no way to log an attack
  that started in the past, except moving the start of the attack in progress earlier.
- **Navigation:** routes `Home(startAttack)` and `AttackDetails(attackId)`.

See proposal.md for motivation and the specs for the required behaviour. Modules, in checklist order:
1. `:features:attack:{domain,presentation,data,ui}`;
2. `:datasource:{source,implementation}`;
3. `:features:home:*`;
4. `:app`.

## Goals / Non-Goals

**Goals:**
- Add an ended attack in a few taps, with defaults that need at most one picker in the common case ("last night").
- One set of merge rules, used by the add screen and the details screen alike.
- Overlap handling that never stores two overlapping attacks, even with a double tap.

**Non-Goals:**
- No schema change: database version 2 stays.
- No undo for merges (BRD D28).
- No E2E test suite. DAO SQL gets instrumented tests; everything else gets unit tests.

## Decisions

### D1. A second screen in the `attack` feature

"Add a past migraine" is a screen of the existing `attack` feature, with its own ViewModel, view state, notifications,
navigation events and `Dependencies`. It shares the feature's domain, models and UI pieces (`TimeRow`, picker labels,
duration text, messages) with the details screen. A new feature would duplicate the overlap and time rules, which belong
to one attack's domain.

### D2. Domain (`:features:attack:domain`)

**New and changed models:**

| Model | Fields / cases |
|---|---|
| `AttackRangeDomainModel` | `start`, `end?` (null while in progress) |
| `OverlapLookupResultDomainModel` (changed) | `Found(attacks: List<AttackDetailsDomainModel>)`, oldest first · `None` · `Failed` |
| `UpdateAttackTimesResultDomainModel.Overlaps` (changed) | `attacks: List<AttackRangeDomainModel>` |
| `PastAttackDraftDomainModel` | `start`, `end`, `zone` |
| `CreatePastAttackRequestDomainModel` | `start`, `end` |
| `CreatePastAttackResultDomainModel` | `Created(attackId)` · `InFuture(field)` · `EndBeforeStart` · `Overlaps(attacks: List<AttackRangeDomainModel>)` · `StorageFull` · `Failed` |
| `MergeAttacksRequestDomainModel` | `attackId?` (null for a new attack), `start`, `end?` |
| `MergeAttacksResultDomainModel` | `Merged(attackId)` · `Saved(attackId)` (nothing overlapped any more) · `InFuture(field)` · `EndBeforeStart` · `NotFound` · `StorageFull` · `Failed` |
| `StorePastAttackResultDomainModel` | `Stored(attackId)` · `Overlapping` · `StorageFull` · `Failed` |
| `AttackMergeDomainModel` | `survivorId`, `absorbedIds`, `start`, `startZone`, `end?`, `functionalImpact?`, `note` |

**Repositories (one function each):**
- `FindOverlappingAttacksRepository.overlappingAttacks(excludingId?, start, end)` replaces
  `FindOverlappingAttackRepository`. It returns every overlapping attack, so errors can name all of them and a merge
  can read their details.
- `StorePastAttackRepository.storePastAttack(start, zone, end)` stores an ended attack only when nothing overlaps it, in
  one transaction (EC-ATK-17).
- `MergeAttacksRepository.mergeAttacks(merge)` writes a merge in one transaction.
- `GetCurrentTimeRepository` and `GetAttackDetailsRepository` are reused.

**Use cases** (all `BackgroundExecutingUseCase`):

| Use case | Rules |
|---|---|
| `GetPastAttackDraftUseCase` | `end` = now rounded down to the minute, `start` = `end` − 2 h, `zone` = the device's zone (D26). |
| `CreatePastAttackUseCase` | (a) A time more than `CLOCK_TOLERANCE` after now is `InFuture`; an end not after the start is `EndBeforeStart`. (b) Overlaps snap (D3). (c) What still overlaps is `Overlaps`, oldest first. (d) Otherwise it is stored in the device's zone; an `Overlapping` answer from the store (a race) looks the overlaps up again. |
| `MergeAttacksUseCase` | Validates like (a) and snaps like (b), then merges (D4). |
| `UpdateAttackTimesUseCase` (changed) | Uses the shared snapping and returns every remaining overlap. |
| `UndoTimesChangeUseCase` (changed) | Uses the new lookup; any overlap is still `NotPossible`. |

### D3. Snapping is shared

`snapToNeighbours(start, end?, others)` in the use case package moves the start to a neighbour's end, or the end to a
neighbour's start, when they overlap by less than a minute (EC-ATK-18). It generalises the 1b rule from one neighbour to
a list, so the details screen, the add screen and a merge treat the same picked times the same way. After snapping, the
use case checks the end against the start and looks the overlaps up again.

### D4. Merging (BRD D27)

| Step | Rule |
|---|---|
| Survivor | The edited attack, or for a new attack the overlapping attack that started first. |
| Range | The union of the snapped request, the survivor's current times, every overlapping attack and every pain entry. It grows until no other attack overlaps it (at most a few passes, because stored attacks never overlap each other). It never shrinks, so a second "Merge them" changes nothing. |
| In progress | The end is empty when any merged attack, or the edited one, is in progress. |
| Start zone | The zone of whichever start is earliest: an attack's own zone, or the device's zone for the new attack's start (EC-TIM-01). |
| Pain entries and tags | Moved and copied to the survivor in the datasource; a tag selected on both is kept once. |
| Impact | The highest level among the merged attacks, or none. |
| Note | The edited attack's note is the one on screen, stored first (ATK-F6). Non-empty notes in order of start are joined with a blank line; a single note is kept as it is. Text past 1,000 characters is cut from the end, without splitting a surrogate pair or leaving a blank line (EC-ATK-16). |
| Long-attack check | Kept by the datasource: disabled if any was disabled, and the latest confirmation (change 1, EC-ATK-02). The domain of this feature does not model it. |
| Nothing overlaps | When the lookup finds no other attack any more: a new attack is stored, an edited one gets the range; the result is `Saved`. |

### D5. Storage (`:datasource:*`)

`AttackDataSource` changes:
- `overlapping(excludingId: String?, start, end): List<AttackDataModel>` replaces the single result, oldest first.
- `createEnded(start, zoneId, end): CreateEndedDataModel` is `Created(attackId)` or `Overlapping`.
- `merge(survivorId, absorbedIds, start, startZoneId, end?, functionalImpact?, note)`.

DAO:
- `overlappingAll(excludingId, start, end)`: `(:excludingId IS NULL OR id != :excludingId)` and the existing overlap
  rule (in progress is open-ended, touching does not count, deleted rows are hidden). `overlapping` stays for
  `restoreIfPossible`.
- `insertEndedIfNoOverlap(attack)`: one `@Transaction`, so two quick taps cannot store two attacks.
- `mergeInto(...)`: one `@Transaction` that moves `intensity_point` rows, copies tags with `OnConflictStrategy.IGNORE`,
  merges the long-attack check, updates the survivor and deletes the absorbed rows (their tags cascade).
- The datasource validates impact (0–3), the note (at most 1,000) and that the survivor is not absorbed.

### D6. Presentation (`:features:attack:presentation`)

**Add screen:**
- `AddPastAttackViewState`: `Loading` · `Ready(draft: PastAttackDraftPresentationModel)`.
- `AddPastAttackPresentationNotification`: `InFuture(field)` · `EndBeforeStart` ·
  `Overlaps(attacks: List<AttackRangePresentationModel>)` · `StorageFull` · `SomethingWentWrong`.
- `AddPastAttackPresentationNavigationEvent`: `OnLeave` · `OnPastAttackAdded(attackId)` · `OnAttacksMerged(attackId)`.
- `AddPastAttackViewModel`: `onEnter()`, `onAddAction(start, end)`, `onMergeAction(start, end)` and `onLeaveAction()`.
  `Saved` from a merge emits `OnPastAttackAdded`.

**Details screen:**
- `TimeRejectionPresentationModel.Overlaps(attacks, requestedStart, requestedEnd?)` carries the rejected times, so
  "Merge them" can send them back without ViewModel state (skill rule 4).
- `onMergeAction(attackId, start, end?, note?)` and the `AttacksMerged` notification. `note` is the note on screen, so
  the merge joins what the user sees.
- `onEnter(attackId, attacksMerged)` notifies `AttacksMerged` once when the screen was opened by a merge from the add
  screen.

### D7. UI (`:features:attack:ui`)

**Add screen layout.** A top app bar with a back arrow and "Past migraine" (`titleLarge`). Then one scrolling column,
capped at 640 dp:
1. a short line: "When was it? You can add the pain and the rest next.";
2. "Started" and "Ended" rows (`TimeRow`, shared with details) and the duration;
3. "Add migraine" (filled, at least 56 dp), or the overlap card in its place. With the card shown, "Add migraine"
   would only bring the card back, so the card's two choices replace it.

**State.**
- The picked times live in `rememberSaveable` as `PastAttackDraftUiModel(startEpochMillis, endEpochMillis, zoneId)`.
  It is set once from `Ready` and survives rotation and process death (EC-ATK-10).
- `PastAttackDraftUiModel.withStart(start, latestEnd)` keeps the duration and caps the end at the current minute.
- Inline errors and the overlap card are saveable too. Any new pick clears them.
- A `saving` flag disables both buttons between a tap and its result (EC-ATK-17).

**Pickers.** The start goes from 2000 to one minute before the current minute. The end goes from one minute after the
start to now + 5 minutes (the domain checks again).

**Overlap card.**
- It uses the details error text: "This would overlap the migraine from 12:00 to 13:30.", the in-progress variant, or
  "This would overlap these migraines: …" for several.
- A body line explains merging. The buttons are "Merge them" (tonal) and "Change the times" (text).
- It is a polite live region, so TalkBack announces it.

**Details screen.**
- The inline overlap error lists every overlapping attack and has a "Merge them" text button.
- The note being typed moves from `NoteSection` to the screen's saveable state. "Merge them" sends it and clears it,
  so a stale draft can never overwrite the merged note when the screen is left.
- "Migraines merged." is a confirmation message. It is also shown when the screen opens with `announceMerge`.

### D8. Home and navigation

- **Home:** `HomePresentationNavigationEvent.OnAddPastAttack` and `HomeViewModel.onAddPastAttackAction()`. In
  `TodayContents`, a `TextButton` "Add a past migraine" (at least 48 dp) sits under "Migraine started", in both layouts.
- **Routes:** `@Serializable data object AddPastAttack`, and `AttackDetails(attackId, announceMerge = false)`.
- **Mappers:**
  - home `OnAddPastAttack` → `navigate(AddPastAttack) { launchSingleTop = true }`;
  - add `OnLeave` → `popBackStack(AddPastAttack::class, inclusive = true)`;
  - `OnPastAttackAdded(id)` and `OnAttacksMerged(id)` → `navigate(AttackDetails(id, announceMerge))` with
    `popUpTo<AddPastAttack> { inclusive = true }` and `launchSingleTop`, so a late second event updates that details
    screen instead of stacking another one.

  All of them have parameterized tests.

## Risks / Trade-offs

- **No undo for a merge (D28).** The card names what will be merged, and nothing the user entered is lost except a
  lower impact level and note text past 1,000 characters. Undo would mean restoring several attacks and the survivor's
  old fields. Beta feedback decides whether it is needed.
- **Merging into the attack in progress from the details screen** gives the edited attack the in-progress end. Home
  reloads on resume and shows attack mode for the survivor.
- **Read, then write.** The merge plan is computed from reads and written in one transaction. A write that lands in
  between (only possible from another screen) is caught by the next overlap check; the tap guard and the store
  transaction cover the double tap.
- **Rotation during a write.** ViewModels are unscoped (profile §7), so a rotation in the milliseconds between a tap
  and its result sends the result to the old screen. The attack is stored, but the screen stays. Tapping again then
  names the attack just added. Scoping screen dependencies to the back-stack entry is left to `upgrade-toolchain`
  (Navigation 3).
- **Defaults are a guess.** Two hours ago to now is only a starting point. Moving the start carries the end along, and
  the duration under the times makes a wrong default visible before "Add migraine".
