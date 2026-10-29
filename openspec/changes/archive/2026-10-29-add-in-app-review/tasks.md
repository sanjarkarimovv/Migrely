# Tasks

## 1. Domain

- [x] 1.1 `:features:review:domain` (scaffolded with `cag`, build files as `features/home/*`):
  - models: `ReviewMarksDomainModel`, `ReviewFlowResultDomainModel`;
  - the eight single-function repositories;
  - `ShouldRequestReviewUseCase` with the D130 constants, and `RequestReviewUseCase`;
  - tests for every rule and edge (EC-REV-01…12).
  Verify: `./gradlew :features:review:domain:test`.

## 2. Presentation

- [x] 2.1 `ReviewPromptViewModel` and `ReviewPromptPresentationNotification`, with tests: one question at a time,
  launched once, never again after a launch, and asked again after a "no".
  Verify: `./gradlew :features:review:presentation:test`.

## 3. Datasources and data

- [x] 3.1 `ReviewDataSource`, `ReviewFlowDataModel` and `PlayReviewDataSourceImpl` with `PlayReviewTimings`. Tests:
  completed; request failed; launch failed; no activity; request timeout; thrown exception.
  Verify: `./gradlew :datasource:implementation:testDebugUnitTest`.
- [x] 3.2 Preferences: `reviewRequests()` and `markReviewRequested()`, with tests, including that `clear()` erases
  them.
- [x] 3.3 `AppInfoDataSource.previousExitCrashed()`, with tests: crash, native crash, ANR, a normal exit, none,
  Android 10, and an exception.
- [x] 3.4 `:features:review:data`: `ReviewRepository` and `ReviewFlowDomainMapper`, with tests.
  Verify: `./gradlew :features:review:data:test`.

## 4. UI and home

- [x] 4.1 `:features:review:ui`: `ReviewPromptDependencies` and `ReviewPrompt` (the RESUMED-gated 2-second settle).
- [x] 4.2 Home: the `reviewPrompt` slot, drawn only in today's mode.
  Verify: `./gradlew :features:home:ui:testDebugUnitTest`.

## 5. App wiring

- [x] 5.1 The version catalog `play-review`; `settings.gradle.kts`; `:app` dependencies; `ReviewModule` (with a
  singleton view model); `AppNavHost` fills the slot; `AppInfoDataSourceModule` passes the `ActivityManager`.
- [x] 5.2 The permission check: `assembleDebug assembleRelease` pass, and a merged manifest without INTERNET.

## 6. Docs

- [x] 6.1 `docs/ARCHITECTURE.md`: the review feature, the slot and the datasource package.
- [x] 6.2 Privacy policy (md and html): the rating sentence (design D6).

## 7. Verify

- [x] 7.1 Gate: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`; instrumented tests
  of `:datasource:implementation`.
- [x] 7.2 Emulator walk on this session's own emulator: every scenario that a device can show. The marks are read
  from `migrely_preferences`, and the previous exit is made with `am crash`.
- [x] 7.3 Independent review (`superpowers:code-reviewer`); findings fixed and recorded.
- [x] 7.4 Verification record below.

## Verification record

**Gate:** `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` at 1d2dadbe: BUILD
SUCCESSFUL (the first run at 5cdd82d1 was green too).
- Unit tests of the change: review domain 23, presentation 8, data 10, `:datasource:implementation` 209 (the whole
  module, including Play review 13, app info 8 and preferences).
- Instrumented: `ANDROID_SERIAL=emulator-5590 ./gradlew :datasource:implementation:connectedDebugAndroidTest`, 112 of
  112 passed.

**Permissions:** the permission check passes for debug and release. Both merged manifests hold the same 5
permissions as 1.0.1, and none is INTERNET or any network permission. The review library adds only Play's
`PlayCoreDialogWrapperActivity`. On the device, `dumpsys package` shows no INTERNET.

**Walk 1, at 5cdd82d1, on emulator-5590 (`Migrely_GTM_API_36`, this session's own AVD):** 11 of 11 passed.
- Scenarios: onboarding with 0 migraines; the 3-day paywall gap; never during an attack; the settle with Settings
  opened early; once per process across rotation, background and History; a crash, then a normal start; the limit
  of 3; deleted migraines (control at 3, none at 2); Delete all data clearing the marks; no unexpected crash; no
  INTERNET.
- Evidence: `migrely-work/walks/review/`.

**Walk 2, at 1d2dadbe, after the review fixes:** all re-run scenarios passed.
- Re-run: the paywall gap, the attack, the settle, the crash, deleted migraines and the quick checks.
- An unplanned ANR was correctly treated as a crash: no request after it, then one after a normal start.
- Evidence: `r2_*` files.

**Not checked on a device, and why:**
- **The exact I-1 race** (an attack starting between "prepared" and "opened"): FakeReviewManager answers at once.
  Covered by `ReviewPromptViewModelTest` (left while preparing) and `RequestReviewUseCaseTest` (re-checks).
- **Once per process in isolation:** after a request, the 120-day rule blocks anyway, and SharedPreferences cannot be
  edited inside a running process. Covered by unit tests.
- **The real Google Play card:** it shows only for a release build installed from Play. To see it, use an
  internal-testing install of 1.1.0.
- **200% font, landscape, TalkBack:** the change draws nothing of its own.

**Observed on the walk:**
- **Timing:** the host was overloaded (load average 17–26, software GPU), so requests came 4–8 s after home instead
  of about 2 s, and taps right after a cold start needed retries.
- **The ANR (17:35):** "Input dispatching timed out" while the main thread was measuring Compose layout, at 10.6 s of
  CPU. No review, Play or `runBlocking` frame was on the main thread; the dispatcher workers were idle. Read as the
  overload, not this change. QA may watch for it on a normal machine.

## Review (7.3)

An independent `superpowers:code-reviewer` read `git diff origin/develop...HEAD` at 5cdd82d1, against the spec, BRD
D129–D132 and the architecture profile. Each finding was checked against the code; all were real, and all are fixed in
1d2dadbe.

| # | Finding | Fix |
|---|---|---|
| I-1 | The 2-second settle guarded only the wait. Google Play's preparation could take up to 10 s, and the flow could then open over attack mode, another screen or a paywall (D130 says never) | Two steps: `PrepareReviewUseCase`, then `RequestReviewUseCase` only if `ReviewPrompt` has not called `onLeave()`. The launch re-reads the attack in progress and the paywall mark just before opening, and the activity is read in the same main-thread step that launches |
| I-2 | A 5th-migraine paywall decided more than 2 s after home loads could follow the card | A paywall that opens first takes home away, so the round is dropped; the launch re-checks the paywall mark. The remaining window, which would need the subscription feature changed, is recorded in design Risks |
| M-1 | The full attack query ran on every home load, even after the requests were used up | The preference rules are read first, the count after them, and the attack in progress last |
| M-2 | `runBlocking` plus the main context would deadlock if ever called on the main thread | Both calls check the thread first and throw at once |
| M-3 | The live site's privacy page had not been rebuilt | `website/build.py` run; deploying is the owner's task (tracker T09) |
| M-4 | The design said `tried` and a one-sentence policy change; the policy said "asked" where only completed flows count | The design matches the code (`done`, the D6 list). The policy now says "how many times Google Play's rating window has run" |
| M-5 | No test of the real threading; no test of marks dated after now | Added: a single-thread "main" dispatcher with answers arriving later on another thread; marks dated after now; prepare; and the re-checks. The `ReviewPrompt` composable has no unit test, as no feature UI in the project has one; the walk covers it |
| M-6 | Template dependencies in `:features:review:ui` and `:data` | Removed |
