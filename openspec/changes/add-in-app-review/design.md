# Design

## Context

BRD §14.4 asks for Google Play's in-app review after the 3rd migraine, in a session without a crash. The rules are
D129–D132 and EC-REV-01…12.

Migrely has no INTERNET permission (D44). Google Play's review library talks to the Play Store app over IPC, as
billing does, and its AAR (`review` 2.0.2, `core-common` 2.0.4) declares no permission.

Home already hosts the subscription's 5th-migraine paywall trigger through a slot that draws nothing (D110). Billing
already opens Google Play's UI over the activity held by `CurrentActivityHolder`. This change reuses both patterns.

## Goals and non-goals

**Goals:**
- Ask at a calm moment: after migraines, never during one, never right after a paywall, never after a crash.
- Ask rarely and honestly.
- Follow every architecture rule. The flow is invisible to features.

**Non-goals:**
- a custom rating UI, feedback form or "Rate us" entry in Settings;
- knowing whether the user rated.

## Decisions

### D1. The datasource

| Piece | Where | What |
|---|---|---|
| `ReviewDataSource` | `:datasource:source`, package `review` | `prepareReviewFlow(): Boolean` and `launchReviewFlow(): ReviewFlowDataModel`, both blocking, off the main thread |
| `ReviewFlowDataModel` | same, `model` | `Completed`, `Unavailable` |
| `PlayReviewDataSourceImpl` | `:datasource:implementation` | `requestReviewFlow()` keeps the `ReviewInfo`; `launchReviewFlow(activity, info)` uses it once |

- **Two steps (code review I-1):** the flow is prepared first and launched only when the caller still wants it. A
  launch without a prepared `ReviewInfo` is `Unavailable`, and a launch uses it up.
- **Activity:** it comes from `currentActivity`, which is `CurrentActivityHolder::resumed`. It is read in the same
  main-thread block that launches, so the flow opens over the activity on screen at that moment. With no resumed
  activity the result is `Unavailable`.
- **Waiting:** each Google Play task is awaited through a `CompletableDeferred` completed by
  `addOnCompleteListener`.
- **Timeouts (`PlayReviewTimings`):** 10 s for preparing; 10 min for the launch, which completes only when the card
  closes.
- **Unavailable:** a failed task, a timeout or any thrown `RuntimeException`.
- **Main thread (review M-2):** both calls block until Google Play answers on the main thread, so calling either on
  the main thread would deadlock. Each checks first and throws at once.
- **Tests:** `newManager` builds the `ReviewManager`, and tests pass their own.

### D2. Preferences and the previous exit

- **`PreferencesDataSource`:**
  - `reviewRequests(): ReviewRequestsDataModel(count, lastMillis)`, read from the keys `review_request_count` and
    `review_last_requested`;
  - `markReviewRequested(atMillis)`, which raises the count and stores the time in one commit.
  - These keys are in the preferences file, so Auto Backup takes them (D74) and `clear()` erases them (EC-STO-03).
