# Design

BRD D175, issue #197, D170 (3b). Code read on develop 74095b8d.

## Context

The person affected is on Android 8.0, cleared Migrely's storage on an old build, opened it again, and later got an update while
not using the phone. They do not know anything happened. What they would miss is one evening's "How was today?" and possibly a
"Did it help?" nudge. Their diary and their open questions are intact.

### What plans reminders today

| Path | Class | What it plans |
|---|---|---|
| Every process start (activity, receiver, job, provider) | `MigrelyApplication.onCreate` → `RescheduleRemindersUseCase` | daily (next time strictly after now) and visit (`VisitReminderPlan`: a passed time on the visit day runs at once) |
| Clock or zone change | `TimeChangeReceiver` (manifest, `exported="false"`) → the same use case | the same |
| Reboot | WorkManager's merged `RescheduleReceiver` (`RECEIVE_BOOT_COMPLETED`) starts the process, and `onCreate` runs | the same |
| Each daily run | `ReminderWorker` → `RunDailyReminderUseCase` | the next daily run |
| An attack-medication intake | `LogIntakeUseCase` | one "Did it help?" run at intake + 2 h, only if still ahead |
| After an import | `ReminderScheduleDataSourceImpl.planAgain` → `PlanRemindersAfterImportUseCase` | the visit reminder |

`ReminderScheduleDataSourceImpl` makes every kind unique work with `ExistingWorkPolicy.REPLACE`. The open "Did it help?" question
is not WorkManager state. It is in Room: an acute intake with no effect answer, not closed. `GetMedicationPromptUseCase` shows it
on home from 2 to 24 hours after the intake. `RunEffectQuestionUseCase` only decides whether to post the notification.

### What the residual removes

installd empties `no_backup/` at the update, so `androidx.work.workdb` is gone. JobScheduler's jobs survive the update. The first
stale job that comes due starts the process: `onCreate` runs, then WorkManager logs "Didn't find WorkSpec" (QA row
`26-timed-ctl-1`). The re-plan in `onCreate` therefore runs at the first stale job's time, with no user action.

## Decisions

### D1. No code fix (BRD D175)

Weighed in the lead's order:

1. **Data loss: none.** Room holds the diary and the questions. The issue's "lost for good" is true of the notification only.
2. **Reminders that must fire.** Visit reminder: recovered on its own day (its stale job starts the process, the plan sees a
   passed time and runs at once). Daily reminder: the run whose stale job starts the process is lost, because
   `DailyReminderTime.next` then gives tomorrow; tomorrow's posts. "Did it help?": the notification is lost only for intakes in
   the 2 hours before the update; the card still asks.
3. **Complexity and audience.** 1.2.1's first process start corrects the mark (D170 (3a)). A fix shipped after 1.2.1 helps only
   people whose first update after an old clear skips 1.2.1, and that group only shrinks.

### D2. Why not `MY_PACKAGE_REPLACED`

The old objection (D170 (3b)) holds, now checked in code. A manifest receiver for it starts the process at every update for every
user. `onCreate` then re-plans with REPLACE and `DailyReminderTime.next`, so a daily run that is past its time but held by Doze is
replaced by tomorrow's. Using KEEP inside the receiver does not help, because `onCreate` runs first and uses REPLACE. Gain for a
near-empty group, regression risk for all, and an update path in every future Go/No-Go.

For the record, if this is ever revisited: `MY_PACKAGE_REPLACED` needs no permission; the system sends it only to the replaced
package, so a receiver with `exported="false"` gets it, and API 26's implicit-broadcast limit does not apply. No
`RECEIVE_BOOT_COMPLETED`-style handling is needed. R2 has to be solved first.

### D3. Why not rebuild "Did it help?" runs from Room

Room has no "notification sent" mark. Re-planning open questions at each start, the normal path for everyone, would post again a
question that already posted and is still unanswered. Preventing that needs a column and a Room migration, for a nudge whose
question is on home anyway. KEEP plus WorkManager's own finished rows is not a substitute: WorkManager prunes finished work after
about a day, and the data layer should not depend on that.

### D4. Pin the premise in the spec and the code

D1 depends on `onCreate` re-planning at every process start. Moving it into `MainActivity`, or behind "only when the user opens
the app", would silently turn the one-day gap into "until the user opens the app". So the `reminders` spec gets the requirement,
and `MigrelyApplication` gets a comment that names D175 and EC-REM-22 next to the call.

## Edge cases

| Case | Outcome |
|---|---|
| API 26 residual | Covered above. QA proves it (tasks 3.x) |
| API 27–36 | Android 16: no residual (D170 evidence). 8.1–15: unknown; same code path, no version branch |
| Normal update, nothing cleared | Nothing changes: no receiver, no extra start, no double post |
| Daily reminder off | `RescheduleRemindersUseCase` cancels; nothing posts |
| Free and Premium | Reminders and the card are not Premium; the same. The Premium periodic check is re-planned by the next `PremiumChecker.check()`; Premium stays as kept meanwhile (D166) |
| Doctor-visit reminder (D168) | Recovered on its day; already reminded (`visitRemindedFor`) stays unposted |
| Question window passed in the gap | No card after 24 h, as EC-MED-05 already says |
| Clock or zone change in the gap | `TimeChangeReceiver` starts the process, re-plans earlier; `dailyLastShownOn` stops a second post for a date |
| Reboot in the gap | WorkManager's boot receiver starts the process; `onCreate` re-plans (EC-REM-04) |
| Process death mid-plan | The next start plans again; unique work with REPLACE keeps one run per kind |
| ExistingWorkPolicy | Unchanged: REPLACE everywhere for reminders, KEEP for the Premium check |

## Risks

- **R1. The premise is read from code.** QA has not yet seen a new daily run planned after a stale job's start. If QA finds no
  re-plan, D175 reopens with a new D-number and this change gets a code task.
- **R2. Suspicion, not filed.** Any process start between a daily run's time and its actual run, including the start made by
  that run's own job, re-plans with REPLACE for tomorrow. If the REPLACE lands before WorkManager marks the run as running,
  today's reminder is dropped. QA's controls so far show the reminder posting, so the run usually wins. It is unproven under load.
  QA's 3.4 control runs it 5 times. Only a reproduced drop becomes an issue.
- **R3.** Evidence comes from `adb install -r` on emulators. A Play update and vendor installd are not covered.
