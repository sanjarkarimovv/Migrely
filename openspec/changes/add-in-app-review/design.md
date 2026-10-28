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
| `ReviewDataSource` | `:datasource:source`, package `review` | `launchReviewFlow(): ReviewFlowDataModel`, blocking, off the main thread |
| `ReviewFlowDataModel` | same, `model` | `Completed`, `Unavailable` |
| `PlayReviewDataSourceImpl` | `:datasource:implementation` | `requestReviewFlow()`, then `launchReviewFlow(activity, info)`, both on the main context |

- **Activity:** it comes from `currentActivity`, which is `CurrentActivityHolder::resumed`. With no resumed activity
  the result is `Unavailable`.
- **Waiting:** each Google Play task is awaited through a `CompletableDeferred` completed by
  `addOnCompleteListener`.
- **Timeouts (`PlayReviewTimings`):** 10 s for the request; 10 min for the launch, which completes only when the card
  closes.
- **Unavailable:** a failed task, a timeout or any thrown `RuntimeException`.
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
  - `LaunchReviewFlowRepository`;
  - `MarkReviewRequestedRepository`.
- `ShouldRequestReviewUseCase` applies D130 with these constants:
  - `MIN_MIGRAINES` = 3;
  - `PAYWALL_GAP` = 3 days;
  - `REQUEST_GAP` = 120 days;
  - `MAX_REQUESTS` = 3.
  It reads the attack in progress last, like the paywall, so an attack started meanwhile is seen. An unknown count
  or an unknown attack in progress means no.
- `RequestReviewUseCase` launches the flow. On `Completed` it marks the request with the current time and returns
  `true`; otherwise it marks nothing.

**Data:** `ReviewRepository` implements all eight repositories.
- It uses `AttackDataSource`, `PreferencesDataSource`, `AppInfoDataSource`, `ClockDataSource` and
  `ReviewDataSource`.
- Migraines: `overlapping(null, EPOCH, now)`, as the subscription counts them. Soft-deleted attacks are invisible
  (EC-REV-11).
- A storage error on a read gives null. A failed mark write is swallowed: at worst the app asks once more.
- `ReviewFlowDomainMapper` maps the result.

**Presentation:** `ReviewPromptViewModel` is `BaseViewModel<Unit, ReviewPromptPresentationNotification>`.
- `onEnter()` asks `ShouldRequestReviewUseCase` and, when it answers yes, `RequestReviewUseCase`.
- `asking` keeps one question at a time. `tried` is set when the flow is launched and is never cleared.

**UI:** `ReviewPromptDependencies.ReviewPrompt(refreshKey)` draws nothing.
- `LaunchedEffect(refreshKey, lifecycle)` runs `repeatOnLifecycle(RESUMED) { delay(2 s); onEnter() }`.
- Leaving today's mode removes the slot, and leaving the screen pauses the lifecycle. Both cancel the wait
  (EC-REV-07).

### D4. Once per process

The `AppNavHostDependencies` are built per activity. A view model built with them would forget `tried` on rotation.
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

The privacy policy (md and html) gains one sentence under Purchases: rating Migrely in Google Play's window sends the
rating to Google under Google's terms; Migrely never sees it. Data safety does not change: Migrely collects nothing.

## Risks

| Risk | Mitigation |
|---|---|
| A library update brings a network permission | The build's permission check fails the build (issue #36); the AARs are checked in this change |
| Play's quota hides the card, and a request is "spent" | Accepted: Play does not report display. 120 days later the app may ask again, at most 3 times |
| The flow interrupts the paywall | The 3-day gap, and a paywall navigates home away, which cancels the settle wait |
| `launchReviewFlow` never completes | A 10-minute timeout, then `Unavailable`; the attempt is spent for the process |
| QA cannot see the card in a debug build | Debug uses `FakeReviewManager`, so every launch completes and is marked: QA checks the marks in `migrely_preferences` with `run-as`. The real card can be seen on an internal-testing install of a release build |
