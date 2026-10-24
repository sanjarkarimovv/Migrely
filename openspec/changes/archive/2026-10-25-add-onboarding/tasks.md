# Tasks

## 1. Datasource

- [x] 1.1 `PreferencesDataSource` (onboarding finished, goal, frequency, period tracking) over SharedPreferences, with
  tests.

## 2. Onboarding feature

- [x] 2.1 Domain: `ShouldShowOnboardingUseCase`, the save use cases, `FinishOnboardingUseCase`, tests.
- [x] 2.2 Data: `OnboardingRepository`, tests.
- [x] 2.3 Presentation: `OnboardingViewModel`, tests.
- [x] 2.4 UI: the four steps, Back, the reminder slot, and the restore link.

## 3. Reminders offer

- [x] 3.1 `DailyReminderOfferViewModel` and `DailyReminderOffer`, with tests.

## 4. Track periods

- [x] 4.1 Settings domain and data, the switch, and tests.
- [x] 4.2 Home and History read it: the chip and the switch show when it is on or the day is marked.

## 5. App

- [x] 5.1 core-splashscreen, the start decision in `MainActivity`, the route, the slot, and DI.

## 6. Verify

- [x] 6.1 Gate (1060 unit tests), and instrumented tests (datasource 82/82, designsystem 28/28).
- [x] 6.2 Emulator walk:
  - fresh install (`pm clear`), every step answered and every step skipped;
  - Back, and rotation mid-step;
  - the shortcut on a fresh install;
  - an update with data;
  - restore from a backup;
  - the reminder on, refused and not now;
  - Track periods off in home, History and Settings;
  - 200% font, TalkBack.
- [x] 6.3 Independent review; fix and record.

## Verification record

**Emulator walk** on emulator-5570, by a walk agent on 2aece4a, clearing the app's data between scenarios. At the end
the database, files and permission were restored to the baseline (dump md5 c3cfd324…). All 10 scenarios passed:

| Scenario | Result |
|---|---|
| An update with data | home opens at once; the preferences gain `onboarding_finished` only |
| A fresh install, answering | screen-recorded launch: launcher, splash, then "Step 1 of 4", no frame of home; every answer saved; "No" hides the Period chip; a relaunch opens home |
| A fresh install, skipping | Skip four times, then home with the chip, and only `onboarding_finished` saved |
| Back and rotation | Back walks the steps back; landscape keeps the step; Back on step 1 leaves; the next start shows step 1 again |
| A double tap | two taps 190 ms apart answer one step only; the guard also swallows a double Skip and a tap on "Turn on" |
| The shortcut on a fresh install | attack mode at once, no onboarding; the next start finds the attack and finishes onboarding silently |
| Restore from a backup | step 1's link opens Your data, the import restores 16 migraines, Back returns to step 1, and the next start finishes it |
| The reminder offer | Turn on asks for permission; refused, home opens and Reminders shows the notice; allowed, the daily reminder is on at 8:00 PM with its job |
| Track periods | off hides the chip and an unmarked day's switch; a marked day keeps its switch on; today marked brings the chip back; on restores both |
| Accessibility | nothing clipped at 200% font, steps scroll in landscape, the Settings switch is one node, titles are headings |

**Changed after the walk:** with Track periods off, a toggle once shown now stays for the visit (home) or while the
day is chosen (History), so a mark removed by mistake can be put back.

**Accepted:** the calendar legend keeps its Period key with tracking off, since saved marks still show.

**For QA:** "Turn on at 8:00 PM" has a narrow no-break space (U+202F) before "PM", as the system time format writes it.

## Review (6.3)

An independent `superpowers:code-reviewer` pass found nothing critical. It confirmed:
- no path shows onboarding to a phone with records, and none traps the user;
- the splash and its decision, the saved start, the shortcut while onboarding shows, and Back;
- the offer's permission flow, Track periods with saved marks kept, the preferences threading, the layering and the
  module boundaries.

| Finding | Outcome |
|---|---|
| **I1** a double tap answered the next step, which has its choices in the same places | fixed: a tap guard covers each new step for 800 ms |
| **I2** a recreation during the splash dropped the shortcut | fixed: the shortcut counts until the start is decided; one arriving during the decision is kept |
| **I3** ARCHITECTURE was not updated | fixed: the onboarding, settings and app start rows, and the offer slot's deviation |
| **I4** the offer read presentation models in the UI | fixed: its own UI mappers, and the time format shared with the Reminders screen |
| Minor | fixed: a 2 s fallback to home for the decision, the daily reminder's "Log a migraine" skips onboarding, History's rule in its presentation mapper, the question announced as the step changes, dead `onException` handlers, copy, `PreferencesDataSourceModule`, design D1 and D3, and tests (History's rule, home's `periodOffered`, `HistoryRepository.periodTracking`, the offer's failed read and save, its UI mappers, the settings mapper). Accepted: onboarding goes on after a restore (the next start finishes it), the message timing as on Reminders, the defaults on the new model fields |
