# Design

BRD D176 (#213) and D175 (#197). Code read on develop 74095b8d. Evidence for #213: `migrely-work/walks/r2/`.

## Context

The daily reminder asks "How was today?" so the diary has no silent gaps. Its moment is the evening, often tired, sometimes
in pain. Two things go wrong today:

1. **Lost.** A Doze-held run for today is cancelled by any process start before it runs (#213).
2. **Noise, if we only fix 1.** In #213's main path the person opens the app at 8:01 PM. If start-up simply kept the run, the
   run would come seconds later, while they are on home, or while they are logging an attack, with a sound.

### Who plans the daily run, before and after

| Path | Class | 74095b8d | After |
|---|---|---|---|
| Every process start | `MigrelyApplication.onCreate` → `RescheduleRemindersUseCase` | REPLACE, `next()` | **KEEP**, `due()` |
| Clock or zone change | `TimeChangeReceiver` → `RescheduleRemindersUseCase` | REPLACE, `next()` | REPLACE, **`due()`** |
| Time set, turned on | `SetDailyReminderUseCase` | REPLACE, `next()` | unchanged |
| Turned off; start while off | `SetDailyReminderUseCase`, `RescheduleRemindersUseCase` | cancel | unchanged |
| Each daily run | `ReminderWorker` → `RunDailyReminderUseCase` | REPLACE, `after(date)` | unchanged, plus the on-screen rule |
| Run comes while Migrely is on screen | `RunDailyReminderUseCase` | posts | **REPLACE**, same date, now + 30 min |
| Reboot | WorkManager's `RescheduleReceiver` restores runs, the start runs `onCreate` | REPLACE at start | KEEP at start |
| Import | `planAgain` → `PlanRemindersAfterImportUseCase` | visit only | unchanged |
| Visit reminder, "Did it help?" | `VisitReminderPlan`, `LogIntakeUseCase` | REPLACE | unchanged |

Every path that must still REPLACE: the time setting and turning on (the old run is at the wrong time), a clock or zone
change (the run's instant must move to the new local time), a run planning its own next run (WorkManager marks the
running work cancelled but never interrupts `doWork`), and a run postponing itself because Migrely is on screen (D3).
Every other caller of the daily plan is the start-up path.

## Decisions

### D1. Start-up uses KEEP (issue option a)

`ExistingWorkPolicy.KEEP` leaves a unique "daily" work alone while it is ENQUEUED, RUNNING or BLOCKED, and enqueues when
there is none or it has finished. So:

- a Doze-held run survives the start (#213);
- the R2 race disappears: KEEP never cancels, whichever of the job and `onCreate` comes first;
- #197's lost database still gets a plan, because there is nothing to keep;
- a run that ended `FAILED` (no next planned) is replanned at the next start, as before.

KEEP is atomic inside WorkManager, so the domain does not need to ask "is one planned?" first. The domain gets a new
repository, for example `PlanDailyReminderIfNoneRepository.planDailyReminderIfNone(at, date)`, and the data source a
matching `scheduleDailyIfNone` with `ExistingWorkPolicy.KEEP`. The existing `scheduleDailyReminder` stays REPLACE.

### D2. A target that does not skip today (issue option b, where it belongs)

New `DailyReminderTime.due(now, time, handledOn)`:

1. today's `time` if it is still ahead: today;
2. else, if `handledOn != today`: now, for today;
3. else tomorrow's `time`.

Used by `RescheduleRemindersUseCase` for both callers: with KEEP at app start (so it only matters when nothing is planned),
with REPLACE at a clock or zone change. Option (b) alone at every start would rewrite a held run to "now" on each start and
make every after-the-time open run a job at once. Option (a) alone leaves the clock change, which must REPLACE, dropping
today; Android also sends `TIME_SET` for network time corrections. Both together cover every start.

`handledOn` is `ReminderSettingsDomainModel.dailyLastShownOn`, unchanged in storage and meaning: the date the daily
reminder last showed. An on-screen skip does not set it (D3).

### D3. No daily notification while Migrely is on screen: postpone 30 minutes

ux-designer's rule, accepted by team-lead (task 1.1), word for word in the spec, EC-REM-27 and D176 (3):

> A daily run that comes while Migrely is on screen with the screen on, including while it is opening, SHALL NOT post and
> SHALL NOT mark the date as handled. It SHALL plan that date's run again 30 minutes later, replacing the planned run. That
> run follows the normal rules: no post if the date is answered, notifications are off, or the date is no longer today; no
> post if Migrely is on screen, which postpones it again. Otherwise it posts once. With the screen off or locked, or
> Migrely in the background, it posts as before. At most one daily reminder SHALL post for a date.

In `RunDailyReminderUseCase`, after the existing checks (date is today, notifications allowed, not already shown, not
answered): if Migrely is on screen, plan the same date again at now + 30 minutes through `ScheduleDailyReminderRepository`
(REPLACE), do not call `setDailyShown`, and return `Skipped`. This replaces the next run the use case planned at its top;
the postponed run plans the next day again when it runs. A postponed run that comes after midnight does not post and plans
the next day (existing rule). A start keeps it (KEEP, D1).

Why not mark the date: a person who opens the app, browses History and leaves without logging would lose the day's
reminder, which is worse than 74095b8d. Why 30 minutes: long enough not to post while they are still in the app, short
enough to stay in the evening. A clock change after a skip finds the date not shown and runs at once (D2); the same
on-screen rule then applies.

"On screen" must be true from the instant a tap starts the process, before any activity resumes: the start-up plan and a
held job can run in that first second. `CurrentActivityHolder` is empty until `onResume`, so it races. Use the process's
importance: `ActivityManager.getMyMemoryState(info)` and `info.importance == IMPORTANCE_FOREGROUND`. The system sets it for a
process started for a top activity before the app's code runs; a dark or locked screen reports a lower importance. A new
data source in `app` (for example `AppOnScreenDataSource`) behind a domain repository (for example
`GetAppOnScreenRepository.appOnScreen()`). The engineer proves both claims on device (task 2.9); if either is false, stop and
send it to the product-manager.

Measured in 2.9 (emulator-5570, API 36, 909561bd, 3/3): in #213's path the tap at 8:01 PM starts the process, and
WorkManager's in-process scheduler runs the kept, overdue work about 1 s later, while Migrely is opening (importance 100).
D3 skips it, leaves the date unmarked and plans it at about 8:31 PM. After the person left (HOME), it posted once at
8:31–8:36 PM and marked the date. So the person sees the reminder at the postponed run, about 30 minutes after the
skipped run (at most about 30 minutes after leaving), never at the moment they leave. 74095b8d lost the run (0/3).

### D4. No new cut-off

The question is about today and stays answerable until midnight; the existing rule (a run for a date that is not today
does not post) remains. The late posts this change adds come at a start that mostly means the phone is in use: a reboot, a
clock change, leaving the app. A run that Doze holds for hours already posted late before this change.

### D5. One change for both issues

#197 and #213 change the same call in `onCreate`, the same use case and the same QA scripts, and D175's premise depends on
D176. Two changes would conflict in the spec and in `MigrelyApplication.kt`. The change is renamed to
`keep-late-daily-reminder`. The branch keeps its name `bugfix/197-replan-after-update` (pushed, with a worktree; a rename buys
nothing). Commits and the PR write `Refs #197` and `Refs #213`.

### D6. D175 restated

No receiver, still. Reasons (1) no data lost, (2) recovery without opening the app, and (3) almost nobody helped all hold.
Reason (4) changes: the REPLACE displacement it feared was real (#213) and D176 removes it; the receiver is still not added
because the daily reminder now comes back the same day on its own, and a receiver adds a process start at every update for
everyone and an update path to every Go/No-Go.

## Edge cases

| Case | 74095b8d | After |
|---|---|---|
| Doze-held run, app opened after the time (EC-REM-24) | today lost | kept; runs about 1 s after the tap, on screen: postponed 30 min; left the app: posts once at the postponed run (about 8:31 PM) |
| Doze-held run, another Migrely job or a reboot starts the process | today lost | kept; posts once when it runs, if still today |
| Job cold-starts the process (R2) | posted 10/10, 1–7 ms margin | posts; no race (KEEP) |
| Clock or zone change after the time, today not shown (EC-REM-25) | today lost | runs at once for today |
| Clock change after today was shown | next is tomorrow | unchanged |
| Clock moved back below the time after a post | one post | one post (`dailyLastShownOn`) |
| Clock moved back to yesterday | tomorrow's time | asks about the phone's "today" once |
| Zone change east past midnight | asks about the new date | unchanged: the old date has ended |
| DST gap or overlap | `at()` moves forward | unchanged |
| Nothing planned at start, after the time (EC-REM-26): #197, restore, failed chain | tomorrow | at once for today, unless shown |
| Restore, first open after the time | tomorrow | the run comes while on screen: postponed; posts 30 min later if they left unanswered |
| Run while Migrely on screen, any time (EC-REM-27) | posts in the app | no post, not marked, postponed 30 min (REPLACE) |
| Still on screen at the postponed run | posts in the app | postponed again |
| Opened, browsed History, left without logging | posts in the app | posts once about 30 min after the skip |
| Postponed run after midnight | n/a | no post, next day planned |
| Clock change after an on-screen skip | n/a | runs at once (not shown); on screen: postponed again |
| Lock screen right after the power key, Migrely behind | posts | posts (probe 2.9, 4.3b) |
| Split screen with Migrely visible; shade pulled over Migrely | posts | counts as on screen: postponed (probe 2.9, 4.3b) |
| Screen locked or off with Migrely on top | posts | posts |
| Time set today (EC-REM-28) | strictly after now | unchanged |
| Reminder turned on after the time, then a clock change | nothing today | may post today at once (accepted) |
| Reminder off | cancelled at start | unchanged |
| Today answered (pain-free, attack) | no post | unchanged |
| Notifications off | no post | unchanged, a catch-up run just skips |
| Midnight between plan and run | run skips, plans next | unchanged |
| Two runs for one date (clock change during a run, a postponed run after a post) | guarded by `dailyLastShownOn` | same guard; same notification id |
| Upgrade with a run planned by any shipped version | replaced at first start | kept; old runs without a date ask about the run's day |
| Process death while planning | next start plans | next start keeps or plans |
| Visit reminder (D168), "Did it help?", import | unchanged | unchanged |
| Free and Premium | same | same |
| API 26 and API 36 | same code | same code; checked on both |

## Risks

- **R1. Importance at cold start.** D3 relies on `IMPORTANCE_FOREGROUND` being set before the app's code runs. Checked on
  device by the engineer (2.9) and QA (4.3a, 4.3b), including the lock screen, split screen and notification shade. If false, the fallback is not ours to pick in code review: back to the
  product-manager.
- **R2. Store review.** A reviewer who sets the time a minute ahead and waits inside the app sees nothing then. It posts
  within about 30 minutes after they leave the app or lock the screen. Accepted.
- **R5. Postpone loop.** A person who keeps Migrely on screen for hours gets a re-plan every 30 minutes until midnight: one
  job each, no post. Harmless.
- **R3. A catch-up run at a clock change** posts at once on a day the reminder was turned on after its time. Rare, harmless.
- **R4. Evidence is from emulators** with forced idle. Real Doze windows and vendor job batching are not covered.
