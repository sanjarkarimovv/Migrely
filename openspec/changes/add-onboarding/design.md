# Design

## Context

- `MainActivity` sets `Home(startAttack)` as the start destination; the "Start attack" shortcut passes `startAttack`.
  There is no splash screen yet.
- The Reminders screen owns the permission flow (D68): ask only on turning on, and at most once in 30 days after a
  refusal.
- Home's "Period" chip and History's "Period" switch always show (D48).
- Settings is a list of links with no domain (D70).

## Decisions

### D1. Modules

- **`:features:onboarding:{domain,data,presentation,ui}`.**
- **`:features:settings:{domain,data}`:** added for "Track periods", ending the §13 deviation row.
- **Datasource `preferences`:** `PreferencesDataSource` in `:datasource:source`, and `PreferencesDataSourceImpl` over
  `SharedPreferences("migrely_preferences")` in `:datasource:implementation`. It has:
  - `onboardingFinished(): Boolean` and `finishOnboarding()`;
  - `goal()` and `setGoal(GoalDataModel)`, `attackFrequency()` and `setAttackFrequency(AttackFrequencyDataModel)`, stored
    by their keys;
  - `periodTracking(): Boolean` (default true) and `setPeriodTracking(Boolean)`.
  - Writes use `commit()` on the background thread, so a failure is seen; a failed write is a `DataException`.
- **The goal and frequency keys** are typed in the datasource (`GoalDataModel`, `AttackFrequencyDataModel`, each with a
  stored key that never changes).

### D2. The start decision (BRD D76)

- `ShouldShowOnboardingUseCase(startAttackRequested)`:
  - when an attack is requested, it is false;
  - when onboarding is finished, false;
  - when the phone has records (`BackupDataSource.hasRecords()`), it finishes onboarding and returns false;
  - otherwise true. A failed read is false, so a broken store never blocks the app.
- **`MainActivity`:**
  - `installSplashScreen().setKeepOnScreenCondition { decision == null }`;
  - the decision runs on the background dispatcher through a Hilt entry point, like `MigrelyApplication`'s reschedule;
  - the result is kept in `savedInstanceState`, so a recreated activity keeps its start destination;
  - start destination `Onboarding` or `Home(startAttack)`.
- **Onboarding's route** sits under nothing: finishing navigates to `Home` with `popUpTo<Onboarding> { inclusive = true }`.

### D3. Steps (BRD D77)

- **`OnboardingViewModel` is stateless.** The UI keeps the step index in `rememberSaveable` and moves on as soon as an
  answer is chosen or "Skip" is tapped.
- **Events:**
  - `onGoalAction(goal)`, `onFrequencyAction(frequency)`, `onPeriodTrackingAction(on)`: save the answer;
  - `onFinishAction()`: after the last step, finish, then `OnFinished`, whatever the save's result;
  - `onRestoreAction()`: `OnRestoreBackup`.
- **A tap guard** covers each new step for 800 ms, so a double tap never answers the next step (BRD EC-UX-18).
- **Step 4** hosts the reminders slot. When the offer reports done (turned on, refused or "Not now"), onboarding
  finishes.
- **System Back** moves back a step, with `BackHandler` while step > 1. On step 1 it leaves the app.
- **A failed save** shows "Couldn't save that answer. Migrely works without it." and still moves on, so onboarding never
  traps the user.
- **Opened to log:** the shortcut and the daily reminder's "Log a migraine" skip onboarding. A shortcut arriving while
  the start is still decided is kept and applied. A recreation during the splash keeps the shortcut. The decision
  falls back to home after 2 s or on any failure.

### D4. The reminder offer (BRD D80)

- **`:features:reminders:presentation`:** `DailyReminderOfferViewModel`:
  - `onTurnOnAction()`: as `RemindersViewModel.onDailyReminderAction(true, 20:00)` — when notifications are allowed, it
    sets and plans the reminder, then emits `Done`;
  - when they are not allowed, it asks for permission if `mayAskPermission`, otherwise emits `Done` with the reminder
    off;
  - `onPermissionResultAction(granted)`;
  - `onNotNowAction()`.
- **It reuses** `GetRemindersUseCase`, `SetDailyReminderUseCase` and `RecordPermissionAskedUseCase`; the saved time is
  kept.
- **`:features:reminders:ui`:** `DailyReminderOfferDependencies.DailyReminderOffer(onDone)`, with the permission
  launcher, as on the Reminders screen.
- **`:app`** fills onboarding's `reminderOffer: @Composable (onDone: () -> Unit) -> Unit` slot.

### D5. Track periods (BRD D79)

- **Home:** `GetTodayUseCase`'s model gains `periodTracking`. The chip shows when it is true or today is marked.
- **History:** the month model gains `periodTracking`. The day panel's "Period" switch shows when it is true or the
  day is marked.
- **Settings:**
  - `SettingsViewState.Ready(periodTracking)`;
  - `onPeriodTrackingAction(on)` through `GetSettingsUseCase` and `SetPeriodTrackingUseCase`;
  - a switch row, with the Reminders screen's `SwitchRow` semantics: one node, Role switch.

### D6. Copy

- **Step 1:** "What brings you to Migrely?". The choices are "Keep a record for my doctor", "Find what triggers my
  migraines" and "Take fewer painkillers". Then the disclaimer, and the text button "Restore from a backup file".
- **Step 2:** "How often do you get migraines?". The choices are "Fewer than 4 a month", "4 to 14 a month" and "15 or
  more a month".
- **Step 3:** "Track your period too?", with "Migraines often follow the cycle. You can change this in Settings.". The
  choices are "Yes" and "No".
- **Step 4:** "A daily check-in?", with "Each evening, Migrely asks how your day was, so pain-free days count too.".
  The buttons are "Turn on at 8:00 PM" and "Not now".
- **Every step:** "Step N of 4" and "Skip".

## Risks

| Risk | Mitigation |
|---|---|
| The decision is slow on an old phone | one indexed `EXISTS` query and a preferences read; the splash holds at most until it returns |
| An existing user sees onboarding after the update | any health record finishes it silently before the first frame |
| The shortcut on a fresh install | `startAttack` skips the decision entirely |
| A process death mid-onboarding | the step is saveable, and the answers given are already stored |