- **`AppInfoDataSource.previousExitCrashed()`:**
  - On Android 11 and later, it reads `ActivityManager.getHistoricalProcessExitReasons(packageName, 0, 1)` and checks
    the reason against `REASON_CRASH`, `REASON_CRASH_NATIVE` and `REASON_ANR`.
  - Before Android 11, or on any error, the answer is `false`, so the rule holds (EC-REV-04).
  - The SDK check is injected and annotated, as for `versionName` (QA #74).

### D3. The review feature

**Domain, `com.migrely.app.review.domain`:**
- Single-function repositories:
  - `GetMigraineCountRepository`;
  - `GetAttackInProgressRepository`;
  - `GetLastPaywallShownRepository`;
  - `GetPreviousExitRepository`;
  - `GetReviewMarksRepository`;
  - `GetCurrentTimeRepository`;
  - `PrepareReviewFlowRepository`;
  - `LaunchReviewFlowRepository`;
  - `MarkReviewRequestedRepository`.
- `ShouldRequestReviewUseCase` applies D130 with these constants:
  - `MIN_MIGRAINES` = 3;
  - `PAYWALL_GAP` = 3 days;
  - `REQUEST_GAP` = 120 days;
  - `MAX_REQUESTS` = 3.
- **Order of reads:**
  1. The preference rules (marks, paywall, previous exit) come first, as they are cheap (review M-1).
  2. The migraine count comes next.
  3. The attack in progress is read last, like the paywall, so an attack started meanwhile is seen.
  An unknown count or an unknown attack in progress means no.
- **Marks dated after now:** say no, as a clock moved back would leave them, until the clock passes them.
- `PrepareReviewUseCase` has Google Play prepare the flow and answers whether it did.
- `RequestReviewUseCase` re-checks, just before launching, that no attack is in progress and that no paywall showed
  itself within `PAYWALL_GAP` (I-1, I-2). It then launches the prepared flow. On `Completed` it marks the request
  with the current time and answers `true`; otherwise it marks nothing.

**Data:** `ReviewRepository` implements all nine repositories.
- It uses `AttackDataSource`, `PreferencesDataSource`, `AppInfoDataSource`, `ClockDataSource` and
  `ReviewDataSource`.
- Migraines: `overlapping(null, EPOCH, now)`, as the subscription counts them. Soft-deleted attacks are invisible
  (EC-REV-11).
- A storage error on a read gives null. A failed mark write is swallowed: at worst the app asks once more.
- `ReviewFlowDomainMapper` maps the result.

**Presentation:** `ReviewPromptViewModel` is `BaseViewModel<Unit, ReviewPromptPresentationNotification>`.
- `onEnter()` (home settled) runs `ShouldRequestReviewUseCase`, then `PrepareReviewUseCase`, then
  `RequestReviewUseCase`. The last runs only if `onLeave()` has not come in between.
- The view model keeps three flags:
  - `onHome` is set by `onEnter()` and cleared by `onLeave()`;
  - `asking` keeps one round at a time;
  - `done` is set when the flow is launched or Google Play could not prepare it, and is never cleared.
- A round dropped because home was left leaves `done` unset, so the next settle tries again.

**UI:** `ReviewPromptDependencies.ReviewPrompt(refreshKey)` draws nothing.
- `LaunchedEffect(refreshKey, lifecycle)` runs `repeatOnLifecycle(RESUMED)` with
  `try { delay(2 s); onEnter(); awaitCancellation() } finally { onLeave() }`.
- Leaving today's mode removes the slot, leaving the screen pauses the lifecycle, and a new home load restarts the
  effect. Each of these cancels the wait, or tells the view model that home was left (EC-REV-07).

### D4. Once per process

The `AppNavHostDependencies` are built per activity. A view model built with them would forget `done` on rotation.
`:app` therefore provides `ReviewPromptViewModel` as `@Singleton`. The flag then lives as long as the process
(EC-REV-08), and "Delete all data", which starts a new process, starts afresh. The view model holds no activity or
context.

### D5. Home and `:app`

- **Home:** `Home` and `HomeContents` get `reviewPrompt: @Composable (refreshKey: Int) -> Unit = {}`. It is drawn
  only while `mode == HomeMode.Today`, next to `paywallTrigger`, and receives `homeLoads`.
- **`:app`:**
  - `ReviewModule` provides the datasource (`@Singleton`), the repository, the use cases, the view model
    (`@Singleton`) and the dependencies.
  - `AppNavHost` fills the slot.
  - `AppInfoDataSourceModule` passes the `ActivityManager`.
  - The datasource is bound per build type, as billing is, in `app/src/debug` and `app/src/release`:
    - Debug builds pass Google's `FakeReviewManager`. It completes at once with no card, so the walk and QA check
      the rules through the marks, as with billing's simulated store (D113). A debug build is never installed
      from Play, so the real manager would show no card there anyway.
    - Release builds always pass the real manager.

### D6. Privacy text

- **Privacy policy (md and html):** a "Rating Migrely" section, and a line in the list of what Migrely keeps. A rating
  goes to Google Play under Google's terms, and Migrely never sees it.
- **Data safety:** a "Ratings" entry. Migrely still collects nothing.
- **Website:** `python3 website/build.py` regenerates `website/public/privacy/`, which the owner deploys (tracker T09;
  review M-3).

## Risks

| Risk | Mitigation |
|---|---|
| A library update brings a network permission | The build's permission check fails the build (issue #36); the AARs are checked in this change |
| Play's quota hides the card, and a request is "spent" | Accepted: Play does not report display. 120 days later the app may ask again, at most 3 times |
| The flow interrupts the paywall (review I-2) | The 3-day gap. A paywall that opens before the launch takes home away, so `onLeave()` drops the round, and the launch re-checks the paywall mark. The one window left: the 5th-migraine paywall decides more than 2 seconds after home loads (Google Play billing slow to connect) and only after the card has opened. The card and the paywall then follow each other once. Sequencing the two would mean changing the subscription feature, which QA has verified for release. It is left for later and recorded here |
| `launchReviewFlow` never completes | A 10-minute timeout, then `Unavailable`; the attempt is spent for the process |
| An attack starts while Google Play prepares (review I-1) | Two steps: a launch needs home still on screen (`onLeave()` not called) and re-reads the attack in progress just before launching |
| QA cannot see the card in a debug build | Debug uses `FakeReviewManager`, so every launch completes and is marked: QA checks the marks in `migrely_preferences` with `run-as`. The real card can be seen on an internal-testing install of a release build |
