# Tasks

## 1. Datasource

- [x] 1.1 `PreferencesDataSource` (onboarding finished, goal, frequency, period tracking) over SharedPreferences, with
  tests.

## 2. Onboarding feature

- [ ] 2.1 Domain: `ShouldShowOnboardingUseCase`, the save use cases, `FinishOnboardingUseCase`, tests.
- [ ] 2.2 Data: `OnboardingRepository`, tests.
- [ ] 2.3 Presentation: `OnboardingViewModel`, tests.
- [ ] 2.4 UI: the four steps, Back, the reminder slot, and the restore link.

## 3. Reminders offer

- [ ] 3.1 `DailyReminderOfferViewModel` and `DailyReminderOffer`, with tests.

## 4. Track periods

- [ ] 4.1 Settings domain and data, the switch, and tests.
- [ ] 4.2 Home and History read it: the chip and the switch show when it is on or the day is marked.

## 5. App

- [ ] 5.1 core-splashscreen, the start decision in `MainActivity`, the route, the slot, and DI.

## 6. Verify

- [ ] 6.1 Gate, and instrumented tests.
- [ ] 6.2 Emulator walk:
  - fresh install (`pm clear`), every step answered and every step skipped;
  - Back, and rotation mid-step;
  - the shortcut on a fresh install;
  - an update with data;
  - restore from a backup;
  - the reminder on, refused and not now;
  - Track periods off in home, History and Settings;
  - 200% font, TalkBack.
- [ ] 6.3 Independent review; fix and record.
